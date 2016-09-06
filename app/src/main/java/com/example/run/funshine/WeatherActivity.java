package com.example.run.funshine;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.run.funshine.Model.DailyWeatherReport;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
, LocationListener{


    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String COOR_BASE=  "?lat=49.2694284&lon=-123.2540448";  //49.2694284&lon=-123.2540448";
    final String API_KEY = "&APPID=3ff221623382afc4fb28ac1ad812edf5";
    final String UINT = "&units=metric";
    //49.2694284,-123.2540448

    private GoogleApiClient mgoogleApiClient;
    private final int PERMISSION_LOCATION = 111;
    private ArrayList<DailyWeatherReport> weatherReports = new ArrayList<DailyWeatherReport>();

    private ImageView weatherIcon;
    private ImageView weatherIconMini;
    private TextView date;
    private TextView currentTemp;
    private  TextView minTemp;
    private TextView city;
    private TextView weather;

    WeatherAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);


        weatherIcon = (ImageView)findViewById(R.id.weatherIcon);
        weatherIconMini = (ImageView)findViewById(R.id.weatherIconMini);
        date = (TextView)findViewById(R.id.date);
        currentTemp = (TextView)findViewById(R.id.currentTemp);
        minTemp = (TextView)findViewById(R.id.lowestTemp);
        city = (TextView)findViewById(R.id.location);
        weather = (TextView)findViewById(R.id.weather);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.content_weather_reports);
        mAdapter = new WeatherAdapter(weatherReports);

        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

        mgoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(LocationServices.API).enableAutoManage(this,this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        downloadWeatherData(null);
    }

    public void downloadWeatherData(Location location){

        //final String coordinates = COOR_BASE + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = URL_BASE + COOR_BASE + UINT + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("Fun", "Response: " + response.toString());
                try{

                    JSONObject city = response.getJSONObject("city");
                    String cityName = city.getString("name");
                    String country =  city.getString("country");

                    JSONArray list = response.getJSONArray("list");

                    for (int x= 0; x<5; x++){
                        JSONObject obj = list.getJSONObject(x);
                        JSONObject main = obj.getJSONObject("main");


                        Double maxTemp = main.getDouble("temp_max");
                        Double minTemp = main.getDouble("temp_min");
                        Double currentTemp = main.getDouble("temp");

                        JSONArray weatherarr = obj.getJSONArray("weather");
                        JSONObject weather = weatherarr.getJSONObject(0);
                        String weatherType = weather.getString("main");

                        String rawDate = obj.getString("dt_txt");

                        DailyWeatherReport dailyWeatherReport = new DailyWeatherReport(cityName,country,currentTemp.intValue(),minTemp.intValue(),maxTemp.intValue(),weatherType,rawDate);

                        weatherReports.add(dailyWeatherReport);
                    }

                    Log.v("JSON","Name: " + cityName + "-" + "Country: " + country);


                }catch (JSONException e){
                    Log.v("JSON", "error on data");
                }

                updateUI();
                mAdapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Fun","Error: " + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }else{
            startLocationSevices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void startLocationSevices(){
        try{
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient,req,this);
        }catch (SecurityException exception){

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startLocationSevices();
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void updateUI(){
        if(weatherReports.size()>0){
            DailyWeatherReport weatherReport = weatherReports.get(0);

            switch (weatherReport.getWeather()){
                case DailyWeatherReport.WEATHER_CLOUD:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    break;
                case DailyWeatherReport.WEATHER_RAIN:
                    Log.v("Yeah","it's raining");
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    break;
                case DailyWeatherReport.WEATHER_SNOW:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                    break;
                default: weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            }

            date.setText("Today, May 1");
            currentTemp.setText(Integer.toString(weatherReport.getCurrentTemp()));
            minTemp.setText(Integer.toString(weatherReport.getMinTemp()));
            city.setText(weatherReport.getCityName() + ", " + weatherReport.getCountry());
            weather.setText(weatherReport.getWeather());

        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder>{
        private ArrayList<DailyWeatherReport> mDailyWeatherReports;

        public WeatherAdapter(ArrayList<DailyWeatherReport> dailyWeatherReports){
            mDailyWeatherReports = dailyWeatherReports;
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = getLayoutInflater().from(parent.getContext()).inflate(R.layout.card_weather, parent, false);

            return new WeatherReportViewHolder(card);
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report = mDailyWeatherReports.get(position);
            holder.updateUI(report);

        }

        @Override
        public int getItemCount() {
            return mDailyWeatherReports.size();
        }
    }

    public class WeatherReportViewHolder extends RecyclerView.ViewHolder{

        private ImageView weatherIcon;
        private TextView weatherDate;
        private TextView weather;
        private TextView maxTemp;
        private TextView minTemp;

        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            weatherIcon = (ImageView)itemView.findViewById(R.id.weather_icon);
            weatherDate = (TextView)itemView.findViewById(R.id.weather_day);
            weather = (TextView)itemView.findViewById(R.id.weather_description);
            maxTemp = (TextView)itemView.findViewById(R.id.max);
            minTemp = (TextView)itemView.findViewById(R.id.min);
        }

        public void updateUI(DailyWeatherReport report){

            weatherDate.setText(report.getFormatDate());
            weather.setText(report.getWeather());
            maxTemp.setText(Integer.toString(report.getMaxTemp()));
            minTemp.setText(Integer.toString(report.getMinTemp()));

            switch (report.getWeather()){
                case DailyWeatherReport.WEATHER_CLOUD:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_RAIN:
                    Log.v("Yeah","it's raining");
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));
                    break;
                case DailyWeatherReport.WEATHER_SNOW:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow_mini));
                    break;
                default: weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));

            }
        }


    }
}
