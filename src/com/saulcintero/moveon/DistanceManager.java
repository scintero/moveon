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
import com.saulcintero.moveon.listeners.DistanceListener;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.utils.FunctionUtils;

public class DistanceManager implements DistanceListener {
	private Context mContext;
	private SharedPreferences prefs;

	private Intent i = new Intent("android.intent.action.ACTION_SAY_PRACTICE_INFORMATION");

	private int meters = 0, lastDistanceUnit = 0, distanceUnit;

	private boolean isMetric;

	public DistanceManager(Context context) {
		mContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public int getMetersValue() {
		return meters;
	}

	public void setMetersValue(int value) {
		meters = value;
	}

	@Override
	public void onDistance(int meters) {
		if ((MoveOnService.getIsPracticeRunning()) && (!MoveOnService.getIsPracticePaused())) {
			this.meters += meters;
			isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);
			distanceUnit = (isMetric ? 1000 : 1609);

			if (prefs.getString("distance_interval_selection", mContext.getString(R.string.disabled))
					.matches(mContext.getString(R.string.disabled)) == false) {

				if (((int) (this.meters / distanceUnit)) % (prefs.getInt("distance_interval", 1)) == 0
						&& lastDistanceUnit < (int) (this.meters / distanceUnit)) {
					lastDistanceUnit = (int) (this.meters / distanceUnit);
					i.putExtra("type", String.valueOf(isMetric ? NotificationTypes.METRIC_DISTANCE.getTypes()
							: NotificationTypes.IMPERIAL_DISTANCE.getTypes()));
					mContext.sendBroadcast(i);
				}
			}
		}
	}
}
