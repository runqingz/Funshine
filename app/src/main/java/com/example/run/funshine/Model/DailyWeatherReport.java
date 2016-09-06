package com.example.run.funshine.Model;

/**
 * Created by Run on 16/9/5.
 */
public class DailyWeatherReport {


    public static final String WEATHER_CLEAR = "Clear";
    public static final String WEATHER_CLOUD = "Clouds";
    public static final String WEATHER_WIND = "Wind";
    public static final String WEATHER_RAIN = "Rain";
    public static final String WEATHER_SNOW = "Snow";


    private String cityName;
    private String country;
    private int currentTemp;
    private int minTemp;
    private int maxTemp;
    private String weather;
    private String formatDate;


    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public String getWeather() {
        return weather;
    }

    public String getFormatDate() {
        return formatDate;
    }

    public DailyWeatherReport(String cityName, String country, int currentTemp, int minTemp, int maxTemp, String weather, String rawDate) {
        this.cityName = cityName;
        this.country = country;
        this.currentTemp = currentTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.weather = weather;
        this.formatDate = formattingRawDate(rawDate);
    }

    public String formattingRawDate(String rawDate){

        return "";
    }
}
