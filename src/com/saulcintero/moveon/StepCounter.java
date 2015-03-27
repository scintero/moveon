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

import android.content.Context;
import android.content.Intent;

import com.saulcintero.moveon.listeners.StepListener;
import com.saulcintero.moveon.services.MoveOnService;

public class StepCounter implements StepListener {
	private Context mContext;

	private int steps = 0;

	private Intent i = new Intent("android.intent.action.STEP_COUNTER");

	public StepCounter(Context context) {
		mContext = context;
	}

	public void onStep() {
		if ((MoveOnService.getIsPracticeRunning()) && (!MoveOnService.getIsPracticePaused())) {
			steps += 1;
			i.putExtra("steps", String.valueOf(steps));
			mContext.sendBroadcast(i);
		}
	}

	public int getStepValue() {
		return steps;
	}

	public void setStepValue(int value) {
		steps = value;
	}
}