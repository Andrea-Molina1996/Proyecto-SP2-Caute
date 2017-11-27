package com.molina.andrea.myapplication;

import android.location.Address;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Andrea Molina on 11/15/2017.
 */

public class AlertInfo {
    int distance;
    String cameraPosition;
    String objects;
    int seriousness;
    String date;
    String time;
    String imgUrl;
    String vidUrl;
    int speed;
    Location location;
    double latitude;
    double longitude;
    String  address;
    boolean notified;

    public AlertInfo(){} // constructor obligatorio

    public AlertInfo(int distance, String cameraPosition, String objects, int seriousness, String date,
                     String time, String imgUrl, String vidUrl, int speed, double latitude, double longitude, String address, boolean notified){
        this.distance = distance;
        this.cameraPosition = cameraPosition;
        this.objects = objects;
        this.seriousness = seriousness;
        this.date = date;
        this.time = time;
        this.imgUrl =imgUrl;
        this.vidUrl = vidUrl;
        this.speed = speed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.notified = notified;
    }

    public AlertInfo(Alert alert, int speed, double latitude, double longitude, String address){
        this.distance = alert.getDistance();
        this.cameraPosition = alert.getCameraPosition();
        this.objects = alert.getObjects();
        this.seriousness = alert.getSeriousness();
        this.date = alert.getDate();
        this.time = alert.getTime();
        this.imgUrl =alert.getImgUrl();
        this.vidUrl = alert.getVidUrl();
        this.speed = speed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.notified = alert.getNotified();
    }

    public int getDistance() { return distance;}
    public String getCameraPosition() { return cameraPosition; }
    public String getObjects(){ return objects;}// retorna un arreglo con los objetos (3 a 5)
    public int getSeriousness() { return seriousness; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getImgUrl() { return imgUrl; }
    public String getVidUrl() { return vidUrl; }
    public int getSpeed() { return speed; }
    public double getLatitude(){ return latitude; }
    public double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public boolean isNotified(){ return notified; }
    public void setNotified() { notified = true; }

    public String toString(){
        return "Distance: "+distance+" CameraPosition: "+cameraPosition+" Object: "
                +objects+" Seriousness: "+seriousness+" Date: "+date+" Time: "+time+" imgUrl: "
                +imgUrl+" vidUrl: "+vidUrl+ " Speed: "+speed+" Location: "+location+" Address: "+address+ "Notified: "+notified;
    }

    public boolean equals(AlertInfo b){
        boolean resp = false;
        if(distance == b.distance && cameraPosition.equals(b.cameraPosition) && objects.equals(b.objects) && date.equals(b.date) && notified == b.notified && seriousness == b.seriousness && time.equals(b.time) && speed == b.speed && longitude == b.longitude && latitude == b.latitude && address.equals(b.address)){
            resp = true;
        }
        return resp;
    }
}
