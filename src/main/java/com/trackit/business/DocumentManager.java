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

// IMPORTANT NOTICE (12335 - 2018-06-29)
//
// The following methods discard documents, activities and courses but do not remove them from the database
// 		public void discard( GPSDocument)
//		public void delete( Activity)
//		public void delete( Course)
// Discarding a document generates a DOCUMENT_DISCARD event only, no activity or course associated events are raised
// Discarding an activity or a course raises either a ACTIVITY_DISCARDED or COURSE_DISCARDED event 
//	and a DOCUMENT_UPDATED event
//
// The methods that discard documents, activities and courses AND remove them from the database are:
//		public void remove( GPSDocument)
//		public void remove( Activity)
//		public void remove( Course)
// Removing a document generates as many events as those associated with an activity or course removal 
//	plus a DOCUMENT_DISCARD event at the end
// Removing an activity or a course raises either a ACTIVITY_REMOVED or COURSE_REMOVED event 
//	and a DOCUMENT_UPDATED event

package com.trackit.business;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.print.Doc;
import javax.sql.rowset.serial.SerialException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.Document;
import javax.swing.text.Segment;

import org.apache.derby.tools.sysinfo;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;

import com.drew.metadata.exif.GpsDescriptor;
import com.garmin.fit.Decode.RETURN;
import com.trackit.TrackIt;
import com.trackit.business.common.BoundingBox2;
import com.trackit.business.common.ColorSchemeV2;
import com.trackit.business.common.Constants;
import com.trackit.business.common.JoinOptions;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.common.Pair;
import com.trackit.business.common.SetPaceMethod;
import com.trackit.business.common.WGS84;
import com.trackit.business.database.Database;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CourseLap;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.DifficultyLevelType;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.FieldGroup;
import com.trackit.business.domain.Folder;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.SegmentType;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;
import com.trackit.business.domain.TrackConditionType;
import com.trackit.business.domain.TrackSegment;
import com.trackit.business.domain.TrackStatus;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Waypoint;
import com.trackit.business.exception.ReaderException;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.operation.ActivityToCourseOperation;
import com.trackit.business.operation.AddLapOperation;
import com.trackit.business.operation.ColorGradingOperation;
import com.trackit.business.operation.CompareSegmentsOperation;
import com.trackit.business.operation.ConsolidationLevel;
import com.trackit.business.operation.ConsolidationOperation;
import com.trackit.business.operation.CopyToOperation;
import com.trackit.business.operation.DetectClimbsDescentsOperation;
import com.trackit.business.operation.DuplicateOperation;
import com.trackit.business.operation.JoiningOperation;
import com.trackit.business.operation.MarkingOperation;
import com.trackit.business.operation.MoveToOperation;
import com.trackit.business.operation.Operation;
import com.trackit.business.operation.PauseDetectionPicCaseOperation;
import com.trackit.business.operation.RemoveLapOperation;
import com.trackit.business.operation.RemovePausesOperation;
import com.trackit.business.operation.ReverseOperation;
import com.trackit.business.operation.SettingPaceOperation;
import com.trackit.business.operation.SplitIntoSegmentsOperation;
import com.trackit.business.operation.TrackSimplificationOperation;
import com.trackit.business.operation.TrackSplittingOperation;
import com.trackit.business.operation.UndoItem;
import com.trackit.business.operation.UndoManagerCustom;
import com.trackit.business.operation.UndoableActionType;
import com.trackit.business.operation.RemovePausesOperation.PauseInformation;
import com.trackit.business.reader.Reader;
import com.trackit.business.reader.ReaderFactory;
import com.trackit.business.utilities.Utilities;
import com.trackit.presentation.CompareSegmentDialog;
import com.trackit.presentation.SplitIntoSegmentsDialog;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;
import com.trackit.presentation.utilities.DBOnlyDataSelectionDialog;
import com.trackit.presentation.utilities.DocumentSelectionDialog;
import com.trackit.presentation.utilities.OperationsFactory;
import com.trackit.presentation.view.folder.ActivitiesItem;
import com.trackit.presentation.view.folder.CoursesItem;
import com.trackit.presentation.view.map.MapView;
import com.trackit.presentation.view.map.SphericalMercatorProjection;
import com.trackit.presentation.view.map.Map.MapMode;
import com.trackit.presentation.view.map.layer.EditionLayer;
import com.trackit.presentation.view.map.layer.LegendLayer;
import com.trackit.presentation.view.map.layer.MapLayer;
import com.trackit.presentation.view.map.provider.MapProvider;
import com.trackit.presentation.view.map.provider.RoutingType;
import com.trackit.presentation.view.map.provider.TransportMode;

public class DocumentManager implements EventPublisher, EventListener {
	public static final String FOLDER_WORKSPACE = "Workspace";
	public static final String FOLDER_LIBRARY = "Library";

	private static DocumentManager documentManager;
	private List<Folder> folders;
	private Map<Long, DocumentEntry> documents;
	private GPSDocument defaultCollectionDocument = null;		//12335: 2017-03-09
	private DocumentItem selectedItem;
	private Course selectedCourse;
	private Course previousSelectedCourse;
	private Long selectedDocumentId;
	private Folder selectedFolder;
	private Database database;
	private UndoManagerCustom undoManager; // 57421
	private UndoItem undoItem;
	private MapMode mapMode;
	private boolean updateLegend;
	private boolean updateEdition;

	private List<Pair<Boolean, Course>> joinedCoursesInfo;
	private boolean originalCourse;
	private boolean createdCourse;
	private boolean appendJoin;

	private boolean mergeJoin;

	private enum ReadMode {
		OPEN, IMPORT
	};

	private static Logger logger = Logger.getLogger(DocumentManager.class.getName());

	static {
		documentManager = new DocumentManager();
		EventManager.getInstance().register(documentManager);
	}

	private DocumentManager() {
		init();
	}

	public Database getDatabase() {
		return database;
	}

	public synchronized static DocumentManager getInstance() {
		return documentManager;
	}

	private void init() {
		folders = new ArrayList<Folder>();
		folders.add( new Folder(FOLDER_WORKSPACE));
		folders.add( new Folder(FOLDER_LIBRARY));
		selectedFolder = getFolder(FOLDER_WORKSPACE);
		mapMode = MapMode.MULTI;
		updateLegend = false;
		updateEdition = false;

		documents = new HashMap<Long, DocumentManager.DocumentEntry>();

		database = Database.getInstance();

		undoManager = new UndoManagerCustom(); // 57421
		joinedCoursesInfo = new ArrayList<Pair<Boolean, Course>>();
		originalCourse = true;
		createdCourse = false;
		appendJoin = false;
		mergeJoin = true;
	}

	// 57421
	public UndoManagerCustom getUndoManager() {
		return undoManager;
	}
	
	//57421
	public void setMapMode(MapMode mode){
		this.mapMode = mode;
	}
	
	//57421
	public boolean updateLegend(){
		return updateLegend;
	}
	
	//57421
	public boolean updateEdition(){
		return updateEdition;
	}
	
	//57421
	public void setUpdateLegend(boolean update){
		this.updateLegend = update;
	}
	//57421
	public void setUpdateEdition(boolean update){
		this.updateEdition = update;
	}
	
	//12335: 2017-03-30
	public GPSDocument getDocument( long documentID) {
		DocumentEntry entry = documents.get( documentID);
		if ( entry != null )
			return entry.getDocument();
		if ( getDefaultCollectionDocument().getId() == documentID )
			return getDefaultCollectionDocument();
		return null;
	}


	public List<GPSDocument> getDocuments(String folderName) {
		for (Folder folder : folders) {
			if (folder.getName().equals(folderName)) {
				return folder.getDocuments();
			}
		}

		return new ArrayList<GPSDocument>();
	}

	public List<GPSDocument> getDocuments() {
		List<GPSDocument> documentList = new ArrayList<GPSDocument>();

		for (DocumentEntry entry : documents.values()) {
			documentList.add(entry.getDocument());
		}

		return documentList;
	}
	
	//12335: 2017-03-09
	public void setDefaultCollectionDocument( GPSDocument document) {
		if ( document != null )
			defaultCollectionDocument = document;
	}
	
	// 12335: 2017-03-09
	public GPSDocument getDefaultCollectionDocument() {
		return defaultCollectionDocument;
	}

	public List<Folder> getFolders() {
		return folders;
	}

	public Folder getFolder(GPSDocument document) {
		Folder folder = null;

		for (Folder currentFolder : folders) {
			if (currentFolder.getDocuments().contains(document)) {
				folder = currentFolder;
				break;
			}
		}

		return folder;
	}

	public GPSDocument getSelectedDocument() {
		if (selectedDocumentId == null) {
			return null;
		}

		DocumentEntry entry = documents.get(selectedDocumentId);
		if (entry == null) {
			return null;
		}

		return entry.getDocument();
	}

	public Folder getSelectedFolder() {
		return selectedFolder;
	}

	public GPSDocument createDocument(String folderName) {
		return createDocument(folderName, Messages.getMessage("documentManager.untitledDocument"));
	}

	public GPSDocument createDocument(String folderName, String documentName) {
		Folder folder = getFolder(folderName);
		if (folder == null) {
			return null;
		}

		GPSDocument document = new GPSDocument(documentName);
		folder.add(document);

		DocumentEntry entry = new DocumentEntry(folder, document);
		documents.put(document.getId(), entry);

		return document;
	}
	
	//12335: 2017-03-10 - created
	public void clearDocumentContents( GPSDocument document) {
		for( Activity activity: new ArrayList<>( document.getActivities()) ) {
			this.delete( activity);
		}
		for( Course course: new ArrayList<Course>( document.getCourses()) ) {
			this.delete( course);
		}
		EventManager.getInstance().publish( DocumentManager.this, 
											Event.DOCUMENT_UPDATED, document);
	}
	
	public Folder getWorspaceFolder() {    			//12335: 2016-06-11
		return getFolder( FOLDER_WORKSPACE);
	}
	
	public boolean isWorspaceFolder( Folder folder) {    	//12335: 2016-10-12
		return getFolder( FOLDER_WORKSPACE).equals( folder);
	}
	
	public Folder getLibraryFolder() {   			//12335: 2016-06-11
		return getFolder( FOLDER_LIBRARY);
	}

	public Folder getFolder(String folderName) {
		Folder targetFolder = null;

		for (Folder folder : folders) {
			if (folder.getName().equals(folderName)) {
				targetFolder = folder;
				break;
			}
		}

		return targetFolder;
	}
	
// DOCUMENT LOAD (start)

	public void initFromDB() {
		openDocumentsFromDB();
//		updateSports();						//12335: 2018-07-16 - no longer needed, DB start does it		
	}

	//12335: 2017-03-11 - imports correctly into Collection
	public void importDocuments(File[] files) {
		if ( selectedDocumentId == getDefaultCollectionDocument().getId() ) {
			importToCollection( files);
		}
		else
			readControl( Arrays.asList( files), ReadMode.IMPORT);
	}
	
	public void openDocument(File file) {
		readControl(new ArrayList<>(Arrays.asList(file)), ReadMode.OPEN);
	}
			
	public void openDocumentsFromDB() {
		openDocumentsForFolder( getFolder(FOLDER_LIBRARY));
		openDocumentsForFolder( getFolder(FOLDER_WORKSPACE));
	}
	
	private void openDocumentsForFolder( Folder folder) {
		System.out.println( "Opening docs from " + 
							(isWorspaceFolder( folder) ? "WORKSPACE" : "LIBRARY"));
		openDocumentsForFolder( database.initFromDb(folder), folder);
	}
	
	public void openDocumentsForFolder( final List<String> filenames, Folder folder) {
		System.out.println( "# documents to open " + filenames.size());
		for( String filename: filenames)
			if ( isWorspaceFolder( folder) )
					read( new ArrayList<File>(Arrays.asList(new File(filename))),
						  ReadMode.OPEN, folder);
				else
					selectiveImport( getDefaultCollectionDocument(), folder, filename, 
							         database.getDocumentActivitiesNames( filename, folder),
							         database.getDocumentCoursesNames( filename, folder));
	}
	
	//12335: 2017-03-10
	public void selectiveImport( GPSDocument destinationDocument, Folder folder, String fromFile,
								  List<String> activiesToImport, List<String> coursesToImport) {
		// 2018-03-09: 12335 - Until further notice, do not load any TCX 
		if ( fromFile.toLowerCase().endsWith( ".tcx") )
			return;
		// 2017-06-06: 12335 - do not load files that do not exist
		File file = new File( fromFile);
		if ( ! file.exists() || ( activiesToImport.isEmpty() && coursesToImport.isEmpty()) )
			return;
		// import to a temporary document
		GPSDocument temporary = new GPSDocument( "Temporary");
		Long currentDocumentId = selectedDocumentId;
		selectedDocumentId     = temporary.getId();
		addDocument( folder, temporary);
//		read( new ArrayList<File>(Arrays.asList( new File( fromFile))), ReadMode.IMPORT, folder);
		read( new ArrayList<File>(Arrays.asList( file)), ReadMode.IMPORT, folder); //12335: 2017-06-06
		
		// move selected activities to the destination document (count how many)
		HashSet<String> activities = new HashSet<>( activiesToImport);
		DocumentItem lastItem = null;
		int selectedItems = 0;
		for( Activity activity: new ArrayList<Activity>( temporary.getActivities()) ) {
			System.out.println( "Activity name: " + activity.getName());
			if ( activities.contains( activity.getName())) {
				activity.setFilepath( fromFile);
				activity.setParent( destinationDocument);
				lastItem = activity;
				selectedItems++;
				destinationDocument.add( activity);
			}
			temporary.remove( activity);
		}
		
		// move selected courses to the destination document (count how many)
		HashSet<String> courses = new HashSet<>( coursesToImport);
		for( Course course: new ArrayList<Course>( temporary.getCourses()) ) {
			System.out.println( "Course name: " + course.getName());
			if ( courses.contains( course.getName()) ) {
				course.setFilepath( fromFile);
				course.setParent( destinationDocument);
				lastItem = course;
				selectedItems++;
				destinationDocument.add( course);
			}
			temporary.remove( course);
		}
		
		// housekeeping
		discard( temporary);
		Map<String, Object> options = new HashMap<>();
		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.BASIC);
		try {
			new ConsolidationOperation(options).process( destinationDocument);
		} catch (TrackItException e) {
			showAndLogErrorMessage(e.getMessage());
			return ;
		}
		EventManager.getInstance().publish(DocumentManager.this,
				Event.DOCUMENT_UPDATED,
				destinationDocument);
		selectedDocumentId = currentDocumentId;
		// Highlight last activity/course if any has been selected
		if ( selectedItems > 0 ) {
			if ( lastItem.isActivity() )
				((Activity) lastItem).publishSelectionEvent( DocumentManager.this);
			else
				((Course) lastItem).publishSelectionEvent( DocumentManager.this);
		} else
			System.out.println( Messages.getMessage( "documentManager.message.noTracksLoaded"));
	}
	
	//12335: 2017-03-11
	// Imports a complete document into Collection
	public void importToCollection( File[] files) {
		for ( File file: files) {
			String filename = file.getAbsolutePath();
			GPSDocument temporary = new GPSDocument( "Temporary");
			GPSDocument destination = getDefaultCollectionDocument();
			Long currentDocumentId = selectedDocumentId;
			selectedDocumentId     = temporary.getId();
			Folder folder          = getLibraryFolder();
			DocumentItem lastItem  = null;
			addDocument( folder, temporary);
			read( new ArrayList<File>(Arrays.asList( new File( filename))), ReadMode.IMPORT, folder);
			for( Activity a: temporary.getActivities() ) {
				System.out.print( "Import to Collection " + a.getId() + "-  "+ a.getParent().getId());
				a.setFilepath( filename);
				a.setParent( destination);
				System.out.println( "  " + a.getParent().getId());
				lastItem = a;
			}
			destination.addActivities( temporary.getActivities());
			for( Course c: temporary.getCourses() ) {
				System.out.print( "Import to Collection " + c.getId() + "-  " + c.getParent().getId());
				c.setFilepath( filename);
				c.setParent( destination);
				System.out.println( "  " + c.getParent().getId());
				lastItem = c;
			}
			destination.addCourses( temporary.getCourses());
			discard( temporary);
			EventManager.getInstance().publish(DocumentManager.this,
				Event.DOCUMENT_UPDATED,
				destination);
			selectedDocumentId = currentDocumentId;
			if ( lastItem.isActivity() )
				((Activity) lastItem).publishSelectionEvent( DocumentManager.this);
			else
				((Course) lastItem).publishSelectionEvent( DocumentManager.this);
		}
	}
	
	//12335: 2018-03-05 - provide a name to single track documents that store no tracknames
	public String getSingleTrackDocumentTrackName( String filepath ) {
		String name = "";
		List<String> answer = database.getActivityNames( filepath);
		if ( answer.size() > 0 ) 
			name = answer.get( 0);
		else {
			answer = database.getCourseNames( filepath);
			if ( answer.size() > 0 )
				name = answer.get( 0);
		}
		if ( name.isEmpty() ) {
			name = Paths.get( filepath).getFileName().toString();
			name = name.substring(0, name.lastIndexOf('.'));
		}
		return name;
	}
	
	private void list( GPSDocument doc, String title) {
		System.out.print( "Document: " + doc.getName() + "   -  " + title + "\nActivities:  ");
		for( Activity a: doc.getActivities() )
			System.out.print( a.getName() + "  ");
		System.out.print( "\nCourses:  ");
		for( Course c: doc.getCourses())
			System.out.print( c.getName() + "  ");
		System.out.println( );
	}
		
	private void readControl( final List<File> files, final ReadMode mode) {
		readControl(files, mode, getWorspaceFolder());
	}
		
	private void readControl( final List<File> files, final ReadMode mode, final Folder targetFolder) {
		new Task( new Action() {
			
			@Override
			public String getMessage() {
				if ( mode.equals( ReadMode.OPEN) )									//12335: 2018-07-08
					return Messages.getMessage( "readMode.open.progressMessage");
				return Messages.getMessage( "readMode.import.progressMessage");
			}
			
			@Override
			public Object execute() throws TrackItException {
				if ( mode == ReadMode.OPEN) {
					for( File file: files)
						read( new ArrayList<File>(Arrays.asList(file)) , mode, targetFolder);
				}
				else
					read( files, mode, targetFolder);
				return null;
			}
			
			@Override
			public void done(Object result) {
				// TODO Auto-generated method stub
				
			}
		}).execute();
	}

	private GPSDocument read( final List<File> files, final ReadMode mode, final Folder targetFolder) {
		InputStream inputStream = null;
		Reader      reader      = null;
		Map<String, Object> options = new HashMap<>();
		List<Course> voidCourses = new ArrayList<>();
		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.BASIC);
		GPSDocument selectedDocument = null;
		GPSDocument document = null;
		
		if ( mode == ReadMode.OPEN)
