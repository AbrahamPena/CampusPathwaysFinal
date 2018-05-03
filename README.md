# CampusPathwaysFinal
Campus Pathway Discovery is an android app that tracks and displays pathways taken by users
This readme file contains ways to get the app to run if you're developing on either Windows or Mac/OSX
---Windows Set Up---
This set of instructions will help with setting up the project on Windows
A few things that are needed
1) Android Studio DownloadLink: https://developer.android.com/studio/
2) Github Desktop DownloadLink: https://desktop.github.com/
3)Install both Android Studio and Github desktop before continuing

With both applications installed, go to the GitHub Desktop first
1) Select File -> Clone Repository -> URL tab
2) https://github.com/AbrahamPena/CampusPathwaysFinal Paste this link into the repository link
3) Save the repository in a desired file on your computer, preferably some place easy to access
4) With the repository cloned, start Android Studio

If this is the first time using Android Studio, you will be prompted to start new project
or open existing project, select existing project and navigate to where the repository is saved

If you have used Android studio before, go to File -> Open -> navigate to repository file
Move on to Getting the app to run

---Mac/OSX Project Set Up---
1) Download Android Studio Link: https://developer.android.com/studio/
2) Install Android studio
3) Once installed, select "Check out project from Version Control" -> GitHub
   (You will need a GitHub account if you're going to clone the repository this way)
4) Sign in to GitHub when prompted
5) Copy link to clone repository Link: https://github.com/AbrahamPena/CampusPathwaysFinal
Move on to Getting the app to run

---Getting the app to run---
1) In android studio with the app loaded, select Project from drop down menu on the top left
2) Open CampusPathwaysFinal -> app -> src -> res -> values -> google_maps_api.xml
3) Line 23 of this file contains an API key from google. If running on a different computer,
   a new api key must first be obtained.
   https://developers.google.com/maps/documentation/android-api/signup link to obtain API key
4) Follow Google's instructions on obtaining API key and replace the current API key
5) Run the app on either an emulator or test phone plugged in
6) Click on button "Display Pathways", if the google maps displays correctly, you have successfully
   set up the the app and are ready to develop on it. If not, you may have the wrong API key or placed
   it in the wrong area.
   
---Setting up the server---

This app is set up to run with Amazon's AWS (Amazon Web Services) servers. The SQL database that is preferred is Microsoft SQL Server.
You will need a remote server to be able to collect/retrieve data. Link to AWS: https://aws.amazon.com/
1) Follow their set up instructions using Microsoft SQL Server instead. If you are familiar with setting up a remote database,
set up a Microsoft SQL Server for this project.
2) Once the remote server is set up, using Microsoft SQL Server Management Studio Version XX, login
   into the database using the Endpoint address provided by AWS.
3) For Authentication choose “SQL Server Authentication”
4) Login using credentials you used when setting up the remote server on AWS
5) Data collected will be provided by the instructor. It contains a query to set up the database and tables
   along with data to get started. You simply need to copy and paste the whole query to set it up.
6) Java Classes that begin with "DatabaseConnection" in their name have the endpoint set to another server.
   The server may or may not be of use anymore, so it is important you set yours up first and change the
   "String dns" variable to your endpoint. Each "DatabaseConnection" class has a comment, directing the user 
   where to change the endpoint.
   
File Directory: CampusPathwaysFinal -> app -> src -> main -> java

You are now ready to begin collecting and retrieving data!

---Known Problems---
1) Some phones tend to work better than others, as their magnetometer is more sensitive
   Phones that had no issues: Samsung Galaxys in general, Motorola Moto X, LGs
   Phones that had issues: Google Pixels, ZTEs
2) Major bugs that have been found and fixed
   App crashes when pathway has been clicked
   -Problem was fixed by moving the onPolyLineClicked function being moved to the main thread
   Drop down menu sometimes doesn't display options
   -Problem was fixed by allowing the separate thread adequate time to load the spinner list
   Compass readings are sometimes off
   -Problem can be mitigated by calibrating the magnet sensor
3) One phone has crashed and had to be factory reset after app was installed.
   Problem seems to have been fixed and testing has not showed this to be a problem.

---Wish list---
These are some of the functionality we wished to implement but were unsuccessful due to time
1) Pathway ranking system
   Allow the user to view pathways based on their choice of either time or distance
2) User profile tab
   The tab would allow the user to see their settings and permissions
3) Get pathways in various phone orientations
   The phone was only able to get the pathway of the user if they held out the phone directly in front of them
