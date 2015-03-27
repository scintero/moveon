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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.saulcintero.moveon.utils.FunctionUtils;
import com.saulcintero.moveon.utils.UIFunctionUtils;

public class EditHiitInterval extends Activity implements OnClickListener {
	private Context mContext;

	private TextView minutes_text, seconds_text;

	private Spinner spinner;
	private ArrayAdapter<String> spinnerArrayAdapter;

	private Button minutes_upButton, minutes_downButton, seconds_upButton, seconds_downButton, cancel, save;

	private String[] mContent;

	private int minutes = 0, seconds = 0;

	int type = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);

		setContentView(R.layout.add_or_edit_hiit_interval);

		super.onCreate(savedInstanceState);

		mContext = getApplicationContext();

		Bundle b = getIntent().getExtras();
		mContent = b.getString("row").split(",");

		minutes_upButton = (Button) findViewById(R.id.minutes_upButton);
		minutes_downButton = (Button) findViewById(R.id.minutes_downButton);
		seconds_upButton = (Button) findViewById(R.id.seconds_upButton);
		seconds_downButton = (Button) findViewById(R.id.seconds_downButton);
		cancel = (Button) findViewById(R.id.cancel);
		save = (Button) findViewById(R.id.save);
		spinner = (Spinner) findViewById(R.id.spinner1);
		minutes_text = (EditText) findViewById(R.id.numberEditText1);
		seconds_text = (EditText) findViewById(R.id.numberEditText2);

		minutes_upButton.setOnClickListener(this);
		minutes_downButton.setOnClickListener(this);
		seconds_upButton.setOnClickListener(this);
		seconds_downButton.setOnClickListener(this);
		cancel.setOnClickListener(this);
		save.setOnClickListener(this);

		String[] activities = { getString(R.string.hiit_type_description1),
				getString(R.string.hiit_type_description2), getString(R.string.hiit_type_description3),
				getString(R.string.hiit_type_description4) };

		spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, activities);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerArrayAdapter);

		spinner.setSelection(Integer.parseInt(mContent[3]) - 1);
		type = Integer.parseInt(mContent[3]) - 1;
		int time = Integer.parseInt(mContent[2]);
		minutes = time / 60;
		seconds = time % 60;
		seconds_text.setText(String.valueOf(seconds));
		minutes_text.setText(String.valueOf(minutes));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				type = i + 1;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		minutes_text.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (FunctionUtils.isNumeric(minutes_text.getText().toString())) {
					if (Integer.parseInt(minutes_text.getText().toString()) < 60)
						minutes = (Integer.parseInt(minutes_text.getText().toString()));
				} else {
					UIFunctionUtils.showMessage(mContext, true,
							getString(R.string.minutes_interval_error_in_number));

					minutes_text.setText(String.valueOf(minutes));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});

		seconds_text.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (FunctionUtils.isNumeric(seconds_text.getText().toString())) {
					if (Integer.parseInt(seconds_text.getText().toString()) < 60)
						seconds = (Integer.parseInt(seconds_text.getText().toString()));
				} else {
					UIFunctionUtils.showMessage(mContext, true,
							getString(R.string.seconds_interval_error_in_number));

					seconds_text.setText(String.valueOf(seconds));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.minutes_upButton:
			if ((minutes >= 0) && (minutes < 59)) {
				minutes += 1;
				minutes_text.setText(String.valueOf(minutes));
			}
			break;
		case R.id.minutes_downButton:
			if ((minutes > 0) && (minutes < 60)) {
				minutes -= 1;
				minutes_text.setText(String.valueOf(minutes));
			}
			break;
		case R.id.seconds_upButton:
			if ((seconds >= 0) && (seconds < 59)) {
				seconds += 1;
				seconds_text.setText(String.valueOf(seconds));
			}
			break;
		case R.id.seconds_downButton:
			if ((seconds > 0) && (seconds < 60)) {
				seconds -= 1;
				seconds_text.setText(String.valueOf(seconds));
			}
			break;
		case R.id.cancel:
			finish();
			break;
		case R.id.save:
			int time = (minutes * 60) + seconds;
			if (time > 0) {
				Intent i = new Intent("android.intent.action.EDIT_INTERVAL");

				String description = "";
				switch (type) {
				case 1:
					description = getString(R.string.hiit_type_description1).toLowerCase(Locale.getDefault());
					break;
				case 2:
					description = getString(R.string.hiit_type_description2).toLowerCase(Locale.getDefault());
					break;
				case 3:
					description = getString(R.string.hiit_type_description3).toLowerCase(Locale.getDefault());
					break;
				case 4:
					description = getString(R.string.hiit_type_description4).toLowerCase(Locale.getDefault());
					break;
				}
				i.putExtra("id", mContent[0]);
				i.putExtra("description", description);
				i.putExtra("type", String.valueOf(type));
				i.putExtra("time", String.valueOf(time));

				sendBroadcast(i);

				finish();
			} else {
				UIFunctionUtils.showMessage(this, true, getString(R.string.hiit_create_interval_error));
			}

			break;
		}
	}
}