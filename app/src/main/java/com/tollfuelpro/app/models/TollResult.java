package com.tollfuelpro.app.models;

public class TollResult {
    private String plazaName;
    private String highway;
    private String locationKm;
    private double charge;

    public String getPlazaName() { return plazaName; }
    public void setPlazaName(String plazaName) { this.plazaName = plazaName; }

    public String getHighway() { return highway; }
    public void setHighway(String highway) { this.highway = highway; }

    public String getLocationKm() { return locationKm; }
    public void setLocationKm(String locationKm) { this.locationKm = locationKm; }

    public double getCharge() { return charge; }
    public void setCharge(double charge) { this.charge = charge; }
}
