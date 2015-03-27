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
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class Summary4 extends Fragment implements OnClickListener {
	private Context mContext;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private LinearLayout fragmentView, layout;

	private ArrayList<Integer> hrList = new ArrayList<Integer>();
	private ArrayList<Integer> timeList = new ArrayList<Integer>();

	private RadioButton radio1, radio2;

	private TextView txt1;

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private GraphicalView mChartView;

	private int[] hr_zones_time = new int[6], hr_zones = new int[10];

	private int id, hr, time, lastTime = 0, fc_type, min_hr, max_hr = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = (LinearLayout) inflater.inflate(R.layout.summary4, container, false);

		mContext = getActivity().getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		layout = (LinearLayout) fragmentView.findViewById(R.id.linearLayout1);

		radio1 = (RadioButton) fragmentView.findViewById(R.id.RadioButton01);
		radio2 = (RadioButton) fragmentView.findViewById(R.id.RadioButton02);

		txt1 = (TextView) fragmentView.findViewById(R.id.no_hr_info);

		radio1.setText(getString(R.string.hr_zones_label));
		radio2.setText(getString(R.string.beats_per_minute).toUpperCase(Locale.getDefault()));

		fc_type = prefs.getInt("fc_type", 1);

		checkValues();

		radio1.setOnClickListener(this);
		radio2.setOnClickListener(this);

		id = prefs.getInt("selected_practice", 0);

		hrList.clear();
		timeList.clear();

		for (int i = 0; i < hr_zones.length; i++) {
			hr_zones[i] = 0;
			switch (i) {
			case 0:
				if (prefs.getInt("hrZone1Min", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone1Min", 0);
				break;
			case 1:
				if (prefs.getInt("hrZone1Max", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone1Max", 0);
				break;
			case 2:
				if (prefs.getInt("hrZone2Min", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone2Min", 0);
				break;
			case 3:
				if (prefs.getInt("hrZone2Max", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone2Max", 0);
				break;
			case 4:
				if (prefs.getInt("hrZone3Min", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone3Min", 0);
				break;
			case 5:
				if (prefs.getInt("hrZone3Max", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone3Max", 0);
				break;
			case 6:
				if (prefs.getInt("hrZone4Min", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone4Min", 0);
				break;
			case 7:
				if (prefs.getInt("hrZone4Max", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone4Max", 0);
				break;
			case 8:
				if (prefs.getInt("hrZone5Min", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone5Min", 0);
				break;
			case 9:
				if (prefs.getInt("hrZone5Max", 0) > 0)
					hr_zones[i] = prefs.getInt("hrZone5Max", 0);
				break;
			}
		}

		DBManager = new DataManager(mContext);
		DBManager.Open();
		cursor = DBManager.getRowsFromTable(String.valueOf(id), "hr");
		cursor.moveToFirst();
		boolean isFirstRow = true;
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				hr = cursor.getInt(cursor.getColumnIndex("hr"));

				if (isFirstRow) {
					isFirstRow = false;
					min_hr = hr;
				}

				if ((hr_zones[0] > 0) && (hr_zones[2] > 0) && (hr_zones[4] > 0) && (hr_zones[6] > 0)
						&& (hr_zones[8] > 0)) {
					time = cursor.getInt(cursor.getColumnIndex("time"));

					if (hr < hr_zones[0]) {
						hr_zones_time[0] = hr_zones_time[0] + (time - lastTime);
						lastTime = time;
					} else if (hr < hr_zones[2]) {
						hr_zones_time[1] = hr_zones_time[1] + (time - lastTime);
						lastTime = time;
					} else if (hr < hr_zones[4]) {
						hr_zones_time[2] = hr_zones_time[2] + (time - lastTime);
						lastTime = time;
					} else if (hr < hr_zones[6]) {
						hr_zones_time[3] = hr_zones_time[3] + (time - lastTime);
						lastTime = time;
					} else if (hr < hr_zones[8]) {
						hr_zones_time[4] = hr_zones_time[4] + (time - lastTime);
						lastTime = time;
					} else if (hr >= hr_zones[8]) {
						hr_zones_time[5] = hr_zones_time[5] + (time - lastTime);
						lastTime = time;
					}

					if (hr > max_hr)
						max_hr = hr;

					if (hr < min_hr)
						min_hr = hr;

					hrList.add(hr);
					timeList.add(time);
				}

				cursor.moveToNext();
			}
		}
		cursor.close();
		DBManager.Close();

		paintGraphView();

		return fragmentView;
	}

	private void checkValues() {
		if (fc_type == 1)
			radio1.setChecked(true);
		else
			radio2.setChecked(true);
	}

	private void paintGraphView() {
		boolean paintChartView = true;

		mChartView = null;

		if ((hr_zones_time[0] == 0) && (hr_zones_time[1] == 0) && (hr_zones_time[2] == 0)
				&& (hr_zones_time[3] == 0) && (hr_zones_time[4] == 0) && (hr_zones_time[5] == 0)) {
			paintChartView = false;

			radio1.setVisibility(View.GONE);
			radio2.setVisibility(View.GONE);

			txt1.setVisibility(View.VISIBLE);
		}

		if (paintChartView) {
			radio1.setVisibility(View.VISIBLE);
			radio2.setVisibility(View.VISIBLE);
			txt1.setVisibility(View.GONE);

			layout.removeAllViews();

			if (fc_type == 1)
				pieChartView();
			else
				lineChartView();
		}
	}

	private void pieChartView() {
		int sumTime = hr_zones_time[0] + hr_zones_time[1] + hr_zones_time[2] + hr_zones_time[3]
				+ hr_zones_time[4] + hr_zones_time[5];
		double[] distribution = new double[6];
		distribution[0] = (hr_zones_time[0] * 100) / sumTime;
		distribution[1] = (hr_zones_time[1] * 100) / sumTime;
		distribution[2] = (hr_zones_time[2] * 100) / sumTime;
		distribution[3] = (hr_zones_time[3] * 100) / sumTime;
		distribution[4] = (hr_zones_time[4] * 100) / sumTime;
		distribution[5] = (hr_zones_time[5] * 100) / sumTime;

		final String[] status = new String[] {
				getString(R.string.hr_resting).toUpperCase(Locale.getDefault()) + " ("
						+ (int) distribution[0] + "%)",
				getString(R.string.hr_level1).toUpperCase(Locale.getDefault()) + " (" + (int) distribution[1]
						+ "%)",
				getString(R.string.hr_level2).toUpperCase(Locale.getDefault()) + " (" + (int) distribution[2]
						+ "%)",
				getString(R.string.hr_level3).toUpperCase(Locale.getDefault()) + " (" + (int) distribution[3]
						+ "%)",
				getString(R.string.hr_level4).toUpperCase(Locale.getDefault()) + " (" + (int) distribution[4]
						+ "%)",
				getString(R.string.hr_maximum).toUpperCase(Locale.getDefault()) + " ("
						+ (int) distribution[5] + "%)" };

		int[] colors = { Color.LTGRAY, Color.rgb(111, 183, 217), Color.rgb(54, 165, 54),
				Color.rgb(234, 206, 74), Color.rgb(246, 164, 83), Color.rgb(246, 103, 88) };

		TypedValue outValue1 = new TypedValue();
		TypedValue outValue2 = new TypedValue();
		mContext.getResources().getValue(R.dimen.pie_legend_text_size_value, outValue1, true);
		mContext.getResources().getValue(R.dimen.pie_labels_text_size_value, outValue2, true);
		float pieLegendTextSizeValue = outValue1.getFloat();
		float pieLabelTextSizeValue = outValue2.getFloat();

		final CategorySeries distributionSeries = new CategorySeries("");
		for (int i = 0; i < distribution.length; i++) {
			distributionSeries.add(status[i], distribution[i]);
		}

		final DefaultRenderer defaultRenderer = new DefaultRenderer();
		defaultRenderer.setShowLabels(true);
		defaultRenderer.setLegendTextSize(pieLegendTextSizeValue);
		defaultRenderer.setLabelsTextSize(pieLabelTextSizeValue);
		defaultRenderer.setZoomButtonsVisible(true);
		defaultRenderer.setStartAngle(180);
		defaultRenderer.setDisplayValues(false);
		defaultRenderer.setClickEnabled(true);
		defaultRenderer.setInScroll(true);
		for (int i = 0; i < distribution.length; i++) {
			SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
			seriesRenderer.setColor(colors[i]);
			seriesRenderer.setDisplayChartValues(true);
			seriesRenderer.setHighlighted(false);

			defaultRenderer.addSeriesRenderer(seriesRenderer);
		}

		mChartView = ChartFactory.getPieChartView(mContext, distributionSeries, defaultRenderer);

		mChartView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();

				if (seriesSelection == null) {
					for (int i = 0; i < distributionSeries.getItemCount(); i++) {
						defaultRenderer.getSeriesRendererAt(i).setHighlighted(false);
					}
					mChartView.repaint();
				} else {
					for (int i = 0; i < distributionSeries.getItemCount(); i++) {
						defaultRenderer.getSeriesRendererAt(i).setHighlighted(
								i == seriesSelection.getPointIndex());
					}
					mChartView.repaint();

					int selectedSerie = (int) seriesSelection.getPointIndex();
					String sZone = " " + getString(R.string.of_time) + " (";
					switch (selectedSerie) {
					case 0:
						if (time < 3600)
							sZone = sZone + FunctionUtils.shortFormatTime(hr_zones_time[0]);
						else
							sZone = sZone + FunctionUtils.longFormatTime(hr_zones_time[0]);

						sZone = sZone + ") " + getString(R.string.in_restring);
						break;
					case 1:
						if (time < 3600)
							sZone = sZone + FunctionUtils.shortFormatTime(hr_zones_time[1]);
						else
							sZone = sZone + FunctionUtils.longFormatTime(hr_zones_time[1]);

						sZone = sZone + ") " + getString(R.string.in_hr_range1);
						break;
					case 2:
						if (time < 3600)
							sZone = sZone + FunctionUtils.shortFormatTime(hr_zones_time[2]);
						else
							sZone = sZone + FunctionUtils.longFormatTime(hr_zones_time[2]);

						sZone = sZone + ") " + getString(R.string.in_hr_range2);
						break;
					case 3:
						if (time < 3600)
							sZone = sZone + FunctionUtils.shortFormatTime(hr_zones_time[3]);
						else
							sZone = sZone + FunctionUtils.longFormatTime(hr_zones_time[3]);

						sZone = sZone + ") " + getString(R.string.in_hr_range3);
						break;
					case 4:
						if (time < 3600)
							sZone = sZone + FunctionUtils.shortFormatTime(hr_zones_time[4]);
						else
							sZone = sZone + FunctionUtils.longFormatTime(hr_zones_time[4]);

						sZone = sZone + ") " + getString(R.string.in_hr_range4);
						break;
					case 5:
						if (time < 3600)
							sZone = sZone + FunctionUtils.shortFormatTime(hr_zones_time[5]);
						else
							sZone = sZone + FunctionUtils.longFormatTime(hr_zones_time[5]);

						sZone = sZone + ") " + getString(R.string.in_maximum_effort);
						break;
					}

					UIFunctionUtils.showMessage(mContext, true, (int) seriesSelection.getValue() + "%"
							+ sZone);
				}
			}
		});

		layout.addView(mChartView);
	}

	private void lineChartView() {
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

		XYMultipleSeriesDataset mDataset = getDataset();

		XYMultipleSeriesRenderer mRenderer = getRenderer();
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.TRANSPARENT);
		mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
		mRenderer.setAxisTitleTextSize(lineAxisTitleTextSizeValue);
		mRenderer.setChartTitleTextSize(lineCharTitleTextSizeValue);
		mRenderer.setLabelsTextSize(lineLabelsTextSizeValue);
		mRenderer.setLegendTextSize(lineLegendTextSizeValue);
		mRenderer.setMargins(new int[] { 12, 25, 12, 12 });
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setPointSize(10);
		mRenderer.setClickEnabled(true);

		mChartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);

		mChartView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
				if (seriesSelection != null) {
					UIFunctionUtils.showMessage(mContext, true, (int) seriesSelection.getValue() + " "
							+ getString(R.string.beats_per_minute));
				}
			}
		});

		layout.addView(mChartView);
	}

	private XYMultipleSeriesDataset getDataset() {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		XYSeries firstSeries = new XYSeries(getString(R.string.tab_summary4).toUpperCase(Locale.getDefault()));

		for (int i = 0; i < timeList.size(); i++) {
			firstSeries.add(FunctionUtils.customizedRound(((float) timeList.get(i) / 60), 1), hrList.get(i));
		}
		dataset.addSeries(firstSeries);

		return dataset;
	}

	private XYMultipleSeriesRenderer getRenderer() {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

		XYSeriesRenderer r = new XYSeriesRenderer();

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
		r.setDisplayChartValues(true);
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
		renderer.setYTitle(getString(R.string.beats));
		renderer.setXLabels(20);
		renderer.setYLabels(20);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setShowGrid(false);
		renderer.setXAxisMin((timeList.get(0) / 60));
		renderer.setXAxisMax((float) ((float) timeList.get(timeList.size() - 1) / 60));
		renderer.setYAxisMin(min_hr);
		renderer.setYAxisMax(max_hr);

		return renderer;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.RadioButton01:
			fc_type = 1;
			editor.putInt("fc_type", 1);
			editor.commit();

			checkValues();

			paintGraphView();
			break;
		case R.id.RadioButton02:
			fc_type = 2;
			editor.putInt("fc_type", 2);
			editor.commit();

			checkValues();

			paintGraphView();
			break;
		}
	}
}