//			ChangesSemaphore.Disable();		//12335: 2016-10-03
			TrackStatus.disableChanges();
		
		for( File file: files ) {
			String documentFilename = file.getAbsolutePath();
			// Avoid reopening the same document
			boolean isOpen = false;
			if ( mode == ReadMode.OPEN && targetFolder == getWorspaceFolder() ) {
				for(GPSDocument doc: getWorspaceFolder().getDocuments() )
					if ( doc.getFileName().equals( documentFilename) ) {
						isOpen = true;
						break;
					}
			}
			if ( isOpen ) {
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
						Messages.getMessage("documentManager.message.reloadAttempt",
											file.getAbsolutePath()),
						Messages.getMessage("trackIt.error"), JOptionPane.INFORMATION_MESSAGE);
				logger.error(Messages.getMessage("documentManager.message.reloadAttempt",
												file.getAbsolutePath()));
				selectedDocument = documents.get(selectedDocumentId).getDocument();
				continue;
			}
			// OK, proceed to read document's file
			if ( file.exists() ) {
				selectedDocument = ( mode.equals(ReadMode.IMPORT) ?
						documents.get(selectedDocumentId).getDocument():
						new GPSDocument(Messages.getMessage("documentManager.untitledDocument")));
				try {			
					inputStream = getInputStream(file);
					reader = ReaderFactory.getInstance().getReader(file, null);
					document = reader.read(inputStream, documentFilename);
	System.out.println( "Document filename is " + documentFilename);
					document.setFileName( documentFilename);
					addDocumentDBOnlyData( document);
					mergeDocuments(document, selectedDocument);
				} catch ( ReaderException e) {
					return null;
				} finally {
					closeInputStream(inputStream);
				}
			}
			else {
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
						Messages.getMessage("documentManager.message.fileNotFound",
											file.getAbsolutePath()),
						Messages.getMessage("trackIt.error"), JOptionPane.INFORMATION_MESSAGE);
				logger.error(Messages.getMessage("documentManager.message.fileNotFound",
												file.getAbsolutePath()));
				continue;
			}
			
			// Post read housekeeping - document
			if ( mode == ReadMode.OPEN) {
				selectedDocument.setFileName(documentFilename);
				selectedDocument.setName(document.getName());
			}
			HashMap<DocumentItem, Boolean> tracksWithoutSport = new HashMap<>();
			// Post read housekeeping - activities
			for ( Activity a: document.getActivities() ) {
				if (mode == ReadMode.IMPORT ) {
//					a.setUnsavedTrue();			// 12335 : 2016-10-03
					a.setTrackStatusTo( true);
				}
				if ( a.getTrackpoints().get(0).getSpeed() != null )
					a.setNoSpeedInFile(false);
				
			}
						
			// Post read housekeeping - courses
			// Courses without trackpoints must be temporarily removed before consolidation
			for( Iterator<Course> iterator = selectedDocument.getCourses().iterator(); iterator.hasNext(); ) {
				Course c = iterator.next();
				if ( mode == ReadMode.IMPORT ) {
//					c.setUnsavedTrue();				// 12335 : 2016-10-03
					c.setTrackStatusTo( true);
				}
				if ( !c.getTrackpoints().isEmpty() ) {
					if ( c.getTrackpoints().get(0).getSpeed() != null )
						c.setNoSpeedInFile(false);
				}
				else {
					iterator.remove();
					voidCourses.add(c);
				}
			}
		}
			
		try {
			new ConsolidationOperation(options).process(selectedDocument);
			//12335: 2018-07-02
			for( Activity activity: selectedDocument.getActivities() ) {
				activity.setCircuitType();
			}
			for( Course course: selectedDocument.getCourses()) {
				course.setCircuitType();
			}
		} catch (TrackItException e) {
			showAndLogErrorMessage(e.getMessage());
			return null;
		}
				
		// Courses without trackpoints can now be inserted back
		for( Course c: voidCourses)
			selectedDocument.getCourses().add(c);
		
		if ( mode.equals(ReadMode.IMPORT) ) {
			EventManager.getInstance().publish(DocumentManager.this,
												Event.DOCUMENT_UPDATED,
												selectedDocument);
		}
		else
			addDocument(targetFolder, selectedDocument);

		// Load pictures if any
		if ( mode == ReadMode.OPEN)
			loadPictures(selectedDocument, selectedDocument.getFileName());
		else
			for( File file: files )
				if ( file.exists() )
					loadPictures(selectedDocument, file.getAbsolutePath());

		// Correct anomalies (right after loading pictures)
		correctAnomalies(selectedDocument);
				
		// Turn to green the changes semaphore 
		if ( mode == ReadMode.OPEN )
//			ChangesSemaphore.Enable();		//12335:2016-10-03
			TrackStatus.enableChanges();
		
		if ( targetFolder != getFolder(FOLDER_LIBRARY) ) {
		selectDocumentsFirstItem(selectedDocument);
		TrackIt.getApplicationPanel().getMapView().zoomToFitFeature(selectedItem);
		}
		
		return selectedDocument;
	}
	
	//12335: 2017-03-16
	private void addDocumentDBOnlyData( GPSDocument document ) {
		HashMap<DocumentItem, Boolean> incompleteDataTracks = new HashMap<>();
		short tmp, sport, subSport;
		
		// Complete Activity data from DB
		for( Activity activity: document.getActivities()) {
			activity.setFilepath( document.getFileName());
			// Sport and SubSport
			sport    = database.getSport( activity);
			subSport = database.getSubSport( activity);
			if ( sport != -1 && subSport != -1 ) {
//12335: 2017-07-15
//				activity.setSportAndSubSport( SportType.lookup( sport), SubSportType.lookup( subSport));
				activity.setSportAndSubSport( SportType.lookup( sport), SubSportType.lookup( sport, subSport));
			}
			else
				incompleteDataTracks.put( activity, true);
			// Difficulty Level
			tmp = database.getDifficultyLevel( activity);
			if ( tmp != -1 )
				activity.setDifficulty( DifficultyLevelType.lookup( tmp));
			else
				activity.setDifficulty( DifficultyLevelType.UNDEFINED);
			if ( activity.getDifficulty() == DifficultyLevelType.UNDEFINED )
				incompleteDataTracks.put( activity, true);
			// Track Condition
			tmp = database.getTrackCondition( activity);
			if ( tmp != -1 )
				activity.setTrackCondition( TrackConditionType.lookup( tmp));
			else
				activity.setTrackCondition( TrackConditionType.UNDEFINED);
			if ( activity.getTrackCondition() == TrackConditionType.UNDEFINED)
				incompleteDataTracks.put( activity, true);
		}
		
		// Complete Course data from DB
		for( Course course: document.getCourses() ) {
			course.setFilepath( document.getFileName());
			// Sport and SubSport
			sport    = database.getSport( course);
			subSport = database.getSubSport( course);
			if ( sport != -1 && subSport != -1 )
//12335: 2017-07-15
//				course.setSportAndSubSport( SportType.lookup( sport), SubSportType.lookup( subSport));
				course.setSportAndSubSport( SportType.lookup( sport), SubSportType.lookup( sport, subSport));
			else
				incompleteDataTracks.put( course, false);
			// Difficulty Level
			tmp = database.getDifficultyLevel( course);
			if ( tmp != -1 )
				course.setDifficulty( DifficultyLevelType.lookup( tmp));
			else
				course.setDifficulty( DifficultyLevelType.UNDEFINED);
			if ( course.getDifficulty() == DifficultyLevelType.UNDEFINED )
				incompleteDataTracks.put( course, false);
			// Track Condition
			tmp = database.getTrackCondition( course);
			if ( tmp != -1 )
				course.setTrackCondition( TrackConditionType.lookup( tmp));
			else
				course.setTrackCondition( TrackConditionType.UNDEFINED);
			if ( course.getTrackCondition() == TrackConditionType.UNDEFINED)
				incompleteDataTracks.put( course, false);
		}
		
		// Exit if all data obtained from DB
		if ( incompleteDataTracks.isEmpty() )
			return;
		
		// Ask user to supply any missing data
		SportType           sportType;
		SubSportType        subSportType;
		DifficultyLevelType difficulty;
		TrackConditionType  condition;
		Iterator<DocumentItem> iterator = incompleteDataTracks.keySet().iterator();
		while( iterator.hasNext() ) {
			DocumentItem item  = iterator.next();
			boolean isActivity = incompleteDataTracks.get( item);
			if ( isActivity )
				DBOnlyDataSelectionDialog.showSelectionDialog( (Activity) item, iterator.hasNext(), false);
			else
				DBOnlyDataSelectionDialog.showSelectionDialog( (Course) item, iterator.hasNext(), false);
			if ( DBOnlyDataSelectionDialog.sameForAll() ) {
				sportType      = DBOnlyDataSelectionDialog.selectedSport();
				subSportType   = DBOnlyDataSelectionDialog.selectedSubSport();
				difficulty = DBOnlyDataSelectionDialog.selectedDifficultyLevel();
				condition  = DBOnlyDataSelectionDialog.selectedTrackCondition();
				while( iterator.hasNext() ) {
					item = iterator.next();
					isActivity = incompleteDataTracks.get( item);
					if ( isActivity ) {
						((Activity) item).setSportAndSubSport(sportType, subSportType);
						((Activity) item).setDifficulty( difficulty);
						((Activity) item).setTrackCondition( condition);
					}
					else {
						((Course) item).setSportAndSubSport(sportType, subSportType);
						((Course) item).setDifficulty( difficulty);
						((Course) item).setTrackCondition( condition);
					}
				}
			}
		}
		
	}
	
	
	// TODO : Uncomment the code
	// 12335: 2015-10-05 - Correct anomalies after opening a document
	// So far, looks for documents with incomplete GPSFiles entries (IsActivity=-1)
	// and fixes their entries	
	private void correctAnomalies( GPSDocument document) {
		database.correctTracksWithInterimIsActivityValue(document);
	}


	private void loadPictures( final GPSDocument document, final String sourceFilename) {
		HashMap<String, ArrayList<String>> pictureFilesList =
										database.getPicturesFromDB(sourceFilename);
		if ( !pictureFilesList.isEmpty() ) {
			File[] files = null;
			ArrayList<String> names;
			int index;
			for( Activity a: document.getActivities() )
				if ( (names=pictureFilesList.get(a.getName())) != null ) {
					files = new File[names.size()];
					index = 0;
					for( String name: names)
						files[index++] = new File(name);
					a.addPictures(files);
				}
			for (Course c: document.getCourses() ) 
				if ( (names=pictureFilesList.get(c.getName())) != null ) {
					files = new File[names.size()];
					index = 0;
					for( String name: names)
						files[index++] = new File(name);
					c.addPictures(files);
				}
		}
	}
	
	
	private void selectDocumentsFirstItem(GPSDocument document) {
		if (!document.getActivities().isEmpty() ) {
			document.getActivities().get(0).publishSelectionEvent(DocumentManager.this);
		} else if (!document.getCourses().isEmpty() ) {
			document.getCourses().get(0).publishSelectionEvent(DocumentManager.this);
		} else if (!document.getWaypoints().isEmpty()) {
			document.getWaypoints().get(0).publishSelectionEvent(DocumentManager.this);
		}
	}
	
	//12335 - 2017-04-08
	private void selectDocumentItem( DocumentItem item) {
		if ( item.isCourse() )
			((Course) item).publishSelectionEvent( DocumentManager.this);
		else {
			if ( item.isActivity() )
				((Activity) item).publishSelectionEvent( DocumentManager.this);
			else
				if ( item instanceof GPSDocument )
					selectDocumentsFirstItem( (GPSDocument) item);
		}
	}

// DOCUMENT LOAD (end)
	
	
	private InputStream getInputStream(final File file) throws ReaderException {
		ProgressMonitorInputStream progressMonitor;
		BufferedInputStream in;
		try {
			progressMonitor = new ProgressMonitorInputStream(TrackIt.getApplicationFrame(),
					"Reading " + file.getAbsolutePath(), new FileInputStream(file));
			in = new BufferedInputStream(progressMonitor);
		} catch (FileNotFoundException fnfe) {
			throw new ReaderException(fnfe.getMessage());
		}
		return in;
	}

	private void closeInputStream(InputStream inputStream) {
		try {
			inputStream.close();
		} catch (IOException e) {
			logger.debug("Failed to close input stream.");
		}
	}

	private void showAndLogErrorMessage(final String message) {
		logger.error(message);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), message,
						Messages.getMessage("documentManager.title.error"), JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	// 12335: 2018-07-07
	public void delete( Activity activity) {
		discard( activity, false);
	}
//	public void delete(Activity activity) {
//		for (Folder currentFolder : folders) {
//			for (GPSDocument document : currentFolder.getDocuments()) {
//				if (document.getActivities().contains(activity)) {
//					if ( ! activity.getPictures().isEmpty() )				//12335: 2018-07-07
//						activity.removePictures();
//					document.remove(activity);
////					EventManager.getInstance().publish(this, Event.ACTIVITY_REMOVED, activity);
//					EventManager.getInstance().publish(this, Event.ACTIVITY_DISCARDED, activity); //12335: 2018-06-29
//					EventManager.getInstance().publish( this, Event.DOCUMENT_UPDATED, document);
//					return;
//				}
//			}
//		}
//	}

	// 12335: 2018-07-07
	public void delete( Course course) {
		discard( course, false);
	}
//	public void delete(Course course) {
//		for (Folder currentFolder : folders) {
//			for (GPSDocument document : currentFolder.getDocuments()) {
//				if (document.getCourses().contains(course)) {
//					if ( ! course.getPictures().isEmpty() )				//12335: 2018-07-07
//						course.removePictures();
//					document.remove(course);
////					EventManager.getInstance().publish(this, Event.COURSE_REMOVED, course);
//					EventManager.getInstance().publish(this, Event.COURSE_DISCARDED, course);	//12335: 2018-06-29
//					EventManager.getInstance().publish( this, Event.DOCUMENT_UPDATED, document);
//					return;
//				}
//			}
//		}
//	}

	// 12335: 2018-07-07
	public void discard( GPSDocument document) {
		discard( document, false);
	}
