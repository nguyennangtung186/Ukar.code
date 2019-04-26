package com.example.nogam.ukar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FlashActivity extends AppCompatActivity {
    String URL = "https://ukarbetezminor.azurewebsites.net/login";
    SharedPreferences sharedPreferences;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_flash);
        sharedPreferences = getSharedPreferences("Ukar", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("Username", "");
        String password = sharedPreferences.getString("Password" , "");
        Log.d("abc" , username);
        if (username.length() > 0){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", username);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new LoginAsync().execute(jsonObject.toString());
        }
        else {
            Intent intent = new Intent(FlashActivity.this , Login.class);
            FlashActivity.this.startActivity(intent);
        }
    }
    class LoginAsync extends AsyncTask<String , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        @Override
        protected String doInBackground(String... strings) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = strings[0];
            String res ;
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(URL)
                    .header("Content-Type" , "application/json")
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                cookie =  response.headers("Set-Cookie").get(0).replace("; path=/; secure; samesite=lax; httponly" , "");
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
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
            boolean jsonReults = jsondata.optBoolean("succeeded");
            Log.d("AAA", String.valueOf(jsonReults));
            if (jsonReults){
                sharedPreferences = getSharedPreferences("Ukar" ,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Cookie", cookie);
                editor.commit();
                new RoleAsync().execute();
            }
            else {
                Toast.makeText(FlashActivity.this, "Mật khẩu của bạn đã thay đổi", Toast.LENGTH_SHORT).show();
                sharedPreferences = getSharedPreferences("Ukar" ,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("Cookie");
                editor.remove("Username");
                editor.remove("Password");
                editor.commit();
                Intent intent = new Intent(FlashActivity.this , Login.class);
                FlashActivity.this.startActivity(intent);
            }
        }
    }
    class RoleAsync extends AsyncTask<Void , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        @Override
        protected String doInBackground(Void... voids) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String res;
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/role")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                parseJSon(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void parseJSon(final String data) throws JSONException {
            if (data == null)
                return;
            JSONObject jsonData = new JSONObject(data);
            String role = jsonData.getString("data");
            if (role.equals("Driver")){
                Intent intent = new Intent(FlashActivity.this , MapDriverActivity.class);
                FlashActivity.this.startActivity(intent);
                Log.d("role" ,"Driver");
            }
            else {
                Intent intent = new Intent(FlashActivity.this , MapsActivity.class);
                FlashActivity.this.startActivity(intent);
                Log.d("role" ,"Employer");
            }
        }
    }
}
