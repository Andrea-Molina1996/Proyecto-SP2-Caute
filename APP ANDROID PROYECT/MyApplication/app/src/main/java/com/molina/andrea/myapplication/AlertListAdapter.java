package com.molina.andrea.myapplication;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by amoli on 11/10/2017.
 */

public class AlertListAdapter extends ArrayAdapter<Alert> {

    private Context context;
    private List<Alert> alertsList;


    public AlertListAdapter(@NonNull Context context, List<Alert> alertsList) {
        super(context, R.layout.alert_list_view, alertsList);
        this.context = context;
        this.alertsList = alertsList;
    }

    @NonNull

    @Override

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View listViewItem = inflater.inflate(R.layout.alert_list_view, null, true);
        TextView textViewAlertID = (TextView) listViewItem.findViewById(R.id.textViewAlertID);
        TextView textViewLevel = (TextView) listViewItem.findViewById(R.id.textViewLevel);
        TextView textViewNotified = (TextView) listViewItem.findViewById(R.id.textViewNotified);
        TextView textViewDate = (TextView) listViewItem.findViewById(R.id.textViewDate);
        TextView textViewSide = (TextView) listViewItem.findViewById(R.id.textViewSide);
        TextView textViewTime = (TextView) listViewItem.findViewById(R.id.textViewTime);

        Alert alerts = alertsList.get(position);
        textViewAlertID.setText("Distancia: "+ alerts.getDistance()+"m");
        textViewLevel.setText("Seriedad: " + alerts.getSeriousness());
        textViewNotified.setText("Notificada: " + alerts.getNotified());
        textViewDate.setText("Fecha: " + alerts.getDate());
        textViewSide.setText("Camara: " + alerts.getCameraPosition());
        textViewTime.setText("Hora: " + alerts.getTime());
        return listViewItem;
    }
}
