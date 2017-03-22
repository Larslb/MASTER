package com.example.larslb.trigger;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by larslb on 15.02.2017.
 */
public class AthleteData {

    public AthleteData(){

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


