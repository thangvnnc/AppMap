package net.sic.appmap.ui.map;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import net.sic.appmap.R;
import net.sic.appmap.service.GPSService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private final int LOCATION_REFRESH_TIME = 2;
    private final int LOCATION_REFRESH_DISTANCE = 1;
    private final int FINE_LOCATION_PERMISSION_REQUEST = 1001;

    private GoogleMap mMap = null;
    private View mMapView;
    private LocationManager mLocationManager = null;
    private Boolean isPermissionGrantedGoogleMap = false;
    private Marker mMarkerCurrent = null;
    private List<Marker> mMarkerTargetLocations = new ArrayList<>();
    private List<Polyline> mPolylines = new ArrayList<>();
    private List<Marker> mDrawMarkerLineLocations = new ArrayList<>();
    private Button btnCenterMarker = null;
    private Button btnReverse  = null;
    private Button btnDriving = null;
    private Switch swSync = null;
    private View root = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_maps, container, false);

        btnCenterMarker = root.findViewById(R.id.btnCenterMarker);
        btnCenterMarker.setOnClickListener(btnCenterClick);

        btnReverse = root.findViewById(R.id.btnReverse);
        btnReverse.setOnClickListener(btnReverseClick);

        btnDriving = root.findViewById(R.id.btnDriving);
        btnDriving.setOnClickListener(btnDrivingClick);

        swSync = root.findViewById(R.id.swSync);

        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), getString(R.string.key_google_api), Locale.US);
        }
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(placeSelectionListener);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mMapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

        mLocationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        currentLocationOnLoad();

        getContext().registerReceiver(locationBroadcastReceiver, new IntentFilter(GPSService.KEY_BROADCAST_LOCATION));
        return root;
    }

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Float bearing = intent.getFloatExtra("bearing", 0);
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);
            Log.i("MapsFragment bearing:", "Bearing: " + bearing);
            Log.i("MapsFragment lat:", "Lat: " + lat);
            Log.i("MapsFragment lng:", "Lng: " + lng);
            if (swSync.isChecked()) {
                LatLng currentLatLng = new LatLng(lat, lng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(locationBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar supportActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (supportActionBar != null){
            supportActionBar.hide();
        }
    }

    private void startIntentGoogleMapApp(LatLng fromLatLng, LatLng toLatLng) {
        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
                fromLatLng.latitude, fromLatLng.longitude, "Home Sweet Home",
                toLatLng.latitude, toLatLng.longitude, "Where the party is at");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private Marker mPlaceMarkerLine = null;
    private final PlaceSelectionListener placeSelectionListener = new PlaceSelectionListener() {

        @Override
        public void onPlaceSelected(@NonNull Place place) {
            if (mMap != null) {
                if (mPlaceMarkerLine != null) {
                    mPlaceMarkerLine.remove();
                    mPlaceMarkerLine = null;
                }
                checkAndDrawDirection(place.getLatLng());
            }
        }

        @Override
        public void onError(@NonNull Status status) {
            // TODO: Handle the error.
            Log.i("MapsActivity", "An error occurred: " + status);
        }
    };

    private final View.OnClickListener btnCenterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LatLng centerLatLang = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
            checkAndDrawDirection(centerLatLang);
        }
    };

    private final View.OnClickListener btnReverseClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Collections.reverse(mDrawMarkerLineLocations);
            if (mDrawMarkerLineLocations.size() == 2) {
                Marker startMarker = mDrawMarkerLineLocations.get(0);
                Marker endMarker = mDrawMarkerLineLocations.get(1);

                drawDirection(startMarker.getPosition(), endMarker.getPosition());
            }
        }
    };
    private final View.OnClickListener btnDrivingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mDrawMarkerLineLocations.size() == 2) {
                Marker startMarker = mDrawMarkerLineLocations.get(0);
                Marker endMarker = mDrawMarkerLineLocations.get(1);
                startIntentGoogleMapApp(startMarker.getPosition(), endMarker.getPosition());
            }
        }
    };

    private void clearMarkers(List<Marker> markers) {
        for (int idxMarker = 0; idxMarker < markers.size(); idxMarker++) {
            markers.get(idxMarker).remove();
            markers.set(idxMarker, null);
        }
        markers.clear();
    }

    private void clearPolylines(List<Polyline> polylines) {
        for (int idxPolyline = 0; idxPolyline < polylines.size(); idxPolyline++) {
            polylines.get(idxPolyline).remove();
            polylines.set(idxPolyline, null);
        }
        polylines.clear();
    }

    private final GoogleMap.OnMapClickListener onCreateMarkerMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            checkAndDrawDirection(latLng);
        }
    };

    private void checkAndDrawDirection(LatLng latLng) {
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

    private void drawDirection(LatLng start, LatLng end) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(mRoutingListener)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .key(getString(R.string.key_google_api))  //also define your api key here.
                .build();
        routing.execute();
    }

    private final RoutingListener mRoutingListener = new RoutingListener() {
        @Override
        public void onRoutingFailure(RouteException e) {
//            View parentLayout = root.findViewById(android.R.id.content);
//            Snackbar snackbar = Snackbar.make(parentLayout, e.getMessage(), Snackbar.LENGTH_LONG);
//            snackbar.show();
        }

        @Override
        public void onRoutingStart() {
//            Toast.makeText(getActivity(), "Finding Route...", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
            clearMarkers(mDrawMarkerLineLocations);
            clearMarkers(mMarkerTargetLocations);
            clearPolylines(mPolylines);

            PolylineOptions polyOptions = new PolylineOptions();
            LatLng polylineStartLatLng = null;
            LatLng polylineEndLatLng = null;

            //add route(s) to the map using polyline
            for (int idxRoute = 0; idxRoute < routes.size(); idxRoute++) {

                if (idxRoute == shortestRouteIndex) {
                    polyOptions.color(getResources().getColor(R.color.line));
                    polyOptions.width(16);
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
            mDrawMarkerLineLocations.add(fromMarkerLine);

            //Add Marker on route ending position
            MarkerOptions toMarker = new MarkerOptions();
            toMarker.position(polylineEndLatLng);
            toMarker.title("To");
            Marker toMarkerLine = mMap.addMarker(toMarker);
            mDrawMarkerLineLocations.add(toMarkerLine);

            CameraUpdate center = CameraUpdateFactory.newLatLng(polylineStartLatLng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
            mMap.moveCamera(center);
            mMap.moveCamera(zoom);
        }

        @Override
        public void onRoutingCancelled() {
            Toast.makeText(getActivity(), "onRoutingCancelled", Toast.LENGTH_LONG).show();
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (swSync.isChecked()) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
            }
        }
    };

    private void currentLocationOnLoad() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
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
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = mLocationManager.getLastKnownLocation(provider);
            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            }
            mLocationManager.requestLocationUpdates(provider, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
        }

        // Compass (la bàn)
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Button icon blue mylocation
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST);
            return;
        }

        mMap.setMyLocationEnabled(true);
        View locationButton = mMapView.findViewWithTag("GoogleMapMyLocationButton");
        RelativeLayout.LayoutParams myLocationRlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        myLocationRlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        myLocationRlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        myLocationRlp.setMargins(0, 0, 0, 300);

        View compassButton = mMapView.findViewWithTag("GoogleMapCompass");
        RelativeLayout.LayoutParams compassRlp = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
        compassRlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        compassRlp.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
        compassRlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        compassRlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        compassRlp.rightMargin = compassRlp.leftMargin;
        compassRlp.bottomMargin = 60;
        compassButton.requestLayout();

        mMap.setOnMapClickListener(onCreateMarkerMapClickListener);
    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        Log.i("MapsFragment", intent.getDoubleExtra("lat", 0) + "");
//        Log.i("MapsFragment", intent.getDoubleExtra("lng", 0) + "");
//        Log.i("MapsFragment", intent.getFloatExtra("bearing", 0) + "");
//    }
}