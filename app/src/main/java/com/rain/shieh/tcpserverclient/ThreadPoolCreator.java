package com.rain.shieh.tcpserverclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author: xiecong
 * create time: 2018/3/15 15:20
 * lastUpdate time: 2018/3/15 15:20
 */

public class ThreadPoolCreator {

    private ExecutorService mSingleThreadExecutor;
    private ExecutorService mFixedThreadPool;
    private ExecutorService mCachedThreadPool;

    private static final class ClassHolder{
        private static final ThreadPoolCreator creator = new ThreadPoolCreator();
    }

    private ThreadPoolCreator(){

    }

    public static ThreadPoolCreator get(){
       return ClassHolder.creator;
    }

    public void create(){
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        mCachedThreadPool = Executors.newCachedThreadPool();
        mFixedThreadPool = Executors.newFixedThreadPool(10);
    }

    public ExecutorService getSingleThreadExecutor() {
        return mSingleThreadExecutor;
    }

    public ExecutorService getFixedThreadPool() {
        return mFixedThreadPool;
    }

    public ExecutorService getCachedThreadPool() {
        return mCachedThreadPool;
    }
}