//	public void discard(GPSDocument document) {
//		for( Activity activity: document.getActivities() ) 				//12335: 2018-07-07
//			if ( ! activity.getPictures().isEmpty() )
//				activity.removePictures();
//		for( Course course: document.getCourses() )					 	//12335: 2018-07-07
//			if ( ! course.getPictures().isEmpty() )
//				course.removePictures();
//		Folder folder = getFolder(document);
//		folder.remove(document);
//		documents.remove(document.getId());// 58406
//
//		EventManager.getInstance().publish(this, Event.DOCUMENT_DISCARDED, document);
//	}
	
	// 12335: 2018-07-07
	public void discard( GPSDocument document, boolean removeFromDB) {
		Folder folder = getFolder(document);
		folder.remove(document);
		for( Activity activity: document.getActivities())
			discard( activity, removeFromDB);
		for( Course course: document.getCourses())
			discard( course, removeFromDB);
		documents.remove(document.getId());// 58406
		documents.remove( document);

		EventManager.getInstance().publish(this, Event.DOCUMENT_DISCARDED, document);
	}
	
	// 12335: 2018-07-07
	public void discard( Activity activity, boolean removeFromDB) {
		for (Folder currentFolder : folders) {
			for (GPSDocument document : currentFolder.getDocuments()) {
				if (document.getActivities().contains(activity)) {
					document.remove(activity);
					EventManager.getInstance().publish(
							this, 
							removeFromDB ? Event.ACTIVITY_REMOVED: Event.ACTIVITY_DISCARDED, 
							activity);
					EventManager.getInstance().publish( this, Event.DOCUMENT_UPDATED, document);
				}
				if ( ! activity.getPictures().isEmpty() )
					activity.removePictures();
				if ( removeFromDB )
					database.deleteFromDB(activity);
			}
		}
	}
	
	// 12335: 2018-07-07
	public void discard( Course course, boolean removeFromDB) {
		for (Folder currentFolder : folders) {
			for (GPSDocument document : currentFolder.getDocuments()) {
				if (document.getCourses().contains( course)) {
					document.remove( course);
					EventManager.getInstance().publish(
							this, 
							removeFromDB ? Event.COURSE_REMOVED: Event.COURSE_DISCARDED, 
									course);
					EventManager.getInstance().publish( this, Event.DOCUMENT_UPDATED, document);
				}
				if ( ! course.getPictures().isEmpty() )
					course.removePictures();
				if ( removeFromDB )
					database.deleteFromDB( course);
			}
		}
	}

	// 12335: 2014-09-22	
	public void fullyMergeDocuments( GPSDocument source, GPSDocument destination) {
		mergeDocuments(source, destination);
		destination.setFileName(source.getFileName());
		destination.setName(source.getName());
	}

	private void mergeDocuments(GPSDocument source, GPSDocument destination) {
		for (Activity activity : source.getActivities()) {
			activity.setParent(destination);
		}
		destination.addActivities(source.getActivities());

		for (Course course : source.getCourses()) {
			course.setParent(destination);
		}
		destination.addCourses(source.getCourses());

		for (Waypoint waypoint : source.getWaypoints()) {
			waypoint.setParent(destination);
		}
		destination.addWaypoints(source.getWaypoints());

		selectedDocumentId = destination.getId();
		selectedFolder = getFolder(destination);
	}

	public void addDocument(Folder folder, GPSDocument document) {
		folder.add(document);
		DocumentEntry entry = new DocumentEntry(folder, document);
		documents.put(document.getId(), entry);

		EventManager.getInstance().publish(DocumentManager.this, Event.DOCUMENT_ADDED, document);
	}

	public Activity consolidate(final Activity activity, final ConsolidationLevel level) throws TrackItException {
		GPSDocument document = new GPSDocument(activity.getParent().getFileName());
		document.add(activity);

		Map<String, Object> options = new HashMap<>();
		options.put(Constants.ConsolidationOperation.LEVEL, level);

		new ConsolidationOperation(options).process(document);

		return document.getActivities().get(0);
	}

	public void newCourse(GPSDocument document) {
		Course course = new Course();
		course.setParent(document);
		course.setName(Messages.getMessage("course.newCourse.name"));
		course.setFilepath( document.getFileName()); 						//12335: 2018-10-01
		document.add(course);
//		// 57421
//		JDialog sportDialog = new SportDialog(course);
//		sportDialog.setVisible(true);
//		changeSportType(course, true);
		// 12335: 2017-03-17
		DBOnlyDataSelectionDialog.showSelectionDialog( course, false, false);

		EventManager.getInstance().publish(this, Event.DOCUMENT_UPDATED, document);
		EventManager.getInstance().publish(DocumentManager.this, Event.COURSE_SELECTED, course);
	}
	
	// 12335: 2017-04-07
	public void duplicate( List<DocumentItem> selectedItems) {
		boolean addToUndoManager = true;
		duplicate( selectedItems, null, null, null, addToUndoManager, false);
	}
	
	// 12335: 2017-04-06
	private void duplicateSetup( UndoItem undoItem, boolean undo) {
		List<Long> srcItemsId  = undoItem.getCoursesIds();
		List<Long> dstItemsId  = undoItem.getDestinationItemsIds();
		List<Long> extraIds    = undoItem.getDestinationDocumentsIds();
		List<Long> srcDocsId   = undoItem.getOriginDocumentsIds();
		
		List<DocumentItem> items = new ArrayList<>();
		Iterator<Long> srcItemsIterator = srcItemsId.iterator();
		Iterator<Long> srcDocsIterator  = srcDocsId.iterator();
		for( long id: (undo ? dstItemsId : srcItemsId) ) {
			long srcItemId = srcItemsIterator.next();
			long srcDocId  = srcDocsIterator.next();
			if ( srcItemId == srcDocId ) {		// it's a document
				items.add( getDocument( id));
			}
			else {
				items.add( getDocumentItem( getDocument( srcDocId), id));
			}
		}
		
		boolean addToUndoManager = false;
		duplicate( items, dstItemsId, extraIds, srcItemsId, addToUndoManager, undo);
	}
	
	// 12335: 2017-04-06
	private void duplicate( List<DocumentItem> itemsToProcess,
							final List<Long> idsToSet,
							final List<Long> extraIds,
							final List<Long> srcItemsIds,
							final boolean addToUndoManager, final boolean undo) {
		final List<DocumentItem> selectedItems = new ArrayList<>( itemsToProcess);
		
		new Task( new Action() {
			
			@Override
			public String getMessage() {
				return Messages.getMessage( "applicationPanel.menu.duplicate");
			}
			
			@Override
			public Object execute() throws TrackItException {
				List<DocumentItem> items = new ArrayList<>( selectedItems);
				if ( addToUndoManager ) {
					// first, get rid of things that are not documents, activities or courses
					// or groups of activities or courses
					Iterator<DocumentItem> itemsIterator = items.iterator();
					while( itemsIterator.hasNext() ) {
						DocumentItem item = itemsIterator.next();
						if ( !(item instanceof GPSDocument) &&
							 !(item.isActivity()) && !(item.isCourse()) &&
							 !(item instanceof ActivitiesItem) && !(item instanceof CoursesItem) )
									itemsIterator.remove();
					}
					// get the documents on the list (if any)
					List<GPSDocument> docsOnList = new ArrayList<>();
					for (DocumentItem item: items)
						if ( item instanceof GPSDocument )
							docsOnList.add( (GPSDocument) item);
					// expand any groups of activities and courses on the list if their documents
					// are not on the list
					List<DocumentItem> addOns = new ArrayList<>();
					itemsIterator = items.iterator();
					while( itemsIterator.hasNext() ) {
						DocumentItem item = itemsIterator.next();
						if ( item instanceof ActivitiesItem || item instanceof CoursesItem ) {
							if ( !docsOnList.contains( item.getParent()) ) {
								if ( item instanceof ActivitiesItem)
									addOns.addAll( ((GPSDocument) item.getParent()).getActivities());
								else
									addOns.addAll( ((GPSDocument) item.getParent()).getCourses());
							}
							itemsIterator.remove();
						}
					}
					// add if not already on the list
					for( DocumentItem item: addOns)
						if ( !items.contains(  item) )
							items.add( item);
					// we may still have on the list items that belong to documents also on the list
					if ( ! docsOnList.isEmpty() ) {
						itemsIterator = items.iterator();
						while( itemsIterator.hasNext() ) {
							DocumentItem item = itemsIterator.next();
							if ( item.isActivity() || item.isCourse() ) {
								GPSDocument doc = (GPSDocument) item.getParent();
								if ( docsOnList.contains( doc))
									itemsIterator.remove();
							}
						}
					}
					// finally, check if anything remains
					if ( items.isEmpty() ) {
						JOptionPane.showMessageDialog( TrackIt.getApplicationFrame(),
								Messages.getMessage( "applicationPanel.error.duplicate"),
								Messages.getMessage( "applicationPanel.menu.duplicate"),
								JOptionPane.WARNING_MESSAGE);
						return null;
					}
				}
				// prepare and carry out the operation
				HashMap<String, Object> options = new HashMap<>();
				options.put( Constants.UndoOperation.UNDO, undo);
				options.put( Constants.DuplicateOperation.ITEMS, items);
				options.put( Constants.DuplicateOperation.IDS_TO_SET, idsToSet);
				options.put( Constants.DuplicateOperation.EXTRA_IDS_TO_SET, extraIds);
				options.put( Constants.DuplicateOperation.ORIGIN_ITEMS_IDS, srcItemsIds);
				options.put( Constants.DuplicateOperation.ADD_TO_UNDO_MANAGER, addToUndoManager);
				DuplicateOperation operation = new DuplicateOperation(options);
				operation.process( (GPSDocument) null);
				// if first time
				if ( addToUndoManager ) {
					String name = UndoableActionType.DUPLICATE.toString();
					UndoItem undoItem = new UndoItem.UndoItemBuilder( name, operation.getItemsIds(), 0L)
							.originDocumentsIds( operation.getDocsIds())
							.destinationItemsIds( operation.getDestinationItemsIds())
							.destinationDocumentsIds( operation.getExtraIds())
							.build();
					undoManager.addUndo( undoItem);
					TrackIt.getApplicationPanel().forceRefresh();
				}
				// done
				return operation;
			}
			
			@Override
			public void done(Object result) {
				if ( result != null ) {
					DuplicateOperation operation = (DuplicateOperation) result;
					for( GPSDocument document: operation.getModifiedDocuments() )
						EventManager.getInstance().publish( DocumentManager.this, 
														    Event.DOCUMENT_UPDATED, document);
					for( DocumentItem item: operation.getLastItems())
						selectDocumentItem( item);
				}
			}
		}).execute();
	}
	
	// 12335: 2017-04-06
	public void copyTo( List<DocumentItem> items, boolean isCopy) {
		copyTo( isCopy, items, null, null, true, false, -1L, false);
	}
	
	// 12335: 2017-04-04
	private void copyToSetup( UndoItem item, boolean undo) {
		List<Long> itemsIds      = item.getCoursesIds();
		List<Long> docsIds       = item.getOriginDocumentsIds();
		List<Long> dstItemsIds   = item.getDestinationItemsIds();
		long       dstDocId      = item.getDocumentId();
		boolean	   dstDocCreated = item.createdDestinationDocument();
		
		List<DocumentItem> items = new ArrayList<>();
		List<Long> docs = undo ? new ArrayList<>( Collections.nCopies( itemsIds.size(), dstDocId)) : docsIds;
		
		Iterator<Long> iterator = docs.iterator();
		for( long itemId: (undo ? dstItemsIds : itemsIds) ) {
			long temp = iterator.next();
			GPSDocument doc = getDocument( temp);
			DocumentItem it = getDocumentItem( getDocument(temp), itemId);
			items.add( it);			
		}
		
		List<Long> tgtItemsIds = null;
		List<Long> tgtDocsIds  = null;
		if ( undo ) {
			tgtItemsIds = itemsIds;
			tgtDocsIds  = docsIds;
		}
		else {
			tgtItemsIds = dstItemsIds;
		}
		copyTo( item.getOperationType().equals( UndoableActionType.COPY_TO.toString()),
				items, tgtItemsIds, tgtDocsIds, 
				false, undo, dstDocId, dstDocCreated);
	}
	
	// 12335: 2017-04-04
	private void copyTo( final boolean isCopy,
					            List<DocumentItem> itemsToProcess, 
							    final List<Long> targetItemsIDs,
							    final List<Long> targetDocumentsIDs,
							    final boolean addToUndoManager,    final boolean undo,
							    final long destinationDocumentID,  final boolean createdDestinationDocument) {
		final List<DocumentItem> selectedItems = new ArrayList<>( itemsToProcess);
		
		new Task( new Action() {
			
			@Override
			public String getMessage() {
				return ( isCopy ? Messages.getMessage( "applicationPanel.menu.copyTo") :
								  Messages.getMessage( "applicationPanel.menu.moveTo"));
			}
			
			@Override
			public Object execute() throws TrackItException {
				List<DocumentItem> items = null;
				GPSDocument selected     = null;
				if ( addToUndoManager ) {
					// first get the destination document
					if ( isCopy ) 
						selected = DocumentSelectionDialog.showCopyToDialog( 
											getDocuments( getWorspaceFolder().getName()), true);
					else
						selected = DocumentSelectionDialog.showMoveToDialog( 
											getDocuments( getWorspaceFolder().getName()), true);
					if ( selected == null && ! DocumentSelectionDialog.newDocumentSelected())
						return null;
					// now run through the list
					// 1) do not accept any move or copy to the same document
					// 2) accept only activities and courses
					// 3) expand any document into its activities and courses
					//	  but do not include the document itself
					// 4) same for groups of activities and courses
					// create a list of documents to copy/move contents
					HashSet<DocumentItem> list = new HashSet<>();
					for( DocumentItem item: selectedItems)
						if ( item instanceof GPSDocument ) {
							if ( ((GPSDocument) item) != selected ) {
								list.addAll( ((GPSDocument) item).getActivities());
								list.addAll( ((GPSDocument) item).getCourses());
							}
						}
						else {
							if ( item.getParent() != selected ) {
								if ( item.isActivity() || item.isCourse() )
									list.add( item);
								else
									if ( item instanceof ActivitiesItem )
										list.addAll( ((GPSDocument) item.getParent()).getActivities());
									else
										if ( item instanceof CoursesItem )
											list.addAll( ((GPSDocument) item.getParent()).getCourses());
							}
						}
					if ( list.isEmpty() ) {
						JOptionPane.showMessageDialog( TrackIt.getApplicationFrame(),
								Messages.getMessage( "applicationPanel.error.copyToMoveTo"),
								isCopy ? Messages.getMessage( "applicationPanel.menu.copyTo")
									   : Messages.getMessage( "applicationPanel.menu.moveTo"),
								JOptionPane.WARNING_MESSAGE);
						return null;
					}
					items = new ArrayList<>( list);
				}
				else {
					items = selectedItems;
					selected = getDocument( destinationDocumentID);
				}
				HashMap<String, Object> options = new HashMap<>();
				options.put( Constants.UndoOperation.UNDO, undo);
				options.put( Constants.CopyToOperation.ADD_TO_UNDO_MANAGER, addToUndoManager);
				options.put( Constants.CopyToOperation.ITEMS, items);
				options.put( Constants.CopyToOperation.TARGET_ITEMS_IDS,     targetItemsIDs);
				options.put( Constants.CopyToOperation.TARGET_DOCUMENTS_IDS, targetDocumentsIDs);
				options.put( Constants.CopyToOperation.DESTINATION_DOCUMENT_ID, destinationDocumentID);
				options.put( Constants.CopyToOperation.DESTINATION_DOCUMENT_CREATED, createdDestinationDocument);
				CopyToOperation operation;
				if ( isCopy ) {
					CopyToOperation op = new CopyToOperation( options);
					op.process( selected);
					operation = op;
				}
				else {
					MoveToOperation op = new MoveToOperation( options);
					op.process( selected);
					operation = op;
				}
				
				if ( addToUndoManager ) {
					long documentId = operation.getDocumentId();
					String name = (isCopy ? UndoableActionType.COPY_TO :
						                    UndoableActionType.MOVE_TO).toString();
					UndoItem undoItem = new UndoItem.UndoItemBuilder( 
												name, operation.getItemsIds(), documentId)
							.originDocumentsIds(         operation.getDocsIds())
							.destinationItemsIds(        operation.getDestinationItemsIds())
							.createdDestinationDocument( operation.newDocumentCreated())
							.build();
					undoManager.addUndo( undoItem);
					TrackIt.getApplicationPanel().forceRefresh();
				}

				return operation;
			}
			
			@Override
			public void done(Object result) {
				if ( result != null ) {
					CopyToOperation operation = (CopyToOperation) result;
					for( GPSDocument document: operation.getDestroyedDocuments() )
						discard( document);
					for( GPSDocument document: operation.getModifiedDocuments()) {
						EventManager.getInstance().publish( DocumentManager.this,
															Event.DOCUMENT_UPDATED, document);
						selectDocumentsFirstItem( document);
					}
					
					DocumentItem last = operation.getLastItem();
					if ( last != null )
						selectDocumentItem( operation.getLastItem());
				}
			}
		}).execute();
	}
		
	// 57421
	public Course getCourse(GPSDocument document, long courseId) {
		ListIterator<Course> iter = document.getCourses().listIterator();
		Course iterCourse;
		while (iter.hasNext()) {
			iterCourse = iter.next();
			if (iterCourse.getId() == courseId) {
				return iterCourse;
			}

		}
		return null;
	}
	
	// 12335: 2017-03-30
	public DocumentItem getDocumentItem( GPSDocument document, long itemId) {
		for( Activity activity: document.getActivities() )
			if ( activity.getId() == itemId )
				return activity;
		for( Course course: document.getCourses() )
			if ( course.getId() == itemId )
				return course;
		return null;
	}

	public Trackpoint getTrackpoint(Course course, long trackpointId) {
		ListIterator<Trackpoint> iter = course.getTrackpoints().listIterator();
		Trackpoint trackpoint;
		while (iter.hasNext()) {
			trackpoint = iter.next();
			if (trackpoint.getId() == trackpointId) {
				return trackpoint;
			}

		}
		return null;
	}

	public void addLap(final Course course, final Trackpoint trackpoint) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.addLap");
			}

			@Override
			public Object execute() throws TrackItException {
				AddLapOperation addLapOperation = new AddLapOperation(course, trackpoint);
				addLapOperation.execute();

				return null;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
//				course.setUnsavedTrue();			// 12335 : 2016-10-03
				course.setTrackStatusTo( true);
			}
		}).execute();
	}

	public void removeLap(final CourseLap lap) {
		Objects.requireNonNull(lap);

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.removeLap");
			}

			@Override
			public Object execute() throws TrackItException {
				RemoveLapOperation removeLapOperation = new RemoveLapOperation(lap);
				removeLapOperation.execute();

				return null;
			}

			@Override
			public void done(Object result) {
				lap.getParent().publishUpdateEvent(null);
			}
		}).execute();
	}

	public void activityToCourse(final Activity activity, final boolean removeActivity) {
		final GPSDocument originalDocument = activity.getParent();

		SwingWorker<Runnable, Void> worker = new SwingWorker<Runnable, Void>() {
			@Override
			protected Runnable doInBackground() {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ActivitiesToCoursesOperation.REMOVE_ACTIVITIES, removeActivity);
				ActivityToCourseOperation Operation = new ActivityToCourseOperation(options);
				GPSDocument document = new GPSDocument(activity.getParent().getFileName());
				document.add(activity);

				try {
					Operation.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}

				final Course course = document.getCourses().get(0);
				originalDocument.add(course);
				if (removeActivity) {
					originalDocument.remove(activity);
				}

				return new Runnable() {
					@Override
					public void run() {
						originalDocument.publishUpdateEvent(null);
						course.publishSelectionEvent(null);
					}
				};
			}

			@Override
			protected void done() {
				try {
					Runnable todo = get();
					if (todo != null) {
						todo.run();
					}
				} catch (InterruptedException e) {
					logger.debug(e.getMessage());
				} catch (ExecutionException e) {
					logger.debug(e.getMessage());
				}
			}
		};
		worker.execute();
	}

	//12335: 2017-04-24
	private void renameSetup( final UndoItem item, final String undoMode) {
		long documentId = item.getDocumentId();
		GPSDocument document = getDocument( documentId);
		long itemId = item.getCourseIdAt(0);
		DocumentItem documentItem = null;
		if ( documentId == itemId )
			documentItem = document;
		else
			documentItem = getDocumentItem( document, itemId);
		final boolean addToUndoManager = false;
		final boolean undo             = undoMode.equals( this.undoMode);
		List<String> names = item.getOldNames();
		final String formerName  = names.get( 0);
		final String currentName = names.get( 1);
		final String newName     = names.get( 2);
		rename( documentItem, formerName, currentName, newName, addToUndoManager, undo);
	}
	
	//12335: 2017-04-25
	public void rename( final DocumentItem documentItem,
							   final String oldName, final String currentName, final String newName,
							   final boolean addToUndoManager, final boolean undo) {
		String previousName = oldName;
		long documentId     = -1L;
		long itemId			= -1L;
		if ( documentItem instanceof GPSDocument ) {
			GPSDocument document = (GPSDocument) documentItem;
			if ( addToUndoManager ) {
				previousName = document.getOldName();
				documentId   = itemId = document.getId();
			}
			if ( undo ) {
				document.setName(previousName);
				document.setOldNameWhenSaving();
				document.setName(currentName);
			}
			else {
				document.rename( newName);
			}
			EventManager.getInstance().publish( this, Event.DOCUMENT_UPDATED, documentItem);
			selectDocumentsFirstItem(document);
		}
		else if ( documentItem.isCourse() ) {
			Course course = (Course) documentItem;
			if ( addToUndoManager ) {
				previousName = course.getOldName();
				documentId   = course.getParent().getId();
				itemId		 = course.getId();
			}
			if ( undo)
				course.rename( currentName);
			else
				course.rename( newName);
			EventManager.getInstance().publish( this, Event.COURSE_UPDATED, documentItem);
			System.out.println( "COURSE OUT " + course.wasRenamed());
		}
		else if ( documentItem.isActivity() ) {
			Activity activity = (Activity) documentItem;
			if ( addToUndoManager ) {
				previousName = activity.getOldName();
				documentId   = activity.getParent().getId();
				itemId       = activity.getId();
			}
			if ( undo )
				activity.rename( currentName);
			else
				activity.rename( newName);
			EventManager.getInstance().publish( this, Event.ACTIVITY_UPDATED, documentItem);
		}
		if ( addToUndoManager ) {
			List<Long> itemsIds = new ArrayList<>();
			itemsIds.add( itemId);
			List<String> names = new ArrayList<>();
			names.add( previousName);
			names.add( currentName);
			names.add( newName);
			UndoItem item = new UndoItem.UndoItemBuilder(
								UndoableActionType.RENAME.toString(), itemsIds, documentId)
								.oldNames(names).build();
			undoManager.addUndo(item);
			TrackIt.getApplicationPanel().forceRefresh();
		}
	}

	// 57421
	public void changeStartTime(Course course, long newTime, boolean addToUndoManager) {
		long oldTime = course.getFirstTrackpoint().getTimestamp().getTime();
		long timeDifference = newTime - oldTime;
		String mode = "start";
		changeTime(course, timeDifference, mode);
		// course.consolidate(ConsolidationLevel.SUMMARY);
		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(course);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
		try {
			new ConsolidationOperation(options).process(document);
		} catch (TrackItException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (addToUndoManager) {
			String name = UndoableActionType.CHANGE_START_TIME.toString();
			List<Long> courseIds = new ArrayList<Long>();
			long documentId = course.getParent().getId();
			courseIds.add(course.getId());
			UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).oldTime(oldTime).newTime(newTime)
					.build();
			undoManager.addUndo(item);
			TrackIt.getApplicationPanel().forceRefresh();
		}
		EventManager.getInstance().publish(this, Event.COURSE_UPDATED, course);
	}

	public void changeEndTime(Course course, long newTime, boolean addToUndoManager) {
		long oldTime = course.getLastTrackpoint().getTimestamp().getTime();
		long timeDifference = newTime - oldTime;
		String mode = "end";
		changeTime(course, timeDifference, mode);
		// course.consolidate(ConsolidationLevel.SUMMARY);
		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(course);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
		try {
			new ConsolidationOperation(options).process(document);
		} catch (TrackItException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (addToUndoManager) {
			String name = UndoableActionType.CHANGE_END_TIME.toString();
			List<Long> courseIds = new ArrayList<Long>();
			long documentId = course.getParent().getId();
			courseIds.add(course.getId());
			UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).oldTime(oldTime).newTime(newTime)
					.build();
			undoManager.addUndo(item);
			TrackIt.getApplicationPanel().forceRefresh();
		}
		EventManager.getInstance().publish(this, Event.COURSE_UPDATED, course);
	}

	private void changeTime(Course course, long timeDifference, String mode) {
		List<Trackpoint> trackpoints = course.getTrackpoints();
		if (mode.equals("end")) {
			// Collections.reverse(trackpoints);
			// timeDifference *= -1;
		}
		course.setStartTime(new Date(course.getStartTime().getTime() + timeDifference));
		course.setEndTime(new Date(course.getEndTime().getTime() + timeDifference));
		for (Lap lap : course.getLaps()) {
			lap.setStartTime(new Date(lap.getStartTime().getTime() + timeDifference));
			lap.setEndTime(new Date(lap.getEndTime().getTime() + timeDifference));
		}
		for (Trackpoint trackpoint : course.getTrackpoints()) {
			trackpoint.setTimestamp(new Date(trackpoint.getTimestamp().getTime() + timeDifference));
		}
		if (mode.equals("end")) {
			// Collections.reverse(trackpoints);
		}
		// course.setTrackpoints(trackpoints);
	}

	///////////////////////////////////////////////////

	public void consolidate(final Course course, final Map<String, Object> options) {
		final GPSDocument masterDocument = course.getParent();

		new Task(new Action() {
			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.consolidating", course.getDocumentItemName());
			}

			@Override
			public Object execute() {
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}
				return document.getCourses().get(0);
			}

			@Override
			public void done(Object result) {
				if (result == null) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("documentManager.error.consolidationError"),
							Messages.getMessage("documentManager.title.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				Course course = (Course) result;
				course.setParent(masterDocument);
				course.publishUpdateEvent(null);
			}
		}, false).execute();
	}

	public DocumentItem simplify(DocumentItem item, Map<String, Object> options) throws TrackItException {
		GPSDocument document = new GPSDocument(null);

		if (!item.isCourse()) {
			throw new IllegalStateException("Only courses can be simplified!");
		} else {
			document.add((Course) item);
			document.setFileName(((Course) item).getParent().getFileName());
		}

		new TrackSimplificationOperation(options).process(document);

		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
		new ConsolidationOperation(options).process(document);

		return document.getCourses().get(0);
	}

	public DocumentItem mark(DocumentItem item, Map<String, Object> options) throws TrackItException {
		GPSDocument document = new GPSDocument(null);

		if (!item.isCourse()) {
			throw new IllegalStateException("Only courses can be marked!");
		} else {
			document.add((Course) item);
			document.setFileName(((Course) item).getParent().getFileName());
		}

		new MarkingOperation(options).process(document);

		return document.getCourses().get(0);
	}

	public DocumentItem detectClimbsDescents(DocumentItem item, Map<String, Object> options) throws TrackItException {
		GPSDocument document = new GPSDocument(null);

		if (item.isActivity()) {
			document.add((Activity) item);
			document.setFileName(((Activity) item).getParent().getFileName());
		} else {
			document.add((Course) item);
			document.setFileName(((Course) item).getParent().getFileName());
		}

		new DetectClimbsDescentsOperation(options).process(document);

		if (!document.getActivities().isEmpty()) {
			return document.getActivities().get(0);
		} else {
			return document.getCourses().get(0);
		}
	}

	public void addPause(final Course course, final Trackpoint trackpoint, final long pausedTime,
			final boolean addToUndoManager) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		final Double speed = trackpoint.getSpeed();
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.addPause");
			}

			@Override
			public Object execute() throws TrackItException {
				RemovePausesOperation removePausesOperation = new RemovePausesOperation(course, trackpoint, pausedTime);
				removePausesOperation.executeAddPause();

				return removePausesOperation;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				RemovePausesOperation removePausesOperation = (RemovePausesOperation) result;
				/* Undo */
				if (addToUndoManager) {
					String name = UndoableActionType.ADD_PAUSE.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = course.getParent().getId();
					courseIds.add(course.getId());
					Trackpoint startPausePoint = trackpoint;
					Trackpoint endPausePoint = removePausesOperation.getFinalPauseTrackpoint();
					Double oldSpeed = removePausesOperation.getPointSpeed();

					int index = course.getTrackpoints().indexOf(trackpoint);
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId)
							.pauseInformationAdd(pausedTime, startPausePoint.getId(), endPausePoint.getId(), oldSpeed)
							.build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
				} else {
					Trackpoint endPausePoint = removePausesOperation.getFinalPauseTrackpoint();
					undoItem.getPauseInformation().setEndID(endPausePoint.getId());
				}

				/* end undo */
			}
		}).execute();
	}

	public void changePauseDuration(final Course course, final Trackpoint trackpoint, final long pausedTime,
			final boolean addToUndoManager) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.addPause");
			}

			@Override
			public Object execute() throws TrackItException {
				RemovePausesOperation removePausesOperation = new RemovePausesOperation(course, trackpoint, pausedTime);
				removePausesOperation.executeChangePauseDuration();

				return removePausesOperation;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				RemovePausesOperation removePausesOperation = (RemovePausesOperation) result;
				/* Undo */
				if (addToUndoManager) {
					String name = UndoableActionType.CHANGE_PAUSE_DURATION.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = course.getParent().getId();
					courseIds.add(course.getId());
					Double oldPauseDuration = removePausesOperation.getOldPauseDuration();
					Double newPauseDuration = (double) pausedTime;
					Pair<Double, Double> save = new Pair<Double, Double>(oldPauseDuration, newPauseDuration);
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).trackpoint(trackpoint)
							.oldPauseTime(save).build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
				}

				/* end undo */
			}
		}).execute();
	}

	public void removePause(final Course course, final Trackpoint trackpoint, final boolean addToUndoManager,
			final Double pointSpeed) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		int firstTrackpointIndex = course.getTrackpoints().indexOf(trackpoint);
		int secondTrackpointIndex = firstTrackpointIndex + 1;
		Double timeFromPrevious = course.getTrackpoints().get(secondTrackpointIndex).getTimeFromPrevious();
		final long pausedTime = (long) timeFromPrevious.doubleValue();
		final Double firstPointSpeed = trackpoint.getSpeed();
		final Double secondPointSpeed = course.getTrackpoints().get(secondTrackpointIndex).getSpeed();

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.removePause");
			}

			@Override
			public Object execute() throws TrackItException {
				RemovePausesOperation removePausesOperation = new RemovePausesOperation(course, trackpoint, pointSpeed);
				removePausesOperation.executeRemovePause();
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return removePausesOperation;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				RemovePausesOperation removePausesOperation = (RemovePausesOperation) result;
				/* Undo */
				if (addToUndoManager) {
					String name = UndoableActionType.REMOVE_PAUSE.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = course.getParent().getId();
					courseIds.add(course.getId());
					Trackpoint leftEdge = removePausesOperation.getLeftEdge();
					Trackpoint rightEdge = removePausesOperation.getRightEdge();
					List<Trackpoint> removedTrackpoints = removePausesOperation.getRemovedTrackpoints();
					long time = removePausesOperation.getPausedTime();
					//List<Lap> laps = removePausesOperation.getLaps();
					// savePoint.setId(trackpoint.getId());
					int index = course.getTrackpoints().indexOf(trackpoint);
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId)
							.pauseInformationRemove(time, removedTrackpoints, leftEdge, rightEdge).build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
				}

				/* end undo */

			}
		}).execute();
	}

	public void addTrackpoint(final Course course, final Trackpoint trackpoint, final boolean addToUndoManager) {
		addTrackpoint(course, trackpoint, course.getTrackpoints().size(), addToUndoManager);
	}

	public void addTrackpoint(final Course course, final Trackpoint trackpoint, final int index,
			final boolean addToUndoManager) {
		final GPSDocument masterDocument = course.getParent();
		final String courseName = course.getName();// 58406
		final String filepath = course.getFilepath();

		SwingWorker<Course, Course> worker = new SwingWorker<Course, Course>() {
			@Override
			protected Course doInBackground() {
				trackpoint.setParent(course);
				course.getTrackpoints().add(index, trackpoint);

				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}

				publish(document.getCourses().get(0));

				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.RECALCULATION);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}
				return document.getCourses().get(0);
			}

			@Override
			protected void process(List<Course> courses) {
				if (!courses.isEmpty()) {
					courses.get(0).setParent(masterDocument);
					courses.get(0).publishUpdateEvent(null);
				}
			}

			@Override
			protected void done() {
				try {
					Course resultCourse = get();
					resultCourse.setCircuitType();						//12335: 2017-03-22
					if (resultCourse != null) {
						resultCourse.setParent(masterDocument);
						resultCourse.publishUpdateEvent(null);
					}
				} catch (InterruptedException | ExecutionException ignore) {
				}
				course.setName(courseName);
				// if(course.getFilepath() != null){
				course.setFilepath(filepath);
				// }
				course.setTrackStatusTo( true);			//12335: 2016-10-16

				/* Undo */
				if (addToUndoManager) {
					String name = UndoableActionType.ADD_TRACKPOINT.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = masterDocument.getId();
					courseIds.add(course.getId());
					Trackpoint savePoint = trackpoint.clone();
					savePoint.setId(trackpoint.getId());
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).trackpoint(savePoint)
							.trackpointIndex(index).build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
				}

				/* end undo */

			}
		};
		worker.execute();

	}

	public void removeTrackpoint(final Course course, final Trackpoint trackpoint, final boolean addToUndoManager) {
		final GPSDocument masterDocument = course.getParent();
		final int index = course.getTrackpoints().indexOf(trackpoint);
		final boolean keepTimes = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.EDITION, null,
				Constants.EditionPreferences.KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL, true);
		SwingWorker<Course, Course> worker = new SwingWorker<Course, Course>() {
			@Override
			protected Course doInBackground() {
				// boolean keepTimes =
				// TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.EDITION,
				// null,
				// Constants.EditionPreferences.KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL,
				// true);
				if (keepTimes) {
					List<Trackpoint> trackpoints = course.getTrackpoints();
					int n = trackpoints.indexOf(trackpoint);
					if (n == 0) {
						Trackpoint trkp = trackpoints.get(1);
						trkp.setDistance(0.0);
						trkp.setDistanceFromPrevious(0.0);
						trkp.setTimeFromPrevious(0.0);
						trkp.setSpeed(0.0);
					} else if (n == trackpoints.size() - 1) {
						// DO NOTHING
					} else {
						Trackpoint prevTrakp = trackpoints.get(n - 1);
						Trackpoint nextTrakp = trackpoints.get(n + 1);
						Double distanceFromPrevious = trackpoint.getDistanceFromPrevious()
								+ nextTrakp.getDistanceFromPrevious();
						nextTrakp.setDistanceFromPrevious(distanceFromPrevious);
						Double timeFromPrevious = trackpoint.getTimeFromPrevious() + nextTrakp.getTimeFromPrevious();
						nextTrakp.setTimeFromPrevious(timeFromPrevious);
						Trackpoint temp = trackpoints.get(n - 2);
						Double speed = (nextTrakp.getDistance() - temp.getDistance()) / (nextTrakp.getTimeFromPrevious()
								+ trackpoint.getTimeFromPrevious() + prevTrakp.getTimeFromPrevious());
						prevTrakp.setSpeed(speed);
						temp = trackpoints.get(n + 2);
						speed = (temp.getDistance() - prevTrakp.getDistance()) / (temp.getTimeFromPrevious()
								+ nextTrakp.getTimeFromPrevious() + trackpoint.getTimeFromPrevious());
						nextTrakp.setSpeed(speed);
					}
				}
				course.remove(trackpoint);// 58406

				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}

				publish(course);

				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.RECALCULATION);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}

				return course;

			}

			@Override
			protected void process(List<Course> courses) {
				if (!courses.isEmpty()) {
					courses.get(0).setParent(masterDocument);
					courses.get(0).publishUpdateEvent(null);
				}
			}

			@Override
			protected void done() {
				try {
					Course resultCourse = get();
					if (resultCourse != null) {
						resultCourse.setParent(masterDocument);
						resultCourse.publishUpdateEvent(null);
						EventManager.getInstance().publish(null, Event.TRACKPOINT_HIGHLIGHTED, null);
					}
					/* Undo */
					if (addToUndoManager) {
						String name = UndoableActionType.REMOVE_TRACKPOINT.toString();
						List<Long> courseIds = new ArrayList<Long>();
						long documentId = masterDocument.getId();
						courseIds.add(resultCourse.getId());
						Trackpoint savePoint = trackpoint.clone();
						savePoint.setId(trackpoint.getId());
						UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).trackpoint(savePoint)
								.trackpointIndex(index).keepTimes(keepTimes).build();
						//keep times não está a ser usado, adicionar suporte para o add trackpoint também e fazer nas funções de undo e redo
						undoManager.addUndo(item);
						TrackIt.getApplicationPanel().forceRefresh();
					}

					/* end undo */

				} catch (InterruptedException | ExecutionException ignore) {
				}

			}
		};
		worker.execute();

	}

