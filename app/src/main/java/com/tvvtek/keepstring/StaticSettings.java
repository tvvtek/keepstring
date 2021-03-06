package com.tvvtek.keepstring;


public final class StaticSettings {


    public static final String YES_ACTION = "YES_ACTION";
    public static final String STOP_ACTION = "STOP_ACTION";

    private static String url = "https://keepstring.com/";
    private static String androidkey = "asfw;qpl[a!mwfeASLMCVs;adafeqwo2[ez";
    //private static String url = "https://192.168.1.15/wots";
    private static int minEditTextValue = 6;
    private static int maxEditTextValue = 65535;
    private static int minInputPinCode = 6;
    private static String logTag = "KeepString";
    private static int time_update = 5;
    private static int time_timeout_request = 10;
    private static int time_splash_screen = 2000; // ms
    private static int time_re_read_clipboad = 1000; //ms
    private static int minLoginPass = 3;
    private static int maxLoginPass = 30;
    private static int fragmentNumberOpen = 0;

    public static String getUrl() {
        return url;
    }

    public static int getMinEditText() {
        return minEditTextValue;
    }

    public static int getMaxEditText() {
        return maxEditTextValue;
    }

    public static int getMinInputPin() {
        return minInputPinCode;
    }

    public static int getTimePerUpdateData() {
        return time_update;
    }

    public static String getLogTag() {
        return logTag;
    }

    public static int getMinLoginPass() {
        return minLoginPass;
    }

    public static int getMaxLoginPass() {
        return maxLoginPass;
    }

    public static String getAndroidKey() {
        return androidkey;
    }

    public static int getTimeTimeout() {
        return time_timeout_request;
    }

    public static int getTimeSplashScreen() {return time_splash_screen;}
    public static int getTimeReReadClipboard() {return time_re_read_clipboad;}
}