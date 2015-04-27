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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.saulcintero.moveon.ExportPracticesTask;
import com.saulcintero.moveon.ImportPracticesTask;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.SummaryHolder;
import com.saulcintero.moveon.adapters.SeparatedListAdapter;
import com.saulcintero.moveon.adapters.ShareIntentListAdapter;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.enums.FilterTypes;
import com.saulcintero.moveon.listeners.IActionListener;
import com.saulcintero.moveon.osm.FileSenderFactory;
import com.saulcintero.moveon.osm.IFileSender;
import com.saulcintero.moveon.osm.OSMHelper;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.ui.widgets.myDatePickerDialog;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class History extends Fragment implements IActionListener {
	final static String TAG = History.class.getName().toString();

	private DataManager DBManager = null;
	private Cursor cursor = null;

	private Context mContext;
	private Activity act;

	private Resources res;

	private SeparatedListAdapter adapter;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private TextView hello_text, sessions_text, distance_text, distance_unit_text, time_text, calories_text;

	private ListView historyListView;

	private ArrayList<Integer> idList, headers_register, headers_session_counter, sectionList;

	private IntentFilter intentFilter, intentFilter2, intentFilter3, intentFilter4, intentFilter5,
			intentFilter6, intentFilter7, intentFilter8, intentFilter9, intentFilter10;

	private File folder;

	private View view;

	private int session_counter = 0, headers_pos, currentPosition = 0, top, pos, typeOfFilter = 0,
			activities_id = 0;

	private String[] files;

	private String[] headers, years;

	private int year1, year2, month1, month2, day1, day2, show_dialog;
	private String mYear1, mMonth1, mDay1, mYear2, mMonth2, mDay2;

	private ArrayList<CustomArrayAdapter> adapterList;

	private static String[] listOfFiles;
	private static ArrayList<String> osmUrlsToShare = new ArrayList<String>();
	private static ArrayList<String> osmFilesNameFromUploadPosition = new ArrayList<String>();
	private int osmFilesToUpload, osmFailedFilesToUpload, osmSuccessFilesToUpload;
	private int osmIdList[];
	private String osmNamesList[], osmActivitiesList[], osmShortDescriptionList[], osmLongDescriptionList[];

	private ArrayList<String> history_icon, history_data1, history_data2, history_data3, history_data4,
			history_data5, history_data6, nameList, headerList, yearList;

	private String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/moveon/images/";

	private String mTime, lastMonth = "";
	private String name;
	private int sessions, calories, accum_calories, avg_hr, id;
	private long time, accum_time;
	private float distance, accum_distance;

	private ProgressDialog pd = null;

	private ImportPracticesTask importTask;
	private ExportPracticesTask exportTask;

	private boolean taskCancelled;

	private UiLifecycleHelper uiHelper;

	private org.slf4j.Logger tracer;

	private BroadcastReceiver mReceiverRefreshRoutes = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshRoutes();
		}
	};

	private BroadcastReceiver mReceiverFilterRoutes = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			typeOfFilter = Integer.parseInt(intent.getStringExtra("typeOfFilter"));
			activities_id = Integer.parseInt(intent.getStringExtra("activities_id"));

			if ((typeOfFilter == FilterTypes.BY_DATE_CUSTOM_DATE.getTypes())
					|| (typeOfFilter == FilterTypes.BY_ACTIVITY_AND_DATE_CUSTOM_DATE.getTypes())) {
				show_dialog = 0;
				launchDatePickerDialog();
			} else {
				refreshRoutes();
				historyListView.setSelection(0);
			}
		}
	};

	private void launchDatePickerDialog() {
		myDatePickerDialog date = new myDatePickerDialog();

		Calendar calender = Calendar.getInstance();
		Bundle args = new Bundle();
		args.putInt("year", calender.get(Calendar.YEAR));
		args.putInt("month", calender.get(Calendar.MONTH));
		args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));

		switch (show_dialog) {
		case 0:
			date.setTitle(getString(R.string.start_date));
			break;
		case 1:
			date.setTitle(getString(R.string.end_date));
		}
		date.setArguments(args);
		date.setCallBack(ondate);
		date.show(getActivity().getSupportFragmentManager(), "Date Picker");
	}

	OnDateSetListener ondate = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			switch (show_dialog) {
			case 0:
				show_dialog += 1;

				year1 = year;
				month1 = monthOfYear + 1;
				day1 = dayOfMonth;

				launchDatePickerDialog();

				break;
			case 1:
				year2 = year;
				month2 = monthOfYear + 1;
				day2 = dayOfMonth;

				String pattern = "dd/MM/yyyy";
				SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
				try {
					Date date1 = sdf.parse(day1 + "/" + month1 + "/" + year1);
					Date date2 = sdf.parse(day2 + "/" + month2 + "/" + year2);

					if (date2.compareTo(date1) > -1) {
						refreshRoutes();
					} else {
						UIFunctionUtils.showMessage(mContext, true,
								getString(R.string.end_date_greater_than_start_date));
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};

	private BroadcastReceiver mReceiverExportRoutes = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final int id[] = intent.getExtras().getIntArray("idList");
			final int type = Integer.parseInt(intent.getStringExtra("type"));
			exportTask = null;

			DialogInterface.OnCancelListener dialogCancelled = new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					exportTask.cancel(true);
					UIFunctionUtils.showMessage(mContext, true, getString(R.string.export_canceled));

					exportTask = null;
				}
			};

			int coordinates_route = 0;
			DBManager = new DataManager(mContext);
			DBManager.Open();
			for (int t = 0; t < id.length; t++) {
				cursor = DBManager.CountRowsFromTable(String.valueOf(id[t]), "locations");
				cursor.moveToFirst();

				if (cursor.getCount() > 0) {
					coordinates_route = coordinates_route
							+ cursor.getInt(cursor.getColumnIndex("coordinates_route"));

					cursor.moveToNext();
				}
			}
			cursor.close();
			DBManager.Close();

			pd = new ProgressDialog(act);
			pd.setTitle(R.string.dialog_export_exporting);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax(coordinates_route);
			pd.setCancelable(false);
			pd.setOnCancelListener(dialogCancelled);
			pd.setMessage(getString(R.string.dialog_export_preparing));
			exportTask = new ExportPracticesTask(pd, act, mContext, type, id, false, null);
			exportTask.execute();
		}
	};

	private BroadcastReceiver mReceiverImportRoutes = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] files = intent.getExtras().getStringArray("files");
			String date = intent.getStringExtra("date");
			String time = intent.getStringExtra("time");
			boolean deleteOnFinish = intent.getBooleanExtra("deleteOnFinish", false);
			int activity = intent.getIntExtra("activity", 0);
			int type = intent.getIntExtra("type", 0);

			switch (type) {
			case 0:
				importRoute(activity, ImportPracticesTask.TYPE_GPX, date, time, files, deleteOnFinish);
				break;
			case 1:
				importRoute(activity, ImportPracticesTask.TYPE_KML, date, time, files, deleteOnFinish);
				break;
			}
		}
	};

	private void importRoute(int activity, int type, String date, String time, String[] files,
			boolean deleteOnFinish) {
		importTask = null;

		DialogInterface.OnCancelListener dialogCancelled = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				UIFunctionUtils.showMessage(mContext, true, getString(R.string.import_canceled));

				importTask = null;
			}
		};

		pd = new ProgressDialog(act);
		pd.setTitle(R.string.dialog_import_importing);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCancelable(false);
		pd.setOnCancelListener(dialogCancelled);
		pd.setMessage(getString(R.string.dialog_import_text));
		importTask = new ImportPracticesTask(pd, act, mContext, type, date, time, activity, files,
				deleteOnFinish);
		importTask.execute();
	}

	private BroadcastReceiver mReceiverExportAndShareRoutes = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			osmUrlsToShare.clear();
			osmFilesNameFromUploadPosition.clear();
			osmFilesToUpload = 0;
			osmFailedFilesToUpload = 0;
			osmSuccessFilesToUpload = 0;
			osmIdList = null;
			osmNamesList = null;
			osmActivitiesList = null;
			osmShortDescriptionList = null;
			osmLongDescriptionList = null;
			listOfFiles = null;
			exportTask = null;
			taskCancelled = false;

			osmIdList = intent.getExtras().getIntArray("idList");
			int type = Integer.parseInt(intent.getStringExtra("type"));
			osmNamesList = intent.getExtras().getStringArray("names");
			osmActivitiesList = intent.getExtras().getStringArray("activities");
			listOfFiles = intent.getExtras().getStringArray("files");
			osmShortDescriptionList = intent.getExtras().getStringArray("short_description");
			osmLongDescriptionList = intent.getExtras().getStringArray("long_description");

			osmFilesToUpload = listOfFiles.length;

			DialogInterface.OnCancelListener dialogCancelled = new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					taskCancelled = true;
					exportTask.cancel(true);
					UIFunctionUtils.showMessage(mContext, true, getString(R.string.export_canceled));

					exportTask = null;
				}
			};

			DialogInterface.OnDismissListener dialogDismissed = new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (!taskCancelled)
						UploadToOpenStreetMap(listOfFiles);
				}
			};

			int coordinates_route = 0;
			DBManager = new DataManager(mContext);
			DBManager.Open();
			for (int t = 0; t < osmIdList.length; t++) {
				cursor = DBManager.CountRowsFromTable(String.valueOf(osmIdList[t]), "locations");
				cursor.moveToFirst();

				if (cursor.getCount() > 0) {
					coordinates_route = coordinates_route
							+ cursor.getInt(cursor.getColumnIndex("coordinates_route"));

					cursor.moveToNext();
				}
			}
			cursor.close();
			DBManager.Close();

			pd = new ProgressDialog(act);
			pd.setTitle(R.string.dialog_export_exporting);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax(coordinates_route);
			pd.setCancelable(false);
			pd.setOnDismissListener(dialogDismissed);
			pd.setOnCancelListener(dialogCancelled);
			pd.setMessage(getString(R.string.dialog_export_preparing));
			exportTask = new ExportPracticesTask(pd, act, mContext, type, osmIdList, true, listOfFiles);
			exportTask.execute();
		}
	};

	private void UploadToOpenStreetMap(String[] files) {
		Intent settingsIntent = OSMHelper.GetOsmSettingsIntent(mContext);
		ShowFileListDialog(settingsIntent, FileSenderFactory.GetOsmSender(mContext, this), files);
	}

	private void ShowFileListDialog(final Intent settingsIntent, final IFileSender sender,
			final String[] files) {

		if (files.length > 0) {
			List<File> chosenFiles = new ArrayList<File>();

			for (Object item : files) {
				String f = item.toString() + ".gpx";
				tracer.info(getString(R.string.osm_selected_file_to_upload) + " " + f);
				chosenFiles.add(new File(Environment.getExternalStorageDirectory().toString()
						+ "/moveon/gpx/tmp/", f));
			}

			if (chosenFiles.size() > 0) {
				ShowProgress(act, getString(R.string.osm_publishing), getString(R.string.osm_please_wait));
				sender.UploadFile(chosenFiles);
			}
		} else {
			tracer.debug(getString(R.string.osm_sorry));
		}
	}

	private BroadcastReceiver mReceiverIncrementProgress = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (pd != null)
				pd.incrementProgressBy(1);
		}
	};

	private BroadcastReceiver mReceiverWriteInProgressDialog = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (pd != null)
				pd.setMessage(intent.getStringExtra("msg"));
		}
	};

	private BroadcastReceiver mReceiverPublishToFbWall = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			facebook(intent.getStringExtra("name"), intent.getStringExtra("msg"),
					intent.getStringExtra("link"));
		}
	};

	private BroadcastReceiver mReceiverShowOsmMessage = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			UIFunctionUtils.showMessage(mContext, true, intent.getStringExtra("msg"));
		}
	};

	private BroadcastReceiver mReceiverSendAction = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendAction(act, osmIdList, osmNamesList, osmActivitiesList, osmShortDescriptionList,
					osmLongDescriptionList);
		}
	};

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, getString(R.string.fb_logged_in));
		} else if (state.isClosed()) {
			Log.i(TAG, getString(R.string.fb_logged_out));
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				Log.e(TAG, String.format(getString(R.string.fb_error) + " %s", error.toString()));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				Log.i(TAG, getString(R.string.fb_success));
			}
		});
	}

	@Override
	public void onDestroy() {
		uiHelper.onDestroy();

		getActivity().unregisterReceiver(mReceiverRefreshRoutes);
		getActivity().unregisterReceiver(mReceiverFilterRoutes);
		getActivity().unregisterReceiver(mReceiverExportRoutes);
		getActivity().unregisterReceiver(mReceiverImportRoutes);
		getActivity().unregisterReceiver(mReceiverExportAndShareRoutes);
		getActivity().unregisterReceiver(mReceiverIncrementProgress);
		getActivity().unregisterReceiver(mReceiverWriteInProgressDialog);
		getActivity().unregisterReceiver(mReceiverPublishToFbWall);
		getActivity().unregisterReceiver(mReceiverShowOsmMessage);
		getActivity().unregisterReceiver(mReceiverSendAction);

		super.onDestroy();
	}

	public void onResume() {
		super.onResume();

		uiHelper.onResume();
	}

	public void onPause() {
		super.onPause();

		if ((pd != null) && pd.isShowing()) {
			pd.cancel();
			pd.dismiss();
		}
		pd = null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout fragmentView = (LinearLayout) inflater.inflate(R.layout.history, container, false);

		act = getActivity();
		mContext = act.getApplicationContext();
		res = getResources();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		tracer = LoggerFactory.getLogger(History.class.getSimpleName());

		uiHelper = new UiLifecycleHelper(act, callback);
		uiHelper.onCreate(savedInstanceState);

		hello_text = (TextView) fragmentView.findViewById(R.id.history_hello_item);
		distance_text = (TextView) fragmentView.findViewById(R.id.history_item_one);
		distance_unit_text = (TextView) fragmentView.findViewById(R.id.history_item_nine);
		sessions_text = (TextView) fragmentView.findViewById(R.id.history_item_two);
		time_text = (TextView) fragmentView.findViewById(R.id.history_item_three);
		calories_text = (TextView) fragmentView.findViewById(R.id.history_item_four);
		historyListView = (ListView) fragmentView.findViewById(R.id.list_routes);

		refreshRoutes();

		intentFilter = new IntentFilter("android.intent.action.REFRESH_ROUTES");
		getActivity().registerReceiver(mReceiverRefreshRoutes, intentFilter);

		intentFilter2 = new IntentFilter("android.intent.action.EXPORT_ROUTES");
		getActivity().registerReceiver(mReceiverExportRoutes, intentFilter2);

		intentFilter3 = new IntentFilter("android.intent.action.EXPORT_AND_SHARE_ROUTES");
		getActivity().registerReceiver(mReceiverExportAndShareRoutes, intentFilter3);

		intentFilter4 = new IntentFilter("android.intent.action.EXPORT_INCREMENT_PROGRESS");
		getActivity().registerReceiver(mReceiverIncrementProgress, intentFilter4);

		intentFilter5 = new IntentFilter("android.intent.action.WRITE_MSG_IN_DIALOG");
		getActivity().registerReceiver(mReceiverWriteInProgressDialog, intentFilter5);

		intentFilter6 = new IntentFilter("android.intent.action.FILTER_ROUTES");
		getActivity().registerReceiver(mReceiverFilterRoutes, intentFilter6);

		intentFilter7 = new IntentFilter("android.intent.action.PUBLISH_TO_FB_WALL");
		getActivity().registerReceiver(mReceiverPublishToFbWall, intentFilter7);

		intentFilter8 = new IntentFilter("android.intent.action.SHOW_OSM_MESSAGE");
		getActivity().registerReceiver(mReceiverShowOsmMessage, intentFilter8);

		intentFilter9 = new IntentFilter("android.intent.action.SHARE_SEND_ACTION");
		getActivity().registerReceiver(mReceiverSendAction, intentFilter9);

		intentFilter10 = new IntentFilter("android.intent.action.IMPORT_ROUTES");
		getActivity().registerReceiver(mReceiverImportRoutes, intentFilter10);

		historyListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
				int sum = 0;
				for (int p = 0; p < sectionList.size(); p++) {
					if (p <= position) {
						if (sectionList.get(p) == 1)
							sum = sum + 1;
					}
				}

				if (!prefs.getBoolean("blocked", false)) {
					if (!MoveOnService.getIsPracticeRunning()) {
						int lastAct = Integer.parseInt(idList.get(position - sum).toString());

						editor.putInt("selected_practice", lastAct);
						editor.commit();

						Intent intent = new Intent(act, SummaryHolder.class);
						startActivity(intent);
					} else {
						UIFunctionUtils.showMessage(mContext, false, getString(R.string.route_in_progress));
					}
				}
			}
		});

		historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long duration) {
				if (!prefs.getBoolean("blocked", false)) {
					if (!MoveOnService.getIsPracticeRunning()) {
						int sum = 0;
						for (int p = 0; p < sectionList.size(); p++) {
							if (p <= position) {
								if (sectionList.get(p) == 1)
									sum = sum + 1;
							}
						}

						int lastAct = Integer.parseInt(idList.get(position - sum).toString());

						editor.putInt("selected_practice", lastAct);
						editor.commit();

						UIFunctionUtils.createAlertDialog(act, 1, nameList.get(position - sum).toString());
					} else {
						UIFunctionUtils.showMessage(mContext, false, getString(R.string.end_route_before));
					}
				}
				return true;
			}
		});

		return fragmentView;
	}

	private void refreshRoutes() {
		boolean isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		if (!DataFunctionUtils.checkInformationInDB(mContext))
			hello_text.setVisibility(View.INVISIBLE);
		else
			hello_text.setVisibility(View.VISIBLE);

		List<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();

		idList = new ArrayList<Integer>();
		nameList = new ArrayList<String>();
		headerList = new ArrayList<String>();
		yearList = new ArrayList<String>();
		sectionList = new ArrayList<Integer>();
		headers_session_counter = new ArrayList<Integer>();

		adapterList = new ArrayList<CustomArrayAdapter>();

		accum_time = 0;
		accum_distance = 0;
		accum_calories = 0;
		pos = 0;
		sessions = 0;

		DBManager = new DataManager(mContext);
		DBManager.Open();
		FilterTypes whichFilter = FilterTypes.values()[typeOfFilter];
		switch (whichFilter) {
		case ALL_DESC:
			cursor = DBManager.getAll_DESC("routes");
			break;
		case BY_ACTIVITY:
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_activity),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE category_id = '"
									+ activities_id + "' ORDER BY iso_date DESC");
			break;
		case BY_DATE_THIS_YEAR:
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE substr(date,7) = '"
									+ Calendar.getInstance().get(Calendar.YEAR) + "' ORDER BY iso_date DESC");
			break;
		case BY_DATE_THIS_MONTH:
			getMonth();
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE substr(date,4,2) = '"
									+ mMonth1
									+ "' "
									+ "AND substr(date,7) = '"
									+ Calendar.getInstance().get(Calendar.YEAR) + "' ORDER BY iso_date DESC");
			break;
		case BY_DATE_THIS_WEEK:
			getWeek();
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE substr(date,7)||substr(date,4,2)||substr(date,1,2) "
									+ "BETWEEN '"
									+ mYear1
									+ mMonth1
									+ mDay1
									+ "' AND '"
									+ mYear2
									+ mMonth2
									+ mDay2 + "' ORDER BY iso_date DESC");
			break;
		case BY_DATE_CUSTOM_DATE:
			extractPartsOfDate();
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE substr(date,7)||substr(date,4,2)||substr(date,1,2) "
									+ "BETWEEN '"
									+ mYear1
									+ mMonth1
									+ mDay1
									+ "' AND '"
									+ mYear2
									+ mMonth2
									+ mDay2 + "' " + "ORDER BY iso_date DESC");
			break;
		case BY_ACTIVITY_AND_DATE_THIS_YEAR:
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_activity_and_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE category_id = '"
									+ activities_id
									+ "' "
									+ "AND substr(date,7) = '"
									+ Calendar.getInstance().get(Calendar.YEAR) + "' ORDER BY iso_date DESC");
			break;
		case BY_ACTIVITY_AND_DATE_THIS_MONTH:
			getMonth();
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_activity_and_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE category_id = '"
									+ activities_id
									+ "' "
									+ "AND substr(date,4,2) = '"
									+ mMonth1
									+ "' "
									+ "AND substr(date,7) = '"
									+ Calendar.getInstance().get(Calendar.YEAR)
									+ "' ORDER BY iso_date DESC");
			break;
		case BY_ACTIVITY_AND_DATE_THIS_WEEK:
			getWeek();
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_activity_and_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE category_id = '"
									+ activities_id
									+ "' "
									+ "AND substr(date,7)||substr(date,4,2)||substr(date,1,2) "
									+ "BETWEEN '"
									+ mYear1
									+ mMonth1
									+ mDay1
									+ "' AND '"
									+ mYear2
									+ mMonth2
									+ mDay2
									+ "' ORDER BY iso_date DESC");
			break;
		case BY_ACTIVITY_AND_DATE_CUSTOM_DATE:
			extractPartsOfDate();
			cursor = DBManager
					.CustomQuery(
							getString(R.string.db_query_select_records_by_activity_and_date),
							"SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date FROM routes WHERE category_id = '"
									+ activities_id
									+ "' "
									+ "AND substr(date,7)||substr(date,4,2)||substr(date,1,2) "
									+ "BETWEEN '"
									+ mYear1
									+ mMonth1
									+ mDay1
									+ "' AND '"
									+ mYear2
									+ mMonth2
									+ mDay2
									+ "' ORDER BY iso_date DESC");
			break;
		}
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			sessions = cursor.getCount();

			history_icon = new ArrayList<String>();
			history_data1 = new ArrayList<String>();
			history_data2 = new ArrayList<String>();
			history_data3 = new ArrayList<String>();
			history_data4 = new ArrayList<String>();
			history_data5 = new ArrayList<String>();
			history_data6 = new ArrayList<String>();

			headers_register = new ArrayList<Integer>();

			adapter = new SeparatedListAdapter(mContext);

			Locale currenLocale = res.getConfiguration().locale;

			// create headers Strings
			boolean isFirstEntry = true;
			while (!cursor.isAfterLast()) {
				String mDate = cursor.getString(cursor.getColumnIndex("date"));
				String[] splitDate = mDate.split("/");
				String currentMonth = FunctionUtils.getNameOfMonth(Integer.parseInt(splitDate[1]),
						currenLocale);

				if (isFirstEntry) {
					lastMonth = currentMonth;
					isFirstEntry = false;
				}

				if (currentMonth != lastMonth) {
					headers_session_counter.add(session_counter);
					session_counter = 1;
					lastMonth = currentMonth;
				} else {
					session_counter += 1;
				}

				pos += 1;

				if (pos == cursor.getCount())
					headers_session_counter.add(session_counter);

				cursor.moveToNext();
			}

			cursor.moveToFirst();
			pos = 0;
			session_counter = 0;
			headers_pos = 0;
			lastMonth = "";

			while (!cursor.isAfterLast()) {
				HashMap<String, String> hm = new HashMap<String, String>();

				id = cursor.getInt(cursor.getColumnIndex("_id"));

				time = Long.parseLong(String.valueOf(cursor.getInt(cursor.getColumnIndex("time"))));
				distance = cursor.getFloat(cursor.getColumnIndex("distance"));
				calories = cursor.getInt(cursor.getColumnIndex("kcal"));
				avg_hr = cursor.getInt(cursor.getColumnIndex("avg_hr"));
				name = cursor.getString(cursor.getColumnIndex("name"));

				accum_time = accum_time + time;
				accum_distance = accum_distance + distance;
				accum_calories = accum_calories + calories;

				String mDate = cursor.getString(cursor.getColumnIndex("date"));
				String[] splitDate = mDate.split("/");
				String currentMonth = FunctionUtils.getNameOfMonth(Integer.parseInt(splitDate[1]),
						currenLocale);
				String currentYear = FunctionUtils.getYear(mDate);

				if (headerList.size() == 0) {
					headerList.add(currentMonth);
					yearList.add(currentYear);
					lastMonth = currentMonth;
					sectionList.add(1);
					sectionList.add(0);
				}

				if (currentMonth != lastMonth) {
					sectionList.add(1);
					sectionList.add(0);

					headers_register.add(pos);

					headers = new String[headerList.size()];
					years = new String[headerList.size()];

					for (int m = 0; m <= (headerList.size() - 1); m++) {
						headers[m] = headerList.get(m).toString();
						years[m] = yearList.get(m).toString();
					}

					CustomArrayAdapter listadapter_history_rows = null;

					listadapter_history_rows = new CustomArrayAdapter(mContext, R.layout.history_list_row,
							history_icon, history_data1, history_data2, history_data3, history_data4,
							history_data5, history_data6);

					adapter.addSection(headers[headers_pos].substring(0, 1).toUpperCase(Locale.getDefault())
							+ headers[headers_pos].substring(1) + " " + years[headers_pos] + " ( "
							+ headers_session_counter.get(headers_pos) + " " + getString(R.string.practices)
							+ " )", listadapter_history_rows);

					adapterList.add(listadapter_history_rows);

					headerList.add(currentMonth);
					yearList.add(currentYear);
					lastMonth = currentMonth;

					headers_pos += 1;

					history_icon = new ArrayList<String>();
					history_data1 = new ArrayList<String>();
					history_data2 = new ArrayList<String>();
					history_data3 = new ArrayList<String>();
					history_data4 = new ArrayList<String>();
					history_data5 = new ArrayList<String>();
					history_data6 = new ArrayList<String>();
				} else {
					sectionList.add(0);
				}

				hm.put("history_list_icon",
						Integer.toString(res.getIdentifier("com.saulcintero.moveon:drawable/activity"
								+ (cursor.getInt(cursor.getColumnIndex("category_id")) + 1), null, null)));
				history_icon.add(Integer.toString(cursor.getInt(cursor.getColumnIndex("category_id"))));
				hm.put("history_data1", name);
				history_data1.add(name);
				if (time < 3600)
					mTime = FunctionUtils.shortFormatTime(time);
				else
					mTime = FunctionUtils.longFormatTime(time);
				hm.put("history_data2",
						String.valueOf((isMetric ? distance + " " + getString(R.string.long_unit1_detail_1)
								+ " " : FunctionUtils.getMilesFromKilometersWithTwoDecimals(distance) + " "
								+ getString(R.string.long_unit2_detail_1) + " "))
								+ getString(R.string.in) + " " + mTime);
				history_data2.add(String.valueOf((isMetric ? distance + " "
						+ getString(R.string.long_unit1_detail_1) + " " : FunctionUtils
						.getMilesFromKilometersWithTwoDecimals(distance)
						+ " "
						+ getString(R.string.long_unit2_detail_1) + " "))
						+ getString(R.string.in)
						+ " "
						+ mTime);
				hm.put("history_data3",
						String.valueOf(calories + " " + getString(R.string.tell_calories_setting_details)));
				history_data3.add(String.valueOf(calories + " "
						+ getString(R.string.tell_calories_setting_details)));
				hm.put("history_data4", String.valueOf(avg_hr + " " + getString(R.string.beats_per_minute)));
				history_data4.add(String.valueOf(avg_hr + " " + getString(R.string.beats_per_minute)));
				hm.put("history_data5", countPictures(id));
				history_data5.add(countPictures(id));
				hm.put("history_data6",
						cursor.getString(cursor.getColumnIndex("date")) + ", "
								+ (cursor.getString(cursor.getColumnIndex("hour")).substring(0, 5)));
				history_data6.add(cursor.getString(cursor.getColumnIndex("date")) + ", "
						+ (cursor.getString(cursor.getColumnIndex("hour")).substring(0, 5)));

				mList.add(hm);

				idList.add(id);
				nameList.add(cursor.getString(cursor.getColumnIndex("name")));

				if ((pos + 1) == cursor.getCount()) {
					if (currentMonth != lastMonth) {
						headerList.add(currentMonth);
						yearList.add(currentYear);
					}

					lastMonth = currentMonth;

					headers = new String[headerList.size()];
					years = new String[headerList.size()];

					for (int m = 0; m <= (headerList.size() - 1); m++) {
						headers[m] = headerList.get(m).toString();
						years[m] = yearList.get(m).toString();
					}

					CustomArrayAdapter listadapter_history_rows = null;

					listadapter_history_rows = new CustomArrayAdapter(mContext, R.layout.history_list_row,
							history_icon, history_data1, history_data2, history_data3, history_data4,
							history_data5, history_data6);

					adapter.addSection(headers[headers_pos].substring(0, 1).toUpperCase(Locale.getDefault())
							+ headers[headers_pos].substring(1) + " " + years[headers_pos] + " ( "
							+ headers_session_counter.get(headers_pos) + " práctica(s) )",
							listadapter_history_rows);

					adapterList.add(listadapter_history_rows);

					headers_pos += 1;
				}

				pos += 1;

				cursor.moveToNext();
			}
		} else {
			adapter = null;
		}
		cursor.close();
		DBManager.Close();

		currentPosition = historyListView.getFirstVisiblePosition();
		view = historyListView.getChildAt(0);
		top = (view == null) ? 0 : view.getTop();

		int sum = 0;
		for (int p = 0; p < sectionList.size(); p++) {
			if (currentPosition > p)
				sum = sum + Integer.parseInt(sectionList.get(p).toString());
		}

		historyListView.setAdapter(adapter);

		if (currentPosition > 0)
			historyListView.setSelectionFromTop(((currentPosition - 1) + sum), top);

		sessions_text.setText(String.valueOf(sessions));
		distance_text.setText(String.valueOf((isMetric ? FunctionUtils.customizedRound(accum_distance, 2)
				: FunctionUtils.customizedRound(((accum_distance * 1000f) / 1609f), 2))));
		distance_unit_text.setText(isMetric ? getString(R.string.long_unit1_detail_1).toUpperCase(
				Locale.getDefault()) : getString(R.string.long_unit2_detail_1).toUpperCase(
				Locale.getDefault()));
		time_text.setText(FunctionUtils.longFormatTime(accum_time));
		calories_text.setText(String.valueOf(accum_calories));
	}

	private void getWeek() {
		Calendar c1 = Calendar.getInstance();

		// first day of week
		c1.setFirstDayOfWeek(Calendar.MONDAY);
		c1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

		int y = c1.get(Calendar.YEAR);
		int m = c1.get(Calendar.MONTH) + 1;
		int d = c1.get(Calendar.DAY_OF_MONTH);

		mYear1 = String.valueOf(y);
		mMonth1 = String.valueOf(m);
		if (m < 10)
			mMonth1 = "0" + mMonth1;
		mDay1 = String.valueOf(d);
		if (d < 10)
			mDay1 = "0" + mDay1;

		// last day of week
		c1.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

		int y2 = c1.get(Calendar.YEAR);
		int m2 = c1.get(Calendar.MONTH) + 1;
		int d2 = c1.get(Calendar.DAY_OF_MONTH);

		mYear2 = String.valueOf(y2);
		mMonth2 = String.valueOf(m2);
		if (m2 < 10)
			mMonth2 = "0" + mMonth2;
		mDay2 = String.valueOf(d2);
		if (d2 < 10)
			mDay2 = "0" + mDay2;
	}

	private void getMonth() {
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		mMonth1 = String.valueOf(month);
		if (month < 10)
			mMonth1 = "0" + mMonth1;
	}

	private void extractPartsOfDate() {
		mYear1 = String.valueOf(year1);
		mMonth1 = String.valueOf(month1);
		if (month1 < 10)
			mMonth1 = "0" + mMonth1;
		mDay1 = String.valueOf(day1);
		if (day1 < 10)
			mDay1 = "0" + mDay1;

		mYear2 = String.valueOf(year2);
		mMonth2 = String.valueOf(month2);
		if (month2 < 10)
			mMonth2 = "0" + mMonth2;
		mDay2 = String.valueOf(day2);
		if (day2 < 10)
			mDay2 = "0" + mDay2;
	}

	private String countPictures(int id) {
		int counter = 0;
		folder = new File(file_path);
		files = folder.list();

		if (files != null) {
			for (int i = 0; i < files.length; i++) {

				String[] splitFile = files[i].split("_");

				if (splitFile[0].equals(String.valueOf(id))) {
					counter++;
				}
			}
		}

		return counter + " " + getString(R.string.pictures);
	}

	public void facebook(String name, String message, String link) {
		if (!FunctionUtils.checkNetwork(act)) {
			UIFunctionUtils.showMessage(mContext, true, getString(R.string.no_internet));
			return;
		}

		if (!FunctionUtils.checkFbInstalled(act)) {
			UIFunctionUtils.showMessage(mContext, true, getString(R.string.fb_not_installed));
			return;
		}

		UIFunctionUtils.showMessage(
				mContext,
				true,
				FunctionUtils.capitalizeFirtsLetter(getString(R.string.loading).toLowerCase(
						Locale.getDefault())));

		if (FacebookDialog.canPresentShareDialog(mContext, FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
			FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(act)
					.setName(name)
					.setLink(link)
					.setDescription(message)
					.setPicture(
							"http://wiki.openstreetmap.org/w/images/thumb/b/b0/Openstreetmap_logo.svg/80px-Openstreetmap_logo.svg.png")
					.build();

			uiHelper.trackPendingDialogCall(shareDialog.present());
		}
	}

	private class CustomArrayAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final ArrayList<String> icons, objects, objects2, objects3, objects4, objects5, objects6;

		public CustomArrayAdapter(Context context, int textViewResourceId, ArrayList<String> icons,
				ArrayList<String> objects, ArrayList<String> objects2, ArrayList<String> objects3,
				ArrayList<String> objects4, ArrayList<String> objects5, ArrayList<String> objects6) {
			super(context, textViewResourceId, objects);

			this.context = context;
			this.icons = icons;
			this.objects = objects;
			this.objects2 = objects2;
			this.objects3 = objects3;
			this.objects4 = objects4;
			this.objects5 = objects5;
			this.objects6 = objects6;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View row = inflater.inflate(R.layout.history_list_row, parent, false);

			ImageView icon = (ImageView) row.findViewById(R.id.history_list_icon);
			int j = res.getIdentifier("com.saulcintero.moveon:drawable/activity"
					+ icons.get(position).toString(), "drawable", context.getPackageName());
			icon.setImageResource(j);

			TextView item = (TextView) row.findViewById(R.id.history_route_name);
			item.setText(objects.get(position).toString());

			TextView item2 = (TextView) row.findViewById(R.id.history_data1);
			item2.setText(objects2.get(position).toString());

			TextView item3 = (TextView) row.findViewById(R.id.history_data2);
			item3.setText(objects3.get(position).toString());

			TextView item4 = (TextView) row.findViewById(R.id.history_data3);
			item4.setText(objects4.get(position).toString());

			TextView item5 = (TextView) row.findViewById(R.id.history_data4);
			item5.setText(objects5.get(position).toString());

			TextView item6 = (TextView) row.findViewById(R.id.history_route_date);
			item6.setText(objects6.get(position).toString());

			return row;
		}
	}

	@Override
	public void OnComplete(String url, String file) {
		tracer.debug(getString(R.string.osm_complete) + " " + osmSuccessFilesToUpload + "/"
				+ osmFilesToUpload);

		osmUrlsToShare.add(url);
		osmFilesNameFromUploadPosition.add(file);

		osmSuccessFilesToUpload += 1;

		if (osmFilesToUpload == (osmFailedFilesToUpload + osmSuccessFilesToUpload)) {
			DeleteRecursive(new File(Environment.getExternalStorageDirectory().toString()
					+ "/moveon/gpx/tmp/"));
			HideProgress();

			mContext.sendBroadcast(new Intent("android.intent.action.SHARE_SEND_ACTION"));
		}
	}

	@Override
	public void OnFailure() {
		tracer.debug(getString(R.string.osm_failure) + " " + osmFailedFilesToUpload + "/" + osmFilesToUpload);

		osmFailedFilesToUpload += 1;

		if (osmFilesToUpload == (osmFailedFilesToUpload + osmSuccessFilesToUpload)) {
			DeleteRecursive(new File(Environment.getExternalStorageDirectory().toString()
					+ "/moveon/gpx/tmp/"));
			HideProgress();

			if (osmSuccessFilesToUpload > 0)
				mContext.sendBroadcast(new Intent("android.intent.action.SHARE_SEND_ACTION"));
		}

		Intent i = new Intent("android.intent.action.SHOW_OSM_MESSAGE");
		i.putExtra("msg", getString(R.string.osm_error_uploading) + " " + osmFailedFilesToUpload + " "
				+ getString(R.string.osm_gps_trace));
		mContext.sendBroadcast(i);
	}

	private void ShowProgress(Context ctx, String title, String message) {
		if (ctx != null) {
			pd = new ProgressDialog(ctx, ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax(100);
			pd.setIndeterminate(true);
			pd = ProgressDialog.show(ctx, title, message, true, false);
		}
	}

	private void HideProgress() {
		if (pd != null)
			pd.dismiss();
	}

	private void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();
	}

	public static void sendAction(final Activity act, int[] idList, String[] nameList, String[] activityList,
			String[] shortDescriptionList, String[] longDescriptionList) {
		ArrayList<Integer> positions = new ArrayList<Integer>();
		for (int p = 0; p < idList.length; p++) {
			String nameToSearch = osmFilesNameFromUploadPosition.get(p).substring(0,
					osmFilesNameFromUploadPosition.get(p).length() - 4);
			positions.add(ArrayUtils.indexOf(listOfFiles, nameToSearch));
		}

		for (int j = 0; j < idList.length; j++) {
			final String name = nameList[j];
			final String activity = activityList[j];
			final String shortDescription = shortDescriptionList[j];
			final String longDescription = longDescriptionList[j];
			final String url = osmUrlsToShare.get(positions.get(j));
			Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
			sendIntent.setType("text/plain");
			List<ResolveInfo> activities = act.getPackageManager().queryIntentActivities(sendIntent, 0);
			AlertDialog.Builder builder = new AlertDialog.Builder(act);
			builder.setTitle(act.getText(R.string.send_to) + " " + name + " "
					+ act.getText(R.string.share_with));
			final ShareIntentListAdapter adapter = new ShareIntentListAdapter(act, R.layout.social_share,
					activities.toArray());
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ResolveInfo info = (ResolveInfo) adapter.getItem(which);
					if (info.activityInfo.packageName.contains("facebook")) {
						Intent i = new Intent("android.intent.action.PUBLISH_TO_FB_WALL");
						i.putExtra("name", activity + " " + name);
						i.putExtra("msg", String.format("%s", shortDescription));
						i.putExtra("link", String.format("%s", url));
						act.sendBroadcast(i);
					} else {
						Intent intent = new Intent(android.content.Intent.ACTION_SEND);
						intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
						intent.setType("text/plain");
						if ((info.activityInfo.packageName.contains("twitter"))
								|| (info.activityInfo.packageName.contains("sms"))
								|| (info.activityInfo.packageName.contains("mms"))) {
							intent.putExtra(Intent.EXTRA_TEXT, shortDescription + url);
						} else {
							intent.putExtra(Intent.EXTRA_TEXT, longDescription + url);
						}
						act.startActivity(intent);
					}
				}
			});
			builder.create().show();
		}
	}
}