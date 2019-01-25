package com.agasinsk.recman.audioconverter;

import java.io.File;

public interface IConvertCallback {

    void onSuccess(File convertedFile);

    void onFailure(Exception error);

    void onStart();

    void onFinish();

    void onProgress(String message);
}