package com.tollfuelpro.app.models;

import com.google.gson.annotations.SerializedName;

public class VehicleCharge {
    @SerializedName("Type of vehicle")
    private String typeOfVehicle;

    @SerializedName("Single Journey")
    private String singleJourney;

    @SerializedName("Return Journey")
    private String returnJourney;

    @SerializedName("Monthly Pass")
    private String monthlyPass;

    public String getTypeOfVehicle() { return typeOfVehicle; }
    public void setTypeOfVehicle(String typeOfVehicle) { this.typeOfVehicle = typeOfVehicle; }

    public String getSingleJourney() { return singleJourney; }
    public void setSingleJourney(String singleJourney) { this.singleJourney = singleJourney; }

    public String getReturnJourney() { return returnJourney; }
    public void setReturnJourney(String returnJourney) { this.returnJourney = returnJourney; }

    public String getMonthlyPass() { return monthlyPass; }
    public void setMonthlyPass(String monthlyPass) { this.monthlyPass = monthlyPass; }
}
