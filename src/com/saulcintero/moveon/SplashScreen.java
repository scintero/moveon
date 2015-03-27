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

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.saulcintero.moveon.utils.UIFunctionUtils;

public class SplashScreen extends Activity {
	private static final long SPLASH_SCREEN_DELAY = 1000;

	private boolean mainClassIsLaunched = false;

	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			UIFunctionUtils.showMessage(this, true, getString(R.string.wait_until_app_begins));

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("mainClassIsLaunched", mainClassIsLaunched);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash_screen);

		if (savedInstanceState != null) {
			mainClassIsLaunched = savedInstanceState.getBoolean("mainClassIsLaunched");
		}

		if (!mainClassIsLaunched) {
			startAnimations();
			LaunchMainActivityInBackground();
		}
	}

	private void LaunchMainActivityInBackground() {
		Thread splashThread = new Thread() {
			@Override
			public void run() {
				mainClassIsLaunched = true;

				try {
					int waited = 0;
					while (waited < SPLASH_SCREEN_DELAY) {
						sleep(100);
						waited += 100;
					}
				} catch (InterruptedException e) {
				} finally {
					Intent i = new Intent();
					i.setClassName("com.saulcintero.moveon", "com.saulcintero.moveon.MainHolder");
					startActivity(i);

					finish();
				}
			}
		};
		splashThread.start();
	}

	private void startAnimations() {
		Animation alpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
		alpha.reset();
		RelativeLayout r = (RelativeLayout) findViewById(R.id.splash_layout);
		r.clearAnimation();
		r.startAnimation(alpha);
	}
}