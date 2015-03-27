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

package com.saulcintero.moveon.services;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.view.KeyEvent;

import com.saulcintero.moveon.enums.NotificationTypes;

public class MediaButtonEventReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(Context mContext, Intent intent) {
		String intentAction = intent.getAction();
		if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intentAction)) {
		} else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			if (event == null)
				return;

			if (event.getAction() == KeyEvent.ACTION_UP) {
				Intent i = new Intent("android.intent.action.ACTION_SAY_PRACTICE_INFORMATION");
				i.putExtra("type", String.valueOf(NotificationTypes.TIME.getTypes()));
				mContext.sendBroadcast(i);
			}
		}
	}
}