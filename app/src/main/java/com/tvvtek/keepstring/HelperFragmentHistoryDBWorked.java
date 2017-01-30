package com.tvvtek.keepstring;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.tvvtek.interfaces.InterfaceForIODataBase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class HelperFragmentHistoryDBWorked implements InterfaceForIODataBase {

    public HelperFragmentHistoryDBWorked(Context context){
        this.context = context;
    }

    StaticSettings staticSettings;
    private String oneitem;
    ArrayList<String> result_db = new ArrayList<>();
    Context context;

    public HelperFragmentHistoryDataBase mDatabaseHelper;
    public SQLiteDatabase mSqLiteDatabase;

    @Override
    public void writeDataBase(String userdata) {
        try {
            Calendar calendar = new GregorianCalendar();
            String iter_data;
            String day, month, hour, minute, second;
            // if month, day, min, sec < 10 add left side 0
            if (calendar.get(Calendar.DATE) < 10) day = "0" + calendar.get(Calendar.DATE);
            else day = String.valueOf(calendar.get(Calendar.DATE));
            if (calendar.get(Calendar.MONTH) < 10) month = "0" + calendar.get(Calendar.MONTH);
            else month = String.valueOf(calendar.get(Calendar.MONTH));
            if (calendar.get(Calendar.HOUR_OF_DAY) < 10) hour = "0" + calendar.get(Calendar.HOUR_OF_DAY);
            else hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
            if (calendar.get(Calendar.MINUTE) < 10) minute = "0" + calendar.get(Calendar.MINUTE);
            else minute = String.valueOf(calendar.get(Calendar.MINUTE));
            if (calendar.get(Calendar.SECOND) < 10) second = "0" + calendar.get(Calendar.SECOND);
            else second = String.valueOf(calendar.get(Calendar.SECOND));

            iter_data = day + "."
                    + month + "."
                    + calendar.get(Calendar.YEAR) + " "
                    + hour + ":"
                    + minute + ":"
                    + second;
            mDatabaseHelper = new HelperFragmentHistoryDataBase(context, "history_local.db", null, 1);
            mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();

            ContentValues newValues = new ContentValues();
            //inflate object key-value
            newValues.put(HelperFragmentHistoryDataBase.TIMESTAMP_COLUMN, iter_data);
            newValues.put(HelperFragmentHistoryDataBase.DEVICENAME_COLUMN, Build.MODEL); // модель устройства
            newValues.put(HelperFragmentHistoryDataBase.NAME_COLUMN, "name");
            newValues.put(HelperFragmentHistoryDataBase.DATA_COLUMN, userdata);
            newValues.put(HelperFragmentHistoryDataBase.OTHER_COLUMN, "other");
            mSqLiteDatabase.insert("history", null, newValues);
            mSqLiteDatabase.close();
            Log.d(staticSettings.getLogTag(), "data writed");
        }catch (Exception db_write_error){
            Log.d(staticSettings.getLogTag(), db_write_error.toString());
        }
    }

    @Override
    public ArrayList<String> getArrayListItem() {
        try {
            result_db.clear();
            mDatabaseHelper = new HelperFragmentHistoryDataBase(context, "history_local.db", null, 1);
            mSqLiteDatabase = mDatabaseHelper.getReadableDatabase();
            Cursor c = mSqLiteDatabase.query("history", null, null, null, null, null, null);
            if (c.moveToFirst()) {
                int timestampColIndex = c.getColumnIndex("timestamp");
                int dataColIndex = c.getColumnIndex("data");
                do {
                    oneitem = null;
                    oneitem = "<font color=\'#00bcd4\'><b>" + c.getString(timestampColIndex) + "</b></font>" + "<br>" + c.getString(dataColIndex);
                    result_db.add(oneitem);
                } while (c.moveToNext());
            } else
            c.close();
        }catch (Exception db_read_error){
            Log.d(staticSettings.getLogTag(), db_read_error.toString());
        }
        return result_db;
    }

    @Override
    public void removeItem(String data_row) {
        // стираем значение по таймштампу из локальной базы
        mDatabaseHelper = new HelperFragmentHistoryDataBase(context, "history_local.db", null, 1);
        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
        mSqLiteDatabase.delete("history", "timestamp"+ "='" + data_row + "'", null);
    }

    @Override
    public String readDataByDate(String timestamp) {
        String result = "";
        mDatabaseHelper = new HelperFragmentHistoryDataBase(context, "history_local.db", null, 1);
        mSqLiteDatabase = mDatabaseHelper.getReadableDatabase();
        // пробуем сделать запрос с длинной датой, если ошибка, то режем дату(символ <) и пробуем снова
       try {
           Cursor cursor =
                   mSqLiteDatabase.query("history", // a. table
                           new String[]{"data"}, // b. column names
                           " timestamp = ?", // c. selections
                           new String[]{String.valueOf(timestamp)}, // d. selections args
                           null, // e. group by
                           null, // f. having
                           null, // g. order by
                           null); // h. limit
        //   Log.d(TAG, "pos_item" + timestamp);
           if (cursor != null) {
               cursor.moveToFirst();
               result = cursor.getString(0);
           }
       //    Log.d(TAG, "cursor1=" + cursor.getString(0));
           cursor.close();
       }catch (Exception lenght_substring){
           Cursor cursor =
                   mSqLiteDatabase.query("history", // a. table
                           new String[]{"data"}, // b. column names
                           " timestamp = ?", // c. selections
                           new String[]{String.valueOf(timestamp.substring(0, timestamp.length()-1))}, // d. selections args
                           null, // e. group by
                           null, // f. having
                           null, // g. order by
                           null); // h. limit
           if (cursor != null) {
               cursor.moveToFirst();
               result = cursor.getString(0);
           }
         //  Log.d(TAG, "cursor=" + cursor.getString(0));
           cursor.close();
       }
        return result;
    }
}