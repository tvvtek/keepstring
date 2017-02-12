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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tvvtek.connectpackage.ConnectLogic;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

public class FragmentEnter extends Fragment {
    SharedPreferences sPref;
    HelperFragmentHistoryDBWorked db;
    public static final String APP_PREFERENCES_SWITCH_NOTIFICATION = "switch_notification";
    public static final String BROADCAST_ACTION = "action_session_closed";
    private BroadcastReceiver broadcastReceiver;

    ProgressBar progressBarEnter;
    EditText enterLogin, enterPass;
    TextView enter_help_text, info_login, info_clipboardnow, clipboardnow, register_text, forgotpass;
    Button btnEnter, btnExit, btnRe_read, btnCleanHistory;
    Switch switchAutoUpdate, switchNotification;
    Handler handler_enter;
    StaticSettings staticSettings;
    private volatile String result = "";
    private static final int NOTIFY_ID = 100;

    private static String login = "null"; // for save login name
    private static String userkey = "userkey";
    private static String userlogin = "userlogin";
    private FragmentActivity myContext;

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

        btnExit = (Button)myView.findViewById(R.id.btnExit);
        switchAutoUpdate = (Switch) myView.findViewById(R.id.switchAutoUpdateData);
        enter_help_text = (TextView)myView.findViewById(R.id.enter_help_text);
        progressBarEnter = (ProgressBar)myView.findViewById(R.id.progressBarEnter);
        enterLogin = (EditText)myView.findViewById(R.id.settings_enter_login);
        enterPass = (EditText)myView.findViewById(R.id.settings_enter_pass);
        btnEnter = (Button)myView.findViewById(R.id.btn_enter);
        register_text = (TextView)myView.findViewById(R.id.register_text);
        forgotpass = (TextView)myView.findViewById(R.id.forgot_pass);

        progressBarEnter.setVisibility(View.INVISIBLE);
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
            public void handleMessage(Message msg)
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
                    Toast toast = Toast.makeText(getContext(), R.string.error_enter, Toast.LENGTH_SHORT);
                    toast.show();
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
                    Toast toast = Toast.makeText(getContext(), R.string.error_connect, Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    btnEnter.setEnabled(true);
                    progressBarEnter.setVisibility(View.INVISIBLE);
                    Toast toast = Toast.makeText(getContext(), R.string.error_invalid_responce, Toast.LENGTH_LONG);
                    toast.show();
                }
            };
        };

// -------------------------------------------------------------------------------------------------
        btnEnter.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            if (    enterLogin.length() >= staticSettings.getMinLoginPass()
                    && enterLogin.length() <= staticSettings.getMaxLoginPass())
            {
                login = null;
                login = enterLogin.getText().toString();
                btnEnter.setEnabled(false);
                // Thread per operations
                progressBarEnter.setVisibility(View.VISIBLE);
                Thread thread_enter = new Thread(new Runnable() {

                    public void run() {
                        Message message = handler_enter.obtainMessage();
                        Bundle bundle = new Bundle();
                        ConnectLogic connect = new ConnectLogic(); // For server connect and send data
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
        clipboardnow.setText(reReadClipboard());

        switchAutoUpdate = (Switch)myViewAuthOk.findViewById(R.id.switchAutoUpdateData);
        switchNotification = (Switch)myViewAuthOk.findViewById(R.id.switchNotification);

        Context context = getContext();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(getContext());
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_name));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.cancel(NOTIFY_ID);

        btnExit.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            saveCookie("", "");
            writeCookieOnFile("0");
            getContext().stopService(new Intent(getContext(), ServiceInterCloud.class));
            restartFirstActivity();
        }});
        btnRe_read.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            clipboardnow.setText(reReadClipboard());
        }});
        btnCleanHistory.setOnClickListener(new View.OnClickListener(){public void onClick(View myView) {
            Context context = getContext();
            db = new HelperFragmentHistoryDBWorked(context);
            db.dropAllTable();
        }});
        //------------------------------
        // Start service into data
        getContext().startService(new Intent(getContext(), ServiceInterCloud.class)
                .putExtra("mode_state_io", true)
                .putExtra("userkey", loadCookie()));

        switchAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        // listener switch auto update clipboard
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Make PendingIntent for Task1
                    getContext().startService(new Intent(getContext(), ServiceInterCloud.class)
                            .putExtra("mode_state_io", true)
                            .putExtra("userkey", loadCookie())
                            .putExtra("data in clipboard", ""));
                } else {
                    getContext().stopService(new Intent(getContext(), ServiceInterCloud.class));
                }
            }
        });
        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // listener switch auto update clipboard
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sPref.edit();
                if (isChecked) {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true);
                    editor.apply();
                 //   Log.d(TAG, "switch=" + sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
                } else {
                    editor.putBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, false);
                    editor.apply();
                 //   Log.d(TAG, "switch=" + sPref.getBoolean(APP_PREFERENCES_SWITCH_NOTIFICATION, true));
                }
            }
        });
        info_login.setText(loadLoginWork()); //replace via data from server

        broadcastReceiver = new BroadcastReceiver() {
            // тут принимаем данные из сервиса
            public void onReceive(Context context, Intent intent) {
                boolean sessionClosed = intent.getBooleanExtra("sessionClosed", false);
              //  Log.d(TAG, "tusa=" + sessionClosed);
            }
        };
        IntentFilter intFilter = new IntentFilter(BROADCAST_ACTION);
        getContext().registerReceiver(broadcastReceiver, intFilter);
        return myViewAuthOk;
    }

    // other methods
    private String reReadClipboard() {
        // create obj for read clipboard
        String result = "";
        try {
        ClipboardManager cliboardManager = (ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
        ClipData clipnow = cliboardManager.getPrimaryClip();
        if (clipnow.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            try {
                result = clipnow.getItemAt(0).getText().toString();
               // Log.d(TAG, "clipnowOK=" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }catch (Exception errorreadclip){
           // Log.d(TAG, "clipnow=" + errorreadclip);
    }
        return result;
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
            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(getActivity().openFileOutput("key", MODE_PRIVATE)));
            // пишем данные
            bw.write(key);
            // закрываем поток
            bw.close();
       //     Log.d(TAG, "Файл записан" + key);
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
}