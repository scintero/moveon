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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public final class Constants {
	public static final String SETTINGS_NAME = "SettingsActivity";
	public final static int GPS_ITEM_OFF = 1, GPS_ITEM_SEARCHING = 2, GPS_ITEM_ON = 3;
	public final static int VOICE_COACH_STATUS = 1, LOCKED_STATUS = 2;
	public final static int PRACTICE_STOP_STATUS = 0, PRACTICE_START_STATUS = 1,
			PRACTICE_PAUSE_OR_RESUME_STATUS = 2, PRACTICE_AUTO_PAUSE_STATUS = 3,
			PRACTICE_AUTO_RESUME_STATUS = 4;
	public final static int PRACTICE_FROM_STARTED_TO_PAUSED = 1, PRACTICE_FROM_PAUSED_TO_RESUMED = 2;
	public static final String BLUETOOTH_SENSOR_DEFAULT = "";
	public static final int CHEESE_BURGER = 294;
	public static final float ALL_THE_WAY_AROUND_THE_WORLD = 40076;
	public static final double TO_THE_MOON = 384.400;

	private Constants() {
		throw new UnsupportedOperationException();
	}

	public static String getString(Context context, int keyId, String defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		return sharedPreferences.getString(getKey(context, keyId), defaultValue);
	}

	public static String getKey(Context context, int keyId) {
		return context.getString(keyId);
	}

	public static void setString(Context context, int keyId, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(getKey(context, keyId), value);
		editor.commit();
	}

	public static int getInt(Context context, int keyId, int defaultValue) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		return sharedPreferences.getInt(getKey(context, keyId), defaultValue);
	}

	public static void setInt(Context context, int keyId, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt(getKey(context, keyId), value);
		editor.commit();
	}
}