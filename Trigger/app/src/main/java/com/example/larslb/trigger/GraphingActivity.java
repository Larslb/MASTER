package com.example.larslb.trigger;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import android.widget.Toast;
import android.widget.ViewFlipper;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
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
import java.util.HashMap;
import java.util.List;

public class GraphingActivity extends AppCompatActivity {
    private static final String TAG = GraphingActivity.class.getSimpleName();

    String mAthleteFirstName;
    String mAthleteSurName;
    TextView mTextAthleteFirstName;
    TextView mTextAthleteSurName;
    EditText mDescriptorText;
    Button mSaveButton;
    ArrayList<ShootingData> mShootings;

    ViewFlipper mFlipper;

    DBHelper mDBAthlete;
    HashMap<String,ArrayList<Integer>> mForceDataList;
    HashMap<String,ArrayList<Integer>> mGyroDataList;
    HashMap<String,ArrayList<Integer>> mAccDataList;
    ArrayList<Integer> mForceTime;
    ArrayList<Integer> mAccTime;
    ArrayList<Integer> mGyroTime;

    private int mAthleteDataId;
    private int mShootingCounter = 0;
    private boolean mEnableSave;

    LineChart mForceGraph;
    LineChart mAccGraph;
    LineChart mGyroGraph;

    public static final String FORCE_TEXT = "FORCE";
    public static final String ACCELEROMETER_TEXT = "ACC";
    public static final String GYROSCOPE_TEXT = "GYRO";
    public static final String FORCE_TIME_TEXT = "FORCE_TIME";
    public static final String ACC_TIME_TEXT = "ACC_TIME";
    public static final String GYRO_TIME_TEXT = "GYRO_TIME";
    public static final String ENABLE_SAVE = "ENABLE_SAVE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphing);
        Log.d(TAG,"onCreate!");
        mForceDataList = new HashMap<>();
        mAccDataList = new HashMap<>();
        mGyroDataList = new HashMap<>();

