/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Dialog to be shown when the game ends.
 */
public class GameEndedDialogFragment extends DialogFragment {
    public static final String PLAYER_NAME_KEY = "player_name";
    public static final String TEAM_KEY = "team";
    public static final String FRAGMENT_TAG = "GameEndedDialogFragment";

    private DialogButtonListener mListener;

    public GameEndedDialogFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogButtonListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement DialogButtonListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String name = args.getString(PLAYER_NAME_KEY);
        String team = args.getString(TEAM_KEY);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Flag captured by " + name + ", team " + team + " wins");

        builder.setPositiveButton(
                getResources().getText(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.OkClicked();
                    }
                });

        return builder.create();
    }

    public interface DialogButtonListener {
        public void OkClicked();
    }
}
