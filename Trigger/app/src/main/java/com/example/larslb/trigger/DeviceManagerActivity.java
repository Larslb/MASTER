package com.example.larslb.trigger;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.widget.ViewFlipper;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.R.attr.max;
import static android.R.attr.shareInterpolator;


public class DeviceManagerActivity extends AppCompatActivity {
    private final static String TAG = DeviceManagerActivity.class.getSimpleName();
    String mDeviceName;
    private String mDeviceAddress;
    private String mAthleteFirstName;
    private String mAthleteLastName;
    private int mAthleteId;
    private boolean mstore;
    private boolean mNotify;
    private TextView mConnectionState;
    private TextView mDeviceText;

    private int mCounter;
    private ArrayList<String> mServices;
    private ArrayAdapter<String> mArrayAdapter;
    private boolean mBound;
    private BLEConnectService mBLEConnectService;
    private ArrayList<BluetoothGattCharacteristic> mNotifyCharacteristics =
            new ArrayList<>();
    private ArrayList<BluetoothGattCharacteristic> mGattCharacteristics =
            new ArrayList<BluetoothGattCharacteristic>();
    ArrayList<HashMap<String, String>> mGattServiceData;

    private final ConcurrentLinkedQueue<HashMap<String,ArrayList<Integer>>> mQueue = new ConcurrentLinkedQueue<>();

    ArrayList<Integer> mForceData;
    ArrayList<Integer> mGyroData;
    ArrayList<Integer> mAccData;
    ArrayList<Integer> mForceTimeData;
    ArrayList<Integer> mGyroTimeData;
    ArrayList<Integer> mAccTimeData;

    private int shotCounter;
    private LineChart mForceChart;
    private LineChart mAccChart;
    private LineChart mGyroChart;


    private ViewFlipper mDeviceFlipper;
    private PlottingThread pThread;



