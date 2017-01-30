package com.tvvtek.keepstring;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tvvtek.connectpackage.ConnectLogic;
import com.tvvtek.interfaces.InterfaceOnBackPressedListener;

public class FragmentRegister extends Fragment implements InterfaceOnBackPressedListener {
   // private static final String TAG = "KeepString";
    ProgressBar progressBarRegister;
    EditText registerLogin, registerPass, registerPassConfirm, registerEmail;
    TextView enter_help_text, register_help_text;
    Button btnRegister;
    Handler  handler_register;
    StaticSettings staticSettings;
    private FragmentActivity myContext;
    private volatile String result = "";
    private Integer result_trigger = 0;

    /**
     * @param context
     * Receive app Context for receive Activity, for interception Back hardware button
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;
        myContext=(FragmentActivity) activity;
    }
    /**
     * Section Create View for this fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View myView = inflater.inflate(R.layout.fragment_register, container, false);
        enter_help_text = (TextView)myView.findViewById(R.id.enter_help_text);
        progressBarRegister = (ProgressBar)myView.findViewById(R.id.progressBarRegister);

        register_help_text = (TextView)myView.findViewById(R.id.register_help_text);

        registerLogin = (EditText)myView.findViewById(R.id.settings_reg_login);
        registerPass = (EditText)myView.findViewById(R.id.settings_reg_pass);
        registerPassConfirm = (EditText)myView.findViewById(R.id.settings_reg_pass_confirm);
        registerEmail = (EditText)myView.findViewById(R.id.settings_reg_email);
        btnRegister = (Button)myView.findViewById(R.id.btn_register);

        progressBarRegister.setVisibility(View.INVISIBLE);

/**
* Section handler for update Viev form thread into section btnRegister
*/
        handler_register = new Handler()
        {
            public void handleMessage(Message msg)
            {
                // update TextView
                Bundle bundle_register = msg.getData();
                String data_from_thread_register = bundle_register.getString("1");
                if (data_from_thread_register.equals("0"))
                {
                    btnRegister.setEnabled(true);
                    progressBarRegister.setVisibility(View.INVISIBLE);
                    Toast toast = Toast.makeText(getContext(), R.string.error_register_login_exist, Toast.LENGTH_LONG);
                    toast.show();
                }

                else if (data_from_thread_register.equals("1"))
                {
                    btnRegister.setEnabled(true);
                    progressBarRegister.setVisibility(View.INVISIBLE);
                    registerLogin.setText("");
                    registerPass.setText("");
                    registerPassConfirm.setText("");
                    registerEmail.setText("");
                    Toast toast = Toast.makeText(getContext(), R.string.register_ok, Toast.LENGTH_SHORT);
                    toast.show();
                    goEnterFragment();
                }
                else if (data_from_thread_register.equals("error_connect"))
                {
                    btnRegister.setEnabled(true);
                    progressBarRegister.setVisibility(View.INVISIBLE);
                    Toast toast = Toast.makeText(getContext(), R.string.error_connect, Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    btnRegister.setEnabled(true);
                    progressBarRegister.setVisibility(View.INVISIBLE);
                    Toast toast = Toast.makeText(getContext(), R.string.error_invalid_responce, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        };
/**
 * Section for do press btn Register
 */
        btnRegister.setOnClickListener(new View.OnClickListener() {public void onClick(View myView) {
            if (!registerPass.getText().toString().equals(registerPassConfirm.getText().toString())){
                Toast toast = Toast.makeText(getContext(), R.string.register_pass_error, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmail.getText().toString()).matches()) {
                Toast toast = Toast.makeText(getContext(), R.string.register_email_error, Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (    registerLogin.length() >= staticSettings.getMinLoginPass()
                    && registerLogin.length() <= staticSettings.getMaxLoginPass())
            {
                btnRegister.setEnabled(false);
                progressBarRegister.setVisibility(View.VISIBLE);
                // Thread per operations
                Thread thread_register = new Thread(new Runnable() {

                    public void run() {
                        Message message = handler_register.obtainMessage();
                        Bundle bundle = new Bundle();
                        ConnectLogic connect = new ConnectLogic(); // For server connect and send data
                        connect.setScriptName("cloudapi.php");
                     //   connect.setScriptName("print.php"); // this variant for testing setRequest POST data
                        String[] request_register = {"flag", "1",
                                            "login", registerLogin.getText().toString(),
                                            "password", registerPass.getText().toString(),
                                            "email", registerEmail.getText().toString(),
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
                     //   Log.d(staticSettings.getLogTag(), "connect.getStateRequest()=" + connect.getStateRequest());
                        bundle.putString("1", result);
                        message.setData(bundle);
                        handler_register.sendMessage(message);
                    //    Log.d(staticSettings.getLogTag(), "thread register end");
                    }
                });
                thread_register.start();
            }
            else
            {
                Toast toast = Toast.makeText(getContext(), R.string.error_login_pass, Toast.LENGTH_SHORT);
                toast.show();
            }
        }});
        return myView;
    }
    @Override
    public void onBackPressed() {
        goEnterFragment();
    }

    /**
     * Section goBack previous fragment
     * Go Enter Fragment
     */
    private void goEnterFragment(){
        Fragment fragment;
        Class fragmentClass;
        FragmentManager fragManager = myContext.getSupportFragmentManager();
        fragmentClass = FragmentEnter.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            // Put fragment and replace this fragment
            fragManager.beginTransaction().replace(R.id.container, fragment).commit();
        }catch (Exception error_replace_fragment){
            error_replace_fragment.printStackTrace();
        }
    }
}