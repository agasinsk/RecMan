package com.agasinsk.recman.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.agasinsk.recman.audioconverter.AndroidAudioConverter;
import com.agasinsk.recman.audioconverter.IConvertCallback;
import com.agasinsk.recman.helpers.BundleConstants;
import com.agasinsk.recman.models.AudioFormat;

import java.io.File;

public class ConversionJobService extends JobIntentService {
    private static final String TAG = ConversionJobService.class.getSimpleName();
    public static final int RESULT_CONVERSION_OK = 122;
    public static final int RESULT_CONVERSION_FAILED = 123;
    public static final int RESULT_CONVERSION_STARTED = 121;

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
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ConversionJobService.class, AUDIO_CONVERSION_JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.i(TAG, "Executing work: " + intent);
        String audioFormat = intent.getStringExtra(BundleConstants.AUDIO_FORMAT);
        String audioDetails = intent.getStringExtra(BundleConstants.AUDIO_DETAILS);
        String filePath = intent.getStringExtra(BundleConstants.FILE_PATH);
        int fileId = intent.getIntExtra(BundleConstants.FILE_ID, 0);
        int fileCount = intent.getIntExtra(BundleConstants.FILE_TOTAL_COUNT, 0);
        mResultReceiver = intent.getParcelableExtra(BundleConstants.RECEIVER);

        File fileToConvert = new File(filePath);

        AndroidAudioConverter.with(getApplicationContext())
                .setFile(fileToConvert)
                .setFormat(AudioFormat.valueOf(audioFormat))
                .setAudioFormatDetails(audioDetails)
                .setCallback(new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {
                        Log.i(TAG, "File " + convertedFile.getName() + " successfully converted!");
                        sendResultWithBundle(RESULT_CONVERSION_OK, convertedFile.getPath(), convertedFile.getName(), fileId, fileCount);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e(TAG, "An error occurred during file conversion!", error);
                        sendResultWithBundle(RESULT_CONVERSION_FAILED, filePath, fileToConvert.getName(), fileId, fileCount);
                    }

                    @Override
                    public void onStart() {
                        sendResultWithBundle(RESULT_CONVERSION_STARTED, filePath, fileToConvert.getName(), fileId, fileCount);
                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onProgress(String message) {

                    }
                })
                .convert();
    }

    private void sendResultWithBundle(int resultCode, String filePath, String fileName, int fileId, int fileCount) {
        Bundle bundle = new Bundle();
        bundle.putString(BundleConstants.FILE_PATH, filePath);
        bundle.putString(BundleConstants.FILE_NAME, fileName);
        bundle.putInt(BundleConstants.FILE_ID, fileId);
        bundle.putInt(BundleConstants.FILE_TOTAL_COUNT, fileCount);
        mResultReceiver.send(resultCode, bundle);
    }
}
