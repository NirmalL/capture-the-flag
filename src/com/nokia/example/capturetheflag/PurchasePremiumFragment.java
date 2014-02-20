/**
 * Copyright (c) 2014 Nokia Corporation.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag;

import com.nokia.example.capturetheflag.iap.PremiumHandler;
import com.nokia.example.capturetheflag.iap.PremiumHandler.PremiumHandlerListener;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A fragment for upgrading the app to premium version. Requests price details
 * from the PremiumHandler class and starts the payment process.
 */
public class PurchasePremiumFragment extends Fragment implements PremiumHandlerListener, MainActivity.BackCallback {
    public static final String FRAGMENT_TAG = "PurchasePremiumFragment";
    private PremiumHandler mPremiumHandler;
    private TextView mPriceLine;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity)activity).setBackCallback(this);
        Controller controller =
                (Controller)activity.getFragmentManager()
                .findFragmentByTag(Controller.FRAGMENT_TAG);
        mPremiumHandler = controller.getPremiumHandler();
        mPremiumHandler.addListener(this);
        mPremiumHandler.requestPrice();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.buy_premium_layout, container, false);
        Button buyButton = (Button)v.findViewById(R.id.buy_premium);
        
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Controller controller =
                        (Controller)getFragmentManager()
                        .findFragmentByTag(Controller.FRAGMENT_TAG);
                controller.getPremiumHandler().purchasePremium();
            }
        });
        
        mPriceLine = (TextView) v.findViewById(R.id.premium_price_line);
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (mPremiumHandler != null) {
            mPremiumHandler.removeListener(this);
        }
    }

    @Override
    public void IapInitialized(int resultcode) {
        // Do nothing
    }

    @Override
    public void setPremiumPurchased(boolean isPurchased) {
        // Do nothing
    }

    @Override
    public void onPriceReceived(String premiumPrice) {
        mPriceLine.setText("Upgrade to premium (" + premiumPrice + ")");
    }

    @Override
    public void onBackPressed() {
        getFragmentManager().beginTransaction().remove(this).commit();
        ((MainActivity)getActivity()).setBackCallback(null);
        
    }
}
