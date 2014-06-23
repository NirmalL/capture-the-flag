/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nokia.example.capturetheflag.location.LocationManagerInterface.ReverseGeocodingResultListener;
import com.nokia.example.capturetheflag.network.GameListRequest;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.ModelConstants;

/**
 * Shows the game menu where user can select to create a new game or join an
 * existing game.
 */
public class GameMenuFragment
        extends Fragment
        implements View.OnClickListener, ReverseGeocodingResultListener
{
    public static final String FRAGMENT_TAG = "GameMenuFragment";
    private static final String TAG = "CtF/GameMenuFragment";

    private Game[] mGames;
    private Button mNewGameButton;
    private TextView mUserLocation;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Always try to use online mode when showing game menu
        Controller.getInstance().switchOnlineMode(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.game_menu_fragment, container, false);

        v.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mUserLocation = (TextView) v.findViewById(R.id.user_address);
        mNewGameButton = (Button) v.findViewById(R.id.create_new_game);
        mNewGameButton.setOnClickListener(this);
        mProgressBar = (ProgressBar) v.findViewById(R.id.menu_loading_indicator);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Controller.getInstance().getNetworkClient().emit(new GameListRequest());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        FragmentManager manager = getFragmentManager();
        if (!Controller.getInstance().isLocationFound()) {
            /* Location is required for starting a game, but the user has not
             * been located yet.
             */
            AlertDialog.Builder noLocationDialogBuilder = new AlertDialog.Builder(getActivity());
            noLocationDialogBuilder.setTitle(getString(R.string.no_location));
            noLocationDialogBuilder.setMessage(getString(R.string.location_required_to_start));

            noLocationDialogBuilder.setPositiveButton(getString(android.R.string.ok), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            noLocationDialogBuilder.create().show();
            return;
        }
        if (view == mNewGameButton) {
            FragmentTransaction transaction = manager.beginTransaction();
            CreateGameFragment createGameFragment = new CreateGameFragment();
            Controller controller =
                    (Controller) getFragmentManager()
                            .findFragmentByTag(Controller.FRAGMENT_TAG);
            Bundle args = new Bundle();
            args.putBoolean(ModelConstants.IS_PREMIUM_KEY, controller.isPremium());
            createGameFragment.setArguments(args);
            transaction.remove(this);
            transaction.add(R.id.top_fragments,
                    createGameFragment, CreateGameFragment.FRAGMENT_TAG);
            transaction.commit();
        } else {
            Game game = (Game) view.getTag();
            FragmentTransaction transaction = manager.beginTransaction();
            JoinGameFragment join = new JoinGameFragment();
            join.setGame(game);
            transaction.remove(this);
            transaction.add(R.id.top_fragments, join, JoinGameFragment.FRAGMENT_TAG);
            transaction.commit();
        }
    }

    /**
     * Populates the view with "join game" buttons.
     *
     * @param games
     */
    public void setGames(Game[] games) {
        Log.d(TAG, "setGames()");
        mGames = games;
        LinearLayout layout = (LinearLayout) getView().findViewById(R.id.open_game_area);
        layout.removeAllViews();
        setProgressBarVisibility(false);

        for (Game game : mGames) {
            Button button = (Button) getActivity().getLayoutInflater().inflate(R.layout.button, layout, false);
            button.setTag(game);
            button.setText("Join \"" + game.getName() + "\"");
            button.setOnClickListener(this);
            layout.addView(button);
        }
    }

    /**
     * Helper method to toggle the progress bar visibility.
     * 
     * @param visible If true will show the progress bar, if false will hide it.
     */
    public void setProgressBarVisibility(boolean visible) {
        if (isVisible()) {
            int visibility = (visible ? View.VISIBLE : View.GONE);
            
            if (mProgressBar.getVisibility() != visibility) {
                mProgressBar.setVisibility(visibility);
            }
        }
    }

    /**
     * Shows the address, found with reverse geocoding, to the user.
     */
    @Override
    public void onReverseGeocodingResult(String result) {
        if (getActivity() != null) {
            if (result == null) {
                mUserLocation.setText("Unable to find location");
            } else {
                if (result.isEmpty()) {
                    mUserLocation.setText(getString(R.string.no_street_address));
                } else {
                    mUserLocation.setText(getString(R.string.you_are_at) + " " + result);
                }
            }
        }
    }
}
