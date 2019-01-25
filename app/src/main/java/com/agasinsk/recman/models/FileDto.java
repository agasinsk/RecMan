package com.agasinsk.recman.models;

public class FileDto {
    int id;
    String name;
    int progress;

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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean hasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    boolean hasError;

    public FileDto(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
