package com.molina.andrea.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by amoli on 11/13/2017.
 */

public class AlertListAddInfoAdapter  extends ArrayAdapter<AlertInfo> {

    private Context context;
    private List<AlertInfo> alertsList;

    public AlertListAddInfoAdapter(@NonNull Context context, List<AlertInfo> alertsList) {
        super(context, R.layout.alert_list_view_add_info, alertsList);
        this.context = context;
        this.alertsList = alertsList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View listViewItem = inflater.inflate(R.layout.alert_list_view_add_info, null, true);

        TextView textViewDate = (TextView) listViewItem.findViewById(R.id.textViewDate);
        TextView textViewTime = (TextView) listViewItem.findViewById(R.id.textViewTime);
        TextView textViewSeriousness = (TextView) listViewItem.findViewById(R.id.textViewSeriousness);
        TextView textViewObject = (TextView) listViewItem.findViewById(R.id.textViewObject);

        AlertInfo alerts = alertsList.get(position);
        textViewDate.setText("Fecha: "+ alerts.getDate());
        textViewTime.setText("Hora: " + alerts.getTime() + " (24h)");
        textViewSeriousness.setText("Rango: " + alerts.getSeriousness());
        textViewObject.setText("Objeto: " + alerts.getObjects());

        return listViewItem;
    }
}
