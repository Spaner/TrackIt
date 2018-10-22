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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import com.trackit.business.DocumentManager;
import com.trackit.business.common.FileType;
import com.trackit.business.common.Messages;
import com.trackit.business.database.Database;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.task.ActionType;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.view.data.DataType;
import com.trackit.presentation.view.folder.FolderTreeItem;

public class GPSDocument extends TrackItBaseType implements DocumentItem, FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("document.png");
	
	private List<Activity> activities;
	private List<Course> courses;
	private List<Waypoint> waypoints;
	private String name;
	private String oldName;							// 12335: 2015-09-21
	private String fileName;
	private boolean changed;						// 58406
	private ArrayList<String> trackNames; 			// 12335: 2015-09-18
	
	public GPSDocument(String name) {
		this.name = name;
		this.oldName = "";							// 12335: 2015-09-18

		activities = new ArrayList<Activity>();
		courses = new ArrayList<Course>();
		waypoints = new ArrayList<Waypoint>();
		changed = false;							//58406
		trackNames = new ArrayList<String>();     	// 12335: 2015-09-18
	}
	
	// Global changes check: checks the document and all siblings (activities and courses)
	// 12335 : 2015-07-24 start	
	public boolean hasUnsavedChanges() {
		return changed;
	}
	// 12335 : 2015-07-24 end
	
	public boolean getChanged(){
		return changed;
	}
	
	public void setChangedFalse(){
		setChanged(false);
	}
	
	public void setChangedTrue(){
		setChanged(true);
	}

	private boolean setChanged( boolean changed) {
		if ( TrackStatus.changesAreEnabled() )
			this.changed = changed;
		return this.changed;
	}
	
	// 12335: 2015-09-23  - Support for track change checking when saving/discarding/closing	
	public boolean needsToBeSavedToFile() {
		System.out.println("\n -----> " + name + "(" + oldName +") from " + getFileName());
		System.out.println("Checking document - changed=" + hasUnsavedChanges() + "  renamed=" +wasRenamed());
		if ( hasUnsavedChanges() || wasRenamed() )
			return true;
		System.out.println("Document has no changes\nChecking courses");
		for( Course c: courses )
		{
			System.out.println("Checking course " + c.getName() + " - changed=" + c.getStatus().trackWasChanged() + "  renamed=" +c.wasRenamed());
//			if ( c.getUnsavedChanges() || c.wasRenamed() )
			if ( c.getStatus().trackWasChanged() || c.wasRenamed() )
				return true;
		}
		System.out.println("No courses were changed\nChecking activities");
		for( Activity a: activities )
		{
			System.out.println("Checking activity " + a.getName() + " -  renamed=" +a.wasRenamed());
//			if ( a.getUnsavedChanges() || a.wasRenamed() )
			if ( a.wasRenamed() )
				return true;
		}
		System.out.println("No activities were changed\nDocument needs no saves");
		return false;
	}
	// 12335 ##########################################################################
	
	//12335: 2018-06-29
	@Override
	public boolean isGPSDocument() {
		return true;
	}
	

	
	// 12335: 2016-10-16
	public void resetStatus() {
		if ( TrackStatus.changesAreEnabled() ) {
			setChangedFalse();
			oldName = "";
			for( Course course: courses)
				course.resetStatus();
			for( Activity activity: activities)
				activity.resetStatus();
		}
	}
	
	// 12335: 2016-10-07 -Support for media change checking when saving/discarding/closing
	public boolean hasMediaChanges() {
		for( Course c: courses ) {
			if ( c.getStatus().mediaWasChanged() )
				return true;
		}
		for( Activity a: activities ) {
			if ( a.getStatus().mediaWasChanged() )
				return true;
		}
		return false;
	}
	// 12335 #############################################################################
	
	// 12335: 2016-06-30  - Support for sport/subsport change detection
