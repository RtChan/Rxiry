package com.gzcz.rtchen.rxiry;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 *  当前只能看VDML
 */

public class HistoryActivity extends AppCompatActivity {

    TextView tv_History;
    Button btn_Clean;
    ArrayList<StoreStructure> mList;

    StageManager mStageManager = new StageManager();

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

        mStageManager.setMode("VDML");

        mList = new SimpleDataManager(getApplication()).getDatasList();

        for (StoreStructure ss : mList) {
            tv_History.append(ss.getName() + "  " + ss.getTimeStamp() + "\r\n");
            for (String s : ss.getDataList()) {
                //tv_History.append(s);
                parseMessage(s);
            }
            tv_History.append("\r\n\r\n");
        }
    }

    protected void dispHV(final RxiryFormat.HV hv) {
        tv_History.append(getResources().getString(R.string.rxiry_SD)
                + String.valueOf(hv.SD)
                + (hv.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
        tv_History.append("\r\n");
        tv_History.append(getResources().getString(R.string.rxiry_INC)
                + String.valueOf(hv.INC)
                + (hv.INCUnit == RxiryFormat.RXUnitType.Degree ? "度" : "%"));
        tv_History.append("\r\n");
    }
    protected void dispML(final RxiryFormat.ML ml) {
        tv_History.append(getResources().getString(R.string.rxiry_HD)
                + String.valueOf(ml.HD)
                + (ml.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
        tv_History.append("\r\n");
        tv_History.append(getResources().getString(R.string.rxiry_VD)
                + String.valueOf(ml.VD)
                + (ml.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
        tv_History.append("\r\n");
        tv_History.append(getResources().getString(R.string.rxiry_SD)
                + String.valueOf(ml.SD)
                + (ml.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
        tv_History.append("\r\n");
        tv_History.append(getResources().getString(R.string.rxiry_INC)
                + String.valueOf(ml.INC)
                + (ml.INCUnit == RxiryFormat.RXUnitType.Degree ? "度" : "%"));
        tv_History.append("\r\n");
        tv_History.append(getResources().getString(R.string.rxiry_AZ)
                + String.valueOf(ml.AZ)
                + (ml.AZUnit == RxiryFormat.RXUnitType.Degree ? "度" : "%"));
        tv_History.append("\r\n");

    }
    protected void dispTst(final int index) {
        tv_History.append("---测量点" + index + "---\r\n");
    }
    protected void dispResult() {
        tv_History.append("---测量结果---\r\n");
    }

    protected void parseMessage(String str) {
        RxiryFormat rf = new RxiryFormat(str);

        if (rf.getType() == RxiryFormat.RXDataType.HV) {
            RxiryFormat.HV hv = rf.getHV();
            if (null != hv) {
                mStageManager.addHV();
                dispTst(mStageManager.getHVIndex());
                dispHV(hv);
            }
        }

        if (rf.getType() == RxiryFormat.RXDataType.ML) {
            RxiryFormat.ML ml = rf.getML();
            if (null != ml) {
                mStageManager.addML();
                dispResult();
                dispML(ml);
            }
        }

        // 判断是否测量结束
        if (StageManager.STAGE_DONE == mStageManager.getStage()) {
            mStageManager.reset();
        } else if (StageManager.STAGE_ERROR == mStageManager.getStage()) {
            mStageManager.reset();
        }
    }

    private class StageManager {
        private int hv;
        private int ml;
        //private boolean bad;

        private String mode;
        private int stage;
        static final int STAGE_IDLE = 0;
        static final int STAGE_PROCESSING = 1;
        static final int STAGE_ERROR = 2;
        static final int STAGE_DONE = 3;

        public StageManager() {
            reset();
        }

        public StageManager addHV() {
            hv = hv + 1;
            return this;
        }
        public StageManager addML() {
            // 防止 hv ml hv 错序的情况
            if ((hv != 2) && (mode.contains("ML"))) {
                stage = STAGE_ERROR;
            }

            ml = ml + 1;
            return this;
        }

        public int getHVIndex() {
            return hv;
        }

        public int getStage() {
            if (mode.isEmpty()) return -1;  // 未设定当前测量模式
            if (STAGE_ERROR == stage) return stage;   // 测量数据返回值是乱序的

            switch (mode) {
                case "VDML":
                case "HDML":
                case "SDML":
                    if ((1 == hv) && (0 == ml)) stage = STAGE_PROCESSING;
                    if ((2 == hv) && (0 == ml)) stage = STAGE_PROCESSING;
                    if ((2 == hv) && (1 == ml)) stage = STAGE_DONE;
                    break;
                default:
                    stage = STAGE_ERROR;
            }

            return stage;
        }

        public StageManager setMode(String inMode) {
            reset();
            mode = inMode;
            return this;
        }
        public void reset() {
            hv = 0;
            ml = 0;
            stage = STAGE_IDLE;
        }
    }
}
