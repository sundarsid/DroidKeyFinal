package com.example.sundar.droidkey;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by sundar on 11/2/16.
 */
public class MyService extends Service {

    BroadcastReceiver mReceiver;
    Bundle bundle;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    Intent resultIntent;
    TaskStackBuilder stackBuilder;


    IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("New Key Received")
                        .setContentText("Click to ADD");

        resultIntent = new Intent(this, OnSmsReceive.class);



        stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(OnSmsReceive.class);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Let it continue running until it is stopped.
        mReceiver = new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {
                bundle = intent.getExtras();
                resultIntent.putExtra("mySms",bundle);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                Log.e("Hi", "smsReceiver: SMS Received");



                // bundle = intent.getExtras();
                if (bundle != null) {
                    Log.e("Hi", "smsReceiver : Reading Message");



                    Object[] pdus = (Object[])bundle.get("pdus");
                    SmsMessage sms = SmsMessage.createFromPdu((byte[])pdus[0]);

                    if(sms.getMessageBody().contains("DROIDKEY")){

                        mNotificationManager.notify(1, mBuilder.build());
                        abortBroadcast();


                    }
                }
                Log.e("Hi", "Message Received");

            }

        };


        registerReceiver(mReceiver,filter);
       Log.e("Hi","Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
