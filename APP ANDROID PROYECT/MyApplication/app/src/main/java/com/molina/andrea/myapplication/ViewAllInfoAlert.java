package com.molina.andrea.myapplication;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewAllInfoAlert extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "ViewAllInfoAlert";
    private String alertID;
    private String monthID;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;
    private StorageReference mStorageRef;

    EditText editTextFecha;
    EditText editTextHora;
    EditText editTextVelocidad;
    EditText editTextLimite;
    EditText editTextDistancia;
    EditText editTextAddress;
    EditText editTextObject;
    EditText editTextSeriousness;
    Button buttonAddInfo;
    Button  buttonBack;
    ImageView imageViewAlertImage;
    GoogleMap mainMapLocation;

    private AlertInfo alertInfo;

    public ViewAllInfoAlert() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        alertID = getArguments().getString("edttext").split(" ")[0];
        monthID = getArguments().getString("edttext").split(" ")[1];
        View v = inflater.inflate(R.layout.fragment_view_all_info_alert, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mainMapLocation);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser(); // getting the info of the user
        final String userId = user.getUid();
        Log.d(TAG, "User: "+userId);

        editTextFecha = (EditText) getView().findViewById(R.id.editTextFecha);
        editTextFecha.setFocusable(false);
        editTextFecha.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextFecha.setClickable(false);
        editTextHora = (EditText) getView().findViewById(R.id.editTextHora);
        editTextHora.setFocusable(false);
        editTextHora.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextHora.setClickable(false);
        editTextVelocidad = (EditText) getView().findViewById(R.id.editTextVelocidad);
        editTextVelocidad.setFocusable(false);
        editTextVelocidad.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextVelocidad.setClickable(false);
        editTextLimite = (EditText) getView().findViewById(R.id.editTextLimite);
        editTextLimite.setFocusable(false);
        editTextLimite.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextLimite.setClickable(false);
        editTextDistancia = (EditText) getView().findViewById(R.id.editTextDistancia);
        editTextDistancia.setFocusable(false);
        editTextDistancia.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextDistancia.setClickable(false);
        editTextAddress = (EditText) getView().findViewById(R.id.editTextAddress);
        editTextAddress.setFocusable(false);
        editTextAddress.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextAddress.setClickable(false);
        editTextObject = (EditText) getView().findViewById(R.id.editTextObject);
        editTextObject.setFocusable(false);
        editTextObject.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextObject.setClickable(false);
        editTextSeriousness = (EditText) getView().findViewById(R.id.editTextSeriousness);
        editTextSeriousness.setFocusable(false);
        editTextSeriousness.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
        editTextSeriousness.setClickable(false);

        Log.d(TAG, "AlertID: "+alertID);
        Log.d(TAG, " Month: "+monthID);

        databaseReference.child(userId).child("Alerts").child(monthID).child(alertID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                alertInfo = dataSnapshot.getValue(AlertInfo.class);
                editTextFecha.setText(alertInfo.getDate());
                editTextHora.setText(alertInfo.getTime());
                editTextVelocidad.setText(alertInfo.getSpeed() + " km/h");
                editTextLimite.setText(alertInfo.getSeriousness() + " (1 a 5)");
                editTextDistancia.setText(alertInfo.getDistance() + " m");
                editTextAddress.setText(alertInfo.getAddress());
                editTextObject.setText(alertInfo.getObjects());
                editTextSeriousness.setText(alertInfo.getLatitude() + ", " + alertInfo.getLongitude());
                LatLng ubicacionAlerta = new LatLng(alertInfo.getLatitude(), alertInfo.getLongitude());
                mainMapLocation.addMarker(new MarkerOptions().position(ubicacionAlerta).title("Alerta!"));
                mainMapLocation.moveCamera(CameraUpdateFactory.newLatLng(ubicacionAlerta));
                mainMapLocation.moveCamera(CameraUpdateFactory.newLatLng(ubicacionAlerta));
                mainMapLocation.animateCamera(CameraUpdateFactory.zoomTo(15));
                imageViewAlertImage = (ImageView) getView().findViewById(R.id.imageViewAlertImage);
                if(!alertInfo.getImgUrl().toString().contains(" ")){
                    Glide.with(getContext())
                            .load(alertInfo.getImgUrl().toString())
                            .into(imageViewAlertImage);
                }else{
                    String[] images = alertInfo.getImgUrl().toString().split(" ");
                    Glide.with(getContext())
                            .load(images[0])
                            .into(imageViewAlertImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "ERROR: " + databaseError.toString());
            }
        });

        buttonBack = (Button) getView().findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Button nav_alerts");
                FragmentManager manager = getFragmentManager();
                AlertsAddInfoFragment alertsAddInfoFragment = new AlertsAddInfoFragment();
                manager.beginTransaction().replace(R.id.mainLayout, alertsAddInfoFragment).commit();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mainMapLocation = googleMap;
    }
}
