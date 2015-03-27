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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.lingala.zip4j.core.ZipFile;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.saulcintero.moveon.utils.UIFunctionUtils;

public class ImportAllDataTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = ImportAllDataTask.class.getSimpleName();

	private ProgressDialog progress;

	private Activity act;

	private boolean result, showToast = false;

	public static String DB_FILEPATH = Environment.getExternalStorageDirectory().toString()
			+ "/moveon/database/moveon.db";

	@SuppressLint("SdCardPath")
	public static String dbPath = "/data/data/com.saulcintero.moveon/databases/moveon.db";

	public ImportAllDataTask(ProgressDialog progress, Activity act) {
		this.progress = progress;
		this.act = act;
	}

	public void onPreExecute() {
		UIFunctionUtils.lockScreenOrientation(act);

		progress.show();
	}

	public Void doInBackground(Void... unused) {
		Log.i(TAG, act.getString(R.string.importing_data));

		try {
			String source = Environment.getExternalStorageDirectory().toString()
					+ "/moveon/moveon_backup.zip";
			String destination = Environment.getExternalStorageDirectory().toString();
			ZipFile zipFile = new ZipFile(source);
			zipFile.extractAll(destination);

			File file = new File(DB_FILEPATH);
			if (file.exists()) {
				showToast = true;

				result = importDatabase();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (net.lingala.zip4j.exception.ZipException e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean importDatabase() throws IOException {
		OutputStream myOutput;

		try {
			myOutput = new FileOutputStream(dbPath);

			File directory = new File(Environment.getExternalStorageDirectory().toString()
					+ "/moveon/database");

			InputStream myInputs = new FileInputStream(directory.getPath() + "/moveon.db");

			// Transfer bytes from the input file to the output file
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInputs.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}

			myOutput.flush();
			myOutput.close();
			myInputs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void onPostExecute(Void unused) {
		if (showToast) {
			Intent intent;
			Bundle b = new Bundle();
			intent = new Intent("android.intent.action.IMPORT_DATA_TOAST");
			if (result) {
				b.putInt("toast_option", 1);

			} else {
				b.putInt("toast_option", 2);
			}
			intent.putExtras(b);
			act.sendBroadcast(intent);
		}

		act.sendBroadcast(new Intent("android.intent.action.REFRESH_ROUTES"));
		act.sendBroadcast(new Intent("android.intent.action.REFRESH_STATISTICS"));

		progress.dismiss();

		UIFunctionUtils.unlockScreenOrientation(act);
	}
}