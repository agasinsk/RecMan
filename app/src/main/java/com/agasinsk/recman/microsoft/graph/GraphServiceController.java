/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.agasinsk.recman.microsoft.graph;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.agasinsk.recman.R;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.extensions.IGraphServiceClient;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class GraphServiceController {

    private final Context mContext;

    private final IGraphServiceClient mGraphServiceClient;

    public GraphServiceController(Context context) {
        mContext = context;
        mGraphServiceClient = GraphServiceClientManager.getInstance(mContext).getGraphServiceClient();
    }

    public void uploadFileToOneDrive(File fileToUpload, ICallback<DriveItem> callback) {

        byte[] fileContent = new byte[1024];
        try {
            fileContent = IOUtils.toByteArray(new FileInputStream(fileToUpload));
        } catch (IOException ex) {
            Log.e("GraphServiceController", "Exception on file to byte array conversion", ex);
        }
        if (fileContent.length > 1024) {
            uploadFileToOneDrive(fileContent, fileToUpload.getName(), callback);
        }
    }

    private void uploadFileToOneDrive(byte[] file, String fileName, ICallback<DriveItem> callback) {
        try {
            String folder = mContext.getResources().getString(R.string.default_one_drive_folder);
            mGraphServiceClient
                    .getMe()
                    .getDrive()
                    .getRoot()
                    .getItemWithPath(folder + fileName)
                    .getContent()
                    .buildRequest()
                    .put(file, callback);
        } catch (Exception ex) {
            Log.e("GraphServiceController", "Exception on upload file to OneDrive", ex);
        }
    }

    /*
     * Opens a user dialog that shows the failure result of an exception and writes a log entry
     * */
    private void showException(Exception ex, String exceptionAction, String exceptionTitle, String exceptionMessage) {
        Log.e("GraphServiceController", exceptionAction + ex.getLocalizedMessage());
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(exceptionTitle);
        alertDialogBuilder.setMessage(exceptionMessage);
        alertDialogBuilder.setNeutralButton("Ok", (dialog, which) -> {
        });
        alertDialogBuilder.show();
    }
}