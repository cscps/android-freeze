package com.myangelcrys.freeze;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.Set;

import static com.myangelcrys.freeze.StartAppReceiver.ENABLE_BROADCAST_EXTRA;

/**
 * Created by cs on 16-11-2.
 */
public class FreezeService extends IntentService {
    Handler handler=new Handler(Looper.getMainLooper());
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public FreezeService(String name) {
        super(name);
    }

    public FreezeService() {
        this("freeze worker");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        final Boolean enable = intent.getBooleanExtra(ENABLE_BROADCAST_EXTRA,false);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                Set<String> r = MySharedPref.getApps(getApplicationContext());
                for (String p : r) {
                    if (enable) {
                        try {
                            if (!getPackageManager().getApplicationInfo(p, 0).enabled)
                                Utils.enable(p, true);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (getPackageManager().getApplicationInfo(p, 0).enabled)
                                Utils.enable(p, false);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FreezeService.this, "已全部休眠", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        run.run();
    }
}
