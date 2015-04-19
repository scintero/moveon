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

package com.saulcintero.moveon.services;

import static com.saulcintero.moveon.Constants.GPS_ITEM_OFF;
import static com.saulcintero.moveon.Constants.GPS_ITEM_ON;
import static com.saulcintero.moveon.Constants.GPS_ITEM_SEARCHING;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.saulcintero.moveon.Constants;
import com.saulcintero.moveon.DistanceManager;
import com.saulcintero.moveon.HiitCountDownTimerPausable;
import com.saulcintero.moveon.R;
import com.saulcintero.moveon.SplashScreen;
import com.saulcintero.moveon.StepCounter;
import com.saulcintero.moveon.TimeManager;
import com.saulcintero.moveon.entities.EntityCadence;
import com.saulcintero.moveon.entities.EntityHeartRate;
import com.saulcintero.moveon.entities.EntityLocations;
import com.saulcintero.moveon.entities.EntityRoutes;
import com.saulcintero.moveon.enums.ActivityTypes;
import com.saulcintero.moveon.enums.DisplayTypes;
import com.saulcintero.moveon.enums.GpsReception;
import com.saulcintero.moveon.enums.NotificationTypes;
import com.saulcintero.moveon.enums.TypesOfPractices;
import com.saulcintero.moveon.sensors.SensorManagerFactory;
import com.saulcintero.moveon.sensors.StepDetector;
import com.saulcintero.moveon.utils.BluetoothDeviceUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.TextToSpeechUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class MoveOnService extends Service {
	private static final String TAG = MoveOnService.class.getSimpleName();

	private static final String MEDIA_BUTTON_RECEIVER = "media_button_receiver";

	private static final int MAX_METERS_EXCELLENT = 20, MAX_METERS_GOOD = 40, MAX_METERS_WEAK = 60,
			MAX_METERS_BAD = 100;

	private static Context mContext;
	private static SharedPreferences prefs;

	private static Intent buttonsStatusIntent = new Intent("android.intent.action.PRACTICE_BUTTONS_STATUS");
	private static Intent practiceStatusIntent = new Intent("android.intent.action.PRACTICE_STATUS");

	private TextToSpeechUtils tts;

	private Sensor mSensor;
	private SensorManager mSensorManager;
	private StepDetector mStepDetector;
	private StepCounter mStepCounter;

	private TimeManager mTimeManager;
	private DistanceManager mDistanceManager;

	private static ArrayList<EntityRoutes> routesList = new ArrayList<EntityRoutes>();
	private static ArrayList<EntityLocations> locationsList = new ArrayList<EntityLocations>();
	private static ArrayList<EntityHeartRate> hrList = new ArrayList<EntityHeartRate>();
	private static ArrayList<EntityCadence> cadenceList = new ArrayList<EntityCadence>();
	private static EntityRoutes route;
	private EntityLocations location;

	private EntityHeartRate hr;
	private EntityCadence cadence;

	private LocationManager locMgr;
	private static mLocationListener locListener;
	private mGpsListener listener;

	private AudioManager mAudioManager;
	private static MediaPlayer mMediaPlayer;
	private SettingsObserver mSettingsObserver;
	private ContentResolver mContentResolver;
	private ComponentName mComponentName;

	private Location actualLocation = null;
	private Location lastLocation = null;

	private BluetoothAdapter bluetoothAdapter = null;

	private static long elapsedRealtime, accumulatedTime, lengthChrono;
	private static boolean pause = true, isPracticeRunning = false, resumelocations = false,
			isPausedForSavePractice = false, autoPause = false;

	// millis
	private long GPS_UPDATE_TIME_INTERVAL = 2000;
	private int GPS_UPDATE_EXCEEDED_TIME = 11000;

	// meters
	private float GPS_UPDATE_DISTANCE_INTERVAL = 0;

	private long mLastLocationMillis;
	private long time = 0, mTime = 0;

	private boolean isGPSFix, isMetric;

	private PowerManager.WakeLock wakeLock;

	private com.saulcintero.moveon.sensors.SensorManager sensorManager;

	private NotificationManager mNotificationManager;
	private int NOTIFICATION_ID = 10002; // Any unique number for this
											// notification

	private static Handler mHandler;

	private static HiitCountDownTimerPausable timerPausable = null;

	private static int maxHR, maxCadence, gpsStatus;

	private static List<Integer> heartRateValuesList = new ArrayList<Integer>();
	private static List<Integer> cadenceValuesList = new ArrayList<Integer>();

	private static List<Integer> heartRateList = new ArrayList<Integer>();

	private static List<Float> speedList = new ArrayList<Float>();

	private String mSteps, mHeartRate, mCadence;

	private static int mCalories;

	public float sumOfKcal, howHasPassedAway, lastTime, lastDistance;
	public int howLongHasItBeen;

	private static HiitCountDownTimerPausable pausableCountDownTimer;

	private String[] ttsParams;

	private BroadcastReceiver mReceiverScreenOff = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				MoveOnService.this.unregisterDetector();
				MoveOnService.this.registerDetector();
				if (prefs.getString("operation_level", "run_in_background").equals("wake_up")) {
					wakeLock.release();
					acquireWakeLock();
				}
			}
		}
	};

	private BroadcastReceiver mReceiverRefreshScreenPreferences = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			wakeLock.release();
			acquireWakeLock();
		}
	};

	private BroadcastReceiver mReceiverSayInformation = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isPracticeRunning) {
				if (prefs.getBoolean("speak", false) == true)
					sayFinalInformation(Integer.parseInt(intent.getStringExtra("type")));
			}
		}
	};

	private void sayFinalInformation(int type) {
		String sentence = "";
		String[] distance_unit = new String[4];
		ttsParams = locListener.checkParametersToSay(true);

		if (type == NotificationTypes.METRIC_DISTANCE.getTypes()
				|| type == NotificationTypes.IMPERIAL_DISTANCE.getTypes()) {
			distance_unit[0] = (isMetric ? getString(R.string.long_unit1_detail_5)
					: getString(R.string.long_unit2_detail_5));
			distance_unit[1] = (isMetric ? getString(R.string.long_unit1_detail_6)
					: getString(R.string.long_unit2_detail_6));
			distance_unit[2] = (isMetric ? getString(R.string.long_unit1_detail_7)
					: getString(R.string.long_unit2_detail_7));
			distance_unit[3] = (isMetric ? getString(R.string.long_unit1_detail_8)
					: getString(R.string.long_unit2_detail_8));
			boolean roundDistance = true;

			if (Float.parseFloat(ttsParams[1]) < (float) 1) {
				distance_unit[0] = (isMetric ? getString(R.string.long_unit1_detail_9)
						: getString(R.string.long_unit2_detail_9));
				distance_unit[1] = (isMetric ? getString(R.string.long_unit1_detail_10)
						: getString(R.string.long_unit2_detail_10));
				roundDistance = false;
			}

			if (Float.parseFloat(ttsParams[1]) >= (float) 2) {
				distance_unit[0] = (isMetric ? getString(R.string.long_unit1_detail_9)
						: getString(R.string.long_unit2_detail_9));
				distance_unit[1] = (isMetric ? getString(R.string.long_unit1_detail_10)
						: getString(R.string.long_unit2_detail_10));
			}

			sentence = FunctionUtils.generateStringToSay(mContext, ttsParams, distance_unit, roundDistance,
					false);
		} else if (type == NotificationTypes.TIME.getTypes()) {
			distance_unit[0] = "";
			distance_unit[1] = "";

			distance_unit[0] = (isMetric ? getString(R.string.long_unit1_detail_9)
					: getString(R.string.long_unit2_detail_9));
			distance_unit[1] = (isMetric ? getString(R.string.long_unit1_detail_10)
					: getString(R.string.long_unit2_detail_10));
			distance_unit[2] = (isMetric ? getString(R.string.long_unit1_detail_7)
					: getString(R.string.long_unit2_detail_7));
			distance_unit[3] = (isMetric ? getString(R.string.long_unit1_detail_8)
					: getString(R.string.long_unit2_detail_8));

			sentence = FunctionUtils.generateStringToSay(mContext, ttsParams, distance_unit, false, false);
		}

		TextToSpeechUtils.say(mContext, sentence);
	}

	private BroadcastReceiver mReceiverEmptySensorManager = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			releaseSensorManager();
			sensorManager = null;
		}
	};

	private BroadcastReceiver mReceiverBeatCounter = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ((MoveOnService.getIsPracticeRunning()) && (!MoveOnService.getIsPracticePaused())) {
				mHeartRate = String.valueOf(Integer.parseInt(intent.getStringExtra("beats")));

				heartRateList.add(Integer.parseInt(mHeartRate));

				if (maxHR < (Integer.parseInt(mHeartRate)))
					maxHR = Integer.parseInt(mHeartRate);

				mTime = (((SystemClock.elapsedRealtime() / 1000) - (elapsedRealtime / 1000)) + accumulatedTime);

				populatesHr(mTime, Integer.parseInt(mHeartRate));
			}
		}
	};

	private void populatesHr(long time, int mHr) {
		hr = new EntityHeartRate();
		hr.setTime(String.valueOf(time));
		hr.setHr(String.valueOf(mHr));
		hrList.add(hr);
	}

	private BroadcastReceiver mReceiverStepCounter = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mSteps = intent.getStringExtra("steps");
		}
	};

	private BroadcastReceiver mReceiverPlaySound = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("sound") != null)
				playSound(Integer.parseInt(intent.getStringExtra("sound")));
		}
	};

	private BroadcastReceiver mReceiverRefreshSensitivity = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			stepDetectorSensitivity();
		}
	};

	private BroadcastReceiver mReceiverCadence = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ((MoveOnService.getIsPracticeRunning()) && (!MoveOnService.getIsPracticePaused())) {
				mCadence = String.valueOf(Integer.parseInt(intent.getStringExtra("cadence")));

				cadenceValuesList.add(Integer.parseInt(mCadence));

				if (maxCadence < (Integer.parseInt(mCadence)))
					maxCadence = Integer.parseInt(mCadence);

				mTime = (((SystemClock.elapsedRealtime() / 1000) - (elapsedRealtime / 1000)) + accumulatedTime);

				populatesCadence(mTime, Integer.parseInt(mCadence));
			}
		}
	};

	private void populatesCadence(long time, int mCad) {
		cadence = new EntityCadence();
		cadence.setTime(String.valueOf(time));
		cadence.setCadence(String.valueOf(mCad));
		cadenceList.add(cadence);
	}

	private BroadcastReceiver mReceiverRegisterMediaButton = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			registerMediaButtonReceiver();
		}
	};

	private BroadcastReceiver mReceiverUnregisterMediaButton = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			unregisterMediButtonReceiver();
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public MoveOnService getService() {
			return MoveOnService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		tts = TextToSpeechUtils.getInstance();
		tts.initTTS(mContext);

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		showNotification();

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mHandler = new Handler();
		mHandler.postDelayed(mRunnable, 0);

		acquireWakeLock();

		mStepDetector = new StepDetector();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		registerDetector();

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mComponentName = new ComponentName(getPackageName(), MediaButtonEventReceiver.class.getName());

		mSettingsObserver = new SettingsObserver(this, mContext, mComponentName);
		mContentResolver = getContentResolver();
		mContentResolver.registerContentObserver(Settings.System.getUriFor(MEDIA_BUTTON_RECEIVER), false,
				mSettingsObserver);

		registerMediaButtonReceiver();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiverScreenOff, filter);

		IntentFilter filter2 = new IntentFilter("android.intent.action.ACTION_SCREEN_REFRESH");
		registerReceiver(mReceiverRefreshScreenPreferences, filter2);

		IntentFilter filter3 = new IntentFilter("android.intent.action.ACTION_SAY_PRACTICE_INFORMATION");
		registerReceiver(mReceiverSayInformation, filter3);

		IntentFilter filter4 = new IntentFilter("android.intent.action.STEP_COUNTER");
		registerReceiver(mReceiverStepCounter, filter4);

		IntentFilter filter5 = new IntentFilter("android.intent.action.BEAT_COUNTER");
		registerReceiver(mReceiverBeatCounter, filter5);

		IntentFilter filter6 = new IntentFilter("android.intent.action.EMPTY_SENSOR_MANAGER");
		registerReceiver(mReceiverEmptySensorManager, filter6);

		IntentFilter filter7 = new IntentFilter("android.intent.action.ACTION_PLAY_SOUND");
		registerReceiver(mReceiverPlaySound, filter7);

		IntentFilter filter8 = new IntentFilter("android.intent.action.ACTION_REFRESH_SENSITIVITY");
		registerReceiver(mReceiverRefreshSensitivity, filter8);

		IntentFilter filter9 = new IntentFilter("android.intent.action.CADENCE");
		registerReceiver(mReceiverCadence, filter9);

		IntentFilter filter10 = new IntentFilter("android.intent.action.REGISTER_MEDIA_BUTTON_STATUS");
		registerReceiver(mReceiverRegisterMediaButton, filter10);

		IntentFilter filter11 = new IntentFilter("android.intent.action.UNREGISTER_MEDIA_BUTTON_STATUS");
		registerReceiver(mReceiverUnregisterMediaButton, filter11);

		mStepCounter = new StepCounter(mContext);
		mStepDetector.addStepListener(mStepCounter);

		mTimeManager = new TimeManager(mContext);
		mDistanceManager = new DistanceManager(mContext);

		locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		listener = new mGpsListener();
		locListener = new mLocationListener();
		locMgr.addGpsStatusListener(listener);
		locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_TIME_INTERVAL,
				GPS_UPDATE_DISTANCE_INTERVAL, locListener);
	}

	@Override
	public void onDestroy() {
		stopLocations();

		unregisterReceiver(mReceiverScreenOff);
		unregisterReceiver(mReceiverRefreshScreenPreferences);
		unregisterReceiver(mReceiverSayInformation);
		unregisterReceiver(mReceiverStepCounter);
		unregisterReceiver(mReceiverEmptySensorManager);
		unregisterReceiver(mReceiverBeatCounter);
		unregisterReceiver(mReceiverPlaySound);
		unregisterReceiver(mReceiverRefreshSensitivity);
		unregisterReceiver(mReceiverCadence);
		unregisterReceiver(mReceiverRegisterMediaButton);
		unregisterReceiver(mReceiverUnregisterMediaButton);

		mContentResolver.unregisterContentObserver(mSettingsObserver);
		unregisterMediButtonReceiver();

		releaseSensorManager();
		if (bluetoothAdapter != null)
			bluetoothAdapter = null;

		mHandler.removeCallbacks(mRunnable);

		releaseCountDownTimer();

		if (timerPausable != null) {
			timerPausable.cancel();
			timerPausable = null;
		}

		unregisterDetector();
		wakeLock.release();
		releaseMediaPlayer();
		mNotificationManager.cancel(NOTIFICATION_ID); // The same unique
														// notification number

		tts.shutdownTTS();

		super.onDestroy();
	}

	private void releaseSensorManager() {
		if (sensorManager != null) {
			SensorManagerFactory.releaseSystemSensorManager();
			sensorManager = null;
		}
	}

	@SuppressWarnings("deprecation")
	private void unregisterMediButtonReceiver() {
		try {
			mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void unregisterDetector() {
		mSensorManager.unregisterListener(mStepDetector);
	}

	private static void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startid) {
		super.onStart(intent, startid);
	}

	private void showNotification() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_silhouette)
				.setContentTitle(getString(R.string.app_complete_name))
				.setContentText(getString(R.string.local_service_started)).setOngoing(true);

		Intent notificationIntent = new Intent(mContext, SplashScreen.class);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		PendingIntent pIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, notificationIntent, 0);
		builder.setContentIntent(pIntent);

		Notification notif = builder.build();
		mNotificationManager.notify(NOTIFICATION_ID, notif);
	}

	@SuppressWarnings("deprecation")
	private void acquireWakeLock() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		int wakeFlags;

		if (prefs.getString("operation_level", "run_in_background").equals("wake_up"))
			wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
		else if (prefs.getString("operation_level", "run_in_background").equals("keep_screen_on"))
			wakeFlags = PowerManager.SCREEN_DIM_WAKE_LOCK;
		else
			wakeFlags = PowerManager.PARTIAL_WAKE_LOCK;

		wakeLock = pm.newWakeLock(wakeFlags, TAG);
		wakeLock.acquire();
	}

	private void registerDetector() {
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(mStepDetector, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

		stepDetectorSensitivity();
	}

	public void stepDetectorSensitivity() {
		if (mStepDetector != null)
			mStepDetector.setSensitivity(Float.valueOf(prefs.getString("sensitivity_level", "10")));
	}

	@SuppressWarnings("deprecation")
	private void registerMediaButtonReceiver() {
		try {
			mAudioManager.registerMediaButtonEventReceiver(mComponentName);
		} catch (Exception e) {
			UIFunctionUtils.showMessage(mContext, false,
					mContext.getString(R.string.tts_media_button_register_error) + ":\n" + e.toString());
		}
	}

	private void playSound(int id) {
		switch (id) {
		case 1:
			mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
			break;
		case 2:
			mMediaPlayer = MediaPlayer.create(this, R.raw.coach_whistle);
			break;
		}

		mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
			}
		});

		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.stop();
				mp.release();
			}
		});
	}

	public void stopLocations() {
		if (listener != null)
			locMgr.removeGpsStatusListener(listener);

		if (locListener != null)
			locMgr.removeUpdates(locListener);

		locMgr = null;
	}

	public static void startPractice(long t) {
		if (prefs.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes()) == TypesOfPractices.HIIT_PRACTICE
				.getTypes()) {
			releaseCountDownTimer();
			startCountDownTimer();
		}

		isPracticeRunning = true;
		pause = false;
		autoPause = false;
		isPausedForSavePractice = false;

		elapsedRealtime = t;
		accumulatedTime = 0;

		locListener.start();

		routesList.clear();
		locationsList.clear();
		speedList.clear();
		hrList.clear();
		heartRateValuesList.clear();
		maxHR = 0;
		cadenceValuesList.clear();
		maxCadence = 0;

		SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		String currentDate = sdfDate.format(new Date());
		String currentTime = sdfTime.format(new Date());

		route = new EntityRoutes();
		route.setDate(currentDate);
		route.setHour(currentTime);

		TextToSpeechUtils.say(mContext, mContext.getString(R.string.tts_start_practice));

		if (prefs.getBoolean("lock_app", true)) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("blocked", true);
			editor.commit();

			buttonsStatusIntent.putExtra("practiceButtonsStatus", String.valueOf(Constants.LOCKED_STATUS));
			mContext.sendBroadcast(buttonsStatusIntent);
		}
	}

	public static void pausePractice() {
		pause = true;
		lengthChrono = SystemClock.elapsedRealtime();

		if (prefs.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes()) == TypesOfPractices.HIIT_PRACTICE
				.getTypes())
			pausableCountDownTimer.pause();

		if (prefs.getBoolean("speak", false))
			TextToSpeechUtils.say(mContext, mContext.getString(R.string.tts_pause_practice));
	}

	public static void resumePractice() {
		accumulatedTime = accumulatedTime + (((lengthChrono / 1000) - (elapsedRealtime / 1000)));
		elapsedRealtime = SystemClock.elapsedRealtime();
		pause = false;
		resumelocations = true;

		if (prefs.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes()) == TypesOfPractices.HIIT_PRACTICE
				.getTypes())
			pausableCountDownTimer.resume();

		if (prefs.getBoolean("speak", false))
			TextToSpeechUtils.say(mContext, mContext.getString(R.string.tts_resume_practice));
	}

	public static void stopPractice() {
		isPracticeRunning = false;
		isPausedForSavePractice = false;

		TextToSpeechUtils.say(mContext, mContext.getString(R.string.tts_stop_practice));

		if (!pause)
			locListener.populatesRoute(((SystemClock.elapsedRealtime() / 1000) - (elapsedRealtime / 1000))
					+ accumulatedTime);
		else
			locListener.populatesRoute((accumulatedTime + (lengthChrono / 1000)) - (elapsedRealtime / 1000));

		pause = true;
		autoPause = false;

		if (prefs.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes()) == TypesOfPractices.HIIT_PRACTICE
				.getTypes())
			releaseCountDownTimer();

		releaseMediaPlayer();
	}

	public static boolean getIsPracticeRunning() {
		return isPracticeRunning;
	}

	public static boolean getIsPracticePaused() {
		return pause;
	}

	public static long getTime() {
		return elapsedRealtime;
	}

	public static long getLengthChrono() {
		return lengthChrono;
	}

	public static long getAccumulatedTime() {
		return accumulatedTime;
	}

	public static long getCalories() {
		return mCalories;
	}

	public static ArrayList<EntityRoutes> getRoutesList() {
		return routesList;
	}

	public static ArrayList<EntityLocations> getLocationsList() {
		return locationsList;
	}

	public static ArrayList<EntityHeartRate> getHrList() {
		return hrList;
	}

	public static ArrayList<EntityCadence> getCadenceList() {
		return cadenceList;
	}

	public static void setPausedForSavePractice(boolean value) {
		isPausedForSavePractice = value;
	}

	public static void addMoreDataToRoutesEntity(String name, String objetive_type, String shoe, String notes) {
		routesList.clear();

		route.setName(name);
		route.setShoe_id(shoe);
		route.setComments(notes);
		if (Integer.parseInt(objetive_type) != TypesOfPractices.HIIT_PRACTICE.getTypes())
			route.setHiit_id("0");
		else
			route.setHiit_id(String.valueOf(prefs.getInt("selected_hiit", 0)));

		routesList.add(route);
	}

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			long time = (((SystemClock.elapsedRealtime() / 1000) - (elapsedRealtime / 1000)) + accumulatedTime);

			if ((isPracticeRunning) && (!autoPause) && (!pause) && (time > 0)) {
				mTimeManager.onTime((int) time);
				calculateCalories();
			}

			sensorConnectionManager();

			mHandler.postDelayed(this, 1000);
		}
	};

	private void sensorConnectionManager() {
		if (Constants.getString(mContext, R.string.sensor_type_key, mContext.getString(R.string.none))
				.equals(mContext.getString(R.string.none))) {
			releaseSensorManager();
			sensorManager = null;
		}

		if (sensorManager == null) {
			boolean checkANT = false, checkBT = false;

			if ((prefs.getString("sensor_type_key", mContext.getString(R.string.none))).equals(mContext
					.getString(R.string.ant_sensor_type)))
				checkANT = true;

			if ((bluetoothAdapter != null) && (bluetoothAdapter.isEnabled())) {
				List<String> optionsList = new ArrayList<String>();
				List<String> valuesList = new ArrayList<String>();

				BluetoothDeviceUtils.populateDeviceLists(bluetoothAdapter, optionsList, valuesList);

				String value = Constants.getString(this, R.string.bluetooth_sensor_key,
						Constants.BLUETOOTH_SENSOR_DEFAULT);

				if (valuesList.contains(value))
					value = Constants.getString(this, R.string.bluetooth_sensor_key,
							Constants.BLUETOOTH_SENSOR_DEFAULT);
				else
					value = Constants.BLUETOOTH_SENSOR_DEFAULT;

				if (value.length() > 0)
					checkBT = true;
			}

			if (checkANT || checkBT)
				sensorManager = SensorManagerFactory.getSystemSensorManager(mContext);
			else
				sensorManager = null;
		}
	}

	private void calculateCalories() {
		calculatesTimeAndDistanceHavePassed(time, locListener.getmDistance());
		sumOfKcal = sumOfKcal
				+ FunctionUtils.calculateCalories(
						ActivityTypes.values()[prefs.getInt("last_activity", 1) - 1],
						Float.parseFloat(prefs.getString("body_weight", "75.0")),
						prefs.getString("gender", "M"), howLongHasItBeen, howHasPassedAway);
		mCalories = (int) sumOfKcal;
	}

	private void calculatesTimeAndDistanceHavePassed(long seconds, String mDistance) {
		if (((int) seconds - (int) lastTime) > 0) {
			howLongHasItBeen = (int) seconds - (int) lastTime;
			lastTime = Float.valueOf(seconds);
		} else {
			howLongHasItBeen = 0;
		}

		if (Float.valueOf(mDistance) > lastDistance) {
			howHasPassedAway = Float.valueOf(mDistance) - lastDistance;
			lastDistance = Float.valueOf(mDistance);
		} else {
			howHasPassedAway = 0;
		}
	}

	private static void releaseCountDownTimer() {
		if (pausableCountDownTimer != null) {
			pausableCountDownTimer.cancel();
			pausableCountDownTimer = null;
		}
	}

	private static void startCountDownTimer() {
		if (prefs.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes()) == TypesOfPractices.HIIT_PRACTICE
				.getTypes())
			pausableCountDownTimer = new HiitCountDownTimerPausable(mContext, prefs, 1000).start();
	}

	public class mLocationListener implements LocationListener {
		private String mSpeed = getString(R.string.zero_with_one_decimal_place_value);
		private String mMaxSpeed = getString(R.string.zero_with_one_decimal_place_value);
		private String mDistance = getString(R.string.zero_with_two_decimal_places_value);
		private String mLatitude;
		private String mLongitude;
		private String mAltitude = "";
		private String mMinAltitude = getString(R.string.zero_value);
		private String mMaxAltitude = getString(R.string.zero_value);
		private String up_accum_altitude = getString(R.string.zero_value);
		private String down_accum_altitude = getString(R.string.zero_value);

		private float dist, last_altitude;

		private boolean areTheFirstGpsData;

		public String getmDistance() {
			return mDistance;
		}

		public void start() {
			mSpeed = getString(R.string.zero_with_one_decimal_place_value);
			mMaxSpeed = getString(R.string.zero_with_one_decimal_place_value);
			mDistance = getString(R.string.zero_with_two_decimal_places_value);
			mLatitude = "";
			mLongitude = "";
			mAltitude = "";
			up_accum_altitude = getString(R.string.zero_value);
			down_accum_altitude = getString(R.string.zero_value);
			mMinAltitude = getString(R.string.zero_value);
			mMaxAltitude = getString(R.string.zero_value);
			mSteps = mContext.getString(R.string.zero_value);
			mHeartRate = mContext.getString(R.string.zero_value);
			mCadence = mContext.getString(R.string.zero_value);
			mCalories = 0;
			dist = 0;
			time = 0;
			mTime = 0;
			howHasPassedAway = 0;
			howLongHasItBeen = 0;
			sumOfKcal = 0;
			lastTime = 0;
			lastDistance = 0;
			mStepCounter.setStepValue(0);
			mTimeManager.setSeconds(0);
			mDistanceManager.setMetersValue(0);
			areTheFirstGpsData = true;
			lastLocation = null;
			actualLocation = null;
			ttsParams = null;

			isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

			autoPause = false;
		}

		@Override
		public void onLocationChanged(Location location) {
			sendGpsAccuracy(location);
			checkGpsValues(location);

			if (isPracticeRunning) {
				float autoPauseSpeedLimit = 0.0f;
				switch (Integer.valueOf(prefs.getString("auto_pause", "0"))) {
				case 2:
					autoPauseSpeedLimit = 2.0f;
					break;
				case 5:
					autoPauseSpeedLimit = 5.0f;
					break;
				}

				if ((Float.valueOf(mSpeed) <= autoPauseSpeedLimit) && (autoPauseSpeedLimit > 0.0f)
						&& (!autoPause))
					autoPause = true;

				if (prefs.getInt("practice_display5", TypesOfPractices.BASIC_PRACTICE.getTypes()) != TypesOfPractices.HIIT_PRACTICE
						.getTypes()) { // HIIT disables auto-pause
					if ((autoPause) && (pause) && (Float.valueOf(mSpeed) > autoPauseSpeedLimit)) {
						autoPause = false;

						resumePractice();

						practiceStatusIntent.putExtra("practiceStatus",
								String.valueOf(Constants.PRACTICE_AUTO_RESUME_STATUS));
						mContext.sendBroadcast(practiceStatusIntent);

						MoveOnService.setPausedForSavePractice(false);
					} else if ((autoPause) && (!pause)) {
						pausePractice();

						practiceStatusIntent.putExtra("practiceStatus",
								String.valueOf(Constants.PRACTICE_AUTO_PAUSE_STATUS));
						mContext.sendBroadcast(practiceStatusIntent);

						MoveOnService.setPausedForSavePractice(true);
					}
				}
			}

			if (!pause) {
				if (areTheFirstGpsData == true) {
					if (location.getAltitude() > 0) {
						mMaxAltitude = String.valueOf((int) location.getAltitude());
						mMinAltitude = String.valueOf((int) location.getAltitude());

						last_altitude = (float) location.getAltitude();
					}
					areTheFirstGpsData = false;
				}

				if ((Float.valueOf(mMaxSpeed)) < (Float.valueOf(mSpeed)))
					mMaxSpeed = mSpeed;

				if (actualLocation != null && (FunctionUtils.isNumeric(mAltitude))) {
					if ((Float.valueOf(mMaxAltitude)) < (Float.valueOf(mAltitude)))
						mMaxAltitude = mAltitude;

					if ((Float.valueOf(mMinAltitude)) > (Float.valueOf(mAltitude)))
						mMinAltitude = mAltitude;
				} else {
					mAltitude = getString(R.string.zero_value);
				}

				Intent i = new Intent("android.intent.action.GPS_DATA");
				i.putExtra("speed", mSpeed);
				i.putExtra("maxSpeed", mMaxSpeed);
				i.putExtra("distance", mDistance);
				i.putExtra("latitude", mLatitude);
				i.putExtra("longitude", mLongitude);
				i.putExtra("altitude", mAltitude);
				sendBroadcast(i);

				speedList.add(Float.parseFloat(mSpeed));

				if (FunctionUtils.isNumeric(String.valueOf(last_altitude))) {
					if (location.getAltitude() > last_altitude)
						up_accum_altitude = String.valueOf(Float.parseFloat(up_accum_altitude)
								+ ((location.getAltitude() - last_altitude)));
					else if (location.getAltitude() < last_altitude)
						down_accum_altitude = String.valueOf(Float.parseFloat(down_accum_altitude)
								+ ((last_altitude - location.getAltitude())));
				}

				ttsParams = checkParametersToSay(false);

				time = (((SystemClock.elapsedRealtime() / 1000) - (elapsedRealtime / 1000)) + accumulatedTime);

				if (resumelocations) {
					lastLocation = location;
					resumelocations = false;
				}
				actualLocation = location;
			}

			if ((isPracticeRunning) && (!isPausedForSavePractice) && (!autoPause))
				populatesLocation(location, String.valueOf((int) time));

			mLastLocationMillis = SystemClock.elapsedRealtime();

			Intent intent = new Intent("android.intent.action.GPS_LOCATION");
			intent.putExtra("changedlocation", true);
			sendBroadcast(intent);
		}

		private String[] checkParametersToSay(boolean roundTime) {
			String[] params = new String[11];
			long mTime = 0;
			isMetric = FunctionUtils.checkIfUnitsAreMetric(mContext);

			for (int l = 0; l <= 10; l++) {
				DisplayTypes whichData = DisplayTypes.values()[l];
				switch (whichData) {
				case TIME:
					if (prefs.getBoolean("tell_duration", true)) {
						if (!pause)
							mTime = (((SystemClock.elapsedRealtime() / 1000) - (elapsedRealtime / 1000)) + accumulatedTime);
						else
							mTime = (accumulatedTime + (lengthChrono / 1000)) - (elapsedRealtime / 1000);

						params[l] = String.valueOf(mTime);

						if (roundTime)
							params[l] = String.valueOf((int) Float.parseFloat(params[l]));
					} else {
						params[l] = "";
					}
					break;
				case DISTANCE:
					if (prefs.getBoolean("tell_distance", true))
						params[l] = String
								.valueOf(isMetric ? mDistance
										: FunctionUtils.formatFloatToDecimalFormatString((Float
												.valueOf(mDistance) * 1000f) / 1609f));
					else
						params[l] = "";
					break;
				case SPEED:
					if (prefs.getBoolean("tell_speed", false))
						params[l] = String.valueOf(isMetric ? mSpeed : FunctionUtils
								.formatFloatToDecimalFormatString((Float.valueOf(mSpeed) * 1000f) / 1609f));
					else
						params[l] = "";
					break;
				case AVG_SPEED:
					if (prefs.getBoolean("tell_avg_speed", false))
						params[l] = String.valueOf(FunctionUtils.calculateAvg(mContext, mTime, mDistance,
								isMetric));
					else
						params[l] = "";
					break;
				case MAX_SPEED:
					if (prefs.getBoolean("tell_max_speed", false))
						params[l] = String
								.valueOf(isMetric ? mMaxSpeed
										: FunctionUtils.formatFloatToDecimalFormatString((Float
												.valueOf(mMaxSpeed) * 1000f) / 1609f));
					else
						params[l] = "";
					break;
				case RITM:
					if (prefs.getBoolean("tell_ritm", true))
						params[l] = String.valueOf(FunctionUtils.calculateRitm(mContext, mTime, mDistance,
								isMetric, true));
					else
						params[l] = "";
					break;
				case ALTITUDE:
					if (prefs.getBoolean("tell_altitude", false))
						params[l] = String.valueOf(isMetric ? mAltitude : String.valueOf((int) (Float
								.parseFloat(mAltitude) * 1.0936f)));
					else
						params[l] = "";
					break;
				case KCAL:
					if (prefs.getBoolean("tell_calories", false))
						params[l] = String.valueOf(mCalories);
					else
						params[l] = "";
					break;
				case STEPS:
					if (prefs.getBoolean("tell_steps", false))
						params[l] = String.valueOf(mSteps);
					else
						params[l] = "";
					break;
				case BEATS:
					if (prefs.getBoolean("tell_heart_rate", false))
						params[l] = String.valueOf(mHeartRate);
					else
						params[l] = "";
					break;
				case CADENCE:
					if (prefs.getBoolean("tell_cadence", false))
						params[l] = String.valueOf(mCadence);
					else
						params[l] = "";
					break;
				default:
					break;
				}
			}
			return params;
		}

		@Override
		public void onProviderDisabled(String provider) {
			isGPSFix = false;
			sendStoppedStatus();
		}

		@Override
		public void onProviderEnabled(String provider) {
			isGPSFix = false;
			checkGpsStatus();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		private void checkGpsValues(Location loc) {
			if (lastLocation != null)
				last_altitude = Float.valueOf(String.valueOf(lastLocation.getAltitude()));

			mSpeed = getSpeed(loc);
			mDistance = getDistance();

			mLatitude = String.valueOf(loc.getLatitude());
			mLongitude = String.valueOf(loc.getLongitude());
			mAltitude = String.valueOf((int) loc.getAltitude());
		}

		public String getSpeed(Location loc) {
			String s = null;
			StringBuffer sb = new StringBuffer();
			float kph;

			if (loc.hasSpeed()) {
				kph = FunctionUtils.round((float) loc.getSpeed() / 0.277); // m/s
																			// to
																			// km/h
				sb.append(kph);
			} else {
				sb.append(getString(R.string.zero_with_one_decimal_place_value));
			}

			s = sb.toString();
			return s;
		}

		public String getDistance() {
			String locDistance = getString(R.string.zero_with_two_decimal_places_value);
			StringBuffer sb = new StringBuffer();

			if ((actualLocation != null) && (lastLocation != null)) {
				float d = FunctionUtils.round((float) lastLocation.distanceTo(actualLocation));

				mDistanceManager.onDistance((int) d);
				dist += d;

				DecimalFormat df = new DecimalFormat("0.00");
				String formated_distance = df.format(dist / 1000);
				sb.append(formated_distance.replace(",", "."));
				locDistance = sb.toString();
			}

			lastLocation = actualLocation;
			return locDistance;
		}

		private void populatesRoute(long time) {
			route.setCategory_id(String.valueOf(prefs.getInt("last_activity", 1)));
			route.setTime(String.valueOf(time));
			route.setDistance(mDistance);
			route.setAvg_speed(String.valueOf(FunctionUtils.calculateAverageToFloatValue(speedList)));
			route.setMax_speed(mMaxSpeed);
			calculatesTimeAndDistanceHavePassed(time, mDistance);
			route.setKcal(String.valueOf(mCalories));
			route.setUp_accum_altitude(up_accum_altitude);
			route.setDown_accum_altitude(down_accum_altitude);
			route.setMax_altitude(mMaxAltitude);
			route.setMin_altitude(mMinAltitude);
			route.setSteps(mSteps);
			route.setAvg_hr(String.valueOf(((int) FunctionUtils.calculateAverageToDoubleValue(heartRateList))));
			route.setMax_hr(String.valueOf(maxHR));

			routesList.add(route);
		}

		private void populatesLocation(Location loc, String time) {
			if (actualLocation != null) {
				location = new EntityLocations();
				location.setLatitude(String.valueOf(loc.getLatitude()));
				location.setLongitude(String.valueOf(loc.getLongitude()));
				location.setAltitude(String.valueOf(loc.getAltitude()));
				location.setDistance(mDistance);
				location.setSpeed(mSpeed);
				location.setTime(time);
				location.setSteps(mSteps);
				location.setHr(mHeartRate);
				if (pause)
					location.setPause(String.valueOf(1));
				else
					location.setPause(String.valueOf(0));
				location.setCadence(mCadence);
				locationsList.add(location);
			}
		}
	}

	public class mGpsListener implements GpsStatus.Listener {
		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				isGPSFix = false;
				checkGpsStatus();
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				isGPSFix = true;
				checkGpsStatus();
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				isGPSFix = false;
				sendStoppedStatus();
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				isGPSFix = false;
				if ((SystemClock.elapsedRealtime() - mLastLocationMillis) < GPS_UPDATE_EXCEEDED_TIME)
					isGPSFix = true;

				checkGpsStatus();

				break;
			}
		}
	}

	public static int getGpsStatus() {
		return gpsStatus;
	}

	private void sendGpsAccuracy(Location location) {
		Intent i = new Intent("android.intent.action.GPS_ACCURACY");
		i.putExtra("gpsAccuracy", String.valueOf(gpsAccuracyToInteger(getGPSReception(location))));
		sendBroadcast(i);
	}

	private GpsReception getGPSReception(Location location) {
		if (!location.hasAccuracy()) {
			return GpsReception.NONE;
		}
		if (location.getAccuracy() <= MAX_METERS_EXCELLENT) {
			return GpsReception.EXCELLENT;
		} else if (location.getAccuracy() <= MAX_METERS_GOOD) {
			return GpsReception.GOOD;
		} else if (location.getAccuracy() <= MAX_METERS_WEAK) {
			return GpsReception.WEAK;
		} else if (location.getAccuracy() <= MAX_METERS_BAD) {
			return GpsReception.BAD;
		} else {
			return GpsReception.NONE;
		}
	}

	private int gpsAccuracyToInteger(GpsReception reception) {
		switch (reception) {
		case EXCELLENT:
			return 4;
		case GOOD:
			return 3;
		case WEAK:
			return 2;
		case BAD:
			return 1;
		case NONE:
			return 0;
		default:
			return 0;
		}
	}

	private void checkGpsStatus() {
		if (isGPSFix) { // A fix has been acquired.
			if (gpsStatus != GPS_ITEM_ON) {
				gpsStatus = GPS_ITEM_ON;
			}
			sendStatus();
		} else { // The fix has been lost.
			if (gpsStatus != GPS_ITEM_SEARCHING) {
				gpsStatus = GPS_ITEM_SEARCHING;
			}
			sendStatus();
		}
	}

	private void sendStoppedStatus() {
		if (gpsStatus != GPS_ITEM_OFF) {
			gpsStatus = GPS_ITEM_OFF;
			sendStatus();
		}
	}

	private void sendStatus() {
		Intent i = new Intent("android.intent.action.GPS_STATUS");
		i.putExtra("GPSStatus", String.valueOf(gpsStatus));
		sendBroadcast(i);
	}
}