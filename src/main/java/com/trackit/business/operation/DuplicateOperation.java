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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.swing.plaf.synth.SynthSeparatorUI;

import org.apache.http.impl.entity.StrictContentLengthStrategy;

import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.exception.TrackItException;

public class DuplicateOperation extends OperationBase implements Operation {
	
	protected List<Long> srcItemsIds = null;
	protected List<Long> dstItemsIds = null;
	protected List<Long> srcDocsIds  = null;
	protected List<Long> extraIds    = null;
	protected List<GPSDocument> modifiedDocuments = new ArrayList<>();
	protected HashMap<GPSDocument, DocumentItem> lastItems  = new HashMap<>();
	DocumentManager manager          = DocumentManager.getInstance();
	boolean   firstTime              = false;
	static String append             = " " + Messages.getMessage( "document.copy.documentNameSuffix");
	
	public DuplicateOperation() {
	}

	public DuplicateOperation(Map<String, Object> options) {
		super(options);
	}

	public List<Long> getItemsIds() {
		return srcItemsIds;
	}
	
	public List<Long> getDestinationItemsIds() {
		return dstItemsIds;
	}
	
	public List<Long> getExtraIds() {
		return extraIds;
	}
	
	public List<Long> getDocsIds() {
		return srcDocsIds;
	}
	
	public List<DocumentItem> getLastItems() {
		return new ArrayList<>( lastItems.values());
	}
	
	public List<GPSDocument> getModifiedDocuments() {
		return modifiedDocuments;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		if ( (boolean) options.get( Constants.UndoOperation.UNDO) )
			undoOperation(document);
		else
			redoOperation(document);
	}

	@Override
	public void process(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		List<DocumentItem> items  = (List<DocumentItem>) options.get( Constants.DuplicateOperation.ITEMS);
		List<Long>         srcIds = (List<Long>) options.get( Constants.DuplicateOperation.ORIGIN_ITEMS_IDS);
		HashSet<GPSDocument> modified = new HashSet<>();
		Iterator<Long> srcIdIterator = srcIds.iterator();
		for( DocumentItem item: items) {
			long srcId = srcIdIterator.next();
			if ( item.isActivity() ) {
				GPSDocument doc = (GPSDocument) item.getParent();
				manager.delete( (Activity) item);
				modified.add( doc);
				lastItems.put( doc, manager.getDocumentItem(doc, srcId));
			}
			else {
				if ( item.isCourse() ) {
					GPSDocument doc = (GPSDocument) item.getParent();
					manager.delete( (Course) item);
					modified.add( doc);
					lastItems.put( doc, manager.getDocumentItem( doc, srcId));
				}
				else {
					manager.discard( (GPSDocument) item);
				}
			}
		}
		modifiedDocuments = new ArrayList<>( modified);
	}

	@Override
	public void undoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		
		List<DocumentItem> items = (List<DocumentItem>) options.get( Constants.DuplicateOperation.ITEMS);
		firstTime = (boolean) options.get( Constants.DuplicateOperation.ADD_TO_UNDO_MANAGER);
				
		List<Long> dstIds = null;
		List<Long> xIds   = null;
		if ( firstTime ) {
			srcItemsIds = new ArrayList<>();
			srcDocsIds  = new ArrayList<>();
			dstItemsIds = new ArrayList<>();
			extraIds    = new ArrayList<>();
			dstIds = new ArrayList<>( Collections.nCopies( items.size(), -1L));
			int count = 0;
			for( DocumentItem item: items )
				if ( item instanceof GPSDocument )
					count += ((GPSDocument) item).countActivitiesAndCourses();
			if ( count == 0 )
				count = 1;
			xIds = new ArrayList<>( Collections.nCopies( count, -1L));
		}
		else {
			dstIds = (List<Long>) options.get( Constants.DuplicateOperation.IDS_TO_SET);
			xIds    = (List<Long>) options.get( Constants.DuplicateOperation.EXTRA_IDS_TO_SET);
		}
		
