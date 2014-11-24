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

package com.amlcurran.messages.conversationlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amlcurran.messages.R;

class ConversationViewCreator {
    public ConversationViewCreator() {
    }

    View createUnreadView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation_unread, parent, false);
        view.setTag(R.id.tag_view_holder, new ConversationViewHolder(view));
        return view;
    }

    ConversationViewHolder createUnreadViewHolder(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation_read, parent, false);
        return new ConversationViewHolder(view);
    }

    View createReadView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation_read, parent, false);
        view.setTag(R.id.tag_view_holder, new ConversationViewHolder(view));
        return view;
    }

    ConversationViewHolder createReadViewHolder(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation_read, parent, false);
        return new ConversationViewHolder(view);
    }

}