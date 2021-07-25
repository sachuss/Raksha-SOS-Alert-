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

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SosActivity extends AppCompatActivity  {
    Toolbar toolbarSos;
    EditText emergencyNo1, emergencyNo2;
    Button saveBut,clearBut;
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


       /* servic = (LocationManager) getSystemService(LOCATION_SERVICE);
        enable = servic.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(! enable)
        {enabledGps();}*/



        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Delete existing SOS contact number before a new save.
                //This will be helpful to prevent persisting old values when one of the sos contact number needs to deleted.
                MyGlobalClass.delete_pref(getApplicationContext(),"e1");
                MyGlobalClass.delete_pref(getApplicationContext(),"e2");

                String ph1 = PhoneNumberUtils.formatNumberToE164(emergencyNo1.getText().toString(), 
                String ph2 = PhoneNumberUtils.formatNumberToE164(emergencyNo2.getText().toString(), "IN");
                boolean goodph = false;

                if ((ph1 == null || ph1.isEmpty()) && (ph2 == null || ph2.isEmpty())) {
                    // If there is no valid mobile no. entered or both the phonenumber edittext are empty
                    Toast.makeText(getApplicationContext(), "Please enter atleat one valid SOS contact number.", Toast.LENGTH_SHORT).show();
                } else if (emergencyNo1.getText().length()!=0 && ph1 == null){
                    //Invalid mobile no. is entered in emergencyNo1
                    Toast.makeText(getApplicationContext(), "Please enter valid SOS contact number.", Toast.LENGTH_SHORT).show();
                    emergencyNo1.getText().clear();
                } else if (emergencyNo2.getText().length()!=0 && ph2 == null){
                    //Invalid mobile no. is entered in emergencyNo2
                    Toast.makeText(getApplicationContext(), "Please enter valid SOS contact number.", Toast.LENGTH_SHORT).show();
                    emergencyNo2.getText().clear();
                } else { // All the entered SOS contact number. Going to save
                    if (ph1 != null) {
                        MyGlobalClass.save_pref(getApplicationContext(), "e1", ph1);
                    }
                    if (ph2 != null) {
                        MyGlobalClass.save_pref(getApplicationContext(), "e2", ph2);
                    }

                    Toast.makeText(getApplicationContext(), "Successfully saved SOS contact numbers.", Toast.LENGTH_LONG).show();
                    Intent sosIntent = new Intent(SosActivity.this, MainActivity.class);
                    startActivity(sosIntent);
                }
            }
        });

        clearBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((emergencyNo1.getText().length() != 0) || (emergencyNo2.getText().length() != 0) ) {
//                    sharedPreferences = getSharedPreferences("emergencybook", MODE_PRIVATE);
//                    SharedPreferences.Editor edito = sharedPreferences.edit();
//                    edito.clear().commit();

                    MyGlobalClass.delete_pref(getApplicationContext(),"e1");
                    MyGlobalClass.delete_pref(getApplicationContext(),"e2");



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


//        sharedPreferences = getSharedPreferences("emergencybook",MODE_PRIVATE);
//        phoneN1 = sharedPreferences.getString("e1","");
//        phoneN2 = sharedPreferences.getString("e2","");

        phoneN1 = MyGlobalClass.read_pref(getApplicationContext(),"e1");
        phoneN2 = MyGlobalClass.read_pref(getApplicationContext(),"e2");

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

    public void onStop() {

        super.onStop();
   


    }




}