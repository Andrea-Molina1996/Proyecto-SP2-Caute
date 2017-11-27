package com.molina.andrea.myapplication;


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private GoogleApiClient googleClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private LatLng destinyLocation;
    private Marker currentLocationMarker;
    private FusedLocationProviderClient mFusedLocationClient;

    private EditText editTextPlace;
    private ImageButton imageButtonSearchPlace;
    private ImageButton imageButtonHospital;
    private ImageButton imageButtonGas;
    private ImageButton imageButtonDinner;
    private ImageButton imageButtonGrocery;
    private ImageButton imageButtonMall;
    private ImageButton imageButtonMovie;
    private ImageButton imageButtonConvenienceStore;
    private ImageButton imageButtonCaffe;
    private String nearByPlace;
    private String url;

    private FloatingActionButton fabGetDirections;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude, end_latitude, end_longitude;

    public static final int PERMISSION_REQUEST_LOCATION_CODE =  99;
    final String TAG = "MainMapFragment";

    public MainMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main_map, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mainMap);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        editTextPlace = (EditText) getView().findViewById(R.id.editTextPlace);
        imageButtonSearchPlace = (ImageButton) getView().findViewById(R.id.imageButtonSearchPlace);
        imageButtonHospital = (ImageButton) getView().findViewById(R.id.imageButtonHospital);
        imageButtonGas = (ImageButton) getView().findViewById(R.id.imageButtonGas);
        imageButtonDinner = (ImageButton) getView().findViewById(R.id.imageButtonDinner);
        imageButtonGrocery = (ImageButton) getView().findViewById(R.id.imageButtonGrocery);
        imageButtonMall = (ImageButton) getView().findViewById(R.id.imageButtonMall);
        imageButtonMovie = (ImageButton) getView().findViewById(R.id.imageButtonMovie);
        imageButtonConvenienceStore = (ImageButton) getView().findViewById(R.id.imageButtonConvenienceStore);
        imageButtonCaffe = (ImageButton) getView().findViewById(R.id.imageButtonCaffe);

        imageButtonSearchPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = editTextPlace.getText().toString();
                List<Address> addressList = null;
                MarkerOptions mo = new MarkerOptions();

                if(!location.equals("")){
                    Geocoder geocoder = new Geocoder(getActivity());
                    try {
                        addressList = geocoder.getFromLocationName(location, 5); // max location
                        for(int i = 0; i < addressList.size(); i++){
                            Address myAddress = addressList.get(i);
                            LatLng latlng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                            destinyLocation = latlng;
                            mo.position(latlng);
                            mo.title(myAddress.getFeatureName());
                            mMap.addMarker(mo);
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        imageButtonHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_hospital");
                nearByPlace = "hospital";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        imageButtonGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_gas");
                nearByPlace = "gas_station";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        imageButtonDinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_dinnner");
                nearByPlace = "restaurant";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        imageButtonGrocery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_grocery");
                nearByPlace = "grocery_or_supermarket";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        imageButtonMall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_mall");
                nearByPlace = "shopping_mall";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        imageButtonMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_movie");
                nearByPlace = "movie_theater";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        imageButtonConvenienceStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_convenience_store");
                nearByPlace = "convenience_store";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        imageButtonCaffe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                getLastLocation();
                GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                Object dataTransfer[] = new Object[2];
                Log.d(TAG, "R.id.B_convenience_store");
                nearByPlace = "cafe";
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                url = getUrl(latitude, longitude, nearByPlace);
                Log.d(TAG, "URL: "+ url);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                getNearbyPlacesData.execute(dataTransfer);
            }
        });

        fabGetDirections = (FloatingActionButton) getView().findViewById(R.id.fabGetDirections);

        fabGetDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation(); // getting the users last location
                LatLng destination = new LatLng(14.651205, -90.542362);
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                end_latitude = destinyLocation.latitude;
                end_longitude = destinyLocation.longitude;
                Object[] dataTransfer = new Object[3];
                String url = getDirectionsUrl();
                Log.d(TAG, "URL: "+ url);
                GetDirectionsData getDirectionsData = new GetDirectionsData();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = destination;
                getDirectionsData.execute(dataTransfer);

                Snackbar.make(view, "Obteniendo ruta...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                CameraPosition cameraPosition=new CameraPosition.Builder()
                        .target(new LatLng(latitude, longitude)) // Sets the center of the map
                        .tilt(45) // Sets the tilt of the camera to 20 degrees
                        .zoom(20) // Sets the zoom
                        .bearing(((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) // Sets the orientation of the camera to east
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.style_json));
        if(!success){Log.e(TAG, "Style parsing failed.");}
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                buildGoogleApiClient();
                Log.d(TAG, "Google Client: " + googleClient.toString());
                mMap.setMyLocationEnabled(true);
                mMap.setTrafficEnabled(true);
            }
        }else{
            buildGoogleApiClient();
            Log.d(TAG, "Google Client: " + googleClient.isConnected());
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
        }

    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            lastLocation = task.getResult();
                            Log.d(TAG, "Latitude: " + lastLocation.getLatitude());
                            Log.d(TAG, "Longitude: " + lastLocation.getLongitude());
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            Snackbar.make(getView().findViewById(R.id.mainLayout), getString(R.string.no_location_detected), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                });
    }

    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);
            }else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION_CODE);
            }
            return false;
        }else{
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission is granted
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(googleClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else{ //permission is denied
                    Toast.makeText(getActivity(), "Permission Denied!", Toast.LENGTH_SHORT);
                }
        }
    }

    protected synchronized  void buildGoogleApiClient(){
        googleClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        googleClient.connect();
    }

    private String getDirectionsUrl(){
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyBW0Vygb229Ih8p2wcKn-mR8vHdBp7rHnM");
        googleDirectionsUrl.append("&language="+"es");
        return googleDirectionsUrl.toString();
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace){
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyBfKx2ZW3y1ljylq0HzFj6bdoKsnq3x1Wo");

        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        Log.d(TAG, "onConnected");
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkSelfPermission ACCESS_FINE_LOCATION GRANTED");
            LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: "+i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: "+connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        Log.d(TAG, "Location: "+lastLocation);
        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions(); // set properties to the marker
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if(googleClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleClient, this);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;
    }

}
