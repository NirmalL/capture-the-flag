/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.nokia.example.capturetheflag.location.LocationManagerFactory;
import com.nokia.example.capturetheflag.location.LocationManagerInterface;
import com.nokia.example.capturetheflag.location.LocationManagerListener;
import com.nokia.example.capturetheflag.map.GameMapInterface;
import com.nokia.example.capturetheflag.network.FlagCapturedResponse;
import com.nokia.example.capturetheflag.network.GameListResponse;
import com.nokia.example.capturetheflag.network.JSONResponse;
import com.nokia.example.capturetheflag.network.JoinedResponse;
import com.nokia.example.capturetheflag.network.NetworkClient;
import com.nokia.example.capturetheflag.network.OfflineClient;
import com.nokia.example.capturetheflag.network.SocketIONetworkClient;
import com.nokia.example.capturetheflag.network.UpdatePlayerRequest;
import com.nokia.example.capturetheflag.network.UpdatePlayerResponse;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.ModelConstants;
import com.nokia.example.capturetheflag.network.model.Player;
import com.nokia.example.capturetheflag.notifications.NotificationsManagerFactory;

/**
 * Controller class is responsible for communicating server responses back to
 * the UI. It listens for message events from the server.
 * It also maintains a state about the current Game and Player objects.
 * <p/>
 * The class is implemented as a Fragment but it's a retained fragment, meaning
 * that it doesn't have an UI and it will be kept alive if possible i.e. if
 * there is enough memory for it.
 */
