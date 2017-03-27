package com.example.larslb.trigger;

import java.util.Date;

/**
 * Created by larslb on 23.03.2017.
 */

public class ShootingData {

    int _id;
    String date;
    int numberOfShootings;
    String filename;

    public static final String TABLE_NAME = "shootings_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUM_DATE = "date";
    public static final String COLUMN_NUMBER_SHOOTINGS = "number_of_shootings";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_ATHLETEID = "";

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

}
