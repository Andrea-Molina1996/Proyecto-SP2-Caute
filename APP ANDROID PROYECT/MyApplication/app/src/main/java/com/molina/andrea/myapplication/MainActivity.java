package com.molina.andrea.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, com.google.android.gms.location.LocationListener, TextToSpeech.OnInitListener {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    protected Location mLastLocation;

    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;

    private LocationManager locationManager;
    private LocationListener locationListener;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1   ; //10*1 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second

    double mySpeed, maxSpeed;
    private final String Speed = null;

    PrefManager manager;
    MainMapFragment mainMapFragment;

    private String[] months_selection = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    private TextView textViewEmail;
    private TextView textViewUsername;
    private ImageView profile_image_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        FragmentManager manager = getSupportFragmentManager();
        mainMapFragment = new MainMapFragment();
        manager.beginTransaction().replace(R.id.mainLayout, mainMapFragment).commit();

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(); // Getting the instance of the database service
        FirebaseUser user = mAuth.getCurrentUser(); // getting the info of the user
        final String userId = user.getUid();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // i use this to get the location of the user for the alert
        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        Log.d(TAG, "Before user sign in check" );
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent i = new Intent(getApplicationContext(), SignUpOrSignIn.class);
                    startActivity(i);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        Date currentTime = Calendar.getInstance().getTime();
        final int month = currentTime.getMonth();
        Log.d(TAG, "Month: "+month+" str: "+ months_selection[month]);
        databaseReference.child(userId).child("Alerts").child(months_selection[month]).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    Alert alert = dataSnapshot.getValue(Alert.class);
                    /*Log.d(TAG, "Alert= "+alert.toString());
                    Log.d(TAG, "Notified? "+alert.getNotified());
                    Log.d(TAG, "Location " +mLastLocation);*/
                    if(mLastLocation != null){
                        getLastLocation();
                        if(!alert.getNotified()){
                            Log.d(TAG, "Alert = "+alert.toString());
                            int speed=(int) ((mLastLocation.getSpeed()*3600)/1000);
                            List<Address> addresses = geocoder.getFromLocation( mLastLocation.getLatitude(), mLastLocation.getLongitude(),1);
                            String address = addresses.get(0).getAddressLine(0);
                            AlertInfo alertInfo = new AlertInfo(alert, speed, mLastLocation.getLatitude(), mLastLocation.getLongitude(), address);
                            alertInfo.setNotified();
                            String alertKey = ""+dataSnapshot.getKey();
                            Log.d(TAG, "AlertInfo = "+alertInfo.toString());
                            Log.d(TAG, "alertKey: "+alertKey);
                            String textAlertNotification = alert.displayAlert();
                            //String textAlertNotification = "ALERT! A " + alert.getObjects()+ " it's approaching from the "+ alert.getCameraPosition()+" of the car, distance for impact of "+alert.getDistance() + "meters.";
                            Toast.makeText(getApplicationContext(), textAlertNotification, Toast.LENGTH_LONG).show();
                            speakWords(textAlertNotification);
                            databaseReference.child(userId).child("Alerts").child(months_selection[month]).child(alertKey).setValue(alertInfo);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        textViewEmail = (TextView) header.findViewById(R.id.textViewEmail);
        textViewUsername = (TextView) header.findViewById(R.id.textViewUsername);


        profile_image_view = (ImageView) header.findViewById(R.id.profile_image_view);

        Log.d(TAG, "Provider: "+FirebaseAuth.getInstance().getCurrentUser().getProviderId());
        if(user != null){
            if(user.getPhotoUrl() != null){
                Glide.with(getApplicationContext())
                        .load(user.getPhotoUrl().toString())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(profile_image_view);
            }
            if(user.getDisplayName() != null){
                textViewUsername.setText(user.getDisplayName());
            }
            if(user.getEmail() != null){
                textViewEmail.setText(user.getEmail());
            }
        }
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "-- ON START --");
        if (!checkPermissions()) {
            Log.d(TAG, "getLastLocation()");
            requestPermissions();
            getLastLocation();
        } else {
            Log.d(TAG, "Getting last location");
            getLastLocation();
        }
    }

    private void speakWords(String speech) {
        //implement TTS here
        Log.d(TAG, "speakWords: "+speech);
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
        }else if (i == TextToSpeech.ERROR) {
            Toast.makeText(getApplicationContext(), "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(getApplicationContext(), this);
            }else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            Log.d(TAG, "Latitude: " + mLastLocation.getLatitude());
                            Log.d(TAG, "Longitude: " + mLastLocation.getLongitude());
                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            Snackbar.make(findViewById(R.id.mainLayout), getString(R.string.no_location_detected), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    }
                });
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.
                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.
                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager manager = getSupportFragmentManager();

        if (id == R.id.nav_navigation) {
            Log.d(TAG, "Button nav_navigation");
            mainMapFragment = new MainMapFragment();
            manager.beginTransaction().replace(R.id.mainLayout, mainMapFragment).commit();
        } else if (id == R.id.nav_gallery) {
            Log.d(TAG, "Button nav_gallery");
            /*PictureGalleryFragment pictureGalleryFragment = new PictureGalleryFragment();
            manager.beginTransaction().replace(R.id.mainLayout, pictureGalleryFragment).commit();*/
            Toast.makeText(getApplicationContext(), "Esta funcionalidad esta desactivada por el momento, " +
                    "las fotografias las puede ven en la ventana Alert", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_assistent) {
            Log.d(TAG, "Button nav_assistent");
            ChatAsistenteFragment chatAsistenteFragment = new ChatAsistenteFragment();
            manager.beginTransaction().replace(R.id.mainLayout, chatAsistenteFragment).commit();
        } else if (id == R.id.nav_statistics) {
            Log.d(TAG, "Button nav_statistics");
            StatisticsFragment statisticsFragment = new StatisticsFragment();
            manager.beginTransaction().replace(R.id.mainLayout, statisticsFragment).commit();
        } else if (id == R.id.nav_edit_profile) {
            Log.d(TAG, "Button nav_edit_profile");
            EditProfileFragment editProfileFragment = new EditProfileFragment();
            manager.beginTransaction().replace(R.id.mainLayout, editProfileFragment).commit();
        } else if (id == R.id.nav_settings){
            Log.d(TAG, "Button nav_settings");
        } else if(id == R.id.nav_bluethood){
            Log.d(TAG, "Button nav_bluethood");
            BluetoothSettingsFragment bluetoothSettingsFragment = new BluetoothSettingsFragment();
            manager.beginTransaction().replace(R.id.mainLayout, bluetoothSettingsFragment).commit();
        } else if (id == R.id.nav_sign_out) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            mAuth.signOut();
                            startActivity(new Intent(getApplicationContext(), SignUpOrSignIn.class));
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("¿Estás seguro?").setPositiveButton("Si", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        }else if(id == R.id.nav_alerts){
            Log.d(TAG, "Button nav_alerts");
            AlertsAddInfoFragment alertsAddInfoFragment = new AlertsAddInfoFragment();
            manager.beginTransaction().replace(R.id.mainLayout, alertsAddInfoFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        mySpeed = location.getSpeed();
    }


}
