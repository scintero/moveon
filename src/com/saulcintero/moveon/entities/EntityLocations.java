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

public class EntityLocations {
	private String latitude;
	private String longitude;
	private String altitude;
	private String distance;
	private String speed;
	private String time;
	private String steps;
	private String hr;
	private String cadence;
	private String pause;

	public EntityLocations() {
		latitude = "";
		longitude = "";
		altitude = "";
		distance = "";
		speed = "";
		time = "";
		steps = "";
		hr = "";
		cadence = "";
		pause = "";
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setSteps(String steps) {
		this.steps = steps;
	}

	public void setHr(String hr) {
		this.hr = hr;
	}

	public void setCadence(String cadence) {
		this.cadence = cadence;
	}

	public void setPause(String pause) {
		this.pause = pause;
	}

	public String getLatitude() {
		return this.latitude;
	}

	public String getLongitude() {
		return this.longitude;
	}

	public String getAltitude() {
		return this.altitude;
	}

	public String getDistance() {
		return this.distance;
	}

	public String getSpeed() {
		return this.speed;
	}

	public String getTime() {
		return this.time;
	}

	public String getSteps() {
		return this.steps;
	}

	public String getHr() {
		return this.hr;
	}

	public String getCadence() {
		return this.cadence;
	}

	public String getPause() {
		return this.pause;
	}
}