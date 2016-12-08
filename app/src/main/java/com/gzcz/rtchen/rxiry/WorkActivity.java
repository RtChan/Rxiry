package com.gzcz.rtchen.rxiry;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import dji.common.error.DJIError;
import dji.common.remotecontroller.DJIRCHardwareState;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;
import dji.sdk.remotecontroller.DJIRemoteController;

public class WorkActivity extends AppCompatActivity {
    private static final String TAG = "WorkActivity";

    private DJIBaseProduct mProduct;
    private DJIFlightController mFlightController;
    private DJIRemoteController mRemoteController;

    StringBuffer mSerialMessage = new StringBuffer();
    OnboardSdkMsgHelper mOnboardSdkMsgHelper = new OnboardSdkMsgHelper();

    //int curStep = 1;
    StageManager mStageManager;
    private TextView tv_ResultDisplay;
    ScrollView mScroll;
    Handler mMainLoopHandler = new Handler(Looper.getMainLooper());

    ArrayList<String> mDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_work);
        tv_ResultDisplay = (TextView) findViewById(R.id.tv_ResultDisplay);
        mScroll = (ScrollView) findViewById(R.id.DisplayArea);

        Button btnMeasure = (Button) findViewById(R.id.btn_c2);
        btnMeasure.setOnClickListener(new btnC2OnClickListener());

        setCallbackFunction();
        mStageManager = new StageManager().setMode("VDML");

        // TODO: Clean this Debug Code.
        parseMessage("$XRPT,HV,3.0,0.0,3.0,M,-000.8,D,248,D,*64\r\n");
        parseMessage("$XRPT,ML,0.1,0.0,0.1,M,0.0,D,270,D,*77\r\n");
        parseMessage("$XRPT,HV,3.1,0.0,3.1,M,-000.8,D,248,D,*64\r\n");
        parseMessage("$XRPT,HV,3.0,0.0,3.0,M,-000.8,D,248,D,*64\r\n");
        parseMessage("$XRPT,ML,0.1,0.0,0.1,M,0.0,D,270,D,*77\r\n");

        IntentFilter filter = new IntentFilter();
        filter.addAction(DjiSdkApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* onReceive方法会在DJI产品连接状态改变时被调用 */
        @Override
        public void onReceive(Context context, Intent intent) {
            setCallbackFunction();
        }
    };

    private void setCallbackFunction() {
        mProduct = DjiSdkApplication.getAircraftInstance();
        if (null == mProduct) return;
        mFlightController = ((DJIAircraft) mProduct).getFlightController();
        if (null == mFlightController) return;

        mFlightController.setReceiveExternalDeviceDataCallback(new DJIFlightControllerDelegate.FlightControllerReceivedDataFromExternalDeviceCallback() {
            @Override
            public void onResult(final byte[] bytes) {

                for (byte b : bytes) {
                    if (b == '@') {
                        mSerialMessage.setLength(0);    // As same as clean the buffer.
                    }

                    mSerialMessage.append((char) b);

                    if (b == 0x0A) {  // The ascii of '\n' is 0x0A.
                        final String str = mOnboardSdkMsgHelper.parseReceivedFromOnboard(new String(mSerialMessage));
                        Log.d(TAG, "onResult: recv:" + str);

                        if (str.isEmpty()) break;

                        mMainLoopHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Recv:" + new String(bytes), Toast.LENGTH_SHORT).show();
                            }
                        });

                        if (!str.isEmpty()) {
                            parseMessage(str);
                        }
                    }
                }
            }
        });

        mRemoteController = ((DJIAircraft) mProduct).getRemoteController();
        if (null == mRemoteController) return;

        mRemoteController.setHardwareStateUpdateCallback(new DJIRemoteController.RCHardwareStateUpdateCallback() {
            @Override
            public void onHardwareStateUpdate(DJIRemoteController djiRemoteController, DJIRCHardwareState djircHardwareState) {

                if (djircHardwareState.customButton1.buttonDown) {
                    mMainLoopHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "左键C1按下", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                if (djircHardwareState.customButton2.buttonDown) {
                    mMainLoopHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "右键C2按下", Toast.LENGTH_SHORT).show();
                        }
                    });

                    measure();
                }
            }
        });
    }

    protected void parseMessage(String str) {
        RxiryFormat rf = new RxiryFormat(str);

        if (!rf.isChecksumValid()) {
            mMainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Checksum Invalid.", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        mDataList.add(str);

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
            mMainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    tv_ResultDisplay.append("--> 数据完整，已自动保存！\r\n\r\n");
                }
            });

            new SimpleDataManager(getApplication()).addData(new StoreStructure("test", mDataList));
            //MainActivity.mSDM.addData(new StoreStructure("test", mDataList));
            mDataList.clear();
            mStageManager.reset();
        } else if (StageManager.STAGE_ERROR == mStageManager.getStage()) {
            mMainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    tv_ResultDisplay.append("--> 数据不完整，无法保存！\r\n\r\n");
                }
            });
            mDataList.clear();
            mStageManager.reset();
        }

        mMainLoopHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 500);
    }

    protected void dispHV(final RxiryFormat.HV hv) {
        mMainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                //tv_ResultDisplay.append("---测量点1---\r\n");
                tv_ResultDisplay.append(getResources().getString(R.string.rxiry_SD)
                        + String.valueOf(hv.SD)
                        + (hv.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
                tv_ResultDisplay.append("\r\n");
                tv_ResultDisplay.append(getResources().getString(R.string.rxiry_INC)
                        + String.valueOf(hv.INC)
                        + (hv.INCUnit == RxiryFormat.RXUnitType.Degree ? "度" : "%"));
                tv_ResultDisplay.append("\r\n");
            }
        });
    }
    protected void dispML(final RxiryFormat.ML ml) {
        mMainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                //tv_ResultDisplay.append("---测量结果---\r\n");
                tv_ResultDisplay.append(getResources().getString(R.string.rxiry_HD)
                        + String.valueOf(ml.HD)
                        + (ml.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
                tv_ResultDisplay.append("\r\n");
                tv_ResultDisplay.append(getResources().getString(R.string.rxiry_VD)
                        + String.valueOf(ml.VD)
                        + (ml.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
                tv_ResultDisplay.append("\r\n");
                tv_ResultDisplay.append(getResources().getString(R.string.rxiry_SD)
                        + String.valueOf(ml.SD)
                        + (ml.HVSUnit == RxiryFormat.RXUnitType.Metre ? "米" : "码"));
                tv_ResultDisplay.append("\r\n");
                tv_ResultDisplay.append(getResources().getString(R.string.rxiry_INC)
                        + String.valueOf(ml.INC)
                        + (ml.INCUnit == RxiryFormat.RXUnitType.Degree ? "度" : "%"));
                tv_ResultDisplay.append("\r\n");
                tv_ResultDisplay.append(getResources().getString(R.string.rxiry_AZ)
                        + String.valueOf(ml.AZ)
                        + (ml.AZUnit == RxiryFormat.RXUnitType.Degree ? "度" : "%"));
                tv_ResultDisplay.append("\r\n");
            }
        });
    }
    protected void dispTst(final int index) {
        mMainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                tv_ResultDisplay.append("---测量点" + index + "---\r\n");
            }
        });
    }
    protected void dispResult() {
        mMainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                tv_ResultDisplay.append("---测量结果---\r\n");
            }
        });
    }

    protected void measure() {
        /**
         *  @ Timestamp $ RxiryCommands [params],\r\n
         */
        StringBuilder sb_rxiry = new StringBuilder();
        sb_rxiry.append("$");
        sb_rxiry.append("ST");
        sb_rxiry.append(",\r\n");
        final String msg_rxiry = mOnboardSdkMsgHelper.getSendToOnboard(sb_rxiry.toString());

        mMainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "发送：" + msg_rxiry, Toast.LENGTH_SHORT).show();
            }
        });

        if (mFlightController != null && mFlightController.isConnected()) {
            byte bytes[];
            try {
                bytes = msg_rxiry.getBytes("ASCII");
            } catch (UnsupportedEncodingException e) {
                bytes = null;
                e.printStackTrace();
            }

            if (null == bytes) return;

            mFlightController.sendDataToOnboardSDKDevice(bytes, new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(final DJIError djiError) {
                    if (null != djiError) {
                        mMainLoopHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "发生错误：" + djiError.getDescription(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

//        /**
//         *  @ Timestamp # FireflyCommands [params],\r\n
//         */
//        StringBuilder sb_firefly = new StringBuilder();
//        sb_firefly.append('#');
//        sb_firefly.append('P');
//        sb_firefly.append(",\r\n");
//        final String msg_firefly = mOnboardSdkMsgHelper.getSendToOnboard(sb_firefly.toString());
//
//        mMainLoopHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(getApplicationContext(), "发送：" + msg_firefly, Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        if (mFlightController != null && mFlightController.isConnected()) {
//            byte bytes[];
//            try {
//                bytes = msg_firefly.getBytes("ASCII");
//            } catch (UnsupportedEncodingException e) {
//                bytes = null;
//                e.printStackTrace();
//            }
//
//            if (null == bytes) return;
//
//            mFlightController.sendDataToOnboardSDKDevice(bytes, new DJICommonCallbacks.DJICompletionCallback() {
//                @Override
//                public void onResult(final DJIError djiError) {
//                    if (null != djiError) {
//                        mMainLoopHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(), "发生错误：" + djiError.getDescription(), Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                }
//            });
//        }
    }

    class btnC2OnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            measure();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        if (null != mRemoteController) {
            mRemoteController.setHardwareStateUpdateCallback(null);
        }
        super.onDestroy();
    }

    protected class StageManager {
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