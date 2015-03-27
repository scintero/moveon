/*
 * Copyright 2012 Google Inc.
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

package com.saulcintero.moveon.sensors.ant;

import android.content.Context;
import android.content.Intent;

/**
 * Contains the latest ant sensor value.
 *
 * @author Jimmy Shih
 */
public class AntSensorValue {

	private int heartRate = -1;
	private int cadence = -1;

	private Context context;

	public AntSensorValue(Context context) {
		this.context = context;
	}

	/**
	 * Gets the heart rate.
	 */
	public int getHeartRate() {
		Intent i = new Intent("android.intent.action.BEAT_COUNTER");
		i.putExtra("beats", String.valueOf(heartRate));
		context.sendBroadcast(i);

		return heartRate;
	}

	/**
	 * Sets the heart rate.
	 *
	 * @param heartRate
	 *            the heart rate to set
	 */
	public void setHeartRate(int heartRate) {
		this.heartRate = heartRate;
	}

	/**
	 * Gets the cadence.
	 */
	public int getCadence() {
		Intent i = new Intent("android.intent.action.CADENCE");
		i.putExtra("cadence", String.valueOf(cadence));
		context.sendBroadcast(i);

		return cadence;
	}

	/**
	 * Sets the cadence.
	 *
	 * @param cadence
	 *            the cadence to set
	 */
	public void setCadence(int cadence) {
		this.cadence = cadence;
	}
}
