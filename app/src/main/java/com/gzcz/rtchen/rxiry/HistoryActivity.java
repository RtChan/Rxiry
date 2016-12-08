package com.gzcz.rtchen.rxiry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    TextView tv_History;
    Button btn_Clean;
    ArrayList<StoreStructure> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        tv_History = (TextView) findViewById(R.id.tv_History);
        btn_Clean = (Button) findViewById(R.id.btn_CleanAll);

        btn_Clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_History.setText("");
                new SimpleDataManager(getApplication()).cleanData();
            }
        });

        mList = new SimpleDataManager(getApplication()).getDatasList();

        for (StoreStructure ss : mList) {
            tv_History.append(ss.getName() + "  " + ss.getTimeStamp() + "\r\n");
            for (String s : ss.getDataList()) {
                tv_History.append(s);
            }
            tv_History.append("\r\n\r\n");
        }
    }
}
