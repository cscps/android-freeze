package com.myangelcrys.freeze;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cs on 16-10-31.
 */

public class MySharedPref {
    public static String PREF_FREEZE="freeze";
    static final String key="freeze";
    static HashMap<String,SharedPreferences>map=new HashMap<>();
    public static SharedPreferences getInstance(Context context,String name){
        if (map.get(name)==null)map.put(name,context.getSharedPreferences(name,Context.MODE_PRIVATE));
        return map.get(name);
    }
    public static Set<String> getApps(Context context){
        return getInstance(context,PREF_FREEZE).getStringSet(key,new HashSet<String>());
    }
    public static HashSet<String> getAppsHashSet(Context context){
        final HashSet<String>apps=new HashSet<>();
        apps.addAll(getApps(context));
        return apps;
    }
    public static void saveApps(Context context,Set<String>apps){
        SharedPreferences.Editor editor = getInstance(context, PREF_FREEZE).edit();
        editor.putStringSet(PREF_FREEZE,apps);
        editor.apply();
    }
    public static void addApp(Context context,String app){
        SharedPreferences.Editor editor = getInstance(context, PREF_FREEZE).edit();
        Set<String>t= getAppsHashSet(context);
        t.add(app);
        editor.putStringSet(PREF_FREEZE,t);
        editor.apply();
    }
}
