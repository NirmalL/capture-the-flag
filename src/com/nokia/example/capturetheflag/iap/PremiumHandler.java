/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.iap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;

import android.app.Activity;
import android.content.ComponentName;
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
    
    String base64EncodedPublicKey   = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8A4rv1uXF5mqJGrtGkQ5PQGpyNIgcZhvRD3yNLC5T+NlIlvMlkuGUmgZnXHfPdORZT/s5QXa2ytjffOyDVgXpHrZ0J9bRoR+hePP4o0ANzdEY/ehkt0EsifB2Kjhok+kTNpikplwuFtIJnIyFyukcesPAXksu2LTQAEzYwlMeJ8W4ToDHw6U5gEXLZcMKiDVTFA0pb89wVfb76Uerv9c6lrydKZiTn/gxg8J1yrz7vNzX7IzoWPO0+pXLnkcgqtEHePF2DIW1D29GkNJOt6xH3IvyS4ZI+1xs3wuSg8vWq3fQP/XIVHZQOqd5pmJY0tdgzboHuqq3ebtNrBI6Ky0SwIDAQAB";
    
    static final String SKU_PREMIUM = "android.test.purchased";
    
    static {
    	OpenIabHelper.mapSku(SKU_PREMIUM, OpenIabHelper.NAME_NOKIA, PREMIUM_PRODUCT_ID);
    }
    
    OpenIabHelper mHelper;
    private boolean mIsPremium;
    
    static final int RC_REQUEST = 10001;
    
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
        
        /* Old one
        Intent paymentEnabler = new Intent("com.nokia.payment.iapenabler.InAppBillingService.BIND");
        paymentEnabler.setPackage("com.nokia.payment.iapenabler"); 
        a.bindService(paymentEnabler, this, Context.BIND_AUTO_CREATE);
        */
        
        Map<String, String> storeKeys = new HashMap<String, String>();
       // storeKeys.put(OpenIabHelper.NAME_GOOGLE, base64EncodedPublicKey);
        
        mHelper = new OpenIabHelper(mActivity, storeKeys);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    //complain("Problem setting up in-app billing: " + result);
                	
                	Log.e(TAG, "Problem setting up in-app billing: " + result);
                    
                    return;
                }
                Log.d(TAG, "Setup successful. Querying inventory.");
                	mHelper.queryInventoryAsync(mGotInventoryListener);
                }
        });
        
    }
    
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    	public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                //complain("Failed to query inventory: " + result);
                Log.d(TAG, "Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");
            
            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */
            
            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
                        
            //TODO notify listener!
            // updateUi();
            
            //TODO implement some wait screen
            //setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };
    
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
    	mHelper.handleActivityResult(requestCode, resultCode, data);
    }
    
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        
        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
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
         */
        
        return true;
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
        }
        catch (RemoteException e) {
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
                }
                catch (Exception e) {
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
    	/*
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
                }
                catch (Exception e) {
                    Log.e(TAG, "Error in purchase prosess: " + e.toString(), e);
                }
                
                return null;
            }
        };
        
        purchase.execute();
        */
    	
    	String payload = "set this payload where you can recognize user";
    	
    	mHelper.launchPurchaseFlow(mActivity, SKU_PREMIUM, RC_REQUEST, 	mPurchaseFinishedListener, payload);
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
                }
                catch (Exception e) {
                    Log.e(TAG, "Failed to get price: " + e.getMessage(), e);
                }
                
                return price;
            }

            @Override
            protected void onPostExecute(String result) {
                notifyPrice(result);
            }
        };
        
        //getPrices.execute();
        notifyPrice("0,00");
    }
    
 // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
            	Log.d(TAG, "Error purchasing: " + result);
                //complain("Error purchasing: " + result);
                //setWaitScreen(false);
                return;
            }
            
            if (!verifyDeveloperPayload(purchase)) {
                //complain("Error purchasing. Authenticity verification failed.");
                //setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                //alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                //updateUi();
                //setWaitScreen(false);
            }
        }
    };

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
