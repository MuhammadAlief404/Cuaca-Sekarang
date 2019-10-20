package com.quantum.higgs.cuacasekarang;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Quantum Higgs on 10/10/2017.
 */

public class WeatherFragment extends Fragment {
    Typeface weatherFont;

    public static TextView cityField,updatedField,detailsField,currentTemperatureField,weatherIcon;
    public static Button btn;

    public static InterstitialAd mInterstitialAd;


    Handler handler;

    public WeatherFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = rootView.findViewById(R.id.city_field);
        updatedField = rootView.findViewById(R.id.updated_field);
        detailsField = rootView.findViewById(R.id.details_field);
        currentTemperatureField = rootView.findViewById(R.id.current_temperature_field);
        weatherIcon = rootView.findViewById(R.id.weather_icon);

        weatherIcon.setTypeface(weatherFont);

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId("ca-app-pub-2760112945176520/7593399970");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());



//        ArrayList<String> items=getCountries("countries.json");
//
//        Spinner spinner=(Spinner)rootView.findViewById(R.id.spinner);
//        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),
//                R.layout.spinner_layout,R.id.txt,items);
//        spinner.setAdapter(adapter);

        //Tombol cari lokasi
        btn = rootView.findViewById(R.id.btn_lokasi);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                builder.setTitle("Ganti Kota");
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("Cari", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Log.d("TAG", "The interstitial wasn't loaded yet.");
                        }
                        changeCity(input.getText().toString());
                    }
                });
                builder.show();
            }
        });

        //animasi hujan
//        new ParticleSystem(getActivity(), 80, R.drawable.hujan, 10000)
//                .setSpeedByComponentsRange(0f, 0f, 0.05f, 0.1f)
//                .setAcceleration(0.00005f, 90)
//                .emitWithGravity(rootView.findViewById(R.id.city_field), Gravity.BOTTOM, 8);

        return rootView;
    }


//    Batas Uji coba JSON tanpa WEB SERVICE

//    private ArrayList<String> getCountries(String fileName){
//        JSONArray jsonArray=null;
//        ArrayList<String> cList=new ArrayList<String>();
//        try {
//            InputStream is = getResources().getAssets().open(fileName);
//            int size = is.available();
//            byte[] data = new byte[size];
//            is.read(data);
//            is.close();
//            String json = new String(data, "UTF-8");
//            jsonArray=new JSONArray(json);
//            if (jsonArray != null) {
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    cList.add(jsonArray.getJSONObject(i).getString("Indonesia"));
//                }
//            }
//        }catch (IOException e){
//            e.printStackTrace();
//        }catch (JSONException je){
//            je.printStackTrace();
//        }
//        return cList;
//    }

//    Batas Uji coba JSON tanpa WEB SERVICE

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        updateWeatherData(new CityPreference(getActivity()).getCity());
    }

    private void updateWeatherData(final String city){
        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
//                            Toast.makeText(getActivity(),
//                                    getActivity().getString(R.string.place_not_found),
//                                    Toast.LENGTH_LONG).show();


//                            Lokasi Alert Ketika Tidak Kon

                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(getContext());
                            }
                            builder.setTitle("Terjadi Kesalahan")
                                    .setMessage("Mohon Periksa Koneksi Internet Anda")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
//                                            Open Setting Ketika Tekan Yes
                                            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){
        try {
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            detailsField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Kelembapan: " + main.getString("humidity") + "%" +
                            "\n" + "Tekanan Udara: " + main.getString("pressure") + " hPa");

            currentTemperatureField.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt")*1000));
            updatedField.setText("Last update: " + updatedOn);

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = getActivity().getString(R.string.weather_sunny);
            } else {
                icon = getActivity().getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = getActivity().getString(R.string.weather_thunder);
                    break;
                case 3 : icon = getActivity().getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = getActivity().getString(R.string.weather_foggy);
                    break;
                case 8 : icon = getActivity().getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = getActivity().getString(R.string.weather_snowy);
                    break;
                case 5 : icon = getActivity().getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    private void startColorAnimation(View v)
    {
        int colorStart = v.getSolidColor();
//        int colorEnd = #FF9F10;
    }

    public void changeCity(String city){
        updateWeatherData(city);
    }
}