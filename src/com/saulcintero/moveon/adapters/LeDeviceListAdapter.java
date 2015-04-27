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

package com.saulcintero.moveon.adapters;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.saulcintero.moveon.R;

public class LeDeviceListAdapter extends BaseAdapter {
	private ArrayList<BluetoothDevice> mLeDevices;
	private LayoutInflater mInflator;

	public LeDeviceListAdapter(LayoutInflater mInflator) {
		super();
		mLeDevices = new ArrayList<BluetoothDevice>();
		this.mInflator = mInflator;
	}

	public void addDevice(BluetoothDevice device) {
		if (!mLeDevices.contains(device)) {
			mLeDevices.add(device);
		}
	}

	public BluetoothDevice getDevice(int position) {
		return mLeDevices.get(position);
	}

	public void clear() {
		mLeDevices.clear();
	}

	@Override
	public int getCount() {
		return mLeDevices.size();
	}

	@Override
	public Object getItem(int i) {
		return mLeDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		if (view == null) {
			view = mInflator.inflate(R.layout.btle_devices_list_row, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
			viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		BluetoothDevice device = mLeDevices.get(i);
		final String deviceName = device.getName();
		if (deviceName != null && deviceName.length() > 0)
			viewHolder.deviceName.setText(deviceName);
		else
			viewHolder.deviceName.setText(R.string.unknown_device);
		viewHolder.deviceAddress.setText(device.getAddress());

		return view;
	}

	private class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}
}