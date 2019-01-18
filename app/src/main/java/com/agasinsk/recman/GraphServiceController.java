/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.agasinsk.recman;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.extensions.DriveItem;
import com.microsoft.graph.extensions.IGraphServiceClient;
import com.microsoft.graph.extensions.Permission;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Handles the creation of the message and using the GraphServiceClient to
 * upload the files to OneDrive.
 */
class GraphServiceController {

    private final Context mContext;

    public enum StorageState {
        NOT_AVAILABLE, WRITEABLE, READ_ONLY
    }

    private final IGraphServiceClient mGraphServiceClient;

    public GraphServiceController(Context context) {
        mContext = context;
        mGraphServiceClient = GraphServiceClientManager.getInstance(mContext).getGraphServiceClient();
    }

    /**
     * Uploads a file to the user's OneDrive root folder
     *
     * @param fileToUpload File to upload
     * @param callback
     */
    public void uploadFileToOneDrive(File fileToUpload, ICallback<DriveItem> callback) {

        byte[] fileContent = new byte[1024];
        try {
            fileContent = IOUtils.toByteArray(new FileInputStream(fileToUpload));
        } catch (IOException e) {
            showException(e, "exception on file to byte array conversion ", "Byte array conversion failed",
                    "The file to byte array conversion method failed");
        }
        if (fileContent.length > 1024) {
            uploadFileToOneDrive(fileContent, callback);
        }
    }

    /**
     * Uploads a user picture as byte array to the user's OneDrive root folder
     *
     * @param file  byte[] picture byte array
     * @param callback
     */
    public void uploadFileToOneDrive(byte[] file, ICallback<DriveItem> callback) {

        try {
            mGraphServiceClient
                    .getMe()
                    .getDrive()
                    .getRoot()
                    .getItemWithPath("me.png")
                    .getContent()
                    .buildRequest()
                    .put(file, callback);
        } catch (Exception ex) {
            showException(ex, "exception on upload picture to OneDrive ", "Upload picture failed",
                    "The upload picture method failed");
        }
    }

    public void getSharingLink(String id, ICallback<Permission> callback) {

        try {
            mGraphServiceClient
                    .getMe()
                    .getDrive()
                    .getItems(id)
                    .getCreateLink(null, "view")
                    .buildRequest()
                    .post(callback);
        } catch (Exception ex) {
            showException(ex, "exception on get OneDrive sharing link ", "Get sharing link failed", "The get sharing link method failed");
        }
    }

    /**
     * Gets a picture from the device external storage root folder
     *
     * @return byte[] the default picture in a byte array
     */
    private byte[] getDefaultPicture() {

        int bytesRead;
        byte[] bytes = new byte[1024];

        String pathName = Environment.getExternalStorageDirectory() + "/";
        String fileName = mContext.getString(R.string.defaultImageFileName);
        File file = new File(pathName, fileName);
        FileInputStream buf = null;
        if (file.exists() && file.canRead()) {
            int size = (int) file.length();

            bytes = new byte[size];
            try {
                buf = new FileInputStream(file);
                bytesRead = buf.read(bytes, 0, size);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * Gets the mounted state of device external storage
     *
     * @return
     */
    private StorageState getExternalStorageState() {
        StorageState result = StorageState.NOT_AVAILABLE;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return StorageState.WRITEABLE;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return StorageState.READ_ONLY;
        }
        return result;
    }

    /*
     * Opens a user dialog that shows the failure result of an exception and writes a log entry
     * */
    private void showException(Exception ex, String exceptionAction, String exceptionTitle, String exceptionMessage) {
        Log.e("GraphServiceController", exceptionAction + ex.getLocalizedMessage());
        AlertDialog.Builder alertDialogBuidler = new AlertDialog.Builder(mContext);
        alertDialogBuidler.setTitle(exceptionTitle);
        alertDialogBuidler.setMessage(exceptionMessage);
        alertDialogBuidler.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialogBuidler.show();
    }
}