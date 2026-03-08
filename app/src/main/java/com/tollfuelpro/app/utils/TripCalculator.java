package com.tollfuelpro.app.utils;

import android.content.Context;
import com.tollfuelpro.app.models.TollPlaza;
import com.tollfuelpro.app.models.TollResult;
import com.tollfuelpro.app.models.TripRecord;
import com.tollfuelpro.app.services.TollDataService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TripCalculator {

    public static TripRecord calculate(
            Context ctx,
            String source, String destination,
            double srcLat, double srcLng,
            double dstLat, double dstLng,
            double distanceKm,
            java.util.List<String> routeKeywords,
            String vehicleType,
            boolean isRoundTrip,
            double mileage,
            double fuelPrice) {

        List<TollPlaza> matchedTolls =
            TollDataService.findTollsOnRoute(ctx, source, destination, routeKeywords);

        List<TollResult> tollResults = new ArrayList<>();
        double totalTollCost = 0;
        
        for (TollPlaza plaza : matchedTolls) {
            double charge = TollDataService.getTollFee(plaza, vehicleType, isRoundTrip);
            if (charge > 0) {
                TollResult tr = new TollResult();
                tr.setPlazaName(plaza.getName());
                tr.setHighway(plaza.getNationalHighway() != null
                    ? plaza.getNationalHighway() : "NH");
                tr.setLocationKm(plaza.getLocationChainage() != null
                    ? plaza.getLocationChainage() : "");
                tr.setCharge(charge);
                tollResults.add(tr);
                totalTollCost += charge;
            }
        }

        double effectiveDistance = isRoundTrip ? distanceKm * 2 : distanceKm;
        double fuelNeeded = effectiveDistance / mileage;
        double fuelCost = Math.round(fuelNeeded * fuelPrice);

        TripRecord record = new TripRecord();
        record.setId(UUID.randomUUID().toString());
        record.setSource(source);
        record.setDestination(destination);
        record.setVehicleType(vehicleType);
        record.setTripType(isRoundTrip ? "round_trip" : "one_way");
        record.setDistanceKm(distanceKm);
        record.setTollCost(totalTollCost);
        record.setFuelCost(fuelCost);
        record.setTotalCost(totalTollCost + fuelCost);
        record.setMileage(mileage);
        record.setFuelPricePerLitre(fuelPrice);
        record.setFuelNeeded(Math.round(fuelNeeded * 10.0) / 10.0);
        record.setTollResults(tollResults);
        record.setTimestamp(System.currentTimeMillis());
        
        return record;
    }
}
