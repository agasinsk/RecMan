package com.agasinsk.recman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileNameDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ProfileNameDialogListener {
        void onProfileNameDialogClosed(String profileName);
    }

    // Use this instance of the interface to deliver action events
    private ProfileNameDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ProfileNameDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement ProfileNameDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.profile_name_dialog, null);

        builder.setView(dialogView)
                .setMessage(R.string.dialog_set_profile_name)
                .setPositiveButton(R.string.ok, (dialog, id) -> {

                    EditText nameEditText = dialogView.findViewById(R.id.profileNameEditText);
                    String name = nameEditText.getText().toString();
                    if ("".equals(name)) {
                        Toast.makeText(getContext(), "You must give a name!", Toast.LENGTH_SHORT).show();
                    } else {
                        mListener.onProfileNameDialogClosed(name);
                    }
                })
                .setNegativeButton(R.string.skip, (dialog, id) -> mListener.onProfileNameDialogClosed(""));

        return builder.create();

    }
}