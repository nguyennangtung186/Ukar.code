package com.example.nogam.ukar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterCar extends AppCompatActivity {
    ImageView img_anhxe , img_anhdangky;
    Button btn_accept;
    EditText edt_brand , edt_color , edt_plateNumber , edt_registerDate;
    String URL = "https://ukarbetezminor.azurewebsites.net/car/register";
    boolean selectedCar;
    String cookie;
    private String CarImageFileType , RegistrationImageFileType;
    SharedPreferences sharedPreferences;
    boolean chooseCar = false;
    boolean chooseRegisterCar = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_car);
        sharedPreferences = getSharedPreferences("Ukar", Context.MODE_PRIVATE);
        cookie = sharedPreferences.getString("Cookie", "");
        findById();
        img_anhxe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                selectedCar = true;
                startActivityForResult(photoPickerIntent, 1);
            }

        });
        img_anhdangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                selectedCar = false;
                startActivityForResult(photoPickerIntent, 1);
            }
        });
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String brand = edt_brand.getText().toString().trim();
                String color = edt_color.getText().toString().trim();
                String plateNumber = edt_plateNumber.getText().toString().trim();
                if (!brand.isEmpty() && !color.isEmpty() && !plateNumber.isEmpty() && chooseCar && chooseRegisterCar){
                    img_anhxe.buildDrawingCache();
                    String code_anhxe = encodeBase64(img_anhxe.getDrawingCache() , CarImageFileType);
                    img_anhdangky.buildDrawingCache();
                    String code_anhdangky = encodeBase64(img_anhdangky.getDrawingCache() , RegistrationImageFileType);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("brand" , brand);
                        jsonObject.put("color" , color);
                        jsonObject.put("PlateNumber" , plateNumber);
                        jsonObject.put("CarImage" , code_anhxe);
                        jsonObject.put("CarImageFileType" , CarImageFileType);
                        jsonObject.put("RegistrationDate", "2012-03-19T07:22Z");
                        jsonObject.put("RegistrationImage" , code_anhdangky);
                        jsonObject.put("RegistrationImageFileType" , RegistrationImageFileType);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jsonObject.length() > 0 ){
                        new RegisterCarAsync().execute(jsonObject.toString());
                    }
                }
                else {
                    Toast.makeText(RegisterCar.this, "Xin điền đủ thông tin", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private String encodeBase64(Bitmap bitmap , String type){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        if (type.equals("jpeg")){
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        }
        else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        }
        byte[] image=stream.toByteArray();
        System.out.println("byte array:"+image);
        String code = Base64.encodeToString(image, 0);
        Log.d("code", code);
        return code;
    }
    private String getMimeType(Context context, Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType.substring(6);
    }
    class RegisterCarAsync extends AsyncTask<String , Void, String > {
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
                    .header("Cookie", cookie)
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("AAA" , res);
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
            String jsondata = jsonData.getString("errorMessage");
            if (jsondata == "Success"){
                Intent intent = new Intent(RegisterCar.this, MapsActivity.class);
                RegisterCar.this.startActivity(intent);
            }
        }
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
                if (selectedCar){
                    chooseCar = true;
                    Picasso.get().load(imageUri.toString()).fit().centerCrop().into(img_anhxe);
                    CarImageFileType = getMimeType(RegisterCar.this, imageUri);
                }
                else {
                    chooseRegisterCar = true;
                    Picasso.get().load(imageUri.toString()).fit().centerCrop().into(img_anhdangky);
                    RegistrationImageFileType = getMimeType(RegisterCar.this, imageUri);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }else {
            Toast.makeText(RegisterCar.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }
    private void findById() {
        img_anhdangky = findViewById(R.id.img_dangkyxe);
        img_anhxe = findViewById(R.id.img_anhxe);
        btn_accept = findViewById(R.id.btn_accept);
        edt_brand = findViewById(R.id.brand);
        edt_color = findViewById(R.id.color);
        edt_plateNumber = findViewById(R.id.PlateNumber);
        edt_registerDate = findViewById(R.id.registerDate);
    }
}
