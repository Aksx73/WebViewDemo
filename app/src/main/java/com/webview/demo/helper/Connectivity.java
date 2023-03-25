package com.webview.demo.helper;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.telephony.TelephonyManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * Check device's network connectivity and speed 
 *
 *
 */
public class Connectivity {

    /**
     * Get the network info

     */
    private static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo();
    }

    /**
     * Check if there is any connectivity

     */
    public static boolean isConnected(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);

        return (info != null && info.isConnectedOrConnecting());

    }

    /**
     * Check if there is any connectivity to a Wifi network

     */
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);

        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network

     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is fast connectivity

     */
    public static boolean isConnectedFast(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(),info.getSubtype()));
    }

    /**
     * Check if the connection is fast

     */
    private static boolean isConnectionFast(int type, int subType){
        if(type== ConnectivityManager.TYPE_WIFI){
            return true;
        }else if(type== ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        }else{
            return false;
        }
    }
   /* public static String getntype(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        String s;
        if(info != null){
            s = info.getTypeName();
        }else{s = context.getString(R.string.network_unavailable);}
        return s;}


    public static String getnstype(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        String s;
        if(info != null){
            // String ns =getnetworkgen(info.getType(),info.getSubtype());
            s = info.getSubtypeName();
        }else{s = context.getString(R.string.unavailable);}
        return s;}


    public static String getextrai(Context context){
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        String s;
        if(info != null){
            s = info.getExtraInfo();
        }else{s = context.getString(R.string.network_unavailable);}
        return s;

    }*/

    public static String getnetworkgen(int type, int subType){
        if(type== ConnectivityManager.TYPE_WIFI){
            return "WiFi";
        }else if(type== ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return "";
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return "";
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return "(2.75G)";
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return "";
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return "";
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return "";
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "(2.5G)";
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return "(3.5G)";
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return "(3.5G)";
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "(3.75G)";
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return "(3.75G)";

                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return ""; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "(4G)"; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "(3G)"; // ~ 400-7000 kbps

                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return "";
            }
        }else{
            return "";
        }
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";

        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1000));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1000, digitGroups)) + " " + units[digitGroups];
    }

    @SuppressLint("Range")
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static boolean isWhatappInstalled(Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean whatsappInstalled;
        try {
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            whatsappInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            whatsappInstalled = false;
        }
        return whatsappInstalled;
    }

    public static boolean isWhatappBusinessInstalled(Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean whatsappInstalled;
        try {
            packageManager.getPackageInfo("com.whatsapp.w4b", PackageManager.GET_ACTIVITIES);
            whatsappInstalled = true;
        } catch (PackageManager.NameNotFoundException e) {
            whatsappInstalled = false;
        }
        return whatsappInstalled;
    }

    public static String addThousandComma(long d) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,##,###");
        return formatter.format(d);
    }

}