		Iterator<Long> itemsIdsIterator = dstIds.iterator();
		Iterator<Long> extraIdsIterator = xIds.iterator();
		HashSet<GPSDocument> modified   = new HashSet<>();
		for( DocumentItem item: items) {
			if ( item.isActivity() ) {
				Activity a = duplicate( (Activity) item, itemsIdsIterator.next(), null, dstIds );
				modified.add( a.getParent());
			}
			else {
				if ( item.isCourse() ) {
					Course c = duplicate( (Course) item, itemsIdsIterator.next(), null, dstIds);
					modified.add( c.getParent());					lastItems.put( c.getParent(), c);
				}
				else {
					GPSDocument doc = duplicate( (GPSDocument) item, itemsIdsIterator.next());
					modified.add( doc);
					for( Activity activity: ((GPSDocument) item).getActivities()) {
						Activity a = duplicate( activity, extraIdsIterator.next(), doc, extraIds);
					}
					for( Course course: ((GPSDocument) item).getCourses()) {
						Course c = duplicate( course, extraIdsIterator.next(), doc, extraIds);
					}
				}
			}
		}
		modifiedDocuments = new ArrayList<>( modified);
		consolidate( modifiedDocuments);
	}

	@Override
	public void redoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}
	
	protected Activity duplicate( Activity activity, long idToSet, GPSDocument parentToSet, List<Long> newIds) {
		Activity newActivity = activity.clone();
		if ( idToSet > 0L )
			newActivity.setId( idToSet);
		GPSDocument parent = (parentToSet != null) ? parentToSet: activity.getParent();
		newActivity.setName( activity.getName() + (parentToSet == null ? append : "") );
		newActivity.setParent( parent);
		newActivity.setFilepath( parent.getFileName());
		parent.add( newActivity);
		if ( firstTime ) {
			if ( parentToSet == null ) {
				srcItemsIds.add( activity.getId());
				srcDocsIds.add(  parent.getId());
				dstItemsIds.add( newActivity.getId());
			}
			else
				extraIds.add( newActivity.getId());
		}
		lastItems.put( parent, newActivity);
		return newActivity;
	}

	protected Course duplicate( Course course, long idToSet, GPSDocument parentToSet, List<Long> newIds) {
		Course newCourse = course.clone();
		if ( idToSet > 0L )
			newCourse.setId( idToSet);
		GPSDocument parent = (parentToSet != null) ? parentToSet: course.getParent();
		newCourse.setName( course.getName() + (parentToSet == null ? append : "") );
		newCourse.setParent( parent);
		newCourse.setFilename( parent.getFileName());
		parent.add( newCourse);
		if ( firstTime ) {
			if ( parentToSet == null ) {
				srcItemsIds.add( course.getId());
				srcDocsIds.add(  parent.getId());
				dstItemsIds.add( newCourse.getId());
			}
			else
				extraIds.add( newCourse.getId());
		}
		lastItems.put( parent, newCourse);
		return newCourse;
	}

	protected GPSDocument duplicate( GPSDocument document, long idToSet) {
		GPSDocument newDocument = new GPSDocument( document.getName() + append);
		if ( idToSet > 0L)
			newDocument.setId( idToSet);
		manager.addDocument( manager.getWorspaceFolder(), newDocument);
		if ( firstTime ) {
			srcItemsIds.add( document.getId());
			srcDocsIds.add(  document.getId());
			dstItemsIds.add( newDocument.getId());
		}
		return newDocument;
	}
	
	protected void consolidate( List<GPSDocument> modified) {
		for( GPSDocument document: modified) {
			Map<String, Object> opts = new HashMap<String, Object>();
			opts.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.BASIC);
			try {
				new ConsolidationOperation(options).process( document);
			} catch (TrackItException e) {
				e.printStackTrace();
			}

		}
	}

}
