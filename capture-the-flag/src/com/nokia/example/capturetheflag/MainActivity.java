/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.nokia.example.capturetheflag.map.GameMapFactory;
import com.nokia.example.capturetheflag.map.GameMapInterface;
import com.nokia.example.capturetheflag.network.NetworkClient;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.notifications.NotificationsManagerFactory;

/**
 * Main Activity of the application. This Activity is responsible for
 * initialisation and de-initialisation of push notifications. Also when IAP
 * request is made the results are received on the onActivityResult() method.
 * <p/>
 * Note that this class overrides the default back button behavior because while
 * the game is running and user presses back, we first show a small dialog
 * (resume/drop out) and it requires diverting from the normal backstack
 * handling. For the back button to work normally, we implement BackCallBack
 * interface on classes that need to know when back is pressed. Check usage from
 * CreateGameFragment or JoinGameFragment.
 */
public class MainActivity extends Activity implements
        GameEndedDialogFragment.DialogButtonListener {
    private static final String TAG = "CtF/MainActivity";

    private PurchasePremiumFragment mPremiumFragment;
    private GameMapInterface mGameMap = null;
    private Controller mController = null;
    private MenuItem mBuyPremiumMenuItem = null;
    private BackCallback mBackKeyCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmanager = getFragmentManager();

        mController = (Controller) fragmanager.findFragmentByTag(Controller.FRAGMENT_TAG);

        // First time or fragment couldn't be retained
        if (mController == null) {
            mController = new Controller();
            fragmanager.beginTransaction()
                    .add(mController, Controller.FRAGMENT_TAG).commit();
        }

        // Create and add the map fragment to the UI.
        mGameMap = GameMapFactory.createGameMap();
        android.app.FragmentTransaction fragmentTransaction = fragmanager.beginTransaction();
        fragmentTransaction.add(R.id.mapfragment, (Fragment) mGameMap);
        fragmentTransaction.commit();
        mController.setMap(mGameMap);

        // Make sure the application is registered for receiving push notifications
        NotificationsManagerFactory.getInstance(getApplicationContext()).register();

        if (savedInstanceState == null) {
            showGameMenu(null);
        }
    }

    @Override
    protected void onResume() {
        // Engine.getInstance().connectionNotIdle();
        super.onResume();
    }

    @Override
    protected void onStop() {
        // Engine.getInstance().connectionIdle();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //PushRegistrar.onDestroy(this);
        mController.cleanUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mBuyPremiumMenuItem = menu.findItem(R.id.buy_premium_menuitem);
        mBuyPremiumMenuItem.setVisible(!mController.isPremium());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retval = false;

        switch (item.getItemId()) {
            case R.id.buy_premium_menuitem:
                FragmentManager fragmentManager = getFragmentManager();
                Fragment fragment =
                    fragmentManager.findFragmentByTag(PurchasePremiumFragment.FRAGMENT_TAG);
                
                // Check if the fragment is already visible
                if (fragment == null || !fragment.isVisible()) {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    
                    if (mPremiumFragment == null) {
                        mPremiumFragment = new PurchasePremiumFragment();
                    }
                    
                    transaction.add(R.id.fragmentcontainer, mPremiumFragment,
                        PurchasePremiumFragment.FRAGMENT_TAG);
                    transaction.commit();
                }
                
                retval = true;
                break;
            case R.id.help_menuitem:
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                retval = true;
                break;
            case R.id.about_menuitem:
                Intent i = new Intent(this, AboutActivity.class);
                startActivity(i);
                retval = true;
                break;
            case R.id.server_settings_menuitem:
                final MainActivity context = this;

                ServerSettingsDialog dialog = new ServerSettingsDialog(this) {
                    @Override
                    public boolean onOkClicked(final String url, final String port) {
                        Log.i(TAG, "Server URL set to " + url + " and port as "
                                + port + ".");
                        final int portAsInt = Integer.parseInt(port);
                        Settings.setServerUrl(url, context);
                        Settings.setServerPort(portAsInt, context);

                        // Try to (re)connect
                        NetworkClient client = mController.getNetworkClient();

                        if (client.isConnected()) {
                            client.disconnect();
                        }

                        client.connect(url, portAsInt);
                        return true;
                    }
                };

                dialog.show();
                retval = true;
                break;
            default:
                retval = super.onOptionsItemSelected(item);
                break;
        }

        return retval;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPremiumFragment != null && requestCode == PurchasePremiumFragment.RC_REQUEST) {
            mPremiumFragment.handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBackKeyCallback != null) {
            mBackKeyCallback.onBackPressed();
        } else {
            /*
             * If no back callback, we check if the game is running and if it
             * is, we show the pause dialog.
             */
            Log.d(TAG, "onBackPressed(): No back callback.");
            
            if (mController.getCurrentGame() != null
                    && !mController.getCurrentGame().getHasEnded())
            {
                PauseDialog dialog = new PauseDialog();
                dialog.show(getFragmentManager(), PauseDialog.FRAGMENT_TAG);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void OkClicked() {
        showGameMenu(null);
        mGameMap.clearMarkers();
    }

    /**
     * Called when a game is started. Sets the markers (flags, other players),
     * centers the map to user position and adjusts the zoom level.
     *
     * @param game The instance of the {@link Game} that was started.
     */
    public void startGame(Game game) {
        Log.d(TAG, "startGame()");
        mGameMap.setMarkers(game);
    }

    /**
     * Unlocks the premium version of the application.
     */
    public void unlockPremium() {
        if (mBuyPremiumMenuItem != null) {
            mBuyPremiumMenuItem.setVisible(false);
        }
    }

    /**
     * Sets the back callback.
     *
     * @param callback {@link BackCallback} to set.
     */
    public void setBackCallback(BackCallback callback) {
        mBackKeyCallback = callback;
    }

    /**
     * Removes the back callback.
     */
    public void removeBackCallback() {
        mBackKeyCallback = null;
    }

    /**
     * Shows the game menu.
     *
     * @param removable The fragment to be removed from the activity when menu is shown.
     */
    protected void showGameMenu(Fragment removable) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (removable != null) {
            transaction.remove(removable);
        }

        // Create and show the dialog
        Fragment newFragment = new GameMenuFragment();
        transaction.add(R.id.fragmentcontainer, newFragment,
                GameMenuFragment.FRAGMENT_TAG);
        transaction.commit();

        // If previously in offline mode, try to connect to server again
        mController.switchOnlineMode(true);
    }

    /**
     * Interface for classes that need to know when back is pressed. Only to be
     * used by views and the views should remove themselves via
     * removeBackCallback(), when they aren't on the top of the view stack
     * anymore. Best to be used with fragments.
     */
    public interface BackCallback {
        public void onBackPressed();
    }
}
