package com.example.kelvin.campuspathways;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

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

class DatabaseConnectionGetNodes extends AsyncTask<Void, Void, Void> {

    //Variables to be used
    private final GoogleMap gMap;
    private final ArrayList<Double> latitudeList;     //Buffer the latitudes
    private final ArrayList<Double> longitudeList;    //Buffer the longitudes
    final ArrayList<String> buildingList;             //Buffer the list of buildings, public so dropdown menu can be populated
    final ArrayList<LatLng> points;                   //Coordinates for nodes, public to allow point selection

    //Constructor
    DatabaseConnectionGetNodes(GoogleMap map) {

        //Initialize all the variables
        gMap = map;
        latitudeList = new ArrayList<>();
        longitudeList = new ArrayList<>();
        buildingList = new ArrayList<>();
        points = new ArrayList<>();

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
            @SuppressWarnings("unused") String pathQuery = "SELECT * FROM Pathways";

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

        createNodes();  //When it's done getting the list of buildings and coordiantes, create the nodes
        plotNodes();    //Place the nodes on the map for the users to see

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
        for (int i = 0; i < buildingList.size(); i++) {

            //Draw circle around node; Clickable to get building names
            Circle circle = gMap.addCircle(new CircleOptions()
                    .center(points.get(i))
                    .radius(120 / 2));

            circle.setClickable(true);              //Let's users click on buildings to see names
            circle.setStrokeWidth(5f);              //For debugging purposes
            circle.setFillColor(Color.TRANSPARENT); //Set the circles to transparent to let users see the map
            circle.setTag(buildingList.get(i));     //Give the circle a string tag
        }
    }


}
