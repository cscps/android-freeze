package com.myangelcrys.freeze;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by cs on 16-11-2.
 */
public class StartAppReceiver extends BroadcastReceiver {
    public static final String START_BROADCAST_EXTRA = "com.myangelcrys.freeze.start";
    public static final String ENABLE_BROADCAST_EXTRA = "com.myangelcrys.freeze.freeze";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String pkg = intent.getStringExtra(START_BROADCAST_EXTRA);
        if (pkg==null)return;
        Utils.enable(pkg,true);
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(pkg);
        launch.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(launch);
    }
}