//	public void reverse(final Course course, final String reverseMode, final boolean addToUndoManager,
//			final String undoRedoMode) {			//12335: 2017-04-19
	public void reverse(final Course course,            final String reverseMode,
						final boolean addToUndoManager, final boolean undo, final long returnNewID) {

		Objects.requireNonNull(course);
		final GPSDocument masterDocument = course.getParent();

		final String returnNewMode = Constants.ReverseOperation.RETURN_NEW;

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.reverse");
			}

			@Override
			public Object execute() throws TrackItException {
				return reverse(course);
			}

			private List<Course> reverse(Course course) {
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);
					// 12335: 2017-04-17, 2017-04-19
//					Map<String, Object> copyOptions = new HashMap<String, Object>();
//					copyOptions.put(Constants.CopyOperation.COURSE, course);
//					CopyOperation copyOperation = new CopyOperation(copyOptions);
//
//					try {
//						copyOperation.process(document);
//						copyOptions.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.BASIC);
//						new ConsolidationOperation(copyOptions).process(document);
//
//					} catch (TrackItException e) {
//						logger.error(e.getMessage());
//						return null;
//					}
				Map<String, Object> reverseOptions = new HashMap<String, Object>();
				if ( reverseMode.equals(returnNewMode)) {
					if ( undo ) {
						reverseOptions.put( Constants.ReverseOperation.RETURN_NEW, 
											getDocumentItem( masterDocument, returnNewID));
					}
					else {
						Course clone = course.clone();
						clone.setName( course.getName() + " " + 
									   Messages.getMessage( "document.copy.documentNameSuffix"));
						if ( !addToUndoManager )
							clone.setId( returnNewID);
						document.add( clone);
					}
				}

				//12335: 2017-04-19
