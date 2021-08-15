package com.vedantamadam.raksha;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class MyGlobalClass {
    public static String phoneNumber1 = "", phoneNumber2 = "", senstivityNumber = "",timestamp;
    public static long time_startLocUpdates, time_onLocationResult;
    public static boolean fall = true;
    public static boolean permission_approved = false;


    public static void save_pref(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences("com.vedantamadam.raksha.PREFERENCE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if (value != null && !value.isEmpty()) //write only if value is not null and not empty
        editor.putString(key, value);
        editor.apply();
    }

    public static String read_pref(Context context, String key) {
        String value;
        SharedPreferences pref = context.getSharedPreferences("com.vedantamadam.raksha.PREFERENCE", Context.MODE_PRIVATE);
        if (pref.contains(key)) {
            value = pref.getString(key,"");
        } else {
            value = null;
        }
        return value;
    }

    public static void delete_pref(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences("com.vedantamadam.raksha.PREFERENCE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if (pref.contains(key)) {
            editor.remove(key);
        }
        editor.apply();
    }

    public static boolean if_sosnumber_exist(Context context) {
        String sos_ph1, sos_ph2 = null;
        sos_ph1 = read_pref(context, "e1");
        sos_ph2 = read_pref(context, "e2");
        return (sos_ph1 != null && !sos_ph1.isEmpty()) || (sos_ph2 != null && !sos_ph2.isEmpty());

    }

    public static String time_forLocFetch(){
        try{
            long time_duration = time_onLocationResult - time_startLocUpdates ;
            time_startLocUpdates = 0;
            time_onLocationResult = 0;
            return String.valueOf(time_duration);
        } catch (Exception ex){

            return null;
        }
    }

    public void sendSMS(Context context, String msg) {
        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> emerMsg;
            emerMsg = sms.divideMessage(msg);
            if (if_sosnumber_exist(context)) {
                String[] ph = {read_pref(context, "e1"), read_pref(context, "e2")};
                for (int i = 0; i < ph.length && ph[i] != null; i++) {
                    sms.sendMultipartTextMessage(ph[i], null, emerMsg, null, null);
                    Toast.makeText(context, "Successfully send SOS to "+ph[i].toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Please set SOS contact numbers.", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception ex){
            Toast.makeText(context, "Failed to send SOS", Toast.LENGTH_SHORT).show();
        }
    }


    public static boolean checkPermission(String mPermission, Context context, String deniedMessage)
    {
        new TedPermission()
                .with(context)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        permission_approved = true;
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        permission_approved = false;
                    }
                })
                .setDeniedMessage( deniedMessage)
                .setDeniedCloseButtonText(android.R.string.ok)
                .setGotoSettingButton(true)
                .setPermissions(Manifest.permission.READ_CONTACTS)
                .check();

        return permission_approved;
    }

}
