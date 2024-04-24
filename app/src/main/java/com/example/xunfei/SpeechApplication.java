package com.example.xunfei;
import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

public class SpeechApplication extends Application{
    public void onCreate() {
        SpeechUtility.createUtility(SpeechApplication.this, "appid=2528c593");
        super.onCreate();
    }
}