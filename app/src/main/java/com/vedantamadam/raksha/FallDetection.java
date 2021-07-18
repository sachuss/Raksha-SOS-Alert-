package com.vedantamadam.raksha;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


public class FallDetection extends AppCompatActivity implements ComponentCallbacks2 {


    Switch bgSwitch;
    EditText senstivityValue;
    String sValue;
    SharedPreferences spSwitchState;
    Boolean checking;
    Toolbar fallDetection;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detection);



        bgSwitch = (Switch) findViewById(R.id.fallDetect);
        senstivityValue = (EditText) findViewById(R.id.senstivity);


        fallDetection = (Toolbar) findViewById(R.id.fallDetection);

        fallDetection.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });

        loadSwitchState();


        bgSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyGlobalClass.senstivityNumber = senstivityValue.getText().toString();
                if(MyGlobalClass.senstivityNumber.length() > 0) {
                    Intent serviceIntent = new Intent(getApplicationContext(), BG.class);
                    serviceIntent.putExtra("inputExtra", "");
                    if (bgSwitch.isChecked()) {
                        MyGlobalClass.senstivityNumber = senstivityValue.getText().toString();
                        spSwitchState = getSharedPreferences("SwitchState", MODE_PRIVATE);
                        SharedPreferences.Editor editSwitch = spSwitchState.edit();
                        editSwitch.putBoolean("s1", true);
                        editSwitch.putString("s2", MyGlobalClass.senstivityNumber);

                        editSwitch.apply();

                    } else {
                        MyGlobalClass.senstivityNumber = senstivityValue.getText().toString();
                        spSwitchState = getSharedPreferences("SwitchState", MODE_PRIVATE);
                        SharedPreferences.Editor editSwitch = spSwitchState.edit();
                        editSwitch.putBoolean("s1", false);
                        editSwitch.putString("s2", MyGlobalClass.senstivityNumber);
                        editSwitch.apply();
                    }

                    if (b) {




                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);


                    } else {

                        unregisterList();

                    }
                }
                else
                {Toast.makeText(getApplicationContext(),"Please enter sensitivity value",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }
            }
        });






    }

    public void unregisterList() {

        Intent serviceIntent = new Intent(getApplicationContext(), BG.class);
        //mSensorManager.unregisterListener(mSensorListener);
        stopService(serviceIntent);

    }


    public void loadSwitchState() {
        spSwitchState = getSharedPreferences("SwitchState", MODE_PRIVATE);
        //  bgSwitch.setChecked(true);
        checking = spSwitchState.getBoolean("s1", Boolean.parseBoolean(""));
        sValue = spSwitchState.getString("s2","");
        bgSwitch.setChecked(checking);
        senstivityValue.setText(sValue);
    }




}
