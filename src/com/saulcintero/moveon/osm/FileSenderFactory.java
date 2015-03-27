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

package com.saulcintero.moveon.osm;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.LoggerFactory;

import android.content.Context;
import android.os.Environment;

import com.saulcintero.moveon.R;
import com.saulcintero.moveon.listeners.IActionListener;
import com.saulcintero.moveon.utils.FunctionUtils;

public class FileSenderFactory {
	private static final org.slf4j.Logger tracer = LoggerFactory.getLogger(FileSenderFactory.class
			.getSimpleName());

	public static IFileSender GetOsmSender(Context applicationContext, IActionListener callback) {
		return new OSMHelper(applicationContext, callback);
	}

	public static void SendFiles(Context applicationContext, IActionListener callback) {
		tracer.info(applicationContext.getString(R.string.sending_files_to_osm));

		File gpxFolder = new File(Environment.getExternalStorageDirectory().toString() + "/moveon/gpx/tmp/");

		List<File> files = new ArrayList<File>(Arrays.asList(FunctionUtils.GetFilesInFolder(gpxFolder,
				new FilenameFilter() {
					@Override
					public boolean accept(File file, String s) {
						return s.contains(file.getAbsoluteFile().toString()) && !s.contains("zip");
					}
				})));

		if (files.size() == 0) {
			callback.OnFailure();
			return;
		}

		List<IFileSender> senders = GetFileSenders(applicationContext, callback);

		for (IFileSender sender : senders) {
			sender.UploadFile(files);
		}
	}

	public static List<IFileSender> GetFileSenders(Context applicationContext, IActionListener callback) {
		List<IFileSender> senders = new ArrayList<IFileSender>();

		if (OSMHelper.IsOsmAuthorized(applicationContext)) {
			senders.add(new OSMHelper(applicationContext, callback));
		}

		return senders;
	}
}