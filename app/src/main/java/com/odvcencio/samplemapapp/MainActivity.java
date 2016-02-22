package com.odvcencio.samplemapapp;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.common.api.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.*;
import android.content.*;
import android.app.*;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.graphics.*;
import com.google.android.gms.maps.model.MarkerOptions;
import android.view.*;
import android.widget.*;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
        ResultCallback<LocationSettingsResult>, ActivityCompat.OnRequestPermissionsResultCallback {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    protected static final String TAG = "MainActivity";

    private GoogleMap m_map;
    boolean mapReady = false;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mLastLocation;

    MarkerOptions selfMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the map fragment
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //floating button init
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                CharSequence text = "Someone will be coming for you shortly... >:)";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        //build API Client
        buildGoogleApiClient();

        //build request
        buildLocationSettingsRequest();

        //check location settings
        checkLocationSettings();

    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        //call location request
        createLocationRequest();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        m_map = map;

        //where i would modify code to take user coordinates and display markers of nearby users

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            checkLocationSettings();
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                //update UI here
            }
        });
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                //update UI Here
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onConnected(Bundle connectionHint) {

        Log.i(TAG, "Connected to GoogleApiClient");
        if (mLastLocation == null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            LatLng mLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            CameraPosition selfLoc = CameraPosition.builder()
                    .target(mLatLng)
                    .zoom(14)
                    .bearing(0)
                    .tilt(45)
                    .build();
            m_map.moveCamera(CameraUpdateFactory.newCameraPosition(selfLoc));
            selfMarker = new MarkerOptions()
                    .position(mLatLng)
                    .title("Here you are!");

            m_map.addMarker(selfMarker);
            CircleOptions selfCircle = new CircleOptions()
                    .center(mLatLng)
                    .radius(3218.69)
                    .strokeColor(Color.BLUE);


            m_map.addCircle(selfCircle);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }
}