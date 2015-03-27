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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.saulcintero.moveon.adapters.PagerAdapter;
import com.saulcintero.moveon.adapters.ScrollingTabsAdapter;
import com.saulcintero.moveon.fragments.Summary1;
import com.saulcintero.moveon.fragments.Summary2;
import com.saulcintero.moveon.fragments.Summary2_hiit;
import com.saulcintero.moveon.fragments.Summary3;
import com.saulcintero.moveon.fragments.Summary4;
import com.saulcintero.moveon.ui.widgets.ScrollableTabView;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;

public class SummaryHolder extends FragmentActivity {
	private Context mContext;

	private SharedPreferences prefs;

	private boolean isMetric;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);
		setContentView(R.layout.holder);

		super.onCreate(savedInstanceState);

		initPager();
	}

	public void initPager() {
		PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

		mContext = getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

		String[] route = DataFunctionUtils.getRouteData(mContext, prefs.getInt("selected_practice", 0),
				isMetric);

		mPagerAdapter.addFragment(new Summary1());
		if (Integer.parseInt(route[22]) == 0)
			mPagerAdapter.addFragment(new Summary2());
		else
			mPagerAdapter.addFragment(new Summary2_hiit());
		mPagerAdapter.addFragment(new Summary3());
		mPagerAdapter.addFragment(new Summary4());

		ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setPageMargin(getResources().getInteger(R.integer.viewpager_margin_width));
		mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(0);

		initScrollableTabs(mViewPager);
	}

	public void initScrollableTabs(ViewPager mViewPager) {
		ScrollableTabView mScrollingTabs = (ScrollableTabView) findViewById(R.id.scrollingTabs);
		ScrollingTabsAdapter mScrollingTabsAdapter = new ScrollingTabsAdapter(this, 1);
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mViewPager);
	}
}