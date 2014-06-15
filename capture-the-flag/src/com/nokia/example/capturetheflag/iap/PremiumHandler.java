/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.iap;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nokia.example.capturetheflag.Settings;
import com.nokia.payment.iap.aidl.INokiaIAPService;

/**
 * @deprecated Not used in current implementation, where IAP is handled via OpenIAB
 * <p/>
 * Class to handling all the server communication related to IAP. Almost
 * identical to the class used in IAP example.
 */
public class PremiumHandler implements ServiceConnection {
    public static final String ITEM_TYPE_INAPP = "inapp";
    public static final String PREMIUM_PRODUCT_ID = "1023610"; // Test ID

    public static final int RESULT_OK = 0;
    private static final String TAG = "CtF/PremiumHandler";
    private static final String RESPONSE_CODE_KEY = "RESPONSE_CODE";
    private static final String ITEM_ID_LIST_KEY = "ITEM_ID_LIST";
    private static final String IAP_ITEM_LIST_KEY = "INAPP_PURCHASE_ITEM_LIST";
    private static final String DETAILS_LIST_KEY = "DETAILS_LIST";
    private static final String PRODUCT_ID_KEY = "productId";
    private static final String PRICE_KEY = "price";
    private static final int BILLING_API_VERSION = 3;

    private INokiaIAPService mService;
    private Activity mActivity;
    private ArrayList<PremiumHandlerListener> mListeners;
    private boolean mPremiumAlreadyChecked = false;

    public PremiumHandler() {
        mListeners = new ArrayList<PremiumHandler.PremiumHandlerListener>();
    }

    public void connect(Activity a) {
        Log.d(TAG, "Connecting");
        mActivity = a;

        Intent paymentEnabler = new Intent("com.nokia.payment.iapenabler.InAppBillingService.BIND");
        paymentEnabler.setPackage("com.nokia.payment.iapenabler");
        a.bindService(paymentEnabler, this, Context.BIND_AUTO_CREATE);

    }


