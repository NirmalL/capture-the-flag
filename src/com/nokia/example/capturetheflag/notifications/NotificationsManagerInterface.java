/**
 * Copyright (c) 2013-2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications;

/**
 * 
 */
public interface NotificationsManagerInterface {

    public enum NotificationServiceType {
        NOKIA_PUSH_MESSAGING,
        GOOGLE_CLOUD_MESSAGING
    }
    /**
     * 
     */
    public void register();
    
    /**
     * 
     * @return
     */
    public String getRegistrationId();
    
    /**
     * 
     * @return
     */
    public NotificationServiceType getServiceType();
    
    /**
     * 
     */
    public void onDestroy();

}
