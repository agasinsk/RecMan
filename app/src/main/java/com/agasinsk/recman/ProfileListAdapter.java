package com.agasinsk.recman;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class ProfileListAdapter extends ArrayAdapter<Profile> {

    private static final String LOG_TAG = "ProfileList";

    private Context mContext;
    private ArrayList<Profile> profiles;

    public ProfileListAdapter(@NonNull Context context,  @LayoutRes int resource, ArrayList<Profile> list) {
        super(context, resource, list);
        mContext = context;
        profiles = list;
    }

    @Override
    public Profile getItem(int position) {
        return profiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        final Profile currentProfile = profiles.get(position);

        //Inflate the view
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.profile_list_item, parent, false);
        }

        CheckBox isDefaultCheckBox = listItem.findViewById(R.id.isDefaultCheckBox);
        isDefaultCheckBox.setChecked(currentProfile.isDefault);
        isDefaultCheckBox.setEnabled(false);

        TextView name = listItem.findViewById(R.id.profileNameTextView);
        name.setText(currentProfile.name);

        TextView source = listItem.findViewById(R.id.profileSourceTextView);
        source.setText(currentProfile.sourceFolder);

        TextView fileHandling = listItem.findViewById(R.id.profileFileHandlingTextView);
        fileHandling.setText(currentProfile.fileHandling);

        TextView audioFormat = listItem.findViewById(R.id.profileAudioFormatTextView);
        audioFormat.setText(currentProfile.audioFormat);

        return listItem;
    }


}
