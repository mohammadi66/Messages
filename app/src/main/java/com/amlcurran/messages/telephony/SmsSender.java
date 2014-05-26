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

package com.amlcurran.messages.telephony;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import com.amlcurran.messages.MessagesApp;
import com.amlcurran.messages.data.SmsMessage;
import com.amlcurran.messages.events.BroadcastEventBus;

import java.util.ArrayList;

public class SmsSender extends IntentService implements SmsDatabaseWriter.SentWriteListener {

    public static final String TAG = SmsSender.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "message";
    public static final String ACTION_SEND_REQUEST = "send_request";
    public static final String ACTION_MESSAGE_SENT = "message_send";
    private static final String EXTRA_OUTBOX_URI = "outbox_uri";

    private final SmsManager smsManager;
    private final SmsDatabaseWriter smsDatabaseWriter;
    private final BroadcastEventBus eventBus;

    public SmsSender() {
        super(TAG);
        smsManager = SmsManager.getDefault();
        smsDatabaseWriter = new SmsDatabaseWriter();
        eventBus = new BroadcastEventBus(this);
        setIntentRedelivery(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, intent.toString());
        if (isSendRequest(intent)) {
            SmsMessage message = intent.getParcelableExtra(EXTRA_MESSAGE);
            sendMessage(message);
        } else if (isSentNotification(intent)) {
            int result = intent.getIntExtra("result", 0);
            SmsMessage message = intent.getParcelableExtra(EXTRA_MESSAGE);
            Uri outboxSms = Uri.parse(intent.getStringExtra(EXTRA_OUTBOX_URI));
            if (result == Activity.RESULT_OK) {
                deleteOutboxMessages(message.getAddress());
                writeMessageToProvider(message);
            } else {
                notifyFailureToSend(message, result);
            }
        }
    }

    private void deleteOutboxMessages(String address) {
        smsDatabaseWriter.deleteOutboxMessages(getContentResolver(), address);
    }

    private void notifyFailureToSend(SmsMessage message, int result) {
        MessagesApp.getNotifier(this).showSendError(message);
    }

    private void writeMessageToProvider(SmsMessage message) {
        smsDatabaseWriter.writeSentMessage(getContentResolver(), this, message);
        Log.d(TAG, "Write sent message to provider " + message.toString());
    }

    private void sendMessage(final SmsMessage message) {
        smsDatabaseWriter.writeOutboxSms(getContentResolver(), new SmsDatabaseWriter.OutboxWriteListener() {
            @Override
            public void onWrittenToOutbox(Uri inserted) {
                eventBus.postMessageSending(message);
                Log.d(TAG, "Sending message: " + message.toString());
                ArrayList<PendingIntent> messageSendIntents = getMessageSendIntents(message, inserted);
                smsManager.sendMultipartTextMessage(message.getAddress(), null, smsManager.divideMessage(message.getBody()), messageSendIntents, null);
            }

            @Override
            public void onOutboxWriteFailed() {

            }
        }, message);
    }

    private ArrayList<PendingIntent> getMessageSendIntents(SmsMessage message, Uri inserted) {
        Intent intent = new Intent(this, SmsReceiver.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_OUTBOX_URI, inserted.toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        ArrayList<PendingIntent> pendingIntents = new ArrayList<PendingIntent>();
        pendingIntents.add(pendingIntent);
        return pendingIntents;
    }

    private boolean isSentNotification(Intent intent) {
        return intent.getAction().equals(ACTION_MESSAGE_SENT);
    }

    private boolean isSendRequest(Intent intent) {
        return intent.getAction().equals(ACTION_SEND_REQUEST);
    }

    @Override
    public void onWrittenToSentBox() {
        Log.d(TAG, "Sending broadcast for sent message");
        new BroadcastEventBus(this).postMessageSent();
    }

    @Override
    public void onSentBoxWriteFailed() {
        Log.e(TAG, "Failed to write a sent message to the database");
    }
}