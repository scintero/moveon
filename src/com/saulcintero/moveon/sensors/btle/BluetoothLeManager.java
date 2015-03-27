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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.common.collect.ImmutableMap;
import com.saulcintero.moveon.Constants;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.sensors.Sensor.SensorDataSet;
import com.saulcintero.moveon.sensors.Sensor.SensorState;
import com.saulcintero.moveon.sensors.SensorManager;

/**
 * BluetoothLeManager implements a sensor manager for Bluetooth LE (Smart)
 * sensors.
 * 
 * @see <a href=
 *      'http://developer.android.com/guide/topics/connectivity/bluetooth-le.htm
 *      l ' > Android Bluetooth LE Support</a>
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeManager extends SensorManager {
	private static final String TAG = BluetoothLeManager.class.getSimpleName();

	/**
	 * Mapping of sensor characteristic UUIDs with a {@link BluetoothLeSensor}
	 * that knows how to convert reads of that characteristic into a
	 * {@link SensorDataSet}.
	 */
	static final Map<UUID, BluetoothLeSensor> BTLE_CHARACTERISTIC_MAP = ImmutableMap.of(
			LeHrmSensor.INSTANCE.name(), (BluetoothLeSensor) LeHrmSensor.INSTANCE);

	private static final Object SYNC = new Object();

	private final Map<String, BluetoothGatt> leSensorMap = new HashMap<String, BluetoothGatt>(5);
	private final GattCallback leCallback = new GattCallback();
	private final Context mContext;
	private final BluetoothAdapter mAdapter;
	private SensorDataSet dataset;

	/**
	 * GattCallback is an inner class that implements callbacks needed to
	 * support device/service discovery and handling value change notifications.
	 */
	class GattCallback extends BluetoothGattCallback implements BluetoothAdapter.LeScanCallback {
		/**
		 * Invoked when LE scan finds a new device. The handler attempts to
		 * initiate a GATT connection.
		 */
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			Log.i(TAG, "Found LE device " + device.getName() + ", will attempt to connectGatt");
			device.connectGatt(mContext, true, this);
		}

		/**
		 * Invoked when GATT connection state changes. When disconnected, it
		 * removes the device from the known GATT device map. When connected,
		 * initiates service discovery on the device.
		 */
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			BluetoothDevice device = gatt.getDevice();
			Log.i(TAG, "Device = " + device.getName() + ", addr: " + device.getAddress());
			if (status != BluetoothGatt.GATT_SUCCESS) {
				Log.w(TAG, "Gatt status = %d" + status);
				return;
			}
			Log.i(TAG, "New state = " + newState);
			switch (newState) {
			case BluetoothProfile.STATE_DISCONNECTED:
				Log.i(TAG, "Disconnected: removing Gatt device " + device.getName());

				// This shouldn't be needed.
				gatt.disconnect();

				leSensorMap.remove(device.getName());
				if (leSensorMap.isEmpty()) {
					setSensorState(SensorState.DISCONNECTED);
				}
				break;

			case BluetoothProfile.STATE_CONNECTED:
				setSensorState(SensorState.CONNECTING);
				Log.i(TAG, "Discovering services on Gatt device " + device.getName());
				gatt.discoverServices();
				break;

			default:
				Log.i(TAG, "Ignoring new state: " + newState);
			}
		}

		/**
		 * Invoked when services are found on a GATT connection. Checks if we
		 * found a known characteristic, and if so, register to get notified on
		 * characteristic changed.
		 */
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			BluetoothDevice device = gatt.getDevice();

			if (device == null) {
				Log.e(TAG, "onServicesDiscovered: device was null");
				return;
			}

			Log.i(TAG, "Device = " + device.getName() + ", addr: " + device.getAddress());
			boolean foundSupportedCharacteristic = false;

			for (BluetoothGattService s : gatt.getServices()) {
				for (BluetoothGattCharacteristic ch : s.getCharacteristics()) {
					Log.i(TAG, "Services discovered for " + device.getName()
							+ ": found GATT characteristic with UUID: " + ch.getUuid());
					BluetoothLeSensor sensor = BTLE_CHARACTERISTIC_MAP.get(ch.getUuid());
					if (sensor != null) {
						foundSupportedCharacteristic = true;
						if (!sensor.subscribe(gatt, ch)) {
							Log.e(TAG, "Failed to subscribe to characteristic " + ch.getUuid());
						} else {
							Log.d(TAG, "Subscribed to characteristic " + ch.getUuid());
						}
					}
				}
			}

			if (foundSupportedCharacteristic) {
				if (getSensorState() != SensorState.SENDING) {
					setSensorState(SensorState.CONNECTED);
				}
				leSensorMap.put(device.getName(), gatt);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic ch) {
			BluetoothLeSensor sensor = BTLE_CHARACTERISTIC_MAP.get(ch.getUuid());
			if (sensor == null) {
				Log.e(TAG, "Could not locate decoder for characteristic " + ch.getUuid());
				return;
			}
			SensorDataSet ds = sensor.read(gatt, ch);
			if (ds == null) {
				return;
			}

			if (getSensorState() != SensorState.SENDING) {
				setSensorState(SensorState.SENDING);
			}

			synchronized (SYNC) {
				dataset = ds;
			}
		}
	}

	public void startConnect() {
		if (mAdapter == null) {
			return;
		}

		if (leSensorMap.size() > 0) {
			Log.i(TAG, "Already connected to a GATT device; skipping.");
			return;
		}
		String address = Constants.getString(mContext, R.string.bluetooth_sensor_key,
				Constants.BLUETOOTH_SENSOR_DEFAULT);
		if (address == null || address.trim().equals("")) {
			Log.e(TAG, "Default bluetooth sensor not set; can't connect");
			setSensorState(SensorState.NONE);
			return;
		}

		setSensorState(SensorState.CONNECTING);
		try {
			BluetoothDevice device = mAdapter.getRemoteDevice(address);
			device.connectGatt(mContext, true, leCallback);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Failed to open BT address: " + address);
			setSensorState(SensorState.NONE);
			return;
		}
	}

	public BluetoothLeManager(Context context) {
		final BluetoothManager bluetoothManager = (BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE);
		mAdapter = bluetoothManager != null ? bluetoothManager.getAdapter() : null;
		mContext = context;
	}

	@Override
	public boolean isEnabled() {
		return mAdapter != null && mAdapter.isEnabled();
	}

	@Override
	protected void setUpChannel() {
		if (mAdapter == null || !mAdapter.isEnabled()) {
			return;
		}
		mAdapter.cancelDiscovery();
		startConnect();
	}

	@Override
	protected void tearDownChannel() {
		for (Map.Entry<String, BluetoothGatt> g : leSensorMap.entrySet()) {
			Log.i(TAG, "Shutting down device " + g.getKey());
			g.getValue().close();
		}
		setSensorState(SensorState.DISCONNECTED);
	}

	@Override
	public SensorDataSet getSensorDataSet() {
		synchronized (SYNC) {
			return dataset;
		}
	}
}