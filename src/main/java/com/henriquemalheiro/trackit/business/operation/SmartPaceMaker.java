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
package com.henriquemalheiro.trackit.business.operation;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;

class SmartPaceMaker implements PaceMaker {
	private static final double SPEED_SMOOTHING_FACTOR = 75.0;
	private static final double MINIMUM_SPEED = 1.11;
	private static Map<Long, Double> gradeSpeedMap;
	private Course course;
	
	SmartPaceMaker(Course course, Map<String, Object> options) {
		this.course = course;
	}

	@Override
	public void setPace() {
		long currentTimeMS = course.getFirstTrackpoint().getTimestamp().getTime();
		double[] trackpointsSpeed = calculateSpeed(course.getTrackpoints());
		double speed;
		double timeFromPrevious;
		int pos = 0;
		
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			speed = trackpointsSpeed[pos++];
			timeFromPrevious = trackpoint.getDistanceFromPrevious() / speed;
			currentTimeMS += (long) (timeFromPrevious * 1000);
			
			trackpoint.setTimeFromPrevious(timeFromPrevious);
			trackpoint.setTimestamp(new Date(currentTimeMS));
			trackpoint.setSpeed(speed);
		}
	}
	
	private double[] calculateSpeed(List<Trackpoint> trackpoints) {
		double[][] speed = calculateGradeBasedSpeed(trackpoints);
		AltitudeSmoothingOperation.smoothing(speed, SPEED_SMOOTHING_FACTOR, true);
		double[] smoothedSpeed = speedToSmoothedSpeed(speed);
		
		return smoothedSpeed;
	}

	private double[] speedToSmoothedSpeed(double[][] speed) {
		double[] smoothedSpeed = new double[speed.length];
		for (int i = 0; i < speed.length; i++) {
			smoothedSpeed[i] = speed[i][1];
		}
		return smoothedSpeed;
	}

	private double[][] calculateGradeBasedSpeed(List<Trackpoint> trackpoints) {
		double[][] speed = new double[trackpoints.size()][2];
		int pos = 0;
		for (Trackpoint trackpoint : trackpoints) {
			speed[pos][0] = trackpoint.getDistance();
			speed[pos][1] = calculateSpeed(trackpoint);
			pos++;
		}
		return speed;
	}

	private double calculateSpeed(Trackpoint trackpoint) {
		long grade = 0L;
		if (trackpoint.getGrade() != null) {
			grade = Long.valueOf(Math.round(trackpoint.getGrade()));
		}
		
		return getSpeed(grade);
	}

	private double getSpeed(long grade) {
		Double speed = gradeSpeedMap.get(grade);
		return (speed == null ? MINIMUM_SPEED : speed);
	}
	
	static {
		gradeSpeedMap = new HashMap<Long, Double>();
		
		gradeSpeedMap.put(-29L, 5.282000065);
		gradeSpeedMap.put(-28L, 5.182285718);
		gradeSpeedMap.put(-27L, 2.857222199);
		gradeSpeedMap.put(-26L, 3.874428613);
		gradeSpeedMap.put(-25L, 2.398099995);
		gradeSpeedMap.put(-24L, 3.478571432);
		gradeSpeedMap.put(-23L, 5.530549955);
		gradeSpeedMap.put(-22L, 3.399374987);
		gradeSpeedMap.put(-21L, 3.434159989);
		gradeSpeedMap.put(-20L, 3.251065226);
		gradeSpeedMap.put(-19L, 3.367437503);
		gradeSpeedMap.put(-18L, 3.639638472);
		gradeSpeedMap.put(-17L, 3.652546956);
		gradeSpeedMap.put(-16L, 4.661532164);
		gradeSpeedMap.put(-15L, 3.991882986);
		gradeSpeedMap.put(-14L, 4.052468609);
		gradeSpeedMap.put(-13L, 4.02509211);
		gradeSpeedMap.put(-12L, 4.655507004);
		gradeSpeedMap.put(-11L, 6.200765343);
		gradeSpeedMap.put(-10L, 7.876954348);
		gradeSpeedMap.put(-9L, 9.192436291);
		gradeSpeedMap.put(-8L, 8.927080966);
		gradeSpeedMap.put(-7L, 9.435431097);
		gradeSpeedMap.put(-6L, 9.567197385);
		gradeSpeedMap.put(-5L, 9.124867046);
		gradeSpeedMap.put(-4L, 8.458980184);
		gradeSpeedMap.put(-3L, 7.619727323);
		gradeSpeedMap.put(-2L, 6.882043392);
		gradeSpeedMap.put(-1L, 6.471149053);
		gradeSpeedMap.put(0L, 5.314490372);
		gradeSpeedMap.put(1L, 5.200722862);
		gradeSpeedMap.put(2L, 4.193061455);
		gradeSpeedMap.put(3L, 4.079951804);
		gradeSpeedMap.put(4L, 3.686417124);
		gradeSpeedMap.put(5L, 3.455468907);
		gradeSpeedMap.put(6L, 3.205506946);
		gradeSpeedMap.put(7L, 3.032039946);
		gradeSpeedMap.put(8L, 2.837058679);
		gradeSpeedMap.put(9L, 2.656698792);
		gradeSpeedMap.put(10L, 2.392672825);
		gradeSpeedMap.put(11L, 2.24660535);
		gradeSpeedMap.put(12L, 2.141515453);
		gradeSpeedMap.put(13L, 2.030786539);
		gradeSpeedMap.put(14L, 1.930603073);
		gradeSpeedMap.put(15L, 1.812958816);
		gradeSpeedMap.put(16L, 1.739184862);
		gradeSpeedMap.put(17L, 1.712963535);
		gradeSpeedMap.put(18L, 1.635741936);
		gradeSpeedMap.put(19L, 1.556904152);
		gradeSpeedMap.put(20L, 1.513682797);
		gradeSpeedMap.put(21L, 1.468742187);
		gradeSpeedMap.put(22L, 1.423617012);
		gradeSpeedMap.put(23L, 1.540249995);
		gradeSpeedMap.put(24L, 1.522206894);
		gradeSpeedMap.put(25L, 1.58405556);
		gradeSpeedMap.put(26L, 1.486399984);
	}
}
