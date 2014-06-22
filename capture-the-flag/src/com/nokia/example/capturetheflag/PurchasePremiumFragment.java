/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;
import org.onepf.oms.appstore.googleUtils.SkuDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment for upgrading the app to premium version.
 *
 * Uses the OpenIAB library to request product info and purchasing premium version.
 * @see <a href="https://github.com/onepf/OpenIAB">OpenIAB documentation</a>.
 */
public class PurchasePremiumFragment extends Fragment implements MainActivity.BackCallback {

    private static final String TAG = "CtF/PurchasePremiumFragment";

    public static final String FRAGMENT_TAG = "PurchasePremiumFragment";
    public static final int RC_REQUEST = 10001;

    // Nokia In-App Payment constants
    private static final String NOKIA_PREMIUM_PRODUCT_ID = "1023610"; // Test ID

    // Google In-App Billing constants
    private static final String Base64EncodedPublicKey = "INSERT_YOUR_PUBLIC_KEY_HERE";
    private static final String SKU_PREMIUM = "android.test.purchased";

    /**
     * SKU mappings
     */
    static {
        OpenIabHelper.mapSku(SKU_PREMIUM, OpenIabHelper.NAME_NOKIA, NOKIA_PREMIUM_PRODUCT_ID);
    }

    private OpenIabHelper mHelper;
    private boolean mIsPremium;
    private boolean mSetupDone = false;
    private String mDeveloperPayload;

    private TextView mTitleLine;
    private TextView mDescriptionLine;
    private TextView mPriceLine;
    private Button mBuyButton;

    private ProgressDialog mWaitScreen;

    /**
     * Callback for when setup is finished. See OpenIAB documentation for
     * details.
     */
    private IabHelper.OnIabSetupFinishedListener mSetupListener =
        new IabHelper.OnIabSetupFinishedListener() {
        
        @Override
        public void onIabSetupFinished(IabResult result) {
            if (!result.isSuccess()) {
                toast(getString(R.string.problem_setting_up_iap) + result);
                Log.e(TAG, "Problem setting up in-app billing: " + result);
            } else {
                Log.d(TAG, "Setup successful. Querying inventory.");
                mSetupDone = true;
                mWaitScreen = ProgressDialog.show(getActivity(), null, getText(R.string.fetching_product_info));

                // Query for already purchased items and SKU details for items what user can buy.
                List<String> skus = new ArrayList<String>();
                skus.add(SKU_PREMIUM);
                mHelper.queryInventoryAsync(true, skus, mGotInventoryListener);
            }
        }
    };

    /**
     * Callback for when inventory query is finished. See OpenIAB documentation for details.
     */
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
        new IabHelper.QueryInventoryFinishedListener() {
        
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                Log.d(TAG, "Failed to query inventory: " + result);
                toast(getString(R.string.failed_to_query_inventory, result));
                mTitleLine.setText(getString(R.string.failed_to_fetch_product_info));
            } else {
                Log.d(TAG, "Query inventory was successful.");
                SkuDetails premiumDetails = inventory.getSkuDetails(SKU_PREMIUM);
                
                if (premiumDetails != null) {
                    String title = premiumDetails.getTitle();
                    String description = premiumDetails.getDescription();
                    String price = premiumDetails.getPrice();
                    mTitleLine.setText(title);
                    mDescriptionLine.setText(description);
                    mPriceLine.setText(getString(R.string.upgrade_to_premium, price));
                } else {
                    mTitleLine.setText(getString(R.string.failed_to_fetch_product_info));
                }

                // Do we already have the premium upgrade purchased?
                Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
                mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
                Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

                if (mIsPremium) {
                    purchased();
                }
            }
            
