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
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.util.Log;

import com.saulcintero.moveon.sensors.Sensor.SensorDataSet;

/**
 * BluetoothLeSensor represents a Bluetooth Smart sensor that supports reading
 * data from a Bluetooth GATT server using notifications.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BluetoothLeSensor {
	private static final String TAG = BluetoothLeSensor.class.getSimpleName();

	protected BluetoothLeSensor() {
	}

	/**
	 * UUID for descriptor to enable notifications when a characteristic's value
	 * changes.
	 */
	public static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	/**
	 * @return a UUID representing the characteristic.
	 */
	public abstract UUID name();

	/**
	 * Reads the current value of a characteristic from a Bluetooth Smart
	 * server. Note that this operation does not imply reading data from the
	 * peer; the characteristic will already have the latest value once it
	 * starts receiving notifications from the peer.
	 * 
	 * @param gatt
	 *            A GATT endpoint representing the remote server
	 * @param ch
	 *            The characteristic associated with {@link gatt}.
	 * @return A {@link SensorDataSet} representing the latest value.
	 */
	public abstract SensorDataSet read(BluetoothGatt gatt, BluetoothGattCharacteristic ch);

	/**
	 * Turns on notifications for characteristic value changes from a GATT
	 * server.
	 */
	public boolean subscribe(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
		if (!name().equals(ch.getUuid())) {
			return false;
		}

		// Turn on notification for the HRM_CHARACTERISTIC.
		if (!gatt.setCharacteristicNotification(ch, true)) {
			Log.e(TAG, "Failed to start notifier for characteristic " + name());
			return false;
		}

		// In addition, update the descriptor to indicate that notifications
		// are enabled.
		BluetoothGattDescriptor descriptor = ch.getDescriptor(BluetoothLeSensor.CLIENT_CHARACTERISTIC_CONFIG);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		if (!gatt.writeDescriptor(descriptor)) {
			Log.e(TAG, "Failed to write notification descriptor for characteristic: " + name());
			return false;
		}

		return true;
	}
}