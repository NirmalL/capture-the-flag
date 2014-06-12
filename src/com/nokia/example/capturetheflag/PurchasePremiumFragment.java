/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;
import org.onepf.oms.appstore.googleUtils.SkuDetails;

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

/**
 * A fragment for upgrading the app to premium version. 
 * Uses the OpenIAB library to request product info and 
 * purchasing premium version. 
 * 
 */
public class PurchasePremiumFragment extends Fragment implements MainActivity.BackCallback {
    
	private static final String TAG = "CtF/PurchasePremiumFragment";
	public static final String FRAGMENT_TAG = "PurchasePremiumFragment";
    
	public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String NOKIA_PREMIUM_PRODUCT_ID = "1023610"; // Test ID
    
    //TODO for test purpose only, remove before release
    String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE";
    
    static final String SKU_PREMIUM = "android.test.purchased";
    
    static {
    	OpenIabHelper.mapSku(SKU_PREMIUM, OpenIabHelper.NAME_NOKIA, NOKIA_PREMIUM_PRODUCT_ID);
    }
    
    OpenIabHelper mHelper;
    private boolean mIsPremium;
    private boolean mSetupDone = false;
    private String mDeveloperPayload;
    
    static final int RC_REQUEST = 10001;
    
    private TextView mTitleLine;
    private TextView mDescriptionLine;
	private TextView mPriceLine;
	private Button mBuyButton;
	
	private ProgressDialog mWaitScreen;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity)activity).setBackCallback(this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.buy_premium_layout, container, false);
        mBuyButton = (Button)v.findViewById(R.id.buy_premium);
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
        
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
        	mBuyButton.setEnabled(false);
            alert("Please put your app's public key in PurchasePremiumFragment.java. See README.");
            
        } else {
        	Map<String, String> storeKeys = new HashMap<String, String>();
            storeKeys.put(OpenIabHelper.NAME_GOOGLE, base64EncodedPublicKey);
            
            mHelper = new OpenIabHelper(getActivity(), storeKeys);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        toast("Problem setting up in-app billing: " + result);
                    	Log.e(TAG, "Problem setting up in-app billing: " + result);
                        return;
                    }
                    
                    mSetupDone = true;
                    Log.d(TAG, "Setup successful. Querying inventory.");
                    
                    mWaitScreen = ProgressDialog.show(getActivity(), null, getText(R.string.fetching_product_info));
                    
                    //Query for already purchased items and 
                    //SKU details(title, description, price) for items what user can buy
                    List<String> skus = new ArrayList<String>();
                    skus.add(SKU_PREMIUM);
                    mHelper.queryInventoryAsync(true, skus, mGotInventoryListener);
                }
            });
        }
        
        return v;
    }
    
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    	public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
            	toast("Failed to query inventory: " + result);
                Log.d(TAG, "Failed to query inventory: " + result);
                mTitleLine.setText("Failed to fetch product info");
                dismissWaitScreen();
                return;
            }

            SkuDetails premiumDetails = inventory.getSkuDetails(SKU_PREMIUM);
            if (premiumDetails != null) {
            	String title = premiumDetails.getTitle();
            	String description = premiumDetails.getDescription();
            	String price = premiumDetails.getPrice();
            	
            	mTitleLine.setText(title);
            	mDescriptionLine.setText(description);
            	mPriceLine.setText("Upgrade to premium (" + price + ") (user not charged!)");
                
			} else {
            	mTitleLine.setText("Failed to fetch product info");
            }
            
            Log.d(TAG, "Query inventory was successful.");
            
            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
            
            if (mIsPremium) {
            	purchased();
			}
            
            dismissWaitScreen();  
        }
    };
    
    private void dismissWaitScreen() {
    	mWaitScreen.dismiss();
    	mWaitScreen = null;
    }
    
    private void purchased() {
    	mBuyButton.setText("Purchased");
    	mBuyButton.setEnabled(false);
    	Settings.setPremium(SKU_PREMIUM, getActivity());
    	((MainActivity) getActivity()).unlockPremium();
    }
    
    boolean verifyDeveloperPayload(Purchase p) {
        /*
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
         */
        
    	String payload = p.getDeveloperPayload();
        if (payload.equals(mDeveloperPayload)) {
        	
        	Log.d(TAG, "Developer payload was successful");
        	
        	return true;
		} else {
			
			Log.d(TAG, "Developer payload failed");
			
			return false;
		}
    }
    
    public void purchasePremium() {
    	if (!mSetupDone) {
			toast("Billing setup is not completed yet, try again after a while.");
			return;
		}
    	
    	if (!mIsPremium) {
    		mWaitScreen = ProgressDialog.show(getActivity(), null, getText(R.string.purchasing_product));
    		mHelper.launchPurchaseFlow(getActivity(), SKU_PREMIUM, RC_REQUEST, 	mPurchaseFinishedListener, mDeveloperPayload);
		} else {
			toast("Premium already purchased.");
			mBuyButton.setEnabled(false);
		}
    }
    
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
            	Log.d(TAG, "Error purchasing: " + result);
            	alert("Error purchasing: " + result);
                dismissWaitScreen();
                return;
            }
            
            if (!verifyDeveloperPayload(purchase)) {
                alert("Error purchasing. Authenticity verification failed.");
                dismissWaitScreen();
                return;
            }
            
            Log.d(TAG, "Purchase successful.");
            dismissWaitScreen();
            
            if (purchase.getSku().equals(SKU_PREMIUM)) {
                toast("Thank you for upgrading to premium!");
                mIsPremium = true;
                purchased();
            }
        }
    };
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (mHelper != null) {
        	mHelper.dispose();        
            mHelper = null;
		}
    }
    
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
    	mHelper.handleActivityResult(requestCode, resultCode, data);
    }
    
    private void toast(String message) {
    	Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }	
    
    private void alert(String message) {
    	AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }    

    @Override
    public void onBackPressed() {
        getFragmentManager().beginTransaction().remove(this).commit();
        ((MainActivity)getActivity()).setBackCallback(null);   
    }
}
