package com.gzcz.rtchen.rxiry;


import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rt Chen on 2016/11/17.
 */

public class StoreStructure {
    public String Name;
    public String TimeStamp;
    public String RxiryValue;

    StoreStructure(String name, String rxiryValue) {
        Name = name;
        setTimeStamp();
        setRxiryValue(rxiryValue);
    }
    StoreStructure(String name, String date, String rxiryValue) {
        Name = name;
        setTimeStamp(date);
        setRxiryValue(rxiryValue);
    }

    public String getRxiryValue() {
        return RxiryValue;
    }
    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setRxiryValue(String rxiryValue) {
        RxiryValue = rxiryValue;
    }
    public void setTimeStamp() {
        DateFormat df = null ; // 声明一个DateFormat
        df = DateFormat.getDateTimeInstance(
                DateFormat.YEAR_FIELD,
                DateFormat.ERA_FIELD,
                new Locale("zh","CN")); // 得到日期时间的DateFormat对象
        TimeStamp = df.format(new Date());
    }
    public void setTimeStamp(String date) {
        TimeStamp = date;
    }
}
