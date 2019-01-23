package com.agasinsk.recman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.agasinsk.recman.helpers.BundleConstants;
import com.agasinsk.recman.microsoft.graph.GraphServiceController;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.extensions.DriveItem;

import java.io.File;

public class UploadJobService extends JobIntentService {
    private static final String TAG = UploadJobService.class.getSimpleName();
    private static Context mContext;
    public static final int RESULT_UPLOAD_OK = 124;
    public static final int RESULT_UPLOAD_FAILED = 125;

    /**
     * Result receiver object to send results
     */
    private ResultReceiver mResultReceiver;

    /**
     * Unique job ID for this service.
     */
    private static final int FILE_UPLOAD_JOB_ID = 2000;
    private GraphServiceController mGraphServiceController;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        mContext = context;
        enqueueWork(context, UploadJobService.class, FILE_UPLOAD_JOB_ID, work);
    }

    GraphServiceController getGraphServiceController() {
        if (mGraphServiceController == null) {
            mGraphServiceController = new GraphServiceController(mContext);
        }
        return mGraphServiceController;
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.i(TAG, "Executing work: " + intent);
        String fileToUploadPath = intent.getStringExtra(BundleConstants.CONVERTED_FILE_PATH);
        String fileToUploadName = intent.getStringExtra(BundleConstants.CONVERTED_FILE_NAME);
        mResultReceiver = intent.getParcelableExtra(BundleConstants.RECEIVER);

        File fileToUpload = new File(fileToUploadPath);
        try {
            getGraphServiceController()
                    .uploadFileToOneDrive(fileToUpload, new ICallback<DriveItem>() {
                        @Override
                        public void success(DriveItem driveItem) {
                            Log.i(TAG, "Successfully uploaded file " + driveItem.name + " to OneDrive");
                            removeFile(fileToUploadPath);
                            Bundle bundle = new Bundle();
                            bundle.putString(BundleConstants.UPLOADED_FILE_NAME, driveItem.name);
                            mResultReceiver.send(RESULT_UPLOAD_OK, bundle);
                        }

                        @Override
                        public void failure(ClientException ex) {
                            Log.e(TAG, "Exception on file upload to OneDrive ", ex);
                            Bundle bundle = new Bundle();
                            bundle.putString(BundleConstants.UPLOADED_FILE_NAME, fileToUploadName);
                            mResultReceiver.send(RESULT_UPLOAD_FAILED, bundle);
                        }
                    });
        } catch (Exception ex) {
            Log.e(TAG, "Exception on file upload " + ex.getLocalizedMessage());
        }
    }

    private void removeFile(String filePath) {
        File fileToDelete = new File(filePath);
        if (fileToDelete.isFile() && fileToDelete.canRead()) {
            if (!fileToDelete.delete()) {
                Log.e(TAG, "An error occurred while deleting file " + filePath);
            }
        }
    }
}
