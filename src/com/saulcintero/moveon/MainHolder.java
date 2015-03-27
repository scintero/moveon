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

import java.io.File;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.saulcintero.moveon.adapters.PagerAdapter;
import com.saulcintero.moveon.adapters.ScrollingTabsAdapter;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.fragments.History;
import com.saulcintero.moveon.fragments.Main;
import com.saulcintero.moveon.fragments.Statistics;
import com.saulcintero.moveon.services.MoveOnService;
import com.saulcintero.moveon.ui.widgets.ScrollableTabView;
import com.saulcintero.moveon.utils.TextToSpeechUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class MainHolder extends FragmentActivity implements ServiceConnection {
	private DataManager DBManager = null;
	private MoveOnService mService;

	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	private AlertDialog.Builder alert;
	private AlertDialog dialog;

	private boolean doubleBackToExitPressedOnce;

	private boolean isBound = false;

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = ((MoveOnService.LocalBinder) service).getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (doubleBackToExitPressedOnce) {
				if (MoveOnService.getIsPracticeRunning()) {
					alert = new AlertDialog.Builder(this);
					alert.setIcon(android.R.drawable.ic_dialog_alert);
					alert.setTitle(getString(R.string.confirm_action));
					alert.setMessage(getString(R.string.discard_and_exit));
					alert.setCancelable(true);

					alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							moveon_exit();
						}
					});
					alert.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
								}
							});

					dialog = alert.create();
					dialog.show();
				} else {
					moveon_exit();
				}
			} else {
				UIFunctionUtils.showMessage(this, true, getString(R.string.exit));
			}

			this.doubleBackToExitPressedOnce = true;

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					doubleBackToExitPressedOnce = false;
				}
			}, 2000);
		}
		return super.onKeyDown(keyCode, event);
	}

	private void moveon_exit() {
		finish();
	}

	@Override
	public void onDestroy() {
		if (isFinishing()) {
			stopService();

			DBManager = null;
		}

		super.onDestroy();
	}

	@Override
	public void onPause() {
		if (mService != null)
			unbindService();

		super.onPause();
	}

	@Override
	public void onResume() {
		if (mService == null)
			bindService();

		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.holder);

		// FunctionUtils.getAppKeyHash(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = prefs.edit();

		initPager();

		if (!isFinishing())
			startService();

		initDefaultValues();
	}

	public void initPager() {
		PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

		mPagerAdapter.addFragment(new History());
		mPagerAdapter.addFragment(new Main());
		mPagerAdapter.addFragment(new Statistics());

		ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager.setPageMargin(getResources().getInteger(R.integer.viewpager_margin_width));
		mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
		mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(1);

		initScrollableTabs(mViewPager);
	}

	public void initScrollableTabs(ViewPager mViewPager) {
		ScrollableTabView mScrollingTabs = (ScrollableTabView) findViewById(R.id.scrollingTabs);
		ScrollingTabsAdapter mScrollingTabsAdapter = new ScrollingTabsAdapter(this, 0);
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mViewPager);
	}

	private void startService() {
		Intent service = new Intent(this, MoveOnService.class);
		getBaseContext().startService(service);
	}

	private void stopService() {
		Intent service = new Intent(this, MoveOnService.class);
		getBaseContext().stopService(service);
	}

	private void bindService() {
		if (!isBound) {
			isBound = bindService(new Intent(this, MoveOnService.class), this, Context.BIND_AUTO_CREATE);
		}
	}

	private void unbindService() {
		if (isBound) {
			unbindService(this);
			isBound = false;
		}
	}

	private void initDefaultValues() {
		if (prefs.getBoolean("first_start", true)) {
			initDB();

			editor.putBoolean("speak", true);
			editor.commit();

			String externalStorage = Environment.getExternalStorageDirectory().toString();

			File directory1 = new File(externalStorage + "/moveon/database");
			File directory2 = new File(externalStorage + "/moveon/images");
			File directory3 = new File(externalStorage + "/moveon/gpx");
			File directory4 = new File(externalStorage + "/moveon/kml");
			File directory5 = new File(externalStorage + "/moveon/kmz");
			if (!directory1.exists())
				directory1.mkdirs();
			if (!directory2.exists())
				directory2.mkdirs();
			if (!directory3.exists())
				directory3.mkdirs();
			if (!directory4.exists())
				directory4.mkdirs();
			if (!directory5.exists())
				directory5.mkdirs();

			File file = new File(externalStorage + "/moveon/moveon_backup.zip");
			if (file.exists()) {
				UIFunctionUtils.createAlertDialog(this, 4);
			}
		}
	}

	private void initDB() {
		DBManager = new DataManager(this);
		DBManager.Open();
		DBManager.Close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		return true;

	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.coach:
			Intent i = new Intent("android.intent.action.PRACTICE_BUTTONS_STATUS");
			if (!prefs.getBoolean("blocked", false)) {
				boolean speak = prefs.getBoolean("speak", false);
				if (speak) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("speak", false);
					editor.commit();

					TextToSpeechUtils.setSpeak(false);
				} else {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("speak", true);
					editor.commit();

					TextToSpeechUtils.setSpeak(true);
				}
				i.putExtra("practiceButtonsStatus", "1");
				sendBroadcast(i);
			}
			break;
		case R.id.camera:
			Intent i2 = new Intent("android.intent.action.TAKE_PICTURE");
			if (!prefs.getBoolean("blocked", false) && MoveOnService.getIsPracticeRunning()) {
				sendBroadcast(i2);
			}
			break;
		case R.id.padlock:
			changeLockedStatus();

			Intent i3 = new Intent("android.intent.action.PRACTICE_BUTTONS_STATUS");
			i3.putExtra("practiceButtonsStatus", "2");
			sendBroadcast(i3);
			break;
		case R.id.music:
			if (!prefs.getBoolean("blocked", false)) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					startActivity(new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER));
				} else {
					Intent intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN,
							Intent.CATEGORY_APP_MUSIC);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
			break;
		case R.id.settings:
			if (!prefs.getBoolean("blocked", false))
				startActivity(new Intent(this, MoveOnPreferences.class));
			break;
		}
		return true;
	}

	private void changeLockedStatus() {
		SharedPreferences.Editor editor = prefs.edit();

		if (prefs.getBoolean("blocked", false)) {
			editor.putBoolean("blocked", false);
		} else {
			editor.putBoolean("blocked", true);
		}
		editor.commit();
	}
}