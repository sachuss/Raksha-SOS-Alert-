package com.vedantamadam.raksha;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.app.ActivityManager;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;



import android.location.Location;

import android.location.LocationManager;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;


import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;


import android.widget.Spinner;

import android.widget.Toast;


import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;


import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {

    private static final int SMS_LOC_REQUEST_CODE = 100;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5000;
    private Dialog dialog, prevDialog;
    int count, i;







    String msg;

    Button sosBut;

    String phGlobal1, phGlobal2;
    Intent locIntent;
    SharedPreferences sharedPref;
    String cityNameLat="", cityNameLon="", preCityNameLat="", preCityNameLon="";



    private LocationRequest mLocationRequest;




    LocationManager service;
    Location mLocation;
    boolean enabled;




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(broadcastReceiver, new IntentFilter("FALL DETECTED"));




        sosBut = (Button) findViewById(R.id.sosButton);
        Spinner sosSpin = (Spinner) findViewById(R.id.spinner);




        sosSpin.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        ArrayAdapter<CharSequence> dataAdaptor = ArrayAdapter.createFromResource(this, R.array.spinnerItems, R.layout.support_simple_spinner_dropdown_item);
        dataAdaptor.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sosSpin.setAdapter(dataAdaptor);






        checkPermission();


        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            enableGps();
        }

        if (!Settings.canDrawOverlays(this)){
        enableDisplayOver();}



        if(MyGlobalClass.phoneNumber1.length() > 0)
        {
        sharedPref = getSharedPreferences("globalbook", MODE_PRIVATE);
        SharedPreferences.Editor edi = sharedPref.edit();
        edi.putString("g1", MyGlobalClass.phoneNumber1);
        edi.putString("g2", MyGlobalClass.phoneNumber2);

        edi.apply();
        }

        if(MyGlobalClass.phoneNumber1.length() == 0)
        {loadDa();}



        sosBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startLocationUpdates();





            }
        });









    }



    public void onDestroy() {

        MyGlobalClass.fall = true;
        super.onDestroy();

    }

    public void loadDa() {


        sharedPref = getSharedPreferences("globalbook", MODE_PRIVATE);
        MyGlobalClass.phoneNumber1 = sharedPref.getString("g1", "");
        MyGlobalClass.phoneNumber2 = sharedPref.getString("g2", "");




    }





    // Function to check and request permission.
    public void checkPermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
         {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, SMS_LOC_REQUEST_CODE);
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SMS_LOC_REQUEST_CODE: {
                if (((grantResults).length > 0) && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "SMS & Location permissions granted", Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(this, "SMS/Permissions Denied", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Please grant SMS & Location permissions to proceed further.\n" +
                            "Application is going to exit");
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
             ;
                // Do as per your logic
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void enableGps() {


        AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(this);
        gpsBuilder.setMessage("TURN ON GPS");

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
                ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
            }
        });
        AlertDialog dialog = gpsBuilder.create();
        dialog.show();




    }

    protected void enableDisplayOver()
    {
        AlertDialog.Builder displayOver = new AlertDialog.Builder(this);
        displayOver.setMessage("Please allow Raksha to display over other apps.");

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

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      //  mLocationRequest.setInterval(UPDATE_INTERVAL);
     //   mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationRequest.setNumUpdates(1);



        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //  mLocationRequest.

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //  fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here



                mLocation = locationResult.getLastLocation();
                msg =
                        "[Emergency SOS] I have initiated this SOS message. \n\n You are my emergency contact and I need your help. \n\n I am at " + " https://www.google.com/maps/dir/?api=1&destination=" + mLocation.getLatitude() + "," + mLocation.getLongitude()
                                + "&travelmode=driving";

                        cityNameLat = String.valueOf(mLocation.getLatitude());
                        cityNameLon = String.valueOf(mLocation.getLongitude());


                        if((cityNameLat.equals(preCityNameLat)) && (cityNameLon.equals(preCityNameLon))) {
                            Toast.makeText(getApplicationContext(),"Location same as the previous send location...",Toast.LENGTH_SHORT).show();
                            MyGlobalClass.fall = true;
                        }
                       else {

                            phGlobal1 = MyGlobalClass.phoneNumber1;
                            phGlobal2 = MyGlobalClass.phoneNumber2;
                            if (phGlobal1.length() > 0 && phGlobal2.length() > 0) {
                                String[] phNos = {phGlobal1, phGlobal2};
                                MyGlobalClass glbclsobj = new MyGlobalClass();
                                glbclsobj.sendSMS(phNos, msg);
                                MyGlobalClass.fall = true;
                                Toast.makeText(getApplicationContext(), "Sending message...", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter SOS numbers in SOS page", Toast.LENGTH_SHORT).show();
                            }
                            preCityNameLat = cityNameLat;
                            preCityNameLon = cityNameLon;

                        }




                        try {
                            onLocationChanged(locationResult.getLastLocation());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },
                Looper.myLooper());
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
        switch(item){
        case "Home":
        break;
        case "Instructions":
        Intent intent = new Intent(MainActivity.this,about.class);
        startActivity(intent);
        break;
        case "SOS":
        Intent intent1 = new Intent(MainActivity.this,SosActivity.class);
        startActivity(intent1);
        break;
            case "Fall Detection":
                Intent intent2 = new Intent(MainActivity.this,FallDetection.class);
                startActivity(intent2);
                break;

        }


        }

@Override
public void onNothingSelected(AdapterView<?> adapterView) {

        }

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            //  Toast.makeText(getApplicationContext(),"In main activity",Toast.LENGTH_SHORT).show();

        fallAlertBuild();

            }

    };

    public void fallAlertBuild(){

        if(MyGlobalClass.fall == true) {


            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);

            builder.setMessage("Your device suffered a violent shake.\n" + "Do you want to cancel sending Emergency SOS  ?")

                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                             /*   Intent intent1 = new Intent(Intent.ACTION_MAIN);
                                intent1.addCategory(Intent.CATEGORY_HOME);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent1);
                                Toast.makeText(getApplicationContext(),"Exiting...",Toast.LENGTH_SHORT).show();*/
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
                                startLocationUpdates();
                                dialog.dismiss();
                            }
                        }
                    }.start();
                }
            });

           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
           } else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_BASE_APPLICATION);
           }
            dialog.show();


            MyGlobalClass.fall = false;
        }

    }

}