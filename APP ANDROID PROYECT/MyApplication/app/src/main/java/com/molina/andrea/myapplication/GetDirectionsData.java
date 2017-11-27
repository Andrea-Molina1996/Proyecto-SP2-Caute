package com.molina.andrea.myapplication;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by amoli on 10/20/2017.
 */

public class GetDirectionsData extends AsyncTask<Object, String, String> {

    String googleDirectionsData;
    GoogleMap mMap;
    String url;
    String duration, distance;
    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        latLng = (LatLng) objects[2];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googleDirectionsData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {
        HashMap<String, String> directionsListDurDir = null;
        DataParser parser = new DataParser();
        directionsListDurDir = parser.parseDirectionsDurDist(s); // parse directions method to parse the directions from the JSON
        duration = directionsListDurDir.get("duration");
        distance = directionsListDurDir.get("distance");

        String[] directionsList;
        directionsList = parser.parseDirections(s);
        displayDirection(directionsList);

        /*mMap.clear();*/
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Duration = "+duration+" \n Distance = "+distance);
        mMap.addMarker(markerOptions);
    }

    public String getDuration(){
        return duration;
    }

    public String getDistance(){
        return distance;
    }

    public void displayDirection(String[] directionsList){
        int count = directionsList.length;
        for(int i = 0; i < count; i++){
            PolylineOptions options = new PolylineOptions();
            options.color(Color.parseColor("#0091EA"));
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));

            mMap.addPolyline(options);
        }
    }

    public HashMap<LatLng, String> getManeuverList(String s){
        HashMap<LatLng,String> maneuverList = null;
        DataParser parser = new DataParser();
        //maneuverList = parser.parse1(s);

        return maneuverList;
    }
}
