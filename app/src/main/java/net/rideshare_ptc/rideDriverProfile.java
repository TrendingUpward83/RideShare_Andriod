package net.rideshare_ptc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class rideDriverProfile extends AppCompatActivity {
    User aUser;
    Byte isADriver;
    Ride thisRide;
    String activeRideDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        Button btnReturnHome = (Button) findViewById(R.id.btnReturnHome2);
        Button btnCarInfo = (Button) findViewById(R.id.btnCarInfo2);
        Button btnLogout = (Button) findViewById(R.id.btnLogout2);

        TextView fName = (TextView) findViewById(R.id.txtFirstName2);
        TextView lName = (TextView) findViewById(R.id.txtLastName2);
        TextView riderScore = (TextView) findViewById(R.id.txtRiderScore2);
        TextView driverScore = (TextView) findViewById(R.id.txtDriverScore2);
        TextView role = (TextView) findViewById(R.id.txtRole2);
        ImageView pfp = (ImageView) findViewById(R.id.profileIMG);


        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            ActiveRide curRide = ActiveRide.getInstance();
            thisRide = curRide.getRideInfo();

            activeRideDriver = thisRide.getDriverID();
            if (activeRideDriver == null) {
                startActivity(new Intent(rideDriverProfile.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "No driver accepted this requested ride yet."));
            } else {
                try {

                    aUser = getRideUserDetails(activeRideDriver);
                    isADriver = aUser.getIsDriver();
                    fName.setText(aUser.getUserFName());
                    lName.setText(aUser.getUserLName());
                    String pullPhoto = aUser.getProfileImgFile();


                    String photoLink = "a" + pullPhoto.substring(0, 8);



                    String PACKAGE_NAME = getApplicationContext().getPackageName();
                    int imgId = getResources().getIdentifier(PACKAGE_NAME+":drawable/"+photoLink , null, null);


                    System.out.println("IMG ID :: "+imgId);
                    System.out.println("PACKAGE_NAME :: "+PACKAGE_NAME);
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),imgId);
                    pfp.setImageBitmap(BitmapFactory.decodeResource(getResources(),imgId));



                    float numRiderScore = aUser.getuRiderScore();
                    String riderScoreTxt = Float.toString(numRiderScore);
                    riderScore.setText(riderScoreTxt);

                    float numDriverScore = aUser.getuDriverScore();
                    String DriverScoreTxt = Float.toString(numDriverScore);
                    driverScore.setText(DriverScoreTxt);

                    if (isADriver == 1) {
                        role.setText("Driver");
                        btnCarInfo.setVisibility(View.VISIBLE);
                    } else {
                        role.setText("Rider");
                        btnCarInfo.setVisibility(View.INVISIBLE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        btnReturnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(rideDriverProfile.this, MainMenu.class));
            }
        });

        btnCarInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(rideDriverProfile.this, RideDriverCarInfo.class));
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager mgr = LoginManager.getInstance();
                mgr.removeLoggedInUsers();
                //startActivity(new Intent(MainMenu.this,DriverOnlySplash.class).putExtra("Success Ride Posted","User Logged out: "+ mgr.getLoggedInUserList().toString()));
                startActivity(new Intent(rideDriverProfile.this, MainActivity.class));
            }
        });
    }
    public User getRideUserDetails(String driverID) throws IOException {

        User retUser = null;
        int resCode = 0;
        String strResponse = "";


        URL urld = new URL("http://10.0.2.2:8080/user?User="+driverID);
        HttpURLConnection con = (HttpURLConnection) urld.openConnection();

        con.setUseCaches(false);
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.connect();

        try {
            BufferedReader buffread = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = buffread.readLine()) != null) {
                stringBuilder.append(line);
            }
            buffread.close();
            strResponse = stringBuilder.toString();
            resCode = con.getResponseCode();
            ObjectMapper mapper = new ObjectMapper();
            try {
                retUser = mapper.readValue(strResponse,  User.class);
            } catch (JsonGenerationException ge) {
                System.out.println(ge);
            } catch (JsonMappingException me) {
                System.out.println(me);
            }

        } catch (IOException e){
            startActivity(new Intent(rideDriverProfile.this, DriverOnlySplash.class).putExtra("Success Ride Posted", "Connection Error: \n ERROR \n"+"URL"+ urld.toString()+"\n"+e));
        }
        con.disconnect();
        return retUser;
    }


}