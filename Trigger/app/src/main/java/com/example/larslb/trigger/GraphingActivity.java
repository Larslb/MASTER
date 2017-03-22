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

import java.util.ArrayList;
import java.util.List;

public class GraphingActivity extends AppCompatActivity {
    private static final String TAG = GraphingActivity.class.getSimpleName();

    String mAthleteFirstName;
    String mAthleteSurName;
    TextView mTextAthleteFirstName;
    TextView mTextAthleteSurName;

    ViewFlipper mFlipper;

    DBAthlete mDBAthlete;
    ArrayList<Integer> mForceDataList;
    ArrayList<Integer> mGyroDataList;
    ArrayList<Integer> mAccDataList;
    ArrayList<Integer> mForceTime;
    ArrayList<Integer> mGyroAccTime;

    LineChart mForceGraph;
    LineChart mAccGraph;
    LineChart mGyroGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphing);
        Log.d(TAG,"onCreate!");

        mDBAthlete = new DBAthlete(this);
        Intent intent = getIntent();
        mForceDataList = intent.getIntegerArrayListExtra("FORCE");
        mAccDataList = intent.getIntegerArrayListExtra("ACC");
        mGyroDataList = intent.getIntegerArrayListExtra("GYRO");
        mForceTime = intent.getIntegerArrayListExtra("FORCETIME");
        mGyroAccTime = intent.getIntegerArrayListExtra("GYROACCTIME");
        mAthleteFirstName = intent.getStringExtra(NewExerciseActivity.FIRST_NAME);
        mAthleteSurName = intent.getStringExtra(NewExerciseActivity.LAST_NAME);
        mTextAthleteFirstName = (TextView) findViewById(R.id.athletefirstName);
        mTextAthleteFirstName.setText(mAthleteFirstName);
        mTextAthleteSurName = (TextView) findViewById(R.id.athleteLastName);
        mTextAthleteSurName.setText(mAthleteSurName);


        mFlipper = (ViewFlipper) findViewById(R.id.GraphFlipper);
        mForceGraph = (LineChart) findViewById(R.id.SingleForceGraph);
        mAccGraph = (LineChart) findViewById(R.id.SingleAccGraph);
        mGyroGraph = (LineChart) findViewById(R.id.SingleGyroGraph);

        CreateView(mForceGraph,300f,-10f);
        CreateView(mAccGraph, 5000f,-5000f);
        CreateView(mGyroGraph, 5000f,-5000f);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        Log.d(TAG,"OnCreateOptionsMenu!");
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.plottingmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
        String forceData = mForceDataList.toString();
        String AccData = mAccDataList.toString();
        String GyroData = mGyroDataList.toString();
        String ForceTime = mForceTime.toString();
        String AccGyroTime = mGyroAccTime.toString();
        if (mDBAthlete.contains(mAthleteFirstName + mAthleteSurName));
        mDBAthlete.addToExistingAthlete(mAthleteFirstName,mAthleteSurName,
                forceData,ForceTime,AccData,GyroData,AccGyroTime);
        return true;
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
