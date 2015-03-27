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

public enum ActivityTypes {
	WALKING(0), RUNNING(1), CLIMBING_STAIRS(2), SPORT_CYCLING(3), MOUNTAIN_BIKING(4), INDOOR_CYCLING(5), HIKING(
			6), SOCCER(7), BASKETBALL(8), HANDBALL(9), TENNIS(10), SKATEBOARDING(11), BASEBALL(12), DRIVE_CAR(
			13), DRIVE_MOTORBIKE(14), DRIVE_QUAD(15), MARTIAL_ARTS(16), BOXING(17), SQUASH(18), TREKKING(19), SKATING(
			20), ICE_SKATING(21), SKIING(22), SNOWBOARDING(23), CROSS_COUNTRY_SKI(24), ROWING(25), NAVIGATION(
			26), BILLIARDS(27), GOLFING(28), BOWLING(29), TABLE_TENNIS(30), HORSE_RIDING(31), SKIPPING(32), BOULES(
			33), VOLLEYBALL(34), PILATES(35), YOGA(36), RUGBY(37), HOCKEY(38), SWIMMING(39), SURFING(40), WINDSURFING(
			41), SCUBA_DIVING(42), CLIMBING(43), DANCING(44), HANG_GLIDING(45), ARCHERY(46), OTHERS(47);

	private int types;

	private ActivityTypes(int types) {
		this.setTypes(types);
	}

	public int getTypes() {
		return types;
	}

	public void setTypes(int types) {
		this.types = types;
	}
}