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

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.saulcintero.moveon.adapters.TabAdapter;

public class ScrollableTabView extends HorizontalScrollView implements ViewPager.OnPageChangeListener {
	private final Context mContext;

	private Resources res;
	private Drawable mDividerDrawable;

	private ViewPager mPager;
	private TabAdapter mAdapter;

	private final LinearLayout mContainer;

	private final ArrayList<View> mTabs = new ArrayList<View>();

	private final int mDividerColor = 0xFFe3e3e3;
	private int mDividerMarginTop = 12, mDividerMarginBottom = 12, mDividerWidth = 1;

	public ScrollableTabView(Context context) {
		this(context, null);
	}

	public ScrollableTabView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScrollableTabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

		mContext = context;
		res = getResources();

		mDividerMarginTop = (int) (res.getDisplayMetrics().density * mDividerMarginTop);
		mDividerMarginBottom = (int) (res.getDisplayMetrics().density * mDividerMarginBottom);
		mDividerWidth = (int) (res.getDisplayMetrics().density * mDividerWidth);

		this.setHorizontalScrollBarEnabled(false);
		this.setHorizontalFadingEdgeEnabled(false);

		mContainer = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		mContainer.setLayoutParams(params);
		mContainer.setOrientation(LinearLayout.HORIZONTAL);

		this.addView(mContainer);

	}

	public void setAdapter(TabAdapter adapter) {
		this.mAdapter = adapter;

		if ((mPager != null) && (mAdapter != null))
			initTabs();
	}

	public void setViewPager(ViewPager pager) {
		this.mPager = pager;
		mPager.setOnPageChangeListener(this);

		if ((mPager != null) && (mAdapter != null))
			initTabs();
	}

	private void initTabs() {
		mContainer.removeAllViews();
		mTabs.clear();

		if (mAdapter == null)
			return;

		for (int i = 0; i < mPager.getAdapter().getCount(); i++) {
			final int index = i;

			View tab = mAdapter.getView(i);
			mContainer.addView(tab);

			tab.setFocusable(true);

			mTabs.add(tab);

			if (i != mPager.getAdapter().getCount() - 1)
				mContainer.addView(getSeparator());

			tab.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPager.getCurrentItem() == index)
						selectTab(index);
					else
						mPager.setCurrentItem(index, true);
				}
			});
		}

		selectTab(mPager.getCurrentItem());
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		selectTab(position);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (changed)
			selectTab(mPager.getCurrentItem());
	}

	private View getSeparator() {
		View v = new View(mContext);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mDividerWidth,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		params.setMargins(0, mDividerMarginTop, 0, mDividerMarginBottom);
		v.setLayoutParams(params);

		if (mDividerDrawable != null)
			v.setBackground(mDividerDrawable);
		else
			v.setBackgroundColor(mDividerColor);

		return v;
	}

	private void selectTab(int position) {
		for (int i = 0, pos = 0; i < mContainer.getChildCount(); i += 2, pos++) {
			View tab = mContainer.getChildAt(i);
			tab.setSelected(pos == position);
		}

		View selectedTab = mContainer.getChildAt(position * 2);

		final int w = selectedTab.getMeasuredWidth();
		final int l = selectedTab.getLeft();
		final int x = l - this.getWidth() / 2 + w / 2;

		smoothScrollTo(x, this.getScrollY());
	}
}