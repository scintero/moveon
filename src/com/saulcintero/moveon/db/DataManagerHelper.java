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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.saulcintero.moveon.R;

public class DataManagerHelper extends SQLiteOpenHelper {
	private static final String TAG = DataManagerHelper.class.getSimpleName();

	private Context mContext;

	private static final String DATABASE_NAME = "moveon.db";
	private static final int DATABASE_VERSION = 30;

	private static final String ROUTES_TABLE = "routes";
	private static final String LOCATIONS_TABLE = "locations";

	private String CREATETABLE_ROUTES = "create table if not exists routes (_id integer primary key autoincrement, "
			+ "category_id integer not null, shoe_id references shoes(_id), "
			+ "name text not null, hiit_id references hiit(_id), "
			+ "date text not null, hour text not null, "
			+ "time integer not null, distance float not null, "
			+ "avg_speed float not null, max_speed float not null, "
			+ "max_altitude integer not null, min_altitude integer not null, "
			+ "up_accum_altitude integer not null, down_accum_altitude integer not null, "
			+ "avg_hr integer not null, max_hr integer not null, "
			+ "kcal integer not null, steps integer not null, "
			+ "avg_cadence integer not null, max_cadence integer not null, " + "comments text);";

	private String CREATETABLE_LOCATIONS = "create table if not exists locations (_id integer references routes(_id), "
			+ "latitude float not null, longitude float not null, "
			+ "altitude float not null, distance float not null, "
			+ "speed float not null, time integer not null, "
			+ "steps integer not null, hr integer default 0 not null, "
			+ "cadence integer default 0 not null, pause bool default 1 not null);";

	private String CREATETABLE_HR = "create table if not exists hr (_id integer references routes(_id), "
			+ "time integer not null, hr integer default 0 not null);";

	private String CREATETABLE_CADENCE = "create table if not exists cadence (_id integer references routes(_id), "
			+ "time integer not null, cadence integer default 0 not null);";

	private String CREATETABLE_SHOES = "create table if not exists shoes (_id integer primary key autoincrement, "
			+ "name text not null, distance float default 0 not null, "
			+ "default_shoe bool default 0 not null, active bool default 1 not null););";

	private String CREATETABLE_HIIT = "create table if not exists hiit (_id integer primary key autoincrement, "
			+ "name text not null, total_time integer not null, "
			+ "rounds integer default 1 not null, actions integer not null, "
			+ "preparation_time integer default 0 not null, cooldown_time integer default 0 not null, "
			+ "active bool default 1 not null);";

	// type: 1 = rest; 2 = low intensity; 3 = medium intensity; 4 = high
	// intensity;
	private String CREATETABLE_HIIT_INTERVALS = "create table if not exists hiit_intervals (_id integer references hiit(_id), "
			+ "type integer not null, time integer not null);";

