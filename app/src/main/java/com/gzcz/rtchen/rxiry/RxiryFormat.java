package com.gzcz.rtchen.rxiry;

import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Created by Rt Chen on 2016/9/9.
 */
public class RxiryFormat {
    private String mMessage = null;
    private boolean mValidity = false;
    private String[] mParams = null;
    private RXDataType mType = null;

    public enum RXDataType {
        HV, HD, HT, ML, Unknown;
    }
    public enum RXUnitType {
        Metre, Yard, Degree, Percent, Unknown;
    }
    public enum RXModeType {
        HDML("HDML", "HD ML"), VDML("HDML", "VD ML"), SDML("SDML", "SD ML"), HT("HT", "HT");

        private String mode;
        private String describe;

        private RXModeType(String mode, String describe) {
            this.mode = mode;
            this.describe = describe;
        }

        @Override
        public String toString() {
            return this.describe;
        }
    }

    public class HV {
        public RXDataType   HV      = RXDataType.Unknown;
        public double       HD      = Double.NaN;
        public double       VD      = Double.NaN;
        public double       SD      = Double.NaN;
        public RXUnitType HVSUnit   = RXUnitType.Unknown;
        public double       INC     = Double.NaN;
        public RXUnitType INCUnit   = RXUnitType.Unknown;
        public double       AZ      = Double.NaN;
        public RXUnitType AZUnit    = RXUnitType.Unknown;
    }

    public class ML {
        public RXDataType   ML      = RXDataType.Unknown;
        public double       HD      = Double.NaN;
        public double       VD      = Double.NaN;
        public double       SD      = Double.NaN;
        public RXUnitType HVSUnit   = RXUnitType.Unknown;
        public double       INC     = Double.NaN;
        public RXUnitType INCUnit   = RXUnitType.Unknown;
        public double       AZ      = Double.NaN;
        public RXUnitType AZUnit    = RXUnitType.Unknown;
    }

    public class HT {
        public RXDataType   HT      = RXDataType.Unknown;
        public double     HTValue   = Double.NaN;
        public RXUnitType HTUnit    = RXUnitType.Unknown;
    }

    public RxiryFormat(String src) {
        mMessage = src;
        mValidity = isChecksumValid();
        if (mValidity) {
            mParams = mMessage.split(",");
            mType = getType();
        }
    }
    public boolean isChecksumValid(){
        /* 字符串为 null ! */
        if (null == mMessage) return false;
        /* 字符串为空！ */
        if (mMessage.isEmpty()) return false;

        /* 将字符串转换为字节数组 */
        byte[] msg = null;
        try {
             msg = mMessage.getBytes("ASCII");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            msg = null;
        }

        /* 转换失败！ */
        if (null == msg) return false;

        /* 寻找 Checksum 的索引 */
        int strEnd = mMessage.indexOf('*');

        /* 获取字符串中的 Checksum */
        int hisChecksum = 0;
        byte[] chk = {0,0};
        chk[0] = msg[strEnd + 2];
        chk[1] = msg[strEnd + 1];
        hisChecksum = Integer.valueOf(new String(chk), 16);
        Log.d("CHK", "isChecksumValid: [his]" + hisChecksum);

        /* 计算实际的 Checksum */
        int myChecksum = 0;
        for (int i = 1; i < strEnd; ++i) {
            myChecksum ^= msg[i];
        }
        Log.d("CHK", "isChecksumValid: [my]" + myChecksum);

        return (myChecksum == hisChecksum);
    }
    public RXDataType getType() {
        /* 字符串校验失败 */
        if (!mValidity) return null;
        
        return parseXRDataType(mParams[1]);
    }

    public HV getHV() {
        /* 字符串校验失败 */
        if (!mValidity) return null;
        /* 数据类型不符 */
        if (mType != RXDataType.HV) return null;
        
        // $XRPT,HV,HD,VD,SD,Unit,INC,Unit,AZ,Unit*CHK,<CR><LF>
        HV hv = new HV();

        hv.HV       = parseXRDataType(mParams[1]);
        hv.HD       = parseDouble(mParams[2]);
        hv.VD       = parseDouble(mParams[3]);
        hv.SD       = parseDouble(mParams[4]);
        hv.HVSUnit  = parseXRUnitType(mParams[5]);
        hv.INC      = parseDouble(mParams[6]);
        hv.INCUnit  = parseXRUnitType(mParams[7]);
        hv.AZ       = parseDouble(mParams[8]);
        hv.AZUnit   = parseXRUnitType(mParams[9]);

        return hv;
    }

    public ML getML() {
        /* 字符串校验失败 */
        if (!mValidity) return null;
        /* 数据类型不符 */
        if (mType != RXDataType.ML) return null;

        // $XRPT,ML,HD,VD,SD,Unit,INC,Unit,AZ,Unit*CHK,<CR><LF>
        ML ml = new ML();

        ml.ML       = parseXRDataType(mParams[1]);
        ml.HD       = parseDouble(mParams[2]);
        ml.VD       = parseDouble(mParams[3]);
        ml.SD       = parseDouble(mParams[4]);
        ml.HVSUnit  = parseXRUnitType(mParams[5]);
        ml.INC      = parseDouble(mParams[6]);
        ml.INCUnit  = parseXRUnitType(mParams[7]);
        ml.AZ       = parseDouble(mParams[8]);
        ml.AZUnit   = parseXRUnitType(mParams[9]);

        return ml;
    }

    /* 底层 “字符串转类型” 工具方法 */
    public RXDataType parseXRDataType(String str) {
        switch (str) {
            case "HV":
                return RXDataType.HV;
            case "HD":
                return RXDataType.HD;
            case "HT":
                return RXDataType.HT;
            case "ML":
                return RXDataType.ML;
            default:
                return RXDataType.Unknown;
        }
    }
    public RXUnitType parseXRUnitType(String str) {
        switch (str) {
            case "M":
                return RXUnitType.Metre;
            case "Y":
                return RXUnitType.Yard;
            case "P":
                return RXUnitType.Percent;
            case "D":
                return RXUnitType.Degree;
            default:
                return RXUnitType.Unknown;
        }
    }
    public double parseDouble(String str) {
        if (str.isEmpty())
            return Double.NaN;
        else
            return Double.valueOf(str);
    }
}
