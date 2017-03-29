package com.example.larslb.trigger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.IntegerRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class LoadDataActivity extends MainActivity {
    private static final String TAG = LoadDataActivity.class.getSimpleName();

    ListView mLoadShootingList;
    ListView mLoadList;
    private CharSequence mTitle;
    private TextView mSelectText;
    private ArrayAdapter mArrayAdapter;
    private ArrayAdapter mShootingAdapter;
    private ArrayList<AthleteData> mAthleteList;
    private ArrayList<ShootingData> mShootingList;
    private DBHelper mDBAthlete;

    private int mAthleteId;
    private String mAthleteFirstName;
    private String mAthleteLastName;

    ViewFlipper mFlipper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);
        mDBAthlete = new DBHelper(this);

        mLoadList = (ListView) findViewById(R.id.Load_list);
        mLoadShootingList = (ListView) findViewById(R.id.load_shooting_list);
        mSelectText = (TextView) findViewById(R.id.select_name);
        mAthleteList = mDBAthlete.getAllAthletes();
        mFlipper = (ViewFlipper) findViewById(R.id.loadFlipper);

        mLoadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClick(position);
            }
        });

        mLoadShootingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loadShooting(position);
            }
        });
        mArrayAdapter = new arrayListAdapter(this,mAthleteList);

        mLoadList.setAdapter(mArrayAdapter);
        mArrayAdapter.notifyDataSetChanged();
        registerForContextMenu(mLoadList);

    }

    public void itemClick(int position){
        Log.d(TAG,"itemClick");
        AthleteData Athlete = mAthleteList.get(position);
        mAthleteId = Athlete.getId();
        mAthleteFirstName = Athlete.getFirstName();
        mAthleteLastName = Athlete.getLastName();
        loadShootings(Athlete.getId());
        mDBAthlete.printAthletesTable();
        mDBAthlete.printShootingDataTable();
    }

    public void loadShootings(int id){
        mShootingList = mDBAthlete.getAllShootingsFromAthleteID(id);
        mShootingAdapter = new shootingArrayListAdapter(this,mShootingList);
        mLoadShootingList.setAdapter(mShootingAdapter);
        mFlipper.setDisplayedChild(1);
    }

    public void loadShooting(int position){
        ShootingData shootingData = mShootingList.get(position);
        String Text = readFromFile(shootingData.filename);
        Log.d(TAG,"String from txtfile : " + Text);
        HashMap<String,ArrayList<Integer>> dataToload = parseFromTextFile(Text);

        Intent graphingIntent = new Intent(this,GraphingActivity.class);
        graphingIntent.putExtra(NewExerciseActivity.FIRST_NAME,mAthleteFirstName);
        graphingIntent.putExtra(NewExerciseActivity.LAST_NAME,mAthleteLastName);
        graphingIntent.putExtra(NewExerciseActivity.ATHLETE_ID,mAthleteId);
        graphingIntent.putExtra(GraphingActivity.ENABLE_SAVE,false);
        graphingIntent.putIntegerArrayListExtra(GraphingActivity.FORCE_TEXT,dataToload.get(GraphingActivity.FORCE_TEXT));
        graphingIntent.putIntegerArrayListExtra(GraphingActivity.ACCELEROMETER_TEXT,dataToload.get(GraphingActivity.ACCELEROMETER_TEXT));
        graphingIntent.putIntegerArrayListExtra(GraphingActivity.GYROSCOPE_TEXT,dataToload.get(GraphingActivity.GYROSCOPE_TEXT));
        graphingIntent.putIntegerArrayListExtra(GraphingActivity.FORCETIME_TEXT,dataToload.get(GraphingActivity.FORCETIME_TEXT));
        graphingIntent.putIntegerArrayListExtra(GraphingActivity.ACCGYROTIME_TEXT,dataToload.get(GraphingActivity.ACCGYROTIME_TEXT));

        startActivity(graphingIntent);


    }

    public HashMap<String,ArrayList<Integer>> parseFromTextFile(String file){
        HashMap<String,ArrayList<Integer>> data = new HashMap<>();

        String[] Force = {};
        String[] Accelerometer = {};
        String[] Gyrometer = {};
        String[] ForceTime = {};
        String[] AccGyroTime = {};



        try {
            JSONObject object = new JSONObject(file);
            Force = object.getString(GraphingActivity.FORCE_TEXT).split(",");
            Accelerometer = object.getString(GraphingActivity.ACCELEROMETER_TEXT).split(",");
            Gyrometer = object.getString(GraphingActivity.GYROSCOPE_TEXT).split(",");
            ForceTime = object.getString(GraphingActivity.FORCETIME_TEXT).split(",");
            AccGyroTime = object.getString(GraphingActivity.ACCGYROTIME_TEXT).split(",");


        }catch (Exception e){
            e.printStackTrace();
        }

        ArrayList<Integer> ForceInt = fromStringToInts(Force);
        ArrayList<Integer> AccInt = fromStringToInts(Accelerometer);
        ArrayList<Integer> GyroInt = fromStringToInts(Gyrometer);
        ArrayList<Integer> ForceTimeInt = fromStringToInts(ForceTime);
        ArrayList<Integer> AccGyroTimeInt = fromStringToInts(AccGyroTime);

        data.put(GraphingActivity.FORCE_TEXT,ForceInt);
        data.put(GraphingActivity.ACCELEROMETER_TEXT,AccInt);
        data.put(GraphingActivity.GYROSCOPE_TEXT,GyroInt);
        data.put(GraphingActivity.FORCETIME_TEXT,ForceTimeInt);
        data.put(GraphingActivity.ACCGYROTIME_TEXT,AccGyroTimeInt);
        return data;
    }


    public ArrayList<Integer> fromStringToInts(String[] array){
        ArrayList<Integer> resultData = new ArrayList<>();

        for (String item : array){
            if (item.contains("[")){
                resultData.add(Integer.parseInt(item.split("\\[")[1]));
            } else if (item.contains("]")){
                resultData.add(Integer.parseInt(item.split("\\]")[0].split(" ")[1]));
            }else resultData.add(Integer.parseInt(item.split(" ")[1]));
        }

        return resultData;
    }


    public String readFromFile(String filename){
        File file = new File(getApplicationContext().getFilesDir(),filename);
        InputStream is = this.getClass().getResourceAsStream(filename);

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = bufferedReader.readLine()) != null){
                text.append(line);
                text.append('\n');
            }
            bufferedReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return text.toString();
    }

    public ArrayList<Integer> convertStringToIntArray(String data){
        ArrayList<Integer> integerArrayList = new ArrayList<>();
        for (int i = 0;i<data.length()-1;i++){
            integerArrayList.add(Character.getNumericValue(data.charAt(i)));
        }
        return integerArrayList;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.Athlete_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle("Select Option");
            menu.add(Menu.NONE,R.id.changeName,Menu.NONE,"Change Name");
            menu.add(Menu.NONE,R.id.delete,Menu.NONE,"Delete");
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        AthleteData athlete = mAthleteList.get(info.position);
        switch (item.getItemId()){
            case R.id.changeName:
                //TODO: change name in database
                break;
            case R.id.delete:
                mDBAthlete.deleteAthlete(athlete.getId());
                mAthleteList.remove(info.position);
                mArrayAdapter.notifyDataSetChanged();
        }
        return true;
    }


    public class shootingArrayListAdapter extends ArrayAdapter<ShootingData> {

        public shootingArrayListAdapter(Context context, ArrayList<ShootingData> athletes){
            super(context,0,athletes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            ShootingData shootingData = getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.data_list,parent,false);
            }

            TextView name1 = (TextView) convertView.findViewById(R.id.name1);
            TextView name2 = (TextView) convertView.findViewById(R.id.name2);


            StringBuilder sb = new StringBuilder();
            sb.append(shootingData.getNumberOfShootings());
            name2.setText(sb.toString());
            name1.setText(shootingData.getDate());

            return convertView;
        }


    }


    public class arrayListAdapter extends ArrayAdapter<AthleteData> {

        public arrayListAdapter(Context context, ArrayList<AthleteData> athletes){
            super(context,0,athletes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){

            AthleteData athleteData = getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.data_list,parent,false);
            }

            TextView name1 = (TextView) convertView.findViewById(R.id.name1);
            TextView name2 = (TextView) convertView.findViewById(R.id.name2);

            name1.setText(athleteData.getFirstName());
            name2.setText(athleteData.getLastName());

            return convertView;
        }


    }

}