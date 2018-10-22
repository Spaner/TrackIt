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
package com.trackit.presentation.view.folder;

import static com.trackit.business.common.Messages.getMessage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import com.trackit.business.common.FileType;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.TrackItBaseType;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Visitor;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.task.ActionType;
import com.trackit.presentation.utilities.ImageUtilities;

public class CoursesItem extends TrackItBaseType implements FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("courses_16.png");
	private static FileType[] exportFileTypes;
	private GPSDocument parent;
	
	static {
		exportFileTypes = new FileType[] { FileType.FIT, FileType.TCX, FileType.GPX,
				FileType.KML, FileType.CSV };
	}
	
	public CoursesItem(GPSDocument document) {
		this.parent = document;
	}
	
	@Override
	public GPSDocument getParent() {
		return parent;
	}

	@Override
	public String getFolderTreeItemName() {
		return getMessage("folderView.label.courses");
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
		EventManager.getInstance().publish(publisher, Event.COURSES_SELECTED, parent, parent.getCourses());
	}
	
	public FileType[] getExportFileTypes() {
		return exportFileTypes;
	}
	
	@Override
	public List<ActionType> getSupportedActions() {
		List<ActionType> supportedActions = new ArrayList<>();
		supportedActions.add(ActionType.NEW_COURSE);
		
		return supportedActions;
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		List<Trackpoint> trackpoints = new ArrayList<>();
		for (Course course : parent.getCourses()) {
			trackpoints.addAll(course.getTrackpoints());
		}
		return trackpoints;
	}
	
	/* FolderTreeItem Interface Implementation */

	@Override
	public boolean acceptItem(FolderTreeItem item) {
		boolean result = false;
		
		result |= (item.isCourse() && !parent.getCourses().contains(item));
		result |= (item.isActivity());
		result |= (item instanceof CoursesItem);
		result |= (item instanceof ActivitiesItem);
		
		return result;
	}
}
