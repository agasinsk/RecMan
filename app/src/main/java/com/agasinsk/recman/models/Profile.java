package com.agasinsk.recman.models;

import android.support.annotation.NonNull;

public class Profile {
    private int id;
    String name;
    String sourceFolder;
    String fileHandling;
    String audioFormat;
    boolean isDefault;

    public Profile(int id, String name, String sourceFolder, String fileHandling, String audioFormat, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.sourceFolder = sourceFolder;
        this.fileHandling = fileHandling;
        this.audioFormat = audioFormat;
        this.isDefault = isDefault;
    }

    public Profile(String sourceFolder, String fileHandling, String audioFormat) {
        this.sourceFolder = sourceFolder;
        this.fileHandling = fileHandling;
        this.audioFormat = audioFormat;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name + " " + this.sourceFolder + " " + this.fileHandling + " " + this.audioFormat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public String getFileHandling() {
        return fileHandling;
    }

    public void setFileHandling(String fileHandling) {
        this.fileHandling = fileHandling;
    }

    public String getAudioFormat() {
        return audioFormat;
    }

    public void setAudioFormat(String audioFormat) {
        this.audioFormat = audioFormat;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
