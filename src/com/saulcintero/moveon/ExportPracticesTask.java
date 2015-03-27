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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;

import com.saulcintero.moveon.db.DataManager;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.MediaFunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class ExportPracticesTask extends AsyncTask<Void, Void, Void> {
	private ProgressDialog progress;

	private Activity act;
	private Context mContext;

	private Intent i = new Intent("android.intent.action.WRITE_MSG_IN_DIALOG");

	private int[] idList;

	public static final int TYPE_GPX = 0;
	public static final int TYPE_KML = 1;
	public static final int TYPE_KMZ = 2;

	private int type = TYPE_KML;

	private String[] data;

	private boolean forSharing, isRunning;

	private DataManager DBManager = null;

	public ExportPracticesTask(ProgressDialog progress, Activity act, Context mContext, int type,
			int[] idList, boolean forSharing, String[] names) {
		this.progress = progress;
		this.act = act;
		this.mContext = mContext;

		this.type = type;
		this.idList = idList;
		this.forSharing = forSharing;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		isRunning = false;
		progress.dismiss();
	}

	public void onPreExecute() {
		UIFunctionUtils.lockScreenOrientation(act);

		isRunning = true;
		progress.show();
	}

	public Void doInBackground(Void... params) {
		for (int f = 0; f <= idList.length; f++) {
			if (f == idList.length) {
				isRunning = false;
			} else {
				if (!isRunning)
					break;

				if (type == TYPE_GPX) {
					try {
						exportGpx(idList[f]);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else if (type == TYPE_KML) {
					exportKml(idList[f], false);
				} else if (type == TYPE_KMZ) {
					exportKmz(idList[f]);
				}
			}
		}

		return null;
	}

	public String exportGpx(int id) throws ParseException {
		String path = "";
		if (forSharing)
			path = Environment.getExternalStorageDirectory().toString() + "/moveon/gpx/tmp/";
		else
			path = Environment.getExternalStorageDirectory().toString() + "/moveon/gpx/";

		FunctionUtils.createDirectory(path);

		data = DataFunctionUtils.getRouteData(mContext, id, true);

		String f = "";

		String time[] = data[19].replace(":", " ").split(" ");
		time[0] = time[0] + "h";
		time[1] = time[1] + "m";
		time[2] = time[2] + "s";

		f = path + data[18].replace("/", "-") + " " + time[0] + time[1] + time[2] + ".gpx";

		try {
			i.putExtra("msg", mContext.getString(R.string.dialog_export_creating_files));
			mContext.sendBroadcast(i);

			File file = new File(f);
			if (file.exists())
				file.delete();
			FileOutputStream fOut = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);

			i.putExtra("msg", mContext.getString(R.string.dialog_export_creating_gpx));
			mContext.sendBroadcast(i);

			osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<?xml-stylesheet type=\"text/xsl\" href=\"details.xsl\"?>\n"
					+ "<gpx version=\"1.0\""
					+ " creator=\"MoveOn Sports Tracker para Android. - http://www.saulcintero.com\""
					+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
					+ " xmlns=\"http://www.topografix.com/GPX/1/0\""
					+ " xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\""
					+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.topografix.com/GPX/Private/TopoGrafix/0/1 http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">\n"
					+ "\t<trk>\n" + "\t\t<name><![CDATA[" + data[1] + "]]></name>\n" + "\t\t<desc><![CDATA["
					+ data[11] + "]]></desc>\n" + "\t\t<number></number>\n"
					+ "\t\t<topografix:color>ff0000</topografix:color>\n" + "\t\t<trkseg>\n");

			DBManager = new DataManager(mContext);
			DBManager.Open();

			Cursor cursor = DBManager.getRowsFromTable(String.valueOf(id), "locations");
			cursor.moveToFirst();

			if (!cursor.isAfterLast()) {
				cursor.moveToFirst();

				while (!cursor.isAfterLast()) {
					SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
					Date dt = format.parse(data[18].replace("/", "-"));
					SimpleDateFormat my_format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
					String date2 = my_format.format(dt);

					osw.append("\t\t\t<trkpt lat=\""
							+ cursor.getFloat(cursor.getColumnIndex("latitude"))
							+ "\" lon=\""
							+ cursor.getFloat(cursor.getColumnIndex("longitude"))
							+ "\">\n"
							+ "\t\t\t\t<ele>"
							+ cursor.getFloat(cursor.getColumnIndex("altitude"))
							+ "</ele>\n"
							+ "\t\t\t\t<time>"
							+ date2
							+ "T"
							+ FunctionUtils.longFormatTime(FunctionUtils.getSecondsFromTime(data[19])
									+ cursor.getInt(cursor.getColumnIndex("time"))) + "Z" + "</time>\n"
							+ "\t\t\t</trkpt>\n");

					cursor.moveToNext();

					act.sendBroadcast(new Intent("android.intent.action.EXPORT_INCREMENT_PROGRESS"));
				}

				osw.append("\t\t</trkseg>\n" + "\t</trk>\n" + "</gpx>\n");

			}
			cursor.close();
			DBManager.Close();
			DBManager = null;

			osw.flush();
			osw.close();

			i.putExtra("msg", mContext.getString(R.string.dialog_export_finish));
			mContext.sendBroadcast(i);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return f;
	}

	public String exportKmz(int id) {
		FunctionUtils.createDirectory(Environment.getExternalStorageDirectory().toString() + "/moveon/kmz/");

		data = DataFunctionUtils.getRouteData(mContext, id, true);

		String time[] = data[19].replace(":", " ").split(" ");
		time[0] = time[0] + "h";
		time[1] = time[1] + "m";
		time[2] = time[2] + "s";

		String fZip = Environment.getExternalStorageDirectory().toString() + "/moveon/kmz/"
				+ data[18].replace("/", "-") + " " + time[0] + time[1] + time[2] + ".kmz";
		try {
			String fXml = Environment.getExternalStorageDirectory().toString() + "/moveon/kmz/"
					+ data[18].replace("/", "-") + " " + time[0] + time[1] + time[2] + ".kml";

			i.putExtra("msg", mContext.getString(R.string.dialog_export_creating_files));
			mContext.sendBroadcast(i);

			exportKml(id, true);

			i.putExtra("msg", mContext.getString(R.string.dialog_export_packaging));
			mContext.sendBroadcast(i);

			ZipFile zipfile = new ZipFile(fZip);
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			zipfile.addFile(new File(fXml), parameters);

			File file = new File(fXml);
			if (file.exists())
				file.delete();

			String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/moveon/images/";
			File folder = new File(file_path);

			if (folder.isDirectory()) {
				String[] files = folder.list();

				if (files.length > 0) {
					Arrays.sort(files);

					boolean areResources = false;

					for (int i = 0; i < files.length; i++) {
						String[] splitFile = files[i].split("_");

						if (splitFile[0].equals(String.valueOf(id))) {
							areResources = true;
						}
					}

					if (areResources) {
						i.putExtra("msg", mContext.getString(R.string.dialog_export_packaging_images));
						mContext.sendBroadcast(i);

						for (int i = 0; i < files.length; i++) {
							String[] splitFile = files[i].split("_");

							if (splitFile[0].equals(String.valueOf(id))) {
								// image marker
								File imageFile = new File(file_path + files[i]);
								ExifInterface exif = null;

								try {
									exif = new ExifInterface(imageFile.getAbsolutePath());

									float[] LatLong = MediaFunctionUtils.ShowExif(mContext, exif);

									if (LatLong != null) {
										zipfile.addFile(new File(file_path + files[i]), parameters);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

					}
				}
			}

			i.putExtra("msg", mContext.getString(R.string.dialog_export_finish));
			mContext.sendBroadcast(i);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return fZip;
	}

	public String exportKml(int id, boolean forKMZ) {
		FunctionUtils.createDirectory(Environment.getExternalStorageDirectory().toString() + "/moveon/kml/");

		data = DataFunctionUtils.getRouteData(mContext, id, true);

		String f = "";

		String time[] = data[19].replace(":", " ").split(" ");
		time[0] = time[0] + "h";
		time[1] = time[1] + "m";
		time[2] = time[2] + "s";

		if (forKMZ) {
			f = Environment.getExternalStorageDirectory().toString() + "/moveon/kmz/";
		} else {
			f = Environment.getExternalStorageDirectory().toString() + "/moveon/kml/";
		}
		f = f + data[18].replace("/", "-") + " " + time[0] + time[1] + time[2] + ".kml";

		try {
			i.putExtra("msg", mContext.getString(R.string.dialog_export_creating_files));
			mContext.sendBroadcast(i);

			File file = new File(f);
			if (file.exists())
				file.delete();
			FileOutputStream fOut = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);

			if (forKMZ) {
				i.putExtra("msg", mContext.getString(R.string.dialog_export_creating_kmz));
				mContext.sendBroadcast(i);
			} else {
				i.putExtra("msg", mContext.getString(R.string.dialog_export_creating_kml));
				mContext.sendBroadcast(i);
			}

			osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n"
					+ "\t<Document>\n"
					+ "\t\t<name><![CDATA["
					+ data[1]
					+ "]]></name>\n"
					+ "\t\t<description><![CDATA["
					+ data[11]
					+ "]]></description>\n"
					+ "\t\t<Style id=\"track\"><LineStyle><color>7f0000ff</color><width>4</width></LineStyle></Style>\n"
					+ "\t\t<Style id=\"sh_grn-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
					+ "\t\t<Style id=\"sh_red-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
					+ "\t\t<Style id=\"sh_blue-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/blu-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
					+ "\t\t<Style id=\"sh_ylw-pushpin\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
					+ "\t\t<Style id=\"sh_blue-pushpin\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pushpin/blue-pushpin.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n");

			DBManager = new DataManager(mContext);
			DBManager.Open();

			Cursor cursor = DBManager.getRowsFromTable(String.valueOf(id), "locations");
			cursor.moveToFirst();

			if (!cursor.isAfterLast()) {
				cursor.moveToFirst();

				String coords = cursor.getFloat(cursor.getColumnIndex("longitude")) + ","
						+ cursor.getFloat(cursor.getColumnIndex("latitude")) + ","
						+ cursor.getFloat(cursor.getColumnIndex("altitude"));

				osw.append("\t\t<Placemark>\n" + "\t\t\t<name><![CDATA[("
						+ mContext.getString(R.string.start) + ")]]></name>\n"
						+ "\t\t\t<description><![CDATA[" + mContext.getString(R.string.route_start)
						+ "]]></description>\n" + "\t\t\t<styleUrl>#sh_grn-circle</styleUrl>\n"
						+ "\t\t\t<Point>\n" + "\t\t\t\t<coordinates>" + coords + "</coordinates>\n"
						+ "\t\t\t</Point>\n" + "\t\t</Placemark>\n");

				// coords
				osw.append("\t\t<Placemark>\n" + "\t\t\t<name><![CDATA[" + data[1] + "]]></name>\n"
						+ "\t\t\t<description><![CDATA[" + data[11] + "]]></description>\n"
						+ "\t\t\t<styleUrl>#track</styleUrl>\n" + "\t\t\t<MultiGeometry>\n"
						+ "\t\t\t\t<LineString><coordinates>\n");

				while (!cursor.isAfterLast()) {
					osw.append("\t\t\t\t\t" + cursor.getFloat(cursor.getColumnIndex("longitude")) + ","
							+ cursor.getFloat(cursor.getColumnIndex("latitude")) + ","
							+ cursor.getFloat(cursor.getColumnIndex("altitude")) + "\n");
					cursor.moveToNext();
					act.sendBroadcast(new Intent("android.intent.action.EXPORT_INCREMENT_PROGRESS"));
				}

				osw.append("\t\t\t\t</coordinates></LineString>\n" + "\t\t\t</MultiGeometry>\n"
						+ "\t\t</Placemark>\n");

				// pauses
				boolean pauseMarker = true;

				cursor.moveToFirst();
				while (!cursor.isAfterLast()) {
					if (String.valueOf(cursor.getInt(cursor.getColumnIndex("pause"))).equals("1")) {
						if (pauseMarker) {
							osw.append("\t\t<Placemark>\n" + "\t\t\t<name><![CDATA[("
									+ mContext.getString(R.string.pause) + ")]]></name>\n"
									+ "\t\t\t<description><![CDATA["
									+ mContext.getString(R.string.route_pause) + "]]></description>\n"
									+ "\t\t\t<styleUrl>#sh_blue-circle</styleUrl>\n" + "\t\t\t<Point>\n"
									+ "\t\t\t\t<coordinates>"
									+ cursor.getFloat(cursor.getColumnIndex("longitude")) + ","
									+ cursor.getFloat(cursor.getColumnIndex("latitude")) + "</coordinates>\n"
									+ "\t\t\t</Point>\n" + "\t\t</Placemark>\n");

							pauseMarker = false;
						}
					} else {
						if (!pauseMarker) {
							osw.append("\t\t<Placemark>\n" + "\t\t\t<name><![CDATA[("
									+ mContext.getString(R.string.continued) + ")]]></name>\n"
									+ "\t\t\t<description><![CDATA["
									+ mContext.getString(R.string.route_continue) + "]]></description>\n"
									+ "\t\t\t<styleUrl>#sh_blue-circle</styleUrl>\n" + "\t\t\t<Point>\n"
									+ "\t\t\t\t<coordinates>"
									+ cursor.getFloat(cursor.getColumnIndex("longitude")) + ","
									+ cursor.getFloat(cursor.getColumnIndex("latitude")) + "</coordinates>\n"
									+ "\t\t\t</Point>\n" + "\t\t</Placemark>\n");
						}

						pauseMarker = true;
					}

					cursor.moveToNext();
				}

				String file_path = Environment.getExternalStorageDirectory().getAbsolutePath()
						+ "/moveon/images/";
				File folder = new File(file_path);

				if (folder.isDirectory()) {
					String[] files = folder.list();

					if (files.length > 0) {
						Arrays.sort(files);

						boolean areResources = false;

						for (int i = 0; i < files.length; i++) {
							String[] splitFile = files[i].split("_");

							if (splitFile[0].equals(String.valueOf(id))) {
								areResources = true;
							}
						}

						if (areResources) {
							for (int i = 0; i < files.length; i++) {
								String[] splitFile = files[i].split("_");

								if (splitFile[0].equals(String.valueOf(id))) {
									// image marker
									File imageFile = new File(file_path + files[i]);
									ExifInterface exif = null;

									try {
										exif = new ExifInterface(imageFile.getAbsolutePath());

										float[] LatLong = MediaFunctionUtils.ShowExif(mContext, exif);

										if (LatLong != null) {
											osw.append("\t\t<Placemark>\n" + "\t\t\t<name><![CDATA["
													+ files[i] + "]]></name>\n"
													+ "\t\t\t<description><![CDATA[");

											if (forKMZ) {
												osw.append("<img src=\"" + files[i]
														+ "\" width=\"192px\"/><br/>");
											} else {
												osw.append(mContext.getString(R.string.original_route_image));
											}

											osw.append("]]></description>\n"
													+ "\t\t\t<styleUrl>#sh_blue-pushpin</styleUrl>\n"
													+ "\t\t\t<Point>\n" + "\t\t\t\t<coordinates>"
													+ LatLong[1] + "," + LatLong[0] + "," + 0
													+ "</coordinates>\n" + "\t\t\t</Point>\n"
													+ "\t\t</Placemark>\n");

										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}

						}
					}
				}

				cursor.moveToLast();

				coords = cursor.getFloat(cursor.getColumnIndex("longitude")) + ","
						+ cursor.getFloat(cursor.getColumnIndex("latitude")) + ","
						+ cursor.getFloat(cursor.getColumnIndex("altitude"));

				osw.append("\t\t<Placemark>\n" + "\t\t\t<name><![CDATA[(" + mContext.getString(R.string.end)
						+ ")]]></name>\n" + "\t\t\t<description><![CDATA[" + data[3] + " "
						+ mContext.getString(R.string.long_unit1_detail_1) + " "
						+ mContext.getString(R.string.rides) + " " + data[2] + "]]></description>\n"
						+ "\t\t\t<styleUrl>#sh_red-circle</styleUrl>\n" + "\t\t\t<Point>\n"
						+ "\t\t\t\t<coordinates>" + coords + "</coordinates>\n" + "\t\t\t</Point>\n"
						+ "\t\t</Placemark>\n");

			}
			cursor.close();
			DBManager.Close();
			DBManager = null;

			osw.append("\t</Document>\n");
			osw.append("</kml>\n");

			osw.flush();

			i.putExtra("msg", mContext.getString(R.string.dialog_export_finish));
			mContext.sendBroadcast(i);

			osw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return f;
	}

	public void onPostExecute(Void params) {
		if (!forSharing) {
			String msg = mContext.getString(R.string.practice_save_in)
					+ Environment.getExternalStorageDirectory().getParent();
			if (type == TYPE_KML) {
				msg = msg + "/moveon/kml";
			} else if (type == TYPE_GPX) {
				msg = msg + "/moveon/gpx";
			} else if (type == TYPE_KMZ) {
				msg = msg + "/moveon/kmz";
			}
			UIFunctionUtils.showMessage(mContext, false, msg + "'");
		}

		progress.dismiss();

		UIFunctionUtils.unlockScreenOrientation(act);
	}
}