//	public boolean hasChangedSportOrSubSport() {
//		Database database = Database.getInstance();
//		short sport;
//		short subSport;
//		for( Course c : courses) {
//			sport    = database.getSport( c);
//			subSport = database.getSubSport( c);
//			if ( sport != -1 && subSport != -1 ) {
//				if ( c.getSport().getSportID() != sport || c.getSubSport().getSubSportID() != subSport )
//					return true;
//			}
//		}
//		for( Activity a : activities) {
//			sport    = database.getSport( a);
//			subSport = database.getSubSport( a);
//			if ( sport != -1 && subSport != -1 ) {
//				if ( a.getSport().getSportID() != sport || a.getSubSport().getSubSportID() != subSport )
//					return true;
//			}
//		}
//		return false;
//	}
	// 12335 ##########################################################################
	
	// 12335: 2017-03-18
	public boolean hasChangedDBOnlyData() {
		Database db = Database.getInstance();
		short value;
		for( Course c: courses) {
			value = db.getSport( c);
			if ( value == -1 || c.getSport().getSportID() != value )
				return true;
			value = db.getSubSport( c);
			if ( value == -1 || c.getSubSport().getSubSportID() != value )
				return true;
			value = db.getDifficultyLevel( c);
			if ( value <= 0 || c.getDifficulty().getValue() != value )
				return true;
			value = db.getTrackCondition( c);
			if ( value <= 0 || c.getTrackCondition().getValue() != value )
				return true;
			value = db.getCircuitType( c);
//			if ( value == -1 || c.getCircuitType().getValue() != (value==1) )	//12335: 2017-08-09
			if ( value == -1 || c.getCircuitType().getValue() != value )
				return true;
		}
		for( Activity a: activities) {
			value = db.getSport( a);
			if ( value == -1 || a.getSport().getSportID() != value )
				return true;
			value = db.getSubSport( a);
			if ( value == -1 || a.getSubSport().getSubSportID() != value )
				return true;
			value = db.getDifficultyLevel( a);
			if ( value <= 0 || a.getDifficulty().getValue() != value )
				return true;
			value = db.getTrackCondition( a);
			if ( value <= 0 || a.getTrackCondition().getValue() != value )
				return true;
			value = db.getCircuitType( a);
//			if ( value == -1 || a.getCircuitType().getValue() != (value==1) )	//12335: 2017-08-09
			if ( value == -1 || a.getCircuitType().getValue() != value )
				return true;
		}
		return false;
	}
	
	public String getName() {
		// 12335: 2015-08-06
//		return (name != null ? name : getFileName());  
		if ( name == null )
			name = Messages.getMessage("documentManager.untitledDocument");
		return name;
		// 12335: 2015-08-06
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	//12335: 2015-09-21 - support for renaming  ##############################################
	public void rename(String name) {
		if ( oldName.isEmpty() )
			oldName = this.name;
		setName(name);
		System.out.println("Renaming Document from " + oldName + " to " + name);
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
	// 12335: #############################################################################

	public String getFileName() {
		return (fileName != null ? fileName : "");
	}
	
	public void setFileName(String filename) {
		this.fileName = filename;
	}
	
	public String getUniqueTrackName( String nameProposal, boolean isActivity) {
		String newName = nameProposal;
		String baseName = nameProposal;
		int sufix = 0;
		if ( baseName == null || baseName.isEmpty() ) {
			baseName = Messages.getMessage((isActivity?"activity":"course")+".label") + " #";
		}
		while ( trackNames.contains(newName) ) {
			newName = baseName + " " + Integer.toString(++sufix);
		}
		return newName;
	}
	
	// 12335: 2015-10-06 - Report number of courses and activities	
	public int countActivities() {
		return activities.size();
	}
	
	public int countCourses() {
		return courses.size();
	}
	
	public int countActivitiesAndCourses() {
		return countActivities() + countCourses();
	}
	// 12335

	
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
		setChangedTrue();								//2018-04-14: 12335
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
			.publish(publisher, com.trackit.presentation.event.Event.DOCUMENT_SELECTED, this);
	}
	
	@Override
	public void publishUpdateEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher,
				com.trackit.presentation.event.Event.DOCUMENT_UPDATED, this);
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
		return new FileType[] {
				FileType.GPX, FileType.KML, FileType.TCX, FileType.FITLOG, FileType.NMEA, FileType.CSV };
	}
	
	@Override
	public List<ActionType> getSupportedActions() {
		List<ActionType> supportedActions = new ArrayList<>();
		supportedActions.add(ActionType.NEW_COURSE);
		supportedActions.add(ActionType.COPY);				//12335: 2015-10-13
		
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
	
	// 12335: 2017-03-30
	// Note: clone does not consolidate the new document
	public GPSDocument clone() {
		return clone( DocumentManager.getInstance().getFolder( this), "");
	}
	
	public GPSDocument clone( Folder folder, String nameSuffix) {
		GPSDocument newDocument = new GPSDocument( this.getName() + nameSuffix );
		for( Activity activity: this.getActivities() )
			newDocument.add( activity.clone( newDocument, nameSuffix));
		for( Course course: this.getCourses() )
			newDocument.add( course.clone( newDocument, nameSuffix));
		DocumentManager.getInstance().addDocument( folder, newDocument);
		return newDocument;
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
