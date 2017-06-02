package com.tvvtek.keepstring;

import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

public class FragmentMain extends Fragment {
    SharedPreferences sPref;
    HelperFragmentHistoryDBWorked db;
    public final String APP_PREFERENCES_SWITCH_SYNC = "switch_sycn";
    public final String APP_PREFERENCES_SWITCH_NOTIFICATION = "switch_notification";
    public final String APP_PREFERENCES_SWITCH_AUTOSTART = "switch_auto_start";
    public final String APP_PREFERENCES_SWITCH_SLEEP_SYNC = "switch_sleep_sync";
    public static final String BROADCAST_ACTION = "action_session_closed";
    private BroadcastReceiver broadcastReceiver;


    TextView enter_help_text, info_login, info_clipboardnow, clipboardnow, register_text, forgotpass;
    Button btnEnter, btnExit, btnRe_read, btnCleanHistory;
    Switch switchAutoUpdate, switchNotification, switchAutoStart, switchSleepSync;
    Handler handler_auto_reread_clip;
    StaticSettings staticSettings;
    private static final int NOTIFY_ID = 100;
    private boolean state_thread_reread_clip = true;

    private static String userkey = "userkey";
    private static String userlogin = "userlogin";
    private int position_space;
    private FragmentActivity myContext;

    @Override
    public void onDestroy() {
        this.state_thread_reread_clip = false;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        this.state_thread_reread_clip = false;
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;
        myContext=(FragmentActivity) activity;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createViewAuthOk(inflater, container, savedInstanceState);
    }
    private View createViewAuthOk(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View myViewAuthOk = inflater.inflate(R.layout.fragment_settings_auth, container, false);
        btnExit = (Button)myViewAuthOk.findViewById(R.id.btnExit);
        btnRe_read = (Button)myViewAuthOk.findViewById(R.id.btnRe_readclipboard);
        btnCleanHistory = (Button)myViewAuthOk.findViewById(R.id.btnCleanDb);
        info_login = (TextView)myViewAuthOk.findViewById(R.id.info_login);
        info_clipboardnow = (TextView)myViewAuthOk.findViewById(R.id.info_clipboardnow);
        info_clipboardnow.setText(R.string.clipboardnow);
        clipboardnow = (TextView)myViewAuthOk.findViewById(R.id.clipboardnow);
        switchAutoUpdate = (Switch)myViewAuthOk.findViewById(R.id.switchAutoUpdateData);
        switchNotification = (Switch)myViewAuthOk.findViewById(R.id.switchNotification);
        switchAutoStart = (Switch)myViewAuthOk.findViewById(R.id.switchAutoStart);
        switchSleepSync = (Switch)myViewAuthOk.findViewById(R.id.switchSleepModeSync);
        Context context = getContext();
        // получаем данные для состояния переключателей
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences sPref = context.getSharedPreferences("TAG", MODE_PRIVATE);
        switchAutoUpdate.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_SYNC, true));
        switchNotification.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
        switchAutoStart.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_AUTOSTART, true));
        switchSleepSync.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_SLEEP_SYNC,false));

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_name));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFY_ID);
        // Start service into data
        context.startService(new Intent(context, ServiceInterCloud.class)
                .putExtra("mode_state_io", true)
                .putExtra("userkey", loadCookie()));
        /**
         * handler for auto re-read clipboard, receive command from
         * @thread thread_auto_reread_clip;
         */
        handler_auto_reread_clip = new Handler()
        {
            public void handleMessage(Message msg) throws NullPointerException {
                // update TextView
                Bundle bundle = msg.getData();
                String data_from_thread_auto_reread_clip;
                data_from_thread_auto_reread_clip = bundle.getString("1");
                if (data_from_thread_auto_reread_clip.equals("go")) reReadClipboard();
            }
        };

        btnExit.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            saveCookie("", "");
            writeCookieOnFile("0");
            getContext().stopService(new Intent(getContext(), ServiceInterCloud.class));
            restartFirstActivity();
        }});
        btnRe_read.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            reReadClipboard();
        }});
        btnCleanHistory.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            Context context = getContext();
            db = new HelperFragmentHistoryDBWorked(context);
            db.dropAllTable();
        }});
        clipboardnow.setOnClickListener(new View.OnClickListener() {public void onClick(View myView) {
            if (clipboardnow.getText().toString().substring(0, 4).equals("http")) {
                // Пытаемся получить пробел.
                try {
                    position_space = clipboardnow.getText().toString().indexOf(" ");
                }
                catch (StringIndexOutOfBoundsException error_find_space){error_find_space.printStackTrace();
                }
                if (position_space != -1) {
                    startBrowser(clipboardnow.getText().toString().substring(0, position_space));
                    clipboardnow.setTextColor(getResources().getColor(R.color.colorTextLink));
                }
                else {
                    startBrowser(clipboardnow.getText().toString());
                    clipboardnow.setTextColor(getResources().getColor(R.color.colorTextLink));
                }
            }
        }});
        //------------------------------
        switchAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // listener switch auto update clipboard
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //     sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sPref.edit();
                if (isChecked) {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_SYNC, true);
                    editor.apply();
                    // Make PendingIntent for Task1
                    getContext().stopService(new Intent(getContext(), ServiceInterCloud.class)); // стопарим и потом запускаем сервис
                    getContext().startService(new Intent(getContext(), ServiceInterCloud.class)
                            .putExtra("mode_state_io", true)
                            .putExtra("userkey", loadCookie())
                            .putExtra("data in clipboard", ""));
                } else {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_SYNC, false);
                    editor.apply();
                    getContext().stopService(new Intent(getContext(), ServiceInterCloud.class));
                }
            }
        });
        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // listener switch auto update clipboard
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //   sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sPref.edit();
                if (isChecked) {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true);
                    editor.apply();
                } else {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, false);
                    editor.apply();
                }
            }
        });
        switchAutoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // listener switch auto update clipboard
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //    sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sPref.edit();
                if (isChecked) {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_AUTOSTART, true);
                    editor.apply();
                } else {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_AUTOSTART, false);
                    editor.apply();
                }
            }
        });
        switchSleepSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // listener switch auto update clipboard
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //    sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sPref.edit();
                if (isChecked) {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_SLEEP_SYNC, true);
                    editor.apply();
                } else {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_SLEEP_SYNC, false);
                    editor.apply();
                }
            }
        });
        info_login.setText(loadLoginWork());
        broadcastReceiver = new BroadcastReceiver() {
            // тут принимаем данные из сервиса
            public void onReceive(Context context, Intent intent) {
                boolean sessionClosed = intent.getBooleanExtra("sessionClosed", false);
            }
        };
        IntentFilter intFilter = new IntentFilter(BROADCAST_ACTION);
        context.registerReceiver(broadcastReceiver, intFilter);
        /**
         *  Thread for auto re-read clip
         */
        Thread thread_auto_reread_clip = new Thread(new Runnable() {
            public void run() {
                while(state_thread_reread_clip){
                    try{
                        Thread.sleep(staticSettings.getTimeReReadClipboard());
                        Message message = handler_auto_reread_clip.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("1", "go"); // var result for cookie
                        message.setData(bundle);
                        handler_auto_reread_clip.sendMessage(message);
                    }
                    catch (InterruptedException error_sleep){
                        error_sleep.printStackTrace();
                    }
                }
            }
        });
        thread_auto_reread_clip.start();
        return myViewAuthOk;
    }
    /**
     * Other methods
     */
    private void reReadClipboard() {
        try {
            ClipboardManager cliboardManager = (ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
            ClipData clipnow = cliboardManager.getPrimaryClip();
            if (clipnow.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                try {
                    clipboardnow.setText(clipnow.getItemAt(0).getText().toString());
                    clipboardnow.setFocusable(false);
                    clipboardnow.setTextColor(getResources().getColor(R.color.colorInfoText));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception error_read_clip){
            error_read_clip.printStackTrace();
        }
    }
    private void saveCookie(String userKeyIn, String userLoginWork) {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(userkey, userKeyIn);
        ed.putString(userlogin, userLoginWork);
        ed.commit();
    }
    void writeCookieOnFile(String key) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(getActivity().openFileOutput("key", MODE_PRIVATE)));
            bw.write(key);
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String loadCookie() {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(userkey, "");
        return savedText;
    }
    private String loadLoginWork() {
        sPref = getActivity().getPreferences(MODE_PRIVATE);
        String savedLogin = sPref.getString(userlogin, "");
        return savedLogin;
    }
    private void restartFirstActivity()
    {
        getActivity().finish();
        Intent i = getContext().getPackageManager()
                .getLaunchIntentForPackage(getContext().getPackageName() );
        i.setClass(getContext(), MainActivity.class);
        startActivity(i);
    }
    private void startBrowser(String link){
        Intent startBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(startBrowserIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
