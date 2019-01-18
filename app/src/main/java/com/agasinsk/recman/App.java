package com.agasinsk.recman;

import android.app.Application;
import android.util.Log;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                Log.i("RecMan:App", "Android Audio Converter loaded.");
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("RecMan:App", "Android Audio Converter failed to load!");
            }
        });
    }
}