            dismissWaitScreen();
        }
    };

    /**
     * Callback for when a purchase is finished. See OpenIAB documentation for details.
     */
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener =
        new IabHelper.OnIabPurchaseFinishedListener() {
        
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                alert(getString(R.string.error_purchasing, result));
            } else if (!verifyDeveloperPayload(purchase)) {
                alert(getString(R.string.error_purchasing,
                    getString(R.string.authenticity_verification_failed)));
            } else {
                Log.d(TAG, "Purchase successful.");

                if (purchase.getSku().equals(SKU_PREMIUM)) {
                    toast(getString(R.string.thank_you_for_purchasing));
                    mIsPremium = true;
                    purchased();
                }
            }
            
            dismissWaitScreen();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).setBackCallback(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.buy_premium_layout, container, false);
        mBuyButton = (Button) v.findViewById(R.id.buy_premium);

        mBuyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                purchasePremium();
            }
        });

        mTitleLine = (TextView) v.findViewById(R.id.premium_title_line);
        mDescriptionLine = (TextView) v.findViewById(R.id.premium_description_line);
        mPriceLine = (TextView) v.findViewById(R.id.premium_price_line);

        mDeveloperPayload = "You_should_pass_in_a_string_token_that_helps_your_application_to_identify_the_user_who_made_the_purchase";

        if (Base64EncodedPublicKey.contains("INSERT_YOUR")) {
            mBuyButton.setEnabled(false);
            mTitleLine.setText(getString(R.string.failed_to_fetch_product_info));
            alert(getString(R.string.missing_iap_key, "PurchasePremiumFragment"));
        } else {
            Map<String, String> storeKeys = new HashMap<String, String>();
            storeKeys.put(OpenIabHelper.NAME_GOOGLE, Base64EncodedPublicKey);

            try {
                mHelper = new OpenIabHelper(getActivity(), storeKeys);
                mHelper.startSetup(mSetupListener);
            }
            catch (IllegalArgumentException e) {
                mBuyButton.setEnabled(false);
                mTitleLine.setText(getString(R.string.failed_to_fetch_product_info));
                alert(getString(R.string.invalid_iap_key, "PurchasePremiumFragment"));
            }
        }

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    public void onBackPressed() {
        getFragmentManager().beginTransaction().remove(this).commit();
        ((MainActivity) getActivity()).setBackCallback(null);
    }

    /**
     * Dismisses the wait screen.
     */
    private void dismissWaitScreen() {
        mWaitScreen.dismiss();
        mWaitScreen = null;
    }

    /**
     * Updates the UI and unlocks the premium functionality when the purchase has been completed.
     */
    private void purchased() {
        mBuyButton.setText("Purchased");
        mBuyButton.setEnabled(false);
        Settings.setPremium(SKU_PREMIUM, getActivity());
        ((MainActivity) getActivity()).unlockPremium();
    }

    /**
     * Verifies the authenticity of the purchase.
     *
     * WARNING: Locally generating a random string when starting a purchase and
     * verifying it here might seem like a good approach, but this will fail in the
     * case where the user purchases an item on one device and then uses your app on
     * a different device, because on the other device you will not have access to the
     * random string you originally generated.
     *
     * So a good developer payload has these characteristics:
     *
     * 1. If two different users purchase an item, the payload is different between them,
     *    so that one user's purchase can't be replayed to another user.
     *
     * 2. The payload must be such that you can verify it even when the app wasn't the
     *    one who initiated the purchase flow (so that items purchased by the user on
     *    one device work on other devices owned by the user).
     *
     * Using your own server to store and verify developer payloads across app
     * installations is recommended.
     *
     * @param purchase The {@link org.onepf.oms.appstore.googleUtils.Purchase} to verify.
     * @return <code>true</code> if the payload was valid, <code>false</code> if not.
     */
    private boolean verifyDeveloperPayload(Purchase purchase) {
        String payload = purchase.getDeveloperPayload();

        return payload.equals(mDeveloperPayload);
    }

    /**
     * Initiates the purchase process for the premium product if not already purchased.
     */
    public void purchasePremium() {
        if (!mSetupDone) {
            toast(getString(R.string.billing_setup_not_complete_yet));
        } else {
            if (!mIsPremium) {
                mWaitScreen = ProgressDialog.show(getActivity(), null, getText(R.string.purchasing_product));
                mHelper.launchPurchaseFlow(getActivity(), SKU_PREMIUM, RC_REQUEST, mPurchaseFinishedListener, mDeveloperPayload);
            } else {
                toast(getString(R.string.premium_already_purchased));
                mBuyButton.setEnabled(false);
            }
        }
    }

    /**
     * Handles an in-app purchase activity result with request code = PurchasePremiumFragment.RC_REQUEST.
     * Called from {@link com.nokia.example.capturetheflag.MainActivity#onActivityResult(int, int, android.content.Intent)}.
     *
     * @param requestCode The request code.
     * @param resultCode  The result code.
     * @param data The data.
     */
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        mHelper.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * Creates and shows toast with the given text.
     *
     * @param message Text for the toast.
     */
    private void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Creates and shows an alert dialog with the given text.
     *
     * @param message Text for the alert dialog.
     */
    private void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
        bld.setMessage(message);
        bld.setNeutralButton(getString(R.string.ok), null);
        bld.create().show();
    }
}
