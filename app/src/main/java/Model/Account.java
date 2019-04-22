package Model;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.nogam.ukar.Login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Account {
    private String username;
    private String password;
    private String phonenumber;
    private String name;
    private AccountListener listener;
    private String role;

    public Account(String username, String password, AccountListener listener) {
        this.username = username;
        this.password = password;
        this.listener = listener;
    }

    public Account(String username, String password, String phonenumber, String name, String role , AccountListener listener) {
        this.username = username;
        this.password = password;
        this.phonenumber = phonenumber;
        this.name = name;
        this.listener = listener;
        this.role = role;
    }

    public void Login() {
        listener.onLoginStart();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject.length() > 0) {
            new LoginAsync().execute(jsonObject.toString());
        }
    }
    public void Register(){
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("fullname", name);
            jsonObject.put("email", username);
            jsonObject.put("PhoneNumber", phonenumber);
            jsonObject.put("password", password);
            jsonObject.put("role", role);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject.length() > 0) {
            new RegisterAsync().execute(jsonObject.toString());
        }
    }
    class RegisterAsync extends AsyncTask<String , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(String... strings) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = strings[0];
            String res;
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/register")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("AAA", res);
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("AAA", s);
            try {
                parseJSon(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private void parseJSon(String data) throws JSONException {
            if (data == null)
                return;
            JSONObject jsonData = new JSONObject(data);
            JSONObject jsondata = jsonData.getJSONObject("data");
            listener.onRegisterSuccess(jsondata);
        }
    }
    class LoginAsync extends AsyncTask<String, Void, String> {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        String rescookie;
        @Override
        protected String doInBackground(String... strings) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = strings[0];
            String res;
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/login")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                rescookie = response.headers("Set-Cookie").get(0).replace("; path=/; secure; samesite=lax; httponly", "");
                Log.d("AAA", res);
                Log.d("AAA", rescookie);
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d("AAA", s);
            try {
                parseJSon(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        private void parseJSon(String data) throws JSONException {
            if (data == null)
                return;
            JSONObject jsonData = new JSONObject(data);
            JSONObject jsondata = jsonData.getJSONObject("data");
            listener.onLoginSuccess(jsondata , rescookie);
        }
    }
}
