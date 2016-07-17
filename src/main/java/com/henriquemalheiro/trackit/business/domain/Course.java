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
package com.henriquemalheiro.trackit.business.domain;

import java.awt.Graphics2D;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.utility.decoratedlist.EventList;
import com.henriquemalheiro.trackit.business.utility.decoratedlist.EventListener;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.task.ActionType;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.data.DataType;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayer;
import com.henriquemalheiro.trackit.presentation.view.map.layer.MapLayerType;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainter;
import com.henriquemalheiro.trackit.presentation.view.map.painter.MapPainterFactory;
import com.jb12335.trackit.business.utilities.ChangesSemaphore;
import com.pg58406.trackit.business.common.ColorSchemeV2;
import com.pg58406.trackit.business.common.ColorSchemeV2Container;
import com.pg58406.trackit.business.db.Database;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.domain.PhotoContainer;
import com.pg58406.trackit.business.domain.Picture;
import com.pg58406.trackit.business.operation.PauseDetectionPicCaseOperation;
import com.pg58406.trackit.business.utility.PictureComparator;

public class Course extends TrackItBaseType implements DocumentItem,
		FolderTreeItem, PhotoContainer, ColorSchemeV2Container {
	private static ImageIcon icon = ImageUtilities
			.createImageIcon("mountain_biking_16.png");

	private String name;
	private String oldName ="";						// 12335: 2015-08-09
	private List<Lap> laps;
	private List<Track> tracks;
	private EventList<Trackpoint> trackpoints;
	private EventList<CoursePoint> coursePoints;
	private List<TrackSegment> segments;
	private List<Event> events;
	private String notes;
	private Double elapsedTime;
	private Double timerTime;
	private Double movingTime;
	private Double pausedTime;
	private Double distance;
	private Double averageSpeed;
	private Double averageMovingSpeed;
	private Double maximumSpeed;
	private Short averageHeartRate;
	private Short minimumHeartRate;
	private Short maximumHeartRate;
	private Short averageCadence;
	private Short maximumCadence;
	private Short averageRunningCadence;
	private Short maximumRunningCadence;
	private Integer averagePower;
	private Integer maximumPower;
	private Integer calories;
	private Integer fatCalories;
	private Byte averageTemperature;
	private Byte minimumTemperature;
	private Byte maximumTemperature;
	private Integer totalAscent;
	private Integer totalDescent;
	private Float averageAltitude;
	private Float minimumAltitude;
	private Float maximumAltitude;
	private Float averageGrade;
	private Float averagePositiveGrade;
	private Float averageNegativeGrade;
	private Float maximumPositiveGrade;
	private Float maximumNegativeGrade;
	private Float averagePositiveVerticalSpeed;
	private Float averageNegativeVerticalSpeed;
	private Float maximumPositiveVerticalSpeed;
	private Float maximumNegativeVerticalSpeed;
	private Double startLatitude;
	private Double startLongitude;
	private Double startAltitude;
	private Double endLatitude;
	private Double endLongitude;
	private Double endAltitude;
	private Double northeastLatitude;
	private Double northeastLongitude;
	private Double southwestLatitude;
	private Double southwestLongitude;
	private GPSDocument parent;
	private boolean unsavedChanges;					// 58406
	private boolean synchronizeWithDB;				// 12335: 2016-06-16
	private String filepath;						// 58406
	private List<Pause> pauses;						// 58406
	private List<Picture> pictures;					// 58406
	private String creator;							// 58406
	private ColorSchemeV2 colorScheme;				// 58406
	private Boolean noSpeedInFile;					// 58406
	private Date startTime;
	private Date endTime;
	private SportType sport; 						//57421
	private SubSportType subSport; 					//57421
//	private SportType temporarySport;
//	private SubSportType temporarySubSport;
	
	//only used for "segments"
	private Double segmentMovingTime;
	private Double segmentDistance;
	private long parentCourseId;
	private int segmentNumber;
	
	private String filename;

	//12335: 2015-08-10 Former Course() was split into Course() and initialize() to make room
	//                  for Course(String name) so that we may control renaming
	public Course() {
		super();

		name = String.format("%s %d", Messages.getMessage("course.label"),
				getId());
		initialize();								// 12335: 2015-08-10
//		laps = new ArrayList<Lap>();
//		tracks = new ArrayList<Track>();
//		trackpoints = new EventList<Trackpoint>(new ArrayList<Trackpoint>());
//		coursePoints = new EventList<CoursePoint>(new ArrayList<CoursePoint>());
//		segments = new ArrayList<TrackSegment>();
//		events = new ArrayList<Event>();
//		unsavedChanges = false;// 58406
//		//filepath = null;// 58406
//		filepath = new String();// 57421
//		filename = new String();// 57421
//		pauses = new ArrayList<Pause>();// 58406
//		pictures = new ArrayList<Picture>();// 58406
//		colorScheme = TrackIt.getDefaultColorScheme();// 58406
//		noSpeedInFile = true;//58406
//		segmentNumber = 1;
//		//filename = null;
	}
	
	public Course(String name) {
		super();
		
		this.name = name;
		
		initialize();
	}
	
	private void initialize() {

		laps = new ArrayList<Lap>();
		tracks = new ArrayList<Track>();
		trackpoints = new EventList<Trackpoint>(new ArrayList<Trackpoint>());
		coursePoints = new EventList<CoursePoint>(new ArrayList<CoursePoint>());
		segments = new ArrayList<TrackSegment>();
		events = new ArrayList<Event>();
		unsavedChanges = false;// 58406
		synchronizeWithDB = false;			// 12335: 2016-06-16
		filepath = null;// 58406
		pauses = new ArrayList<Pause>();// 58406
		pictures = new ArrayList<Picture>();// 58406
		colorScheme = TrackIt.getDefaultColorScheme();// 58406
		noSpeedInFile = true;//58406
		
		filename = null;
	}
	// 12335: 2015-08-10 end
	
	//12335: 2016-06-16
	public boolean needsSynchronizationWithDB() {
		return synchronizeWithDB;
	}
	

	// 58406###################################################################################

	public boolean getUnsavedChanges() {
		return unsavedChanges;
	}

	public boolean setUnsavedChanges(boolean changes) {
		if ( ChangesSemaphore.IsEnabled() )
			unsavedChanges = changes;
		System.out.println("\nsetUnsavedChanges " + unsavedChanges + " for " + this.name);
		return unsavedChanges;
	}

	public void setUnsavedTrue() {
		setUnsavedChanges(true);
	}

	public void setUnsavedFalse() {
		setUnsavedChanges(false);
	}

	public void setFilepath(String fullFilepath) {
		this.filepath = fullFilepath;
		this.filename = getFilenameFromPath(fullFilepath);
	}
	
	private String getFilenameFromPath(String fullFilepath){
		String name;
		Path path = Paths.get(fullFilepath);
		name = path.getFileName().toString();
		return name;
	}

	public String getFilepath() {
		return filepath;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setPauses(List<Pause> pauses) {
		this.pauses = pauses;
	}

	public List<Pause> getPauses() {
		return pauses;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreator() {
		return creator;
	}

	// ########################################################################################

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
//		setUnsavedTrue();// 58406
	}
	
	//12335: 2015-09-21 - support for renaming operation
	public void rename(String name) {
		if ( oldName.isEmpty() )
			oldName = this.name;
		setName(name);
		System.out.println("Renaming Course from " + oldName + " to " + name);
	}
	
	public void setOldNameWhenSaving() {
		oldName = name;
	}
	
	public String getOldName() {
		return oldName;
	}
	
	public boolean wasRenamed() {
		if ( !oldName.isEmpty() && oldName != name )
			return true;
		return false;
	}
	
	public void seTProvisionalOdNameWhenSaving( String provName) {
		oldName = provName;
	}
	// 12335: 2015-09-21 end

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Double getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(Double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Double getTimerTime() {
		return timerTime;
	}

	public void setTimerTime(Double timerTime) {
		this.timerTime = timerTime;
	}

	public Double getMovingTime() {
		return movingTime;
	}

	public void setMovingTime(Double movingTime) {
		this.movingTime = movingTime;
	}

	public Double getPausedTime() {
		return pausedTime;
	}

	public void setPausedTime(Double pausedTime) {
		this.pausedTime = pausedTime;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getAverageSpeed() {
		return averageSpeed;
	}

	public void setAverageSpeed(Double averageSpeed) {
		this.averageSpeed = averageSpeed;
	}

	public Double getAverageMovingSpeed() {
		return averageMovingSpeed;
	}

	public void setAverageMovingSpeed(Double averageMovingSpeed) {
		this.averageMovingSpeed = averageMovingSpeed;
	}

	public Double getMaximumSpeed() {
		return maximumSpeed;
	}

	public void setMaximumSpeed(Double maximumSpeed) {
		this.maximumSpeed = maximumSpeed;
	}

	public Short getAverageHeartRate() {
		return averageHeartRate;
	}

	public void setAverageHeartRate(Short averageHeartRate) {
		this.averageHeartRate = averageHeartRate;
	}

	public Short getMinimumHeartRate() {
		return minimumHeartRate;
	}

	public void setMinimumHeartRate(Short minimumHeartRate) {
		this.minimumHeartRate = minimumHeartRate;
	}

	public Short getMaximumHeartRate() {
		return maximumHeartRate;
	}

	public void setMaximumHeartRate(Short maximumHeartRate) {
		this.maximumHeartRate = maximumHeartRate;
	}

	public Short getAverageCadence() {
		return averageCadence;
	}

	public void setAverageCadence(Short averageCadence) {
		this.averageCadence = averageCadence;
	}

	public Short getMaximumCadence() {
		return maximumCadence;
	}

	public void setMaximumCadence(Short maximumCadence) {
		this.maximumCadence = maximumCadence;
	}

	public Short getAverageRunningCadence() {
		return averageRunningCadence;
	}

	public void setAverageRunningCadence(Short averageRunningCadence) {
		this.averageRunningCadence = averageRunningCadence;
	}

	public Short getMaximumRunningCadence() {
		return maximumRunningCadence;
	}

	public void setMaximumRunningCadence(Short maximumRunningCadence) {
		this.maximumRunningCadence = maximumRunningCadence;
	}

	public Integer getAveragePower() {
		return averagePower;
	}

	public void setAveragePower(Integer averagePower) {
		this.averagePower = averagePower;
	}

	public Integer getMaximumPower() {
		return maximumPower;
	}

	public void setMaximumPower(Integer maximumPower) {
		this.maximumPower = maximumPower;
	}

	public Integer getCalories() {
		return calories;
	}

	public void setCalories(Integer calories) {
		this.calories = calories;
	}

	public Integer getFatCalories() {
		return fatCalories;
	}

	public void setFatCalories(Integer fatCalories) {
		this.fatCalories = fatCalories;
	}

	public Byte getAverageTemperature() {
		return averageTemperature;
	}

	public void setAverageTemperature(Byte averageTemperature) {
		this.averageTemperature = averageTemperature;
	}

	public Byte getMinimumTemperature() {
		return minimumTemperature;
	}

	public void setMinimumTemperature(Byte minimumTemperature) {
		this.minimumTemperature = minimumTemperature;
	}

	public Byte getMaximumTemperature() {
		return maximumTemperature;
	}

	public void setMaximumTemperature(Byte maximumTemperature) {
		this.maximumTemperature = maximumTemperature;
	}

	public Integer getTotalAscent() {
		return totalAscent;
	}

	public void setTotalAscent(Integer totalAscent) {
		this.totalAscent = totalAscent;
	}

	public Integer getTotalDescent() {
		return totalDescent;
	}

	public void setTotalDescent(Integer totalDescent) {
		this.totalDescent = totalDescent;
	}

	public Float getAverageAltitude() {
		return averageAltitude;
	}

	public void setAverageAltitude(Float averageAltitude) {
		this.averageAltitude = averageAltitude;
	}

	public Float getMinimumAltitude() {
		return minimumAltitude;
	}

	public void setMinimumAltitude(Float minimumAltitude) {
		this.minimumAltitude = minimumAltitude;
	}

	public Float getMaximumAltitude() {
		return maximumAltitude;
	}

	public void setMaximumAltitude(Float maximumAltitude) {
		this.maximumAltitude = maximumAltitude;
	}

	public Float getAverageGrade() {
		return averageGrade;
	}

	public void setAverageGrade(Float averageGrade) {
		this.averageGrade = averageGrade;
	}

	public Float getAveragePositiveGrade() {
		return averagePositiveGrade;
	}

	public void setAveragePositiveGrade(Float averagePositiveGrade) {
		this.averagePositiveGrade = averagePositiveGrade;
	}

	public Float getAverageNegativeGrade() {
		return averageNegativeGrade;
	}

	public void setAverageNegativeGrade(Float averageNegativeGrade) {
		this.averageNegativeGrade = averageNegativeGrade;
	}

	public Float getMaximumPositiveGrade() {
		return maximumPositiveGrade;
	}

	public void setMaximumPositiveGrade(Float maximumPositiveGrade) {
		this.maximumPositiveGrade = maximumPositiveGrade;
	}

	public Float getMaximumNegativeGrade() {
		return maximumNegativeGrade;
	}

	public void setMaximumNegativeGrade(Float maximumNegativeGrade) {
		this.maximumNegativeGrade = maximumNegativeGrade;
	}

	public Float getAveragePositiveVerticalSpeed() {
		return averagePositiveVerticalSpeed;
	}

	public void setAveragePositiveVerticalSpeed(
			Float averagePositiveVerticalSpeed) {
		this.averagePositiveVerticalSpeed = averagePositiveVerticalSpeed;
	}

	public Float getAverageNegativeVerticalSpeed() {
		return averageNegativeVerticalSpeed;
	}

	public void setAverageNegativeVerticalSpeed(
			Float averageNegativeVerticalSpeed) {
		this.averageNegativeVerticalSpeed = averageNegativeVerticalSpeed;
	}

	public Float getMaximumPositiveVerticalSpeed() {
		return maximumPositiveVerticalSpeed;
	}

	public void setMaximumPositiveVerticalSpeed(
			Float maximumPositiveVerticalSpeed) {
		this.maximumPositiveVerticalSpeed = maximumPositiveVerticalSpeed;
	}

	public Float getMaximumNegativeVerticalSpeed() {
		return maximumNegativeVerticalSpeed;
	}

	public void setMaximumNegativeVerticalSpeed(
			Float maximumNegativeVerticalSpeed) {
		this.maximumNegativeVerticalSpeed = maximumNegativeVerticalSpeed;
	}

	public Double getStartLatitude() {
		return startLatitude;
	}

	public void setStartLatitude(Double startLatitude) {
		this.startLatitude = startLatitude;
	}

	public Double getStartLongitude() {
		return startLongitude;
	}

	public void setStartLongitude(Double startLongitude) {
		this.startLongitude = startLongitude;
	}

	public Double getStartAltitude() {
		return startAltitude;
	}

	public void setStartAltitude(Double startAltitude) {
		this.startAltitude = startAltitude;
	}

	public Double getEndLatitude() {
		return endLatitude;
	}

	public void setEndLatitude(Double endLatitude) {
		this.endLatitude = endLatitude;
	}

	public Double getEndLongitude() {
		return endLongitude;
	}

	public void setEndLongitude(Double endLongitude) {
		this.endLongitude = endLongitude;
	}

	public Double getEndAltitude() {
		return endAltitude;
	}

	public void setEndAltitude(Double endAltitude) {
		this.endAltitude = endAltitude;
	}

	public Double getNortheastLatitude() {
		return northeastLatitude;
	}

	public void setNortheastLatitude(Double northeastLatitude) {
		this.northeastLatitude = northeastLatitude;
	}

	public Double getNortheastLongitude() {
		return northeastLongitude;
	}

	public void setNortheastLongitude(Double northeastLongitude) {
		this.northeastLongitude = northeastLongitude;
	}

	public Double getSouthwestLatitude() {
		return southwestLatitude;
	}

	public void setSouthwestLatitude(Double southwestLatitude) {
		this.southwestLatitude = southwestLatitude;
	}

	public Double getSouthwestLongitude() {
		return southwestLongitude;
	}

	public void setSouthwestLongitude(Double southwestLongitude) {
		this.southwestLongitude = southwestLongitude;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public List<Lap> getLaps() {
		return laps;
	}

	public List<Lap> getLaps(Date startTime, Date endTime) {
		List<Lap> laps = getLaps();

		if (laps == null || laps.isEmpty()) {
			return laps;
		}

		int startLapIndex = 0;
		while (laps.get(startLapIndex).getStartTime().compareTo(startTime) < 0) {
			startLapIndex++;
		}

		int endLapIndex = laps.size() - 1;
		while (laps.get(endLapIndex).getEndTime().compareTo(endTime) > 0) {
			endLapIndex--;
		}

		return laps.subList(startLapIndex, endLapIndex + 1);
	}

	public void setLaps(List<Lap> laps) {
		this.laps = laps;
	}

	public Lap getLap(Lap lap) {
		if (lap == null) {
			return lap;
		}

		for (Lap currentLap : getLaps()) {
			if (lap.equals(currentLap)) {
				return currentLap;
			}
		}

		return null;
	}

	public Lap getLastLap() {
		Lap lap = null;
		if (!laps.isEmpty()) {
			return laps.get(laps.size() - 1);
		}
		return lap;
	}

	public void add(Lap lap) {
		getLaps().add(lap);
		setUnsavedTrue();// 58406
	}

	public void remove(Lap lap) {
		getLaps().remove(lap);
		setUnsavedTrue();// 58406
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public List<Track> getTracks(Date startTime, Date endTime) {
		return new ArrayList<Track>();
	}

	public List<Track> getTracks(Track firstTrack, Track lastTrack) {
		if (firstTrack == null || lastTrack == null) {
			return new ArrayList<Track>();
		}

		List<Track> tracks = getTracks();
		if (tracks == null || tracks.isEmpty()) {
			return new ArrayList<Track>();
		}

		int firstTrackIndex = tracks.indexOf(firstTrack);
		int lastTrackIndex = tracks.indexOf(lastTrack);

		if (firstTrackIndex == -1 || lastTrackIndex == -1
				|| lastTrackIndex < firstTrackIndex) {
			return new ArrayList<Track>();
		}

		return tracks.subList(firstTrackIndex, lastTrackIndex + 1);
	}

	public Track getTrack(Track track) {
		if (track == null) {
			return track;
		}

		for (Track currentTrack : getTracks()) {
			if (track.equals(currentTrack)) {
				return currentTrack;
			}
		}

		return null;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
		for (Track currentTrack : this.tracks) {
			((EventList<Trackpoint>) getTrackpoints())
					.addListener(currentTrack);
		}
	}

	public void add(Track track) {
		getTracks().add(track);
		((EventList<Trackpoint>) getTrackpoints()).addListener(track);
		setUnsavedTrue();// 58406
	}

	public void remove(Track track) {
		getTracks().remove(track);
		((EventList<Trackpoint>) getTrackpoints()).removeListener(track);
		setUnsavedTrue();// 58406
	}

	@Override
	public List<TrackSegment> getSegments() {
		return segments;
	}

	public TrackSegment getSegment(TrackSegment segment) {
		if (segment == null) {
			return segment;
		}

		for (TrackSegment currentSegment : getSegments()) {
			if (segment.equals(currentSegment)) {
				return currentSegment;
			}
		}

		return null;
	}

	public void setSegments(List<TrackSegment> segments) {
		this.segments = segments;
	}

	public void add(TrackSegment segment) {
		getSegments().add(segment);
		setUnsavedTrue();// 58406
	}

	public void remove(TrackSegment segment) {
		getSegments().remove(segment);
		setUnsavedTrue();// 58406
	}

	public List<Event> getEvents() {
		return events;
	}

	public Event getEvent(Event event) {
		if (event == null) {
			return event;
		}

		for (Event currentEvent : getEvents()) {
			if (event.equals(currentEvent)) {
				return currentEvent;
			}
		}

		return null;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public void add(Event event) {
		getEvents().add(event);
	}

	public void remove(Event event) {
		getEvents().remove(event);
	}

	public List<DeviceInfo> getDevices() {
		return new ArrayList<DeviceInfo>();
	}

	public List<Trackpoint> getTrackpoints() {
		return trackpoints;
	}

	public List<Trackpoint> getTrackpoints(Trackpoint firstTrackpoint,
			Trackpoint lastTrackpoint) {
		if (firstTrackpoint == null || lastTrackpoint == null) {
			return new ArrayList<Trackpoint>();
		}

		List<Trackpoint> trackpoints = getTrackpoints();
		if (trackpoints == null || trackpoints.isEmpty()) {
			return new ArrayList<Trackpoint>();
		}

		int firstTrackpointIndex = trackpoints.indexOf(firstTrackpoint);
		int lastTrackpointIndex = trackpoints.indexOf(lastTrackpoint);

		if (firstTrackpointIndex == -1 || lastTrackpointIndex == -1
				|| lastTrackpointIndex < firstTrackpointIndex) {
			return new ArrayList<Trackpoint>();
		}

		return trackpoints.subList(firstTrackpointIndex,
				lastTrackpointIndex + 1);
	}

	public List<Trackpoint> getTrackpoints(Date startTime, Date endTime) {
		int startIndex = -1;
		int endIndex = -1;
		Trackpoint trackpoint = null;
		for (int i = 0; i < trackpoints.size(); i++) {
			trackpoint = trackpoints.get(i);

			if (trackpoint.getTimestamp().equals(startTime)) {
				startIndex = i;
			} else if (trackpoint.getTimestamp().after(startTime)
					&& startIndex == -1) {
				startIndex = (i > 0 ? i - 1 : i);
				logger.debug("Lap start time does not match trackpoint timestamp.");
			}

			if (trackpoint.getTimestamp().equals(endTime)) {
				endIndex = i + 1;
				break;
			} else if (trackpoint.getTimestamp().after(endTime)
					&& endIndex == -1) {
				logger.debug("Lap end time does not match trackpoint timestamp.");
				break;
			}
		}

		if (startIndex == -1) {
			startIndex = 0;
			logger.debug("Invalid start time for lap!");
		}

		if (endIndex == -1) {
			endIndex = trackpoints.size();
			logger.debug("Invalid end time for lap!");
		}

		return trackpoints.subList(startIndex, endIndex);
	}

	public Trackpoint getTrackpoint(Trackpoint trackpoint) {
		if (trackpoint == null) {
			return trackpoint;
		}

		for (Trackpoint currentTrackpoint : getTrackpoints()) {
			if (trackpoint.equals(currentTrackpoint)) {
				return currentTrackpoint;
			}
		}

		return null;
	}

	public Trackpoint getFirstTrackpoint() {
		if (trackpoints == null) {
			return null;
		}

		return trackpoints.get(0);
	}

	public Trackpoint getLastTrackpoint() {
		if (trackpoints == null) {
			return null;
		}

		return trackpoints.get(trackpoints.size() - 1);
	}

	public void setTrackpoints(List<Trackpoint> trackpoints) {
		List<EventListener<Trackpoint>> listeners = this.trackpoints
				.getListeners();

		Iterator<EventListener<Trackpoint>> listenerIterator = listeners
				.iterator();
		while (listenerIterator.hasNext()) {
			listenerIterator.next();
			listenerIterator.remove();
		}

		this.trackpoints = new EventList<Trackpoint>(new ArrayList<Trackpoint>(
				trackpoints));
	}

	public void add(Trackpoint trackpoint) {
		getTrackpoints().add(trackpoint);
		setUnsavedTrue();// 58406
	}

	public void addTrackpoints(List<Trackpoint> trackpoints) {
		getTrackpoints().addAll(trackpoints);
		setUnsavedTrue();// 58406
	}

	public void remove(Trackpoint trackpoint) {
		getTrackpoints().remove(trackpoint);
		setUnsavedTrue();// 58406
	}

	public List<CoursePoint> getCoursePoints() {
		return coursePoints;
	}

	public CoursePoint getCoursePoint(CoursePoint coursePoint) {
		if (coursePoint == null) {
			return coursePoint;
		}

		for (CoursePoint currentCoursePoint : getCoursePoints()) {
			if (coursePoint.equals(currentCoursePoint)) {
				return currentCoursePoint;
			}
		}

		return null;
	}

	public void setCoursePoints(List<CoursePoint> coursePoints) {
		List<EventListener<CoursePoint>> listeners = this.coursePoints
				.getListeners();

		Iterator<EventListener<CoursePoint>> listenerIterator = listeners
				.iterator();
		while (listenerIterator.hasNext()) {
			listenerIterator.next();
			listenerIterator.remove();
		}

		this.coursePoints = new EventList<CoursePoint>(
				new ArrayList<CoursePoint>(coursePoints));
		sortCoursePoints();
	}

	public void add(CoursePoint coursePoint) {
		getCoursePoints().add(coursePoint);
		setUnsavedTrue();// 58406
	}

	public void addCoursePoints(List<CoursePoint> coursePoints) {
		getCoursePoints().addAll(coursePoints);
		setUnsavedTrue();// 58406
	}

	public void remove(CoursePoint coursePoint) {
		getCoursePoints().remove(coursePoint);
		setUnsavedTrue();// 58406
	}
	
	//57421
	public Double getSegmentMovingTime(){
		return segmentMovingTime;
	}
	//57421
	public void setSegmentMovingTime(Double segmentMovingTime){
		this.segmentMovingTime = segmentMovingTime;
	}
	//57421
	public Double getSegmentDistance(){
		return segmentDistance;
	}
	//57421
	public void setSegmentDistance(Double segmentDistance){
		this.segmentDistance = segmentDistance;
	}

	private void sortCoursePoints() {
		Collections.sort(getCoursePoints(), new Comparator<CoursePoint>() {
			@Override
			public int compare(CoursePoint coursePoint1,
					CoursePoint coursePoint2) {
				return coursePoint1.getTime().compareTo(coursePoint2.getTime());
			}
		});
	}

	public List<Waypoint> getWaypoints() {
		return new ArrayList<Waypoint>();
	}

	@Override
	public GPSDocument getParent() {
		return parent;
	}

	public void setParent(GPSDocument parent) {
		this.parent = parent;
	}

	@Override
	public boolean isCourse() {
		return true;
	}

	@Override
	public void consolidate(ConsolidationLevel level) {
		calculateSummaryData(level);
	}

	private void calculateSummaryData(ConsolidationLevel level) {
		double elapsedTime = 0.0;
		double timerTime = 0.0;
		double pausedTime = 0.0;
		double distance = 0.0;
		double maximumSpeed = 0.0;
		double averageHeartRate = 0;
		short minimumHeartRate = 300;
		short maximumHeartRate = 0;
		double averageCadence = 0;
		short maximumCadence = 0;
		double averageRunningCadence = -1;
		short maximumRunningCadence = 0;
		double averagePower = 0;
		int maximumPower = -1;
		int calories = -1;
		int fatCalories = -1;
		double averageTemperature = 0;
		byte minimumTemperature = 127;
		byte maximumTemperature = -128;
		float averageAltitude = 0.0f;
		float minimumAltitude = 10000.0f;
		float maximumAltitude = -100.0f;
		float averagePositiveGrade = 0.0f;
		float averageNegativeGrade = 0.0f;
		float maximumPositiveGrade = -100.0f;
		float maximumNegativeGrade = 100.0f;
		int totalAscent = 0;
		int totalDescent = 0;
		double northEastLongitude = -181.0;
		double northEastLatitude = -91.0;
		double southWestLongitude = 181.0;
		double southWestLatitude = 91.0;

		boolean recalculate = (level == ConsolidationLevel.SUMMARY || level == ConsolidationLevel.RECALCULATION);

		if (name == null) {
			name = String.format("%s_%d", Messages.getMessage("course.label"),
					getId());
		} else {
			name = name.trim();
		}

		if(trackpoints.size() != 1){
		this.startTime = this.laps.get(0).getStartTime();
		this.endTime = this.laps.get(this.laps.size()-1).getEndTime();
		}
		
		new PauseDetectionPicCaseOperation().process(this);
		for(Pause pause : pauses){
			pausedTime += pause.getDuration();
		}
		for (Lap lap : laps) {
			elapsedTime += lap.getElapsedTime();
			timerTime += lap.getTimerTime();
			
			//pausedTime += lap.getPausedTime();
			distance += lap.getDistance();
		}

		for (Lap lap : laps) {
			double lapPercentage = lap.getTimerTime() / timerTime;

			maximumSpeed = Math.max(maximumSpeed, lap.getMaximumSpeed());

			if (lap.getAverageHeartRate() != null) {
				averageHeartRate += (lap.getAverageHeartRate() * lapPercentage);
			}

			if (lap.getMinimumHeartRate() != null) {
				minimumHeartRate = (short) Math.min(minimumHeartRate,
						lap.getMinimumHeartRate());
			}

			if (lap.getMaximumHeartRate() != null) {
				maximumHeartRate = (short) Math.max(maximumHeartRate,
						lap.getMaximumHeartRate());
			}

			if (lap.getAverageCadence() != null) {
				averageCadence += (lap.getAverageCadence() * lapPercentage);
			}

			if (lap.getMaximumCadence() != null) {
				maximumCadence = (short) Math.max(maximumCadence,
						lap.getMaximumCadence());
			}

			if (lap.getMaximumRunningCadence() != null) {
				maximumRunningCadence = (short) Math.max(maximumRunningCadence,
						lap.getMaximumRunningCadence());
			}

			if (lap.getAveragePower() != null) {
				averagePower += (lap.getAveragePower() * lapPercentage);
			}

			if (lap.getMaximumPower() != null) {
				maximumPower = (int) Math.max(maximumPower,
						lap.getMaximumPower());
			}

			if (lap.getAverageAltitude() != null) {
				averageAltitude += (lap.getAverageAltitude() * lapPercentage);
			}

			if (lap.getMinimumAltitude() != null) {
				minimumAltitude = (float) Math.min(minimumAltitude,
						lap.getMinimumAltitude());
			}

			if (lap.getMaximumAltitude() != null) {
				maximumAltitude = (float) Math.max(maximumAltitude,
						lap.getMaximumAltitude());
			}

			if (lap.getAverageTemperature() != null) {
				averageTemperature += (lap.getAverageTemperature() * lapPercentage);
			}

			if (lap.getMinimumTemperature() != null) {
				minimumTemperature = (byte) Math.min(minimumTemperature,
						lap.getMinimumTemperature());
			}

			if (lap.getMaximumTemperature() != null) {
				maximumTemperature = (byte) Math.max(maximumTemperature,
						lap.getMaximumTemperature());
			}

			if (lap.getTotalAscent() != null) {
				totalAscent += lap.getTotalAscent();
			}

			if (lap.getTotalDescent() != null) {
				totalDescent += lap.getTotalDescent();
			}

			if (lap.getCalories() != null) {
				calories += lap.getCalories();
			}

			if (lap.getFatCalories() != null) {
				fatCalories += lap.getFatCalories();
			}

			if (lap.getAveragePositiveGrade() != null) {
				averagePositiveGrade += (lap.getAveragePositiveGrade() * lapPercentage);
			}

			if (lap.getAverageNegativeGrade() != null) {
				averageNegativeGrade += (lap.getAverageNegativeGrade() * lapPercentage);
			}

			if (lap.getMaximumNegativeGrade() != null) {
				maximumNegativeGrade = Math.min(maximumNegativeGrade,
						lap.getMaximumNegativeGrade());
			}

			if (lap.getMaximumPositiveGrade() != null) {
				maximumPositiveGrade = Math.max(maximumPositiveGrade,
						lap.getMaximumPositiveGrade());
			}
		}

		for (Trackpoint trackpoint : getTrackpoints()) {
			if (trackpoint.getLongitude() != null
					&& trackpoint.getLatitude() != null) {
				northEastLongitude = Math.max(northEastLongitude,
						trackpoint.getLongitude());
				northEastLatitude = Math.max(northEastLatitude,
						trackpoint.getLatitude());
				southWestLongitude = Math.min(southWestLongitude,
						trackpoint.getLongitude());
				southWestLatitude = Math.min(southWestLatitude,
						trackpoint.getLatitude());
			}
		}

		if (startLongitude == null || recalculate) {
			startLongitude = getFirstTrackpoint().getLongitude();
		}

		if (startLatitude == null || recalculate) {
			startLatitude = getFirstTrackpoint().getLatitude();
		}

		if (startAltitude == null || recalculate) {
			startAltitude = getFirstTrackpoint().getAltitude();
		}

		if (endLongitude == null || recalculate) {
			endLongitude = getLastTrackpoint().getLongitude();
		}

		if (endLatitude == null || recalculate) {
			endLatitude = getLastTrackpoint().getLatitude();
		}

		if (endAltitude == null || recalculate) {
			endAltitude = getLastTrackpoint().getAltitude();
		}

		if (this.elapsedTime == null || recalculate) {
			this.elapsedTime = elapsedTime;
		}

		if (this.timerTime == null || recalculate) {
			this.timerTime = timerTime;
		}

		if (this.pausedTime == null || recalculate) {
			if (pauses == null || pauses.isEmpty()) {
				this.pausedTime = Math.abs((this.elapsedTime - this.timerTime)
						- pausedTime);
			} else {
				pausedTime = 0.;
				for (Pause p : pauses) {
					pausedTime += p.getDuration();
				}
				this.pausedTime = pausedTime;
			}
		}

		if (this.movingTime == null || recalculate) {
			this.movingTime = this.timerTime - pausedTime;
		}

		if (this.distance == null || recalculate) {
			this.distance = distance;
		}

		if (this.averageSpeed == null || recalculate) {
			this.averageSpeed = this.distance / this.timerTime;
		}

		if (this.averageMovingSpeed == null || recalculate) {
			this.averageMovingSpeed = this.distance / this.movingTime;
		}

		if (this.maximumSpeed == null || recalculate) {
			this.maximumSpeed = maximumSpeed;
		}

		if ((this.averageHeartRate == null || recalculate)
				&& averageHeartRate > 0) {
			this.averageHeartRate = (short) Math.round(averageHeartRate);
		}

		if ((this.minimumHeartRate == null || recalculate)
				&& minimumHeartRate != 300) {
			this.minimumHeartRate = minimumHeartRate;
		}

		if ((this.maximumHeartRate == null || recalculate)
				&& maximumHeartRate > 0) {
			this.maximumHeartRate = maximumHeartRate;
		}

		if ((this.averageCadence == null || recalculate) && averageCadence > 0) {
			this.averageCadence = (short) Math.round(averageCadence);
		}

		if ((this.maximumCadence == null || recalculate) && maximumCadence > 0) {
			this.maximumCadence = maximumCadence;
		}

		if ((this.averageRunningCadence == null || recalculate)
				&& averageRunningCadence > 0) {
			this.averageRunningCadence = (short) Math
					.round(averageRunningCadence);
		}

		if ((this.maximumRunningCadence == null || recalculate)
				&& maximumRunningCadence > 0) {
			this.maximumRunningCadence = maximumRunningCadence;
		}

		if ((this.averagePower == null || recalculate) && averagePower > 0) {
			this.averagePower = (int) Math.round(averagePower);
		}

		if ((this.maximumPower == null || recalculate) && maximumPower > -1) {
			this.maximumPower = maximumPower;
		}

		if ((this.calories == null || recalculate) && calories > -1) {
			this.calories = calories;
		}

		if ((this.fatCalories == null || recalculate) && fatCalories > -1) {
			this.fatCalories = fatCalories;
		}

		if ((this.averageAltitude == null || recalculate)
				&& averageAltitude != 0.0f) {
			this.averageAltitude = (float) Math.round(averageAltitude);
		}

		if ((this.minimumAltitude == null || recalculate)
				&& minimumAltitude != 10000.0f) {
			this.minimumAltitude = minimumAltitude;
		}

		if ((this.maximumAltitude == null || recalculate)
				&& maximumAltitude != -100.0f) {
			this.maximumAltitude = maximumAltitude;
		}

		if ((this.averageTemperature == null || recalculate)
				&& averageTemperature != 0) {
			this.averageTemperature = (byte) Math.round(averageTemperature);
		}

		if ((this.minimumTemperature == null || recalculate)
				&& minimumTemperature < 127) {
			this.minimumTemperature = minimumTemperature;
		}

		if ((this.maximumTemperature == null || recalculate)
				&& maximumTemperature > -128) {
			this.maximumTemperature = maximumTemperature;
		}

		if (this.totalAscent == null || recalculate) {
			this.totalAscent = totalAscent;
		}

		if (this.totalDescent == null || recalculate) {
			this.totalDescent = totalDescent;
		}

		if ((getNortheastLongitude() == null || recalculate)
				&& northEastLongitude != -181.0) {
			setNortheastLongitude(northEastLongitude);
		}

		if ((getNortheastLatitude() == null || recalculate)
				&& northEastLatitude != -91.0) {
			setNortheastLatitude(northEastLatitude);
		}

		if ((getSouthwestLongitude() == null || recalculate)
				&& southWestLongitude != 181.0) {
			setSouthwestLongitude(southWestLongitude);
		}

		if ((getSouthwestLatitude() == null || recalculate)
				&& southWestLatitude != 91.0) {
			setSouthwestLatitude(southWestLatitude);
		}

		if ((this.averageGrade == null || recalculate)
				&& (getStartAltitude() != null && getEndAltitude() != null)) {
			this.averageGrade = (float) ((getEndAltitude() - getStartAltitude()) / getDistance());
		}

		if ((this.averagePositiveGrade == null || recalculate)
				&& averagePositiveGrade != 0.0f) {
			this.averagePositiveGrade = averagePositiveGrade;
		}

		if ((this.averageNegativeGrade == null || recalculate)
				&& averageNegativeGrade != 0.0f) {
			this.averageNegativeGrade = averageNegativeGrade;
		}

		if ((this.maximumPositiveGrade == null || recalculate)
				&& maximumPositiveGrade != -100.0f) {
			this.maximumPositiveGrade = maximumPositiveGrade;
		}

		if ((this.maximumNegativeGrade == null || recalculate)
				&& maximumNegativeGrade != 100.0f) {
			this.maximumNegativeGrade = maximumNegativeGrade;
		}
	}

	@Override
	public String getDocumentItemName() {
		return String.format("%s (%s)", Messages.getMessage("course.label"),
				getName());
	}

	@Override
	public List<ActionType> getSupportedActions() {
		List<ActionType> supportedActions = new ArrayList<>();
		supportedActions.add(ActionType.CONSOLIDATION);
		supportedActions.add(ActionType.SET_PACE);
		supportedActions.add(ActionType.DETECT_CLIMBS_DESCENTS);
		supportedActions.add(ActionType.MARKING);
		supportedActions.add(ActionType.SIMPLIFICATION);
		supportedActions.add(ActionType.NEW_COURSE);
		supportedActions.add(ActionType.REVERSE);
		supportedActions.add(ActionType.DETECT_PAUSES);// 58406
		supportedActions.add(ActionType.REMOVE_PAUSES);
		supportedActions.add(ActionType.IMPORT_PICTURE);// 58406
		supportedActions.add(ActionType.COPY);// 57421
		if(!pictures.isEmpty()){
			supportedActions.add(ActionType.AUTO_LOCATE_PICTURES);//58406
		}

		return supportedActions;
	}

	@Override
	public FileType[] getSupportedFileTypes() {
		return new FileType[] { FileType.GPX, /*FileType.FIT, */FileType.TCX,
				/*FileType.KML, */FileType.CSV };
	}

	@Override
	public String getFolderTreeItemName() {
		return name;
	}

	@Override
	public ImageIcon getOpenIcon() {
		return icon;
	}

	@Override
	public ImageIcon getClosedIcon() {
		return icon;
	}

	@Override
	public ImageIcon getLeafIcon() {
		return icon;
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager
				.getInstance()
				.publish(
						publisher,
						com.henriquemalheiro.trackit.presentation.event.Event.COURSE_SELECTED,
						this);
	}

	@Override
	public void publishUpdateEvent(EventPublisher publisher) {
		EventManager
				.getInstance()
				.publish(
						publisher,
						com.henriquemalheiro.trackit.presentation.event.Event.COURSE_UPDATED,
						this);
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	/* Paintable Interface */

	@Override
	public void paint(Graphics2D graphics, MapLayer layer,
			Map<String, Object> paintingAttributes) {
		MapPainter coursePainter = MapPainterFactory.getInstance()
				.getMapPainter(layer, this);
		coursePainter.paint(graphics, paintingAttributes);
	}

	@Override
	public List<DataType> getDisplayableElements() {
		return Arrays.asList(new DataType[] { DataType.COURSE, DataType.LAP,
				DataType.TRACK, DataType.TRACK_SEGMENT, DataType.TRACKPOINT,
				DataType.COURSE_POINT, DataType.EVENT });
	}

	@Override
	public List<? extends DocumentItem> getDisplayedElements(DataType dataType) {
		switch (dataType) {
		case COURSE:
			return Arrays.asList(new DocumentItem[] { this });
		case LAP:
			return getLaps();
		case TRACK:
			return getTracks();
		case TRACK_SEGMENT:
			return getSegments();
		case TRACKPOINT:
			return getTrackpoints();
		case COURSE_POINT:
			return getCoursePoints();
		case EVENT:
			return getEvents();
		default:
			return Collections.emptyList();
		}
	}

	// 58406###################################################################################

	public void addPicture(File file) {
		Trackpoint middle = trackpoints.get(Math.abs(trackpoints.size() / 2));
		Picture pic = new Picture(file, middle.getLatitude(),
				middle.getLongitude(), middle.getAltitude(), this);
		pictures.add(pic);
		refreshMap();
		Collections.sort(pictures, new PictureComparator());
		publishUpdateEvent(null);
	}
	
	// 12335: 2015-10-02: batch picture adding works faster
	public void addPictures( File[] files) {
		Trackpoint middle = trackpoints.get(Math.abs(trackpoints.size() / 2));
		for( File file: files)
			if ( file.exists() ) {
				Picture pic = new Picture(file, middle.getLatitude(), middle.getLongitude(), middle.getAltitude(), this);
				pictures.add(pic);
			}
		refreshMap();
		Collections.sort(pictures, new PictureComparator());
		publishUpdateEvent(null);
		setUnsavedTrue();
	}
	// 12335: 2015-10-02 end


	public void removePicture(Picture pic) {
//		Database.getInstance().removePicture(pic);
		pictures.remove(pic);
		refreshMap();
		publishUpdateEvent(null);
	}

	// 12335: 2015-9-17 - Batch removal makes operation faster
	public void removePictures()
	{
		Database.getInstance().removeAllPictures(this);
		pictures.clear();
		refreshMap();
		publishUpdateEvent(null);
		setUnsavedTrue();
	}
	// 12335: 2015-9-17 end
	
	public void refreshMap() {
		MapView mv = TrackIt.getApplicationPanel().getMapView();
		mv.getMap().getLayer(MapLayerType.PHOTO_LAYER).validate();
		mv.getMap().refresh();
	}

	@Override
	public List<Picture> getPictures() {
		return pictures;
	}
	
	public void setPictures(List<Picture> pictures){
		this.pictures = pictures;
	}

	@Override
	public ColorSchemeV2 getColorSchemeV2() {
		return colorScheme;
	}

	public void setColorSchemeV2(ColorSchemeV2 colorScheme) {
		this.colorScheme = colorScheme;
	}
	
	public void setStartTime(Date date){
		this.startTime = date;
	}
	
	public void setEndTime(Date date){
		this.endTime = date;
	}
	
	public Date getStartTime(){
		return startTime;
	}
	
	public Date getEndTime(){
		return endTime;
	}

	/*public void updateSpeedWithPauseTime(double pauseLimit) {
		if (noSpeedInFile) {
			double vm;
			Trackpoint trkp = trackpoints.get(0);
			Trackpoint trkpPlusOne = trackpoints.get(1);
			Trackpoint trkpMinusOne = null;
			double speed;
			double distance = trkp.getDistance();
			double time = trkp.getTimestamp().getTime() / 1000;
			double distancePlusOne = trkpPlusOne.getDistance();
			double timePlusOne = trkpPlusOne.getTimestamp().getTime() / 1000;
			double distanceMinusOne, timeMinusOne;
			vm = (distancePlusOne - distance) / (timePlusOne - time);
			if (vm < pauseLimit) {
				trkp.setSpeed(-1.);
				trkpPlusOne.setSpeed(-1.);
			} else {
				trkp.setSpeed(vm);
				trkpPlusOne.setSpeed(vm);
			}
			int n = trackpoints.size();
			for (int i = 2; i < n; i++) {
				trkp = trackpoints.get(i);
				trkpMinusOne = trackpoints.get(i - 1);
				distance = trkp.getDistance();
				time = trkp.getTimestamp().getTime() / 1000;
				distanceMinusOne = trkpMinusOne.getDistance();
				timeMinusOne = trkpMinusOne.getTimestamp().getTime() / 1000;

				vm = (distance - distanceMinusOne) / (time - timeMinusOne);

				if (vm <= pauseLimit) {
					trkp.setSpeed(vm);
					if (trkpMinusOne.getSpeed() >= 0) {
						trkpMinusOne
								.setSpeed((trkpMinusOne.getSpeed() + vm) / 2);
					} else {
						trkpMinusOne.setSpeed(vm);
					}
				} else {
					trkp.setSpeed(-1.);
				}
			}
			for (int i = 1; i < n - 1; i++) {
				trkp = trackpoints.get(i);
				trkpPlusOne = trackpoints.get(i + 1);
				trkpMinusOne = trackpoints.get(i - 1);
				distance = trkp.getDistance();
				time = trkp.getTimestamp().getTime() / 1000;
				distancePlusOne = trkpPlusOne.getDistance();
				timePlusOne = trkpPlusOne.getTimestamp().getTime() / 1000;
				distanceMinusOne = trkpMinusOne.getDistance();
				timeMinusOne = trkpMinusOne.getTimestamp().getTime() / 1000;
				if (trkp.getSpeed() < 0) {
					if (time != timeMinusOne && time != timePlusOne) {
						speed = (((time - timeMinusOne) * (distancePlusOne - distance))
								/ (timePlusOne - time) + ((timePlusOne - time) * (distance - distanceMinusOne))
								/ (time - timeMinusOne))
								/ (timePlusOne - timeMinusOne);
						trkp.setSpeed(speed);
					} else {
						if (time == timeMinusOne && time == timePlusOne) {
							trkp.setSpeed(trkpMinusOne.getSpeed());
						} else {
							speed = (distancePlusOne - distanceMinusOne)
									/ (timePlusOne - timeMinusOne);
							trkp.setSpeed(speed);
						}
					}
				}
			}
			trkp = trackpoints.get(0);
			trkpPlusOne = trackpoints.get(1);
			if (trkp.getSpeed() < 0) {
				distance = trkp.getDistance();
				time = trkp.getTimestamp().getTime() / 1000;
				distancePlusOne = trkpPlusOne.getDistance();
				timePlusOne = trkpPlusOne.getTimestamp().getTime() / 1000;
				speed = 2 * (distancePlusOne - distance) / (timePlusOne - time)
						- trkpPlusOne.getSpeed();
				trkp.setSpeed(speed);
			}
			trkp = trackpoints.get(n - 1);
			trkpMinusOne = trackpoints.get(n - 2);
			if (trkp.getSpeed() < 0) {
				distance = trkp.getDistance();
				time = trkp.getTimestamp().getTime() / 1000;
				distanceMinusOne = trkpMinusOne.getDistance();
				timeMinusOne = trkpMinusOne.getTimestamp().getTime() / 1000;
				speed = 2 * (distance - distanceMinusOne)
						/ (time - timeMinusOne) - trkpMinusOne.getSpeed();
				trkp.setSpeed(speed);
			}
		}
	}*/

	public Boolean getNoSpeedInFile() {
		return noSpeedInFile;
	}

	public void setNoSpeedInFile(Boolean noSpeedInFile) {
		this.noSpeedInFile = noSpeedInFile;
	}
	
	//57421
	public SportType getSport() {
		return sport;
	}

	public SubSportType getSubSport() {
		return subSport;
	}
	
	//12335: 2016-06-15
	public void setSportAndSubSport( SportType sport, SubSportType subSport) {
		setSportAndSubSport( sport, subSport, false);
	}

	//12335: 2016-06-15
	public void updateSportAndSubSport( SportType sport, SubSportType subSport) {
		setSportAndSubSport( sport, subSport, true);
	}

	//12335: 2016-06-14 (made private 2016-06-15)
	private void setSportAndSubSport( SportType sport, SubSportType subSport, boolean isChange){
		if ( this.sport != sport || this.subSport != subSport ) {
			this.sport    = sport;
			this.subSport = subSport;
			for ( Lap lap : laps ) {
				lap.setSport(    sport);
				lap.setSubSport( subSport);
			}
			
			if ( isChange ) {
				synchronizeWithDB = true;
				this.publishUpdateEvent(null);
			}
		}
	}
	
	
	public long getParentCourseId(){
		return parentCourseId;
	}
	
	public void setParentCourseId(long id){
		parentCourseId = id;
	}
	
	public int getSegmentNumber(){
		return segmentNumber;
	}
	
	public void setSegmentNumber (int i){
		segmentNumber+=i;
	}
	
	public boolean isSegment(){
		if(this.segmentDistance != null && this.segmentMovingTime != null){
			return true;
		}
		return false;
	}
	
	//}57421
	
	//57421
		public boolean isInsidePause(Long time){
			/*for(Pause pause : pauses){
				Long startTime = pause.getStart().getTime();
				Long endTime = pause.getEnd().getTime();
				if(time >= startTime && time <= endTime){
					return true;
				}
			}
			return false;*/
			return (getPause(time)!=null);
		}
		
		public Pause getPause(Long time){
			for(Pause pause : pauses){
				Long startTime = pause.getStart().getTime();
				Long endTime = pause.getEnd().getTime();
				if(time >= startTime && time <= endTime){
					return pause;
				}
			}
			return null;
		}
		
		public boolean insidePause(Pause pause, Long time){
			Long startTime = pause.getStart().getTime();
			Long endTime = pause.getEnd().getTime();
			if(time > startTime && time < endTime){
				return true;
			}
			return false;
		}
		
		public Course clone(){
			Course course = new Course();
			course.setId(this.getId());
			course.setName(this.name);
//			course.setSport(this.sport);						//12335: 2016-06-15
//			course.setSubSport(this.subSport);					//12335: 2016-06-15
			setSportAndSubSport( this.sport, this.subSport);	//12335: 2016-06-15
			
			course.setLaps(this.laps);
			course.setTracks(this.tracks);
			
			List<Trackpoint> newPoints = new ArrayList<Trackpoint>();
			for(Trackpoint trackpoint : this.getTrackpoints()){
				Trackpoint newTrackpoint = trackpoint.clone();
				newTrackpoint.setId(trackpoint.getId());
				newPoints.add(newTrackpoint);
			}
			course.setTrackpoints(newPoints);
			course.setCoursePoints(this.coursePoints);
			course.setSegments(this.segments);
			course.setEvents(this.events);
			course.setNotes(this.notes);
			course.setElapsedTime(this.elapsedTime);
			course.setTimerTime(this.timerTime);
			course.setMovingTime(this.movingTime);
			course.setPausedTime(this.pausedTime);
			course.setDistance(this.distance);
			course.setAverageSpeed(this.averageSpeed);
			course.setAverageMovingSpeed(this.averageMovingSpeed);
			course.setMaximumSpeed(this.maximumSpeed);
			course.setAverageHeartRate(this.averageHeartRate);
			course.setMinimumHeartRate(this.minimumHeartRate);
			course.setMaximumHeartRate(this.maximumHeartRate);
			course.setAverageCadence(this.averageCadence);
			course.setMaximumCadence(this.maximumCadence);
			course.setAverageRunningCadence(this.averageRunningCadence);
			course.setMaximumRunningCadence(this.maximumRunningCadence);
			course.setAveragePower(this.averagePower);
			course.setMaximumPower(this.maximumPower);
			course.setCalories(this.calories);
			course.setFatCalories(this.fatCalories);
			course.setAverageTemperature(this.averageTemperature);
			course.setMinimumTemperature(this.minimumTemperature);
			course.setMaximumTemperature(this.maximumTemperature);
			course.setTotalAscent(this.totalAscent);
			course.setTotalDescent(this.totalDescent);
			course.setAverageAltitude(this.averageAltitude);
			course.setMinimumAltitude(this.minimumAltitude);
			course.setMaximumAltitude(this.maximumAltitude);
			course.setAverageGrade(this.averageGrade);
			course.setAveragePositiveGrade(this.averagePositiveGrade);
			course.setAverageNegativeGrade(this.averageNegativeGrade);
			course.setMaximumPositiveGrade(this.maximumPositiveGrade);
			course.setMaximumNegativeGrade(this.maximumNegativeGrade);
			course.setAveragePositiveVerticalSpeed(this.averagePositiveVerticalSpeed);
			course.setAverageNegativeVerticalSpeed(this.averageNegativeVerticalSpeed);
			course.setMaximumPositiveVerticalSpeed(this.maximumPositiveVerticalSpeed);
			course.setMaximumNegativeVerticalSpeed(this.maximumNegativeVerticalSpeed);
			course.setStartLatitude(this.startLatitude);
			course.setStartLongitude(this.startLongitude);
			course.setStartAltitude(this.startAltitude);
			course.setEndLatitude(this.endLatitude);
			course.setEndLongitude(this.endLongitude);
			course.setEndAltitude(this.endAltitude);
			course.setNortheastLatitude(this.northeastLatitude);
			course.setNortheastLongitude(this.northeastLongitude);
			course.setSouthwestLatitude(this.southwestLatitude);
			course.setSouthwestLongitude(this.southwestLongitude);
			course.setParent(this.parent);
			course.setUnsavedChanges(this.unsavedChanges);
			course.synchronizeWithDB = this.synchronizeWithDB;		//12335: 2016-06-16
			course.setFilepath(this.filepath);
			course.setPauses(this.pauses);
			course.setPictures(this.pictures);
			course.setCreator(this.creator);
			course.setColorSchemeV2(this.colorScheme);
			course.setNoSpeedInFile(this.noSpeedInFile);
			course.setStartTime(this.startTime);
			course.setEndTime(this.endTime);
			
			return course;
		}

}