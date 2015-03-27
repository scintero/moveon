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
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.saulcintero.moveon.R;

public class ScrollingTabsAdapter implements TabAdapter {
	private final FragmentActivity activity;
	private Resources res;
	private int titles;

	public ScrollingTabsAdapter(FragmentActivity act, int titles) {
		activity = act;
		res = activity.getResources();

		this.titles = titles;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position) {
		LayoutInflater inflater = activity.getLayoutInflater();
		final Button tab = (Button) inflater.inflate(R.layout.tabs, null);
		final String[] mTitles;
		switch (titles) {
		case 0:
			mTitles = res.getStringArray(R.array.tab_titles1);
			break;
		case 1:
			mTitles = res.getStringArray(R.array.tab_titles2);
			break;
		default:
			mTitles = res.getStringArray(R.array.tab_titles1);
			break;
		}

		if (position < mTitles.length)
			tab.setText(mTitles[position]);

		return tab;
	}
}