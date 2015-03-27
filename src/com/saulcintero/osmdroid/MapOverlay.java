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

package com.saulcintero.osmdroid;

import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class MapOverlay extends org.osmdroid.views.overlay.Overlay {
	private Context mContext;

	public MapOverlay(Context ctx) {
		super(ctx);

		mContext = ctx;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		if (e.getAction() == MotionEvent.ACTION_MOVE)
			mContext.sendBroadcast(new Intent("android.intent.action.UNFOLLOW_LOCATION"));

		return false;
	}

	@Override
	protected void draw(Canvas c, MapView osmv, boolean shadow) {
	}
}