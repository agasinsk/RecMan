package com.agasinsk.recman.service;

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
    public static final int RESULT_UPLOAD_FILE_TOO_BIG = 126;
    private static Context mContext;
    private static final int ONEDRIVE_FILE_SIZE_LIMIT = 4 * 1024 * 1024;
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
    public static void enqueueWork(Context context, Intent work) {
        mContext = context;
        enqueueWork(context, UploadJobService.class, FILE_UPLOAD_JOB_ID, work);
    }

    private GraphServiceController getGraphServiceController() {
        if (mGraphServiceController == null) {
            mGraphServiceController = new GraphServiceController(mContext);
        }
        return mGraphServiceController;
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.i(TAG, "Executing work: " + intent);
        String fileToUploadPath = intent.getStringExtra(BundleConstants.FILE_PATH);
        int fileId = intent.getIntExtra(BundleConstants.FILE_ID, 0);
        int fileCount = intent.getIntExtra(BundleConstants.FILE_TOTAL_COUNT, 0);
        mResultReceiver = intent.getParcelableExtra(BundleConstants.RECEIVER);

        File fileToUpload = new File(fileToUploadPath);

        if (fileToUpload.length() > ONEDRIVE_FILE_SIZE_LIMIT) {
            Log.e(TAG, "File " + fileToUpload.getName() + " is over 4MB and cannot be uploaded.");
            removeFile(fileToUploadPath);
            sendResultWithBundle(RESULT_UPLOAD_FILE_TOO_BIG, fileToUpload.getName(), fileId, fileCount);
        }
        try {
            getGraphServiceController()
                    .uploadFileToOneDrive(fileToUpload, new ICallback<DriveItem>() {
                        @Override
                        public void success(DriveItem driveItem) {
                            Log.d(TAG, "Successfully uploaded file " + driveItem.name + " to OneDrive");
                            removeFile(fileToUploadPath);
                            sendResultWithBundle(RESULT_UPLOAD_OK, driveItem.name, fileId, fileCount);
                        }

                        @Override
                        public void failure(ClientException ex) {
                            Log.e(TAG, "Exception on file upload to OneDrive ", ex);
                            sendResultWithBundle(RESULT_UPLOAD_FAILED, fileToUpload.getName(), fileId, fileCount);
                        }
                    });
        } catch (Exception ex) {
            Log.e(TAG, "Exception on file upload " + ex.getLocalizedMessage(), ex);
        }
    }

    private void sendResultWithBundle(int resultCode, String fileName, int fileId, int fileCount) {
        Bundle bundle = new Bundle();
        bundle.putString(BundleConstants.FILE_NAME, fileName);
        bundle.putInt(BundleConstants.FILE_ID, fileId);
        bundle.putInt(BundleConstants.FILE_TOTAL_COUNT, fileCount);
        mResultReceiver.send(resultCode, bundle);
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
