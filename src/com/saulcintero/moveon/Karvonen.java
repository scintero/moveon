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

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class Karvonen extends Activity {
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private Button dateBtn, calculateBtn, cancelBtn, saveBtn;
	private EditText restingHr, maxHr, hrZone1Min, hrZone1Max, hrZone2Min, hrZone2Max, hrZone3Min,
			hrZone3Max, hrZone4Min, hrZone4Max, hrZone5Min, hrZone5Max;

	private final int DATE_DIALOG_ID = 0;
	private int year, month, day, yearsFromBirthDay, mMaxHr, mRestingHr, zone1Min, zone1Max, zone2Min,
			zone2Max, zone3Min, zone3Max, zone4Min, zone4Max, zone5Min, zone5Max;
	private String mBirthday;

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		if (dateBtn.getText().toString().length() > 0)
			savedInstanceState.putString("mBirthday", dateBtn.getText().toString());
		if (maxHr.getText().toString().length() > 0)
			savedInstanceState.putInt("mMaxHr", Integer.parseInt(maxHr.getText().toString()));
		if (restingHr.getText().toString().length() > 0)
			savedInstanceState.putInt("mRestingHr", Integer.parseInt(restingHr.getText().toString()));
		if (hrZone1Min.getText().toString().length() > 0)
			savedInstanceState.putInt("zone1Min", Integer.parseInt(hrZone1Min.getText().toString()));
		if (hrZone1Max.getText().toString().length() > 0)
			savedInstanceState.putInt("zone1Max", Integer.parseInt(hrZone1Max.getText().toString()));
		if (hrZone2Min.getText().toString().length() > 0)
			savedInstanceState.putInt("zone2Min", Integer.parseInt(hrZone2Min.getText().toString()));
		if (hrZone2Max.getText().toString().length() > 0)
			savedInstanceState.putInt("zone2Max", Integer.parseInt(hrZone2Max.getText().toString()));
		if (hrZone3Min.getText().toString().length() > 0)
			savedInstanceState.putInt("zone3Min", Integer.parseInt(hrZone3Min.getText().toString()));
		if (hrZone3Max.getText().toString().length() > 0)
			savedInstanceState.putInt("zone3Max", Integer.parseInt(hrZone3Max.getText().toString()));
		if (hrZone4Min.getText().toString().length() > 0)
			savedInstanceState.putInt("zone4Min", Integer.parseInt(hrZone4Min.getText().toString()));
		if (hrZone4Max.getText().toString().length() > 0)
			savedInstanceState.putInt("zone4Max", Integer.parseInt(hrZone4Max.getText().toString()));
		if (hrZone5Min.getText().toString().length() > 0)
			savedInstanceState.putInt("zone5Min", Integer.parseInt(hrZone5Min.getText().toString()));
		if (hrZone5Max.getText().toString().length() > 0)
			savedInstanceState.putInt("zone5Max", Integer.parseInt(hrZone5Max.getText().toString()));

		savedInstanceState.putInt("yearsFromBirthDay", yearsFromBirthDay);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		setContentView(R.layout.karvonen);

		dateBtn = (Button) findViewById(R.id.birthday_date);
		calculateBtn = (Button) findViewById(R.id.calculate);
		cancelBtn = (Button) findViewById(R.id.cancel);
		saveBtn = (Button) findViewById(R.id.save);
		restingHr = (EditText) findViewById(R.id.resting_hr);
		maxHr = (EditText) findViewById(R.id.max_hr);
		hrZone1Min = (EditText) findViewById(R.id.hr_zone1_min);
		hrZone1Max = (EditText) findViewById(R.id.hr_zone1_max);
		hrZone2Min = (EditText) findViewById(R.id.hr_zone2_min);
		hrZone2Max = (EditText) findViewById(R.id.hr_zone2_max);
		hrZone3Min = (EditText) findViewById(R.id.hr_zone3_min);
		hrZone3Max = (EditText) findViewById(R.id.hr_zone3_max);
		hrZone4Min = (EditText) findViewById(R.id.hr_zone4_min);
		hrZone4Max = (EditText) findViewById(R.id.hr_zone4_max);
		hrZone5Min = (EditText) findViewById(R.id.hr_zone5_min);
		hrZone5Max = (EditText) findViewById(R.id.hr_zone5_max);

		yearsFromBirthDay = 0;

		Calendar cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		updateDisplay();

		restoreSavedData();

		if (savedInstanceState != null)
			restoreFormData(savedInstanceState);

		dateBtn.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveAndExit();
			}
		});

		cancelBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		calculateBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				calculateHrZones();
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, pDateSetListener, year, month, day);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int y, int monthOfYear, int dayOfMonth) {
			year = y;
			month = monthOfYear;
			day = dayOfMonth;

			updateDisplay();
		}
	};

	private void updateDisplay() {
		String sDay = String.valueOf(day), sMonth = String.valueOf(month + 1);
		int iMonth = month;

		if (day < 10)
			sDay = "0" + sDay;

		iMonth = month + 1; // Month is 0 based so add 1
		if (month < 10)
			sMonth = "0" + iMonth;

		dateBtn.setText(new StringBuilder().append(sDay).append("/").append(sMonth).append("/").append(year)
				.append(" "));
	}

	private void restoreSavedData() {
		boolean loadData = false;

		if (prefs.getInt("BirthdayYear", 0) > 0) {
			loadData = true;

			mBirthday = prefs.getString("BirthdayString", "");
			year = prefs.getInt("BirthdayYear", 0);
			month = prefs.getInt("BirthdayMonth", 0);
			day = prefs.getInt("BirthdayDay", 0);

			yearsFromBirthDay = FunctionUtils.getAge(FunctionUtils
					.getDateFromParameters(year, month - 1, day));
		}
		if (prefs.getInt("maxHr", 0) > 0)
			mMaxHr = prefs.getInt("maxHr", 0);
		if (prefs.getInt("restingHr", 0) > 0)
			mRestingHr = prefs.getInt("restingHr", 0);
		if (prefs.getInt("hrZone1Min", 0) > 0)
			zone1Min = prefs.getInt("hrZone1Min", 0);
		if (prefs.getInt("hrZone1Max", 0) > 0)
			zone1Max = prefs.getInt("hrZone1Max", 0);
		if (prefs.getInt("hrZone2Min", 0) > 0)
			zone2Min = prefs.getInt("hrZone2Min", 0);
		if (prefs.getInt("hrZone2Max", 0) > 0)
			zone2Max = prefs.getInt("hrZone2Max", 0);
		if (prefs.getInt("hrZone3Min", 0) > 0)
			zone3Min = prefs.getInt("hrZone3Min", 0);
		if (prefs.getInt("hrZone3Max", 0) > 0)
			zone3Max = prefs.getInt("hrZone3Max", 0);
		if (prefs.getInt("hrZone4Min", 0) > 0)
			zone4Min = prefs.getInt("hrZone4Min", 0);
		if (prefs.getInt("hrZone4Max", 0) > 0)
			zone4Max = prefs.getInt("hrZone4Max", 0);
		if (prefs.getInt("hrZone5Min", 0) > 0)
			zone5Min = prefs.getInt("hrZone5Min", 0);
		if (prefs.getInt("hrZone5Max", 0) > 0)
			zone5Max = prefs.getInt("hrZone5Max", 0);

		if (loadData)
			loadDataInUiWidgets();
	}

	private void restoreFormData(Bundle savedInstanceState) {
		if (savedInstanceState.getString("mBirthday") != null)
			mBirthday = savedInstanceState.getString("mBirthday");
		if (savedInstanceState.getInt("mMaxHr") > 0)
			mMaxHr = savedInstanceState.getInt("mMaxHr");
		if (savedInstanceState.getInt("mRestingHr") > 0)
			mRestingHr = savedInstanceState.getInt("mRestingHr");
		if (savedInstanceState.getInt("zone1Min") > 0)
			zone1Min = savedInstanceState.getInt("zone1Min");
		if (savedInstanceState.getInt("zone1Max") > 0)
			zone1Max = savedInstanceState.getInt("zone1Max");
		if (savedInstanceState.getInt("zone2Min") > 0)
			zone2Min = savedInstanceState.getInt("zone2Min");
		if (savedInstanceState.getInt("zone2Max") > 0)
			zone2Max = savedInstanceState.getInt("zone2Max");
		if (savedInstanceState.getInt("zone3Min") > 0)
			zone3Min = savedInstanceState.getInt("zone3Min");
		if (savedInstanceState.getInt("zone3Max") > 0)
			zone3Max = savedInstanceState.getInt("zone3Max");
		if (savedInstanceState.getInt("zone4Min") > 0)
			zone4Min = savedInstanceState.getInt("zone4Min");
		if (savedInstanceState.getInt("zone4Max") > 0)
			zone4Max = savedInstanceState.getInt("zone4Max");
		if (savedInstanceState.getInt("zone5Min") > 0)
			zone5Min = savedInstanceState.getInt("zone5Min");
		if (savedInstanceState.getInt("zone5Max") > 0)
			zone5Max = savedInstanceState.getInt("zone5Max");
		yearsFromBirthDay = savedInstanceState.getInt("yearsFromBirthDay");

		loadDataInUiWidgets();
	}

	private void loadDataInUiWidgets() {
		dateBtn.setText(mBirthday);
		maxHr.setText(String.valueOf(mMaxHr));
		restingHr.setText(String.valueOf(mRestingHr));
		hrZone1Min.setText(String.valueOf(zone1Min));
		hrZone1Max.setText(String.valueOf(zone1Max));
		hrZone2Min.setText(String.valueOf(zone2Min));
		hrZone2Max.setText(String.valueOf(zone2Max));
		hrZone3Min.setText(String.valueOf(zone3Min));
		hrZone3Max.setText(String.valueOf(zone3Max));
		hrZone4Min.setText(String.valueOf(zone4Min));
		hrZone4Max.setText(String.valueOf(zone4Max));
		hrZone5Min.setText(String.valueOf(zone5Min));
		hrZone5Max.setText(String.valueOf(zone5Max));
	}

	private void calculateHrZones() {
		if (restingHr.getText().toString().length() > 0) {
			yearsFromBirthDay = FunctionUtils.getAge(FunctionUtils
					.getDateFromParameters(year, month - 1, day));
			if (yearsFromBirthDay > 0) {
				mMaxHr = (220 - yearsFromBirthDay);
				mRestingHr = Integer.parseInt(restingHr.getText().toString());
				zone1Min = (int) ((mMaxHr - mRestingHr) * 0.5f + mRestingHr);
				zone1Max = (int) ((mMaxHr - mRestingHr) * 0.6f + mRestingHr);
				zone2Min = zone1Max + 1;
				zone2Max = (int) ((mMaxHr - mRestingHr) * 0.7f + mRestingHr);
				zone3Min = zone2Max + 1;
				zone3Max = (int) ((mMaxHr - mRestingHr) * 0.8f + mRestingHr);
				zone4Min = zone3Max + 1;
				zone4Max = (int) ((mMaxHr - mRestingHr) * 0.9f + mRestingHr);
				zone5Min = zone4Max + 1;
				zone5Max = (int) ((mMaxHr - mRestingHr) + mRestingHr);

				maxHr.setText(String.valueOf(mMaxHr));
				hrZone1Min.setText(String.valueOf(zone1Min));
				hrZone1Max.setText(String.valueOf(zone1Max));
				hrZone2Min.setText(String.valueOf(zone2Min));
				hrZone2Max.setText(String.valueOf(zone2Max));
				hrZone3Min.setText(String.valueOf(zone3Min));
				hrZone3Max.setText(String.valueOf(zone3Max));
				hrZone4Min.setText(String.valueOf(zone4Min));
				hrZone4Max.setText(String.valueOf(zone4Max));
				hrZone5Min.setText(String.valueOf(zone5Min));
				hrZone5Max.setText(String.valueOf(zone5Max));
			} else {
				UIFunctionUtils.showMessage(this, false, getString(R.string.at_least_one_year));
			}
		} else {
			UIFunctionUtils.showMessage(this, true, getString(R.string.hr_required));
		}
	}

	private void saveAndExit() {
		boolean missingData = false;
		ArrayList<String> errorText = new ArrayList<String>();

		if (yearsFromBirthDay == 0) {
			missingData = true;
			errorText.add(getString(R.string.at_least_one_year));
		}
		if (maxHr.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.max_hr_missing));
		}
		if (restingHr.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.resting_hr_missing));
		}
		if (hrZone1Min.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.min_hr_missing) + " " + getString(R.string.in_zone1));
		}
		if (hrZone1Max.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.max_hr_missing) + " " + getString(R.string.in_zone1));
		}
		if (hrZone2Min.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.min_hr_missing) + " " + getString(R.string.in_zone2));
		}
		if (hrZone2Max.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.max_hr_missing) + " " + getString(R.string.in_zone2));
		}
		if (hrZone3Min.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.min_hr_missing) + " " + getString(R.string.in_zone3));
		}
		if (hrZone3Max.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.max_hr_missing) + " " + getString(R.string.in_zone3));
		}
		if (hrZone4Min.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.min_hr_missing) + " " + getString(R.string.in_zone4));
		}
		if (hrZone4Max.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.max_hr_missing) + " " + getString(R.string.in_zone4));
		}
		if (hrZone5Min.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.min_hr_missing) + " " + getString(R.string.in_zone5));
		}
		if (hrZone5Max.getText().toString().length() == 0) {
			missingData = true;
			errorText.add(getString(R.string.max_hr_missing) + " " + getString(R.string.in_zone5));
		}

		if (!missingData) {
			editor = prefs.edit();
			editor.putString("BirthdayString", dateBtn.getText().toString());
			editor.putInt("BirthdayYear", year);
			editor.putInt("BirthdayMonth", month);
			editor.putInt("BirthdayDay", day);
			editor.putInt("maxHr", Integer.parseInt(maxHr.getText().toString()));
			editor.putInt("restingHr", Integer.parseInt(restingHr.getText().toString()));
			editor.putInt("hrZone1Min", Integer.parseInt(hrZone1Min.getText().toString()));
			editor.putInt("hrZone1Max", Integer.parseInt(hrZone1Max.getText().toString()));
			editor.putInt("hrZone2Min", Integer.parseInt(hrZone2Min.getText().toString()));
			editor.putInt("hrZone2Max", Integer.parseInt(hrZone2Max.getText().toString()));
			editor.putInt("hrZone3Min", Integer.parseInt(hrZone3Min.getText().toString()));
			editor.putInt("hrZone3Max", Integer.parseInt(hrZone3Max.getText().toString()));
			editor.putInt("hrZone4Min", Integer.parseInt(hrZone4Min.getText().toString()));
			editor.putInt("hrZone4Max", Integer.parseInt(hrZone4Max.getText().toString()));
			editor.putInt("hrZone5Min", Integer.parseInt(hrZone5Min.getText().toString()));
			editor.putInt("hrZone5Max", Integer.parseInt(hrZone5Max.getText().toString()));
			editor.commit();

			finish();
		} else {
			for (String error : errorText) {
				UIFunctionUtils.showMessage(this, true, error);
			}
		}
	}
}