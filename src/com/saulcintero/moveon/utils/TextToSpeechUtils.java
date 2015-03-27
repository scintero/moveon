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

package com.saulcintero.moveon.utils;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.saulcintero.moveon.Constants;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.enums.TtsSupportedLanguages;

@SuppressWarnings("deprecation")
public class TextToSpeechUtils implements TextToSpeech.OnInitListener {
	private static final String TAG = TextToSpeechUtils.class.getSimpleName().toString();

	private static TextToSpeechUtils instance = null;
	private static AudioManager am;
	private Context mContext;

	private static TextToSpeech mTts;
	private static boolean mSpeak = false, hasErrors = false;
	private static boolean mSpeakingEngineAvailable = false;

	public static boolean isEngineAvailable() {
		return mSpeakingEngineAvailable;
	}

	public static boolean isSpeakingNow() {
		return mTts.isSpeaking();
	}

	public static boolean getIfHasErrors() {
		return hasErrors;
	}

	public static void setSpeak(boolean speak) {
		mSpeak = speak;
	}

	public static TextToSpeechUtils getInstance() {
		if (instance == null)
			instance = new TextToSpeechUtils();

		return instance;
	}

	public void initTTS(Context context) {
		mContext = context;

		Log.i(TAG, mContext.getString(R.string.tts_starting));

		mTts = new TextToSpeech(mContext, this);
	}

	public void shutdownTTS() {
		Log.i(TAG, mContext.getString(R.string.tts_stopping));

		mSpeakingEngineAvailable = false;
		mTts.stop();
		mTts.shutdown();
		Log.i(TAG, mContext.getString(R.string.tts_stopped));
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = mTts.setLanguage(Locale.getDefault());

			if (Build.VERSION.SDK_INT >= 15) {
				mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
					@Override
					public void onDone(String utteranceId) {
						am.abandonAudioFocus(null);
					}

					@Override
					public void onError(String utteranceId) {
					}

					@Override
					public void onStart(String utteranceId) {
					}

				});
			} else {
				mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
					@Override
					public void onUtteranceCompleted(String utteranceId) {
						am.abandonAudioFocus(null);
					}
				});
			}
			
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED
					|| !getSupportedLanguagesForTTS(Locale.getDefault().toString())) {
				disableTtsEngine();
			} else {
				mSpeakingEngineAvailable = true;
				hasErrors = false;
				Log.i(TAG, mContext.getString(R.string.tts_started));
			}
		} else {
			disableTtsEngine();
		}
	}

	private boolean getSupportedLanguagesForTTS(String locale) {
		for (int i = 0; i < TtsSupportedLanguages.values().length; i++) {
			if (TtsSupportedLanguages.values()[i].toString().equals(locale)) {
				return true;
			}
		}
		return false;
	}

	private void disableTtsEngine() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("speak", false);
		editor.commit();

		mSpeakingEngineAvailable = false;
		mSpeak = false;
		hasErrors = true;

		UIFunctionUtils.showMessage(mContext, false,
				mContext.getString(R.string.tts_language_is_not_available));

		Log.e(TAG, mContext.getString(R.string.tts_language_is_not_available));
		Log.e(TAG, mContext.getString(R.string.tts_error));

		Intent intentMainButtonsStatus = new Intent("android.intent.action.PRACTICE_BUTTONS_STATUS");
		intentMainButtonsStatus.putExtra("practiceButtonsStatus",
				String.valueOf(Constants.VOICE_COACH_STATUS));
		mContext.sendBroadcast(intentMainButtonsStatus);
	}

	public static void say(Context mContext, String text) {
		System.out.println(mContext.getString(R.string.tts_engine_says) + " " + text);

		if (mSpeak && mSpeakingEngineAvailable) {
			am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
			am.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

			HashMap<String, String> ttsParams = new HashMap<String, String>();
			ttsParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
			ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MOVEON");

			mTts.speak(text, TextToSpeech.QUEUE_ADD, ttsParams);
		}
	}
}