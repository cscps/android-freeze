package com.myangelcrys.freeze;

import eu.chainfire.libsuperuser.Application;

/**
 * Created by cs on 16-10-30.
 */

public class MyApplication extends Application{
    static private RootUtil rootUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        rootUtil=new RootUtil();
        rootUtil.startShell();
    }

    public static RootUtil getRootUtil() {
        return rootUtil;
    }
}
