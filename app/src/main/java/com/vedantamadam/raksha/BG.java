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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;

import android.os.IBinder;
import android.os.Looper;

import android.telephony.SmsManager;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class BG extends Service implements ComponentCallbacks2 {


   // private Timer timer;
   // private TimerTask timerTask;
    public int counter=0;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String TAG = "tag";

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    String bgPhone1, bgPhone2, emergencySos="";

    SharedPreferences sharedPrefPhone;

    private LocationRequest mLocationRequest;
    LocationManager service;
    Location mLocation;
 //   private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
 //   private long FASTEST_INTERVAL = 2000; /* 2 sec */
    boolean enabled;
    List<Address> addresses;
    Geocoder geocoder;


    String cityName = "",preCityName="";
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

        geocoder = new Geocoder(this, Locale.getDefault());




      // startLocationUpdates();
        if(MyGlobalClass.phoneNumber1.length() == 0)
        {
            loadBgData();
            //    updateDa();

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



      //  startLocationUpdates();

        //   onTaskRemoved(intent);

       // startTimer();

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


   /* public void onTaskRemoved(Intent rootIntent) {


        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }*/

       @Override
       public void onDestroy(){
           super.onDestroy();
           //stoptimertask();

          /* Intent broadcastIntent = new Intent();
           broadcastIntent.setAction("restartservice");
           broadcastIntent.setClass(this, MyReceiver.class);
           this.sendBroadcast(broadcastIntent);*/
           mSensorManager.unregisterListener(mSensorListener);
            stopSelf();


       }

  /*  public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  "+ (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }*/

  /*  public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }*/


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
            //mAccel > 38 (used value)
             senValue = Integer.parseInt(MyGlobalClass.senstivityNumber);

                if (mAccel > (senValue)) {

                    startLocationUpdates();

                    Toast.makeText(getApplicationContext(), "Shake event detected", Toast.LENGTH_LONG).show();

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
                       // Toast.makeText(getApplicationContext(),emergencySos,Toast.LENGTH_LONG).show();
                        try {
                            addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        cityName = addresses.get(0).getAddressLine(0);
                        sosAlert();
                        //    Toast.makeText(getApplicationContext(),locationResult.getLastLocation().toString(), Toast.LENGTH_LONG).show();


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



       /* emergencySos =
                "[Emergency SOS] I have initiated this SOS message. \n\n You are my emergency contact and I need your help. \n\n I am at " + " https://www.google.com/maps/dir/?api=1&destination=" + location.getLatitude() +"," + location.getLongitude()
                        + "&travelmode=driving";






        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        cityName = addresses.get(0).getAddressLine(0);


        sosAlert();*/


    }

    public void sosAlert(){



     //   Toast.makeText(this,"location is" +cityName,Toast.LENGTH_LONG).show();

        bgPhone1 = MyGlobalClass.phoneNumber1;
        bgPhone2 = MyGlobalClass.phoneNumber2;
        if (!(cityName.equals(preCityName))){

            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> emerMsg;
            emerMsg = sms.divideMessage(emergencySos);
            if( bgPhone1.length() == 13) {
                sms.sendMultipartTextMessage(bgPhone1, null, emerMsg, null, null);
            }
            if( bgPhone2.length() == 13) {

                sms.sendMultipartTextMessage(bgPhone2, null, emerMsg, null, null);
            }
            Toast.makeText(this,"Sending message",Toast.LENGTH_LONG).show();
            preCityName = cityName;
            bgPhone1 = null;
            bgPhone2 = null;
        }





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