//				Map<String, Object> reverseOptions = new HashMap<String, Object>();
//				reverseOptions.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
//
//				ReverseOperation reverseOperation = new ReverseOperation(reverseOptions);
//				ConsolidationOperation consolidationOP = new ConsolidationOperation(reverseOptions);
				ReverseOperation reverseOperation = new ReverseOperation(reverseOptions);
				Map<String, Object> consolidationOptions = new HashMap<>();
				consolidationOptions.put( Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
				ConsolidationOperation consolidationOP = new ConsolidationOperation( consolidationOptions);
				
				try {
//					if (addToUndoManager) {									//12335: 2017-04-19
//						reverseOperation.process(document, reverseMode);
//					}
//					if (!addToUndoManager && undoRedoMode.equals(undoMode)) {
//						reverseOperation.undoOperation(document, reverseMode);
//
//					}
//					if (!addToUndoManager && undoRedoMode.equals(redoMode)) {
//						reverseOperation.redoOperation(document, reverseMode);
//					}
					if ( addToUndoManager )
						reverseOperation.process( document, reverseMode);
					else {
						if ( undo )
							reverseOperation.undoOperation( document, reverseMode);
						else
							reverseOperation.redoOperation( document, reverseMode);
					}
					consolidationOP.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}

				return document.getCourses();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void done(Object result) {
				List<Course> courses = (List<Course>) result;
				for (Course course : courses) {
					course.setParent(masterDocument);
//					course.setUnsavedTrue();			// 12335 : 2016-10-03
					course.setTrackStatusTo( true);
				}

				/* undo */
				if (addToUndoManager) {
					String name = UndoableActionType.REVERSE.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = masterDocument.getId();

					courseIds.add(courses.get(0).getId());
					if (reverseMode.equals(Constants.ReverseOperation.RETURN_NEW)) {
						courseIds.add(courses.get(1).getId());
					}
//					if (reverseMode.equals(Constants.ReverseOperation.RETURN)
//							|| reverseMode.equals(Constants.ReverseOperation.NORMAL)) {
						UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId)
								.reverseMode(reverseMode).build();
						undoManager.addUndo(item);
//					}
				}
				/* end undo */
				masterDocument.remove(course);
				masterDocument.addCourses(courses);

				masterDocument.publishUpdateEvent(null);
//				courses.get(0).publishSelectionEvent(null);			//12335: 2017-04-19
				courses.get( courses.size()-1).publishSelectionEvent( null);

			}

		}).execute();

	}

	public void splitAtSelected(final Course course, final Trackpoint trackpoint, final boolean keepSpeed,
			final Map<String, Object> undoOptions) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);
		final Double tempSpeed = trackpoint.getSpeed();
		if (!keepSpeed) {
			Double speed = 0.0;
			trackpoint.setSpeed(speed);
		}

		final GPSDocument masterDocument = course.getParent();

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.splitingCourse");
			}

			@Override
			public Object execute() throws TrackItException {
				return splitCourse(course, trackpoint);
			}

			private List<Course> splitCourse(Course course, Trackpoint trackpoint) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.SplitAtSelectedOperation.COURSE, course);
				options.put(Constants.SplitAtSelectedOperation.TRACKPOINT, trackpoint);

				TrackSplittingOperation splittingOperation = new TrackSplittingOperation(options);
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);

				try {
					splittingOperation.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}
				return document.getCourses();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void done(Object result) {
				List<Course> courses = (List<Course>) result;
				Course first = courses.get( 0);			// 12335: 2017-03-23
				for (Course course : courses) {
					course.setParent(masterDocument);
//					course.setUnsavedTrue();			// 12335 : 2016-10-03
					course.setTrackStatusTo( true);
					course.setDifficulty( first.getDifficulty());			// 12335: 2017-03-23
					course.setTrackCondition( first.getTrackCondition()); 	// 12335: 2017-03-23
					course.setCircuitType();			// 12335: 2017-03-18
				}

				/* undo */
				if ((boolean) undoOptions.get(Constants.ExtraUndoOptions.ADD_TO_MANAGER)) {
					String name = UndoableActionType.SPLIT.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = masterDocument.getId();

					for (Course course : courses) {
						courseIds.add(course.getId());
					}

					if (!keepSpeed) {
						UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).trackpoint(trackpoint)
								.splitSpeed(tempSpeed).build();
						undoManager.addUndo(item);
					}
					if (keepSpeed) {

						Double speed = null;
						UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).trackpoint(trackpoint)
								.splitSpeed(speed).build();
						undoManager.addUndo(item);

					}
				} else if ((boolean) undoOptions.get(Constants.ExtraUndoOptions.SPLIT_UNDO)) {
					UndoItem item = undoManager.getUndoableItem();
					courses.get(1).setId(item.getDeletedCourseId());

					List<Long> newIds = new ArrayList<Long>();
					newIds.add(courses.get(0).getId());
					newIds.add(courses.get(1).getId());
					// UndoItem item = undoManager.getUndoableItem();
					UndoItem newItem = new UndoItem.UndoItemBuilder(item.getOperationType(), newIds,
							item.getDocumentId()).trackpoint(item.getTrackpoint()).splitSpeed(item.getSplitSpeed())
									.build();
					undoManager.deleteUndo();
					undoManager.pushUndo(newItem);
				}
				if ((boolean) undoOptions.get(Constants.ExtraUndoOptions.JOIN_UNDO)) {
					long newId = (long) undoOptions.get(Constants.ExtraUndoOptions.EXTRA_ID);
					courses.get(1).setId(newId);
					courses.get(0).remove(courses.get(0).getLastTrackpoint());
				}

				/* end undo */

				masterDocument.remove(course);
				masterDocument.addCourses(courses);
				if ((boolean) undoOptions.get(Constants.ExtraUndoOptions.APPEND_UNDO)) {
					masterDocument.remove(courses.get(1));
				}
				masterDocument.publishUpdateEvent(null);
				courses.get(0).publishSelectionEvent(null);

			}
		}).execute();
	}

	public void join(final List<Course> courses, final boolean merge, final Double minimumDistance,
			Map<String, Object> undoOptions) throws TrackItException {

		List<Long> routeIds = new ArrayList<Long>();
		Map<Long, Date[]> startTimes = new HashMap<Long, Date[]>();
		undoOptions.put(Constants.ExtraUndoOptions.SPLIT_UNDO, false);
		undoOptions.put(Constants.ExtraUndoOptions.JOIN_UNDO, false);
		if (merge) {
			join(courses, routeIds, startTimes, undoOptions);
		} else {

			List<Course> courseList = appendJoin(courses, minimumDistance, routeIds, startTimes);

			join(courseList, routeIds, startTimes, undoOptions);
		}

	}

	private double getMinimumDistance(Course course) {
		return DocumentManager.getInstance().getDatabase().getJoinMergeDistanceTolerance(course.getSport(),
				course.getSubSport(), false);
	}

	private List<Course> appendJoin(final List<Course> joiningCourses, final Double minimumDistance,
			List<Long> routeIds, Map<Long, Date[]> startTimes) throws TrackItException {

		List<Course> newJoiningCourses = new ArrayList<Course>();
		Trackpoint trailingTrackpoint;
		Trackpoint leadingTrackpoint;
		double distance;

		RoutingType routingType = RoutingType
				.lookup(TrackIt.getPreferences().getPreference(Constants.PrefsCategories.JOIN, null,
						Constants.JoinPreferences.ROUTING_TYPE, RoutingType.FASTEST.getRoutingTypeName()));
		TransportMode transportMode = TransportMode
				.lookup(TrackIt.getPreferences().getPreference(Constants.PrefsCategories.JOIN, null,
						Constants.JoinPreferences.TRANSPORT_MODE, TransportMode.CAR.getTransportModeName()));
		boolean followRoads = joiningCourses.get(0).getSubSport().getFollowRoads();
		boolean avoidHighways = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.JOIN, null,
				Constants.JoinPreferences.AVOID_HIGHWAYS, true);
		boolean avoidTollRoads = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.JOIN, null,
				Constants.JoinPreferences.AVOID_TOLL_ROADS, true);
		boolean addDirectionCoursePoints = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.JOIN,
				null, Constants.JoinPreferences.ADD_COURSE_POINTS_AT_JUNCTIONS, true);
		if (followRoads) {

			appendJoin = true;
			java.util.Map<String, Object> routingOptions = new HashMap<>();
			routingOptions.put(Constants.RoutingOptions.ROUTING_TYPE, routingType);
			routingOptions.put(Constants.RoutingOptions.TRANSPORT_MODE, transportMode);
			routingOptions.put(Constants.RoutingOptions.AVOID_HIGHWAYS, avoidHighways);
			routingOptions.put(Constants.RoutingOptions.AVOID_TOLL_ROADS, avoidTollRoads);
			routingOptions.put(Constants.RoutingOptions.ADD_DIRECTION_COURSE_POINTS, addDirectionCoursePoints);
			Pair<Boolean, Course> pair;
			for (int i = 0; i < joiningCourses.size() - 1; i++) {
				trailingTrackpoint = joiningCourses.get(i).getLastTrackpoint();
				leadingTrackpoint = joiningCourses.get(i + 1).getFirstTrackpoint();
				distance = calculateDistance(trailingTrackpoint, leadingTrackpoint) * 1000.0;

				newJoiningCourses.add(joiningCourses.get(i));
				Course newCourse = joiningCourses.get(i).clone();
				pair = new Pair<Boolean, Course>(originalCourse, newCourse);
				joinedCoursesInfo.add(pair);
				Location location = new Location(joiningCourses.get(i + 1).getFirstTrackpoint().getLongitude(),
						joiningCourses.get(i + 1).getFirstTrackpoint().getLatitude());

				if (distance > minimumDistance) {
					Course route = createInnerCourseJoin(MapLayer.getInstance(), routingOptions, joiningCourses.get(i), joiningCourses.get(i+1),
							location);
					routeIds.add(route.getId());
					Date[] times = new Date[2];
					times[0] = joiningCourses.get(i + 1).getFirstTrackpoint().getTimestamp();
					times[1] = joiningCourses.get(i + 1).getLastTrackpoint().getTimestamp();
					startTimes.put(joiningCourses.get(i + 1).getId(), times);
					joinAppendSetPaceSetup(joiningCourses.get(i), route, joiningCourses.get(i + 1));
					newJoiningCourses.add(route);
					pair = new Pair<Boolean, Course>(createdCourse, route);
					joinedCoursesInfo.add(pair);
				}

			}
			newJoiningCourses.add(joiningCourses.get(joiningCourses.size() - 1));
			Course lastCourse = joiningCourses.get(joiningCourses.size() - 1).clone();
			pair = new Pair<Boolean, Course>(originalCourse, lastCourse);
			joinedCoursesInfo.add(pair);
			return newJoiningCourses;
		}
		mergeJoin = false;
		return joiningCourses;
	}

	private Course createInnerCourseJoin(final MapProvider mapProvider, final Map<String, Object> routingOptions,
			final Course course, final Course course2, final Location location) throws TrackItException {
		Objects.requireNonNull(mapProvider);
		Objects.requireNonNull(routingOptions);
		Objects.requireNonNull(course);
		Objects.requireNonNull(course2);
		Objects.requireNonNull(location);

		if (!mapProvider.hasRoutingSupport()) {
			throw new TrackItException(
					String.format("%s provider does not have routing support!", mapProvider.getName()));
		}

		final GPSDocument masterDocument = course.getParent();
		Trackpoint startTrackpoint = course.getLastTrackpoint();
		Location startLocation = new Location(startTrackpoint.getLongitude(), startTrackpoint.getLatitude());
		Location endLocation = location;
		Course route = mapProvider.getRoute(startLocation, endLocation, routingOptions);
//		route.setSport(course.getSport());										//12335: 2016-06-15
//		route.setSubSport(course.getSubSport());								//12335: 2016-06-15
		route.setSportAndSubSport( course.getSport(), course.getSubSport());	//12335: 2016-06-15
		route.getTrackpoints().remove(0);

		Map<String, Object> options = new HashMap<>();
		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.RECALCULATION);
		/*double values[] = new double[2];
		values = getJoinOptions(course, course2);
		if(values[0] != -1.0){
			for(Trackpoint trackpoint : route.getTrackpoints()){
				trackpoint.setSpeed(values[0]);
			}
		}*/
		//options.put(Constants.JoinPreferences.JOIN_SPEED, values);
		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(route);
		new ConsolidationOperation(options).process(document);
		route = document.getCourses().get(0);
		route.setParent(masterDocument);

		//appendSetPaceSetup(course, route);

		return route;
	}

	private Double calculateDistance(Trackpoint trailingTrackpoint, Trackpoint leadingTrackpoint) {
		return Utilities.getGreatCircleDistance(trailingTrackpoint.getLatitude(), trailingTrackpoint.getLongitude(),
				leadingTrackpoint.getLatitude(), leadingTrackpoint.getLongitude());
	}

	public void join(final List<Course> courses, final List<Long> routeIds, final Map<Long, Date[]> startTimes,
			final Map<String, Object> undoOptions) {
		if (courses == null || courses.size() < 2) {
			throw new IllegalArgumentException("Join only applies to two or more courses!");
		}

		final GPSDocument masterDocument = courses.get(0).getParent();

		new Task(new Action() {
			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.joiningCourses");
			}

			@Override
			public Object execute() throws TrackItException {
				return joinCourses(courses);
			}

			private Course joinCourses(final List<Course> courses) throws TrackItException {
				if (!appendJoin) {
					for (Course course : courses) {
						Course newCourse = course.clone();
						Pair<Boolean, Course> pair = new Pair<Boolean, Course>(originalCourse, newCourse);
						joinedCoursesInfo.add(pair);
					}
				}
				Map<String, Object> options = new HashMap<>();
				options.put(Constants.JoinOperation.ADD_LAP_MARKER, true);
				options.put(Constants.JoinOperation.MERGE_JOIN, mergeJoin);
				JoiningOperation operation = new JoiningOperation(options);
				GPSDocument document = new GPSDocument(courses.get(0).getParent().getFileName());
				document.addCourses(courses);
				try {
					if ((boolean) undoOptions.get(Constants.ExtraUndoOptions.ADD_TO_MANAGER)
							|| (boolean) undoOptions.get(Constants.ExtraUndoOptions.JOIN_UNDO)
							|| (boolean) undoOptions.get(Constants.ExtraUndoOptions.APPEND_UNDO)) {
						operation.process(document);
					} else {
						operation.undoSplit(document);
					}
					options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
					try {
						new ConsolidationOperation(options).process(document);
					} catch (TrackItException e) {
						return null;
					}
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}

				if (document.getCourses().size() != 1) {
					throw new TrackItException("Join operation resulted in more than one course!");
				}
				return document.getCourses().get(0);
			}

			@Override
			public void done(Object result) {
				/*
				 * List<String> oldNames = new ArrayList<String>(); for (Course
				 * course : courses) { oldNames.add(course.getName()); }
				 */

				Course jointCourse = (Course) result;
				jointCourse.setId(courses.get(0).getId());
				jointCourse.setDifficulty( courses);				//12335: 2017-03-23
				jointCourse.setTrackCondition( courses); 			//12335: 2017-03-23

				List<Long> allPointsIds = new ArrayList<Long>();
				List<Long> possibleMergePoints = new ArrayList<Long>();
				for (Course course : courses) {
					for (Trackpoint trackpoint : course.getTrackpoints()) {
						allPointsIds.add(trackpoint.getId());
					}
					masterDocument.remove(course);
				}
				jointCourse.setCircuitType();				// 12335: 2017-03-18

				/* undo */
				if ((boolean) undoOptions.get(Constants.ExtraUndoOptions.ADD_TO_MANAGER)) {
					for (Long id : allPointsIds) {
						if (Collections.frequency(allPointsIds, id) > 1) {
							possibleMergePoints.add(id);
						}
					}
					String name;
					if (appendJoin) {
						name = UndoableActionType.APPEND_JOIN.toString();
						appendJoin = false;
					} else {
						name = UndoableActionType.JOIN.toString();
					}
					List<Long> courseIds = new ArrayList<Long>();
					Map<Long, Long> connectingPoints = new HashMap<Long, Long>();
					long documentId = masterDocument.getId();

					for (Course course : courses) {
						courseIds.add(course.getId());
						connectingPoints.put(course.getId(), course.getFirstTrackpoint().getId());
					}
					Collections.reverse(courseIds);

					List<Pair<Boolean, Course>> newPairings = new ArrayList<Pair<Boolean, Course>>();
					newPairings.addAll(joinedCoursesInfo);
					// joinedCoursesInfo.clear();
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId)
							.connectingPointsIds(connectingPoints).duplicatePointIds(possibleMergePoints)
							.routeIds(routeIds).joinedCoursesInfo(newPairings).startTimes(startTimes).build();
					undoManager.addUndo(item);
				} else if ((boolean) undoOptions.get(Constants.ExtraUndoOptions.SPLIT_UNDO)) {
					List<Long> newIds = new ArrayList<Long>();
					newIds.add(jointCourse.getId());

					long deletedId = courses.get(1).getId();
					UndoItem item = undoManager.getRedoableItem();

					UndoItem newItem = new UndoItem.UndoItemBuilder(item.getOperationType(), newIds,
							item.getDocumentId()).trackpoint(item.getTrackpoint()).splitSpeed(item.getSplitSpeed())
									.deletedCourseId(deletedId).build();
					undoManager.deleteRedo();
					undoManager.pushRedo(newItem);
					// joinedCoursesInfo.clear();
				}
				joinedCoursesInfo.clear();
				mergeJoin = true;
				/* end undo */

				jointCourse.setParent(masterDocument);
				masterDocument.add(jointCourse);

				masterDocument.publishUpdateEvent(null);
				jointCourse.publishSelectionEvent(null);
				EventManager.getInstance().publish(null, Event.ZOOM_TO_ITEM, jointCourse);
			}
		}).execute();
	}

	public Course setPace(Course course, Map<String, Object> options, boolean addToUndoManager)
			throws TrackItException {
		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(course);
		List<Pair<Boolean, Course>> newPairings = new ArrayList<Pair<Boolean, Course>>();
		Course newCourse = course.clone();
		Pair<Boolean, Course> pair = new Pair<Boolean, Course>(originalCourse, newCourse);

		new SettingPaceOperation(options).process(document);

		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
		new ConsolidationOperation(options).process(document);
//		course.setUnsavedTrue();			// 12335 : 2016-10-03
		course.setTrackStatusTo( true);

		/* undo */
		if (addToUndoManager && !options.get(Constants.SetPaceOperation.METHOD).equals(SetPaceMethod.SMART_PACE)) {
			String name = UndoableActionType.SET_PACE.toString();
			List<Long> courseIds = new ArrayList<Long>();
			long documentId = course.getParent().getId();
			courseIds.add(course.getId());
			UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).paceOptions(options).build();
			undoManager.addUndo(item);
		}
		if (addToUndoManager && options.get(Constants.SetPaceOperation.METHOD).equals(SetPaceMethod.SMART_PACE)) {
			String name = UndoableActionType.SMART_PACE.toString();

			newPairings.add(pair);
			List<Long> courseIds = new ArrayList<Long>();
			long documentId = course.getParent().getId();
			courseIds.add(course.getId());
			UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).paceOptions(options)
					.joinedCoursesInfo(newPairings).build();
			undoManager.addUndo(item);
		}

		/* end undo */
		course.publishSelectionEvent(null);
		EventManager.getInstance().publish(null, Event.ZOOM_TO_ITEM, course);
		return document.getCourses().get(0);
	}

	public DocumentItem clearSegments(DocumentItem item) {
		item.getSegments().clear();
		return item;
	}

	public void removeAllPauses(final Course course, final boolean addToUndoManager) {
		Objects.requireNonNull(course);

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.removePauses");
			}

			@Override
			public Object execute() throws TrackItException {
				RemovePausesOperation removePausesOperation = new RemovePausesOperation(course);
				removePausesOperation.execute();
				return removePausesOperation;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				RemovePausesOperation removePausesOperation = (RemovePausesOperation) result;
				/* Undo */
				if (addToUndoManager) {
					String name = UndoableActionType.REMOVE_PAUSES.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = course.getParent().getId();
					courseIds.add(course.getId());
					List<RemovePausesOperation.PauseInformation> removedPauses = removePausesOperation
							.getRemovedPauses();
					Collections.reverse(removedPauses);
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId)
							.removedPauses(removedPauses).build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
				}

				/* end undo */
			}
		}).execute();
	}

	public void appendRoute(final MapProvider mapProvider, final Map<String, Object> routingOptions,
			final Course course, final Location location, final boolean addToUndoManager) throws TrackItException {
		Objects.requireNonNull(mapProvider);
		Objects.requireNonNull(routingOptions);
		Objects.requireNonNull(course);
		Objects.requireNonNull(location);

		if (!mapProvider.hasRoutingSupport()) {
			throw new TrackItException(
					String.format("%s provider does not have routing support!", mapProvider.getName()));
		}

		final GPSDocument masterDocument = course.getParent();
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.appendRoute");
			}

			@Override
			public Object execute() throws TrackItException {
				Trackpoint startTrackpoint = course.getLastTrackpoint();
				Location startLocation = new Location(startTrackpoint.getLongitude(), startTrackpoint.getLatitude());
				Location endLocation = location;
				Course route = mapProvider.getRoute(startLocation, endLocation, routingOptions);
//				route.setSport(course.getSport());										//12335: 2016-06-15
//				route.setSubSport(course.getSubSport());								//12335: 2016-06-15
				route.setSportAndSubSport( course.getSport(), course.getSubSport());	//12335: 2016-06-15
				long id = course.getLastTrackpoint().getId();
				int indexId = course.getTrackpoints().size() - 1;

				Map<String, Object> options = new HashMap<>();
				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.RECALCULATION);
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(route);
				new ConsolidationOperation(options).process(document);
				route = document.getCourses().get(0);
				route.setParent(masterDocument);

				appendSetPaceSetup(course, route);

				GPSDocument document2 = new GPSDocument(course.getParent().getFileName());
				document2.addCourses(Arrays.asList(course, route));
				options.put(Constants.JoinOperation.ADD_LAP_MARKER, false);
				options.put(Constants.JoinOperation.MERGE_JOIN, mergeJoin);
				new JoiningOperation(options).process(document2);
				Course newCourse = document2.getCourses().get(0);
				newCourse.setParent(masterDocument);
				newCourse.getTrackpoints().get(indexId).setId(id);
				// addLap(newCourse, course.getLastTrackpoint());

				masterDocument.remove(course);
				masterDocument.add(newCourse);
				return newCourse;
			}

			@Override
			public void done(Object result) {
				Course newCourse = (Course) result;

				/* undo */
				if (addToUndoManager) {
					String name = UndoableActionType.APPEND.toString();
					List<Long> courseIds = new ArrayList<Long>();
					long documentId = masterDocument.getId();
					newCourse.setId(course.getId());
					courseIds.add(newCourse.getId());
					Trackpoint lastPoint = course.getLastTrackpoint().clone();
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).trackpoint(lastPoint)
							.mapProvider(mapProvider).routingOptions(routingOptions).location(location)
							.trackpointIndex(course.getTrackpoints().size() - 1).build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
					// newCourse.getTrackpoints().get(item.getTrackpointIndex()).setId(item.getTrackpoint().getId());
				} else {
					newCourse.setId(course.getId());
				}
				/* end undo */
				newCourse.setName(course.getName());
				// if(course.getFilepath() != null){
				newCourse.setFilepath(course.getFilepath());
				// }
				newCourse.setCircuitType();							//12335: 2017-03-22
				masterDocument.publishUpdateEvent(null);
				newCourse.publishSelectionEvent(null);
			}
		}).execute();
	}

	private void appendSetPaceSetup(final Course course, Course route) throws TrackItException {
		final Map<String, Object> paceOptions = new HashMap<String, Object>();

		Double targetSpeed = getJoinAppendSpeed();

		paceOptions.put(Constants.SetPaceOperation.METHOD, SetPaceMethod.TARGET_SPEED);
		paceOptions.put(Constants.SetPaceOperation.SPEED, targetSpeed);
		paceOptions.put(Constants.SetPaceOperation.INCLUDE_PAUSES, true);

		setPace(route, paceOptions, false);
	}

	private String getJoinAppendOption() {
		return TrackIt.getPreferences().getPreference(Constants.PrefsCategories.JOIN, null,
				Constants.JoinPreferences.JOIN_OPTIONS, null);
	}

	private Double getJoinAppendSpeed() {
		return TrackIt.getPreferences().getDoublePreference(Constants.PrefsCategories.JOIN, null,
				Constants.JoinPreferences.JOIN_SPEED, 0);
	}

	private Double getJoinAppendTime() {
		return TrackIt.getPreferences().getDoublePreference(Constants.PrefsCategories.JOIN, null,
				Constants.JoinPreferences.JOIN_TIME, 0);
	}
	
	private double[] getJoinOptions(final Course firstCourse, final Course secondCourse) {
		String option = getJoinAppendOption();
		double result[] = new double[2];
		double targetSpeed = -1.0;
		double targetTime = -1.0;
		// constant default
		if (option.equals(JoinOptions.getAvailableOptions().get(0))) {
			 targetSpeed = DocumentManager.getInstance().getDatabase().getDefaultAverageSpeed(firstCourse.getSport(), firstCourse.getSubSport(), false) /3.6;
		}
		// constant user set
		if (option.equals(JoinOptions.getAvailableOptions().get(1))) {
			targetSpeed = getJoinAppendSpeed() / 3.6;
		}
		// first avg
		if (option.equals(JoinOptions.getAvailableOptions().get(2))) {
			targetSpeed = firstCourse.getAverageMovingSpeed();
		}
		// first end
		if (option.equals(JoinOptions.getAvailableOptions().get(3))) {
			targetSpeed = firstCourse.getLastTrackpoint().getSpeed();
		}
		// second avg
		if (option.equals(JoinOptions.getAvailableOptions().get(4))) {
			targetSpeed = secondCourse.getAverageMovingSpeed();
		}
		// second start
		if (option.equals(JoinOptions.getAvailableOptions().get(5))) {
			targetSpeed = secondCourse.getFirstTrackpoint().getSpeed();
		}
		// avg both
		if (option.equals(JoinOptions.getAvailableOptions().get(6))) {
			targetSpeed = (firstCourse.getAverageMovingSpeed() + secondCourse.getAverageMovingSpeed()) / 2;
		}
		// avg connecting
		if (option.equals(JoinOptions.getAvailableOptions().get(7))) {
			targetSpeed = (firstCourse.getLastTrackpoint().getSpeed() + secondCourse.getFirstTrackpoint().getSpeed())
					/ 2;
		}
		// keep time stamps
		if (option.equals(JoinOptions.getAvailableOptions().get(8))) {
			long firstCourseEndTime = firstCourse.getLastTrackpoint().getTimestamp().getTime();
			long secondCourseStartTime = secondCourse.getFirstTrackpoint().getTimestamp().getTime();
			if (secondCourseStartTime < firstCourseEndTime) {
				try {
					throw new TrackItException("Second Course starts before First Course");
				} catch (TrackItException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			targetTime = (double) (secondCourseStartTime - firstCourseEndTime) / 1000;
		}
		// user set time
		if (option.equals(JoinOptions.getAvailableOptions().get(9))) {

			targetTime = getJoinAppendTime() * 60;
		}
		result[0] = targetSpeed;
		result[1] = targetTime;
		 return result;
	}

	private void joinAppendSetPaceSetup(final Course firstCourse, Course route, final Course secondCourse)
			throws TrackItException {
		final Map<String, Object> paceOptions = new HashMap<String, Object>();

		String option = getJoinAppendOption();
		SetPaceMethod method = SetPaceMethod.CONSTANT_TARGET_SPEED;
		Double targetSpeed = 0.0;
		Double targetTime = 0.0;

		// constant default
		if (option.equals(JoinOptions.getAvailableOptions().get(0))) {
			method = SetPaceMethod.CONSTANT_TARGET_SPEED;
			
			targetSpeed = database.getDefaultAverageSpeed(firstCourse.getSport(), firstCourse.getSubSport(), false)
					/ 3.6;
		}
		// constant user set
		if (option.equals(JoinOptions.getAvailableOptions().get(1))) {
			method = SetPaceMethod.CONSTANT_TARGET_SPEED;
			targetSpeed = getJoinAppendSpeed() / 3.6;
		}
		// first avg
		if (option.equals(JoinOptions.getAvailableOptions().get(2))) {
			method = SetPaceMethod.TARGET_SPEED;
			targetSpeed = firstCourse.getAverageMovingSpeed();
		}
		// first end
		if (option.equals(JoinOptions.getAvailableOptions().get(3))) {
			method = SetPaceMethod.TARGET_SPEED;
			targetSpeed = firstCourse.getLastTrackpoint().getSpeed();
		}
		// second avg
		if (option.equals(JoinOptions.getAvailableOptions().get(4))) {
			method = SetPaceMethod.TARGET_SPEED;
			targetSpeed = secondCourse.getAverageMovingSpeed();
		}
		// second start
		if (option.equals(JoinOptions.getAvailableOptions().get(5))) {
			method = SetPaceMethod.TARGET_SPEED;
			targetSpeed = secondCourse.getFirstTrackpoint().getSpeed();
		}
		// avg both
		if (option.equals(JoinOptions.getAvailableOptions().get(6))) {
			method = SetPaceMethod.TARGET_SPEED;
			targetSpeed = (firstCourse.getAverageMovingSpeed() + secondCourse.getAverageMovingSpeed()) / 2;
		}
		// avg connecting
		if (option.equals(JoinOptions.getAvailableOptions().get(7))) {
			method = SetPaceMethod.TARGET_SPEED;
			targetSpeed = (firstCourse.getLastTrackpoint().getSpeed() + secondCourse.getFirstTrackpoint().getSpeed())
					/ 2;
		}
		// keep time stamps
		if (option.equals(JoinOptions.getAvailableOptions().get(8))) {
			method = SetPaceMethod.TARGET_TIME;
			long firstCourseEndTime = firstCourse.getLastTrackpoint().getTimestamp().getTime();
			long secondCourseStartTime = secondCourse.getFirstTrackpoint().getTimestamp().getTime();
			if (secondCourseStartTime < firstCourseEndTime) {
				throw new TrackItException("Second Course starts before First Course");
			}
			targetTime = (double) (secondCourseStartTime - firstCourseEndTime) / 1000;
		}
		// user set time
		if (option.equals(JoinOptions.getAvailableOptions().get(9))) {
			method = SetPaceMethod.TARGET_TIME;

			targetTime = getJoinAppendTime() * 60;
		}

		paceOptions.put(Constants.SetPaceOperation.METHOD, method);
		paceOptions.put(Constants.SetPaceOperation.INCLUDE_PAUSES, true);
		if (method.equals(SetPaceMethod.TARGET_TIME)) {
			paceOptions.put(Constants.SetPaceOperation.TIME, targetTime);

		} else {
			targetSpeed *= 3.6;
			paceOptions.put(Constants.SetPaceOperation.SPEED, targetSpeed);

		}

		setPace(route, paceOptions, false);
	}

	/* Event Listener interface implementation */

	@Override
	public void process(Event event, DocumentItem item) {
		if ( event.equals(Event.COORDINATES_TYPE_CHANGED) ||
			 event.equals(Event.SEND_COORDINATES) ||		//71062: 2017-05-18
			 event.equals(Event.GET_COORDINATES) )			//71062: 2017-05-18
			return;
		boolean differentCourses = false;
		if (item instanceof Folder) {
			selectedFolder = (Folder) item;
			selectedItem = null;
		}
		else if( item instanceof Course){
			Course tempCourse = (Course) item;
			if(!tempCourse.equals(selectedCourse)){
				previousSelectedCourse = selectedCourse;
				selectedCourse = tempCourse;
				differentCourses = true;
			}
			if(mapMode == MapMode.LEGEND && updateLegend == true){
				//updateLegend = true;
				LegendLayer.update();
				
			}
			if(updateEdition == true && differentCourses){
				EditionLayer.updateFollowRoads();
				differentCourses = false;
			}
		
		} else if (item == null) {
			selectedItem = null;
			// Do nothing
		} else if (event == Event.ACTIVITY_ADDED) {
			// Do nothing
		} else {
			selectedItem = item;
			while (item != null && !(item instanceof GPSDocument)) {
				item = item.getParent();
			}

			selectedDocumentId = ((GPSDocument) item).getId();
			selectedFolder = getFolder((GPSDocument) item);
		}
	}

	@Override
	public void process(Event event, DocumentItem parent, List<? extends DocumentItem> items) {
		selectedItem = parent;

		while (parent != null && !(parent instanceof GPSDocument)) {
			parent = parent.getParent();
		}

		if (parent == null) {
			selectedDocumentId = null;
		} else {
			selectedDocumentId = ((GPSDocument) parent).getId();
		}
	}

	public DocumentItem getSelectedItem() {
		return selectedItem;
	}
	
	public Course getSelectedCourse() {
		return selectedCourse;
	}

	@Override
	public String toString() {
		return Messages.getMessage("documentManager.name");
	}

	class DocumentEntry {
		private Folder folder;
		private GPSDocument document;

		public DocumentEntry(Folder folder, GPSDocument document) {
			this.folder = folder;
			this.document = document;
		}

		Folder getFolder() {
			return folder;
		}

		void setFolder(Folder folder) {
			this.folder = folder;
		}

		GPSDocument getDocument() {
			return document;
		}

		void setDocument(GPSDocument document) {
			this.document = document;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (document.getId() ^ (document.getId() >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DocumentEntry other = (DocumentEntry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (document.getId() != other.document.getId())
				return false;
			return true;
		}

		private DocumentManager getOuterType() {
			return DocumentManager.this;
		}
	}

	// 58406

	
// SPORTS and SUBSPORTS (Start)

	public void loadSportFromDB(Activity activity, boolean firstLoad) {
//		short sportID = database.getSport(activity);
//		short subSportID = database.getSubSport(activity);
//		List<SportType> sportList = Arrays.asList(SportType.values());
//		List<SubSportType> subSportList = Arrays.asList(SubSportType.values());
//		for (SportType sport : sportList) {
//			if (sport.getSportID() == sportID) {
//				activity.setSport(sport);
//				activity.setTemporarySport(sport);
//				changeSportType(activity, true);
//				break;
//			}
//		}
//		for (SubSportType subSport : subSportList) {
//			if (subSport.getSubSportID() == subSportID && subSport.getSportID() == sportID) {
//				activity.setSubSport(subSport);
//				activity.setTemporarySubSport(subSport);
//				break;
//			}
//		}
//		changeSportType(activity, firstLoad);
	}

	public void loadSportFromDB(Course course, boolean firstLoad) {
//		short sportID = database.getSport(course);
//		short subSportID = database.getSubSport(course);
//		List<SportType> sportList = Arrays.asList(SportType.values());
//		List<SubSportType> subSportList = Arrays.asList(SubSportType.values());
//		for (SportType sport : sportList) {
//			if (sport.getSportID() == sportID) {
//				course.setSport(sport);
//				course.setTemporarySport(sport);
//				// changeSportType(course, true);
//				break;
//			}
//		}
//		for (SubSportType subSport : subSportList) {
//			if (subSport.getSubSportID() == subSportID && subSport.getSportID() == sportID) {
//				course.setSubSport(subSport);
//				course.setTemporarySubSport(subSport);
//				break;
//			}
//		}
//		changeSportType(course, firstLoad);
	}

	public void loadSports(Activity activity) {
//		short sportID = database.getSport(activity);
//		short subSportID = database.getSubSport(activity);
//		List<SportType> sportList = Arrays.asList(SportType.values());
//		List<SubSportType> subSportList = Arrays.asList(SubSportType.values());
//		if (sportID == -1 || subSportID == -1) {
//			JDialog sportDialog = new SportDialog(activity);
//			sportDialog.setVisible(true);
//			changeSportType(activity, true);
//		} else {
//			for (SportType sport : sportList) {
//				if (sport.getSportID() == sportID) {
//					activity.setSport(sport);
//					activity.setTemporarySport(sport);
//					changeSportType(activity, true);
//					break;
//				}
//			}
//			for (SubSportType subSport : subSportList) {
//				if (subSport.getSubSportID() == subSportID && subSport.getSportID() == sportID) {
//					activity.setSubSport(subSport);
//					activity.setTemporarySubSport(subSport);
//					break;
//				}
//			}
//			changeSportType(activity, false);
//		}
	}

	public void loadSports(Course course) {
//		short sportID = database.getSport(course);
//		short subSportID = database.getSubSport(course);
//		List<SportType> sportList = Arrays.asList(SportType.values());
//		List<SubSportType> subSportList = Arrays.asList(SubSportType.values());
//		if (sportID == -1 || subSportID == -1) {
//			JDialog sportDialog = new SportDialog(course);
//			sportDialog.setVisible(true);
//			changeSportType(course, true);
//		} else {
//			for (SportType sport : sportList) {
//				if (sport.getSportID() == sportID) {
//					course.setSport(sport);
//					course.setTemporarySport(sport);
//					break;
//				}
//			}
//			for (SubSportType subSport : subSportList) {
//				if (subSport.getSubSportID() == subSportID && subSport.getSportID() == sportID) {
//					course.setSubSport(subSport);
//					course.setTemporarySubSport(subSport);
//					break;
//				}
//			}
//			changeSportType(course, false);
//		}
	}
	
//	12335: 2018-07-16 - no longer necessary, DB initializes all SubSport at start time
//	private void updateSports(){
//		List<GPSDocument> documents = DocumentManager.getInstance().getDocuments();
//		List<Course> courses;
//		List<Activity> activities;
//		double defaultAverageSpeed;
//		double maximumAverageSpeed;
//		double pauseThresholdSpeed;
//		long defaultPauseDuration;
//		boolean followRoads;
//		double joinMaximumWarningDistance;
//		double joinMergeDistanceTolerance;
//		long joinMergeTimeTolerance;
//		double gradeLimit;
//		short  ascentDescentClass;
//		SportType sport;
//		SubSportType subSport;
//		boolean defaultValues = false;
//		for(GPSDocument document : documents){
//			courses = document.getCourses();
//			for(Course course : courses){
//				sport = course.getSport();
//				subSport = course.getSubSport();
//				defaultAverageSpeed = getDatabase().getDefaultAverageSpeed(sport, subSport, defaultValues);
//				maximumAverageSpeed = getDatabase().getMaximumAllowedSpeed(sport, subSport, defaultValues);
//				pauseThresholdSpeed = getDatabase().getPauseThresholdSpeed(sport, subSport, defaultValues);
//				defaultPauseDuration = getDatabase().getDefaultPauseDuration(sport, subSport, defaultValues);
//				followRoads = getDatabase().getFollowRoads(sport, subSport, defaultValues);
//				joinMaximumWarningDistance = getDatabase().getJoinMaximumWarningDistance(sport, subSport, defaultValues);
//				joinMergeDistanceTolerance = getDatabase().getJoinMergeDistanceTolerance(sport, subSport, defaultValues);
//				joinMergeTimeTolerance = getDatabase().getJoinMergeTimeTolerance(sport, subSport, defaultValues);
//				gradeLimit = getDatabase().getGradeLimit(sport, subSport, defaultValues);
//				ascentDescentClass = getDatabase().getAscentDescentClass( sport, subSport);	//12335: 2017-06-08
//				course.getSubSport().setValues(defaultAverageSpeed, maximumAverageSpeed, 
//						pauseThresholdSpeed, defaultPauseDuration, followRoads,
//						joinMaximumWarningDistance, joinMergeDistanceTolerance, joinMergeTimeTolerance,
//						gradeLimit, ascentDescentClass);
//			}
//			activities = document.getActivities();
//			for(Activity activity : activities){
//				sport = activity.getSport();
//				subSport = activity.getSubSport();
//				defaultAverageSpeed = getDatabase().getDefaultAverageSpeed(sport, subSport, defaultValues);
//				maximumAverageSpeed = getDatabase().getMaximumAllowedSpeed(sport, subSport, defaultValues);
//				pauseThresholdSpeed = getDatabase().getPauseThresholdSpeed(sport, subSport, defaultValues);
//				defaultPauseDuration = getDatabase().getDefaultPauseDuration(sport, subSport, defaultValues);
//				followRoads = getDatabase().getFollowRoads(sport, subSport, defaultValues);
//				joinMaximumWarningDistance = getDatabase().getJoinMaximumWarningDistance(sport, subSport, defaultValues);
//				joinMergeDistanceTolerance = getDatabase().getJoinMergeDistanceTolerance(sport, subSport, defaultValues);
//				joinMergeTimeTolerance = getDatabase().getJoinMergeTimeTolerance(sport, subSport, defaultValues);
//				gradeLimit = getDatabase().getGradeLimit(sport, subSport, defaultValues);
//				ascentDescentClass = getDatabase().getAscentDescentClass( sport, subSport); //12335: 2017-06-08
//				activity.getSubSport().setValues(defaultAverageSpeed, maximumAverageSpeed, 
//						pauseThresholdSpeed, defaultPauseDuration, followRoads,
//						joinMaximumWarningDistance, joinMergeDistanceTolerance, joinMergeTimeTolerance,
//						gradeLimit, ascentDescentClass);
//			}
//			
//		}
//	}

	// 12335: 2018-07-07
	public void remove( GPSDocument document) {
		discard( document, true);
	}
//	public void remove(GPSDocument document) {
//		Folder folder = getFolder(document);
//		folder.remove(document);
//		for (Activity a : document.getActivities()) {
//			remove(a);
////			database.deleteFromDB(a);          12335: 2018-06-29 - remove( Activity) already does this
//		}
//		for (Course c : document.getCourses()) {
//			remove(c);
////			database.deleteFromDB(c);          12335: 2018-06-29 - remove( Course) already does this
//		}
//		documents.remove(document.getId());// 58406
//		EventManager.getInstance().publish(this, Event.DOCUMENT_DISCARDED, document);
//	}

	// 12335: 2018-07-07
	public void remove( Activity activity) {
		discard( activity, true);
	}
//	public void remove(Activity activity) {
//		for (Folder currentFolder : folders) {
//			for (GPSDocument document : currentFolder.getDocuments()) {
//				if (document.getActivities().contains(activity)) {
//					document.remove(activity);
//					database.deleteFromDB(activity);
//					EventManager.getInstance().publish(this, Event.ACTIVITY_REMOVED, activity);
//					//12335: 2018-06-29
//					EventManager.getInstance().publish(this, Event.DOCUMENT_UPDATED, document);
//					return;
//				}
//			}
//		}
//	}

	// 12335: 2018-07-07
	public void remove( Course course) {
		discard( course, true);
	}
//	public void remove(Course course) {
//		for (Folder currentFolder : folders) {
//			for (GPSDocument document : currentFolder.getDocuments()) {
//				if (document.getCourses().contains(course)) {
//					document.remove(course);
//					database.deleteFromDB(course);
//					EventManager.getInstance().publish(this, Event.COURSE_REMOVED, course);
//					//12335: 2018-06-29
//					EventManager.getInstance().publish(this, Event.DOCUMENT_UPDATED, document);
//					return;
//				}
//			}
//		}
//	}
	
	//57421
	public void changeSportType(Activity activity, boolean firstLoad) {
//		activity.setSport(activity.getTemporarySport());
//		activity.setSubSport(activity.getTemporarySubSport());
//		for (Session session : activity.getSessions()) {
//			session.setSport(activity.getTemporarySport());
//			session.setSubSport(activity.getTemporarySubSport());
//			for (Lap lap : session.getLaps()) {
//				lap.setSport(activity.getTemporarySport());
//				lap.setSubSport(activity.getTemporarySubSport());
//			}
//		}
//		if (!firstLoad) {
//			activity.publishUpdateEvent(null);
//		}
//		database.updateDBSport(activity);
	}


	//57421
	public void changeSportType(Course course, boolean firstLoad) {
//		
//		course.setSport(course.getTemporarySport());
//		course.setSubSport(course.getTemporarySubSport());
//		for (Lap lap : course.getLaps()) {
//			lap.setSport(course.getTemporarySport());
//			lap.setSubSport(course.getTemporarySubSport());
//		}
//		if (!firstLoad) {
//			course.publishUpdateEvent(null);
//		}
//		database.updateDBSport(course);
	}

	
	//57421
	public void compareSegments(final List<Course> segments) {
		Objects.requireNonNull(segments);
		for (Course segment : segments) {
			if (segment.getSegmentDistance() == null || segment.getSegmentMovingTime() == null) {
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
						Messages.getMessage("dialog.compareSegments.notSegment", segment.getName()),
						Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		CompareSegmentsOperation compareSegmentsOperation = new CompareSegmentsOperation(segments);
		compareSegmentsOperation.compareSegments();
	}
	
	
	
	//57421
	public void colorGrading(final Course course){
		Objects.requireNonNull(course);
		SportType sport = course.getSport();
		if(sport.equals(SportType.SAILING)){
			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), 
										  Messages.getMessage("colorGrading.message.unsupportedSport"),
					Messages.getMessage("applicationPanel.title.warning"),
			        JOptionPane.WARNING_MESSAGE);

			return;
		}

		final GPSDocument masterDocument = course.getParent();
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.colorGrading");
			}

			@Override
			public Object execute() throws TrackItException {
				return colorGrading(course);
			}

			private Course colorGrading(Course course) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.SplitIntoSegmentsOperation.COURSE, course);
				ColorGradingOperation colorGradingOperation = new ColorGradingOperation(options);
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);

				try {
					colorGradingOperation.process(document);

				} catch (TrackItException e) {
					logger.error(e.getMessage());
					logger.error("---->colorGrading<----");
					return null;
				}
				return document.getCourses().get(0);
			}

			@SuppressWarnings("unchecked")
			@Override
			public void done(Object result) {
				Course finalCourse = (Course) result;
				finalCourse.setParent(masterDocument);
//				finalCourse.setUnsavedTrue();
				finalCourse.setTrackStatusTo( true);
				masterDocument.remove(course);
				masterDocument.add(finalCourse);
				masterDocument.publishUpdateEvent(null);
				finalCourse.publishSelectionEvent(null);

			}
		}).execute();
	}
	
	
	//57421
	//test function for use with test menu
	public void test(Course course){
		
	}
	
	/*public void test(Course course){
		createSegments(course);
		TrackSegment.switchColors(course.getSegments());
	}*/
	//57421
	public void splitIntoSegments(final Course course, final boolean isNumber, final boolean isTime,
			final boolean isDistance, final double targetValue, final boolean addToUndoManager, final boolean redo) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(isNumber);
		Objects.requireNonNull(isTime);
		Objects.requireNonNull(isDistance);
		Objects.requireNonNull(targetValue);
		Objects.requireNonNull(addToUndoManager);

		final GPSDocument masterDocument = course.getParent();
		
		
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.splitingCourseIntoSegments");
			}

			@Override
			public Object execute() throws TrackItException {
				return splitIntoSegments(course, targetValue);
			}

			private List<Course> splitIntoSegments(Course course, double targetValue) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.SplitIntoSegmentsOperation.COURSE, course);
				options.put(Constants.SplitIntoSegmentsOperation.VALUE, targetValue);

				SplitIntoSegmentsOperation splitIntoSegmentsOperation = new SplitIntoSegmentsOperation(options);
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);

				try {
					if (isNumber) {
						splitIntoSegmentsOperation.processNumber(document);
					} else if (isTime) {
						splitIntoSegmentsOperation.processDuration(document);
					} else if (isDistance) {
						splitIntoSegmentsOperation.processLength(document);
					}
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					logger.error("---->splitIntoSegments<----");
					return null;
				}
				return document.getCourses();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void done(Object result) {
				List<Course> courses = (List<Course>) result;
				for (Course course : courses) {
					course.setParent(masterDocument);
//					course.setUnsavedTrue();			// 12335 : 2016-10-03
					course.setTrackStatusTo( true);
				}

				// undo
				if (addToUndoManager) {
					String name = UndoableActionType.SPLIT_INTO_SEGMENTS.toString();
					List<Long> courseIds = new ArrayList<Long>();
					List<Long> courseIdsToRemove = new ArrayList<Long>();
					for (Course course : courses) {
						courseIds.add(course.getId());
						courseIdsToRemove.add(course.getId());
					}

					courseIdsToRemove.remove(0);
					long documentId = course.getParent().getId();
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).segmentValue(targetValue)
							.isNumber(isNumber).isTime(isTime).isDistance(isDistance).removedCourses(courseIdsToRemove)
							.build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
				}
				if (redo) {
					List<Long> courseIds = new ArrayList<Long>();
					List<Long> courseIdsToRemove = new ArrayList<Long>();
					for (Course course : courses) {
						courseIds.add(course.getId());
						courseIdsToRemove.add(course.getId());
					}

					undoItem.getCoursesIds().clear();
					undoItem.getCoursesIds().addAll(courseIds);
					undoItem.getRemovedCourses().addAll(courseIdsToRemove);
					undoItem = null;
				}
				//

				masterDocument.remove(course);
				masterDocument.addCourses(courses);
				masterDocument.publishUpdateEvent(null);
				courses.get(0).publishSelectionEvent(null);

			}
		}).execute();
	}
	//57421
	public void createSegment(final TrackSegment segment, final boolean addToUndoManager, final boolean redo) {
		Objects.requireNonNull(segment);
		Objects.requireNonNull(addToUndoManager);

		final GPSDocument masterDocument = (GPSDocument) segment.getParent().getParent();
		final Course course = ((Course) segment.getParent());
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.creatingSegment");
			}

			@Override
			public Object execute() throws TrackItException {
				return createSegment(segment);
			}

			private List<Course> createSegment(TrackSegment segment) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.SplitIntoSegmentsOperation.SEGMENT, segment);
				options.put(Constants.SplitIntoSegmentsOperation.COURSE, course);
				SplitIntoSegmentsOperation splitIntoSegmentsOperation = new SplitIntoSegmentsOperation(options);
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);

				try {
					splitIntoSegmentsOperation.processCreateSegment(document);

				} catch (TrackItException e) {
					logger.error(e.getMessage());
					logger.error("---->splitIntoSegments<----");
					return null;
				}
				return document.getCourses();
			}

			@SuppressWarnings("unchecked")
			@Override
			public void done(Object result) {
				List<Course> courses = (List<Course>) result;
				for (Course course : courses) {
					course.setParent(masterDocument);
//					course.setUnsavedTrue();
					course.setTrackStatusTo( true);
				}
				// undo
				if (addToUndoManager) {
					String name = UndoableActionType.CREATE_SEGMENT.toString();
					List<Long> courseIds = new ArrayList<Long>();
					List<Long> courseIdsToRemove = new ArrayList<Long>();
					for (Course course : courses) {
						courseIds.add(course.getId());
						courseIdsToRemove.add(course.getId());
					}

					courseIdsToRemove.remove(0);
					long documentId = course.getParent().getId();
					UndoItem item = new UndoItem.UndoItemBuilder(name, courseIds, documentId).segment(segment)
							.removedCourses(courseIdsToRemove).build();
					undoManager.addUndo(item);
					TrackIt.getApplicationPanel().forceRefresh();
				}
				if (redo) {
					List<Long> courseIds = new ArrayList<Long>();
					List<Long> courseIdsToRemove = new ArrayList<Long>();
					for (Course course : courses) {
						courseIds.add(course.getId());
						courseIdsToRemove.add(course.getId());
					}

					undoItem.getCoursesIds().clear();
					undoItem.getCoursesIds().addAll(courseIds);
					undoItem.getRemovedCourses().addAll(courseIdsToRemove);
					undoItem = null;
				}
				//

				masterDocument.remove(course);
				masterDocument.addCourses(courses);
				masterDocument.publishUpdateEvent(null);
				courses.get(1).publishSelectionEvent(null);

			}
		}).execute();
	}

	/*
	 * UNDO/REDO 57421
	 */

	String undoMode = Constants.UndoOperation.UNDO;
	String redoMode = Constants.UndoOperation.REDO;

