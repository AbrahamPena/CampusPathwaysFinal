package com.example.kelvin.campuspathways;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * Created by Abraham on 4/13/18.
 * This class gets the pathways in the database and displays
 * them on the NodeSelectionActivity.java. The purpose of this
 * class is to get all the pathways that meet the starting
 * and ending location criteria
 */

public class DatabaseConnectionGetPaths extends AsyncTask<Void, Void, Void> {

    //List of paths between 2 selected nodes
    private ArrayList<String> paths;
    //List of all paths
    private ArrayList<String> allPaths;
    //List of paths
    private ArrayList<LatLng> points;
    //List of all visible paths
    private ArrayList<Polyline> pathLines;
    //Map to use
    private GoogleMap gMap;

    DatabaseConnectionGetPaths(GoogleMap map) {

        //Initialize objects
        points = new ArrayList<>();
        paths = new ArrayList<>();
        allPaths = new ArrayList<>();
        pathLines = new ArrayList<>();
        gMap = map;

    }


    @Override
    protected Void doInBackground(Void... voids) {

        //Connect to database to get path information
        try {

            //Connection information
            String dns = "on-campus-navigation.caqb3uzoiuo3.us-east-1.rds.amazonaws.com";
            String aClass = "net.sourceforge.jtds.jdbc.Driver";
            Class.forName(aClass).newInstance();

            //Connect to database
            Connection dbConnection = DriverManager.getConnection("jdbc:jtds:sqlserver://" + dns +
                    "/Campus-Navigation;user=Android;password=password");

            //Create query
            String query = "SELECT * FROM Pathways";

            //Execute query; In this case Selection
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            //Iterate through result and put in ArrayList
            while (resultSet.next()) {

                //Get path JSON
                String pathJSONString = resultSet.getString("User_Path");
                JSONArray pathJSON = new JSONArray(pathJSONString);

                //Get JSON of start and end points
                JSONObject startPointJSON = pathJSON.getJSONObject(0);
                JSONObject endPointJSON = pathJSON.getJSONObject(pathJSON.length() - 1);

                //Get LatLng of start and end points
                LatLng startPoint =
                        new LatLng(startPointJSON.getDouble("Latitude"), startPointJSON.getDouble("Longitude"));
                LatLng endPoint =
                        new LatLng(endPointJSON.getDouble("Latitude"), endPointJSON.getDouble("Longitude"));

                //Insert points into ArrayList
                points.add(startPoint);
                points.add(endPoint);

                //Insert path into ArrayList
                paths.add(pathJSONString);

            }

            //Store all the pathways in an arraylist
            allPaths.addAll(paths);

            //Close connection to database
            dbConnection.close();

        } catch (Exception e) {
            Log.w("Error", "" + e.getMessage());
            return null;
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }


    //Removes any paths from the list that don't have node as the start or end node
    private void filterPathsNode(LatLng node) {

        //No use if list is empty
        if (paths.isEmpty()) return;

        //Buffer to store paths to remove
        ArrayList<String> pathsToRemove = new ArrayList<>();

        //Iterate through paths and filter
        for (String path : paths) {

            //Parse JSON to get start and top points
            JSONArray pathJSON = null;
            try {
                pathJSON = new JSONArray(path);

                JSONObject startPointJSON = pathJSON.getJSONObject(0);
                JSONObject endPointJSON = pathJSON.getJSONObject(pathJSON.length() - 1);

                LatLng startPoint = new LatLng(startPointJSON.getDouble("Latitude"),
                        startPointJSON.getDouble("Longitude"));
                LatLng endPoint = new LatLng(endPointJSON.getDouble("Latitude"),
                        endPointJSON.getDouble("Longitude"));

                //Check if start or end point is near node
                if (SphericalUtil.computeDistanceBetween(startPoint, node) > 120 / 2
                        && SphericalUtil.computeDistanceBetween(endPoint, node) > 120 / 2) {

                    //If start and end point too far from node, store in buffer
                    pathsToRemove.add(path);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Remove paths in buffer from global list
        paths.removeAll(pathsToRemove);
    }


    //Plot paths
    void plotPaths(LatLng markerPosition) {

        gMap.clear();

        filterPathsNode(markerPosition);

        //Iterate through List of JSON arrays
        //Each JSON array is 1 pathway
        try {
            for (int i = 0; i < paths.size(); i++) {

                //Decode JSON array into polyline
                JSONArray pathJSON = new JSONArray(paths.get(i));

                ArrayList<LatLng> points = new ArrayList<>();
                //Get JSON array into list of points
                for (int j = 0; j < pathJSON.length(); j++) {
                    //Get data from JSON object
                    JSONObject point = pathJSON.getJSONObject(j);
                    double lat = point.getDouble("Latitude");
                    double lng = point.getDouble("Longitude");

                    //Make point from JSON data and add to list
                    points.add(new LatLng(lat, lng));
                }

                //Get time taken for path
                //First we get the start time from the JSON string and convert to a long
                long startTime = pathJSON.getJSONObject(0).getLong("Time");
                //We get the end time from the JSON string and convert to a long
                long endTime = pathJSON.getJSONObject(pathJSON.length() - 1).getLong("Time");
                
                //We get the time as a long from start to end
                int timeTaken = (int) (endTime - startTime);
                double pathTime = timeTaken / 1000;     //Convert time from long to double
                int minutes = (int) pathTime / 60;      //Convert from double to minutes
                double seconds = pathTime - (60 / minutes); //Get the amount of seconds
                
                String mySeconds = String.format("%1.0f", seconds);     //Format the time
                //Set string when user wants to see the time of the pathway taken
                String totalTime = "Path Time: " + minutes + "" + ":" + mySeconds + "";


                //Draw pathways and make clickable
                Polyline path = gMap.addPolyline(new PolylineOptions().addAll(points).width(15).color(Color.BLUE));
                path.setClickable(true);    //Let the users click on the pathways
                path.setTag(totalTime);     //We set the time as a string tag
                pathLines.add(path);        //We add the filtered path into the arraylist
            }

        } catch (Exception e) {
            e.getStackTrace();
        }
    }


    //Reset paths to all paths
    void resetPaths() {

        //Remove drawn lines
        for (Polyline line : pathLines) line.remove();
        pathLines.clear();

        paths.clear();
        paths.addAll(allPaths);
    }
}
