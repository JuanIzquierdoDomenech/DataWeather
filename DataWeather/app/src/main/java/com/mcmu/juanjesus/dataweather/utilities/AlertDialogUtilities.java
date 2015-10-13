package com.mcmu.juanjesus.dataweather.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.mcmu.juanjesus.dataweather.R;

public class AlertDialogUtilities {

    public static void showNoLocationSettingsEnabled(final Context c) {

        AlertDialog.Builder alert = new AlertDialog.Builder(c);
        alert.setMessage(c.getString(R.string.location_service_disabled));
        alert.setPositiveButton(c.getString(R.string.enable), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                c.startActivity(locationSettingsIntent);
            }
        });
        alert.setNegativeButton(c.getString(R.string.cancel), null);
        alert.show();
    }
}
