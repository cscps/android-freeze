package com.myangelcrys.freeze;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import static com.myangelcrys.freeze.StartAppReceiver.ENABLE_BROADCAST_EXTRA;
import static com.myangelcrys.freeze.StartAppReceiver.START_BROADCAST_EXTRA;

/**
 * Created by cs on 16-11-2.
 */
public class StartAppActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent()==null)return;
        String pkg = getIntent().getStringExtra(START_BROADCAST_EXTRA);
        if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())){
            setResult(RESULT_OK,Utils.createShortCutIntent(this));
            finish();
        }
        else if (pkg!=null){
            Utils.startApp(this,pkg);
            finish();
        }
        else {
            Intent intent=new Intent();
            intent.setClass(this,FreezeService.class);
            intent.putExtra(ENABLE_BROADCAST_EXTRA,false);
            startService(intent);
            finish();
        }
    }
}
