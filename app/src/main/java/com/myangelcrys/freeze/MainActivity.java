package com.myangelcrys.freeze;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.DialogPlusBuilder;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton btn_add= (FloatingActionButton) findViewById(R.id.btn_add_app);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialogPlus().create().show();
            }
        });
        new DialogAsyncTask(this,getPkgLoadingTask()).execute();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_free_all);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new DialogAsyncTask(MainActivity.this, getFreeAll(view)).execute();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private DialogPlusBuilder getDialogPlus() {
        DialogPlusBuilder dialogPlus= DialogPlus.newDialog(MainActivity.this);
        final MySimpleAdpter adpter= (MySimpleAdpter) getAdapter(getNotInPrefsApps());
        dialogPlus.setAdapter(adpter);
        final GridHolder holder=new GridHolder(4);
        dialogPlus.setContentHolder(holder);
        dialogPlus.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogPlus dialog) {
                refresh(getPrefsApps());
            }
        });
        dialogPlus.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                adpter.getmData().remove(position);
                final String pkg= (String) view.getTag();
                new DialogAsyncTask(MainActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        MySharedPref.addApp(getApplicationContext(),pkg);
                        Utils.enable(pkg,false);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.setAdapter(adpter);
                            }
                        });
                    }
                }).execute();
            }
        });
        dialogPlus.setContentHeight(dialogPlus.getDefaultContentHeight()*5/2);
        return dialogPlus;
    }

    private Runnable getFreeAll(final View view) {
        return new Runnable() {
                    @Override
                    public void run() {
                        freezeAll();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(view, "freeze", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        });
                    }
                };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private Runnable getPkgLoadingTask(){
        final GridView gridView = (GridView) findViewById(R.id.grid_view);
        Runnable run = new Runnable() {
            @Override
            public void run() {
                final HashSet<String> apps = MySharedPref.getAppsHashSet(MainActivity.this);
                final MySimpleAdpter simpleAdapter = (MySimpleAdpter) getAdapter(new AppFilter() {
                    @Override
                    public boolean isInclude(ApplicationInfo applicationInfo) {
                        return apps.contains(applicationInfo.packageName);
                    }
                });
                final View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        new DialogAsyncTask(MainActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                Utils.startApp(MainActivity.this, (String) v.getTag());
                            }
                        }).execute();
                    }
                };
                final View.OnLongClickListener l = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        List<String> out;
                        ApplicationInfo app = null;
                        try {
                            app = getPackageManager().getApplicationInfo("" + v.getTag(), PackageManager.GET_META_DATA);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            return false;
                        }
                        if (app.enabled) {
                            out = Utils.enable(app.packageName, false);
                            if (v instanceof ViewGroup) {
                                TextView textView = (TextView) v.findViewById(R.id.app_name);
                                textView.setTextColor(Color.RED);
                            }
                        } else {
                            out = Utils.enable(app.packageName, true);
                            if (v instanceof ViewGroup) {
                                TextView textView = (TextView) v.findViewById(R.id.app_name);
                                textView.setTextColor(Color.GREEN);
                            }
                        }
                        if (out.size() != 0)
                            Toast.makeText(MainActivity.this, out.get(0), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                };
                simpleAdapter.addListener(new MySimpleAdpter.Listener() {
                    @Override
                    public void onCreateView(final View view, Map map) {
                        view.setTag(map.get("pkg"));
                        view.setOnLongClickListener(l);
                        view.setOnClickListener(clickListener);
                    }
                });
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        gridView.setAdapter(simpleAdapter);
                    }
                });
            }
        };
        return run;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id==R.id.refresh){
            refresh(getPrefsApps());
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh(final AppFilter appFilter) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                GridView view= (GridView) findViewById(R.id.grid_view);
                MySimpleAdpter mySimpleAdpter= (MySimpleAdpter) view.getAdapter();
                if (mySimpleAdpter==null)return;
                mySimpleAdpter.getmData().clear();
                mySimpleAdpter.getmData().addAll(getData(appFilter));
                view.setAdapter(mySimpleAdpter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh(getPrefsApps());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    interface AppFilter{
        boolean isInclude(ApplicationInfo applicationInfo);
    }
    private SimpleAdapter getAdapter(AppFilter appFilter){
        MySimpleAdpter mySimpleAdpter=new MySimpleAdpter(this,
                getData(appFilter),R.layout.grid_item,new String[]{"img","text"},new int[]{R.id.ic,R.id.app_name});
        mySimpleAdpter.addListener(new MySimpleAdpter.Listener() {
            @Override
            public void onCreateView(View view, Map map) {
                view.setTag(map.get("pkg"));
                if (view instanceof ViewGroup){
                    TextView textView= (TextView) view.findViewById(R.id.app_name);
                    if (isEnabled((String) view.getTag()))textView.setTextColor(Color.GREEN);
                    else textView.setTextColor(Color.RED);
                }
            }
        });
        return mySimpleAdpter;
    }
    private List<Map<String, ?>> getData(AppFilter appFilter){
        List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(0);
        List<Map<String, ?>> data=new ArrayList<>();
        for (ApplicationInfo app:packages){
            if (!appFilter.isInclude(app))continue;
            Map<String,Object>map=new HashMap<>();
            map.put("img",app.loadIcon(getPackageManager()));
            map.put("text",app.loadLabel(getPackageManager()));
            map.put("pkg",app.packageName);
            if (app.enabled) data.add(map);
            else data.add(0,map);
        }
        return data;
    }
    private boolean isEnabled(String pkg){
        try {
            return getPackageManager().getApplicationInfo(pkg,0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void freezeAll(){
        Set<String>r=MySharedPref.getApps(getApplicationContext());
        for (String pkg:r){
            if (isEnabled(pkg))Utils.enable(pkg,false);
        }
    }
    private AppFilter getPrefsApps(){
        final HashSet<String>apps=MySharedPref.getAppsHashSet(getApplicationContext());
        return new AppFilter() {
            @Override
            public boolean isInclude(ApplicationInfo applicationInfo) {
                return isUserApp(applicationInfo)&&apps.contains(applicationInfo.packageName);
            }
        };
    }

    private AppFilter getNotInPrefsApps(){
        final HashSet<String>apps=MySharedPref.getAppsHashSet(getApplicationContext());
        return new AppFilter() {
            @Override
            public boolean isInclude(ApplicationInfo applicationInfo) {
                return isUserApp(applicationInfo)&&!apps.contains(applicationInfo.packageName);
            }
        };
    }
    private void init(){
        handler=new Handler(Looper.getMainLooper());
    }
    private boolean isUserApp(ApplicationInfo applicationInfo){
        return (applicationInfo.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))==0;
    }
}
