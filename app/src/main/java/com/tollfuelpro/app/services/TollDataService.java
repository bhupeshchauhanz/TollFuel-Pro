package com.tollfuelpro.app.services;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tollfuelpro.app.models.TollPlaza;
import com.tollfuelpro.app.models.VehicleCharge;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TollDataService {
    private static List<TollPlaza> cachedTolls = null;

    public static void invalidateCache() {
        cachedTolls = null;
    }

    public static List<TollPlaza> loadTolls(Context context) {
        if (cachedTolls != null)
            return cachedTolls;
        try {
            InputStream is = context.getAssets().open("toll_plaza_data.json");
            String json = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining());
            Gson gson = new Gson();
            Type listType = new TypeToken<List<TollPlaza>>() {
            }.getType();
            cachedTolls = gson.fromJson(json, listType);
        } catch (IOException e) {
            Log.e("TollDataService", "Failed to load toll data", e);
            cachedTolls = new ArrayList<>();
        }
        return cachedTolls;
    }

    private static final List<String> IGNORE_WORDS = java.util.Arrays.asList(
            "india", "uttar pradesh", "haryana", "punjab", "jammu & kashmir", "jammu and kashmir",
            "rajasthan", "madhya pradesh", "maharashtra", "gujarat", "karnataka",
            "andhra pradesh", "telangana", "tamil nadu", "kerala", "bihar", "west bengal",
            "delhi", "new delhi", "chandigarh", "himachal pradesh", "uttarakhand",
            "road", "marg", "highway", "expressway", "street", "bypass", "toll", "plaza",
            "n.h.", "s.h.", "state highway", "national highway");

    public static List<TollPlaza> findTollsOnRoute(
            Context context, String source, String destination, List<String> routeKeywords, List<String> routeStates) {
        List<TollPlaza> all = loadTolls(context);
        List<TollPlaza> results = new ArrayList<>();
        if (routeKeywords == null) {
            routeKeywords = new ArrayList<>();
        }
        if (routeStates == null) {
            routeStates = new ArrayList<>();
        }
        String srcLow = source.toLowerCase(java.util.Locale.ROOT).trim();
        String dstLow = destination.toLowerCase(java.util.Locale.ROOT).trim();

        // Clean source/destination names (extract first word/city)
        String srcCity = srcLow.split(",")[0].trim();
        String dstCity = dstLow.split(",")[0].trim();

        List<String> kwLow = routeKeywords.stream()
                .map(s -> s.toLowerCase(java.util.Locale.ROOT))
                .map(String::trim)
                .filter(k -> k.length() >= 3)
                .filter(k -> !IGNORE_WORDS.contains(k))
                .collect(Collectors.toList());

        // Extract highway numbers from the route keywords (e.g. "48" from "nh 48")
        List<String> routeHighways = new ArrayList<>();
        for (String kw : kwLow) {
            if (kw.startsWith("nh") || kw.startsWith("sh") || kw.contains("highway") || kw.contains("expressway")) {
                String num = kw.replaceAll("[^0-9]", "");
                if (!num.isEmpty() && !routeHighways.contains(num)) {
                    routeHighways.add(num);
                }
            }
        }

        for (TollPlaza t : all) {
            // First: State match (if states are available)
            if (!routeStates.isEmpty()) {
                boolean stateMatch = false;
                if (t.getState() != null) {
                    String stateLow = t.getState().toLowerCase(java.util.Locale.ROOT).trim();
                    for (String s : routeStates) {
                        if (stateLow.contains(s) || s.contains(stateLow)) {
                            stateMatch = true;
                            break;
                        }
                    }
                }
                if (!stateMatch) continue;
            }

            String stretch = t.getStretch() != null ? t.getStretch().toLowerCase(java.util.Locale.ROOT) : "";
            String name = t.getName() != null ? t.getName().toLowerCase(java.util.Locale.ROOT) : "";
            String nh = t.getNationalHighway() != null ? t.getNationalHighway().toLowerCase(java.util.Locale.ROOT) : "";

            // Check if the plaza's highway matches the route's highways
            boolean highwayMatches = false;
            String nhNum = nh.replaceAll("[^0-9]", "");
            if (!nhNum.isEmpty() && routeHighways.contains(nhNum)) {
                highwayMatches = true;
            }

            // Check if the plaza is specifically on our route's cities/localities
            boolean localityMatches = false;
            for (String kw : kwLow) {
                // If it's a city/locality keyword (not a highway keyword)
                if (!kw.startsWith("nh") && !kw.startsWith("sh")) {
                    if (stretch.contains(kw) || name.contains(kw)) {
                        localityMatches = true;
                        break;
                    }
                }
            }

            // Also check if stretch contains both source and destination cities (direct match)
            boolean directRouteMatch = (stretch.contains(srcCity) && stretch.contains(dstCity));

            boolean isMatch = false;
            if (directRouteMatch) {
                isMatch = true;
            } else if (!nhNum.isEmpty()) {
                // If it has a highway number, require highway to match
                if (highwayMatches) {
                    boolean matchesCity = stretch.contains(srcCity) || stretch.contains(dstCity) 
                                        || name.contains(srcCity) || name.contains(dstCity) 
                                        || localityMatches;
                    if (matchesCity) {
                        isMatch = true;
                    }
                }
            } else {
                // No highway number, fall back to name/locality matching
                boolean matchesCity = stretch.contains(srcCity) || stretch.contains(dstCity) 
                                    || name.contains(srcCity) || name.contains(dstCity) 
                                    || localityMatches;
                if (matchesCity) {
                    isMatch = true;
                }
            }

            if (isMatch) {
                // Prevent duplicate entries for the same plaza name
                boolean exists = false;
                for (TollPlaza added : results) {
                    if (added.getName().equalsIgnoreCase(t.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    results.add(t);
                }
            }
        }
        return results;
    }

    public static double getTollFee(TollPlaza plaza, String vehicleType, boolean isReturn) {
        String key = vehicleType;

        if (plaza.getVehicleCharges() == null)
            return 0.0;

        for (VehicleCharge vc : plaza.getVehicleCharges()) {
            if (vc.getTypeOfVehicle() != null
                    && vc.getTypeOfVehicle().contains(key)) {
                String raw = isReturn
                        ? vc.getReturnJourney()
                        : vc.getSingleJourney();
                if (raw != null) {
                    raw = raw.replaceAll("[^0-9.]", "");
                    if (raw.isEmpty())
                        continue;
                    try {
                        return Double.parseDouble(raw);
                    } catch (NumberFormatException e) {
                        Log.w("TollDataService", "Failed to parse toll fee", e);
                    }
                }
            }
        }
        return 0.0;
    }
}