        mDBAthlete = new DBHelper(this);
        Intent intent = getIntent();
        mForceDataList.put(FORCE_TEXT,intent.getIntegerArrayListExtra(FORCE_TEXT));
        mForceDataList.put(FORCE_TIME_TEXT,intent.getIntegerArrayListExtra(FORCE_TIME_TEXT));
        mAccDataList.put(ACCELEROMETER_TEXT,intent.getIntegerArrayListExtra(ACCELEROMETER_TEXT));
        mAccDataList.put(ACC_TIME_TEXT,intent.getIntegerArrayListExtra(ACC_TIME_TEXT));
        mGyroDataList.put(GYROSCOPE_TEXT,intent.getIntegerArrayListExtra(GYROSCOPE_TEXT));
        mGyroDataList.put(GYRO_TIME_TEXT,intent.getIntegerArrayListExtra(GYRO_TIME_TEXT));
        mAthleteDataId = intent.getIntExtra(NewExerciseActivity.ATHLETE_ID,0);
        mEnableSave = intent.getBooleanExtra(ENABLE_SAVE,true);
        mAthleteFirstName = intent.getStringExtra(NewExerciseActivity.FIRST_NAME);
        mAthleteSurName = intent.getStringExtra(NewExerciseActivity.LAST_NAME);
        mTextAthleteFirstName = (TextView) findViewById(R.id.athletefirstName);
        mTextAthleteFirstName.setText(mAthleteFirstName);
        mTextAthleteSurName = (TextView) findViewById(R.id.athleteLastName);
        mTextAthleteSurName.setText(mAthleteSurName);
        mDescriptorText = (EditText) findViewById(R.id.descriptionText);
        mSaveButton = (Button) findViewById(R.id.SaveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(saveToDataBase()){
                    Toast toast = Toast.makeText(getApplicationContext(),"Saved to Database",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    Toast toast =Toast.makeText(getApplicationContext(),"Not able to save",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        mShootings = mDBAthlete.getAllShootings();


        mFlipper = (ViewFlipper) findViewById(R.id.GraphFlipper);
        mForceGraph = (LineChart) findViewById(R.id.SingleForceGraph);
        mAccGraph = (LineChart) findViewById(R.id.SingleAccGraph);
        mGyroGraph = (LineChart) findViewById(R.id.SingleGyroGraph);

        CreateView(mForceGraph,300f,-10f, "Force Measurement");
        CreateView(mAccGraph, 33500f,-33500f, "Accelerometer Measurement");
        CreateView(mGyroGraph, 33500f,-33500f, "Gyroscope Measurement");
        mShootingCounter ++;
        printData();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        Log.d(TAG, "Enable Save mode:   " + mEnableSave);
        if (mEnableSave) getMenuInflater().inflate(R.menu.plottingmenu,menu);
        else getMenuInflater().inflate(R.menu.plottingmenu_without_save,menu);
        return super.onCreateOptionsMenu(menu);
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
                mFlipper.setDisplayedChild(4);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    public void printData(){
        Log.d(TAG,"Data in stock:   \n");
        Log.d(TAG," ------------------------------  \n");
        Log.d(TAG,FORCE_TEXT + ":   " + mForceDataList.get(FORCE_TEXT) + "\n");
        Log.d(TAG,FORCE_TIME_TEXT + ":      " + mForceDataList.get(FORCE_TIME_TEXT) + "\n");
        Log.d(TAG,ACCELEROMETER_TEXT + ":       " + mAccDataList.get(ACCELEROMETER_TEXT) + "\n");
        Log.d(TAG,ACC_TIME_TEXT + ":            " + mAccDataList.get(ACC_TIME_TEXT) + "\n");
        Log.d(TAG,GYROSCOPE_TEXT + ":           " + mGyroDataList.get(GYROSCOPE_TEXT) + "\n");
        Log.d(TAG,GYRO_TIME_TEXT + ":           " + mGyroDataList.get(GYRO_TIME_TEXT) + "\n");
        Log.d(TAG," ------------------------------  \n");

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

        Log.d(TAG,"ForceData:   " + mForceDataList.get(FORCE_TEXT));
        Log.d(TAG,"ForceTimeData:   " + mForceDataList.get(FORCE_TIME_TEXT));

        for (int i =0;i<mForceDataList.get(FORCE_TIME_TEXT).size()-1;i++){
            forceData.add(new Entry(mForceDataList.get(FORCE_TIME_TEXT).get(i),mForceDataList.get(FORCE_TEXT).get(i)));
        }
        LineDataSet set1 = new LineDataSet(forceData,"Force Measurement");
        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setDrawFilled(true);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData data = new LineData(set1);
        mForceGraph.setData(data);
        mForceGraph.invalidate();

    }



    public void createSingleAccView(){
        List<List<Integer>> convertedData = convertToXYZData(mAccDataList.get(ACCELEROMETER_TEXT));
        List<Integer> Xdata = convertedData.get(0);
        List<Integer> Ydata = convertedData.get(1);
        List<Integer> Zdata = convertedData.get(2);
        List<Entry> xAxisData = new ArrayList<>();
        List<Entry> yAxisData = new ArrayList<>();
        List<Entry> zAxisData = new ArrayList<>();


        Log.d(TAG,"ACC size: " + mAccDataList.size());
        Log.d(TAG,"ACCTIme size:  " + mAccDataList.get(ACC_TIME_TEXT).size());
        for (int i =0;i<Zdata.size()-1;i++){
            Entry xEntry = new Entry(mAccDataList.get(ACC_TIME_TEXT).get(i),Xdata.get(i));
            Entry yEntry = new Entry(mAccDataList.get(ACC_TIME_TEXT).get(i),Ydata.get(i));
            Entry zEntry = new Entry(mAccDataList.get(ACC_TIME_TEXT).get(i),Zdata.get(i));
            xAxisData.add(xEntry);
            yAxisData.add(yEntry);
            zAxisData.add(zEntry);
        }
        LineDataSet xDataset = new LineDataSet(xAxisData,"X-Axis");
        xDataset.setDrawValues(false);
        xDataset.setDrawCircles(false);
        xDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        xDataset.setColor(Color.RED);
        LineDataSet yDataset = new LineDataSet(yAxisData,"Y-Axis");
        yDataset.setDrawValues(false);
        yDataset.setDrawCircles(false);
        yDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        yDataset.setColor(Color.BLUE);
        LineDataSet zDataset = new LineDataSet(zAxisData,"Z-Axis");
        zDataset.setDrawValues(false);
        zDataset.setDrawCircles(false);
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
        List<List<Integer>> convertedData = convertToXYZData(mGyroDataList.get(GYROSCOPE_TEXT));
        ArrayList<Integer> time = mGyroDataList.get(GYRO_TIME_TEXT);
        List<Integer> Xdata = convertedData.get(0);
        List<Integer> Ydata = convertedData.get(1);
        List<Integer> Zdata = convertedData.get(2);
        List<Entry> xAxisData = new ArrayList<>();
        List<Entry> yAxisData = new ArrayList<>();
        List<Entry> zAxisData = new ArrayList<>();

        Log.d(TAG,"GyroData Size: " + mGyroDataList.size());
        Log.d(TAG,"Gyro Time Size: " + mGyroDataList.get(GYRO_TIME_TEXT).size());
        for (int i =0;i<Xdata.size()-1;i++){
            Entry xEntry = new Entry(time.get(i),Xdata.get(i));
            Entry yEntry = new Entry(time.get(i),Ydata.get(i));
            Entry zEntry = new Entry(time.get(i),Zdata.get(i));
            xAxisData.add(xEntry);
            yAxisData.add(yEntry);
            zAxisData.add(zEntry);
        }
        LineDataSet xDataset = new LineDataSet(xAxisData,"X-Axis");
        xDataset.setDrawValues(false);
        xDataset.setDrawCircles(false);
        xDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        xDataset.setColor(Color.RED);
        LineDataSet yDataset = new LineDataSet(yAxisData,"Y-Axis");
        yDataset.setDrawValues(false);
        yDataset.setDrawCircles(false);
        yDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        yDataset.setColor(Color.BLUE);
        LineDataSet zDataset = new LineDataSet(zAxisData,"Z-Axis");
        zDataset.setDrawValues(false);
        zDataset.setDrawCircles(false);
        zDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        zDataset.setColor(Color.GREEN);
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
        shooting.setShootingDescriptor(mDescriptorText.getText().toString());
        shooting.setAthlete_id(mAthleteDataId);
        shooting.printShootingData();
        shooting.setFilename(saveFile(jsonObject,stringDate, shooting.getShootingDescriptor()));


        mDBAthlete.createShooting(shooting,mAthleteDataId);


        return true;
    }
    public String saveFile(JSONObject jsonObject,String date, String description){
        String filename =  mAthleteFirstName + mAthleteSurName + description + date + ".txt";
        String directoryName = mAthleteFirstName + mAthleteSurName;
        Log.w(TAG,"IsExternalStorageWritable?   " + isExternalStorageWritable());
        File dir;
        if (isExternalStorageWritable()) {
            dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),directoryName);
            if (!dir.mkdir()){
                Log.e(TAG,"directory not created");
            }
            Log.d(TAG,"root path    -----       " + dir.getPath());

        }else {
            Toast toast = Toast.makeText(getApplicationContext(),"External Storage not writeable, saving to Internal memory",Toast.LENGTH_LONG);
            toast.show();
            dir = new File(getFilesDir(), directoryName);
        }

        File file = new File(dir, filename);
        if(file.exists()) file.delete();
        Log.d(TAG,"Saving file : " + filename + "\n" +
            " to Path: " + file.getAbsolutePath());


        File listFile = getFilesDir();
        for (File f : listFile.listFiles()){
            Log.d(TAG,"File in filesdir:    " + f.toString());
        }
        try {
            file.createNewFile();
            FileOutputStream f = new FileOutputStream(file);
            OutputStreamWriter ow = new OutputStreamWriter(f);
            ow.append(jsonObject.toString());
            ow.close();
            f.flush();
            f.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return filename;
    }

    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    public File getExternalStorageDir(String fileName){
        File file = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),fileName);
        if (!file.mkdir()){
            Log.e(TAG,"Directory not created!");

        }

        return file;
    }

    public JSONObject makeJSONObject(){
        JSONObject jsonObject = new JSONObject();

        try{
            jsonObject.put(FORCE_TEXT,mForceDataList.get(FORCE_TEXT).toString() + "\n");
            jsonObject.put(FORCE_TIME_TEXT,mForceDataList.get(FORCE_TIME_TEXT).toString() + "\n");
            jsonObject.put(ACCELEROMETER_TEXT,mAccDataList.get(ACCELEROMETER_TEXT).toString() + "\n");
            jsonObject.put(ACC_TIME_TEXT,mAccDataList.get(ACC_TIME_TEXT).toString() + "\n");
            jsonObject.put(GYROSCOPE_TEXT,mGyroDataList.get(GYROSCOPE_TEXT).toString() + "\n");
            jsonObject.put(GYRO_TIME_TEXT,mGyroDataList.get(GYRO_TIME_TEXT).toString() + "\n");

        }catch (JSONException e){
            e.printStackTrace();

        }
        Log.d(TAG,"JSON object to be stored: " + jsonObject.toString());
        return jsonObject;
    }


    public void CreateView(LineChart chart, float Ymax, float Ymin,String name){

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setScaleXEnabled(true);
        chart.setBackgroundColor(Color.rgb(102,209,255));
        Description description = new Description();
        description.setText(name);
        chart.setDescription(description);



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
