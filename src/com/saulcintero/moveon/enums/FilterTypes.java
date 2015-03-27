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

package com.saulcintero.moveon.enums;

public enum FilterTypes {
	ALL_DESC(0), BY_ACTIVITY(1), BY_DATE_THIS_YEAR(2), BY_DATE_THIS_MONTH(3), BY_DATE_THIS_WEEK(4), BY_DATE_CUSTOM_DATE(
			5), BY_ACTIVITY_AND_DATE_THIS_YEAR(6), BY_ACTIVITY_AND_DATE_THIS_MONTH(7), BY_ACTIVITY_AND_DATE_THIS_WEEK(
			8), BY_ACTIVITY_AND_DATE_CUSTOM_DATE(9);

	private int types;

	private FilterTypes(int types) {
		this.setTypes(types);
	}

	public int getTypes() {
		return types;
	}

	public void setTypes(int types) {
		this.types = types;
	}
}