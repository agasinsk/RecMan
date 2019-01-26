package com.agasinsk.recman;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agasinsk.recman.models.FileDto;

import java.util.ArrayList;

public class FileDtoListAdapter extends ArrayAdapter<FileDto> {

    private final Context mContext;
    private final ArrayList<FileDto> mFiles;

    public FileDtoListAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<FileDto> list) {
        super(context, resource, list);
        mContext = context;
        mFiles = list;
    }

    @Override
    public FileDto getItem(int position) {
        return mFiles.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        final FileDto fileDto = mFiles.get(position);

        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.file_list_item, parent, false);
        }

        TextView name = listItem.findViewById(R.id.fileNameTextView);
        name.setText(fileDto.getName());

        ProgressBar fileProgress = listItem.findViewById(R.id.fileProgressBar);
        if (fileDto.isStarted() && fileDto.getProgress() == 0 && !fileDto.hasError()) {
            fileProgress.setIndeterminate(true);
            return listItem;
        } else {
            fileProgress.setIndeterminate(false);
        }

        if (fileDto.hasError()) {
            fileProgress.setProgress(100, true);
            fileProgress.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            fileProgress.setProgress(fileDto.getProgress(), true);
        }

        return listItem;
    }
}
