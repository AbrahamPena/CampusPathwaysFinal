package com.example.kelvin.campuspathways;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Kelvin on 2/25/2018.
 * Used to asynchronously select data from the database
 */

@SuppressWarnings("unused")
class DatabaseConnectionSelect extends AsyncTask<String, Void, String> {

    private final String query;//Query to be performed
    private final GoogleMap googleMap;//Map to be used for drawing of paths

    private final ArrayList<String> paths;//List of paths to be drawn
    private final ArrayList<Integer> pathTimes;//List of time taken for each path, in milliseconds
    private final ArrayList<Double> userHeights;//List of distances for each path
    private LatLng mapStart;//Sets camera start
    private double timeTaken, distance;
    String timeInfo;
    String distanceInfo;

    private Marker m1, m2;//Markers on start and end of selected path

    //Only constructor
    DatabaseConnectionSelect(String query, GoogleMap gMap) {
        this.query = query;
        this.googleMap = gMap;

        paths = new ArrayList<>();
        pathTimes = new ArrayList<>();
        userHeights = new ArrayList<>();
    }

    //Connect to database and perform query
    @Override
    protected String doInBackground(String... strings) {

        try {

            //Connection information
            String dns = "on-campus-navigation.caqb3uzoiuo3.us-east-1.rds.amazonaws.com"; //This must be changed to the endpoint of your AWS server
            String aClass = "net.sourceforge.jtds.jdbc.Driver";
            Class.forName(aClass).newInstance();

            //Connect to database
            Connection dbConnection = DriverManager.getConnection("jdbc:jtds:sqlserver://" + dns +
                    "/Campus-Navigation;user=Android;password=password");

            //Execute query; In this case Selection
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            //Iterate through result and put in ArrayList
            while (resultSet.next()) {

                //4 Columns: Android_ID, Step_Length, Path_ID, User_Path
                @SuppressWarnings("unused") String Android_ID = resultSet.getString("Android_ID");
                double step_length = resultSet.getDouble("Step_Length");
                int path_id = resultSet.getInt("Path_ID");
                String path = resultSet.getString("User_Path");

                if (path != null) {
                    paths.add(resultSet.getString("User_Path"));
                    userHeights.add(step_length);
                }

            }

            //Close connection to database
            dbConnection.close();

        } catch (Exception e) {
            Log.w("Error", "" + e.getMessage());
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

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
                long startTime = pathJSON.getJSONObject(0).getLong("Time");
                long endTime = pathJSON.getJSONObject(pathJSON.length() - 1).getLong("Time");
                int timeTaken = (int) (endTime - startTime);
                pathTimes.add(timeTaken);

                //Make map start at position of 1st start point
                if (mapStart == null) mapStart = points.get(0);

                //Draw pathways and make clickable
                Polyline path = googleMap.addPolyline(new PolylineOptions().addAll(points).width(10).color(Color.RED));
                int minutesTaken = (timeTaken /1000) / 60;
                double extraSeconds = timeTaken - (60 * minutesTaken);
                double distanceTraveled = userHeights.get(i) * path.getPoints().size() * 2;

                //Formating the time to display appropriately
                timeInfo = minutesTaken + " minutes, " + (int) extraSeconds + " seconds \n";
                distanceInfo = distanceTraveled + " meters";
                String pathInfo = minutesTaken + " minutes, " + (int) extraSeconds + " seconds \n"
                        + distanceTraveled + " meters";
                String myDistance = distanceTraveled + " meters";
                path.setTag(myDistance);
                path.setClickable(true);

            }

            //Move map to 1st point of 1st path
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapStart, 20.0f));


        } catch (Exception e) {
            Log.w("Error", "" + e.getMessage());
        }

    }


}
