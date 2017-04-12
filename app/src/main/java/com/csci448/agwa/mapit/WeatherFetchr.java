package com.csci448.agwa.mapit;

/**
 * Created by amosgwa on 4/11/17.
 */

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherFetchr {

    private static final String TAG = "WF";

    private static final String API_KEY = "920ff5b8171b6adff94268a5271ef472";
    //http://api.openweathermap.org/data/2.5/weather?lat=39.744850&lon=-105.231654&units=imperial&appid=920ff5b8171b6adff94268a5271ef472
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public WeatherItem fetchWeather(Pin pin) {

        WeatherItem item = new WeatherItem();
        String lat = String.valueOf(pin.getPos().latitude);
        String lng = String.valueOf(pin.getPos().longitude);

        try {
            String url = Uri.parse("http://api.openweathermap.org/data/2.5/weather")
                    .buildUpon()
                    .appendQueryParameter("lat", lat)
                    .appendQueryParameter("lon", lng)
                    .appendQueryParameter("appid", API_KEY)
                    .appendQueryParameter("units", "imperial")
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(item, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return item;
    }

    private void parseItems(WeatherItem item, JSONObject jsonBody)
            throws IOException, JSONException {

        JSONObject weatherTmpJson = jsonBody.getJSONObject("main");
        item.setmTmp(weatherTmpJson.getDouble("temp"));

        JSONArray weatherJsonArray = jsonBody.getJSONArray("weather");
        item.setmDesc(weatherJsonArray.getJSONObject(0).getString("description"));
    }

}

