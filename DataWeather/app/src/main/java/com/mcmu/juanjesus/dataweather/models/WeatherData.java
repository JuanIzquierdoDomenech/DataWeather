package com.mcmu.juanjesus.dataweather.models;

public class WeatherData {

    private String userName;
    private String location;
    private float latitude, longitude;
    private String weather;
    private String date;

    public WeatherData(String userName, String location, float latitude, float longitude, String weather, String date) {
        this.userName = userName;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weather = weather;
        this.date = date;
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "userName='" + userName + '\'' +
                ", location='" + location + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", weather='" + weather + '\'' +
                ", date=" + date +
                '}';
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
