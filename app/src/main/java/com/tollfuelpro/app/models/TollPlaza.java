package com.tollfuelpro.app.models;

import java.util.List;

public class TollPlaza {
    private String state;
    private String name;
    @com.google.gson.annotations.SerializedName("national_highway")
    private String nationalHighway;
    
    private String stretch;
    
    @com.google.gson.annotations.SerializedName("location_chainage")
    private String locationChainage;
    
    @com.google.gson.annotations.SerializedName("tollable_distance")
    private String tollableDistance;
    
    @com.google.gson.annotations.SerializedName("toll_plaza_code")
    private String tollPlazaCode;
    
    @com.google.gson.annotations.SerializedName("vehicle_charges")
    private List<VehicleCharge> vehicleCharges;

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNationalHighway() { return nationalHighway; }
    public void setNationalHighway(String nationalHighway) { this.nationalHighway = nationalHighway; }

    public String getStretch() { return stretch; }
    public void setStretch(String stretch) { this.stretch = stretch; }

    public String getLocationChainage() { return locationChainage; }
    public void setLocationChainage(String locationChainage) { this.locationChainage = locationChainage; }

    public String getTollableDistance() { return tollableDistance; }
    public void setTollableDistance(String tollableDistance) { this.tollableDistance = tollableDistance; }

    public String getTollPlazaCode() { return tollPlazaCode; }
    public void setTollPlazaCode(String tollPlazaCode) { this.tollPlazaCode = tollPlazaCode; }

    public List<VehicleCharge> getVehicleCharges() { return vehicleCharges; }
    public void setVehicleCharges(List<VehicleCharge> vehicleCharges) { this.vehicleCharges = vehicleCharges; }
}
