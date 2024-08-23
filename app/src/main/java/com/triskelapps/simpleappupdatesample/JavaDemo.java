package com.triskelapps.simpleappupdatesample;

import android.content.Context;

import com.triskelapps.simpleappupdate.SimpleAppUpdate;

public class JavaDemo {

    public void demo(Context context) {
        SimpleAppUpdate simpleAppUpdate = new SimpleAppUpdate(context);
        simpleAppUpdate.cancelWork("uniqueName");
    }
}
