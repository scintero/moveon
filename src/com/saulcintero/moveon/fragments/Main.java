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

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.saulcintero.moveon.Constants;
import com.saulcintero.moveon.CountDown;
import com.saulcintero.moveon.PracticeDetails;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.entities.EntityLocations;
import com.saulcintero.moveon.enums.DisplayTypes;
import com.saulcintero.moveon.enums.TypesOfPractices;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.MediaFunctionUtils;
import com.saulcintero.moveon.utils.TextToSpeechUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;
import com.saulcintero.osmdroid.MapOverlay;

public class Main extends Fragment {
	private SharedPreferences prefs;
	private LinearLayout fragmentView;
	private Resources res;

	private Activity act;
	private Context mContext;

	public static boolean launchPractice = false;

	private IntentFilter intentFilter_GPS_Status, intentFilter_GPS_Accuracy, intentFilter_GPS_Data,
			intentFilter_Practice_Status, intentFilter_GPS_Location, intentFilter_Buttons_Status,
			intentFilter_Step_Counter, intentFilter_Expand_Restart_Mapview, intentFilter_Take_Picture,
			intentFilter_Beat_Counter, intentFilter_Import_Data_Toast, intentFilter_Follow_Location,
			intentFilter_Unfollow_Location, intentFilter_Stop_And_Save, intentFilter_Update_Text_Of_Display5,
			intentFilter_Update_Color_Of_Display5, intentFilter_Cadence;

	private Chronometer chrono;

	private ImageButton full_mapview;

	private LinearLayout layout1, layout2, layout4, layout5;

	private MapView mapView;
	private MapController mapController;
	private MyLocationNewOverlay mLocationOverlay = null;
	private PathOverlay pathOverlay;
	private CompassOverlay mCompassOverlay;

	private double latCenterPoint, lonCenterPoint;
	private GeoPoint centerPoint;
	private ArrayList<GeoPoint> paintedPath;

	private ImageView gpsImage;

	private Animation animationFadeIn, animationFadeOut;

	private TextView gpsText;

	private ImageView display1_icon, display2_icon, display3_icon, display4_icon;
	private TextView display1_label, display1_text1, display1_text2;
	private TextView display2_label, display2_text1, display2_text2;
	private TextView display3_label, display3_text1, display3_text2;
	private TextView display4_label, display4_text1, display4_text2;
	private TextView display5_label, display5_text1;
	private ImageView display5_image;
	private TextView display6_text1;
	private ImageView display6_image;

	private ImageView start_button, pauseresume_button, stop_button, coach_button, locked_button,
			follow_button;
	private TextView start_text, pauseresume_text, stop_text;

	private String mSpeed, mMaxSpeed, mDistance, mLatitude, mLongitude, mAltitude, mSteps, mHeartRate,
			mCadence;
	private String gpsStatus = String.valueOf(Constants.GPS_ITEM_OFF), gpsAccuracy = "0";

	private String path, file;

	private int pauseOrResumeStatus = 0, stopStatus = 0, activity = 1, ACTIVITY_CAMERA = 1, pictureCounter,
			zoom = 15;

	private int hiit_total_time, hiit_rounds, hiit_prepSeconds, hiit_coolDownSeconds, hiit_actionSeconds,
			hiit_passedRounds = 1, hiit_actionSecondsByRound, hiit_lastRoundsSeconds = 0;

	private ArrayList<int[]> hiit_intervals;

	private String[] hiit_data;

	private String hiit_information = "", hiit_label = "", hiit_name = "";

	private boolean pathOverlayChangeColor = true, isExpanded = false, followLocation = true, hasBeenResumed,
			isMetric;

	private long chronoBaseValue;

	private BroadcastReceiver mReceiverGpsAccuracy = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			if (context == null)
				return;
			String action = intent.getAction();
			if (action == null)
				return;

