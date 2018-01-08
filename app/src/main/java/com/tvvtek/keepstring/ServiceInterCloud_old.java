package com.tvvtek.keepstring;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.tvvtek.connectpackage.ConnectLogic;
import com.tvvtek.helpers.HelperFragmentHistoryDBWorked;
import com.tvvtek.ui.FragmentEnter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class ServiceInterCloud_old extends Service {
    StaticSettings staticSettings;
    HelperFragmentHistoryDBWorked db;

    public final String APP_PREFERENCES_SWITCH_SYNC = "switch_sycn";
    public final String APP_PREFERENCES_SWITCH_NOTIFICATION = "switch_notification";
    public final String APP_PREFERENCES_SWITCH_SLEEP_SYNC = "switch_sleep_sync";
    public final String APP_PREFERENCES_DIALOG_BEFORE_SYNC = "switch_dialog_before_sync";
    private String data_from_server = "";
    private String data_from_server_md5 = "";
    private volatile boolean state_thread = true;
    private String user_key_fromdevice = "";
    private volatile static String data_clipboard_first_start = "";
    private volatile static String data_clipboard_first_start_md5 = "";
    private volatile static String data_clipboard_ondevice = "";
    private volatile static String data_clipboard_now = "";
    private volatile static String data_clipboard_md5 = "";
    private volatile int trigger_send_receive_notification = 0;
    public volatile static boolean trigger = false;
    private volatile boolean state_screen_on = false;
    private int position_space;
    private int count_thread = 0;
    // notification
    private static final int NOTIFY_ID = 100;
    private static String notification_data_to_present = "";
    private static NotificationManager mNotificationManager;

    private ClipboardManager.OnPrimaryClipChangedListener listener = new ClipboardManager.OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            listenerClipCopy();
        }

    };
    public ServiceInterCloud_old() {
    }
    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).addPrimaryClipChangedListener(listener);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, final int startId) {
        /**
         * count_thread it`s counter thread, this variable does not run clones flows, preventing overload of memory.
         */
        count_thread++;
        Log.d(staticSettings.getLogTag(), "count_thread=" + count_thread);
        try {
            data_clipboard_first_start = clipRead(); // read clipboard on device at first start
        }
        catch (Exception clipnothing){
        }

        try {
            user_key_fromdevice = intent.getStringExtra("userkey");
            saveCookie(user_key_fromdevice);
         //   Log.d(TAG, "получили с интента, так как впервые=" + user_key_fromdevice);
        }catch (Exception key_is_there_is){
            user_key_fromdevice = loadKeyFromDevice();
        }
        // setRequest to server inside new thread
        Thread thread_cloud = new Thread(new Runnable() {
            public void run() {
             //   ConnectLogic connect = new ConnectLogic(); // For server connect and send data
                PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                ConnectLogic connect = new ConnectLogic();
                while (state_thread) {
                    SharedPreferences sPref = getSharedPreferences("TAG", MODE_PRIVATE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        state_screen_on = pm.isInteractive();
                    } else {
                        state_screen_on = pm.isScreenOn();
                    }
                    try {
                        /**
                         * Check on or off screen before send request
                         */
                        if (sPref.getBoolean(APP_PREFERENCES_SWITCH_SLEEP_SYNC, false)) state_screen_on = true;
                        if(state_screen_on) {
                            TimeUnit.SECONDS.sleep(staticSettings.getTimePerUpdateData());
                            connect.setScriptName("cloudapi.php");
                            // Если true, значит дернули метод буфера обмена и есть новые данные, шлем их на сервер
                            if (trigger) {
                                String[] request_new = {
                                        "flag", "2",
                                        "cookie", user_key_fromdevice,
                                        "data", data_clipboard_now,
                                        "androidkey", staticSettings.getAndroidKey(),};
                                connect.setRequest(request_new);
                                //   connect.work();
                                //   while(connect.getStateRequest() == 2){}
                                connect.toWork();
                                data_from_server = connect.getResponse();

                                trigger = false;
                                // send body ---------------------------------------------------------------
                                //    Log.d(staticSettings.getLogTag(), "дернули метод буфера, шлем на сервак и получаем ответ=" + data_from_server);
// end send body -----------------------------------------------------------
                            } else {
                                // flag 3 - reseive clip data form server
                                String[] request_read = {
                                        "flag", "4",
                                        "cookie", user_key_fromdevice,
                                        "md5data", toMd5(clipRead()),
                                        "androidkey", staticSettings.getAndroidKey(),};
                                connect.setRequest(request_read);
                                //  connect.work();
                                connect.toWork();
                                //     Log.d(staticSettings.getLogTag(), "flag=4" + "|" + "md5clip=" + toMd5(clipRead()) + "|" + "clipread=" + clipRead());
                            /*

                        /* connect.getStateRequest() param result
                            -1 timeout
                            0 error connect
                            1 successfully
                         */
                                while (connect.getStateRequest() == 2) {
                                }
                                data_from_server = null;
                                data_from_server = connect.getResponse(); // string from server
                                // тут решаем пришли другие данные или теже, чтобы не писать лишний раз в буфер обмена
                                data_from_server_md5 = toMd5(data_from_server);
                                data_clipboard_md5 = toMd5(data_clipboard_now);
                                data_clipboard_first_start_md5 = toMd5(data_clipboard_first_start); // после рестарта сервиса сравниваем то что сейчас в бефере с тем что пришло и решаем показывать ли уведомление
                                // Log.d(TAG, "STATE=" + Boolean.toString(data_from_server_md5.equals(data_clipboard_first_start_md5)));
                                // Log.d(TAG, "md5=" + data_from_server_md5 + "|" + data_clipboard_first_start_md5);

                                if (!data_from_server_md5.equals(data_clipboard_md5) & !data_from_server_md5.equals(data_clipboard_first_start_md5) & !data_from_server_md5.equals(toMd5(clipRead())) & !data_from_server.equals("")) {
                                    data_clipboard_ondevice = data_from_server;
                                    if (data_from_server.equals("errrd")) {
                                        Log.d(staticSettings.getLogTag(), "error read from server=" + data_from_server);
                                    } else if (data_from_server.equals("==")) {
                                        Log.d(staticSettings.getLogTag(), "md5 sum is equal, there is nothing to writing");
                                    } else {
                                        trigger_send_receive_notification = 1;
                                        clipWrite(data_from_server);
                                        Log.d(staticSettings.getLogTag(), "switch=" + sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
                                        if (sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true)) {
                                            basedNotification(data_from_server);
                                            Log.d(staticSettings.getLogTag(), "NOTIFICATION=" + data_from_server);
                                            //    Log.d(staticSettings.getLogTag(), "notification=" + sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
                                        }
                                    }
                                }
                                // Log.d(staticSettings.getLogTag(), "Response from server=" + data_from_server);
// end send body -----------------------------------------------------------
                            }
                        }
                        else {
                            TimeUnit.SECONDS.sleep(staticSettings.getTimePerUpdateData());
                            Log.d(staticSettings.getLogTag(), "Screen Off, no sync");
                        }
                    } catch (Exception e) {
                     //   Log.d(staticSettings.getLogTag(), "error_connection_service=" + e);
                    }
                }
            }
        });
        SharedPreferences sPref = getSharedPreferences("TAG", MODE_PRIVATE);
        if (count_thread == 1 & sPref.getBoolean(APP_PREFERENCES_SWITCH_SYNC, true))  thread_cloud.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        state_thread = false;
        Log.d(staticSettings.getLogTag(), "serviceEND");
        sendCloseSessionMessage();
        state_thread = false;
        super.onDestroy();
    }

    private void sendCloseSessionMessage() {
        Intent intent = new Intent(FragmentEnter.BROADCAST_ACTION);
        intent.putExtra("sessionClosed", true);
        sendBroadcast(intent);
        state_thread = false;
        Log.d(staticSettings.getLogTag(), "serviceEND");
    }
    //----------------------- WORKING CLIPBOARD ---------------------------------------------------
    //этот метод дергается когда в буфер что-то копируют в т.ч. метод записи
    private void listenerClipCopy() {
        SharedPreferences sPref = getSharedPreferences("TAG", MODE_PRIVATE);
        Context context = getApplicationContext();
   ///     ConnectLogic connect_for_send = new ConnectLogic();
   //     connect_for_send.setScriptName("cloudapi.php");
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cb.hasPrimaryClip()) {
            ClipData cd = cb.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                try {
                    /**
                     * Тут мы смотрим включена ли опция запрос перед синхронизацией данных, если да, то просто показываем диалог
                     * решение о синхронизации запрашивается в пуш уведомлении
                     */
                    data_clipboard_now = cd.getItemAt(0).getText().toString();
                    if (sPref.getBoolean(APP_PREFERENCES_DIALOG_BEFORE_SYNC, false)){
                        // тут будет логика работы отказа от синхронизации или синхронизации
                        notificationBeforeSync(data_clipboard_now);// шлем данные скопированные
                        // в буфер обмена
                    //    noto2();
                    }
                    else Log.d(staticSettings.getLogTag(), "dialog disable"); {
                        trigger = true;
                        db = new HelperFragmentHistoryDBWorked(context);
                        db.writeDataBase(data_clipboard_now);
                    }
                //    Log.d(TAG, "что то записали в буфер");
                    //Log.d(TAG, "send_new_local_clip=" + cd.getItemAt(0).getText().toString());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void clipWrite(String textforwriteclip){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clip", textforwriteclip);
        clipboard.setPrimaryClip(clip);
   //     Log.d(TAG, "write=" + textforwriteclip);
    }
    private String clipRead(){
        try{ClipboardManager clipboard = (ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
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
    private String loadKeyFromDevice() {
        SharedPreferences sPref = getApplicationContext().getSharedPreferences("TAG", MODE_PRIVATE);
       // Log.d(TAG, "prefer into service=" + sPref.getString("userkey",""));
        return sPref.getString("userkey","");
    }
    // other methods
    private void saveCookie(String userKeyIn) {
        String userkey = "userkey";
        SharedPreferences sPref = this.getSharedPreferences("TAG", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(userkey, userKeyIn);
        ed.commit();
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
    public void notificationBeforeSync(String my_clip_now) {
        String my_content = "";
        int id = (int) System.currentTimeMillis();
        // crop string before display
        if (my_clip_now.length() > 150){my_content = my_clip_now.substring(0, 150);}
        else {my_content = my_clip_now;}
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.notification_before_sync);

        Notification noti = new Notification();
        noti = setBigTextStyleNotification();
        noti.defaults |= Notification.DEFAULT_LIGHTS;
        noti.defaults |= Notification.DEFAULT_VIBRATE;
        noti.defaults |= Notification.DEFAULT_SOUND;
        noti.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
        mNotificationManager.notify(0, noti);
    }
    public void noto2() // paste in activity
    {
        Notification.Builder notif;
        NotificationManager nm;
        notif = new Notification.Builder(getApplicationContext());
        notif.setSmallIcon(R.drawable.ic_notification);
        notif.setContentTitle("");
        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notif.setSound(path);
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent yesReceive = new Intent();
        yesReceive.setAction(StaticSettings.YES_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.ic_notification, "Yes", pendingIntentYes);


        Intent yesReceive2 = new Intent();
        yesReceive2.setAction(StaticSettings.STOP_ACTION);
        PendingIntent pendingIntentYes2 = PendingIntent.getBroadcast(this, 12345, yesReceive2, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.ic_notification, "No", pendingIntentYes2);
        nm.notify(10, notif.getNotification());
    }


    private Notification setBigTextStyleNotification() {
        Bitmap remote_picture = null;

        // Create the style object with BigTextStyle subclass.
        NotificationCompat.BigTextStyle notiStyle = new NotificationCompat.BigTextStyle();
        notiStyle.setBigContentTitle("Big Text Expanded");
        notiStyle.setSummaryText("Nice big text.");

        try {
            remote_picture = BitmapFactory.decodeStream((InputStream) new URL("").getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add the big text to the style.
        CharSequence bigText = "This is an example of a large string to demo how much " +
                "text you can show in a 'Big Text Style' notification.";
        notiStyle.bigText(bigText);

        // Creates an explicit intent for an ResultActivity to receive.
        Intent resultIntent = new Intent(this, MyNotificationBeforeSync.class);

        // This ensures that the back button follows the recommended convention for the back key.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself).
        stackBuilder.addParentStack(MyNotificationBeforeSync.class);

        // Adds the Intent that starts the Activity to the top of the stack.
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setLargeIcon(remote_picture)
                .setContentIntent(resultPendingIntent)
                .addAction(R.drawable.ic_search, "One", resultPendingIntent)
                .addAction(R.drawable.ic_search, "Two", resultPendingIntent)
                .addAction(R.drawable.ic_search, "Three", resultPendingIntent)
                .setContentTitle("Big Text Normal")
                .setContentText("This is an example of a Big Text Style.")
                .setStyle(notiStyle).build();
    }


 /*   public class NotificationBeforeSyncSkip extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(staticSettings.getLogTag(),"Received Skip Event");
        }
    } */
}