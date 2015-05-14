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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.task.ActionType;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.data.DataType;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;

public class GPSDocument extends TrackItBaseType implements DocumentItem, FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("document.png");
	
	private List<Activity> activities;
	private List<Course> courses;
	private List<Waypoint> waypoints;
	private String name;
	private String fileName;
	private boolean changed;//58406
	
	public GPSDocument(String name) {
		this.name = name;

		activities = new ArrayList<Activity>();
		courses = new ArrayList<Course>();
		waypoints = new ArrayList<Waypoint>();
		changed = false;//58406
	}
	
	public boolean getChanged(){
		return changed;
	}
	
	public void setChangedFalse(){
		changed = false;
	}
	
	public void setChangedTrue(){
		changed = true;
	}

	public String getName() {
		return (name != null ? name : getFileName());
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFileName() {
		return (fileName != null ? fileName : "");
	}
	
	public void setFileName(String filename) {
		this.fileName = filename;
	}
	
	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}
	
	public void add(Activity activity) {
		if (activity.getParent() == null) {
			activity.setParent(this);
		}
		this.activities.add(activity);
		setChangedTrue();
	}

	public void addActivities(List<Activity> activities) {
		for (Activity activity : activities) {
			add(activity);
		}
	}
	
	public void remove(Activity activity) {
		this.activities.remove(activity);
		setChangedTrue();
	}
	
	public void clearActivities() {
		this.activities = new ArrayList<Activity>();
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}
	
	public void add(Course course) {
		if (course.getParent() == null) {
			course.setParent(this);
		}
		this.courses.add(course);
		setChangedTrue();
	}
	
	public void addCourses(List<Course> courses) {
		for (Course course : courses) {
			add(course);
		}
	}
	
	public void remove(Course course) {
		this.courses.remove(course);
		setChangedTrue();
	}
	
	public void clearCourses() {
		this.courses = new ArrayList<Course>();
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		List<Trackpoint> trackpoints = new ArrayList<Trackpoint>();
		
		for (Activity activity : activities) {
			trackpoints.addAll(activity.getTrackpoints());
		}
		
		for (Course course : courses) {
			trackpoints.addAll(course.getTrackpoints());
		}
		
		return trackpoints;
	}
	
	public List<Waypoint> getWaypoints() {
		return waypoints;
	}

	public void setWaypoints(List<Waypoint> waypoints) {
		this.waypoints = waypoints;
	}
	
	public void add(Waypoint waypoint) {
		waypoint.setParent(this);
		this.waypoints.add(waypoint);
	}
	
	public void addWaypoints(List<Waypoint> waypoints) {
		for (Waypoint waypoint : waypoints) {
			add(waypoint);
		}
	}

	public String getFolderTreeItemName() {
		return getName();
	}

	public ImageIcon getOpenIcon() {
		return icon;
	}

	public ImageIcon getClosedIcon() {
		return icon;
	}

	public ImageIcon getLeafIcon() {
		return icon;
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance()
			.publish(publisher, com.henriquemalheiro.trackit.presentation.event.Event.DOCUMENT_SELECTED, this);
	}
	
	@Override
	public void publishUpdateEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher,
				com.henriquemalheiro.trackit.presentation.event.Event.DOCUMENT_UPDATED, this);
	}
	
	public FileType[] getExportFileTypes() {
		return new FileType[0];
	}

	@Override
	public String toString() {
		return String.format("GPSDocument [name=%s,id=%d]", getName(), getId());
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	@Override
	public String getDocumentItemName() {
		return String.format("%s (%s)", Messages.getMessage("gpsDocument.label"), getName());
	}

	@Override
	public FileType[] getSupportedFileTypes() {
		return new FileType[] { FileType.GPX/*, FileType.KML */};
	}
	
	@Override
	public List<ActionType> getSupportedActions() {
		List<ActionType> supportedActions = new ArrayList<>();
		supportedActions.add(ActionType.NEW_COURSE);
		
		return supportedActions;
	}

	@Override
	public List<DataType> getDisplayableElements() {
		return Arrays.asList(new DataType[] { DataType.DOCUMENT, DataType.ACTIVITY, DataType.COURSE, DataType.WAYPOINT });
	}
	
	@Override
	public List<? extends DocumentItem> getDisplayedElements(DataType dataType) {
		switch (dataType) {
		case DOCUMENT:
			return Arrays.asList(new DocumentItem[] { this });
		case ACTIVITY:
			return getActivities();
		case COURSE:
			return getCourses();
		case WAYPOINT:
			return getWaypoints();
		default:
			return Collections.emptyList();
		}
	}
	
	
	/*public void updateSpeedWithPauseTime(double pauseLimit){
		for(Activity a : activities){
			a.updateSpeedWithPauseTime(pauseLimit);
		}
		for(Course c : courses){
			c.updateSpeedWithPauseTime(pauseLimit);
		}
		
	}*/
}
