package com.csci448.agwa.mapit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by amosgwa on 4/9/17.
 * TODO :
 * Add database
 * Query marker data from weather
 * Clean all markers and database
 */

public class fragment_map extends Fragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "<MAP>";
    private Context mContext;
    private LayoutInflater mInflater;
    private FragmentActivity mActivity;
    private View mView;

    // Data pins
    private HashMap<String, Pin> mPins = new HashMap<String, Pin>();

    // Database
    private PinsDataSource datasource;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private CameraPosition mCameraPosition;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;


    // Return a new instance of the fragment.
    public static fragment_map newInstance() {
        return new fragment_map();
    }

    // Inflate the fragment in the parent activity.
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Declare the inflater from the parent.
        mInflater = inflater;

        View v = inflater.inflate(R.layout.fragment_map, container, false);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Declare the mContext here
        mActivity = this.getActivity();
        mContext = mActivity.getApplicationContext();

        // Open database.
        datasource = new PinsDataSource(mContext);
        datasource.open();

        // Generate dummy data.
        generateDummyData();

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .enableAutoManage(this.getActivity() /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        // Add listener to the floating button for checking in.
        FloatingActionButton checkInFab = (FloatingActionButton) this.getActivity().findViewById(R.id.check_in_button);
        checkInFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Checked In");
                checkIn();
            }
        });
    }

    private void checkIn() {
        // Move to current location.
        getDeviceLocation();
        // Extract latlng for current location.
        LatLng currLocation = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        // Add marker to the current location.
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(currLocation));
        // First create a new pin.
        Pin pin = new Pin(new Date(), marker.getPosition());
        // Append to the local variable.
        mPins.put(marker.getId(), pin);
        // Append to the database.
        // Show info window for the check in.
        marker.showInfoWindow();
        // Show the snackbar.
        showSnackBar(pin);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Build the mpa.
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        Log.d(TAG, "Connected");
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Play services connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0,50,0,0);

        // On tapping the marker.
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Get the selected pin.
                Pin selectedPin = mPins.get(marker.getId());
                // Move the camera to the marker.
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPin.getPos(), DEFAULT_ZOOM));
                // Show the info window.
                marker.showInfoWindow();
                // Show the snack bar.
                showSnackBar(selectedPin);
                return true;
            }
        });

        // On tapping the popup.
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                // Get information on the info window click.
            }
        });

        // Setup a custom window adapter to show custom data.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LatLng pos = marker.getPosition();

                // Inflate the layouts for the info window.
                View infoWindow = mInflater.inflate(R.layout.custom_info_contents, null);

                TextView latLngInfo = (TextView) infoWindow.findViewById(R.id.title);
                latLngInfo.setText("lat/lng: ("+ String.valueOf(pos.latitude) + ","+ String.valueOf(pos.longitude) + ")");

                return infoWindow;
            }
        });

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Map is ready and query all of the pins.
        ArrayList<Pin> values = datasource.getAllPins();

        // Generate the pins hash map.
        generateMarkers(values);

        // Show makers.
        // putMarkers();
    }

    /**
     * Generate Markers and build a hashmap with markers' id and their pins.
     * @param values
     */
    private void generateMarkers(ArrayList<Pin> values) {
        for(Pin pin : values) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pin.getPos()));
            mPins.put(marker.getId(), pin);
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            Log.d(TAG, "Camera Moved");
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            Log.d(TAG, "Location found");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
        } else {
            // The user hasn't granted permission.
        }
    }

    private void showSnackBar(Pin pin) {
        String first_line = "You were here : " + pin.getTime().toString();
        String second_line = "The weather is nice";
        Snackbar.make(getView(), first_line + "\n" + second_line, Snackbar.LENGTH_LONG).show();
    }

    private void generateDummyData() {
        // Query data form the database.
        LatLng a = new LatLng(39.744850, -105.231654);
        LatLng b = new LatLng(39.742961, -105.230570);
        LatLng c = new LatLng(39.742062, -105.231214);
        ArrayList<LatLng> positions = new ArrayList<LatLng>();
        positions.add(a);
        positions.add(b);
        positions.add(c);

        for(LatLng pos : positions) {
            Pin pin = new Pin(new Date(), pos);
            datasource.addPin(pin);
        }
    }

}
