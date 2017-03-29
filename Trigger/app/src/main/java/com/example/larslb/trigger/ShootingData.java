package com.example.larslb.trigger;

import android.util.Log;

import java.util.Date;

/**
 * Created by larslb on 23.03.2017.
 */

public class ShootingData {
    private static final String TAG = ShootingData.class.getSimpleName();

    int _id;
    String date;
    int numberOfShootings;
    String filename;
    int athleteId;

    public static final String TABLE_NAME = "shootings_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUM_DATE = "date";
    public static final String COLUMN_NUMBER_SHOOTINGS = "number_of_shootings";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_ATHLETEID = "athlete_id";

    //constructors
    public ShootingData(){

    }

    public ShootingData(String date, String filename, int numberOfShootings){
        this.date = date;
        this.filename = filename;
        this.numberOfShootings = numberOfShootings;
    }

    //setters

    public void setDate(String date){
        this.date = date;
    }

    public void setNumberOfShootings(int number){
        this.numberOfShootings = number;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void set_id(int id){
        this._id = id;
    }

    public void setAthlete_id(int id) {this.athleteId = id;}


    //getters
    public long getId(){
        return this._id;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getDate() {
        return this.date;
    }
    public int getNumberOfShootings(){
        return this.numberOfShootings;
    }

    //print
    public void printShootingData(){
        String tableString = String.format("Table %s:\n",TABLE_NAME);
        tableString += "\n";

        tableString += "Shooting ID: " + this._id;
        tableString += "\n";
        tableString += "Shooting Date: " + this.date;
        tableString += "\n";
        tableString += "Shooting filename: "+ this.filename;
        tableString += "\n";
        tableString += "Shooting number of shootings: "+ this.numberOfShootings;
        tableString += "\n";
        tableString += "Shooting AthleteId: " + this.athleteId;

        Log.d(TAG,tableString);

    }
}
