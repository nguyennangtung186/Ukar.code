package com.example.nogam.ukar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.Account;
import Model.AccountListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register extends AppCompatActivity implements AccountListener {
    TextInputLayout txt_fullname;
    TextInputLayout txt_phoneNumber;
    TextInputLayout txt_password;
    TextInputLayout txt_email;
    Button btn_signUp;
    TextView txt_link;
    Spinner spinner;
    String role ;
    Boolean selected = false;
    private static final String URL = "https://ukarbetezminor.azurewebsites.net/register";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_register);
        findById();
        hideKeyboard(Register.this);
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(Register.this);
                String Email = txt_email.getEditText().getText().toString().trim();
                String Name = txt_fullname.getEditText().getText().toString().trim();
                String PhoneNumber = txt_phoneNumber.getEditText().getText().toString().trim();
                String Password = txt_password.getEditText().getText().toString().trim();
                JSONObject jsonObject = new JSONObject();
                if (!Email.isEmpty() && !Name.isEmpty() && !PhoneNumber.isEmpty() && !Password.isEmpty() && selected){
                    sendRequest(Email , Password , PhoneNumber , Name ,role);
                }
                else {
                    Toast.makeText(Register.this, "Xin điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }

            }
        });
        txt_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                Register.this.startActivity(intent);
            }
        });

        String[] roles = new String[]{
                "Bạn cần gì",
                "Công việc",
                "Tìm tài xế"
        };
        final List<String> rolesList = new ArrayList<>(Arrays.asList(roles));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.spinner_item, rolesList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    if (selectedItemText == "Công việc") {
                        role = "Driver";
                    }
                    else {
                        role = "Employer";
                    }
                    selected = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    void sendRequest(String username, String password, String phonenumber, String name, String role ){
        new Account( username,  password,  phonenumber,  name,  role , this).Register();
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

    }

    @Override
    public void onLoginSuccess(JSONObject jsonObject, String cookie) {

    }

    @Override
    public void onRegisterSuccess(JSONObject jsonObject) {
        boolean jsonReults = jsonObject.optBoolean("succeeded");
        if (jsonReults){
            Toast.makeText(Register.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Register.this, Login.class);
            Register.this.startActivity(intent);
        }
        else Toast.makeText(Register.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
    }


    private void findById() {
        txt_email = findViewById(R.id.input_email);
        txt_fullname = findViewById(R.id.input_name);
        txt_password = findViewById(R.id.input_password);
        txt_phoneNumber = findViewById(R.id.input_phoneNumber);
        btn_signUp = findViewById(R.id.btn_signup);
        txt_link = findViewById(R.id.link_login);
        spinner = findViewById(R.id.spinner1);
    }
}
