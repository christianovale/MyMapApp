package br.com.christiano.mymapapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Christiano on 31/03/2016.
 */
public class MyMapAppDAO {

    private DataBaseHelper helper;
    private SQLiteDatabase db;

    public MyMapAppDAO(Context context){
        helper = new DataBaseHelper(context);
    }

    private SQLiteDatabase getDb() {
        if (db == null) {
            db = helper.getWritableDatabase();
        }
        return db;
    }

    public void close(){
        helper.close();
    }

    public long save(String searchParam){
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.SearchParam.PARAM, searchParam);
        return getDb().insert(DataBaseHelper.SearchParam.TABLE, null, values);
    }

    public String find(){
        Cursor cursor = getDb().query(DataBaseHelper.SearchParam.TABLE, DataBaseHelper.SearchParam.COLUMNS, null, null, null, null, null);
        String searchParam = null;
        while(cursor.moveToNext()){
            searchParam = cursor.getString(cursor.getColumnIndex(DataBaseHelper.SearchParam.PARAM));
        }
        cursor.close();
        return searchParam;
    }

    public Long getId(){
        Cursor cursor = getDb().query(DataBaseHelper.SearchParam.TABLE, DataBaseHelper.SearchParam.COLUMNS, null, null, null, null, null);
        int idSearchParam = 0;
        while(cursor.moveToNext()){
            idSearchParam = cursor.getInt(cursor.getColumnIndex( DataBaseHelper.SearchParam._ID));
        }
        cursor.close();
        return new Long(idSearchParam);
    }

    public boolean remove(){
        Long id = this.getId();
        String whereClause = DataBaseHelper.SearchParam._ID + " = ?";
        String[] whereArgs = new String[]{id.toString()};
        int removidos = getDb().delete(DataBaseHelper.SearchParam.TABLE,whereClause, whereArgs);
        return removidos > 0;
    }

}
