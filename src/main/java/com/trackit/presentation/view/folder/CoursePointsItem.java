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

import javax.swing.ImageIcon;

import com.trackit.business.common.FileType;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.TrackItBaseType;
import com.trackit.business.domain.Visitor;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.utilities.ImageUtilities;

public class CoursePointsItem extends TrackItBaseType implements FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("course_points_16.png");
	private Course parent;
	
	public CoursePointsItem(Course course) {
		this.parent = course;
	}
	
	@Override
	public DocumentItem getParent() {
		return parent;
	}

	@Override
	public String getFolderTreeItemName() {
		return getMessage("folderView.label.coursePoints");
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
		EventManager.getInstance().publish(publisher, Event.COURSE_POINTS_SELECTED, parent, parent.getCoursePoints());
	}
	
	public FileType[] getExportFileTypes() {
		return new FileType[0];
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
}
