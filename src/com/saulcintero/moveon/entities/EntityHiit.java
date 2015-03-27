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

public class EntityHiit {
	private String name, total_time, rounds, actions, preparation_time, cooldown_time, active;

	public EntityHiit() {
		name = "";
		total_time = "";
		rounds = "";
		actions = "";
		preparation_time = "";
		cooldown_time = "";
		active = "";
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTotal_time(String total_time) {
		this.total_time = total_time;
	}

	public void setRounds(String rounds) {
		this.rounds = rounds;
	}

	public void setActions(String actions) {
		this.actions = actions;
	}

	public void setPreparation_time(String preparation_time) {
		this.preparation_time = preparation_time;
	}

	public void setCoolDown_time(String cooldown_time) {
		this.cooldown_time = cooldown_time;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getName() {
		return this.name;
	}

	public String getTotal_time() {
		return this.total_time;
	}

	public String getRounds() {
		return this.rounds;
	}

	public String getActions() {
		return this.actions;
	}

	public String getPreparation_time() {
		return this.preparation_time;
	}

	public String getCooldown_time() {
		return this.cooldown_time;
	}

	public String getActive() {
		return this.active;
	}
}