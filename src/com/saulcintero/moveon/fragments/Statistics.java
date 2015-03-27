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

package com.saulcintero.moveon.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.saulcintero.moveon.Constants;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.enums.DatesTypes;
import com.saulcintero.moveon.ui.widgets.SelectAgainSpinner;
import com.saulcintero.moveon.ui.widgets.myDatePickerDialog;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class Statistics extends Fragment {
	private LinearLayout fragmentView, base_layout, layout1, layout2, layout3, layout4;

	private ScrollView scrollView;

	private TextView start_label;

	private SelectAgainSpinner spinner1;
	private Spinner spinner2;

	private GraphicalView mChartView1, mChartView2, mChartView3;

	private float sum_distance, sum_avg_speed;

	private int sum_kcal, sum_time, sum_up_accum_altitude, sum_down_accum_altitude, sum_avg_hr, sum_steps,
			practices_counter, hr_practices_counter;

	private int year1, year2, month1, month2, day1, day2, show_dialog;

	private String[] activities;

	private float[] distance_distribution;

	private int[] kcal_distribution, time_distribution;

	private TableLayout mTableLayout;

	private DataManager DBManager;
	private Cursor cursor;

	private Context mContext;

	private Resources res;

	private int ALL_DATES = 0, THIS_YEAR = 1, THIS_MONTH = 2, THIS_WEAK = 3, BETWEEN_TWO_DATES = 4;

	private IntentFilter intentFilter;

	private TextView label1, label2, label3, label4, label5, label6, label7, label8, label9, label10,
			label11, label12, label13, label14;

	private TextView text1, text2, text3, text4, text5, text6, text7, text8, text9, text10, text11, text12,
			text13, text14;

	private String between_dates_query_part;

	private String customDay1, customDay2, customMonth1, customMonth2, customYear1, customYear2;

	private int selected_activity = 0, selected_date = 0;

	private boolean isRotated, spinnerItemSelected;

	private BroadcastReceiver mReceiverRefreshStatistics = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			paintData();
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public void onDestroy() {
		getActivity().unregisterReceiver(mReceiverRefreshStatistics);

		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putString("customDay1", customDay1);
		savedInstanceState.putString("customDay2", customDay2);
		savedInstanceState.putString("customMonth1", customMonth1);
		savedInstanceState.putString("customMonth2", customMonth2);
		savedInstanceState.putString("customYear1", customYear1);
		savedInstanceState.putString("customYear2", customYear2);
		savedInstanceState.putInt("show_dialog", show_dialog);
		savedInstanceState.putInt("day1", day1);
		savedInstanceState.putInt("month1", month1);
		savedInstanceState.putInt("year1", year1);
		savedInstanceState.putInt("day2", day2);
		savedInstanceState.putInt("month2", month2);
		savedInstanceState.putInt("year2", year2);
		savedInstanceState.putInt("selected_date", selected_date);
		savedInstanceState.putInt("selected_activity", selected_activity);
		savedInstanceState.putBoolean("isRotated", true);
		savedInstanceState.putBoolean("spinnerItemSelected", spinnerItemSelected);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = (LinearLayout) inflater.inflate(R.layout.statistics, container, false);

		mContext = getActivity().getApplicationContext();

		isRotated = false;

		if (savedInstanceState != null) {
			customDay1 = savedInstanceState.getString("customDay1");
			customDay2 = savedInstanceState.getString("customDay2");
			customMonth1 = savedInstanceState.getString("customMonth1");
			customMonth2 = savedInstanceState.getString("customMonth2");
			customYear1 = savedInstanceState.getString("customYear1");
			customYear2 = savedInstanceState.getString("customYear2");
			show_dialog = savedInstanceState.getInt("show_dialog");
			day1 = savedInstanceState.getInt("day1");
			month1 = savedInstanceState.getInt("month1");
			year1 = savedInstanceState.getInt("year1");
			day2 = savedInstanceState.getInt("day2");
			month2 = savedInstanceState.getInt("month2");
			year2 = savedInstanceState.getInt("year2");
			selected_date = savedInstanceState.getInt("selected_date");
			selected_activity = savedInstanceState.getInt("selected_activity");
			isRotated = savedInstanceState.getBoolean("isRotated");
			spinnerItemSelected = savedInstanceState.getBoolean("spinner1ItemSelected");
		} else {
			removeCustomDataValues();
			restart_dates();
			spinnerItemSelected = true;
		}

		base_layout = (LinearLayout) fragmentView.findViewById(R.id.linearLayout1);
		scrollView = (ScrollView) fragmentView.findViewById(R.id.scrollView1);
		layout1 = (LinearLayout) fragmentView.findViewById(R.id.graph1);
		layout2 = (LinearLayout) fragmentView.findViewById(R.id.graph2);
		layout3 = (LinearLayout) fragmentView.findViewById(R.id.graph3);
		layout4 = (LinearLayout) fragmentView.findViewById(R.id.linearLayout2_B);
		mTableLayout = (TableLayout) fragmentView.findViewById(R.id.dynamictable);
		start_label = (TextView) fragmentView.findViewById(R.id.statistics_start_item);
		spinner1 = (SelectAgainSpinner) fragmentView.findViewById(R.id.spinner1);
		spinner2 = (Spinner) fragmentView.findViewById(R.id.spinner2);
		label1 = (TextView) fragmentView.findViewById(R.id.label_one);
		label2 = (TextView) fragmentView.findViewById(R.id.label_two);
		label3 = (TextView) fragmentView.findViewById(R.id.label_three);
		label4 = (TextView) fragmentView.findViewById(R.id.label_four);
		label5 = (TextView) fragmentView.findViewById(R.id.label_five);
		label6 = (TextView) fragmentView.findViewById(R.id.label_six);
		label7 = (TextView) fragmentView.findViewById(R.id.label_seven);
		label8 = (TextView) fragmentView.findViewById(R.id.label_eight);
		label9 = (TextView) fragmentView.findViewById(R.id.label_nine);
		label10 = (TextView) fragmentView.findViewById(R.id.label_ten);
		label11 = (TextView) fragmentView.findViewById(R.id.label_eleven);
		label12 = (TextView) fragmentView.findViewById(R.id.label_twelve);
		label13 = (TextView) fragmentView.findViewById(R.id.label_thirteen);
		label14 = (TextView) fragmentView.findViewById(R.id.label_fourteen);
		text1 = (TextView) fragmentView.findViewById(R.id.text_one);
		text2 = (TextView) fragmentView.findViewById(R.id.text_two);
		text3 = (TextView) fragmentView.findViewById(R.id.text_three);
		text4 = (TextView) fragmentView.findViewById(R.id.text_four);
		text5 = (TextView) fragmentView.findViewById(R.id.text_five);
		text6 = (TextView) fragmentView.findViewById(R.id.text_six);
		text7 = (TextView) fragmentView.findViewById(R.id.text_seven);
		text8 = (TextView) fragmentView.findViewById(R.id.text_eight);
		text9 = (TextView) fragmentView.findViewById(R.id.text_nine);
		text10 = (TextView) fragmentView.findViewById(R.id.text_ten);
		text11 = (TextView) fragmentView.findViewById(R.id.text_eleven);
		text12 = (TextView) fragmentView.findViewById(R.id.text_twelve);
		text13 = (TextView) fragmentView.findViewById(R.id.text_thirteen);
		text14 = (TextView) fragmentView.findViewById(R.id.text_fourteen);

		label1.setText(getString(R.string.statistics_label1).toUpperCase(Locale.getDefault()));
		label2.setText(getString(R.string.statistics_label2).toUpperCase(Locale.getDefault()));
		label3.setText(getString(R.string.statistics_label3).toUpperCase(Locale.getDefault()));
		label4.setText(getString(R.string.statistics_label4).toUpperCase(Locale.getDefault()));
		label5.setText(getString(R.string.statistics_label5).toUpperCase(Locale.getDefault()));
		label6.setText(getString(R.string.statistics_label6).toUpperCase(Locale.getDefault()));
		label7.setText(getString(R.string.statistics_label7).toUpperCase(Locale.getDefault()));
		label8.setText(getString(R.string.statistics_label8).toUpperCase(Locale.getDefault()));
		label9.setText(getString(R.string.statistics_label9).toUpperCase(Locale.getDefault()));
		label10.setText(getString(R.string.statistics_label10).toUpperCase(Locale.getDefault()));
		label11.setText(getString(R.string.statistics_label11).toUpperCase(Locale.getDefault()));
		label12.setText(getString(R.string.statistics_label12).toUpperCase(Locale.getDefault()));
		label13.setText(getString(R.string.statistics_label13).toUpperCase(Locale.getDefault()));
		label14.setText(getString(R.string.statistics_label14).toUpperCase(Locale.getDefault()));

		res = mContext.getResources();
		activities = res.getStringArray(R.array.activities);

		String[] choiceTimeList = res.getStringArray(R.array.choice_time_list);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, choiceTimeList);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner1.setAdapter(spinnerArrayAdapter);

		String[] choiceActivityList = new String[activities.length + 1];
		choiceActivityList[0] = getString(R.string.activitylist_header_two);
		for (int d = 0; d < activities.length; d++) {
			choiceActivityList[d + 1] = activities[d];
		}

		ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item, choiceActivityList);
		spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(spinnerArrayAdapter2);

		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if ((selected_date != position && position < 4) || (position == 4 && spinnerItemSelected)) {
					selected_date = position;
					paintData();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		spinner1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					spinnerItemSelected = true;

				return false;
			}
		});

		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
				if (selected_activity != position) {
					selected_activity = position;
					paintData();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		spinner2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					spinnerItemSelected = true;

				return false;
			}
		});

		paintData();

		intentFilter = new IntentFilter("android.intent.action.REFRESH_STATISTICS");
		getActivity().registerReceiver(mReceiverRefreshStatistics, intentFilter);

		return fragmentView;
	}

	private void removeCustomDataValues() {
		customDay1 = "";
		customDay2 = "";
		customMonth1 = "";
		customMonth2 = "";
		customYear1 = "";
		customYear2 = "";
	}

	private void restart_dates() {
		final Calendar c = Calendar.getInstance();
		year1 = c.get(Calendar.YEAR);
		month1 = c.get(Calendar.MONTH) + 1;
		day1 = c.get(Calendar.DAY_OF_MONTH);

		year2 = year1;
		month2 = month1;
		day2 = day1;
	}

	private void launchDatePickerDialog() {
		myDatePickerDialog date = new myDatePickerDialog();

		Calendar calendar = Calendar.getInstance();
		Bundle args = new Bundle();
		args.putInt("year", calendar.get(Calendar.YEAR));
		args.putInt("month", calendar.get(Calendar.MONTH));
		args.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));

		switch (show_dialog) {
		case 0:
			date.setTitle(getString(R.string.start_date));
			break;
		case 1:
			date.setTitle(getString(R.string.end_date));
		}

		date.setArguments(args);
		date.setCallBack(ondate);
		date.show(getActivity().getSupportFragmentManager(), "Date Picker");
	}

	OnDateSetListener ondate = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			switch (show_dialog) {
			case 0:
				show_dialog += 1;

				year1 = year;
				month1 = monthOfYear + 1;
				day1 = dayOfMonth;

				launchDatePickerDialog();

				break;
			case 1:
				year2 = year;
				month2 = monthOfYear + 1;
				day2 = dayOfMonth;

				String pattern = "dd/MM/yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
				try {
					Date date1 = sdf.parse(day1 + "/" + month1 + "/" + year1);
					Date date2 = sdf.parse(day2 + "/" + month2 + "/" + year2);

					if (date2.compareTo(date1) > -1) {
						paintData(BETWEEN_TWO_DATES, selected_activity);
					} else {
						UIFunctionUtils.showMessage(mContext, true,
								getString(R.string.end_date_greater_than_start_date));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	private void paintData() {
		if (!DataFunctionUtils.checkInformationInDB(mContext))
			start_label.setVisibility(View.INVISIBLE);
		else
			start_label.setVisibility(View.VISIBLE);

		DatesTypes whichDate = DatesTypes.values()[selected_date];
		switch (whichDate) {
		case ALL_DATES:
			paintData(ALL_DATES, selected_activity);
			break;
		case THIS_YEAR:
			paintData(THIS_YEAR, selected_activity);
			break;
		case THIS_MONTH:
			paintData(THIS_MONTH, selected_activity);
			break;
		case THIS_WEAK:
			paintData(THIS_WEAK, selected_activity);
			break;
		case BETWEEN_TWO_DATES:
			show_dialog = 0;

			if (isRotated) {
				isRotated = false;

				if (customDay1.length() > 0 && customMonth1.length() > 0 && customYear1.length() > 0
						&& customDay2.length() > 0 && customMonth2.length() > 0 && customYear2.length() > 0) {
					paintData(BETWEEN_TWO_DATES, selected_activity);
				} else {
					restart_dates();
					launchDatePickerDialog();
				}

			} else {
				if (spinnerItemSelected) {
					spinnerItemSelected = false;
					restart_dates();
					launchDatePickerDialog();
				}
			}

			break;
		}
	}

	private void paintData(int sql_option, int activity) {
		boolean isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		sum_distance = 0;
		sum_kcal = 0;
		sum_time = 0;
		sum_up_accum_altitude = 0;
		sum_down_accum_altitude = 0;
		sum_avg_speed = 0;
		sum_steps = 0;
		sum_avg_hr = 0;
		practices_counter = 0;
		hr_practices_counter = 0;

		int[] colors = { Color.rgb(111, 183, 217), Color.rgb(54, 165, 54), Color.rgb(246, 103, 88),
				Color.rgb(234, 206, 74), Color.rgb(246, 164, 83), Color.LTGRAY, Color.rgb(35, 142, 36),
				Color.rgb(0, 129, 125), Color.rgb(0, 0, 220), Color.rgb(255, 255, 0), Color.rgb(255, 215, 0),
				Color.rgb(184, 134, 11), Color.rgb(245, 245, 220), Color.rgb(139, 137, 137),
				Color.rgb(96, 57, 138), Color.rgb(176, 0, 103), Color.rgb(77, 19, 106), Color.rgb(218, 0, 0),
				Color.rgb(252, 115, 0), Color.rgb(243, 42, 0), Color.rgb(255, 202, 44),
				Color.rgb(176, 214, 7), Color.rgb(255, 235, 44), Color.rgb(255, 255, 255),
				Color.rgb(186, 29, 29), Color.rgb(146, 436, 20), Color.rgb(245, 175, 209),
				Color.rgb(29, 91, 139), Color.rgb(128, 128, 0), Color.rgb(128, 0, 128),
				Color.rgb(0, 128, 128), Color.rgb(246, 233, 207), Color.rgb(231, 56, 142),
				Color.rgb(173, 141, 193), Color.rgb(191, 199, 32), Color.rgb(0, 128, 0),
				Color.rgb(4, 136, 125), Color.rgb(140, 0, 255), Color.rgb(135, 0, 118),
				Color.rgb(2, 132, 132), Color.rgb(0, 127, 204), Color.rgb(128, 250, 255),
				Color.rgb(192, 192, 192), Color.rgb(207, 94, 97), Color.rgb(137, 189, 199),
				Color.rgb(138, 168, 161), Color.rgb(171, 166, 191), Color.rgb(199, 153, 125) };

		DBManager = null;
		cursor = null;

		distance_distribution = null;
		kcal_distribution = null;
		time_distribution = null;

		DBManager = new DataManager(mContext);
		DBManager.Open();

		cursor = DBManager.CustomQuery(getString(R.string.checking_routes), "SELECT * FROM routes");

		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			between_dates_query_part = "";

			DatesTypes whichDate = DatesTypes.values()[sql_option];
			switch (whichDate) {
			case ALL_DATES:
				removeCustomDataValues();

				if (activity > 0) {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_all_routes) + " "
											+ getString(R.string.filter_by_activity) + " "
											+ getString(R.string.and) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE category_id = '" + activity + "' "
											+ "GROUP BY category_id");
				} else {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_all_routes) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes GROUP BY category_id");
				}

				break;
			case THIS_YEAR:
				removeCustomDataValues();

				if (activity > 0) {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_this_year_routes) + " "
											+ getString(R.string.filter_by_activity) + " "
											+ getString(R.string.and) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,7) = '"
											+ Calendar.getInstance().get(Calendar.YEAR) + "' "
											+ "AND category_id = '" + activity + "' "
											+ "GROUP BY category_id");
				} else {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_this_year_routes) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,7) = '"
											+ Calendar.getInstance().get(Calendar.YEAR) + "' "
											+ "GROUP BY category_id");
				}

				between_dates_query_part = "substr(date,7) = '" + Calendar.getInstance().get(Calendar.YEAR)
						+ "' ";

				break;
			case THIS_MONTH:
				removeCustomDataValues();

				int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
				String sMonth = String.valueOf(month);
				if (month < 10)
					sMonth = "0" + sMonth;

				if (activity > 0) {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_this_month_routes) + " "
											+ getString(R.string.filter_by_activity) + " "
											+ getString(R.string.and) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,4,2) = '" + sMonth + "' "
											+ "AND substr(date,7) = '"
											+ Calendar.getInstance().get(Calendar.YEAR) + "' "
											+ "AND category_id = '" + activity + "' "
											+ "GROUP BY category_id");
				} else {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_this_month_routes) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,4,2) = '" + sMonth + "' "
											+ "AND substr(date,7) = '"
											+ Calendar.getInstance().get(Calendar.YEAR) + "' "
											+ "GROUP BY category_id");
				}

				between_dates_query_part = "substr(date,4,2) = '" + sMonth + "' AND substr(date,7) = '"
						+ Calendar.getInstance().get(Calendar.YEAR) + "' ";

				break;
			case THIS_WEAK:
				removeCustomDataValues();

				Calendar c1 = Calendar.getInstance();
				c1.setFirstDayOfWeek(Calendar.MONDAY);
				c1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

				int y = c1.get(Calendar.YEAR);
				int m = c1.get(Calendar.MONTH) + 1;
				int d = c1.get(Calendar.DAY_OF_MONTH);

				String sYear1 = String.valueOf(y);
				String sMonth1 = String.valueOf(m);
				if (m < 10)
					sMonth1 = "0" + sMonth1;
				String sDay1 = String.valueOf(d);
				if (d < 10)
					sDay1 = "0" + sDay1;

				c1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

				int y2 = c1.get(Calendar.YEAR);
				int m2 = c1.get(Calendar.MONTH) + 1;
				int d2 = c1.get(Calendar.DAY_OF_MONTH);

				String sYear2 = String.valueOf(y2);
				String sMonth2 = String.valueOf(m2);
				if (m2 < 10)
					sMonth2 = "0" + sMonth2;
				String sDay2 = String.valueOf(d2);
				if (d2 < 10)
					sDay2 = "0" + sDay2;

				if (activity > 0) {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_this_weak_routes) + " "
											+ getString(R.string.filter_by_activity) + " "
											+ getString(R.string.and) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,7)||substr(date,4,2)||substr(date,1,2) "
											+ "BETWEEN '" + sYear1 + sMonth1 + sDay1 + "' AND '" + sYear2
											+ sMonth2 + sDay2 + "' " + "AND category_id = '" + activity
											+ "' " + "GROUP BY category_id");
				} else {
					cursor = DBManager
							.CustomQuery(
									getString(R.string.selecting_this_weak_routes) + " "
											+ getString(R.string.group_by_activities),
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,7)||substr(date,4,2)||substr(date,1,2) "
											+ "BETWEEN '" + sYear1 + sMonth1 + sDay1 + "' AND '" + sYear2
											+ sMonth2 + sDay2 + "' " + "GROUP BY category_id");
				}

				between_dates_query_part = "substr(date,7)||substr(date,4,2)||substr(date,1,2) "
						+ "BETWEEN '" + sYear1 + sMonth1 + sDay1 + "' AND '" + sYear2 + sMonth2 + sDay2
						+ "' ";

				break;
			case BETWEEN_TWO_DATES:
				String mYear1 = String.valueOf(year1);
				String mMonth1 = String.valueOf(month1);
				if (month1 < 10)
					mMonth1 = "0" + mMonth1;
				String mDay1 = String.valueOf(day1);
				if (day1 < 10)
					mDay1 = "0" + mDay1;

				String mYear2 = String.valueOf(year2);
				String mMonth2 = String.valueOf(month2);
				if (month2 < 10)
					mMonth2 = "0" + mMonth2;
				String mDay2 = String.valueOf(day2);
				if (day2 < 10)
					mDay2 = "0" + mDay2;

				customDay1 = mDay1;
				customDay2 = mDay2;
				customMonth1 = mMonth1;
				customMonth2 = mMonth2;
				customYear1 = mYear1;
				customYear2 = mYear2;

				if (activity > 0) {
					cursor = DBManager
							.CustomQuery(
									"Seleccionando las rutas de este mes agrupadas por actividad",
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,7)||substr(date,4,2)||substr(date,1,2) "
											+ "BETWEEN '" + customYear1 + customMonth1 + customDay1
											+ "' AND '" + customYear2 + customMonth2 + customDay2 + "' "
											+ "AND category_id = '" + activity + "' "
											+ "GROUP BY category_id");
				} else {
					cursor = DBManager
							.CustomQuery(
									"Seleccionando las rutas de este mes agrupadas por actividad",
									"SELECT category_id, COUNT(*) AS count_practices, SUM(distance) AS sum_distance, "
											+ "SUM(kcal) AS sum_kcal, SUM(time) AS sum_time, SUM(avg_speed) AS sum_avg_speed, "
											+ "SUM(up_accum_altitude) AS sum_up_accum_altitude, "
											+ "SUM(down_accum_altitude) AS sum_down_accum_altitude, "
											+ "SUM(steps) AS sum_steps " + "FROM routes "
											+ "WHERE substr(date,7)||substr(date,4,2)||substr(date,1,2) "
											+ "BETWEEN '" + customYear1 + customMonth1 + customDay1
											+ "' AND '" + customYear2 + customMonth2 + customDay2 + "' "
											+ "GROUP BY category_id");
				}

				between_dates_query_part = "substr(date,7)||substr(date,4,2)||substr(date,1,2) "
						+ "BETWEEN '" + customYear1 + customMonth1 + customDay1 + "' AND '" + customYear2
						+ customMonth2 + customDay2 + "' ";

				break;
			}
			cursor.moveToFirst();

			base_layout.setVisibility(View.VISIBLE);
			scrollView.setVisibility(View.VISIBLE);
			layout1.setVisibility(View.VISIBLE);
			layout2.setVisibility(View.VISIBLE);
			layout3.setVisibility(View.VISIBLE);
			layout4.setVisibility(View.VISIBLE);

			distance_distribution = new float[cursor.getCount()];
			kcal_distribution = new int[cursor.getCount()];
			time_distribution = new int[cursor.getCount()];

			int i = 0;

			mTableLayout.removeAllViews();

			while (!cursor.isAfterLast()) {
				TextView color = new TextView(mContext);
				TextView label = new TextView(mContext);
				TextView value = new TextView(mContext);
				LinearLayout.LayoutParams colorLayoutParams = new LinearLayout.LayoutParams(new LayoutParams(
						FunctionUtils.calculateDpFromPx(mContext, 20), FunctionUtils.calculateDpFromPx(
								mContext, 20)));
				colorLayoutParams.setMargins(0, 1, 5, 1);
				color.setLayoutParams(colorLayoutParams);
				label.setLayoutParams(new LayoutParams(FunctionUtils.calculateDpFromPx(mContext, 95),
						LayoutParams.WRAP_CONTENT));
				label.setTypeface(null, Typeface.BOLD);
				label.setTextColor(Color.parseColor("#b5b5b5"));
				value.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				value.setTextColor(res.getColor(R.color.white));

				color.setBackgroundColor(colors[i]);

				label.setText(activities[cursor.getInt(cursor.getColumnIndex("category_id")) - 1] + ":");
				value.setText((isMetric ? String.valueOf(cursor.getFloat(cursor
						.getColumnIndex("sum_distance")))
						+ " "
						+ getString(R.string.long_unit1_detail_1)
						+ ", " : String.valueOf(FunctionUtils.customizedRound(
						((cursor.getFloat(cursor.getColumnIndex("sum_distance")) * 1000f) / 1609f), 2))
						+ " " + getString(R.string.long_unit2_detail_1) + ", ")
						+ String.valueOf((int) cursor.getFloat(cursor.getColumnIndex("sum_kcal")))
						+ " "
						+ getString(R.string.tell_calories_setting_details)
						+ ", "
						+ String.valueOf(FunctionUtils.statisticsFormatTime(mContext,
								(long) cursor.getFloat(cursor.getColumnIndex("sum_time")))));

				LinearLayout mLinearLayout = new LinearLayout(mContext);
				mLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
				mLinearLayout.setOrientation(0);
				mLinearLayout.addView(color);
				mLinearLayout.addView(label);
				mLinearLayout.addView(value);
				mTableLayout.addView(mLinearLayout);

				sum_distance = sum_distance + cursor.getFloat(cursor.getColumnIndex("sum_distance"));
				sum_kcal = sum_kcal + cursor.getInt(cursor.getColumnIndex("sum_kcal"));
				sum_time = sum_time + cursor.getInt(cursor.getColumnIndex("sum_time"));
				sum_up_accum_altitude = sum_up_accum_altitude
						+ cursor.getInt(cursor.getColumnIndex("sum_up_accum_altitude"));
				sum_down_accum_altitude = sum_down_accum_altitude
						+ cursor.getInt(cursor.getColumnIndex("sum_down_accum_altitude"));
				sum_avg_speed = sum_avg_speed + cursor.getFloat(cursor.getColumnIndex("sum_avg_speed"));
				sum_steps = sum_steps + cursor.getInt(cursor.getColumnIndex("sum_steps"));

				distance_distribution[i] = cursor.getFloat(cursor.getColumnIndex("sum_distance"));
				kcal_distribution[i] = cursor.getInt(cursor.getColumnIndex("sum_kcal"));
				time_distribution[i] = cursor.getInt(cursor.getColumnIndex("sum_time"));

				practices_counter = practices_counter
						+ (int) cursor.getFloat(cursor.getColumnIndex("count_practices"));

				i++;

				cursor.moveToNext();
			}

			String activity_query_part = "";
			if (activity > 0)
				activity_query_part = " AND category_id = '" + activity + "'";

			if (between_dates_query_part.length() > 0) {
				cursor = DBManager.CustomQuery(getString(R.string.routes_with_hr),
						"SELECT avg_hr FROM routes WHERE avg_hr > 0 AND " + between_dates_query_part
								+ activity_query_part);
			} else {
				cursor = DBManager.CustomQuery(getString(R.string.routes_with_hr),
						"SELECT avg_hr FROM routes WHERE avg_hr > 0" + activity_query_part);
			}
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				while (!cursor.isAfterLast()) {
					sum_avg_hr = sum_avg_hr + cursor.getInt(cursor.getColumnIndex("avg_hr"));
					hr_practices_counter += 1;

					cursor.moveToNext();
				}
			}

			text8.setText("");
			if (between_dates_query_part.length() > 0) {
				if (activity > 0)
					activity_query_part = " AND category_id = '" + activity + "' ";

				cursor = DBManager.CustomQuery(getString(R.string.selecting_most_used_shoes),
						"SELECT shoe_id, COUNT(shoe_id) AS count_shoes " + "FROM routes " + "WHERE "
								+ between_dates_query_part + activity_query_part + "GROUP BY shoe_id "
								+ "ORDER BY count_shoes DESC");
			} else {
				if (activity > 0)
					activity_query_part = "WHERE category_id = '" + activity + "' ";

				cursor = DBManager.CustomQuery(getString(R.string.selecting_most_used_shoes_in_data_range),
						"SELECT shoe_id, COUNT(shoe_id) AS count_shoes " + "FROM routes "
								+ activity_query_part + "GROUP BY shoe_id " + "ORDER BY count_shoes DESC");
			}
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				int shoe = cursor.getInt(cursor.getColumnIndex("shoe_id"));
				if (cursor.getCount() > 1) {
					int[] shoes = new int[cursor.getCount()];
					int m = 0;
					while (!cursor.isAfterLast()) {
						shoes[m] = cursor.getInt(cursor.getColumnIndex("shoe_id"));
						m++;
						cursor.moveToNext();
					}

					if (shoe == 0 && shoes.length > 1)
						shoe = shoes[1];
				}

				if (shoe > 0) {
					cursor = DBManager.CustomQuery(getString(R.string.shoe_name),
							"SELECT name FROM shoes WHERE _id = '" + shoe + "'");
					cursor.moveToFirst();
					text8.setText(cursor.getString(cursor.getColumnIndex("name")));
				}

			}

			text1.setText(String.valueOf(practices_counter));
			text2.setText(String.valueOf(FunctionUtils.statisticsFormatTime(mContext, (long) sum_time)));
			text3.setText(isMetric ? String.valueOf(FunctionUtils.customizedRound(sum_distance, 2)) + " "
					+ getString(R.string.long_unit1_detail_1) : String.valueOf(FunctionUtils.customizedRound(
					((sum_distance * 1000f) / 1609f), 2)) + " " + getString(R.string.long_unit2_detail_1));
			text4.setText(isMetric ? String.valueOf(sum_up_accum_altitude) + " "
					+ getString(R.string.long_unit1_detail_4) : String
					.valueOf((int) (sum_up_accum_altitude * 1.0936f))
					+ " "
					+ getString(R.string.long_unit2_detail_4));
			text5.setText(isMetric ? String.valueOf(sum_down_accum_altitude) + " "
					+ getString(R.string.long_unit1_detail_4) : String
					.valueOf((int) (sum_down_accum_altitude * 1.0936f))
					+ " "
					+ getString(R.string.long_unit2_detail_4));
			if ((sum_avg_speed > 0) && (practices_counter > 0)) {
				text6.setText((isMetric ? String.valueOf(FunctionUtils.customizedRound(
						(sum_avg_speed / practices_counter), 2))
						+ " "
						+ getString(R.string.long_unit1_detail_2) : String.valueOf(FunctionUtils
						.customizedRound((((sum_avg_speed * 1000f) / 1609f) / practices_counter), 2))
						+ " "
						+ getString(R.string.long_unit2_detail_2)));
			} else {
				text6.setText(getString(R.string.zero_value)
						+ " "
						+ (isMetric ? getString(R.string.long_unit1_detail_2) : mContext
								.getString(R.string.long_unit2_detail_2)));
			}
			text7.setText(String.valueOf(FunctionUtils.calculateRitm(mContext, sum_time,
					String.valueOf(sum_distance), isMetric, false))
					+ " "
					+ (isMetric ? getString(R.string.long_unit1_detail_3) : mContext
							.getString(R.string.long_unit2_detail_3)));
			text9.setText(String.valueOf(sum_kcal) + " " + getString(R.string.tell_calories_setting_details));
			text10.setText(String.valueOf(sum_steps));
			if ((sum_avg_hr > 0) && (hr_practices_counter > 0)) {
				text11.setText(String.valueOf(sum_avg_hr / hr_practices_counter) + " "
						+ getString(R.string.beats_per_minute));
			} else {
				text11.setText(getString(R.string.zero_value) + " " + getString(R.string.beats_per_minute));
			}
			if (sum_kcal > 0) {
				text12.setText(String.valueOf(sum_kcal / Constants.CHEESE_BURGER));
			} else {
				text12.setText(getString(R.string.zero_value));
			}
			if (sum_distance > 0) {
				text13.setText(String.valueOf(FunctionUtils.customizedRound(
						(sum_distance / Constants.ALL_THE_WAY_AROUND_THE_WORLD), 3)));
			} else {
				text13.setText(getString(R.string.zero_with_three_decimal_places_value));
			}
			if (sum_distance > 0) {
				double moon_distance = ((double) sum_distance) / ((double) Constants.TO_THE_MOON);
				text14.setText(String.valueOf(FunctionUtils.customizedRound(
						Float.parseFloat(String.valueOf(moon_distance)), 1)));
			} else {
				text14.setText(getString(R.string.zero_with_one_decimal_place_value));
			}

			layout1.removeAllViews();
			layout2.removeAllViews();
			layout3.removeAllViews();

			mChartView1 = null;
			mChartView2 = null;
			mChartView3 = null;

			for (int h = 0; h < distance_distribution.length; h++) {
				float percent = 0;

				if (distance_distribution[h] > 0)
					percent = (distance_distribution[h] * 100) / sum_distance;

				if (sum_distance == 0)
					percent = 100 / distance_distribution.length;

				distance_distribution[h] = percent;
			}

			for (int b = 0; b < kcal_distribution.length; b++) {
				int percent = 0;

				if (sum_kcal > 0)
					percent = (kcal_distribution[b] * 100) / sum_kcal;

				if (sum_kcal == 0)
					percent = 100 / kcal_distribution.length;

				kcal_distribution[b] = percent;
			}

			final CategorySeries distance_distributionSeries = new CategorySeries("");
			for (int g = 0; g < distance_distribution.length; g++) {
				if (distance_distribution.length == 1) {
					distance_distributionSeries.add("", 100);
				} else {
					distance_distributionSeries.add("", distance_distribution[g]);
				}
			}

			final CategorySeries kcal_distributionSeries = new CategorySeries("");
			for (int p = 0; p < kcal_distribution.length; p++) {
				if (kcal_distribution.length == 1) {
					kcal_distributionSeries.add("", 100);
				} else {
					kcal_distributionSeries.add("", kcal_distribution[p]);
				}
			}

			final CategorySeries time_distributionSeries = new CategorySeries("");
			for (int l = 0; l < time_distribution.length; l++) {
				if (time_distribution.length == 1) {
					time_distributionSeries.add("", 100);
				} else {
					time_distributionSeries.add("", time_distribution[l]);
				}
			}

			DefaultRenderer defaultRenderer = new DefaultRenderer();
			DefaultRenderer defaultRenderer2 = new DefaultRenderer();
			DefaultRenderer defaultRenderer3 = new DefaultRenderer();

			defaultRenderer.setShowLabels(false);
			defaultRenderer.setZoomButtonsVisible(false);
			defaultRenderer.setStartAngle(180);
			defaultRenderer.setDisplayValues(false);
			defaultRenderer.setClickEnabled(true);
			defaultRenderer.setInScroll(true);
			defaultRenderer.setShowLegend(false);

			defaultRenderer2.setShowLabels(false);
			defaultRenderer2.setZoomButtonsVisible(false);
			defaultRenderer2.setStartAngle(180);
			defaultRenderer2.setDisplayValues(false);
			defaultRenderer2.setClickEnabled(true);
			defaultRenderer2.setInScroll(true);
			defaultRenderer2.setShowLegend(false);

			defaultRenderer3.setShowLabels(false);
			defaultRenderer3.setZoomButtonsVisible(false);
			defaultRenderer3.setStartAngle(180);
			defaultRenderer3.setDisplayValues(false);
			defaultRenderer3.setClickEnabled(true);
			defaultRenderer3.setInScroll(true);
			defaultRenderer3.setShowLegend(false);

			for (int u = 0; u < distance_distribution.length; u++) {
				SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
				seriesRenderer.setColor(colors[u]);
				seriesRenderer.setDisplayChartValues(true);
				seriesRenderer.setHighlighted(false);

				defaultRenderer.addSeriesRenderer(seriesRenderer);
			}

			for (int p = 0; p < kcal_distribution.length; p++) {
				SimpleSeriesRenderer seriesRenderer2 = new SimpleSeriesRenderer();
				seriesRenderer2.setColor(colors[p]);
				seriesRenderer2.setDisplayChartValues(true);
				seriesRenderer2.setHighlighted(false);

				defaultRenderer2.addSeriesRenderer(seriesRenderer2);
			}

			for (int o = 0; o < distance_distribution.length; o++) {
				SimpleSeriesRenderer seriesRenderer3 = new SimpleSeriesRenderer();
				seriesRenderer3.setColor(colors[o]);
				seriesRenderer3.setDisplayChartValues(true);
				seriesRenderer3.setHighlighted(false);

				defaultRenderer3.addSeriesRenderer(seriesRenderer3);
			}

			mChartView1 = ChartFactory
					.getPieChartView(mContext, distance_distributionSeries, defaultRenderer);
			mChartView2 = ChartFactory.getPieChartView(mContext, kcal_distributionSeries, defaultRenderer2);
			mChartView3 = ChartFactory.getPieChartView(mContext, time_distributionSeries, defaultRenderer3);

			layout1.addView(mChartView1);
			layout2.addView(mChartView2);
			layout3.addView(mChartView3);
		} else {
			base_layout.setVisibility(View.INVISIBLE);
			scrollView.setVisibility(View.INVISIBLE);
			layout1.setVisibility(View.INVISIBLE);
			layout2.setVisibility(View.INVISIBLE);
			layout3.setVisibility(View.INVISIBLE);
			layout4.setVisibility(View.INVISIBLE);
		}
		cursor.close();
		DBManager.Close();
	}
}