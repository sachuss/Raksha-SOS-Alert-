package com.vedantamadam.raksha;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ActivityManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SosActivity extends AppCompatActivity  {
    Toolbar toolbarSos;
    EditText emergencyNo1, emergencyNo2;
    Button saveBut,clearBut;
    SharedPreferences sharedPreferences;
    private String phoneN1, phoneN2;
    public  String appendedPh1,appendedPh2;
    LocationManager servic;
    boolean enable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
         emergencyNo1 = (EditText) findViewById(R.id.sos1);
         emergencyNo2 = (EditText)  findViewById(R.id.sos2);

         saveBut = (Button)  findViewById(R.id.save);
         clearBut = (Button) findViewById(R.id.butClear);


        toolbarSos = (Toolbar) findViewById(R.id.toolbarSOS);
        toolbarSos.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });


        servic = (LocationManager) getSystemService(LOCATION_SERVICE);
        enable = servic.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(! enable)
        {enabledGps();}



        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if((emergencyNo1.getText().length()  == 10) || (emergencyNo2.getText().length()  == 10))   {





                        sharedPreferences = getSharedPreferences("emergencybook", MODE_PRIVATE);
                        SharedPreferences.Editor edito = sharedPreferences.edit();
                        edito.putString("e1", emergencyNo1.getText().toString());
                        edito.putString("e2", emergencyNo2.getText().toString());

                    edito.apply();



                        appendedPh1 = ("+91" + emergencyNo1.getText().toString());
                        MyGlobalClass.phoneNumber1 = appendedPh1;


                        appendedPh2 = ("+91" + emergencyNo2.getText().toString());
                        MyGlobalClass.phoneNumber2 = appendedPh2;



                    Toast.makeText(getApplicationContext(),"Saving data...",Toast.LENGTH_LONG).show();

                    Intent sosIntent = new Intent(SosActivity.this,MainActivity.class);
                    startActivity(sosIntent);



                }

                else
                { Toast.makeText(getApplicationContext(),"Please enter two phone numbers or Check for number of digits",Toast.LENGTH_SHORT).show();}
            }
        });

        clearBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((emergencyNo1.getText().length() != 0) || (emergencyNo2.getText().length() != 0) ) {
                    sharedPreferences = getSharedPreferences("emergencybook", MODE_PRIVATE);
                    SharedPreferences.Editor edito = sharedPreferences.edit();
                    edito.clear().commit();

                    emergencyNo1.getText().clear();
                    emergencyNo2.getText().clear();

                    appendedPh1 = null;
                    appendedPh2 = null;

                    MyGlobalClass.phoneNumber1="";
                    MyGlobalClass.phoneNumber2="";

                }
            }
        });


        if(emergencyNo1.getText().length() == 0 && emergencyNo2.getText().length() == 0 ) {
            loadDat();
            updateView();
        }

    }

    public void loadDat()
    {


        sharedPreferences = getSharedPreferences("emergencybook",MODE_PRIVATE);
        phoneN1 = sharedPreferences.getString("e1","");
        phoneN2 = sharedPreferences.getString("e2","");


    }

    public void updateView()
    {
        emergencyNo1.setText(phoneN1);
        emergencyNo2.setText(phoneN2);

    }


    protected void enabledGps()
    {


        AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(this);
        gpsBuilder.setMessage("Without GPS turned on our application will not work");

        gpsBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                                        ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();


            }
        });

        AlertDialog dialog = gpsBuilder.create();
        dialog.show();






    }




}