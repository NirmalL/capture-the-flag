package com.nokia.example.capturetheflag.notifications.nokia;

import android.content.Context;

import com.nokia.example.capturetheflag.notifications.NotificationsManagerBase;
import com.nokia.push.PushRegistrar;

public class NotificationsManagerNokia extends NotificationsManagerBase {

    public NotificationsManagerNokia(Context context) {
        super(context);
    }

    @Override
    public void register() {
        PushRegistrar.checkDevice(mContext);
        PushRegistrar.checkManifest(mContext);
        final String regId = PushRegistrar.getRegistrationId(mContext);
        if (regId == null || regId.isEmpty()) {
            PushRegistrar.register(mContext, PushIntentService.SENDER_ID);
        }
    }

    @Override
    public String getRegistrationId() {
        return PushRegistrar.getRegistrationId(mContext);
    }

    @Override
    public void onDestroy() {
        PushRegistrar.onDestroy(mContext);
    }

    @Override
    public NotificationServiceType getServiceType() {
        return NotificationServiceType.NOKIA_PUSH_MESSAGING;
    }
}
