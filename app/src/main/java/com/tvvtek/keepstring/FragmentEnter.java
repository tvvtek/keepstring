package com.tvvtek.keepstring;

import android.app.Activity;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationManagerCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tvvtek.connectpackage.ConnectLogic_old;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

public class FragmentEnter extends Fragment {
    SharedPreferences sPref;
    HelperFragmentHistoryDBWorked db;
    public final String APP_PREFERENCES_SWITCH_SYNC = "switch_sycn";
    public final String APP_PREFERENCES_SWITCH_NOTIFICATION = "switch_notification";
    public final String APP_PREFERENCES_SWITCH_AUTOSTART = "switch_auto_start";
    public final String APP_PREFERENCES_SWITCH_SLEEP_SYNC = "switch_sleep_sync";
    public final String APP_PREFERENCES_DIALOG_BEFORE_SYNC = "switch_dialog_before_sync";
    public static final String BROADCAST_ACTION = "action_session_closed";
    private BroadcastReceiver broadcastReceiver;


    ProgressBar progressBarEnter;
    EditText enterLogin, enterPass;
    TextView enter_help_text, info_login, info_clipboardnow, clipboardnow, register_text, forgotpass;
    Button btnEnter, btnExit, btnRe_read, btnCleanHistory;
    Switch switchAutoUpdate, switchNotification, switchAutoStart, switchSleepSync, switchDialogBeforeSync;
    Handler handler_enter, handler_auto_reread_clip;
    StaticSettings staticSettings;
    private volatile String result = "";
    private static final int NOTIFY_ID = 100;
    private boolean state_thread_reread_clip = true;

    private static String login = "null"; // for save login name
    private static String userkey = "userkey";
    private static String userlogin = "userlogin";
    private int position_space;
    private boolean state_switch_pass = false;
    private boolean state_notification, state_sync, state_auto_update;
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
       // Log.d(TAG, "PREFER=" + loadCookie());
        if (loadCookie().length() == 128){
            return createViewAuthOk(inflater, container, savedInstanceState);
          //  Log.d(TAG, "coki" + loadCookie());
        }
        else {
            return createViewEnter(inflater, container, savedInstanceState);
        }
    }

    private View createViewEnter(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View myView = inflater.inflate(R.layout.fragment_enter, container, false);
        setRetainInstance(true);
        btnExit = (Button)myView.findViewById(R.id.btnExit);
   //     switchAutoUpdate = (Switch) myView.findViewById(R.id.switchAutoUpdateData);
        enter_help_text = (TextView)myView.findViewById(R.id.enter_help_text);
        progressBarEnter = (ProgressBar)myView.findViewById(R.id.progressBarEnter);
        enterLogin = (EditText)myView.findViewById(R.id.settings_enter_login);
        enterPass = (EditText)myView.findViewById(R.id.settings_enter_pass);
        btnEnter = (Button)myView.findViewById(R.id.btn_enter);
        register_text = (TextView)myView.findViewById(R.id.register_text);
        forgotpass = (TextView)myView.findViewById(R.id.forgot_pass);

        progressBarEnter.setVisibility(View.INVISIBLE);
        enterPass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(event.getRawX() >= (enterPass.getRight() - enterPass.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (!state_switch_pass) {
                            enterPass.setInputType(InputType.TYPE_CLASS_TEXT);
                            state_switch_pass = true;
                            enterPass.setNextFocusUpId(enterPass.getNextFocusUpId());
                        }
                        else {
                            enterPass.setInputType(129);
                            state_switch_pass = false;
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        register_text.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            Fragment fragment;
            Class fragmentClass;
            FragmentManager fragManager = myContext.getSupportFragmentManager();
            fragmentClass = FragmentRegister.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                fragManager.beginTransaction().replace(R.id.container, fragment).commit();
            }catch (Exception h){
            }
        }});
        forgotpass.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            Uri address = Uri.parse("https://keepstring.com/forgothtml.php");
            Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openlinkIntent);
        }});

        // Refresh IU
        handler_enter = new Handler()
        {
            public void handleMessage(Message msg) throws NullPointerException
            {
                // update TextView
                Bundle bundle = msg.getData();
                String data_from_thread_io_enter;
                data_from_thread_io_enter = bundle.getString("1");
                // for enter
                if (data_from_thread_io_enter.equals("0")) // error enter, login or pass not valid
                {
                    btnEnter.setEnabled(true);
                    progressBarEnter.setVisibility(View.INVISIBLE);
                    try {
                        Toast toast = Toast.makeText(getContext(), R.string.error_enter, Toast.LENGTH_SHORT);
                        toast.show();
                    }catch (NullPointerException error){
                        error.printStackTrace();
                    }
                }
                else if (data_from_thread_io_enter.length() == 128) // enter is OK, added cookie into file staticSettings and hide button and input
                {
                    writeCookieOnFile(data_from_thread_io_enter);
                    saveCookie(data_from_thread_io_enter, login); // write userkey into preferences
                    restartFirstActivity();
                    progressBarEnter.setVisibility(View.INVISIBLE);
                }
                else if (data_from_thread_io_enter.equals("error_connect"))
                {
                    btnEnter.setEnabled(true);
                    progressBarEnter.setVisibility(View.INVISIBLE);
                    try{
                    Toast toast = Toast.makeText(getContext(), R.string.error_connect, Toast.LENGTH_LONG);
                    toast.show();
                    }catch (NullPointerException error){
                        error.printStackTrace();
                    }
                }
                else
                {
                    btnEnter.setEnabled(true);
                    progressBarEnter.setVisibility(View.INVISIBLE);
                    try{
                    Toast toast = Toast.makeText(getContext(), R.string.error_invalid_responce, Toast.LENGTH_LONG);
                    toast.show();
                    }catch (NullPointerException error){
                        error.printStackTrace();
                    }
                }
            };
        };