public class Controller
        extends Fragment
        implements NetworkClient.NetworkListener, LocationManagerListener {
    public static final String FRAGMENT_TAG = "Controller";
    private static final String TAG = "CtF/Controller";

    private static Controller mSelf;
    private Game mCurrentGame;
    private Player mPlayer;
    private GameMapInterface mMap;
    private NetworkClient mClient;
    private SocketIONetworkClient mSocketClient;
    private OfflineClient mOfflineClient;
    private Handler mUIHandler;
    private int mIsConnected = -1;
    private boolean mIsLocationFound = false;
    private LocationManagerInterface mLocationManager;

    /**
     * Receiver to handle push notifications. When push note is received parse
     * it and show it to the user.
     */
    private BroadcastReceiver mPushHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG + ".mPushHandler", "Received broadcast");

            try {
                Player capturer = new Player(
                        new JSONObject(intent
                                .getStringExtra(ModelConstants.CAPTURER_KEY))
                                .getJSONObject(ModelConstants.CAPTURED_BY_PLAYER_KEY));
                FlagCaptured(capturer);
            } catch (JSONException e) {
                // Received corrupted data, do nothing
                Log.e(TAG + ".mPushHandler", "Parse error: " + e.getMessage(), e);
            }
        }
    };

    public static Controller getInstance() {
        return mSelf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelf = this;
        mUIHandler = new Handler();
        setRetainInstance(true);
        mSocketClient = new SocketIONetworkClient();
        mClient = mSocketClient;
        mClient.setListener(this);
        mClient.connect(
                Settings.getServerUrl(getActivity()),
                Settings.getServerPort(getActivity()));
        mOfflineClient = new OfflineClient();
        mOfflineClient.setListener(this);

        mLocationManager = LocationManagerFactory.getInstance(getActivity());
        mLocationManager.setListener(this);

        NotificationsManagerFactory.getInstance(getActivity()).register();
    }

    public void setMap(GameMapInterface map) {
        mMap = map;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMap = null; // Remove the reference since map is not retained, would
        // leak everything
        Log.d(TAG, "onDetach(): getActivity() returns "
                + (getActivity() == null ? "null" : "not null"));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Unregistering broadcast receiver");

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                mPushHandler);
        mClient.setConnectionIdle(true);
        mLocationManager.stop();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Registering broadcast receiver");

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mPushHandler,
                new IntentFilter(NotificationsManagerFactory.PUSH_MESSAGE_ACTION));
        mClient.setConnectionIdle(false);
        mLocationManager.start();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocketClient.cleanUp();
        mSelf = null;
    }

    @Override
    public void onGameListMessage(GameListResponse rsp) {
        FragmentManager manager = getFragmentManager();

        if (manager != null) {
            GameMenuFragment menu = (GameMenuFragment) getFragmentManager()
                    .findFragmentByTag(GameMenuFragment.FRAGMENT_TAG);

            if (menu != null && menu.isVisible()) {
                Log.d(TAG, "Menu is not null");
                menu.setGames(rsp.getGames());
            }
        }
    }

    @Override
    public void onJoinedMessage(JoinedResponse joined) {
        Fragment createGameFragment =
                getFragmentManager().findFragmentByTag(CreateGameFragment.FRAGMENT_TAG);

        if (createGameFragment != null) {
            getFragmentManager().beginTransaction().remove(createGameFragment).commit();
        }

        setCurrentGame(joined.getJoinedGame());
        mPlayer = joined.getPlayer();
        ((MainActivity) getActivity()).startGame(joined.getJoinedGame());
    }

    @Override
    public void onUpdatePlayerMessage(UpdatePlayerResponse update) {
        if (mCurrentGame == null) {
            return;
        }

        Player updatedPlayer = update.getUpdatedPlayer();

        if (!mCurrentGame.getPlayers().contains(update.getUpdatedPlayer())) {
            Log.d(TAG, "onUpdatePlayerMessage(): New player");
            mCurrentGame.getPlayers().add(updatedPlayer);
            
            /* Show a toast message informing the user that a new player has
             * joined the game.
             */
            final String playerName = updatedPlayer.getName();
            
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    String text = getString(R.string.new_player_joined, playerName);
                    Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            });
        } else {
            Log.d(TAG, "onUpdatePlayerMessage(): Existing player");
            int i = mCurrentGame.getPlayers().indexOf(updatedPlayer);
            Player old = mCurrentGame.getPlayers().get(i);
            mMap.updateMarkerForPlayer(updatedPlayer, old);
            mCurrentGame.getPlayers().set(i, updatedPlayer);
        }
        
        mMap.updatePlayerMarkerPosition(updatedPlayer);
    }

    @Override
    public void onError(JSONResponse error) {
        Log.w(TAG, "onError()");

        AlertDialog.Builder errordialog = new AlertDialog.Builder(getActivity());
        errordialog.setTitle("Error");

        if (error.getErrorCode() == JSONResponse.ERROR_GAME_FULL) {
            errordialog.setMessage(getString(R.string.game_full));
        } else if (error.getErrorCode() == JSONResponse.ERROR_EXCEPTION) {
            errordialog.setMessage(error.getException().getMessage());
        }

        errordialog.setPositiveButton(getString(android.R.string.ok),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity m = (MainActivity) getActivity();
                        m.showGameMenu(null);
                    }
                });

        errordialog.create().show();
    }

    /**
     * In case you don't want to use the push notifications to inform users that
     * the game has ended, you can enable the ending info to be sent via TCP
     * socket and handle the message here.
     */
    @Override
    public void onFlagCapturedMessage(FlagCapturedResponse captured) {
        Player p = captured.getCapturer();
        FlagCaptured(p);
    }

    @Override
    public void onLocationManagerReady(boolean success) {
        Log.d(TAG, "Location Manager Ready -" + (success ? "SUCCESS" : "FAILED"));
        if (success && mLocationManager.isLocationAvailable()) {
            mLocationManager.start();
        } else {
            showEnableGPSDialog();
        }
    }

    @Override
    public void onLocationUpdated(Location position) {
        if (!mIsLocationFound) {
            mMap.centerMapToPosition(position);
            mIsLocationFound = true;
        }

        Player user = getPlayer();

        // Only if game is running, we send updated location to the server
        if (user != null && getCurrentGame() != null && !getCurrentGame().getHasEnded()) {
            if (user.getLatitude() != position.getLatitude()
                    || user.getLongitude() != position.getLongitude())
            {
                Log.d(TAG, "onLocationUpdated(): Sending updated position to server and updating the player marker.");
                user.setLatitude(position.getLatitude());
                user.setLongitude(position.getLongitude());
                UpdatePlayerRequest updatePlayerRequest =
                        new UpdatePlayerRequest(user, getCurrentGame().getId());
                mClient.emit(updatePlayerRequest);

                // Update the marker
                mMap.updatePlayerMarkerPosition(user);
            }
        }
  
        /*
         * If the menu is visible, we create and send the reverse geocode
         * request so that we can update the user location in the fragment.
         */

        GameMenuFragment menu = (GameMenuFragment) getFragmentManager()
                .findFragmentByTag(GameMenuFragment.FRAGMENT_TAG);
        if (menu != null) {
            mLocationManager.reverseGeocodeLocation(position, menu);
        }
    }

    /**
     * Displays the connection status using a toast message.
     */
    @Override
    public void onNetworkStateChange(final boolean isConnected, final NetworkClient client) {
        Log.d(TAG, "onNetworkStateChange(): " + (isConnected ? "Online" : "Offline"));

        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean showToast = false;

                if (mIsConnected == -1
                        || (isConnected && mIsConnected == 0)
                        || (!isConnected && mIsConnected == 1)) {
                    showToast = true;
                }

                Activity activity = getActivity();

                if (showToast && activity != null) {
                    final int toastTextId = isConnected
                            ? R.string.connected_to_server
                            : R.string.not_connected_to_server;

                    Toast toast = Toast.makeText(activity,
                            getString(toastTextId), Toast.LENGTH_SHORT);
                    toast.show();
                    mIsConnected = isConnected ? 1 : 0;
                }

                // Hide the progress bar from game menu fragment, if visible
                GameMenuFragment gameMenuFragment =
                        (GameMenuFragment)getFragmentManager().findFragmentByTag(
                                GameMenuFragment.FRAGMENT_TAG);

                if (gameMenuFragment != null && gameMenuFragment.isVisible()) {
                    gameMenuFragment.setProgressBarVisibility(false);
                }
            }
        });
    }

    /**
     * Checks from saved preferences if the app is the premium version.
     *
     * @return True if premium, false otherwise.
     */
    public boolean isPremium() {
        return Settings.getPremium(getActivity()).length() > 0;
    }

    public void setCurrentGame(Game g) {
        if (g != null) {
            Log.d(TAG, "Setting as current game: " + g.getId());
        }

        mCurrentGame = g;
    }

    public Game getCurrentGame() {
        return mCurrentGame;
    }

    public void clearGame() {
        mCurrentGame = null;
        mMap.clearMarkers();
    }

    public Player getPlayer() {
        return mPlayer;
    }

    /**
     * Call this in activity's onDestroy() to release possible handles to the
     * activity object.
     */
    public void cleanUp() {
        mClient.disconnect();
    }

    public NetworkClient getNetworkClient() {
        return mClient;
    }

    /**
     * Switches to online/offline mode depending on the argument.
     *
     * @param online If true, will switch to online mode. Otherwise will switch
     *               to offline mode.
     */
    public void switchOnlineMode(boolean online) {
        if (mOfflineClient == null || mSocketClient == null) {
            Log.d(TAG, "switchOnlineMode(): Not initialised yet.");
            return;
        }
        
        if (online) {
            mOfflineClient.disconnect();
            mClient = mSocketClient;
            mSocketClient.connect(
                    Settings.getServerUrl(getActivity()),
                    Settings.getServerPort(getActivity()));
        } else {
            mSocketClient.disconnect();
            mClient = mOfflineClient;
            mOfflineClient.connect(null, 0);
        }
    }

    public boolean isLocationFound() {
        return mIsLocationFound;
    }

    private void FlagCaptured(Player capturer) {
        GameEndedDialogFragment dialog = new GameEndedDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(GameEndedDialogFragment.PLAYER_NAME_KEY,
                capturer.getName());
        bundle.putString(GameEndedDialogFragment.TEAM_KEY, capturer.getTeam());
        dialog.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(dialog, GameEndedDialogFragment.FRAGMENT_TAG).commit();

        if (mCurrentGame != null) {
            mCurrentGame.setHasEnded(true);
        }
    }

    private void showEnableGPSDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getText(R.string.gps_not_enabled));

        builder.setPositiveButton(
                getResources().getText(R.string.action_settings),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivity(i);
                    }
                });

        builder.setNegativeButton(
                getResources().getText(R.string.cancel),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }
}