//	private boolean isUndoPossible(UndoItem item) {			//12335: 2017-04-03
//12335: 2017-04-08 MoveTo, CopyTo, Duplicate support
	private boolean isUndoPossible( UndoItem item, boolean undo) {
		GPSDocument document;
		
		String operationType = item.getOperationType();
		boolean moveToCopyToDuplicateExceptions = 
				operationType.equals( UndoableActionType.MOVE_TO.toString()) ||
				operationType.equals( UndoableActionType.COPY_TO.toString()) ||
				operationType.equals( UndoableActionType.DUPLICATE.toString());
		
		if ( !moveToCopyToDuplicateExceptions ) {
			document = getDocument( item.getDocumentId());
			if ( document == null )
				return false;
			
			List<Long> coursesIds = new ArrayList<Long>();

			if ( item.getOperationType().equals(UndoableActionType.JOIN.toString()) ||
				 item.getOperationType().equals(UndoableActionType.APPEND_JOIN.toString())) {
				coursesIds.add(item.getCoursesIds().get(item.getCoursesIds().size() - 1));
			} else {
//12335: 2017-04-19
//				coursesIds = item.getCoursesIds();
				if ( operationType.equals( UndoableActionType.REVERSE.toString()) && !undo )
					coursesIds.add( item.getCourseIdAt( 0));
				else
					coursesIds = item.getCoursesIds();
			}
			for (long id : coursesIds) {
//				Course course = getCourse(document, id);				//12335: 2017-04-25
//				if (course == null) {
//					System.out.println( "course is null, id: " + id);
//					return false;
//				}
				DocumentItem docItem = null;
				if ( id == document.getId() )
					docItem = document;
				else
					docItem = getDocumentItem( document, id);
				if ( docItem == null ) {
					System.out.println( "Item is null, id: " + id);
					return false;
				}
			}
		}
		
		if ( moveToCopyToDuplicateExceptions) {
			if ( undo ) {
				List<Long> dstItemsIds = item.getDestinationItemsIds();
				List<Long> dstDocsids;
				if ( operationType.equals( UndoableActionType.DUPLICATE.toString()) )
					dstDocsids = item.getOriginDocumentsIds();
				else
					dstDocsids = new ArrayList<>( Collections.nCopies( dstItemsIds.size(), item.getDocumentId()));
				Iterator<Long> dstDocsIterator     = dstDocsids.iterator();
				GPSDocument doc;
				for( long id: dstItemsIds) {
					long dstDocId  = dstDocsIterator.next();
					if ( (doc = getDocument( dstDocId)) == null )
						return false;
					if ( getDocument(id) == null  && getDocumentItem( doc, id) == null )
						return false;
				}
				if ( operationType.equals( UndoableActionType.MOVE_TO.toString())) {
					List<Long>srcDocsIds  = item.getOriginDocumentsIds();
					for( long id: srcDocsIds)
						if ( getDocument( id) == null )
							return false;
				}
			}
			else {
				List<Long> srcItemsIds = item.getCoursesIds();
				List<Long> srcDocsIds  = item.getOriginDocumentsIds();
				Iterator<Long> docsIterator = srcDocsIds.iterator();
				GPSDocument  doc     = null;
				for( long id: srcItemsIds) {
					long srcDocId = docsIterator.next();
					if ( (doc = getDocument( srcDocId)) == null )
						return false;
					if ( id != srcDocId && getDocumentItem( doc, id) == null )
						return false;
				}
				if ( !operationType.equals( UndoableActionType.DUPLICATE.toString()) ) 
					if ( !item.createdDestinationDocument() ) {
						if ( getDocument( item.getDocumentId()) == null )
							return false;
					}
			}
		}

		return true;
	}

	public void undo() throws TrackItException {
		boolean undo = true;
		undoRedo(undo);

	}

	public void redo() throws TrackItException {
		boolean undo = false;
		undoRedo(undo);

	}

	private void undoRedo(boolean undo) throws TrackItException {
		final UndoItem item;
		String undoRedoMode = new String();

		if (undo) {
			undoRedoMode = this.undoMode;
			item = undoManager.getUndoableItem();
		} else {
			undoRedoMode = this.redoMode;
			item = undoManager.getRedoableItem();
		}
		final String operationType = item.getOperationType();
		final UndoableActionType undoableAction = UndoableActionType.lookup(operationType);
//		if (isUndoPossible(item)) {			// 12335: 2017-04-03
		if ( isUndoPossible(item, undo) ) {

			switch (undoableAction) {
			case ADD_TRACKPOINT:
				addTrackpointSetup(item, undoRedoMode);
				break;
			case APPEND:
				appendSetup(item, undoRedoMode);
				break;
			case REMOVE_TRACKPOINT:
				removeTrackpointSetup(item, undoRedoMode);
				break;
			case JOIN:
				try {
					joinSetup(item, undoRedoMode);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case APPEND_JOIN:
				appendJoinSetup(item, undoRedoMode);
				break;
			case SPLIT:
				splitSetup(item, undoRedoMode);
				break;
			case REVERSE:
				reverseSetup(item, undoRedoMode);
				break;
			case ADD_PAUSE:
				addPauseSetup(item, undoRedoMode);
				break;
			case CHANGE_PAUSE_DURATION:
				changePauseDurationSetup(item, undoRedoMode);
				break;
			case REMOVE_PAUSE:
				removePauseSetup(item, undoRedoMode);
				break;
			case REMOVE_PAUSES:
				removeAllPausesSetup(item, undoRedoMode);
				break;
			case SET_PACE:
				setPaceSetup(item, undoRedoMode);
				break;
			case SMART_PACE:
				smartPaceSetup(item, undoRedoMode);
				break;
			case COPY_TO:
				copyToSetup( item, undo);
				break;
			case RENAME:
				renameSetup(item, undoRedoMode);
				break;
			case DUPLICATE:
			case COPY:
				duplicateSetup( item, undo); 
				break;
			case MOVE_TO:
				copyToSetup( item, undo);
				break;
			case CHANGE_START_TIME:
				changeTimeSetup(item, "start", undoRedoMode);
				break;
			case CHANGE_END_TIME:
				changeTimeSetup(item, "end", undoRedoMode);
				break;
			case SPLIT_INTO_SEGMENTS:
				splitIntoSegmentsSetup(item, undoRedoMode);
				break;
			case CREATE_SEGMENT:
				createSegmentSetup(item, undoRedoMode);
				break;
			default:
				// Do nothing
			}
			if (undo) {
				undoManager.popUndo();
			} else {
				undoManager.popRedo();
			}

		} else {
			undoManager.deleteUndo();
			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
					Messages.getMessage( "documentManager.message.undoUnavailableExplanation"),
					Messages.getMessage( "documentManager.message.undoUnavailableTitle"),
					JOptionPane.ERROR_MESSAGE);
		}

	}

	private void splitIntoSegmentsSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		final double segmentValue = item.getSegmentValue();
		final boolean isNumber = item.isNumber();
		final boolean isTime = item.isTime();
		final boolean isDistance = item.isDistance();

		List<Course> courses = new ArrayList<Course>();

		for (long id : item.getCoursesIds()) {
			courses.add(getCourse(document, id));
		}
		if (undoMode.equals(this.undoMode)) {
			courses.get(0).setSegmentNumber(-(courses.size()-1));
			courses.remove(0);
			deleteCourses(document, courses);
			List<Long> removedCourses = new ArrayList<Long>();
			for (long id : item.getCoursesIds()) {
				removedCourses.add(id);
			}
			removedCourses.remove(0);
			item.getCoursesIds().removeAll(removedCourses);
			item.getRemovedCourses().clear();

		}
		if (undoMode.equals(this.redoMode)) {
			final Course course = courses.get(0);
			List<Course> allCoursesBeforeSplit = new ArrayList<Course>();
			for (Course documentCourse : document.getCourses()) {
				allCoursesBeforeSplit.add(documentCourse);
			}
			boolean redo = true;
			this.undoItem = item;
			splitIntoSegments(course, isNumber, isTime, isDistance, segmentValue, addToUndoManager, redo);

		}
	}

	private void createSegmentSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		final TrackSegment segment = item.getSegment();

		List<Course> courses = new ArrayList<Course>();

		for (long id : item.getCoursesIds()) {
			courses.add(getCourse(document, id));
		}
		if (undoMode.equals(this.undoMode)) {
			courses.get(0).setSegmentNumber(-1);
			courses.remove(0);
			deleteCourses(document, courses);
			List<Long> removedCourses = new ArrayList<Long>();
			for (long id : item.getCoursesIds()) {
				removedCourses.add(id);
			}
			removedCourses.remove(0);
			item.getCoursesIds().removeAll(removedCourses);
			item.getRemovedCourses().clear();

		}
		if (undoMode.equals(this.redoMode)) {
			boolean redo = true;
			this.undoItem = item;
			createSegment(segment, addToUndoManager, redo);

		}
	}

	private void deleteCourses(GPSDocument document, List<Course> coursesToDelete) {
		List<Course> documentCourses = document.getCourses();
		List<Course> coursesToRemove = new ArrayList<Course>();
		for (Course course : documentCourses) {
			if (coursesToDelete.contains(course)) {
				coursesToRemove.add(course);
			}
		}
		for (Course course : coursesToRemove) {
			remove(course);
		}
		// document.setCourses(newDocumentCourses);
	}

	private void addPauseSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		Course course = getCourse(document, item.getCourseIdAt(0));
		Double pointSpeed = item.getPauseInformation().getOldSpeed();
		long pausedTime = item.getPauseInformation().getPauseTime();
		long startId = item.getPauseInformation().getStartPauseID();
		long endId = item.getPauseInformation().getEndPauseID();
		this.undoItem = item;

		if (undoMode.equals(this.undoMode)) {
			undoAddPause(course, getTrackpoint(course, startId), getTrackpoint(course, endId), pausedTime, pointSpeed);
		}
		if (undoMode.equals(this.redoMode)) {
			addPause(course, getTrackpoint(course, startId), pausedTime, addToUndoManager);
		}
	}

	private void changePauseDurationSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		double oldPauseDuration = item.getOldPauseTime().getFirst();
		double newPauseDuration = item.getOldPauseTime().getSecond();
		Course course = getCourse(document, item.getCourseIdAt(0));
		Trackpoint trackpoint = item.getTrackpoint();

		if (undoMode.equals(this.undoMode)) {
			changePauseDuration(course, trackpoint, (long) oldPauseDuration, addToUndoManager);
		}
		if (undoMode.equals(this.redoMode)) {
			changePauseDuration(course, trackpoint, (long) newPauseDuration, addToUndoManager);
		}
	}

	public void undoAddPause(final Course course, final Trackpoint start, final Trackpoint end, final long pausedTime,
			final Double speed) {
		Objects.requireNonNull(course);
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.removePauses");
			}

			@Override
			public Object execute() throws TrackItException {
				RemovePausesOperation removePausesOperation = new RemovePausesOperation();
				removePausesOperation.undoAddPauseSetup(course, start, end, pausedTime, speed);
				removePausesOperation.executeUndoAddPause();
				return null;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				course.getPauses().clear();
				new PauseDetectionPicCaseOperation().process(course);
				TrackIt.getApplicationPanel().forceRefresh();

			}
		}).execute();
	}
	
	private void removePauseSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		Course course = getCourse(document, item.getCourseIdAt(0));
		//final List<Lap> laps = item.getLaps();
		long pausedTime = item.getPauseInformation().getPauseTime();
		Trackpoint leftEdge = item.getPauseInformation().getStart();
		Trackpoint rightEdge = item.getPauseInformation().getEnd();
		List<Trackpoint> removedPoints = item.getPauseInformation().getRemovedPoints();
		Trackpoint pausePoint = getTrackpoint(course, removedPoints.get(0).getId());

		if (undoMode.equals(this.undoMode)) {
			undoRemovePause(course, removedPoints, leftEdge, rightEdge, pausedTime);
		}
		if (undoMode.equals(this.redoMode)) {
			removePause(course, pausePoint, addToUndoManager, null);
		}
	}

	public void undoRemovePause(final Course course, final List<Trackpoint> removedPoints, final Trackpoint start,
			final Trackpoint end, final long pausedTime) {
		Objects.requireNonNull(course);
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.removePauses");
			}

			@Override
			public Object execute() throws TrackItException {
				RemovePausesOperation removePausesOperation = new RemovePausesOperation();
				removePausesOperation.undoRemovePauseSetup(course, removedPoints, start, end, pausedTime);
				removePausesOperation.executeUndoRemovePause();
				return null;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				course.getPauses().clear();
				//course.setLaps(laps);
				new PauseDetectionPicCaseOperation().process(course);
				TrackIt.getApplicationPanel().forceRefresh();

			}
		}).execute();
	}

	private void removeAllPausesSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		Course course = getCourse(document, item.getCourseIdAt(0));
		final List<RemovePausesOperation.PauseInformation> removedPauses = item.getRemovedPauses();

		if (undoMode.equals(this.undoMode)) {
			undoRemoveAllPauses(course, removedPauses);
		}
		if (undoMode.equals(this.redoMode)) {
			removeAllPauses(course, addToUndoManager);
		}
	}

	public void undoRemoveAllPauses(final Course course,
			final List<RemovePausesOperation.PauseInformation> removedPauses) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(removedPauses);

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.removePauses");
			}

			@Override
			public Object execute() throws TrackItException {
				long pausedTime;
				Trackpoint leftEdge;
				Trackpoint rightEdge;
				List<Trackpoint> removedPoints;
				for (RemovePausesOperation.PauseInformation removedPause : removedPauses) {
					pausedTime = removedPause.getPauseTime();
					leftEdge = removedPause.getLeftEdge();
					rightEdge = removedPause.getRightEdge();
					removedPoints = removedPause.getRemovedPoints();
					RemovePausesOperation removePausesOperation = new RemovePausesOperation();
					removePausesOperation.undoRemovePauseSetup(course, removedPoints, leftEdge, rightEdge, pausedTime);
					removePausesOperation.executeUndoRemovePause();
				}

				return null;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				course.getPauses().clear();
				new PauseDetectionPicCaseOperation().process(course);
				TrackIt.getApplicationPanel().forceRefresh();

			}
		}).execute();
	}

	private void addTrackpointSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		Course course = getCourse(document, item.getCourseIdAt(0));
		if (undoMode.equals(this.undoMode)) {
			removeTrackpoint(course, item.getTrackpoint(), addToUndoManager);
		}
		if (undoMode.equals(this.redoMode)) {
			addTrackpoint(course, item.getTrackpoint(), item.getTrackpointIndex(), addToUndoManager);
		}
	}

	private void removeTrackpointSetup(final UndoItem item, final String undoMode) {

		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		Course course = getCourse(document, item.getCourseIdAt(0));
		if (undoMode.equals(this.undoMode)) {
			addTrackpoint(course, item.getTrackpoint(), item.getTrackpointIndex(), addToUndoManager);
		}
		if (undoMode.equals(this.redoMode)) {
			removeTrackpoint(course, item.getTrackpoint(), addToUndoManager);
		}

	}

	private void setPaceSetup(final UndoItem item, final String undoMode) throws TrackItException {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		boolean addToUndoManager = false;
		Course course = getCourse(document, item.getCourseIdAt(0));
		if (undoMode.equals(this.undoMode) || (undoMode.equals(this.redoMode))) {
			setPace(course, item.getPaceOptions(), addToUndoManager);
		}
	}

	private void smartPaceSetup(final UndoItem item, final String undoMode) throws TrackItException {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final List<Long> courses = new ArrayList<Long>();
		final List<Pair<Boolean, Course>> oldCourseInfo = item.getJoinedCoursesInfo();

		boolean addToUndoManager = false;
		for (long id : item.getCoursesIds()) {
			courses.add(id);
		}
		if (undoMode.equals(this.undoMode)) {
			Course course = getCourse(document, courses.get(courses.size() - 1));
			courses.remove(courses.size() - 1);
			undoSmartPace(course, oldCourseInfo);

		}
		if (undoMode.equals(this.redoMode)) {
			Course course = getCourse(document, courses.get(courses.size() - 1));
			courses.remove(courses.size() - 1);
			setPace(course, item.getPaceOptions(), addToUndoManager);
		}
	}

	public void undoSmartPace(Course course, List<Pair<Boolean, Course>> oldCourseInfo) {
		Course oldCourse = oldCourseInfo.get(0).getSecond();
		copyData(course, oldCourse);

		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(course);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
		try {
			new ConsolidationOperation(options).process(document);
		} catch (TrackItException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GPSDocument masterDocument = course.getParent();
		masterDocument.publishUpdateEvent(null);
		course.publishSelectionEvent(null);
		TrackIt.getApplicationPanel().forceRefresh();

	}

	private void reverseSetup(final UndoItem item, final String undoMode) {
//		final DocumentEntry entry = documents.get(item.getDocumentId());	//12335: 2017-04-18
//		final GPSDocument document = entry.getDocument();
		final GPSDocument document = getDocument( item.getDocumentId());
		final boolean addToUndoManager = false;
		final boolean undo             = undoMode.equals( this.undoMode);	//12335: 2017-04-19
		Course course = getCourse(document, item.getCourseIdAt(0));
		final String reverseMode = item.getReverseMode();
//		if (undoMode.equals(this.undoMode) || (undoMode.equals(this.redoMode))) {	//12335: 2017-04-19
//
//			reverse(course, reverseMode, addToUndoManager, undoMode);
		long returnNewID = -1L;
		if ( reverseMode.equals( Constants.ReverseOperation.RETURN_NEW) )
			returnNewID = item.getCourseIdAt( 1);
		reverse(course, reverseMode, addToUndoManager, undo, returnNewID);
	}
	
	private void changeTimeSetup(final UndoItem item, final String mode, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final boolean addToUndoManager = false;
		Course course = getCourse(document, item.getCourseIdAt(0));
		final long oldTime = item.getOldTime();
		final long newTime = item.getNewTime();
		if (undoMode.equals(this.undoMode)) {
			if (mode.equals("start")) {
				changeStartTime(course, oldTime, addToUndoManager);
			} else if (mode.equals("end")) {
				changeEndTime(course, oldTime, addToUndoManager);
			}
		}
		if (undoMode.equals(this.redoMode)) {
			if (mode.equals("start")) {
				changeStartTime(course, newTime, addToUndoManager);
			} else if (mode.equals("end")) {
				changeEndTime(course, newTime, addToUndoManager);
			}
		}
	}

	public void appendSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		Course appendCourse = getCourse(document, item.getCourseIdAt(0));
		Trackpoint trackpoint = appendCourse.getTrackpoints().get(item.getTrackpointIndex());
		if (undoMode.equals(this.undoMode)) {
			final boolean keepSpeed = true;

			final Map<String, Object> undoOptions = new HashMap<String, Object>();
			undoOptions.put(Constants.ExtraUndoOptions.ADD_TO_MANAGER, false);
			undoOptions.put(Constants.ExtraUndoOptions.SPLIT_UNDO, false);
			undoOptions.put(Constants.ExtraUndoOptions.JOIN_UNDO, false);
			undoOptions.put(Constants.ExtraUndoOptions.APPEND_UNDO, true);

			// undoAppend(appendCourse, trackpoint);
			splitAtSelected(appendCourse, trackpoint, keepSpeed, undoOptions);
		}
		if (undoMode.equals(this.redoMode)) {
			MapProvider mapProvider = item.getMapProvider();
			Map<String, Object> routingOptions = item.getRoutingOptions();
			Location location = item.getLocation();
			final boolean addToUndoManager = false;
			try {
				appendRoute(mapProvider, routingOptions, appendCourse, location, addToUndoManager);
			} catch (TrackItException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void splitSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final Double splitSpeed = item.getSplitSpeed();
		List<Course> courses = new ArrayList<Course>();

		final Map<String, Object> undoOptions = new HashMap<String, Object>();
		undoOptions.put(Constants.ExtraUndoOptions.ADD_TO_MANAGER, false);
		undoOptions.put(Constants.ExtraUndoOptions.SPLIT_UNDO, true);
		undoOptions.put(Constants.ExtraUndoOptions.JOIN_UNDO, false);
		undoOptions.put(Constants.ExtraUndoOptions.APPEND_UNDO, false);

		boolean keepSpeed = true;

		for (long id : item.getCoursesIds()) {
			courses.add(getCourse(document, id));
		}

		if (undoMode.equals(this.undoMode)) {

			if (splitSpeed != null) {
				int lastIndex = courses.get(0).getTrackpoints().size() - 1;
				courses.get(0).getTrackpoints().get(lastIndex).setSpeed(splitSpeed);
				courses.get(1).getTrackpoints().get(0).setSpeed(splitSpeed);
			}
			join(courses, null, null, undoOptions);

		}
		if (undoMode.equals(this.redoMode)) {
			if (splitSpeed != null) {
				keepSpeed = false;
			}
			splitAtSelected(courses.get(0), item.getTrackpoint(), keepSpeed, undoOptions);

		}
	}

	public void appendJoinSetup(final UndoItem item, final String undoMode) {
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument document = entry.getDocument();
		final List<Long> courses = new ArrayList<Long>();
		final List<Pair<Boolean, Course>> joinedCoursesInfo = item.getJoinedCoursesInfo();
		for (long id : item.getCoursesIds()) {
			courses.add(id);
		}
		if (undoMode.equals(this.undoMode)) {
			Course joinedCourse = getCourse(document, courses.get(courses.size() - 1));
			courses.remove(courses.size() - 1);
			undoAppendJoin(joinedCourse, joinedCoursesInfo);

		}
		if (undoMode.equals(this.redoMode)) {
			List<Course> coursesToJoin = new ArrayList<Course>();

			final Map<String, Object> undoOptions = new HashMap<String, Object>();
			undoOptions.put(Constants.ExtraUndoOptions.ADD_TO_MANAGER, false);
			undoOptions.put(Constants.ExtraUndoOptions.SPLIT_UNDO, false);
			undoOptions.put(Constants.ExtraUndoOptions.JOIN_UNDO, false);
			undoOptions.put(Constants.ExtraUndoOptions.APPEND_UNDO, true);
			for (int i = 0; i < joinedCoursesInfo.size(); i++) {
				long id = joinedCoursesInfo.get(i).getSecond().getId();
				boolean original = joinedCoursesInfo.get(i).getFirst();
				for (Course course : document.getCourses()) {
					if (course.getId() == id && original) {
						coursesToJoin.add(course);
					}
				}
			}
			try {
				final double minimumDistance = getMinimumDistance(coursesToJoin.get(0));
				join(coursesToJoin, false, minimumDistance, undoOptions);
			} catch (TrackItException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TrackIt.getApplicationPanel().forceRefresh();

		}
	}

	private void consolidate(Course course) {
		for (Lap lap : course.getLaps()) {
			lap.consolidate(ConsolidationLevel.SUMMARY);
		}
		course.consolidate(ConsolidationLevel.SUMMARY);
	}

	public void undoAppendJoin(final Course course, final List<Pair<Boolean, Course>> joinedCoursesInfo) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(joinedCoursesInfo);
		final GPSDocument masterDocument = course.getParent();

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.splitingCourse");
			}

			@Override
			public Object execute() throws TrackItException {
				return splitCourses(course);
			}

			private List<Course> splitCourses(Course course) {
				List<Trackpoint> indexSearcher = new ArrayList<Trackpoint>();
				for (int i = 0; i < joinedCoursesInfo.size(); i++) {
					indexSearcher.addAll(joinedCoursesInfo.get(i).getSecond().getTrackpoints());
				}
				Collections.reverse(joinedCoursesInfo);
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);
				for (int i = 0; i < joinedCoursesInfo.size() - 1; i++) {
					Trackpoint splitPoint = joinedCoursesInfo.get(i).getSecond().getFirstTrackpoint();
					int currentIndex = indexSearcher.indexOf(splitPoint);
					splitPoint = course.getTrackpoints().get(currentIndex);
					Map<String, Object> splitOptions = new HashMap<String, Object>();
					splitOptions.put(Constants.SplitAtSelectedOperation.COURSE, course);
					splitOptions.put(Constants.SplitAtSelectedOperation.TRACKPOINT, splitPoint);
					TrackSplittingOperation splittingOperation = new TrackSplittingOperation(splitOptions);

					try {
						splittingOperation.undoOperation(document);
					} catch (TrackItException e) {
						logger.error(e.getMessage());
						return null;
					}
					indexSearcher.removeAll(indexSearcher.subList(currentIndex, indexSearcher.size() - 1));
					Course oldCourse = joinedCoursesInfo.get(i).getSecond();
					boolean original = joinedCoursesInfo.get(i).getFirst();
					Course newCourse = document.getCourses().get(i + 1);
					if (original) {
						copyData(newCourse, oldCourse);
					}
				}
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}

				Course first = document.getCourses().get(0);
				document.remove(first);
				document.add(course);
				Collections.reverse(document.getCourses());
				Collections.reverse(joinedCoursesInfo);
				Course oldCourse = joinedCoursesInfo.get(0).getSecond();
				Course newCourse = document.getCourses().get(0);
				copyData(newCourse, oldCourse);

				for (Course course2 : document.getCourses()) {
					consolidate(course2);
				}

				return document.getCourses();

			}

			@SuppressWarnings("unchecked")
			@Override
			public void done(Object result) {
				List<Course> courses = (List<Course>) result;
				List<Course> newCourses = new ArrayList<Course>();
				for (int i = 0; i < courses.size(); i++) {
					boolean original = joinedCoursesInfo.get(i).getFirst();
					if (original) {
						newCourses.add(courses.get(i));
					}
				}

				for (Course course : newCourses) {
					course.setParent(masterDocument);
//					course.setUnsavedTrue();
					course.setTrackStatusTo( true);
				}

				masterDocument.remove(course);
				masterDocument.addCourses(newCourses);
				masterDocument.publishUpdateEvent(null);
				courses.get(0).publishSelectionEvent(null);
				TrackIt.getApplicationPanel().forceRefresh();

			}
		}).execute();
	}

	public void copyData(Course newCourse, Course oldCourse) {
		newCourse.setName(oldCourse.getName());
//		newCourse.setSport(oldCourse.getSport());				//12335: 2016-06-15
//		newCourse.setSubSport(oldCourse.getSubSport());			//12335: 2016-06-15
		newCourse.setSportAndSubSport(oldCourse.getSport(), oldCourse.getSubSport());	//12335: 2016-06-15
		newCourse.setNotes(oldCourse.getNotes());
		newCourse.setId(oldCourse.getId());
		newCourse.setLaps(oldCourse.getLaps());
		for (int i = 0; i < newCourse.getTrackpoints().size(); i++) {
			Trackpoint oldPoint = oldCourse.getTrackpoints().get(i);
			Trackpoint newPoint = newCourse.getTrackpoints().get(i);
			oldPoint.copyData(newPoint);
		}
	}

	public void joinSetup(final UndoItem item, final String undoMode) throws TrackItException, InterruptedException {
		final List<Long> courses = new ArrayList<Long>();
		final DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument masterDocument = entry.getDocument();
		final List<Long> merge = item.getDuplicatePointIds();
		final List<Long> routeIds = item.getRouteIds();
		final Map<Long, Date[]> startTimes = item.getStartTimes();
		final List<Pair<Boolean, Course>> joinedCoursesInfo = item.getJoinedCoursesInfo();

		final Map<String, Object> undoOptions = new HashMap<String, Object>();
		undoOptions.put(Constants.ExtraUndoOptions.ADD_TO_MANAGER, false);
		undoOptions.put(Constants.ExtraUndoOptions.SPLIT_UNDO, false);
		undoOptions.put(Constants.ExtraUndoOptions.JOIN_UNDO, true);
		undoOptions.put(Constants.ExtraUndoOptions.APPEND_UNDO, false);
		boolean test = true;
		for (long id : item.getCoursesIds()) {
			courses.add(id);
		}
		if (undoMode.equals(this.undoMode) && test) {
			Course joinedCourse = getCourse(masterDocument, courses.get(courses.size() - 1));
			courses.remove(courses.size() - 1);
			undoAppendJoin(joinedCourse, joinedCoursesInfo);
			/*
			 * if (!routeIds.isEmpty() && !startTimes.isEmpty()) { for (Long id
			 * : routeIds) { Course route = getCourse(masterDocument, id);
			 * masterDocument.remove(route); } for (Long id :
			 * startTimes.keySet()) { Course course = getCourse(masterDocument,
			 * id);
			 * 
			 * GPSDocument document = new
			 * GPSDocument(course.getParent().getFileName());
			 * document.add(course); Date startTime = startTimes.get(id)[0];
			 * Date endTime = startTimes.get(id)[1];
			 * course.getFirstTrackpoint().setTimestamp(startTime);
			 * course.getLastTrackpoint().setTimestamp(endTime);
			 * 
			 * Map<String, Object> options = new HashMap<String, Object>();
			 * options.put(Constants.ConsolidationOperation.LEVEL,
			 * ConsolidationLevel.SUMMARY); new
			 * ConsolidationOperation(options).process(document);
			 * options.put(Constants.ConsolidationOperation.LEVEL,
			 * ConsolidationLevel.RECALCULATION); new
			 * ConsolidationOperation(options).process(document);
			 * 
			 * } }
			 */
		}
		if (undoMode.equals(this.undoMode) && !test) {
			// courses.remove(courses.size() - 1);
			// Course joinedCourse = getCourse(masterDocument,
			// item.getCoursesIds().get(item.getCoursesIds().size() - 1));
			Course joinedCourse = getCourse(masterDocument, courses.get(courses.size() - 1));
			courses.remove(courses.size() - 1);
			for (Long course : courses) {
				Trackpoint trackpoint = getTrackpoint(joinedCourse, item.getConnectingPointsIds().get(course));
				undoOptions.put(Constants.ExtraUndoOptions.EXTRA_ID, course);

				splitAtSelected(joinedCourse, trackpoint, true, undoOptions);

				// undoJoin(joinedCourse, trackpoint, course, merge);
			}
			/*
			 * if (!routeIds.isEmpty() && !startTimes.isEmpty()) { for (Long id
			 * : routeIds) { Course route = getCourse(masterDocument, id);
			 * masterDocument.remove(route); } for (Long id :
			 * startTimes.keySet()) { Course course = getCourse(masterDocument,
			 * id);
			 * 
			 * GPSDocument document = new
			 * GPSDocument(course.getParent().getFileName());
			 * document.add(course); Date startTime = startTimes.get(id)[0];
			 * Date endTime = startTimes.get(id)[1];
			 * course.getFirstTrackpoint().setTimestamp(startTime);
			 * course.getLastTrackpoint().setTimestamp(endTime);
			 * 
			 * Map<String, Object> options = new HashMap<String, Object>();
			 * options.put(Constants.ConsolidationOperation.LEVEL,
			 * ConsolidationLevel.SUMMARY); new
			 * ConsolidationOperation(options).process(document);
			 * options.put(Constants.ConsolidationOperation.LEVEL,
			 * ConsolidationLevel.RECALCULATION); new
			 * ConsolidationOperation(options).process(document);
			 * 
			 * } }
			 */
		}
		if (undoMode.equals(this.redoMode)) {
			List<Course> coursesToJoin = new ArrayList<Course>();
			for (Long course : courses) {
				coursesToJoin.add(getCourse(masterDocument, course));
			}
			Collections.reverse(coursesToJoin);
			join(coursesToJoin, null, null, undoOptions);
		}
	}

	public void undoNormalJoin(final Course course, final List<Pair<Boolean, Course>> joinedCoursesInfo) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(joinedCoursesInfo);
		final GPSDocument masterDocument = course.getParent();

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.splitingCourse");
			}

			@Override
			public Object execute() throws TrackItException {
				return splitCourses(course);
			}

			private List<Course> splitCourses(Course course) {
				List<Trackpoint> indexSearcher = new ArrayList<Trackpoint>();
				for (int i = 0; i < joinedCoursesInfo.size(); i++) {
					indexSearcher.addAll(joinedCoursesInfo.get(i).getSecond().getTrackpoints());
				}
				Collections.reverse(joinedCoursesInfo);
				GPSDocument document = new GPSDocument(course.getParent().getFileName());
				document.add(course);
				for (int i = 0; i < joinedCoursesInfo.size() - 1; i++) {
					Trackpoint splitPoint = joinedCoursesInfo.get(i).getSecond().getFirstTrackpoint();
					int currentIndex = indexSearcher.indexOf(splitPoint);
					splitPoint = course.getTrackpoints().get(currentIndex);
					Map<String, Object> splitOptions = new HashMap<String, Object>();
					splitOptions.put(Constants.SplitAtSelectedOperation.COURSE, course);
					splitOptions.put(Constants.SplitAtSelectedOperation.TRACKPOINT, splitPoint);
					TrackSplittingOperation splittingOperation = new TrackSplittingOperation(splitOptions);

					try {
						splittingOperation.undoOperation(document);
					} catch (TrackItException e) {
						logger.error(e.getMessage());
						return null;
					}
					indexSearcher.removeAll(indexSearcher.subList(currentIndex, indexSearcher.size() - 1));
					Course oldCourse = joinedCoursesInfo.get(i).getSecond();
					boolean original = joinedCoursesInfo.get(i).getFirst();
					Course newCourse = document.getCourses().get(i + 1);
					if (original) {
						copyData(newCourse, oldCourse);
					}
				}
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}

				Course first = document.getCourses().get(0);
				document.remove(first);
				document.add(course);
				Collections.reverse(document.getCourses());
				Collections.reverse(joinedCoursesInfo);
				Course oldCourse = joinedCoursesInfo.get(0).getSecond();
				Course newCourse = document.getCourses().get(0);
				copyData(newCourse, oldCourse);

				for (Course course2 : document.getCourses()) {
					consolidate(course2);
				}

				return document.getCourses();

			}

			@SuppressWarnings("unchecked")
			@Override
			public void done(Object result) {
				List<Course> courses = (List<Course>) result;
				List<Course> newCourses = new ArrayList<Course>();
				for (int i = 0; i < courses.size(); i++) {
					boolean original = joinedCoursesInfo.get(i).getFirst();
					if (original) {
						newCourses.add(courses.get(i));
					}
				}

				for (Course course : newCourses) {
					course.setParent(masterDocument);
//					course.setUnsavedTrue();			// 12335 : 2016-10-03
					course.setTrackStatusTo( true);
				}

				masterDocument.remove(course);
				masterDocument.addCourses(newCourses);
				masterDocument.publishUpdateEvent(null);
				courses.get(0).publishSelectionEvent(null);
				TrackIt.getApplicationPanel().forceRefresh();

			}
		}).execute();
	}

	/*
	 * public void undoJoin(final Course course, final Trackpoint trackpoint,
	 * final long newId, final List<Long> ids) { Objects.requireNonNull(course);
	 * Objects.requireNonNull(trackpoint); final GPSDocument masterDocument =
	 * course.getParent(); new Task(new Action() {
	 * 
	 * @Override public String getMessage() { return
	 * Messages.getMessage("documentManager.message.splitingCourse"); }
	 * 
	 * @Override public Object execute() throws TrackItException { return
	 * splitCourse(course, trackpoint); }
	 * 
	 * private List<Course> splitCourse(Course course, Trackpoint trackpoint) {
	 * Map<String, Object> options = new HashMap<String, Object>();
	 * options.put(Constants.SplitAtSelectedOperation.COURSE, course);
	 * options.put(Constants.SplitAtSelectedOperation.TRACKPOINT, trackpoint);
	 * 
	 * TrackSplittingOperation splittingOperation = new
	 * TrackSplittingOperation(options); GPSDocument document = new
	 * GPSDocument(course.getParent().getFileName()); document.add(course);
	 * 
	 * try { splittingOperation.process(document); } catch (TrackItException e)
	 * { logger.error(e.getMessage()); return null; } return
	 * document.getCourses(); }
	 * 
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Override public void done(Object result) { List<Course> courses =
	 * (List<Course>) result;
	 * 
	 * for (Course course : courses) { course.setParent(masterDocument);
	 * course.setUnsavedTrue(); } courses.get(1).setId(newId);
	 * 
	 * List<Long> allPointIds = new ArrayList<Long>();
	 * 
	 * 
	 * for (Course course : courses) { if
	 * (!ids.contains(course.getLastTrackpoint().getId())) {
	 * course.getTrackpoints().remove(course.getTrackpoints().size() - 1);
	 * course.getLaps().remove(course.getLastLap()); } }
	 * 
	 * 
	 * masterDocument.remove(course); masterDocument.addCourses(courses);
	 * 
	 * masterDocument.publishUpdateEvent(null);
	 * courses.get(0).publishSelectionEvent(null);
	 * 
	 * } }).execute(); }
	 */

}
