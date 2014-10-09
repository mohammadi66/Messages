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

package com.amlcurran.messages.core.analysis;

import com.amlcurran.messages.core.data.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsMessageAnalyser {

    private static final int ONE_MINUTE_IN_SECS = 60;
    private static final int ONE_HOUR_IN_MINS = 60;
    private static final int MAX_HOURS_TO_SHOW = 6;
    private final DifferenceStringProvider differencesStringProvider;
    private final DateFormat formatter = new SimpleDateFormat("HH:mm dd-MMM-yy");
    private final Date date = new Date();

    public SmsMessageAnalyser(DifferenceStringProvider differencesStringProvider) {
        this.differencesStringProvider = differencesStringProvider;
    }

    private boolean isYesterday(Time time) {
        Time now = Time.fromMillis(System.currentTimeMillis());
        date.setTime(now.toMillis());
        return false;
    }

    public String getDifferenceToNow(Time time) {
        long millisDifference = Math.abs(time.toMillis() - Time.fromMillis(System.currentTimeMillis()).toMillis());
        long secondsDifference = millisDifference / 1000;
        if (secondsDifference > ONE_MINUTE_IN_SECS) {
            long minutesDifference = secondsDifference / ONE_MINUTE_IN_SECS;
            if (minutesDifference > ONE_HOUR_IN_MINS) {
                long hoursDifference = minutesDifference / ONE_HOUR_IN_MINS;
                if (hoursDifference > MAX_HOURS_TO_SHOW) {
                    if (isYesterday(time)) {
                        return differencesStringProvider.yesterday();
                    }
                    date.setTime(time.toMillis());
                    return formatter.format(date);
                } else {
                    return differencesStringProvider.hoursDifference(hoursDifference);
                }
            } else {
                return differencesStringProvider.minutesDifference(minutesDifference);
            }
        } else {
            return differencesStringProvider.underAMinute();
        }
    }
}