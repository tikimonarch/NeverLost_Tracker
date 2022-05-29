package com.example.tracker1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.tracker1.Model.MyLocation;
import com.example.tracker1.Util.Common;
import com.example.tracker1.Util.NotificationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class TokenActivity extends AppCompatActivity {
    static int PERMISSION_CODE = 100;
    private LatLng currentLocation;
    protected LocationManager mLocationManager;
    private List<LatLng> gtpList = new ArrayList<LatLng>();
    {
        //gtpList.add(new LatLng(1.2984595157153607, 103.77169233683492));
        gtpList.add(new LatLng(1.296724, 103.772856));
        gtpList.add(new LatLng(1.2983375329616014, 103.77500346568435));
    }
    int LOCATION_REFRESH_TIME = 0; // 15(15000) seconds to update
    int LOCATION_REFRESH_DISTANCE = 0; // 500 meters to update
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);
        if(ContextCompat.checkSelfPermission(TokenActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(TokenActivity.this, new String[]{Manifest.permission.CALL_PHONE},PERMISSION_CODE);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        }
    };

    public void ClickCall(View view) {
        String phoneNo = "96609685";
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+phoneNo));
        startActivity(intent);
    }

    public void VoiceNavi(View view) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=1.2983375329616014, 103.77500346568435&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private double checkForDist(LatLng Location, LatLng geoLoc){
        double distInMeters =  SphericalUtil.computeDistanceBetween(Location, geoLoc);
        return distInMeters;
    }

    public void VoiceNaviGTP(View view) {
        double distCheck = 1000000; //large dist for initial check comparison
        String gtpLocation = "google.navigation:q=1.2987585779024426, 103.7722492330566&mode=w";
        for (LatLng gtpCheck : gtpList) {
            if (checkForDist(currentLocation,gtpCheck)< distCheck) {
                distCheck = checkForDist(currentLocation,gtpCheck);
                gtpLocation = "google.navigation:q=" + Double.toString(gtpCheck.latitude) + ", " + Double.toString(gtpCheck.longitude) + "&mode=w";
            }
        }
        Uri gmmIntentUri = Uri.parse(gtpLocation);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

}