package com.tvvtek.interfaces;

public interface ConnectLogicInterface {
     void setRequest(String data[]);
     void setScriptName(String script_name);
     String getResponse();
     int getStateRequest();
     void work();
}