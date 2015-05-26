/*
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

package com.saulcintero.moveon.sensors.btle;

import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.util.Log;

import com.saulcintero.moveon.sensors.Sensor.SensorData;
import com.saulcintero.moveon.sensors.Sensor.SensorDataSet;
import com.saulcintero.moveon.sensors.Sensor.SensorState;

/**
 * LeHrmSensor extracts sensor data received from a Bluetooth Smart HRM server
 * such as a heart rate monitor.
 * 
 * @see <a href=
 *      'https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_m
 *      e a s u r e m e n t . x m l ' > HRM Characteristic specification at
 *      bluetooth.org</a>
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class LeHrmSensor extends BluetoothLeSensor {
	private static final String TAG = LeHrmSensor.class.getSimpleName();

	private static final UUID HRM_CHARACTERISTIC = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");

	// Bitmask to figure out whether heart rates are UINT8 or UINT16.
	private static final int HR_FMT_BITMASK = 0x01;

	int hr = 0;

	public static final LeHrmSensor INSTANCE = new LeHrmSensor();

	public LeHrmSensor() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * public LeHrmSensor() { // TODO Auto-generated constructor stub }
	 **/

	@Override
	public UUID name() {
		return HRM_CHARACTERISTIC;
	}

	public int hr() {
		return hr;
	}

	/**
	 * Read populates a {@link SensorDataSet} with content read from the
	 * Bluetooth HRM server. Bluetooth Smart HRM servers are required to support
	 * notifications per <a href=
	 * 'https://www.bluetooth.org/docman/handlers/downloaddoc.ashx?doc_id=239866
	 * ' > HRM Specification</a>.
	 */
	@Override
	public SensorDataSet read(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
		byte[] val = ch.getValue();
		int format = (((int) val[0] & HR_FMT_BITMASK) == 0 ? BluetoothGattCharacteristic.FORMAT_UINT8
				: BluetoothGattCharacteristic.FORMAT_UINT16);

		// Heart rate (unit BPM) is at offset 1.
		hr = ch.getIntValue(format, 1);

		// TODO(dgupta): read RR intervals once we can log them.

		// TODO(dgupta): debug: remove this logging.
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			String valStr = "";
			for (byte b : val) {
				valStr += " . " + b;
			}
			Log.d(TAG, "Read characteristic: heart rate = " + hr + ", byteval = " + valStr);
		}

		SensorData.Builder datum = SensorData.newBuilder().setState(SensorState.SENDING).setValue(hr);
		SensorDataSet dataset = SensorDataSet.newBuilder().setCreationTime(System.currentTimeMillis())
				.setHeartRate(datum).build();

		return dataset;
	}
}