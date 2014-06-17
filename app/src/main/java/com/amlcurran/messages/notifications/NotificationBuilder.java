/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amlcurran.messages.notifications;

import android.app.Notification;
import android.content.Context;

import com.amlcurran.messages.R;
import com.amlcurran.messages.core.data.Conversation;
import com.amlcurran.messages.data.InFlightSmsMessage;
import com.amlcurran.messages.preferences.PreferenceStore;

import java.util.Calendar;
import java.util.List;

public class NotificationBuilder {
    private static final long[] VIBRATE_PATTERN = new long[]{ 0, 200 };
    private final NotificationIntentFactory notificationIntentFactory;
    private final StyledTextFactory styledTextFactory;
    private Context context;
    private final PreferenceStore preferenceStore;

    public NotificationBuilder(Context context, PreferenceStore preferenceStore) {
        this.context = context;
        this.preferenceStore = preferenceStore;
        this.notificationIntentFactory = new NotificationIntentFactory(context);
        this.styledTextFactory = new StyledTextFactory();
    }

    public Notification buildUnreadNotification(List<Conversation> conversations) {
        if (conversations.size() == 1) {
            return buildSingleUnreadNotification(conversations.get(0));
        } else {
            return buildMultipleUnreadNotification(conversations);
        }
    }

    private Notification buildMultipleUnreadNotification(List<Conversation> conversations) {
        long timestampMillis = Calendar.getInstance().getTimeInMillis();
        return getDefaultBuilder()
                .setTicker(styledTextFactory.buildListSummary(conversations))
                .setStyle(buildInboxStyle(conversations))
                .setContentText(styledTextFactory.buildSenderList(conversations))
                .setContentTitle(styledTextFactory.buildListSummary(conversations))
                .setWhen(timestampMillis)
                .build();
    }

    private Notification.Style buildInboxStyle(List<Conversation> conversations) {
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        for (Conversation conversation : conversations) {
            inboxStyle.addLine(styledTextFactory.getInboxLine(conversation));
        }
        return inboxStyle;
    }

    private Notification buildSingleUnreadNotification(Conversation conversation) {
        long timestampMillis = Calendar.getInstance().getTimeInMillis();
        return getDefaultBuilder()
                .setTicker(styledTextFactory.buildTicker(conversation))
                .setContentTitle(conversation.getContact().getDisplayName())
                .setContentIntent(notificationIntentFactory.createViewConversationIntent(conversation))
                .setContentText(conversation.getBody())
                .setStyle(buildBigStyle(conversation))
                .setWhen(timestampMillis)
                .build();
    }

    private static Notification.Style buildBigStyle(Conversation conversation) {
        return new Notification.BigTextStyle()
                .bigText(conversation.getBody())
                .setBigContentTitle(conversation.getContact().getDisplayName());
    }

    private Notification.Builder getDefaultBuilder() {
        return new Notification.Builder(this.context)
                .setContentIntent(notificationIntentFactory.createLaunchActivityIntent())
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setSound(preferenceStore.getRingtoneUri())
                .setAutoCancel(true)
                .setVibrate(VIBRATE_PATTERN)
                .setSmallIcon(R.drawable.ic_notify_sms);
    }

    public Notification buildFailureToSendNotification(InFlightSmsMessage message) {
        return getDefaultBuilder()
                .setContentTitle(string(R.string.failed_to_send_message))
                .setTicker(string(R.string.failed_to_send_message))
                .setContentText(context.getString(R.string.couldnt_send_to, message.getAddress()))
                .addAction(R.drawable.ic_action_send_holo, string(R.string.resend), notificationIntentFactory.createResendIntent(message))
                .setSmallIcon(R.drawable.ic_notify_error)
                .build();
    }

    private String string(int resId) {
        return context.getString(resId);
    }

    public Notification buildMmsErrorNotification() {
        return getDefaultBuilder()
                .setContentTitle(string(R.string.mms_error_title))
                .setTicker(string(R.string.mms_error_title))
                .setContentText(string(R.string.mms_error_text))
                .setSmallIcon(R.drawable.ic_notify_error)
                .build();
    }
}
