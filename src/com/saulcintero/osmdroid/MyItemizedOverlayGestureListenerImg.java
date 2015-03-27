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

import java.util.List;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.Intent;

/**
 * Simple implementation of an ItemizedOverlay handling tap events with
 * osmdroid.
 */
public class MyItemizedOverlayGestureListenerImg extends ItemizedIconOverlay<OverlayItem> {
	protected Context mContext;

	public MyItemizedOverlayGestureListenerImg(final Context context, final List<OverlayItem> mList) {
		super(context, mList, new OnItemGestureListener<OverlayItem>() {
			@Override
			public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
				return false;
			}

			@Override
			public boolean onItemLongPress(final int index, final OverlayItem item) {
				return false;
			}
		});
		mContext = context;
	}

	@Override
	protected boolean onSingleTapUpHelper(final int index, final OverlayItem item, final MapView mapView) {
		Intent i = new Intent("android.intent.action.SUMMARY_GALLERY_SELECTED_IMAGE");
		i.putExtra("tappedPicture", String.valueOf(item.getTitle()));
		i.putExtra("item", String.valueOf(item));
		mContext.sendBroadcast(i);

		return true;
	}
}