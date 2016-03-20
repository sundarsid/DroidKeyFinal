package com.example.sundar.droidkey;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sundar on 28/1/16.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DroidKey.db";
    public static final String LOCKs_TABLE_NAME = "locks";
    public static final String LOCKS_COLUMN_ID = "id";
    public static final String LOCKS_COLUMN_NAME = "name";
    public static final String LOCKS_COLUMN_KEY = "key";
    public static final String LOCKS_COLUMN_IP = "ip";
    public static final String LOCKS_COLUMN_ISADMIN = "isadmin";
    public static final String USERS_COLUMN_NAME ="uname";
    public static final String USERS_COLUMN_KEY ="ukey";


    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "create table locks " +
                        "( _id integer primary key, name text,key integer,ip text,isadmin integer)"
        );
        db.execSQL(
                "create table users " +
                        "( _id integer primary key, uname text,ukey integer,lockno integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS locks");
        onCreate(db);
    }

    public boolean insertLock  (String name, String ip, int key, int isAdmin)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("key", key);
        contentValues.put("ip", ip);
        contentValues.put("isadmin", isAdmin);
        db.insert("locks", null, contentValues);
        return true;
    }

    public boolean insertUser (String name,int key,int lockNo)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uname", name);
        contentValues.put("ukey", key);
        contentValues.put("lockno", lockNo);
        db.insert("users", null, contentValues);
        return true;

    }

    public Cursor populate_list_admin(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from locks", null );
        return res;
    }
    public Cursor populate_list_client(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from locks where isadmin = 0", null );
        return res;
    }
    public Cursor populate_list_users(int lockNo){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from users where lockno = "+lockNo, null );
        return res;
    }


    public Integer deleteLock (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("locks",
                "_id = ? ",
                new String[] { Integer.toString(id) });
    }
    public Integer delete_alluser (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("users",
                "lockno = ? ",
                new String[] { Integer.toString(id) });
    }







}
