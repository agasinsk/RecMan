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
    public File[] getFilesWithHandling(File[] allFiles, String fileHandling) throws IOException {
        if (allFiles.length == 0) {
            throw new IOException("No files to monitor in the dir");
        }

        if (fileHandling == null || fileHandling.equals("")) {
            return allFiles;
        }
        switch (fileHandling) {
            case "Take all from today":
                return getFilesFromToday(allFiles);
            case "Take last one":
                return getLastFile(allFiles);
            case "Take all":
                return allFiles;
        }

        return allFiles;
    }

    private File[] getLastFile(File[] allFiles) {
        Arrays.sort(allFiles, Comparator.comparingLong(File::lastModified).reversed());
        return new File[]{allFiles[0]};
    }

    private File[] getFilesFromToday(File[] allFiles) {
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

        File[] filesArray = filesFromToday.toArray(new File[0]);
        return filesArray;
    }
}
