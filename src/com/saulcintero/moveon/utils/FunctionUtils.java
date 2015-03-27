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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.TypedValue;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.enums.ActivityTypes;
import com.saulcintero.moveon.enums.DisplayTypes;

public class FunctionUtils {
	private final static Map<ActivityTypes, Float[]> KCALS = new HashMap<ActivityTypes, Float[]>();

	static {
		KCALS.put(ActivityTypes.CLIMBING_STAIRS, new Float[] { 0.254f });
		KCALS.put(ActivityTypes.HIKING, new Float[] { 0.147f });
		KCALS.put(ActivityTypes.SOCCER, new Float[] { 0.137f });
		KCALS.put(ActivityTypes.BASKETBALL, new Float[] { 0.14f });
		KCALS.put(ActivityTypes.HANDBALL, new Float[] { 0.093f });
		KCALS.put(ActivityTypes.TENNIS, new Float[] { 0.101f });
		KCALS.put(ActivityTypes.SKATEBOARDING, new Float[] { 0.089f });
		KCALS.put(ActivityTypes.BASEBALL, new Float[] { 0.168f });
		KCALS.put(ActivityTypes.DRIVE_CAR, new Float[] { 0.043f });
		KCALS.put(ActivityTypes.DRIVE_MOTORBIKE, new Float[] { 0.053f });
		KCALS.put(ActivityTypes.DRIVE_QUAD, new Float[] { 0.056f });
		KCALS.put(ActivityTypes.MARTIAL_ARTS, new Float[] { 0.185f });
		KCALS.put(ActivityTypes.BOXING, new Float[] { 0.159f });
		KCALS.put(ActivityTypes.SQUASH, new Float[] { 0.152f });
		KCALS.put(ActivityTypes.TREKKING, new Float[] { 0.125f });
		KCALS.put(ActivityTypes.SKATING, new Float[] { 0.15f });
		KCALS.put(ActivityTypes.ICE_SKATING, new Float[] { 0.149f });
		KCALS.put(ActivityTypes.SNOWBOARDING, new Float[] { 0.103f });
		KCALS.put(ActivityTypes.CROSS_COUNTRY_SKI, new Float[] { 0.162f });
		KCALS.put(ActivityTypes.ROWING, new Float[] { 0.18f });
		KCALS.put(ActivityTypes.NAVIGATION, new Float[] { 0.149f });
		KCALS.put(ActivityTypes.BILLIARDS, new Float[] { 0.026f });
		KCALS.put(ActivityTypes.GOLFING, new Float[] { 0.079f });
		KCALS.put(ActivityTypes.BOWLING, new Float[] { 0.098f });
		KCALS.put(ActivityTypes.TABLE_TENNIS, new Float[] { 0.057f });
		KCALS.put(ActivityTypes.HORSE_RIDING, new Float[] { 0.107f });
		KCALS.put(ActivityTypes.SKIPPING, new Float[] { 0.175f });
		KCALS.put(ActivityTypes.BOULES, new Float[] { 0.052f });
		KCALS.put(ActivityTypes.VOLLEYBALL, new Float[] { 0.053f });
		KCALS.put(ActivityTypes.PILATES, new Float[] { 0.058f });
		KCALS.put(ActivityTypes.YOGA, new Float[] { 0.062f });
		KCALS.put(ActivityTypes.RUGBY, new Float[] { 0.17f });
		KCALS.put(ActivityTypes.HOCKEY, new Float[] { 0.119f });
		KCALS.put(ActivityTypes.SWIMMING, new Float[] { 0.128f });
		KCALS.put(ActivityTypes.SURFING, new Float[] { 0.98f });
		KCALS.put(ActivityTypes.WINDSURFING, new Float[] { 0.05f });
		KCALS.put(ActivityTypes.SCUBA_DIVING, new Float[] { 0.107f });
		KCALS.put(ActivityTypes.CLIMBING, new Float[] { 0.14f });
		KCALS.put(ActivityTypes.DANCING, new Float[] { 0.11f });
		KCALS.put(ActivityTypes.HANG_GLIDING, new Float[] { 0.041f });
		KCALS.put(ActivityTypes.ARCHERY, new Float[] { 0.041f });
		KCALS.put(ActivityTypes.OTHERS, new Float[] { 0f });
		KCALS.put(ActivityTypes.WALKING, new Float[] { 0.051f, 0.069f, 0.08f });
		KCALS.put(ActivityTypes.RUNNING, new Float[] { 0.1f, 0.2f, 0.3f });
		KCALS.put(ActivityTypes.SPORT_CYCLING, new Float[] { 0.1f, 0.142f, 0.177f, 0.212f, 0.292f });
		KCALS.put(ActivityTypes.MOUNTAIN_BIKING, new Float[] { 0.15f, 0.192f, 0.227f, 0.262f, 0.342f });
		KCALS.put(ActivityTypes.INDOOR_CYCLING, new Float[] { 0.124f, 0.186f, 0.155f });
		KCALS.put(ActivityTypes.SKIING, new Float[] { 0.142f, 0.235f });
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public static boolean showGpsStatus(Context context) {
		String provider = "";

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
			provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
		else
			provider = Settings.Secure.getString(context.getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (!provider.contains("gps"))
			return false;
		else
			return true;
	}

	public static boolean checkIfUnitsAreMetric(Context context) {
		boolean isMetric;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if ((prefs.getString("units", context.getString(R.string.long_unit1)).equals(context
				.getString(R.string.long_unit1))))
			isMetric = true;
		else
			isMetric = false;

		return isMetric;
	}

	public static boolean containsChar(String text, String character) {
		if (text.indexOf(character) != -1)
			return true;
		else
			return false;
	}

	public static String formatFloatToDecimalFormatString(float number) {
		StringBuffer sb = new StringBuffer();
		DecimalFormat df = new DecimalFormat("0.00");
		String formated_distance = df.format(number);
		return sb.append(formated_distance.replace(",", ".")).toString();
	}

	public static float round(double d) {
		int rounded = (int) ((float) d * 10.0f);
		return (((float) rounded) / 10.0f);

	}

	public static float customizedRound(float val, int places) {
		long factor = (long) Math.pow(10, places);
		val = val * factor;
		long tmp = Math.round(val);
		return (float) tmp / factor;
	}

	public static String longFormatTTSTime(Context mContext, long seconds) {
		long s = seconds % 60;
		long m = seconds / 60;

		String min = mContext.getString(R.string.minute);
		String sec = mContext.getString(R.string.seconds);

		if (m != 1)
			min = mContext.getString(R.string.minutes);
		if (s != 1)
			sec = mContext.getString(R.string.seconds);

		return m + " " + min + " " + mContext.getString(R.string.and) + " " + s + " " + sec;
	}

	public static String shortFormatTTSTime(Context mContext, long seconds) {
		long m = seconds / 60;

		String min = mContext.getString(R.string.minute);

		if (m != 1)
			min = mContext.getString(R.string.minutes);

		return m + " " + min;
	}

	static public String longFormatTime(long seconds) {
		String sec = "";
		String min = "";
		String hou = "";
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / 60) / 60;

		sec = s + "";
		min = m + "";
		hou = h + "";

		if (s < 10)
			sec = "0" + s;
		if (m < 10)
			min = "0" + m;
		if (h < 10)
			hou = "0" + h;

		return hou + ":" + min + ":" + sec;
	}

	static public String statisticsFormatTime(Context mContext, long seconds) {
		String sec = "";
		String min = "";
		String hou = "";
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / 60) / 60;

		sec = s + "";
		min = m + "";
		hou = h + "";

		if (s < 10)
			sec = "0" + s;
		if (m < 10)
			min = "0" + m;
		if (h < 10)
			hou = "0" + h;

		if (h > 0)
			return hou + mContext.getString(R.string.hours_first_letter) + ":" + min
					+ mContext.getString(R.string.minutes_first_letter) + ":" + sec
					+ mContext.getString(R.string.seconds_first_letter);
		else
			return min + mContext.getString(R.string.minutes_first_letter) + ":" + sec
					+ mContext.getString(R.string.seconds_first_letter);
	}

