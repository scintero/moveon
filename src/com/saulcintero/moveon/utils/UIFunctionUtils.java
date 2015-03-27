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

package com.saulcintero.moveon.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.saulcintero.moveon.EditPractice;
import com.saulcintero.moveon.ImportAllDataTask;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.SummaryHolder;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.entities.EntityShoe;
import com.saulcintero.moveon.osm.OSMHelper;

@SuppressLint("InflateParams")
public class UIFunctionUtils {
	private static final int DIALOG_HISTORY_OPTIONS = 1;
	private static final int DIALOG_ADD_SHOE = 2;
	private static final int DIALOG_SHOES_OPTIONS = 3;
	private static final int DIALOG_IMPORT_DB = 4;
	private static final int DIALOG_EXPORT_FORMATS = 5;

	private static final int GPX = 0, KML = 1, KMZ = 2;

	private static int DATE_DIALOG_ID = 0, TIME_DIALOG_ID = 1;
	private static int seconds, minutes, hours, day, month, year;
	private static int selected_activity = 0;

	private static boolean deleteOnFinish;

	public static void showMessage(Context context, boolean isShortMsg, String msg) {
		Toast toast;
		toast = (isShortMsg ? Toast.makeText(context, msg, Toast.LENGTH_SHORT) : Toast.makeText(context, msg,
				Toast.LENGTH_LONG));
		toast.show();
	}

	@SuppressWarnings("deprecation")
	public static void lockScreenOrientation(Activity act) {
		Display display = act.getWindowManager().getDefaultDisplay();
		int rotation = display.getRotation();
		int height;
		int width;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			height = display.getHeight();
			width = display.getWidth();
		} else {
			Point size = new Point();
			display.getSize(size);
			height = size.y;
			width = size.x;
		}
		switch (rotation) {
		case Surface.ROTATION_90:
			if (width > height)
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			else
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			break;
		case Surface.ROTATION_180:
			if (height > width)
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			else
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			break;
		case Surface.ROTATION_270:
			if (width > height)
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			else
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		default:
			if (height > width)
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			else
				act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	public static void unlockScreenOrientation(Activity act) {
		act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	public static void createAlertDialog(final Context mContext, int id, final String item) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		switch (id) {
		case DIALOG_HISTORY_OPTIONS:
			AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
			alert.setTitle(mContext.getString(R.string.select_action));

			final CharSequence[] choiceList = { mContext.getString(R.string.alertdialog_option_1),
					mContext.getString(R.string.alertdialog_option_2),
					mContext.getString(R.string.alertdialog_option_3),
					mContext.getString(R.string.alertdialog_option_7),
					mContext.getString(R.string.alertdialog_option_4) };

			alert.setItems(choiceList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						Intent intent = new Intent(mContext, SummaryHolder.class);
						mContext.startActivity(intent);
						break;
					case 1:
						Intent intent2 = new Intent(mContext, EditPractice.class);
						mContext.startActivity(intent2);
						break;
					case 2:
						int[] iArray = new int[1];
						iArray[0] = prefs.getInt("selected_practice", 0);
						exportPracticeDialog(mContext, iArray);
						break;
					case 3:
						if (!OSMHelper.IsOsmAuthorized(mContext))
							createOsmAuthAlertDialog(mContext);
						else
							exportAndShareSimpleRoute(mContext, prefs.getInt("selected_practice", 0));
						break;
					case 4:
						confirmDialog(mContext, item, 1);
						break;
					}

					dialog.dismiss();
				}
			});

			AlertDialog dialog = alert.create();
			dialog.show();
			break;
		case DIALOG_ADD_SHOE:
			alert = new AlertDialog.Builder(mContext);
			alert.setTitle(mContext.getString(R.string.add_new_shoe));
			alert.setCancelable(true);

			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			View alertLayout = inflater.inflate(R.layout.shoe_custom_dialog, null);
			alert.setView(alertLayout);

