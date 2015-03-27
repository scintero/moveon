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

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.utils.FunctionUtils;

public class StableArrayAdapter extends ArrayAdapter<String> {
	Activity activity;
	private Resources res;

	final int INVALID_ID = -1;

	List<String> content;

	int resource;

	HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	public StableArrayAdapter(Activity act, int ViewResourceId, List<String> objects) {
		super(act, ViewResourceId, objects);

		activity = act;
		res = activity.getResources();
		resource = ViewResourceId;
		content = objects;

		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
	}

	@Override
	public long getItemId(int position) {
		if (position < 0 || position >= mIdMap.size()) {
			return INVALID_ID;
		}
		String item = getItem(position);
		return mIdMap.get(item);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		ViewHolder viewHolder;
		View rowView = convertView;

		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(resource, parentView, false);

			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.TextView01);
			viewHolder.details = (TextView) rowView.findViewById(R.id.TextView02);
			viewHolder.color = (TextView) rowView.findViewById(R.id.TextView03);
			rowView.setTag(viewHolder);
		} else
			viewHolder = (ViewHolder) rowView.getTag();

		String[] mContent = content.get(position).split(",");

		viewHolder.title.setText(mContent[1]);
		viewHolder.title.setTextColor(res.getColor(R.color.gray));

		viewHolder.details.setText(FunctionUtils.shortFormatTime(Long.parseLong(mContent[2])));
		viewHolder.details.setTextColor(res.getColor(R.color.gray));

		switch (Integer.parseInt(mContent[3])) {
		case 1:
			viewHolder.color.setBackgroundColor(res.getColor(R.color.green));
			break;
		case 2:
			viewHolder.color.setBackgroundColor(res.getColor(R.color.yellow));
			break;
		case 3:
			viewHolder.color.setBackgroundColor(res.getColor(R.color.orange));
			break;
		case 4:
			viewHolder.color.setBackgroundColor(res.getColor(R.color.red));
			break;
		}

		return rowView;
	}

	private class ViewHolder {
		TextView title;
		TextView details;
		TextView color;
	}
}