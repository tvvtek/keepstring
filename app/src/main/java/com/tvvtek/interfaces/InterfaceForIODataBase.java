package com.tvvtek.interfaces;


import java.util.ArrayList;

public interface InterfaceForIODataBase {
    public void writeDataBase(String userdata);
    public ArrayList<String> getArrayListItem();
    public void removeItem(String data_row);
    public String readDataByDate(String timestamp);
}