package com.example.larslb.trigger;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

public class NewExerciseActivity extends Activity  {
    private static final String TAG = NewExerciseActivity.class.getSimpleName();

    Button addNewAthlete;
    Button createNewButton;
    ListView mDataList;
    EditText mFirstName;
    EditText mLastName;
    ViewFlipper mFlipper;
    ArrayList<AthleteData> mAthleteList;
    DatePicker mDatePicker;
    FragmentManager mFragmentMangaer;
    ArrayAdapter mAdapter;
    private DBHelper mAthleteDB;

    public final static String FIRST_NAME = "FIRST_NAME";
    public final static String LAST_NAME = "LAST_NAME";
    public final static String ATHLETE_ID = "ATHLETE_ID";


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
        mDatePicker = (DatePicker) findViewById(R.id.datePicker);
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
        mAthleteDB = new DBHelper(this);
        mFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        mAthleteList = mAthleteDB.getAllAthletes();
        Log.d(TAG,"mAThLETeLIST;: " + mAthleteList.toString());
        mAdapter = new arrayListAdapter(this,mAthleteList);
        mDataList.setAdapter(mAdapter);

        registerForContextMenu(mDataList);
    }

    public void onItemClicked(int pos){
        AthleteData Athlete = mAthleteList.get(pos);
        Intent intent = new Intent(this,DeviceScanActivity.class);
        intent.putExtra(FIRST_NAME,Athlete.getFirstName());
        intent.putExtra(LAST_NAME,Athlete.getLastName());
        Log.d(TAG,"Athlete ID to Exercise: " + Athlete.getId());
        intent.putExtra(ATHLETE_ID, Athlete.getId());
        startActivity(intent);
    }

    public void createNewAthlete(){
        mFlipper.showNext();

    }

    public void onCreateNewButtonPressed(){
        String dateofBirth = convertDOB(mDatePicker.getDayOfMonth(),mDatePicker.getMonth(),mDatePicker.getYear());
        AthleteData athlete = createAthlete(mFirstName.getText().toString(),mLastName.getText().toString(),dateofBirth);
        int res = mAthleteDB.createAthlete(athlete);
        Log.d(TAG,"Result from Adding athelete:     " + res);
        Intent intent = new Intent(this,DeviceScanActivity.class);
        intent.putExtra(FIRST_NAME,mFirstName.getText().toString());
        intent.putExtra(LAST_NAME,mLastName.getText().toString());
        intent.putExtra(ATHLETE_ID,res);
        startActivity(intent);
    }

    public String convertDOB(int day, int month, int year){
        StringBuilder str = new StringBuilder();
        str.append(year);
        str.append("-");
        str.append(month);
        str.append("-");
        str.append(day);
        return str.toString();
    }

    public AthleteData createAthlete(String firstName, String lastName, String DoB){
        AthleteData athleteData = new AthleteData();
        athleteData.set_id(mAthleteList.size() + 1);
        athleteData.setFirstName(firstName);
        athleteData.setLastName(lastName);
        athleteData.setDateOfBirth(DoB);
        athleteData.setGender("not set");
        return athleteData;
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
                break;
            case R.id.delete:
                mAthleteDB.deleteAthlete(athlete.getId());
                mAthleteList.remove(info.position);
                mAdapter.notifyDataSetChanged();
        }
        return true;
    }


    private static class arrayListAdapter extends ArrayAdapter<AthleteData> {

        private arrayListAdapter(Context context, ArrayList<AthleteData> athletes){
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