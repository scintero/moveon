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
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.dsi.ant.AntInterface;
import com.saulcintero.moveon.osm.OSMHelper;
import com.saulcintero.moveon.sensors.ant.AntSensorManager;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.utils.BluetoothDeviceUtils;
import com.saulcintero.moveon.utils.TextToSpeechUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class MoveOnPreferences extends PreferenceActivity {
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private PreferenceActivity act;

	private ListPreference operationLevel, sensorType, sensor, sensitivityLevel, mListPrefTemp;

	private Preference hrMenu1, hrMenu2, antHr, antSpeedDistance, antBikeCadence, antCombinedBike, autoPause,
			osmSetup, exportMenu;

	private CheckBoxPreference speakCheckBox;

	private String lastSelection = "", lastSelection2 = "";

	private boolean hasAntSupport;

	private IntentFilter intentFilter_Export_Data_Toast, intentFilter_Unlock_Screen_Orientation;

	private ExportAllDataTask backupTask;

	private ProgressDialog progress;

	private BroadcastReceiver mReceiverExportDataToast = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int option = intent.getIntExtra("toast_option", 1);

			switch (option) {
			case 1:
				UIFunctionUtils.showMessage(act, false, getString(R.string.backup_completed_successfully));
				break;
			case 2:
				UIFunctionUtils.showMessage(act, false, getString(R.string.backup_error));
				break;
			}
		}
	};

	private BroadcastReceiver mReceiverUnlockScreenOrientation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			UIFunctionUtils.unlockScreenOrientation(act);
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiverExportDataToast);
		unregisterReceiver(mReceiverUnlockScreenOrientation);
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkSensorOptionsStatus();
		showBluetoothSensorInListpreference();
	}

	public void onPause() {
		super.onPause();

		if ((progress != null) && progress.isShowing()) {
			progress.cancel();
			progress.dismiss();
		}
		progress = null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		act = this;

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		operationLevel = (ListPreference) findPreference("operation_level");
		sensorType = (ListPreference) findPreference("sensor_type_key");
		sensor = (ListPreference) findPreference("sensor");
		sensitivityLevel = (ListPreference) findPreference("sensitivity_level");
		hrMenu1 = (Preference) findPreference("hr_menu1");
		hrMenu2 = (Preference) findPreference("hr_menu2");
		antHr = (Preference) findPreference("settingsSensorAntResetHeartRateMonitor");
		antSpeedDistance = (Preference) findPreference("settingsSensorAntResetSpeedDistanceMonitor");
		antBikeCadence = (Preference) findPreference("settingsSensorAntResetBikeCadenceSensor");
		antCombinedBike = (Preference) findPreference("settingsSensorAntResetCombinedBikeSensor");
		autoPause = (Preference) findPreference("auto_pause");
		osmSetup = (Preference) findPreference("osm_setup");
		exportMenu = (Preference) findPreference("export_menu");
		speakCheckBox = (CheckBoxPreference) findPreference("speak");

		exportMenu.setSummary(getString(R.string.backup_in) + " '"
				+ Environment.getExternalStorageDirectory().getParent() + "/moveon/moveon_backup.zip'");

		hasAntSupport = AntInterface.hasAntSupport(this);

		checkAutoPauseStatus();
		checkBackupStatus();

		updateUiBySensorType(PreferenceManager.getDefaultSharedPreferences(this).getString("sensor_type_key",
				getString(R.string.none)));

		antHr.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateUiBySensorType((String) newValue);

				return true;
			}

		});

		intentFilter_Export_Data_Toast = new IntentFilter("android.intent.action.EXPORT_DATA_TOAST");
		intentFilter_Unlock_Screen_Orientation = new IntentFilter(
				"android.intent.action.UNLOCK_SCREEN_ORIENTATION");

		registerReceiver(mReceiverExportDataToast, intentFilter_Export_Data_Toast);
		registerReceiver(mReceiverUnlockScreenOrientation, intentFilter_Unlock_Screen_Orientation);

		operationLevel.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				lastSelection = "";
				lastSelection = prefs.getString("operation_level", "run_in_background");

				return false;
			}
		});

		operationLevel.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (lastSelection != newValue.toString()) {
					editor = prefs.edit();
					editor.putString("operation_level", newValue.toString());
					editor.commit();

					if (newValue.toString().equals("run_in_background")) {
						operationLevel.setValueIndex(0);
					} else if (newValue.toString().equals("keep_screen_on")) {
						operationLevel.setValueIndex(1);
					} else if (newValue.toString().equals("wake_up")) {
						operationLevel.setValueIndex(2);
					}

					Intent i = new Intent("android.intent.action.ACTION_SCREEN_REFRESH");
					sendBroadcast(i);
				}

				return false;
			}
		});

		sensorType.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				lastSelection2 = "";
				lastSelection2 = sensorType.getValue();

				checkSensorOptionsStatus();
				showBluetoothSensorInListpreference();

				sendBroadcast(new Intent("android.intent.action.EMPTY_SENSOR_MANAGER"));

				return false;
			}
		});

		sensorType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Constants.setString(act, R.string.sensor_type_key, newValue.toString());

				if (lastSelection2.compareTo(newValue.toString()) != 0) {
					if (newValue.toString().equals(getString(R.string.polar_sensor_type))) // polar
					{
						sensorType.setValue(getString(R.string.polar_sensor_type));

					} else if (newValue.toString().equals(getString(R.string.zephyr_sensor_type))) { // zephyr
						sensorType.setValue(getString(R.string.zephyr_sensor_type));
					} else if (newValue.toString().equals(getString(R.string.btle_sensor_type))) { // bluetooth
																									// smart
						if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
							sensorType.setValue(getString(R.string.btle_sensor_type));
						} else {
							UIFunctionUtils.showMessage(act, false, getString(R.string.btle_not_supported));
						}
					} else if (newValue.toString().equals(getString(R.string.ant_sensor_type))) { // ant+
						if (hasAntSupport) {
							sensorType.setValue(getString(R.string.ant_sensor_type));
						} else {
							UIFunctionUtils.showMessage(act, false,
									getString(R.string.ant_plus_not_supported));
						}
					} else { // none
						sensorType.setValue(getString(R.string.none));
					}
				}

				checkSensorOptionsStatus();
				showBluetoothSensorInListpreference();

				return false;
			}
		});

		sensitivityLevel.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				editor = prefs.edit();
				editor.putString("sensitivity_level", newValue.toString());
				editor.commit();

				sendBroadcast(new Intent("android.intent.action.ACTION_REFRESH_SENSITIVITY"));

				return true;
			}
		});

		hrMenu1.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent settingsIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
				startActivity(settingsIntent);

				return true;
			}
		});

		hrMenu2.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(act, BtleDevices.class));

				return true;
			}
		});

		exportMenu.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MoveOnPreferences.this);
				alert.setTitle(getString(R.string.backup_title) + ":");
				alert.setMessage(getString(R.string.backup_msg));
				alert.setCancelable(true);

				alert.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						DialogInterface.OnCancelListener dialogCancelled = new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								backupTask.cancel(true);
								UIFunctionUtils.showMessage(act, true, getString(R.string.backup_canceled));

								backupTask = null;

								UIFunctionUtils.unlockScreenOrientation(act);
							}
						};

						UIFunctionUtils.lockScreenOrientation(act);

						progress = new ProgressDialog(MoveOnPreferences.this);
						progress.setMessage(getString(R.string.wait_until_data_is_exported));
						progress.setCancelable(false);
						progress.setOnCancelListener(dialogCancelled);
						backupTask = new ExportAllDataTask(progress, MoveOnPreferences.this);
						backupTask.execute();
					}
				});
				alert.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});

				AlertDialog dialog = alert.create();
				dialog.show();

				return true;
			}
		});

		osmSetup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (preference.getKey().equals("osm_setup")) {
					startActivity(OSMHelper.GetOsmSettingsIntent(act));
					return true;
				}

				return false;
			}
		});

		speakCheckBox.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (TextToSpeechUtils.getIfHasErrors()) {
					UIFunctionUtils.showMessage(getBaseContext(), false,
							getBaseContext().getString(R.string.tts_language_is_not_available));

					speakCheckBox.setChecked(false);

					return false;
				} else {
					return true;
				}
			}
		});
	}

	private void checkAutoPauseStatus() {
		if (MoveOnService.getIsPracticeRunning()) {
			autoPause.setEnabled(false);
		} else {
			autoPause.setEnabled(true);
		}
	}

	private void checkBackupStatus() {
		if (MoveOnService.getIsPracticeRunning()) {
			exportMenu.setEnabled(false);
		} else {
			exportMenu.setEnabled(true);
		}
	}

	private void checkSensorOptionsStatus() {
		boolean isBluetoothClassic = (PreferenceManager.getDefaultSharedPreferences(this).getString(
				"sensor_type_key", getString(R.string.none)).equals(getString(R.string.polar_sensor_type)))
				|| (PreferenceManager.getDefaultSharedPreferences(this).getString("sensor_type_key",
						getString(R.string.none)).equals(getString(R.string.zephyr_sensor_type)));

		boolean isBluetoothSmart = (PreferenceManager.getDefaultSharedPreferences(this).getString(
				"sensor_type_key", getString(R.string.none)).equals(getString(R.string.btle_sensor_type)));

		boolean isAnt = (PreferenceManager.getDefaultSharedPreferences(this).getString("sensor_type_key",
				getString(R.string.none)).equals(getString(R.string.ant_sensor_type)));

		if (isBluetoothClassic) {
			sensor.setEnabled(true);
			hrMenu1.setEnabled(true);
			hrMenu2.setEnabled(false);
			antHr.setEnabled(false);
			antSpeedDistance.setEnabled(false);
			antBikeCadence.setEnabled(false);
			antCombinedBike.setEnabled(false);
		} else if (isBluetoothSmart) {
			sensor.setEnabled(false);
			hrMenu1.setEnabled(false);
			hrMenu2.setEnabled(true);
			antHr.setEnabled(false);
			antSpeedDistance.setEnabled(false);
			antBikeCadence.setEnabled(false);
			antCombinedBike.setEnabled(false);
		} else if (isAnt) {
			sensor.setEnabled(false);
			hrMenu1.setEnabled(false);
			hrMenu2.setEnabled(false);
			antHr.setEnabled(true);
			antSpeedDistance.setEnabled(true);
			antBikeCadence.setEnabled(true);
			antCombinedBike.setEnabled(true);
		} else {
			sensor.setEnabled(false);
			hrMenu1.setEnabled(false);
			hrMenu2.setEnabled(false);
			antHr.setEnabled(false);
			antSpeedDistance.setEnabled(false);
			antBikeCadence.setEnabled(false);
			antCombinedBike.setEnabled(false);
		}
	}

	private void showBluetoothSensorInListpreference() {
		List<String> optionsList = new ArrayList<String>();
		List<String> valuesList = new ArrayList<String>();

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			BluetoothDeviceUtils.populateDeviceLists(bluetoothAdapter, optionsList, valuesList);
		}

		String[] options = optionsList.toArray(new String[optionsList.size()]);
		String[] values = valuesList.toArray(new String[valuesList.size()]);

		String value = Constants.getString(this, R.string.bluetooth_sensor_key,
				Constants.BLUETOOTH_SENSOR_DEFAULT);

		configureListPreference(sensor, options, options, values, value);
	}

	protected void configureListPreference(ListPreference listPreference, final String[] summary,
			final String[] options, final String[] values, String value) {
		listPreference.setEntryValues(values);
		listPreference.setEntries(options);
		mListPrefTemp = listPreference;
		listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference pref, Object newValue) {
				updatePreferenceSummary(pref, summary, values, (String) newValue);
				return true;
			}
		});

		updatePreferenceSummary(listPreference, summary, values, value);
	}

	private void updatePreferenceSummary(Preference preference, String[] summary, String[] values,
			String value) {
		int index = getIndex(values, value);
		if (index == -1) {
			preference.setSummary(R.string.dash);
		} else {
			mListPrefTemp.setSummary(summary[index]);
			mListPrefTemp.setValue(values[index]);
			Constants.setString(this, R.string.bluetooth_sensor_key, values[index]);
		}
	}

	private int getIndex(String[] values, String value) {
		for (int i = 0; i < values.length; i++) {
			if (value.equals(values[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Updates the UI based on the sensor type.
	 * 
	 * @param sensorType
	 *            the sensor type
	 */
	private void updateUiBySensorType(String sensorType) {
		boolean isAnt = getString(R.string.settings_sensor_type_ant).equals(sensorType);

		updateAntSensor(R.string.settings_sensor_ant_reset_heart_rate_monitor_key,
				R.string.ant_heart_rate_monitor_id_key, isAnt);
		updateAntSensor(R.string.settings_sensor_ant_reset_speed_distance_monitor_key,
				R.string.ant_speed_distance_monitor_id_key, isAnt);
		updateAntSensor(R.string.settings_sensor_ant_reset_bike_cadence_sensor_key,
				R.string.ant_bike_cadence_sensor_id_key, isAnt);
		updateAntSensor(R.string.settings_sensor_ant_reset_combined_bike_sensor_key,
				R.string.ant_combined_bike_sensor_id_key, isAnt);
	}

	/**
	 * Updates an ant sensor.
	 * 
	 * @param preferenceKey
	 *            the preference key
	 * @param valueKey
	 *            the value key
	 * @param enabled
	 *            true if enabled
	 */
	@SuppressWarnings("deprecation")
	private void updateAntSensor(int preferenceKey, final int valueKey, boolean enabled) {
		Preference preference = findPreference(getString(preferenceKey));
		if (preference != null) {
			preference.setEnabled(enabled);
			int deviceId = Constants.getInt(this, valueKey, AntSensorManager.WILDCARD);
			if (deviceId == AntSensorManager.WILDCARD) {
				preference.setSummary(R.string.settings_sensor_ant_not_connected);
			} else {
				preference.setSummary(getString(R.string.settings_sensor_ant_paired, deviceId));
			}
			preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference pref) {
					Constants.setInt(getBaseContext(), valueKey, AntSensorManager.WILDCARD);
					pref.setSummary(R.string.settings_sensor_ant_not_connected);
					return true;
				}
			});
		}
	}
}