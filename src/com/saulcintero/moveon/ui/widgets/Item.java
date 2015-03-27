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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

import com.saulcintero.moveon.ActivityList;
import com.saulcintero.moveon.AddHiitInterval;
import com.saulcintero.moveon.AddHiitPressets;
import com.saulcintero.moveon.Constants;
import com.saulcintero.moveon.DisplayInfo;
import com.saulcintero.moveon.MoveOnPreferences;
import com.saulcintero.moveon.Objetives;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.utils.TextToSpeechUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class Item extends ImageButton implements OnLongClickListener, OnClickListener {
	private final Context mContext;

	private SharedPreferences prefs;

	private Intent intentPracticeStatus = new Intent("android.intent.action.PRACTICE_STATUS");
	private Intent intentMainButtonsStatus = new Intent("android.intent.action.PRACTICE_BUTTONS_STATUS");
	private Intent intentExpandRestartMainMapview = new Intent(
			"android.intent.action.EXPAND_OR_RESTART_PRACTICE_MAPVIEW");
	private Intent intentExpandRestartSummaryMapview = new Intent(
			"android.intent.action.EXPAND_OR_RESTART_SUMMARY_MAPVIEW");
	private Intent intentTakePicture = new Intent("android.intent.action.TAKE_PICTURE");
	private Intent intentFollowLocation = new Intent("android.intent.action.FOLLOW_LOCATION");

	public Item(Context context) {
		super(context);

		mContext = context;
	}

	public Item(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		setOnLongClickListener(this);
		setOnClickListener(this);
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		switch (v.getId()) {
		case R.id.practice_gps_image:
			if (!prefs.getBoolean("blocked", false)) {
				mContext.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			}
			break;
		case R.id.practice_display1_image:
			if (!prefs.getBoolean("blocked", false)) {
				launchDisplayInfoActivity(1);
			}
			break;
		case R.id.practice_display2_image:
			if (!prefs.getBoolean("blocked", false)) {
				launchDisplayInfoActivity(2);
			}
			break;
		case R.id.practice_display3_image:
			if (!prefs.getBoolean("blocked", false)) {
				launchDisplayInfoActivity(3);
			}
			break;
		case R.id.practice_display4_image:
			if (!prefs.getBoolean("blocked", false)) {
				launchDisplayInfoActivity(4);
			}
			break;
		case R.id.practice_display5_image:
			if (!MoveOnService.getIsPracticeRunning()) {
				if (!prefs.getBoolean("blocked", false))
					mContext.startActivity(new Intent(mContext, Objetives.class));
			} else {
				UIFunctionUtils.showMessage(mContext, true,
						mContext.getString(R.string.stop_running_practice_to_choose_new_objetive));
			}
			break;
		case R.id.practice_display6_image:
			if (!prefs.getBoolean("blocked", false))
				mContext.startActivity(new Intent(mContext, ActivityList.class));
			break;
		case R.id.practice_mapview_fullscreen:
			if (!prefs.getBoolean("blocked", false))
				mContext.sendBroadcast(intentExpandRestartMainMapview);
			break;
		case R.id.summary_mapview_fullscreen:
			mContext.sendBroadcast(intentExpandRestartSummaryMapview);
			break;
		case R.id.main_action_bar_item_two:
			changeLockedStatus();
			intentMainButtonsStatus
					.putExtra("practiceButtonsStatus", String.valueOf(Constants.LOCKED_STATUS));
			mContext.sendBroadcast(intentMainButtonsStatus);
			break;
		case R.id.new_shoes:
			UIFunctionUtils.createAlertDialog(mContext, 2, null);
			break;
		case R.id.new_hiit:
			mContext.startActivity(new Intent(mContext, AddHiitPressets.class));
			break;
		case R.id.main_action_bar_item_three:
			if ((!prefs.getBoolean("blocked", false)) && (MoveOnService.getIsPracticeRunning())) {
				mContext.sendBroadcast(intentTakePicture);
			}
			break;
		case R.id.main_action_bar_item_four:
			if (!prefs.getBoolean("blocked", false)) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					mContext.startActivity(new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER));
				} else {
					Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
							Intent.CATEGORY_APP_MUSIC);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
				}
			}
			break;
		case R.id.main_action_bar_settings:
			if (!prefs.getBoolean("blocked", false))
				mContext.startActivity(new Intent(mContext, MoveOnPreferences.class));
			break;
		case R.id.practice_start_image:
			if ((!prefs.getBoolean("blocked", false)) && (!MoveOnService.getIsPracticeRunning())) {
				intentPracticeStatus.putExtra("practiceStatus",
						String.valueOf(Constants.PRACTICE_START_STATUS));
				mContext.sendBroadcast(intentPracticeStatus);
			} else {
				showLockedMsg();
			}
			break;
		case R.id.practice_pauseresume_image:
			if (!prefs.getBoolean("blocked", false)) {
				intentPracticeStatus.putExtra("practiceStatus",
						String.valueOf(Constants.PRACTICE_PAUSE_OR_RESUME_STATUS));
				mContext.sendBroadcast(intentPracticeStatus);
			} else {
				showLockedMsg();
			}
			break;
		case R.id.practice_stop_image:
			if (!prefs.getBoolean("blocked", false)) {
				intentPracticeStatus.putExtra("practiceStatus",
						String.valueOf(Constants.PRACTICE_STOP_STATUS));
				mContext.sendBroadcast(intentPracticeStatus);
			} else {
				showLockedMsg();
			}
			break;
		case R.id.main_action_bar_item_one:
			if (!TextToSpeechUtils.getIfHasErrors()) {
				if (!prefs.getBoolean("blocked", false)) {
					if (prefs.getBoolean("speak", false)) {
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("speak", false);
						editor.commit();

						TextToSpeechUtils.setSpeak(false);
					} else {

						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("speak", true);
						editor.commit();

						TextToSpeechUtils.setSpeak(true);

					}
					intentMainButtonsStatus.putExtra("practiceButtonsStatus",
							String.valueOf(Constants.VOICE_COACH_STATUS));
					mContext.sendBroadcast(intentMainButtonsStatus);
				} else {
					UIFunctionUtils.showMessage(mContext, false,
							mContext.getString(R.string.tts_language_is_not_available));
				}
			}
			break;
		case R.id.practice_mapview_followlocation:
			mContext.sendBroadcast(intentFollowLocation);
			break;
		case R.id.add_hiit_interval:
			mContext.startActivity(new Intent(mContext, AddHiitInterval.class));
			break;
		default:
			break;
		}
	}

	private void showLockedMsg() {
		UIFunctionUtils.showMessage(mContext, true, mContext.getString(R.string.app_locked));
	}

	private void changeLockedStatus() {
		SharedPreferences.Editor editor = prefs.edit();
		if (prefs.getBoolean("blocked", false))
			editor.putBoolean("blocked", false);
		else
			editor.putBoolean("blocked", true);
		editor.commit();
	}

	private void launchDisplayInfoActivity(int d) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("selected_display", d);
		editor.commit();

		mContext.startActivity(new Intent(mContext, DisplayInfo.class));
	}

	@Override
	public boolean onLongClick(View v) {
		UIFunctionUtils.showMessage(mContext, true, v.getContentDescription().toString());
		return true;
	}
}