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

package com.saulcintero.moveon.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.entities.EntityCadence;
import com.saulcintero.moveon.entities.EntityHeartRate;
import com.saulcintero.moveon.entities.EntityHiitIntervals;
import com.saulcintero.moveon.entities.EntityLocations;

public class DataManager {
	private static final String TAG = DataManager.class.getSimpleName();

	private Context mContext;

	private DataManagerHelper mHelper = null;
	private SQLiteDatabase db = null;

	public DataManager(final Context context) {
		mContext = context;

		this.mHelper = new DataManagerHelper(context);
	}

	public DataManager Open() {
		Log.i(TAG, mContext.getString(R.string.db_open));

		db = this.mHelper.getWritableDatabase();
		return this;
	}

	public void Close() {
		Log.i(TAG, mContext.getString(R.string.db_close));

		if (db != null) {
			db.close();
		}
	}

	public void Insert(String DATA_MANAGER_TABLENAME, String field, String value) {
		try {
			db.beginTransaction();

			Log.i(TAG, "INSERT INTO " + DATA_MANAGER_TABLENAME + "(" + field + ") VALUES (" + value + ")");

			SQLiteStatement stmt = db.compileStatement("INSERT INTO " + DATA_MANAGER_TABLENAME + "(" + field
					+ ") VALUES (" + value + ")");

			stmt.execute();
			stmt.clearBindings();

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, mContext.getString(R.string.db_transaction_rolled_back), e);
		} finally {
			db.endTransaction();
		}
	}

	public void Insert(String DATA_MANAGER_TABLENAME, String[] fields, String[] values) {
		try {
			db.beginTransaction();

			String stmt_fields = "(", stmt_values = "(";

			for (int i = 0; i <= fields.length - 1; i++) {
				if (stmt_fields.length() > 1 && (i <= (fields.length - 1)))
					stmt_fields = stmt_fields + ",";

				stmt_fields = stmt_fields + fields[i];

				if (i == (fields.length - 1))
					stmt_fields = stmt_fields + ")";

				if ((stmt_values.length() > 1) && (i <= (fields.length - 1)))
					stmt_values = stmt_values + ",";

				stmt_values = stmt_values + "'" + values[i] + "'";

				if (i == (fields.length - 1))
					stmt_values = stmt_values + ")";
			}

			Log.i(TAG, "INSERT INTO " + DATA_MANAGER_TABLENAME + stmt_fields + " VALUES " + stmt_values);

			SQLiteStatement stmt = db.compileStatement("INSERT INTO " + DATA_MANAGER_TABLENAME + stmt_fields
					+ " VALUES " + stmt_values);

			stmt.execute();
			stmt.clearBindings();

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, mContext.getString(R.string.db_transaction_rolled_back), e);
		} finally {
			db.endTransaction();
		}
	}

	public void Edit(int rowId, String field, String value, String DATA_MANAGER_TABLENAME) {
		String DATA_MANAGER_EDIT = "UPDATE " + DATA_MANAGER_TABLENAME + " SET " + field + " = '" + value
				+ "'" + " WHERE _id = '" + rowId + "'";
		Log.i(TAG, DATA_MANAGER_EDIT);

		db.execSQL(DATA_MANAGER_EDIT);
	}

	public void Edit(String field, String value, String DATA_MANAGER_TABLENAME) {
		String DATA_MANAGER_EDIT = "UPDATE " + DATA_MANAGER_TABLENAME + " SET " + field + " = '" + value
				+ "'";
		Log.i(TAG, DATA_MANAGER_EDIT);

		db.execSQL(DATA_MANAGER_EDIT);
	}

	public int Delete(int rowId, String DATA_MANAGER_TABLENAME) {
		Log.i(TAG, mContext.getString(R.string.db_deleting_register) + " '" + rowId + "'");
		String whereClause = "_id=" + rowId;
		String[] whereArgs = null;
		return db.delete(DATA_MANAGER_TABLENAME, whereClause, whereArgs);
	}

	public Cursor getAll(String DATA_MANAGER_TABLENAME) {
		String DATA_MANAGER_GETALL = "SELECT * FROM " + DATA_MANAGER_TABLENAME;
		Log.i(TAG, "SELECT * FROM " + DATA_MANAGER_TABLENAME);

		return db.rawQuery(DATA_MANAGER_GETALL, null);
	}

	public Cursor getAll_DESC(String DATA_MANAGER_TABLENAME) {
		String DATA_MANAGER_GETALL = "SELECT *, ((substr(date,7,4)||substr(date,4,2)||substr(date,1,2))) AS iso_date"
				+ " FROM " + DATA_MANAGER_TABLENAME + " ORDER BY iso_date DESC";
		Log.i(TAG, DATA_MANAGER_GETALL);

		return db.rawQuery(DATA_MANAGER_GETALL, null);
	}

	public Cursor getLastIdFromTable(String DATA_MANAGER_TABLENAME) {
		String DATA_MANAGER_LASTID = "SELECT _id FROM " + DATA_MANAGER_TABLENAME + " ORDER BY _id DESC"
				+ " LIMIT 1";
		Log.i(TAG, DATA_MANAGER_LASTID);

		return db.rawQuery(DATA_MANAGER_LASTID, null);
	}

	public Cursor getRowsFromTable(String _id, String DATA_MANAGER_TABLENAME) {
		String DATA_MANAGER_SELECTEDROW = "SELECT * FROM " + DATA_MANAGER_TABLENAME + " WHERE _id = '" + _id
				+ "'";
		Log.i(TAG, DATA_MANAGER_SELECTEDROW);

		return db.rawQuery(DATA_MANAGER_SELECTEDROW, null);
	}

	public Cursor CountRowsFromTable(String _id, String DATA_MANAGER_TABLENAME) {
		String DATA_MANAGER_COUNTROWS = "SELECT COUNT (*) AS coordinates_route FROM "
				+ DATA_MANAGER_TABLENAME + " WHERE _id = '" + _id + "'";
		Log.i(TAG, DATA_MANAGER_COUNTROWS);

		return db.rawQuery(DATA_MANAGER_COUNTROWS, null);
	}

	public Cursor CustomQuery(String description, String query) {
		Log.i(TAG, description);

		return db.rawQuery(query, null);
	}

	public void saveLocations(String _id, ArrayList<EntityLocations> locationList) {
		try {
			db.beginTransaction();

			String[] fields = { "_id", "latitude", "longitude", "altitude", "distance", "speed", "time",
					"pause", "steps", "hr", "cadence" };

			String stmt_fields = "", stmt_values = "";

			for (int g = 0; g <= locationList.size() - 1; g++) {
				EntityLocations mLocation = (EntityLocations) locationList.get(g);
				String[] values = { _id, mLocation.getLatitude(), mLocation.getLongitude(),
						mLocation.getAltitude(), mLocation.getDistance(), mLocation.getSpeed(),
						mLocation.getTime(), mLocation.getPause(), mLocation.getSteps(), mLocation.getHr(),
						mLocation.getCadence() };

				stmt_fields = "(";
				stmt_values = "(";

				for (int i = 0; i <= fields.length - 1; i++) {
					if (stmt_fields.length() > 1 && (i <= (fields.length - 1)))
						stmt_fields = stmt_fields + ",";

					stmt_fields = stmt_fields + fields[i];

					if (i == (fields.length - 1))
						stmt_fields = stmt_fields + ")";

					if ((stmt_values.length() > 1) && (i <= (fields.length - 1)))
						stmt_values = stmt_values + ",";

					stmt_values = stmt_values + "'" + values[i] + "'";

					if (i == (fields.length - 1))
						stmt_values = stmt_values + ")";
				}

				Log.i(TAG, "INSERT INTO locations " + stmt_fields + " VALUES " + stmt_values);

				SQLiteStatement stmt = db.compileStatement("INSERT INTO locations " + stmt_fields
						+ " VALUES " + stmt_values);

				stmt.execute();
				stmt.clearBindings();
			}

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, mContext.getString(R.string.db_transaction_rolled_back), e);
		} finally {
			db.endTransaction();
		}
	}

	public void saveHeartRate(String _id, ArrayList<EntityHeartRate> hrList) {
		try {
			db.beginTransaction();

			String[] fields = { "_id", "time", "hr" };

			String stmt_fields = "", stmt_values = "";

			for (int g = 0; g <= hrList.size() - 1; g++) {
				EntityHeartRate mHr = (EntityHeartRate) hrList.get(g);
				String[] values = { _id, mHr.getTime(), mHr.getHr() };

				stmt_fields = "(";
				stmt_values = "(";

				for (int i = 0; i <= fields.length - 1; i++) {
					if (stmt_fields.length() > 1 && (i <= (fields.length - 1)))
						stmt_fields = stmt_fields + ",";

					stmt_fields = stmt_fields + fields[i];

					if (i == (fields.length - 1))
						stmt_fields = stmt_fields + ")";

					if ((stmt_values.length() > 1) && (i <= (fields.length - 1)))
						stmt_values = stmt_values + ",";

					stmt_values = stmt_values + "'" + values[i] + "'";

					if (i == (fields.length - 1))
						stmt_values = stmt_values + ")";
				}

				Log.i(TAG, "INSERT INTO hr " + stmt_fields + " VALUES " + stmt_values);

				SQLiteStatement stmt = db.compileStatement("INSERT INTO hr " + stmt_fields + " VALUES "
						+ stmt_values);

				stmt.execute();
				stmt.clearBindings();
			}

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, mContext.getString(R.string.db_transaction_rolled_back), e);
		} finally {
			db.endTransaction();
		}
	}

	public void saveCadence(String _id, ArrayList<EntityCadence> cadenceList) {
		try {
			db.beginTransaction();

			String[] fields = { "_id", "time", "cadence" };

			String stmt_fields = "", stmt_values = "";

			for (int g = 0; g <= cadenceList.size() - 1; g++) {
				EntityCadence mCadence = (EntityCadence) cadenceList.get(g);
				String[] values = { _id, mCadence.getTime(), mCadence.getCadence() };

				stmt_fields = "(";
				stmt_values = "(";

				for (int i = 0; i <= fields.length - 1; i++) {
					if (stmt_fields.length() > 1 && (i <= (fields.length - 1)))
						stmt_fields = stmt_fields + ",";

					stmt_fields = stmt_fields + fields[i];

					if (i == (fields.length - 1))
						stmt_fields = stmt_fields + ")";

					if ((stmt_values.length() > 1) && (i <= (fields.length - 1)))
						stmt_values = stmt_values + ",";

					stmt_values = stmt_values + "'" + values[i] + "'";

					if (i == (fields.length - 1))
						stmt_values = stmt_values + ")";
				}

				Log.i(TAG, "INSERT INTO cadence " + stmt_fields + " VALUES " + stmt_values);

				SQLiteStatement stmt = db.compileStatement("INSERT INTO cadence " + stmt_fields + " VALUES "
						+ stmt_values);

				stmt.execute();
				stmt.clearBindings();
			}

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, mContext.getString(R.string.db_transaction_rolled_back), e);
		} finally {
			db.endTransaction();
		}
	}

	public void saveHiitIntervals(String _id, ArrayList<EntityHiitIntervals> hiitIntervalsList) {
		try {
			db.beginTransaction();

			String[] fields = { "_id", "type", "time" };

			String stmt_fields = "", stmt_values = "";

			for (int g = 0; g <= hiitIntervalsList.size() - 1; g++) {
				EntityHiitIntervals mHiitIntervals = (EntityHiitIntervals) hiitIntervalsList.get(g);
				String[] values = { _id, mHiitIntervals.getType(), mHiitIntervals.getTime() };

				stmt_fields = "(";
				stmt_values = "(";

				for (int i = 0; i <= fields.length - 1; i++) {
					if (stmt_fields.length() > 1 && (i <= (fields.length - 1)))
						stmt_fields = stmt_fields + ",";

					stmt_fields = stmt_fields + fields[i];

					if (i == (fields.length - 1))
						stmt_fields = stmt_fields + ")";

					if ((stmt_values.length() > 1) && (i <= (fields.length - 1)))
						stmt_values = stmt_values + ",";

					stmt_values = stmt_values + "'" + values[i] + "'";

					if (i == (fields.length - 1))
						stmt_values = stmt_values + ")";
				}

				Log.i(TAG, "INSERT INTO hiit_intervals " + stmt_fields + " VALUES " + stmt_values);

				SQLiteStatement stmt = db.compileStatement("INSERT INTO hiit_intervals " + stmt_fields
						+ " VALUES " + stmt_values);

				stmt.execute();
				stmt.clearBindings();
			}

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, mContext.getString(R.string.db_transaction_rolled_back), e);
		} finally {
			db.endTransaction();
		}
	}
}