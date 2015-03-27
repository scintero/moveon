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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.saulcintero.moveon.enums.TypesOfPractices;

public class Objetives extends Activity {
	private Context mContext;

	private Resources res;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private ListView objListView;

	private ArrayList<HashMap<String, String>> mList;

	private BroadcastReceiver mReceiverCloseObjetivesActivity = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiverCloseObjetivesActivity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);
		setContentView(R.layout.objetives);

		mContext = this.getApplicationContext();
		res = getResources();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		objListView = (ListView) findViewById(R.id.list_objetives);

		printMenu();

		registerReceiver(mReceiverCloseObjetivesActivity, new IntentFilter(
				"android.intent.action.ACTION_CLOSE_OBJETIVES_ACTIVITY"));

		objListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
				switch (position) {
				case 0:
					editor.putInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes());
					editor.commit();
					finish();
					break;
				case 1:
					startActivity(new Intent(mContext, HiitList.class));
					break;
				}
			}
		});

		super.onCreate(savedInstanceState);
	}

	private void printMenu() {
		mList = new ArrayList<HashMap<String, String>>();

		for (int i = 0; i <= 1; i++) {
			HashMap<String, String> hm = new HashMap<String, String>();

			TypesOfPractices whichType = TypesOfPractices.values()[i];
			switch (whichType) {
			case BASIC_PRACTICE:
				hm.put("icon1", Integer.toString(res.getIdentifier(
						"com.saulcintero.moveon:drawable/basic_practice", null, null)));
				hm.put("icon2", Integer.toString(res.getIdentifier(
						"com.saulcintero.moveon:drawable/button_practice_arrow", null, null)));
				hm.put("text1", getString(R.string.basic_practice));
				hm.put("text2", getString(R.string.basic_practice_details));
				break;
			case HIIT_PRACTICE:
				hm.put("icon1", Integer.toString(res.getIdentifier(
						"com.saulcintero.moveon:drawable/intervals", null, null)));
				hm.put("icon2", Integer.toString(res.getIdentifier(
						"com.saulcintero.moveon:drawable/button_practice_arrow", null, null)));
				hm.put("text1", getString(R.string.hiit));
				hm.put("text2", getString(R.string.hiit_details));
				break;
			default:
				break;
			}

			mList.add(hm);
		}

		String[] from = { "icon1", "icon2", "text1", "text2" };
		int[] to = { R.id.objetives_icon, R.id.objetives_arrow, R.id.objetives_label1, R.id.objetives_label2 };

		SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.objetives_list_row, from, to);
		objListView.setAdapter(adapter);
	}
}