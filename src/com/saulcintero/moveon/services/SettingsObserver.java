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

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;

public class SettingsObserver extends ContentObserver {
	private static final String MEDIA_BUTTON_RECEIVER = "media_button_receiver";

	MoveOnService mMonitorService;
	ComponentName mComponentName;
	ContentResolver mContentResolver;
	Context mContext;

	SettingsObserver(MoveOnService monitorService, Context mContext, ComponentName mComponentName) {
		super(new Handler());

		this.mMonitorService = monitorService;
		this.mContext = mContext;
		this.mComponentName = mComponentName;

		mContentResolver = mMonitorService.getApplicationContext().getContentResolver();
		mContentResolver
				.registerContentObserver(Settings.System.getUriFor(MEDIA_BUTTON_RECEIVER), true, this);
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		super.onChange(selfChange);

		String receiverName = Settings.System.getString(mContentResolver, MEDIA_BUTTON_RECEIVER);

		if (!selfChange
				&& !receiverName.equals(mComponentName.flattenToString())
				&& !receiverName
						.equals("com.saulcintero.moveon/com.saulcintero.moveon.services.MediaButtonEventReceiver$1")) {
			mContext.sendBroadcast(new Intent("android.intent.action.UNREGISTER_MEDIA_BUTTON_STATUS"));
			mContext.sendBroadcast(new Intent("android.intent.action.REGISTER_MEDIA_BUTTON_STATUS"));
		}
	}
}