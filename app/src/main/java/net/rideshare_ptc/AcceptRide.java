package net.rideshare_ptc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AcceptRide extends AppCompatActivity {

    Boolean rideAcceptable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_ride);
    }
}