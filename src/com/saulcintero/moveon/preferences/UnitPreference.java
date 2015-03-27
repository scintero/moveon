/*
 * Copyright (C) 2015-present, Saul Cintero www.saulcintero.com
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

package com.saulcintero.moveon.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.saulcintero.moveon.R;

public class UnitPreference extends ListPreference {
	private SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
	private SharedPreferences.Editor editor = prefs.edit();

	private String lastUnitValue, weight;

	double b_weight;

	public UnitPreference(Context context) {
		super(context);
	}

	public UnitPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setEntries(R.array.units_preference);
		setEntryValues(R.array.units_preference_values);
		setSummary(context.getString(R.string.units_setting_title));
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			if (getValue().equals(lastUnitValue)) {
			} else {
				weight = prefs.getString("body_weight", "75.0");

				if (getValue().equals(getContext().getString(R.string.long_unit1))) {
					editor.putString("units", getContext().getString(R.string.long_unit1));
					b_weight = (getPoundToKg(Double.parseDouble(weight)));
				} else {
					editor.putString("units", getContext().getString(R.string.long_unit2));
					b_weight = (getKgToPound(Double.parseDouble(weight)));
				}
				editor.putString("body_weight", String.valueOf(b_weight));
				editor.commit();

				getContext().sendBroadcast(new Intent("android.intent.action.REFRESH_ROUTES"));
				getContext().sendBroadcast(new Intent("android.intent.action.REFRESH_STATISTICS"));
			}
		}
	}

	private double getPoundToKg(double pound) {
		return pound * 0.4535923699997481;
	}

	private double getKgToPound(double kg) {
		return kg * 2.20462262185;
	}

	@Override
	protected void onClick() {
		lastUnitValue = prefs.getString("units", getContext().getString(R.string.long_unit1));

		super.onClick();
	}
}