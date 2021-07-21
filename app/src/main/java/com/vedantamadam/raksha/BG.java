package com.vedantamadam.raksha;

import android.Manifest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.ComponentCallbacks2;
import android.content.Context;

import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.location.Location;

import android.location.LocationManager;
import android.os.Build;

import android.os.IBinder;
import android.os.Looper;


import android.widget.Toast;


import androidx.annotation.RequiresApi;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.io.IOException;

import java.util.Objects;



public class BG extends Service implements ComponentCallbacks2 {



    public int counter=0;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = "tag";

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    String  emergencySos="";

    SharedPreferences sharedPrefPhone;

    private LocationRequest mLocationRequest;
    LocationManager service;
    Location mLocation;

    boolean enabled;



    String cityNameLat="", cityNameLon="", preCityNameLat="", preCityNameLon="";
    float x,y,z,delta;
    int senValue;

    public BG() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {



        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;


        if(MyGlobalClass.phoneNumber1.length() == 0)
        {
            loadBgData();


        }


        sharedPrefPhone = getSharedPreferences("bgbook", MODE_PRIVATE);
        SharedPreferences.Editor edi = sharedPrefPhone.edit();
        edi.putString("b1", MyGlobalClass.phoneNumber1);
        edi.putString("b2", MyGlobalClass.phoneNumber2);


        edi.apply();



        super.onCreate();

    }
public void loadBgData()
{

    sharedPrefPhone = getSharedPreferences("bgbook",MODE_PRIVATE);
    MyGlobalClass.phoneNumber1= sharedPrefPhone.getString("b1","");
    MyGlobalClass.phoneNumber2 = sharedPrefPhone.getString("b2","");


}
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {




        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();


        Intent notificationIntent = new Intent(this, FallDetection.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        notificationBuilder.setContentTitle("Shake Detection & Auto SOS Active")
                .setContentText(input)
                .setSmallIcon(R.drawable.download)
                .setContentIntent(pendingIntent)


                .setSilent(true)

                .setOngoing(true)

                .build();
        final Notification notification = notificationBuilder.build();

            startForeground(1, notification);





        return START_STICKY;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager
                    .class);
            manager.createNotificationChannel(serviceChannel);
        }
    }




       @Override
       public void onDestroy(){
           super.onDestroy();


          /* Intent broadcastIntent = new Intent();
           broadcastIntent.setAction("restartservice");
           broadcastIntent.setClass(this, MyReceiver.class);
           this.sendBroadcast(broadcastIntent);*/
           mSensorManager.unregisterListener(mSensorListener);
            stopSelf();


       }




    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {




            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

             senValue = Integer.parseInt(MyGlobalClass.senstivityNumber);
             int offset = 0;
//                offset = 4; //Use when testing



            if (mAccel > (senValue + offset )) {
                     startLocationUpdates();

                     Toast.makeText(getApplicationContext(), "Shake event detected", Toast.LENGTH_SHORT).show();

                }




        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }




    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    //    mLocationRequest.setInterval(UPDATE_INTERVAL);
   //     mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

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
                        emergencySos =
                                "[Emergency SOS] I have initiated this SOS message. \n\n You are my emergency contact and I need your help. \n\n I am at " + " https://www.google.com/maps/dir/?api=1&destination=" + mLocation.getLatitude() + "," + mLocation.getLongitude()
                                        + "&travelmode=driving";

                        cityNameLat = String.valueOf(mLocation.getLatitude());
                        cityNameLon = String.valueOf(mLocation.getLongitude());
                       



                      if((cityNameLat.equals(preCityNameLat)) && (cityNameLon.equals(preCityNameLon))) {
                          Toast.makeText(getApplicationContext(),"Location not changed...",Toast.LENGTH_SHORT).show();
                      }
                      else{


                                 String[] phNos = {MyGlobalClass.phoneNumber1, MyGlobalClass.phoneNumber2};
                                 MyGlobalClass glbclsobj = new MyGlobalClass();
                                  glbclsobj.sendSMS(phNos, emergencySos);
                                 Toast.makeText(getApplicationContext(), "Sending message...", Toast.LENGTH_SHORT).show();
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






   /* @Override
    public void onTrimMemory(int level){

        switch(level)
        {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                Toast.makeText(getApplicationContext(),"HIDDEN",Toast.LENGTH_SHORT).show();
                Log.v(TAG,"TRIM_MEMORY_UI_HIDDEN");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                Toast.makeText(getApplicationContext(),"RUNNING MODERATE",Toast.LENGTH_SHORT).show();
                Log.v(TAG,"TRIM_MEMORY_RUNNING_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                Toast.makeText(getApplicationContext(),"RUNNING LOW",Toast.LENGTH_SHORT).show();
                Log.v(TAG,"TRIM_MEMORY_RUNNING_LOW");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                Toast.makeText(getApplicationContext(),"RUNNING CRITICAL",Toast.LENGTH_SHORT).show();
                Log.v(TAG,"TRIM_MEMORY_RUNNING_CRITICAL");
               // System.gc();
               //   stopSelf();
              //  stoptimertask();
              */


         /*  Intent broadcastIntent = new Intent();
           broadcastIntent.setAction("restartservice");
           broadcastIntent.setClass(this, MyReceiver.class);
           this.sendBroadcast(broadcastIntent);


                ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC,2000);
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP,150);*/

           /*     break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
                Toast.makeText(getApplicationContext(),"BACKGROUND",Toast.LENGTH_SHORT).show();
                Log.v(TAG,"TRIM_MEMORY_BACKGROUND");




                break;
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
                Toast.makeText(getApplicationContext(),"MODERATE",Toast.LENGTH_SHORT).show();
                Log.v(TAG,"TRIM_MEMORY_MODERATE");
                break;
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                Toast.makeText(getApplicationContext(),"COMPLETE",Toast.LENGTH_SHORT).show();
                Log.v(TAG,"TRIM_MEMORY_COMPLETE");
                break;




        }


        //    super.onTrimMemory(level);

    }*/



}