    private static final int SLEEP_RATE = 5;
    private static final int FORCE = 0;
    private static final int ACCELEROMETER = 1;
    private static final int GYROSCOPE = 2;
    private static final int TIME = 3;
    private static final int SINGLEACC = 4;
    private static final int SINGLEGYRO = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_manager);
        mCounter = 0;


        mstore = true;
        mNotify = false;

        mServices = new ArrayList<>();


        shotCounter = 0;

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceScanActivity.EXTRA_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceScanActivity.EXTRA_ADDRESS);
        mAthleteFirstName = intent.getStringExtra(NewExerciseActivity.FIRST_NAME);
        mAthleteLastName = intent.getStringExtra(NewExerciseActivity.LAST_NAME);
        mAthleteId = intent.getIntExtra(NewExerciseActivity.ATHLETE_ID,0);

        //mServiceName = (TextView) findViewById(R.id.service_name);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDeviceText = (TextView) findViewById(R.id.serviceText);
        mDeviceText.setText(mDeviceName);

        //mAccChart = (LineChart) findViewById(R.id.accgraph);
        //mGyroChart = (LineChart) findViewById(R.id.gyrograph);


        //initGraph(mAccChart,true,5000f,0f);
        //initGraph(mGyroChart,true,5000f,0f);


    }
    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG,"OnPause");

        if (pThread != null){
            pThread.interrupt();
        }

        mNotify = false;
        notifyAllCharacteristics(false);


    }
    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"OnResume!");
        initGraph(mForceChart,false, 300f,-10f, "Force Measurement");
        pThread = new PlottingThread("Plotting Thread");
        pThread.start();

    }

    private final BroadcastReceiver mGattUpdateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BLEConnectService.ACTION_GATT_CONNECTED.equals(action)){
                updateConnection(R.string.connected);

            }else if(BLEConnectService.ACTION_GATT_DISCONNECTED.equals(action)){
                updateConnection(R.string.disconnected);

            }
            else if(BLEConnectService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){

                lookupServices( mBLEConnectService.getSupportedGattService());
            }
            else if(BLEConnectService.ACTION_DATA_AVAILABLE.equals(action)){
                String uuid = intent.getStringExtra(BLEConnectService.CHARACTERISTIC);
                byte[] data = intent.getByteArrayExtra(BLEConnectService.SERVICE_DATA);

                dataHandler(uuid,data);

            }
        }

    };

    public void notifyAllCharacteristics(boolean enable){
        for (BluetoothGattCharacteristic characteristic : mNotifyCharacteristics){
            Log.d(TAG, "characteristic to be notified:  " + DeviceServices.lookup(characteristic.getUuid().toString(),"unknown"));
            mBLEConnectService.setCharacteristicNotification(characteristic,enable);
        }
    }

    public void startGraphingActivity(){

        Intent graphingintent = new Intent(this,GraphingActivity.class);
        graphingintent.putExtra(NewExerciseActivity.FIRST_NAME,mAthleteFirstName);
        graphingintent.putExtra(NewExerciseActivity.LAST_NAME,mAthleteLastName);
        graphingintent.putExtra(NewExerciseActivity.ATHLETE_ID,mAthleteId);
        graphingintent.putExtra(GraphingActivity.ENABLE_SAVE,true);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.FORCE_TEXT,mForceData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.FORCE_TIME_TEXT,mForceTimeData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.ACCELEROMETER_TEXT,mAccData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.ACC_TIME_TEXT,mAccTimeData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.GYROSCOPE_TEXT,mGyroData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.GYRO_TIME_TEXT,mGyroTimeData);
        Log.d(TAG, "Lengths : ------------------- \n");
        Log.d(TAG, "Force: " + mForceData.size() + " \nForceTime: " + mForceTimeData.size()
        + "\nAcc: " + mAccData.size() +  "     \nAccTime: " + mAccTimeData.size() +
        "       \nGyro: " + mGyroData.size() + "      \nGyroTime:   " + mGyroTimeData.size());

        startActivity(graphingintent);
    }

    /*
    public ArrayList<Integer> createTimeSet(){
        Log.d(TAG,"Time Force Size:  " + mForceTimeData.size());
        Log.d(TAG,"Time Acc Size:  " + mAccTimeData.size());
        Log.d(TAG,"Time Gyro Size:  " + mGyroTimeData.size());
        ArrayList<Integer> time;
        if ((mForceTimeData.size() < mAccTimeData.size()) && mForceTimeData.size() < mGyroTimeData.size()){
            time = mForceTimeData;
            shrinkData(time.size(),ACCELEROMETER);
            shrinkData(time.size(),GYROSCOPE);
        }
        else if (mAccTimeData.size() < mGyroTimeData.size()){
            time = mAccTimeData;
            shrinkData(time.size(),FORCE);
            shrinkData(time.size(),GYROSCOPE);
        }
        else{
            time=mGyroTimeData;
            shrinkData(time.size(),FORCE);
            shrinkData(time.size(),ACCELEROMETER);
        }
        return time;
    }

    public void shrinkData(int TimeSize,int dataName){


        Log.d(TAG,"Data to be shrinked (0 Force, 1 Acc, 2 Gyro) : " + dataName);
        switch (dataName){
            case FORCE:
                int forceIndexSize = mForceData.size() - 1;
                Log.d(TAG,"ForceIndexSize:      " + forceIndexSize + "  TimeSize:    " + TimeSize);
                for (int i=forceIndexSize;i<TimeSize;i--){
                    mForceData.remove(i);
                }
                break;
            case ACCELEROMETER:
                int accIndexSize = mAccData.size() - 1;

                Log.d(TAG,"AccIndexSize:      " + accIndexSize + "  TimeSize:    " + TimeSize);
                for (int i=accIndexSize;i<TimeSize;i--){
                    mAccData.remove(i);
                }
                break;
            case GYROSCOPE:
                int gyroIndexSize = mGyroData.size() - 1;

                Log.d(TAG,"GyroIndexSize:      " + gyroIndexSize + "  TimeSize:    " + TimeSize);
                for (int i=gyroIndexSize;i<TimeSize;i--){
                    mGyroData.remove(i);
                }
        }

    }
*/

    public void initGraph(LineChart lineChart,boolean threeAxis, float Ymax, float Ymin, String name){
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(true);
        lineChart.setBackgroundColor(Color.rgb(102,209,255));
        Description description = new Description();
        description.setText(name);
        lineChart.setDescription(description);

        List<ILineDataSet> datasets = new ArrayList<>();
        if (threeAxis){
            LineDataSet xSet = createSet("X-Axis");
            xSet.setColor(Color.BLUE);
            LineDataSet ySet = createSet("Y-Axis");
            ySet.setColor(Color.RED);
            LineDataSet zSet = createSet("Z-Axis");
            zSet.setColor(Color.GREEN);
            datasets.add(xSet);
            datasets.add(ySet);
            datasets.add(zSet);
        }else{
            LineDataSet set = createSet("Force");
            set.setDrawCircles(false);
            set.setDrawValues(false);
            datasets.add(set);
        }
        LineData lineData = new LineData(datasets);
        lineData.setValueTextColor(Color.rgb(0,89,255));
        lineChart.setData(lineData);


        Legend l = lineChart.getLegend();
        l.setEnabled(false);

        //l.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(Typeface.DEFAULT);
        //l.setTextColor(Color.WHITE);

        XAxis xl = lineChart.getXAxis();
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(Ymax);
        leftAxis.setAxisMinimum(Ymin);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);


    }




    private LineDataSet createSet(String name) {

        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setDrawFilled(true);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        return set;
    }





    public void lookupCharacteristics(){
        if (mGattCharacteristics != null) {
            //final ArrayList<BluetoothGattCharacteristic> characteristics = mGattCharacteristics.get(position);
            mNotifyCharacteristics = new ArrayList<>();
            for (BluetoothGattCharacteristic characteristic : mGattCharacteristics) {
                final int charaProp = characteristic.getProperties();
                Log.d(TAG,"CHARACTERISTIC: " + DeviceServices.lookup(characteristic.getUuid().toString(),"unknown"));
                Log.d(TAG, "Properties: " + charaProp);


                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristics.add(characteristic);
                }
            }
            Log.d(TAG,"Notify bool:     " + mNotify);
            notifyAllCharacteristics(true);
            mNotify = true;

        }
    }


    public boolean addToQueue(HashMap<String,ArrayList<Integer>> data){
        if (data == null){
            return false;
        }
        mQueue.add(data);
        return true;
    }


    private void updateConnection(final int Id) {
        mConnectionState.setText(Id);
    }



    private void lookupServices(List<BluetoothGattService> supportedGattService) {
        if (supportedGattService == null) return;

        String unkownName = "Unkown service";
        mGattCharacteristics = new ArrayList<>();

        for (BluetoothGattService gattService : supportedGattService){

            if (gattService.getUuid().toString().equals(DeviceServices.OUR_SERVICE)){
                mServices.add(DeviceServices.lookup(gattService.getUuid().toString(),unkownName));

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                    mGattCharacteristics.add(characteristic);


                }
            }
        }
        lookupCharacteristics();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBLEConnectService = ((BLEConnectService.LocalBinder) service).getService();
            if (!mBLEConnectService.init()){
                Log.e(TAG,"Init not complete");
            }
            mBound = true;
            mBLEConnectService.connect(mDeviceAddress);
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBLEConnectService = null;
            mBound = false;
        }
    };



    public void dataHandler(String id, byte[] data){
        HashMap<String,ArrayList<Integer>> compactQueueData;
        byte[] measurement;
        if (data!=null){
            if (DeviceServices.lookup(id,"unkown").equals(DeviceServices.attributes.get(DeviceServices.FORCE_ATTRIBUTE))){
                measurement = Arrays.copyOfRange(data,0,18);
                compactQueueData = forceBytearray2intarray(measurement);
                if (!addToQueue(compactQueueData)){
                    Log.d(TAG,"Unable to add data To Queue");
                }

            }else if (DeviceServices.lookup(id,"unkown").equals(DeviceServices.attributes.get(DeviceServices.ACC_ATTRIBUTE))){
                measurement = Arrays.copyOfRange(data,0,14);
                //Log.d(TAG,"ACC Raw DaTA:            ---            " +  Arrays.copyOfRange(data,0,14).toString());
                ArrayList<Integer> array = AccBytearray2intarray(measurement);

            }else if (DeviceServices.lookup(id,"unkown").equals(DeviceServices.attributes.get(DeviceServices.GYRO_ATTRIBUTE))){
                measurement = Arrays.copyOfRange(data,0,14);
                //Log.d(TAG,"Gyro Raw DaTA:            ---            " +  Arrays.copyOfRange(data,0,14).toString());
                ArrayList<Integer> array = gyroBytearray2intarray(measurement);

            }

        }
    }
    public HashMap<String,ArrayList<Integer>> forceBytearray2intarray(byte[] barray){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> intarray = new ArrayList<>();
        ArrayList<Integer> timeintArray = new ArrayList<>();
        HashMap<String,ArrayList<Integer>> res = new HashMap<>();
        for (int i=0;i<=barray.length-1;i+=3){
            /*
            Log.d(TAG,"Signed Time byte:        ---      " + (barray[i] & 0xFF));
            mTimeData.add((barray[i] & 0xFF));
            Log.d(TAG,"Force uint16 byte:       ---         "+ convertToUint16((barray[i+1] & 0xFF), (barray[i+2] & 0xFF)));
            */
            /*
            stringBuilder.append(convertToUint16((barray[i+1] & 0xFF), (barray[i+2] & 0xFF)));
            stringBuilder.append(",");
            */
            Integer delta_t = (barray[i] & 0xFF);
            timeintArray.add(delta_t);
            mForceTimeData.add(delta_t*mForceTimeData.size());
            Integer convertedInt =convertToUint16((barray[i+1] & 0xFF), (barray[i+2] & 0xFF));
            mForceData.add(convertedInt);
            intarray.add(convertedInt);
        }

        res.put(GraphingActivity.FORCE_TEXT,intarray);
        res.put(GraphingActivity.FORCE_TIME_TEXT,timeintArray);
        return res;
    }

    public ArrayList<Integer> AccBytearray2intarray(byte[] barray){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> intarray = new ArrayList<>();
        for (int i=0;i<=barray.length-1;i+=7) {

            /*Log.d(TAG, "x uint16 byte:       ---         " + convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF)));
            Log.d(TAG, "y uint16 byte:       ---         " + convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF)));
            Log.d(TAG, "z uint16 byte:       ---         " + convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF)));
            */
            Integer Time = barray[i] & 0xFF;
            Integer convertedX = convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF));
            Integer convertedY = convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF));
            Integer convertedZ = convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF));
            intarray.add(convertedX);
            intarray.add(convertedY);
            intarray.add(convertedZ);
            mAccTimeData.add(Time * mAccTimeData.size());
            mAccData.add(convertedX);
            mAccData.add(convertedY);
            mAccData.add(convertedZ);

            /*
            stringBuilder.append(convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF)));
            stringBuilder.append(",");
            stringBuilder.append(convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF)));
            stringBuilder.append(",");
            stringBuilder.append(convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF)));
            stringBuilder.append(",");
            */


        }
        return intarray;
    }

    public ArrayList<Integer> gyroBytearray2intarray(byte[] barray){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> intarray = new ArrayList<>();
        for (int i=0;i<=barray.length-1;i+=7) {

            /*Log.d(TAG, "x uint16 byte:       ---         " + convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF)));
            Log.d(TAG, "y uint16 byte:       ---         " + convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF)));
            Log.d(TAG, "z uint16 byte:       ---         " + convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF)));
            */
            Integer Time = barray[i] & 0xFF;
            Integer convertedX = convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF));
            Integer convertedY = convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF));
            Integer convertedZ = convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF));
            intarray.add(convertedX);
            intarray.add(convertedY);
            intarray.add(convertedZ);
            mGyroTimeData.add(Time * mGyroTimeData.size());
            mGyroData.add(convertedX);
            mGyroData.add(convertedY);
            mGyroData.add(convertedZ);

            /*
            stringBuilder.append(convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF)));
            stringBuilder.append(",");
            stringBuilder.append(convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF)));
            stringBuilder.append(",");
            stringBuilder.append(convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF)));
            stringBuilder.append(",");
            */


        }
        return intarray;
    }

    public int convertToUint16(int dataFirst, int dataSecond){
        int result = dataFirst << 8;
        result += dataSecond;

        if ((result & (1L << 15)) != 0){
            result = result & ~(1 << 15);
            return result-32768;

        } else{

            return result;
        }

    }


    @Override
    protected void onStart(){
        Log.d(TAG,"OnStart");
        super.onStart();
        Intent intent = new Intent(this, BLEConnectService.class);
        bindService(intent,mServiceConnection,BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReciever, makeGattUpdateIntentFilter());


        mForceChart = (LineChart) findViewById(R.id.forcegraph);
        mForceData = new ArrayList<>();
        mGyroData = new ArrayList<>();
        mAccData = new ArrayList<>();
        mForceTimeData = new ArrayList<>();
        mGyroTimeData = new ArrayList<>();
        mAccTimeData = new ArrayList<>();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mBLEConnectService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEConnectService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEConnectService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEConnectService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEConnectService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
        Log.d(TAG,"BOUNDED: " + mBound);
        if (mBound){
            unbindService(mServiceConnection);
            mBound = false;

        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGattUpdateReciever);


        mNotify = false;
        mForceData = null;
        mAccData = null;
        mGyroData = null;
        mForceTimeData = null;
        mGyroTimeData = null;
        mAccTimeData = null;
        mForceChart = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.devicemenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.Connect:
                mBLEConnectService.connect(mDeviceAddress);
                updateConnection(R.string.connected);
                break;
            case R.id.Disconnect:
                mBLEConnectService.disconnect();
                updateConnection(R.string.disconnected);
                break;

            case R.id.Done:
                pThread.interrupt();
                startGraphingActivity();
                break;

        }
        return true;
    }




    public void addEntry(ArrayList<Integer> plotData, ArrayList<Integer> timePlotData){
        LineData data = mForceChart.getData();
        int timestamp = 0;
        if (data != null){
            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet("Force");
                data.addDataSet(set);
            }
            for (int i =0;i<plotData.size()-1;i++){
                data.addEntry(new Entry(data.getEntryCount(),plotData.get(i)), 0);
            }


            data.notifyDataChanged();
            // let the chart know it's data has changed

            mForceChart.notifyDataSetChanged();

            // limit the number of visible entries
            mForceChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);


            // move to the latest entry
            mForceChart.moveViewToX(data.getEntryCount());


            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }

    }




    class PlottingThread extends Thread {
        private Thread t;
        private String threadName;
        private int mode;
        private HashMap<String,ArrayList<Integer>> order = new HashMap<>();
        PlottingThread(String name){
            threadName = name;
        }

        public void run() {
            while (true) {
                if(!mNotify){
                    continue;
                }
                if ((order = mQueue.poll()) != null) {
                    Log.d(TAG,"Values to be plotted:     " + order);
                    addEntry(order.get(GraphingActivity.FORCE_TEXT),order.get(GraphingActivity.FORCE_TIME_TEXT));
                }

                try {
                    Thread.sleep(SLEEP_RATE);
                }catch (InterruptedException e){
                    Log.d(TAG,"Interrupt Exception");
                    break;
                }
            }
        }
        public void start() {
            if (t == null){
                t = new Thread(this,threadName);
                t.start();
            }
        }
    }
}
