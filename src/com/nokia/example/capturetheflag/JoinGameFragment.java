/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag;

import com.here.android.common.GeoPosition;
import com.here.android.mapping.MapFactory;
import com.nokia.example.capturetheflag.network.JoinRequest;
import com.nokia.example.capturetheflag.network.model.Game;
import com.nokia.example.capturetheflag.network.model.Player;
import com.nokia.push.PushRegistrar;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
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
 * Shows the join game UI.
 */
public class JoinGameFragment
    extends Fragment
    implements MainActivity.BackCallback
{
    public static final String FRAGMENT_TAG = "JoinGameFragment";
    private Game mGame;
    private ProgressBar mProgressBar;
    private EditText mPlayerName;

    public JoinGameFragment() {
        super();
        mGame = new Game(Game.NEW_GAME);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.join_game_fragment, container, false);
        TextView gameName = (TextView)root.findViewById(R.id.game_name);
        gameName.setText(mGame.getName());
        mPlayerName = (EditText)root.findViewById(R.id.editText1);
        mPlayerName.setText(Settings.getUsername(getActivity()));
        mProgressBar = (ProgressBar) root.findViewById(R.id.join_loading_indicator);
        final Button joinGameButton = (Button) root.findViewById(R.id.join_game_button);
        
        joinGameButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    joinGameButton.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    Settings.setUsername(mPlayerName.getText().toString().trim(), getActivity());
                    joinGame();
                    FragmentManager manager = getActivity().getFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.remove(JoinGameFragment.this);
                    transaction.commit();
                }
            }
        });
        
        return root;
    }

    @Override
    public void onBackPressed() {
        MainActivity mainActivity = (MainActivity) getActivity();
        
        if (mainActivity != null) {
            mainActivity.showGameMenu(this);
        }
    }

    public void setGame(Game g) {
        mGame = g;
    }

    private boolean validate() {
        if (mPlayerName.getText().toString().trim().length() > 0) {
            mPlayerName.setError(null); // Clear error if present
            return true;
        }
        
        mPlayerName.setError(getString(R.string.invalid_name));
        return false;
    }

    private void joinGame() {
        Player player = new Player(0, Settings.getUsername(getActivity()));
        player.setRegistrationId(PushRegistrar.getRegistrationId(getActivity()));
        RadioGroup group = (RadioGroup)getView().findViewById(R.id.radiobuttons);
        
        switch(group.getCheckedRadioButtonId()) {
            case R.id.blue_team_choice:
                player.setTeam(Player.BLUE);
                break;
            case R.id.red_team_choice:
                player.setTeam(Player.RED);
                break;
            default:
                // TODO: Show validation error
                break;
        }
        
        GeoPosition pos = MapFactory.getPositioningManager().getPosition();
        player.setLatitude(pos.getCoordinate().getLatitude());
        player.setLongitude(pos.getCoordinate().getLongitude());
        Controller.getInstance().getNetworkClient().emit(new JoinRequest(mGame, player));
    }
}
