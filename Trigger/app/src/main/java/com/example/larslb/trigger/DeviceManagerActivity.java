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

import java.util.ArrayList;
import java.util.Arrays;
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

import java.util.concurrent.ConcurrentLinkedQueue;

import static android.R.attr.max;


public class DeviceManagerActivity extends AppCompatActivity {
    private final static String TAG = DeviceManagerActivity.class.getSimpleName();
    private String mDeviceName;
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

    private final ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<>();

    ArrayList<Integer> mForceData;
    ArrayList<Integer> mGyroData;
    ArrayList<Integer> mAccData;
    ArrayList<Integer> mForceTimeData;
    ArrayList<Integer> mGyroAccTimeData;

    private int shotCounter;
    private LineChart mForceChart;
    private LineChart mAccChart;
    private LineChart mGyroChart;


    private ViewFlipper mDeviceFlipper;
    private PlottingThread pThread;



    private static final int FORCE = 0;
    private static final int ACCELEROMETER = 1;
    private static final int GYROSCOPE = 2;
    private static final int SINGLEFORCE = 3;
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

                storeData(uuid,data);

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
        graphingintent.putIntegerArrayListExtra(GraphingActivity.ACCELEROMETER_TEXT,mAccData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.GYROSCOPE_TEXT,mGyroData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.FORCETIME_TEXT,mForceTimeData);
        graphingintent.putIntegerArrayListExtra(GraphingActivity.ACCGYROTIME_TEXT,mGyroAccTimeData);
        startActivity(graphingintent);
    }


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


    public boolean addToQueue(String data){
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



    public void storeData(String id, byte[] data){
        ArrayList<String> stringArrayList = new ArrayList<>();
        String compactQueueData = "";
        String compactStorageData;
        byte[] measurement;
        if (data!=null){
            if (DeviceServices.lookup(id,"unkown").equals(DeviceServices.attributes.get(DeviceServices.FORCE_ATTRIBUTE))){
                measurement = Arrays.copyOfRange(data,0,18);
                ArrayList<Integer> array = forceBytearray2intarray(measurement);
                compactQueueData = FORCE + ":" + array.toString();
                for (int i=0;i<array.size();i++){
                    mForceData.add(array.get(i));
                }
            }else if (DeviceServices.lookup(id,"unkown").equals(DeviceServices.attributes.get(DeviceServices.ACC_ATTRIBUTE))){
                measurement = Arrays.copyOfRange(data,0,14);
                //Log.d(TAG,"ACC Raw DaTA:            ---            " +  Arrays.copyOfRange(data,0,14).toString());
                ArrayList<Integer> array = accGyroBytearray2intarray(measurement);
                compactQueueData = ACCELEROMETER + ":" + array.toString();
                for (int i=0;i<array.size();i++){
                    mAccData.add(array.get(i));
                }
            }else if (DeviceServices.lookup(id,"unkown").equals(DeviceServices.attributes.get(DeviceServices.GYRO_ATTRIBUTE))){
                measurement = Arrays.copyOfRange(data,0,14);
                //Log.d(TAG,"Gyro Raw DaTA:            ---            " +  Arrays.copyOfRange(data,0,14).toString());
                ArrayList<Integer> array = accGyroBytearray2intarray(measurement);
                compactQueueData = GYROSCOPE + ":" + array.toString();
                for (int i=0;i<array.size();i++){
                    mGyroData.add(array.get(i));
                }
            }
            if (!addToQueue(compactQueueData)){
                Log.d(TAG,"Unable to add data To Queue");
            }
        }
    }
    public ArrayList<Integer> forceBytearray2intarray(byte[] barray){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> intarray = new ArrayList<>();
        for (int i=0;i<=barray.length-1;i+=3){
            /*
            Log.d(TAG,"Signed Time byte:        ---      " + (barray[i] & 0xFF));
            mTimeData.add((barray[i] & 0xFF));
            Log.d(TAG,"Force uint16 byte:       ---         "+ convertToUint16((barray[i+1] & 0xFF), (barray[i+2] & 0xFF)));
            */
            stringBuilder.append(convertToUint16((barray[i+1] & 0xFF), (barray[i+2] & 0xFF)));
            stringBuilder.append(",");
            intarray.add(convertToUint16((barray[i+1] & 0xFF), (barray[i+2] & 0xFF)));
        }
        return intarray;
    }

    public ArrayList<Integer> accGyroBytearray2intarray(byte[] barray){
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> intarray = new ArrayList<>();
        for (int i=0;i<=barray.length-1;i+=7) {

            /*Log.d(TAG, "x uint16 byte:       ---         " + convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF)));
            Log.d(TAG, "y uint16 byte:       ---         " + convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF)));
            Log.d(TAG, "z uint16 byte:       ---         " + convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF)));
            */
            intarray.add(convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF)));
            intarray.add(convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF)));
            intarray.add(convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF)));
            stringBuilder.append(convertToUint16((barray[i + 1] & 0xFF), (barray[i + 2] & 0xFF)));
            stringBuilder.append(",");
            stringBuilder.append(convertToUint16((barray[i + 3] & 0xFF), (barray[i + 4] & 0xFF)));
            stringBuilder.append(",");
            stringBuilder.append(convertToUint16((barray[i + 5] & 0xFF), (barray[i + 6] & 0xFF)));
            stringBuilder.append(",");



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
        mGyroAccTimeData = new ArrayList<>();

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
        mGyroAccTimeData = null;
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
                createForceTimeSet();
                createGyroAccTimeSet();
                startGraphingActivity();
                break;

        }
        return true;
    }


    public void createForceTimeSet(){
        mForceTimeData = new ArrayList<>();
        for (int i =0;i<mForceData.size();i++){
            mForceTimeData.add(mForceTimeData.size() * 10);
        }
    }
    public void createGyroAccTimeSet(){
        mGyroAccTimeData = new ArrayList<>();
        int size = 0;
        if (mAccData.size() > mGyroData.size()){size = mAccData.size();}
        else {size = mGyroData.size();}
        for (int i = 0;i < size/3;i++){
            mGyroAccTimeData.add(mGyroAccTimeData.size() * 10);
        }
    }


    public void addEntry(String stringID,String plotData){
        int id = Integer.parseInt(stringID);
        if (id == FORCE){
            LineData data = mForceChart.getData();
            if (data != null){
                ILineDataSet set = data.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set == null) {
                    set = createSet("Force");
                    data.addDataSet(set);
                }
                String[] parts = plotData.split(",");
                for (String item : parts){
                    if (item.contains("[")){
                        data.addEntry(new Entry((set.getEntryCount()), Float.parseFloat(item.split("\\[")[1])),0);
                    } else if (item.contains("]")){
                        data.addEntry(new Entry(set.getEntryCount(),Float.parseFloat(item.split("\\]")[0])),0);
                    }else {
                        data.addEntry(new Entry(set.getEntryCount(), Float.parseFloat(item)), 0);
                    }
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
    }

    /*

    public void addEntry(String stringID, String plotData){
        int id = Integer.parseInt(stringID);
        LineData lineData;
        boolean multipleSets = false;
        ILineDataSet xDataSet;
        ILineDataSet yDataSet;
        ILineDataSet zDataSet;
        LineChart chart;
        switch (id){
            case FORCE:
                lineData = mForceChart.getData();
                chart = mForceChart;
                break;
            case ACCELEROMETER:
                lineData = mAccChart.getData();
                multipleSets = true;
                chart = mAccChart;
                break;
            case GYROSCOPE:
                lineData = mGyroChart.getData();
                multipleSets = true;
                chart = mGyroChart;
                break;
            default:
                lineData = null;
                chart = null;
                return;
        }

        if (lineData != null) {
            if (multipleSets){

                xDataSet = lineData.getDataSetByIndex(0);
                yDataSet = lineData.getDataSetByIndex(1);
                zDataSet = lineData.getDataSetByIndex(2);

                if (xDataSet == null || yDataSet == null || zDataSet == null ){
                    xDataSet = createSet("X-Axis");
                    yDataSet = createSet("Y-Axis");
                    zDataSet = createSet("Z-Axis");
                    lineData.addDataSet(xDataSet);
                    lineData.addDataSet(yDataSet);
                    lineData.addDataSet(zDataSet);
                }

                String[] parts = plotData.split(",");
                for (int i=0; i<parts.length-1;i+=3){
                    lineData.addEntry(new Entry(xDataSet.getEntryCount(),Float.parseFloat(parts[i])),0);
                    lineData.addEntry(new Entry(yDataSet.getEntryCount(),Float.parseFloat(parts[i+1])),1);
                    lineData.addEntry(new Entry(zDataSet.getEntryCount(),Float.parseFloat(parts[i+2])),2);
                }


            }else {
                ILineDataSet set = lineData.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set == null) {
                    set = createSet("Force");
                    lineData.addDataSet(set);
                }
                String[] parts = plotData.split(",");
                for (String item : parts){
                    lineData.addEntry(new Entry(set.getEntryCount(), Float.parseFloat(item)), 0);
                }
            }


            lineData.notifyDataChanged();
            // let the chart know it's data has changed

            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);


            // move to the latest entry
            chart.moveViewToX(lineData.getEntryCount());


            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }
    */



    class PlottingThread extends Thread {
        private Thread t;
        private String threadName;
        private int mode;
        private String order;

        PlottingThread(String name){
            threadName = name;
        }

        public void run() {
            while (true) {
                if(!mNotify){
                    continue;
                }
                if ((order = mQueue.poll()) != null) {
                    String[] parts = order.split(":");
                    addEntry(parts[0], parts[1]);
                }

                try {
                    Thread.sleep(0,10000);
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
