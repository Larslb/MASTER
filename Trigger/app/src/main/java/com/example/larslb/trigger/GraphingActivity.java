package com.example.larslb.trigger;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GraphingActivity extends AppCompatActivity {
    private static final String TAG = GraphingActivity.class.getSimpleName();

    String mAthleteFirstName;
    String mAthleteSurName;
    TextView mTextAthleteFirstName;
    TextView mTextAthleteSurName;
    ArrayList<ShootingData> mShootings;

    ViewFlipper mFlipper;

    DBHelper mDBAthlete;
    ArrayList<Integer> mForceDataList;
    ArrayList<Integer> mGyroDataList;
    ArrayList<Integer> mAccDataList;
    ArrayList<Integer> mForceTime;
    ArrayList<Integer> mGyroAccTime;
    private int mAthleteDataId;
    private int mShootingCounter = 0;
    private boolean mEnableSave;

    LineChart mForceGraph;
    LineChart mAccGraph;
    LineChart mGyroGraph;

    public static final String FORCE_TEXT = "FORCE";
    public static final String ACCELEROMETER_TEXT = "ACC";
    public static final String GYROSCOPE_TEXT = "GYRO";
    public static final String FORCETIME_TEXT = "FORCETIME";
    public static final String ACCGYROTIME_TEXT = "ACCGYROTIME";
    public static final String ENABLE_SAVE = "ENABLE_SAVE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphing);
        Log.d(TAG,"onCreate!");

        mDBAthlete = new DBHelper(this);
        Intent intent = getIntent();
        mForceDataList = intent.getIntegerArrayListExtra(FORCE_TEXT);
        mAccDataList = intent.getIntegerArrayListExtra(ACCELEROMETER_TEXT);
        mGyroDataList = intent.getIntegerArrayListExtra(GYROSCOPE_TEXT);
        mAthleteDataId = intent.getIntExtra(NewExerciseActivity.ATHLETE_ID,0);
        mForceTime = intent.getIntegerArrayListExtra(FORCETIME_TEXT);
        mEnableSave = intent.getBooleanExtra(ENABLE_SAVE,true);
        mGyroAccTime = intent.getIntegerArrayListExtra(ACCGYROTIME_TEXT);
        mAthleteFirstName = intent.getStringExtra(NewExerciseActivity.FIRST_NAME);
        mAthleteSurName = intent.getStringExtra(NewExerciseActivity.LAST_NAME);
        mTextAthleteFirstName = (TextView) findViewById(R.id.athletefirstName);
        mTextAthleteFirstName.setText(mAthleteFirstName);
        mTextAthleteSurName = (TextView) findViewById(R.id.athleteLastName);
        mTextAthleteSurName.setText(mAthleteSurName);
        mShootings = mDBAthlete.getAllShootings();


        mFlipper = (ViewFlipper) findViewById(R.id.GraphFlipper);
        mForceGraph = (LineChart) findViewById(R.id.SingleForceGraph);
        mAccGraph = (LineChart) findViewById(R.id.SingleAccGraph);
        mGyroGraph = (LineChart) findViewById(R.id.SingleGyroGraph);

        CreateView(mForceGraph,300f,-10f);
        CreateView(mAccGraph, 5000f,-5000f);
        CreateView(mGyroGraph, 5000f,-5000f);
        mShootingCounter ++;

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        Log.d(TAG,"OnCreateOptionsMenu!");
        super.onCreateOptionsMenu(menu);
        if (mEnableSave) getMenuInflater().inflate(R.menu.plottingmenu,menu);
        else getMenuInflater().inflate(R.menu.plottingmenu_without_save,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Force:
                mFlipper.setDisplayedChild(1);
                createSingleForceView();
                break;
            case R.id.ACC:
                mFlipper.setDisplayedChild(2);
                createSingleAccView();
                break;
            case R.id.Gyro:
                mFlipper.setDisplayedChild(3);
                createSingleGyroView();
                break;

            case R.id.Save:
                mFlipper.setDisplayedChild(0);
                saveToDataBase();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public List<List<Integer>> convertToXYZData(ArrayList<Integer> data){
        List<Integer> Xdata = new ArrayList<>();
        List<Integer> Ydata = new ArrayList<>();
        List<Integer> Zdata = new ArrayList<>();
        for (int i =0;i<data.size()-1;i+=3){
                Xdata.add(data.get(i));
                Ydata.add(data.get(i + 1));
                Zdata.add(data.get(i + 2));
        }
        List<List<Integer>> convertedData = new ArrayList<>();
        convertedData.add(Xdata);
        convertedData.add(Ydata);
        convertedData.add(Zdata);
        return convertedData;
    }

    public void createSingleForceView(){
        List<Entry> forceData = new ArrayList<>();
        List<Float> measurementData = new ArrayList<>();
        Log.d(TAG,"ForceMeasurement: " + mForceDataList);
        Log.d(TAG,"ACC Measurement: " + mAccDataList);
        Log.d(TAG,"Gyro Measurement: " + mGyroDataList);
        Log.d(TAG,"Time Stamp: " + mForceTime);
        for (int i =0;i<mForceDataList.size()-1;i++){
            forceData.add(new Entry(mForceTime.get(i),mForceDataList.get(i)));
        }
        LineDataSet set1 = new LineDataSet(forceData,"Force Measurement");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData data = new LineData(set1);
        mForceGraph.setData(data);
        mForceGraph.invalidate();

    }



    public void createSingleAccView(){
        List<List<Integer>> convertedData = convertToXYZData(mAccDataList);
        List<Integer> Xdata = convertedData.get(0);
        List<Integer> Ydata = convertedData.get(1);
        List<Integer> Zdata = convertedData.get(2);
        List<Entry> xAxisData = new ArrayList<>();
        List<Entry> yAxisData = new ArrayList<>();
        List<Entry> zAxisData = new ArrayList<>();


        Log.d(TAG,"ACC size: " + mAccDataList.size());
        Log.d(TAG,"ACCTIme size:  " + mGyroAccTime.size());
        for (int i =0;i<Xdata.size()-1;i++){
            Entry xEntry = new Entry(mGyroAccTime.get(i),Xdata.get(i));
            Entry yEntry = new Entry(mGyroAccTime.get(i),Ydata.get(i));
            Entry zEntry = new Entry(mGyroAccTime.get(i),Zdata.get(i));
            xAxisData.add(xEntry);
            yAxisData.add(yEntry);
            zAxisData.add(zEntry);
        }
        LineDataSet xDataset = new LineDataSet(xAxisData,"X-Axis");
        xDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        xDataset.setColor(Color.RED);
        LineDataSet yDataset = new LineDataSet(yAxisData,"Y-Axis");
        yDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        yDataset.setColor(Color.BLUE);
        LineDataSet zDataset = new LineDataSet(zAxisData,"Z-Axis");
        zDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        zDataset.setColor(Color.GREEN);
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(xDataset);
        dataSets.add(yDataset);
        dataSets.add(zDataset);

        LineData lineData = new LineData(dataSets);
        mAccGraph.setData(lineData);
        mAccGraph.invalidate();
    }

    public int numberOfShootings(){
     return 1; //TODO: implement detection of shootings
    }

    public void createSingleGyroView(){
        List<List<Integer>> convertedData = convertToXYZData(mGyroDataList);
        List<Integer> Xdata = convertedData.get(0);
        List<Integer> Ydata = convertedData.get(1);
        List<Integer> Zdata = convertedData.get(2);
        List<Entry> xAxisData = new ArrayList<>();
        List<Entry> yAxisData = new ArrayList<>();
        List<Entry> zAxisData = new ArrayList<>();

        Log.d(TAG,"GyroData Size: " + mGyroDataList.size());
        Log.d(TAG,"Gyro Time Size: " + mGyroAccTime.size());
        for (int i =0;i<Xdata.size()-1;i++){
            Entry xEntry = new Entry(mGyroAccTime.get(i),Xdata.get(i));
            Entry yEntry = new Entry(mGyroAccTime.get(i),Ydata.get(i));
            Entry zEntry = new Entry(mGyroAccTime.get(i),Zdata.get(i));
            xAxisData.add(xEntry);
            yAxisData.add(yEntry);
            zAxisData.add(zEntry);
        }
        LineDataSet xDataset = new LineDataSet(xAxisData,"X-Axis");
        xDataset.setColor(Color.RED);
        xDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet yDataset = new LineDataSet(yAxisData,"Y-Axis");
        yDataset.setColor(Color.BLUE);
        yDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineDataSet zDataset = new LineDataSet(zAxisData,"Z-Axis");
        zDataset.setColor(Color.GREEN);
        zDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(xDataset);
        dataSets.add(yDataset);
        dataSets.add(zDataset);

        LineData lineData = new LineData(dataSets);
        mGyroGraph.setData(lineData);
        mGyroGraph.invalidate();
    }

    public boolean saveToDataBase(){
        if (mAthleteFirstName.equals("Test")){
            Log.d(TAG,"TEST is not part of DB");
            return false;
        }


        JSONObject jsonObject = makeJSONObject();
        ShootingData shooting = new ShootingData();
        Date date = new Date(System.currentTimeMillis());
        String  stringDate = DateFormat.getDateTimeInstance().format(date);
        shooting.setDate(stringDate);
        shooting.setNumberOfShootings(numberOfShootings());
        shooting.set_id(mShootings.size() + 1);
        shooting.setFilename(saveFile(jsonObject,stringDate));
        shooting.setAthlete_id(mAthleteDataId);
        shooting.printShootingData();
        mDBAthlete.createShooting(shooting,mAthleteDataId);

        return true;
    }
    public String saveFile(JSONObject jsonObject,String date){
        String filename =  mAthleteFirstName + mAthleteSurName + date + ".txt";
        Context context = getApplicationContext();
        File file = new File(context.getFilesDir(),filename);
        Log.d(TAG,"Saving file : " + filename + "\n" +
            " to Path: " + file.getAbsolutePath());

        try {
            FileWriter out = new FileWriter(file);
            out.write(jsonObject.toString());
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return filename;
    }

    public JSONObject makeJSONObject(){
        JSONObject jsonObject = new JSONObject();
        String forceData = mForceDataList.toString();
        String AccData = mAccDataList.toString();
        String GyroData = mGyroDataList.toString();
        String ForceTime = mForceTime.toString();
        String AccGyroTime = mGyroAccTime.toString();
        try{
            jsonObject.put(FORCE_TEXT,forceData);
            jsonObject.put(ACCELEROMETER_TEXT,AccData);
            jsonObject.put(GYROSCOPE_TEXT,GyroData);
            jsonObject.put(FORCETIME_TEXT,ForceTime);
            jsonObject.put(ACCGYROTIME_TEXT,AccGyroTime);

        }catch (JSONException e){
            e.printStackTrace();

        }
        Log.d(TAG,"JSON object to be stored: " + jsonObject.toString());
        return jsonObject;
    }


    public void CreateView(LineChart chart, float Ymax, float Ymin){

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setScaleXEnabled(true);
        chart.setBackgroundColor(Color.rgb(102,209,255));


        XAxis xl = chart.getXAxis();
        xl.setTypeface(Typeface.DEFAULT);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(Ymax);
        leftAxis.setAxisMinimum(Ymin);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }


    private ILineDataSet createSet(String name){

        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setDrawFilled(true);
        set.setCircleRadius(1f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));

        set.setDrawValues(false);
        return set;
    }

}
