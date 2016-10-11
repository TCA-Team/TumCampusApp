/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tum.in.tumcampusapp.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;

import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.notifications.Alarm;
import de.tum.in.tumcampusapp.notifications.Chat;
import de.tum.in.tumcampusapp.notifications.GenericNotification;
import de.tum.in.tumcampusapp.notifications.Update;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmReceiverService extends GcmListenerService {

    private static final String PAYLOAD = "payload";

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param extras Data bundle containing message data as key/value pairs.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle extras) {
        //Check that we have some data and the intent was indeed a gcm message (gcm might be subject to change in the future)
        if (extras.isEmpty()) {
            return;
        }  // has effect of un-parcelling Bundle
        //Legacy messages need to be handled - maybe some data is missing?
        if (!extras.containsKey(PAYLOAD) || !extras.containsKey("type")) {

            //Try to match it as a legacy chat notification
            try {
                this.postNotification(new Chat(extras, this, -1));
            } catch (Exception e) {
                //@todo do something
            }
        } else {
            //Get some important values
            int notification = Integer.parseInt(extras.getString("notification"));
            int type = Integer.parseInt(extras.getString("type"));

            //Initialize our outputs
            GenericNotification n = null;

            Utils.logv("Notification recieved: " + extras);

            //switch on the type as both the type and payload must be present
            switch (type) { //https://github.com/TCA-Team/TumCampusApp/wiki/GCM-Message-format
                case 0: //Nothing to do, just confirm the retrieved notification
                    try {
                        TUMCabeClient.getInstance(this).confirm(notification);
                    } catch (IOException e) {
                        Utils.log(e);
                    }
                    break;
                case 1: //Chat
                    n = new Chat(extras.getString(PAYLOAD), this, notification);
                    break;
                case 2: //Update
                    n = new Update(extras.getString(PAYLOAD), this, notification);
                    break;
                case 3: //Alert
                    n = new Alarm(extras.getString(PAYLOAD), this, notification);
                    break;
            }

            //Post & save the notification if it was of any significance
            if (n != null) {
                this.postNotification(n);

                //Send confirmation if type requires it
                try {
                    n.sendConfirmation();
                } catch (IOException e) {
                    Utils.log(e);
                }

                //de.tum.in.tumcampusapp.models.managers.NotificationManager man = new de.tum.in.tumcampusapp.models.managers.NotificationManager(this);
                //@todo save to our notificationmanager
            }
        }
    }

    /**
     * @param n the Notification to post
     */
    private void postNotification(GenericNotification n) {
        if (n != null) {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification note = n.getNotification();
            if (note != null) {
                mNotificationManager.notify(n.getNotificationIdentification(), note);
            }
        }
    }


}