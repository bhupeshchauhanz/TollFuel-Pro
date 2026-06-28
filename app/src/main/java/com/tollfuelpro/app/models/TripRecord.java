package com.tollfuelpro.app.models;

import java.util.List;

public class TripRecord {
    private String id;
    private String source;
    private String destination;
    private String vehicleType;
    private String tripType;
    private double distanceKm;
    private double tollCost;
    private double fuelCost;
    private double totalCost;
    private double mileage;
    private double fuelPricePerLitre;
    private double fuelNeeded;
    private List<TollResult> tollResults;
    private long timestamp;
    private String routeGeometry;
    private double srcLat, srcLng, dstLat, dstLng;

    private String fuelType;
    private boolean fuelCostIncluded;
    private double batteryCapacityKwh;
    private double evRangeKm;
    private double chargeLevelPercent;
    private double costPerKwh;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getTripType() { return tripType; }
    public void setTripType(String tripType) { this.tripType = tripType; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getTollCost() { return tollCost; }
    public void setTollCost(double tollCost) { this.tollCost = tollCost; }

    public double getFuelCost() { return fuelCost; }
    public void setFuelCost(double fuelCost) { this.fuelCost = fuelCost; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getMileage() { return mileage; }
    public void setMileage(double mileage) { this.mileage = mileage; }

    public double getFuelPricePerLitre() { return fuelPricePerLitre; }
    public void setFuelPricePerLitre(double fuelPricePerLitre) { this.fuelPricePerLitre = fuelPricePerLitre; }

    public double getFuelNeeded() { return fuelNeeded; }
    public void setFuelNeeded(double fuelNeeded) { this.fuelNeeded = fuelNeeded; }

    public List<TollResult> getTollResults() { return tollResults; }
    public void setTollResults(List<TollResult> tollResults) { this.tollResults = tollResults; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getRouteGeometry() { return routeGeometry; }
    public void setRouteGeometry(String routeGeometry) { this.routeGeometry = routeGeometry; }

    public double getSrcLat() { return srcLat; }
    public void setSrcLat(double srcLat) { this.srcLat = srcLat; }

    public double getSrcLng() { return srcLng; }
    public void setSrcLng(double srcLng) { this.srcLng = srcLng; }

    public double getDstLat() { return dstLat; }
    public void setDstLat(double dstLat) { this.dstLat = dstLat; }

    public double getDstLng() { return dstLng; }
    public void setDstLng(double dstLng) { this.dstLng = dstLng; }

    public boolean isRoundTrip() {
        return "Round Trip".equalsIgnoreCase(this.tripType);
    }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public boolean isFuelCostIncluded() { return fuelCostIncluded; }
    public void setFuelCostIncluded(boolean fuelCostIncluded) { this.fuelCostIncluded = fuelCostIncluded; }

    public double getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public void setBatteryCapacityKwh(double batteryCapacityKwh) { this.batteryCapacityKwh = batteryCapacityKwh; }

    public double getEvRangeKm() { return evRangeKm; }
    public void setEvRangeKm(double evRangeKm) { this.evRangeKm = evRangeKm; }

    public double getChargeLevelPercent() { return chargeLevelPercent; }
    public void setChargeLevelPercent(double chargeLevelPercent) { this.chargeLevelPercent = chargeLevelPercent; }

    public double getCostPerKwh() { return costPerKwh; }
    public void setCostPerKwh(double costPerKwh) { this.costPerKwh = costPerKwh; }
}
