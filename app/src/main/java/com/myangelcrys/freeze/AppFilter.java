package com.myangelcrys.freeze;

import android.content.pm.ApplicationInfo;

interface AppFilter {
        boolean isInclude(ApplicationInfo applicationInfo);
    }