package br.com.christiano.mymapapp.dao;

/**
 * Created by Christiano on 31/03/2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATA_BASE = "MyMapApp";
    private static int VERSION = 1;

    public DataBaseHelper(Context context) {
        super(context, DATA_BASE, null, VERSION);
    }

    public static class SearchParam {
        public static final String TABLE = "SEARCH_PARAM";
        public static final String _ID = "_ID";
        public static final String PARAM = "PARAM";

        public static final String[] COLUMNS = new String[]{ _ID, PARAM };
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE SEARCH_PARAM (_ID INTEGER PRIMARY KEY, PARAM TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

