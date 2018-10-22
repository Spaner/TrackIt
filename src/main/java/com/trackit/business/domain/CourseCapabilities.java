/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * 
 * TrackIt! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Track It! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Track It!. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.trackit.business.domain;

public class CourseCapabilities {
	public static final long PROCESSED = 0x00000001;
	public static final long VALID = 0x00000002;
	public static final long TIME = 0x00000004;
	public static final long DISTANCE = 0x00000008;
	public static final long POSITION = 0x00000010;
	public static final long HEART_RATE = 0x00000020;
	public static final long POWER = 0x00000040;
	public static final long CADENCE = 0x00000080;
	public static final long TRAINING = 0x00000100;
	public static final long NAVIGATION = 0x00000200;
}
