/*
 * Copyright (C) 2015-present Saul Cintero <http://www.saulcintero.com>.
 * 
 * This file is part of MoveOn Sports Tracker.
 *
 * MoveOn Sports Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MoveOn Sports Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MoveOn Sports Tracker.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.saulcintero.moveon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.saulcintero.moveon.enums.NotificationTypes;
import com.saulcintero.moveon.listeners.TimeListener;

public class TimeManager implements TimeListener {
	private Context mContext;
	private SharedPreferences prefs;

	private Intent i = new Intent("android.intent.action.ACTION_SAY_PRACTICE_INFORMATION");

	private int seconds = 0;

	public TimeManager(Context context) {
		mContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	@Override
	public void onTime(int time) {
		this.seconds = time;

		if (prefs.getString("time_interval_selection", mContext.getString(R.string.disabled)).matches(
				mContext.getString(R.string.disabled)) == false) {
			if (seconds % (prefs.getInt("time_interval", 1) * 60) == 0) {
				i.putExtra("type", String.valueOf(NotificationTypes.TIME.getTypes()));
				mContext.sendBroadcast(i);
			}
		}
	}
}