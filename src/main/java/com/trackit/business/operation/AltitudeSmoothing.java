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
package com.trackit.business.operation;

import java.util.List;
import java.util.Map;

import com.trackit.business.common.Constants;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;

public class AltitudeSmoothing extends OperationBase implements Operation {
	
	public AltitudeSmoothing() {
		super();
		setUp();
	}
	
	public AltitudeSmoothing(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
	}

	@Override
	public String getName() {
		return Constants.AltitudeSmoothingOperation.NAME;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		if (processActivities) {
			for (Activity activity : document.getActivities()) {
				smoothAltitude(activity.getTrackpoints());
			}
		}
		
		if (processCourses) {
			for (Course course : document.getCourses()) {
				smoothAltitude(course.getTrackpoints());
			}
		}
	}

	@Override
	public void process(List<GPSDocument> document) throws TrackItException {
	}
	
	private void smoothAltitude(List<Trackpoint> trackpoints) {
		for (int i = 1; i < trackpoints.size() - 1; i++) {
			int j = i;
			
			while (j < trackpoints.size()
					&& trackpoints.get(j).getAltitude().equals(trackpoints.get(i).getAltitude())) {
				j++;
			}
			
			if (j > i && j < trackpoints.size()) {
				double delta = (trackpoints.get(j).getAltitude() - trackpoints.get(i - 1).getAltitude()) / (j - i + 1);
				for (int k = i; k < j; k++) {
					trackpoints.get(k).setAltitude(trackpoints.get(k - 1).getAltitude() + delta);
				}

				i = j;
			}
		}
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void redoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub
		
	}
}
