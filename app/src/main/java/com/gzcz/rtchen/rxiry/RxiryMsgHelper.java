package com.gzcz.rtchen.rxiry;

import android.util.Log;

/**
 * Created by Rt Chen on 2016/10/4.
 */

public class RxiryMsgHelper {
    private static final String TAG = "RxiryMsgHelper";
    static long mPreTime;

    public RxiryMsgHelper() {
        mPreTime = 0;
    }

    /**
     * Add the Timestamp to the message.
     * Format: @Timestamp#DeviceCommand\r\n or @Timestamp$RxiryCommand*Checksum\r\n
     *
     * @param inStr The command will be sent to onboard sdk device.
     * @return The formatted message.
     */
    public String getSendToOnboard(String inStr) {
        StringBuilder sb = new StringBuilder();
        sb.append("@");
        sb.append(System.currentTimeMillis()/1000);
        sb.append(inStr);
        Log.d(TAG, "getSendToOnboard: str:" + sb.toString());
        return sb.toString();
    }

    /**
     * Remove the timestamp from message.
     *
     * @param inStr The message received from onboard device.
     * @return 1.return the String if message is new.
     *         2.return the ""     if message has been received.
     */
    public String parseReceivedFromOnboard(String inStr) {
        String strTime = inStr.substring(inStr.indexOf('@') + 1, inStr.indexOf('$'));
        Log.d(TAG, "parseReceivedFromOnboard: strTime:" + strTime);
        String strMsg = inStr.substring(inStr.indexOf('$'), inStr.indexOf('\n') + 1);
        Log.d(TAG, "parseReceivedFromOnboard: strMsg:" + strMsg);

        long curTime = Long.parseLong(strTime);
        if (curTime > mPreTime) {
            mPreTime = curTime;
            return strMsg;
        } else {
            return "";
        }
    }

}
