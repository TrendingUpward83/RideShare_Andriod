package net.rideshare_ptc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

public class RateRide extends AppCompatActivity {
    @TargetApi(Build.VERSION_CODES.O)
    //get current ride information; should be available (is NOT taken)
    ActiveRide active_ride = ActiveRide.getInstance();
    Ride activeRide = active_ride.getRideInfo();
    Integer activeRideID = activeRide.getRideID();
    String driverID = activeRide.getDriverID();
    String riderID = activeRide.getRiderID();


    LoginManager mgr = LoginManager.getInstance();
    User loggedInUser = mgr.getLoggedInUser();
    String UserId = loggedInUser.getUserID();

    String rideOrigin;
    String rideDest;
    String rideDate;


    float rideRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_ride);
        TextView rideSum = (TextView) findViewById(R.id.txtRideSummary);

        //use these to set ride summary
        rideOrigin = activeRide.getPickUpLoc();
        rideDest = activeRide.getDest();
        rideDate = activeRide.getRideDate();
        String rideSummary = "You are rating ride "+rideOrigin+" to "+rideDest+" at "+rideDate+".";
        rideSum.setText(rideSummary);
        Button getRating = findViewById(R.id.btnRateSubmit);
        final RatingBar ratingBar = findViewById(R.id.ratingBar);
        getRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rideRating = ratingBar.getRating();
                String rating = "Rating is :" + rideRating;
                Toast.makeText(RateRide.this, rating, Toast.LENGTH_LONG).show();
                //determine if we are rating the ride rider or driver
                if (UserId.equals(driverID)){ //if logged in user is ride driver, rate rider
                    if (activeRide.getDriverScore().isEmpty()|| activeRide.getDriverScore().equals( "0.0")){//if Driver did not rate rider, do not complete
                        try {
                            driverRateRider(activeRideID,rideRating, 0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {//if rider rated driver, this rider rating sends complettion flag to ride.
                        try {
                            driverRateRider(activeRideID,rideRating, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (UserId.equals(riderID)){//else if logged in user is rider, rate driver
                    if (activeRide.getRiderScore().isEmpty()|| activeRide.getRiderScore().equals( "0.0")){//if Driver did not rate rider, do not complete
                        try {
                            riderRateDriver(activeRideID,rideRating, 0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {//if driver rated rider, this driver rating sends completion flag to ride.
                        try {
                            riderRateDriver(activeRideID,rideRating, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }



            }
        });



        //return success message for rated ride.
    }

    private void riderRateDriver(Integer activeRideId, float rating, Integer completed) throws IOException {

        URL url = new URL("http://10.0.2.2:8080/rateRide/Driver?accRideId="+activeRideId +"&rating="+rating+"&complete="+completed); //set URL
        HttpURLConnection conWeb = (HttpURLConnection) url.openConnection(); //open connection
        conWeb.setRequestMethod("PUT");//set request method
        conWeb.setRequestProperty("Content-Type", "application/json"); //set the request content-type header parameter
        conWeb.setDoOutput(true); //enable this to write content to the connection OUTPUT STREAM
        Ride updatedRide = new Ride();
        //Create the request body
        OutputStream os = conWeb.getOutputStream();
        // send the JSON as bye array input

        Integer respCode = conWeb.getResponseCode();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(conWeb.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            String strResponse = response.toString();
            ObjectMapper mapper = new ObjectMapper();
            try {
                updatedRide = mapper.readValue(strResponse, Ride.class);
            }
            catch (JsonGenerationException ge){
                System.out.println(ge);
            }
            catch (JsonMappingException me) {
                System.out.println(me);
            }

            startActivity(new Intent(RateRide.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Ride Rating Posted, \n you gave driver a rating of "+updatedRide.getDriverScore()));
            //get response status code

        } catch (IOException e) {
            //TODO: Add error message for user
            e.printStackTrace();
        }finally {
            conWeb.disconnect();
            //return updatedRide;
        }
    }
    private void driverRateRider(Integer activeRideId, float rating, Integer completed) throws IOException {

        URL url = new URL("http://10.0.2.2:8080/rateRide/Rider?accRideId="+activeRideId +"&rating="+rating+"&complete="+completed); //set URL
        HttpURLConnection conWeb = (HttpURLConnection) url.openConnection(); //open connection
        conWeb.setRequestMethod("PUT");//set request method
        conWeb.setRequestProperty("Content-Type", "application/json"); //set the request content-type header parameter
        conWeb.setDoOutput(true); //enable this to write content to the connection OUTPUT STREAM
        Ride updatedRide = new Ride();
        //Create the request body
        OutputStream os = conWeb.getOutputStream();
        // send the JSON as bye array input

        Integer respCode = conWeb.getResponseCode();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(conWeb.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            String strResponse = response.toString();
            ObjectMapper mapper = new ObjectMapper();
            try {
                updatedRide = mapper.readValue(strResponse, Ride.class);
            }
            catch (JsonGenerationException ge){
                System.out.println(ge);
            }
            catch (JsonMappingException me) {
                System.out.println(me);
            }

            startActivity(new Intent(RateRide.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Ride Rated, \n you gave driver a rating of "+updatedRide.getRiderScore()));
            //get response status code

        } catch (IOException e) {
            //TODO: Add error message for user
            e.printStackTrace();
        }finally {
            conWeb.disconnect();
            //return updatedRide;
        }
    }


}