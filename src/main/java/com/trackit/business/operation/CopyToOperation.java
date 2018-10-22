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

import javax.print.Doc;
import javax.swing.text.Document;

import org.apache.derby.mbeans.Management;

import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.exception.TrackItException;

public class CopyToOperation extends OperationBase implements Operation {
	
	protected List<Long>            srcItemsIds = null;
	protected List<Long>            srcDocsIds  = null;
	protected List<Long> 		  	dstItemsIds = null;
	protected GPSDocument		  	dstDocument;
	protected boolean				dstDocCreated = false;
	protected DocumentItem 		  	lastItem;
	protected HashSet<GPSDocument> 	modifiedDocuments = new HashSet<>();
	protected List<GPSDocument> 	destroyedDocuments = new ArrayList<>();
	DocumentManager					manager = DocumentManager.getInstance();

	public CopyToOperation() {
	}

	public CopyToOperation(Map<String, Object> options) {
		super(options);
	}
	
	public List<Long> getItemsIds() {
		return srcItemsIds;
	}
	
	public List<Long> getDocsIds() {
		return srcDocsIds;
	}
	
	public List<Long> getDestinationItemsIds() {
		return dstItemsIds;
	}
	
	public long getDocumentId() {
		return dstDocument.getId();
	}
	
	public boolean newDocumentCreated() {
		return dstDocCreated;
	}
	
	public List<GPSDocument> getModifiedDocuments() {
		return new ArrayList<GPSDocument>( modifiedDocuments);
	}
	
	public DocumentItem getLastItem() {
		return lastItem;
	}
	
	public List<GPSDocument> getDestroyedDocuments() {
		return destroyedDocuments;
	}
	
	@Override
	public String getName() {
		return ( Messages.getMessage( "applicationPanel.menu.copyTo"));
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		if ( ( (boolean) options.get( Constants.UndoOperation.UNDO)) )
			undoOperation( document);
		else
			redoOperation( document);
	}

	@Override
	public void process(List<GPSDocument> document) throws TrackItException {
	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		
		dstDocument         = document;
		dstDocCreated       = (boolean)
				                options.get( Constants.CopyToOperation.DESTINATION_DOCUMENT_CREATED);
		List<DocumentItem> items  = (List<DocumentItem>)
								options.get( Constants.CopyToOperation.ITEMS);
		List<Long> docsIDs  = (List<Long>)
								options.get( Constants.CopyToOperation.TARGET_DOCUMENTS_IDS);

		Iterator<Long> iterator = docsIDs.iterator();
		for( DocumentItem item: items) {
			undoAtomicOperation( item, manager.getDocument( iterator.next()));
		}
		if ( dstDocCreated ) {
			destroyedDocuments.add( dstDocument);
			modifiedDocuments.remove( dstDocument);
		}
		else
			modifiedDocuments.add( dstDocument);
	}

	@Override
	public void undoOperation(List<GPSDocument> document) throws TrackItException {
	}

	@Override
	public void redoOperation( GPSDocument document) throws TrackItException {
		
		List<DocumentItem> items  = (List<DocumentItem>)
									options.get( Constants.CopyToOperation.ITEMS);
		List<Long>         dstIds = (List<Long>) 
									options.get( Constants.CopyToOperation.TARGET_ITEMS_IDS);
		
		boolean generateIds = (boolean) options.get( Constants.CopyToOperation.ADD_TO_UNDO_MANAGER);
		if ( generateIds ) {
			srcItemsIds = new ArrayList<>();
			srcDocsIds  = new ArrayList<>();
			for( DocumentItem item: items) {
				srcItemsIds.add( item.getId());
				srcDocsIds.add( item.getParent().getId());
			}
			dstItemsIds = new ArrayList<>();
		}
		if ( document == null ) {
			dstDocument = new GPSDocument( Messages.getMessage( "documentManager.untitledDocument"));
			long dstID = (long) options.get( Constants.CopyToOperation.DESTINATION_DOCUMENT_ID);
			if ( dstID > 0L )
				dstDocument.setId( dstID);
			dstDocCreated = true;
		}
		else
			dstDocument = document;
		
		if ( dstIds == null )
			dstIds = new ArrayList<>( Collections.nCopies( items.size(), -1L));
		
		Iterator<Long> iterator = dstIds.iterator();
		for( DocumentItem item: items) {
			long temp = redoAtomicOperation( item, iterator.next());
			if ( generateIds )
				dstItemsIds.add( temp);
		}
		
		modifiedDocuments.add( dstDocument);
		
		if ( isCopy() ) {
			Map<String, Object> opts = new HashMap<String, Object>();
			opts.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.BASIC);
			try {
				new ConsolidationOperation(options).process( dstDocument);
			} catch (TrackItException e) {
				e.printStackTrace();
			}
		}

		if ( dstDocCreated )
			manager.addDocument( manager.getWorspaceFolder(), dstDocument);
		else
			modifiedDocuments.add( dstDocument);
	}

	@Override
	public void redoOperation(List<GPSDocument> document) throws TrackItException {
	}
	
	protected long redoAtomicOperation( DocumentItem item, long newID) {
		DocumentItem newItem;
		if ( item.isActivity() ) {
			Activity activity = ((Activity) item).clone();
			activity.setParent( dstDocument);
			activity.setFilepath( dstDocument.getFileName());
			dstDocument.add( activity);
			newItem = activity;
		}
		else {
			Course course = ((Course) item).clone();
			course.setParent( dstDocument);
			course.setFilename( dstDocument.getFileName());
			dstDocument.add( course);
			newItem = course;
		}
		if ( newID > 0 )
			newItem.setId( newID);
		lastItem = newItem;
		return newItem.getId();
	}
	
	protected void undoAtomicOperation( DocumentItem item, GPSDocument destinationDocument) {
		if ( item.isActivity() )
			manager.delete( (Activity) item);
		else
			manager.delete( (Course) item);
	}
	
	protected boolean isCopy() {
		return true;
	}

}
