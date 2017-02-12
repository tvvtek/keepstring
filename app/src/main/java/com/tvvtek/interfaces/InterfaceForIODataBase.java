package com.tvvtek.interfaces;


import java.util.ArrayList;

public interface InterfaceForIODataBase {
    void writeDataBase(String userdata);
    ArrayList<String> getArrayListItem();
    void removeItem(String data_row);
    String readDataByDate(String timestamp);
    void dropAllTable();
}