/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

/**
 *
 */
public abstract class ServerSettingsDialog
        extends AlertDialog.Builder
        implements OnClickListener {
    private final EditText mServerUrlAndPortEditText;

    /**
     * Constructor.
     *
     * @param context The application context.
     */
    public ServerSettingsDialog(Context context) {
        super(context);
        setTitle(R.string.server_settings);
        setMessage(R.string.server_settings_dialog_info);
        mServerUrlAndPortEditText = new EditText(context);
        mServerUrlAndPortEditText.setHint(R.string.server_url_and_port_hint_text);
        mServerUrlAndPortEditText.setText(
                Settings.getServerUrl(context) + ":" + Settings.getServerPort(context));
        setView(mServerUrlAndPortEditText);
        setPositiveButton(R.string.ok, this);
        setNegativeButton(R.string.cancel, this);
    }

    /**
     * @see android.content.DialogInterface.OnClickListener#onClick(
     *android.content.DialogInterface, int)
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String urlAndPort[] = mServerUrlAndPortEditText.getText().toString().split(":");

            if (urlAndPort != null && urlAndPort.length >= 2) {
                String url = urlAndPort[0];
                String port = urlAndPort[urlAndPort.length - 1];

                if (urlAndPort.length > 2) {
                    url = urlAndPort[0] + ":" + urlAndPort[1];
                }

                if (url != null && !url.isEmpty()
                        && port != null && !port.isEmpty()
                        && onOkClicked(url, port)) {
                    dialog.dismiss();
                }
            }
        } else {
            // Cancel was tapped
            dialog.dismiss();
        }
    }

    /**
     * Called when OK is tapped.
     *
     * @param url  The server URL.
     * @param port The server port.
     * @return True if the dialog should be closed, false otherwise.
     */
    abstract protected boolean onOkClicked(final String url, final String port);
}
