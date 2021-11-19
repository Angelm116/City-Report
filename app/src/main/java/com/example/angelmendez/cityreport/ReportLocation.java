package com.example.angelmendez.cityreport;

import com.android.volley.RequestQueue;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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

    public ReportLocation(HashMap<String, String> details, double latitude, double longitude)
    {
        this.country = details.get("country");
        this.state = details.get("state");
        this.county = details.get("county");
        this.city = details.get("city");
        this.zip = details.get("zip");
        this.streetNumber = details.get("street_number");
        this.streetName = details.get("street_name");
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

    public void setLatLng(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public String getFormattedAddress()
    {
        String formattedAddress = "";

        formattedAddress += streetNumber == null ? "" : streetNumber + " ";
        formattedAddress += streetName == null ? "" : streetName + ", ";
        formattedAddress += city == null ? "" : city + " ";
        formattedAddress += state == null ? "" : state + " ";
        formattedAddress += zip == null ? "" : zip;


        return formattedAddress.trim();
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