    public void handleActivityResult(int requestCode, int resultCode, Intent data) {

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = INokiaIAPService.Stub.asInterface(service);
        int response = -1;

        try {
            response = mService.isBillingSupported(
                    BILLING_API_VERSION, mActivity.getPackageName(), ITEM_TYPE_INAPP);

            if (response == RESULT_OK) {
                for (PremiumHandlerListener listener : mListeners) {
                    listener.IapInitialized(response);
                }

                Log.d(TAG, "Billing is supported");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Error requesting billing support: " + e.toString(), e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }


    /**
     * Check if the premium version has been purchased. The result will be sent
     * to listeners' setPremiumPurchased().
     */
    public void checkIfPremiumPurchased() {
        // First check from settings if premium already purchased
        if (Settings.getPremium(mActivity).length() > 0) {
            notifyIsPremium(true);
            return;
        }

        // Check if premium check from server is done, if so use the settings
        if (mPremiumAlreadyChecked) {
            notifyIsPremium(Settings.getPremium(mActivity).length() > 0);
            return;
        }

        Log.d(TAG, "Checking premium version can be restored from server...");
        AsyncTask<Void, Void, Boolean> request = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean isPremium = false;

                try {
                    Bundle productBundle = new Bundle();
                    ArrayList<String> productIdArray = new ArrayList<String>();
                    productIdArray.add(PREMIUM_PRODUCT_ID);
                    productBundle.putStringArrayList(ITEM_ID_LIST_KEY, productIdArray);

                    Bundle purchases = mService.getPurchases(
                            BILLING_API_VERSION, mActivity.getPackageName(),
                            "inapp", productBundle, null);

                    int response = purchases.getInt(RESPONSE_CODE_KEY);

                    if (response == RESULT_OK) {
                        ArrayList<String> ownedProducts =
                                purchases.getStringArrayList(IAP_ITEM_LIST_KEY);

                        for (String ownedProduct : ownedProducts) {
                            Log.d(TAG, "Found owned product: " + ownedProduct);
                            JSONObject object = new JSONObject(ownedProduct);
                            String productId = object.getString(PRODUCT_ID_KEY);

                            if (productId.equals(PREMIUM_PRODUCT_ID)) {
                                Log.d(TAG, "Premium has been purchased.");
                                Settings.setPremium(productId, mActivity);
                                isPremium = true;
                                break;
                            }
                        }
                    }

                    Log.d(TAG, "Setting mPremiumAlreadyChecked to \"true\".");
                    mPremiumAlreadyChecked = true;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to check purchase status from server: " + e.toString(), e);
                }

                return isPremium;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                notifyIsPremium(result);
            }
        };

        request.execute();
    }

    public void purchasePremium() {
        AsyncTask<Void, Void, Void> purchase = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Bundle buyIntentBundle = mService.getBuyIntent(
                            BILLING_API_VERSION, mActivity.getPackageName(),
                            PREMIUM_PRODUCT_ID, ITEM_TYPE_INAPP, "");

                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    mActivity.startIntentSenderForResult(
                            pendingIntent.getIntentSender(),
                            Integer.valueOf(0),
                            new Intent(),
                            Integer.valueOf(0),
                            Integer.valueOf(0),
                            Integer.valueOf(0));
                } catch (Exception e) {
                    Log.e(TAG, "Error in purchase prosess: " + e.toString(), e);
                }

                return null;
            }
        };

        purchase.execute();
    }

    public void requestPrice() {
        Log.d(TAG, "requestPrice()");

        AsyncTask<Void, Void, String> getPrices = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String price = "0.00";
                Bundle productBundle = new Bundle();
                ArrayList<String> productIdArray = new ArrayList<String>();
                productIdArray.add(PREMIUM_PRODUCT_ID);
                productBundle.putStringArrayList(ITEM_ID_LIST_KEY, productIdArray);

                try {
                    Log.d(TAG, "requestPrice(): Package name: " + mActivity.getPackageName());

                    Bundle details = mService.getProductDetails(
                            BILLING_API_VERSION, mActivity.getPackageName(),
                            ITEM_TYPE_INAPP, productBundle);

                    int response = details.getInt(RESPONSE_CODE_KEY);
                    Log.d(TAG, "Price request response: " + response);

                    if (response == RESULT_OK) {
                        ArrayList<String> responseList =
                                details.getStringArrayList(DETAILS_LIST_KEY);

                        for (String thisResponse : responseList) {
                            Log.d(TAG, "requestPrice(): Response: " + thisResponse);
                            JSONObject object = new JSONObject(thisResponse);
                            String product = object.getString(PRODUCT_ID_KEY);

                            if (product.equals(PREMIUM_PRODUCT_ID)) {
                                price = object.getString(PRICE_KEY);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get price: " + e.getMessage(), e);
                }

                return price;
            }

            @Override
            protected void onPostExecute(String result) {
                notifyPrice(result);
            }
        };

        getPrices.execute();
    }

    public void cleanup() {
        mActivity.unbindService(this);
        mActivity = null;
    }

    public void addListener(PremiumHandlerListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(PremiumHandlerListener listener) {
        mListeners.remove(listener);
    }

    private void notifyIsPremium(final boolean isPremium) {
        Log.d(TAG, "Is premium: " + isPremium);

        for (PremiumHandlerListener listener : mListeners) {
            listener.setPremiumPurchased(isPremium);
        }
    }

    private void notifyPrice(final String price) {
        Log.d(TAG, "Premium price: " + price);

        for (PremiumHandlerListener listener : mListeners) {
            listener.onPriceReceived(price);
        }
    }

    /**
     * Interface for premium handler listeners.
     */
    public interface PremiumHandlerListener {
        public void IapInitialized(int resultcode);

        public void setPremiumPurchased(boolean isPurchased);

        public void onPriceReceived(String premiumPrice);
    }
}
