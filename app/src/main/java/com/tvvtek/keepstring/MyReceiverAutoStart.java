package com.tvvtek.keepstring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class MyReceiverAutoStart extends BroadcastReceiver {
    Context ctx;
    StaticSettings staticSettings;
    public static final String APP_PREFERENCES_SWITCH_AUTOSTART = "switch_auto_start";
    public MyReceiverAutoStart() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.ctx = context;
        SharedPreferences sPref = ctx.getSharedPreferences("TAG", MODE_PRIVATE);
        if (sPref.getBoolean(APP_PREFERENCES_SWITCH_AUTOSTART, true)){
            try {
                context.startService(new Intent(context, ServiceInterCloud.class)
                        .putExtra("mode_state_io", true)
                        .putExtra("userkey", loadKeyFromDevice(context))
                        .putExtra("data in clipboard", ""));
                Log.d(staticSettings.getLogTag(), " start service KeepString");
                Toast toast = Toast.makeText(context,
                        context.getResources().getString(R.string.auto_start_service), Toast.LENGTH_LONG);
                toast.show();
            }
            catch (Exception auto_start_error){
                auto_start_error.printStackTrace();
            }
        }else{
            Log.d(staticSettings.getLogTag(), "auto start service KeepString disabled");
        }
    }
    private String loadKeyFromDevice(Context ctx) {
        SharedPreferences sPref = ctx.getSharedPreferences("TAG", MODE_PRIVATE);
        return sPref.getString("userkey","");
    }
}
