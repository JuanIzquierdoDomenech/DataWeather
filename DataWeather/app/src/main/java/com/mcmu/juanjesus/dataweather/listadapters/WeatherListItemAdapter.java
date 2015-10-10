package com.mcmu.juanjesus.dataweather.listadapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcmu.juanjesus.dataweather.R;
import com.mcmu.juanjesus.dataweather.models.WeatherData;
import com.mcmu.juanjesus.dataweather.utilities.DateUtilities;
import com.mcmu.juanjesus.dataweather.utilities.WeatherUtilities;
import com.mcmu.juanjesus.dataweather.utilities.WeatherUtilities.WeatherType;

import java.util.Vector;

public class WeatherListItemAdapter extends BaseAdapter {

    private final Activity activity;
    private final Vector<WeatherData> originalWeatherDataList;

    public WeatherListItemAdapter(Activity activity, Vector<WeatherData> dataList) {
        super();
        this.activity = activity;
        this.originalWeatherDataList = dataList;
    }

    //region BaseAdapter
    @Override
    public int getCount() {
        return originalWeatherDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return originalWeatherDataList.elementAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_weatherlist_item, null, true);

        TextView cityTextView = (TextView)view.findViewById(R.id.weatherListItemCityText);
        TextView dateTextView = (TextView)view.findViewById(R.id.weatherListItemDateText);
        TextView latLngTextView = (TextView)view.findViewById(R.id.weatherListItemLatLng);
        TextView weatherTextView = (TextView)view.findViewById(R.id.weatherListItemWeatherText);
        ImageView weatherImageView = (ImageView)view.findViewById(R.id.weatherListItemWeatherIcon);

        WeatherData rowData = originalWeatherDataList.elementAt(position);
        StringBuilder latLng = new StringBuilder();
        latLng.append(rowData.getLatitude()).append(", ").append(rowData.getLongitude());

        cityTextView.setText(rowData.getLocation());
        dateTextView.setText(rowData.getDate());
        latLngTextView.setText(latLng.toString());
        weatherTextView.setText(rowData.getWeather().toLowerCase());

        // Convert weather string to enum
        WeatherUtilities.WeatherType weather = WeatherUtilities.WeatherType.valueOf(rowData.getWeather());
        switch (weather) {
            case CLEAR:
                weatherImageView.setImageResource(R.drawable.clear);
                break;
            case THUNDERSTORM:
                weatherImageView.setImageResource(R.drawable.thunderstorm);
                break;
            case DRIZZLE:
                weatherImageView.setImageResource(R.drawable.drizzle);
                break;
            case FOGGY:
                weatherImageView.setImageResource(R.drawable.foggy);
                break;
            case CLOUDY:
                weatherImageView.setImageResource(R.drawable.cloudy);
                break;
            case SNOWY:
                weatherImageView.setImageResource(R.drawable.snowy);
                break;
            case RAINY:
                weatherImageView.setImageResource(R.drawable.rainy);
                break;
            default:
                weatherImageView.setImageResource(R.drawable.clear);
                break;
        }

        Log.d("WeatherListItemAdapter", "getView " + position + " (" + rowData + ") with weather " + weather);

        return view;
    }
    //endregion BaseAdapter
    
}


