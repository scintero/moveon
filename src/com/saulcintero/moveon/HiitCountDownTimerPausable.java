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

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;

import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.TextToSpeechUtils;

public class HiitCountDownTimerPausable {
	private Context mContext;
	private SharedPreferences prefs;

	private Intent i = new Intent("android.intent.action.ACTION_PLAY_SOUND");

	private Vibrator vibrator;

	private final int MSG = 1;
	private int PREPARATION_TIME = 0, INTERVALS_TIME = 1;
	private int hiit_rounds, hiit_prepSeconds, hiit_coolDownSeconds, hiit_actionSeconds, hiit_passedRounds,
			hiit_actionSecondsByRound, hiit_lastRoundsSeconds;
	private long mMillisInFuture, mCountdownInterval, mStopTimeInFuture, mPauseTime;
	private boolean mCancelled = false, mPaused = false, isSaidCurrentInterval = false;
	private ArrayList<int[]> hiit_intervals;
	private String[] hiit_data;

	public HiitCountDownTimerPausable(Context mContext, SharedPreferences prefs, long countDownInterval) {
		this.mContext = mContext;
		this.prefs = prefs;
		vibrator = (Vibrator) this.mContext.getSystemService(Context.VIBRATOR_SERVICE);
		mCountdownInterval = countDownInterval;
		loadHiitPresset(prefs.getInt("selected_hiit", 1));
	}

	private void loadHiitPresset(int _id) {
		hiit_data = null;
		hiit_intervals = null;
		hiit_rounds = 0;
		hiit_prepSeconds = 0;
		hiit_coolDownSeconds = 0;
		hiit_actionSeconds = 0;

		hiit_data = DataFunctionUtils.getHiitData(mContext, _id);
		hiit_intervals = DataFunctionUtils.getHiitIntervalsData(mContext, _id);

		hiit_rounds = Integer.parseInt(hiit_data[1]);
		hiit_prepSeconds = Integer.parseInt(hiit_data[2]);
		hiit_coolDownSeconds = Integer.parseInt(hiit_data[3]);

		for (int i = 0; i <= (hiit_intervals.size() - 1); i++) {
			hiit_actionSeconds = hiit_actionSeconds + hiit_intervals.get(i)[1];
		}

		mMillisInFuture = Integer.parseInt(hiit_data[0]) * 1000;
	}

	public final void cancel() {
		mHandler.removeMessages(MSG);
		mCancelled = true;
	}

	public synchronized final HiitCountDownTimerPausable start() {
		if (mMillisInFuture <= 0) {
			onFinish();
			return this;
		}
		mStopTimeInFuture = SystemClock.elapsedRealtime() + mMillisInFuture;
		mHandler.sendMessage(mHandler.obtainMessage(MSG));
		mCancelled = false;
		mPaused = false;
		return this;
	}

	public long pause() {
		mPauseTime = mStopTimeInFuture - SystemClock.elapsedRealtime();
		mPaused = true;
		return mPauseTime;
	}

