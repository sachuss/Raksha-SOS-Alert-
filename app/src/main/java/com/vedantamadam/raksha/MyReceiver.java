package com.vedantamadam.raksha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.


       // serviceIntent.putExtra("inputExtra", "");
        Log.i("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
        Intent serviceIntent = new Intent(context.getApplicationContext(), BG.class);
        serviceIntent.putExtra("inputExtra", "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         //   context.startForegroundService(new Intent(context, BG.class));
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(new Intent(context, BG.class));
        }

//        throw new UnsupportedOperationException("Not yet implemented");
    }
}
