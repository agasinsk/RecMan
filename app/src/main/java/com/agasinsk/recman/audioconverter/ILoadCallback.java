package com.agasinsk.recman.audioconverter;

public interface ILoadCallback {
    
    void onSuccess();
    
    void onFailure(Exception error);
    
}