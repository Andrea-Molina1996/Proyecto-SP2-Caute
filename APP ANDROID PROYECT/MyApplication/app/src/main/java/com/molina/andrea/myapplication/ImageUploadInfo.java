package com.molina.andrea.myapplication;

/**
 * Created by amoli on 11/23/2017.
 */

public class ImageUploadInfo {

    public String imageName;
    public String imageURL;

    public ImageUploadInfo() {}

    public ImageUploadInfo(String name, String url) {
        this.imageName = name;
        this.imageURL= url;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageURL() {
        return imageURL;
    }

}