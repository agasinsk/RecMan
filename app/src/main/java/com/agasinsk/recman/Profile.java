package com.agasinsk.recman;

public class Profile {
    String name;
    String sourceFolder;
    String fileHandling;
    String audioFormat;
    boolean isDefault;

    public Profile(String name, String sourceFolder, String fileHandling, String audioFormat, boolean isDefault) {
        this.name = name;
        this.sourceFolder = sourceFolder;
        this.fileHandling = fileHandling;
        this.audioFormat = audioFormat;
        this.isDefault = isDefault;
    }

    public Profile(String sourceFolder, String fileHandling, String audioFormat, boolean isDefault) {
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

    public Profile() {
    }
}
