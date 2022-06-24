package net.rideshare_ptc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;




public class RideDetailsActivity extends AppCompatActivity {


    TextView rideDetails;
    Ride ride = new Ride();
    Button returnToMenu;
    Button driverDetails;
    Button riderDetails;
    Button acceptRide;
    String rideJSON;
    String userRiderId;
    String userDriverId;
    Integer rideID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);
        returnToMenu = (Button) findViewById(R.id.btnRideRetMenu);
        driverDetails = (Button) findViewById(R.id.btnViewRideDriver);
        riderDetails = (Button) findViewById(R.id.btnViewRideRider);
        acceptRide = (Button) findViewById(R.id.btnAcceptRide);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        String rideInformation = bundle.getString("Ride Details");
        TextView msg = (TextView) findViewById(R.id.txtViewRideDetail);
        msg.setMovementMethod(new ScrollingMovementMethod());
        msg.setText(rideInformation);
        userRiderId = "";
        userDriverId = "";
        rideID = 0;
        LoginManager mgr = LoginManager.getInstance();
        User loggedInUser = mgr.getLoggedInUser();

        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            returnToMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(RideDetailsActivity.this, MainMenu.class));
                }
            });

            driverDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ActiveRide active_ride = ActiveRide.getInstance();
                    //String rideDriver = active_ride.getRideInfo().getDriverID();
                    //TODO: case handling when not assigned
                    startActivity(new Intent(RideDetailsActivity.this, rideDriverProfile.class));
                }
            });
            riderDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ActiveRide active_ride = ActiveRide.getInstance();
                    //String rideRider  =  active_ride.getRideInfo().getRiderID();
                    //TODO: case handling when not assigned
                    startActivity(new Intent(RideDetailsActivity.this, rideRiderProfile.class));
                }
            });

            acceptRide.setOnClickListener(new View.OnClickListener() {
                //get Logged in user ID & is Rider/Driver
                Byte isDriver = loggedInUser.getIsDriver();


                //check if ride is available

                //get ride Number
                @Override
                public void onClick(View v) {

                 /*   if (isDriver == 1) {
                        userDriverId = loggedInUser.getUserID();
                        try {
                            riderAcceptsRide();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (isDriver == 0) {

                    }*/
                }
            });
        }
    }
}








