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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.util.Log;

import com.amlcurran.messages.MessagesApp;
import com.amlcurran.messages.data.SmsMessage;
import com.amlcurran.messages.events.BroadcastEventBus;

public class SmsReceiver extends BroadcastReceiver implements SmsDatabaseWriter.InboxWriteListener {

    public static final String TAG = SmsReceiver.class.getSimpleName();

    private final SmsDatabaseWriter smsDatabaseWriter;

    public SmsReceiver() {
        smsDatabaseWriter = new SmsDatabaseWriter();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(intent.getAction())) {
            android.telephony.SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            writeSmsToProvider(context, SmsMessage.fromDeliverBroadcast(messages));
        } else {
            Intent intent2 = new Intent(context, SmsSender.class);
            intent2.setAction(SmsSender.ACTION_MESSAGE_SENT);
            intent2.putExtras(intent.getExtras());
            int resultCode = getResultCode();
            intent2.putExtra("result", resultCode);
            context.startService(intent2);
        }
    }

    private void writeSmsToProvider(final Context context, final SmsMessage message) {
        smsDatabaseWriter.writeInboxSms(context.getContentResolver(), new SmsDatabaseWriter.InboxWriteListener() {
            @Override
            public void onWrittenToInbox() {
                new BroadcastEventBus(context).postMessageReceived();
                MessagesApp.getNotifier(context).updateUnreadNotification();
            }

            @Override
            public void onInboxWriteFailed() {
                Log.e(TAG, "Failed to write message to inbox database");
            }
        }, message);

    }

    @Override
    public void onWrittenToInbox() {
    }

    @Override
    public void onInboxWriteFailed() {

    }
}