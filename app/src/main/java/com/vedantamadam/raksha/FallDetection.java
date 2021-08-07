package com.vedantamadam.raksha;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;

import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;


public class FallDetection extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {


    Switch bgSwitch;

    String spinner_value;

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


        spinner = (Spinner) findViewById(R.id.spinnerValue);
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        ArrayAdapter<CharSequence> dAdaptor = ArrayAdapter.createFromResource(this, R.array.sensitivity_value, R.layout.spinner_item);
        dAdaptor.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dAdaptor);



        fallDetection = (Toolbar) findViewById(R.id.fallDetection);

        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!service.isProviderEnabled(LocationManager.GPS_PROVIDER))
        { Toast.makeText(getApplicationContext(),"Raksha requires the location of your mobile for sending SOS SMS. Please turn on Location in your mobile settings. ",Toast.LENGTH_LONG).show();}


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


                    Intent serviceIntent = new Intent(getApplicationContext(), BG.class);
                    serviceIntent.putExtra("inputExtra", "");
                    if (isChecked) {

                        MyGlobalClass.save_pref(getApplicationContext(),"fallDetection_enabled","true");
                        MyGlobalClass.save_pref(getApplicationContext(),"spinner_value",MyGlobalClass.senstivityNumber);
                        MyGlobalClass.save_pref(getApplicationContext(),"spinner_pos", Integer.toString(spinner.getSelectedItemPosition()));
                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
                        spinner.setEnabled(false);
                    } else {

                        spinner.setEnabled(true);
                        MyGlobalClass.save_pref(getApplicationContext(),"fallDetection_enabled","false");
                        unregisterList();
                    }
                }
        });






    }

    public void unregisterList() {

        Intent serviceIntent = new Intent(getApplicationContext(), BG.class);

        stopService(serviceIntent);

    }


    public void loadSwitchState() {


        fallDetection_enabled = Boolean.parseBoolean(MyGlobalClass.read_pref(getApplicationContext(),"fallDetection_enabled"));
        spinner_value = MyGlobalClass.read_pref(getApplicationContext(),"spinner_value");
        String spinner_pos = MyGlobalClass.read_pref(getApplicationContext(),"spinner_pos");
        index = spinner_pos !=null ? Integer.parseInt(spinner_pos):0;
        bgSwitch.setChecked(fallDetection_enabled);
        spinner.setSelection(index);
        if (fallDetection_enabled){
            spinner.setEnabled(false);
        }

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
