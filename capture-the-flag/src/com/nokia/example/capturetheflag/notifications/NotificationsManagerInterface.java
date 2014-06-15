/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file provided with this project for more information.
 */

package com.nokia.example.capturetheflag.notifications;

/**
 * Interface for registering the application to the platform's push notification
 * service for receiving push notifications from the game server.
 */
public interface NotificationsManagerInterface {

    /**
     * Enumeration defining supported notification services types,
     *
     * @see {@link NotificationsManagerInterface#getServiceType()}.
     */
    public enum NotificationServiceType {
        NOKIA_NOTIFICATIONS,
        GOOGLE_CLOUD_MESSAGING
    }

    /**
     * Registers the application for receiving push notifications.
     *
     * @see {@link NotificationsManagerInterface#getRegistrationId()}.
     */
    public void register();

    /**
     * Returns the application's registration id.
     *
     * @return The registration id if available, null otherwise.
     * @see {@link NotificationsManagerInterface#register()}.
     */
    public String getRegistrationId();

    /**
     * Returns the type of the instantiated push notification service.
     *
     * @return Notification service type.
     * @see {@link NotificationServiceType}.
     */
    public NotificationServiceType getServiceType();

    /**
     * Releases any allocated resources and closes opened connections when
     * the application is terminated.
     */
    public void onDestroy();
}
