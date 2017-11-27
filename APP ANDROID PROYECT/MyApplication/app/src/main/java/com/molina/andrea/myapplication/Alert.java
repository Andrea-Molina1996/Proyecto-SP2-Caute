package com.molina.andrea.myapplication;

/**
 * Created by amoli on 11/6/2017.
 */

public class Alert {
    int distance;
    String objects;
    String cameraPosition;
    int seriousness;
    String date;
    String time;
    String imgUrl;
    String vidUrl;
    boolean notified;


    public Alert(int distance, String objects, String cameraPosition, String date, Boolean notified, int seriousness, String time, String imgUrl, String vidUrl){
        this.distance = distance;
        this.objects = objects;
        this.cameraPosition = cameraPosition;
        this.date = date;
        this.notified = notified;
        this.seriousness = seriousness;
        this.time = time;
        this.imgUrl = imgUrl;
        this.vidUrl = vidUrl;
    }

    public Alert(){}

    public int getDistance() { return distance; }
    public String getObjects() {return objects; }
    public String getCameraPosition(){ return cameraPosition;}
    public String getDate(){ return date;}
    public Boolean getNotified(){ return notified;}
    public int getSeriousness(){ return seriousness;}
    public String getTime(){ return time;}
    public boolean equals(Alert b){
        boolean resp = false;
        if(distance == b.distance && cameraPosition.equals(b.cameraPosition) && date.equals(b.date) && notified == b.notified && seriousness == b.seriousness && time.equals(b.time)){
            resp = true;
        }
        return resp;
    }

    //ALERT! A " + alert.getObjects()+ " it's approaching from the "+ alert.getCameraPosition()+" of the car, distance for impact of "+alert.getDistance() + "meters."
    public String getImgUrl() { return imgUrl; }
    public String getVidUrl() { return vidUrl; }
    public String displayAlert(){
        String resp = "Alert! ";
        String[] objs = objects.split(" ");
        switch (objs.length){
            case 1:
                resp += " a "+objs[0]+" it's approaching from the "+ cameraPosition+" side of the car";
                resp += ", distance for impact of "+ distance + " meters. Watch out!";
                break;
            case 2:
                resp += " two posible objects approaching,";
                resp += " a "+objs[0]+" and a"+objs[1]+ " are approaching from the "+ cameraPosition+" side of the car";
                resp += ", distance for impact of "+ distance + " meters. Watch out!";
                break;
            case 3:
                resp += " three posible objects approaching,";
                resp += " a "+objs[0]+", a "+objs[1]+" and a"+objs[2]+" are approaching from the "+ cameraPosition+" side of the car";
                resp += ", distance for impact of "+ distance + " meters. Watch out!";
                break;
        }
        return resp;
    }
    public String toString() { return "Distance: "+distance+ " Objects: "+objects+" CameraPosition: "+cameraPosition+" Date: "+date+" Time: "+time+" Seriousness: "+seriousness+" imgUrl: "+imgUrl+" vidUrl: "+vidUrl;}
}