			final EditText brand_model = (EditText) alertLayout.findViewById(R.id.shoe_brand_and_model);
			final EditText distance = (EditText) alertLayout.findViewById(R.id.shoe_distance);
			final CheckBox isDefault = (CheckBox) alertLayout.findViewById(R.id.default_shoe);

			distance.setText(mContext.getString(R.string.zero_value));
			isDefault.setChecked(true);

			alert.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if ((brand_model.getText().toString().length() > 0)
							&& (distance.getText().toString().length() > 0)) {
						ArrayList<EntityShoe> shoeList = new ArrayList<EntityShoe>();
						EntityShoe shoe = new EntityShoe();

						shoe.setName(brand_model.getText().toString());
						shoe.setDistance(FunctionUtils.checkIfUnitsAreMetric(mContext) ? distance.getText()
								.toString() : String.valueOf(FunctionUtils.getKilometersFromMiles(Float
								.valueOf(distance.toString()))));
						shoe.setActive("1");
						if (isDefault.isChecked())
							shoe.setDefault_shoe(String.valueOf("1"));
						else
							shoe.setDefault_shoe(String.valueOf("0"));

						shoeList.add(shoe);

						DataFunctionUtils.createShoeInDB(mContext, shoeList);
					} else {
						showMessage(mContext, false, mContext.getString(R.string.add_new_shoe_info_missing));
					}
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
		case DIALOG_SHOES_OPTIONS:
			alert = new AlertDialog.Builder(mContext);

			final CharSequence[] optionsList = { mContext.getString(R.string.shoes_listview_options_1),
					mContext.getString(R.string.shoes_listview_options_2),
					mContext.getString(R.string.shoes_listview_options_3) };

			alert.setTitle(mContext.getString(R.string.select_action));
			alert.setItems(optionsList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						DataManager DBManager = new DataManager(mContext);
						DBManager.Open();
						DBManager.Edit("default_shoe", "0", "shoes");
						DBManager.Edit(prefs.getInt("selected_shoe", 0), "default_shoe", "1", "shoes");
						DBManager.Close();

						mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_SHOES"));
						break;
					case 1:
						String[] data = DataFunctionUtils.getShoeData(mContext,
								prefs.getInt("selected_shoe", 0),
								FunctionUtils.checkIfUnitsAreMetric(mContext));
						editDialog(mContext, data, prefs.getInt("selected_shoe", 0));
						break;
					case 2:
						confirmDialog(mContext, item, 2);
						break;
					}

