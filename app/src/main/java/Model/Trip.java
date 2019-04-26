package Model;

import android.os.AsyncTask;
import android.util.Log;

import com.example.nogam.ukar.MapDriverActivity;
import com.example.nogam.ukar.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Trip {
    String tripType;
    Double LatitudeDestination;
    Double LongitudeDestination;
    TripListener listener;
    String cookie;

    public Trip(String tripType, TripListener listener, String cookie) {
        this.tripType = tripType;
        this.listener = listener;
        this.cookie = cookie;
    }

    public Trip(String tripType, Double latitudeDestination, Double longitudeDestination, TripListener listener, String cookie) {
        this.tripType = tripType;
        LatitudeDestination = latitudeDestination;
        LongitudeDestination = longitudeDestination;
        this.listener = listener;
        this.cookie = cookie;
    }

    public Trip(TripListener listener, String cookie) {
        this.listener = listener;
        this.cookie = cookie;
    }
    public void getStart(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("TripType" , tripType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject.length() > 0){
            new GetStart().execute(jsonObject.toString());
        }
    }
    class GetStart extends AsyncTask<String, Void, String> {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(String... strings) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = strings[0];
            String res;
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/get-start")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .post(body)
                    //.get()
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("GetStart", res);
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
            listener.onStartSuccess();
        }
    }
    public void findDriver(){
        JSONObject jsonObject = new JSONObject();
        if (tripType.equals("OneWay")){
            try {
                jsonObject.put("TripType" , "Oneway");
                jsonObject.put("LatitudeDestination", LatitudeDestination);
                jsonObject.put("LongitudeDestination", LongitudeDestination);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new FindDriver().execute(jsonObject.toString());
        }
        else {
            try {
                jsonObject.put("TripType" , "Round");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new FindDriver().execute(jsonObject.toString());
        }
    }
    class FindDriver extends AsyncTask<String , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(String... strings) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = strings[0];
            String res;
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/find-driver")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("find driver", res);
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
            listener.onFindReponse(jsonData);
        }
    }
    public void cancelFind(){
        new CancelTrip().execute();
    }
    class CancelTrip extends AsyncTask<Void , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(Void... voids) {
            String res;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = "ab";
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/cancel-find")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("Canceltrip", res);
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
            listener.onCancelFindReponse(jsonDatas);
        }
    }
    public void partnerInfo(){
        new partnerInfo().execute();
    }
    class partnerInfo extends AsyncTask<Void , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        @Override
        protected String doInBackground(Void... voids) {
            String res ;
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/partner")
                    .header("Content-Type" , "application/json")
                    .header("Cookie",cookie)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("Info" , res);
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
            listener.onPartnerInfoReponse(jsonData);
        }
    }
    public void acceptTrip(){
        new AcceptTrip().execute();
    }
    class AcceptTrip extends AsyncTask<Void, Void, String> {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(Void... voids) {
            String res;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = "ab";
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/accept")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("AcceptTrip", res);
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
            JSONObject jsonObject = new JSONObject(data);
            JSONObject jsonData = jsonObject.getJSONObject("data");
            listener.onAcceptReponse(jsonData);
        }
    }
    public void rejectTrip(){
        new RejectTrip().execute();
    }
    class RejectTrip extends AsyncTask<Void, Void, String> {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        @Override
        protected String doInBackground(Void... voids) {
            Log.d("abc" , "asdasd");
            String res;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = "ab";
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/reject")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .post(body)
                    .build();

            try {
                Log.d("abc" , "asdads");
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("RejectTrip", res);
                return res;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Log.d("RejectTrip" , s);
                parseJSon(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void parseJSon(String data) throws JSONException {
            if (data == null)
                return;
        }
    }
    public void cancelTrip(){
        new Cancel().execute();
    }
    class Cancel extends AsyncTask<Void , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(Void... voids) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String res ;
            String json = "ab";
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/cancel")
                    .header("Content-Type" , "application/json")
                    .header("Cookie",cookie)
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                //String rescookie =  response.headers("Set-Cookie").get(0).replace("; path=/; secure; samesite=lax; httponly" , "");
                Log.d("Cancel" , res);
                //Log.d("AAA" , rescookie);
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
            JSONObject jsonObject = new JSONObject(data);
            listener.onCancelReponse(jsonObject);
        }
    }
    public void finishTrip(){
        new Finish().execute();
    }
    class Finish extends AsyncTask<Void , Void, String > {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(Void... voids) {
            String res;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String json = "ab";
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/finish")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .post(body)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                //String rescookie =  response.headers("Set-Cookie").get(0).replace("; path=/; secure; samesite=lax; httponly" , "");
                Log.d("finish", res);
                //Log.d("AAA" , rescookie);
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
            JSONObject jsonObject = new JSONObject(data);
            listener.onFinishReponse(jsonObject);
        }
    }
    public void cancelWait(){
        new CancelWait().execute();
    }
    class CancelWait extends AsyncTask<Void, Void, String> {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        @Override
        protected String doInBackground(Void... voids) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String res;
            String json = "ab";
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://ukarbetezminor.azurewebsites.net/trip/cancel-wait")
                    .header("Content-Type", "application/json")
                    .header("Cookie", cookie)
                    .post(body)
                    //.get()
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                res = response.body().string();
                Log.d("CancelWait", res);
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
            JSONObject jsonObject = new JSONObject(data);
            listener.onCancelWaitReponse(jsonObject);
        }
    }
}
