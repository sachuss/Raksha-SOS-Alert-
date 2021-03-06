package com.vedantamadam.raksha;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;

import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.provider.Settings;

import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int PERM_REQUEST_CODE = 100, PERM_REQUEST_CODE_API26 = 101;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5000;
    private static final long UPDATE_INTERVAL = 5 * 1000 ; // 5 seconds
    private static final long FASTEST_INTERVAL = 1 * 1000; // 1 second


    String msg;
    Button sosBut;

    Intent locIntent;

    boolean ranBefore;
    String cityNameLat = "", cityNameLon = "", preCityNameLat = "", preCityNameLon = "";
    LocationManager service;
    Location mLocation;
    Criteria criteria;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    MediaPlayer welcomeMusic;




    boolean enabled;
    private Dialog dialog;
    private LocationRequest mLocationRequest;
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {


            fallAlertBuild();

        }

    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(broadcastReceiver, new IntentFilter("FALL DETECTED"));


        sosBut = findViewById(R.id.sosButton);
        Spinner sosSpin = findViewById(R.id.spinner);


        sosSpin.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> dataAdaptor = ArrayAdapter.createFromResource(this, R.array.spinnerItems, R.layout.support_simple_spinner_dropdown_item);
        dataAdaptor.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sosSpin.setAdapter(dataAdaptor);

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);





        if(isFirstTime())
        {popUpMessage();}



        if( ranBefore == true) {
            checkPermission();
            service = (LocationManager) getSystemService(LOCATION_SERVICE);
            enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!enabled) {
                enableGps();
            }

            if (!Settings.canDrawOverlays(getApplicationContext())) {
                enableDisplayOver();
            }

            turnonService(); //Turn on background service if falldetection is already turned on

        }




        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        sosBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Button shake animation*/
                Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_shake);
                view.startAnimation(shake);
                /*Button shake animation*/






                if (MyGlobalClass.if_sosnumber_exist(getApplicationContext())) {
                    /*FusedLocationProviderClient*/
                    startLocationUpdates();

                } else {
                    Toast.makeText(getApplicationContext(), "Please set emergency phone numbers in SOS Numbers first.", Toast.LENGTH_LONG).show();
                }
                    }


        });


        /*FusedLocationClient Callback*/
