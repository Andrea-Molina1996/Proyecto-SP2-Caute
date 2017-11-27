package com.molina.andrea.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Created by amoli on 11/10/2017.
 */

public class ImageListAdapter {

    private Context context;
    private String[] urls;
    private StorageReference mStorageRef;
    public static final String TAG = "ImageListAdapter";

    public ImageListAdapter(Context context, String[] urls){
        this.context = context;
        this.urls = urls;
    }


}
