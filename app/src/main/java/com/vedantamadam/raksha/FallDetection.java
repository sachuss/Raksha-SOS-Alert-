package com.vedantamadam.raksha;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;


public class FallDetection extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {


    Switch bgSwitch;
    EditText senstivityValue;
    String spinner_value;
//    SharedPreferences spSwitchState;
    Boolean fallDetection_enabled;
    Toolbar fallDetection;
    int index;
    Spinner spinner;
    LocationManager service;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detection);



        bgSwitch = (Switch) findViewById(R.id.fallDetect);
      //  senstivityValue = (EditText) findViewById(R.id.senstivity);

        spinner = (Spinner) findViewById(R.id.spinnerValue);
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        ArrayAdapter<CharSequence> dAdaptor = ArrayAdapter.createFromResource(this, R.array.sensitivity_value, R.layout.spinner_item);
        dAdaptor.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dAdaptor);



        fallDetection = (Toolbar) findViewById(R.id.fallDetection);

        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!service.isProviderEnabled(LocationManager.GPS_PROVIDER))
        { Toast.makeText(getApplicationContext(),"No Location Info will be available with Location turned OFF",Toast.LENGTH_LONG).show();}


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
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
               // MyGlobalClass.senstivityNumber = senstivityValue.getText().toString();

                    Intent serviceIntent = new Intent(getApplicationContext(), BG.class);
                    serviceIntent.putExtra("inputExtra", "");
                    if (isChecked) {
                      //  MyGlobalClass.senstivityNumber = senstivityValue.getText().toString();
//                        spSwitchState = getSharedPreferences("SwitchState", MODE_PRIVATE);
//                        SharedPreferences.Editor editSwitch = spSwitchState.edit();
//                        editSwitch.putBoolean("fallDetection_enabled", true);
//                        editSwitch.putString("spinner_value", MyGlobalClass.senstivityNumber);
//                        editSwitch.putInt("spinner_pos",spinner.getSelectedItemPosition());
//                        editSwitch.apply();
                        MyGlobalClass.save_pref(getApplicationContext(),"fallDetection_enabled","true");
                        MyGlobalClass.save_pref(getApplicationContext(),"spinner_value",MyGlobalClass.senstivityNumber);
                        MyGlobalClass.save_pref(getApplicationContext(),"spinner_pos", Integer.toString(spinner.getSelectedItemPosition()));
                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                        spinner.setEnabled(false);
                    } else {
                     //   MyGlobalClass.senstivityNumber = senstivityValue.getText().toString();
//                        spSwitchState = getSharedPreferences("SwitchState", MODE_PRIVATE);
//                        SharedPreferences.Editor editSwitch = spSwitchState.edit();
//                        editSwitch.putBoolean("fallDetection_enabled", false);
//                        editSwitch.putString("spinner_value", MyGlobalClass.senstivityNumber);
//                        editSwitch.putInt("spinner_pos",spinner.getSelectedItemPosition());
//                        editSwitch.apply();
                        spinner.setEnabled(true);
                        MyGlobalClass.save_pref(getApplicationContext(),"fallDetection_enabled","false");
                        unregisterList();
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
//        spSwitchState = getSharedPreferences("SwitchState", MODE_PRIVATE);
        //  bgSwitch.setChecked(true);
//        checking = spSwitchState.getBoolean("fallDetection", Boolean.parseBoolean(""));
//        sValue = spSwitchState.getString("spinner_value",""); //No use
//        index = spSwitchState.getInt("spinner_pos",0);

        fallDetection_enabled = Boolean.parseBoolean(MyGlobalClass.read_pref(getApplicationContext(),"fallDetection_enabled"));
        spinner_value = MyGlobalClass.read_pref(getApplicationContext(),"spinner_value");
        String spinner_pos = MyGlobalClass.read_pref(getApplicationContext(),"spinner_pos");
        index = spinner_pos !=null ? Integer.parseInt(spinner_pos):0;
        bgSwitch.setChecked(fallDetection_enabled);
        spinner.setSelection(index);
        if (fallDetection_enabled){
            spinner.setEnabled(false);
        }
      //  senstivityValue.setText(sValue);
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String item = adapterView.getItemAtPosition(i).toString();
        MyGlobalClass.senstivityNumber = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    public void onStop() {


        super.onStop();

    }
}