	static public int getSecondsFromTime(String t) {
		String[] time = t.split(":");

		String sec = time[2];
		String min = time[1];
		String hou = time[0];
		int s = Integer.parseInt(sec);
		int m = Integer.parseInt(min) * 60;
		int h = Integer.parseInt(hou) * 3600;

		return s + m + h;
	}

	static public String shortFormatTime(long seconds) {
		String sec = "";
		String min = "";
		long s = seconds % 60;
		long m = seconds / 60;

		sec = s + "";
		min = m + "";

		if (s < 10)
			sec = "0" + s;
		if (m < 10)
			min = "0" + m;

		return min + ":" + sec;
	}

	public static boolean isNumeric(String str) {
		if (str.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+"))
			return true;
		else
			return false;
	}

	public static boolean dateValidator(final String sDate) {
		boolean result = false;

		String dateFormat = "dd/MM/yyyy";

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
		sdf.setLenient(false);

		try {
			@SuppressWarnings("unused")
			Date date = sdf.parse(sDate);
			result = true;
		} catch (ParseException e) {
		}

		return result;
	}

	public static boolean time24HoursValidator(final String sTime) {
		boolean result = false;

		String dateFormat = "HH:mm:ss";

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
		sdf.setLenient(false);

		try {
			@SuppressWarnings("unused")
			Date time = sdf.parse(sTime);
			result = true;
		} catch (ParseException e) {
		}

		return result;
	}

	public static Date getDateFromParameters(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static int getAge(Date initDate) {
		if (initDate != null) {
			Calendar dob = Calendar.getInstance();
			dob.setTime(initDate);
			Calendar today = Calendar.getInstance();

			int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
			if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH))
				age--;
			else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
					&& today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))
				age--;

