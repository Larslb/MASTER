package com.example.larslb.trigger;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;



public class BLEConnectService extends Service {
    private static final String TAG = BLEConnectService.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mGatt;
    private final IBinder mBinder = new LocalBinder();
    private int mConnected = STATE_DISCONNECTED;
    private String mDeviceAddress;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String SERVICE_DATA =
            "SERVICE_DATA";
    public final static String CHARACTERISTIC = "CHARACTERISTIC";



    public BLEConnectService() {
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    final Intent connectIntent = new Intent(ACTION_GATT_CONNECTED);
                    broadCastUpdate(connectIntent);
                    mConnected = STATE_CONNECTED;
                    mGatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    final Intent disconnectIntent = new Intent(ACTION_GATT_DISCONNECTED);
                    broadCastUpdate(disconnectIntent);
                    mConnected = STATE_DISCONNECTED;
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            if (status == BluetoothGatt.GATT_SUCCESS){
                final Intent intent = new Intent(ACTION_GATT_SERVICES_DISCOVERED);
                broadCastUpdate(intent);
            }else {
                Log.i(TAG,"onServicesDiscovered recieved: " + status);
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            broadCastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
        }
    };

    public void broadCastUpdate(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    public void broadCastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        if(data != null && data.length >0){

            intent.putExtra(CHARACTERISTIC, characteristic.getUuid().toString());
            intent.putExtra(SERVICE_DATA,data);

        }else{
            Log.e(TAG, "ERROR:          -           data == null!!");
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }




    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        BLEConnectService getService() {
            return BLEConnectService.this;
        }
    }
    public boolean init(){
        if (mBluetoothManager == null){
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null){
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        return !(mBluetoothAdapter == null);


    }

    public boolean connect(final String address){
        Log.d(TAG, "BluetoothAdapter: " + mBluetoothAdapter.getName());
        Log.d(TAG, "Adress: " + address);
        if (mBluetoothAdapter == null || address == null){
            Log.e(TAG,"Adapter or address null");
            return false;
        }
        if (mDeviceAddress != null && address.equals(mDeviceAddress)){
            if (mGatt.connect()){
                mConnected = STATE_CONNECTED;
                return true;
            }else{
                Log.e(TAG,"mGatt not able to connect to adress");
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null){
            return false;
        }
        mGatt = device.connectGatt(this,false,mGattCallback);
        mDeviceAddress = address;
        mConnected = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mGatt == null){
            Log.w(TAG,"BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG,"Disconnect");
        mGatt.disconnect();
    }



    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if (mBluetoothAdapter == null || mGatt == null){
            return;
        }
        mGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled){
        if (mBluetoothAdapter == null || mGatt == null){
            Log.w(TAG, "BluetoothAdapter not Initialized");
            return;
        }
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(DeviceServices.CLIENT_CHARACTERISTIC_CONFIG));
        mGatt.setCharacteristicNotification(characteristic,enabled);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                while(!success){
                    if (mGatt.writeDescriptor(descriptor))
                        success = true;
                }
            }
        }).start();


    }

    public List<BluetoothGattService> getSupportedGattService() {
        if (mGatt == null) return null;
        return mGatt.getServices();
    }


}
