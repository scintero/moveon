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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.saulcintero.moveon.adapters.LeDeviceListAdapter;

public class BtleDevices extends Activity {
	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;

	private Handler mHandler;

	private Button scan;
	private TextView scanBtnText;
	private ListView list;
	private ProgressBar progressBar1;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000;

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);
		setContentView(R.layout.btle_devices);

		super.onCreate(savedInstanceState);

		scan = (Button) findViewById(R.id.scan);
		scanBtnText = (TextView) findViewById(R.id.scan_btn_text);
		list = (ListView) findViewById(R.id.list);
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);

		progressBar1.setVisibility(View.VISIBLE);
		scanBtnText.setText(R.string.stop);

		mHandler = new Handler();

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		scan.setOnClickListener(new View.OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				if (mScanning) {
					mScanning = false;
					progressBar1.setVisibility(View.INVISIBLE);
					scanBtnText.setText(R.string.scan);
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				} else {
					mScanning = true;
					progressBar1.setVisibility(View.VISIBLE);
					scanBtnText.setText(R.string.stop);
					mBluetoothAdapter.startLeScan(mLeScanCallback);
				}
			}
		});

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView textViewItem = ((TextView) view.findViewById(R.id.device_address));
				String address = textViewItem.getText().toString();
				if (address.length() > 0)
					Constants
							.setString(getApplicationContext(), R.string.bluetooth_smart_sensor_key, address);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		mLeDeviceListAdapter = new LeDeviceListAdapter(BtleDevices.this.getLayoutInflater());
		list.setAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	@SuppressWarnings("deprecation")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					progressBar1.setVisibility(View.INVISIBLE);
					scanBtnText.setText(R.string.scan);
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);

			mScanning = true;
			progressBar1.setVisibility(View.VISIBLE);
			scanBtnText.setText(R.string.stop);
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			progressBar1.setVisibility(View.INVISIBLE);
			scanBtnText.setText(R.string.scan);
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLeDeviceListAdapter.addDevice(device);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};
}