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
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;

public class Summary2_hiit extends Fragment {
	private Context mContext;

	private SharedPreferences prefs;

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private ListView summary2ListView;

	private TextView summary2Label1;

	private int id, accum_intervals_seconds = 0, actions = 0;

	private boolean isMetric;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String[] route = DataFunctionUtils.getRouteData(mContext, id, isMetric);

		String[] hiit = DataFunctionUtils.getHiitData(mContext, Integer.parseInt(route[22]));

		ArrayList<int[]> hiit_intervals = DataFunctionUtils.getHiitIntervalsData(mContext,
				Integer.parseInt(route[22]));

		splitData(hiit, hiit_intervals);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout fragmentView = (LinearLayout) inflater.inflate(R.layout.summary2_hiit, container, false);

		summary2ListView = (ListView) fragmentView.findViewById(R.id.summary2_list_splitlaps);
		summary2ListView.setSelector(android.R.color.transparent);
		summary2Label1 = (TextView) fragmentView.findViewById(R.id.summary2_item_one);

		mContext = getActivity().getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		id = prefs.getInt("selected_practice", 0);

		isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		summary2Label1.setText((isMetric ? getString(R.string.long_unit1_detail_1).toUpperCase(
				Locale.getDefault()) : getString(R.string.long_unit2_detail_1).toUpperCase(
				Locale.getDefault()))
				+ ".");

