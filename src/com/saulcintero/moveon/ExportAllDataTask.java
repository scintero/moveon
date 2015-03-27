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
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class ExportAllDataTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = ExportAllDataTask.class.getSimpleName();

	private ProgressDialog progress;

	private Activity act;

	private boolean result;

	private boolean isRunning;

	public ExportAllDataTask(ProgressDialog progress, ListActivity act) {
		this.progress = progress;
		this.act = act;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		isRunning = false;
		progress.dismiss();
	}

	public void onPreExecute() {
		isRunning = true;
		progress.show();
	}

	@SuppressLint("SdCardPath")
	public Void doInBackground(Void... unused) {
		Log.i(TAG, act.getString(R.string.exporting_data));

		while (isRunning) {
			InputStream myInput;

			try {
				File file = null;
				for (int i = 0; i <= 1; i++) {
					switch (i) {
					case 0:
						file = new File(Environment.getExternalStorageDirectory().toString()
								+ "/moveon/database/moveon.db");
						break;
					case 1:
						file = new File(Environment.getExternalStorageDirectory().toString()
								+ "/moveon/moveon_backup.zip");
						break;
					}
					if (file.exists())
						file.delete();
				}

				myInput = new FileInputStream("/data/data/com.saulcintero.moveon/databases/moveon.db");

				File directory = new File(Environment.getExternalStorageDirectory().toString()
						+ "/moveon/database");

				if (!directory.exists()) {
					directory.mkdirs();
				}

				OutputStream myOutput = new FileOutputStream(directory.getPath() + "/moveon.db");

				// Transfer bytes from the input file to the output file
				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer)) > 0) {
					myOutput.write(buffer, 0, length);
				}

				myOutput.flush();
				myOutput.close();
				myInput.close();

				ZipFile zipfile = new ZipFile(Environment.getExternalStorageDirectory().toString()
						+ "/moveon/moveon_backup.zip");
				ZipParameters parameters = new ZipParameters();
				parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
				parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
				zipfile.addFolder(Environment.getExternalStorageDirectory().toString() + "/moveon",
						parameters);

			} catch (FileNotFoundException e) {
				result = false;

				e.printStackTrace();
			} catch (IOException e) {
				result = false;

				e.printStackTrace();
			} catch (ZipException e) {
				result = false;

				e.printStackTrace();
			}

			result = true;
			isRunning = false;
		}

		return null;
	}

	public void onPostExecute(Void unused) {
		Intent intent;
		Bundle b = new Bundle();
		intent = new Intent("android.intent.action.EXPORT_DATA_TOAST");
		if (result) {
			b.putInt("toast_option", 1);

		} else {
			b.putInt("toast_option", 2);
		}
		intent.putExtras(b);
		act.sendBroadcast(intent);

		progress.dismiss();

		act.sendBroadcast(new Intent("android.intent.action.UNLOCK_SCREEN_ORIENTATION"));
	}
}