package com.myangelcrys.freeze;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.DialogPlusBuilder;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cs on 16-11-22.
 */
public class ResolverActivity extends Activity {
    final String key="com.myangelcrys.freeze.protect";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent()!=null&&getIntent().getBooleanExtra(key,true)){
            Intent intent= getIntent().setComponent(null);
            intent.putExtra(key,false);
            List<ResolveInfo> res = getPackageManager().queryIntentActivities(intent, PackageManager.GET_DISABLED_COMPONENTS);
            getDialogPlus(res).show();
            getIntent().putExtra(key,false);
        }
        else {
            Toast.makeText(this, R.string.not_found_prog,Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private DialogPlus getDialogPlus(List<ResolveInfo> r){
        DialogPlusBuilder dg = DialogPlus.newDialog(this);
        dg.setContentHolder(new ListHolder());
        MySimpleAdpter adpter=getAdpter(r);
        dg.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogPlus dialog) {
                ResolverActivity.this.finish();
            }
        });
        adpter.addListener(new MySimpleAdpter.Listener() {
            @Override
            public void onCreateView(View view, Map map) {
                view.setTag(map.get("res"));
            }
        });
        dg.setAdapter(adpter);
//        dg.setExpanded(true,300);
        dg.setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        dg.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                final ResolveInfo res= (ResolveInfo) view.getTag();
                new DialogAsyncTask(ResolverActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        if (!getMyApplication().isEnabled(res.activityInfo.packageName)){
                            Utils.enable(res.activityInfo.packageName,true);
                        }
                        ActivityInfo info = res.activityInfo;
                        ComponentName cn=new ComponentName(info.packageName,info.name);
                        ResolverActivity.this.startActivity(ResolverActivity.this.getIntent().setComponent(cn));
                        ResolverActivity.this.finish();
                    }
                }).execute();
            }
        });
        return dg.create();
    }
    private MySimpleAdpter getAdpter(List<ResolveInfo> resolveInfos){
        List<Map<String, ?>> data=new ArrayList<>();
        for (ResolveInfo res:resolveInfos){
            Map<String,Object>m=new HashMap<>();
            m.put("img",res.loadIcon(getPackageManager()));
            m.put("text",res.loadLabel(getPackageManager()));
            m.put("res",res);
            data.add(m);
        }
        return new MySimpleAdpter(this,
                data, R.layout.list_item, new String[]{"img", "text"}, new int[]{R.id.ic, R.id.app_name});
    }
    private MyApplication getMyApplication(){
        return (MyApplication) getApplication();
    }
}
