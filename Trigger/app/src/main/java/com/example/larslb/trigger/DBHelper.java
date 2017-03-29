package com.example.larslb.trigger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by larslb on 23.03.2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "triggerdb";

    private static final String CREATE_TABLE_ATHLETES = "CREATE TABLE " + AthleteData.TABLE_NAME + "(" +
            AthleteData.COLUMN_ID + " INTEGER PRIMARY KEY, " +
            AthleteData.COLUMN_FIRSTNAME + " TEXT, " +
            AthleteData.COLUMN_LASTNAME + " TEXT, " +
            AthleteData.COLUMN_DATEOFBIRTH + " DATETIME, " +
            AthleteData.COLUMN_GENDER + " TEXT" + ")";

    private static final String CREATE_TABLE_SHOOTINGS = "CREATE TABLE " + ShootingData.TABLE_NAME + "(" +
            ShootingData.COLUMN_ID + " INTEGER PRIMARY KEY, " +
            ShootingData.COLUM_DATE + " DATETIME, " +
            ShootingData.COLUMN_NUMBER_SHOOTINGS + " INTEGER, " +
            ShootingData.COLUMN_FILENAME + " TEXT, " +
            ShootingData.COLUMN_ATHLETEID + " INTEGER, " +
            "FOREIGN KEY(" +ShootingData.COLUMN_ATHLETEID +  ") REFERENCES " + AthleteData.TABLE_NAME + "(" +
            AthleteData.COLUMN_ID + "));";



    public DBHelper(Context context){
        super(context,DATABASE_NAME,null, DATABASE_VERSION);

    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ATHLETES);
        db.execSQL(CREATE_TABLE_SHOOTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + AthleteData.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ShootingData.TABLE_NAME);

        onCreate(db);

    }



    /*
        CRUD methods for AthleteData Table
     */
    public int createAthlete(AthleteData athleteData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(AthleteData.COLUMN_FIRSTNAME,athleteData.getFirstName());
        contentValues.put(AthleteData.COLUMN_LASTNAME,athleteData.getLastName());
        contentValues.put(AthleteData.COLUMN_DATEOFBIRTH,athleteData.getDateOfBirth());
        contentValues.put(AthleteData.COLUMN_GENDER,athleteData.getGender());
        int id = (int) db.insert(AthleteData.TABLE_NAME,null,contentValues);
        db.close();
        return id;
    }

    public int getAthleteId(String firstName, String lastName){
        SQLiteDatabase db = this.getWritableDatabase();
        String getQuery = "SELECT * FROM " + AthleteData.TABLE_NAME + " WHERE " +
                AthleteData.COLUMN_FIRSTNAME + " = " + firstName + " AND " + AthleteData.COLUMN_LASTNAME +
                " = " + lastName;
        Cursor cursor = db.rawQuery(getQuery,null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        int id = cursor.getInt(cursor.getColumnIndex(AthleteData.COLUMN_ID));
        db.close();
        return id;
    }

    public AthleteData getAthleteData(long AthleteId){
        SQLiteDatabase db = this.getReadableDatabase();

        String getQuery = "SELECT * FROM " + AthleteData.TABLE_NAME + " WHERE " +
                AthleteData.COLUMN_ID +  " = " + AthleteId;
        Log.d(TAG,"get AthleteData Query: " + getQuery);
        Cursor cursor = db.rawQuery(getQuery,null);

        if (cursor != null){
            cursor.moveToFirst();
        } else{
            Log.e(TAG, "ERROR -->  Cursor empty from : " + getQuery );
        }
        AthleteData returnData = new AthleteData();
        returnData.set_id(cursor.getInt(cursor.getColumnIndex(AthleteData.COLUMN_ID)));
        returnData.setFirstName(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_FIRSTNAME)));
        returnData.setLastName(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_LASTNAME)));
        returnData.setDateOfBirth(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_DATEOFBIRTH)));
        returnData.setGender(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_GENDER)));
        db.close();
        return returnData;
    }

    public ArrayList<AthleteData> getAllAthletes() {
        ArrayList<AthleteData> list = new ArrayList<>();
        String getAllQuery = "SELECT * FROM " + AthleteData.TABLE_NAME;

        Log.d(TAG,"get all athletes Query: " + getAllQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(getAllQuery,null);

        if (cursor.moveToFirst()){
            do {
                AthleteData athlete = new AthleteData();
                athlete.set_id(cursor.getInt(cursor.getColumnIndex(AthleteData.COLUMN_ID)));
                athlete.setFirstName(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_FIRSTNAME)));
                athlete.setLastName(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_LASTNAME)));
                athlete.setDateOfBirth(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_DATEOFBIRTH)));
                athlete.setGender(cursor.getString(cursor.getColumnIndex(AthleteData.COLUMN_GENDER)));

                list.add(athlete);
            } while (cursor.moveToNext());
        }
        db.close();
        return list;
    }

    public int updateAthleteData(AthleteData athlete){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AthleteData.COLUMN_FIRSTNAME,athlete.getFirstName());
        values.put(AthleteData.COLUMN_LASTNAME,athlete.getLastName());
        values.put(AthleteData.COLUMN_DATEOFBIRTH,athlete.getDateOfBirth());
        values.put(AthleteData.COLUMN_GENDER,athlete.getGender());

        int id =  db.update(AthleteData.TABLE_NAME,values,AthleteData.COLUMN_ID + " = ?", new String[] {String.valueOf(athlete.getId())});
        db.close();
        return id;
    }

    public void deleteAthlete(long athleteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectShootingsQuery = "SELECT * FROM " + ShootingData.TABLE_NAME + " WHERE " + ShootingData.COLUMN_ATHLETEID +
                " = " + athleteId;

        //delete all shootings from athlete
        List<ShootingData> shootingdata = getAllShootingsFromAthleteID(athleteId);
        for (ShootingData data : shootingdata){
            deleteShooting(data.getId());
        }

        db.delete(AthleteData.TABLE_NAME,AthleteData.COLUMN_ID + " = ?", new String[] {String.valueOf(athleteId)});
        db.close();
    }

    public void deleteShootingFromAthlete(long athleteId){
        String selectShootingsQuery = "SELECT * FROM " + ShootingData.TABLE_NAME + " WHERE " + ShootingData.COLUMN_ATHLETEID +
                " = " + athleteId;

        SQLiteDatabase db = this.getReadableDatabase();


    }




        /*
        CRUD methods for Shootings Table
     */

    public int createShooting(ShootingData shootingData, int athletedataId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ShootingData.COLUMN_ID,shootingData.getId());
        values.put(ShootingData.COLUM_DATE,shootingData.getDate());
        values.put(ShootingData.COLUMN_NUMBER_SHOOTINGS,shootingData.getNumberOfShootings());
        values.put(ShootingData.COLUMN_FILENAME,shootingData.getFilename());
        values.put(ShootingData.COLUMN_ATHLETEID,athletedataId);
        int id = (int) db.insert(ShootingData.TABLE_NAME,null,values);
        db.close();
        return id;
    }

    public ShootingData getShootingData(long id){
        SQLiteDatabase db = this.getReadableDatabase();
        String getShooting = "SELECT * FROM " + ShootingData.TABLE_NAME + " WHERE " + ShootingData.COLUMN_ID
                + " = " + id;

        Log.d(TAG, "get shooting data query : " + getShooting);
        Cursor cursor = db.rawQuery(getShooting,null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        else{
            Log.e(TAG, "ERROR -->  Cursor empty from : " + getShooting );
        }

        ShootingData shootingData = new ShootingData();
        shootingData.set_id(cursor.getInt(cursor.getColumnIndex(ShootingData.COLUMN_ID)));
        shootingData.setDate(cursor.getString(cursor.getColumnIndex(ShootingData.COLUM_DATE)));
        shootingData.setNumberOfShootings(cursor.getInt(cursor.getColumnIndex(ShootingData.COLUMN_NUMBER_SHOOTINGS)));
        shootingData.setFilename(cursor.getString(cursor.getColumnIndex(ShootingData.COLUMN_FILENAME)));
        db.close();
        return shootingData;
    }

    public ArrayList<ShootingData> getAllShootings(){
        ArrayList<ShootingData> shootings = new ArrayList<>();
        String getAllQuery = "SELECT * FROM " + ShootingData.TABLE_NAME;

        Log.d(TAG,"get all shootings data query: " + getAllQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(getAllQuery,null);

        if (cursor.moveToFirst()){
            do {
                ShootingData data = new ShootingData();
                data.set_id(cursor.getInt(cursor.getColumnIndex(ShootingData.COLUMN_ID)));
                data.setDate(cursor.getString(cursor.getColumnIndex(ShootingData.COLUM_DATE)));
                data.setFilename(cursor.getString(cursor.getColumnIndex(ShootingData.COLUMN_FILENAME)));
                data.setNumberOfShootings(cursor.getInt(cursor.getColumnIndex(ShootingData.COLUMN_NUMBER_SHOOTINGS)));

                shootings.add(data);
            } while(cursor.moveToNext());
        }
        db.close();
        return shootings;
    }

    public ArrayList<ShootingData> getAllShootingsFromAthleteID(long AthleteId){
        ArrayList<ShootingData> shootingDataList = new ArrayList<>();
        String selectAllFromOneQuery = "SELECT * FROM " + ShootingData.TABLE_NAME + " WHERE " +
                ShootingData.COLUMN_ATHLETEID + " = " + AthleteId;

        Log.d(TAG,"Select All Shootings From Athlete Query : " + selectAllFromOneQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectAllFromOneQuery,null);

        if (cursor.moveToFirst()){
            do {
                ShootingData sd = new ShootingData();
                sd.set_id(cursor.getInt(cursor.getColumnIndex(ShootingData.COLUMN_ID)));
                sd.setDate(cursor.getString(cursor.getColumnIndex(ShootingData.COLUM_DATE)));
                sd.setFilename(cursor.getString(cursor.getColumnIndex(ShootingData.COLUMN_FILENAME)));
                sd.setNumberOfShootings(cursor.getInt(cursor.getColumnIndex(ShootingData.COLUMN_NUMBER_SHOOTINGS)));

                shootingDataList.add(sd);
            }while (cursor.moveToNext());
        }
        db.close();
        return shootingDataList;
    }

    public void deleteShooting(long shooting_id){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(ShootingData.TABLE_NAME, ShootingData.COLUMN_ID + " = ?" ,new String[]{String.valueOf(shooting_id)});
        db.close();
    }


    //PrintTables

    public void printAthletesTable(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + AthleteData.TABLE_NAME,null);
        String tableString = String.format("Table %s:\n",AthleteData.TABLE_NAME);

        while (cursor.moveToNext()){
            String[] columnNames = cursor.getColumnNames();
            for (String name : columnNames){
                //Log.d(TAG,"Column : " + name + "    Data: " + cursor.getString(cursor.getColumnIndex(name)));
                tableString += String.format("%s: %s\n", name, cursor.getString(cursor.getColumnIndex(name)));
            }
            tableString += "\n";
        }
        db.close();
        Log.d(TAG,tableString);
    }

    public void printShootingDataTable(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + ShootingData.TABLE_NAME,null);
        String tableString = String.format("Table %s:\n",ShootingData.TABLE_NAME);

        while (cursor.moveToNext()){
            String[] columnNames = cursor.getColumnNames();
            for (String name : columnNames){
                //Log.d(TAG,"Column : " + name + "    Data: " + cursor.getString(cursor.getColumnIndex(name)));
                tableString += String.format("%s: %s\n", name, cursor.getString(cursor.getColumnIndex(name)));
            }
            tableString += "\n";
        }
        db.close();
        Log.d(TAG,tableString);
    }

}
