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

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.entities.EntityCadence;
import com.saulcintero.moveon.entities.EntityHeartRate;
import com.saulcintero.moveon.entities.EntityHiit;
import com.saulcintero.moveon.entities.EntityHiitIntervals;
import com.saulcintero.moveon.entities.EntityLocations;
import com.saulcintero.moveon.entities.EntityRoutes;
import com.saulcintero.moveon.entities.EntityShoe;

public class DataFunctionUtils {
	private static DataManager DBManager = null;

	public static boolean checkInformationInDB(Context mContext) {
		boolean isDBEmpty = true;

		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.CustomQuery(mContext.getString(R.string.routes_counter_query),
				"SELECT _id FROM routes");
		cursor.moveToFirst();
		if (cursor.getCount() > 0)
			isDBEmpty = false;
		cursor.close();
		DBManager.Close();

		return isDBEmpty;
	}

	public static int getLastRoute(Context mContext) {
		int rId = 0;
		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.getLastIdFromTable("routes");
		cursor.moveToFirst();
		if (cursor.getCount() > 0)
			rId = cursor.getInt(cursor.getColumnIndex("_id"));
		cursor.close();
		DBManager.Close();

		return rId;
	}

	public static int getLastHIIT(Context mContext) {
		int rId = 0;
		DBManager = new DataManager(mContext);
		DBManager.Open();

		Cursor cursor = DBManager.getLastIdFromTable("hiit");
		cursor.moveToFirst();
		if (cursor.getCount() > 0)
			rId = cursor.getInt(cursor.getColumnIndex("_id"));

		cursor.close();
		DBManager.Close();

		return rId;
	}

	static public void createRouteInDB(Context mContext, ArrayList<EntityRoutes> routeList) {
		EntityRoutes mRoute = (EntityRoutes) routeList.get(0);

		DBManager = new DataManager(mContext);
		DBManager.Open();

		String[] fields = { "category_id", "shoe_id", "name", "hiit_id", "date", "hour", "time", "distance",
				"avg_speed", "max_speed", "kcal", "up_accum_altitude", "down_accum_altitude", "max_altitude",
				"min_altitude", "avg_hr", "max_hr", "steps", "avg_cadence", "max_cadence", "comments" };
		String[] values = { mRoute.getCategory_id(), mRoute.getShoe_id(), mRoute.getName(),
				mRoute.getHiit_id(), mRoute.getDate(), mRoute.getHour(), mRoute.getTime(),
				mRoute.getDistance(), mRoute.getAvg_speed(), mRoute.getMax_speed(), mRoute.getKcal(),
				mRoute.getUpAccum_altitude(), mRoute.getDown_accum_altitude(), mRoute.getMax_altitude(),
				mRoute.getMin_altitude(), mRoute.getAvg_hr(), mRoute.getMax_hr(), mRoute.getSteps(),
				mRoute.getAvg_cadence(), mRoute.getMax_cadence(), mRoute.getComments() };

		DBManager.Insert("routes", fields, values);
		DBManager.Close();
	}

	static public void createLocationsInDB(Context mContext, ArrayList<EntityLocations> locationList) {
		String _id = String.valueOf(getLastRoute(mContext));

		DBManager = new DataManager(mContext);
		DBManager.Open();
		DBManager.saveLocations(_id, locationList);
		DBManager.Close();

		locationList.clear();
	}

	static public void createHeartRateInDB(Context mContext, ArrayList<EntityHeartRate> hrList) {
		String _id = String.valueOf(getLastRoute(mContext));

		DBManager = new DataManager(mContext);
		DBManager.Open();
		DBManager.saveHeartRate(_id, hrList);
		DBManager.Close();

		hrList.clear();
	}

	static public void createCadenceInDB(Context mContext, ArrayList<EntityCadence> cadenceList) {
		String _id = String.valueOf(getLastRoute(mContext));

		DBManager = new DataManager(mContext);
		DBManager.Open();
		DBManager.saveCadence(_id, cadenceList);
		DBManager.Close();

		cadenceList.clear();
	}

	static public void createShoeInDB(Context mContext, ArrayList<EntityShoe> shoeList) {
		EntityShoe mShoe = (EntityShoe) shoeList.get(0);

		DBManager = new DataManager(mContext);
		DBManager.Open();

		if (mShoe.getDefault_shoe().equals("1"))
			DBManager.Edit("default_shoe", "0", "shoes");

		String[] fields = { "name", "distance", "default_shoe", "active" };
		String[] values = { mShoe.getName(), mShoe.getDistance(), mShoe.getDefault_shoe(), mShoe.getActive() };

		DBManager.Insert("shoes", fields, values);
		DBManager.Close();

		mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_SHOES"));
	}

