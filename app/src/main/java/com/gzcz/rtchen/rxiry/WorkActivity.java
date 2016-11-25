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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.base.DJIBaseProduct;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;

public class WorkActivity extends AppCompatActivity {
    private static final String TAG = "WorkActivity";

    private DJIBaseProduct mProduct;
    private DJIFlightController mFlightController;

    StringBuffer mSerialMessage = new StringBuffer();
    RxiryMsgHelper mRxiryMsgHelper = new RxiryMsgHelper();

    int curStep = 1;
    private TextView tv_ResultDisplay;
    ScrollView mScroll;
    Handler mMainLoopHandler = new Handler(Looper.getMainLooper());

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

        setCallbackFunction();

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
                Handler handler = new Handler(Looper.getMainLooper());

//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "Recv:" + new String(bytes), Toast.LENGTH_SHORT).show();
//                    }
//                });


                for (byte b : bytes) {
                    if (b == '@') {
                        mSerialMessage.setLength(0);    // As same as clean the buffer.
                    }

                    mSerialMessage.append((char) b);

                    if (b == 0x0A) {  // The ascii of '\n' is 0x0A.
                        final String str = mRxiryMsgHelper.parseReceivedFromOnboard(new String(mSerialMessage));
                        Log.d(TAG, "onResult: recv:" + str);

                        if (str.isEmpty()) break;

                        handler.post(new Runnable() {
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

        if (1 == curStep || 2== curStep) {
            if (rf.getType() == RxiryFormat.RXDataType.HV) {
                RxiryFormat.HV hv = rf.getHV();
                dispHV(hv, curStep);
            }
        }

        if (3 == curStep) {
            if (rf.getType() == RxiryFormat.RXDataType.ML) {
                RxiryFormat.ML ml = rf.getML();
                dispML(ml);
            }
        }

        mMainLoopHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 500);


        if (3 == curStep) {
            curStep = 1;
        } else {
            ++curStep;
        }
    }

    protected void dispHV(final RxiryFormat.HV hv, int index) {

        if (1 == index) {
            mMainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    tv_ResultDisplay.append("---测量点1---\r\n");
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

        if (2 == index) {
            mMainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    tv_ResultDisplay.append("---测量点2---\r\n");
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
    }

    protected void dispML(final RxiryFormat.ML ml) {
        mMainLoopHandler.post(new Runnable() {
            @Override
            public void run() {
                tv_ResultDisplay.append("---测量结果---\r\n");
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
                        + (ml.AZUnit  == RxiryFormat.RXUnitType.Degree ? "度" : "%"));
                tv_ResultDisplay.append("\r\n\r\n");
            }
        });
    }
}