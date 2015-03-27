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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.entities.EntityPathPoints;
import com.saulcintero.moveon.enums.MarkerTypes;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.MediaFunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;
import com.saulcintero.osmdroid.MyItemizedOverlay;
import com.saulcintero.osmdroid.MyItemizedOverlayGestureListenerImg;
import com.saulcintero.osmdroid.MyItemizedOverlayGestureListenerTxt;

@SuppressWarnings("deprecation")
public class Summary1 extends Fragment implements OnClickListener {
	private Activity act;
	private Context mContext;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private IntentFilter intentFilter, intentFilter2;

	private Resources res;

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private CheckBox check1, check2, check3;

	private ImageButton full_mapview;

	private LinearLayout layout1;

	private MapView mapView;
	private PathOverlay pathOverlay;
	private MyItemizedOverlay myItemizedOverlay = null;

	private ArrayList<OverlayItem> oItem = new ArrayList<OverlayItem>();

	private ArrayList<EntityPathPoints> pathPointsList = new ArrayList<EntityPathPoints>();
	private ArrayList<GeoPoint> gPointsList = new ArrayList<GeoPoint>();
	private ArrayList<GeoPoint> distanceList = new ArrayList<GeoPoint>();
	private ArrayList<String> distanceTitleList = new ArrayList<String>();
	private ArrayList<String> distanceDetailsList = new ArrayList<String>();
	private EntityPathPoints pathPoints;

	private float[] startCoords, finishCoords;

	private ImageView activity_image;
	private TextView label1, label2, label3, label4, label5, label6, label7, label8, label9, label10,
			label11, label12, label13, label14, label15, label16, label17;
	private TextView text1, text2, text3, text4, text5, text6, text7, text8, text9, text10, text11, text12,
			text13, text14, text15, text16, text17, text18;

	private String metric_text1, metric_text2, metric_text3, metric_text4;

	private int id;

	private int lap, unit_counter, unit, nextDistance, sumDistance, position = 0;;

	private boolean isExpanded = false, mapviewBigPreview;

	private String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/moveon/images/";
	File folder = new File(file_path);

	ArrayList<Bitmap> pics = new ArrayList<Bitmap>();

	private Gallery mGallery;

	private String[] files;

	private int markChosenPicturePosition = 0;

	private boolean areAnyPictures, isMetric;

