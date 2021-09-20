package com.example.angelmendez.cityreport;

import com.android.volley.RequestQueue;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class ReportLocation implements Serializable {

    private String country;
    private String state;
    private String county;
    private String city;
    private String zip;
    private String streetNumber;
    private String streetName;
    private double latitude;
    private double longitude;

    public ReportLocation(String country, String state, String county, String city, String zip, String streetNumber, String streetName, double latitude, double longitude)
    {
        this.country = country;
        this.state = state;
        this.county = county;
        this.city = city;
        this.zip = zip;
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ReportLocation(ArrayList<String> details, double latitude, double longitude)
    {
        this.country = details.get(0);
        this.state = details.get(1);
        this.county = details.get(2);
        this.city = details.get(3);
        this.zip = details.get(4);
        this.streetNumber = details.get(5);
        this.streetName = details.get(6);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFormattedAddress()
    {
        return streetNumber + " " + streetName + ", " + city + " " + state + " " + zip;
    }

    public String getStreetAddress()
    {
        return streetNumber + " " + streetName;
    }

    public LatLng getLatLng()
    {
        return new LatLng(latitude, longitude);
    }
}
