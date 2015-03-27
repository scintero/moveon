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

package com.saulcintero.moveon.entities;

public class EntityRoutes {
	private String category_id;
	private String shoe_id;
	private String name;
	private String hiit_id;
	private String date;
	private String hour;
	private String time;
	private String distance;
	private String avg_speed;
	private String max_speed;
	private String kcal;
	private String up_accum_altitude;
	private String down_accum_altitude;
	private String max_altitude;
	private String min_altitude;
	private String avg_hr;
	private String max_hr;
	private String steps;
	private String avg_cadence;
	private String max_cadence;
	private String comments;

	public EntityRoutes() {
		category_id = "";
		shoe_id = "";
		name = "";
		hiit_id = "";
		date = "";
		hour = "";
		time = "";
		distance = "";
		avg_speed = "";
		max_speed = "";
		kcal = "";
		up_accum_altitude = "";
		down_accum_altitude = "";
		max_altitude = "";
		min_altitude = "";
		avg_hr = "";
		max_hr = "";
		steps = "";
		avg_cadence = "";
		max_cadence = "";
		comments = "";
	}

	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}

	public void setShoe_id(String shoe_id) {
		this.shoe_id = shoe_id;
	}

	public void setHiit_id(String hiit_id) {
		this.hiit_id = hiit_id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setHour(String hour) {
		this.hour = hour;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public void setAvg_speed(String avg_speed) {
		this.avg_speed = avg_speed;
	}

	public void setMax_speed(String max_speed) {
		this.max_speed = max_speed;
	}

	public void setKcal(String kcal) {
		this.kcal = kcal;
	}

	public void setUp_accum_altitude(String up_accum_altitude) {
		this.up_accum_altitude = up_accum_altitude;
	}

	public void setDown_accum_altitude(String down_accum_altitude) {
		this.down_accum_altitude = down_accum_altitude;
	}

	public void setMax_altitude(String max_altitude) {
		this.max_altitude = max_altitude;
	}

	public void setMin_altitude(String min_altitude) {
		this.min_altitude = min_altitude;
	}

	public void setAvg_hr(String avg_hr) {
		this.avg_hr = avg_hr;
	}

	public void setMax_hr(String max_hr) {
		this.max_hr = max_hr;
	}

	public void setSteps(String steps) {
		this.steps = steps;
	}

	public void setAvg_cadence(String avg_cadence) {
		this.avg_cadence = avg_cadence;
	}

	public void setMax_cadence(String max_cadence) {
		this.max_cadence = max_cadence;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getCategory_id() {
		return this.category_id;
	}

	public String getShoe_id() {
		return this.shoe_id;
	}

	public String getName() {
		return this.name;
	}

	public String getHiit_id() {
		return this.hiit_id;
	}

	public String getDate() {
		return this.date;
	}

	public String getHour() {
		return this.hour;
	}

	public String getTime() {
		return this.time;
	}

	public String getDistance() {
		return this.distance;
	}

	public String getAvg_speed() {
		return this.avg_speed;
	}

	public String getMax_speed() {
		return this.max_speed;
	}

	public String getKcal() {
		return this.kcal;
	}

	public String getUpAccum_altitude() {
		return this.up_accum_altitude;
	}

	public String getDown_accum_altitude() {
		return this.down_accum_altitude;
	}

	public String getMax_altitude() {
		return this.max_altitude;
	}

	public String getMin_altitude() {
		return this.min_altitude;
	}

	public String getAvg_hr() {
		return this.avg_hr;
	}

	public String getMax_hr() {
		return this.max_hr;
	}

	public String getSteps() {
		return this.steps;
	}

	public String getAvg_cadence() {
		return this.avg_cadence;
	}

	public String getMax_cadence() {
		return this.max_cadence;
	}

	public String getComments() {
		return this.comments;
	}
}