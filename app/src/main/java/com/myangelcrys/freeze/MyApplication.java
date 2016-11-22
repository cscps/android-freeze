package com.myangelcrys.freeze;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public List<Map<String, ?>> getData(AppFilter appFilter) {
        final Collator c = Collator.getInstance(getResources().getConfiguration().locale);
        List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(0);
        List<Map<String, ?>> data = new ArrayList<>();
        List<Map<String, ?>> sys = new ArrayList<>();
        for (ApplicationInfo app : packages) {
            if (!appFilter.isInclude(app)) continue;
            if (getPackageName().equals(app.packageName)) continue;
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

    public boolean isEnabled(String pkg) {
        try {
            return getPackageManager().getApplicationInfo(pkg, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void enableAll(Set<String> r, boolean enable) {
        for (String pkg : r) {
            if (enable) {
                if (!isEnabled(pkg)) Utils.enable(pkg, true);
            } else {
                if (isEnabled(pkg)) Utils.enable(pkg, false);
            }
        }
    }

    public void enableAll(boolean enable) {
        Set<String> r = MySharedPref.getApps(getApplicationContext());
        for (String pkg : r) {
            if (enable) {
                if (!isEnabled(pkg)) Utils.enable(pkg, true);
            } else {
                if (isEnabled(pkg)) Utils.enable(pkg, false);
            }
        }
    }

    public AppFilter getPrefsApps() {
        final HashSet<String> apps = MySharedPref.getAppsHashSet(getApplicationContext());
        return new AppFilter() {
            @Override
            public boolean isInclude(ApplicationInfo applicationInfo) {
                return apps.contains(applicationInfo.packageName);
            }
        };
    }

    public AppFilter getNotInPrefsApps() {
        final HashSet<String> apps = MySharedPref.getAppsHashSet(getApplicationContext());
        return new AppFilter() {
            @Override
            public boolean isInclude(ApplicationInfo applicationInfo) {
//                return isUserApp(applicationInfo)&&!apps.contains(applicationInfo.packageName);
                return !apps.contains(applicationInfo.packageName) &&
//                        !applicationInfo.packageName.contains("com.android") &&
//                        !applicationInfo.packageName.contains("com.google.android") &&
                        !applicationInfo.packageName.contains("supersu") &&
                        !applicationInfo.packageName.equals("android");
            }
        };
    }
    public boolean isUserApp(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0;
    }
}
