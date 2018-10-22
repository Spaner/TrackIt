/*
 * This file is part of Track It!.
 * Copyright (C) 2017 João Brisson Lopes
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

import java.util.Map;

import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;

public class MoveToOperation extends CopyToOperation {

	public MoveToOperation() {
		super();
	}

	public MoveToOperation(Map<String, Object> options) {
		super(options);
	}
	
	public String getName() {
		return Messages.getMessage( "applicationPanel.menu.moveTo" + "  extended");
	}
	
	protected long redoAtomicOperation( DocumentItem item, long newID ) {
		return moveItem(item, (GPSDocument) item.getParent(), dstDocument);
	}
	
	protected void undoAtomicOperation( DocumentItem item, GPSDocument destinationDocument) {
		moveItem( item, dstDocument, destinationDocument);
	}
	
	private long moveItem( DocumentItem item, GPSDocument fromDoc, GPSDocument toDoc) {
		if ( item.isActivity() ) {
			Activity activity = (Activity) item;
			manager.delete( activity);
			toDoc.add(activity);
			activity.setParent( toDoc);
			activity.setFilepath( toDoc.getFileName());
		}
		else {
			Course course = (Course) item;
			manager.delete(course);
			toDoc.add(course);
			course.setParent( toDoc);
			course.setFilepath( toDoc.getFileName());
		}
		lastItem = item;
		modifiedDocuments.add( fromDoc);
		modifiedDocuments.add( toDoc);
		return item.getId();
	}
	
	protected boolean isCopy() {
		return false;
	}

}