	static public void createHiitInDB(Context mContext, ArrayList<EntityHiit> hiitList) {
		EntityHiit mHiit = (EntityHiit) hiitList.get(0);

		DBManager = new DataManager(mContext);
		DBManager.Open();

		String[] fields = { "name", "total_time", "rounds", "actions", "preparation_time", "cooldown_time",
				"active" };
		String[] values = { mHiit.getName(), mHiit.getTotal_time(), mHiit.getRounds(), mHiit.getActions(),
				mHiit.getPreparation_time(), mHiit.getCooldown_time(), mHiit.getActive() };

		DBManager.Insert("hiit", fields, values);
		DBManager.Close();
	}

	static public void createHiitIntervalsInDB(Context mContext,
			ArrayList<EntityHiitIntervals> hiitIntervalsList) {
		String _id = String.valueOf(getLastHIIT(mContext));

		DBManager = new DataManager(mContext);
		DBManager.Open();
		DBManager.saveHiitIntervals(_id, hiitIntervalsList);
		DBManager.Close();

		hiitIntervalsList.clear();
	}

	static public String[] getRouteData(Context mContext, int _id, boolean isMetric) {
		String[] data = new String[25];
		int shoe_id = 0;

		DBManager = new DataManager(mContext);
		DBManager.Open();

		Cursor cursor = DBManager.getRowsFromTable(String.valueOf(_id), "routes");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			data[0] = String.valueOf(cursor.getInt(cursor.getColumnIndex("category_id")));
			data[1] = cursor.getString(cursor.getColumnIndex("name"));
			data[2] = FunctionUtils.longFormatTime(Long.parseLong(String.valueOf(cursor.getInt(cursor
					.getColumnIndex("time")))));
			data[3] = String.valueOf(isMetric ? cursor.getFloat(cursor.getColumnIndex("distance")) : (cursor
					.getFloat(cursor.getColumnIndex("distance")) * 1000f) / 1609f);
			data[4] = String.valueOf(isMetric ? cursor.getFloat(cursor.getColumnIndex("avg_speed")) : (cursor
					.getFloat(cursor.getColumnIndex("avg_speed")) * 1000f) / 1609f);
			data[6] = String.valueOf(isMetric ? cursor.getFloat(cursor.getColumnIndex("max_speed"))
					: FunctionUtils.customizedRound(
							(cursor.getFloat(cursor.getColumnIndex("max_speed")) / 1.609f), 2));
			data[7] = String.valueOf(isMetric ? cursor.getInt(cursor.getColumnIndex("max_altitude"))
					: (cursor.getInt(cursor.getColumnIndex("max_altitude")) * 1.0936f));
			data[8] = String.valueOf(isMetric ? cursor.getInt(cursor.getColumnIndex("min_altitude"))
					: (int) (cursor.getInt(cursor.getColumnIndex("min_altitude")) * 1.0936f));
			data[20] = String.valueOf(isMetric ? cursor.getInt(cursor.getColumnIndex("up_accum_altitude"))
					: (int) (cursor.getInt(cursor.getColumnIndex("up_accum_altitude")) * 1.0936f));
			data[21] = String.valueOf(isMetric ? cursor.getInt(cursor.getColumnIndex("down_accum_altitude"))
					: (int) (cursor.getInt(cursor.getColumnIndex("down_accum_altitude")) * 1.0936f));
			data[5] = FunctionUtils.calculateRitm(mContext,
					Long.parseLong(String.valueOf(cursor.getInt(cursor.getColumnIndex("time")))),
					String.valueOf(cursor.getFloat(cursor.getColumnIndex("distance"))), isMetric, false);
			data[9] = String.valueOf(cursor.getInt(cursor.getColumnIndex("kcal")));
			data[10] = String.valueOf(cursor.getInt(cursor.getColumnIndex("steps")));
			data[11] = cursor.getString(cursor.getColumnIndex("comments"));
			shoe_id = cursor.getInt(cursor.getColumnIndex("shoe_id"));
			data[13] = String.valueOf(cursor.getInt(cursor.getColumnIndex("avg_hr")));
			data[14] = String.valueOf(cursor.getInt(cursor.getColumnIndex("max_hr")));
			data[15] = String.valueOf(shoe_id);
			data[16] = String.valueOf(cursor.getInt(cursor.getColumnIndex("category_id")));
			data[17] = String.valueOf(cursor.getInt(cursor.getColumnIndex("time")));
			data[18] = cursor.getString(cursor.getColumnIndex("date"));
			data[19] = cursor.getString(cursor.getColumnIndex("hour"));
			data[22] = String.valueOf(cursor.getInt(cursor.getColumnIndex("hiit_id")));
			data[23] = String.valueOf(cursor.getInt(cursor.getColumnIndex("avg_cadence")));
			data[24] = String.valueOf(cursor.getInt(cursor.getColumnIndex("max_cadence")));
			data[12] = "";
			if (shoe_id > 0) {
				cursor = DBManager.CustomQuery(mContext.getString(R.string.getting_shoe_with_id) + " '"
						+ shoe_id + "' " + mContext.getString(R.string.from_shoes_table),
						"SELECT * FROM shoes WHERE _id=" + shoe_id);
				cursor.moveToFirst();
				data[12] = cursor.getString(cursor.getColumnIndex("name"));
			}
		}

