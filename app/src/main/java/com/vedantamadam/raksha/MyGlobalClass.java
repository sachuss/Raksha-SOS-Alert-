package com.vedantamadam.raksha;

import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MyGlobalClass extends AppCompatActivity {
    public static String phoneNumber1 = "", phoneNumber2 = "", senstivityNumber = "";

    public void sendSMS(String[] ph, String msg) {
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> emerMsg;
        emerMsg = sms.divideMessage(msg);
        for (int i = 0; i < ph.length; i++) {
            if (ph[i].length() == 13) {
                sms.sendMultipartTextMessage(ph[i], null, emerMsg, null, null);

            }
        }

    }


}
