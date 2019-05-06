package com.example.nogam.ukar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.Info;
import Model.Trip;
import Model.TripListener;
import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        DirectionFinderListener,
        NavigationView.OnNavigationItemSelectedListener,
        TripListener {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PERMISSION = "permission";

    boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private CameraPosition mCameraPosition;
    private static final float DEFAULT_ZOOM = 15;

    private Location mLastKnownLocation;
    private String YOUR_API_KEY = "AIzaSyAPl2SgacgnnYAMWFqo0zJde_MBbHmbECU";

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    View headerView;
    NavigationView navigationView;

    DrawerLayout drawerLayout;
    ImageView buttonOpenMenu;
    ImageView buttonGps , btn_partner , btn_close;
    Button btn_findDriver , btn_cancelTrip , btn_onTrip;
    TextView diem_den , gia , distance;
    RelativeLayout relativeLayout;
    LinearLayout linearLayout;
    LatLng endLocation , sPartnerLocation;
    String diemden;
    Marker partnerMarker;

    private AutoCompleteTextView mSearchText;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    String cookie , fullName;
    private String tripType = "Round";
    SharedPreferences sharedPreferences;
    private boolean firstRun , first , hasDirection;
    Info partnerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.d("AAA", "onCreate");
        firstRun = true;
        first = true;
        hasDirection = false;
//        if (savedInstanceState != null) {
//            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
//            Log.d("AAA", "Instance");
//        }
        sharedPreferences = getSharedPreferences("Ukar", Context.MODE_PRIVATE);
        cookie = sharedPreferences.getString("Cookie", "");
        Log.d("Cookie" , cookie);
        new Userinfo().execute();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Places.initialize(getApplicationContext(), YOUR_API_KEY);

        // Create a new Places client instance.

        PlacesClient placesClient = Places.createClient(this);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), YOUR_API_KEY);
        }
        findById();
        init();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void findById() {
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.main_drawer_layout);
        buttonOpenMenu = findViewById(R.id.button_open_header);
        mSearchText = findViewById(R.id.input_search);
        buttonGps = findViewById(R.id.gps);
        btn_findDriver = findViewById(R.id.find_driver);
        diem_den = findViewById(R.id.diem_den);
        gia = findViewById(R.id.gia);
        distance = findViewById(R.id.distance);
        relativeLayout = findViewById(R.id.relLayout);
        linearLayout = findViewById(R.id.liLayout);
        btn_cancelTrip = findViewById(R.id.cancel_trip);
        btn_onTrip = findViewById(R.id.on_trip);
        btn_partner = findViewById(R.id.partner);
        btn_close = findViewById(R.id.close);
    }

    private void init() {

        buttonGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setCountry("VN")
                        .build(getApplicationContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });
        diem_den.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .setCountry("VN")
                        .build(getApplicationContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripType = "Round";
                hasDirection = false;
                removeTrip();
            }
        });
        btn_findDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
                builder1.setTitle("Tìm chuyến");
                builder1.setMessage("Bạn có chắc chắn muốn tìm tài xế không?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Đồng Ý",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d("TripType" , tripType);
                                if (tripType.equals("OneWay")){
                                    findRequest(tripType , endLocation.latitude , endLocation.longitude , cookie);
                                }
                                else {
                                    findRequest(tripType , cookie);
                                }
                                dialog.dismiss();
                            }
                        });

                builder1.setNegativeButton(
                        "Hủy bỏ",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
        btn_cancelTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
                builder1.setTitle("Hủy chuyến");
                builder1.setMessage("Bạn có chắc chắn muốn hủy chuyến?");
                builder1.setCancelable(true);
                builder1.setPositiveButton(
                        "Đồng Ý",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cancelRequest(cookie);
                                dialog.dismiss();
                            }
                        });
                builder1.setNegativeButton(
                        "Hủy bỏ",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });
        btn_partner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInfo();
            }
        });
    }
    private void DialogInfo() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.info_partner, null);
        TextView txt_name = alertLayout.findViewById(R.id.name_person);
        TextView txt_phone = alertLayout.findViewById(R.id.phone_person);
        Button btn_call = alertLayout.findViewById(R.id.call);
        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBtnClick();
                dialogBuilder.dismiss();
            }
        });
        txt_name.setText("Họ và tên: " + partnerInfo.getUserName().toString());
        txt_phone.setText("Số điện thoại: "+ partnerInfo.getPhoneNumber().toString());
        dialogBuilder.setView(alertLayout);
        dialogBuilder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                diemden = place.getName();
                sendRequest(place.getName());

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {


            case R.id.change_password: {
                Toast.makeText(this, getString(R.string.change_password), Toast.LENGTH_LONG).show();
                onBackPressed();
                break;
            }

            case R.id.logout: {
                Toast.makeText(this, getString(R.string.logout), Toast.LENGTH_LONG).show();
                sharedPreferences = getSharedPreferences("Ukar" ,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("Cookie");
                editor.remove("Username");
                editor.remove("Password");
                editor.commit();
                stopRepeatingTask();
                Intent intent = new Intent(MapsActivity.this , Login.class);
                MapsActivity.this.startActivity(intent);

                break;
            }
        }

        return true;

    }

    private void sendRequest(String destination) {
        String origin = mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude();
        try {
            new DirectionFinder(this, origin, destination).execute();
            Log.d("AAA", "send requested");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        relativeLayout.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        Log.d("AAA", "Tìm đường");
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            Log.d("AAA", "Time: " + route.duration.text);
            Log.d("AAA", "Distance: " + route.distance.text);
            Log.d("AAA", "Diemden: " + diemden);
            diem_den.setText(diemden);
            distance.setText(route.distance.text);
            gia.setText("40000 đồng");
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .position(route.endLocation)));
            endLocation = route.endLocation;
            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);
            hasDirection = true;
            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
            tripType = "OneWay";
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            outState.putBoolean(KEY_PERMISSION, mLocationPermissionGranted);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("AAA", "Map ready");
        mMap = googleMap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getLocationPermission();
            Log.d("AAA", "get Permission");
        }
    }



    private final static int INTERVAL = 1000 * 5 ; //2 minutes
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Task locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(MapsActivity.this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = (Location) task.getResult();
                        Log.d("AAA" , mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                    } else {
                        Log.d("AAA", "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());

                    }
                }
            });
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Latitude", mLastKnownLocation.getLatitude());
                jsonObject.put("Longitude", mLastKnownLocation.getLongitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new Locations().execute(jsonObject.toString());
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };
    Runnable CurrentLocation = new Runnable() {
        @Override
        public void run() {
            getDeviceLocation();
            mHandler.postDelayed(CurrentLocation , INTERVAL);
        }
    };

    @Override
    public void onStartSuccess() {

    }

    @Override
    public void onFindReponse(JSONObject jsonObject) {
        String rejectReason = jsonObject.optString("rejectReason");
        if (rejectReason.equals("Driver Not Available")) {
            Toast.makeText(MapsActivity.this, "Không tìm thấy tài xế phù hợp", Toast.LENGTH_SHORT).show();
        }
        else {
            btn_findDriver.setVisibility(View.INVISIBLE);
            btn_cancelTrip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancelFindReponse(JSONObject jsonObject) {
        String jsonData = jsonObject.optString("errorMessage");
        if (jsonData.equals("Success")){
            btn_findDriver.setVisibility(View.VISIBLE);
            btn_cancelTrip.setVisibility(View.INVISIBLE);
            hasDirection = false;
        }
        else {
            Log.d("errorMessage" , jsonData);
        }
    }

    @Override
    public void onPartnerInfoReponse(JSONObject jsonObject) {
        partnerInfo = new Info(jsonObject.optString("fullName") , jsonObject.optString("phoneNumber"));
        btn_partner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAcceptReponse(JSONObject jsonObject) {

    }

    @Override
    public void onFinishReponse(JSONObject jsonObject) {

    }

    @Override
    public void onCancelWaitReponse(JSONObject jsonObject) {

    }

    @Override
    public void onCancelReponse(JSONObject jsonObject) {

    }

    class Locations extends AsyncTask<String , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        @Override
        protected String doInBackground(String... strings) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = strings[0];
            String res ;
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/location")
                    .header("Content-Type" , "application/json")
                    .header("Cookie",cookie)
                    .post(body)
                    //.get()
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("Location" , res);
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
            JSONObject jsonDatas = new JSONObject(data);
            JSONObject jsonData = jsonDatas.getJSONObject("data");
            JSONObject trip;
            JSONObject partnerLocation;
            trip = jsonData.optJSONObject("trip");
            if (trip != null){
                Log.d("trip", trip.toString());
                String rejectReason = trip.optString("rejectReason");
                if (rejectReason.equals("Driver Not Available")){
                    Toast.makeText(MapsActivity.this, "Không tìm thấy tài xế phù hợp", Toast.LENGTH_SHORT).show();
                }
                else {
                    btn_findDriver.setVisibility(View.INVISIBLE);
                    btn_cancelTrip.setVisibility(View.VISIBLE);
                }
            }
            partnerLocation =jsonData.optJSONObject("partnerLocation");
            if (partnerLocation != null){
                Log.d("partnerLocation" , partnerLocation.toString());
                btn_cancelTrip.setVisibility(View.INVISIBLE);
                btn_onTrip.setVisibility(View.VISIBLE);
                sPartnerLocation = new LatLng(partnerLocation.getDouble("latitude") , partnerLocation.getDouble("longitude"));
                if (partnerMarker == null){
                    partnerMarker = mMap.addMarker(new MarkerOptions().position(sPartnerLocation).title("Tài xế"));
                }
                else {
                    partnerMarker.setPosition(sPartnerLocation);
                }
                if (hasDirection){
                    hasDirection = false;
                }
                if (first){
                    first = false;
                    btn_partner.setVisibility(View.VISIBLE);
                    partnerRequest(cookie);
                }
            }
            if ((trip == null) && (partnerLocation == null)){
                removeTrip();
            }
        }
    }
    private void removeTrip(){
        btn_findDriver.setVisibility(View.VISIBLE);
        btn_cancelTrip.setVisibility(View.INVISIBLE);
        btn_onTrip.setVisibility(View.INVISIBLE);
        if (partnerMarker != null){
            partnerMarker.remove();
        }
        if (!hasDirection){
            for(Polyline line : polylinePaths)
            {
                line.remove();
            }
            if (destinationMarkers != null){
                for(Marker marker : destinationMarkers)
                {
                    marker.remove();
                }
            }
            if (originMarkers != null){
                for(Marker marker : originMarkers)
                {
                    marker.remove();
                }
            }
            relativeLayout.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
        }
        first = true;
        partnerInfo = null;
        btn_partner.setVisibility(View.INVISIBLE);
    }
    class Userinfo extends AsyncTask<Void , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        @Override
        protected String doInBackground(Void... voids) {
            Log.d("Cookie" , "Start");
            String res ;
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/userinfo")
                    .header("Content-Type" , "application/json")
                    .header("Cookie",cookie)
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("Userinfo" , res);
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
            JSONObject jsonDatas = new JSONObject(data);
            JSONObject jsondata = jsonDatas.optJSONObject("data");
            if (jsondata.optString("car") == null){
                Intent intent = new Intent(MapsActivity.this , RegisterCar.class);
                intent.putExtra("Cookie", cookie);
                MapsActivity.this.startActivity(intent);
            }
            fullName = jsondata.optString("fullName");
            headerView = navigationView.inflateHeaderView(R.layout.drawer_header);
            TextView txt_name = headerView.findViewById(R.id.full_name);
            txt_name.setText(fullName);
            navigationView.setNavigationItemSelectedListener(MapsActivity.this);
            // mo menu
            buttonOpenMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }
    void startRepeatingTask()
    {
        mHandlerTask.run();
    }
    void startRepeatingCurrentlocation(){
        CurrentLocation.run();
    }
    void stopRepeatingCurrentLocation(){
        mHandler.removeCallbacks(CurrentLocation);
    }
    void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
        boolean permissionGranted = false;
        switch(requestCode){
            case 9:
                permissionGranted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                if(permissionGranted){
                    phoneCall();
                }else {
                    Toast.makeText(MapsActivity.this, "Bạn không cấp quyền gọi.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                Log.d("Cookie" , cookie);
                startRepeatingCurrentlocation();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude() , mLastKnownLocation.getLongitude()) , DEFAULT_ZOOM));
                            Log.d("AAA" , mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
                            if (firstRun){
                                firstRun = false;
                                stopRepeatingCurrentLocation();
                                startRepeatingTask();
                            }
                        } else {
                            Log.d("AAA", "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());

                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
        Log.d("AAA", "device location");
    }
    @Override
    public void onBackPressed() {

        // khi an back, neu navigation view dang mo thi se dong lai
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    void findRequest(String tripType , Double LatitudeDestination , Double LongitudeDestination , String cookie){
        new Trip(tripType , LatitudeDestination , LongitudeDestination , this , cookie).findDriver();
    }
    void findRequest(String tripType , String cookie){
        new Trip(tripType ,  this , cookie).findDriver();
    }
    void cancelRequest(String cookie){
        new Trip(this , cookie).cancelFind();
    }
    void partnerRequest(String cookie){
        new Trip(this , cookie).partnerInfo();
    }
    private void onCallBtnClick(){
        if (Build.VERSION.SDK_INT < 23) {
            phoneCall();
        }else {

            if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                phoneCall();
            }else {
                final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
                //Asking request Permissions
                ActivityCompat.requestPermissions(MapsActivity.this, PERMISSIONS_STORAGE, 9);
            }
        }
    }
    private void phoneCall(){
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + partnerInfo.getPhoneNumber().toString()));
            MapsActivity.this.startActivity(callIntent);
        }else{
            Toast.makeText(MapsActivity.this, "You don't assign permission.", Toast.LENGTH_SHORT).show();
        }
    }
}
