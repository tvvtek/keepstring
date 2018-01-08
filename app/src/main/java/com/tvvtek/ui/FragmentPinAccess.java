package com.tvvtek.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tvvtek.connectpackage.ConnectLogic_old;
import com.tvvtek.keepstring.R;
import com.tvvtek.keepstring.StaticSettings;

public class FragmentPinAccess extends Fragment {
    StaticSettings staticSettings;
    private static final String TAG = "KeepString";
    Button btnGet, btnSave, btnReset_overall;
    EditText input_pin_get, input_received_data, input_pin_save;
  //  TextView you_pin_is_authok, you_pin_received_authok;
    ProgressBar progressBarOverall;
    Handler handleroverall_manual_mode;
    private String result = "";
    private Integer result_triggerGet = 0;
    private Integer result_triggerSave = 0;
    private String data_from_server_result_get = "";
    private String data_from_server_result_save = "";
    private Boolean data_from_server_result_save_error = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_pin_access, container, false);
        setRetainInstance(true);
        input_pin_get = (EditText)myView.findViewById(R.id.input_pin_get_authok);
        input_received_data = (EditText)myView.findViewById(R.id.input_received_data);
        input_pin_save = (EditText)myView.findViewById(R.id.input_pin_authok);
        btnGet = (Button) myView.findViewById(R.id.btnGet_authok);
        btnSave = (Button) myView.findViewById(R.id.btn_save_authok);
        btnReset_overall = (Button) myView.findViewById(R.id.btnReset_overall);
        progressBarOverall = (ProgressBar) myView.findViewById(R.id.progressBarOverall);
        btnReset_overall.setVisibility(View.INVISIBLE);
        progressBarOverall.setVisibility(View.INVISIBLE);

        handleroverall_manual_mode = new Handler() {
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                data_from_server_result_save_error = false;
                data_from_server_result_get = bundle.getString("1");
                data_from_server_result_save = bundle.getString("2");
              //  Log.d(TAG, "SAVE=" + data_from_server_result_save);
              //  Log.d(TAG, "GET=" + data_from_server_result_get);
                try {
                    data_from_server_result_save_error = data_from_server_result_save.equals("10");
                }catch (Exception k){
                    //Log.d(TAG, "Booleanerror_save=" + k);
                }

                if (data_from_server_result_get != null)
                {
                    if (data_from_server_result_get.equals("You pin is not found")){
                        Toast toast = Toast.makeText(getContext(), R.string.you_pin_is_not_found, Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else {
                        input_received_data.setText(data_from_server_result_get);
                    }
                }
                progressBarOverall.setVisibility(View.INVISIBLE);
                btnSave.setEnabled(true);
                btnGet.setEnabled(true);
                btnReset_overall.setVisibility(View.VISIBLE);

               // if (result_triggerGet == 1) {}

                if (data_from_server_result_save_error){
                    Toast toast = Toast.makeText(getContext(), R.string.save_auth_data_written_pinexist, Toast.LENGTH_LONG);
                    toast.show();
                    btnSave.setEnabled(true);
                    btnGet.setEnabled(true);
                    progressBarOverall.setVisibility(View.INVISIBLE);
                    return;
                }
                else if (data_from_server_result_save != null) {
                    input_pin_save.setText(data_from_server_result_save);
                    progressBarOverall.setVisibility(View.INVISIBLE);
                    btnReset_overall.setVisibility(View.VISIBLE);
                    Toast toast = Toast.makeText(getContext(), R.string.save_auth_data_written, Toast.LENGTH_LONG);
                    toast.show();
                    data_from_server_result_save = "";}
                btnSave.setEnabled(true);
                btnGet.setEnabled(true);
                if (result_triggerSave == 1 || result_triggerGet == 1) {}
                btnReset_overall.setVisibility(View.VISIBLE);
                btnGet.setEnabled(true);
                btnSave.setEnabled(true);
            }};
        btnGet.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View myView)
            {
                if (input_pin_get.getText().length() >= staticSettings.getMinInputPin()) {
                    btnGet.setEnabled(false);
                    btnSave.setEnabled(false);
                    progressBarOverall.setVisibility(View.VISIBLE);
                    result_triggerGet = 1;
                    Thread thread_get = new Thread(new Runnable() {
                        public void run() {
                            Message message = handleroverall_manual_mode.obtainMessage();
                            Bundle bundle = new Bundle();
                            ConnectLogic_old connect = new ConnectLogic_old(); // For server connect and send data
                            connect.setScriptName("getapi.php");
                            //   connect.setScriptName("print.php"); // this variant for testing setRequest POST data
                            String[] request_register = {
                                    "getpin", input_pin_get.getText().toString(),
                                    "androidkey", staticSettings.getAndroidKey()};
                            Log.d(staticSettings.getLogTag(), "getpin=" + input_pin_get.getText().toString());
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
                                } catch (Exception e) {
                                    Log.d(staticSettings.getLogTag(), "ErrorGetDataFromConnectobj= " + e);
                                }
                            }
                            else if (connect.getStateRequest() == 0) {
                                result = "error_connect";
                            }
                            //   Log.d(staticSettings.getLogTag(), "connect.getStateRequest()=" + connect.getStateRequest());
                            bundle.putString("1", result);
                            message.setData(bundle);
                            handleroverall_manual_mode.sendMessage(message);
                            //    Log.d(staticSettings.getLogTag(), "thread register end");
                        }
                    });
                    thread_get.start();
                } else {
                    Toast toast = Toast.makeText(getContext(), R.string.error_input_pin_get, Toast.LENGTH_SHORT);
                    toast.show();}}});

        btnSave.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View myView)
            {
                if (input_pin_save.length() > 0 & input_pin_save.length() < staticSettings.getMinInputPin())
                {
                    Toast toast = Toast.makeText(getContext(), R.string.error_input_pin_get, Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                else if
                        (input_received_data.getText().length() >= staticSettings.getMinEditText() &&
                                input_received_data.getText().length() <= staticSettings.getMaxEditText())
                {
                    btnGet.setEnabled(false);
                    btnSave.setEnabled(false);
                    progressBarOverall.setVisibility(View.VISIBLE);
                    result_triggerGet = 0;
                    // Thread per operations
                    Thread thread_save = new Thread(new Runnable() {

                        public void run() {
                            Message message = handleroverall_manual_mode.obtainMessage();
                            Bundle bundle = new Bundle();
                            ConnectLogic_old connect = new ConnectLogic_old(); // For server connect and send data
                            connect.setScriptName("saveapi.php");
                            //   connect.setScriptName("print.php"); // this variant for testing setRequest POST data
                            String[] request_register = {
                                    "mypincode", input_pin_save.getText().toString(),
                                    "data", input_received_data.getText().toString(),
                                    "androidkey", staticSettings.getAndroidKey()};
                            Log.d(staticSettings.getLogTag(), "getpin=" + input_pin_get.getText().toString());
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
                                } catch (Exception e) {
                                    Log.d(staticSettings.getLogTag(), "ErrorGetDataFromConnectobj= " + e);
                                }
                            }
                            else if (connect.getStateRequest() == 0) {
                                result = "error_connect";
                            }
                            bundle.putString("2", result);
                            message.setData(bundle);
                            handleroverall_manual_mode.sendMessage(message);
                        }
                    });
                    thread_save.start();
                }
                else {
                    Toast toast = Toast.makeText(getContext(), R.string.error_input_data_write, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        btnReset_overall.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View myView)
            {
                btnGet.setEnabled(true);
                btnSave.setEnabled(true);
                btnReset_overall.setVisibility(View.INVISIBLE);
                input_pin_get.setText("");
                input_pin_save.setText("");
                input_received_data.setText("");
                progressBarOverall.setVisibility(View.INVISIBLE);
            }
        });
        // other methods for view
        input_received_data.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.input_received_data) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;}}return false;}});
        return myView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}