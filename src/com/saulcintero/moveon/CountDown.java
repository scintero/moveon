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
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.saulcintero.moveon.fragments.Main;
import com.saulcintero.moveon.utils.TextToSpeechUtils;

public class CountDown extends Activity implements OnClickListener {
	private Context mContext;

	private CountDownTimer countDownTimer;
	private SharedPreferences prefs;

	private Button startB;
	public TextView text;

	private boolean timerHasStarted = false;

	private long startTime;
	private long interval;

	private long millisUntilFinished;

	@Override
	public void onDestroy() {
		countDownTimer.cancel();

		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLong("startTime", startTime);
		savedInstanceState.putLong("millisUntilFinished", millisUntilFinished);
		savedInstanceState.putBoolean("timerHasStarted", timerHasStarted);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.countdown);

		mContext = getApplicationContext();

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		startTime = Long.parseLong((prefs.getString("countdown", "0"))) * 1000;

		if (savedInstanceState != null) {
			millisUntilFinished = savedInstanceState.getLong("millisUntilFinished");
			timerHasStarted = savedInstanceState.getBoolean("timerHasStarted");
		} else {
			timerHasStarted = false;
		}

		interval = 1 * 1000;

		startB = (Button) this.findViewById(R.id.button);
		text = (TextView) this.findViewById(R.id.timer);

		if (timerHasStarted) {
			countDownTimer = new MyCountDownTimer(millisUntilFinished, interval);
			text.setText(text.getText() + String.valueOf(millisUntilFinished / 1000));
		} else {
			countDownTimer = new MyCountDownTimer(startTime, interval);
			text.setText(text.getText() + String.valueOf(startTime / 1000));
		}

		startB.setTypeface(null, Typeface.BOLD);
		startB.setTextColor(Color.parseColor("white"));
		startB.setTextSize(16);

		startB.setOnClickListener(this);

		countDownTimer.start();

		startB.setText(getString(R.string.interrupt));
	}

	public class MyCountDownTimer extends CountDownTimer {
		public MyCountDownTimer(long startTime, long interval) {
			super(startTime, interval);

			timerHasStarted = true;
		}

		@Override
		public void onFinish() {
			Main.launchPractice = true;

			finish();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			if (millisUntilFinished / 1000 <= 5)
				TextToSpeechUtils.say(mContext, "" + millisUntilFinished / 1000);

			CountDown.this.millisUntilFinished = millisUntilFinished;

			text.setText("" + millisUntilFinished / 1000);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button:
			if (!timerHasStarted) {
				countDownTimer.start();
				timerHasStarted = true;
				startB.setText(getString(R.string.interrupt));
			} else {
				countDownTimer.cancel();
				timerHasStarted = false;
				startB.setText(getString(R.string.restart));
			}

			break;
		}
	}
}