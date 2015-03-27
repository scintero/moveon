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

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

@SuppressWarnings("deprecation")
public class Summary3 extends Fragment implements OnClickListener {
	private Context mContext;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private GraphicalView mChartView;

	private LinearLayout fragmentView, chartLayout;

	private ArrayList<Float> distanceList = new ArrayList<Float>();
	private ArrayList<Float> speedList = new ArrayList<Float>();
	private ArrayList<Float> altitudeList = new ArrayList<Float>();
	private ArrayList<Integer> timeList = new ArrayList<Integer>();

	private CheckBox check1, check2;

	private TextView txt1;

	private int id, time;

	private float distance, speed, altitude, min_speed, max_speed, min_altitude, max_altitude;

	private boolean paintChartView = true, isSpeedAndTime, isUpAndDownAccumAltitude, isMetric;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = (LinearLayout) inflater.inflate(R.layout.summary3, container, false);

		mContext = getActivity().getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		chartLayout = (LinearLayout) fragmentView.findViewById(R.id.linearLayout2);

		check1 = (CheckBox) fragmentView.findViewById(R.id.checkBox1);
		check2 = (CheckBox) fragmentView.findViewById(R.id.checkBox2);

		txt1 = (TextView) fragmentView.findViewById(R.id.no_info_selected);

		check1.setText(getString(R.string.altitude_label));
		check2.setText(getString(R.string.speed_label));

		check1.setOnClickListener(this);
		check2.setOnClickListener(this);

		id = prefs.getInt("selected_practice", 0);

		distanceList.clear();
		speedList.clear();
		altitudeList.clear();
		timeList.clear();

		DBManager = new DataManager(mContext);
		DBManager.Open();

		boolean isData = false;
		isSpeedAndTime = false;
		isUpAndDownAccumAltitude = false;

		cursor = DBManager.CustomQuery(getString(R.string.getting_time_and_distance_from_row_of_table_routes)
				+ " '" + id + "'",
				"SELECT time, avg_speed, up_accum_altitude, down_accum_altitude FROM routes WHERE _id = '"
						+ id + "'");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			float avg_speed = cursor.getFloat(cursor.getColumnIndex("avg_speed"));
			int time = cursor.getInt(cursor.getColumnIndex("time"));
			int up_accum_altitude = cursor.getInt(cursor.getColumnIndex("up_accum_altitude"));
			int down_accum_altitude = cursor.getInt(cursor.getColumnIndex("down_accum_altitude"));

			if ((avg_speed > 0) && (time > 0))
				isSpeedAndTime = true;

			if ((up_accum_altitude > 0) && (down_accum_altitude > 0 && time > 0))
				isUpAndDownAccumAltitude = true;

