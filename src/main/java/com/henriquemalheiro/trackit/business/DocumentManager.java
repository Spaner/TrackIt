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
package com.henriquemalheiro.trackit.business;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CourseLap;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Folder;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.exception.ReaderException;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.ActivityToCourseOperation;
import com.henriquemalheiro.trackit.business.operation.AddLapOperation;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.operation.ConsolidationOperation;
import com.henriquemalheiro.trackit.business.operation.CopyOperation;
import com.henriquemalheiro.trackit.business.operation.DetectClimbsDescentsOperation;
import com.henriquemalheiro.trackit.business.operation.JoiningOperation;
import com.henriquemalheiro.trackit.business.operation.MarkingOperation;
import com.henriquemalheiro.trackit.business.operation.RemoveLapOperation;
import com.henriquemalheiro.trackit.business.operation.RemovePausesOperation;
import com.henriquemalheiro.trackit.business.operation.ReverseOperation;
import com.henriquemalheiro.trackit.business.operation.SettingPaceOperation;
import com.henriquemalheiro.trackit.business.operation.TrackSimplificationOperation;
import com.henriquemalheiro.trackit.business.operation.TrackSplittingOperation;
import com.henriquemalheiro.trackit.business.operation.UndoItem;
import com.henriquemalheiro.trackit.business.operation.UndoManagerCustom;
import com.henriquemalheiro.trackit.business.operation.UndoableActionType;
import com.henriquemalheiro.trackit.business.reader.Reader;
import com.henriquemalheiro.trackit.business.reader.ReaderFactory;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterFactory;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventListener;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;
import com.pg58406.trackit.business.db.Database;

public class DocumentManager implements EventPublisher, EventListener {
	public static final String FOLDER_WORKSPACE = "Workspace";
	public static final String FOLDER_LIBRARY = "Library";

	private static DocumentManager documentManager;
	private List<Folder> folders;
	private Map<Long, DocumentEntry> documents;
	private DocumentItem selectedItem;
	private Long selectedDocumentId;
	private Folder selectedFolder;
	private Database database;
	
	private UndoManagerCustom undoManager; //57421

	private enum ReadMode {
		OPEN, IMPORT
	};

	private static Logger logger = Logger.getLogger(DocumentManager.class
			.getName());

	static {
		documentManager = new DocumentManager();
		EventManager.getInstance().register(documentManager);
	}

	private DocumentManager() {
		init();
	}

	public synchronized static DocumentManager getInstance() {
		return documentManager;
	}

	private void init() {
		folders = new ArrayList<Folder>();
		folders.add(new Folder(FOLDER_WORKSPACE));
		folders.add(new Folder(FOLDER_LIBRARY));
		selectedFolder = getFolder(FOLDER_WORKSPACE);

		documents = new HashMap<Long, DocumentManager.DocumentEntry>();

		database = Database.getInstance();
		
		undoManager = new UndoManagerCustom(); //57421
	}
	
