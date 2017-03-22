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

    public static final int DATABASE_VERSION = 7;
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
        if (oldVersion < 6){
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
        //contentValues.put(AthleteData.AthleteDataEntry.FULL_NAME,fullName);
        Log.d(TAG,"ContentValues: " + contentValues.toString());
        Log.d(TAG,"DB version: " + db.getVersion());
        long val =  db.insert(AthleteData.AthleteDataEntry.TABLE_NAME,null, contentValues);
        db.close();
        return val;

    }

    public boolean contains(String fullName){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + AthleteData.AthleteDataEntry.TABLE_NAME,null);
        Log.d(TAG,"Cursor: " + cursor.toString());
        return true;
    }

    public long addToExistingAthlete(String firstName, String surName, String ForceData, String ForceTimeData, String ACCData, String GyroData, String ACCGyroTimeData){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AthleteData.AthleteDataEntry.COLUMN_NAME,firstName);
        contentValues.put(AthleteData.AthleteDataEntry.COLUMN_SURNAME,surName);
        contentValues.put(AthleteData.AthleteDataEntry.FORCE,ForceData);
        contentValues.put(AthleteData.AthleteDataEntry.FORCETIME,ForceTimeData);
        contentValues.put(AthleteData.AthleteDataEntry.ACCELORMETER,ACCData);
        contentValues.put(AthleteData.AthleteDataEntry.GYROSCOPE,GyroData);
        contentValues.put(AthleteData.AthleteDataEntry.ACCGYROTIME,ACCGyroTimeData);
        Log.d(TAG,"ContentValues: " + contentValues.toString());
        long val =  db.insert(AthleteData.AthleteDataEntry.TABLE_NAME,null, contentValues);
        db.close();
        return val;
    }

    public long getRowId(String firstName, String LastName){
        String query = "SELECT rowid FROM " + AthleteData.AthleteDataEntry.TABLE_NAME + " WHERE " + AthleteData.AthleteDataEntry.FULL_NAME + " = '" + firstName+LastName + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery(query,null);
        long id = res.getLong(res.getColumnIndex("_id"));
        db.close();
        return id;
    }

    public Cursor getData(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] returnTemplate = {
                AthleteData.AthleteDataEntry._ID,
                AthleteData.AthleteDataEntry.COLUMN_NAME,
                AthleteData.AthleteDataEntry.COLUMN_SURNAME,
                AthleteData.AthleteDataEntry.FORCE,
                AthleteData.AthleteDataEntry.ACCELORMETER,
                AthleteData.AthleteDataEntry.GYROSCOPE,
                AthleteData.AthleteDataEntry.FORCETIME,
                AthleteData.AthleteDataEntry.ACCGYROTIME
        };

        String selection = AthleteData.AthleteDataEntry.COLUMN_NAME + " = ?";
        String[] selectionArgs = {name};
        String sortOrder = AthleteData.AthleteDataEntry.COLUMN_SURNAME + "DESC";

        return db.query(AthleteData.AthleteDataEntry.TABLE_NAME,returnTemplate,selection,selectionArgs,null,null,sortOrder);
    }


    public ArrayList<HashMap<String,String>> getAll(){
        ArrayList<HashMap<String,String>> athleteList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + AthleteData.AthleteDataEntry.TABLE_NAME,null);
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
