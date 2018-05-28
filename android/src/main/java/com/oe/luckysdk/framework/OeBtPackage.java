package com.oe.luckysdk.framework;

import android.app.Activity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OeBtPackage implements ReactPackage {
    static final String TAG = "OeBt";

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
        OeBtNativeModule oeBtModule = new OeBtNativeModule(reactApplicationContext);

        List<NativeModule> nativeModules = new ArrayList<>();
        nativeModules.add(oeBtModule);
        return nativeModules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        return new ArrayList<>();
    }
}
