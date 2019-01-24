package com.agasinsk.recman;

public class FileDto {
    int id;
    String name;
    int progress;
    boolean hasError;

    public FileDto(String name) {
        this.name = name;
    }

    public FileDto(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