					dialog.dismiss();
				}
			});

			dialog = alert.create();
			dialog.show();
			break;
		case DIALOG_EXPORT_FORMATS:
			alert = new AlertDialog.Builder(mContext);
			alert.setTitle(mContext.getString(R.string.select_action));

			final CharSequence[] exportList = { mContext.getString(R.string.file_format_1),
					mContext.getString(R.string.file_format_2), mContext.getString(R.string.file_format_3) };

			alert.setItems(exportList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
					}

					dialog.dismiss();
				}
			});

			dialog = alert.create();
			dialog.show();
			break;
		}
	}

	public static void createAlertDialog(final Activity act, int id) {
		switch (id) {
		case DIALOG_IMPORT_DB:
			AlertDialog.Builder alert = new AlertDialog.Builder(act);
			alert.setCancelable(false);
			alert.setIcon(android.R.drawable.ic_dialog_alert);
			alert.setTitle(act.getString(R.string.confirm_import_action));
			alert.setMessage(act.getString(R.string.restore_backup_confirmation));

			alert.setPositiveButton(act.getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					FunctionUtils.appHasAlreadyBeenLaunched(act);

					ProgressDialog progress = new ProgressDialog(act);
					progress.setMessage(act.getString(R.string.wait_restoring_backup));
					progress.setCancelable(false);
					new ImportAllDataTask(progress, act).execute();

					dialog.dismiss();
				}

			});

			alert.setNegativeButton(act.getString(R.string.no), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					FunctionUtils.appHasAlreadyBeenLaunched(act);

					dialog.dismiss();
				}
			});

			AlertDialog dialog = alert.create();
			dialog.show();
			break;
		}
	}

	public static void createOsmAuthAlertDialog(final Context mContext) {
		AlertDialog.Builder osmAlert = new AlertDialog.Builder(mContext);
		AlertDialog osmDialog;
		osmAlert.setTitle(mContext.getString(R.string.osm_authorization));
		osmAlert.setMessage(mContext.getString(R.string.osm_gps_traces_authorization));
		osmAlert.setCancelable(true);
		osmAlert.setPositiveButton(mContext.getString(R.string.osm_authorization_form),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mContext.startActivity(OSMHelper.GetOsmSettingsIntent(mContext));
					}
				});
		osmDialog = osmAlert.create();
		osmDialog.show();
	}

	private static void confirmDialog(final Context mContext, String item, final int option) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.setTitle(mContext.getString(R.string.delete_confirm_action));

		String msg = "";
		switch (option) {
		case 1:
			msg = mContext.getString(R.string.delete_practice_confirm_action) + " '" + item + "'?";
			break;
		case 2:
			msg = mContext.getString(R.string.delete_shoe_confirm_action) + " '" + item + "'?";
			break;
		}
		alert.setMessage(msg);

		alert.setPositiveButton(mContext.getString(R.string.alertdialog_option_4),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DataManager DBManager = new DataManager(mContext);
						DBManager.Open();
						switch (option) {
						case 1:
							DBManager.Delete(prefs.getInt("selected_practice", 0), "routes");
							DBManager.Delete(prefs.getInt("selected_practice", 0), "locations");

							mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_ROUTES"));
							mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_STATISTICS"));
							break;
						case 2:
							if (prefs.getInt("default_shoe", 0) == 0)
								DBManager.Edit(prefs.getInt("selected_shoe", 0), "active", "0", "shoes");
							else
								showMessage(mContext, false,
										mContext.getString(R.string.error_deleting_default_shoe));

							mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_SHOES"));
							break;
						}
						DBManager.Close();

						dialog.dismiss();
					}
				});

		alert.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		AlertDialog dialog = alert.create();
		dialog.show();
	}

	private static void editDialog(final Context mContext, String[] items, final int option) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(mContext.getString(R.string.edit_shoe));
		alert.setCancelable(true);

		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		View alertLayout = inflater.inflate(R.layout.shoe_custom_dialog, null);
		alert.setView(alertLayout);

		final EditText brand_model = (EditText) alertLayout.findViewById(R.id.shoe_brand_and_model);
		final EditText distance = (EditText) alertLayout.findViewById(R.id.shoe_distance);
		final CheckBox isDefault = (CheckBox) alertLayout.findViewById(R.id.default_shoe);

		brand_model.setText(items[0]);
		distance.setText(items[1]);
		if (items[2].equals("0"))
			isDefault.setChecked(false);
		else
			isDefault.setChecked(true);

		alert.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if ((brand_model.getText().toString().length() > 0)
						&& (distance.getText().toString().length() > 0)) {
					DataManager DBManager = new DataManager(mContext);
					DBManager.Open();
					DBManager.Edit(prefs.getInt("selected_shoe", 0), "name",
							brand_model.getText().toString(), "shoes");
					DBManager.Edit(prefs.getInt("selected_shoe", 0), "distance", distance.getText()
							.toString(), "shoes");
					if (isDefault.isChecked()) {
						DBManager.Edit("default_shoe", "0", "shoes");
						DBManager.Edit(prefs.getInt("selected_shoe", 0), "default_shoe", String.valueOf("1"),
								"shoes");
					} else {
						DBManager.Edit(prefs.getInt("selected_shoe", 0), "default_shoe", String.valueOf("0"),
								"shoes");
					}
					DBManager.Close();
					mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_SHOES"));
				} else {
					showMessage(mContext, false, mContext.getString(R.string.add_new_shoe_info_missing));
				}
			}
		});
		alert.setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});

		AlertDialog dialog = alert.create();
		dialog.show();
	}

	public static void importPracticeDialog(final Context mContext) {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(mContext.getString(R.string.select_format));

		final CharSequence[] importList = { mContext.getString(R.string.file_format_1),
				mContext.getString(R.string.file_format_2) };

		alert.setItems(importList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					importRoutesDialogWithCheckBox(mContext, GPX);
					break;
				case 1:
					importRoutesDialogWithCheckBox(mContext, KML);
					break;
				}

				dialog.dismiss();
			}
		});

		AlertDialog dialog = alert.create();
		dialog.show();
	}

	public static void importRoutesDialogWithCheckBox(final Context mContext, final int type) {
		List<String> listItems = new ArrayList<String>();

		String path = Environment.getExternalStorageDirectory().toString();
		String extension = "";
		switch (type) {
		case GPX:
			path += "/moveon/gpx/";
			extension = ".gpx";
			break;
		case KML:
			path += "/moveon/kml/";
			extension = ".kml";
			break;
		}

		File f = new File(path);
		File files[] = f.listFiles();

		if (files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().contains(extension))
					listItems.add(files[i].getName());
			}

			Collections.sort(listItems, String.CASE_INSENSITIVE_ORDER);

			final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
			final boolean[] itemsChecked = new boolean[items.length];

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mContext.getString(R.string.practices_to_import));
			builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean check = false;
					for (int i = 0; i < items.length; i++) {
						if (itemsChecked[i])
							check = true;
					}

					if (check) {
						List<String> selectedFiles = new ArrayList<String>();
						for (int i = 0; i < itemsChecked.length; i++) {
							if (itemsChecked[i])
								selectedFiles.add(items[i].toString());
						}

						CharSequence[] files = selectedFiles.toArray(new CharSequence[selectedFiles.size()]);

						showFileDetailsDialog(mContext, files, type);
					} else {
						showMessage(mContext, true, mContext.getString(R.string.files_no_selected));
					}
				}

				private void showFileDetailsDialog(final Context mContext, final CharSequence[] items,
						final int type) {
					AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
					AlertDialog dialog;
					Resources res;
					Spinner spinner;
					CheckBox checkbox;
					final EditText date, time;
					deleteOnFinish = false;

					alert.setTitle(mContext.getString(R.string.activity_to_apply));
					alert.setCancelable(true);

					LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
					View alertLayout = inflater.inflate(R.layout.import_task_previous_details, null);
					alert.setView(alertLayout);

					spinner = (Spinner) alertLayout.findViewById(R.id.spinner1);
					checkbox = (CheckBox) alertLayout.findViewById(R.id.checkBox1);
					date = (EditText) alertLayout.findViewById(R.id.datetxt);
					time = (EditText) alertLayout.findViewById(R.id.time);

					final Calendar cal = Calendar.getInstance();
					year = cal.get(Calendar.YEAR);
					month = cal.get(Calendar.MONTH);
					day = cal.get(Calendar.DAY_OF_MONTH);
					hours = cal.get(Calendar.HOUR_OF_DAY);
					minutes = cal.get(Calendar.MINUTE);
					seconds = cal.get(Calendar.SECOND);
					String sHours = String.valueOf(hours);
					String sMinutes = String.valueOf(minutes);
					String sSeconds = String.valueOf(seconds);
					if (hours < 10)
						sHours = "0" + sHours;
					if (minutes < 10)
						sMinutes = "0" + sMinutes;
					if (seconds < 10)
						sSeconds = "0" + sSeconds;
					updateDisplay(DATE_DIALOG_ID, date, time);
					updateDisplay(TIME_DIALOG_ID, time, time);

					spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
						public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
							selected_activity = i;
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					});

					checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							deleteOnFinish = isChecked;
						}
					});

					res = mContext.getResources();
					final String[] activities = res.getStringArray(R.array.activities);

					ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext,
							android.R.layout.simple_spinner_item, activities);
					spinnerArrayAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(spinnerArrayAdapter);

					alert.setPositiveButton(mContext.getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									if (FunctionUtils.dateValidator(date.getText().toString())
											&& FunctionUtils.time24HoursValidator(time.getText().toString())) {
										Intent i = new Intent("android.intent.action.IMPORT_ROUTES");
										i.putExtra("files",
												FunctionUtils.fromCharsequenceArrayToArrayOfString(items));
										i.putExtra("activity", selected_activity);
										i.putExtra("type", type);
										i.putExtra("date", date.getText().toString().replace(" ", ""));
										i.putExtra("time", time.getText().toString().replace(" ", ""));
										i.putExtra("deleteOnFinish", deleteOnFinish);
										mContext.sendBroadcast(i);
									} else {
										showMessage(mContext, false,
												mContext.getString(R.string.date_time_wrong_values));
									}
								}
							});
					alert.setNegativeButton(mContext.getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
								}
							});

					dialog = alert.create();
					dialog.show();
				}
			});

			boolean[] values = new boolean[items.length];
			for (int g = 0; g <= (items.length - 1); g++) {
				values[g] = false;
			}

			builder.setMultiChoiceItems(items, values, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					itemsChecked[which] = isChecked;
				}
			});
			builder.show();
		} else {
			showMessage(mContext, false, mContext.getString(R.string.no_files_to_import_in_directory) + " '"
					+ path + "'");
		}
	}

	private static void updateDisplay(int DIALOG_ID, TextView date, TextView time) {
		switch (DIALOG_ID) {
		case 0:
			String sDay = String.valueOf(day),
			sMonth = String.valueOf(month + 1);
			int mMonth = month;

			if (day < 10)
				sDay = "0" + sDay;

			mMonth = month + 1;
			if (month < 10)
				sMonth = "0" + mMonth;

			date.setText(new StringBuilder().append(sDay).append("/").append(sMonth).append("/").append(year)
					.append(" "));
		case 1:
			String sHours = String.valueOf(hours);
			String sMinutes = String.valueOf(minutes);
			String sSeconds = String.valueOf(seconds);
			if (hours < 10)
				sHours = "0" + sHours;
			if (minutes < 10)
				sMinutes = "0" + sMinutes;
			if (seconds < 10)
				sSeconds = "0" + sSeconds;

			time.setText(new StringBuilder().append(sHours).append(":").append(sMinutes).append(":")
					.append(sSeconds).append(" "));
		}
	}

	public static void exportRoutesDialogWithCheckBox(final Context mContext) {
		List<String> listItems = new ArrayList<String>();
		final List<Integer> id = new ArrayList<Integer>();
		final int[] idList;

		DataManager DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.getAll_DESC("routes");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			idList = new int[cursor.getCount()];
			int pos = 0;

			while (!cursor.isAfterLast()) {
				idList[pos] = cursor.getInt(cursor.getColumnIndex("_id"));
				listItems.add(cursor.getString(cursor.getColumnIndex("name")));

				pos++;
				cursor.moveToNext();
			}
			cursor.close();
			DBManager.Close();

			final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
			final boolean[] itemsChecked = new boolean[items.length];

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mContext.getString(R.string.practices_to_export));
			builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					for (int i = 0; i < items.length; i++) {
						if (itemsChecked[i])
							id.add(idList[i]);
					}

					int iArray[] = new int[id.size()];
					for (Integer i : id)
						iArray[id.indexOf(i)] = i;

					if (iArray.length > 0)
						exportPracticeDialog(mContext, iArray);
				}
			});

			boolean[] values = new boolean[cursor.getCount()];
			for (int g = 0; g <= (cursor.getCount() - 1); g++) {
				values[g] = false;
			}

			builder.setMultiChoiceItems(items, values, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					itemsChecked[which] = isChecked;
				}
			});
			builder.show();
		}
	}

	public static void exportPracticeDialog(final Context mContext, final int[] idList) {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(mContext.getString(R.string.select_format));

		final CharSequence[] exportList = { mContext.getString(R.string.file_format_1),
				mContext.getString(R.string.file_format_2), mContext.getString(R.string.file_format_3) };

		alert.setItems(exportList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent("android.intent.action.EXPORT_ROUTES");
				switch (which) {
				case 0:
					intent.putExtra("type", String.valueOf(GPX));
					break;
				case 1:
					intent.putExtra("type", String.valueOf(KML));
					break;
				case 2:
					intent.putExtra("type", String.valueOf(KMZ));
					break;
				}
				intent.putExtra("idList", idList);
				mContext.sendBroadcast(intent);

				dialog.dismiss();
			}
		});

		AlertDialog dialog = alert.create();
		dialog.show();
	}

	public static void exportAndShareSimpleRoute(final Context mContext, int routeId) {
		final List<String> listItems = new ArrayList<String>();
		final List<String> listFilesName = new ArrayList<String>();
		final List<String> listFilesActivities = new ArrayList<String>();
		final List<String> listShortDescription = new ArrayList<String>();
		final List<String> listLongDescription = new ArrayList<String>();
		final List<Integer> ids = new ArrayList<Integer>();
		final List<String> names = new ArrayList<String>();
		final List<String> activities = new ArrayList<String>();
		final List<String> files = new ArrayList<String>();
		final List<String> short_description = new ArrayList<String>();
		final List<String> long_description = new ArrayList<String>();
		final int[] idList;

		Resources res = mContext.getResources();
		String[] listOfActivities = res.getStringArray(R.array.activities);

		DataManager DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.CustomQuery(mContext.getString(R.string.getting_route_with_id) + " '"
				+ routeId + "'", "SELECT * FROM routes WHERE _id = '" + routeId + "'");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			idList = new int[1];

			while (!cursor.isAfterLast()) {
				idList[0] = routeId;

				listItems.add(cursor.getString(cursor.getColumnIndex("name")));

				listFilesActivities.add("("
						+ listOfActivities[(cursor.getInt(cursor.getColumnIndex("category_id")) - 1)] + ") ");

				String[] time = cursor.getString(cursor.getColumnIndex("hour")).split(":");
				String item = cursor.getString(cursor.getColumnIndex("date")).replace("/", "-") + " "
						+ time[0] + "h" + time[1] + "m" + time[2] + "s";
				listFilesName.add(item);

				String sTime = "";
				long prep_time = (long) cursor.getInt(cursor.getColumnIndex("time"));
				if (prep_time < 3600)
					sTime = FunctionUtils.shortFormatTTSTime(mContext, prep_time);
				else
					sTime = FunctionUtils.longFormatTTSTime(mContext, prep_time);

				boolean isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

				String description_part1 = (isMetric ? cursor.getFloat(cursor.getColumnIndex("distance"))
						: FunctionUtils.getMilesFromKilometersWithTwoDecimals(cursor.getFloat(cursor
								.getColumnIndex("distance"))))
						+ " "
						+ (isMetric ? mContext.getString(R.string.long_unit1_detail_1) : mContext
								.getString(R.string.long_unit2_detail_1))
						+ " "
						+ mContext.getString(R.string.in)
						+ " "
						+ sTime
						+ " "
						+ mContext.getString(R.string.with_moveon) + " ." + "\n\n";
				String description_part2 = mContext.getString(R.string.ritm_label_complete)
						+ ": "
						+ FunctionUtils.calculateRitm(mContext,
								Long.parseLong(String.valueOf(cursor.getInt(cursor.getColumnIndex("time")))),
								String.valueOf(cursor.getFloat(cursor.getColumnIndex("distance"))), isMetric,
								false)
						+ " "
						+ (isMetric ? mContext.getString(R.string.long_unit1_detail_3) : mContext
								.getString(R.string.long_unit2_detail_3))
						+ ", "
						+ mContext.getString(R.string.avg_label_complete)
						+ ": "
						+ FunctionUtils.customizedRound(
								(isMetric ? cursor.getFloat(cursor.getColumnIndex("avg_speed")) : ((cursor
										.getFloat(cursor.getColumnIndex("avg_speed")) * 1000f) / 1609f)), 2)
						+ " "
						+ (isMetric ? mContext.getString(R.string.long_unit1_detail_2) : mContext
								.getString(R.string.long_unit2_detail_2)) + ", "
						+ mContext.getString(R.string.calories_burned) + ": "
						+ cursor.getInt(cursor.getColumnIndex("kcal")) + " "
						+ mContext.getString(R.string.tell_calories_setting_details) + ".\n\n";
				listShortDescription.add(description_part1);
				listLongDescription.add(description_part1 + description_part2
						+ mContext.getString(R.string.click_to_view_or_download_gps_trace) + "\n\n");

				ids.add(idList[0]);
				names.add(listItems.get(0));
				activities.add(listFilesActivities.get(0));
				files.add(listFilesName.get(0));
				short_description.add(listShortDescription.get(0));
				long_description.add(listLongDescription.get(0));

				cursor.moveToNext();
			}
			cursor.close();
			DBManager.Close();

			int idArray[] = new int[ids.size()];
			for (Integer i : ids)
				idArray[ids.indexOf(i)] = i;

			if (idArray.length > 0) {
				exportAction(mContext, idArray, names, activities, files, short_description, long_description);
			}
		}
	}

	public static void exportAndShareMultipleRoutesDialog(final Context mContext) {
		final List<String> listItems = new ArrayList<String>();
		final List<String> listFilesName = new ArrayList<String>();
		final List<String> listFilesActivities = new ArrayList<String>();
		final List<String> listShortDescription = new ArrayList<String>();
		final List<String> listLongDescription = new ArrayList<String>();
		final List<Integer> ids = new ArrayList<Integer>();
		final List<String> names = new ArrayList<String>();
		final List<String> activities = new ArrayList<String>();
		final List<String> files = new ArrayList<String>();
		final List<String> short_description = new ArrayList<String>();
		final List<String> long_description = new ArrayList<String>();
		final int[] idList;

		Resources res = mContext.getResources();
		String[] listOfActivities = res.getStringArray(R.array.activities);

		DataManager DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.getAll_DESC("routes");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			idList = new int[cursor.getCount()];
			int pos = 0;

			while (!cursor.isAfterLast()) {
				idList[pos] = cursor.getInt(cursor.getColumnIndex("_id"));

				listItems.add(cursor.getString(cursor.getColumnIndex("name")));

				listFilesActivities.add("("
						+ listOfActivities[(cursor.getInt(cursor.getColumnIndex("category_id")) - 1)] + ") ");

				String[] time = cursor.getString(cursor.getColumnIndex("hour")).split(":");
				String item = cursor.getString(cursor.getColumnIndex("date")).replace("/", "-") + " "
						+ time[0] + "h" + time[1] + "m" + time[2] + "s";
				listFilesName.add(item);

				String sTime = "";
				long prep_time = (long) cursor.getInt(cursor.getColumnIndex("time"));
				if (prep_time < 3600)
					sTime = FunctionUtils.shortFormatTTSTime(mContext, prep_time);
				else
					sTime = FunctionUtils.longFormatTTSTime(mContext, prep_time);

				boolean isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

				String description_part1 = (isMetric ? cursor.getFloat(cursor.getColumnIndex("distance"))
						: FunctionUtils.getMilesFromKilometersWithTwoDecimals(cursor.getFloat(cursor
								.getColumnIndex("distance"))))
						+ " "
						+ (isMetric ? mContext.getString(R.string.long_unit1_detail_1) : mContext
								.getString(R.string.long_unit2_detail_1))
						+ " "
						+ mContext.getString(R.string.in)
						+ " "
						+ sTime
						+ " "
						+ mContext.getString(R.string.with_moveon) + " ." + "\n\n";
				String description_part2 = mContext.getString(R.string.ritm_label_complete)
						+ ": "
						+ FunctionUtils.calculateRitm(mContext,
								Long.parseLong(String.valueOf(cursor.getInt(cursor.getColumnIndex("time")))),
								String.valueOf(cursor.getFloat(cursor.getColumnIndex("distance"))), isMetric,
								false)
						+ " "
						+ (isMetric ? mContext.getString(R.string.long_unit1_detail_3) : mContext
								.getString(R.string.long_unit2_detail_3))
						+ ", "
						+ mContext.getString(R.string.avg_label_complete)
						+ ": "
						+ FunctionUtils.customizedRound(
								(isMetric ? cursor.getFloat(cursor.getColumnIndex("avg_speed")) : ((cursor
										.getFloat(cursor.getColumnIndex("avg_speed")) * 1000f) / 1609f)), 2)
						+ " "
						+ (isMetric ? mContext.getString(R.string.long_unit1_detail_2) : mContext
								.getString(R.string.long_unit2_detail_2)) + ", "
						+ mContext.getString(R.string.calories_burned) + ": "
						+ cursor.getInt(cursor.getColumnIndex("kcal")) + " "
						+ mContext.getString(R.string.tell_calories_setting_details) + ".\n\n";
				listShortDescription.add(description_part1);
				listLongDescription.add(description_part1 + description_part2
						+ mContext.getString(R.string.click_to_view_or_download_gps_trace) + "\n\n");

				pos++;
				cursor.moveToNext();
			}
			cursor.close();
			DBManager.Close();

			final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
			final boolean[] itemsChecked = new boolean[items.length];

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(mContext.getString(R.string.practices_to_share));
			builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					for (int i = 0; i < items.length; i++) {
						if (itemsChecked[i]) {
							ids.add(idList[i]);
							names.add(listItems.get(i));
							activities.add(listFilesActivities.get(i));
							files.add(listFilesName.get(i));
							short_description.add(listShortDescription.get(i));
							long_description.add(listLongDescription.get(i));
						}
					}

					int idArray[] = new int[ids.size()];
					for (Integer i : ids)
						idArray[ids.indexOf(i)] = i;

					Collections.reverse(names);
					Collections.reverse(activities);
					Collections.reverse(files);
					Collections.reverse(short_description);
					Collections.reverse(long_description);

					if (idArray.length > 0) {
						exportAction(mContext, idArray, names, activities, files, short_description,
								long_description);
					}
				}
			});

			boolean[] values = new boolean[cursor.getCount()];
			for (int g = 0; g <= (cursor.getCount() - 1); g++) {
				values[g] = false;
			}

			builder.setMultiChoiceItems(items, values, new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					itemsChecked[which] = isChecked;
				}
			});
			builder.show();
		}
	}

	private static void exportAction(final Context mContext, final int[] idList, final List<String> nameList,
			final List<String> activityList, final List<String> fileList,
			final List<String> shortDescriptionList, final List<String> longDescriptionList) {
		String[] names = new String[nameList.size()];
		String[] activities = new String[activityList.size()];
		String[] files = new String[fileList.size()];
		String[] short_description = new String[shortDescriptionList.size()];
		String[] long_description = new String[longDescriptionList.size()];
		names = nameList.toArray(names);
		activities = activityList.toArray(activities);
		files = fileList.toArray(files);
		short_description = shortDescriptionList.toArray(short_description);
		long_description = longDescriptionList.toArray(long_description);

		Intent intent = new Intent("android.intent.action.EXPORT_AND_SHARE_ROUTES");
		intent.putExtra("type", "0");
		intent.putExtra("idList", idList);
		intent.putExtra("names", names);
		intent.putExtra("activities", activities);
		intent.putExtra("files", files);
		intent.putExtra("short_description", short_description);
		intent.putExtra("long_description", long_description);
		mContext.sendBroadcast(intent);
	}
}