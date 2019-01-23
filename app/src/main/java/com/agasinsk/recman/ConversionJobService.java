package com.agasinsk.recman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.agasinsk.recman.helpers.BundleConstants;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class ConversionJobService extends JobIntentService {
    private static final String TAG = ConversionJobService.class.getSimpleName();
    public static final int RESULT_CONVERSION_OK = 122;
    public static final int RESULT_CONVERSION_FAILED = 123;

    /**
     * Result receiver object to send results
     */
    private ResultReceiver mResultReceiver;

    /**
     * Unique job ID for this service.
     */
    private static final int AUDIO_CONVERSION_JOB_ID = 1000;


    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ConversionJobService.class, AUDIO_CONVERSION_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.i(TAG, "Executing work: " + intent);
        String audioFormat = intent.getStringExtra(BundleConstants.AUDIO_FORMAT);
        String filePath = intent.getStringExtra(BundleConstants.FILE_PATH);
        mResultReceiver = intent.getParcelableExtra(BundleConstants.RECEIVER);

        File fileToConvert = new File(filePath);

        AndroidAudioConverter.with(getApplicationContext())
                .setFile(fileToConvert)
                .setFormat(AudioFormat.valueOf(audioFormat))
                .setCallback(new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {
                        Log.i(TAG, "File " + convertedFile.getName() + " successfully converted!");
                        Bundle bundle = new Bundle();
                        bundle.putString(BundleConstants.CONVERTED_FILE_PATH, convertedFile.getPath());
                        bundle.putString(BundleConstants.CONVERTED_FILE_NAME, convertedFile.getName());
                        mResultReceiver.send(RESULT_CONVERSION_OK, bundle);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e(TAG, "An error occurred during file conversion!", error);
                        Bundle bundle = new Bundle();
                        bundle.putString(BundleConstants.ERROR_FILE_PATH, filePath);
                        mResultReceiver.send(RESULT_CONVERSION_FAILED, bundle);
                    }
                })
                .convert();
    }
}
