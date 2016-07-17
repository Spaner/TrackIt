/*
 * This file is part of Track It!.
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
package com.pg58406.trackit.business.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.Operation;
import com.henriquemalheiro.trackit.business.operation.OperationBase;
import com.pg58406.trackit.business.domain.Pause;

public class PauseDetectionPicCaseOperation extends OperationBase implements Operation{

	public PauseDetectionPicCaseOperation() {
		super();
	}

	public PauseDetectionPicCaseOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
	}

	private void setup(List<Trackpoint> trkpoints) {
		// detector = new DetPausesOpDistanceDescAVG(trkpoints);
		detector = new DetPausesNewVersionOperation(trkpoints);
	}

	private DetPausesNewVersionOperation detector;

	// private DetectPausesInterface detector;

	@Override
	public String getName() {
		return Constants.PauseDetectionOperation.NAME;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		if (processActivities) {
			for (Activity activity : document.getActivities()) {
				DetectPauses(activity);
			}
		}

		if (processCourses) {
			for (Course course : document.getCourses()) {
				DetectPauses(course);
			}
		}
	}

	public void process(DocumentItem item) {
		DetectPauses(item);
	}

	@Override
	public void process(List<GPSDocument> documents) throws TrackItException {
		for (GPSDocument document : documents) {
			try {
				process(document);
			} catch (RuntimeException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	public void DetectPauses(DocumentItem item) {
		/*double limit = TrackIt.getPreferences().getDoublePreference(
				Constants.PrefsCategories.PAUSE, null,
				Constants.PausePreferences.SPEED_THRESHOLD, 1.5);*/
		double limit = getPauseThreshold(item);
		
		limit = limit / 3.6;
		List<Trackpoint> trackpoints = item.getTrackpoints();
		setup(trackpoints);
		int trackLength = trackpoints.size();
		List<Pause> pauses = new ArrayList<Pause>();
		boolean b = false;
		b = detector.DetectPauses(1, trackLength - 1, 1.0, limit, pauses);
		
		if (b) {
			if (item instanceof Activity) {
				Activity a = (Activity) item;

				for (Session s : a.getSessions()) {
					for (Lap l : s.getLaps()) {
						l.getPauses().clear();
					}
				}

				for (Pause p : pauses) {
					for (Session s : a.getSessions()) {
						for (Lap l : s.getLaps()) {
							if ((p.getStart().after(l.getStartTime()) || 
									p.getStart().equals(l.getStartTime()))
									&& (p.getEnd().after(l.getEndTime()) || 
											p.getEnd().equals(l.getEndTime()))) {
								l.addPause(p);
							}
						}
					}
				}
				a.setPauses(pauses);				
				a.publishSelectionEvent(null);
			}
			if (item instanceof Course) {
				Course c = (Course) item;
				c.setPauses(pauses);				
			}			
		}
	}

	private double getPauseThreshold(DocumentItem item) {
		double limit = 0.0;
		List<SportType> sportList = Arrays.asList(SportType.values());
		List<SubSportType> subSportList = Arrays.asList(SubSportType.values());
		short sportID = -1;
		short subSportID = -1;
		SportType sport = SportType.INVALID;
		SubSportType subSport = SubSportType.INVALID_SUB;
		if (item instanceof Activity) {
			Activity activity = (Activity) item;
			if(activity.getSport()==null || activity.getSport()==SportType.INVALID){
				sportID = DocumentManager.getInstance().getDatabase().getSport(activity);
				subSportID = DocumentManager.getInstance().getDatabase().getSubSport(activity);
			}
			else{
				sportID = activity.getSport().getSportID();
				subSportID = activity.getSubSport().getSubSportID();
			}
		} else if (item instanceof Course) {
			Course course = (Course) item;
			if(course.getSport()==null || course.getSport()==SportType.INVALID){
				sportID = DocumentManager.getInstance().getDatabase().getSport(course);
				subSportID = DocumentManager.getInstance().getDatabase().getSubSport(course);
			}
			else{
				sportID = course.getSport().getSportID();
				subSportID = course.getSubSport().getSubSportID();
			}
		}
		for (SportType sportIt : sportList) {
			if (sportIt.getSportID() == sportID) {
				sport = sportIt;
				break;
			}
		}
		for (SubSportType subSportIt : subSportList) {
			if (subSportIt.getSubSportID() == subSportID) {
				subSport = subSportIt;
				break;
			}
		}

		limit = DocumentManager.getInstance().getDatabase().getPauseThresholdSpeed(sport, subSport, false);
		//double limit = 3.6;
		return limit;
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
