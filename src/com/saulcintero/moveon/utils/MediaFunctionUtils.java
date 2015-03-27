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
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import com.saulcintero.moveon.R;

public class MediaFunctionUtils {
	private static final String TAG = MediaFunctionUtils.class.getSimpleName();

	private static String lat, lon;

	public static int getCorrectImageAngle(Context mContext, String path, String file) {
		int angle = 0;

		try {
			File imageFile = new File(path + file);

			mContext.getContentResolver().notifyChange(Uri.fromFile(imageFile), null);

			ExifInterface exif;
			exif = new ExifInterface(imageFile.getAbsolutePath());

			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				angle = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				angle = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				angle = 90;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return angle;
	}

	public static Bitmap getPreview(Uri uri, float angle) {
		File image = new File(uri.getPath());

		BitmapFactory.Options bounds = new BitmapFactory.Options();
		bounds.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image.getPath(), bounds);
		if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
			return null;

		int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight : bounds.outWidth;

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = originalSize / 75;
		Bitmap thumbnail = BitmapFactory.decodeFile(image.getPath(), opts);

		Matrix mtx = new Matrix();
		mtx.postRotate(angle);
		Bitmap rotatedThumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(),
				thumbnail.getHeight(), mtx, true);

		return rotatedThumbnail;
	}

	public static float[] ShowExif(Context mContext, ExifInterface exif) {
		// String myAttribute="Exif information ---\n";
		// myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
		// myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
		// myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
		// myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF,
		// exif);
		// myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
		// myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF,
		// exif);
		// myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
		// myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
		// myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
		// myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
		// myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
		// myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);

		float[] LatLong = new float[2];
		if (exif.getLatLong(LatLong)) {
			Log.i(TAG, mContext.getString(R.string.latitude) + " " + LatLong[0]);
			Log.i(TAG, mContext.getString(R.string.longitude) + " " + LatLong[1]);
		} else {
			Log.i(TAG, mContext.getString(R.string.exif_tags_are_not_available));
			LatLong = null;
		}

		return LatLong;
	}

	// private static String getTagString(String tag, ExifInterface exif) {
	// return (tag + " : " + exif.getAttribute(tag) + "\n");
	// }

	public static void imageProcessing(Context mContext, float angle, String path, String file) {
		RotateImageAndSaveExif(mContext, path, file, angle, false);
	}

	public static void imageProcessing(Context mContext, float angle, String path, String file,
			String mLatitude, String mLongitude) {
		lat = "";
		lon = "";
		lat = mLatitude;
		lon = mLongitude;
		RotateImageAndSaveExif(mContext, path, file, angle, true);
	}

	private static void RotateImageAndSaveExif(Context mContext, String path, String file, float angle,
			boolean saveExif) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap targetBitmap = BitmapFactory.decodeFile(path + file, options);

		Matrix mtx = new Matrix();
		mtx.postRotate(angle);
		Bitmap rotatedBitmap = Bitmap.createBitmap(targetBitmap, 0, 0, targetBitmap.getWidth(),
				targetBitmap.getHeight(), mtx, true);

		File mFile = new File(path + file);

		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(mFile);
			rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();

			targetBitmap.recycle();
			rotatedBitmap.recycle();

			if (saveExif)
				saveExifData(mContext, path, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveExifData(Context mContext, String path, String file) {
		try {
			File imageFile = new File(path + file);

			mContext.getContentResolver().notifyChange(Uri.fromFile(imageFile), null);

			ExifInterface exif;
			exif = new ExifInterface(imageFile.getAbsolutePath());

			String LATITUDE = degreeDecimal2ExifFormat(Double.parseDouble(lat));
			String LATITUDE_REF = "N";
			String LONGITUDE = degreeDecimal2ExifFormat(Double.parseDouble(lon));
			String LONGITUDE_REF = "E";

			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, LATITUDE);
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, LATITUDE_REF);
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, LONGITUDE);
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, LONGITUDE_REF);

			exif.saveAttributes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String degreeDecimal2ExifFormat(double decimalDegree) {
		StringBuilder sb = new StringBuilder();

		sb.append((int) decimalDegree);
		sb.append("/1,");

		decimalDegree = (decimalDegree - (int) decimalDegree) * 60;
		sb.append((int) decimalDegree);
		sb.append("/1,");

		decimalDegree = (decimalDegree - (int) decimalDegree) * 60000;
		sb.append((int) decimalDegree);
		sb.append("/1000");

		return sb.toString();
	}
}