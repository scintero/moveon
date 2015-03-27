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
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.saulcintero.moveon.utils.FunctionUtils;

abstract public class MeasurementPreference extends EditTextPreference {
	Context mContext;

	boolean mIsMetric;

	protected int mTitleResource;
	protected int mMetricUnitsResource;
	protected int mImperialUnitsResource;

	public MeasurementPreference(Context context) {
		super(context);

		this.mContext = context;

		initPreferenceDetails();
	}

	public MeasurementPreference(Context context, AttributeSet attr) {
		super(context, attr);

		this.mContext = context;

		initPreferenceDetails();
	}

	public MeasurementPreference(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);

		this.mContext = context;

		initPreferenceDetails();
	}

	abstract protected void initPreferenceDetails();

	protected void showDialog(Bundle state) {
		mIsMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		setDialogTitle(mContext.getString(mTitleResource) + " ("
				+ mContext.getString(mIsMetric ? mMetricUnitsResource : mImperialUnitsResource) + ")");

		try {
			setText(PreferenceManager.getDefaultSharedPreferences(mContext).getString("body_weight", "75.0"));
		} catch (Exception e) {
			setText("75.0");
		}

		super.showDialog(state);
	}

	protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
		editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		super.onAddEditTextToDialogView(dialogView, editText);
	}

	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			try {
				Float.valueOf(((CharSequence) (getEditText().getText())).toString());
			} catch (NumberFormatException e) {
				this.showDialog(null);
				return;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}