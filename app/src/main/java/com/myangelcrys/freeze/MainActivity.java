package com.myangelcrys.freeze;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.DialogPlusBuilder;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Handler handler;
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton btn_add = (FloatingActionButton) findViewById(R.id.btn_add_app);
/*        try {
            Process a = Runtime.getRuntime().exec("pm hide com.myangelcrys.freeze");
            BufferedInputStream bf=new BufferedInputStream(a.getInputStream());
            byte[]b=new byte[1024];
            bf.read(b);
            Toast.makeText(MainActivity.this,new String(b),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"error "+e.getMessage(),Toast.LENGTH_LONG).show();
        }*/
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogAsyncTask(MainActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        final DialogPlus dialog = getDialogPlus().create();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.show();
                            }
                        });
                    }
                }).execute();
            }
        });
        new DialogAsyncTask(this, getGridViewInitTask()).execute();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_free_all);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                new DialogAsyncTask(MainActivity.this,
                        new Runnable() {
                            @Override
                            public void run() {
                                enableAll(false);
                                refresh(getPrefsApps());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(view, "已全部休眠", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                });
                            }
                        }
                ).execute();
            }
        });

/*        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/
    }

    private DialogPlusBuilder getDialogPlus() {
        DialogPlusBuilder dialogPlus = DialogPlus.newDialog(MainActivity.this);
        final MySimpleAdpter adpter = (MySimpleAdpter) getAdapter(getNotInPrefsApps());
        dialogPlus.setAdapter(adpter);
        final GridHolder holder = new GridHolder(4);
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
                final String pkg = (String) view.getTag();
                new DialogAsyncTask(MainActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        MySharedPref.addApp(getApplicationContext(), pkg);
                        if (isEnabled(pkg)) Utils.enable(pkg, false);
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
        dialogPlus.setContentHeight(dialogPlus.getDefaultContentHeight() * 5 / 2);
        return dialogPlus;
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

    private Runnable getGridViewInitTask() {
        final GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.select_mode, menu);
                isSelectionMode = true;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.select_all:
                        for (int i = 0; i < gridView.getChildCount(); i++) {
                            gridView.setItemChecked(i, true);
                        }
                        break;
                    case R.id.deselect_all:
                        for (int i = 0; i < gridView.getChildCount(); i++) {
                            gridView.setItemChecked(i, false);
                        }
                        mode.finish();
                        break;
                    case R.id.sleep:
                        new DialogAsyncTask(MainActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                enableAll(getCheckedPkg(), false);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mode.finish();
                                        refresh(getPrefsApps());
                                        Toast.makeText(MainActivity.this,"已休眠",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).execute();
                        break;
                    case R.id.del:
                        new DialogAsyncTask(MainActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                HashSet<String> s = MySharedPref.getAppsHashSet(MainActivity.this);
                                HashSet<String> checked = getCheckedPkg();
                                s.removeAll(checked);
                                MySharedPref.saveApps(getApplicationContext(), s);
                                enableAll(checked, true);
                                refresh(getPrefsApps());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mode.finish();
                                        Toast.makeText(MainActivity.this,"已移除",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).execute();
                        break;
                    case R.id.wake:
                        new DialogAsyncTask(MainActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                HashSet<String> checked = getCheckedPkg();
                                enableAll(checked, true);
                                refresh(getPrefsApps());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mode.finish();
                                        Toast.makeText(MainActivity.this,"已唤醒",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).execute();
                        break;
                    case R.id.create_shortcuts:
                        new DialogAsyncTask(MainActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                HashSet<String> checked = getCheckedPkg();
                                for (String pkg:checked){
                                    Intent intent=new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                                    try {
                                        ApplicationInfo app=getPackageManager().getApplicationInfo(pkg,PackageManager.GET_META_DATA);
                                        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,app.loadLabel(getPackageManager()));
                                        Bitmap icon= ((BitmapDrawable) app.loadIcon(getPackageManager())).getBitmap();
                                        Intent launch=new Intent();
                                        launch.putExtra(StartAppReceiver.START_BROADCAST_EXTRA,pkg);
                                        launch.setClass(MainActivity.this,StartAppActivity.class);
                                        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,launch);
                                        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,Utils.bitmapConvert(icon));
                                        sendBroadcast(intent);
                                    } catch (PackageManager.NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mode.finish();
                                        Toast.makeText(MainActivity.this,"已全部创建",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).execute();
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                isSelectionMode = false;
            }
        });
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
                simpleAdapter.addListener(new MySimpleAdpter.Listener() {
                    @Override
                    public void onCreateView(final View view, Map map) {
                        view.setTag(map.get("pkg"));
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

    private HashSet<String> getCheckedPkg() {
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        SparseBooleanArray ps = gridView.getCheckedItemPositions();
        HashSet<String> checked = new HashSet<>();
        for (int i = 0; i < ps.size(); i++) {
            if (!ps.get(ps.keyAt(i))) continue;
            String pkg = (String) gridView.getChildAt(ps.keyAt(i)).getTag();
            checked.add(pkg);
        }
        return checked;
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
        switch (id){
            case R.id.refresh:
                refresh(getPrefsApps());
                break;
            case R.id.sleep_shortcut:
                sendBroadcast(Utils.createShortCutIntent(MainActivity.this));
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh(final AppFilter appFilter) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                GridView view = (GridView) findViewById(R.id.grid_view);
                MySimpleAdpter mySimpleAdpter = (MySimpleAdpter) view.getAdapter();
                if (mySimpleAdpter == null) return;
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

    private SimpleAdapter getAdapter(AppFilter appFilter) {
        MySimpleAdpter mySimpleAdpter = new MySimpleAdpter(this,
                getData(appFilter), R.layout.grid_item, new String[]{"img", "text"}, new int[]{R.id.ic, R.id.app_name});
        mySimpleAdpter.addListener(new MySimpleAdpter.Listener() {
            @Override
            public void onCreateView(View view, Map map) {
                view.setTag(map.get("pkg"));
                if (view instanceof ViewGroup) {
                    TextView textView = (TextView) view.findViewById(R.id.app_name);
                    if (isEnabled((String) view.getTag())) textView.setTextColor(Color.GREEN);
                    else textView.setTextColor(Color.RED);
                }
            }
        });
        return mySimpleAdpter;
    }

    private List<Map<String, ?>> getData(AppFilter appFilter) {
        final Collator c = Collator.getInstance(getResources().getConfiguration().locale);
        List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(0);
        List<Map<String, ?>> data = new ArrayList<>();
        List<Map<String, ?>> sys = new ArrayList<>();
        for (ApplicationInfo app : packages) {
            if (!appFilter.isInclude(app)) continue;
            if (getApplication().getPackageName().equals(app.packageName)) continue;
            Map<String, Object> map = new HashMap<>();
            map.put("img", app.loadIcon(getPackageManager()));
            map.put("text", app.loadLabel(getPackageManager()));
            map.put("pkg", app.packageName);
            if (!isUserApp(app)) {
                sys.add(map);
            } else if (app.enabled) data.add(map);
            else data.add(0, map);
        }
        Collections.sort(data, new Comparator<Map<String, ?>>() {
            @Override
            public int compare(Map<String, ?> o1, Map<String, ?> o2) {
                return c.compare(o1.get("text"),o2.get("text"));
            }
        });
        Collections.sort(sys, new Comparator<Map<String, ?>>() {
            @Override
            public int compare(Map<String, ?> o1, Map<String, ?> o2) {
                return c.compare(o1.get("text"),o2.get("text"));
            }
        });
        data.addAll(sys);
        return data;
    }

    private boolean isEnabled(String pkg) {
        try {
            return getPackageManager().getApplicationInfo(pkg, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void enableAll(Set<String> r, boolean enable) {
        for (String pkg : r) {
            if (enable) {
                if (!isEnabled(pkg)) Utils.enable(pkg, true);
            } else {
                if (isEnabled(pkg)) Utils.enable(pkg, false);
            }
        }
    }

    private void enableAll(boolean enable) {
        Set<String> r = MySharedPref.getApps(getApplicationContext());
        for (String pkg : r) {
            if (enable) {
                if (!isEnabled(pkg)) Utils.enable(pkg, true);
            } else {
                if (isEnabled(pkg)) Utils.enable(pkg, false);
            }
        }
    }

    private AppFilter getPrefsApps() {
        final HashSet<String> apps = MySharedPref.getAppsHashSet(getApplicationContext());
        return new AppFilter() {
            @Override
            public boolean isInclude(ApplicationInfo applicationInfo) {
                return apps.contains(applicationInfo.packageName);
            }
        };
    }

    private AppFilter getNotInPrefsApps() {
        final HashSet<String> apps = MySharedPref.getAppsHashSet(getApplicationContext());
        return new AppFilter() {
            @Override
            public boolean isInclude(ApplicationInfo applicationInfo) {
//                return isUserApp(applicationInfo)&&!apps.contains(applicationInfo.packageName);
                return !apps.contains(applicationInfo.packageName) &&
                        !applicationInfo.packageName.contains("com.android") &&
                        !applicationInfo.packageName.contains("com.google.android") &&
                        !applicationInfo.packageName.contains("supersu") &&
                        !applicationInfo.packageName.equals("android");
            }
        };
    }

    private void init() {
        handler = new Handler(Looper.getMainLooper());
        final GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (isSelectionMode) return;
                new DialogAsyncTask(MainActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        Utils.startApp(MainActivity.this, (String) gridView.getChildAt(position).getTag());
                    }
                }).execute();
            }
        });
    }

    private boolean isUserApp(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0;
    }
}
