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

import com.amlcurran.messages.DependencyRepository;
import com.amlcurran.messages.core.data.Contact;
import com.amlcurran.messages.ui.contact.ContactClickListener;

public class DefaultContactClickListener implements ContactClickListener {
    private final DependencyRepository dependencyRepository;

    public DefaultContactClickListener(DependencyRepository dependencyRepository) {
        this.dependencyRepository = dependencyRepository;
    }

    @Override
    public void viewContact(Contact contact) {
        dependencyRepository.getExternalEventManager().viewContact(contact);
    }

    @Override
    public void addContact(Contact contact) {
        dependencyRepository.getExternalEventManager().addContact(contact);
    }
}
