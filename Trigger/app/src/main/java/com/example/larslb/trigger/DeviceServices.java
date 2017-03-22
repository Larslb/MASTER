package com.example.larslb.trigger;

import android.util.Log;

import java.util.HashMap;


/**
 * Created by larslb on 27.01.2017.
 */

public class DeviceServices {

    private static final String TAG = DeviceServices.class.getSimpleName();
    public static HashMap<String,String> attributes = new HashMap<>();

    public static String OUR_SERVICE = "0000f00d-1212-efde-1523-785fef13d123";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String FORCE_ATTRIBUTE = "00001337-1212-efde-1523-785fef13d123";
    public static String ACC_ATTRIBUTE = "00001338-1212-efde-1523-785fef13d123";
    public static String GYRO_ATTRIBUTE = "00001339-1212-efde-1523-785fef13d123";
    public static String OUR_SERVICE_NAME = "Trigger Sensor Service";


    static {
        attributes.put(OUR_SERVICE,OUR_SERVICE_NAME);
        attributes.put(FORCE_ATTRIBUTE,"Force Sensor Measurement");
        attributes.put(ACC_ATTRIBUTE, "Accelerometer Measurement");
        attributes.put(GYRO_ATTRIBUTE,"Gyroscope Measurement");

    }


    public static String lookup(String uuid,String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
