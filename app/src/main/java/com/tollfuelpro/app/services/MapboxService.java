package com.tollfuelpro.app.services;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.tollfuelpro.app.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapboxService {
    private static final String MAPBOX_TOKEN = BuildConfig.MAPBOX_TOKEN;
    private static final OkHttpClient client = new OkHttpClient();

    public interface GeocodingCallback {
        void onResults(List<GeocodingResult> results);

        void onError(String message);
    }

    public interface DirectionsCallback {
        void onResult(double distanceKm, List<String> routeKeywords, String routeGeometry);

        void onError(String message);
    }

    public static class GeocodingResult {
        public String placeName;
        public String placeType;
        public double latitude;
        public double longitude;
    }

    public static Call searchCity(String query, GeocodingCallback callback) {
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
                + Uri.encode(query)
                + ".json?country=IN&types=place,locality,district"
                + "&access_token=" + MAPBOX_TOKEN;

        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled())
                    return;
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                if (call.isCanceled())
                    return;
                if (!response.isSuccessful()) {
                    callback.onError("API error: " + response.code());
                    return;
                }
                String body = response.body().string();
                List<GeocodingResult> results = parseGeocodingResponse(body);
                callback.onResults(results);
            }
        });
        return call;
    }

    private static List<GeocodingResult> parseGeocodingResponse(String json) {
        List<GeocodingResult> results = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray features = obj.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject f = features.getJSONObject(i);
                GeocodingResult r = new GeocodingResult();
                r.placeName = f.getString("place_name");
                if (f.has("place_type") && f.getJSONArray("place_type").length() > 0) {
                    r.placeType = f.getJSONArray("place_type").getString(0);
                }
                JSONArray coords = f.getJSONObject("geometry")
                        .getJSONArray("coordinates");
                r.longitude = coords.getDouble(0);
                r.latitude = coords.getDouble(1);
                results.add(r);
            }
        } catch (JSONException e) {
            /* ignore */ }
        return results;
    }

    public static void reverseGeocode(double lat, double lng, GeocodingCallback callback) {
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
                + lng + "," + lat
                + ".json?country=IN&types=place,locality,district,poi,neighborhood"
                + "&access_token=" + MAPBOX_TOKEN;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("API error: " + response.code());
                    return;
                }
                String body = response.body().string();
                List<GeocodingResult> results = parseGeocodingResponse(body);
                callback.onResults(results);
            }
        });
    }

    public static void getDrivingDistance(
            double srcLat, double srcLng,
            double dstLat, double dstLng,
            DirectionsCallback callback) {
        String coords = srcLng + "," + srcLat + ";" + dstLng + "," + dstLat;
        String url = "https://api.mapbox.com/directions/v5/mapbox/driving/"
                + coords + "?overview=simplified&steps=true&access_token=" + MAPBOX_TOKEN;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("API error: " + response.code());
                    return;
                }
                String body = response.body().string();
                try {
                    JSONObject obj = new JSONObject(body);
                    JSONArray routes = obj.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONObject route = routes.getJSONObject(0);
                        double distMeters = route.getDouble("distance");
                        String geometry = route.getString("geometry");

                        List<String> keywords = new ArrayList<>();
                        if (route.has("legs")) {
                            JSONArray legs = route.getJSONArray("legs");
                            for (int i = 0; i < legs.length(); i++) {
                                JSONObject leg = legs.getJSONObject(i);
                                if (leg.has("steps")) {
                                    JSONArray steps = leg.getJSONArray("steps");
                                    for (int j = 0; j < steps.length(); j++) {
                                        JSONObject step = steps.getJSONObject(j);
                                        if (step.has("name") && !step.getString("name").isEmpty())
                                            keywords.add(step.getString("name"));
                                        if (step.has("ref") && !step.getString("ref").isEmpty())
                                            keywords.add(step.getString("ref"));
                                        if (step.has("destinations") && !step.getString("destinations").isEmpty())
                                            keywords.add(step.getString("destinations"));
                                    }
                                }
                            }
                        }

                        callback.onResult(distMeters / 1000.0, keywords, geometry);
                    } else {
                        callback.onError("No route found");
                    }
                } catch (JSONException e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
}
