package net.rideshare_ptc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AcceptRide extends AppCompatActivity {

    //get current ride information; should be available (is NOT taken)
    ActiveRide active_ride = ActiveRide.getInstance();
    Ride activeRide = active_ride.getRideInfo();
    Integer activeRideID = activeRide.getRideID();
    String driverID = activeRide.getDriverID();
    String riderID = activeRide.getRiderID();
    Byte rideTaken = activeRide.getIsTaken();
    Byte rideCompleted = activeRide.getIsCompleted();
    //get current user information
    LoginManager mgr = LoginManager.getInstance();
    User loggedInUser = mgr.getLoggedInUser();
    String UserId = loggedInUser.getUserID();
    //determine if user is a driver or rider
    Byte isDriver = loggedInUser.getIsDriver();

    String RideType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_ride);

        //is ride available
        if (rideTaken == 1 || rideCompleted == 1) {
            startActivity(new Intent(AcceptRide.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Sorry, this ride is not available"));
        } else {
            getRideType(driverID, riderID);
            if (RideType == "Requested") {
                if (isDriver == 0) { //if ride has no driver and you're not a driver, can't accept
                    startActivity(new Intent(AcceptRide.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Sorry, rider cannot accept another rider's ride"));
                } else if (isDriver == 1) {//driver can accept
                    try {
                        driverAcceptRide(activeRideID,UserId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (RideType == "Posted") {


            }


        }
    }
    public String getRideType(String driverId, String riderId){
        if (driverId ==null){
            RideType = "Requested";
        }
        else if (riderId==null){
            RideType = "Posted";
        }
        return RideType;
    }



    private void driverAcceptRide(Integer activeRideId, String driverId) throws IOException {
        String rrideJSON="";
        URL url = new URL("http://10.0.2.2:8080/acceptRide/Driver?driverID="+driverId); //set URL
        HttpURLConnection conWeb = (HttpURLConnection) url.openConnection(); //open connection
        conWeb.setRequestMethod("POST");//set request method
        conWeb.setRequestProperty("Content-Type", "application/json"); //set the request content-type header parameter
        conWeb.setDoOutput(true); //enable this to write content to the connection OUTPUT STREAM
        Ride updatedRide = new Ride();
        //Create the request body
        OutputStream os = conWeb.getOutputStream();
        byte[] input = rrideJSON.getBytes("utf-8");   // send the JSON as bye array input
        os.write(input, 0, input.length);

        //read the response from input stream
        if(conWeb.getResponseCode() >= 400)
        {
            //TODO: Add error output for the user
            conWeb.disconnect();
            return; //short circuit
        }else{
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

                startActivity(new Intent(AcceptRide.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Ride Accepted \n  Details:\n" + updatedRide.toString()));
                //get response status code

            } catch (IOException e) {
                //TODO: Add error message for user
                e.printStackTrace();
            }finally {
                conWeb.disconnect();
            }
        }
    }







}