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

package com.amlcurran.messages.threads;

import android.view.MenuItem;

import com.amlcurran.messages.DependencyRepository;
import com.amlcurran.messages.ExternalEventManager;
import com.amlcurran.messages.R;
import com.amlcurran.messages.core.TextUtils;
import com.amlcurran.messages.core.data.Contact;
import com.amlcurran.messages.core.data.DraftRepository;
import com.amlcurran.messages.core.data.PhoneNumberOnlyContact;
import com.amlcurran.messages.core.data.SmsMessage;
import com.amlcurran.messages.core.loaders.MessagesLoader;
import com.amlcurran.messages.core.loaders.OnContactQueryListener;
import com.amlcurran.messages.core.threads.Thread;
import com.amlcurran.messages.telephony.DefaultAppChecker;
import com.amlcurran.messages.ui.ComposeMessageView;
import com.github.amlcurran.sourcebinder.ListSource;

import java.util.Collections;
import java.util.List;

class ThreadViewController implements ComposeMessageView.ComposureCallbacks {

    private final Contact contact;
    private final String composedMessage;
    private final ThreadView threadView;
    private final ListSource<SmsMessage> source;
    private final DefaultAppChecker defaultChecker;
    private final DraftRepository draftRepository;
    private final ExternalEventManager externalEventManager;
    private final MessagesLoader messageLoader;
    private final Thread thread;

    public ThreadViewController(Thread thread, Contact contact, String composedMessage, ThreadView threadView, DefaultAppChecker defaultChecker, DependencyRepository dependencyRepository) {
        this.contact = contact;
        this.composedMessage = composedMessage;
        this.threadView = threadView;
        this.messageLoader = dependencyRepository.getMessagesLoader();
        this.defaultChecker = defaultChecker;
        this.draftRepository = dependencyRepository.getDraftRepository();
        this.externalEventManager = dependencyRepository.getExternalEventManager();
        this.source = new ListSource<>();
        this.thread = thread;
    }

    void start() {
        setUpContactView(contact);
        defaultChecker.checkSmsApp(threadView);
        thread.setCallbacks(callbacks);
        thread.load();
        retrieveDraft(composedMessage);
    }

    void stop() {
        thread.unsetCallbacks();
        saveDraft();
    }

    private void retrieveDraft(String composedMessage) {
        if (TextUtils.isNotEmpty(composedMessage)) {
            threadView.setComposedMessage(composedMessage);
        } else {
            threadView.setComposedMessage(draftRepository.getDraft(contact.getNumber()));
        }
    }

    private void saveDraft() {
        if (TextUtils.isText(threadView.getComposedMessage())) {
            draftRepository.storeDraft(contact.getNumber(), threadView.getComposedMessage());
        } else {
            draftRepository.clearDraft(contact.getNumber());
        }
    }

    private Thread.ThreadCallbacks callbacks = new Thread.ThreadCallbacks() {
        @Override
        public void threadLoaded(List<SmsMessage> messageList) {
            Collections.reverse(messageList);
            source.replace(messageList);
            messageLoader.markThreadAsRead(thread.getId());
        }
    };

    public ListSource<SmsMessage> getSource() {
        return source;
    }

    private void setUpContactView(Contact contact) {
        if (contact instanceof PhoneNumberOnlyContact) {
            messageLoader.queryContact(contact.getNumber(), new OnContactQueryListener() {
                @Override
                public void contactLoaded(Contact contact) {
                    threadView.bindContactToHeader(contact);
                }
            });
        } else {
            threadView.bindContactToHeader(contact);
        }
    }

    public boolean menuItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.menu_call) {
            externalEventManager.callNumber(contact.getNumber());
            return true;
        } else if (item.getItemId() == R.id.modal_mark_unread) {
            messageLoader.markThreadsAsUnread(Collections.singletonList(thread.getId()));
            threadView.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onMessageComposed(CharSequence body) {
        thread.sendMessage(body);
    }

    public interface ThreadView extends DefaultAppChecker.Callback {

        void bindContactToHeader(Contact contact);

        String getComposedMessage();

        void setComposedMessage(String composedMessage);

        void finish();
    }

}
