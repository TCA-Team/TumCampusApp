package de.tum.in.tumcampusapp.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.ChatActivity;
import de.tum.in.tumcampusapp.activities.ChatRoomsActivity;
import de.tum.in.tumcampusapp.activities.MainActivity;
import de.tum.in.tumcampusapp.api.TUMCabeClient;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.database.dataAccessObjects.ChatMessageDao;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;
import de.tum.in.tumcampusapp.managers.CardManager;
import de.tum.in.tumcampusapp.models.gcm.GCMChat;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMember;
import de.tum.in.tumcampusapp.models.tumcabe.ChatMessage;
import de.tum.in.tumcampusapp.models.tumcabe.ChatRoom;
import de.tum.in.tumcampusapp.models.tumcabe.ChatVerification;

public class Chat extends GenericNotification {

    private static final int NOTIFICATION_ID = CardManager.CARD_CHAT;

    private final GCMChat extras;

    private ChatRoom chatRoom;
    private String notificationText;
    private TaskStackBuilder sBuilder;
    private final ChatMessageDao chatMessageDao;


    public Chat(Bundle extras, Context context, int notfication) {
        super(context, 1, notfication, true);

        //Initialize the object keeping important infos about the update
        this.extras = new GCMChat();

        //Get the update details
        this.extras.setRoom(Integer.parseInt(extras.getString("room")));
        this.extras.setMember(Integer.parseInt(extras.getString("member")));

        //Message part is only present if we have a updated message
        if (extras.containsKey("message")) {
            this.extras.setMessage(Integer.parseInt(extras.getString("message")));
        } else {
            this.extras.setMessage(-1);
        }

        try {
            this.prepare();
        } catch (IOException e) {
            Utils.log(e);
        }
        TcaDb tcaDb = TcaDb.getInstance(context);
        chatMessageDao = tcaDb.chatMessageDao();
    }

    public Chat(String payload, Context context, int notfication) {
        super(context, 1, notfication, true);

        //Check if a payload was passed
        if (payload == null) {
            throw new IllegalArgumentException();
        }

        // parse data
        this.extras = new Gson().fromJson(payload, GCMChat.class);

        try {
            this.prepare();
        } catch (IOException e) {
            Utils.log(e);
        }
        TcaDb tcaDb = TcaDb.getInstance(context);
        chatMessageDao = tcaDb.chatMessageDao();
    }
    //chatRoom might be wrong one
    private Cursor getNewMessages(ChatMember member, int messageId) throws NoPrivateKey, IOException {
        List<ChatMessage> messages;
        if (messageId == -1) {
            messages = TUMCabeClient.getInstance(context)
                                    .getNewMessages(chatRoom.getId(), ChatVerification.Companion.getChatVerification(context, member));
        } else {
            messages = TUMCabeClient.getInstance(context)
                                    .getMessages(chatRoom.getId(), messageId, ChatVerification.Companion.getChatVerification(context, member));
        }
        ChatMember chatMember = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);

        if (member == null) {
            return null;
        }
        for (ChatMessage msg : messages) {
            if (msg == null || msg.getText() == null) {
                Utils.log("Message empty");
                return null;
            }

            Utils.logv("replace " + msg.getText() + " " + msg.getId() + " " + msg.getPrevious() + " " + msg.getSendingStatus());

            // Query read status from the previous message and use this read status as well if it is "0"
            //might be wrong chatMemger.getId
            boolean read = chatMember.getId() == msg.getMember()
                                        .getId();
            int status = chatMessageDao.getRead(msg.getId());
            if (status == 1) {
                read = true;
            }
            msg.setSendingStatus(ChatMessage.STATUS_SENT);
            msg.setRead(read);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
            Date date;
            try {
                date = formatter.parse(msg.getTimestamp());
            } catch (ParseException e) {
                date = new Date();
            }
            msg.setTimestamp(Utils.getDateTimeString(date));
            chatMessageDao.replaceMessage(msg);
/*
            replaceMessage(m);
*/
        }
        return chatMessageDao.getUnread(chatRoom.getId());
    }

    private void prepare() throws IOException {
        Utils.logv("Received GCM notification: room=" + this.extras.getRoom() + " member=" + this.extras.getMember() + " message=" + this.extras.getMessage());

        // Get the data necessary for the ChatActivity
        ChatMember member = Utils.getSetting(context, Const.CHAT_MEMBER, ChatMember.class);
        chatRoom = TUMCabeClient.getInstance(context)
                                .getChatRoom(this.extras.getRoom());

        chatMessageDao.deleteOldEntries();
        try (Cursor messages = getNewMessages(member,this.extras.getMessage())) {
            // Notify any open chat activity that a message has been received
            Intent intent = new Intent("chat-message-received");
            intent.putExtra("GCMChat", this.extras);
            LocalBroadcastManager.getInstance(context)
                                 .sendBroadcast(intent);

            notificationText = null;
            if (messages != null && messages.moveToFirst()) {
                do {
                    if (notificationText == null) {
                        notificationText = messages.getString(3);
                    } else {
                        notificationText += "\n" + messages.getString(3);
                    }
                } while (messages.moveToNext());
            }

            // Put the data into the intent
            Intent notificationIntent = new Intent(context, ChatActivity.class);
            notificationIntent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(chatRoom));

            sBuilder = TaskStackBuilder.create(context);
            sBuilder.addNextIntent(new Intent(context, MainActivity.class));
            sBuilder.addNextIntent(new Intent(context, ChatRoomsActivity.class));
            sBuilder.addNextIntent(notificationIntent);
        } catch (NoPrivateKey noPrivateKey) {
            Utils.log(noPrivateKey);
        }
    }

    @Override
    public Notification getNotification() {
        //Check if chat is currently open then don't show a notification if it is
        if (ChatActivity.mCurrentOpenChatRoom != null && this.extras.getRoom() == ChatActivity.mCurrentOpenChatRoom.getId()) {
            return null;
        }

        if (Utils.getSettingBool(context, "card_chat_phone", true) && this.extras.getMessage() == -1) {

            PendingIntent contentIntent = sBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

            // GCMNotification sound
            Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.message);

            String replyLabel = context.getResources()
                                       .getString(R.string.reply_label);

            RemoteInput remoteInput = new RemoteInput.Builder(ChatActivity.EXTRA_VOICE_REPLY)
                    .setLabel(replyLabel)
                    .build();

            // Create the reply action and add the remote input
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                                                          context.getString(R.string.reply_label), contentIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            //Create a nice notification
            return new NotificationCompat.Builder(context, Const.NOTIFICATION_CHANNEL_DEFAULT)
                    .setSmallIcon(this.icon)
                    .setContentTitle(chatRoom.getName()
                                             .substring(4))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                    .setContentText(notificationText)
                    .setContentIntent(contentIntent)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLights(0xff0000ff, 500, 500)
                    .setSound(sound)
                    .setAutoCancel(true)
                    .extend(new NotificationCompat.WearableExtender().addAction(action))
                    .build();

        }
        return null;
    }

    @Override
    public int getNotificationIdentification() {
        return (this.extras.getRoom() << 4) + Chat.NOTIFICATION_ID;
    }
}
