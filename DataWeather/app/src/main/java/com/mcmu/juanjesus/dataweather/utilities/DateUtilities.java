package com.mcmu.juanjesus.dataweather.utilities;

import android.location.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtilities {

    private static String TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static String milisToDate(long milis) {

        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        Date resultdate = new Date(milis);

        return sdf.format(resultdate);
    }

    public static String getUTCstring(Location location) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(location.getTime()));

        // Append the string "UTC" to the date
        if(!date.contains("UTC")) {
            date += " UTC";
        }
        return date;
    }

}