			gpsAccuracy = intent.getStringExtra("gpsAccuracy");
			changeAccuracyText();
		}
	};

	private void changeAccuracyText() {
		if (Integer.parseInt(gpsStatus) == Constants.GPS_ITEM_ON) {
			int accuracy = Integer.parseInt(gpsAccuracy);
			switch (accuracy) {
			case 0:
				gpsText.setText(getString(R.string.gps_on));
				break;
			case 1:
				gpsText.setText(getString(R.string.gps_on) + " " + getString(R.string.bad_signal));
				break;
			case 2:
				gpsText.setText(getString(R.string.gps_on) + " " + getString(R.string.weak_signal));
				break;
			case 3:
				gpsText.setText(getString(R.string.gps_on) + " " + getString(R.string.good_signal));
				break;
			case 4:
				gpsText.setText(getString(R.string.gps_on) + " " + getString(R.string.excellent_signal));
				break;
			}
		}
	}

	private BroadcastReceiver mReceiverGpsStatus = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			gpsStatus = intent.getStringExtra("GPSStatus");
			changeStatus(Integer.parseInt(gpsStatus));
		}
	};

	private void changeStatus(int status) {
		gpsImage = (ImageButton) act.findViewById(R.id.practice_gps_image);
		gpsText = (TextView) act.findViewById(R.id.practice_gps_item);

		switch (status) {
		case Constants.GPS_ITEM_OFF:
			gpsImage.setImageResource(R.drawable.gps_off);
			gpsText.setText(getString(R.string.gps_off));
			break;
		case Constants.GPS_ITEM_SEARCHING:
			gpsImage.setImageResource(R.drawable.gps_searching);
			gpsText.setText(getString(R.string.gps_searching));
			break;
		case Constants.GPS_ITEM_ON:
			gpsImage.setImageResource(R.drawable.gps_on);
			changeAccuracyText();
			break;
		}
	}

	private BroadcastReceiver mReceiverGpsData = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			showData();

			if (MoveOnService.getIsPracticeRunning()) {
				mSpeed = intent.getStringExtra("speed");
				mMaxSpeed = intent.getStringExtra("maxSpeed");
				mDistance = intent.getStringExtra("distance");
				mLatitude = intent.getStringExtra("latitude");
				mLongitude = intent.getStringExtra("longitude");
				mAltitude = intent.getStringExtra("altitude");
			}

			getMapviewCenterPoint();

			if (followLocation)
				centerMyLocation(centerPoint);
		}
	};

	private BroadcastReceiver mReceiverGpsLocation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean newLocation = intent.getBooleanExtra("changedlocation", false);
			if (newLocation && MoveOnService.getIsPracticeRunning()) {
				if (((mLatitude != null) && (mLatitude.length() > 0))
						&& ((mLongitude != null) & (mLongitude.length() > 0))) {
					GeoPoint gp = new GeoPoint(Double.valueOf(mLatitude), Double.valueOf(mLongitude));
					paintedPath.add(gp);
					pathOverlay.addPoint(gp);
				}
			}
		}
	};

	private BroadcastReceiver mReceiverStepCounter = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mSteps = String.valueOf(Integer.parseInt(intent.getStringExtra("steps")));
			showData();
		}
	};

	private BroadcastReceiver mReceiverBeatCounter = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mHeartRate = String.valueOf(Integer.parseInt(intent.getStringExtra("beats")));
			showData();
		}
	};

	private BroadcastReceiver mReceiverCadence = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mCadence = String.valueOf(Integer.parseInt(intent.getStringExtra("cadence")));
			showData();
		}
	};

	private BroadcastReceiver mReceiverButtonsStatus = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (Integer.parseInt(intent.getStringExtra("practiceButtonsStatus"))) {
			case Constants.VOICE_COACH_STATUS:
				checkCoachStatus();
				break;
			case Constants.LOCKED_STATUS:
				checkLockedStatus();
				break;
			}
		}
	};

	private BroadcastReceiver mReceiverPracticeStatus = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int practiceStatus = Integer.parseInt(intent.getStringExtra("practiceStatus"));

			switch (practiceStatus) {
			case Constants.PRACTICE_STOP_STATUS:
				if (stopStatus == 0) // the first time, pause the practice
				{
					if (!MoveOnService.getIsPracticePaused()) {
						chrono.stop();
						MoveOnService.pausePractice();
						pauseresume_text.setText(getString(R.string.play_2));
						disablePathColor();
						pauseOrResumeStatus += 1;
					}

					MoveOnService.setPausedForSavePractice(true);
					stopStatus += 1;
					stop_text.setText(getString(R.string.save));
				} else { // the second time, stops and saves
					stopAndSave();
				}
				break;
			case Constants.PRACTICE_START_STATUS:
				if (Integer.parseInt((prefs.getString("countdown", "0"))) > 0) {
					startActivity(new Intent(mContext, CountDown.class));
				} else {
					startPractice();
				}
				break;
			case Constants.PRACTICE_PAUSE_OR_RESUME_STATUS:
				switch (pauseOrResumeStatus) {
				case Constants.PRACTICE_FROM_STARTED_TO_PAUSED:
					pausePractice();
					break;
				case Constants.PRACTICE_FROM_PAUSED_TO_RESUMED:
					resumePractice();
					break;
				}
				break;
			case Constants.PRACTICE_AUTO_PAUSE_STATUS:
				chrono.stop();
				pauseresume_text.setText(getString(R.string.play_2));
				disablePathColor();
				pauseOrResumeStatus += 1;
				stopStatus = 1;
				stop_text.setText(getString(R.string.save));
				break;
			case Constants.PRACTICE_AUTO_RESUME_STATUS:
				pauseresume_text.setText(getString(R.string.play_1));
				stop_text.setText(getString(R.string.stop));
				enablePathColor();
				pauseOrResumeStatus = 1;
				stopStatus = 0;
				chrono.setBase(calculateChronoBaseValue(false));
				chrono.start();
				break;
			}
		}

		private void pausePractice() {
			chrono.stop();

			pauseresume_text.setText(getString(R.string.play_2));
			disablePathColor();
			pauseOrResumeStatus += 1;

			MoveOnService.pausePractice();
			MoveOnService.setPausedForSavePractice(false);
		}

		private void resumePractice() {
			MoveOnService.setPausedForSavePractice(false);
			MoveOnService.resumePractice();

			pauseresume_text.setText(getString(R.string.play_1));
			stop_text.setText(getString(R.string.stop));

			enablePathColor();

			pauseOrResumeStatus = 1;
			stopStatus = 0;

			chrono.setBase(calculateChronoBaseValue(false));
			chrono.start();
		}
	};

	private void startPractice() {
		MoveOnService.startPractice(SystemClock.elapsedRealtime() + 1000);

		UIFunctionUtils.showMessage(mContext, true, mContext.getString(R.string.practice_started));

		enablePathColor();

		start_button.setVisibility(View.INVISIBLE);
		start_text.setVisibility(View.INVISIBLE);
		pauseresume_button.setVisibility(View.VISIBLE);
		pauseresume_text.setVisibility(View.VISIBLE);
		stop_button.setVisibility(View.VISIBLE);
		stop_text.setText(getString(R.string.stop));
		stop_text.setVisibility(View.VISIBLE);
		pauseresume_text.setText(getString(R.string.play_1));

		pauseOrResumeStatus += 1;
		stopStatus = 0;
		pictureCounter = 1;
		mLatitude = "";
		mLongitude = "";
		chronoBaseValue = 0;

		launchPractice = true;
		hasBeenResumed = false;

		paintedPath.clear();

		chrono.setBase(SystemClock.elapsedRealtime());
		chrono.start();

		showCalculatedData();
	}

	private void enablePathColor() {
		if (!pathOverlayChangeColor) {
			pathOverlay.setColor(Color.BLUE);
			pathOverlayChangeColor = true;
		}
	}

	private void disablePathColor() {
		if (pathOverlayChangeColor) {
			pathOverlay.setColor(Color.GRAY);
			pathOverlayChangeColor = false;
		}
	}

	private BroadcastReceiver mReceiverExpandRestartMapview = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			expand_restart();
		}
	};

	private void expand_restart() {
		if (isExpanded) {
			restartMap();
			full_mapview.setImageResource(R.drawable.full_screen);
			isExpanded = false;
		} else {
			expandMap();
			full_mapview.setImageResource(R.drawable.return_from_full_screen);
			isExpanded = true;
		}
	}

	private void expandMap() {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
		layout1.setLayoutParams(layoutParams);
		layout4.setLayoutParams(layoutParams);
		layout5.setLayoutParams(layoutParams);
		layout2.setLayoutParams(layoutParams);
	}

	private void restartMap() {
		LinearLayout.LayoutParams layoutParams_1, layoutParams_2, layoutParams_3, layoutParams_4;
		if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			layoutParams_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams_1.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_1_left_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_1_top_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_1_right_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_1_bottom_portrait));

			layoutParams_2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					FunctionUtils.calculateDpFromPx(mContext, 195));
			layoutParams_2.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_2_left_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_2_top_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_2_right_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_2_bottom_portrait));

			layoutParams_3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					FunctionUtils.calculateDpFromPx(mContext, 42));
			layoutParams_3.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_4_left_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_4_top_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_4_right_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_4_bottom_portrait));

			layoutParams_4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					FunctionUtils.calculateDpFromPx(mContext, 42));
			layoutParams_4.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_5_left_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_5_top_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_5_right_portrait),
					(int) res.getDimension(R.dimen.practice_linearlayout_5_bottom_portrait));
		} else {
			layoutParams_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT, 1.5f);
			layoutParams_1.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_1_left_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_1_top_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_1_right_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_1_bottom_landscape));

			layoutParams_2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					FunctionUtils.calculateDpFromPx(mContext, 65));
			layoutParams_2.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_2_left_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_2_top_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_2_right_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_2_bottom_landscape));

			layoutParams_3 = new LinearLayout.LayoutParams(FunctionUtils.calculateDpFromPx(mContext, 42),
					LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
			layoutParams_3.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_4_left_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_4_top_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_4_right_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_4_bottom_landscape));

			layoutParams_4 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					FunctionUtils.calculateDpFromPx(mContext, 42), 0.0f);
			layoutParams_4.setMargins((int) res.getDimension(R.dimen.practice_linearlayout_5_left_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_5_top_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_5_right_landscape),
					(int) res.getDimension(R.dimen.practice_linearlayout_5_bottom_landscape));
		}
		layout1.setLayoutParams(layoutParams_1);
		layout2.setLayoutParams(layoutParams_2);
		layout4.setLayoutParams(layoutParams_3);
		layout5.setLayoutParams(layoutParams_4);
	}

	private BroadcastReceiver mReceiverTakePicture = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			path = Environment.getExternalStorageDirectory().toString() + "/moveon/images/";

			String zeros = "";
			if (pictureCounter < 10)
				zeros = "000";
			else if (pictureCounter < 100)
				zeros = "00";
			else if (pictureCounter < 1000)
				zeros = "0";

			int fileNumber = 1;
			if (!DataFunctionUtils.checkInformationInDB(context))
				fileNumber = DataFunctionUtils.getLastRoute(mContext) + 1;

			file = fileNumber + "_" + zeros + pictureCounter + ".jpg";
			FunctionUtils.createDirectory(path);

			File f = new File(path + file);
			if (f.exists())
				f.delete();

			Intent it = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			startActivityForResult(it, ACTIVITY_CAMERA);
		}
	};

	private BroadcastReceiver mReceiverImportDataToast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int option = intent.getIntExtra("toast_option", 1);

			switch (option) {
			case 1:
				UIFunctionUtils
						.showMessage(mContext, false, getString(R.string.backup_restored_successfully));
				break;
			case 2:
				UIFunctionUtils.showMessage(mContext, false,
						getString(R.string.backup_restored_unsuccessfully));
				break;
			}
		}
	};

	private BroadcastReceiver mReceiverFollowLocation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			followLocation = true;
			enableMyLocation(true);
			follow_button.setImageDrawable(res.getDrawable(R.drawable.gps));

			if (mLocationOverlay.getMyLocation() != null)
				centerPoint = new GeoPoint(mLocationOverlay.getMyLocation());
		}
	};

	private BroadcastReceiver mReceiverUnfollowLocation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			followLocation = false;

			if (mLocationOverlay.isFollowLocationEnabled())
				mLocationOverlay.disableFollowLocation();

			follow_button.setImageDrawable(res.getDrawable(R.drawable.gps_unfollow));

			getMapviewCenterPoint();
		}
	};

	private void getMapviewCenterPoint() {
		centerPoint = new GeoPoint(mapView.getMapCenter().getLatitude(), mapView.getMapCenter()
				.getLongitude());
	}

	private BroadcastReceiver mReceiverStopAndSave = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			stopAndSave();
		}
	};

	private BroadcastReceiver mReceiverUpdateTextOfDisplay5 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String label = intent.getStringExtra("label");

			if (label.length() > 0)
				display5_label.setText(label);
		}
	};

	private BroadcastReceiver mReceiverUpdateColorOfDisplay5 = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int color = Integer.parseInt(intent.getStringExtra("color"));
			switch (color) {
			case 0:
				display5_label.setTextColor(res.getColor(R.color.gray));
				break;
			case 1:
				display5_label.setTextColor(res.getColor(R.color.green));
				break;
			case 2:
				display5_label.setTextColor(res.getColor(R.color.yellow));
				break;
			case 3:
				display5_label.setTextColor(res.getColor(R.color.orange));
				break;
			case 4:
				display5_label.setTextColor(res.getColor(R.color.red));
				break;
			case 5:
				display5_label.setTextColor(res.getColor(R.color.blue));
				break;
			case 6:
				display5_label.setTextColor(res.getColor(R.color.white));
				break;
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case Activity.RESULT_OK:
			if (requestCode == ACTIVITY_CAMERA) {
				float angle = MediaFunctionUtils.getCorrectImageAngle(mContext, path, file);

				if (((mLatitude != null) && (mLatitude.length() > 0))
						&& ((mLongitude != null) & (mLongitude.length() > 0))) {
					MediaFunctionUtils.imageProcessing(mContext, angle, path, file, mLatitude, mLongitude);
				} else {
					MediaFunctionUtils.imageProcessing(mContext, angle, path, file);
				}

				pictureCounter++;
			}
			break;
		}
	}

	private void checkCoachStatus() {
		if (prefs.getBoolean("speak", false)) {
			coach_button.setImageDrawable(res.getDrawable(R.drawable.coach));
		} else {
			coach_button.setImageDrawable(res.getDrawable(R.drawable.coach_muted));
		}
	}

	private void checkLockedStatus() {
		if (prefs.getBoolean("blocked", false)) {
			locked_button.startAnimation(animationFadeIn);
			locked_button.setImageDrawable(res.getDrawable(R.drawable.padlock_closed));
		} else {
			locked_button.setAnimation(null);
			locked_button.setImageDrawable(res.getDrawable(R.drawable.padlock));
		}
	}

	@Override
	public void onDestroy() {
		disableMyLocation();

		releaseLocationOverlay();

		if (MoveOnService.getIsPracticeRunning() && act.isFinishing())
			MoveOnService.stopPractice();

		super.onDestroy();
	}

	private void releaseLocationOverlay() {
		if (mLocationOverlay != null) {
			mapView.getOverlays().remove(mLocationOverlay);
			mLocationOverlay = null;
		}
	}

	public void onResume() {
		super.onResume();

		cleanMapViewCache();

		getTileSource(Integer.valueOf(prefs.getString("map_tile_type", "0")));
		mapController.setZoom(zoom);

		isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		TextToSpeechUtils.setSpeak(prefs.getBoolean("speak", true));

		if (!MoveOnService.getIsPracticeRunning()) {
			defineWidgets(fragmentView);
			putWidgetsOnInitStatus();

			if (launchPractice)
				startPractice();
		} else {
			hasBeenResumed = true;

			paintedPath.clear();
			ArrayList<EntityLocations> locationList = MoveOnService.getLocationsList();
			for (int i = 0; i <= locationList.size() - 1; i++) {
				EntityLocations mLocation = (EntityLocations) locationList.get(i);
				paintedPath.add(new GeoPoint(Double.valueOf(mLocation.getLatitude()), Double
						.valueOf(mLocation.getLongitude())));
			}

			putWidgetsOnRunningStatus();
			loadDisplays();
			showData();

			chrono.setBase(calculateChronoBaseValue(true));

			if (pauseOrResumeStatus == 2) {
				chrono.stop();
			} else {
				showCalculatedData();
				chrono.start();
			}
		}

		mCompassOverlay.enableCompass();
		enableMyLocation(followLocation);
		checkCoachStatus();
		checkLockedStatus();
		gpsStatus = String.valueOf(MoveOnService.getGpsStatus());
		changeStatus(Integer.parseInt(gpsStatus));
		changeAccuracyText();

		if (isExpanded) {
			expandMap();
			full_mapview.setImageResource(R.drawable.return_from_full_screen);
		} else {
			restartMap();
			full_mapview.setImageResource(R.drawable.full_screen);
		}

		if (!followLocation)
			follow_button.setImageDrawable(res.getDrawable(R.drawable.gps_unfollow));
		else
			follow_button.setImageDrawable(res.getDrawable(R.drawable.gps));

		intentFilter_GPS_Status = new IntentFilter("android.intent.action.GPS_STATUS");
		intentFilter_GPS_Accuracy = new IntentFilter("android.intent.action.GPS_ACCURACY");
		intentFilter_GPS_Data = new IntentFilter("android.intent.action.GPS_DATA");
		intentFilter_GPS_Location = new IntentFilter("android.intent.action.GPS_LOCATION");
		intentFilter_Practice_Status = new IntentFilter("android.intent.action.PRACTICE_STATUS");
		intentFilter_Buttons_Status = new IntentFilter("android.intent.action.PRACTICE_BUTTONS_STATUS");
		intentFilter_Step_Counter = new IntentFilter("android.intent.action.STEP_COUNTER");
		intentFilter_Take_Picture = new IntentFilter("android.intent.action.TAKE_PICTURE");
		intentFilter_Beat_Counter = new IntentFilter("android.intent.action.BEAT_COUNTER");
		intentFilter_Import_Data_Toast = new IntentFilter("android.intent.action.IMPORT_DATA_TOAST");
		intentFilter_Follow_Location = new IntentFilter("android.intent.action.FOLLOW_LOCATION");
		intentFilter_Unfollow_Location = new IntentFilter("android.intent.action.UNFOLLOW_LOCATION");
		intentFilter_Stop_And_Save = new IntentFilter("android.intent.action.STOP_AND_SAVE");
		intentFilter_Update_Text_Of_Display5 = new IntentFilter(
				"android.intent.action.UPDATE_TEXT_OF_DISPLAY5");
		intentFilter_Update_Color_Of_Display5 = new IntentFilter(
				"android.intent.action.UPDATE_COLOR_OF_DISPLAY5");
		intentFilter_Cadence = new IntentFilter("android.intent.action.CADENCE");
		intentFilter_Expand_Restart_Mapview = new IntentFilter(
				"android.intent.action.EXPAND_OR_RESTART_PRACTICE_MAPVIEW");

		act.registerReceiver(mReceiverGpsStatus, intentFilter_GPS_Status);
		act.registerReceiver(mReceiverGpsAccuracy, intentFilter_GPS_Accuracy);
		act.registerReceiver(mReceiverGpsData, intentFilter_GPS_Data);
		act.registerReceiver(mReceiverGpsLocation, intentFilter_GPS_Location);
		act.registerReceiver(mReceiverPracticeStatus, intentFilter_Practice_Status);
		act.registerReceiver(mReceiverButtonsStatus, intentFilter_Buttons_Status);
		act.registerReceiver(mReceiverStepCounter, intentFilter_Step_Counter);
		act.registerReceiver(mReceiverBeatCounter, intentFilter_Beat_Counter);
		act.registerReceiver(mReceiverTakePicture, intentFilter_Take_Picture);
		act.registerReceiver(mReceiverImportDataToast, intentFilter_Import_Data_Toast);
		act.registerReceiver(mReceiverFollowLocation, intentFilter_Follow_Location);
		act.registerReceiver(mReceiverUnfollowLocation, intentFilter_Unfollow_Location);
		act.registerReceiver(mReceiverStopAndSave, intentFilter_Stop_And_Save);
		act.registerReceiver(mReceiverUpdateTextOfDisplay5, intentFilter_Update_Text_Of_Display5);
		act.registerReceiver(mReceiverUpdateColorOfDisplay5, intentFilter_Update_Color_Of_Display5);
		act.registerReceiver(mReceiverCadence, intentFilter_Cadence);
		act.registerReceiver(mReceiverExpandRestartMapview, intentFilter_Expand_Restart_Mapview);
	}

	private void getTileSource(int type) {
		switch (type) {
		case 0:
			mapView.setTileSource(TileSourceFactory.MAPNIK);
			break;
		case 1:
			mapView.setTileSource(TileSourceFactory.CYCLEMAP);
			break;
		case 2:
			mapView.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
			break;
		case 3:
			mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
			break;
		case 4:
			mapView.setTileSource(TileSourceFactory.MAPQUESTAERIAL);
			break;
		}
	}

	@Override
	public void onPause() {
		if (chrono.isActivated())
			chrono.stop();

		chronoBaseValue = chrono.getBase();

		super.onPause();

		zoom = mapView.getZoomLevel();

		locked_button.setAnimation(null);

		mCompassOverlay.disableCompass();
		disableMyLocation();

		act.unregisterReceiver(mReceiverGpsStatus);
		act.unregisterReceiver(mReceiverGpsAccuracy);
		act.unregisterReceiver(mReceiverGpsData);
		act.unregisterReceiver(mReceiverGpsLocation);
		act.unregisterReceiver(mReceiverButtonsStatus);
		act.unregisterReceiver(mReceiverPracticeStatus);
		act.unregisterReceiver(mReceiverStepCounter);
		act.unregisterReceiver(mReceiverBeatCounter);
		act.unregisterReceiver(mReceiverExpandRestartMapview);
		act.unregisterReceiver(mReceiverTakePicture);
		act.unregisterReceiver(mReceiverImportDataToast);
		act.unregisterReceiver(mReceiverFollowLocation);
		act.unregisterReceiver(mReceiverUnfollowLocation);
		act.unregisterReceiver(mReceiverStopAndSave);
		act.unregisterReceiver(mReceiverUpdateTextOfDisplay5);
		act.unregisterReceiver(mReceiverUpdateColorOfDisplay5);
		act.unregisterReceiver(mReceiverCadence);

		cleanMapViewCache();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLong("chronoBase", chronoBaseValue);
		savedInstanceState.putInt("stopStatus", stopStatus);
		savedInstanceState.putInt("pauseOrResumeStatus", pauseOrResumeStatus);
		savedInstanceState.putInt("activity", activity);
		savedInstanceState.putInt("activityCamera", ACTIVITY_CAMERA);
		savedInstanceState.putInt("pictureCounter", pictureCounter);
		savedInstanceState.putInt("zoom", mapView.getZoomLevel());
		savedInstanceState.putString("gpsStatus", gpsStatus);
		savedInstanceState.putString("gpsAccuracy", gpsAccuracy);
		savedInstanceState.putString("distance", mDistance);
		savedInstanceState.putString("speed", mSpeed);
		savedInstanceState.putString("maxSpeed", mMaxSpeed);
		savedInstanceState.putString("latitude", mLatitude);
		savedInstanceState.putString("longitude", mLongitude);
		savedInstanceState.putString("altitude", mAltitude);
		savedInstanceState.putString("steps", mSteps);
		savedInstanceState.putString("hr", mHeartRate);
		savedInstanceState.putString("cadence", mCadence);
		savedInstanceState.putString("path", path);
		savedInstanceState.putString("file", file);
		savedInstanceState.putBoolean("pathOverlayChangeColor", pathOverlayChangeColor);
		savedInstanceState.putBoolean("launchPractice", launchPractice);
		savedInstanceState.putBoolean("isExpanded", isExpanded);
		savedInstanceState.putBoolean("followLocation", followLocation);
		savedInstanceState.putBoolean("hasBeenResumed", hasBeenResumed);
		savedInstanceState.putBoolean("isMetric", isMetric);
		if (centerPoint != null) {
			savedInstanceState.putDouble("latCenterPoint", centerPoint.getLatitude());
			savedInstanceState.putDouble("lonCenterPoint", centerPoint.getLongitude());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		act = getActivity();
		mContext = act.getApplicationContext();
		res = mContext.getResources();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		mDistance = mContext.getString(R.string.zero_with_two_decimal_places_value);
		launchPractice = false;
		paintedPath = new ArrayList<GeoPoint>();
		centerPoint = null;

		if (savedInstanceState != null) {
			chronoBaseValue = savedInstanceState.getLong("chronoBase");
			stopStatus = savedInstanceState.getInt("stopStatus");
			pauseOrResumeStatus = savedInstanceState.getInt("pauseOrResumeStatus");
			activity = savedInstanceState.getInt("activity");
			ACTIVITY_CAMERA = savedInstanceState.getInt("activityCamera");
			pictureCounter = savedInstanceState.getInt("pictureCounter");
			zoom = savedInstanceState.getInt("zoom");
			gpsStatus = savedInstanceState.getString("gpsStatus");
			gpsAccuracy = savedInstanceState.getString("gpsAccuracy");
			mDistance = savedInstanceState.getString("distance");
			mSpeed = savedInstanceState.getString("speed");
			mMaxSpeed = savedInstanceState.getString("maxSpeed");
			mLatitude = savedInstanceState.getString("latitude");
			mLongitude = savedInstanceState.getString("longitude");
			mAltitude = savedInstanceState.getString("altitude");
			mSteps = savedInstanceState.getString("steps");
			mHeartRate = savedInstanceState.getString("hr");
			mCadence = savedInstanceState.getString("cadence");
			path = savedInstanceState.getString("path");
			file = savedInstanceState.getString("file");
			pathOverlayChangeColor = savedInstanceState.getBoolean("pathOverlayChangeColor");
			launchPractice = savedInstanceState.getBoolean("launchPractice");
			isExpanded = savedInstanceState.getBoolean("isExpanded");
			followLocation = savedInstanceState.getBoolean("followLocation");
			hasBeenResumed = savedInstanceState.getBoolean("hasBeenResumed");
			isMetric = savedInstanceState.getBoolean("isMetric");
			latCenterPoint = savedInstanceState.getDouble("latCenterPoint");
			lonCenterPoint = savedInstanceState.getDouble("lonCenterPoint");

			centerPoint = new GeoPoint(latCenterPoint, lonCenterPoint);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = (LinearLayout) inflater.inflate(R.layout.main, container, false);

		layout1 = (LinearLayout) fragmentView.findViewById(R.id.linearLayout1);
		layout2 = (LinearLayout) fragmentView.findViewById(R.id.linearLayout2);
		layout4 = (LinearLayout) fragmentView.findViewById(R.id.linearLayout4);
		layout5 = (LinearLayout) fragmentView.findViewById(R.id.linearLayout5);

		defineWidgets(fragmentView);

		if (savedInstanceState != null)
			chrono.setBase(chronoBaseValue);

		chrono.setOnChronometerTickListener(new OnChronometerTickListener() {
			public void onChronometerTick(Chronometer arg0) {
				showCalculatedData(arg0);

				if (prefs.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes()) == TypesOfPractices.HIIT_PRACTICE
						.getTypes())
					showHiitTrainingDataInDisplay(arg0);
			}
		});

		mapView = (MapView) fragmentView.findViewById(R.id.practice_mapview);
		mapView.getOverlays().clear();
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);
		mapView.setUseSafeCanvas(true);

		setHardwareAccelerationOff();

		mapController = (MapController) mapView.getController();

		ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mContext);
		mapView.getOverlays().add(mScaleBarOverlay);

		pathOverlay = new PathOverlay(Color.BLUE, mContext);
		pathOverlay.getPaint().setStyle(Style.STROKE);
		pathOverlay.getPaint().setStrokeWidth(3);
		pathOverlay.getPaint().setAntiAlias(true);
		mapView.getOverlays().add(pathOverlay);

		this.mCompassOverlay = new CompassOverlay(mContext, new InternalCompassOrientationProvider(mContext),
				mapView);
		mCompassOverlay.setEnabled(true);
		mapView.getOverlays().add(mCompassOverlay);

		this.mLocationOverlay = new MyLocationNewOverlay(mContext, new GpsMyLocationProvider(mContext),
				mapView);
		mLocationOverlay.setDrawAccuracyEnabled(true);
		mapView.getOverlays().add(mLocationOverlay);

		MapOverlay touchOverlay = new MapOverlay(mContext);
		mapView.getOverlays().add(touchOverlay);

		mLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				if (centerPoint == null)
					centerPoint = mLocationOverlay.getMyLocation();

				centerMyLocation(centerPoint);
			}
		});

		mapView.postInvalidate();

		return fragmentView;
	}

	private void cleanMapViewCache() {
		mapView.getTileProvider().clearTileCache();
		System.gc();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setHardwareAccelerationOff() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	private void defineWidgets(LinearLayout fragmentView) {
		chrono = (Chronometer) fragmentView.findViewById(R.id.chrono);
		display1_icon = (ImageView) fragmentView.findViewById(R.id.practice_display1_icon);
		display1_text1 = (TextView) fragmentView.findViewById(R.id.practice_display1_item_one);
		display1_text2 = (TextView) fragmentView.findViewById(R.id.practice_display1_item_two);
		display1_label = (TextView) fragmentView.findViewById(R.id.practice_display1_item_three);
		display2_icon = (ImageView) fragmentView.findViewById(R.id.practice_display2_icon);
		display2_text1 = (TextView) fragmentView.findViewById(R.id.practice_display2_item_one);
		display2_text2 = (TextView) fragmentView.findViewById(R.id.practice_display2_item_two);
		display2_label = (TextView) fragmentView.findViewById(R.id.practice_display2_item_three);
		display3_icon = (ImageView) fragmentView.findViewById(R.id.practice_display3_icon);
		display3_text1 = (TextView) fragmentView.findViewById(R.id.practice_display3_item_one);
		display3_text2 = (TextView) fragmentView.findViewById(R.id.practice_display3_item_two);
		display3_label = (TextView) fragmentView.findViewById(R.id.practice_display3_item_three);
		display4_icon = (ImageView) fragmentView.findViewById(R.id.practice_display4_icon);
		display4_text1 = (TextView) fragmentView.findViewById(R.id.practice_display4_item_one);
		display4_text2 = (TextView) fragmentView.findViewById(R.id.practice_display4_item_two);
		display4_label = (TextView) fragmentView.findViewById(R.id.practice_display4_item_three);
		display5_label = (TextView) fragmentView.findViewById(R.id.practice_display5_item_one);
		display5_text1 = (TextView) fragmentView.findViewById(R.id.practice_display5_item_two);
		display5_image = (ImageView) fragmentView.findViewById(R.id.practice_display5_item_three);
		display6_text1 = (TextView) fragmentView.findViewById(R.id.practice_display6_item_one);
		display6_image = (ImageView) fragmentView.findViewById(R.id.practice_display6_item_three);
		start_button = (ImageView) fragmentView.findViewById(R.id.practice_start_image);
		pauseresume_button = (ImageView) fragmentView.findViewById(R.id.practice_pauseresume_image);
		stop_button = (ImageView) fragmentView.findViewById(R.id.practice_stop_image);
		start_text = (TextView) fragmentView.findViewById(R.id.practice_start_item);
		pauseresume_text = (TextView) fragmentView.findViewById(R.id.practice_pauseresume_item);
		stop_text = (TextView) fragmentView.findViewById(R.id.practice_stop_item);
		coach_button = (ImageView) fragmentView.findViewById(R.id.main_action_bar_item_one);
		follow_button = (ImageView) fragmentView.findViewById(R.id.practice_mapview_followlocation);
		locked_button = (ImageView) fragmentView.findViewById(R.id.main_action_bar_item_two);
		full_mapview = (ImageButton) fragmentView.findViewById(R.id.practice_mapview_fullscreen);

		animationFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fadein_slow);
		animationFadeOut = AnimationUtils.loadAnimation(mContext, R.anim.fadeout_slow);
		animationFadeIn.setAnimationListener(animationInListener);
		animationFadeOut.setAnimationListener(animationOutListener);

		loadDisplays();
	}

	AnimationListener animationInListener = new AnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			locked_button.startAnimation(animationFadeOut);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	};

	AnimationListener animationOutListener = new AnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			locked_button.startAnimation(animationFadeIn);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}
	};

	private void putWidgetsOnInitStatus() {
		pathOverlay.clearPath();
		pauseOrResumeStatus = 0;
		mSpeed = getString(R.string.zero_with_one_decimal_place_value);
		mMaxSpeed = getString(R.string.zero_with_one_decimal_place_value);
		mDistance = getString(R.string.zero_with_two_decimal_places_value);
		mAltitude = getString(R.string.zero_value);
		mSteps = getString(R.string.zero_value);
		mHeartRate = getString(R.string.zero_value);
		mCadence = getString(R.string.zero_value);
		showData();
		showCalculatedData();

		start_button.setVisibility(View.VISIBLE);
		start_text.setVisibility(View.VISIBLE);
		pauseresume_button.setVisibility(View.INVISIBLE);
		pauseresume_text.setVisibility(View.INVISIBLE);
		stop_button.setVisibility(View.INVISIBLE);
		stop_text.setVisibility(View.INVISIBLE);

		display5_label.setTextColor(res.getColor(R.color.white));
	}

	private void putWidgetsOnRunningStatus() {
		if (MoveOnService.getIsPracticePaused()) {
			pauseOrResumeStatus = 2;
			stopStatus = 1;
		} else {
			pauseOrResumeStatus = 1;
			stopStatus = 0;
		}

		start_button.setVisibility(View.INVISIBLE);
		start_text.setVisibility(View.INVISIBLE);
		pauseresume_button.setVisibility(View.VISIBLE);
		pauseresume_text.setVisibility(View.VISIBLE);
		stop_button.setVisibility(View.VISIBLE);
		stop_text.setVisibility(View.VISIBLE);

		pathOverlay.clearPath();
		for (int i = 0; i < paintedPath.size(); i++) {
			pathOverlay.addPoint(paintedPath.get(i));
		}

		if (pauseOrResumeStatus == 1) {
			pauseresume_text.setText(getString(R.string.play_1));
			enablePathColor();
			stop_text.setText(getString(R.string.stop));
		} else {
			pauseresume_text.setText(getString(R.string.play_2));
			pathOverlayChangeColor = true;
			disablePathColor();
			stop_text.setText(getString(R.string.save));
		}
	}

	@SuppressLint("Recycle")
	private void loadDisplays() {
		activity = prefs.getInt("last_activity", 1);

		TypedArray activities_icons = res.obtainTypedArray(R.array.activities_icons);
		TypedArray display_type_icons = res.obtainTypedArray(R.array.display_type_icons);

		String[] activities = res.getStringArray(R.array.activities);
		String[] values_display1, values_display2, values_display3, values_display4 = new String[3];
		String[] values_display5 = new String[2];

		int UI_Practice_Display1 = prefs.getInt("practice_display1", DisplayTypes.TIME.getTypes());
		int UI_Practice_Display2 = prefs.getInt("practice_display2", DisplayTypes.DISTANCE.getTypes());
		int UI_Practice_Display3 = prefs.getInt("practice_display3", DisplayTypes.RITM.getTypes());
		int UI_Practice_Display4 = prefs.getInt("practice_display4", DisplayTypes.KCAL.getTypes());

		values_display1 = getDisplaysValues(UI_Practice_Display1);
		if (!MoveOnService.getIsPracticeRunning())
			display1_text1.setText(values_display1[0]);
		display1_text2.setText(values_display1[1]);
		display1_label.setText(values_display1[2].toUpperCase(Locale.getDefault()));
		display1_icon.setImageDrawable(display_type_icons.getDrawable(UI_Practice_Display1));

		values_display2 = getDisplaysValues(UI_Practice_Display2);
		if (!MoveOnService.getIsPracticeRunning())
			display2_text1.setText(values_display2[0]);
		display2_text2.setText(values_display2[1]);
		display2_label.setText(values_display2[2].toUpperCase(Locale.getDefault()));
		display2_icon.setImageDrawable(display_type_icons.getDrawable(UI_Practice_Display2));

		values_display3 = getDisplaysValues(UI_Practice_Display3);
		if (!MoveOnService.getIsPracticeRunning())
			display3_text1.setText(values_display3[0]);
		display3_text2.setText(values_display3[1]);
		display3_label.setText(values_display3[2].toUpperCase(Locale.getDefault()));
		display3_icon.setImageDrawable(display_type_icons.getDrawable(UI_Practice_Display3));

		values_display4 = getDisplaysValues(UI_Practice_Display4);
		if (!MoveOnService.getIsPracticeRunning())
			display4_text1.setText(values_display4[0]);
		display4_text2.setText(values_display4[1]);
		display4_label.setText(values_display4[2].toUpperCase(Locale.getDefault()));

		values_display5 = getDisplaysValues(11);
		showObjetiveData(display5_image, display5_label, display5_text1, values_display5[0],
				values_display5[1]);

		display6_text1.setText(activities[activity - 1].toUpperCase(Locale.getDefault()));
		display6_image.setImageDrawable(activities_icons.getDrawable(activity - 1));
		display4_icon.setImageDrawable(display_type_icons.getDrawable(UI_Practice_Display4));
	}

	private String[] getDisplaysValues(int d) {
		String[] values = new String[3];
		String unit_text1, unit_text2, unit_text3, unit_text4;

		unit_text1 = (isMetric ? getString(R.string.long_unit1_detail_1).toUpperCase(Locale.getDefault())
				: getString(R.string.long_unit2_detail_1).toUpperCase(Locale.getDefault()));
		unit_text2 = (isMetric ? getString(R.string.long_unit1_detail_2).toUpperCase(Locale.getDefault())
				: getString(R.string.long_unit2_detail_2).toUpperCase(Locale.getDefault()));
		unit_text3 = (isMetric ? getString(R.string.long_unit1_detail_3).toUpperCase(Locale.getDefault())
				: getString(R.string.long_unit2_detail_3).toUpperCase(Locale.getDefault()));
		unit_text4 = (isMetric ? getString(R.string.long_unit1_detail_4).toUpperCase(Locale.getDefault())
				: getString(R.string.long_unit2_detail_4).toUpperCase(Locale.getDefault()));

		DisplayTypes whichDisplay = DisplayTypes.values()[d];
		switch (whichDisplay) {
		case TIME:
			values[0] = getString(R.string.time_value);
			values[1] = "";
			values[2] = getString(R.string.time_label);
			break;
		case DISTANCE:
			values[0] = getString(R.string.zero_with_two_decimal_places_value);
			values[1] = unit_text1;
			values[2] = getString(R.string.distance_label);
			break;
		case SPEED:
			values[0] = getString(R.string.zero_with_one_decimal_place_value);
			values[1] = unit_text2;
			values[2] = getString(R.string.speed_label);
			break;
		case AVG_SPEED:
			values[0] = getString(R.string.zero_with_one_decimal_place_value);
			values[1] = unit_text2;
			values[2] = getString(R.string.avg_label);
			break;
		case MAX_SPEED:
			values[0] = getString(R.string.zero_with_one_decimal_place_value);
			values[1] = unit_text2;
			values[2] = getString(R.string.max_speed_label);
			break;
		case RITM:
			values[0] = getString(R.string.ritm_value);
			values[1] = unit_text3;
			values[2] = getString(R.string.ritm_label);
			break;
		case ALTITUDE:
			values[0] = getString(R.string.zero_value);
			values[1] = unit_text4;
			values[2] = getString(R.string.altitude_label);
			break;
		case KCAL:
			values[0] = getString(R.string.zero_value);
			values[1] = "KCAL";
			values[2] = getString(R.string.calories_label);
			break;
		case STEPS:
			values[0] = getString(R.string.zero_value);
			values[1] = "";
			values[2] = getString(R.string.steps_label);
			break;
		case BEATS:
			values[0] = getString(R.string.zero_value);
			values[1] = "PPM";
			values[2] = getString(R.string.beats_label);
			break;
		case CADENCE:
			values[0] = getString(R.string.zero_value);
			values[1] = "RPM";
			values[2] = getString(R.string.cadence_label);
			break;
		case OBJETIVES:
			TypesOfPractices whichType = TypesOfPractices.values()[prefs.getInt("practice_display5",
					TypesOfPractices.BASIC_PRACTICE.getTypes())];
			switch (whichType) {
			case BASIC_PRACTICE:
				values[0] = getString(R.string.basic_practice);
				values[1] = getString(R.string.objetive_type);
				break;
			case HIIT_PRACTICE:
				loadHiitPresset(prefs.getInt("selected_hiit", 1));

				values[0] = hiit_data[1] + " " + getString(R.string.round_short_text) + ", " + hiit_data[4]
						+ " " + getString(R.string.interval_short_text) + ", "
						+ getString(R.string.time_uppercase).toLowerCase(Locale.getDefault()) + " ";
				long prep_time = Long.parseLong(hiit_data[0]);
				if (prep_time < 3600) {
					values[0] = values[0] + FunctionUtils.shortFormatTime(prep_time);
				} else {
					values[0] = values[0] + FunctionUtils.longFormatTime(prep_time);
				}

				values[1] = hiit_data[5];
				break;
			default:
				break;
			}
			break;
		}

		return values;
	}

	private void showData() {
		int UI_Practice_Display1 = prefs.getInt("practice_display1", DisplayTypes.TIME.getTypes());
		showData(display1_text1, UI_Practice_Display1);
		int UI_Practice_Display2 = prefs.getInt("practice_display2", DisplayTypes.DISTANCE.getTypes());
		showData(display2_text1, UI_Practice_Display2);
		int UI_Practice_Display3 = prefs.getInt("practice_display3", DisplayTypes.RITM.getTypes());
		showData(display3_text1, UI_Practice_Display3);
		int UI_Practice_Display4 = prefs.getInt("practice_display4", DisplayTypes.KCAL.getTypes());
		showData(display4_text1, UI_Practice_Display4);
	}

	/**
	 * Show information that is not calculated (anything related with the
	 * elapsed time)
	 */
	private void showData(TextView tv, int option) {
		DisplayTypes whichDisplay = DisplayTypes.values()[option];
		switch (whichDisplay) {
		case DISTANCE:
			tv.setText(isMetric ? mDistance : FunctionUtils.formatFloatToDecimalFormatString((Float
					.valueOf(mDistance) * 1000f) / 1609f));
			break;
		case SPEED:
			tv.setText(isMetric ? mSpeed : FunctionUtils.formatFloatToDecimalFormatString((Float
					.valueOf(mSpeed) * 1000f) / 1609f));
			break;
		case MAX_SPEED:
			tv.setText(isMetric ? mMaxSpeed : FunctionUtils.formatFloatToDecimalFormatString((Float
					.valueOf(mMaxSpeed) * 1000f) / 1609f));
			break;
		case ALTITUDE:
			tv.setText(isMetric ? mAltitude : String.valueOf((int) (Float.parseFloat(mAltitude) * 1.0936f)));
			break;
		case STEPS:
			tv.setText(mSteps);
			break;
		case BEATS:
			tv.setText(mHeartRate);
			break;
		case CADENCE:
			tv.setText(mCadence);
			break;
		default:
			break;
		}
	}

	private void showCalculatedData() {
		int UI_Practice_Display1 = prefs.getInt("practice_display1", DisplayTypes.TIME.getTypes());
		showCalculatedData(display1_text1, UI_Practice_Display1);
		int UI_Practice_Display2 = prefs.getInt("practice_display2", DisplayTypes.DISTANCE.getTypes());
		showCalculatedData(display2_text1, UI_Practice_Display2);
		int UI_Practice_Display3 = prefs.getInt("practice_display3", DisplayTypes.RITM.getTypes());
		showCalculatedData(display3_text1, UI_Practice_Display3);
		int UI_Practice_Display4 = prefs.getInt("practice_display4", DisplayTypes.KCAL.getTypes());
		showCalculatedData(display4_text1, UI_Practice_Display4);
	}

	private void showCalculatedData(Chronometer arg0) {
		int UI_Practice_Display1 = prefs.getInt("practice_display1", DisplayTypes.TIME.getTypes());
		showCalculatedData(display1_text1, UI_Practice_Display1, arg0);
		int UI_Practice_Display2 = prefs.getInt("practice_display2", DisplayTypes.DISTANCE.getTypes());
		showCalculatedData(display2_text1, UI_Practice_Display2, arg0);
		int UI_Practice_Display3 = prefs.getInt("practice_display3", DisplayTypes.RITM.getTypes());
		showCalculatedData(display3_text1, UI_Practice_Display3, arg0);
		int UI_Practice_Display4 = prefs.getInt("practice_display4", DisplayTypes.KCAL.getTypes());
		showCalculatedData(display4_text1, UI_Practice_Display4, arg0);
	}

	private void showCalculatedData(TextView tv, int option) {
		DisplayTypes whichDisplay = DisplayTypes.values()[option];
		switch (whichDisplay) {
		case TIME:
			tv.setText(getString(R.string.time_value));
			break;
		case AVG_SPEED:
			tv.setText(getString(R.string.zero_with_one_decimal_place_value));
			break;
		case RITM:
			tv.setText(getString(R.string.ritm_value));
			break;
		case KCAL:
			tv.setText(getString(R.string.zero_value));
			break;
		default:
			break;
		}
	}

	private void showCalculatedData(TextView tv, int option, Chronometer arg0) {
		DisplayTypes whichDisplay = DisplayTypes.values()[option];
		switch (whichDisplay) {
		case TIME:
			tv.setText(FunctionUtils.longFormatTime((SystemClock.elapsedRealtime() - arg0.getBase()) / 1000));
			break;
		case AVG_SPEED:
			tv.setText(FunctionUtils.calculateAvg(mContext,
					((SystemClock.elapsedRealtime() - arg0.getBase()) / 1000), mDistance, isMetric));
			break;
		case RITM:
			tv.setText(FunctionUtils.calculateRitm(mContext,
					((SystemClock.elapsedRealtime() - arg0.getBase()) / 1000), mDistance, isMetric, false));
			break;
		case KCAL:
			tv.setText(String.valueOf(MoveOnService.getCalories()));
			break;
		default:
			break;
		}
	}

	/**
	 * Show only objetive training data
	 */
	private void showObjetiveData(ImageView img, TextView tv1, TextView tv2, String txt1, String txt2) {
		tv1.setText(txt1.toUpperCase(Locale.getDefault()));
		tv2.setText(txt2.toUpperCase(Locale.getDefault()));

		TypesOfPractices whichType = TypesOfPractices.values()[prefs.getInt("practice_display5",
				TypesOfPractices.BASIC_PRACTICE.getTypes())];
		switch (whichType) {
		case BASIC_PRACTICE:
			img.setImageDrawable(res.getDrawable(R.drawable.basic_practice));
			break;
		case HIIT_PRACTICE:
			img.setImageDrawable(res.getDrawable(R.drawable.intervals));
			break;
		default:
			break;
		}
	}

	private void loadHiitPresset(int _id) {
		hiit_data = DataFunctionUtils.getHiitData(mContext, _id);
		hiit_intervals = DataFunctionUtils.getHiitIntervalsData(mContext, _id);

		hiit_rounds = Integer.parseInt(hiit_data[1]);
		hiit_prepSeconds = Integer.parseInt(hiit_data[2]);
		hiit_coolDownSeconds = Integer.parseInt(hiit_data[3]);
		hiit_name = hiit_data[5];

		hiit_actionSeconds = 0;
		for (int i = 0; i <= (hiit_intervals.size() - 1); i++) {
			hiit_actionSeconds = hiit_actionSeconds + hiit_intervals.get(i)[1];
		}

		hiit_passedRounds = 1;
		hiit_total_time = Integer.parseInt(hiit_data[0]);
	}

	private void stopAndSave() {
		if (MoveOnService.getIsPracticeRunning()) {
			MoveOnService.stopPractice();
			chrono.stop();
			launchPractice = false;

			startActivity(new Intent(mContext, PracticeDetails.class));
		}
	}

	private void enableMyLocation(boolean follow) {
		if (!mLocationOverlay.isMyLocationEnabled())
			mLocationOverlay.enableMyLocation();

		if (!mLocationOverlay.isFollowLocationEnabled() && follow)
			mLocationOverlay.enableFollowLocation();
	}

	private void disableMyLocation() {
		if (mLocationOverlay.isMyLocationEnabled())
			mLocationOverlay.disableMyLocation();

		if (mLocationOverlay.isFollowLocationEnabled())
			mLocationOverlay.disableFollowLocation();
	}

	private void centerMyLocation(GeoPoint center) {
		mapController.animateTo(center);
	}

	private long calculateChronoBaseValue(boolean callFromResume) {
		if ((Integer.valueOf(prefs.getString("auto_pause", "0")) > 0)
				&& (MoveOnService.getLengthChrono() > 0)) { // auto-Pause
															// enabled
															// and
															// practice
															// has
															// been
															// paused
															// at
															// least
															// once
			if (!MoveOnService.getIsPracticePaused()) {
				chronoBaseValue = (SystemClock.elapsedRealtime() - ((SystemClock.elapsedRealtime() - MoveOnService
						.getTime()) + (MoveOnService.getAccumulatedTime() * 1000)));
			} else {
				chronoBaseValue = (SystemClock.elapsedRealtime() - (((((MoveOnService.getAccumulatedTime() * 1000) + MoveOnService
						.getLengthChrono()) - MoveOnService.getTime()) / 1000) * 1000));
			}
		} else {
			if (MoveOnService.getIsPracticePaused() && callFromResume) {
				chronoBaseValue = (SystemClock.elapsedRealtime() - (((((MoveOnService.getAccumulatedTime() * 1000) + MoveOnService
						.getLengthChrono()) - MoveOnService.getTime()) / 1000) * 1000));
			} else if (MoveOnService.getIsPracticePaused() || !hasBeenResumed) {
				chronoBaseValue = (SystemClock.elapsedRealtime() - ((SystemClock.elapsedRealtime() - MoveOnService
						.getTime()) + (MoveOnService.getAccumulatedTime() * 1000)));
			} else {
				hasBeenResumed = false;
				chronoBaseValue = chrono.getBase();
			}
		}

		return chronoBaseValue;
	}

	private void showHiitTrainingDataInDisplay(Chronometer arg0) {
		int interval_spent_time = (int) (SystemClock.elapsedRealtime() - arg0.getBase()) / 1000;

		if (interval_spent_time < hiit_total_time) {
			if (hiit_passedRounds == 1) {
				hiit_actionSecondsByRound = hiit_prepSeconds + hiit_actionSeconds;

				hiit_lastRoundsSeconds = hiit_prepSeconds;
			} else {
				hiit_actionSecondsByRound = hiit_prepSeconds + (hiit_actionSeconds * (hiit_passedRounds - 1))
						+ hiit_actionSeconds;

				hiit_lastRoundsSeconds = hiit_prepSeconds + (hiit_actionSeconds * (hiit_passedRounds - 1));
			}

			if ((interval_spent_time <= hiit_prepSeconds) && (hiit_prepSeconds > 0)) {
				display5_label.setTextColor(res.getColor(R.color.gray));

				int countDownTime = hiit_prepSeconds - interval_spent_time;

				hiit_information = getString(R.string.warm_up_description) + " "
						+ FunctionUtils.shortFormatTime(countDownTime);
				hiit_label = hiit_name;
			} else if ((interval_spent_time <= hiit_actionSecondsByRound)
					&& (interval_spent_time < (hiit_total_time - hiit_coolDownSeconds))) {
				int action = hiit_intervals.size(), accum_intervals = 0;
				int[] accum_intervals_array = new int[hiit_intervals.size()];
				for (int m = 0; m <= (hiit_intervals.size() - 1); m++) {
					accum_intervals = accum_intervals + hiit_intervals.get(m)[1];
					accum_intervals_array[m] = accum_intervals;

					if (interval_spent_time <= (hiit_lastRoundsSeconds + accum_intervals)) {
						action -= 1;
					}
				}

				hiit_information = "";
				switch (hiit_intervals.get(action)[0]) {
				case 1:
					display5_label.setTextColor(res.getColor(R.color.green));

					hiit_information = getString(R.string.hiit_type_description1).toLowerCase(
							Locale.getDefault())
							+ " (" + hiit_passedRounds + "/" + hiit_rounds + ") ";
					break;
				case 2:
					display5_label.setTextColor(res.getColor(R.color.yellow));

					hiit_information = getString(R.string.hiit_type_description2).toLowerCase(
							Locale.getDefault())
							+ " (" + hiit_passedRounds + "/" + hiit_rounds + ") ";
					break;
				case 3:
					display5_label.setTextColor(res.getColor(R.color.orange));

					hiit_information = getString(R.string.hiit_type_description3).toLowerCase(
							Locale.getDefault())
							+ " (" + hiit_passedRounds + "/" + hiit_rounds + ") ";
					break;
				case 4:
					display5_label.setTextColor(res.getColor(R.color.red));

					hiit_information = getString(R.string.hiit_type_description4).toLowerCase(
							Locale.getDefault())
							+ " (" + hiit_passedRounds + "/" + hiit_rounds + ") ";
					break;
				}

				accum_intervals = 0;
				for (int j = 0; j <= action; j++) {
					accum_intervals = accum_intervals + hiit_intervals.get(j)[1];
				}

				int countDownTime;
				if (hiit_passedRounds == 1) {
					countDownTime = ((hiit_prepSeconds + accum_intervals) - hiit_lastRoundsSeconds)
							- (interval_spent_time - hiit_lastRoundsSeconds);
					hiit_information = hiit_information + FunctionUtils.shortFormatTime(countDownTime);
				} else {
					countDownTime = ((hiit_lastRoundsSeconds + accum_intervals) - interval_spent_time);
					hiit_information = hiit_information + FunctionUtils.shortFormatTime(countDownTime);
				}

				hiit_label = hiit_name;
			} else {
				display5_label.setTextColor(res.getColor(R.color.blue));

				hiit_information = getString(R.string.cool_down) + " "
						+ FunctionUtils.shortFormatTime(hiit_total_time - interval_spent_time);
				hiit_label = hiit_name;
			}

			if ((hiit_prepSeconds + (hiit_passedRounds * hiit_actionSeconds)) <= interval_spent_time) {
				hiit_passedRounds += 1;
			}
		} else {
			display5_label.setTextColor(res.getColor(R.color.white));

			hiit_information = getString(R.string.practice_completed);
			hiit_label = hiit_name;
		}

		showObjetiveData(display5_image, display5_label, display5_text1, hiit_information, hiit_label);
	}
}