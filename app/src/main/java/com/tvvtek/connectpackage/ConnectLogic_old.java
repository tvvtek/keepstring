package com.tvvtek.connectpackage;

import android.util.Log;

import com.tvvtek.interfaces.ConnectLogicInterface;
import com.tvvtek.keepstring.StaticSettings;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectLogic_old implements ConnectLogicInterface {
    private volatile int now_state_request_result = 2;
    private String data_from_server = "";
    StaticSettings staticSettings;
    private String script_name = null;
    private String request[] = null;

    //make a http client
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(staticSettings.getTimeTimeout(), TimeUnit.SECONDS)
            .writeTimeout(staticSettings.getTimeTimeout(), TimeUnit.SECONDS)
            .readTimeout(staticSettings.getTimeTimeout(), TimeUnit.SECONDS)
            .build();

    private String serverAddress = staticSettings.getUrl();
    /**
     *  params:
        flag (0 - enter, 1 - register , 2 - write clip, 3 - read clip, 4 - read md5 summ clip)
        login ( login register or enter)
        password ( password register or enter)
        email (for register)
        cookie - user key
        androidkey (androidkey for subscription android device)
     */
    /**
     * Request thread, for io client - server
     */
    Thread thread_connect = new Thread(new Runnable() {
        public void run()
        {
            if (script_name != null) {
                try {
                    //    Log.d(staticSettings.getLogTag(), "serverAddress=" + serverAddress + script_name);
                    FormBody.Builder formBuilder = new FormBody.Builder().add("", ""); // this empty key+value
                        // parse array setRequest andm make body for send server
                        for (int request_lenght = request.length, step = 0; step < request_lenght; step++) {
                            formBuilder.add(request[step], request[step + 1]);
                            //    Log.d(staticSettings.getLogTag(), "key=" + setRequest[step] + "&value=" + setRequest[step + 1]);
                            step++;
                        }
                        RequestBody formBody = formBuilder.build();
                        Request request = new Request.Builder()
                                .url(serverAddress + script_name)
                                .post(formBody)
                                .build();

                        Response response = client.newCall(request).execute(); // this stop code till data received from server
                        if (!response.isSuccessful()) {
                            throw new IOException("Error connect code= " + response);
                        } else {
                            data_from_server = response.body().string(); //
                            now_state_request_result = 1;
                         //   Log.d(staticSettings.getLogTag(), "OK INET" + data_from_server);
                        }
                } catch (Exception error_connect) {
                    now_state_request_result = 0;
                    Log.d(staticSettings.getLogTag(), "Not internet=" + error_connect);
                    Log.d(staticSettings.getLogTag(), "Not now_state_request_result" + now_state_request_result);
                }
            }
            else Log.d(staticSettings.getLogTag(), "Error script name, please call 'setScriptName(String script_name)'");
        }
    });
    /**
     * Method work() is start thread setRequest
     */
    @Override
    public void work() {
        if (this.request != null)thread_connect.start();
        else Log.d(staticSettings.getLogTag(), "Request is null, thread 'thread_connect' not start, please call 'setRequest(String[] request)'");
    }
    /**
     * Method getStateRequest
     * @return -1 timeout end
     * @return 0 timeout error or connect error
     * @return 1 responce is successful
     */
    @Override
    public int getStateRequest(){
        return now_state_request_result;
    }
    @Override
    public void setRequest(String[] data) {
        this.request = null;
        this.request = data;
    }
    @Override
    public void setScriptName(String script_name) {
        this.script_name = script_name;
    }

    @Override
    public String getResponse() {
        return data_from_server;
    }
}