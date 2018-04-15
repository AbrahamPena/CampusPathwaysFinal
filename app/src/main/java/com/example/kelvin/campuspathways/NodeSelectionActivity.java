package com.example.kelvin.campuspathways;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is a 'main' thread since two other threads are needed to properly retrieve data from the database
 * This class will allows users to select a starting and ending location
 */

public class NodeSelectionActivity extends FragmentActivity implements OnMapReadyCallback {

    //Variables to hold data
    private GoogleMap mMap;
    private Spinner startLocation;
    private Spinner endLocation;
    private int startingLocationIndex;
    private int endingLocationIndex;
    private LatLng pointStart;
    private LatLng pointEnd;

    //Connection to the database on separate threads
    private DatabaseConnectionGetNodes databaseConnection;
    private DatabaseConnectionGetPaths databasePathConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_selection);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
    }

    //Initialize objects
    private void init() {
        //Find the spinners on the xml
        startLocation = findViewById(R.id.startLocation);
        endLocation = findViewById(R.id.endLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Initialize the map
        mMap = googleMap;

        //Initialize the separate threads
        databaseConnection = new DatabaseConnectionGetNodes(mMap);
        databasePathConnection = new DatabaseConnectionGetPaths(mMap);

        //Run the separate threads
        databaseConnection.execute();
        databasePathConnection.execute();

        //We delay the main thread to allow the separate thread enough time to populate the list for the spinner
        try { databaseConnection.get(1500, TimeUnit.MILLISECONDS); }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
        catch (TimeoutException e) { e.printStackTrace(); }


        //Add names of building to the list for the spinner
        final ArrayList<String> buildingList = databaseConnection.buildingList;

        //Create an adapter for the spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, buildingList);

        //Set the list in the drop down menu
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Populate both starting and ending location list
        startLocation.setAdapter(dataAdapter);
        endLocation.setAdapter(dataAdapter);


        //Listener for the drop down menu
        startLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //We simply get their index and compare that to the index in the point arraylist
                startingLocationIndex = (int) adapterView.getItemIdAtPosition(i);
                pointStart = databaseConnection.points.get(startingLocationIndex);
                //databasePathConnection.plotPaths(pointSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Listener for the drop down menu
        endLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //We simply get their index and compare that to the index in the point arraylist
                endingLocationIndex = (int) adapterView.getItemIdAtPosition(i);
                pointEnd = databaseConnection.points.get(endingLocationIndex);
                //Log.d("TAG", "onItemSelected: " + pointEnd);  //For debugging
                //databasePathConnection.plotPaths(pointSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }


        //This method will allow the button to get the pathways and display them
        public void getPathways(View v) {
            //First we reset all the pathways
            databasePathConnection.resetPaths();
            //We get the pathways we desire
            databasePathConnection.plotPaths(pointStart);
            databasePathConnection.plotPaths(pointEnd);
        }
}
