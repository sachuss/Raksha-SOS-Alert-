package com.vedantamadam.raksha;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.


       // serviceIntent.putExtra("inputExtra", "");
       // Log.i("Broadcast Listened", "Service tried to stop");
      //  Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
       // Intent serviceIntent = new Intent(context.getApplicationContext(), BG.class);
       // serviceIntent.putExtra("inputExtra", "");

      //  Intent receiverIntent = new Intent(context,MainActivity.class);
       // receiverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       // context.startActivity(receiverIntent);
        context.sendBroadcast(new Intent("FALL DETECTED"));






      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         //   context.startForegroundService(new Intent(context, BG.class));
            ContextCompat.startForegroundService(context, serviceIntent);
        } else {
            context.startService(new Intent(context, BG.class));
        }*/

//        throw new UnsupportedOperationException("Not yet implemented");
    }
}
