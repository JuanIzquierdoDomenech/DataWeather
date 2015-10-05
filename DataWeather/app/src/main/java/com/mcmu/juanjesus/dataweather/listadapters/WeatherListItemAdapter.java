package com.mcmu.juanjesus.dataweather.listadapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcmu.juanjesus.dataweather.R;

import java.util.Vector;

public class WeatherListItemAdapter extends BaseAdapter{

    private final Activity activity;
    private final Vector<String> dataList;

    public WeatherListItemAdapter(Activity activity, Vector<String> dataList) {
        super();
        this.activity = activity;
        this.dataList = dataList;
    }

    //region BaseAdapter
    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.elementAt(position);
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
        TextView dateTextView = (TextView)view.findViewById(R.id.weatherListItemCityText);
        TextView weatherTextView = (TextView)view.findViewById(R.id.weatherListItemCityText);
        ImageView weatherImageView = (ImageView)view.findViewById(R.id.weatherListItemWeatherIcon);

        cityTextView.setText(dataList.elementAt(position));
        dateTextView.setText("20-20-2020");
        weatherTextView.setText("Cloudy");
        weatherImageView.setImageResource(R.drawable.cloudy);

        return view;
    }
    //endregion BaseAdapter
}


