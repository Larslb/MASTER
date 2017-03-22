package com.example.larslb.trigger;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by larslb on 16.02.2017.
 */

public class ListDataAdapter extends BaseAdapter {
    private ArrayList<String> dataAthletes;

    public ListDataAdapter(){
        dataAthletes = new ArrayList<>();

    }

    @Override
    public long getItemId(int id){
        return 69;
    }
    @Override
    public Object getItem(int i){
        return 69;
    }

    @Override
    public int getCount(){
        return 0;
    }
    @Override
    public View getView(int pos, View view, ViewGroup viewGroup){
        return  view;
    }

}
