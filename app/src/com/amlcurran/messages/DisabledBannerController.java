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

package com.amlcurran.messages;

import android.view.View;

import com.amlcurran.messages.ui.control.FragmentController;

public class DisabledBannerController {
    private final View disabledBanner;

    public DisabledBannerController(MessagesActivity messagesActivity, FragmentController.FragmentCallback viewCallback) {
        disabledBanner = messagesActivity.findViewById(R.id.disabled_banner);
        disabledBanner.setOnClickListener(new ChangeDefaultMessagingApp(viewCallback));
    }

    public void hideBanner() {
        disabledBanner.setVisibility(View.GONE);
    }

    public void showBanner() {
        disabledBanner.setVisibility(View.VISIBLE);
    }

    private class ChangeDefaultMessagingApp implements View.OnClickListener {
        private final FragmentController.FragmentCallback viewCallback;

        public ChangeDefaultMessagingApp(FragmentController.FragmentCallback viewCallback) {
            this.viewCallback = viewCallback;
        }

        @Override
        public void onClick(View v) {
            viewCallback.defaultsBannerPressed();
        }
    }
}
