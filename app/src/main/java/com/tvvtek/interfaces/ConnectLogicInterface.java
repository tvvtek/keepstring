package com.tvvtek.interfaces;

public interface ConnectLogicInterface {
    public void setRequest(String data[]);
    public void setScriptName(String script_name);
    public String getResponse();
    public int getStateRequest();
    public void work();
}