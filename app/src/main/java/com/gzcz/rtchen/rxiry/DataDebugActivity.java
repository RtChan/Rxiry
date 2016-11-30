package com.gzcz.rtchen.rxiry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;

public class DataDebugActivity extends AppCompatActivity {

    private static final String TAG = "TAG";

    DJIFlightController mFlightController = null;
    Button btn_send = null;
    TextView tv_msgview = null;

    Button btn_takePhoto = null;
    Button btn_takeVideo = null;

    StringBuffer mSerialMessage = new StringBuffer();
    //ArrayList<String> mSerialMessageList = new ArrayList<>();
    OnboardSdkMsgHelper mOnboardSdkMsgHelper = new OnboardSdkMsgHelper();

    Spinner mSpinnerCmd  = null;
    Spinner mSpinnerParm = null;
    ArrayAdapter<String> mSpinnerCmdAdapter  = null;
    ArrayAdapter<String> mSpinnerParmAdapter = null;
    ArrayList<String> mSpinnerParmStrings = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_debug);

        btn_send = (Button) findViewById(R.id.btn_send);              //UI插件初始化
        btn_send.setOnClickListener(new btnrangingOnClickListener());

        btn_takePhoto = (Button) findViewById(R.id.btn_takephoto);
        btn_takeVideo = (Button) findViewById(R.id.btn_takevideo);
        btn_takePhoto.setOnClickListener(new btnfireflyOnClickListener());
        btn_takeVideo.setOnClickListener(new btnfireflyOnClickListener());

        tv_msgview = (TextView) findViewById(R.id.tv_msgview);

        initSpinner();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DjiSdkApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    class btnfireflyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            StringBuilder sb = new StringBuilder();
            sb.append('#');

            switch (view.getId()) {
                case R.id.btn_takephoto:
                    sb.append('P');
                    break;
                case R.id.btn_takevideo:
                    sb.append('V');
                    break;
                default:
                    return;
            }

            sb.append(",\r\n");

            /**
             *  @ Timestamp # FireflyCommands ,\r\n
             */
            final String msg = mOnboardSdkMsgHelper.getSendToOnboard(sb.toString());

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tv_msgview.append("发送：" + msg);
                }
            });

            if (mFlightController != null && mFlightController.isConnected()) {
                byte bytes[];
                try {
                    bytes = msg.getBytes("ASCII");
                } catch (UnsupportedEncodingException e) {
                    bytes = null;
                    e.printStackTrace();
                }

                if (null == bytes) return;

                mFlightController.sendDataToOnboardSDKDevice(bytes, new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(final DJIError djiError) {
                        if (null != djiError) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_msgview.append(djiError.getDescription() + "\n");
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    class btnrangingOnClickListener implements View.OnClickListener {
        //String CMD_Ranging = "$ST,\r\n";

        @Override
        public void onClick(View view) {
            StringBuilder sb = new StringBuilder();

            sb.append("$");
            sb.append(getApplication().getResources().
                    getStringArray(R.array.array_CmdsString)[
                    mSpinnerCmd.getSelectedItemPosition()]);
            if (mSpinnerParm.getSelectedItem() != "N/A") {
                sb.append(" ");
                sb.append(mSpinnerParm.getSelectedItemPosition() + 1);
            }
            sb.append(",\r\n");

            /**
             *  @ Timestamp $ RxiryCommands [params],\r\n
             */
            final String msg = mOnboardSdkMsgHelper.getSendToOnboard(sb.toString());

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tv_msgview.append("发送：" + msg);
                }
            });

            if (mFlightController != null && mFlightController.isConnected()) {
                byte bytes[];
                try {
                    bytes = msg.getBytes("ASCII");
                } catch (UnsupportedEncodingException e) {
                    bytes = null;
                    e.printStackTrace();
                }

                if (null == bytes) return;

                mFlightController.sendDataToOnboardSDKDevice(bytes, new DJICommonCallbacks.DJICompletionCallback() {
                    @Override
                    public void onResult(final DJIError djiError) {
                        if (null != djiError) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_msgview.append(djiError.getDescription() + "\n");
                                }
                            });
                        }
                    }
                });

