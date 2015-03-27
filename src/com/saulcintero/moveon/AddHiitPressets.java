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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.saulcintero.moveon.adapters.StableArrayAdapter;
import com.saulcintero.moveon.entities.EntityHiit;
import com.saulcintero.moveon.entities.EntityHiitIntervals;
import com.saulcintero.moveon.ui.widgets.DynamicListView;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class AddHiitPressets extends Activity implements OnClickListener {
	private Activity act;

	private TextView hiit_name, hiit_rounds, hiit_preparation_minutes, hiit_preparation_seconds,
			hiit_cooldown_minutes, hiit_cooldown_seconds;

	private Button cancel, save;

	private DynamicListView listView;

	private StableArrayAdapter mAdapter;

	private AlertDialog.Builder alert;
	private AlertDialog dialog;

	private String[] mContent;

	private int pos = 0;

	private ArrayList<String> mActionList;

	private BroadcastReceiver mReceiverAddInterval = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newRow = listView.getCount() + "," + intent.getStringExtra("description") + ","
					+ intent.getStringExtra("time") + "," + intent.getStringExtra("type");

			addInterval(newRow);
		}
	};

	private void addInterval(String newRow) {
		mAdapter = null;

		ArrayList<String> temp = new ArrayList<String>();
		for (int i = 0; i < mActionList.size(); ++i) {
			temp.add(i + "," + mActionList.get(i)); // id,description
													// text,time,type
		}
		mActionList.add(newRow);
		temp = null;

		mAdapter = new StableArrayAdapter(act, R.layout.hiit_pressets_list_row, mActionList);
		listView.setActionList(mActionList);
		listView.setAdapter(mAdapter);

		listView.invalidate();
	}

	private BroadcastReceiver mReceiverEditInterval = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String editRow = intent.getStringExtra("id") + "," + intent.getStringExtra("description") + ","
					+ intent.getStringExtra("time") + "," + intent.getStringExtra("type");

			editInterval(intent.getStringExtra("id"), editRow);
		}
	};

	private void editInterval(String id, String editRow) {
		mAdapter = null;
		ArrayList<String> tempActionList = new ArrayList<String>();
		for (int i = 0; i < mActionList.size(); ++i) {
			String[] row = mActionList.get(i).split(",");
			if (row[0].toString().equals(id)) {
				tempActionList.add(editRow);
			} else {
				tempActionList.add(mActionList.get(i)); // id,description
														// text,time,type
			}
		}
		mActionList.clear();
		mActionList = tempActionList;
		tempActionList = null;

		mAdapter = new StableArrayAdapter(act, R.layout.hiit_pressets_list_row, mActionList);
		listView.setActionList(mActionList);
		listView.setAdapter(mAdapter);

		listView.invalidate();
	}

	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiverAddInterval);
		unregisterReceiver(mReceiverEditInterval);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);

		setContentView(R.layout.hiit_pressets);

		super.onCreate(savedInstanceState);

		act = this;

		hiit_name = (TextView) findViewById(R.id.hiit_name);
		hiit_rounds = (TextView) findViewById(R.id.hiit_rounds);
		hiit_preparation_minutes = (TextView) findViewById(R.id.hiit_preparation_minutes);
		hiit_preparation_seconds = (TextView) findViewById(R.id.hiit_preparation_seconds);
		hiit_cooldown_minutes = (TextView) findViewById(R.id.hiit_cooldown_minutes);
		hiit_cooldown_seconds = (TextView) findViewById(R.id.hiit_cooldown_seconds);
		listView = (DynamicListView) findViewById(R.id.listview);

		mActionList = new ArrayList<String>();

		alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.select_action));
		CharSequence[] choiceList = { this.getString(R.string.alertdialog_option_2),
				this.getString(R.string.alertdialog_option_5), this.getString(R.string.alertdialog_option_4) };

		alert.setItems(choiceList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					Intent intent = new Intent(act, EditHiitInterval.class);
					Bundle b = new Bundle();
					b.putString("row", mContent[0] + "," + mContent[1] + "," + mContent[2] + ","
							+ mContent[3]);
					intent.putExtras(b);
					startActivity(intent);

					break;
				case 1:
					String newRow = listView.getCount() + "," + mContent[1] + "," + mContent[2] + ","
							+ mContent[3];

					mAdapter = null;
					ArrayList<String> tempActionList = new ArrayList<String>();
					for (int i = 0; i < mActionList.size(); ++i) {
						tempActionList.add(i + "," + mActionList.get(i)); // id,description
																			// text,time,type
					}
					mActionList.add(newRow);
					tempActionList = null;

					mAdapter = new StableArrayAdapter(act, R.layout.hiit_pressets_list_row, mActionList);
					listView.setActionList(mActionList);
					listView.setAdapter(mAdapter);

					listView.invalidate();

					break;
				case 2:
					ArrayList<String> tempActionList2 = new ArrayList<String>();
					for (int i = 0; i < mActionList.size(); ++i) {
						tempActionList2.add(mActionList.get(i));
					}
					mActionList.clear();

					int g = 0;
					for (int p = 0; p <= (listView.getCount() - 1); p++) {
						if (p != pos) {
							mContent = tempActionList2.get(p).split(",");

							mActionList.add(g + "," + mContent[1] + "," + mContent[2] + "," + mContent[3]);

							g += 1;
						}
					}

					tempActionList2 = null;
					mAdapter = null;
					mAdapter = new StableArrayAdapter(act, R.layout.hiit_pressets_list_row, mActionList);
					listView.setActionList(mActionList);
					listView.setAdapter(mAdapter);

					listView.invalidate();

					break;
				}

				dialog.dismiss();
			}
		});

		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				pos = position;

				mContent = mAdapter.getItem(position).toString().split(",");

				dialog = alert.create();
				dialog.show();
			}
		});

		cancel = (Button) findViewById(R.id.cancel);
		save = (Button) findViewById(R.id.save);

		cancel.setOnClickListener(this);
		save.setOnClickListener(this);

		registerReceiver(mReceiverAddInterval, new IntentFilter("android.intent.action.ADD_INTERVAL"));
		registerReceiver(mReceiverEditInterval, new IntentFilter("android.intent.action.EDIT_INTERVAL"));
	}

	@Override
	public void onClick(View v) {
		ArrayList<String> msgs = new ArrayList<String>();

		switch (v.getId()) {
		case R.id.cancel:
			finish();
			break;
		case R.id.save:
			boolean isDataOK = true;

			if (hiit_name.getText().toString().length() == 0) {
				isDataOK = false;
				msgs.add(getString(R.string.required_name));
			}
			if (hiit_rounds.getText().toString().length() == 0
					&& !FunctionUtils.isNumeric(hiit_rounds.getText().toString())) {
				isDataOK = false;
				msgs.add(getString(R.string.number_of_rounds_error));
			}
			if (hiit_preparation_minutes.getText().toString().length() == 0
					&& !FunctionUtils.isNumeric(hiit_preparation_minutes.getText().toString())) {
				isDataOK = false;
				msgs.add(getString(R.string.hiit_preparation_minutes_error));
			}
			if (hiit_preparation_seconds.getText().toString().length() == 0
					&& !FunctionUtils.isNumeric(hiit_preparation_seconds.getText().toString())) {
				isDataOK = false;
				msgs.add(getString(R.string.hiit_preparation_seconds_error));
			}
			if (hiit_cooldown_minutes.getText().toString().length() == 0
					&& !FunctionUtils.isNumeric(hiit_cooldown_minutes.getText().toString())) {
				isDataOK = false;
				msgs.add(getString(R.string.hiit_cooldown_minutes_error));
			}
			if (hiit_cooldown_seconds.getText().toString().length() == 0
					&& !FunctionUtils.isNumeric(hiit_cooldown_seconds.getText().toString())) {
				isDataOK = false;
				msgs.add(getString(R.string.hiit_cooldown_seconds_error));
			}
			if (listView.getCount() == 0) {
				isDataOK = false;
				msgs.add(getString(R.string.hiit_missing_intervals));
			}

			if (isDataOK) {
				int time = 0;
				int preparation_time = ((Integer.parseInt((hiit_preparation_minutes.getText().toString())) * 60) + Integer
						.parseInt((hiit_preparation_seconds.getText().toString())));
				int cooldown_time = ((Integer.parseInt((hiit_cooldown_minutes.getText().toString())) * 60) + Integer
						.parseInt((hiit_cooldown_seconds.getText().toString())));

				String[][] hiitIntervalsArray = new String[listView.getCount()][2];
				for (int position = 0; position < listView.getCount(); position++) {
					mContent = mAdapter.getItem(position).toString().split(",");

					time = time + Integer.parseInt(mContent[2]);

					hiitIntervalsArray[position][0] = mContent[2];
					hiitIntervalsArray[position][1] = mContent[3];
				}
				time = (time * Integer.parseInt(hiit_rounds.getText().toString())) + preparation_time
						+ cooldown_time;

				ArrayList<EntityHiit> hiitList = new ArrayList<EntityHiit>();
				EntityHiit hiit = new EntityHiit();
				hiit.setName(hiit_name.getText().toString());
				hiit.setTotal_time(String.valueOf(time));
				hiit.setRounds(hiit_rounds.getText().toString());
				hiit.setActions(String.valueOf(listView.getCount()));
				hiit.setPreparation_time(String.valueOf(preparation_time));
				hiit.setCoolDown_time(String.valueOf(cooldown_time));
				hiit.setActive("1");
				hiitList.add(hiit);
				DataFunctionUtils.createHiitInDB(this, hiitList);

				ArrayList<EntityHiitIntervals> hiitIntervalsList = new ArrayList<EntityHiitIntervals>();
				for (int i = 0; i < hiitIntervalsArray.length; i++) {
					EntityHiitIntervals hiitIntervals = new EntityHiitIntervals();
					hiitIntervals.setTime(hiitIntervalsArray[i][0]);
					hiitIntervals.setType(hiitIntervalsArray[i][1]);
					hiitIntervalsList.add(hiitIntervals);
				}
				DataFunctionUtils.createHiitIntervalsInDB(this, hiitIntervalsList);

				finish();
			} else {
				for (int i = 0; i < msgs.size(); i++) {
					UIFunctionUtils.showMessage(this, true, msgs.get(i));
				}
			}

			break;
		}
	}
}