		cursor.close();
		DBManager.Close();

		return data;
	}

	static public String[] getShoeData(Context mContext, int _id, boolean isMetric) {
		String[] data = new String[4];

		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.getRowsFromTable(String.valueOf(_id), "shoes");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			data[0] = cursor.getString(cursor.getColumnIndex("name"));
			data[1] = String.valueOf(isMetric ? cursor.getFloat(cursor.getColumnIndex("distance")) : (cursor
					.getFloat(cursor.getColumnIndex("distance")) * 1000f) / 1609f);
			data[2] = String.valueOf(cursor.getInt(cursor.getColumnIndex("default_shoe")));
			data[3] = String.valueOf(cursor.getInt(cursor.getColumnIndex("active")));
		}
		cursor.close();
		DBManager.Close();

		return data;
	}

	static public String[] getHiitData(Context mContext, int _id) {
		String[] data = new String[6];

		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.getRowsFromTable(String.valueOf(_id), "hiit");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			data[0] = String.valueOf(cursor.getInt(cursor.getColumnIndex("total_time")));
			data[1] = String.valueOf(cursor.getInt(cursor.getColumnIndex("rounds")));
			data[2] = String.valueOf(cursor.getInt(cursor.getColumnIndex("preparation_time")));
			data[3] = String.valueOf(cursor.getInt(cursor.getColumnIndex("cooldown_time")));
			data[4] = String.valueOf(cursor.getInt(cursor.getColumnIndex("actions")));
			data[5] = cursor.getString(cursor.getColumnIndex("name"));
		}
		cursor.close();
		DBManager.Close();

		return data;
	}

	static public ArrayList<int[]> getHiitIntervalsData(Context mContext, int _id) {
		ArrayList<int[]> data = new ArrayList<int[]>();

		DBManager = new DataManager(mContext);
		DBManager.Open();
		Cursor cursor = DBManager.getRowsFromTable(String.valueOf(_id), "hiit_intervals");
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (!cursor.isAfterLast()) {
				int[] fields = new int[2];

				fields[0] = cursor.getInt(cursor.getColumnIndex("type"));
				fields[1] = cursor.getInt(cursor.getColumnIndex("time"));
				data.add(fields);

				cursor.moveToNext();
			}
		}
		cursor.close();
		DBManager.Close();

		return data;
	}

	public static void modifyShoeInDB(Context mContext, ArrayList<EntityRoutes> routeList) {
		EntityRoutes mRoute = (EntityRoutes) routeList.get(0);
		int id = Integer.parseInt(mRoute.getShoe_id());

		DBManager = new DataManager(mContext);
		DBManager.Open();

		Cursor cursor = DBManager
				.CustomQuery(
						mContext.getString(R.string.getting_shoe_with_id) + " '" + id + "' "
								+ mContext.getString(R.string.from_shoes_table),
						"SELECT * FROM shoes WHERE _id=" + id);
		cursor.moveToFirst();
		if (cursor.getCount() > 0)
			DBManager.Edit(
					id,
					"distance",
					String.valueOf(cursor.getFloat(cursor.getColumnIndex("distance"))
							+ Float.parseFloat(mRoute.getDistance())), "shoes");
		cursor.close();

		DBManager.Close();
	}

	public static void modifyHiitInDB(Context mContext, int id, ArrayList<EntityHiit> hiitList) {
		EntityHiit mHiit = (EntityHiit) hiitList.get(0);

		DBManager = new DataManager(mContext);
		DBManager.Open();

		String[] fields = { "name", "total_time", "rounds", "actions", "preparation_time", "cooldown_time",
				"active" };
		String[] values = { mHiit.getName(), mHiit.getTotal_time(), mHiit.getRounds(), mHiit.getActions(),
				mHiit.getPreparation_time(), mHiit.getCooldown_time(), mHiit.getActive() };

		for (int p = 0; p < fields.length; p++) {
			DBManager.Edit(id, fields[p], values[p], "hiit");
		}

		DBManager.Close();
	}

	static public void modifyHiitIntervalsInDB(Context mContext, int id,
			ArrayList<EntityHiitIntervals> hiitIntervalsList) {
		DBManager = new DataManager(mContext);
		DBManager.Open();
		DBManager.Delete(id, "hiit_intervals");
		DBManager.saveHiitIntervals(String.valueOf(id), hiitIntervalsList);
		DBManager.Close();

		hiitIntervalsList.clear();
	}
}