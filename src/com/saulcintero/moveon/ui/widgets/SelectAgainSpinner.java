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

package com.saulcintero.moveon.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class SelectAgainSpinner extends Spinner {
	private int lastSelected = 1;

	public SelectAgainSpinner(Context context) {
		super(context);
	}

	public SelectAgainSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SelectAgainSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (this.lastSelected == this.getSelectedItemPosition() && getOnItemSelectedListener() != null)
			getOnItemSelectedListener().onItemSelected(this, getSelectedView(),
					this.getSelectedItemPosition(), getSelectedItemId());
		if (!changed)
			lastSelected = this.getSelectedItemPosition();

		super.onLayout(changed, l, t, r, b);
	}
}