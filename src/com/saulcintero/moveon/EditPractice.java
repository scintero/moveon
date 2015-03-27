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
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class EditPractice extends Activity implements OnClickListener {
	private Context mContext;

	private SharedPreferences prefs;

	private Button save_btn, discard_btn;

	private Resources res;

	private EditText name, notes, hours, minutes, seconds, distance, avg_speed, max_speed, max_altitude,
			min_altitude, up_accum_altitude, down_accum_altitude, kcal, steps, avg_hr, max_hr;

	private Spinner spinner1, spinner2;

	private DataManager DBManager = null;

	private int shoe_id, activity_id;

	private boolean isMetric;

	private ArrayList<String> activities_name = new ArrayList<String>();
	private ArrayList<String> shoes_name = new ArrayList<String>();
	private ArrayList<Integer> shoes_id = new ArrayList<Integer>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);
		setContentView(R.layout.edit_practice);

		super.onCreate(savedInstanceState);

		mContext = getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		save_btn = (Button) findViewById(R.id.save);
		discard_btn = (Button) findViewById(R.id.cancel);
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
		hours = (EditText) findViewById(R.id.practice_time_hours);
		minutes = (EditText) findViewById(R.id.practice_time_minutes);
		seconds = (EditText) findViewById(R.id.practice_time_seconds);
		spinner1 = (Spinner) findViewById(R.id.spinner1);
		spinner2 = (Spinner) findViewById(R.id.spinner2);

		isMetric = FunctionUtils.checkIfUnitsAreMetric(this);

		String[] data = DataFunctionUtils.getRouteData(mContext, prefs.getInt("selected_practice", 0),
				isMetric);

		res = mContext.getResources();
		final String[] activities = res.getStringArray(R.array.activities);

		shoe_id = Integer.parseInt(data[15]);
		activity_id = Integer.parseInt(data[16]);

		name.setText(data[1]);
		notes.setText(data[11]);
		distance.setText(data[3]);
		avg_speed.setText(data[4]);
		max_speed.setText(data[6]);
		max_altitude.setText(data[7]);
		min_altitude.setText(data[8]);
		up_accum_altitude.setText(data[20]);
		down_accum_altitude.setText(data[21]);
		kcal.setText(data[9]);
		steps.setText(data[10]);
		avg_hr.setText(data[13]);
		max_hr.setText(data[14]);
		hours.setText(String.valueOf((Long.parseLong(data[17]) / 60) / 60));
		minutes.setText(String.valueOf((Long.parseLong(data[17]) / 60) % 60));
		seconds.setText(String.valueOf(Long.parseLong(data[17]) % 60));

		activities_name.add(activities[Integer.parseInt(data[16]) - 1]);

		for (int b = 0; b < activities.length; b++) {
			if (b != (Integer.parseInt(data[16]) - 1)) {
				activities_name.add(activities[b]);
			}
		}

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, activities_name);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(spinnerArrayAdapter);

		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				activity_id = (FunctionUtils.getIdForThisItem(String.valueOf(activities_name.get(i)),
						activities) + 1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.CustomQuery(getString(R.string.db_query_getting_shoes),
				"SELECT * FROM shoes WHERE active=1 AND _id NOT IN ('" + data[15] + "') "
						+ "ORDER BY default_shoe DESC");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			if (Integer.parseInt(data[15]) != 0) {
				shoes_name.add(data[12]);
				shoes_id.add(Integer.parseInt(data[15]));
			} else {
				shoes_name.add(getString(R.string.none_text));
				shoes_id.add(0);
			}

			for (int g = 0; g <= cursor.getCount() - 1; g++) {
				shoes_name.add(cursor.getString(cursor.getColumnIndex("name")));
				shoes_id.add(cursor.getInt(cursor.getColumnIndex("_id")));

				cursor.moveToNext();
			}

			if (Integer.parseInt(data[15]) != 0) {
				shoes_name.add(getString(R.string.none_text));
				shoes_id.add(0);
			}
		} else {
			if (Integer.parseInt(data[15]) != 0) {
				shoes_name.add(data[12]);
				shoes_id.add(Integer.parseInt(data[15]));
			}
			shoes_name.add(getString(R.string.none_text));
			shoes_id.add(0);
		}
		cursor.close();
		DBManager.Close();

		ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, shoes_name);
		spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(spinnerArrayAdapter2);

		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				shoe_id = shoes_id.get(i);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		save_btn.setOnClickListener(this);
		discard_btn.setOnClickListener(this);
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
			if (!FunctionUtils.isNumeric(hours.getText().toString()))
				error_numbers.add(9);
			if (!FunctionUtils.isNumeric(minutes.getText().toString()))
				error_numbers.add(10);
			if (!FunctionUtils.isNumeric(seconds.getText().toString()))
				error_numbers.add(11);
			if (!FunctionUtils.isNumeric(avg_speed.getText().toString()))
				error_numbers.add(12);
			if (!FunctionUtils.isNumeric(down_accum_altitude.getText().toString()))
				error_numbers.add(13);
			if (!FunctionUtils.isNumeric(down_accum_altitude.getText().toString()))
				error_numbers.add(14);

			if (error_numbers.size() == 0) {
				int time_conversion = 0;
				boolean update_query = true;

				int mHours = Integer.parseInt(hours.getText().toString());
				if (mHours > 0)
					time_conversion = mHours * 3600;

				int mMinutes = Integer.parseInt(minutes.getText().toString());
				if ((mMinutes > 0) && (mMinutes < 60)) {
					time_conversion = time_conversion + mMinutes * 60;
				} else {
					if (mMinutes != 0) {
						update_query = false;

						UIFunctionUtils.showMessage(this, false, getString(R.string.minutes_value_error));
					}
				}

				int mSeconds = Integer.parseInt(seconds.getText().toString());
				if ((mSeconds > 0) && (mSeconds < 60)) {
					time_conversion = time_conversion + mSeconds;
				} else {
					if (mSeconds != 0) {
						update_query = false;

						UIFunctionUtils.showMessage(this, false, getString(R.string.seconds_value_error));
					}
				}

				if (update_query) {
					DBManager = new DataManager(mContext);
					DBManager.Open();

					String[] fields = { "category_id", "shoe_id", "name", "time", "distance", "max_speed",
							"kcal", "max_altitude", "min_altitude", "avg_hr", "max_hr", "steps", "comments",
							"avg_speed", "up_accum_altitude", "down_accum_altitude" };

					for (int p = 0; p <= (fields.length - 1); p++) {
						switch (p) {
						case 0:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p],
									String.valueOf(activity_id), "routes");
							break;
						case 1:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p],
									String.valueOf(shoe_id), "routes");
							break;
						case 2:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p], name.getText()
									.toString(), "routes");
							break;
						case 3:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p],
									String.valueOf(time_conversion), "routes");
							break;
						case 4:
							DBManager.Edit(
									prefs.getInt("selected_practice", 0),
									fields[p],
									(isMetric ? distance.getText().toString() : String.valueOf(FunctionUtils
											.getKilometersFromMiles(Float.parseFloat(distance.getText()
													.toString())))), "routes");
							break;
						case 5:
							DBManager.Edit(
									prefs.getInt("selected_practice", 0),
									fields[p],
									(isMetric ? max_speed.getText().toString() : String.valueOf(FunctionUtils
											.getKilometersFromMiles(Float.parseFloat(max_speed.getText()
													.toString())))), "routes");
							break;
						case 6:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p], kcal.getText()
									.toString(), "routes");
							break;
						case 7:
							DBManager.Edit(
									prefs.getInt("selected_practice", 0),
									fields[p],
									(isMetric ? max_altitude.getText().toString() : String
											.valueOf(FunctionUtils.getMetersFromYards(Float
													.parseFloat(max_altitude.getText().toString())))),
									"routes");
							break;
						case 8:
							DBManager.Edit(
									prefs.getInt("selected_practice", 0),
									fields[p],
									(isMetric ? min_altitude.getText().toString() : String
											.valueOf(FunctionUtils.getMetersFromYards(Float
													.parseFloat(min_altitude.getText().toString())))),
									"routes");
							break;
						case 9:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p], avg_hr.getText()
									.toString(), "routes");
							break;
						case 10:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p], max_hr.getText()
									.toString(), "routes");
							break;
						case 11:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p], steps.getText()
									.toString(), "routes");
							break;
						case 12:
							DBManager.Edit(prefs.getInt("selected_practice", 0), fields[p], notes.getText()
									.toString(), "routes");
							break;
						case 13:
							DBManager.Edit(
									prefs.getInt("selected_practice", 0),
									fields[p],
									(isMetric ? avg_speed.getText().toString() : String.valueOf(FunctionUtils
											.getKilometersFromMiles(Float.parseFloat(avg_speed.getText()
													.toString())))), "routes");
							break;
						case 14:
							DBManager.Edit(
									prefs.getInt("selected_practice", 0),
									fields[p],
									(isMetric ? up_accum_altitude.getText().toString() : String
											.valueOf(FunctionUtils.getMetersFromYards(Float
													.parseFloat(up_accum_altitude.getText().toString())))),
									"routes");
							break;
						case 15:
							DBManager.Edit(
									prefs.getInt("selected_practice", 0),
									fields[p],
									(isMetric ? down_accum_altitude.getText().toString() : String
											.valueOf(FunctionUtils.getMetersFromYards(Float
													.parseFloat(down_accum_altitude.getText().toString())))),
									"routes");
							break;
						}
					}

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
						error_text2 = getString(R.string.avg_label_complete);
						break;
					case 13:
						error_text2 = getString(R.string.up_accum_altitude_label_complete).toLowerCase(
								Locale.getDefault());
						break;
					case 14:
						error_text2 = getString(R.string.down_accum_altitude_label_complete).toLowerCase(
								Locale.getDefault());
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