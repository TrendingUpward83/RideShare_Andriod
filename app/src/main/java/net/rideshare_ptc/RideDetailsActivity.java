
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




public class RideDetailsActivity extends AppCompatActivity {


    TextView rideDetails;
    Ride ride = new Ride();
    Button returnToMenu;
    Button driverDetails;
    Button riderDetails;
    Button acceptRide;
    Button rateRide;
    String rrideJSON;
    String userRiderId;
    String userDriverId;
    String RideType;
    Byte isDriver;
    User loggedInUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_details);
        returnToMenu = (Button) findViewById(R.id.btnRideRetMenu);
        driverDetails = (Button) findViewById(R.id.btnViewRideDriver);
        riderDetails = (Button) findViewById(R.id.btnViewRideRider);
        acceptRide = (Button) findViewById(R.id.btnAcceptRide);
        rateRide = (Button) findViewById(R.id.btnRateRide);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        String rideInformation = bundle.getString("Ride Details");
        TextView msg = (TextView) findViewById(R.id.txtViewRideDetail);
        msg.setMovementMethod(new ScrollingMovementMethod());
        msg.setText(rideInformation);

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

        getUserRole();


        if (active_ride.getRideInfo().getDriverID() == null) {
            driverDetails.setVisibility(View.INVISIBLE);
        } else if (active_ride.getRideInfo().getRiderID() == null) {
            riderDetails.setVisibility(View.INVISIBLE);
        }

        if (active_ride.getRideInfo().getDriverID().equals(loggedInUser.getUserID())||active_ride.getRideInfo().getRiderID().equals(loggedInUser.getUserID())){
            acceptRide.setVisibility(View.INVISIBLE);
        }


        if (rideTaken == 1 || rideCompleted == 1) {
            acceptRide.setVisibility(View.INVISIBLE);
        } else {
            getRideType(driverID, riderID);
            if (RideType == "Requested") {
                if (isDriver == 0) { //if ride has no driver and you're not a driver, can't accept
                    acceptRide.setVisibility(View.INVISIBLE);
                } else if (isDriver == 1) {//driver can accept
                    acceptRide.setVisibility(View.VISIBLE);
                }
            } else if (RideType == "Posted") { //if driver posted, doesn't matter if driver or rider accept, driver is also rider.

                    acceptRide.setVisibility(View.VISIBLE);
            }
        }

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
                        getRideType(driverID, riderID);
                        if (RideType == "Requested") {

                                try {
                                    driverAcceptRide(activeRideID, UserId);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                        } else if (RideType == "Posted") { //if driver posted, doesn't matter if driver or rider accept, driver is also rider.
                            try {
                                riderAcceptRide(activeRideID, UserId);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                }
            });
            rateRide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //is ride available
                    if ((UserId.equals(driverID) || UserId.equals(riderID)) ) { //if user accepted & ride is not complete
                        if (rideTaken == 1 && rideCompleted==0 ) {//if ride has both parties, is then able to be possible to be complete
                            startActivity(new Intent(RideDetailsActivity.this, RateRide.class)); //go to rating activity
                        } else
                            startActivity(new Intent(RideDetailsActivity.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Unable to rate- this ride is already completed."));
                    }
                }
            });

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

    public void getUserRole(){
        userDriverId = "";
        userRiderId = "";
        if (isDriver == 1) {
            userDriverId = loggedInUser.getUserID();
        } else if (isDriver == 0) {
            userRiderId = loggedInUser.getUserID();
        }
    }
}








