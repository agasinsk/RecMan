package com.agasinsk.recman;

public class Profile {
    int id;
    String name;
    String sourceFolder;
    String fileHandling;
    String audioFormat;
    boolean isDefault;

    public Profile(int id, String sourceFolder, String fileHandling, String audioFormat, boolean isDefault) {
        this.sourceFolder = sourceFolder;
        this.fileHandling = fileHandling;
        this.audioFormat = audioFormat;
        this.isDefault = isDefault;
    }

    public Profile() {
    }

    public Profile(int id, String name, String sourceFolder, String fileHandling, String audioFormat, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.sourceFolder = sourceFolder;
        this.fileHandling = fileHandling;
        this.audioFormat = audioFormat;
        this.isDefault = isDefault;
    }

    public Profile(int id, String sourceFolder, String fileHandling, String audioFormat) {
        this.id = id;
        this.sourceFolder = sourceFolder;
        this.fileHandling = fileHandling;
        this.audioFormat = audioFormat;
    }

    public Profile(String sourceFolder, String fileHandling, String audioFormat) {
        this.id = id;
        this.sourceFolder = sourceFolder;
        this.fileHandling = fileHandling;
        this.audioFormat = audioFormat;
    }

    @Override
    public String toString() {
        return this.name + " " + this.sourceFolder + " " + this.fileHandling + " " + this.audioFormat;
    }
}
