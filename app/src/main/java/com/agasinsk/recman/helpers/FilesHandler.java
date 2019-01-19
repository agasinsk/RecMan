package com.agasinsk.recman.helpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FilesHandler {
    public List<File> getFilesWithHandling(File[] allFiles, String fileHandling, String audioFormat) throws IOException {
        if (allFiles.length == 0) {
            throw new IOException("No files to monitor in the dir");
        }

        if (fileHandling == null || fileHandling.equals("")) {
            return Arrays.asList(allFiles);
        }

        List<File> filesToResolve = new ArrayList<>();
        if (audioFormat != null && !audioFormat.equals("")) {
            String fileExtension = "." + audioFormat.toLowerCase();
            filesToResolve = new ArrayList<>();
            for (File file : allFiles) {
                if (!file.getName().toLowerCase().endsWith(fileExtension)) {
                    filesToResolve.add(file);
                }
            }
        }

        if (filesToResolve.size() == 0) {
            return filesToResolve;
        }

        switch (fileHandling) {
            case "Take all from today":
                return getFilesFromToday(filesToResolve);
            case "Take last one":
                return getLastFile(filesToResolve);
            case "Take all":
                return filesToResolve;
        }

        return filesToResolve;
    }

    private List<File> getLastFile(List<File> allFiles) {
        allFiles.sort(Comparator.comparingLong(File::lastModified).reversed());

        List<File> firstFile = new ArrayList<>();
        firstFile.add(allFiles.get(0));
        return firstFile;
    }

    private List<File> getFilesFromToday(List<File> allFiles) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        Date lastDate = calendar.getTime();

        List<File> filesFromToday = new ArrayList<>();

        for (File file : allFiles) {
            Date fileModifiedDate = new Date(file.lastModified());
            if (fileModifiedDate.after(lastDate)) {
                filesFromToday.add(file);
            }
        }

        return filesFromToday;
    }
}
