package com.vedantamadam.raksha;



import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;


import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SosActivity extends AppCompatActivity  {
    private static final int PICK_CONTACT1 = 911, PICK_CONTACT2 = 912, CONTACT_READ_PERMISSION_CODE = 913 ;
    Toolbar toolbarSos;
    EditText emergencyNo1, emergencyNo2;
    Button saveBut,clearBut;
    private String phoneN1, phoneN2;
    public  String appendedPh1,appendedPh2;



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





        emergencyNo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(contactPickerIntent, PICK_CONTACT1);


            }
        });
        emergencyNo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(contactPickerIntent, PICK_CONTACT2);


            }
        });







        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Delete existing SOS contact number before a new save.
                //This will be helpful to prevent persisting old values when one of the sos contact number needs to deleted.
                MyGlobalClass.delete_pref(getApplicationContext(),"e1");
                MyGlobalClass.delete_pref(getApplicationContext(),"e2");

                String ph1 = PhoneNumberUtils.formatNumberToE164(emergencyNo1.getText().toString(), "IN");
                String ph2 = PhoneNumberUtils.formatNumberToE164(emergencyNo2.getText().toString(), "IN");
                boolean goodph = false;

                if ((ph1 == null || ph1.isEmpty()) && (ph2 == null || ph2.isEmpty())) {
                    // If there is no valid mobile no. entered or both the phonenumber edittext are empty
                    Toast.makeText(getApplicationContext(), "Please enter at least one valid SOS contact number.", Toast.LENGTH_SHORT).show();
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == PICK_CONTACT1) && (resultCode == RESULT_OK)) {
            if (data != null) {
                Uri contactData = data.getData();

                try {

                    String id = contactData.getLastPathSegment();
                    String[] columns = {ContactsContract.CommonDataKinds.Phone.DATA, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                    Cursor phoneCur = getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    columns,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                            + " = ?", new String[]{id},
                                    null);

                    final ArrayList<String> phonesList = new ArrayList<String>();
                    String Name = null;
                    assert phoneCur != null;
                    if (phoneCur.moveToFirst()) {
                        do {
                            Name = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            String phone = phoneCur
                                    .getString(phoneCur
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                            phonesList.add(phone);

                        } while (phoneCur.moveToNext());

                    }


                    phoneCur.close();

                    if (phonesList.size() == 0) {
                        Toast.makeText(
                                this, "This contact does not contain any numbers...",
                                Toast.LENGTH_LONG).show();
                    } else if (phonesList.size() == 1) {
                        emergencyNo1.setText(phonesList.get(0));
                    } else {

                        final String[] phonesArr = new String[phonesList
                                .size()];
                        for (int i = 0; i < phonesList.size(); i++) {
                            phonesArr[i] = phonesList.get(i);
                        }

                        AlertDialog.Builder dialog = new AlertDialog.Builder(
                                SosActivity.this);
                        dialog.setTitle("Name : " + Name);
                        ((AlertDialog.Builder) dialog).setItems(phonesArr,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        String selectedPhoneNumber = phonesArr[which];
                                        emergencyNo1.setText(selectedPhoneNumber);
                                    }
                                }).create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    Log.e("FILES", "Failed to get phone data", e);
                }
            }

        }

        if ((requestCode == PICK_CONTACT2) && (resultCode == RESULT_OK)) {
            if (data != null) {
                Uri contactData = data.getData();

                try {

                    String id = contactData.getLastPathSegment();
                    String[] columns = {ContactsContract.CommonDataKinds.Phone.DATA, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                    Cursor phoneCur = getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    columns,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                            + " = ?", new String[]{id},
                                    null);

                    final ArrayList<String> phonesList = new ArrayList<String>();
                    String Name = null;
                    assert phoneCur != null;
                    if (phoneCur.moveToFirst()) {
                        do {
                            Name = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            String phone = phoneCur
                                    .getString(phoneCur
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                            phonesList.add(phone);

                        } while (phoneCur.moveToNext());

                    }


                    phoneCur.close();

                    if (phonesList.size() == 0) {
                        Toast.makeText(
                                this, "This contact does not contain any numbers...",
                                Toast.LENGTH_LONG).show();
                    } else if (phonesList.size() == 1) {
                        emergencyNo2.setText(phonesList.get(0));
                    } else {

                        final String[] phonesArr = new String[phonesList
                                .size()];
                        for (int i = 0; i < phonesList.size(); i++) {
                            phonesArr[i] = phonesList.get(i);
                        }

                        AlertDialog.Builder dialog = new AlertDialog.Builder(
                                SosActivity.this);
                        dialog.setTitle("Name : " + Name);
                        ((AlertDialog.Builder) dialog).setItems(phonesArr,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        String selectedPhoneNumber = phonesArr[which];
                                        emergencyNo2.setText(selectedPhoneNumber);
                                    }
                                }).create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    Log.e("FILES", "Failed to get phone data", e);
                }
            }

        }
    }



    public void loadDat()
    {




        phoneN1 = MyGlobalClass.read_pref(getApplicationContext(),"e1");
        phoneN2 = MyGlobalClass.read_pref(getApplicationContext(),"e2");

    }

    public void updateView()
    {
        emergencyNo1.setText(phoneN1);
        emergencyNo2.setText(phoneN2);

    }




    public void onStop() {

        super.onStop();
   


    }




}