// -------------------------------------------------------------------------------------------------
        btnEnter.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            if (    enterLogin.length() > staticSettings.getMinLoginPass()
                    && enterLogin.length() < staticSettings.getMaxLoginPass())
            {
                login = enterLogin.getText().toString();
                btnEnter.setEnabled(false);
                // Thread per operations
                progressBarEnter.setVisibility(View.VISIBLE);
                Thread thread_enter = new Thread(new Runnable() {

                    public void run() {
                        Message message = handler_enter.obtainMessage();
                        Bundle bundle = new Bundle();
                        ConnectLogic_old connect = new ConnectLogic_old(); // For server connect and send data
                        connect.setScriptName("cloudapi.php");
                        //   connect.setScriptName("print.php"); // this variant for testing setRequest POST data
                        String[] request_register = {"flag", "0",
                                "login", login,
                                "password", enterPass.getText().toString(),
                                "androidkey", staticSettings.getAndroidKey()};
                        connect.setRequest(request_register);
                        connect.work();
                         /* connect.getStateRequest() param result
                            -1 timeout
                            0 error connect
                            1 successfully
                         */

                        while (connect.getStateRequest() == 2) {}

                        if (connect.getStateRequest() == 1) {
                            try {
                                result = connect.getResponse();
                                Log.d(staticSettings.getLogTag(), "POST=" + result);
                                //  result_trigger = 1;
                            } catch (Exception e) {
                                //   result_trigger = 0;
                                Log.d(staticSettings.getLogTag(), "ErrorGetDataFromConnectObj= " + e);
                            }
                        }
                        else if (connect.getStateRequest() == 0) {
                            result = "error_connect";
                        }
                        bundle.putString("1", result); // var result for cookie
                      //  Log.d(TAG, "KEY_" + result);
                        message.setData(bundle);
                        handler_enter.sendMessage(message);
                        //   Log.d(TAG, "thread end2=" + result);
                    }
                });
                thread_enter.start();
            }
            else
            {
                Toast toast = Toast.makeText(getContext(), R.string.error_login_pass, Toast.LENGTH_SHORT);
                toast.show();
            }
        }});
// -------------------------------------------------------------------------------------------------
        return myView;
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
    //    switchDialogBeforeSync = (Switch)myViewAuthOk.findViewById(R.id.switchDialogbeforeSync);
        Context context = getContext();
        // получаем данные для состояния переключателей
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences sPref = context.getSharedPreferences("TAG", MODE_PRIVATE);
        switchAutoUpdate.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_SYNC, true));
        switchNotification.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
        switchAutoStart.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_AUTOSTART, true));
        switchSleepSync.setChecked(sPref.getBoolean(APP_PREFERENCES_SWITCH_SLEEP_SYNC,false));
     /*   switchDialogBeforeSync.setChecked(sPref.getBoolean(APP_PREFERENCES_DIALOG_BEFORE_SYNC,
                false)); */

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
    /*    switchDialogBeforeSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // listener switch auto update clipboard
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sPref.edit();
                if (isChecked) {
                    editor.putBoolean(APP_PREFERENCES_DIALOG_BEFORE_SYNC, true);
                    editor.apply();
                } else {
                    editor.putBoolean(APP_PREFERENCES_DIALOG_BEFORE_SYNC, false);
                    editor.apply();
                }
            }
        }); */
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