package net.rideshare_ptc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class RideDriverCarInfo extends AppCompatActivity {
    User rideDriver;
    String userEmailAdd;
    Byte isADriver;
    Byte hasActiveCar;
    Car usersCar = new Car();
    ActiveRide active_ride = ActiveRide.getInstance();
    Ride activeRide = active_ride.getRideInfo();
    String driverID = activeRide.getDriverID();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_car_info);
        Button btnMainMenu = (Button) findViewById(R.id.btnCarToMainMenu);
        //TODO: route this button to myRides page
        Button btnAllRides = (Button) findViewById(R.id.btnCarToAllRides);
        //The below textviews will be populated with user's car data or, if none exists,
        TextView carDetails = (TextView) findViewById(R.id.txtCarDetails);
        TextView userName = (TextView) findViewById(R.id.txtCarUserName);

        ActiveRide active_ride = ActiveRide.getInstance();
        Ride activeRide = active_ride.getRideInfo();
        String driverID = activeRide.getDriverID();
        String driverEmailAdd;


        try {
            rideDriver = getUserData(driverID);
            String fName = rideDriver.getUserFName();

            usersCar = getCarData(driverID);
            userName.setText(fName);
            carDetails.setText(usersCar.toString());

            //carDetails.setText("User has no active registered cars. \n Driver user cannot accept this any rides at this time.");

        } catch (IOException e) {
            e.printStackTrace();
        }


        btnMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainMenu.class);
                startActivity(intent);
            }
        });

        btnAllRides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ViewAllRides.class);
                startActivity(intent);
            }
        });

    }


        private Car getCarData(String userid) throws IOException {
            Car aCar = new Car();
            URL url = new URL("http://10.0.2.2:8080/car?userID=" + userid); //set URL
            HttpURLConnection con = (HttpURLConnection) url.openConnection(); //open connection
            con.setUseCaches(false);
            con.setRequestMethod("GET");//set request method

            //con.setDoOutput(true); //enable this to write content to the connection OUTPUT STREAM
            //con.setDoInput(true);
            con.connect();

            //read the response from input stream
            //TODO: Add error handling for any response code other than 200
            try{
                StringBuilder result = new StringBuilder();
                BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                String strResponse = result.toString();
                int respCode = con.getResponseCode();
                //Map JSON Object to User Object
                ObjectMapper mapper = new ObjectMapper();
                try {
                    aCar = mapper.readValue(strResponse, Car.class);

                }
                catch (JsonGenerationException ge){
                    System.out.println(ge);
                }
                catch (JsonMappingException me) {
                    System.out.println(me);
                }

            }
            catch (IOException e){
                startActivity(new Intent(RideDriverCarInfo.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "User info: \n ERROR \n"+ e + "CAR ERROR"));
            }
            con.disconnect();
            return aCar;

        }

    private User getUserData(String driverid) throws IOException {

        URL url = new URL("http://10.0.2.2:8080/car?userID=" + driverid); //set URL
        HttpURLConnection con = (HttpURLConnection) url.openConnection(); //open connection
        con.setUseCaches(false);
        con.setRequestMethod("GET");//set request method
        con.connect();
        Integer respCode;
        User thisDriver = new User();

        //read the response from input stream
        //TODO: Add error handling for any response code other than 200
        try{
            StringBuilder result = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            String strResponse = result.toString();
            respCode = con.getResponseCode();
            //Map JSON Object to User Object
            ObjectMapper mapper = new ObjectMapper();
            try {
                thisDriver = mapper.readValue(strResponse, User.class);
            }
            catch (JsonGenerationException ge){
                System.out.println(ge);
            }
            catch (JsonMappingException me) {
                System.out.println(me);
            }

        }
        catch (IOException e){
            startActivity(new Intent(RideDriverCarInfo.this, LoginWelcomeStatusActivity.class).putExtra("Login Status", "User info: \n ERROR \n"+ e ));
        }
        con.disconnect();
        return thisDriver;
    }
    }