package com.tvvtek.keepstring;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HelperFragmentHistoryDataBase extends SQLiteOpenHelper {

    StaticSettings staticSettings;
    private static final String DATABASE_TABLE = "history";
    public static final String COLUMN_ID = "id";
    public static final String TIMESTAMP_COLUMN = "timestamp";
    public static final String DEVICENAME_COLUMN = "devicename";
    public static final String NAME_COLUMN = "name";
    public static final String DATA_COLUMN = "data";
    public static final String OTHER_COLUMN = "other";

    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + COLUMN_ID
            + " integer primary key autoincrement, " + TIMESTAMP_COLUMN
            + " text not null, " + DEVICENAME_COLUMN + " text not null, "
            + NAME_COLUMN + " text not null, " + DATA_COLUMN + " text not null, "
            + OTHER_COLUMN + " text not null);";

    public HelperFragmentHistoryDataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(staticSettings.getLogTag(),"SQLite" + "Обновляемся с версии " + oldVersion + " на версию " + newVersion);
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}