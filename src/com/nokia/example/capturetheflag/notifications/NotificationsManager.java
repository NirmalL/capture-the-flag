package com.nokia.example.capturetheflag.notifications;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.*;
import com.nokia.push.PushRegistrar;

/**
 * This class provides a common interface for registering the app to both the
 * GCM and NNA services by wrapping the method calls of both GCMRegistrar and
 * PushRegistrar classes.
 */
public class NotificationsManager {
    private static final String TAG = "NNASingleAPKSample/NotificationsManager";

    private static final String PREFS_KEY_SENDER_ID = "sender_id";
    private static final String PREFS_KEY_REGISTRATION_ID = "registration_id";
    
    private static final String GCM_SENDER_ID = "1006294830624";
    
    public enum SupportedNotificationServices {
        None,
        NokiaPushNotifications,
        GCM
    };

    private static NotificationsManager mInstance;
    private Context mContext;
    private SupportedNotificationServices mSupportedService;
    
    private GoogleCloudMessaging gcm;
    private String registrationId;

    /**
     * Provides the singleton instance of this class.
     * 
     * @param context The application context.
     * @return The singleton instance of this class.
     * @throws NullPointerException If the context is null.
     */
    public static NotificationsManager getInstance(Context context)
        throws NullPointerException, IllegalArgumentException
    {
        if (context == null) {
            throw new NullPointerException("Context is null!");
        }
        
        if (mInstance == null) {
            mInstance = new NotificationsManager(context);
        }
        else if (context != mInstance.mContext) {
            Log.w(TAG, "getInstance(): The given context does not match the previous one, switching.");
            mInstance.mContext = context;
        }
        
        return mInstance;
    }

    /**
     * Constructor.
     * 
     * @param context The application context.
     * @throws NullPointerException If the context is null.
     */
    private NotificationsManager(Context context)
    {
        mContext = context;
        
        // Check the device for supported notifications service
        try {
            PushRegistrar.checkDevice(mContext);
            mSupportedService = SupportedNotificationServices.NokiaPushNotifications;
            Log.i(TAG, "Supported service: Nokia Push Notifications");
        }
        catch (UnsupportedOperationException e1) {
            try {
            	gcm = GoogleCloudMessaging.getInstance(context);
                mSupportedService = SupportedNotificationServices.GCM;
                Log.i(TAG, "Supported service: GCM");
            }
            catch (UnsupportedOperationException e2) {
                mSupportedService = SupportedNotificationServices.None;
                Log.w(TAG, "No supported notifications services!");
            }
        }
    }

    public static String getSenderId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        
        // Check the supported notification service and resolve the default
        // sender ID based on that.
        NotificationsManager.SupportedNotificationServices supportedService =
                NotificationsManager.getInstance(context).getSupportedService();
        String defaultSenderId = null;
        
        switch (supportedService) {
            case NokiaPushNotifications:
                defaultSenderId = "123";
                break;
            case GCM:
                defaultSenderId = "123";
                break;
            default:
                defaultSenderId = "";
                break;
        }
        
        return prefs.getString(PREFS_KEY_SENDER_ID, defaultSenderId);
    }
    
    public static boolean setSenderId(Context context, String id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.edit().putString(PREFS_KEY_SENDER_ID, id).commit();
    }
    
    /** 
     * @return The supported notifications service. This is resolved during the
     * class construction.
     */
    public SupportedNotificationServices getSupportedService() {
        return mSupportedService;
    }

    /** 
     * @return The notification/registration ID if available, null otherwise.
     */
    public String getRegistrationId(Context context) {
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String registrationId = prefs.getString(PREFS_KEY_REGISTRATION_ID, "");
    	if (registrationId.isEmpty()) {
    		Log.i(TAG, "Registration not found.");
    	    }        
        return registrationId;
    }

    /**
     * Registers the application to the service using the stored service/sender
     * ID.
     */
    public void register(Context context) {
        final String id =""; // CommonUtilities.getSenderId(mContext);
        Log.i(TAG, "register(): ID: " + id);
        
        if (mSupportedService == SupportedNotificationServices.NokiaPushNotifications) {
            PushRegistrar.register(mContext, id);
        }
        else if (mSupportedService == SupportedNotificationServices.GCM) {
            gcm = GoogleCloudMessaging.getInstance(context);
            String registrationId = getRegistrationId(context);

            if (registrationId.isEmpty()) {
                registerInBackground(context);
            }
        }
    }

    /**
     * Unregisters the application.
     */
    public void unregister() {
        Log.i(TAG, "unregister()");
        
        if (mSupportedService == SupportedNotificationServices.NokiaPushNotifications) {
            PushRegistrar.unregister(mContext);
        }
        else if (mSupportedService == SupportedNotificationServices.GCM) {
            //GCMRegistrar.unregister(mContext);
        }
    }

    /**
     * Executes the necessary operations when the application is terminated.
     */
    public void onDestroy() {
        if (mSupportedService == SupportedNotificationServices.NokiaPushNotifications) {
            PushRegistrar.onDestroy(mContext);
        }
        else if (mSupportedService == SupportedNotificationServices.GCM) {
            //GCMRegistrar.onDestroy(mContext);
        }
    }
    
    private void registerInBackground(final Context context) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    String regid = gcm.register(GCM_SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    //sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    //storeRegistrationId(context, regid);
                } catch (IOException ex) {
                	Log.d(TAG, "IO ERROR REGISTERING: " + ex.toString());
                }
                return msg;
            }
        }.execute(null, null, null);
    }    
    
}