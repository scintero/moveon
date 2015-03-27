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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import com.saulcintero.moveon.entities.EntityRoutes;
import com.saulcintero.moveon.enums.TypesOfPractices;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class PracticeDetails extends Activity implements OnClickListener {
	private SharedPreferences prefs;

	private Button save_btn, discard_btn;

	private EditText pract_name, pract_notes;

	private boolean save_button_is_tapped;

	private Spinner spinner;

	private DataManager DBManager = null;

	private EntityRoutes mRoute;

	private int id;

	private String[] shoes_name;
	private int[] shoes_id;

	private AlertDialog.Builder alert;
	private AlertDialog dialog;

	private IntentFilter intentFilter_Destroy_PracticeDetails_Activity;

	private BroadcastReceiver mReceiverDestroyPracticeDetailsActivity = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiverDestroyPracticeDetailsActivity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.practice_details);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		intentFilter_Destroy_PracticeDetails_Activity = new IntentFilter(
				"android.intent.action.DESTROY_PRACTICEDETAILS_ACTIVITY");
		registerReceiver(mReceiverDestroyPracticeDetailsActivity,
				intentFilter_Destroy_PracticeDetails_Activity);

		ArrayList<EntityRoutes> routeList;
		routeList = MoveOnService.getRoutesList();
		mRoute = (EntityRoutes) routeList.get(0);
		String name = mRoute.getDate().toString() + ", " + mRoute.getHour().toString();

		save_btn = (Button) findViewById(R.id.save_pract);
		discard_btn = (Button) findViewById(R.id.discard_pract);
		pract_name = (EditText) findViewById(R.id.practice_name);
		pract_notes = (EditText) findViewById(R.id.notes);
		spinner = (Spinner) findViewById(R.id.spinner1);

		pract_name.setText(name);

		DBManager = new DataManager(this);
		DBManager.Open();
		Cursor cursor = DBManager.CustomQuery(getString(R.string.db_query_getting_shoes),
				"SELECT * FROM shoes WHERE active=1 ORDER BY default_shoe DESC");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			shoes_name = new String[cursor.getCount() + 1];
			shoes_id = new int[cursor.getCount() + 1];

			for (int g = 0; g <= cursor.getCount() - 1; g++) {
				shoes_name[g] = cursor.getString(cursor.getColumnIndex("name"));
				shoes_id[g] = cursor.getInt(cursor.getColumnIndex("_id"));

				cursor.moveToNext();
			}
			shoes_name[cursor.getCount()] = getString(R.string.none_text);
			shoes_id[cursor.getCount()] = 0;
		} else {
			shoes_name = new String[1];
			shoes_id = new int[1];
			shoes_name[0] = getString(R.string.none_text);
			shoes_id[0] = 0;
		}
		cursor.close();
		DBManager.Close();

		save_button_is_tapped = false;

		save_btn.setOnClickListener(this);
		discard_btn.setOnClickListener(this);

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, shoes_name);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerArrayAdapter);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				if (shoes_id.length > 0) {
					id = shoes_id[i];
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.save_pract:
			if (pract_name.getText().length() == 0) {
				UIFunctionUtils.showMessage(this, true, getString(R.string.practice_name_missing));

				break;
			}

			if (!save_button_is_tapped) // control to no have leaks
			{
				save_button_is_tapped = true;

				MoveOnService.addMoreDataToRoutesEntity(pract_name.getText().toString(), String.valueOf(prefs
						.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes())), String
						.valueOf(id), pract_notes.getText().toString());

				ProgressDialog progress = new ProgressDialog(this);
				progress.setMessage(getString(R.string.saving_practice));
				progress.setCancelable(false);
				new SavePracticeTask(progress, this, getApplicationContext(), MoveOnService.getRoutesList(),
						MoveOnService.getLocationsList(), MoveOnService.getHrList(),
						MoveOnService.getCadenceList()).execute();
			}

			break;
		case R.id.discard_pract:
			alert = new AlertDialog.Builder(this);
			alert.setIcon(android.R.drawable.ic_dialog_alert);
			alert.setTitle(getString(R.string.confirm_action));
			alert.setMessage(getString(R.string.practice_cancel_saving_process));
			alert.setCancelable(true);

			alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					finish();
				}
			});
			alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});

			dialog = alert.create();
			dialog.show();

			break;
		}
	}
}