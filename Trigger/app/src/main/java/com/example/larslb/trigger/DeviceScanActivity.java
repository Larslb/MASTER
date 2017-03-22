package com.example.larslb.trigger;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.MenuItemHoverListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.R.id.list;
import static android.content.Context.BLUETOOTH_SERVICE;


public class DeviceScanActivity extends ListActivity {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();

    private BluetoothAdapter mBLEAdapter;
    private BluetoothLeScanner mBLEScanner;
    private String athleteFirstName;
    private String athleteLastName;
    private ScanSettings mSettings;
    private Handler mHandler;
    private String mdevice;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    ListView mListView;
    ArrayAdapter<String> mArrayAdapter;
    private BLEConnectService mBLEConnectService;

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final static int DEVICE_TYPE_LE = 2;
    TextView mScanningText;

    final static String EXTRA_NAME = "EXTRA_NAME";

    final static String EXTRA_ADDRESS = "EXTRA_ADDRESS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        Intent intent = getIntent();
        athleteFirstName = intent.getStringExtra(NewExerciseActivity.FIRST_NAME);
        athleteLastName = intent.getStringExtra(NewExerciseActivity.LAST_NAME);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(this, "BLE not supported by device",Toast.LENGTH_SHORT).show();
            finish();
        }


        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBLEAdapter = bluetoothManager.getAdapter();
        mBLEScanner = mBLEAdapter.getBluetoothLeScanner();


        mScanningText = (TextView) findViewById(R.id.scanningText);
        mListView = (ListView) findViewById(android.R.id.list);
        mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,mDeviceList);
        mListView.setAdapter(mArrayAdapter);

        if (mBLEAdapter == null && !mBLEAdapter.isEnabled()){
            Log.d(TAG,"Bluetooth Adapter not Initialized");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent,REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This application needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons");
                builder.setPositiveButton(android.R.string.ok,null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG,"coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int pos, long id){
        mdevice = mDeviceList.get(pos);
        registerForContextMenu(view);
        String[] deviceDetails = mdevice.split("\n");
        startDeviceManager(deviceDetails[0],deviceDetails[1]);

    }


    @Override
    public void onResume(){
        super.onResume();
        mSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        Scanning(true);
        mScanningText.setText(R.string.scanning_name);
        mArrayAdapter.notifyDataSetChanged();

    }

    @Override
    public void onPause(){
        super.onPause();
        Scanning(false);



    }
    private BluetoothAdapter.LeScanCallback adapterLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(device.getName(), device.getAddress());
                    mArrayAdapter.notifyDataSetChanged();

                }
            });
        }
    };


    private ScanCallback scannerLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device.getType()==BluetoothDevice.DEVICE_TYPE_LE) {
                Log.d(TAG,"DeviceList: " + mDeviceList);
                addDevice(device.getName(),device.getAddress());
            }


        }
    };

    public void addDevice(String deviceName, String deviceAddress){
        String newDevice = deviceName + "\n" + deviceAddress;
        if (!mDeviceList.contains(newDevice)){
            mDeviceList.add(newDevice);
            mArrayAdapter.notifyDataSetChanged();
        }
    }


    public void startDeviceManager(String name, String address){
        Intent intent = new Intent(this, DeviceManagerActivity.class);
        intent.putExtra(NewExerciseActivity.FIRST_NAME,athleteFirstName);
        intent.putExtra(NewExerciseActivity.LAST_NAME,athleteLastName);
        intent.putExtra(EXTRA_NAME,name);
        intent.putExtra(EXTRA_ADDRESS,address);
        if (Build.VERSION.SDK_INT < 21){
            mBLEAdapter.stopLeScan(adapterLeScanCallback);
        }else{
            mBLEScanner.stopScan(scannerLeScanCallback);
        }
        startActivity(intent);


    }

    public void Scanning(boolean scan){
        if (scan) {
            if (Build.VERSION.SDK_INT < 21) {
                mBLEAdapter.startLeScan(adapterLeScanCallback);

            } else {
                mBLEScanner.startScan(scannerLeScanCallback);
            }
        }
        else{
            if (Build.VERSION.SDK_INT < 21){
                mBLEAdapter.stopLeScan(adapterLeScanCallback);
            }else{
                mBLEScanner.stopScan(scannerLeScanCallback);
            }
        }
    }


}
