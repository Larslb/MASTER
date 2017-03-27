package com.example.larslb.trigger;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by larslb on 15.02.2017.
 */
public class AthleteData {
    int _id;
    String firstName;
    String lastName;
    String dateOfBirth;
    String gender;

    public static final String TABLE_NAME = "athlete_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIRSTNAME = "first_name";
    public static final String COLUMN_LASTNAME = "last_name";
    public static final String COLUMN_DATEOFBIRTH = "date_of_birth";
    public static final String COLUMN_GENDER = "gender";


    public AthleteData(){

    }
    public AthleteData(String firstName,String lastName, String dob, String gender){
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dob;
        this.gender = gender;
    }

    public AthleteData(int id, String firstName,String lastName){
        this._id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }


    //setters

    public void set_id(int id){
        this._id = id;

    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public void setDateOfBirth(String dob){
        this.dateOfBirth = dob;
    }
    public void setGender(String g) {
        this.gender = g;
    }


    //getters

    public long getId(){
        return this._id;
    }
    public String getFullName(){
        return this.firstName + this.lastName;
    }
    public String getFirstName(){
        return this.firstName ;
    }
    public String getLastName(){
        return this.lastName;
    }
    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public String getGender() {
        return this.gender;
    }

    public static class AthleteDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "athletes";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SURNAME = "sur_name";
        public static final String FULL_NAME = "full_name";
        public static final String FORCE = "force";
        public static final String FORCETIME = "forcetime";
        public static final String ACCGYROTIME = "accgyrotime";
        public static final String ACCELORMETER = "accelerometer";
        public static final String GYROSCOPE = "gyroscope";
    }


}


