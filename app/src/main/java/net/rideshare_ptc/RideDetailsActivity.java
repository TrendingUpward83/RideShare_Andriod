
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
        import java.net.ProtocolException;
        import java.net.URL;
        import java.util.ArrayList;


        import androidx.appcompat.app.AppCompatActivity;

        import com.fasterxml.jackson.core.JsonGenerationException;
        import com.fasterxml.jackson.core.JsonProcessingException;
        import com.fasterxml.jackson.core.type.TypeReference;
        import com.fasterxml.jackson.databind.JsonMappingException;
        import com.fasterxml.jackson.databind.ObjectMapper;

        import org.w3c.dom.Text;


        public class RideDetailsActivity extends AppCompatActivity {


    Button returnToMenu;
    Button driverDetails;
    Button riderDetails;
    Button acceptRide;
    Button rateRide;
    String userRiderId;
    String userDriverId;
    String RideType;
    String UserId;
    String rideInformation;
    User loggedInUser;
    ActiveRide active_ride;
    Ride activeRide;
    Integer activeRideID;
    String RdriverID;
    String RriderID;
    Byte rideTaken;
    Byte isCompleted;
    Byte isDriver;
    String rideDriverScore;
    String rideRiderScore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);
        LoginManager mgr = LoginManager.getInstance();
        loggedInUser = mgr.getLoggedInUser();
        returnToMenu = (Button) findViewById(R.id.btnRideRetMenu);
        driverDetails = (Button) findViewById(R.id.btnViewRideDriver);
        riderDetails = (Button) findViewById(R.id.btnViewRideRider);
        acceptRide = (Button) findViewById(R.id.btnAcceptRide);
        rateRide = (Button) findViewById(R.id.btnRateRide);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        rideInformation = bundle.getString("Ride Details");
        TextView msg = (TextView) findViewById(R.id.txtViewRideDetail);
        msg.setMovementMethod(new ScrollingMovementMethod());
        msg.setText(rideInformation);

        //get current ride information; should be available (is NOT taken)
        active_ride = ActiveRide.getInstance();
        activeRide = active_ride.getRideInfo();
        activeRideID = activeRide.getRideID();
        RdriverID = activeRide.getDriverID();
        RriderID = activeRide.getRiderID();
        rideTaken = activeRide.getIsTaken();
        isCompleted =activeRide.getIsCompleted();
        userDriverId = "";
        userRiderId = "";
        rideDriverScore = activeRide.getDriverScore();
        rideRiderScore = activeRide.getRiderScore();


        //get current user information
        UserId = loggedInUser.getUserID();
        //determine if user is a driver or rider
        isDriver = loggedInUser.getIsDriver();



        rateRide.setVisibility(View.INVISIBLE);
        getRideType(RdriverID, RriderID);
        setBtnStatusTxt(UserId,isDriver, RriderID, RdriverID,rideTaken,isCompleted);
        setRatingStatus(rideDriverScore,rideRiderScore,RriderID, RdriverID);

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
                @Override
                public void onClick(View v) {
                    //is ride available
                    if (rideTaken == 1 || isCompleted == 1) {
                        startActivity(new Intent(RideDetailsActivity.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Sorry, this ride is not available"));
                    } else {
                        if (RideType == "Requested") {
                            if (isDriver == 0) { //if ride has no driver and you're not a driver, can't accept
                                startActivity(new Intent(RideDetailsActivity.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Sorry, rider cannot accept another rider's ride"));
                            } else if (isDriver == 1) {//driver can accept
                                try {
                                    driverAcceptRide(activeRideID, UserId);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (RideType == "Posted") { //if driver posted, doesn't matter if driver or rider accept, driver is also rider.
                            try {
                                riderAcceptRide(activeRideID, UserId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }
            });
            rateRide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //is ride available
                    if (UserId.equals(RdriverID) || UserId.equals(RriderID)) { //if user accepted & ride is not complete
                        if (rideTaken == 1) {//if ride has both parties, is then able to be possible to be complete
                            startActivity(new Intent(RideDetailsActivity.this, RateRide.class)); //go to rating activity

                        } else
                            startActivity(new Intent(RideDetailsActivity.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Unable to rate- this ride is already completed."));
                    }
                }
            });

        }
    }//end of onCreate

    private void setBtnStatusTxt(String userId, Byte isDriver, String rriderID, String rdriverID, Byte ridetaken, Byte isCompleted) {
        if ((userId.equals(rriderID) || userId.equals(rdriverID)) && (ridetaken == 1) && (isCompleted == 0)) { //rating logic is handled separately in rate ride activity
            rateRide.setVisibility(View.VISIBLE); //if logged in user is assigned to the ride AND the ride has both a rider and driver
        }
        if (isDriver == 1) { //logged in user is A driver
            userDriverId = userId;//know user is ride's driver
        } else if (isDriver == 0) {
            userRiderId = userId;//know user is  ride's rider
        }

        if (rdriverID == null) {
            driverDetails.setText("No Driver");
            driverDetails.setEnabled(false);
        } else if (rriderID == null) {
            riderDetails.setText("No Rider");
            riderDetails.setEnabled(false);
        }

        //hide accept ride in these cases
        if ((isCompleted ==1)){
            acceptRide.setText("Ride Completed");
            acceptRide.setEnabled(false);
        }
        else if (UserId.equals(rriderID) || UserId.equals(rdriverID)) //if logged in user already assigned to a ride, hide accept button
        {
            acceptRide.setText("You are Assigned");
            acceptRide.setEnabled(false);
        }
        else if ((isDriver ==1)&& rdriverID != null) //if logged in user is a driver but ride already has driver, can't accept
        {
            acceptRide.setText("Unavailable");
            acceptRide.setEnabled(false);
        }
        else if ((isDriver ==0)&& rriderID != null)//if logged  in user is a rider but ride has rider, can't accept ride
        {
            acceptRide.setText("Unavailable");
            acceptRide.setEnabled(false);
        }
        else {
            acceptRide.setText("Accept Ride");
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

    private void setRatingStatus(String rDriverScore,String rRiderScore, String rriderID, String rdriverID){
        if  (rRiderScore==null && rDriverScore !=null || rRiderScore!= null && rDriverScore == null) {
                rateRide.setText("Pending");
        }
        

    }

    private void driverAcceptRide(Integer activeRideId, String driverId) throws IOException {

        URL url = new URL("http://10.0.2.2:8080/acceptRide/Driver?accRideId="+activeRideId +"&driverID="+driverId); //set URL
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

            startActivity(new Intent(RideDetailsActivity.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Ride Accepted \n  Details:\n" + updatedRide.toString()));
            //get response status code

        } catch (IOException e) {
            //TODO: Add error message for user
            e.printStackTrace();
        }finally {
            conWeb.disconnect();
        }
    }
    private void riderAcceptRide(Integer activeRideId, String riderId) throws IOException {

        URL url = new URL("http://10.0.2.2:8080/acceptRide/Rider?accRideId="+activeRideId +"&riderID="+riderId); //set URL
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

            startActivity(new Intent(RideDetailsActivity.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Ride Accepted \n  Details:\n" + updatedRide.toString()));
            //get response status code

        } catch (IOException e) {
            //TODO: Add error message for user
            e.printStackTrace();
        }finally {
            conWeb.disconnect();
        }

    }


}








