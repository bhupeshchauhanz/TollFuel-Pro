package com.tollfuelpro.app.services;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tollfuelpro.app.models.TripRecord;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageService {
    private static final String PREF_NAME = "TollFuelPro";
    private static final String KEY_TRIPS = "trip_history";

    public static void saveTrip(Context ctx, TripRecord trip) {
        List<TripRecord> trips = getAllTrips(ctx);
        trips.add(0, trip); 
        saveAll(ctx, trips);
    }

    public static List<TripRecord> getAllTrips(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TRIPS, "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<TripRecord>>(){}.getType();
        List<TripRecord> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public static void deleteTrip(Context ctx, String tripId) {
        List<TripRecord> trips = getAllTrips(ctx);
        trips.removeIf(t -> t.getId().equals(tripId));
        saveAll(ctx, trips);
    }

    public static void deleteAllTrips(Context ctx) {
        saveAll(ctx, new ArrayList<>());
    }

    private static void saveAll(Context ctx, List<TripRecord> trips) {
        String json = new Gson().toJson(trips);
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_TRIPS, json).apply();
    }
}
