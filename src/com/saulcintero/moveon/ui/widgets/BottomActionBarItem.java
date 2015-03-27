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

package com.saulcintero.moveon.ui.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.saulcintero.moveon.AddPractice;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.osm.OSMHelper;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class BottomActionBarItem extends ImageButton implements OnLongClickListener, OnClickListener {
	private final Context mContext;

	private int selected_activity = 0, selected_date = 0;

	public BottomActionBarItem(Context context) {
		super(context);

		mContext = context;
	}

	public BottomActionBarItem(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;

		setOnLongClickListener(this);
		setOnClickListener(this);
	}

	@SuppressLint("InflateParams")
	@Override
	public void onClick(View v) {
		if (!MoveOnService.getIsPracticeRunning()) {
			switch (v.getId()) {
			case R.id.history_action_bar_item_one:
				mContext.startActivity(new Intent(mContext, AddPractice.class));
				break;
			case R.id.history_action_bar_item_two:
				UIFunctionUtils.importPracticeDialog(mContext);
				break;
			case R.id.history_action_bar_item_three:
				UIFunctionUtils.exportRoutesDialogWithCheckBox(mContext);
				break;
			case R.id.history_action_bar_item_four:
				AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
				AlertDialog dialog;
				Resources res = mContext.getResources();
				Spinner spinner1,
				spinner2;
				final String[] activities = res.getStringArray(R.array.activities);
				String[] choiceActivityList = new String[activities.length + 1];
				String[] choiceTimeList = res.getStringArray(R.array.choice_time_list);

				alert.setTitle(mContext.getString(R.string.filter_activities));
				alert.setCancelable(true);

				LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
				View alertLayout = inflater.inflate(R.layout.history_custom_filter, null);
				alert.setView(alertLayout);

				spinner1 = (Spinner) alertLayout.findViewById(R.id.spinner1);
				spinner2 = (Spinner) alertLayout.findViewById(R.id.spinner2);

				spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
						selected_activity = i;
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

				spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
						selected_date = i;
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

				choiceActivityList[0] = mContext.getString(R.string.activitylist_header_two);
				for (int d = 0; d < activities.length; d++) {
					choiceActivityList[d + 1] = activities[d];
				}

				ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext,
						android.R.layout.simple_spinner_item, choiceActivityList);
				spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner1.setAdapter(spinnerArrayAdapter);

				ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(mContext,
						android.R.layout.simple_spinner_item, choiceTimeList);
				spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner2.setAdapter(spinnerArrayAdapter2);

				alert.setPositiveButton(mContext.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								int typeOfFilter = 0;
								if ((selected_activity > 0) && ((selected_date > 0))) {
									switch (selected_date) {
									case 1:
										typeOfFilter = 6;
										break;
									case 2:
										typeOfFilter = 7;
										break;
									case 3:
										typeOfFilter = 8;
										break;
									case 4:
										typeOfFilter = 9;
										break;
									}
								} else {
									if (selected_activity > 0)
										typeOfFilter = 1;
									if (selected_date > 0) {
										switch (selected_date) {
										case 1:
											typeOfFilter = 2;
											break;
										case 2:
											typeOfFilter = 3;
											break;
										case 3:
											typeOfFilter = 4;
											break;
										case 4:
											typeOfFilter = 5;
											break;
										}
									}
								}
								Intent i = new Intent("android.intent.action.FILTER_ROUTES");
								i.putExtra("typeOfFilter", String.format("%s", typeOfFilter));
								i.putExtra("activities_id", String.format("%s", selected_activity));
								((Activity) mContext).sendBroadcast(i);
							}
						});
				alert.setNegativeButton(mContext.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
							}
						});

				dialog = alert.create();
				dialog.show();
				break;
			case R.id.history_action_bar_item_five:
				if (!OSMHelper.IsOsmAuthorized(mContext))
					UIFunctionUtils.createOsmAuthAlertDialog(mContext);
				else
					UIFunctionUtils.exportAndShareMultipleRoutesDialog(mContext);
				break;
			default:
				break;
			}
		} else {
			UIFunctionUtils.showMessage(mContext, false,
					mContext.getString(R.string.stop_running_practice_first));
		}
	}

	@Override
	public boolean onLongClick(View v) {
		UIFunctionUtils.showMessage(mContext, true, v.getContentDescription().toString());
		return true;
	}
}