	public UndoManagerCustom getUndoManager(){
		return undoManager;
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
		return createDocument(folderName,
				Messages.getMessage("documentManager.untitledDocument"));
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

	public void importDocuments(File[] files) {
		read(files, ReadMode.IMPORT);
	}

	public void openDocument(File file) {
		read(new File[] { file }, ReadMode.OPEN);
	}

	private void read(final File[] files, final ReadMode mode) {
		SwingWorker<GPSDocument, Void> worker = new SwingWorker<GPSDocument, Void>() {
			@Override
			protected GPSDocument doInBackground() {
				InputStream inputStream = null;
				Reader reader;
				GPSDocument selectedDocument = (mode.equals(ReadMode.IMPORT) ? documents
						.get(selectedDocumentId).getDocument()
						: new GPSDocument(
								Messages.getMessage("gpsDocument.label")));
				GPSDocument document = null;

				for (File file : files) {
					if (file.exists()) {
						try {
							inputStream = getInputStream(file);
							reader = ReaderFactory.getInstance().getReader(
									file, null);
							document = reader.read(inputStream,
									file.getAbsolutePath());// 58406
							mergeDocuments(document, selectedDocument);
						} catch (ReaderException re) {
							return null;
						} finally {
							closeInputStream(inputStream);
						}
					} else {
						logger.error("The file " + file.getAbsolutePath()
								+ " does not exist. Skipping file.");
						continue;
					}
					for (Activity a : document.getActivities()) {
						if (mode != ReadMode.IMPORT)
							database.updateDB(a, file);
						a.setFilepath(file.getAbsolutePath());
						if(a.getTrackpoints().get(0).getSpeed() != null)
							a.setNoSpeedInFile(false);
					}
					for (Course c : document.getCourses()) {
						if (mode != ReadMode.IMPORT)
							database.updateDB(c, file);
						c.setFilepath(file.getAbsolutePath());						
						if(c.getTrackpoints().get(0).getSpeed() != null)
							c.setNoSpeedInFile(false);
						c.setUnsavedFalse();
					}
				}

				return selectedDocument;
			}

			@Override
			protected void done() {
				try {
					GPSDocument document = get();
					if (document == null) {
						return;
					}

					Map<String, Object> options = new HashMap<>();
					options.put(Constants.ConsolidationOperation.LEVEL,
							ConsolidationLevel.BASIC);
					new ConsolidationOperation(options).process(document);

					if (mode.equals(ReadMode.IMPORT)) {
						EventManager.getInstance().publish(
								DocumentManager.this, Event.DOCUMENT_UPDATED,
								document);
					} else {
						addDocument(getFolder(FOLDER_WORKSPACE), document);
					}

					selectDocumentsFirstItem(document);

				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				} catch (ExecutionException e) {
					logger.error(e.getMessage());
				} catch (TrackItException e) {
					showAndLogErrorMessage(e.getMessage());
				}
				MapView map = TrackIt.getApplicationPanel().getMapView();
				map.zoomToFitFeature(selectedItem);
			}

			private void selectDocumentsFirstItem(GPSDocument document) {
				if (!document.getActivities().isEmpty()) {
					document.getActivities().get(0)
							.publishSelectionEvent(DocumentManager.this);
				} else if (!document.getCourses().isEmpty()) {
					document.getCourses().get(0)
							.publishSelectionEvent(DocumentManager.this);
				} else if (!document.getWaypoints().isEmpty()) {
					document.getWaypoints().get(0)
							.publishSelectionEvent(DocumentManager.this);
				}
			}
		};
		worker.execute();
	}

	private InputStream getInputStream(final File file) throws ReaderException {
		ProgressMonitorInputStream progressMonitor;
		BufferedInputStream in;
		try {
			progressMonitor = new ProgressMonitorInputStream(
					TrackIt.getApplicationFrame(), "Reading "
							+ file.getAbsolutePath(), new FileInputStream(file));
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
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
						message,
						Messages.getMessage("documentManager.title.error"),
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	public void delete(Activity activity) {
		for (Folder currentFolder : folders) {
			for (GPSDocument document : currentFolder.getDocuments()) {
				if (document.getActivities().contains(activity)) {
					document.remove(activity);
					EventManager.getInstance().publish(this,
							Event.ACTIVITY_REMOVED, activity);
					return;
				}
			}
		}
	}

	public void delete(Course course) {
		for (Folder currentFolder : folders) {
			for (GPSDocument document : currentFolder.getDocuments()) {
				if (document.getCourses().contains(course)) {
					document.remove(course);
					EventManager.getInstance().publish(this,
							Event.COURSE_REMOVED, course);
					return;
				}
			}
		}
	}

	public void discard(GPSDocument document) {
		Folder folder = getFolder(document);
		folder.remove(document);
		documents.remove(document.getId());// 58406

		EventManager.getInstance().publish(this, Event.DOCUMENT_DISCARDED,
				document);
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

	private void addDocument(Folder folder, GPSDocument document) {
		folder.add(document);
		DocumentEntry entry = new DocumentEntry(folder, document);
		documents.put(document.getId(), entry);

		EventManager.getInstance().publish(DocumentManager.this,
				Event.DOCUMENT_ADDED, document);
	}

	public Activity consolidate(final Activity activity,
			final ConsolidationLevel level) throws TrackItException {
		GPSDocument document = new GPSDocument(activity.getParent()
				.getFileName());
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
		document.add(course);

		EventManager.getInstance().publish(this, Event.DOCUMENT_UPDATED,
				document);
		EventManager.getInstance().publish(DocumentManager.this,
				Event.COURSE_SELECTED, course);
	}

	public void splitAtSelected(final Course course, final Trackpoint trackpoint) {
		Objects.requireNonNull(course);
		Objects.requireNonNull(trackpoint);

		final GPSDocument masterDocument = course.getParent();

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages
						.getMessage("documentManager.message.splitingCourse");
			}

			@Override
			public Object execute() throws TrackItException {
				return splitCourse(course, trackpoint);
			}

			private List<Course> splitCourse(Course course,
					Trackpoint trackpoint) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.SplitAtSelectedOperation.COURSE, course);
				options.put(Constants.SplitAtSelectedOperation.TRACKPOINT,
						trackpoint);

				TrackSplittingOperation splittingOperation = new TrackSplittingOperation(
						options);
				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
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
				for (Course course : courses) {
					course.setParent(masterDocument);
					course.setUnsavedTrue();
				}

				masterDocument.remove(course);
				masterDocument.addCourses(courses);

				masterDocument.publishUpdateEvent(null);
				courses.get(0).publishSelectionEvent(null);
			}
		}).execute();
	}

	public void reverse(final Course course, final boolean wayback) {
		
		Objects.requireNonNull(course);
		final GPSDocument masterDocument = course.getParent();


		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.reverse");
			}

			@Override
			public Object execute() throws TrackItException {
				if(wayback){
					return reverseBack(course);
				}
				return reverse(course);
			}

			private Course reverse(Course course) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(
						com.henriquemalheiro.trackit.business.common.Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.SUMMARY);

				ReverseOperation reverseOperation = new ReverseOperation(
						options);
				ConsolidationOperation consolidationOP = new ConsolidationOperation(
						options);

				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				
				document.add(course);

				try {
					reverseOperation.process(document, wayback);
					consolidationOP.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}

				return document.getCourses().get(0);
			}
			
			private List<Course> reverseBack(Course course) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.CopyOperation.COURSE, course);

				CopyOperation copyOperation = new CopyOperation(
						options);
				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(course);
			

				try {
					copyOperation.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}
				options.put(
						com.henriquemalheiro.trackit.business.common.Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.SUMMARY);
				ReverseOperation reverseOperation = new ReverseOperation(
						options);
				ConsolidationOperation consolidationOP = new ConsolidationOperation(
						options);

				try {
					reverseOperation.process(document, wayback);
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
				if(wayback){
					List<Course> courses = (List<Course>) result;
					for (Course course : courses) {
						course.setParent(masterDocument);
						course.setUnsavedTrue();
					}

					masterDocument.remove(course);
					masterDocument.addCourses(courses);

					masterDocument.publishUpdateEvent(null);
					courses.get(0).publishSelectionEvent(null);
				}
				else{
					Course course = (Course) result;
					course.setParent(masterDocument);
					masterDocument.publishUpdateEvent(null);
					course.setUnsavedTrue();
					course.publishSelectionEvent(null);
				}
			}
			
			
		}).execute();
		
		String name = "REVERSE";
		List<Long> courseId = new ArrayList<Long>();
		long documentId = masterDocument.getId();
		
		courseId.add(course.getId());
		UndoItem item = new UndoItem(name, documentId, courseId, null);
		undoManager.pushUndo(item);
	}
	
	public void copy(final Course course) {
		
		Objects.requireNonNull(course);
		final GPSDocument masterDocument = course.getParent();

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.copy");
			}

			@Override
			public Object execute() throws TrackItException {
				return copy(course);
			}

			private List<Course> copy(Course course) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.CopyOperation.COURSE, course);

				CopyOperation copyOperation = new CopyOperation(
						options);
				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(course);

				try {
					copyOperation.process(document);
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
					course.setUnsavedTrue();
				}

				masterDocument.remove(course);
				masterDocument.addCourses(courses);

				masterDocument.publishUpdateEvent(null);
				courses.get(0).publishSelectionEvent(null);
			}
			
			
		}).execute();
		
		String name = "COPY";
		List<Long> courseId = new ArrayList<Long>();
		long documentId = masterDocument.getId();
		
		courseId.add(course.getId());
		UndoItem item = new UndoItem(name, documentId, courseId, null);
		undoManager.pushUndo(item);
		
		/*Objects.requireNonNull(course);
		final GPSDocument masterDocument = course.getParent();
		
		SwingWorker<GPSDocument, Void> worker = new SwingWorker<GPSDocument, Void>() {
			@Override
			protected GPSDocument doInBackground() {
		
				String filename = course.getFilepath();
				String newName = FilenameUtils.removeExtension(filename);
				String extension = FilenameUtils.getExtension(filename);
				String newFilename = newName + "_copy" + "." + extension;
				
				File file = new File(newFilename);
			
				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(course);
				document.setFileName(newFilename);
				
				//course.setName(course.getName()+ "_copy");
				boolean exists = file.exists();

				if (!exists){
					try {
					Map<String, Object> options = new HashMap<>();
					options.put(Constants.Writer.OUTPUT_DIR, file
							.getParentFile().getAbsolutePath());
					Writer writer = WriterFactory.getInstance().getWriter(
							file, options);
					writer.write(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					JOptionPane.showMessageDialog(
							TrackIt.getApplicationFrame(),
							getMessage("operation.failure.fileExport",
									course.getDocumentItemName()),
							getMessage("operation.failure"),
							JOptionPane.ERROR_MESSAGE);
					return null;
				}
				}
				else {
					logger.error("The file " + file.getAbsolutePath()
							+ " already exists.");
				}
				course.setUnsavedFalse();
				//course.setName(course.getName().substring(0, course.getName().length()-5));
				File[] files = new File[1];
				files[0] = file;
				importDocuments(files);			
				file.delete();

				return document;
			}

			@Override
			protected void done() {
				try{
					GPSDocument document = get();
					if (document == null) {
						return;
					}
					for (Course course : document.getCourses()) {
						course.setParent(masterDocument);
						course.setUnsavedTrue();
					}
					masterDocument.remove(course);
					masterDocument.addCourses(document.getCourses());
					masterDocument.publishUpdateEvent(null);
					document.getCourses().get(0).publishSelectionEvent(null);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				} catch (ExecutionException e) {
					logger.error(e.getMessage());
				} 

		}
		};
		worker.execute();*/
		

		
	}
	
	
	
	
	public Course getCourse(GPSDocument document, long courseId){
		ListIterator<Course> iter = document.getCourses().listIterator();
		Course iterCourse;
		while (iter.hasNext()) {
			iterCourse = iter.next();
			if(iterCourse.getId() == courseId){
				return iterCourse;
			}
			
		}
		return null;
	}
	
	public void undo() {
		final UndoItem item = undoManager.popUndo();
		Objects.requireNonNull(item);
		
		DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument masterDocument = entry.getDocument();
		final Course course = getCourse(masterDocument, item.getFirstCourseId());
		
		//final Trackpoint trackpoint = item.getTrackpoint();
		
		String actionCommand = item.getOperationType();
		final UndoableActionType undoableAction = UndoableActionType.lookup(actionCommand);

		

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.undo");
			}

			@Override
			public Object execute() throws TrackItException {

				
				switch (undoableAction) {
					case ADD_TRACKPOINT:
						break;
					case REMOVE_TRACKPOINT:
						break;
					case JOIN:
						break;
					case SPLIT:
						break;
					case REVERSE:
						return undoReverse(course);
					case ADD_PAUSE:
						break;
					case REMOVE_PAUSE:
						break;
					case SET_PACE:
						break;
					case COPY:
						break;
					default:
						// Do nothing
						}		
				return null;
			}


			private Course undoReverse(Course course) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(
						com.henriquemalheiro.trackit.business.common.Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.SUMMARY);

				ReverseOperation reverseOperation = new ReverseOperation(
						options);
				ConsolidationOperation consolidationOP = new ConsolidationOperation(
						options);

				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(course);

				try {
					reverseOperation.undoOperation(document);
					consolidationOP.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}

				return document.getCourses().get(0);
			}
			

			@Override
			public void done(Object result) {
				Course course = (Course) result;
				course.setParent(masterDocument);
				masterDocument.publishUpdateEvent(null);
				course.setUnsavedTrue();
				course.publishSelectionEvent(null);
			}
		}).execute();
	}
	
	public void redo() {
		final UndoItem item = undoManager.popRedo();
		Objects.requireNonNull(item);
		
		DocumentEntry entry = documents.get(item.getDocumentId());
		final GPSDocument masterDocument = entry.getDocument();
		final Course course = getCourse(masterDocument, item.getFirstCourseId());
		
		//final Trackpoint trackpoint = item.getTrackpoint();
		
		String actionCommand = item.getOperationType();
		final UndoableActionType undoableAction = UndoableActionType.lookup(actionCommand);	

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage("documentManager.message.redo");
			}

			@Override
			public Object execute() throws TrackItException {

				switch (undoableAction) {
				case ADD_TRACKPOINT:
					break;
				case REMOVE_TRACKPOINT:
					break;
				case JOIN:
					break;
				case SPLIT:
					break;
				case REVERSE:
					return redoReverse(course);
				case ADD_PAUSE:
					break;
				case REMOVE_PAUSE:
					break;
				case SET_PACE:
					break;
				default:
					// Do nothing
					}		
			return null;
			}

			private Course redoReverse(Course course) {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(
						com.henriquemalheiro.trackit.business.common.Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.SUMMARY);

				ReverseOperation reverseOperation = new ReverseOperation(
						options);
				ConsolidationOperation consolidationOP = new ConsolidationOperation(
						options);

				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(course);

				try {
					reverseOperation.undoOperation(document);
					consolidationOP.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}

				return document.getCourses().get(0);
			}

			@Override
			public void done(Object result) {
				Course course = (Course) result;
				course.setParent(masterDocument);
				masterDocument.publishUpdateEvent(null);
				course.setUnsavedTrue();
				course.publishSelectionEvent(null);
			}
		}).execute();
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
				AddLapOperation addLapOperation = new AddLapOperation(course,
						trackpoint);
				addLapOperation.execute();

				return null;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
				course.setUnsavedTrue();
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
				RemoveLapOperation removeLapOperation = new RemoveLapOperation(
						lap);
				removeLapOperation.execute();

				return null;
			}

			@Override
			public void done(Object result) {
				lap.getParent().publishUpdateEvent(null);
			}
		}).execute();
	}

	public void activityToCourse(final Activity activity,
			final boolean removeActivity) {
		final GPSDocument originalDocument = activity.getParent();

		SwingWorker<Runnable, Void> worker = new SwingWorker<Runnable, Void>() {
			@Override
			protected Runnable doInBackground() {
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(
						Constants.ActivitiesToCoursesOperation.REMOVE_ACTIVITIES,
						removeActivity);
				ActivityToCourseOperation Operation = new ActivityToCourseOperation(
						options);
				GPSDocument document = new GPSDocument(activity.getParent()
						.getFileName());
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

	public void renameCourse(Course course, String newName) {
		String prevName = course.getName();
		database.renameCourse(prevName, newName);
		course.setName(newName);
		EventManager.getInstance().publish(this, Event.COURSE_UPDATED, course);
	}

	public Course setPace(Course course, Map<String, Object> options)
			throws TrackItException {
		GPSDocument document = new GPSDocument(course.getParent().getFileName());
		document.add(course);

		new SettingPaceOperation(options).process(document);

		options.put(Constants.ConsolidationOperation.LEVEL,
				ConsolidationLevel.SUMMARY);
		new ConsolidationOperation(options).process(document);
		course.setUnsavedTrue();
		return document.getCourses().get(0);
	}

	public void consolidate(final Course course,
			final Map<String, Object> options) {
		final GPSDocument masterDocument = course.getParent();

		new Task(new Action() {
			@Override
			public String getMessage() {
				return Messages.getMessage(
						"documentManager.message.consolidating",
						course.getDocumentItemName());
			}

			@Override
			public Object execute() {
				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
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
					JOptionPane.showMessageDialog(
							TrackIt.getApplicationFrame(),
							Messages.getMessage("documentManager.error.consolidationError"),
							Messages.getMessage("documentManager.title.error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				Course course = (Course) result;
				course.setParent(masterDocument);
				course.publishUpdateEvent(null);
			}
		}, false).execute();
	}

	public DocumentItem simplify(DocumentItem item, Map<String, Object> options)
			throws TrackItException {
		GPSDocument document = new GPSDocument(null);

		if (!item.isCourse()) {
			throw new IllegalStateException("Only courses can be simplified!");
		} else {
			document.add((Course) item);
			document.setFileName(((Course) item).getParent().getFileName());
		}

		new TrackSimplificationOperation(options).process(document);

		options.put(Constants.ConsolidationOperation.LEVEL,
				ConsolidationLevel.SUMMARY);
		new ConsolidationOperation(options).process(document);

		return document.getCourses().get(0);
	}

	public DocumentItem mark(DocumentItem item, Map<String, Object> options)
			throws TrackItException {
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

	public DocumentItem detectClimbsDescents(DocumentItem item,
			Map<String, Object> options) throws TrackItException {
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

	public void join(final List<Course> courses) {
		if (courses == null || courses.size() < 2) {
			throw new IllegalArgumentException(
					"Join only applies to two or more courses!");
		}

		final GPSDocument masterDocument = courses.get(0).getParent();

		new Task(new Action() {
			@Override
			public String getMessage() {
				return Messages
						.getMessage("documentManager.message.joiningCourses");
			}

			@Override
			public Object execute() throws TrackItException {
				return joinCourses(courses);
			}

			private Course joinCourses(final List<Course> courses)
					throws TrackItException {
				Map<String, Object> options = new HashMap<>();
				options.put(Constants.JoinOperation.ADD_LAP_MARKER, true);
				JoiningOperation operation = new JoiningOperation(options);
				GPSDocument document = new GPSDocument(courses.get(0)
						.getParent().getFileName());
				document.addCourses(courses);

				try {
					operation.process(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					return null;
				}

				if (document.getCourses().size() != 1) {
					throw new TrackItException(
							"Join operation resulted in more than one course!");
				}

				return document.getCourses().get(0);
			}

			@Override
			public void done(Object result) {
				Course jointCourse = (Course) result;
				for (Course course : courses) {
					masterDocument.remove(course);
				}
				jointCourse.setParent(masterDocument);
				masterDocument.add(jointCourse);

				masterDocument.publishUpdateEvent(null);
				jointCourse.publishSelectionEvent(null);
				EventManager.getInstance().publish(null, Event.ZOOM_TO_ITEM,
						jointCourse);
			}
		}).execute();
	}


	
	public void addTrackpoint(final Course course, final Trackpoint trackpoint) {
		addTrackpoint(course, trackpoint, course.getTrackpoints().size());
	}
	
	

	public void addTrackpoint(final Course course, final Trackpoint trackpoint,
			final int index) {
		final GPSDocument masterDocument = course.getParent();
		final String courseName = course.getName();// 58406
		final String filepath = course.getFilepath();

		SwingWorker<Course, Course> worker = new SwingWorker<Course, Course>() {
			@Override
			protected Course doInBackground() {
				trackpoint.setParent(course);
				course.getTrackpoints().add(index, trackpoint);

				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(course);
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.SUMMARY);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}

				publish(document.getCourses().get(0));

				options.put(Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.RECALCULATION);
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
					Course course = get();
					if (course != null) {
						course.setParent(masterDocument);
						course.publishUpdateEvent(null);
					}
				} catch (InterruptedException | ExecutionException ignore) {
				}
				course.setName(courseName);
				course.setFilepath(filepath);
				
				/*String name = "ADD_TRACKPOINT";
				List<Course> courseList = new ArrayList<Course>();
				
				courseList.add(course);
				UndoItem item = new UndoItem(name, courseList, trackpoint);
				addUndoItem(item);*/
			}
		};
		worker.execute();
		
		
		
	}

	public void removeTrackpoint(final Course course,
			final Trackpoint trackpoint) {
		final GPSDocument masterDocument = course.getParent();
		
		
		SwingWorker<Course, Course> worker = new SwingWorker<Course, Course>() {
			@Override
			protected Course doInBackground() {
				boolean keepTimes = TrackIt
						.getPreferences()
						.getBooleanPreference(
								Constants.PrefsCategories.EDITION,
								null,
								Constants.EditionPreferences.KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL,
								true);
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
						Double distanceFromPrevious = trackpoint
								.getDistanceFromPrevious()
								+ nextTrakp.getDistanceFromPrevious();
						nextTrakp.setDistanceFromPrevious(distanceFromPrevious);
						Double timeFromPrevious = trackpoint
								.getTimeFromPrevious()
								+ nextTrakp.getTimeFromPrevious();
						nextTrakp.setTimeFromPrevious(timeFromPrevious);
						Trackpoint temp = trackpoints.get(n - 2);
						Double speed = (nextTrakp.getDistance() - temp
								.getDistance())
								/ (nextTrakp.getTimeFromPrevious()
										+ trackpoint.getTimeFromPrevious() + prevTrakp
											.getTimeFromPrevious());
						prevTrakp.setSpeed(speed);
						temp = trackpoints.get(n + 2);
						speed = (temp.getDistance() - prevTrakp.getDistance())
								/ (temp.getTimeFromPrevious()
										+ nextTrakp.getTimeFromPrevious() + trackpoint
											.getTimeFromPrevious());
						nextTrakp.setSpeed(speed);
					}
				}
				course.remove(trackpoint);// 58406

				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(course);
				Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.SUMMARY);
				try {
					new ConsolidationOperation(options).process(document);
				} catch (TrackItException e) {
					return null;
				}

				publish(course);

				options.put(Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.RECALCULATION);
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
					Course course = get();
					if (course != null) {
						course.setParent(masterDocument);
						course.publishUpdateEvent(null);
						EventManager.getInstance().publish(null,
								Event.TRACKPOINT_HIGHLIGHTED, null);
					}
				} catch (InterruptedException | ExecutionException ignore) {
				}

			}
		};
		worker.execute();
		
		/*String name = "REMOVE_TRACKPOINT";
		List<Course> courseList = new ArrayList<Course>();
		
		courseList.add(course);
		UndoItem item = new UndoItem(name, courseList, trackpoint);
		addUndoItem(item);*/
	}

	public DocumentItem clearSegments(DocumentItem item) {
		item.getSegments().clear();
		return item;
	}

	public void removePauses(final Course course) {
		Objects.requireNonNull(course);

		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages
						.getMessage("documentManager.message.removePauses");
			}

			@Override
			public Object execute() throws TrackItException {
				RemovePausesOperation removePausesOperation = new RemovePausesOperation(
						course);
				removePausesOperation.execute();
				return null;
			}

			@Override
			public void done(Object result) {
				course.publishUpdateEvent(null);
			}
		}).execute();
	}

	public void appendRoute(final MapProvider mapProvider,
			final Map<String, Object> routingOptions, final Course course,
			final Location location) throws TrackItException {
		Objects.requireNonNull(mapProvider);
		Objects.requireNonNull(routingOptions);
		Objects.requireNonNull(course);
		Objects.requireNonNull(location);

		if (!mapProvider.hasRoutingSupport()) {
			throw new TrackItException(String.format(
					"%s provider does not have routing support!",
					mapProvider.getName()));
		}

		final GPSDocument masterDocument = course.getParent();
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages
						.getMessage("documentManager.message.appendRoute");
			}

			@Override
			public Object execute() throws TrackItException {
				Trackpoint startTrackpoint = course.getLastTrackpoint();
				Location startLocation = new Location(startTrackpoint
						.getLongitude(), startTrackpoint.getLatitude());
				Location endLocation = location;
				Course route = mapProvider.getRoute(startLocation, endLocation,
						routingOptions);
				route.getTrackpoints().remove(0);

				Map<String, Object> options = new HashMap<>();
				options.put(Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.RECALCULATION);
				GPSDocument document = new GPSDocument(course.getParent()
						.getFileName());
				document.add(route);
				new ConsolidationOperation(options).process(document);
				route = document.getCourses().get(0);
				route.setParent(masterDocument);

				GPSDocument document2 = new GPSDocument(course.getParent()
						.getFileName());
				document2.addCourses(Arrays.asList(course, route));
				options.put(Constants.JoinOperation.ADD_LAP_MARKER, false);
				new JoiningOperation(options).process(document2);
				Course newCourse = document2.getCourses().get(0);
				newCourse.setParent(masterDocument);

				masterDocument.remove(course);
				masterDocument.add(newCourse);

				return newCourse;
			}

			@Override
			public void done(Object result) {
				Course newCourse = (Course) result;
				newCourse.setName(course.getName());
				newCourse.setFilepath(course.getFilepath());
				masterDocument.publishUpdateEvent(null);
				newCourse.publishSelectionEvent(null);
			}
		}).execute();
	}

	/* Event Listener interface implementation */

	@Override
	public void process(Event event, DocumentItem item) {
		if (item instanceof Folder) {
			selectedFolder = (Folder) item;
			selectedItem = null;
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
	public void process(Event event, DocumentItem parent,
			List<? extends DocumentItem> items) {
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
			result = prime * result
					+ (int) (document.getId() ^ (document.getId() >>> 32));
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

	public void initFromDB() {
		List<String> filepathList = database.initFromDb();
		for (int i = 0; i < filepathList.size(); i++) {
			try {
				File f = new File(filepathList.get(i));
				if (f.exists()) {
					readAfterOpen(f);
				} else {
					database.delete(f.getAbsolutePath());
					logger.error("The file "
							+ f.getAbsolutePath()
							+ " does not exist. Skipping file.");
					continue;
				}
			} catch (Exception e1) {
				logger.error(e1.getClass().getName() + ": "
						+ e1.getMessage());
				logger.error("Something went wrong while reading file "
						+ filepathList.get(i) + ".");
				e1.printStackTrace();
				try {
					readAfterOpen(new File(filepathList.get(i)));
				} catch (Exception e2) {
					logger.error(e2.getClass().getName() + ": "
							+ e2.getMessage());
					logger.error("Something went wrong AGAIN while reading file "
									+ filepathList.get(i) + ".");
					e2.printStackTrace();
				}
				continue;
			}
		}
		for (Folder folder : folders) {
			for (GPSDocument doc : folder.getDocuments()) {
				doc.setChangedFalse();
				if (doc.getActivities().size() + doc.getCourses().size() == 0){
					documents.remove(doc.getId());
				}
			}
		}
	}

	private void readAfterOpen(File file) throws ReaderException {
		InputStream inputStream = null;
		Reader reader;
		GPSDocument folderDocument = new GPSDocument(
				Messages.getMessage("gpsDocument.label"));
		GPSDocument document = null;

		if (file.exists()) {
			inputStream = getInputStream(file);
			reader = ReaderFactory.getInstance().getReader(file, null);
			document = reader.read(inputStream, file.getAbsolutePath());
			mergeDocuments(document, folderDocument);
			closeInputStream(inputStream);
		} else {

		}

		try {
			processDocumentAfterOpen(folderDocument);
			addDocument(getFolder(FOLDER_WORKSPACE), folderDocument);
		} catch (TrackItException e) {
			showAndLogErrorMessage(e.getMessage());
		}
		if (!document.getActivities().isEmpty()) {
			document.getActivities().get(0)
					.publishSelectionEvent(DocumentManager.this);
		} else if (!document.getCourses().isEmpty()) {
			document.getCourses().get(0)
					.publishSelectionEvent(DocumentManager.this);
		} else if (!document.getWaypoints().isEmpty()) {
			document.getWaypoints().get(0)
					.publishSelectionEvent(DocumentManager.this);
		}
		
		ArrayList<String> picFileList = database.getPicturesFromDB(document
				.getFileName());
		String photoPath, parentName;
		for(String pic : picFileList){

			String[] parts = pic.split("\\?");
			photoPath = parts[0];
			parentName = parts[1];
						
			for (Activity a : document.getActivities()){
				if(a.getName().equals(parentName)){
					a.addPicture(new File(photoPath));
				}
			}
			for (Course c : document.getCourses()){
				if(c.getName().equals(parentName)){
					c.addPicture(new File(photoPath));
				}
			}
		}
		for (Activity a : document.getActivities()) {
			a.setFilepath(document.getFileName());
		}
		for (Course c : document.getCourses()) {
			c.setFilepath(document.getFileName());
			c.setUnsavedFalse();
		}
	}

	private Object processDocumentAfterOpen(final GPSDocument document)
			throws TrackItException {
		new ConsolidationOperation().process(document);
		return document;
	}

	public void remove(GPSDocument document) {
		Folder folder = getFolder(document);
		folder.remove(document);
		for (Activity a : document.getActivities()) {
			remove(a);
			database.deleteFromDB(a);
		}
		for (Course c : document.getCourses()) {
			remove(c);
			database.deleteFromDB(c);
		}
		documents.remove(document.getId());// 58406
		EventManager.getInstance().publish(this, Event.DOCUMENT_DISCARDED,
				document);
	}

	public void remove(Activity activity) {
		for (Folder currentFolder : folders) {
			for (GPSDocument document : currentFolder.getDocuments()) {
				if (document.getActivities().contains(activity)) {
					document.remove(activity);
					database.deleteFromDB(activity);
					EventManager.getInstance().publish(this,
							Event.ACTIVITY_REMOVED, activity);
					return;
				}
			}
		}
	}

	public void remove(Course course) {
		for (Folder currentFolder : folders) {
			for (GPSDocument document : currentFolder.getDocuments()) {
				if (document.getCourses().contains(course)) {
					document.remove(course);
					database.deleteFromDB(course);
					EventManager.getInstance().publish(this,
							Event.COURSE_REMOVED, course);
					return;
				}
			}
		}
	}
}
