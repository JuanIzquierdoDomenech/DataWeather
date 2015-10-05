package com.mcmu.juanjesus.dataweather;

public final class WeatherUtilities {

    public enum WeatherType {
        CLEAR,
        THUNDERSTORM,
        DRIZZLE,
        FOGGY,
        CLOUDY,
        SNOWY,
        RAINY
    }

    public static WeatherType getWeatherType(int id) {

        if(id == 800) {
            // Exactly 800
            return WeatherType.CLEAR;
        } else {
            id /= 100;  // Range of 200, thunderstorm, Range of 300, drizzle, etc...
            switch (id) {
                case 2:
                    return WeatherType.THUNDERSTORM;
                case 3:
                    return WeatherType.DRIZZLE;
                case 5:
                    return WeatherType.RAINY;
                case 6:
                    return WeatherType.SNOWY;
                case 7:
                    return WeatherType.FOGGY;
                case 8:
                    return WeatherType.CLOUDY;
                default:
                    return WeatherType.CLEAR;
            }
        }
    }
}
