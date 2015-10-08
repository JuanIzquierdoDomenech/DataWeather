package com.mcmu.juanjesus.dataweather.utilities;

import java.text.DateFormat;

public final class DateUtilities {

    public static String milisToDate(long milis) {
        return DateFormat.getInstance().format(milis);

    }
}
