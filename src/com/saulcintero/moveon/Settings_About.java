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

import java.util.Calendar;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class Settings_About extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		TextView tv1 = (TextView) findViewById(R.id.about_version);
		TextView tv2 = (TextView) findViewById(R.id.about_license);

		String versionName = "";
		try {
			final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		tv1.setText(versionName);

		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		if (year > 2015)
			tv2.setText("Copyright \u00a9 2015 - " + year + ", Saúl Cintero www.saulcintero.com\n\n"
					+ getString(R.string.about_license_description));
		else
			tv2.setText("Copyright \u00a9 2015, Saúl Cintero www.saulcintero.com\n\n"
					+ getString(R.string.about_license_description));
	}
}