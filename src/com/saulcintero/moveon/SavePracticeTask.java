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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.saulcintero.moveon.entities.EntityCadence;
import com.saulcintero.moveon.entities.EntityHeartRate;
import com.saulcintero.moveon.entities.EntityLocations;
import com.saulcintero.moveon.entities.EntityRoutes;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class SavePracticeTask extends AsyncTask<Void, Void, Void> {
	private ProgressDialog progress;
	private Context mContext;
	private ArrayList<EntityRoutes> routeList;
	private ArrayList<EntityLocations> locationsList;
	private ArrayList<EntityHeartRate> hrList;
	private ArrayList<EntityCadence> cadenceList;
	private Activity act;

	public SavePracticeTask(ProgressDialog progress, Activity act, Context mContext,
			ArrayList<EntityRoutes> routeList, ArrayList<EntityLocations> locationsList,
			ArrayList<EntityHeartRate> hrList, ArrayList<EntityCadence> cadenceList) {
		this.progress = progress;
		this.mContext = mContext;
		this.routeList = routeList;
		this.locationsList = locationsList;
		this.hrList = hrList;
		this.cadenceList = cadenceList;
		this.act = act;
	}

	public void onPreExecute() {
		UIFunctionUtils.lockScreenOrientation(act);

		progress.show();
	}

	public Void doInBackground(Void... unused) {
		DataFunctionUtils.createRouteInDB(mContext, routeList);
		DataFunctionUtils.createLocationsInDB(mContext, locationsList);
		DataFunctionUtils.createHeartRateInDB(mContext, hrList);
		DataFunctionUtils.createCadenceInDB(mContext, cadenceList);
		DataFunctionUtils.modifyShoeInDB(mContext, routeList);

		return null;
	}

	public void onPostExecute(Void unused) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = prefs.edit();

		int lastAct = DataFunctionUtils.getLastRoute(mContext);

		editor.putInt("selected_practice", lastAct);
		editor.commit();

		mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_ROUTES"));
		mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_STATISTICS"));

		mContext.sendBroadcast(new Intent("android.intent.action.DESTROY_PRACTICEDETAILS_ACTIVITY"));

		act.startActivity(new Intent(mContext, SummaryHolder.class));

		progress.dismiss();

		UIFunctionUtils.unlockScreenOrientation(act);
	}
}