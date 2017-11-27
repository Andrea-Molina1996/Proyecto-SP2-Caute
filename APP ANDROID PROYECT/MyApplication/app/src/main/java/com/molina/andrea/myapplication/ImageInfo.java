package com.molina.andrea.myapplication;

/**
 * Created by amoli on 11/5/2017.
 */

class ImageInfo {
    String imgUrl;
    String imgName;
    String alertID;

    public ImageInfo(String imgUrl, String imgName, String alertID){
        this.imgUrl = imgUrl;
        this.imgName = imgName;
        this.alertID = alertID;
    }

    public ImageInfo(){

    }

    public String getImgUrl(){return imgUrl;}
    public String getImgName(){return imgName;}
    public String getAlertID(){return alertID;}
}
