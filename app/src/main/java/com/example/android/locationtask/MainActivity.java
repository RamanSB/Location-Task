package com.example.android.locationtask;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.locationtask.R.styleable.View;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, Toolbar.OnMenuItemClickListener {

    //Return GoogleMap using OnMapReadyCallbackListener and getMapAsync method
    private GoogleMap mMap;
    private Button userButton;
    private boolean isShiftStarted = false;
    private boolean isLocationEnabled = false;

    public long startTime = 0;
    public long shiftTime;

    public static final int PERMISSION_LOCATION_REQUEST_CODE = 68;
    public static final String CLASS_TAG = MainActivity.class.getName();

    public double currentLongitude;
    public double currentLatitude;


    private Toolbar mainToolbar;
    public PolylineOptions mainPolyLineOption;
    public Polyline shiftPolyline;

    private FloatingActionButton fab;
    private CoordinatorLayout coordinatorLayout;
    //Build GoogleApiClient in onCreate and connect the client in onResume. Hence disconnect in onPause
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private List<LatLng> points;
    private AlertDialog entryDialog;
    private CardView cardView;
    private TextView shiftDurationText;


   //public ViewStub viewStub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(CLASS_TAG, "onCreate called.");



        super.onCreate(savedInstanceState);
        //Checks if App has GPS location enabled.
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );


        entryDialog = new AlertDialog.Builder(this).setTitle("Location Required").setMessage(
                "This app requires that the location is enabled."
        ).setPositiveButton("Enable Location", new Dialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(gpsOptionsIntent);
            }
        }).setNegativeButton("Exit", new Dialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                System.exit(0);
            }
        }).create();

        if(!isLocationEnabled){
            entryDialog.show();
            isLocationEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }


        points = new ArrayList<LatLng>();
        mainPolyLineOption = new PolylineOptions();
        mainPolyLineOption.color(Color.BLUE);
        setContentView(R.layout.activity_main);
        cardView = (CardView) findViewById(R.id.shift_time_display);
        shiftDurationText = (TextView) cardView.findViewById(R.id.tv_shift_duration);
        mainToolbar = (Toolbar) findViewById(R.id.tb);
        mainToolbar.setOnMenuItemClickListener(this);
        userButton = (Button) findViewById(R.id.user_button);
        SupportMapFragment mMapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFrag.getMapAsync(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(5000).setFastestInterval(1000);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShiftStarted = !isShiftStarted;
                Log.v(CLASS_TAG, "Shift started:" + isShiftStarted);

                if (isShiftStarted) {

                    if (mGoogleApiClient.isConnected()) {
                        startTime = System.currentTimeMillis();
                        mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Start Of Shift"));
                        mainPolyLineOption.add(new LatLng(currentLatitude, currentLongitude));
                    }
                    userButton.setText(R.string.end_text);
                    userButton.setTextColor(Color.WHITE);
                    userButton.setBackgroundColor(Color.RED);

                } else {
                    if (mGoogleApiClient.isConnected()) {
                        System.out.println(points.get(points.size() - 1));
                        mMap.addMarker(new MarkerOptions().position(points.get(points.size() - 1)).title("End"));
                        mainPolyLineOption.add(new LatLng(currentLatitude, currentLongitude));
                        shiftPolyline = mMap.addPolyline(mainPolyLineOption);
                        shiftPolyline.setPoints(points);

                    }
                    shiftTime = (System.currentTimeMillis() - startTime) / 1000;
                    String formattedShiftTime = Utilities.formatSeconds(shiftTime);


                    /*viewStub = (ViewStub) findViewById(R.id.view_stub);
                    //Stub already inflated
                    if(viewStub == null){
                        viewStub = (ViewStub) findViewById(R.id.inflated_stub);
                    }
                    View inflated = viewStub.inflate();
                    TextView shiftDisplayText = (TextView) inflated.findViewById(R.id.tv_shift_duration);
                    shiftDisplayText.setText(formattedShiftTime);
                    */
                    shiftDurationText.setText(formattedShiftTime);
                    cardView.setVisibility(android.view.View.VISIBLE);
                    userButton.setText(R.string.start_text);
                    userButton.setTextColor(Color.BLACK);
                    userButton.setBackgroundColor(Color.GREEN);
                    Log.v(CLASS_TAG, points.toString() + "");
                }

            }

        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleApiClient.isConnected()) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(currentLatitude, currentLongitude)));
                }
            }
        });

        mainToolbar.setTitle("Location Task");
        mainToolbar.inflateMenu(R.menu.tb_menu);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.v(CLASS_TAG, "Permission not granted");
            Snackbar.make(coordinatorLayout, "Location permission required.", Snackbar.LENGTH_INDEFINITE).setAction("OK",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS}, PERMISSION_LOCATION_REQUEST_CODE);
                        }
                    }).setActionTextColor(Color.BLUE).show();

        }

        LatLng home = new LatLng(51.525893, 0.039095);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, 15.5f));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tb_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(CLASS_TAG, "onResume called");
        mGoogleApiClient.connect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onStop() {
        Log.d(CLASS_TAG, "onStop called.");
        super.onStop();


    }

    protected void onDestroy(){
        Log.d(CLASS_TAG, "onDestroy called.");
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(CLASS_TAG, "Code:" + requestCode);
    }

    //LocationListener Interface
    @Override
    public void onLocationChanged(Location location) {
        //  Log.d(CLASS_TAG, "Location Changed");
        handleNewLocation(location);
    }

    //onConnected & onConnectionSuspended are abstract methods from the CoonnectionCallbacks interface

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(CLASS_TAG, "Location services connected/");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            } else {
                handleNewLocation(location);
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(CLASS_TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Fill out if connectionFailed
        Log.d(CLASS_TAG, "Connection Failed.");
    }

    private void handleNewLocation(Location location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        if (isShiftStarted) {
            LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);
            points.add(currentLatLng);
        }


        /*
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        */

        //mMap.addMarker(options);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {

            case R.id.clear_marker_item:
                Log.w(CLASS_TAG, "Clear markers");
                if(isShiftStarted) {
                    AlertDialog dialogBox = new AlertDialog.Builder(this).setMessage("Clearing the map will result in ending your shift. Would you like to end your shift?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mMap.clear();
                                    cardView.setVisibility(android.view.View.INVISIBLE);
                                    if (isShiftStarted != false) {
                                        isShiftStarted = !isShiftStarted;
                                        userButton.setText(R.string.start_text);
                                        userButton.setTextColor(Color.BLACK);
                                        userButton.setBackgroundColor(Color.GREEN);
                                    }

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).create();
                    dialogBox.show();
                    return true;

                }else{
                  //  viewStub.setVisibility(android.view.View.GONE);
                    mMap.clear();
                    cardView.setVisibility(android.view.View.INVISIBLE);
                    return true;
                }



            case R.id.share_item:
                Toast.makeText(this, "Route Shared", Toast.LENGTH_LONG).show();
                return true;
        }


        return false;
    }




}