	public long resume() {
		mStopTimeInFuture = mPauseTime + SystemClock.elapsedRealtime();
		mPaused = false;
		mHandler.sendMessage(mHandler.obtainMessage(MSG));
		return mPauseTime;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			synchronized (HiitCountDownTimerPausable.this) {
				if (!mPaused) {
					final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

					if (millisLeft <= 0) {
						onFinish();
					} else if (millisLeft < mCountdownInterval) {
						sendMessageDelayed(obtainMessage(MSG), millisLeft);
					} else {
						long lastTickStart = SystemClock.elapsedRealtime();
						onTick(millisLeft);

						long delay = lastTickStart + mCountdownInterval - SystemClock.elapsedRealtime();

						while (delay < 0)
							delay += mCountdownInterval;

						if (!mCancelled) {
							sendMessageDelayed(obtainMessage(MSG), delay);
						}
					}
				}
			}
		}
	};

	public void onTick(long millisUntilFinished) {
		int interval_spent_time = (int) (((mMillisInFuture) - (millisUntilFinished)) / 1000);

		if (interval_spent_time < (mMillisInFuture / 1000)) {
			if (hiit_passedRounds == 1) {
				hiit_actionSecondsByRound = hiit_prepSeconds + hiit_actionSeconds;

				hiit_lastRoundsSeconds = hiit_prepSeconds;
			} else {
				hiit_actionSecondsByRound = hiit_prepSeconds + (hiit_actionSeconds * (hiit_passedRounds - 1))
						+ hiit_actionSeconds;

				hiit_lastRoundsSeconds = hiit_prepSeconds + (hiit_actionSeconds * (hiit_passedRounds - 1));
			}

			if ((interval_spent_time <= hiit_prepSeconds) && (hiit_prepSeconds > 0)) {
				int countDownTime = hiit_prepSeconds - interval_spent_time;

				countDownSoundManager(PREPARATION_TIME, countDownTime, hiit_intervals, hiit_intervals.size());
			} else if ((interval_spent_time <= hiit_actionSecondsByRound)
					&& (interval_spent_time < (mMillisInFuture / 1000 - hiit_coolDownSeconds))) {
				int action = hiit_intervals.size(), accum_intervals = 0;
				int[] accum_intervals_array = new int[hiit_intervals.size()];
				for (int m = 0; m <= (hiit_intervals.size() - 1); m++) {
					accum_intervals += hiit_intervals.get(m)[1];
					accum_intervals_array[m] = accum_intervals;

					if (interval_spent_time <= (hiit_lastRoundsSeconds + accum_intervals)) {
						action -= 1;
					}
				}

				accum_intervals = 0;
				for (int j = 0; j <= action; j++) {
					accum_intervals += hiit_intervals.get(j)[1];
				}

				int countDownTime;
				if (hiit_passedRounds == 1) {
					countDownTime = ((hiit_prepSeconds + accum_intervals) - hiit_lastRoundsSeconds)
							- (interval_spent_time - hiit_lastRoundsSeconds);
				} else {
					countDownTime = ((hiit_lastRoundsSeconds + accum_intervals) - interval_spent_time);
				}
				countDownSoundManager(INTERVALS_TIME, countDownTime, hiit_intervals, action);
			}

			if ((hiit_prepSeconds + (hiit_passedRounds * hiit_actionSeconds)) <= interval_spent_time)
				hiit_passedRounds += 1;
		}
	}

	public void onFinish() {
		mContext.sendBroadcast(new Intent("android.intent.action.STOP_AND_SAVE"));
	}

	private void countDownSoundManager(int typeOfInterval, int countDownTime,
			ArrayList<int[]> hiit_intervals, int action) {
		int countdown_value = Integer.parseInt(prefs.getString("hiit_countdown", "3"));

		boolean beep = false, vibrate = false, tts_countdown = false, current_interval = false, next_interval = false;

		if (prefs.getBoolean("hiit_sound", true))
			beep = true;

		if (prefs.getBoolean("hiit_vibrate", false))
			vibrate = true;

		if (prefs.getBoolean("hiit_countdown_tts", false))
			tts_countdown = true;

		if (prefs.getBoolean("hiit_current_interval", true))
			current_interval = true;

		if (prefs.getBoolean("hiit_next_interval", true))
			next_interval = true;

		// notification of actual interval
		if ((countDownTime >= (countdown_value + 12)) && current_interval && !isSaidCurrentInterval
				&& !TextToSpeechUtils.isSpeakingNow()) {
			String actual_interval_tts = mContext.getString(R.string.actual_interval_of) + " ";
			switch (typeOfInterval) {
			case 0:
				actual_interval_tts += mContext.getString(R.string.warm_up) + " ";
				break;
			case 1:
				actual_interval_tts += getIntervalDetail(hiit_intervals.get(action)[0]);
				break;
			case 2:
				actual_interval_tts += mContext.getString(R.string.cool_down) + " ";
				break;
			}
			actual_interval_tts += mContext.getString(R.string.until) + " "
					+ FunctionUtils.longFormatTTSTime(mContext, countDownTime);
			TextToSpeechUtils.say(mContext, actual_interval_tts);

			isSaidCurrentInterval = true;
		}

		// notification of next interval
		if ((countDownTime == (countdown_value + 6)) && next_interval && !TextToSpeechUtils.isSpeakingNow()) {
			int next_action = 0;
			if ((action + 1) < hiit_intervals.size())
				next_action = action + 1;

			if ((hiit_passedRounds == hiit_rounds) && ((action + 1) == hiit_intervals.size())) {
				if (hiit_coolDownSeconds > 0)
					TextToSpeechUtils.say(mContext, mContext.getString(R.string.cooldown_interval) + " "
							+ FunctionUtils.longFormatTTSTime(mContext, (long) hiit_coolDownSeconds));
			} else {
				TextToSpeechUtils.say(
						mContext,
						mContext.getString(R.string.next_interval_of)
								+ " "
								+ getIntervalDetail(hiit_intervals.get(next_action)[0])
								+ mContext.getString(R.string.until)
								+ " "
								+ FunctionUtils.longFormatTTSTime(mContext,
										hiit_intervals.get(next_action)[1]));
			}
		}

		// notification time
		if ((countDownTime <= countdown_value) && (countDownTime > 0)) {
			if (tts_countdown)
				TextToSpeechUtils.say(mContext, String.valueOf(countDownTime));

			if (beep) {
				i.putExtra("sound", "1");
				mContext.sendBroadcast(i);
			}

			long[] pattern = { 0, 500 };
			checkVibration(vibrate, pattern);
		} else if (countDownTime == 0) {
			if (tts_countdown)
				TextToSpeechUtils.say(mContext, mContext.getString(R.string.end_of_interval));

			if (beep) {
				i.putExtra("sound", "2");
				mContext.sendBroadcast(i);
			}

			long[] pattern = { 0, 200, 100, 200 };
			checkVibration(vibrate, pattern);

			isSaidCurrentInterval = false;
		}
	}

	private void checkVibration(boolean vibrate, long[] pattern) {
		if (vibrate) {
			vibrator.vibrate(pattern, -1);
		}
	}

	private String getIntervalDetail(int interval) {
		String interval_tts = "";
		switch (interval) {
		case 1:
			interval_tts = mContext.getString(R.string.hiit_type_description1).toLowerCase(
					Locale.getDefault())
					+ " ";
			break;
		case 2:
			interval_tts = mContext.getString(R.string.hiit_type_description2).toLowerCase(
					Locale.getDefault())
					+ " ";
			break;
		case 3:
			interval_tts = mContext.getString(R.string.hiit_type_description3).toLowerCase(
					Locale.getDefault())
					+ " ";
			break;
		case 4:
			interval_tts = mContext.getString(R.string.hiit_type_description4).toLowerCase(
					Locale.getDefault())
					+ " ";
			break;
		}
		return interval_tts;
	}
}