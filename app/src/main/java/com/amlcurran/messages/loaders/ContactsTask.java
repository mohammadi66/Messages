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

package com.amlcurran.messages.loaders;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.amlcurran.messages.ContactListListener;
import com.amlcurran.messages.core.data.Contact;
import com.amlcurran.messages.data.ContactFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ContactsTask implements Callable {
    private final ContentResolver resolver;
    private final ContactListListener contactListListener;

    public ContactsTask(ContentResolver resolver, ContactListListener contactListListener) {
        this.resolver = resolver;
        this.contactListListener = contactListListener;
    }

    @Override
    public Object call() throws Exception {
        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        List<Contact> contacts = new ArrayList<Contact>();
        Contact tempPointer;
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            tempPointer = ContactFactory.fromCursor(cursor);
            if (!contacts.contains(tempPointer)) {
                contacts.add(tempPointer);
            }
        }
        cursor.close();
        contactListListener.contactListLoaded(contacts);
        return null;
    }
}
