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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.decoratedlist.EventList;
import com.henriquemalheiro.trackit.business.utility.decoratedlist.EventListener;
import com.henriquemalheiro.trackit.business.utility.decoratedlist.ListEvent;
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
import com.pg58406.trackit.business.utility.PictureComparator;

public class Activity extends TrackItBaseType implements DocumentItem,
		FolderTreeItem, PhotoContainer, ColorSchemeV2Container {
	private static ImageIcon icon = ImageUtilities
			.createImageIcon("mountain_biking_16.png");

	private ActivityMetadata metadata;
	private Date startTime;
	private Date endTime;
	private Double totalTimerTime;
	private String name;
	private String oldName;						// 12335: 2015-09-21
	private ActivityType type;
	private List<Session> sessions;
	private List<Lap> laps;
	private List<Length> lengths;
	private List<Track> tracks;
	private EventList<Trackpoint> trackpoints;
	private List<TrackSegment> segments;
	private List<Event> events;
	private List<DeviceInfo> devices;
	private String notes;
	private EventType event;
	private EventTypeType eventType;
	private Short eventGroup;
	private GPSDocument parent;
	private List<Picture> pictures;						// 58406
	private String creator;								// 58406
	private String filepath;							// 58406
	private ColorSchemeV2 colorScheme;					// 58406
	private Boolean noSpeedInFile;						// 58406
	private List<Pause> pauses;							// 58406	
	private SportType sport; 							// 57421
	private SubSportType subSport;						// 57421
	private SportType temporarySport;
	private SubSportType temporarySubSport;
	private Boolean unsavedChanges;         			// 12335 : 2015-07-24

	public Activity() {
		super();

		name = "";                          			// 12335: 2015-08-10
		oldName = "";									// 12335: 2015-09-21
		type = ActivityType.MANUAL;
		event = EventType.ACTIVITY;
		eventType = EventTypeType.STOP;
		eventGroup = 0;

		metadata = new ActivityMetadata();
		sessions = new ArrayList<Session>();
		laps = new ArrayList<Lap>();
		lengths = new ArrayList<Length>();
		tracks = new ArrayList<Track>();
		events = new ArrayList<Event>();
		devices = new ArrayList<DeviceInfo>();
		segments = new ArrayList<TrackSegment>();

		trackpoints = new EventList<Trackpoint>(new ArrayList<Trackpoint>());
		trackpoints.addListener(new LapEventListener());

		pictures = new ArrayList<Picture>();// 58406
		filepath = null;// 58406
		pauses = new ArrayList<Pause>();// 58406

		colorScheme = TrackIt.getDefaultColorScheme();
		noSpeedInFile = true;

		unsavedChanges = false;      // 12335 : 2015-07-24
}

	// 12335 : 2015-07-24 : Support for Activity changes: activities may change some properties
	public boolean getUnsavedChanges() {
		System.out.println( "Returning ACTIVITY unsavedChanges as: " + unsavedChanges);
		return unsavedChanges;
	}

	public boolean setUnsavedChanges(boolean changes) {
		System.out.println( "setUnsavedChanges from " + unsavedChanges + " to " + changes);
		if ( ChangesSemaphore.IsEnabled() )					// 12335: 2015-09-30
			this.unsavedChanges = changes;
		return unsavedChanges;
	}

	public void setUnsavedTrue() {
		setUnsavedChanges(true);
	}

	public void setUnsavedFalse() {
		setUnsavedChanges(false);
	}
	// 12335 : 2015-07-24 end

	
	// 58406###################################################################################

	public void setFilepath(String fullFilepath) {
		this.filepath = fullFilepath;
	}

	public String getFilepath() {
		return filepath;
	}

	// ########################################################################################

	public ActivityMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(ActivityMetadata metadata) {
		this.metadata = metadata;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Double getTotalTimerTime() {
		return totalTimerTime;
	}

	public void setTotalTimerTime(Double totalTimerTime) {
		this.totalTimerTime = totalTimerTime;
	}

	public String getName() {
		String name = this.name;
//		if (name == null || name.isEmpty()) {
//			name = startTime != null ? Formatters.getSimpleDateFormat().format(
//					startTime) : "Activity #" + getId();
//		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	//12335: 2015-09-21 - support for renaming operation
	public void rename(String name) {
		if ( oldName.isEmpty() )
			oldName = this.name;
		setName(name);
		System.out.println("Renaming Activity from " + oldName + " to " + name);
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
	// 12335: 2015-09-21

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public SportType getSport(){
		return sport;
	}
	
	public SubSportType getSubSport(){
		return subSport;
	}
	
//	public void setSport(SportType sport){
//		this.sport = sport;
//	}
//	
//	public void setSubSport(SubSportType subSport){
//		this.subSport = subSport;
//	}
	
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
		this.sport    = sport;
		this.subSport = subSport;
		for( Session session : sessions) {
			session.setSport(    sport);
			session.setSubSport( subSport);
			for ( Lap lap : session.getLaps() ) {
				lap.setSport(    sport);
				lap.setSubSport( subSport);
			}
		}
		
		if ( isChange )
			this.publishUpdateEvent(null);
	}
	
	public SportType getTemporarySport(){
		return temporarySport;
	}
	
	public void setTemporarySport(SportType sport){
		this.temporarySport = sport;
	}
	
	public SubSportType getTemporarySubSport(){
		return temporarySubSport;
	}
	
	public void setTemporarySubSport(SubSportType subSport){
		this.temporarySubSport = subSport;
	}

	public EventType getEvent() {
		return event;
	}

	public void setEvent(EventType event) {
		this.event = event;
	}

	public EventTypeType getEventType() {
		return eventType;
	}

	public void setEventType(EventTypeType eventType) {
		this.eventType = eventType;
	}

	public Short getEventGroup() {
		return eventGroup;
	}

	public void setEventGroup(Short eventGroup) {
		this.eventGroup = eventGroup;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public Session getSession(Session session) {
		int sessionIndex = sessions.indexOf(session);
		if (sessionIndex != -1) {
			return sessions.get(sessionIndex);
		} else {
			return null;
		}
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	public Session getFirstSession() {
		Session firstSession = null;
		if (!sessions.isEmpty()) {
			firstSession = sessions.get(0);
		}
		return firstSession;
	}

	public Session getLastSession() {
		Session lastSession = null;
		if (!sessions.isEmpty()) {
			int lastSessionIndex = sessions.size() - 1;
			lastSession = sessions.get(lastSessionIndex);
		}
		return lastSession;
	}

	public void add(Session session) {
		sessions.add(session);
	}

	public void removeSession(Session session) {
		sessions.remove(session);
	}

	public List<Lap> getLaps() {
		return laps;
	}

	public List<Lap> getLaps(final Date startTime, final Date endTime) {
		List<Lap> result = null;
		if (laps.isEmpty()) {
			result = new ArrayList<Lap>();
		} else {
			int startLapIndex = getStartLapIndex(startTime);
			int endLapIndex = getEndLapIndex(endTime);
			result = laps.subList(startLapIndex, endLapIndex + 1);
		}
		return result;
	}

	private int getStartLapIndex(final Date startTime) {
		int startLapIndex = 0;
		int lastLapIndex = laps.size() - 1;
		while (startLapIndex < lastLapIndex
				&& laps.get(startLapIndex).getStartTime().before(startTime)) {
			startLapIndex++;
		}
		return startLapIndex;
	}

	private int getEndLapIndex(final Date endTime) {
		int endLapIndex = laps.size() - 1;
		while (endLapIndex > 0
				&& laps.get(endLapIndex).getEndTime().after(endTime)) {
			endLapIndex--;
		}
		return endLapIndex;
	}

	public void setLaps(List<Lap> laps) {
		this.laps.clear();
		this.laps.addAll(laps);
	}

	public Lap getLap(Lap lap) {
		Lap resultLap = null;
		int lapIndex = laps.indexOf(lap);
		if (lapIndex != -1) {
			resultLap = laps.get(lapIndex);
		}
		return resultLap;
	}

	public Lap getFirstLap() {
		Lap firstLap = null;
		if (!laps.isEmpty()) {
			firstLap = laps.get(0);
		}
		return firstLap;
	}

	public Lap getLastLap() {
		Lap lastLap = null;
		if (!laps.isEmpty()) {
			int lastLapIndex = laps.size() - 1;
			lastLap = laps.get(lastLapIndex);
		}
		return lastLap;
	}

	public void add(Lap lap) {
		laps.add(lap);
	}

	public void removeLap(Lap lap) {
		laps.remove(lap);
	}

	public List<Length> getLengths() {
		return lengths;
	}

	public List<Length> getLengths(Date startTime, Date endTime) {
		List<Length> lengths = getLengths();

		if (lengths == null || lengths.isEmpty()) {
			return lengths;
		}

		int startLengthIndex = 0;
		while (lengths.get(startLengthIndex).getStartTime()
				.compareTo(startTime) < 0) {
			startLengthIndex++;
		}

		int endLengthIndex = lengths.size() - 1;
		while (lengths.get(endLengthIndex).getEndTime().compareTo(endTime) > 0) {
			endLengthIndex--;
		}

		return lengths.subList(startLengthIndex, endLengthIndex + 1);
	}

	public Length getLength(Length length) {
		if (length == null) {
			return length;
		}

		for (Length currentLength : getLengths()) {
			if (length.equals(currentLength)) {
				return currentLength;
			}
		}

		return null;
	}

	public void setLengths(List<Length> lengths) {
		this.lengths = lengths;
	}

	public void add(Length length) {
		getLengths().add(length);
	}

	public void removeLength(Length length) {
		getLengths().remove(length);
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public List<Track> getTracks(Date startTime, Date endTime) {
		List<Track> tracks = getTracks();

		if (tracks == null || tracks.isEmpty()) {
			return tracks;
		}

		int startTrackIndex = 0;
		while (tracks.get(startTrackIndex).getStartTime().compareTo(startTime) < 0) {
			startTrackIndex++;
		}

		int endTrackIndex = tracks.size() - 1;
		while (endTrackIndex > 0
				&& tracks.get(endTrackIndex).getEndTime().compareTo(endTime) > 0) {
			endTrackIndex--;
		}

		return tracks.subList(startTrackIndex, endTrackIndex + 1);
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
	}

	public void removeTrack(Track track) {
		getTracks().remove(track);
		((EventList<Trackpoint>) getTrackpoints()).removeListener(track);
	}

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
	}

	public void removeSegment(TrackSegment segment) {
		getSegments().remove(segment);
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

	public void removeEvent(Event event) {
		getEvents().remove(event);
	}

	public List<DeviceInfo> getDevices() {
		return devices;
	}

	public DeviceInfo getDevice(DeviceInfo device) {
		if (device == null) {
			return device;
		}

		for (DeviceInfo currentDevice : getDevices()) {
			if (device.equals(currentDevice)) {
				return currentDevice;
			}
		}

		return null;
	}

	public void setDevices(List<DeviceInfo> devices) {
		this.devices = devices;
	}

	public void add(DeviceInfo device) {
		getDevices().add(device);
	}

	public void removeDevice(DeviceInfo device) {
		getDevices().remove(device);
	}

	@Override
	public List<Trackpoint> getTrackpoints() {
		return trackpoints;
	}

	public List<Trackpoint> getTrackpoints(Date startTime, Date endTime) {
		List<Trackpoint> trackpoints = getTrackpoints();

		if (trackpoints == null || trackpoints.isEmpty()) {
			return trackpoints;
		}

		int startTrackpointIndex = 0;
		while (startTrackpointIndex < trackpoints.size() - 1
				&& trackpoints.get(startTrackpointIndex).getTimestamp()
						.compareTo(startTime) < 0) {
			startTrackpointIndex++;
		}

		int endTrackpointIndex = trackpoints.size() - 1;
		while (endTrackpointIndex > 0
				&& trackpoints.get(endTrackpointIndex).getTimestamp()
						.compareTo(endTime) > 0) {
			endTrackpointIndex--;
		}

		return trackpoints
				.subList(startTrackpointIndex, endTrackpointIndex + 1);
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
		this.trackpoints.addListener(new LapEventListener());
		for (Track track : getTracks()) {
			this.trackpoints.addListener(track);
		}
	}

	public void add(Trackpoint trackpoint) {
		getTrackpoints().add(trackpoint);
	}

	public void add(List<Trackpoint> trackpoints) {
		getTrackpoints().addAll(trackpoints);
	}

	public void removeTrackpoint(Trackpoint trackpoint) {
		getTrackpoints().remove(trackpoint);
	}

	public List<CoursePoint> getCoursePoints() {
		return new ArrayList<CoursePoint>();
	}

	public void removeCoursePoint(CoursePoint coursePoint) {
		// do nothing
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
	public boolean isActivity() {
		return true;
	}

	private class LapEventListener implements EventListener<Trackpoint> {
		public void processEvent(ListEvent<Trackpoint> event) {
			switch (event.getEvent()) {
			case AFTER_ADD:
				break;

			case AFTER_REMOVE:
				break;

			default:
			}
		}
	}

	@Override
	public String getDocumentItemName() {
		return String.format("%s (%s)", Messages.getMessage("activity.label"),
				getName());
	}

	@Override
	public List<ActionType> getSupportedActions() {
		List<ActionType> supportedActions = new ArrayList<>();
		supportedActions.add(ActionType.CONSOLIDATION);
		supportedActions.add(ActionType.DETECT_CLIMBS_DESCENTS);
		supportedActions.add(ActionType.DETECT_PAUSES);
		supportedActions.add(ActionType.IMPORT_PICTURE);			// 58406
		if(!pictures.isEmpty()){
			supportedActions.add(ActionType.AUTO_LOCATE_PICTURES);	// 58406
		}
		supportedActions.add(ActionType.COPY);						// 12335: 2015-10-13

		return supportedActions;
	}

	@Override
	public FileType[] getSupportedFileTypes() {
		return new FileType[] { FileType.GPX, /*FileType.FIT, */FileType.TCX,
				/*FileType.KML, */FileType.CSV, FileType.FITLOG };
	}

	@Override
	public String getFolderTreeItemName() {
		return getName();
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
						com.henriquemalheiro.trackit.presentation.event.Event.ACTIVITY_SELECTED,
						this);
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	/* Paintable Interface */

	public void paint(Graphics2D graphics, MapLayer layer,
			Map<String, Object> paintingAttributes) {
		MapPainter painter = MapPainterFactory.getInstance().getMapPainter(
				layer, this);
		painter.paint(graphics, paintingAttributes);
	}

	@Override
	public List<DataType> getDisplayableElements() {
		return Arrays.asList(new DataType[] { DataType.ACTIVITY,
				DataType.ACTIVITY_METADATA, DataType.SESSION, DataType.LAP,
				DataType.TRACK, DataType.TRACK_SEGMENT, DataType.TRACKPOINT,
				DataType.EVENT, DataType.DEVICE });
	}

	@Override
	public List<? extends DocumentItem> getDisplayedElements(DataType dataType) {
		switch (dataType) {
		case ACTIVITY:
			return Arrays.asList(new DocumentItem[] { this });
		case ACTIVITY_METADATA:
			return Arrays.asList(new DocumentItem[] { getMetadata() });
		case SESSION:
			return getSessions();
		case LAP:
			return getLaps();
		case TRACK:
			return getTracks();
		case TRACK_SEGMENT:
			return getSegments();
		case TRACKPOINT:
			return getTrackpoints();
		case EVENT:
			return getEvents();
		case DEVICE:
			return getDevices();
		default:
			return Collections.emptyList();
		}
	}

	// 58406###################################################################################
	@Override
	public List<Picture> getPictures() {
		return pictures;
	}

	// TO BE REMOVED 2016-06-05
	public void addPicture(File file) {
		Trackpoint middle = trackpoints.get(Math.abs(trackpoints.size() / 2));
		Picture pic = new Picture(file, middle.getLatitude(),
				middle.getLongitude(), middle.getAltitude(), this);
		pictures.add(pic);
		refreshMap();
		Collections.sort(pictures, new PictureComparator());
		publishUpdateEvent(null);
	}

	// 2015-09-17: 12335 : Batch picture adding is faster
	public void addPictures( File[] files) {
		Trackpoint middle = trackpoints.get(Math.abs(trackpoints.size() / 2));
		for ( File file: files)
			if ( file.exists() ) {
				Picture pic = new Picture(file, middle.getLatitude(),
						middle.getLongitude(), middle.getAltitude(), this);
				pictures.add(pic);
			}
		refreshMap();
		Collections.sort(pictures, new PictureComparator());
		publishUpdateEvent(null);
		setUnsavedTrue();
	}
	// 2015-09-17: 12335 end

	// 12335: 2015-09-17 - Batch removal makes operation faster
	public void removePictures() {
		Database.getInstance().removeAllPictures(this);
		pictures.clear();
		refreshMap();
		publishUpdateEvent(null);
		setUnsavedTrue();
	}
	// 12335: 2015-09-17 end

	public void removePicture(Picture pic) {
		Database.getInstance().removePicture(pic);
		pictures.remove(pic);
		refreshMap();
		publishUpdateEvent(null);
	}

	public void refreshMap() {
		MapView mv = TrackIt.getApplicationPanel().getMapView();
		mv.getMap().getLayer(MapLayerType.PHOTO_LAYER).validate();
		mv.getMap().refresh();
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreator() {
		return creator;
	}

	public void publishUpdateEvent(EventPublisher publisher) {
		EventManager
				.getInstance()
				.publish(
						publisher,
						com.henriquemalheiro.trackit.presentation.event.Event.ACTIVITY_UPDATED,
						this);
	}

	@Override
	public ColorSchemeV2 getColorSchemeV2() {
		return colorScheme;
	}

	public void setColorSchemeV2(ColorSchemeV2 colorScheme) {
		this.colorScheme = colorScheme;
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
	
	public List<Pause> getPauses() {
		return pauses;
	}

	public void setPauses(List<Pause> pauses) {
		this.pauses = pauses;
	}
	
	//57421
	public boolean isInsidePause(Long time){
		for(Pause pause : pauses){
			Long startTime = pause.getStart().getTime();
			Long endTime = pause.getEnd().getTime();
			if(time >= startTime && time <= endTime){
				return true;
			}
		}
		return false;
	}
}
