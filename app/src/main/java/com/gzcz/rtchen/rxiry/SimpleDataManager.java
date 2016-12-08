package com.gzcz.rtchen.rxiry;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rt Chen on 2016/12/7.
 */

public class SimpleDataManager {
    //private static final String PROJECTLISTFILENAME = "_ProjectList";
    //private static final String PACKAGENAME = "com.gzcz.rtchen.rxiry";
    private Context mContext = null;

    private ArrayList<StoreStructure> mStoreStructureList = new ArrayList<>();
    private String mCurrentProject = "PR";

    public SimpleDataManager(Context c) {
        mContext = c;
        initDatasList();
        readStoreStructureListFromeFile();
    }

    private void initDatasList() {
        SharedPreferences sp = mContext.getSharedPreferences(mCurrentProject, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        int total = sp.getInt("Total", -1);
        if (-1 == total) {
            editor.putInt("Total", 0);
            editor.apply();
        }
    }

    public void addData(StoreStructure s) {
        mStoreStructureList.add(s);
        saveStoreStructureListToFile();
    }

    public ArrayList<StoreStructure> getDatasList() {
        mStoreStructureList.clear();
        readStoreStructureListFromeFile();
        return mStoreStructureList;
    }

    private void saveStoreStructureListToFile() {
        SharedPreferences sp = mContext.getSharedPreferences(mCurrentProject, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (null == mStoreStructureList) return; // 列表未被初始化

        int i = 0;
        try {
            for (StoreStructure s : mStoreStructureList) {
                editor.putString(Integer.toString(i), getJsonObjectFromStructure(s).toString());
                i += 1;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        // 写入失败，清除缓存文件
        if (-1 == i) {
            editor.clear().apply();
            return;
        }

        // 写入成功，更新“Total”标签
        editor.putInt("Total", i);
        editor.apply();
    }

    private void readStoreStructureListFromeFile() {
        StoreStructure p;
        ArrayList<StoreStructure> list = new ArrayList<>();

        SharedPreferences sp = mContext.getSharedPreferences(mCurrentProject, Context.MODE_PRIVATE);

        int i = 0;
        int total = sp.getInt("Total", -1);
        if (-1 == total) {  //列表非法初始化
            mStoreStructureList = null;
            return;
        } else if (0 == total) {    //列表为空
            mStoreStructureList = new ArrayList<>();
            return;
        }

        String s = null;
        try {
            for (i = 0; i < total; ++i) {
                s = sp.getString(Integer.toString(i), "");
                list.add(getStructureFromJsonObject(s));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            i = -1;
        }

        // 读取发生错误
        if (-1 == i) {
            mStoreStructureList = null;
            return;
        }

        // 读取成功
        mStoreStructureList = list;
    }

    @Nullable
    private JSONObject getJsonObjectFromStructure(StoreStructure s) {
        JSONObject mJsonObj = new JSONObject();
        try {
            mJsonObj.put("Name", s.getName());
            mJsonObj.put("TimeStamp", s.getTimeStamp());
            int i = 1;
            for (String str : s.getDataList()) {
                mJsonObj.put("data" + i, str);
                ++i;
            }
            mJsonObj.put("Index", i-1);
        } catch (JSONException e) {
            e.printStackTrace();
            mJsonObj = null;
        }

        if (null == mJsonObj) return null;
        else return mJsonObj;
    }

    @Nullable
    private StoreStructure getStructureFromJsonObject(String js) {
        JSONObject mJsonObj = null;
        StoreStructure mData = null;

        String Name;
        String TimeStamp;
        ArrayList<String> DataList = new ArrayList<>();
        int index;

        try {
            mJsonObj = new JSONObject(js);
            Name = mJsonObj.getString("Name");
            TimeStamp = mJsonObj.getString("TimeStamp");
            index = mJsonObj.getInt("Index");
            for (int i = 1; i <= index; ++i) {
                DataList.add(mJsonObj.getString("data"+i));
            }
            mData = new StoreStructure(Name, TimeStamp, DataList);
        } catch (JSONException e) {
            e.printStackTrace();
            mData = null;
        }

        if (null == mData) return null;
        else return mData;
    }
}
