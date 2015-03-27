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
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.enums.TypesOfPractices;
import com.saulcintero.moveon.utils.FunctionUtils;

public class HiitList extends Activity {
	private Context mContext;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private AlertDialog dialog;

	private TextView empty_text;

	private ListView listView;

	private String mTime, mPrepTime;

	private ArrayList<Integer> idList;
	private ArrayList<String> nameList;

	private int pos = 0;

	@Override
	public void onResume() {
		super.onResume();

		loadPrefets();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);

		setContentView(R.layout.hiit);

		mContext = getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		listView = (ListView) findViewById(R.id.list_intervals);
		empty_text = (TextView) findViewById(R.id.empty_interval_list);

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.select_action));
		CharSequence[] choiceList = { this.getString(R.string.alertdialog_option_6),
				this.getString(R.string.alertdialog_option_2), this.getString(R.string.alertdialog_option_4) };

		alert.setItems(choiceList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					editor.putInt("practice_display5", TypesOfPractices.HIIT_PRACTICE.getTypes());
					editor.putInt("selected_hiit", idList.get(pos));
					editor.commit();

					sendBroadcast(new Intent("android.intent.action.ACTION_CLOSE_OBJETIVES_ACTIVITY"));

					finish();

					break;
				case 1:
					Intent intent = new Intent(mContext, EditHiitPressets.class);
					Bundle b = new Bundle();
					b.putString("id", String.valueOf(idList.get(pos)));
					intent.putExtras(b);
					startActivity(intent);

					break;
				case 2:
					DBManager = new DataManager(mContext);
					DBManager.Open();
					DBManager.Edit(idList.get(pos), "active", "0", "hiit");
					DBManager.Close();

					loadPrefets();

					break;
				}

				dialog.dismiss();
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
				pos = position;

				dialog = alert.create();
				dialog.show();
			}
		});
	}

	private void loadPrefets() {
		idList = new ArrayList<Integer>();
		nameList = new ArrayList<String>();

		DBManager = new DataManager(mContext);
		DBManager.Open();
		cursor = DBManager.CustomQuery(getString(R.string.db_query_select_hiit_intervals),
				"SELECT * FROM hiit WHERE active = '1';");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			empty_text.setVisibility(View.INVISIBLE);

			List<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();

			for (int i = 0; i <= (cursor.getCount() - 1); i++) {
				long time = Long
						.parseLong(String.valueOf(cursor.getInt(cursor.getColumnIndex("total_time"))));
				if (time < 3600) {
					mTime = FunctionUtils.shortFormatTime(time);
				} else {
					mTime = FunctionUtils.longFormatTime(time);
				}

				long prep_time = Long.parseLong(String.valueOf(cursor.getInt(cursor
						.getColumnIndex("preparation_time"))));
				if (time < 3600) {
					mPrepTime = FunctionUtils.shortFormatTime(prep_time);
				} else {
					mPrepTime = FunctionUtils.longFormatTime(prep_time);
				}

				HashMap<String, String> hm = new HashMap<String, String>();
				hm.put("hiit_name", cursor.getString(cursor.getColumnIndex("name")));
				hm.put("hiit_total_time", mTime);
				hm.put("hiit_description",
						cursor.getString(cursor.getColumnIndex("rounds")) + " "
								+ getString(R.string.round_or_rounds) + ", "
								+ cursor.getString(cursor.getColumnIndex("actions")) + " "
								+ getString(R.string.intervals) + ", "
								+ getString(R.string.warm_up_description) + " " + mPrepTime);
				mList.add(hm);

				idList.add(cursor.getInt(cursor.getColumnIndex("_id")));
				nameList.add(cursor.getString(cursor.getColumnIndex("name")));

				cursor.moveToNext();
			}

			String[] from = { "hiit_name", "hiit_total_time", "hiit_description" };
			int[] to = { R.id.hiit_name, R.id.hiit_total_time, R.id.hiit_description };

			SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.hiit_list_row, from, to);
			listView.setAdapter(adapter);
		} else {
			empty_text.setVisibility(View.VISIBLE);
			listView.setAdapter(null);
		}
		cursor.close();
		DBManager.Close();
	}
}