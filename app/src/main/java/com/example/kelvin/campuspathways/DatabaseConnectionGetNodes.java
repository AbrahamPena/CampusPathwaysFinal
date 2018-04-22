package com.example.kelvin.campuspathways;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Abraham on 4/13/18.
 * This class is a separate thread that is needed to establish nodes on the map
 * The nodes will serve as the starting and ending points for users to choose from
 */

public class DatabaseConnectionGetNodes extends AsyncTask<Void, Void, Void> {

    //Variables to be used
    private GoogleMap gMap;
    private ArrayList<Double> latitudeList;     //Buffer the latitudes
    private ArrayList<Double> longitudeList;    //Buffer the longitudes
    ArrayList<String> buildingList;             //Buffer the list of buildings, public so dropdown menu can be populated
    ArrayList<LatLng> points;                   //Coordinates for nodes, public to allow point selection

    //Constructor
    DatabaseConnectionGetNodes(GoogleMap map) {

        //Initialize all the variables
        gMap = map;
        latitudeList = new ArrayList<Double>();
        longitudeList = new ArrayList<Double>();
        buildingList = new ArrayList<String>();
        points = new ArrayList<LatLng>();

    }

    @Override
    protected Void doInBackground(Void... voids) {

        //Connect to the database to get information about the nodes
        try {

            //Information to connect to the database
            String dns = "on-campus-navigation.caqb3uzoiuo3.us-east-1.rds.amazonaws.com";
            String aClass = "net.sourceforge.jtds.jdbc.Driver";
            Class.forName(aClass).newInstance();

            //Create query
            String query = "SELECT Building_name, Latitude, Longitude FROM Buildings";
            String pathQuery = "SELECT * FROM Pathways";

            //Connect to database
            Connection dbConnection = DriverManager.getConnection("jdbc:jtds:sqlserver://" + dns +
                    "/Campus-Navigation;user=Android;password=password");

            //Execute query; In this case Selection
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            //Iterate through the results of the query and store
            while (resultSet.next()) {

                //Get the buildingname, latitude, and longitude from the resultSet
                String buildingName = resultSet.getString("Building_name");
                double latitude = resultSet.getDouble("Latitude");
                double longitude = resultSet.getDouble("Longitude");

                //Store them in an arraylist
                buildingList.add(buildingName);
                latitudeList.add(latitude);
                longitudeList.add(longitude);
            }

            //Close the connection to the server
            dbConnection.close();

        }
        catch (Exception e) {
            Log.w("Error", "" + e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        createNodes();
        plotNodes();

    }

    //This method creates nodes
    private void createNodes() {
        for (int i = 0; i < latitudeList.size(); i++) {
            points.add(new LatLng(latitudeList.get(i), longitudeList.get(i)));
        }
    }

    //This method places the nodes on the map
    void plotNodes() {
        if (points.isEmpty()) { return; }

        LatLng mapStart = points.get(0);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapStart, 16.0f));

        //Display a marker at each node
        for (LatLng point : points) {

            //Draw circle around node; Clickable to filter paths
            Circle circle = gMap.addCircle(new CircleOptions()
                    .center(point)
                    .radius(120 / 2));

            circle.setClickable(false);      //Let's users click on buildings to see names
            circle.setStrokeWidth(1f);       //For debugging purposes
            circle.setFillColor(Color.TRANSPARENT);
        }

    }
}
