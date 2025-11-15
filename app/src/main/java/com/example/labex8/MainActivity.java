package com.example.labex8;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> favoritesList;
    private DatabaseHelper dbHelper;

    private static final int REQUEST_CODE_MAP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set title
        setTitle("Favorite Places");

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        listView = findViewById(R.id.listView);
        FloatingActionButton fab = findViewById(R.id.fab);

        // Load favorites from database
        loadFavorites();

        // Set up adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favoritesList);
        listView.setAdapter(adapter);

        // FAB click listener - open map activity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivityForResult(intent, REQUEST_CODE_MAP);
            }
        });

        // List item click listener - show location on map
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<Place> places = dbHelper.getAllPlaces();

                // Don't do anything if clicking the placeholder text
                if (places.isEmpty()) {
                    return;
                }

                // Make sure we have a valid position
                if (position >= 0 && position < places.size()) {
                    Place place = places.get(position);
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra("latitude", place.getLatitude());
                    intent.putExtra("longitude", place.getLongitude());
                    intent.putExtra("name", place.getName());
                    startActivity(intent);
                }
            }
        });

        // List item long click listener - delete item
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<Place> places = dbHelper.getAllPlaces();

                // Don't do anything if clicking the placeholder text
                if (places.isEmpty()) {
                    return true;
                }

                // Make sure we have a valid position
                if (position >= 0 && position < places.size()) {
                    Place place = places.get(position);
                    dbHelper.deletePlace(place.getId());

                    // Reload and update
                    loadFavorites();
                    adapter.clear();
                    adapter.addAll(favoritesList);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(MainActivity.this, "Place deleted", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    private void loadFavorites() {
        favoritesList = new ArrayList<>();
        ArrayList<Place> places = dbHelper.getAllPlaces();

        for (Place place : places) {
            favoritesList.add(place.getName());
        }

        // Only show placeholder if list is truly empty
        if (favoritesList.isEmpty()) {
            favoritesList.add("Add a new place...");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK) {
            // Reload the list
            loadFavorites();
            adapter.clear();
            adapter.addAll(favoritesList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites when returning to this activity
        loadFavorites();
        adapter.clear();
        adapter.addAll(favoritesList);
        adapter.notifyDataSetChanged();
    }
}