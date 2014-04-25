/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag;

import java.util.Random;

import com.here.android.common.GeoCoordinate;
import com.here.android.common.GeoPosition;
import com.here.android.mapping.MapFactory;
import com.nokia.example.capturetheflag.network.JoinRequest;
import com.nokia.example.capturetheflag.network.model.Flag;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.ModelConstants;
import com.nokia.example.capturetheflag.network.model.Player;
import com.nokia.push.PushRegistrar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * UI for creating a game. It uses user's current position and randomly creates 
 * two flag positions that are DISTANCE km away from the user.
 */
public class CreateGameFragment
    extends Fragment
    implements OnClickListener, MainActivity.BackCallback
{
    public static final String FRAGMENT_TAG = "CreateGameFragment";
    private static final String TAG = "CtF/CreateGameFragment";
    private static final double EARTH_RADIUS = 6371.0;
    private static final double DISTANCE = 0.2; // Kilometers

    private EditText mGameName;
    private EditText mPlayerName;
    private RadioGroup mTeamSelection;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.create_game_fragment, container, false);
        Button b = (Button)view.findViewById(R.id.create_game_start_game);
        b.setOnClickListener(this);
        mGameName = (EditText) view.findViewById(R.id.game_name_edit);
        mPlayerName = (EditText)view.findViewById(R.id.create_game_player_name);
        mPlayerName.setText(Settings.getUsername(getActivity()));
        mTeamSelection = (RadioGroup) view.findViewById(R.id.create_game_team_group);
        mProgressBar = (ProgressBar) view.findViewById(R.id.create_game_indicator);
        TextView note = (TextView)view.findViewById(R.id.premium_note);
        TextView offlineNote = (TextView) view.findViewById(R.id.offline_note);
        boolean isPremium = getArguments().getBoolean(ModelConstants.IS_PREMIUM_KEY, false);
        
        if (isPremium) {
            note.setVisibility(View.GONE);
        }
        
        Controller controller = Controller.getInstance();
        
        if (controller.getNetworkClient().isConnected()) {
            offlineNote.setVisibility(View.GONE);
        }
        else {
            Controller.getInstance().switchOnlineMode(false);
        }
        
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity)activity).setBackCallback(this);
    }

    @Override
    public void onDetach() {
        ((MainActivity)getActivity()).removeBackCallback();
        super.onDetach();
    }

    @Override
    public void onBackPressed() {
        MainActivity ma = (MainActivity)getActivity();
        
        if (ma != null) {
            ma.showGameMenu(this);
        }
    }

    /**
     * Creates a new game based on the user selection.
     */
    @Override
    public void onClick(View v) {
        if (validateFields()) {
            v.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            GeoPosition pos = MapFactory.getPositioningManager().getPosition();
            
            Game game = new Game(Game.NEW_GAME);
            game.setName(mGameName.getText().toString());
            Controller controller = (Controller) getFragmentManager()
                    .findFragmentByTag(Controller.FRAGMENT_TAG);
            game.setPremium(controller.isPremium());
            generateFlags(pos.getCoordinate(), game);
            Player player = new Player(0, mPlayerName.getText().toString());
            player.setRegistrationId(PushRegistrar.getRegistrationId(getActivity()));
            Settings.setUsername(mPlayerName.getText().toString(), getActivity());
            
            switch (mTeamSelection.getCheckedRadioButtonId()) {
                case R.id.radio0:
                    player.setTeam(Player.BLUE);
                    break;
                case R.id.radio1:
                    player.setTeam(Player.RED);
                    break;
                default:
                    player.setTeam(Player.BLUE);
                    break;
            }
            
            player.setLatitude(pos.getCoordinate().getLatitude());
            player.setLongitude(pos.getCoordinate().getLongitude());
            Controller.getInstance().getNetworkClient().emit(new JoinRequest(game, player));
        }
    }

    /**
     * Checks the validity of the user input.
     * 
     * @return True if input is valid, false otherwise.
     */
    private boolean validateFields() {
        boolean gamename = false;
        boolean username = false;
        
        if (mGameName.getText().toString().trim().length() > 0) {
            gamename = true;
            mGameName.setError(null);
        }
        else {
            mGameName.setError(getString(R.string.invalid_name));
            gamename = false;
        }
        
        if (mPlayerName.getText().toString().trim().length() > 0) {
            mPlayerName.setError(null);
            username = true;
        }
        else {
            mPlayerName.setError(getString(R.string.invalid_name));
            username = false;
        }
        
        return gamename && username;
    }

    private void generateFlags(GeoCoordinate basePosition, Game g) {
        g.setBlueFlag(createRandomCoordinate(basePosition));
        g.setRedFlag(createRandomCoordinate(basePosition));
    }

    /**
     * Randomly generate flag position from DISTANCE kilometers of user's
     * location. See http://www.movable-type.co.uk/scripts/latlong.html for
     * the theory behind the calculations.
     * 
     * @param basePosition The position respect to which place the flags.
     * @return A newly created flag object with position.
     */
    private Flag createRandomCoordinate(GeoCoordinate basePosition) {
        double dist = DISTANCE / EARTH_RADIUS;
        
        double bearing = Math.toRadians((new Random().nextDouble() * 360));
        double lat1 = Math.toRadians(basePosition.getLatitude());
        double lon1 = Math.toRadians(basePosition.getLongitude());
        
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist)
                + Math.cos(lat1) * Math.sin(dist) * Math.cos(bearing));
        
        double a = Math.atan2(Math.sin(bearing) * Math.sin(dist) * Math.cos(lat1),
                Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
        double lon2 = lon1 + a;
        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        
        Log.d(TAG, "createRandomCoordinate(): lat:" + lat2 + " lon:" + lon2);
        
        return new Flag(lat2, lon2);
    }
}
