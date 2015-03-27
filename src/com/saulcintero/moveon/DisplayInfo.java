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

public class DisplayInfo extends Activity {
	private Context mContext;

	private String ITEM_TITLE = "title";
	private String ITEM_CAPTION = "caption";

	private SharedPreferences prefs;

	private SeparatedListAdapter adapter;

	private ListView journalListView;

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

		mContext = getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		final int display = prefs.getInt("selected_display", 1);

		String[] headers = { getString(R.string.dialog_title) };

		res = mContext.getResources();
		String[] displays = res.getStringArray(R.array.display_info);

		adapter = new SeparatedListAdapter(this);
		CustomArrayAdapter listadapter = new CustomArrayAdapter(this, R.layout.list_item, displays);

		for (int i = 0; i < headers.length; i++) {
			adapter.addSection(headers[i], listadapter);
		}

		journalListView = (ListView) this.findViewById(R.id.list_activities);
		journalListView.setAdapter(adapter);

		journalListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
				SharedPreferences.Editor editor = prefs.edit();

				switch (display) {
				case 1:
					editor.putInt("practice_display1", (position - 1));
					break;
				case 2:
					editor.putInt("practice_display2", (position - 1));
					break;
				case 3:
					editor.putInt("practice_display3", (position - 1));
					break;
				case 4:
					editor.putInt("practice_display4", (position - 1));
					break;
				}
				editor.commit();
				finish();
			}
		});
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
			LayoutInflater inflater = DisplayInfo.this.getLayoutInflater();
			View row = inflater.inflate(R.layout.list_item, parent, false);

			TextView item = (TextView) row.findViewById(R.id.list_item_text);
			item.setText(objects[position]);

			TypedArray type_icons = res.obtainTypedArray(R.array.display_big_type_icons);
			ImageView icon = (ImageView) row.findViewById(R.id.list_item_icon);
			icon.setImageDrawable(type_icons.getDrawable(position));

			return row;
		}
	}
}