package com.rain.shieh.tcpserverclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * author: xiecong
 * create time: 2018/3/15 11:20
 * lastUpdate time: 2018/3/15 11:20
 */

public class SharedPreferencesdUtil {

    private static final String SHARED_PATH = "ubt_shared";
    private static SharedPreferences sharedPreferences;

    private static SharedPreferences getDefaultSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(SHARED_PATH,
                    Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(key, 0);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putString(key, value);
        edit.apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, "");
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    public static boolean getBoolean(Context context, String key,
                                     boolean defValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defValue);
    }

    public static void remove(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.remove(key);
        edit.apply();
    }

    public static void clean(Context context) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.clear().apply();
    }

    public static long getLong(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        return sharedPreferences.getLong(key, 0);
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        edit.apply();
    }

}