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
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;

public class SmoothingOperation extends OperationBase implements Operation {
	private static final double DEFAULT_TAU = 100.0;
	private double tau;

	public SmoothingOperation() {
		super();
		setUp();
	}

	public SmoothingOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		Double factor = (Double) options.get(Constants.SmoothingOperation.FACTOR);
		tau = factor != null ? factor : DEFAULT_TAU;
	}

	@Override
	public String getName() {
		return Constants.SmoothingOperation.NAME;
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		// Do nothing
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		for (Course course : document.getCourses()) {
			smoothAltitude(course.getTrackpoints());
		}
	}

	private void smoothAltitude(List<Trackpoint> trackpoints) {
		final int size = trackpoints.size();

		double[] time = new double[size];
		double[] distance = new double[size];
		double[] altitude = new double[size];

		Trackpoint trackpoint = null;
		double duration = 0.0;
		for (int i = 0; i < size; i++) {
			trackpoint = trackpoints.get(i);
			duration += trackpoint.getTimeFromPrevious();

			time[i] = duration;
			distance[i] = trackpoint.getDistance();
			altitude[i] = trackpoint.getAltitude();
		}

		double[] smoothedDistance = smoothing(time, distance, tau, true);
		double[] smoothedAltitude = smoothing(time, altitude, tau, true);

		double[] horizontalSpeed = new double[size];
		double[] verticalSpeed = new double[size];

		for (int i = 0; i < size - 1; i++) {
			if (time[i + 1] == time[i]) {
				if (i == 0) {
					horizontalSpeed[i] = 0.0;
					verticalSpeed[i] = 0.0;
				} else {
					horizontalSpeed[i] = horizontalSpeed[i - 1];
					verticalSpeed[i] = verticalSpeed[i - 1];
				}
			} else {
				horizontalSpeed[i] = (smoothedDistance[i + 1] - smoothedDistance[i]) / (time[i + 1] - time[i]);
				verticalSpeed[i] = (smoothedAltitude[i + 1] - smoothedAltitude[i]) / (time[i + 1] - time[i]);
			}
		}
		horizontalSpeed[size - 1] = horizontalSpeed[size - 2];
		verticalSpeed[size - 1] = verticalSpeed[size - 2];

		double[] smoothedHorizontalSpeed = smoothing(time, horizontalSpeed, tau, true);
		double[] smoothedVerticalSpeed = smoothing(time, verticalSpeed, tau, true);

		double grade;
		for (int i = 0; i < size; i++) {
			trackpoint = trackpoints.get(i);
			grade = (smoothedVerticalSpeed[i] / smoothedHorizontalSpeed[i] * 100.0);
			trackpoint.setGrade((float) grade);
		}
	}

	private double[] smoothing(double[] time, double[] values, double tau, boolean keepStartEnd) {
		int size = values.length;
		double[] smoothedValues = new double[size];
		double deltaStart = 0.0;
		double deltaEnd = 0.0;

		smoothedValues = smoothingAux(time, values, tau / 12);
		smoothedValues = smoothingAux(time, smoothedValues, tau / 24);

		if (keepStartEnd) {
			deltaStart = values[0] - smoothedValues[0];
			deltaEnd = values[size - 1] - smoothedValues[size - 1];

			for (int i = 0; i < size; i++) {
				smoothedValues[i] = smoothedValues[i] + deltaStart + (deltaEnd - deltaStart) / (double) (size - 1)
				        * (double) (i);
			}
		}

		return smoothedValues;
	}

	private double[] smoothingAux(double[] time, double[] values, double tau) {
		int size = values.length;
		double[] forwardSmoothedValues = new double[size];
		double[] backwardSmoothedValues = new double[size];
		double[] smoothedValues = new double[size];
		double deltaTime = 0.0;

		/* Forward smoothing */
		forwardSmoothedValues[0] = values[0];
		for (int i = 1; i < size; i++) {
			deltaTime = (time[i] - time[i - 1]) / tau;
			forwardSmoothedValues[i] = (forwardSmoothedValues[i - 1] + deltaTime * values[i]) / (1.0 + deltaTime);
		}

		/* Backward smoothing */
		backwardSmoothedValues[size - 1] = values[size - 1];
		for (int i = 2; i < size + 1; i++) {
			deltaTime = (time[size - i + 1] - time[size - i]) / tau;
			backwardSmoothedValues[size - i] = (backwardSmoothedValues[size - i + 1] + deltaTime * values[size - i])
			        / (1.0 + deltaTime);
		}

		/* Centered smoothing */
		for (int i = 0; i < size; i++) {
			smoothedValues[i] = (forwardSmoothedValues[i] + backwardSmoothedValues[i]) / 2.0;
		}

		return smoothedValues;
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
