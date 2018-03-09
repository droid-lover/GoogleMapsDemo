package com.example.sachin.googlemapsdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    /**
     * Google Map Object.
     */
    private GoogleMap mMap;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Current Latlong Object
     */
    private double currentLat, currentLong;

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;


    /**
     * Represents a geographical location
     */
    protected Location mLastLocation;

    @BindView(R.id.map_activity_parent_layout)
    LinearLayout mParentLinearLayout;
    private Geocoder geocoder;
    private boolean animated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);


        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideCoarseLocation =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideCoarseLocation) {
            Log.i("MapsActivity", "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_location, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i("MapsActivity", "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {

        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {

                            try {
                                mLastLocation = task.getResult();

                                currentLat = mLastLocation.getLatitude();
                                currentLong = mLastLocation.getLongitude();

                                LatLng currentLatlong = new LatLng(currentLat, currentLong);
                                List<Address> addresses;
                                geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                                addresses = geocoder.getFromLocation(currentLat, currentLong, 1);
                                // Add a marker in Current Location in map with Address Name
                                mMap.addMarker(new MarkerOptions().position(currentLatlong).title(addresses.get(0).getAddressLine(0)));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatlong));

                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(currentLatlong)
                                        .zoom(17).build();
                                //Zoom in and animate the camera.
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                mMap.getUiSettings().setZoomControlsEnabled(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.w("MapsActivity", "getLastLocation:exception", task.getException());
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }


    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * showing the message
     */
    private void showSnackbar(final String text) {
//        View container = findViewById(R.id.main_activity_container);
//        if (container != null) {
        Snackbar.make(mParentLinearLayout, text, Snackbar.LENGTH_LONG).show();
        // }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

//    private void settingMapTypeDynamically() {
//
//        Timer t = new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        if (animated == false) {
//                            System.out.println("Hello World");
//                            animated = true;
//                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                        } else {
//                            System.out.println("---------");
//                            animated = false;
//                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//                        }
//                    }
//                });
//            }
//        }, 0, 10000);
//
//    }

}
