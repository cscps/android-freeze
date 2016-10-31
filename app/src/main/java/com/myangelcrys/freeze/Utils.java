package com.myangelcrys.freeze;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cs on 16-10-30.
 */

public class Utils {
    public static List<String>enable(String packageName,boolean enable){
        List<String> out=new ArrayList<>();
        if (enable){
            MyApplication.getRootUtil().execute("pm enable " + packageName,out);
        }
        else {
            MyApplication.getRootUtil().execute("pm disable " + packageName,out);
        }
        return out;
    }
    public static void startApp(Context context,String pkg){
        enable(pkg,true);
        Intent intent=context.getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent==null)return;
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(intent);
    }
}