//                Handler handler = new Handler(Looper.getMainLooper());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "Send:" + msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        }
    }

//    public void parseMessage() {
//        if (null == mSerialMessageList) return;
//        if (mSerialMessageList.isEmpty()) return;
//
//        while (!mSerialMessageList.isEmpty()) {
//            String raw = mSerialMessageList.get(0);
//            final String strtime = raw.substring(raw.indexOf('#'), raw.indexOf('$'));
//            final String strmsg = raw.substring(raw.indexOf('$'), raw.indexOf('\n'));
//
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    tv_msgview.append("[" + strtime + "]" + strmsg);
//                }
//            });
//
//            mSerialMessageList.remove(0);
//        }
//    }

    private void initSpinner() {
        /* mSpinnerCmd */
        mSpinnerCmd = (Spinner) findViewById(R.id.sp_cmd);

        //第一步:添加下拉列表的list
        String[] CmdDescriptionStrings = getResources().getStringArray(R.array.array_CmdsDescription);
        //第二步:为下拉列表定义一个适配器
        mSpinnerCmdAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, CmdDescriptionStrings);
        //第三步:为适配器设置下拉列表下拉时的菜单样式
        mSpinnerCmdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步:将适配器添加到下拉列表上
        mSpinnerCmd.setAdapter(mSpinnerCmdAdapter);
        //第五步:为下拉列表设置各种事件的响应
        mSpinnerCmd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: " + i);
                String[] items;

                switch (i) {
                    case 1:
                        items = getResources().getStringArray(R.array.array_ParaOfDU);
                        break;
                    case 2:
                        items = getResources().getStringArray(R.array.array_ParaOfAU);
                        break;
                    case 3:
                        items = getResources().getStringArray(R.array.array_ParaOfDM);
                        break;
                    default:
                        items = new String[]{"N/A"};
                }

                List<String> list = Arrays.asList(items);
                mSpinnerParmStrings.clear();
                mSpinnerParmStrings.addAll(new ArrayList<String>(list));
                mSpinnerParmAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /* mSpinnerParm */
        mSpinnerParm = (Spinner) findViewById(R.id.sp_parm);

        mSpinnerParmStrings = new ArrayList<>();
        mSpinnerParmStrings.add("N/A");

        mSpinnerParmAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mSpinnerParmStrings);
        mSpinnerParmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerParm.setAdapter(mSpinnerParmAdapter);
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* onReceive方法会在DJI产品连接状态改变时被调用 */
        @Override
        public void onReceive(Context context, Intent intent) {
            DJIBaseProduct mProduct = DjiSdkApplication.getProductInstance();

            if (mProduct instanceof DJIAircraft) {
                mFlightController = ((DJIAircraft) mProduct).getFlightController();
            }

            //已连接产品
            if (mProduct != null && mProduct.isConnected()) {

                if (mFlightController != null) {

                    mFlightController.setReceiveExternalDeviceDataCallback(new DJIFlightControllerDelegate.FlightControllerReceivedDataFromExternalDeviceCallback() {
                        @Override
                        public void onResult(final byte[] bytes) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Recv:" + new String(bytes), Toast.LENGTH_SHORT).show();
                                    //tv_msgview.append(new String(bytes));
                                }
                            });

//                            Handler handler = new Handler(Looper.getMainLooper());
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tv_msgview.append("==============\r\n");
//                                    tv_msgview.append(new String(bytes));
//                                    tv_msgview.append(String.valueOf(new String(bytes).length()));
//                                    tv_msgview.append("==============\r\n");
//                                }
//                            });

                            for (byte b : bytes) {
                                if (b == '@') {
                                    mSerialMessage.setLength(0);    // As same as clean the buffer.
                                }

                                mSerialMessage.append((char) b);

                                if (b == 0x0A) {  // The ascii of '\n' is 0x0A.
                                    final String str = mOnboardSdkMsgHelper.parseReceivedFromOnboard(new String(mSerialMessage));
                                    Log.d(TAG, "onResult: recv:" + str);

                                    if (str.isEmpty()) break;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tv_msgview.append("接收：" + str);
                                        }
                                    });
                                }
                            }

//                            parseMessage();
                        }
                    });

                }

            } else {
                mFlightController = null;
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
