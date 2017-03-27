package com.example.larslb.trigger;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.IntegerRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
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
        registerForContextMenu(mLoadList);

    }

    public void itemClick(int position){
        Log.d(TAG,"itemClick");
        HashMap<String,String> Athlete = mAthleteList.get(position);
        String firstName = Athlete.get("FirstName");
        String lastName = Athlete.get("SurName");
        Log.d(TAG,"AThlete Clicked:         ---     " + firstName + " " + lastName);
        Long id = mDBAthlete.getRowId(firstName,lastName);
        Log.d(TAG,"ID   ---     " + id);
        Log.d(TAG,mDBAthlete.printTable());
        Cursor cursor = mDBAthlete.getDataFromId(mDBAthlete.getRowId(firstName,lastName));
        ArrayList<Integer> ForceData = convertStringToIntArray(cursor.getString(cursor.getColumnIndex(AthleteData.AthleteDataEntry.FORCE)));
        Log.d(TAG,"ForceData before conversion:     " + cursor.getString(cursor.getColumnIndex(AthleteData.AthleteDataEntry.FORCE)));
        Log.d(TAG,"ForceData after conversion:      " + ForceData);

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
        String fullname = mAthleteList.get(info.position).get("FirstName") + mAthleteList.get(info.position).get("SurName");
        Log.d(TAG,"FullName : " + fullname);
        switch (item.getItemId()){
            case R.id.changeName:
                //TODO: change name in database
                break;
            case R.id.delete:
                mDBAthlete.deleteAthlete(null);
        }
        return true;
    }

}