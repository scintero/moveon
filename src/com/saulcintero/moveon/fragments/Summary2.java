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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.FunctionUtils;

public class Summary2 extends Fragment {
	private Context mContext;

	private SharedPreferences prefs;

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private SeekBar seekBar;

	private ArrayList<DataByUnits> dataByUnits = new ArrayList<DataByUnits>();

	private ListView summary2ListView;

	private TextView label1, label2;

	private String[] options, options2, options3;
	private int[] icons;

	private long highRitm = 0, slowRitm = 0;

	private float distance = 0;

	private static int progress = 1;

	private int laps;

	private int slowLap = 0, fastLap = 0, oldTime = 0, time = 0, unit, sumDistance, id;

	private boolean isMetric;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		LinearLayout fragmentView = (LinearLayout) inflater.inflate(R.layout.summary2, container, false);
		summary2ListView = (ListView) fragmentView.findViewById(R.id.summary2_list_splitlaps);
		summary2ListView.setSelector(android.R.color.transparent);
		label1 = (TextView) fragmentView.findViewById(R.id.summary2_item_one);
		label2 = (TextView) fragmentView.findViewById(R.id.summary2_label_one);

		isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		unit = (isMetric ? 1000 : 1609);

		label1.setText((isMetric ? getString(R.string.long_unit1_detail_1).toUpperCase(Locale.getDefault())
				: getString(R.string.long_unit2_detail_1).toUpperCase(Locale.getDefault())) + ".");
		label2.setText("1 "
				+ (isMetric ? getString(R.string.long_unit1_detail_1).toUpperCase(Locale.getDefault())
						: getString(R.string.long_unit2_detail_1).toUpperCase(Locale.getDefault())) + ".");

		seekBar = (SeekBar) fragmentView.findViewById(R.id.summary2_slider);
		seekBar.setMax(49);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				splitData(progress * unit);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progr, boolean fromUser) {
				progress = progr + 1;

				label2.setText(String.valueOf(progress)
						+ " "
						+ (isMetric ? getString(R.string.long_unit1_detail_1)
								.toUpperCase(Locale.getDefault()) : getString(R.string.long_unit2_detail_1)
								.toUpperCase(Locale.getDefault())) + ".");
			}
		});

		return fragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		id = prefs.getInt("selected_practice", 0);

		splitData(progress * unit);

		super.onActivityCreated(savedInstanceState);
	}

	private void splitData(int nextDistance) {
		dataByUnits.clear();
		oldTime = 0;
		sumDistance = nextDistance;

		DBManager = new DataManager(mContext);
		DBManager.Open();
		cursor = DBManager.getRowsFromTable(String.valueOf(id), "locations");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				distance = cursor.getFloat(cursor.getColumnIndex("distance"));
				time = cursor.getInt(cursor.getColumnIndex("time"));

				if ((distance * 1000) >= (nextDistance + 10)) { // not shown 3
																// decimals, 10
																// units are
																// added to
																// filling the
																// hundred
					DataByUnits d = new DataByUnits();
					d.time = time - oldTime;
					oldTime = time;
					d.distance = sumDistance;

					dataByUnits.add(d);

					nextDistance += sumDistance;
				}
				cursor.moveToNext();
			}

			if ((distance > 0) && ((distance - dataByUnits.size()) > 0)) {
				DataByUnits d = new DataByUnits();
				d.time = time - oldTime;
				d.distance = (distance + ((((distance * sumDistance) - (dataByUnits.size() * sumDistance)) / sumDistance) / sumDistance));

				dataByUnits.add(d);

				writeValues();
			}
		}
		closeCursorAndDB();

		List<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i <= (dataByUnits.size() - 1); i++) {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("summary2_long_unit", options[i]);
			hm.put("summary2_time", options2[i]);
			hm.put("summary2_ritm", options3[i]);
			hm.put("summary2_icon", Integer.toString(icons[i]));
			mList.add(hm);
		}

		String[] from = { "summary2_icon", "summary2_long_unit", "summary2_time", "summary2_ritm" };
		int[] to = { R.id.summary2_icon, R.id.summary2_long_unit, R.id.summary2_time, R.id.summary2_ritm };

		SimpleAdapter adapter = new SimpleAdapter(mContext, mList, R.layout.summary2_list_row, from, to);
		summary2ListView.setAdapter(adapter);
	}

	private void closeCursorAndDB() {
		cursor.close();
		DBManager.Close();
	}

	private void writeValues() {
		long ritm = 0;
		int currentUnit = 0, accumTime = 0;

		options = new String[dataByUnits.size()];
		options2 = new String[dataByUnits.size()];
		options3 = new String[dataByUnits.size()];

		while (currentUnit < dataByUnits.size()) {
			DataByUnits d = (DataByUnits) dataByUnits.get(currentUnit);

			if (d.distance > 0) {
				if (currentUnit == (dataByUnits.size() - 1)) {
					ritm = (isMetric ? (long) (d.time / (d.distance - ((sumDistance / unit) * currentUnit)))
							: (long) (((sumDistance / unit) * d.time) / ((d.distance * 0.6213f) - ((sumDistance / unit) * currentUnit))));
				} else {
					ritm = (long) ((d.time * sumDistance) / d.distance);
				}

				if ((currentUnit == 0) && (d.distance > 0)) {
					highRitm = ritm;
					slowRitm = ritm;
					slowLap = 0;
					fastLap = 0;
				}

				if (ritm > highRitm) {
					slowLap = currentUnit;
					highRitm = ritm;
				}

				if (ritm <= slowRitm) {
					fastLap = currentUnit;
					slowRitm = ritm;
				}

				accumTime = accumTime + d.time;

				if (currentUnit > 0)
					laps = ((currentUnit + 1) * (sumDistance / unit));
				else
					laps = (sumDistance / unit);

				createArrayOfOptions(currentUnit, laps, d.time, (isMetric ? d.distance
						: (d.distance * 0.6213f)), accumTime, ritm);
			}
			currentUnit++;
		}
		createArrayOfIcons();
	}

	private void createArrayOfOptions(int currentDistanceUnit, int lap, int time, float distance,
			int accumTime, long ritm) {
		if ((currentDistanceUnit + 1) < dataByUnits.size())
			options[currentDistanceUnit] = String.valueOf(lap);
		else
			options[currentDistanceUnit] = String.valueOf(FunctionUtils.customizedRound(distance, 2));

		options2[currentDistanceUnit] = String.valueOf(FunctionUtils.shortFormatTime(accumTime));

		if (ritm < 3600)
			options3[currentDistanceUnit] = FunctionUtils.shortFormatTime(ritm);
		else
			options3[currentDistanceUnit] = FunctionUtils.longFormatTime(ritm);
	}

	private int[] createArrayOfIcons() {
		icons = new int[dataByUnits.size()];

		for (int h = 0; (h <= dataByUnits.size() - 1); h++) {
			if (h == (int) fastLap)
				icons[h] = R.drawable.fast;
			else if (h == (int) slowLap)
				icons[h] = R.drawable.slow;
			else
				icons[h] = R.drawable.empty;
		}
		return icons;
	}

	private class DataByUnits {
		int time;
		float distance;
	}
}