package com.example.larslb.trigger;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LoadDataActivity extends MainActivity {
    private static final String TAG = LoadDataActivity.class.getSimpleName();

    private ListView mLoadList;
    private CharSequence mTitle;
    private TextView mSelectText;
    private ArrayAdapter mArrayAdapter;
    private ArrayList<HashMap<String,String>> mAthleteList;
    private DBAthlete mDBAthlete;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private String[] mNavTitles;
    private ActionBarDrawerToggle mDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);
        mDBAthlete = new DBAthlete(this);

        mLoadList = (ListView) findViewById(R.id.Load_list);
        mSelectText = (TextView) findViewById(R.id.select_name);
        mAthleteList = mDBAthlete.getAll();


        mLoadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClick(position);
            }
        });

        SimpleAdapter adapter = new SimpleAdapter(this,
                mAthleteList,
                R.layout.data_list,new String[] {"FirstName", "SurName"},new int[]{R.id.name1, R.id.name2});
        mLoadList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void itemClick(int position){
        HashMap<String,String> Athlete = mAthleteList.get(position);
        Log.d(TAG,"itemClick");
        String firstName = Athlete.keySet().toArray()[0].toString();
        String lastName = Athlete.values().toArray()[0].toString();
        Log.d(TAG,"AThlete Clicked:         ---     " + firstName + " " + lastName);
        mDBAthlete.contains(firstName + lastName);
    }


}