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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class Shoes extends Activity {
	private Activity act;

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private ListView listView;

	private IntentFilter intentFilter;

	private ArrayList<Integer> idList;
	private ArrayList<String> nameList;

	private TextView empty_text;

	private boolean isMetric;
	private int default_shoe;

	private BroadcastReceiver mReceiverRefreshShoes = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			loadShoes();
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiverRefreshShoes);
	}

	@Override
	public void onResume() {
		super.onResume();

		loadShoes();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.shoes);

		act = this;

		prefs = PreferenceManager.getDefaultSharedPreferences(act);
		editor = prefs.edit();

		listView = (ListView) findViewById(R.id.list_shoes);
		empty_text = (TextView) findViewById(R.id.empty_shoes_list);

		isMetric = FunctionUtils.checkIfUnitsAreMetric(this);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {

				int selected_shoe = Integer.parseInt(idList.get(position).toString());
				if ((default_shoe == position) && (listView.getCount() > 1)) {
					editor.putInt("default_shoe", 1);
				} else {
					editor.putInt("default_shoe", 0);
				}

				editor.putInt("selected_shoe", selected_shoe);
				editor.commit();

				UIFunctionUtils.createAlertDialog(act, 3, nameList.get(position).toString());
			}
		});

		intentFilter = new IntentFilter("android.intent.action.REFRESH_SHOES");
		registerReceiver(mReceiverRefreshShoes, intentFilter);
	}

	private void loadShoes() {
		idList = new ArrayList<Integer>();
		nameList = new ArrayList<String>();

		DBManager = new DataManager(this);
		DBManager.Open();
		cursor = DBManager.CustomQuery("Seleccionando todas las zapatillas activas de la tabla 'shoes'",
				"SELECT * FROM shoes WHERE active = '1' ORDER BY _id DESC;");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			empty_text.setVisibility(View.INVISIBLE);

			List<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();

			for (int i = 0; i <= (cursor.getCount() - 1); i++) {
				HashMap<String, String> hm = new HashMap<String, String>();
				hm.put("shoe_name", cursor.getString(cursor.getColumnIndex("name")));
				hm.put("shoe_distance",
						String.valueOf(isMetric ? FunctionUtils.customizedRound(
								cursor.getFloat(cursor.getColumnIndex("distance")), 2) : FunctionUtils
								.getMilesFromKilometersWithTwoDecimals(cursor.getFloat(cursor
										.getColumnIndex("distance"))))
								+ " "
								+ (isMetric ? getString(R.string.long_unit1_detail_1)
										: getString(R.string.long_unit2_detail_1)));
				if (cursor.getInt(cursor.getColumnIndex("default_shoe")) > 0) {
					hm.put("shoe_default_icon", Integer.toString(R.drawable.default_icon));
					default_shoe = i;
				} else {
					hm.put("shoe_default_icon", Integer.toString(R.drawable.empty));
				}
				mList.add(hm);

				idList.add(cursor.getInt(cursor.getColumnIndex("_id")));
				nameList.add(cursor.getString(cursor.getColumnIndex("name")));

				cursor.moveToNext();
			}

			String[] from = { "shoe_name", "shoe_distance", "shoe_default_icon" };
			int[] to = { R.id.shoe_name, R.id.shoe_distance, R.id.shoe_default_icon };

			SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.shoes_list_row, from, to);
			listView.setAdapter(adapter);
		} else {
			empty_text.setVisibility(View.VISIBLE);
			listView.setAdapter(null);
		}
		cursor.close();
		DBManager.Close();
	}
}