			return age;
		} else {
			return 0;
		}
	}

	static public String calculateAvg(Context mContext, long seconds, String mDistance, boolean isMetric) {
		float formated_elapsedSeconds = Float.valueOf(seconds);
		float distance;
		String formated_avg = null;

		distance = (isMetric ? ((Float.valueOf(mDistance)) * 1000f)
				: ((Float.valueOf(mDistance)) * 1093.6133f));
		formated_avg = String.valueOf(customizedRound(
				(isMetric ? (float) ((distance / formated_elapsedSeconds) * 3.6f)
						: (float) ((distance / formated_elapsedSeconds) * 2.0454f)), 1));

		if (!isNumeric(formated_avg))
			formated_avg = mContext.getString(R.string.zero_with_one_decimal_place_value);

		return formated_avg.replace(",", ".");
	}

	static public String calculateRitm(Context mContext, long seconds, String mDistance, boolean isMetric,
			boolean isForTTS) {
		String gpsRitm = "";
		float metersOrYardsDistance;

		metersOrYardsDistance = (isMetric ? (Float.valueOf(mDistance) * 1000f)
				: ((Float.valueOf(mDistance) * 1000f) / 1609f));

		if ((Float.valueOf(mDistance).equals(Float.valueOf(mContext
				.getString(R.string.zero_with_two_decimal_places_value)))) && (!isForTTS)) {
			gpsRitm = mContext.getString(R.string.ritm_value);
		} else {
			float secondsInDistance;
			secondsInDistance = (isMetric ? ((seconds * 1000f) / metersOrYardsDistance)
					: (seconds / metersOrYardsDistance));

			long lSeconds = 0;
			if (isNumeric(String.valueOf(secondsInDistance)))
				lSeconds = (long) (secondsInDistance);

			if (isForTTS)
				gpsRitm = longFormatTTSTime(mContext, lSeconds);
			else
				gpsRitm = shortFormatTime(lSeconds);
		}
		return gpsRitm;
	}

	public static int getMetersFromYards(float yards) {
		return (int) (yards * 1.0936f);
	}

	public static float getKilometersFromMiles(float milles) {
		return ((milles * 1609f) / 1000f);
	}

	public static float getMilesFromKilometersWithTwoDecimals(float km) {
		return customizedRound(((km * 1000f) / 1609f), 2);
	}

	static public float calculateCalories(ActivityTypes activity, float weight, String sex,
			float howLongHasItBeen, float howHasPassedAway) {
		float calories = 0;
		float kcal = getKcal(activity, howHasPassedAway, howLongHasItBeen);

		boolean isActivityWithMovement = isActivityWithMovement(activity);

		if (kcal > 0) {
			if (isActivityWithMovement) {
				calories = (((Float.valueOf(howLongHasItBeen) / 60) * Float.valueOf(weight)) * kcal);

				if (sex.equals("F"))
					calories = calories - (calories * (float) 0.1);
			} else {
				calories = kcal;
			}
		}

		return calories;
	}

	private static boolean isActivityWithMovement(ActivityTypes activity) {
		boolean result = true;

		switch (activity) {
		case HOCKEY:
		case SWIMMING:
		case SURFING:
		case WINDSURFING:
		case SCUBA_DIVING:
		case CLIMBING:
		case DANCING:
		case ICE_SKATING:
		case BOXING:
		case SQUASH:
		case BILLIARDS:
		case GOLFING:
		case BOWLING:
		case TABLE_TENNIS:
		case SKIPPING:
		case BOULES:
		case VOLLEYBALL:
		case PILATES:
		case YOGA:
		case ARCHERY:
		case INDOOR_CYCLING:
			result = false;
			break;
		default:
			break;
		}

		return result;
	}

	private static float getKcal(ActivityTypes activity, float howHasPassedAway, float howLongHasItBeen) {
		float kcal = 0;
		int level = getLevel(activity, howHasPassedAway, howLongHasItBeen);

		switch (activity) {
		case WALKING:
		case RUNNING:
		case SPORT_CYCLING:
		case MOUNTAIN_BIKING:
		case SKIING:
		case CLIMBING_STAIRS:
		case HIKING:
		case SOCCER:
		case BASKETBALL:
		case HANDBALL:
		case TENNIS:
		case SKATEBOARDING:
		case BASEBALL:
		case DRIVE_CAR:
		case DRIVE_MOTORBIKE:
		case DRIVE_QUAD:
		case MARTIAL_ARTS:
		case TREKKING:
		case SKATING:
		case SNOWBOARDING:
		case CROSS_COUNTRY_SKI:
		case ROWING:
		case NAVIGATION:
		case HORSE_RIDING:
		case RUGBY:
		case HANG_GLIDING:
			kcal = getKcal(activity, level, howHasPassedAway > 0);
			break;
		case HOCKEY:
		case SWIMMING:
		case SURFING:
		case WINDSURFING:
		case SCUBA_DIVING:
		case CLIMBING:
		case DANCING:
		case ICE_SKATING:
		case BOXING:
		case SQUASH:
		case BILLIARDS:
		case GOLFING:
		case BOWLING:
		case TABLE_TENNIS:
		case SKIPPING:
		case BOULES:
		case VOLLEYBALL:
		case PILATES:
		case YOGA:
		case ARCHERY:
		case INDOOR_CYCLING:
		case OTHERS:
			kcal = getKcal(activity, level, true);
			break;
		}
		return kcal;
	}

	private static int getLevel(ActivityTypes activity, float howHasPassedAway, float howLongHasItBeen) {
		float kmHour = (howHasPassedAway * 3600) / Float.valueOf(howLongHasItBeen);
		int level = 0;

		switch (activity) {
		case WALKING:
			if (kmHour < 3.6)
				level = 0;
			else if (kmHour < 6)
				level = 1;
			else
				level = 2;
			break;
		case RUNNING:
			if (kmHour < 5.6)
				level = 0;
			else if (kmHour < 7.6)
				level = 1;
			else
				level = 2;
			break;
		case SPORT_CYCLING:
			if (kmHour < 15)
				level = 0;
			else if (kmHour < 23)
				level = 1;
			else if (kmHour < 27)
				level = 2;
			else if (kmHour < 32)
				level = 3;
			else
				level = 4;
			break;
		case MOUNTAIN_BIKING:
			if (kmHour < 15)
				level = 0;
			else if (kmHour < 23)
				level = 1;
			else if (kmHour < 27)
				level = 2;
			else if (kmHour < 32)
				level = 3;
			else
				level = 4;
			break;
		case SKIING:
			if (kmHour < 17)
				level = 0;
			else
				level = 1;
			break;
		case INDOOR_CYCLING:
			if (howHasPassedAway > 0) {
				if (kmHour < 21)
					level = 0;
				else
					level = 1;
			} else {
				level = 2;
			}
			break;
		default:
			level = 0;
		}
		return level;
	}

	private static float getKcal(ActivityTypes activity, int level, boolean isThereDistanceSinceLastTime) {
		float kcal = 0;
		if (isThereDistanceSinceLastTime)
			kcal = KCALS.get(activity)[level];

		return kcal;
	}

	public static int getIdForThisItem(String item, String[] activities) {
		int pos = 0;

		for (int p = 0; p <= (activities.length - 1); p++) {
			if (item.equals(activities[p]))
				break;

			pos += 1;
		}
		return pos;
	}

	public static BitmapDrawable createMarkerIcon(Context mContext, int drawableId, String text) {
		Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), drawableId).copy(
				Bitmap.Config.ARGB_8888, true);

		TypedValue outValue = new TypedValue();
		mContext.getResources().getValue(R.dimen.summary1_marker_text_size, outValue, true);
		float textSizeValue = outValue.getFloat();

		Paint imagePaint = new Paint();
		imagePaint.setStyle(Style.FILL);
		imagePaint.setColor(Color.BLACK);
		Typeface tf = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
		imagePaint.setTypeface(tf);
		imagePaint.setAntiAlias(true);
		imagePaint.setTextAlign(Align.CENTER);
		imagePaint.setTextSize(textSizeValue);

		Canvas canvas = new Canvas(bm);
		canvas.drawText(text, bm.getWidth() / 2, ((bm.getHeight() * 70) / 100), imagePaint);

		return new BitmapDrawable(mContext.getResources(), bm);
	}

	public static String generateStringToSay(Context mContext, String[] ttsParams, String[] distance_unit,
			boolean roundDistance, boolean roundTime) {
		String sentence = "";

		for (int l = 0; l <= 10; l++) {
			DisplayTypes whichData = DisplayTypes.values()[l];
			switch (whichData) {
			case TIME:
				if (ttsParams[l] != "")
					sentence = roundTime ? shortFormatTTSTime(mContext, Long.parseLong(ttsParams[l]))
							: longFormatTTSTime(mContext, Long.parseLong(ttsParams[l]));
				break;
			case DISTANCE:
				if (ttsParams[l] != "") {
					String distance = ttsParams[l];
					if (roundDistance)
						distance = String.valueOf((int) Float.parseFloat(ttsParams[l]));

					if (ttsParams[0] != "")
						sentence = sentence + " " + mContext.getString(R.string.in) + " " + distance + " "
								+ distance_unit[0];
					else
						sentence = sentence + distance + " " + distance_unit[0];
				}
				break;
			case SPEED:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + mContext.getString(R.string.speed_label) + " " + ttsParams[l] + " "
							+ distance_unit[2];
				}
				break;
			case AVG_SPEED:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + mContext.getString(R.string.avg_label_complete) + " "
							+ ttsParams[l] + " " + distance_unit[2];
				}
				break;
			case MAX_SPEED:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + mContext.getString(R.string.max_speed_label_complete) + " "
							+ ttsParams[l] + " " + distance_unit[2];
				}
				break;
			case RITM:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + mContext.getString(R.string.ritm_label_complete) + " "
							+ ttsParams[l] + " " + distance_unit[3];
				}
				break;
			case ALTITUDE:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + mContext.getString(R.string.altitude_label) + " " + ttsParams[l]
							+ " " + distance_unit[1];
				}
				break;
			case KCAL:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					String calories_label = mContext.getString(R.string.calories_label);
					if ((int) Float.parseFloat(ttsParams[l]) == 1)
						calories_label = mContext.getString(R.string.single_calories_label);

					sentence = sentence + ttsParams[l] + " " + calories_label;
				}
				break;
			case STEPS:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + ttsParams[l] + " " + mContext.getString(R.string.steps_label);
				}
				break;
			case BEATS:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + ttsParams[l] + " "
							+ mContext.getString(R.string.heart_rate_label_complete);
				}
				break;
			case CADENCE:
				if (ttsParams[l] != "") {
					if (sentence.length() > 0)
						sentence = sentence + ", ";

					sentence = sentence + ttsParams[l] + " "
							+ mContext.getString(R.string.cadence_label_complete);
				}
				break;
			default:
				break;
			}
		}
		return sentence;
	}

	static public String getYear(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		Calendar cal = Calendar.getInstance();

		try {
			Date mDate = dateFormat.parse(date);
			cal.setTime(mDate);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		return String.valueOf(cal.get(Calendar.YEAR));
	}

	public static String getNameOfMonth(int month, Locale locale) {
		DateFormatSymbols symbols = new DateFormatSymbols(locale);
		String[] monthNames = symbols.getMonths();
		return monthNames[month - 1];
	}

	static public double calculateAverageToDoubleValue(List<Integer> mList) {
		Integer sum = 0;
		for (Integer i : mList) {
			sum += i;
		}
		return sum.doubleValue() / mList.size();
	}

	static public float calculateAverageToFloatValue(List<Float> mList) {
		float sum = 0;
		for (float i : mList) {
			sum += i;
		}
		return sum / mList.size();
	}

	static public int calculateDpFromPx(Context mContext, int px) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, mContext.getResources()
				.getDisplayMetrics());
	}

	public static boolean IsNullOrEmpty(String text) {
		return text == null || text.length() == 0;
	}

	public static String[] fromCharsequenceArrayToArrayOfString(CharSequence[] sequence) {
		String[] result = new String[sequence.length];
		for (int i = 0; i <= (sequence.length - 1); i++) {
			result[i] = sequence[i].toString();
		}
		return result;
	}

	public static void createDirectory(String path) {
		File routeDirectory = new File(path);
		if (!routeDirectory.exists())
			routeDirectory.mkdirs();
	}

	public static File[] GetFilesInFolder(File folder) {
		return GetFilesInFolder(folder, null);
	}

	public static File[] GetFilesInFolder(File folder, FilenameFilter filter) {
		if (folder == null || !folder.exists() || folder.listFiles() == null) {
			return new File[] {};
		} else {
			if (filter != null)
				return folder.listFiles(filter);

			return folder.listFiles();
		}
	}

	public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
		FileChannel fromChannel = null;
		FileChannel toChannel = null;
		try {
			fromChannel = fromFile.getChannel();
			toChannel = toFile.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel);
		} finally {
			try {
				if (fromChannel != null)
					fromChannel.close();
			} finally {
				if (toChannel != null)
					toChannel.close();
			}
		}
	}

	public static boolean checkNetwork(Activity act) {
		boolean wifiAvailable = false;
		boolean mobileAvailable = false;
		ConnectivityManager conManager = (ConnectivityManager) act
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
		for (NetworkInfo netInfo : networkInfo) {
			if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
				if (netInfo.isConnected())
					wifiAvailable = true;
			if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
				if (netInfo.isConnected())
					mobileAvailable = true;
		}
		return wifiAvailable || mobileAvailable;
	}

	public static boolean checkFbInstalled(Activity act) {
		PackageManager pm = act.getPackageManager();
		boolean flag = false;
		try {
			pm.getPackageInfo("com.facebook.katana", PackageManager.GET_ACTIVITIES);
			flag = true;
		} catch (PackageManager.NameNotFoundException e) {
			flag = false;
		}
		return flag;
	}

	public static void getAppKeyHash(Activity act) {
		try {
			PackageInfo info = act.getPackageManager().getPackageInfo("com.saulcintero.moveon",
					PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());

				System.out.println("KeyHash : " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
		} catch (NoSuchAlgorithmException e) {
		}
	}

	public static void appHasAlreadyBeenLaunched(Context mContext) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("first_start", false);
		editor.commit();
	}

	public static String capitalizeFirtsLetter(String str) {
		StringBuilder sb = new StringBuilder(str);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}
}