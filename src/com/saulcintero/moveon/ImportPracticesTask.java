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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.saulcintero.moveon.entities.EntityLocations;
import com.saulcintero.moveon.entities.EntityRoutes;
import com.saulcintero.moveon.enums.ActivityTypes;
import com.saulcintero.moveon.utils.DataFunctionUtils;
import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class ImportPracticesTask extends AsyncTask<Void, Void, Void> {
	public static int TYPE_GPX = 0;
	public static int TYPE_KML = 1;

	static final int BUFFER = 16384;

	private ProgressDialog progress;

	private Activity act;

	private Context mContext;

	private SharedPreferences prefs;

	private ArrayList<EntityRoutes> routeList;
	private ArrayList<EntityLocations> locationsList;

	private String[] files;

	private String path;

	private int type = TYPE_GPX;

	private int activity;

	private String date, time;

	private boolean deleteOnFinish, importingError = false, isFirstLapReadingGpx;

	public ImportPracticesTask(ProgressDialog progress, Activity act, Context mContext, int type,
			String date, String time, int activity, String[] files, boolean deleteOnFinish) {
		this.progress = progress;
		this.act = act;
		this.mContext = mContext;
		this.type = type;
		this.activity = activity;
		this.date = date;
		this.time = time;
		this.files = files;
		this.deleteOnFinish = deleteOnFinish;

		prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		if (importingError)
			UIFunctionUtils.showMessage(mContext, false, mContext.getString(R.string.import_error));

		progress.dismiss();
	}

	public void onPreExecute() {
		UIFunctionUtils.lockScreenOrientation(act);

		progress.show();
	}

	public Void doInBackground(Void... params) {
		for (int f = 0; f <= (files.length - 1); f++) {
			if (type == TYPE_GPX) {
				readGpxAndFillData(files[f].toString());
			} else if (type == TYPE_KML) {
				readKmlAndFillData(files[f].toString());
			}

			importPractices();

			if (deleteOnFinish) {
				File file = new File(path + files[f].toString());
				if (file.exists())
					file.delete();
			}

			mContext.sendBroadcast(new Intent("android.intent.action.REFRESH_ROUTES"));
		}

		return null;
	}

	private void readGpxAndFillData(String file) {
		try {
			routeList = new ArrayList<EntityRoutes>();
			locationsList = new ArrayList<EntityLocations>();

			path = Environment.getExternalStorageDirectory().toString() + "/moveon/gpx/";

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = (XmlPullParser) factory.newPullParser();
			parser.setInput(new FileReader(path + file));

			int eventType = parser.getEventType();

			boolean hasName = false;
			boolean hasDescription = false;
			boolean isTrkseg = false;
			boolean hasDistance = false;

			float distance = 0.0f, lastDistance = 0.0f, maxSpeed = 0.0f, maxAltitude = 0.0f, minAltitude = 0.0f, upAccumAltitude = 0.0f, downAccumAltitude = 0.0f, lastAltitude = 0.0f;

			int lastTime = 0, totalTime = 0;

			List<Float> speedList = new ArrayList<Float>();

			Location auxLoc = null;

			EntityRoutes route = new EntityRoutes();
			EntityLocations locations = new EntityLocations();

			route.setDate(date);
			route.setHour(time);
			route.setCategory_id(String.valueOf(activity + 1));
			route.setName("Track KML");
			route.setTime("0");
			route.setAvg_speed("0.0");
			route.setMax_speed("0.0");
			route.setKcal("0");
			route.setHiit_id("0");
			route.setShoe_id("0");
			route.setSteps("0");
			route.setAvg_hr("0");
			route.setMax_hr("0");
			route.setAvg_cadence("0");
			route.setMax_cadence("0");
			route.setComments("");

			initializeLocations(locations);

			isFirstLapReadingGpx = true;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (parser.getName() == null) {
					eventType = parser.next();
				}

				if (eventType == XmlPullParser.START_TAG) {
					if ((parser.getName().toLowerCase(Locale.getDefault()).equals("name")) && !hasName) {
						String nextText = parser.nextText();
						if (checkIfNexttextHasContent(nextText)) {
							route.setName(nextText);
							hasName = true;
						}
					}

					if ((parser.getName().toLowerCase(Locale.getDefault()).equals("desc")) && !hasDescription) {
						String nextText = parser.nextText();
						if (checkIfNexttextHasContent(nextText)) {
							route.setComments(nextText);
							hasDescription = true;
						}
					}

					if (parser.getName().toLowerCase(Locale.getDefault()).equals("trkseg")) {
						isTrkseg = true;
					}

					if ((parser.getName().toLowerCase(Locale.getDefault()).equals("trkpt")) && isTrkseg) {
						if (parser.getAttributeValue(null, "lon") != null
								&& parser.getAttributeValue(null, "lat") != null) {
							String[] coords = { parser.getAttributeValue(null, "lon"),
									parser.getAttributeValue(null, "lat") };

							locations.setLongitude(coords[0]);
							locations.setLatitude(coords[1]);

							Location loc = new Location(LocationManager.GPS_PROVIDER);
							loc.setLongitude(Double.parseDouble(coords[0]));
							loc.setLatitude(Double.parseDouble(coords[1]));

							if (auxLoc != null) {
								hasDistance = true;
								lastDistance = auxLoc.distanceTo(loc) / 1000;

								distance += lastDistance;
							}

							locations.setDistance(String.valueOf(FunctionUtils.customizedRound(distance, 2)));

							auxLoc = loc;
						}

						route.setDistance(String.valueOf(FunctionUtils.customizedRound(distance, 2)));
					}
				}

				if ((parser.getName().toLowerCase(Locale.getDefault()).equals("ele")) && isTrkseg) {
					String nextText = parser.nextText();
					if (checkIfNexttextHasContent(nextText)) {
						String elevation = nextText;

						if (elevation.length() > 0)
							locations.setAltitude(elevation);

						if (isFirstLapReadingGpx) {
							maxAltitude = Float.parseFloat(elevation);
							minAltitude = Float.parseFloat(elevation);
						}

						if (maxAltitude < Float.parseFloat(elevation))
							maxAltitude = Float.parseFloat(elevation);

						if (minAltitude > Float.parseFloat(elevation))
							minAltitude = Float.parseFloat(elevation);

						if (Float.parseFloat(elevation) > lastAltitude) {
							upAccumAltitude = upAccumAltitude + (Float.parseFloat(elevation) - lastAltitude);
						} else if (Float.parseFloat(elevation) < lastAltitude) {
							downAccumAltitude = downAccumAltitude
									+ (lastAltitude - Float.parseFloat(elevation));
						}

						route.setMax_altitude(String.valueOf(maxAltitude));
						route.setMin_altitude(String.valueOf(minAltitude));
						route.setUp_accum_altitude(String.valueOf(upAccumAltitude));
						route.setDown_accum_altitude(String.valueOf(downAccumAltitude));

						lastAltitude = Float.parseFloat(elevation);
					}
				}

				if ((parser.getName().toLowerCase(Locale.getDefault()).equals("time")) && isTrkseg) {
					String nextText = parser.nextText();
					if (checkIfNexttextHasContent(nextText)) {
						String[] splitTime = nextText.split("T");
						String[] time;

						if (FunctionUtils.containsChar(splitTime[1], ".")) {
							int dotPlace = splitTime[1].toString().indexOf(".");
							String initTime = splitTime[1].substring(0, dotPlace);
							time = initTime.split(":");
						} else {
							time = splitTime[1].replace("Z", "").split(":");
						}

						if (isFirstLapReadingGpx) {
							locations.setTime("0");
							totalTime = 0;

							lastTime = (Integer.parseInt(time[0]) * 3600) + (Integer.parseInt(time[1]) * 60)
									+ Integer.parseInt(time[2]);

							isFirstLapReadingGpx = false;
						} else {
							totalTime = totalTime
									+ ((Integer.parseInt(time[0]) * 3600) + (Integer.parseInt(time[1]) * 60)
											+ Integer.parseInt(time[2]) - lastTime);

							locations.setTime(String.valueOf(totalTime));

							if (hasDistance) {
								hasDistance = false;

								float speed = ((lastDistance * 1000) / ((Integer.parseInt(time[0]) * 3600)
										+ (Integer.parseInt(time[1]) * 60) + Integer.parseInt(time[2]) - lastTime)) * 3.6f;

								if (speed > maxSpeed)
									maxSpeed = speed;

								speedList.add(speed);
								locations.setSpeed(String.valueOf(speed));
							}
						}

						lastTime = (Integer.parseInt(time[0]) * 3600) + (Integer.parseInt(time[1]) * 60)
								+ Integer.parseInt(time[2]);
					}
				}

				if (eventType == XmlPullParser.END_TAG) {
					if (parser.getName().toLowerCase(Locale.getDefault()).equals("trkpt")) {
						locationsList.add(locations);

						locations = new EntityLocations();
						initializeLocations(locations);
					}

					if (parser.getName().toLowerCase(Locale.getDefault()).equals("trkseg")) {
						isTrkseg = false;

						route.setTime(String.valueOf(totalTime));
						route.setKcal(String.valueOf(FunctionUtils.calculateCalories(
								ActivityTypes.values()[activity + 1],
								Float.parseFloat(prefs.getString("body_weight", "75.0")),
								prefs.getString("gender", "M"), (float) totalTime, (float) distance)));
						route.setUp_accum_altitude(String.valueOf(upAccumAltitude));
						route.setDown_accum_altitude(String.valueOf(downAccumAltitude));
						route.setMax_altitude(String.valueOf(maxAltitude));
						route.setMin_altitude(String.valueOf(minAltitude));
						route.setAvg_speed(String.valueOf(FunctionUtils.customizedRound(
								FunctionUtils.calculateAverageToFloatValue(speedList), 2)));
						route.setMax_speed(String.valueOf(FunctionUtils.customizedRound(maxSpeed, 2)));

						routeList.add(route);
					}
				}

				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();

			importingError = true;
		}
	}

	private void initializeLocations(EntityLocations locations) {
		locations.setSpeed("0.0");
		locations.setTime("0");
		locations.setSteps("0");
		locations.setHr("0");
		locations.setCadence("0");
		locations.setPause("0");
	}

	private void readKmlAndFillData(String file) {
		try {
			routeList = new ArrayList<EntityRoutes>();
			locationsList = new ArrayList<EntityLocations>();

			path = Environment.getExternalStorageDirectory().toString() + "/moveon/kml/";

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = (XmlPullParser) factory.newPullParser();
			parser.setInput(new FileReader(path + file));

			int eventType = parser.getEventType();

			boolean hasName = false;
			boolean hasDescription = false;
			boolean isLineString = false;

			EntityRoutes route = new EntityRoutes();

			route.setDate(date);
			route.setHour(time);
			route.setCategory_id(String.valueOf(activity + 1));
			route.setName("Track KML");
			route.setTime("0");
			route.setAvg_speed("0.0");
			route.setMax_speed("0.0");
			route.setKcal("0");
			route.setHiit_id("0");
			route.setShoe_id("0");
			route.setSteps("0");
			route.setAvg_hr("0");
			route.setMax_hr("0");
			route.setAvg_cadence("0");
			route.setMax_cadence("0");
			route.setComments("");

			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (parser.getName() == null) {
					eventType = parser.next();
				}

				if (eventType == XmlPullParser.START_TAG) {
					if ((parser.getName().toLowerCase(Locale.getDefault()).equals("name")) && !hasName) {
						String nextText = parser.nextText();
						if (checkIfNexttextHasContent(nextText)) {
							route.setName(nextText);
							hasName = true;
						}
					}

					if ((parser.getName().toLowerCase(Locale.getDefault()).equals("description"))
							&& !hasDescription) {
						String nextText = parser.nextText();
						if (checkIfNexttextHasContent(nextText)) {
							route.setComments(nextText);
							hasDescription = true;
						}
					}

					if (parser.getName().toLowerCase(Locale.getDefault()).equals("linestring")) {
						isLineString = true;
					}

					if ((parser.getName().toLowerCase(Locale.getDefault()).equals("coordinates"))
							&& isLineString) {
						String nextText = parser.nextText();
						if (checkIfNexttextHasContent(nextText)) {
							String[] str = nextText.split("[ \t\n]");

							float distance = 0.0f, maxAltitude = 0.0f, minAltitude = 0.0f, upAccumAltitude = 0.0f, downAccumAltitude = 0.0f, lastAltitude = 0.0f;
							Location auxLoc = null;

							for (int i = 0; i < str.length; i++) {
								String chain = str[i].trim();
								if (!chain.equals("")) {
									String[] coords = chain.split(",");

									EntityLocations locations = new EntityLocations();

									locations.setSpeed("0.0");
									locations.setTime("0");
									locations.setSteps("0");
									locations.setHr("0");
									locations.setCadence("0");
									locations.setPause("0");
									locations.setLongitude(coords[0]);
									locations.setLatitude(coords[1]);

									if (coords.length > 1)
										locations.setAltitude(coords[2]);

									Location loc = new Location(LocationManager.GPS_PROVIDER);
									loc.setLongitude(Double.parseDouble(coords[0]));
									loc.setLatitude(Double.parseDouble(coords[1]));

									if (coords.length > 1)
										loc.setAltitude(Double.parseDouble(coords[2]));

									if (maxAltitude < loc.getAltitude())
										maxAltitude = (float) loc.getAltitude();

									if (minAltitude > (float) loc.getAltitude())
										minAltitude = (float) loc.getAltitude();

									if (auxLoc != null) {
										distance += auxLoc.distanceTo(loc) / 1000;

										if (loc.getAltitude() > lastAltitude) {
											upAccumAltitude = upAccumAltitude
													+ ((float) loc.getAltitude() - lastAltitude);
										} else if (loc.getAltitude() < lastAltitude) {
											downAccumAltitude = downAccumAltitude
													+ (lastAltitude - (float) loc.getAltitude());
										}
									}

									locations.setDistance(String.valueOf(FunctionUtils.customizedRound(
											distance, 2)));

									locationsList.add(locations);

									auxLoc = loc;
									lastAltitude = (float) loc.getAltitude();
								}
							}
							route.setMax_altitude(String.valueOf(maxAltitude));
							route.setMin_altitude(String.valueOf(minAltitude));
							route.setUp_accum_altitude(String.valueOf(upAccumAltitude));
							route.setDown_accum_altitude(String.valueOf(downAccumAltitude));
							route.setDistance(String.valueOf(FunctionUtils.customizedRound(distance, 2)));

							routeList.add(route);
						}
					}
				}

				if (eventType == XmlPullParser.END_TAG) {
					if (parser.getName().toLowerCase(Locale.getDefault()).equals("linestring")) {
						isLineString = false;
					}
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();

			importingError = true;
		}
	}

	private boolean checkIfNexttextHasContent(String nextText) {
		if (nextText == null)
			return false;
		else
			return true;
	}

	private void importPractices() {
		DataFunctionUtils.createRouteInDB(mContext, routeList);
		DataFunctionUtils.createLocationsInDB(mContext, locationsList);
	}

	public void onPostExecute(Void params) {
		UIFunctionUtils.showMessage(mContext, false, mContext.getString(R.string.import_correctly));

		progress.dismiss();

		UIFunctionUtils.unlockScreenOrientation(act);
	}
}