locationCallback = new LocationCallback(){

  public void onLocationResult(LocationResult locationResult)
  {

      MyGlobalClass.time_onLocationResult = System.currentTimeMillis();
      mLocation = locationResult.getLastLocation();
      String time_forLocFetch = MyGlobalClass.time_forLocFetch();

      msg =
              "[Emergency SOS] I have initiated this SOS message on " + MyGlobalClass.timestamp + " \n\nYou are my emergency contact and I need your help. \n\nI am at " + " https://maps.google.com/?q=" + mLocation.getLatitude() + "," + mLocation.getLongitude()
                      + "\nLocation fetched in " + time_forLocFetch + "ms";

      cityNameLat = String.valueOf(mLocation.getLatitude());
      cityNameLon = String.valueOf(mLocation.getLongitude());
      if ((cityNameLat.equals(preCityNameLat)) && (cityNameLon.equals(preCityNameLon))) {
          Toast.makeText(getApplicationContext(), "An SOS message with current location details is already sent.", Toast.LENGTH_LONG).show();
          MyGlobalClass.fall = true;
          fusedLocationProviderClient.removeLocationUpdates(locationCallback);
      } else {

          MyGlobalClass glbclsobj = new MyGlobalClass();
          glbclsobj.sendSMS(getApplicationContext(),msg);
          MyGlobalClass.fall = true;
          fusedLocationProviderClient.removeLocationUpdates(locationCallback);
      }

      try {
          onLocationChanged(locationResult.getLastLocation());
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
      if(locationAvailability.isLocationAvailable() == false)
      {Toast.makeText(getApplicationContext(),"Sorry, unable to fetch the location of your mobile. Please turn on Location in your mobile settings.",Toast.LENGTH_LONG).show();}
        super.onLocationAvailability(locationAvailability);
    }
};

    }

    private void turnonService() {

        Boolean fallDetection_enabled = Boolean.parseBoolean(MyGlobalClass.read_pref(getApplicationContext(),"fallDetection_enabled"));
        if(fallDetection_enabled){
            Intent serviceIntent = new Intent(getApplicationContext(), BG.class);
            serviceIntent.putExtra("inputExtra", "");
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            MyGlobalClass.senstivityNumber = MyGlobalClass.read_pref(getApplicationContext(),"spinner_value");
        }
    }

    private boolean isFirstTime()
    {

        ranBefore = (Boolean.valueOf(MyGlobalClass.read_pref(getApplicationContext(),"FirstLaunch")));
      if(!ranBefore)
      {
          MyGlobalClass.save_pref(getApplicationContext(),"FirstLaunch","true");

      }
      return !ranBefore;
    }


    public void popUpMessage()
    {
        AlertDialog.Builder onBoarding;
        TextView textView;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            onBoarding = new AlertDialog.Builder(this,R.style.CustomAlertDialog);
            onBoarding.setTitle("Onboarding Instructions:");
        }
        else
        {
            onBoarding = new AlertDialog.Builder(this);
            textView = new TextView(this);
            textView.setText("Onboarding Instructions:");
            textView.setTextSize(20);
            textView.setTextColor(Color.parseColor("#33e2e5"));
            onBoarding.setCustomTitle(textView);
        }






        onBoarding.setMessage("\nWelcome to Raksha" +
                "\n\n1) Register your emergency contact numbers under HOME/SOS and SAVE." +
                "\n\n2) Press the SOS button on the home page in case of an emergency. On pressing the button, an SOS message along with the link of your current location on google  maps will be send to the emergency contacts." +
                "\n\n3) Use the switch at the 'Fall Detection' page to turn ON/OFF Fall detection functionality and set the Sensitivity value as per your requirement. Switch turned ON will run the application in the background and if any violent shake/fall is detected,  an auto SOS procedure is initiated. You are advised to set your most preferable sensitivity value before registering contacts." +
                "\n\n4) Please tick 'Don't Optimise' for Raksha app in the Battery Settings for better performance of FALL DETECTION functionality: How to do it:- Please check the link on Instructions page." +
                "\n\n5) For dual sim phones, please make sure to insert your messaging SIM in slot one.");

        onBoarding.setCancelable(false);

        onBoarding.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                welcomeMusic.stop();

                checkPermission();


                service = (LocationManager) getSystemService(LOCATION_SERVICE);
                enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (!enabled) {
                    enableGps();
                }

                if (!Settings.canDrawOverlays(getApplicationContext())) {
                    enableDisplayOver();
                }

            }
        });
        AlertDialog alertDialog = onBoarding.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                 welcomeMusic = MediaPlayer.create(getApplicationContext(),R.raw.welcome);
                welcomeMusic.start();
            }

        });
        alertDialog.show();


        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT ));


    }



    public void onDestroy() {

        MyGlobalClass.fall = true;
        super.onDestroy();

    }

    // Function to check and request permission.
    public void checkPermission() {


        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O) { /* Sending sms error in API 26: due to absence of read phone state permission: READ_PHONE_STATE PERMISSION ADDED*/
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                        {Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, PERM_REQUEST_CODE_API26);
            }
        }
        else /*Normal Permissions*/
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]
                        {Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, PERM_REQUEST_CODE);
            }

        }


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERM_REQUEST_CODE: {
          //      if (((grantResults).length > 0) && (grantResults[0] + grantResults[1] + grantResults[2]) == PackageManager.PERMISSION_GRANTED) {
                if ( (grantResults[0]  == PackageManager.PERMISSION_GRANTED) && (grantResults[1]  == PackageManager.PERMISSION_GRANTED)) {
//                    Toast.makeText(this, "SMS & Location permissions granted", Toast.LENGTH_SHORT).show();


                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Please grant SMS/Location/Read_Contact permissions to proceed further.\n" +
                            "Application is going to exit");
                    builder.setCancelable(false);
                    builder.setTitle("PERMISSIONS");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

            }
            case PERM_REQUEST_CODE_API26: {
                //      if (((grantResults).length > 0) && (grantResults[0] + grantResults[1] + grantResults[2]) == PackageManager.PERMISSION_GRANTED) {
                if ( (grantResults[0]  == PackageManager.PERMISSION_GRANTED) && (grantResults[1]  == PackageManager.PERMISSION_GRANTED)
                        && (grantResults[2]  == PackageManager.PERMISSION_GRANTED)   ) {
//                    Toast.makeText(this, "SMS & Location permissions granted", Toast.LENGTH_SHORT).show();


                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Please grant SMS/Location/Read_Phone_State/Read_Contact permissions to proceed further.\n" +
                            "Application is going to exit");
                    builder.setCancelable(false);
                    builder.setTitle("PERMISSIONS");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

            }


        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                enableDisplayOver();
            } else {
                // Do as per your logic
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void enableGps() {


        AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(this);
        gpsBuilder.setMessage("Raksha requires the location of your mobile for sending SOS SMS. Please turn on Location in your mobile settings.");
        gpsBuilder.setCancelable(false);

        gpsBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                locIntent = new Intent((Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                startActivity(locIntent);


            }
        });
        gpsBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Toast.makeText(getApplicationContext(),"Raksha requires the location of your mobile for sending SOS SMS. Please turn on Location in your mobile settings.",Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog dialog = gpsBuilder.create();
        dialog.show();


    }

    protected void enableDisplayOver() {
        AlertDialog.Builder displayOver = new AlertDialog.Builder(this);
        displayOver.setMessage("Raksha requires permission to display over other apps.");
        displayOver.setCancelable(false);

        displayOver.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(getApplicationContext())) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                    }
                }


            }
        });
        displayOver.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
            }
        });
        AlertDialog dialog = displayOver.create();
        dialog.show();
    }

    // Trigger new location updates at interval
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void startLocationUpdates() {



        DateFormat df = new SimpleDateFormat("dd.MM.yyyy, h:mm a");
        MyGlobalClass.timestamp = df.format(Calendar.getInstance().getTime());

        // Create the location request to start receiving updates
        MyGlobalClass.time_startLocUpdates = System.currentTimeMillis();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
       mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationRequest.setNumUpdates(1);


        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);


        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_REQUEST_CODE);
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback,null);


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onLocationChanged(Location location) throws IOException {
        // New location has now been determined

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {


        super.onResume();
    }

    @Override
    protected void onPause() {


        super.onPause();
    }

    protected void onStop() {


        super.onStop();


    }

    protected void onStart() {


        super.onStart();


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        String item = adapterView.getItemAtPosition(i).toString();
        switch (item) {
            case "Home":
                break;
            case "Instructions":
                Intent intent = new Intent(MainActivity.this, about.class);
                startActivity(intent);
                break;
            case "SOS":
                Intent intent1 = new Intent(MainActivity.this, SosActivity.class);
                startActivity(intent1);
                break;
            case "Fall Detection":
                Intent intent2 = new Intent(MainActivity.this, FallDetection.class);
                startActivity(intent2);
                break;

        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void fallAlertBuild() {

        if (MyGlobalClass.fall == true) {


            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);

            builder.setMessage("Your device suffered a violent shake.\n" + "Do you want to cancel sending Emergency SOS  ?")
                    .setCancelable(false)

                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            MyGlobalClass.fall = true;


                            // TODO: Add positive button action code here
                        }
                    });


            dialog = builder.create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                private static final int AUTO_DISMISS_MILLIS = 10000;

                @Override
                public void onShow(final DialogInterface dialog) {
                    final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    final CharSequence positiveButtonText = defaultButton.getText();
                    new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            defaultButton.setText(String.format(
                                    Locale.getDefault(), "%s (%d)",
                                    positiveButtonText,
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero

                            ));
                        }

                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onFinish() {
                            if (((AlertDialog) dialog).isShowing()) {
                                if (MyGlobalClass.if_sosnumber_exist(getApplicationContext())) {
                                    /*FusedLocationProviderClient*/
                                    startLocationUpdates();

                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Please set emergency phone numbers in SOS Numbers first.", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    MyGlobalClass.fall = true;
                                }
                            }
                        }
                    }.start();
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
            dialog.show();


            MyGlobalClass.fall = false;
        }

    }

}