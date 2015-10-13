package com.mcmu.juanjesus.dataweather.utilities;

import android.location.Location;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtilities {

    private static String TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    @NonNull
    public static String milisToDate(long milis) {

        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        Date resultDate = new Date(milis);

        return sdf.format(resultDate);
    }
}
