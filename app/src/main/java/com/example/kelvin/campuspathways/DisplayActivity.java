package com.example.kelvin.campuspathways;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class DisplayActivity extends FragmentActivity implements OnMapReadyCallback {

    private Context thisContext;
    private DatabaseConnectionSelect databaseConnectionSelect;
    private Marker m1, m2;//Markers on start and end of selected path


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
        thisContext = this;

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        //Once map loads, plot all paths
        String query = "SELECT Users.Android_ID, Step_Length, Path_ID, User_Path" +
                " FROM Users FULL OUTER JOIN Pathways ON Users.Android_ID = Pathways.Android_ID;";

        databaseConnectionSelect = (DatabaseConnectionSelect) new DatabaseConnectionSelect(query, googleMap).execute();
        try {
            databaseConnectionSelect.get(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {

            @Override
            public void onPolylineClick(Polyline polyline) {

                //Log.d("TAG", "onPolylineClick: Starting: " + startingPoint + " Ending: " + endingPoint);
                if (m1 != null && m2 != null) {
                    m1.remove();
                    m1 = null;
                    m2.remove();
                    m2 = null;
                }
                LatLng startingPoint = polyline.getPoints().get(0);
                LatLng endingPoint = polyline.getPoints().get(polyline.getPoints().size()-1);
                String ss = polyline.getId().substring(2);//String of line index
                int i = Integer.parseInt(ss);//Path index

                String info = (String) polyline.getTag();

                    m1 = googleMap.addMarker(new MarkerOptions().position(startingPoint)
                            .title("Pathway #" + (i + 1) + " start\n")
                            .snippet(info));
                    m2 = googleMap.addMarker(new MarkerOptions().position(endingPoint)
                            .title("Pathway #" + (i + 1) + " end\n")
                            .snippet(info));

            }
        });
    }

    private void init(){
        Button btDiscover = findViewById(R.id.btDiscoverPathFromDisplay);
        Button btNodes = findViewById(R.id.btNodesFromDisplay);

        //Change to Discover Activity
        btDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check if given location access first; If not, tell user
                if (!getPermissions()) {
                    Toast.makeText(thisContext, "Error. Location access not granted", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(thisContext, DiscoverActivity.class);
                startActivity(intent);
            }
        });

        //Change to Nodes activity
        btNodes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change to nodes screen
                Intent intent = new Intent(thisContext, NodeSelectionActivity.class);
                startActivity(intent);
            }
        });


    }

    //Asks User for runtime permission to access location
    //Required for discovery
    private boolean getPermissions() {

        //Check if permission granted
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //If not already granted, prompt user for them
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},
                    PackageManager.PERMISSION_GRANTED);

            return false;

        }

        //If permission already granted
        else {
            return true;
        }
    }

}
