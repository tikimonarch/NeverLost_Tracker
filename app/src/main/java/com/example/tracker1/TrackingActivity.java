package com.example.tracker1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.tracker1.Model.MyLocation;
import com.example.tracker1.Util.Common;
import com.example.tracker1.Util.GeofenceHelper;
import com.example.tracker1.Util.NotificationHelper;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.tracker1.databinding.ActivityTrackingBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback, ValueEventListener, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private ActivityTrackingBinding binding;
    private GeofenceHelper geofenceHelper;
//    private TrackingActivity mLocationSource;
    //private OnLocationChangedListener mListener;
    //private boolean mPaused;
    DatabaseReference trackingUserLocation;
    private String GEOFENCE_ID = "SOME_ID"; //suppose to get from app

    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    private List<LatLng> geoList = new ArrayList<LatLng>();
    {
        geoList.add(new LatLng(1.2984595157153607, 103.77169233683492));
        geoList.add(new LatLng(1.296509091308428, 103.77352234872173));
        geoList.add(new LatLng(1.2983375329616014, 103.77500346568435));
    }
    private final LatLng Geo1 = new LatLng(1.2984595157153607, 103.77169233683492);//E5
    private final LatLng Geo2 = new LatLng(1.296509091308428, 103.77352234872173);//CLB
    private final LatLng Geo3 = new LatLng(1.2983375329616014, 103.77500346568435);//Yusoff
    private float GEOFENCE_RADIUS = 200; //supposed to be user input
    private long startTime = System.currentTimeMillis();
    private long elapsedTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mLocationSource = new TrackingActivity();
        binding = ActivityTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarTracking.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, binding.appBarTracking.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = binding.navView;

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        //Handling navigation menu clicks here
                        int id = item.getItemId();
                        ;
                        //Log.e("","");
                        if (id == R.id.nav_find_people) {
                            startActivity(new Intent(TrackingActivity.this, AllPeopleActivity.class));
                            finish();
                        } else if (id == R.id.nav_home) {
                            startActivity(new Intent(TrackingActivity.this, HomeActivity.class));
                        } else if (id == R.id.nav_add_people) {
                            startActivity(new Intent(TrackingActivity.this, BuddyRequestActivity.class));
                        } else if (id == R.id.nav_token) {
                            startActivity(new Intent(TrackingActivity.this, TokenActivity.class));
                        } else if (id == R.id.nav_sign_out) {

                        }

                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                        return true;
                    }
                }
        );

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user_logged = (TextView) headerView.findViewById(R.id.txt_logged_email);
        if (Common.loggedUser != null)
            txt_user_logged.setText(Common.loggedUser.getEmail());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerEventRealtime();
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
    }

    private void registerEventRealtime() {
        trackingUserLocation = FirebaseDatabase.getInstance()
                .getReference(Common.PUBLIC_LOCATION)
                .child(Common.trackingUser.getUid());

        trackingUserLocation.addValueEventListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        elapsedTime = elapsedTime + (System.currentTimeMillis() - startTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mPaused = false;
        startTime = System.currentTimeMillis();
        trackingUserLocation.addValueEventListener(this);
    }

    @Override
    protected void onStop() {
        trackingUserLocation.removeEventListener(this);
//        mPaused = true;
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Enable zoom ui
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.setLocationSource(mLocationSource);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
        addCircle(Geo1,GEOFENCE_RADIUS);
        addCircle(Geo2,GEOFENCE_RADIUS);
        addCircle(Geo3,GEOFENCE_RADIUS);
        mMap.setOnMapLongClickListener(this);
    }

//    private void enableUserLocation() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
//                PackageManager.PERMISSION_GRANTED) {
//            mMap.setMyLocationEnabled(true);
//        } else { //Ask for permission
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                //Dialog
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
//            }
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //mMap.setMyLocationEnabled(true);
            } else {
                //We do not have permission
            }
        }
    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }

    private boolean checkForGeoFenceExit(LatLng Location, LatLng geoLoc, double geoRad){
        double distInMeters =  SphericalUtil.computeDistanceBetween(Location, geoLoc);
        if(distInMeters>geoRad){
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

        if (snapshot.getValue() != null) {
            MyLocation location = snapshot.getValue(MyLocation.class);

            //Add Marker
            LatLng userMarker = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(userMarker)
                    .title(Common.trackingUser.getEmail())
                    .snippet(Common.getDateFormatted(Common.convertTimeStampToDate(location.getTime()))));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker, 16f));
            for (LatLng geoCheck : geoList) {
                if (!checkForGeoFenceExit(userMarker,geoCheck,GEOFENCE_RADIUS)) {
                    startTime = System.currentTimeMillis();
                    elapsedTime = 0;
                }

            }
            elapsedTime = elapsedTime + (System.currentTimeMillis() - startTime);
            if(elapsedTime>300000){
                NotificationHelper notificationHelper = new NotificationHelper(this);
                notificationHelper.sendHighPriorityNotification("NeverLost Tracker","PLD Buddy has left the safe zone for 5 mins!", TrackingActivity.class);
                startTime = System.currentTimeMillis();
                elapsedTime = 0;
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                addCircle(latLng, GEOFENCE_RADIUS);
                addGeofence(latLng, GEOFENCE_RADIUS);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //showDialog
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        } else {
            addCircle(latLng, GEOFENCE_RADIUS);
            addGeofence(latLng, GEOFENCE_RADIUS);
        }
    }

    @SuppressLint("MissingPermission")
    private void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofenceRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();


        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("MapActivity", "onSuccess: Geofence Added.... ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d("MapActivity", "onFailure: " + errorMessage);
                    }
                });
    }

//    @Override
//    public void activate(@NonNull OnLocationChangedListener listener) {
//        mListener = listener;
//    }
//
//    @Override
//    public void deactivate() {
//        mListener = null;
//    }
}

