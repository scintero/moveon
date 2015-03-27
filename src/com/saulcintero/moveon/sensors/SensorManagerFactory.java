/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.saulcintero.moveon.sensors;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.sensors.ant.AntSensorManager;
import com.saulcintero.moveon.sensors.btle.BluetoothLeManager;

/**
 * A factory of SensorManagers.
 * 
 * @author Sandor Dornbush
 */
public class SensorManagerFactory {

	private static SensorManager systemSensorManager = null;
	private static SensorManager tempSensorManager = null;

	private SensorManagerFactory() {
	}

	/**
	 * Gets the system sensor manager.
	 * 
	 * @param context
	 *            the context
	 */
	public static SensorManager getSystemSensorManager(Context context) {
		releaseTempSensorManager();
		releaseSystemSensorManager();
		systemSensorManager = getSensorManager(context, true);
		if (systemSensorManager != null) {
			systemSensorManager.startSensor();
		}
		return systemSensorManager;
	}

	/**
	 * Releases the system sensor manager.
	 */
	public static void releaseSystemSensorManager() {
		if (systemSensorManager != null) {
			systemSensorManager.stopSensor();
		}
		systemSensorManager = null;
	}

	/**
	 * Gets the temp sensor manager.
	 * 
	 * @param context
	 */
	public static SensorManager getTempSensorManager(Context context) {
		releaseTempSensorManager();
		if (systemSensorManager != null) {
			return null;
		}
		tempSensorManager = getSensorManager(context, false);
		if (tempSensorManager != null) {
			tempSensorManager.startSensor();
		}
		return tempSensorManager;
	}

	/**
	 * Releases the temp sensor manager.
	 */
	public static void releaseTempSensorManager() {
		if (tempSensorManager != null) {
			tempSensorManager.stopSensor();
		}
		tempSensorManager = null;
	}

	/**
	 * Gets the sensor manager.
	 * 
	 * @param context
	 *            the context
	 */
	private static SensorManager getSensorManager(Context context, boolean sendPageViews) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		String sensorType = pref.getString("sensor_type_key", context.getString(R.string.none));

		if (sensorType == null) {
			return null;
		} else if (sensorType.equals(context.getString(R.string.polar_sensor_type))) {
			return new PolarSensorManager(context);
		} else if (sensorType.equals(context.getString(R.string.zephyr_sensor_type))) {
			return new ZephyrSensorManager(context);
		} else if (sensorType.equals(context.getString(R.string.ant_sensor_type))) {
			return new AntSensorManager(context);
		} else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
				&& sensorType.equals(context.getString(R.string.btle_sensor_type))) {
			return new BluetoothLeManager(context);
		}

		return null;
	}
}