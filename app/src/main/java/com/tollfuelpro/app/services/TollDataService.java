package com.tollfuelpro.app.services;

import android.content.Context;
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
            Context context, String source, String destination, List<String> routeKeywords) {
        List<TollPlaza> all = loadTolls(context);
        List<TollPlaza> results = new ArrayList<>();
        String srcLow = source.toLowerCase(java.util.Locale.ROOT).trim();
        String dstLow = destination.toLowerCase(java.util.Locale.ROOT).trim();
        List<String> kwLow = routeKeywords.stream()
                .map(s -> s.toLowerCase(java.util.Locale.ROOT))
                .map(String::trim)
                .filter(k -> k.length() >= 4)
                .filter(k -> !IGNORE_WORDS.contains(k))
                .collect(Collectors.toList());

        for (TollPlaza t : all) {
            String stretch = t.getStretch() != null ? t.getStretch().toLowerCase(java.util.Locale.ROOT) : "";
            String name = t.getName() != null ? t.getName().toLowerCase(java.util.Locale.ROOT) : "";
            String nh = t.getNationalHighway() != null ? t.getNationalHighway().toLowerCase(java.util.Locale.ROOT) : "";

            boolean matchesSrcDst = stretch.contains(srcLow) || stretch.contains(dstLow)
                    || name.contains(srcLow) || name.contains(dstLow);

            boolean matchesKw = false;
            for (String kw : kwLow) {
                if (stretch.contains(kw) || name.contains(kw)) {
                    matchesKw = true;
                    break;
                }
                // Normalize NH matching (e.g. Mapbox: "NH 44" or "NH44", JSON: "44" or "NH-44")
                String kwNum = kw.replaceAll("[^0-9]", "");
                if (!kwNum.isEmpty() && (kw.startsWith("nh") || kw.startsWith("sh"))) {
                    String nhNum = nh.replaceAll("[^0-9]", "");
                    if (kwNum.equals(nhNum)) {
                        matchesKw = true;
                        break;
                    }
                }
            }

            if (matchesSrcDst || matchesKw) {
                results.add(t);
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
                        /* skip */ }
                }
            }
        }
        return 0.0;
    }
}
