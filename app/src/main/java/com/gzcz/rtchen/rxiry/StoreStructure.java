package com.gzcz.rtchen.rxiry;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rt Chen on 2016/11/17.
 */

public class StoreStructure {
    private static final String TAG = "StoreStructure";

    public String Name;
    public String TimeStamp;
    public String RxiryMode;
    public String RxiryValue_HV1;
    public String RxiryValue_HV2;
    public String RxiryValue_ML;
    public String RxiryValue_HT;

    StoreStructure(String name) {
        Name = name;
        setTimeStamp();
    }
    StoreStructure(String name, String date) {
        Name = name;
        setTimeStamp(date);
    }

    public String getRxiryValueHv1() {
        return RxiryValue_HV1;
    }
    public String getRxiryValueHv2() {
        return RxiryValue_HV2;
    }
    public String getRxiryValueML() {
        return RxiryValue_ML;
    }
    public String getRxiryValueHT() {
        return RxiryValue_HT;
    }
    public String getRxiryMode() {
        return RxiryMode;
    }
    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("US","CN"));
        TimeStamp = sdf.format(new Date());
        Log.d(TAG, "setTimeStamp: " + TimeStamp);
    }
    public void setTimeStamp(String date) {
        TimeStamp = date;
    }
}
