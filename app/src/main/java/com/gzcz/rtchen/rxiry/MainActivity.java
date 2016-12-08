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
import android.widget.TextView;

import dji.sdk.base.DJIBaseProduct;
import dji.sdk.flightcontroller.DJIFlightController;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    TextView tv_DeviceInfo = null;
    Button   btn_History = null;
    Button   btn_Working = null;

    DJIBaseProduct mProduct = null;

    private static Handler mUIHandler = new Handler(Looper.getMainLooper());
    //public  static SimpleDataManager mSDM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the following permissions at runtime to ensure the SDK work well.
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

        setContentView(R.layout.activity_main);

        tv_DeviceInfo = (TextView) findViewById(R.id.tv_DeviceInfo);
        btn_History = (Button) findViewById(R.id.btn_History);
        btn_Working = (Button) findViewById(R.id.btn_Working);
        btn_History.setOnClickListener(myBtnOnClickListener);
        btn_Working.setOnClickListener(myBtnOnClickListener);
        // TODO: 2016/11/15 Clean the comment but keep the code.
        //btn_Working.setEnabled(false);

        //mSDM = new SimpleDataManager(getApplication());

        IntentFilter filter = new IntentFilter();
        filter.addAction(DjiSdkApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    private View.OnClickListener myBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.btn_History:
                    intent = new Intent(MainActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_Working:
                    intent = new Intent(MainActivity.this, WorkActivity.class);
                    startActivity(intent);
//                    finish();
                    break;
                default:
                    Log.d(TAG, "onClick: " + "Error! Unknown button.");
            }
        }
    };

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Comes into the BroadcastReceiver");

            mProduct = DjiSdkApplication.getAircraftInstance();

            if (null != mProduct && mProduct.isConnected()) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_DeviceInfo.setText(mProduct.getModel().getDisplayName() + " 已连接。");
                    }
                });

                DJIFlightController fc = DjiSdkApplication.getAircraftInstance().getFlightController();
                if (fc.isOnboardSDKDeviceAvailable()) {
                    mUIHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_DeviceInfo.append("\r\n" + "Onboard SDK 已就绪。");
                        }
                    });
                }

                btn_Working.setEnabled(true);
            } else {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_DeviceInfo.setText("等待设备连接");
                    }
                });
                // TODO: 2016/11/15 Clean the comment.
                //btn_Working.setEnabled(false);
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
