package com.molina.andrea.myapplication;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.DatePicker;
import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PictureGalleryFragment extends Fragment implements View.OnClickListener  {

    Button buttonDate;
    Button buttonGetPicture;
    ImageView imageViewAlerts;
    EditText editTextDate;
    ListView listImages;

    public static final String TAG = "PictureGalleryFragment";

    int year_x, month_x, day_x;

    static final int DIALOG_ID = 0;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    public PictureGalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_picture_gallery, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewAlerts = (ImageView) getView().findViewById(R.id.imageViewAlerts);
        //buttonDate = (Button) getView().findViewById(R.id.buttonDate);
        buttonGetPicture = (Button) getView().findViewById(R.id.buttonGetPicture);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        buttonDate.setOnClickListener(this);
        buttonGetPicture.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if( view == buttonDate){
            DialogFragment picker = new DatePickerFragment();
            picker.show(getFragmentManager(), "datePicker");
        }else if(view == buttonGetPicture){
            //getPicture();
        }

    }

    public void getPicture(){
        FirebaseUser user = mAuth.getCurrentUser();
        final String userId = user.getUid();
        editTextDate = (EditText) getView().findViewById(R.id.editTextDate);
        Log.d(TAG, "fecha: " + editTextDate.getText().toString());
        databaseReference.child(userId).child("Gallery").child(editTextDate.getText().toString().trim()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                ImageInfo images = dataSnapshot.getValue(ImageInfo.class);
                Log.d(TAG, "Value is: " + images);
                String[] imagesUrl = images.getImgUrl().split(" ");
                Log.d(TAG, "imagen url" + imagesUrl);
                mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(images.getImgUrl());
                Log.d(TAG, "firebaseReference: "+mStorageRef);
                imageViewAlerts = (ImageView) getView().findViewById(R.id.imageViewAlerts);
                // Load the image using Glide
                /*Glide.with(getActivity() )
                        .using(new FirebaseImageLoader())
                        .load(mStorageRef)
                        .into(imageViewAlerts);*/
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void setText(String text) {
        EditText t = (EditText) getView().findViewById(R.id.editTextDate);  //UPDATE
        t.setText(text);
    }



}
