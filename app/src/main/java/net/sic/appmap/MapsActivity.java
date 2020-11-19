package net.sic.appmap;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int LOCATION_REFRESH_TIME = 2;
    private final int LOCATION_REFRESH_DISTANCE = 1;
    private final int FINE_LOCATION_PERMISSION_REQUEST = 1001;

    private Activity mActivity = null;
    private GoogleMap mMap = null;
    private View mMapView;
    private LocationManager mLocationManager = null;
    private Boolean isPermissionGrantedGoogleMap = false;
    private Marker mMarkerCurrent = null;
    private List<Marker> mMarkerTargetLocations = new ArrayList<>();
    private List<Polyline> mPolylines = new ArrayList<>();
    private List<Marker> drawMarkerLineLocations = new ArrayList<>();

    // Bearing
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private Float azimut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mActivity = this;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        currentLocationOnLoad();
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;
            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                    float bearing = (float) Math.toDegrees(azimut);
                    Log.v("", "bearing: " + bearing);
                    if (mMarkerCurrent != null) {
                        mMarkerCurrent.setRotation(bearing);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void clearMarkers(List<Marker> markers) {
        for (int idxMarker = 0; idxMarker < mMarkerTargetLocations.size(); idxMarker++) {
            mMarkerTargetLocations.get(idxMarker).remove();
            mMarkerTargetLocations.set(idxMarker, null);
        }
        markers.clear();
    }

    private GoogleMap.OnMapClickListener onCreateMarkerMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (mMarkerTargetLocations.size() >= 2) {
                clearMarkers(mMarkerTargetLocations);
            }
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Localtion index: " + (mMarkerTargetLocations.size() + 1));
            Marker marker = mMap.addMarker(markerOptions);
            mMarkerTargetLocations.add(marker);

            if (mMarkerTargetLocations.size() == 2) {
                Marker startMarker = mMarkerTargetLocations.get(0);
                Marker endMarker = mMarkerTargetLocations.get(1);

                drawDirection(startMarker.getPosition(), endMarker.getPosition());
            }
        }
    };

    private void drawDirection(LatLng start, LatLng end) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(mRoutingListener)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .key("AIzaSyBFxXP2wHUUyCeoKfm6aPP_sJpqZSHAAJQ")  //also define your api key here.
                .build();
        routing.execute();
    }

    private RoutingListener mRoutingListener = new RoutingListener() {
        @Override
        public void onRoutingFailure(RouteException e) {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        @Override
        public void onRoutingStart() {
            Toast.makeText(mActivity, "Finding Route...", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
            Marker startMarker = mMarkerTargetLocations.get(0);
            LatLng startLatlng = startMarker.getPosition();
            CameraUpdate center = CameraUpdateFactory.newLatLng(startLatlng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
            mMap.moveCamera(center);
            mMap.moveCamera(zoom);

            clearMarkers(mMarkerTargetLocations);
            mPolylines.clear();

            PolylineOptions polyOptions = new PolylineOptions();
            LatLng polylineStartLatLng = null;
            LatLng polylineEndLatLng = null;

            //add route(s) to the map using polyline
            for (int idxRoute = 0; idxRoute < routes.size(); idxRoute++) {

                if (idxRoute == shortestRouteIndex) {
                    polyOptions.color(getResources().getColor(R.color.black));
                    polyOptions.width(7);
                    polyOptions.addAll(routes.get(shortestRouteIndex).getPoints());
                    Polyline polyline = mMap.addPolyline(polyOptions);
                    polylineStartLatLng = polyline.getPoints().get(0);
                    int k = polyline.getPoints().size();
                    polylineEndLatLng = polyline.getPoints().get(k - 1);
                    mPolylines.add(polyline);
                } else {

                }
            }

            //Add Marker on route starting position
            MarkerOptions fromMarker = new MarkerOptions();
            fromMarker.position(polylineStartLatLng);
            fromMarker.title("From");
            Marker fromMarkerLine = mMap.addMarker(fromMarker);
            drawMarkerLineLocations.add(fromMarkerLine);

            //Add Marker on route ending position
            MarkerOptions toMarker = new MarkerOptions();
            toMarker.position(polylineEndLatLng);
            toMarker.title("To");
            Marker toMarkerLine = mMap.addMarker(toMarker);
            drawMarkerLineLocations.add(toMarkerLine);
        }

        @Override
        public void onRoutingCancelled() {
            Toast.makeText(mActivity, "onRoutingCancelled", Toast.LENGTH_LONG).show();
        }
    };

//    private double bearingBetweenLocations(LatLng latLng1,LatLng latLng2) {
//
//        double PI = 3.14159;
//        double lat1 = latLng1.latitude * PI / 180;
//        double long1 = latLng1.longitude * PI / 180;
//        double lat2 = latLng2.latitude * PI / 180;
//        double long2 = latLng2.longitude * PI / 180;
//
//        double dLon = (long2 - long1);
//
//        double y = Math.sin(dLon) * Math.cos(lat2);
//        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
//                * Math.cos(lat2) * Math.cos(dLon);
//
//        double brng = Math.atan2(y, x);
//
//        brng = Math.toDegrees(brng);
//        brng = (brng + 360) % 360;
//
//        return brng;
//    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            setShowCurrentLocationMarker(currentLatLng);
        }
    };

    private void setShowCurrentLocationMarker(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        //Add Marker
        MarkerOptions currentMarker = new MarkerOptions();
        currentMarker.position(latLng);
        currentMarker.title("Current");
        if (mMarkerCurrent != null) {
            mMarkerCurrent.remove();
            mMarkerCurrent = null;
        }
        mMarkerCurrent = mMap.addMarker(currentMarker);
    }

    private void currentLocationOnLoad() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
            return;
        }
        isPermissionGrantedGoogleMap = true;
        if (mLocationManager != null) {
            List<String> providers = mLocationManager.getAllProviders();
            for (String provider : providers) {
                mLocationManager.requestLocationUpdates(provider, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGrantedGoogleMap = true;
                    currentLocationOnLoad();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        List<String> providers = mLocationManager.getAllProviders();
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = mLocationManager.getLastKnownLocation(provider);
            setShowCurrentLocationMarker(new LatLng(location.getLatitude(), location.getLongitude()));
            mLocationManager.requestLocationUpdates(provider, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        }


        // Compass (la bàn)
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Button icon blue mylocation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 0, 400);

        mMap.setOnMapClickListener(onCreateMarkerMapClickListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }
}