	public DataManagerHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, mContext.getString(R.string.db_create_tables));

		if (!db.isReadOnly())
			db.execSQL("PRAGMA foreign_keys=ON;"); // Enable foreign key
													// constraints

		db.execSQL(CREATETABLE_ROUTES);
		db.execSQL(CREATETABLE_LOCATIONS);
		db.execSQL(CREATETABLE_HR);
		db.execSQL(CREATETABLE_CADENCE);
		db.execSQL(CREATETABLE_SHOES);
		db.execSQL(CREATETABLE_HIIT);
		db.execSQL(CREATETABLE_HIIT_INTERVALS);

		// The three default interval training
		db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
				+ "preparation_time, cooldown_time) " + "VALUES ('"
				+ mContext.getString(R.string.standard_interval_training) + "', " + "1260, 1, 13, 0, 0);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 300);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 300);");

		db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
				+ "preparation_time, cooldown_time) " + "VALUES ('"
				+ mContext.getString(R.string.tabata_interval_training) + "', " + "240, 8, 2, 0, 0);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (2, 3, 20);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (2, 0, 10);");

		db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
				+ "preparation_time, cooldown_time) " + "VALUES ('"
				+ mContext.getString(R.string.pyramid_interval_training) + "', " + "1080, 1, 10, 0, 0);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 120);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 120);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 180);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 180);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 120);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 120);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 60);");
		db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 60);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, mContext.getString(R.string.db_updating_from_version) + " '" + oldVersion + "' "
				+ mContext.getString(R.string.db_updating_to_version) + " '" + newVersion + "'");

		if (oldVersion < 2) {
			final String ALTER_TBL = "ALTER TABLE " + ROUTES_TABLE
					+ " ADD COLUMN avg_hr integer default 0 not null;";
			final String ALTER_TBL2 = "ALTER TABLE " + ROUTES_TABLE
					+ " ADD COLUMN max_hr integer default 0 not null;";
			final String ALTER_TBL3 = "ALTER TABLE " + LOCATIONS_TABLE
					+ " ADD COLUMN hr integer default 0 not null;";

			db.execSQL(ALTER_TBL);
			db.execSQL(ALTER_TBL2);
			db.execSQL(ALTER_TBL3);
		} else if (oldVersion < 3) {
			db.execSQL(CREATETABLE_HR);
		} else if (oldVersion < 4) {
			final String ALTER_TBL = "ALTER TABLE " + ROUTES_TABLE + " RENAME TO " + ROUTES_TABLE + "_TMP";
			final String INSERT_INTO_TBL = "INSERT INTO " + ROUTES_TABLE
					+ " (_id, category_id, date, hour, time, distance, "
					+ "max_speed, kcal, max_altitude, min_altitude, avg_hr, max_hr, " + "steps, comments) "
					+ "SELECT _id, category_id, date, hour, time, distance, "
					+ "maxspeed, kcal, max_altitude, min_altitude, avg_fc, max_fc, " + "steps, comments "
					+ "FROM " + ROUTES_TABLE + "_TMP;";
			final String DROP_TBL = "DROP TABLE " + ROUTES_TABLE + "_TMP";

			db.execSQL(ALTER_TBL);
			db.execSQL(CREATETABLE_ROUTES);
			db.execSQL(INSERT_INTO_TBL);
			db.execSQL(DROP_TBL);

			final String ALTER_TBL2 = "ALTER TABLE " + LOCATIONS_TABLE + " RENAME TO " + LOCATIONS_TABLE
					+ "_TMP";
			final String INSERT_INTO_TBL2 = "INSERT INTO " + LOCATIONS_TABLE
					+ " (_id, latitude, longitude, altitude, distance, speed, " + "time, steps, hr, pause) "
					+ "SELECT _id, latitude, longitude, altitude, distance, speed, "
					+ "time, steps, fc, pause " + "FROM " + LOCATIONS_TABLE + "_TMP;";
			final String DROP_TBL2 = "DROP TABLE " + LOCATIONS_TABLE + "_TMP";

			db.execSQL(ALTER_TBL2);
			db.execSQL(CREATETABLE_LOCATIONS);
			db.execSQL(INSERT_INTO_TBL2);
			db.execSQL(DROP_TBL2);
		} else if (oldVersion < 5) {
			db.execSQL(CREATETABLE_HR);
		} else if (oldVersion < 6) {
			db.execSQL(CREATETABLE_SHOES);

			final String ALTER_TBL = "ALTER TABLE " + ROUTES_TABLE + " RENAME TO " + ROUTES_TABLE + "_TMP";
			final String INSERT_INTO_TBL = "INSERT INTO " + ROUTES_TABLE
					+ " (_id, category_id, date, hour, time, distance, "
					+ "max_speed, kcal, max_altitude, min_altitude, up_accum_altitude, down_accum_altitude "
					+ "avg_hr, max_hr, steps, comments) "
					+ "SELECT _id, category_id, date, hour, time, distance, "
					+ "max_speed, kcal, max_altitude, min_altitude, 0, 0, "
					+ "avg_hr, max_hr, steps, comments " + "FROM " + ROUTES_TABLE + "_TMP;";
			final String DROP_TBL = "DROP TABLE " + ROUTES_TABLE + "_TMP";

			db.execSQL(ALTER_TBL);
			db.execSQL(CREATETABLE_ROUTES);
			db.execSQL(INSERT_INTO_TBL);
			db.execSQL(DROP_TBL);
		} else if (oldVersion < 7) {
			db.execSQL(CREATETABLE_SHOES);
		} else if (oldVersion < 8) {
			db.execSQL("DROP TABLE shoes");
			db.execSQL(CREATETABLE_SHOES);
		} else if (oldVersion < 10) {
			final String ALTER_TBL = "ALTER TABLE " + ROUTES_TABLE + " RENAME TO " + ROUTES_TABLE + "_TMP";
			final String INSERT_INTO_TBL = "INSERT INTO " + ROUTES_TABLE
					+ " (_id, category_id, shoe_id, name, date, hour, time, distance, "
					+ "max_speed, kcal, max_altitude, min_altitude, avg_hr, max_hr, " + "steps, comments) "
					+ "SELECT _id, category_id, shoe_id, date + ' ' + hour, date, hour, time, distance, "
					+ "max_speed, kcal, max_altitude, min_altitude, avg_hr, max_hr, " + "steps, comments "
					+ "FROM " + ROUTES_TABLE + "_TMP;";
			final String DROP_TBL = "DROP TABLE " + ROUTES_TABLE + "_TMP";

			db.execSQL(ALTER_TBL);
			db.execSQL(CREATETABLE_ROUTES);
			db.execSQL(INSERT_INTO_TBL);
			db.execSQL(DROP_TBL);
		} else if (oldVersion < 13) {
			final String UPD_TBL = "UPDATE " + ROUTES_TABLE + " SET name = (\"date\" || \", \" || \"hour\")";

			db.execSQL(UPD_TBL);
		} else if (oldVersion < 18) {
			final String ALTER_TBL = "ALTER TABLE " + ROUTES_TABLE + " RENAME TO " + ROUTES_TABLE + "_TMP";
			final String INSERT_INTO_TBL = "INSERT INTO " + ROUTES_TABLE
					+ " (_id, category_id, shoe_id, name, date, hour, time, distance, "
					+ "avg_speed, max_speed, kcal, max_altitude, min_altitude, "
					+ "up_accum_altitude, down_accum_altitude, avg_hr, max_hr, " + "steps, comments) "
					+ "SELECT _id, category_id, shoe_id, name, date, hour, time, distance, "
					+ "0, max_speed, kcal, max_altitude, min_altitude, " + "0, 0, avg_hr, max_hr, "
					+ "steps, comments " + "FROM " + ROUTES_TABLE + "_TMP;";
			final String DROP_TBL = "DROP TABLE " + ROUTES_TABLE + "_TMP";

			db.execSQL(ALTER_TBL);
			db.execSQL(CREATETABLE_ROUTES);
			db.execSQL(INSERT_INTO_TBL);
			db.execSQL(DROP_TBL);
		} else if (oldVersion < 25) {
			db.execSQL("DROP TABLE IF EXISTS hiit");
			db.execSQL("DROP TABLE IF EXISTS hiit_intervals");

			db.execSQL(CREATETABLE_HIIT);
			db.execSQL(CREATETABLE_HIIT_INTERVALS);

			db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
					+ "preparation_time, cooldown_time) " + "VALUES ('"
					+ mContext.getString(R.string.standard_interval_training) + "', " + "1260, 1, 13, 0, 0);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 300);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 1, 300);");

			db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
					+ "preparation_time, cooldown_time) " + "VALUES ('"
					+ mContext.getString(R.string.tabata_interval_training) + "', " + "240, 8, 2, 0, 0);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (2, 3, 20);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (2, 0, 10);");

			db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
					+ "preparation_time, cooldown_time) " + "VALUES ('"
					+ mContext.getString(R.string.pyramid_interval_training) + "', " + "1080, 1, 10, 0, 0);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 180);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 180);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 1, 60);");
		} else if (oldVersion < 27) {
			final String ALTER_TBL_1 = "ALTER TABLE " + ROUTES_TABLE + " RENAME TO " + ROUTES_TABLE + "_TMP";
			final String ALTER_TBL_2 = "ALTER TABLE hiit RENAME TO hiit_TMP";
			final String INSERT_INTO_TBL_1 = "INSERT INTO " + ROUTES_TABLE
					+ " (_id, category_id, shoe_id, name, hiit_id, date, hour, time, "
					+ "distance, avg_speed, max_speed, kcal, max_altitude, min_altitude, "
					+ "up_accum_altitude, down_accum_altitude, avg_hr, max_hr, " + "steps, comments) "
					+ "SELECT _id, category_id, shoe_id, name, 0, date, hour, time, "
					+ "distance, avg_speed, max_speed, kcal, max_altitude, min_altitude, "
					+ "up_accum_altitude, down_accum_altitude, avg_hr, max_hr, " + "steps, comments "
					+ "FROM " + ROUTES_TABLE + "_TMP;";
			final String INSERT_INTO_TBL_2 = "INSERT INTO hiit "
					+ "(_id, name, total_time, rounds, actions, preparation_time, cooldown_time, active) "
					+ "SELECT _id, name, total_time, rounds, actions, preparation_time, cooldown_time, 1 "
					+ "FROM hiit_TMP;";
			final String DROP_TBL_1 = "DROP TABLE " + ROUTES_TABLE + "_TMP";
			final String DROP_TBL_2 = "DROP TABLE hiit_TMP";

			db.execSQL(ALTER_TBL_1);
			db.execSQL(ALTER_TBL_2);
			db.execSQL(CREATETABLE_ROUTES);
			db.execSQL(CREATETABLE_HIIT);
			db.execSQL(INSERT_INTO_TBL_1);
			db.execSQL(INSERT_INTO_TBL_2);
			db.execSQL(DROP_TBL_1);
			db.execSQL(DROP_TBL_2);
		} else if (oldVersion < 28) {
			db.execSQL("DROP TABLE IF EXISTS hiit");
			db.execSQL("DROP TABLE IF EXISTS hiit_intervals");

			db.execSQL(CREATETABLE_HIIT);
			db.execSQL(CREATETABLE_HIIT_INTERVALS);

			db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
					+ "preparation_time, cooldown_time) " + "VALUES ('"
					+ mContext.getString(R.string.standard_interval_training) + "', " + "1260, 1, 13, 0, 0);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 300);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 3, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (1, 2, 300);");

			db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
					+ "preparation_time, cooldown_time) " + "VALUES ('"
					+ mContext.getString(R.string.tabata_interval_training) + "', " + "240, 8, 2, 0, 0);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (2, 4, 20);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (2, 1, 10);");

			db.execSQL("INSERT INTO hiit (name, total_time, rounds, actions, "
					+ "preparation_time, cooldown_time) " + "VALUES ('"
					+ mContext.getString(R.string.pyramid_interval_training) + "', " + "1080, 1, 10, 0, 0);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 4, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 2, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 4, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 2, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 4, 180);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 2, 180);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 4, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 2, 120);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 4, 60);");
			db.execSQL("INSERT INTO hiit_intervals (_id, type, time) VALUES (3, 2, 60);");
		} else if (oldVersion < 29) {
			final String ALTER_TBL = "ALTER TABLE " + ROUTES_TABLE + " RENAME TO " + ROUTES_TABLE + "_TMP";
			final String INSERT_INTO_TBL = "INSERT INTO " + ROUTES_TABLE
					+ " (_id, category_id, shoe_id, name, date, hour, time, distance, "
					+ "avg_speed, max_speed, kcal, max_altitude, min_altitude, "
					+ "up_accum_altitude, down_accum_altitude, avg_hr, max_hr, "
					+ "steps, avg_cadence, max_cadence, comments) "
					+ "SELECT _id, category_id, shoe_id, name, date, hour, time, distance, "
					+ "0, max_speed, kcal, max_altitude, min_altitude, " + "0, 0, avg_hr, max_hr, "
					+ "steps, 0, 0, comments " + "FROM " + ROUTES_TABLE + "_TMP;";
			final String DROP_TBL = "DROP TABLE " + ROUTES_TABLE + "_TMP";

			final String ALTER_TBL2 = "ALTER TABLE " + LOCATIONS_TABLE + " RENAME TO " + LOCATIONS_TABLE
					+ "_TMP";
			final String INSERT_INTO_TBL2 = "INSERT INTO " + LOCATIONS_TABLE
					+ " (_id, latitude, longitude, altitude, distance, speed, "
					+ "time, steps, hr, cadence, pause) "
					+ "SELECT _id, latitude, longitude, altitude, distance, speed, "
					+ "time, steps, hr, 0, pause " + "FROM " + LOCATIONS_TABLE + "_TMP;";
			final String DROP_TBL2 = "DROP TABLE " + LOCATIONS_TABLE + "_TMP";

			db.execSQL(ALTER_TBL);
			db.execSQL(ALTER_TBL2);
			db.execSQL(CREATETABLE_ROUTES);
			db.execSQL(CREATETABLE_LOCATIONS);
			db.execSQL(CREATETABLE_CADENCE);
			db.execSQL(INSERT_INTO_TBL);
			db.execSQL(INSERT_INTO_TBL2);
			db.execSQL(DROP_TBL);
			db.execSQL(DROP_TBL2);
		} else if (oldVersion < 30) {
			db.execSQL("DROP TABLE IF EXISTS fc");
		}
	}
}