	private BroadcastReceiver mReceiverExpandRestartMapview = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			expand_restart();
		}

		private void expand_restart() {
			if (isExpanded) {
				changeLayout1Margins();
				full_mapview.setImageResource(R.drawable.full_screen);
				isExpanded = false;
			} else {
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				layoutParams.setMargins(
						(int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration1),
						(int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration1),
						(int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration1),
						(int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration2));
				layout1.setLayoutParams(layoutParams);

				full_mapview.setImageResource(R.drawable.return_from_full_screen);

				isExpanded = true;
			}
		}
	};

	private BroadcastReceiver mReceiverSelectedPicture = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			selectImageInGallery(Integer.parseInt(intent.getStringExtra("tappedPicture")));

			markChosenPicturePosition = Integer.parseInt(intent.getStringExtra("tappedPicture"));
			markChosenPicture(Integer.parseInt(intent.getStringExtra("tappedPicture")));
		}

		private void selectImageInGallery(int index) {
			mGallery.setSelection(index);
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		act.unregisterReceiver(mReceiverExpandRestartMapview);
		act.unregisterReceiver(mReceiverSelectedPicture);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@SuppressLint("Recycle")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout fragmentView = (LinearLayout) inflater.inflate(R.layout.summary1, container, false);
		layout1 = (LinearLayout) fragmentView.findViewById(R.id.linearLayout1);

		act = getActivity();
		mContext = act.getApplicationContext();
		res = getResources();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		id = prefs.getInt("selected_practice", 0);

		isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		check1 = (CheckBox) fragmentView.findViewById(R.id.checkBox1);
		check2 = (CheckBox) fragmentView.findViewById(R.id.checkBox2);
		check3 = (CheckBox) fragmentView.findViewById(R.id.checkBox3);
		full_mapview = (ImageButton) fragmentView.findViewById(R.id.summary_mapview_fullscreen);
		label1 = (TextView) fragmentView.findViewById(R.id.summary_label_one);
		label2 = (TextView) fragmentView.findViewById(R.id.summary_label_two);
		label3 = (TextView) fragmentView.findViewById(R.id.summary_label_three);
		label4 = (TextView) fragmentView.findViewById(R.id.summary_label_four);
		label5 = (TextView) fragmentView.findViewById(R.id.summary_label_five);
		label6 = (TextView) fragmentView.findViewById(R.id.summary_label_six);
		label7 = (TextView) fragmentView.findViewById(R.id.summary_label_seven);
		label8 = (TextView) fragmentView.findViewById(R.id.summary_label_eight);
		label9 = (TextView) fragmentView.findViewById(R.id.summary_label_nine);
		label10 = (TextView) fragmentView.findViewById(R.id.summary_label_ten);
		label11 = (TextView) fragmentView.findViewById(R.id.summary_label_eleven);
		label12 = (TextView) fragmentView.findViewById(R.id.summary_label_twelve);
		label13 = (TextView) fragmentView.findViewById(R.id.summary_label_thirteen);
		label14 = (TextView) fragmentView.findViewById(R.id.summary_label_fourteen);
		label15 = (TextView) fragmentView.findViewById(R.id.summary_label_fifteen);
		label16 = (TextView) fragmentView.findViewById(R.id.summary_label_sixteen);
		label17 = (TextView) fragmentView.findViewById(R.id.summary_label_seventeen);
		text1 = (TextView) fragmentView.findViewById(R.id.summary_date);
		text2 = (TextView) fragmentView.findViewById(R.id.summary_text_one);
		text3 = (TextView) fragmentView.findViewById(R.id.summary_text_two);
		text4 = (TextView) fragmentView.findViewById(R.id.summary_text_three);
		text5 = (TextView) fragmentView.findViewById(R.id.summary_text_four);
		text6 = (TextView) fragmentView.findViewById(R.id.summary_text_five);
		text7 = (TextView) fragmentView.findViewById(R.id.summary_text_six);
		text8 = (TextView) fragmentView.findViewById(R.id.summary_text_seven);
		text9 = (TextView) fragmentView.findViewById(R.id.summary_text_eight);
		text10 = (TextView) fragmentView.findViewById(R.id.summary_text_nine);
		text11 = (TextView) fragmentView.findViewById(R.id.summary_text_ten);
		text12 = (TextView) fragmentView.findViewById(R.id.summary_text_eleven);
		text13 = (TextView) fragmentView.findViewById(R.id.summary_text_twelve);
		text14 = (TextView) fragmentView.findViewById(R.id.summary_text_thirteen);
		text15 = (TextView) fragmentView.findViewById(R.id.summary_text_fourteen);
		text16 = (TextView) fragmentView.findViewById(R.id.summary_text_fifteen);
		text17 = (TextView) fragmentView.findViewById(R.id.summary_text_sixteen);
		text18 = (TextView) fragmentView.findViewById(R.id.summary_text_seventeen);

		activity_image = (ImageView) fragmentView.findViewById(R.id.summary_activity);

		mGallery = (Gallery) fragmentView.findViewById(R.id.gallery);

		// set gallery to left side
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		MarginLayoutParams mlp = (MarginLayoutParams) mGallery.getLayoutParams();
		mlp.setMargins(-(metrics.widthPixels / 2 + 75), mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);

		String[] data = DataFunctionUtils.getRouteData(mContext, id, isMetric);

		label1.setText(getString(R.string.time_label).toUpperCase(Locale.getDefault()));
		label2.setText(getString(R.string.distance_label).toUpperCase(Locale.getDefault()));
		label3.setText(getString(R.string.avg_label).toUpperCase(Locale.getDefault()));
		label4.setText(getString(R.string.ritm_label).toUpperCase(Locale.getDefault()));
		label5.setText(getString(R.string.max_speed_label).toUpperCase(Locale.getDefault()));
		label6.setText(getString(R.string.max_altitude_label).toUpperCase(Locale.getDefault()));
		label7.setText(getString(R.string.min_altitude_label).toUpperCase(Locale.getDefault()));
		label8.setText(getString(R.string.calories_label).toUpperCase(Locale.getDefault()));
		label9.setText(getString(R.string.steps_label).toUpperCase(Locale.getDefault()));
		label10.setText(getString(R.string.beats_avg_label).toUpperCase(Locale.getDefault()));
		label11.setText(getString(R.string.max_beats_label).toUpperCase(Locale.getDefault()));
		label12.setText(getString(R.string.shoe).toUpperCase(Locale.getDefault()));
		label13.setText(getString(R.string.comments).toUpperCase(Locale.getDefault()));
		label14.setText(getString(R.string.accum_ascent).toUpperCase(Locale.getDefault()));
		label15.setText(getString(R.string.accum_descent).toUpperCase(Locale.getDefault()));
		label16.setText(getString(R.string.avg_cadence).toUpperCase(Locale.getDefault()));
		label17.setText(getString(R.string.max_cadence).toUpperCase(Locale.getDefault()));

		TypedArray activities_icons = res.obtainTypedArray(R.array.activities_icons);
		activity_image.setImageDrawable(activities_icons.getDrawable(Integer.valueOf(data[0]) - 1));

		metric_text1 = (isMetric ? getString(R.string.long_unit1_detail_1)
				: getString(R.string.long_unit2_detail_1));
		metric_text2 = (isMetric ? getString(R.string.long_unit1_detail_2)
				: getString(R.string.long_unit2_detail_2));
		metric_text3 = (isMetric ? getString(R.string.long_unit1_detail_3)
				: getString(R.string.long_unit2_detail_3));
		metric_text4 = (isMetric ? getString(R.string.long_unit1_detail_4)
				: getString(R.string.long_unit2_detail_4));

		text1.setText(data[1]);
		text2.setText(data[2]);
		text3.setText(FunctionUtils.customizedRound(Float.valueOf(data[3]), 2) + " " + metric_text1);
		text4.setText(FunctionUtils.customizedRound(Float.valueOf(data[4]), 2) + " " + metric_text2);
		text5.setText(data[5] + " " + metric_text3);
		text6.setText(data[6] + " " + metric_text2);
		text7.setText(data[7] + " " + metric_text4);
		text8.setText(data[8] + " " + metric_text4);
		text9.setText(data[9] + " " + getString(R.string.tell_calories_setting_details));
		text10.setText(data[10]);
		text11.setText(data[13] + " " + getString(R.string.beats_per_minute));
		text12.setText(data[14] + " " + getString(R.string.beats_per_minute));
		text13.setText(data[12]);
		text14.setText(data[11]);
		text15.setText(data[20] + " " + metric_text4);
		text16.setText(data[21] + " " + metric_text4);
		text17.setText(data[23] + " " + getString(R.string.revolutions_per_minute));
		text18.setText(data[24] + " " + getString(R.string.revolutions_per_minute));

		check1.setOnClickListener(this);
		check2.setOnClickListener(this);
		check3.setOnClickListener(this);

		checkboxStatus();

		populatesMap(fragmentView);

		intentFilter = new IntentFilter("android.intent.action.EXPAND_OR_RESTART_SUMMARY_MAPVIEW");
		intentFilter2 = new IntentFilter("android.intent.action.SUMMARY_GALLERY_SELECTED_IMAGE");
		act.registerReceiver(mReceiverExpandRestartMapview, intentFilter);
		act.registerReceiver(mReceiverSelectedPicture, intentFilter2);

		readImages();
		changeLayout1Margins();

		mGallery.setAdapter(new ImageAdapter(mContext));

		mGallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				position = arg2;

				if (folder.isDirectory()) {
					launchPhoto();
				} else {
					UIFunctionUtils.showMessage(mContext, true,
							mContext.getString(R.string.image_resource_missing));
				}
			}
		});

		mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				markChosenPicturePosition = position;
				markChosenPicture(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapter) {
			}
		});

		return fragmentView;
	}

	private void markChosenPicture(int pic) {
		for (int i = 0; i < pics.size(); i++) {
			if (oItem.size() > 0) {
				OverlayItem o = oItem.get(i);

				if (i == pic)
					o.setMarker(res.getDrawable(R.drawable.selected_picture_pin));
				else
					o.setMarker(res.getDrawable(R.drawable.picture_pin));
			}
		}
		mapView.postInvalidate();
	}

	private void readImages() {
		mapviewBigPreview = true;

		if (pics.size() > 0)
			pics.clear();

		createGalleryThumbnails();
	}

	private void createGalleryThumbnails() {
		if (folder.isDirectory()) {
			files = folder.list();

			if (files.length > 0) {
				Arrays.sort(files);

				for (int i = 0; i < files.length; i++) {
					String[] splitFile = files[i].split("_");

					if (splitFile[0].equals(String.valueOf(prefs.getInt("selected_practice", 0)))) {
						mapviewBigPreview = false;
						float angle = MediaFunctionUtils.getCorrectImageAngle(mContext, file_path, files[i]);
						Uri selectedImageUri = Uri.parse(file_path + files[i]);
						pics.add(MediaFunctionUtils.getPreview(selectedImageUri, angle));
					}
				}
			}
		}
	}

	private void changeLayout1Margins() {
		int dp;
		
		if (act.getResources().getConfiguration().orientation == 1) { // 1 = portrait
			dp = 130;
			if (!mapviewBigPreview)
				dp = 110;
		} else { // 2 = landscape
			dp = 110;
			if (!mapviewBigPreview)
				dp = 90;
		}
		

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, FunctionUtils.calculateDpFromPx(mContext, dp));
		layoutParams.setMargins((int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration1),
				(int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration1),
				(int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration1),
				(int) res.getDimension(R.dimen.summary1_linearlayout_1_configuration1));
		layout1.setLayoutParams(layoutParams);
	}

	private void launchPhoto() {
		if (folder.isDirectory()) {
			String zeros = "";
			if ((position + 1) < 10) {
				zeros = "000";
			} else if ((position + 1) < 100) {
				zeros = "00";
			} else if ((position + 1) < 1000) {
				zeros = "0";
			}

			launchGalleryIntent(prefs.getInt("selected_practice", 0) + "_" + zeros + (position + 1) + ".jpg");
		} else {
			UIFunctionUtils.showMessage(mContext, true, mContext.getString(R.string.image_resource_missing));
		}
	}

	private void launchGalleryIntent(String file_name) {
		File selectedImage = new File(file_path + file_name);
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(selectedImage), "image/*");
		startActivity(intent);
	}

	private void checkboxStatus() {
		if (prefs.getBoolean("check_distance", true))
			check1.setChecked(true);
		else
			check1.setChecked(false);

		if (prefs.getBoolean("check_pauses", true))
			check2.setChecked(true);
		else
			check2.setChecked(false);

		if (prefs.getBoolean("check_pictures", true))
			check3.setChecked(true);
		else
			check3.setChecked(false);
	}

	private void populatesMap(LinearLayout fragmentView) {
		mapView = (MapView) fragmentView.findViewById(R.id.summary_mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setMultiTouchControls(true);

		getTileSource(Integer.valueOf(prefs.getString("map_tile_type", "0")));

		draw_practice_in_map(true);
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

	private void draw_practice_in_map(boolean fitMap) {
		DBManager = new DataManager(mContext);
		DBManager.Open();

		mapView.getOverlays().clear();
		pathPointsList.clear();
		gPointsList.clear();
		distanceList.clear();
		distanceTitleList.clear();
		distanceDetailsList.clear();
		lap = 1;
		unit_counter = 1;
		unit = 1000;
		nextDistance = (isMetric ? 1000 : 1609);
		sumDistance = (isMetric ? 1000 : 1609);

		ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mContext);
		mapView.getOverlays().add(mScaleBarOverlay);

		String speed_unit = (isMetric ? getString(R.string.long_unit1_detail_2)
				: getString(R.string.long_unit2_detail_2));
		String altitude_unit = (isMetric ? getString(R.string.long_unit1_detail_4)
				: getString(R.string.long_unit2_detail_4));
		String distance_unit = (isMetric ? getString(R.string.long_unit1_detail_5)
				: getString(R.string.long_unit2_detail_5));

		cursor = DBManager.getRowsFromTable(String.valueOf(id), "locations");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			for (int i = 1; i <= cursor.getCount(); i++) {
				if (i == 1) {
					startCoords = new float[2];
					startCoords[0] = Float.valueOf(cursor.getString(cursor.getColumnIndex("latitude")));
					startCoords[1] = Float.valueOf(cursor.getString(cursor.getColumnIndex("longitude")));
				} else if (i == cursor.getCount()) {
					finishCoords = new float[2];
					finishCoords[0] = Float.valueOf(cursor.getString(cursor.getColumnIndex("latitude")));
					finishCoords[1] = Float.valueOf(cursor.getString(cursor.getColumnIndex("longitude")));
				}

				String mDistance = String.valueOf(cursor.getFloat(cursor.getColumnIndex("distance")));

				if ((Float.valueOf(mDistance) * unit) >= nextDistance) {
					distanceList.add(new GeoPoint(Float.valueOf(cursor.getString(cursor
							.getColumnIndex("latitude"))), Float.valueOf(cursor.getString(cursor
							.getColumnIndex("longitude")))));

					distanceTitleList.add(distance_unit + " " + unit_counter);

					distanceDetailsList.add(getString(R.string.time_uppercase).toLowerCase(
							Locale.getDefault())
							+ ": "
							+ FunctionUtils.longFormatTime(Long.parseLong(cursor.getString(cursor
									.getColumnIndex("time"))))
							+ "\n"
							+ getString(R.string.speed_label)
							+ ": "
							+ cursor.getString(cursor.getColumnIndex("speed"))
							+ " "
							+ speed_unit
							+ "\n"
							+ getString(R.string.altitude_label)
							+ ": "
							+ cursor.getString(cursor.getColumnIndex("altitude"))
							+ " "
							+ altitude_unit
							+ "\n"
							+ getString(R.string.steps_label)
							+ ": "
							+ cursor.getString(cursor.getColumnIndex("steps"))
							+ "\n"
							+ getString(R.string.beats_label)
							+ ": "
							+ cursor.getString(cursor.getColumnIndex("hr")));

					unit_counter++;
					nextDistance += sumDistance;
				}

				pathPoints = new EntityPathPoints();
				pathPoints.setLatitude(cursor.getString(cursor.getColumnIndex("latitude")));
				pathPoints.setLongitude(cursor.getString(cursor.getColumnIndex("longitude")));
				pathPoints.setPause(String.valueOf(cursor.getInt(cursor.getColumnIndex("pause"))));
				pathPointsList.add(pathPoints);
				gPointsList.add(new GeoPoint(
						Float.valueOf(cursor.getString(cursor.getColumnIndex("latitude"))), Float
								.valueOf(cursor.getString(cursor.getColumnIndex("longitude")))));

				cursor.moveToNext();
			}
			cursor.close();
			DBManager.Close();

			if (pathPointsList.size() > 0) {
				addPathOverlay();

				drawPathAndMarkers();

				if (prefs.getBoolean("check_distance", true))
					distanceMarkers();

				startStopMarkers();

				if (fitMap)
					fitMapToMarkers();
			}

			mapView.invalidate();
		} else {
			cursor.close();
			DBManager.Close();
		}
	}

	private void addPathOverlay() {
		pathOverlay = new PathOverlay(Color.BLUE, mContext);
		pathOverlay.getPaint().setStyle(Style.STROKE);
		pathOverlay.getPaint().setStrokeWidth(3);
		pathOverlay.getPaint().setAntiAlias(true);
		mapView.getOverlays().add(pathOverlay);
	}

	private void drawPathAndMarkers() {
		boolean pauseMarker = true;

		pathOverlay.clearPath();

		for (int m = 0; m <= (pathPointsList.size() - 1); m++) {
			EntityPathPoints mPath = (EntityPathPoints) pathPointsList.get(m);
			GeoPoint gp = new GeoPoint(Float.valueOf(mPath.getLatitude()),
					Float.valueOf(mPath.getLongitude()));
			pathOverlay.addPoint(gp);

			if (prefs.getBoolean("check_pauses", true)) {
				if (mPath.getPause().equals("1")) {
					if (pauseMarker) {
						addMarker(1, new GeoPoint(gp), MarkerTypes.PAUSE1_MARKER.getTypes(), "", "");
						pauseMarker = false;
					}
				} else {
					if (!pauseMarker)
						addMarker(1, new GeoPoint(gp), MarkerTypes.PAUSE2_MARKER.getTypes(), "", "");

					pauseMarker = true;
				}
			}
		}

		if (prefs.getBoolean("check_pictures", true)) {
			oItem.clear();
			pictureMarkers();
		}
	}

	private void pictureMarkers() {
		int picId = 0;
		areAnyPictures = false;

		if (folder.isDirectory()) {
			files = folder.list();

			if (files.length > 0) {
				Arrays.sort(files);

				for (int i = 0; i < files.length; i++) {
					String[] splitFile = files[i].split("_");

					if (splitFile[0].equals(String.valueOf(prefs.getInt("selected_practice", 0)))) {
						areAnyPictures = true;

						File imageFile = new File(file_path + files[i]);
						ExifInterface exif = null;

						try {
							exif = new ExifInterface(imageFile.getAbsolutePath());

							float[] LatLong = MediaFunctionUtils.ShowExif(mContext, exif);

							if (LatLong != null) {
								addMarker(3, new GeoPoint(LatLong[0], LatLong[1]),
										MarkerTypes.PHOTO_MARKER.getTypes(), String.valueOf(picId), "");
								picId++;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if (areAnyPictures)
					markChosenPicture(markChosenPicturePosition);
			}
		}
	}

	private void distanceMarkers() {
		for (int i = 0; i <= (distanceList.size() - 1); i++) {
			addMarker(2, new GeoPoint(distanceList.get(i)), MarkerTypes.DISTANCE_MARKER.getTypes(),
					distanceTitleList.get(i), distanceDetailsList.get(i));

			lap += 1;
		}
	}

	private void startStopMarkers() {
		addMarker(1, new GeoPoint(startCoords[0], startCoords[1]), MarkerTypes.START_MARKER.getTypes(), "",
				"");
		addMarker(1, new GeoPoint(finishCoords[0], finishCoords[1]), MarkerTypes.STOP_MARKER.getTypes(), "",
				"");
	}

	private void addMarker(int overlayType, GeoPoint gp, int typeOfMarker, String title, String description) {
		Drawable marker = null;

		MarkerTypes whichMarker = MarkerTypes.values()[typeOfMarker];
		switch (whichMarker) {
		case START_MARKER:
			marker = res.getDrawable(R.drawable.start_pin);
			break;
		case PAUSE1_MARKER:
			marker = res.getDrawable(R.drawable.pause1_pin);
			break;
		case PAUSE2_MARKER:
			marker = res.getDrawable(R.drawable.pause2_pin);
			break;
		case STOP_MARKER:
			marker = res.getDrawable(R.drawable.stop_pin);
			break;
		case PHOTO_MARKER:
			marker = res.getDrawable(R.drawable.picture_pin);
			break;
		case DISTANCE_MARKER:
			BitmapDrawable bmd = FunctionUtils.createMarkerIcon(mContext, R.drawable.distance_pin,
					String.valueOf(lap));
			marker = bmd;
			break;
		}

		int markerWidth = marker.getIntrinsicWidth();
		int markerHeight = marker.getIntrinsicHeight();
		marker.setBounds(0, markerHeight, markerWidth, 0);

		switch (overlayType) {
		case 1:
			ResourceProxy resourceProxy = new DefaultResourceProxyImpl(mContext);

			myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
			mapView.getOverlays().add(myItemizedOverlay);

			myItemizedOverlay.addItem(gp, "", "");
			break;
		case 2:
			ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
			MyItemizedOverlayGestureListenerTxt txtGestureOverlay = new MyItemizedOverlayGestureListenerTxt(
					act, list);
			OverlayItem overlayItem = new OverlayItem(title, description, gp);
			overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
			overlayItem.setMarker(marker);
			txtGestureOverlay.addItem(overlayItem);
			mapView.getOverlays().add(txtGestureOverlay);
			mapView.invalidate();
			break;
		case 3:
			ArrayList<OverlayItem> list2 = new ArrayList<OverlayItem>();
			MyItemizedOverlayGestureListenerImg imgGestureOverlay = new MyItemizedOverlayGestureListenerImg(
					act, list2);
			OverlayItem overlayItem2 = new OverlayItem(title, description, gp);
			overlayItem2.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
			overlayItem2.setMarker(marker);
			imgGestureOverlay.addItem(overlayItem2);
			mapView.getOverlays().add(imgGestureOverlay);

			oItem.add(overlayItem2);

			mapView.invalidate();
			break;
		}
	}

	private void fitMapToMarkers() {
		final BoundingBoxE6 mOverlayItemsBounds = BoundingBoxE6.fromGeoPoints(gPointsList);

		if (mapView.getHeight() > 0) {
			mapView.zoomToBoundingBox(mOverlayItemsBounds);
		} else {
			ViewTreeObserver vto1 = mapView.getViewTreeObserver();
			vto1.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				public void onGlobalLayout() {
					mapView.zoomToBoundingBox(mOverlayItemsBounds);
					ViewTreeObserver vto2 = mapView.getViewTreeObserver();
					vto2.removeOnGlobalLayoutListener(this);
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.checkBox1:
			if (check1.isChecked())
				editor.putBoolean("check_distance", true);
			else
				editor.putBoolean("check_distance", false);

			editor.commit();

			draw_practice_in_map(false);

			break;
		case R.id.checkBox2:
			if (check2.isChecked())
				editor.putBoolean("check_pauses", true);
			else
				editor.putBoolean("check_pauses", false);

			editor.commit();

			draw_practice_in_map(false);

			break;
		case R.id.checkBox3:
			if (check3.isChecked())
				editor.putBoolean("check_pictures", true);
			else
				editor.putBoolean("check_pictures", false);

			editor.commit();

			draw_practice_in_map(false);

			break;
		}
	}

	public class ImageAdapter extends BaseAdapter {
		private Context ctx;
		int imageBackground;

		public ImageAdapter(Context c) {
			ctx = c;
			TypedArray ta = ctx.obtainStyledAttributes(R.styleable.Gallery1);
			imageBackground = ta.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 1);
			ta.recycle();
		}

		public int getCount() {
			return pics.size();
		}

		public Object getItem(int arg0) {
			return arg0;
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int arg0, View arg1, ViewGroup arg2) {
			ImageView iv = new ImageView(ctx);

			Bitmap bm;
			bm = pics.get(arg0);

			iv.setImageBitmap(bm);
			iv.setScaleType(ImageView.ScaleType.FIT_XY);
			iv.setLayoutParams(new Gallery.LayoutParams(150, 150));
			iv.setBackgroundResource(imageBackground);
			return iv;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

	}
}