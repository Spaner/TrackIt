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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.common.SetPaceMethod;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class SettingPaceOperation extends OperationBase implements Operation {
	private SetPaceMethod method;
	private Course course;
	
	private Map<Lap, Pair<Trackpoint, Trackpoint>> lapData;

	public SettingPaceOperation() {
		super();
		options.put(Constants.SetPaceOperation.METHOD, SetPaceMethod.CONSTANT_TARGET_SPEED);
		options.put(Constants.SetPaceOperation.SPEED, 14.0);
		options.put(Constants.SetPaceOperation.INCLUDE_PAUSES, true);
		setUp();
	}
	
	public SettingPaceOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		method = (SetPaceMethod) options.get(Constants.SetPaceOperation.METHOD);
	}

	@Override
	public String getName() {
		return Constants.SetPaceOperation.NAME;
	}
	
	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for (GPSDocument document : documents) {
			process(document);
		}
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		for (Course course : document.getCourses()) {
			this.course = course;
			
			try {
				setPace();
				course.getLaps().clear();
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new TrackItException(e.getMessage(), e);
			}
		}
	}

	private void setPace() throws TrackItException {
		storeLapData();
		PaceMaker paceMaker = getPaceMaker();
		paceMaker.setPace();
		updateLapData();
	}
	
	private void storeLapData() {
		lapData = new HashMap<>();
		for (Lap lap : course.getLaps()) {
			lapData.put(lap, Pair.create(lap.getFirstTrackpoint(), lap.getLastTrackpoint()));
		}
	}

	private void updateLapData() {
		Pair<Trackpoint, Trackpoint> lapBoundaries;
		List<Trackpoint> lapTrackpoints;
		
		for (Lap lap : course.getLaps()) {
			lapBoundaries = lapData.get(lap);
			lapTrackpoints = course.getTrackpoints(
					lapBoundaries.getFirst().getTimestamp(), lapBoundaries.getSecond().getTimestamp());
			lap.setTrackpoints(lapTrackpoints);
		}
	}

	private PaceMaker getPaceMaker() {
		switch (method) {
		case TARGET_SPEED:
			return new SpeedPaceMaker(course, options);
		case CONSTANT_TARGET_SPEED:
			return new ConstantSpeedPaceMaker(course, options);
		case TARGET_TIME:
			return new TimePaceMaker(course, options);
		case TIME_PERCENTAGE:
			return new TimePercentagePaceMaker(course, options);
		case SPEED_PERCENTAGE:
			return new SpeedPercentagePaceMaker(course, options);
		case SMART_PACE:
			return new SmartPaceMaker(course, options);
		default:
			throw new UnsupportedOperationException(String.format("%s pacing method is not supported.", method.toString()));
		}
	}
}
