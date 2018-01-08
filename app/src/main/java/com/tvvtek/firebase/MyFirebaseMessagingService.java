package com.tvvtek.firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tvvtek.connectpackage.ConnectLogic;
import com.tvvtek.keepstring.MainActivity;
import com.tvvtek.keepstring.R;
import com.tvvtek.keepstring.StaticSettings;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private volatile static String data_clipboard_first_start = "";
    private static final int NOTIFY_ID = 444;
    private int position_space;
    private static String notification_data_to_present = "";
    StaticSettings staticSettings;
    private static NotificationManager mNotificationManager;
    private volatile int trigger_send_receive_notification = 0;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        receiveClipFromServer();
    }
    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }


    private void receiveClipFromServer(){
        String data_from_server, data_from_server_md5, data_clipboard_md5, data_clipboard_first_start_md5, data_clipboard_first_start;
        SharedPreferences sPref = getSharedPreferences("TAG", MODE_PRIVATE);
        String user_key_fromdevice;
        user_key_fromdevice = loadKeyFromDevice();
        try{
            ConnectLogic connect = new ConnectLogic();
            connect.setScriptName("cloudapi.php");
            String[] request_read = {
                    "flag", "4",
                    "cookie", user_key_fromdevice,
                    "md5data", toMd5(clipRead()),
                    "androidkey", staticSettings.getAndroidKey(),};
            connect.setRequest(request_read);
            //  connect.work();
            connect.toWork();
            data_from_server = connect.getResponse();
            data_from_server_md5 = toMd5(data_from_server);
            while (connect.getStateRequest() == 2) {
            }
            Log.d(TAG, "SYNCH");
            if (!data_from_server_md5.equals(toMd5(clipRead())) & !data_from_server.equals("")) {
                if (data_from_server.equals("errrd")) {
             //       Log.d(staticSettings.getLogTag(), "error read from server=" + data_from_server);
                } else if (data_from_server.equals("==")) {
             //       Log.d(staticSettings.getLogTag(), "md5 sum is equal, there is nothing to writing");
                } else {
              //      trigger_send_receive_notification = 1;
                    Log.d(TAG, "data from server" + data_from_server);
                    clipWrite(data_from_server);
               //     Log.d(staticSettings.getLogTag(), "switch=" + sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
                    basedNotification(data_from_server);

                }
            }
        }
        catch (Exception errorreceive){
            Log.d(TAG, "SYNCH ERROR");
            errorreceive.printStackTrace();
        }


    }
    private String clipRead(){
        try{android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            //     Log.d(TAG, "read=" + item.getText().toString());
            return item.getText().toString();}
        catch (Exception errorclipread){
            return "clip_is_empty";}
    }
    // generate MD5 hash
    private String toMd5(String input) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            m.update(input.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte s[] = m.digest();
        String result = "";
        for (int i = 0; i < s.length; i++) {
            result += Integer.toHexString((0x000000ff & s[i]) | 0xffffff00).substring(6);
        }
        return result;
    }
    private void clipWrite(String textforwriteclip){
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("clip", textforwriteclip);
        clipboard.setPrimaryClip(clip);
        //     Log.d(TAG, "write=" + textforwriteclip);
    }
    private String loadKeyFromDevice() {
        SharedPreferences sPref = getApplicationContext().getSharedPreferences("TAG", MODE_PRIVATE);
        // Log.d(TAG, "prefer into service=" + sPref.getString("userkey",""));
        return sPref.getString("userkey","");
    }
    private void basedNotification(String clip_new){
        Context context = getApplicationContext();
        if (clip_new.length() > 36){notification_data_to_present = clip_new.substring(0, 36);}
        else {notification_data_to_present = clip_new;}
        if (clip_new.substring(0, 4).equals("http")){
            try {
                position_space = clip_new.indexOf(" ");
                Log.d(staticSettings.getLogTag(), "position_space=" + position_space);
            }
            catch (StringIndexOutOfBoundsException error_find_space){
                Log.d(staticSettings.getLogTag(),"error_find_space=" + error_find_space);
            }
            if (position_space != -1) clip_new = clip_new.substring(0, position_space);
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(clip_new));
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(context.NOTIFICATION_SERVICE);

            Notification.Builder mBuilder = new Notification.Builder(
                    this)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_name))
                    .setContentText(notification_data_to_present) // Текст уведомления
                    .setAutoCancel(true);
            mBuilder.setContentIntent(contentIntent);
            mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
        }
        else{
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            Notification.Builder builder = new Notification.Builder(this);
            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.notification_name))
                    .setContentText(notification_data_to_present); // Текст уведомления

            Notification notification = builder.build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFY_ID, notification);
        }
    }
}