		return fragmentView;
	}

	private void splitData(String[] hiit, ArrayList<int[]> hiit_intervals) {
		DBManager = new DataManager(mContext);
		DBManager.Open();

		if (hiit_intervals.size() > 0) {
			List<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();

			float last_distance = 0;
			float last_time = 0;
			boolean havePrepTime = false, haveCooldownTime = false;

			int loops = (hiit_intervals.size() * Integer.parseInt(hiit[1]));

			if (Integer.parseInt(hiit[2]) > 0) {
				havePrepTime = true;
				loops += 1;
			}

			if (Integer.parseInt(hiit[3]) > 0) {
				haveCooldownTime = true;
				loops += 1;
			}

			for (int i = 0; i < loops; i++) {
				if (i == 0) {
					if (havePrepTime) {
						cursor = DBManager.CustomQuery(getString(R.string.summary2_hiit_query_part1) + " "
								+ accum_intervals_seconds + " "
								+ getString(R.string.summary2_hiit_query_part2) + " "
								+ (accum_intervals_seconds + hiit_intervals.get(actions)[1]) + " "
								+ getString(R.string.summary2_hiit_query_part3),
								"SELECT * FROM locations WHERE _id = '" + id + "' AND time BETWEEN "
										+ accum_intervals_seconds + " AND "
										+ (accum_intervals_seconds + hiit[2]));
					} else {
						cursor = DBManager.CustomQuery(getString(R.string.summary2_hiit_query_part1) + " "
								+ accum_intervals_seconds + " "
								+ getString(R.string.summary2_hiit_query_part2) + " "
								+ +(accum_intervals_seconds + hiit_intervals.get(actions)[1]) + " "
								+ getString(R.string.summary2_hiit_query_part3),
								"SELECT * FROM locations WHERE _id = '" + id + "' AND time BETWEEN "
										+ accum_intervals_seconds + " AND "
										+ (accum_intervals_seconds + hiit_intervals.get(actions)[1]));
					}
				} else {
					if (haveCooldownTime && (i == (loops - 1))) {
						cursor = DBManager.CustomQuery(getString(R.string.summary2_hiit_query_part1) + " "
								+ accum_intervals_seconds + " "
								+ getString(R.string.summary2_hiit_query_part2) + " "
								+ (accum_intervals_seconds + Integer.parseInt(hiit[3])) + " "
								+ getString(R.string.summary2_hiit_query_part3),
								"SELECT * FROM locations WHERE _id = '" + id + "' AND time BETWEEN "
										+ accum_intervals_seconds + " AND "
										+ (accum_intervals_seconds + Integer.parseInt(hiit[3])));
					} else {
						cursor = DBManager.CustomQuery(getString(R.string.summary2_hiit_query_part1) + " "
								+ accum_intervals_seconds + " "
								+ getString(R.string.summary2_hiit_query_part2) + " "
								+ (accum_intervals_seconds + hiit_intervals.get(actions)[1]) + " "
								+ getString(R.string.summary2_hiit_query_part3),
								"SELECT * FROM locations WHERE _id = '" + id + "' AND time BETWEEN "
										+ accum_intervals_seconds + " AND "
										+ (accum_intervals_seconds + hiit_intervals.get(actions)[1]));
					}
				}

				float distance = 0;
				float time = 0;

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					if (cursor.isLast())
						distance = (isMetric ? distance + cursor.getFloat(cursor.getColumnIndex("distance"))
								: distance
										+ ((cursor.getFloat(cursor.getColumnIndex("distance")) * 1000f) / 1609f));

					cursor.moveToNext();
				}

				if (distance < last_distance)
					distance = last_distance;

				HashMap<String, String> hm = new HashMap<String, String>();
				if ((i == 0) && (Integer.parseInt(hiit[2]) > 0)) {
					hm.put("summary2_icon", String.valueOf(R.drawable.hiit_gray));
				} else {
					if ((Integer.parseInt(hiit[3]) > 0) && (i == (loops - 1))) {
						hm.put("summary2_icon", String.valueOf(R.drawable.hiit_blue));
					} else {
						switch (hiit_intervals.get(actions)[0]) {
						case 1:
							hm.put("summary2_icon", String.valueOf(R.drawable.hiit_green));
							break;
						case 2:
							hm.put("summary2_icon", String.valueOf(R.drawable.hiit_yellow));
							break;
						case 3:
							hm.put("summary2_icon", String.valueOf(R.drawable.hiit_orange));
							break;
						case 4:
							hm.put("summary2_icon", String.valueOf(R.drawable.hiit_red));
							break;
						}
					}
				}
				hm.put("summary2_distance",
						String.valueOf(FunctionUtils.customizedRound(((distance - last_distance)), 2)));

				if ((i == 0) && ((Integer.parseInt(hiit[2]) > 0))) {
					if (Integer.parseInt(hiit[2]) < 3600)
						hm.put("summary2_time", FunctionUtils.shortFormatTime(Long.parseLong(hiit[2])));
					else
						hm.put("summary2_time", FunctionUtils.longFormatTime(Long.parseLong(hiit[2])));
				} else {
					if ((Integer.parseInt(hiit[3]) > 0) && (i == (loops - 1))) {
						if (Integer.parseInt(hiit[0]) < 3600)
							hm.put("summary2_time", FunctionUtils.shortFormatTime(Long.parseLong(hiit[0])));
						else
							hm.put("summary2_time", FunctionUtils.longFormatTime(Long.parseLong(hiit[0])));
					} else {
						if ((accum_intervals_seconds + hiit_intervals.get(actions)[1]) < 3600)
							hm.put("summary2_time",
									FunctionUtils.shortFormatTime((accum_intervals_seconds + hiit_intervals
											.get(actions)[1])));
						else
							hm.put("summary2_time",
									FunctionUtils.longFormatTime((accum_intervals_seconds + hiit_intervals
											.get(actions)[1])));
					}
				}

				if ((i == 0) && ((Integer.parseInt(hiit[2]) > 0))) {
					time = (long) (Long.parseLong(hiit[2]));
				} else {
					if ((Integer.parseInt(hiit[3]) > 0) && (i == (loops - 1)))
						time = (long) (accum_intervals_seconds + Integer.parseInt(hiit[3]));
					else
						time = (long) (accum_intervals_seconds + hiit_intervals.get(actions)[1]);
				}

				hm.put("summary2_ritm", String.valueOf(FunctionUtils.calculateRitm(mContext,
						(long) (time - last_time), String.valueOf((isMetric ? distance - last_distance
								: (FunctionUtils.getKilometersFromMiles(distance) - FunctionUtils
										.getKilometersFromMiles(last_distance)))), isMetric, false)));
				mList.add(hm);

				if ((i == 0) && ((Integer.parseInt(hiit[2]) > 0)))
					accum_intervals_seconds = Integer.parseInt(hiit[2]);
				else
					accum_intervals_seconds = accum_intervals_seconds + hiit_intervals.get(actions)[1];

				last_distance = distance;
				last_time = time;

				if (((i == 0) && (!havePrepTime)) || (i > 0)) {
					if (actions < (hiit_intervals.size() - 1)) {
						actions += 1;
					} else {
						actions = 0;
					}
				}

			}

			String[] from = { "summary2_icon", "summary2_distance", "summary2_time", "summary2_ritm" };
			int[] to = { R.id.summary2_icon, R.id.summary2_long_unit, R.id.summary2_time, R.id.summary2_ritm };

			SimpleAdapter adapter = new SimpleAdapter(mContext, mList, R.layout.summary2_list_row, from, to);
			summary2ListView.setAdapter(adapter);

			cursor.close();
		}

		DBManager.Close();
	}
}