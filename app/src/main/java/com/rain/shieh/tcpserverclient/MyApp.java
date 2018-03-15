package com.rain.shieh.tcpserverclient;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

/**
 * author: xiecong
 * create time: 2018/3/15 15:15
 * lastUpdate time: 2018/3/15 15:15
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        ThreadPoolCreator.get().create();
    }
}
