package com.example.larslb.trigger;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.larslb.trigger.R.id.activity_new_exercise;

public class NewExerciseActivity extends Activity  {
    private static final String TAG = NewExerciseActivity.class.getSimpleName();

    Button addNewAthlete;
    Button createNewButton;
    ListView mDataList;
    EditText mFirstName;
    EditText mLastName;
    ViewFlipper mFlipper;
    ArrayList<HashMap<String,String>> mAthleteList;
    FragmentManager mFragmentMangaer;
    private DBAthlete mAthleteDB;

    final static String FIRST_NAME = "FIRST_NAME";
    final static String LAST_NAME = "LAST_NAME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exercise);
        addNewAthlete = (Button) findViewById(R.id.addNewAthlete);
        addNewAthlete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAthlete();
            }
        });
        createNewButton = (Button) findViewById(R.id.addAthlete);
        createNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewButtonPressed();
            }
        });
        mFirstName = (EditText) findViewById(R.id.add_first_name);
        mLastName = (EditText) findViewById(R.id.add_sur_name);
        mFragmentMangaer = getFragmentManager();
        mDataList = (ListView) findViewById(R.id.Athlete_list);
        mDataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClicked(position);

            }
        });
        mAthleteDB = new DBAthlete(this);
        mFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        mAthleteList = mAthleteDB.getAll();
        Log.d(TAG,"mAThLETeLIST;: " + mAthleteList.toString());
        SimpleAdapter adapter = new SimpleAdapter(this,
                mAthleteList,
                R.layout.data_list,new String[] {"FirstName", "SurName"},new int[]{R.id.name1, R.id.name2});
        mDataList.setAdapter(adapter);

    }

    public void onItemClicked(int pos){
        HashMap<String,String> Athlete = mAthleteList.get(pos);
        Intent intent = new Intent(this,DeviceScanActivity.class);
        intent.putExtra(FIRST_NAME,Athlete.get("FirstName"));
        intent.putExtra(LAST_NAME,Athlete.get("SurName"));
        startActivity(intent);
    }

    public void createNewAthlete(){
        mFlipper.showNext();

    }

    public void onCreateNewButtonPressed(){

        Log.d(TAG,"FirstName: " + mFirstName.getText().toString() + "SurName: " + mLastName.getText().toString());
        mAthleteDB.addAthlete(mFirstName.getText().toString(),mLastName.getText().toString());
        Intent intent = new Intent(this,DeviceScanActivity.class);
        intent.putExtra(FIRST_NAME,mFirstName.getText().toString());
        intent.putExtra(LAST_NAME,mLastName.getText().toString());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.new_exercise_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.changeName:
                break;
            case R.id.delete:
                break;
        }
        return true;
    }
}