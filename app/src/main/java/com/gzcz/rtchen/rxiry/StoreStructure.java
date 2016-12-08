package com.gzcz.rtchen.rxiry;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rt Chen on 2016/11/17.
 */

public class StoreStructure{

    private String Name;
    private String TimeStamp;
    private ArrayList<String> DataList;

    StoreStructure(String name, ArrayList<String> inStrList) {
        Name = name;
        DataList = inStrList;
        setTimeStamp();
    }
    StoreStructure(String name, String time, ArrayList<String> inStrList) {
        Name = name;
        DataList = inStrList;
        setTimeStamp();
    }

    public String getName() {
        return Name;
    }
    public String getTimeStamp() {
        return TimeStamp;
    }
    public ArrayList<String> getDataList() {
        return DataList;
    }

    public StoreStructure setTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("US","CN"));
        TimeStamp = sdf.format(new Date());
        return this;
    }
    public StoreStructure setTimeStamp(String date) {
        TimeStamp = date;
        return this;
    }
}
