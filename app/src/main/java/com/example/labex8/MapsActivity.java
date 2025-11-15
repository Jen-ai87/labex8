package com.example.labex8;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Check if viewing an existing place
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);
        String placeName = getIntent().getStringExtra("name");

        if (latitude != 0 && longitude != 0) {
            // Show existing place
            LatLng location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(location).title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        } else {
            // Default location (Toronto)
            LatLng toronto = new LatLng(43.6532, -79.3832);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toronto, 12f));
        }

        // Enable location if permission granted
        enableMyLocation();

        // Set up long press listener to add new places
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                // Get address from coordinates
                String address = getAddressFromLocation(latLng.latitude, latLng.longitude);

                // Show dialog to name the place
                showAddPlaceDialog(latLng, address);
            }
        });

        // Load existing markers
        loadExistingMarkers();
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);
                if (addressLine != null && !addressLine.isEmpty()) {
                    return addressLine;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If geocoding fails, return coordinates with timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void showAddPlaceDialog(final LatLng latLng, final String defaultName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Favorite Place");
        builder.setMessage("Enter a name for this location:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultName);
        input.setSelection(defaultName.length());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String placeName = input.getText().toString().trim();
            if (placeName.isEmpty()) {
                placeName = defaultName;
            }

            // Save to database
            Place place = new Place(placeName, latLng.latitude, latLng.longitude);
            long id = dbHelper.addPlace(place);

            if (id != -1) {
                // Add marker to map
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(placeName));

                Toast.makeText(MapsActivity.this,
                        "Place added: " + placeName,
                        Toast.LENGTH_SHORT).show();

                setResult(RESULT_OK);
            } else {
                Toast.makeText(MapsActivity.this,
                        "Error saving place",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void loadExistingMarkers() {
        List<Place> places = dbHelper.getAllPlaces();
        for (Place place : places) {
            LatLng location = new LatLng(place.getLatitude(), place.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(place.getName()));
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
