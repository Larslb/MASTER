package com.example.larslb.trigger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.larslb.trigger.AthleteData;

/**
 * Created by larslb on 14.02.2017.
 */

public class DBAthlete extends SQLiteOpenHelper {
    public static final String TAG = DBAthlete.class.getSimpleName();

    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "AthletesData.db";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + AthleteData.AthleteDataEntry.TABLE_NAME + " (" +
            AthleteData.AthleteDataEntry._ID + " INTEGER PRIMARY KEY, " +
            AthleteData.AthleteDataEntry.COLUMN_NAME + " TEXT, " +
            AthleteData.AthleteDataEntry.COLUMN_SURNAME + " TEXT " +
            AthleteData.AthleteDataEntry.FULL_NAME + " TEXT " +
            AthleteData.AthleteDataEntry.FORCETIME + " TEXT " +
            AthleteData.AthleteDataEntry.ACCELORMETER + " TEXT " +
            AthleteData.AthleteDataEntry.GYROSCOPE + " TEXT " +
            AthleteData.AthleteDataEntry.ACCGYROTIME + " TEXT " +
            AthleteData.AthleteDataEntry.FORCE + "TEXT" +")";


    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + AthleteData.AthleteDataEntry.TABLE_NAME;



    public DBAthlete (Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.d(TAG,"onUpgrade!");
        if (oldVersion < 10){
            db.execSQL(SQL_DELETE_ENTRIES);

            onCreate(db);
        }
        else{
            db.execSQL("ALTER TABLE "+AthleteData.AthleteDataEntry.TABLE_NAME + " ADD COLUMN " + AthleteData.AthleteDataEntry.FORCE + " TEXT");
            db.execSQL("ALTER TABLE "+AthleteData.AthleteDataEntry.TABLE_NAME + " ADD COLUMN " + AthleteData.AthleteDataEntry.ACCGYROTIME+ " TEXT");
            db.execSQL("ALTER TABLE "+AthleteData.AthleteDataEntry.TABLE_NAME + " ADD COLUMN " + AthleteData.AthleteDataEntry.ACCELORMETER+ " TEXT");
            db.execSQL("ALTER TABLE "+AthleteData.AthleteDataEntry.TABLE_NAME + " ADD COLUMN " + AthleteData.AthleteDataEntry.GYROSCOPE+ " TEXT");
            db.execSQL("ALTER TABLE "+AthleteData.AthleteDataEntry.TABLE_NAME + " ADD COLUMN " + AthleteData.AthleteDataEntry.FORCETIME+ " TEXT");
            db.execSQL("ALTER TABLE "+AthleteData.AthleteDataEntry.TABLE_NAME + " ADD COLUMN " + AthleteData.AthleteDataEntry.FULL_NAME+ " TEXT");

        }

    }

    public long addAthlete(String firstName, String surName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AthleteData.AthleteDataEntry.COLUMN_NAME,firstName);
        contentValues.put(AthleteData.AthleteDataEntry.COLUMN_SURNAME,surName);
        String fullName = firstName + surName;
        contentValues.put(AthleteData.AthleteDataEntry.FULL_NAME,fullName);
        Log.d(TAG,"ContentValues: " + contentValues.toString());
        Log.d(TAG,"DB version: " + db.getVersion());
        long val =  db.insert(AthleteData.AthleteDataEntry.TABLE_NAME,null, contentValues);
        db.close();
        return val;

    }

    public int deleteAthlete(String fullname){
        SQLiteDatabase db = this.getWritableDatabase();
        int id = db.delete(AthleteData.AthleteDataEntry.TABLE_NAME,AthleteData.AthleteDataEntry.FULL_NAME + " = " + fullname,null);
        db.close();
        return id;

    }

    public String printTable(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + AthleteData.AthleteDataEntry.TABLE_NAME,null);
        String tableString = String.format("Table %s:\n",AthleteData.AthleteDataEntry.TABLE_NAME);
        Log.d(TAG,"Database need upgrade?   " + db.needUpgrade(7));
        Log.d(TAG,"Database in string:  " + db.toString());

        while (cursor.moveToNext()){
            String[] columnNames = cursor.getColumnNames();
            for (String name : columnNames){
                Log.d(TAG,"Column : " + name + "    Data: " + cursor.getString(cursor.getColumnIndex(name)));
                tableString += String.format("%s: %s\n", name, cursor.getString(cursor.getColumnIndex(name)));
            }
            tableString += "\n";
        }
        db.close();
        return tableString;
    }

    public boolean contains(String fullName){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + AthleteData.AthleteDataEntry.TABLE_NAME + " WHERE " + AthleteData.AthleteDataEntry.FULL_NAME + " = '" + fullName + "'";
        Cursor c = db.rawQuery(query,null);
        db.close();
        return c.moveToFirst();
    }

    public long addToExistingAthlete(String firstName, String surName, String ForceData, String ForceTimeData, String ACCData, String GyroData, String ACCGyroTimeData){
        SQLiteDatabase db = this.getWritableDatabase();
        long id = getRowId(firstName,surName);
        ContentValues contentValues = new ContentValues();
        contentValues.put(AthleteData.AthleteDataEntry.FORCE,ForceData);
        contentValues.put(AthleteData.AthleteDataEntry.FORCETIME,ForceTimeData);
        contentValues.put(AthleteData.AthleteDataEntry.ACCELORMETER,ACCData);
        contentValues.put(AthleteData.AthleteDataEntry.GYROSCOPE,GyroData);
        contentValues.put(AthleteData.AthleteDataEntry.ACCGYROTIME,ACCGyroTimeData);
        Log.d(TAG,"ContentValues: " + contentValues.toString());
        Log.d(TAG,"added to row id: " + id);
        long val =  db.update(AthleteData.AthleteDataEntry.TABLE_NAME,contentValues, "_id=" + id,null);
        db.close();
        return val;
    }

    public long getRowId(String firstName, String LastName){
        String query = "SELECT _id FROM " + AthleteData.AthleteDataEntry.TABLE_NAME + " WHERE " + AthleteData.AthleteDataEntry.FULL_NAME + " = " + firstName+LastName + "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query,null);
        long id = res.getLong(res.getColumnIndex("_id"));
        db.close();
        return id;
    }

    public Cursor getDataFromId(Long id){
        String query = "SELECT * FROM " + AthleteData.AthleteDataEntry.TABLE_NAME + " WHERE " + AthleteData.AthleteDataEntry._ID + " = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query,null);
    }


    public ArrayList<HashMap<String,String>> getAll(){
        ArrayList<HashMap<String,String>> athleteList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + AthleteData.AthleteDataEntry.TABLE_NAME,null);
        res.moveToFirst();

        Log.d(TAG,"DB version: " + db.getVersion());

        while(!res.isAfterLast()){
            HashMap<String,String> athlete = new HashMap<>();
            String name = res.getString(res.getColumnIndex(AthleteData.AthleteDataEntry.COLUMN_NAME));
            String secondName = res.getString(res.getColumnIndex(AthleteData.AthleteDataEntry.COLUMN_SURNAME));
            athlete.put("FirstName",name);
            athlete.put("SurName",secondName);
            athleteList.add(athlete);
            res.moveToNext();
        }
        res.close();
        return athleteList;
    }
}
