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

public class EntityShoe {
	private String name;
	private String distance;
	private String default_shoe;
	private String active;

	public EntityShoe() {
		name = "";
		distance = "";
		default_shoe = "";
		active = "";
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public void setDefault_shoe(String default_shoe) {
		this.default_shoe = default_shoe;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getName() {
		return this.name;
	}

	public String getDistance() {
		return this.distance;
	}

	public String getDefault_shoe() {
		return this.default_shoe;
	}

	public String getActive() {
		return this.active;
	}
}