			if (((avg_speed > 0) && (time > 0)) || ((up_accum_altitude > 0) && (down_accum_altitude > 0)))
				isData = true;
		}

		checkValues();

		if (isData) {
			cursor = DBManager.getRowsFromTable(String.valueOf(id), "locations");
			cursor.moveToFirst();
			boolean isFirstRow = true;
			if (cursor.getCount() > 0) {
				while (!cursor.isAfterLast()) {
					distance = isMetric ? cursor.getFloat(cursor.getColumnIndex("distance")) : ((cursor
							.getFloat(cursor.getColumnIndex("distance")) * 1000f) / 1609f);
					speed = isMetric ? cursor.getFloat(cursor.getColumnIndex("speed")) : ((cursor
							.getFloat(cursor.getColumnIndex("speed")) * 1000f) / 1609f);
					altitude = isMetric ? cursor.getFloat(cursor.getColumnIndex("altitude")) : cursor
							.getFloat(cursor.getColumnIndex("altitude")) * 1.0936f;
					time = cursor.getInt(cursor.getColumnIndex("time"));

					if (isFirstRow) {
						min_speed = speed;
						max_speed = speed;
						min_altitude = altitude;
						max_altitude = altitude;

						isFirstRow = false;
					}

					if (String.valueOf(cursor.getInt(cursor.getColumnIndex("pause"))).equals("0")) {
						distanceList.add(distance);
						speedList.add(speed);
						altitudeList.add(altitude);
						timeList.add(time);

						if (speed < min_speed)
							min_speed = speed;

						if (speed > max_speed)
							max_speed = speed;

						if (altitude > max_altitude)
							max_altitude = altitude;

						if (altitude < min_altitude)
							min_altitude = altitude;
					}
					cursor.moveToNext();
				}
				checkGraphToBePainted();
			} else {
				paintChartView = false;
				setCorrectText();
			}
		} else {
			paintChartView = false;
			setCorrectText();
		}
		cursor.close();
		DBManager.Close();

		return fragmentView;
	}

	private void checkValues() {
		if ((prefs.getBoolean("check_altitude", true)) && (isUpAndDownAccumAltitude))
			check1.setChecked(true);
		else
			check1.setChecked(false);

		if ((prefs.getBoolean("check_speed", true)) && (isSpeedAndTime))
			check2.setChecked(true);
		else
			check2.setChecked(false);
	}

	private void checkGraphToBePainted() {
		mChartView = null;
		chartLayout.removeAllViews();

		setCorrectText();

		if ((check1.isChecked()) && (check2.isChecked()))
			lineChartView_type1();
		else if (check1.isChecked())
			lineChartView_type2(1);
		else if (check2.isChecked())
			lineChartView_type2(2);
		else
			txt1.setVisibility(View.VISIBLE);
	}

	private void setCorrectText() {
		if ((paintChartView)) {
			txt1.setText(R.string.no_info_selected);
		} else {
			if ((check1.isChecked()) || (check2.isChecked()))
				txt1.setText(R.string.no_location_info);
			else
				txt1.setText(R.string.no_info_selected);
		}
	}

	private void lineChartView_type1() {
		if (paintChartView) {
			check1.setVisibility(View.VISIBLE);
			check2.setVisibility(View.VISIBLE);
			txt1.setVisibility(View.GONE);

			TypedValue outValue1 = new TypedValue();
			TypedValue outValue2 = new TypedValue();
			TypedValue outValue3 = new TypedValue();
			TypedValue outValue4 = new TypedValue();
			mContext.getResources().getValue(R.dimen.line_axis_title_text_size_value, outValue1, true);
			mContext.getResources().getValue(R.dimen.line_chart_title_text_size_value, outValue2, true);
			mContext.getResources().getValue(R.dimen.line_labels_text_size_value, outValue3, true);
			mContext.getResources().getValue(R.dimen.line_legend_text_size_value, outValue4, true);
			float lineAxisTitleTextSizeValue = outValue1.getFloat();
			float lineCharTitleTextSizeValue = outValue2.getFloat();
			float lineLabelsTextSizeValue = outValue3.getFloat();
			float lineLegendTextSizeValue = outValue4.getFloat();

			XYMultipleSeriesDataset mDataset = getDataset_type1();
			XYMultipleSeriesRenderer mRenderer = getRenderer_type1();
			mRenderer.setApplyBackgroundColor(true);
			mRenderer.setBackgroundColor(Color.TRANSPARENT);
			mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
			mRenderer.setAxisTitleTextSize(lineAxisTitleTextSizeValue);
			mRenderer.setChartTitleTextSize(lineCharTitleTextSizeValue);
			mRenderer.setLabelsTextSize(lineLabelsTextSizeValue);
			mRenderer.setLegendTextSize(lineLegendTextSizeValue);
			mRenderer.setMargins(new int[] { 12, 25, 12, 22 });
			mRenderer.setZoomButtonsVisible(true);
			mRenderer.setPointSize(10);
			mRenderer.setClickEnabled(true);
			mChartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);

			mChartView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();

					if (seriesSelection != null) {
						String units = "";
						switch (seriesSelection.getSeriesIndex()) {
						case 0:
							units = " "
									+ (isMetric ? getString(R.string.long_unit1_detail_4)
											: getString(R.string.long_unit2_detail_4));
							break;
						case 1:
							units = " "
									+ (isMetric ? getString(R.string.long_unit1_detail_2)
											: getString(R.string.long_unit2_detail_2));
							break;
						}
						UIFunctionUtils.showMessage(mContext, true, (int) seriesSelection.getValue() + units);
					}
				}
			});

			chartLayout.addView(mChartView);
		} else {
			setCorrectText();
		}
	}

	private XYMultipleSeriesDataset getDataset_type1() {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		XYSeries firstSeries = new XYSeries(getString(R.string.altitude_label), 1);
		XYSeries secondSeries = new XYSeries(getString(R.string.speed_label));

		for (int i = 0; i < timeList.size(); i++) {
			firstSeries.add(FunctionUtils.customizedRound(((float) timeList.get(i) / 60), 1),
					altitudeList.get(i));

			secondSeries.add(FunctionUtils.customizedRound(((float) timeList.get(i) / 60), 1),
					speedList.get(i));
		}

		dataset.addSeries(firstSeries);
		dataset.addSeries(secondSeries);

		return dataset;
	}

	private XYMultipleSeriesRenderer getRenderer_type1() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(2);

		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setLineWidth(2f);
		r.setColor(Color.rgb(0, 0, 188));
		r.setFillBelowLine(true);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);

		TypedValue outValue1 = new TypedValue();
		TypedValue outValue2 = new TypedValue();
		TypedValue outValue3 = new TypedValue();
		TypedValue outValue4 = new TypedValue();
		mContext.getResources().getValue(R.dimen.xy_chart_text_size_value, outValue1, true);
		mContext.getResources().getValue(R.dimen.xy_labels_text_size_value, outValue2, true);
		mContext.getResources().getValue(R.dimen.xy_axis_title_text_size_value, outValue3, true);
		mContext.getResources().getValue(R.dimen.xy_legend_text_size_value, outValue4, true);
		float xyChartTextSizeValue = outValue1.getFloat();
		float xyLabelsTextSizeValue = outValue1.getFloat();
		float xyAxisTitleTextSizeValue = outValue1.getFloat();
		float xyLegendTextSizeValue = outValue1.getFloat();

		r = new XYSeriesRenderer();
		r.setColor(Color.rgb(255, 124, 0));
		r.setFillPoints(true);
		r.setLineWidth(2.5f);
		r.setDisplayChartValues(false);
		r.setChartValuesTextSize(xyChartTextSizeValue);

		renderer.addSeriesRenderer(r);
		renderer.setAxesColor(Color.WHITE);
		renderer.setLabelsColor(Color.LTGRAY);
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setTextTypeface("sans_serif", Typeface.BOLD);

		renderer.setLabelsTextSize(xyLabelsTextSizeValue);
		renderer.setAxisTitleTextSize(xyAxisTitleTextSizeValue);
		renderer.setLegendTextSize(xyLegendTextSizeValue);

		renderer.setXTitle(FunctionUtils.capitalizeFirtsLetter(getString(R.string.minutes)));
		renderer.setYTitle(isMetric ? getString(R.string.long_unit1_detail_7)
				: getString(R.string.long_unit2_detail_7));
		renderer.setYTitle(FunctionUtils
				.capitalizeFirtsLetter(isMetric ? getString(R.string.long_unit1_detail_10)
						: getString(R.string.long_unit2_detail_10)), 1);
		renderer.setYAxisAlign(Align.RIGHT, 1);
		renderer.setYLabelsAlign(Align.RIGHT, 1);
		renderer.setYAxisMin(min_altitude, 1);
		renderer.setYAxisMax(max_altitude);

		renderer.setXLabels(20);
		renderer.setYLabels(20);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setShowGrid(false);

		float smallest = 0;
		if (0 > min_altitude)
			smallest = min_altitude;

		float highest = max_speed;
		if (max_speed < max_altitude)
			highest = max_altitude;

		renderer.setXAxisMin(0);
		renderer.setXAxisMax((float) ((float) timeList.get(timeList.size() - 1) / 60));
		renderer.setYAxisMin(smallest);
		renderer.setYAxisMax(highest);

		return renderer;
	}

	private void lineChartView_type2(final int type) {
		if (paintChartView) {
			txt1.setVisibility(View.GONE);

			TypedValue outValue1 = new TypedValue();
			TypedValue outValue2 = new TypedValue();
			TypedValue outValue3 = new TypedValue();
			TypedValue outValue4 = new TypedValue();
			mContext.getResources().getValue(R.dimen.line2_axis_title_text_size_value, outValue1, true);
			mContext.getResources().getValue(R.dimen.line2_chart_title_text_size_value, outValue2, true);
			mContext.getResources().getValue(R.dimen.line2_labels_text_size_value, outValue3, true);
			mContext.getResources().getValue(R.dimen.line2_legend_text_size_value, outValue4, true);
			float line2AxisTitleTextSizeValue = outValue1.getFloat();
			float line2CharTitleTextSizeValue = outValue2.getFloat();
			float line2LabelsTextSizeValue = outValue3.getFloat();
			float line2LegendTextSizeValue = outValue4.getFloat();

			XYMultipleSeriesDataset mDataset = null;
			XYMultipleSeriesRenderer mRenderer = null;
			switch (type) {
			case 1:
				mDataset = getDataset_type2(1);
				break;
			case 2:
				mDataset = getDataset_type2(2);
				break;
			}
			mRenderer = getRenderer_type2(type);
			mRenderer.setApplyBackgroundColor(true);
			mRenderer.setBackgroundColor(Color.TRANSPARENT);
			mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
			mRenderer.setAxisTitleTextSize(line2AxisTitleTextSizeValue);
			mRenderer.setChartTitleTextSize(line2CharTitleTextSizeValue);
			mRenderer.setLabelsTextSize(line2LabelsTextSizeValue);
			mRenderer.setLegendTextSize(line2LegendTextSizeValue);
			mRenderer.setMargins(new int[] { 12, 25, 12, 12 });
			mRenderer.setZoomButtonsVisible(true);
			mRenderer.setPointSize(10);
			mRenderer.setClickEnabled(true);
			mChartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);

			mChartView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();

					if (seriesSelection != null) {
						switch (type) {
						case 1:
							UIFunctionUtils.showMessage(mContext, true, (int) seriesSelection.getValue()
									+ " "
									+ (isMetric ? getString(R.string.long_unit1_detail_4)
											: getString(R.string.long_unit2_detail_4)));
							break;
						case 2:
							UIFunctionUtils.showMessage(mContext, true, (int) seriesSelection.getValue()
									+ " "
									+ (isMetric ? getString(R.string.long_unit1_detail_2)
											: getString(R.string.long_unit2_detail_2)));
							break;
						}
					}
				}
			});

			chartLayout.addView(mChartView);
		} else {
			setCorrectText();
		}
	}

	private XYMultipleSeriesDataset getDataset_type2(int type) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYSeries firstSeries = null;

		switch (type) {
		case 1:
			firstSeries = new XYSeries(getString(R.string.altitude_label));

			for (int i = 0; i < altitudeList.size(); i++) {
				firstSeries.add(FunctionUtils.customizedRound(((float) timeList.get(i) / 60), 1),
						altitudeList.get(i));
			}
			break;
		case 2:
			firstSeries = new XYSeries(getString(R.string.speed_label));

			for (int i = 0; i < speedList.size(); i++) {
				firstSeries.add(FunctionUtils.customizedRound(((float) timeList.get(i) / 60), 1),
						speedList.get(i));
			}
			break;
		}

		dataset.addSeries(firstSeries);

		return dataset;
	}

	private XYMultipleSeriesRenderer getRenderer_type2(int type) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		XYSeriesRenderer r = new XYSeriesRenderer();

		r = new XYSeriesRenderer();
		r.setFillPoints(true);
		r.setLineWidth(2f);
		r.setDisplayChartValues(false);
		r.setChartValuesTextSize(10f);

		switch (type) {
		case 1:
			r.setColor(Color.rgb(0, 0, 188));
			r.setFillBelowLine(true);
			break;
		case 2:
			r.setColor(Color.rgb(255, 124, 0));
			break;
		}

		renderer.addSeriesRenderer(r);
		renderer.setAxesColor(Color.WHITE);
		renderer.setLabelsColor(Color.LTGRAY);
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setTextTypeface("sans_serif", Typeface.BOLD);

		renderer.setLabelsTextSize(14f);
		renderer.setAxisTitleTextSize(15);
		renderer.setLegendTextSize(15);

		renderer.setXTitle(FunctionUtils.capitalizeFirtsLetter(getString(R.string.minutes)));
		renderer.setXLabels(20);
		renderer.setYLabels(20);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setShowGrid(false);
		renderer.setXAxisMin((timeList.get(0) / 60));
		renderer.setXAxisMax((float) ((float) timeList.get(timeList.size() - 1) / 60));

		switch (type) {
		case 1:
			renderer.setYTitle(isMetric ? FunctionUtils
					.capitalizeFirtsLetter(getString(R.string.long_unit1_detail_10)) : FunctionUtils
					.capitalizeFirtsLetter(getString(R.string.long_unit2_detail_10)));
			renderer.setYAxisMin(min_altitude);
			renderer.setYAxisMax(max_altitude);
			break;
		case 2:
			renderer.setYTitle(isMetric ? getString(R.string.long_unit1_detail_7)
					: getString(R.string.long_unit2_detail_7));
			renderer.setYAxisMin(min_speed);
			renderer.setYAxisMax(max_speed);
			break;
		}

		return renderer;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.checkBox1:
			if (isUpAndDownAccumAltitude) {
				if (check1.isChecked()) {
					editor.putBoolean("check_altitude", true);
					check1.setChecked(false);

				} else {
					editor.putBoolean("check_altitude", false);
					check1.setChecked(true);
				}
				editor.commit();

				checkValues();

				checkGraphToBePainted();
			} else {
				check1.setChecked(false);

				UIFunctionUtils.showMessage(mContext, true, getString(R.string.graph_time_and_speed_no_data));
			}
			break;
		case R.id.checkBox2:
			if (isSpeedAndTime) {
				if (check2.isChecked()) {
					editor.putBoolean("check_speed", true);
					check2.setChecked(false);

				} else {
					editor.putBoolean("check_speed", false);
					check2.setChecked(true);
				}
				editor.commit();

				checkValues();

				checkGraphToBePainted();
			} else {
				check2.setChecked(false);

				UIFunctionUtils.showMessage(mContext, true,
						getString(R.string.graph_time_and_altitude_no_data));
			}

			break;
		}
	}
}