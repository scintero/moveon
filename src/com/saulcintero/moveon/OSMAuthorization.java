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

import java.util.Locale;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;

import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.saulcintero.moveon.osm.OSMHelper;

public class OSMAuthorization extends PreferenceActivity {
	private static final org.slf4j.Logger tracer = LoggerFactory.getLogger(OSMAuthorization.class
			.getSimpleName());
	private static OAuthProvider provider;
	private static OAuthConsumer consumer;

	private Context mContext;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		editor = prefs.edit();

		addPreferencesFromResource(R.xml.osmsettings);

		Preference visibilityPref = findPreference("osm_visibility");
		Preference descriptionPref = findPreference("osm_description");
		Preference tagsPref = findPreference("osm_tags");
		Preference resetPref = findPreference("osm_resetauth");

		final Intent intent = getIntent();
		final Uri myURI = intent.getData();

		if (myURI != null && myURI.getQuery() != null && myURI.getQuery().length() > 0) {
			// User has returned! Read the verifier info from querystring
			String oAuthVerifier = myURI.getQueryParameter("oauth_verifier");

			try {
				if (provider == null)
					provider = OSMHelper.GetOSMAuthProvider(mContext);

				if (consumer == null)
					// In case consumer is null, re-initialize from stored
					// values
					consumer = OSMHelper.GetOSMAuthConsumer(mContext);

				// Ask OpenStreetMap for the access token. This is the main
				// event.
				provider.retrieveAccessToken(consumer, oAuthVerifier);

				String osmAccessToken = consumer.getToken();
				String osmAccessTokenSecret = consumer.getTokenSecret();

				editor.putString("osm_accesstoken", osmAccessToken);
				editor.putString("osm_accesstokensecret", osmAccessTokenSecret);
				editor.commit();
			} catch (Exception e) {
				tracer.error("MoveOnPreferences.onResume - "
						+ getString(R.string.user_has_returned).toLowerCase(Locale.getDefault()).toString(),
						e);
			}
		}

		if (!OSMHelper.IsOsmAuthorized(mContext)) {
			resetPref.setTitle(R.string.osm_lbl_authorize);
			resetPref.setSummary(R.string.osm_lbl_authorize_description);
			visibilityPref.setEnabled(false);
			descriptionPref.setEnabled(false);
			tagsPref.setEnabled(false);
		} else {
			visibilityPref.setEnabled(true);
			descriptionPref.setEnabled(true);
			tagsPref.setEnabled(true);

		}

		resetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if (OSMHelper.IsOsmAuthorized(mContext)) {
					editor.remove("osm_accesstoken");
					editor.remove("osm_accesstokensecret");
					editor.remove("osm_requesttoken");
					editor.remove("osm_requesttokensecret");
					editor.commit();
					finish();
				} else {
					try {
						// StrictMode.enableDefaults();
						StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
								.detectNetwork().penaltyLog().build();
						StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().penaltyLog().penaltyDeath()
								.build());
						StrictMode.setThreadPolicy(policy);

						// User clicks. Set the consumer and provider up
						consumer = OSMHelper.GetOSMAuthConsumer(mContext);
						provider = OSMHelper.GetOSMAuthProvider(mContext);

						String authUrl;

						// Get the request token and request token secret
						authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

						editor.putString("osm_requesttoken", consumer.getToken());
						editor.putString("osm_requesttokensecret", consumer.getTokenSecret());
						editor.commit();

						// Open browser, send user to OpenStreetMap.org
						Uri uri = Uri.parse(authUrl);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
						finish();
					} catch (Exception e) {
						tracer.error("OSMAuthorizationActivity.onClick", e);
					}
				}

				return true;
			}
		});
	}
}