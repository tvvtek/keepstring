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
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.tvvtek.connectpackage.ConnectLogic_old;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class ServiceInterCloud_old extends Service {
    StaticSettings staticSettings;
    HelperFragmentHistoryDBWorked db;

    public final String APP_PREFERENCES_SWITCH_SYNC = "switch_sycn";
    public final String APP_PREFERENCES_SWITCH_NOTIFICATION = "switch_notification";
    public final String APP_PREFERENCES_SWITCH_AUTOSTART = "switch_auto_start";
    private String data_from_server = "";
    private String data_from_server_md5 = "";
    private volatile boolean state_thread = true;
    private String user_key_fromdevice = "";
    private volatile static String data_clipboard_first_start = "";
    private volatile static String data_clipboard_first_start_md5 = "";
    private volatile static String data_clipboard_ondevice = "";
    private volatile static String data_clipboard_to_send = "";
    private volatile static String data_clipboard_md5 = "";
    public volatile static boolean trigger = false;
    private int position_space;
    private int count_thread = 0;
    // notification
    private static final int NOTIFY_ID = 100;
    private static String notification_data_to_present = "";

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
          //  Log.d(TAG, "Clip is empty");
        }

        try {
            user_key_fromdevice = intent.getStringExtra("userkey");
            saveCookie(user_key_fromdevice);
         //   Log.d(TAG, "получили с интента, так как впервые=" + user_key_fromdevice);
        }catch (Exception key_is_there_is){
            user_key_fromdevice = loadKeyFromDevice();
          //  Log.d(TAG, "загрузили с девайса, так как есть=" + user_key_fromdevice);
        }
       // Log.d(TAG, "param1=" + user_key_fromdevice);
        // setRequest to server inside new thread
        Thread thread_cloud = new Thread(new Runnable() {
            public void run() {
                ConnectLogic_old connect = new ConnectLogic_old(); // For server connect and send data
                while (state_thread) {
                //    Log.d(staticSettings.getLogTag(), "state_thread=true");
                //    Log.d(staticSettings.getLogTag(), "trigger=" + trigger);
                    try {
                        TimeUnit.SECONDS.sleep(staticSettings.getTimePerUpdateData());
                        connect.setScriptName("cloudapi.php");
                        // Если true, значит дернули метод буфера обмена и есть новые данные, шлем их на сервер
                        if (trigger){
                            String[] request_new = {
                                    "flag",  "2",
                                    "cookie",  user_key_fromdevice,
                                    "data",  data_clipboard_to_send,
                                    "androidkey",  staticSettings.getAndroidKey(),};
                            connect.setRequest(request_new);
                            connect.work();
                         //   while(connect.getStateRequest() == 2){}
                            data_from_server = connect.getResponse();
                            trigger = false;
                            // send body ---------------------------------------------------------------
                        //    Log.d(staticSettings.getLogTag(), "дернули метод буфера, шлем на сервак и получаем ответ=" + data_from_server);
// end send body -----------------------------------------------------------
                        } else {
                            // flag 3 - reseive clip data form server
                            String[] request_read = {
                                    "flag",  "4",
                                    "cookie",  user_key_fromdevice,
                                    "md5data",  toMd5(clipRead()),
                                    "androidkey",  staticSettings.getAndroidKey(),};
                            connect.setRequest(request_read);
                            connect.work();
                       //     Log.d(staticSettings.getLogTag(), "flag=4" + "|" + "md5clip=" + toMd5(clipRead()) + "|" + "clipread=" + clipRead());
                            trigger = false;
                            /*

                        /* connect.getStateRequest() param result
                            -1 timeout
                            0 error connect
                            1 successfully
                         */
                            while(connect.getStateRequest() == 2){}
                            data_from_server = null;
                            data_from_server = connect.getResponse(); // string from server
                            // тут решаем пришли другие данные или теже, чтобы не писать лишний раз в буфер обмена
                            data_from_server_md5 = toMd5(data_from_server);
                            data_clipboard_md5 = toMd5(data_clipboard_to_send);
                            data_clipboard_first_start_md5 = toMd5(data_clipboard_first_start); // после рестарта сервиса сравниваем то что сейчас в бефере с тем что пришло и решаем показывать ли уведомление
                           // Log.d(TAG, "STATE=" + Boolean.toString(data_from_server_md5.equals(data_clipboard_first_start_md5)));
                           // Log.d(TAG, "md5=" + data_from_server_md5 + "|" + data_clipboard_first_start_md5);

                            if (!data_from_server_md5.equals(data_clipboard_md5) & !data_from_server_md5.equals(data_clipboard_first_start_md5) & !data_from_server_md5.equals(toMd5(clipRead())) & !data_from_server.equals("")){
                                data_clipboard_ondevice = data_from_server;
                                if (data_from_server.equals("errrd")){Log.d(staticSettings.getLogTag(), "error read from server=" + data_from_server);}
                                else if (data_from_server.equals("==")){Log.d(staticSettings.getLogTag(), "md5 sum is equal, there is nothing to writing");}
                                else {
                                    clipWrite(data_from_server);
                                    SharedPreferences sPref = getSharedPreferences("TAG", MODE_PRIVATE);
                                    Log.d(staticSettings.getLogTag(), "switch=" + sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
                                    if (sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true)) {
                                        sendNotification(data_from_server);
                                        Log.d(staticSettings.getLogTag(), "NOTIFICATION=" + data_from_server);
                                    //    Log.d(staticSettings.getLogTag(), "notification=" + sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
                                    }
                                }
                            }
                           // Log.d(staticSettings.getLogTag(), "Response from server=" + data_from_server);
// end send body -----------------------------------------------------------
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
    //р дергается когда в буфер что-то копируют в т.ч. метод записи
    private void listenerClipCopy() {
        Context context = getApplicationContext();
        ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cb.hasPrimaryClip()) {
            ClipData cd = cb.getPrimaryClip();
            if (cd.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                try {
                    trigger = true;
                    data_clipboard_to_send = cd.getItemAt(0).getText().toString();
                    db = new HelperFragmentHistoryDBWorked(context);
                    db.writeDataBase(data_clipboard_to_send);
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
     /*   String result = "";
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(input.getBytes(), 0, input.length());
            result = new BigInteger(1, m.digest()).toString(16);
        }catch (NoSuchAlgorithmException z){
           // Log.d(TAG, z.toString());
            result = "error";
        }
        return result;*/
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
    private void sendNotification(String clip_new){
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