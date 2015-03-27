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

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.saulcintero.moveon.adapters.SeparatedListAdapter;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.FunctionUtils;

public class ActivityList extends Activity {
	private Context mContext;

	private String ITEM_TITLE = "title";
	private String ITEM_CAPTION = "caption";

	private DataManager DBManager = null;

	private SeparatedListAdapter adapter;

	private ListView actListView;

	private Resources res;

	public Map<String, ?> createItem(String title, String caption) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_TITLE, title);
		item.put(ITEM_CAPTION, caption);
		return item;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);
		setContentView(R.layout.list);

		adapter = new SeparatedListAdapter(this);

		mContext = getApplicationContext();

		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = readAllRecentActivities();

		String[] recent_activities = null;
		String[] headers;

		CustomArrayAdapter listadapter_recent_act = null;

		res = getResources();
		final String[] activities = res.getStringArray(R.array.activities);

		if (cursor.moveToFirst()) // there are recent activities
		{
			int rows = cursor.getCount();
			recent_activities = new String[rows];

			for (int i = 1; i <= rows; i++) {
				recent_activities[i - 1] = activities[cursor.getInt(cursor.getColumnIndex("category_id")) - 1];
				cursor.moveToNext();
			}
			headers = new String[2];
			headers[0] = getString(R.string.activitylist_header_one);
			headers[1] = getString(R.string.activitylist_header_two);
			listadapter_recent_act = new CustomArrayAdapter(this, R.layout.list_item, recent_activities);
		} else {
			headers = new String[1];
			headers[0] = getString(R.string.activitylist_header_two);
		}

		cursor.close();
		DBManager.Close();

		CustomArrayAdapter listadapter_all_act = new CustomArrayAdapter(this, R.layout.list_item, activities);

		for (int i = 0; i < headers.length; i++) {
			if (headers.length == 1) {
				adapter.addSection(headers[i], listadapter_all_act);
			} else {
				switch (i) {
				case 0:
					adapter.addSection(headers[i], listadapter_recent_act);
					break;
				case 1:
					adapter.addSection(headers[i], listadapter_all_act);
					break;
				}
			}
		}

		actListView = (ListView) this.findViewById(R.id.list_activities);
		actListView.setAdapter(adapter);

		actListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
				SharedPreferences.Editor editor = prefs.edit();

				String item = (String) adapter.getItem(position);

				position = (FunctionUtils.getIdForThisItem(item, activities) + 1);

				editor.putInt("last_activity", position);
				editor.commit();

				finish();
			}
		});
	}

	private Cursor readAllRecentActivities() {
		return DBManager.CustomQuery(getString(R.string.db_query_top_three_activities),
				"SELECT DISTINCT(category_id), COUNT(category_id) AS times_selected"
						+ " FROM routes GROUP BY category_id ORDER BY times_selected DESC LIMIT 3;");
	}

	public class CustomArrayAdapter extends ArrayAdapter<String> {
		private final String[] objects;

		public CustomArrayAdapter(Context context, int textViewResourceId, String[] objects) {
			super(context, textViewResourceId, objects);

			this.objects = objects;
		}

		@SuppressLint({ "ViewHolder", "Recycle" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Inflate the layout, mainlvitem.xml, in each row.
			LayoutInflater inflater = ActivityList.this.getLayoutInflater();
			View row = inflater.inflate(R.layout.list_item, parent, false);

			// Declare and define the TextView, "item." This is where the name
			// of each item will appear.
			TextView item = (TextView) row.findViewById(R.id.list_item_text);
			item.setText(objects[position]);

			// Declare and define the TextView, "icon." This is where the icon
			// in each row will appear.
			TypedArray activities_icons = res.obtainTypedArray(R.array.activities_icons);
			ImageView icon = (ImageView) row.findViewById(R.id.list_item_icon);
			icon.setImageDrawable(activities_icons.getDrawable(FunctionUtils.getIdForThisItem(
					objects[position], res.getStringArray(R.array.activities))));

			return row;
		}
	}
}