package com.example.nogam.ukar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import Model.Account;
import Model.AccountListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity implements AccountListener {
    TextInputLayout inputEmail;
    TextInputLayout inputPassword;
    Button btn_login;
    String rescookie;
    TextView txt_register;
    private final static String URL = "https://ukarbetezminor.azurewebsites.net/login";
    private ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
        }

        setContentView(R.layout.activity_login);
        findById();
        hideKeyboard(Login.this);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(Login.this);
                String Email = inputEmail.getEditText().getText().toString().trim();
                String Password = inputPassword.getEditText().getText().toString().trim();
                if (!Email.isEmpty() && !Password.isEmpty()) {
                    sendRequest(Email, Password);
                } else {
                    Toast.makeText(Login.this, "Xin điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }

            }
        });
        txt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class);
                Login.this.startActivity(intent);
            }
        });
    }

    void sendRequest(String email, String password) {
        new Account(email, password, this).Login();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onLoginStart() {
        progressDialog = ProgressDialog.show(Login.this, "Please wait.",
                "Loging ..!", true);
    }

    @Override
    public void onLoginSuccess(JSONObject jsonObject, String cookie) {
        boolean jsonReults = jsonObject.optBoolean("succeeded");
        rescookie = cookie;
        Log.d("AAA", String.valueOf(jsonReults));
        if (jsonReults) {
            Toast.makeText(Login.this, "Login", Toast.LENGTH_SHORT).show();
            sharedPreferences = getSharedPreferences("Ukar", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Cookie", rescookie);
            editor.putString("Username", inputEmail.getEditText().getText().toString().trim());
            editor.putString("Password", inputPassword.getEditText().getText().toString().trim());
            editor.commit();
            new RoleAsync().execute();
        } else {
            Toast.makeText(Login.this, "Mật khẩu hoặc tài khoản không đúng", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    public void onRegisterSuccess(JSONObject jsonObject) {

    }
    class RoleAsync extends AsyncTask<Void, Void, String> {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(Void... voids) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String res;
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/role")
                    .header("Content-Type", "application/json")
                    .header("Cookie", rescookie)
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

        private void parseJSon(final String data) throws JSONException {
            if (data == null)
                return;
            JSONObject jsonData = new JSONObject(data);
            String role = jsonData.getString("data");
            progressDialog.dismiss();
            if (role.equals("Driver")) {
                Intent intent = new Intent(Login.this, MapDriverActivity.class);
                intent.putExtra("Cookie", rescookie);
                Login.this.startActivity(intent);
                Log.d("role", "Driver");
            } else {
                Intent intent = new Intent(Login.this, MapsActivity.class);
                intent.putExtra("Cookie", rescookie);
                Login.this.startActivity(intent);
                Log.d("role", "Employer");
            }
        }
    }

    private void findById() {
        inputEmail = findViewById(R.id.input_email_login);
        inputPassword = findViewById(R.id.input_password_login);
        btn_login = findViewById(R.id.btn_login);
        txt_register = findViewById(R.id.link_signup);
    }
}

