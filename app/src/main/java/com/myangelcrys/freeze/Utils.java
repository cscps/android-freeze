package com.myangelcrys.freeze;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;

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
        try {
            if (!context.getPackageManager().getApplicationInfo(pkg,0).enabled)enable(pkg,true);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Intent intent=context.getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent==null)return;
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public static Intent createShortCutIntent(Context context){
        Intent intent=new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,"zZ");
        Bitmap parce= ((BitmapDrawable) context.getApplicationInfo().loadIcon(context.getPackageManager())).getBitmap();
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,parce);
        Intent launch=new Intent();
        launch.putExtra(StartAppReceiver.ENABLE_BROADCAST_EXTRA,false);
        launch.setClass(context,StartAppActivity.class);
        launch.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,launch);
        return intent;
    }
    public static Bitmap bitmapConvert(Bitmap bitmap){
        float len = Math.max(bitmap.getWidth(), bitmap.getHeight());
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        //scale to 1/10 of the device width
        int i = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / 4;
/*        int i = 24 * (-1 + Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) / 96);
        if (i > 360) {
            i= 360;
        }*/
        float f2 = i / len;
        Bitmap result = Bitmap.createBitmap(i, i, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        Canvas canvas = new Canvas(result);
        paint.setAntiAlias(true);
        matrix.setScale(f2, f2);
        canvas.drawBitmap(bitmap, matrix, paint);
        return result;
    }
}
