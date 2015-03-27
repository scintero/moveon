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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class AddPractice extends Activity implements OnClickListener {
	private Context mContext;

	private final int DATE_DIALOG_ID = 0;
	private final int TIME_DIALOG_ID = 1;

	private Button date_btn, time_btn;
	private int year, month, day, hours, minutes;

	private String sHours, sMinutes;

	private Button save_btn, discard_btn;

	private Resources res;

	private EditText name, notes, hours_text, minutes_text, seconds, distance, avg_speed, max_speed,
			max_altitude, min_altitude, up_accum_altitude, down_accum_altitude, kcal, steps, avg_hr, max_hr,
			avg_cadence, max_cadence;

	private Spinner spinner1, spinner2;

	private DataManager DBManager = null;

	private int shoe_id, activity_id;

	private ArrayList<String> activities_name = new ArrayList<String>();
	private ArrayList<String> shoes_name = new ArrayList<String>();
	private ArrayList<Integer> shoes_id = new ArrayList<Integer>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);
		setContentView(R.layout.add_practice);

		super.onCreate(savedInstanceState);

		mContext = getApplicationContext();

		save_btn = (Button) findViewById(R.id.save);
		discard_btn = (Button) findViewById(R.id.cancel);
		date_btn = (Button) findViewById(R.id.date);
		time_btn = (Button) findViewById(R.id.time);
		name = (EditText) findViewById(R.id.practice_name);
		notes = (EditText) findViewById(R.id.notes);
		distance = (EditText) findViewById(R.id.practice_distance);
		avg_speed = (EditText) findViewById(R.id.practice_avg_speed);
		max_speed = (EditText) findViewById(R.id.practice_max_speed);
		max_altitude = (EditText) findViewById(R.id.practice_max_altitude);
		min_altitude = (EditText) findViewById(R.id.practice_min_altitude);
		up_accum_altitude = (EditText) findViewById(R.id.practice_up_accum_altitude);
		down_accum_altitude = (EditText) findViewById(R.id.practice_down_accum_altitude);
		kcal = (EditText) findViewById(R.id.practice_kcal);
		steps = (EditText) findViewById(R.id.practice_steps);
		avg_hr = (EditText) findViewById(R.id.practice_avg_hr);
		max_hr = (EditText) findViewById(R.id.practice_max_hr);
		avg_cadence = (EditText) findViewById(R.id.practice_avg_cadence);
		max_cadence = (EditText) findViewById(R.id.practice_max_cadence);
		hours_text = (EditText) findViewById(R.id.practice_time_hours);
		minutes_text = (EditText) findViewById(R.id.practice_time_minutes);
		seconds = (EditText) findViewById(R.id.practice_time_seconds);
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner2 = (Spinner) findViewById(R.id.spinner2);

		res = mContext.getResources();
		final String[] activities = res.getStringArray(R.array.activities);

		for (int b = 0; b < activities.length; b++) {
			activities_name.add(activities[b]);
		}

		activity_id = 1;

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, activities);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(spinnerArrayAdapter);

		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.CustomQuery(getString(R.string.db_query_getting_shoes),
				"SELECT * FROM shoes WHERE active=1 ORDER BY default_shoe DESC");
		cursor.moveToFirst();

		if (cursor.getCount() > 0) {
			shoe_id = cursor.getInt(cursor.getColumnIndex("_id"));

			for (int g = 0; g <= cursor.getCount() - 1; g++) {
				shoes_name.add(cursor.getString(cursor.getColumnIndex("name")));
				shoes_id.add(cursor.getInt(cursor.getColumnIndex("_id")));

				cursor.moveToNext();
			}
		}
		shoes_name.add(getString(R.string.none_text));
		shoes_id.add(0);
		shoe_id = 0;

		cursor.close();
		DBManager.Close();

		ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, shoes_name);
		spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(spinnerArrayAdapter2);

		final Calendar cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		hours = cal.get(Calendar.HOUR_OF_DAY);
		minutes = cal.get(Calendar.MINUTE);
		String sHours = String.valueOf(hours);
		String sMinutes = String.valueOf(minutes);
		if (hours < 10)
			sHours = "0" + sHours;
		if (minutes < 10)
			sMinutes = "0" + sMinutes;
		updateDisplay(DATE_DIALOG_ID);
		updateDisplay(TIME_DIALOG_ID);

		SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		String currentDate = sdfDate.format(new Date());
		String currentTime = sdfTime.format(new Date());

		name.setText(currentDate + ", " + currentTime);
		distance.setText("0.0");
		avg_speed.setText("0.0");
		max_speed.setText("0.0");
		max_altitude.setText("0");
		min_altitude.setText("0");
		up_accum_altitude.setText("0");
		down_accum_altitude.setText("0");
		kcal.setText("0");
		steps.setText("0");
		avg_hr.setText("0");
		max_hr.setText("0");
		avg_cadence.setText("0");
		max_cadence.setText("0");
		hours_text.setText("0");
		minutes_text.setText("0");
		seconds.setText("0");

		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				activity_id = (FunctionUtils.getIdForThisItem(String.valueOf(activities_name.get(i)),
						activities) + 1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				shoe_id = shoes_id.get(i);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		date_btn.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		time_btn.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
			}
		});

		save_btn.setOnClickListener(this);
		discard_btn.setOnClickListener(this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, pDateSetListener, year, month, day);
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, timePickerListener, hours, minutes, false);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int y, int monthOfYear, int dayOfMonth) {
			year = y;
			month = monthOfYear;
			day = dayOfMonth;

			updateDisplay(DATE_DIALOG_ID);
		}
	};

	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			hours = hourOfDay;
			minutes = minute;

			updateDisplay(TIME_DIALOG_ID);
		}
	};

	private void updateDisplay(int DIALOG_ID) {
		switch (DIALOG_ID) {
		case DATE_DIALOG_ID:
			String sDay = String.valueOf(day),
			sMonth = String.valueOf(month + 1);
			int iMonth = month;

			if (day < 10)
				sDay = "0" + sDay;

			iMonth = month + 1; // Month is 0 based so add 1
			if (month < 10)
				sMonth = "0" + iMonth;

			date_btn.setText(new StringBuilder().append(sDay).append("/").append(sMonth).append("/")
					.append(year).append(" "));
		case TIME_DIALOG_ID:
			sHours = String.valueOf(hours);
			sMinutes = String.valueOf(minutes);
			if (hours < 10)
				sHours = "0" + sHours;
			if (minutes < 10)
				sMinutes = "0" + sMinutes;

			time_btn.setText(new StringBuilder().append(sHours).append(":").append(sMinutes).append(" "));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.save:
			ArrayList<Integer> error_numbers = new ArrayList<Integer>();

			if (!FunctionUtils.isNumeric(distance.getText().toString()))
				error_numbers.add(1);
			if (!FunctionUtils.isNumeric(max_speed.getText().toString()))
				error_numbers.add(2);
			if (!FunctionUtils.isNumeric(max_altitude.getText().toString()))
				error_numbers.add(3);
			if (!FunctionUtils.isNumeric(min_altitude.getText().toString()))
				error_numbers.add(4);
			if (!FunctionUtils.isNumeric(kcal.getText().toString()))
				error_numbers.add(5);
			if (!FunctionUtils.isNumeric(steps.getText().toString()))
				error_numbers.add(6);
			if (!FunctionUtils.isNumeric(avg_hr.getText().toString()))
				error_numbers.add(7);
			if (!FunctionUtils.isNumeric(max_hr.getText().toString()))
				error_numbers.add(8);
			if (!FunctionUtils.isNumeric(hours_text.getText().toString()))
				error_numbers.add(9);
			if (!FunctionUtils.isNumeric(minutes_text.getText().toString()))
				error_numbers.add(10);
			if (!FunctionUtils.isNumeric(seconds.getText().toString()))
				error_numbers.add(11);
			if (!FunctionUtils.isNumeric(up_accum_altitude.getText().toString()))
				error_numbers.add(12);
			if (!FunctionUtils.isNumeric(down_accum_altitude.getText().toString()))
				error_numbers.add(13);
			if (!FunctionUtils.isNumeric(avg_speed.getText().toString()))
				error_numbers.add(14);
			if (!FunctionUtils.isNumeric(avg_cadence.getText().toString()))
				error_numbers.add(15);
			if (!FunctionUtils.isNumeric(max_cadence.getText().toString()))
				error_numbers.add(16);

			if (error_numbers.size() == 0) {
				int time_conversion = 0;
				boolean update_query = true;

				int mHours = Integer.parseInt(hours_text.getText().toString());
				if (mHours > 0)
					time_conversion = mHours * 3600;

				int mMinutes = Integer.parseInt(minutes_text.getText().toString());
				if ((mMinutes > 0) && (mMinutes < 60)) {
					time_conversion = time_conversion + mMinutes * 60;
				} else {
					if (mMinutes != 0) {
						update_query = false;

						UIFunctionUtils.showMessage(this, true, getString(R.string.minutes_value_error));
					}
				}

				int mSeconds = Integer.parseInt(seconds.getText().toString());
				if ((mSeconds > 0) && (mSeconds < 60)) {
					time_conversion = time_conversion + mSeconds;
				} else {
					if (mMinutes != 0) {
						update_query = false;

						UIFunctionUtils.showMessage(this, true, getString(R.string.seconds_value_error));
					}
				}

				if (update_query) {
					DBManager = new DataManager(mContext);
					DBManager.Open();

					String[] fields = { "category_id", "shoe_id", "name", "date", "hour", "time", "distance",
							"avg_speed", "max_speed", "kcal", "max_altitude", "min_altitude",
							"up_accum_altitude", "down_accum_altitude", "avg_hr", "max_hr", "steps",
							"avg_cadence", "max_cadence", "comments" };

					String sDay = String.valueOf(day), sMonth = String.valueOf(month + 1);
					int iMonth = month;
					if (day < 10)
						sDay = "0" + sDay;
					iMonth = month + 1;
					if (month < 10)
						sMonth = "0" + iMonth;
					String sDate = sDay + "/" + sMonth + "/" + year;

					String[] values = { String.valueOf(activity_id), String.valueOf(shoe_id),
							name.getText().toString(), sDate, sHours + ":" + sMinutes + ":" + "00",
							String.valueOf(time_conversion), distance.getText().toString(),
							avg_speed.getText().toString(), max_speed.getText().toString(),
							kcal.getText().toString(), max_altitude.getText().toString(),
							min_altitude.getText().toString(), up_accum_altitude.getText().toString(),
							down_accum_altitude.getText().toString(), avg_hr.getText().toString(),
							max_hr.getText().toString(), steps.getText().toString(),
							avg_cadence.getText().toString(), max_cadence.getText().toString(),
							notes.getText().toString() };

					DBManager.Insert("routes", fields, values);
					DBManager.Close();

					sendBroadcast(new Intent("android.intent.action.REFRESH_ROUTES"));
					sendBroadcast(new Intent("android.intent.action.REFRESH_STATISTICS"));

					finish();
				}
			} else {
				String error_text1 = getString(R.string.data) + " ";
				String error_text2 = "";
				String error_text3 = " " + getString(R.string.incorrect_format);

				for (int h = 0; h < error_numbers.size(); h++) {
					switch (error_numbers.get(h)) {
					case 1:
						error_text2 = getString(R.string.distance_label).toLowerCase(Locale.getDefault());
						break;
					case 2:
						error_text2 = getString(R.string.max_speed_label_complete).toLowerCase(
								Locale.getDefault());
						break;
					case 3:
						error_text2 = getString(R.string.max_altitude_label_complete).toLowerCase(
								Locale.getDefault());
						break;
					case 4:
						error_text2 = getString(R.string.min_altitude_label_complete).toLowerCase(
								Locale.getDefault());
						break;
					case 5:
						error_text2 = getString(R.string.calories_label).toLowerCase(Locale.getDefault());
						break;
					case 6:
						error_text2 = getString(R.string.steps_label).toLowerCase(Locale.getDefault());
						break;
					case 7:
						error_text2 = getString(R.string.beats_avg_label);
						break;
					case 8:
						error_text2 = getString(R.string.max_beats_label);
						break;
					case 9:
						error_text2 = getString(R.string.hours);
						break;
					case 10:
						error_text2 = getString(R.string.minutes);
						break;
					case 11:
						error_text2 = getString(R.string.seconds);
						break;
					case 12:
						error_text2 = getString(R.string.up_accum_altitude_label_complete).toLowerCase(
								Locale.getDefault());
						break;
					case 13:
						error_text2 = getString(R.string.down_accum_altitude_label_complete).toLowerCase(
								Locale.getDefault());
						break;
					case 14:
						error_text2 = getString(R.string.avg_label_complete).toLowerCase(Locale.getDefault());
						break;
					case 15:
						error_text2 = getString(R.string.avg_cadence_label).toLowerCase(Locale.getDefault());
						break;
					case 16:
						error_text2 = getString(R.string.max_cadence_label);
						break;
					}

					UIFunctionUtils.showMessage(this, true, error_text1 + error_text2 + error_text3);
				}
			}

			break;
		case R.id.cancel:
			finish();

			break;
		}
	}
}
