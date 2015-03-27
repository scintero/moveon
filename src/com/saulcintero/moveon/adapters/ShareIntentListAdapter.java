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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.saulcintero.moveon.R;

@SuppressWarnings("rawtypes")
public class ShareIntentListAdapter extends ArrayAdapter {
	Activity act;
	Object[] items;
	boolean[] arrows;
	int layoutId;

	@SuppressWarnings("unchecked")
	public ShareIntentListAdapter(Activity act, int layoutId, Object[] items) {
		super(act, layoutId, items);

		this.act = act;
		this.items = items;
		this.layoutId = layoutId;
	}

	@SuppressLint("ViewHolder")
	public View getView(int pos, View convertView, ViewGroup parent) {
		LayoutInflater inflater = act.getLayoutInflater();
		View row = inflater.inflate(layoutId, null);
		TextView label = (TextView) row.findViewById(R.id.shareName);
		label.setText(((ResolveInfo) items[pos]).activityInfo.applicationInfo.loadLabel(
				act.getPackageManager()).toString());
		ImageView image = (ImageView) row.findViewById(R.id.shareImage);
		image.setImageDrawable(((ResolveInfo) items[pos]).activityInfo.applicationInfo.loadIcon(act
				.getPackageManager()));

		return (row);
	}
}