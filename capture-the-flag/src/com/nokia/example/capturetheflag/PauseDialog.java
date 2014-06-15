/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A dialog that is shown when the user presses back while the game is running.
 */
public class PauseDialog extends DialogFragment {
    public static final String FRAGMENT_TAG = "PauseDialog";
    private static final int RESUME = 0;
    private static final int DROP_OUT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.game_paused));

        builder.setItems(new String[]{
                        getString(R.string.resume_game),
                        getString(R.string.drop_out)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case RESUME:
                                dismiss();
                                break;
                            case DROP_OUT:
                                MainActivity main = (MainActivity) getActivity();
                                main.showGameMenu(PauseDialog.this);
                                Controller controller =
                                        (Controller) getFragmentManager()
                                                .findFragmentByTag(Controller.FRAGMENT_TAG);
                                controller.clearGame();
                                break;
                            default:
                                dismiss();
                                break;
                        }
                    }
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        getActivity().finish();
        super.onCancel(dialog);
    }
}
