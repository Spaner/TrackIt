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
package com.trackit.presentation.utilities;

import static com.trackit.business.common.Messages.getMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer.Form;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.print.Doc;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.FileType;
import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.database.Database;
import com.trackit.business.dbsearch.SearchControl;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Picture;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.TrackStatus;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.TrackItPreferences;
import com.trackit.business.utilities.OperationConfirmationDialog;
import com.trackit.business.utilities.StringUtilities;
import com.trackit.business.writer.Writer;
import com.trackit.business.writer.WriterFactory;
import com.trackit.presentation.FileFilterFactory;
import com.trackit.presentation.InputTimeDialog;
import com.trackit.presentation.calendar.CalendarInterface;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;
import com.trackit.presentation.view.folder.PicturesItem;

public class OperationsFactory {
	private static OperationsFactory instance;
	private static Logger logger = Logger.getLogger(OperationsFactory.class
			.getName());

	private OperationsFactory() {
	}

	public static OperationsFactory getInstance() {
		if (instance == null) {
			instance = new OperationsFactory();
		}

		return instance;
	}

	// 12335: 2015-07-31  create a fake operation to be interpreted as "Insert Menu Item Separator"
	public static Operation getMenuItemsSeparatorFakeOperation(int seqNo) {
		return new Operation( getMessage("operation.group.menuSeparator")+Integer.toString(seqNo),
				getMessage("operation.name.menuSeparator"), "", null);
	}

	public List<Operation> getSupportedOperations(GPSDocument document) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add(createDiscardOperation(document));
		operations.add(createRemoveFromDatabaseOperation(document));	// 58406
		
		operations.add( getMenuItemsSeparatorFakeOperation(0));       	// 12335: 2015-07-31
		
		operations.add(createSaveDocumentOperation(document,false));   	// 12335: 2015-10-16
		List<Operation> ops = createFileExportOperations( document);
		for( Operation op : ops)
			operations.add(op);
		operations.add( getMenuItemsSeparatorFakeOperation(1));
		operations.add( createDuplicateOperation( document));		// 12335: 2017-04-08
		operations.add(createCopyToOperation(document, true));		// 12335: 2015-10-13
		operations.add(createCopyToOperation(document, false));		// 12335: 2017-04-08
		operations.add( getMenuItemsSeparatorFakeOperation(2));		// 12335: 2015-10-13
		
		operations.add( createEmptyCourseOperation());				// 12335: 2018-10-01
		operations.add( getMenuItemsSeparatorFakeOperation( 3)); 	// 12335: 2018-10-01
		
		operations.add(createRenameOperation(document));            // 12335: 2015-08-01
		operations.add(createShowPropertiesOperation(document));  	// 12335 : 2015-07-16

		return operations;
	}
	
	//12335: 2017-03-09
	public List<Operation> getCollectionDocumentSupportedOperations() {
		List<Operation> operations = new ArrayList<Operation>();
		
		operations.add( createNewSearchOperation());
		operations.add( createCalendarSearchOperation());				// 12335: 2018-04-10
		GPSDocument defaultCollection = DocumentManager.getInstance().getDefaultCollectionDocument();
		operations.add( getMenuItemsSeparatorFakeOperation( 0));
		operations.add( createDuplicateOperation( defaultCollection));
		operations.add( createCopyToOperation( defaultCollection, true));
		operations.add( createCopyToOperation( defaultCollection, false));
		operations.add( getMenuItemsSeparatorFakeOperation( 1));
		operations.add( createEmptyCollectionOperation());
//12335: 2018-06-29
//		operations.add( createShowPropertiesOperation(DocumentManager.getInstance().getDefaultCollectionDocument()));
		//12335: 2018-07-02
		if ( defaultCollection.countActivitiesAndCourses() == 0 )
			for( int i=3; i<operations.size(); i++)
				operations.get( i).setEnabled( false);
		
		return operations;
	}
	
	//12335: 2017-03-09
	public List<Operation> getCollectionSuportedOperations( Activity activity) {
		List<Operation> operations = new ArrayList<Operation>();
		
		operations.add( createDiscardOperation( activity));
		operations.add( getMenuItemsSeparatorFakeOperation( 0));
		operations.add( createLoadParentDocumentOperation( activity));		//2018-04-13: 12335
		operations.add( getMenuItemsSeparatorFakeOperation( 1));
		operations.add( createCopyToOperation( activity, true));
		operations.add( createCopyToOperation( activity, false));
		operations.add( getMenuItemsSeparatorFakeOperation( 2));
		operations.add( createShowPropertiesOperation( activity));
		
		return operations;
	}

	//12335: 2017-03-09
	public List<Operation> getCollectionSuportedOperations( Course course) {
		List<Operation> operations = new ArrayList<Operation>();
		
		operations.add( createDiscardOperation( course));
		operations.add( getMenuItemsSeparatorFakeOperation( 0));
		operations.add( createLoadParentDocumentOperation( course));		//2018-04-13: 12335
		operations.add( getMenuItemsSeparatorFakeOperation( 1));
		operations.add( createCopyToOperation( course, true));
		operations.add( createCopyToOperation( course, false));
		operations.add( getMenuItemsSeparatorFakeOperation( 2));
		operations.add( createShowPropertiesOperation( course));
		
		return operations;
	}

	public List<Operation> getSupportedOperations(Activity activity) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add( createDiscardOperation( activity));
		//12335: 2018-06-29 - add remove from DB if activity is there
		Operation removeOperation = createRemoveFromDatabaseOperation( activity);
		if ( removeOperation != null )
			operations.add( removeOperation);
		
		operations.add( getMenuItemsSeparatorFakeOperation(0));       	// 12335: 2015-09-23
		//12335: 2018-07-02
//		Operation importPhotos = createImportPhotographiesOperation(activity); 
//		if ( importPhotos != null )
//			operations.add( importPhotos);
		operations.add( createImportPhotographiesOperation(activity));
		for( Operation op : createFileExportOperations( activity) )
			operations.add( op);
		operations.add( getMenuItemsSeparatorFakeOperation(1));       	// 12335
		operations.add(createActivityToCourseOperation(activity));
		operations.add( getMenuItemsSeparatorFakeOperation(2));       	// 12335
		operations.add( createDuplicateOperation( activity)); 			// 12335: 2017-04-08
		operations.add( createCopyToOperation( activity, true));
		operations.add( createCopyToOperation( activity, false)); 		// 12335: 2017-04-08
		operations.add( getMenuItemsSeparatorFakeOperation(3));       	// 12335
		operations.add(createRenameOperation(activity));
		operations.add(changeSportType(activity));						//57421
		operations.add( getMenuItemsSeparatorFakeOperation( 4)); 		//12335: 2017-03-09
		operations.add( createShowPropertiesOperation( activity));
		return operations;
	}

	public List<Operation> getSupportedOperations(Course course) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add( createDiscardOperation( course));				//12335: 2015-09-23
		//12335: 2018-06-29 - add remove from DB if course is there
		Operation removeOperation = createRemoveFromDatabaseOperation( course);
		if ( removeOperation != null )
			operations.add( removeOperation);
		
		operations.add( getMenuItemsSeparatorFakeOperation(0));      	//12335
		//12335: 2018-07-02
//		Operation importPhotos = createImportPhotographiesOperation(course); // 12335
//		if ( importPhotos != null )
//			operations.add( importPhotos);
		operations.add( createImportPhotographiesOperation(course));
		for( Operation op : createFileExportOperations( course) )
			operations.add( op);
		operations.add( getMenuItemsSeparatorFakeOperation(1));      	//12335
		operations.add( createDuplicateOperation( course)); 			//12335: 2017-04-08
		operations.add( createCopyToOperation( course, true)); 			//12335: 2015-10-13
		operations.add( createCopyToOperation( course, false)); 		//12335: 2017-04-08
		operations.add( getMenuItemsSeparatorFakeOperation(2));      	//12335
		operations.add(createRenameOperation(course));
		operations.add(changeStartTimeOperation(course));				//57421
		operations.add(changeEndTimeOperation(course));					//57421
		operations.add(changeSportType(course));						//57421
		operations.add( getMenuItemsSeparatorFakeOperation( 4)); 		//12335: 2017-03-09
		operations.add( createShowPropertiesOperation( course));
		return operations;
	}
	
	public List<Operation> getSupportedOperations(Picture picture){
		List<Operation> operations = new ArrayList<Operation>();
		
		operations.add(createDiscardPictureOperation(picture));
		
		return operations;
	}
	
	public List<Operation> getSupportedOperations(PicturesItem pictures){
		List<Operation> operations = new ArrayList<Operation>();
		
		operations.add(createDiscardPicturesOperation(pictures));
		
		return operations;
	}

	//2018-06-24: 12335 - uses confirmAndDiscarOperation
	private Operation createDiscardOperation(final GPSDocument document) {
		String group = getMessage("operation.group.discard");
		String name = getMessage("operation.name.discard");
		String description = getMessage("operation.description.discard");
		Runnable action = new Runnable() {
			public void run() {
//12335: 2018-06-28
				confirmAndDiscardOperation( document, getMessage( "gpsDocument.label"), document.getName());
//				int result = JOptionPane.showConfirmDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage("operation.discard.confirm"),
//						getMessage("messages.warning"), JOptionPane.YES_NO_OPTION);
//				if (result == JOptionPane.YES_OPTION) {
//					DocumentManager.getInstance().discard(document);
//					JOptionPane.showMessageDialog(
//							TrackIt.getApplicationFrame(),
//							getMessage("operation.success.delete",
//									document.getDocumentItemName()));
//				}
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}

	//2018-06-24: 12335 - uses confirmAndDiscarOperation
	private Operation createDiscardOperation(final Activity activity) {
		String group = getMessage("operation.group.discard");
		String name = getMessage("operation.name.discard");
		String description = getMessage("operation.description.discard");
		Runnable action = new Runnable() {
			public void run() {
//12335: 2018-06-28
				confirmAndDiscardOperation( activity, getMessage( "activity.label"), activity.getName());
//				int result = JOptionPane.showConfirmDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage( "operation.discard.confirm"),
//						getMessage( "messages.warning"), JOptionPane.YES_NO_OPTION);
//				if ( result == JOptionPane.YES_OPTION ) {
//					DocumentManager.getInstance().delete(activity);
//					JOptionPane.showMessageDialog(
//							TrackIt.getApplicationFrame(),
//							getMessage("operation.success.delete",
//									activity.getDocumentItemName()));
//				}
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}

	//2018-06-24: 12335 - uses confirmAndDiscarOperation
	private Operation createDiscardOperation(final Course course) {
		String group = getMessage("operation.group.discard");
		String name = getMessage("operation.name.discard");
		String description = getMessage("operation.description.discard");
		Runnable action = new Runnable() {
			public void run() {
				//12335: 2018-06-28
				confirmAndDiscardOperation( course, getMessage( "course.label"), course.getName());
//				if ( OperationConfirmationDialog.showOperationConfirmationDialog(
//						getMessage( "operation.discard.confirm", course.getDocumentItemName()), getMessage( "messages.warning")) ) {
//					DocumentManager.getInstance().delete(course);
//					OperationConfirmationDialog.showOperationCompletionDialog( 
//							getMessage( "operation.success.delete", course.getDocumentItemName()),
//							getMessage( "messages.warning"));
//				}
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}
	
	//12335: 2018-06-29
	private void confirmAndDiscardOperation( DocumentItem item,
			  								 String documentItemType, String documentItemName) {
		if ( item.isGPSDocument() || item.isActivity() || item.isCourse() ) {
			if ( OperationConfirmationDialog.showOperationConfirmationDialog(
					getMessage( "operation.confirm.discard", documentItemType, documentItemName),
					getMessage( "messages.warning")) ) {
				if ( item.isActivity() )
					DocumentManager.getInstance().delete( (Activity) item);
				else
					if ( item.isCourse() )
						DocumentManager.getInstance().delete( (Course) item);
					else
						DocumentManager.getInstance().discard( (GPSDocument) item);
				OperationConfirmationDialog.showOperationCompletionDialog( 
						getMessage( "operation.success.discard", documentItemType, documentItemName),
						getMessage( "messages.warning"));
			}
		}
	}
	
	private Operation createActivityToCourseOperation(final Activity activity) {
		String group = getMessage("operation.group.activityToCourse");
		String name = getMessage("operation.name.activityToCourse");
		String description = getMessage("operation.description.activityToCourse");
		Runnable action = new Runnable() {
			public void run() {
				DocumentManager.getInstance().activityToCourse(activity, false);
			}
		};

		Operation activityToCourseOperation = new Operation(group, name,
				description, action);
		return activityToCourseOperation;
	}
	
	
	//12335: 2017-03-09
//	private Operation createMoveToOperation( Activity activity) {
//		String group = getMessage("operation.group.moveTo");
//		String name = getMessage("operation.name.moveTo");
//		String description = getMessage("operation.description.moveTo");
//
//		Runnable action = new Runnable() {
//			public void run() {
//				// TODO Auto-generated method stub
//				JOptionPane.showOptionDialog(TrackIt.getApplicationFrame(), "NOT YET IMPLEMENTED", 
//						"WARNING",
//						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0);
//			}
//		};
//		
//		return new Operation( group, name, description, action);
//	}
		
	//12335: 2017-03-09
//	private Operation createMoveToOperation( Course course) {
//		String group = getMessage("operation.group.moveTo");
//		String name = getMessage("operation.name.moveTo");
//		String description = getMessage("operation.description.moveTo");
//		
//		Runnable action = new Runnable() {
//			public void run() {
//				JOptionPane.showOptionDialog(TrackIt.getApplicationFrame(), "NOT YET IMPLEMENTED", 
//						"WARNING",
//						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0);
//			}
//		};
//		
//		return new Operation( group, name, description, action);
//	}
	
	// 12335: 2017-04-08
	private Operation createDuplicateOperation( final DocumentItem item) {
		String group       = getMessage( "operation.group.duplicate");
		String name        = getMessage( "operation.name.duplicate");
		String description = getMessage( "operation.description.duplicate");
		
		Runnable action = new Runnable() {
			@Override
			public void run() {
				List<DocumentItem> items = new ArrayList<>();
				items.add( item);
				DocumentManager.getInstance().duplicate( items);
			}
		};
		
		return new Operation( group, name, description, action);
	}
	
	// 12335 : 2015-07-16
	// 12335 : 2016-10-03 updated to support TrackStatus
	private Operation createShowPropertiesOperation(final GPSDocument document) {
		String group = getMessage("operation.group.docProperties");
		String name = getMessage("operation.name.docProperties");
		String description = getMessage("operation.description.docProperties", document.getName());
		Runnable action = new Runnable() {
			public void run() {
//				int na=0; int nc=0;
				TrackStatus stat;
				int aTracks = 0, aPictures=0, aMovies=0, aNotes=0;
				for(Activity a: document.getActivities()) {
//					if ( a.getUnsavedChanges())
//						na++;
					stat = a.getStatus();
					if ( stat.trackWasChanged() )
						aTracks++;
					if ( stat.picturesWereChanged() )
						aPictures++;
					if ( stat.moviesWereChanged() )
						aMovies++;
					if ( stat.notesWereChanged() )
						aNotes++;
				}
				int cTracks=0, cPictures=0, cMovies=0, cNotes=0;
				for(Course c: document.getCourses()) {
//					if (c .getUnsavedChanges() )
//						nc++;
					stat = c.getStatus();
					if ( stat.trackWasChanged() )
						cTracks++;
					if ( stat.picturesWereChanged() )
						cPictures++;
					if ( stat.moviesWereChanged() )
						cMovies++;
					if ( stat.notesWereChanged() )
						cNotes++;
				}
				String status = "Document is dirty: " + document.getChanged() + " dirty activities " +
						aTracks + " " + aPictures + " " + aMovies+ " " + aNotes +
						"(" + document.countActivities() + ")    dirty courses " +
						cTracks + " " + cPictures + " " + cMovies + " " + cNotes +
						"(" + document.countCourses() +")";
				System.out.println( status);
				String message = "";
				File file = new File(document.getFileName());
				if ( file.exists() ) {
					try {
						BasicFileAttributes attrs = Files.readAttributes( Paths.get(file.getAbsolutePath()),
								                                          BasicFileAttributes.class);
						message = getMessage("operation.message1.docProperties",
//	12335: 2018-02-25 - handles long names and paths so that dialogue width is not huge
//								document.getName(), file.getName(), file.getParent(),
								StringUtilities.wrapString( document.getName(), 64, "<br>"),
								StringUtilities.wrapString( file.getName(), 64, "<br>"),
								StringUtilities.breakPathString( file.getAbsolutePath(), 64, "<br>"),
								NumberFormat.getInstance(Locale.FRENCH).format(file.length()/1024),
								Formatters.getDateStringForFiles(attrs.creationTime()),
								Formatters.getDateStringForFiles(attrs.lastModifiedTime()),
								Formatters.getDateStringForFiles(attrs.lastAccessTime()),
								document.getId());
					} catch ( IOException e)
					{
						System.out.println("Error Accessing file attributes");
					}
				}
				else
				if ( message.isEmpty() )
					message = getMessage("operation.message2.docProperties", document.getName(), document.getId());
				JOptionPane.showOptionDialog(TrackIt.getApplicationFrame(), message, 
						getMessage("operation.name.docProperties") + ": " + document.getName(),
						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0);
			}
		};
		
		Operation showPropertiesOperation =
				new Operation(group, name, description, action);
		return showPropertiesOperation;
	}
	
	// 12335; 2017-03-08 - created
	private Operation createShowPropertiesOperation( final Activity activity) {
		String group = getMessage("operation.group.activityProperties");
		String name = getMessage("operation.name.activityProperties");
		String description = getMessage("operation.description.activityProperties", activity.getName());

		String message;
		if ( activity.getTrackpoints().size() > 0 ) {
			File file = new File( activity.getFilepath());
			Double distance   = 0.;
			Double totalTime  = 0.;
			Double movingTime = 0.;
			Double pausedTime = 0.;
			for( Session session: activity.getSessions() ) {
				distance   += session.getDistance();
				totalTime  += session.getElapsedTime();
				movingTime += session.getMovingTime();
				pausedTime += session.getPausedTime();
			}
			message = getMessage( "operation.message1.activityCourseProperties",
				                   getMessage( "activity.label"),
//	12335: 2018-02-25 - handles long names and paths so that dialogue width is not huge
//				                   activity.getName(),
//				                   file.getName(), file.getParent(),
				                   StringUtilities.wrapString( activity.getName(), 64, "<br>"),
				                   StringUtilities.wrapString( file.getName(), 64, "<br>"),
				                   StringUtilities.breakPathString( file.getAbsolutePath(), 64, "<br>"),
				                   Formatters.getFormatedDistance( distance), 
				                   Formatters.getFormatedDuration( totalTime),
				                   Formatters.getFormatedDuration( movingTime), 
				                   Formatters.getFormatedDuration( pausedTime),
				                   NumberFormat.getInstance(Locale.FRENCH).format( 
				                		   						activity.getTrackpoints().size()),
				                   activity.getId());
		}
		else
			message = getMessage( "operation.message2.activityCourseProperties",
								   getMessage( "course.label"), activity.getName());
		return createShowPropertiesActivityCourseOperation( group, name, description, 
															activity.getName(), message);
	}
	
	// 12335: 2017-03-08 - created
	private Operation createShowPropertiesOperation( final Course course) {
		String group = getMessage("operation.group.courseProperties");
		String name = getMessage("operation.name.courseProperties");
		String description = getMessage("operation.description.courseProperties", course.getName());
		
		String message;
		if ( course.getTrackpoints().size() > 0 ) {
			File file = new File( course.getFilepath());
			message = getMessage( "operation.message1.activityCourseProperties",
				                   getMessage( "course.label"), 
//	12335: 2018-02-25 - handles long names and paths so that dialogue width is not huge
//				                   course.getName(),
//				                   file.getName(), file.getParent(),
				                   StringUtilities.wrapString( course.getName(), 64, "<br>"),
				                   StringUtilities.wrapString( file.getName(), 64, "<br>"),
				                   StringUtilities.breakPathString( file.getAbsolutePath(), 64, "<br>"),
				                   Formatters.getFormatedDistance( course.getDistance()), 
				                   Formatters.getFormatedDuration( course.getTimerTime()),
				                   Formatters.getFormatedDuration( course.getMovingTime()),
				                   Formatters.getFormatedDuration( course.getPausedTime()),
				                   NumberFormat.getInstance(Locale.FRENCH).format( 
				                		   						course.getTrackpoints().size()),
				                   course.getId());
		}
		else
			message = getMessage( "operation.message2.activityCourseProperties",
								   getMessage( "course.label"), course.getName());
		return createShowPropertiesActivityCourseOperation( group, name, description, 
															course.getName(), message);
	}
	
	// 12335: 2017-03-08 - created
	private Operation createShowPropertiesActivityCourseOperation( 
			final String group, final String name, final String description,
			final String titleName, final String message) {
		Runnable action = new Runnable() {
			
			@Override
			public void run() {
				JOptionPane.showOptionDialog(TrackIt.getApplicationFrame(), message, 
						name + ": " + titleName,
						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0);
			}
		};
		
		return new Operation( group, name, description, action);
	}
	

	// 12335: 2017-03-10 -created
	private Operation createNewSearchOperation() {
		String group = getMessage("operation.group.newSearch");
		String name = getMessage("operation.name.newSearch");
		String description = getMessage("operation.description.newSearch");
		Runnable action = new Runnable() {
			public void run() {
				SearchControl.getInstance().newSearch(true);
			}
		};
		
		return new Operation( group, name, description, action);
	}

// 12335: 2018-04-10 - removed
//	// 12335: 2017-03-09 - created
//	private Operation createRefineSearchOperation( ) {
//		String group = getMessage("operation.group.refineSearch");
//		String name = getMessage("operation.name.refineSearch");
//		String description = getMessage("operation.description.refineSearch");
//		Runnable action = new Runnable() {
//			public void run() {
//				SearchControl.getInstance().newSearch(false);
//			}
//		};
//		
//		return new Operation( group, name, description, action);
//	}
	
	// 12335: 2018-04-10
	private Operation createCalendarSearchOperation() {
		String group = getMessage("operation.group.calendarSearch");
		String name = getMessage("operation.name.calendarSearch");
		String description = getMessage("operation.description.calendarSearch");
		Runnable action = new Runnable() {
			public void run() {
				CalendarInterface.getInstance().setVisible( true);
			}
		};
		
		return new Operation( group, name, description, action);
	}
	
	// 12335: 2015-08-01 supersedes old rename that only renamed courses
	//                   documents and activities' names can now be changed
	// 12335: 2017-04-26 simplified new name input dialog
	//					 calls the new DocumentManager.rename function suporting undo/redo
	private Operation createRenameOperation ( final DocumentItem item) {
		String group       = getMessage( "operation.group.rename");
		String name        = getMessage( "operation.name.rename");
		String description = getMessage( "operation.description.rename", item.getDocumentItemName());
		Runnable action = new Runnable() {
			public void run() {
				String oldName     = "";
				if ( item instanceof GPSDocument ) {
					oldName     = ((GPSDocument)item).getName();
				}
				else
					if ( item.isActivity() ) {
						oldName     = ((Activity)item).getName();
					}
					else
						if ( item.isCourse() ) {
							oldName     = ((Course)item).getName();
						}
						else
							return;
				String answer = "";
				boolean firstTime = true;
				while ( answer.isEmpty() ) {
					String message = "";
					if ( !firstTime ) 
						message = getMessage("operation.prompt2.rename") + "\n";
					message += getMessage("operation.prompt.rename",item.getDocumentItemName());
					answer = (String) JOptionPane.showInputDialog(
							TrackIt.getApplicationFrame(),
							message,
							getMessage("operation.group.rename") + "...", 
							JOptionPane.QUESTION_MESSAGE, null, null, oldName);
					System.out.println( "ANSWER '" + answer + "' (" + oldName + ")");
					if ( answer == null )
						return;
					if ( answer.equals( oldName) )
						answer = "";
					firstTime = false;
				}
				DocumentManager.getInstance().rename( item, "", oldName, answer, true, false);
//				String answer = (String) JOptionPane.showInputDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage("operation.prompt.rename",item.getDocumentItemName()),
//						getMessage("operation.group.rename") + "...", 
//						JOptionPane.QUESTION_MESSAGE, null, null, oldName);
//				while( answer != null && !answer.isEmpty() ){
//					if ( DocumentManager.getInstance().rename(item, oldName, answer) )
//						break;
//					answer = (String) JOptionPane.showInputDialog(
//							TrackIt.getApplicationFrame(),
//							getMessage("operation.prompt2.rename") + "\n"
//							          + getMessage("operation.prompt.rename",item.getDocumentItemName()),
//							getMessage("operation.group.rename") + "...",
//							JOptionPane.QUESTION_MESSAGE, null, null, oldName);
//				}
			}
		};
		
		return new Operation(group, name, description, action);
	}
	
	//12335: 2018-10-01
	private Operation createEmptyCourseOperation() {
		String group       = getMessage( "operation.group.newCourse");
		String name  	   = getMessage( "operation.name.newCourse");
		String description = getMessage( "operation.description.newCourse");
		Runnable action    = new Runnable() {
			@Override
			public void run() {
				DocumentManager manager = DocumentManager.getInstance();
				if ( manager.getSelectedDocument() != null ) {
					manager.newCourse( manager.getSelectedDocument());
				}
			}
		};
		return new Operation( group, name, description, action);
	}
	
	//12335: 2017-03-09
	private Operation createEmptyCollectionOperation () {
		return createEmptyOperation( DocumentManager.getInstance().getDefaultCollectionDocument(),
									 "Collection");
	}
	
	private Operation createEmptyOperation( final GPSDocument document, String type) {
		String group       = getMessage( "operation.group.empty" + type);
		String name        = getMessage( "operation.name.empty" + type);
		String description = getMessage( "operation.description.empty" + type);
		Runnable action =  new Runnable() {
			public void run() {
				if ( OperationConfirmationDialog.showOperationConfirmationDialog(
							Messages.getMessage( "operation.confirmation.emptyCollection"), 
							Messages.getMessage( "messages.warning")))
					DocumentManager.getInstance().clearDocumentContents( document);
			}
		};
		return new Operation( group, name, description, action);
	}
	
	private Operation changeStartTimeOperation(final Course course){
		String group = getMessage("operation.group.changeStartTime");
		String name = getMessage("operation.name.changeStartTime");
		String description = getMessage("operation.description.changeStartTime");
		final List<Long> savedTime = new ArrayList<Long>();
		final boolean addToUndoManager = true;
		Runnable action = new Runnable() {
			public void run() {
				String mode = "start";
				JDialog inputTimeDialog = new InputTimeDialog(course, savedTime, mode);
				inputTimeDialog.setVisible(true);
				
				DocumentManager.getInstance().changeStartTime(course, savedTime.get(0), addToUndoManager);
			}
		};

		Operation changeStartTimeOperation = new Operation(group, name,
				description, action);
		return changeStartTimeOperation;
	}
	
	private Operation changeEndTimeOperation(final Course course){
		String group = getMessage("operation.group.changeEndTime");
		String name = getMessage("operation.name.changeEndTime");
		String description = getMessage("operation.description.changeEndTime");
		final List<Long> savedTime = new ArrayList<Long>();
		final boolean addToUndoManager = true;
		Runnable action = new Runnable() {
			public void run() {
				String mode = "end";
				JDialog inputTimeDialog = new InputTimeDialog(course, savedTime, mode);
				inputTimeDialog.setVisible(true);
				//Date date = new Date(savedTime.get(0));
				DocumentManager.getInstance().changeEndTime(course, savedTime.get(0), addToUndoManager);
			}
		};

		Operation changeStartTimeOperation = new Operation(group, name,
				description, action);
		return changeStartTimeOperation;
	}
	

//	private Operation createDeleteOperation(final Activity activity) {
//		String group = getMessage("operation.group.delete");
//		String name = getMessage("operation.name.delete");
//		String description = getMessage("operation.description.delete");
//		Runnable action = new Runnable() {
//			public void run() {
//				DocumentManager.getInstance().delete(activity);
//				JOptionPane.showMessageDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage("operation.success.delete",
//								activity.getDocumentItemName()));
//			}
//		};
//
//		Operation deleteOperation = new Operation(group, name, description,
//				action);
//		return deleteOperation;
//	}

//	private Operation createDeleteOperation(final Course course) {
//		String group = getMessage("operation.group.delete");
//		String name = getMessage("operation.name.delete");
//		String description = getMessage("operation.description.delete");
//		Runnable action = new Runnable() {
//			public void run() {
//				DocumentManager.getInstance().delete(course);
//				JOptionPane.showMessageDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage("operation.success.delete",
//								course.getDocumentItemName()));
//			}
//		};
//
//		Operation deleteOperation = new Operation(group, name, description,
//				action);
//		return deleteOperation;
//	}
	
	//57421 ->
	private Operation changeSportType(final Activity activity) {
		String group = getMessage("operation.group.DBOnlyTrackData");
		String name = getMessage("operation.name.DBOnlyTrackData");
		String description = getMessage("operation.description.DBOnlyTrackData");
		Runnable action = new Runnable() {
			public void run() {
				//12335: 2016-06-14: added the following and commented next
				//12335: 2017-03-16: calls DBOnlyDataSelectionDialog
//				if ( SportSelectionDialog.showSportSelectionDialog( activity, false) == SportSelectionDialog.YES)
//					activity.updateSportAndSubSport( SportSelectionDialog.selectedSport(), 
//												  	 SportSelectionDialog.selectedSubSport());
				if ( DBOnlyDataSelectionDialog.showSelectionDialog( activity, false, true) == DBOnlyDataSelectionDialog.YES) {
//					activity.updateSportAndSubSport( DBOnlyDataSelectionDialog.selectedSport(), 
//													 DBOnlyDataSelectionDialog.selectedSubSport());
//					activity.setDifficulty( DBOnlyDataSelectionDialog.selectedDifficultyLevel());
//					activity.setTrackCondition( DBOnlyDataSelectionDialog.selectedTrackCondition());
				}
//				JDialog sportDialog = new SportDialog(activity);
//				sportDialog.setVisible(true);
//				DocumentManager.getInstance().changeSportType(activity, false);
			}
		};

		Operation changeSportTypeOperation = new Operation(group, name, description,
				action);
		return changeSportTypeOperation;
	}

	private Operation changeSportType(final Course course) {
		String group = getMessage("operation.group.DBOnlyTrackData");
		String name = getMessage("operation.name.DBOnlyTrackData");
		String description = getMessage("operation.description.DBOnlyTrackData");
		Runnable action = new Runnable() {
			public void run() {
				//12335: 2016-06-14: added the following and commented next 
				//12335: 2017-03-16: calls DBOnlyDataSelectionDialog
//				if ( SportSelectionDialog.showSportSelectionDialog( course, false) == SportSelectionDialog.YES)
//					course.updateSportAndSubSport( SportSelectionDialog.selectedSport(), 
//							  					   SportSelectionDialog.selectedSubSport());
				if ( DBOnlyDataSelectionDialog.showSelectionDialog( course, false, true) == DBOnlyDataSelectionDialog.YES) {
//					course.updateSportAndSubSport( DBOnlyDataSelectionDialog.selectedSport(), 
//												   DBOnlyDataSelectionDialog.selectedSubSport());
////					course.setDifficulty( DBOnlyDataSelectionDialog.selectedDifficultyLevel());
//					course.setTrackCondition( DBOnlyDataSelectionDialog.selectedTrackCondition());
				}
//				JDialog sportDialog = new SportDialog(course);
//				sportDialog.setVisible(true);
//				DocumentManager.getInstance().changeSportType(course, false);
			}
		};

		Operation changeSportTypeOperation = new Operation(group, name, description,
				action);
		return changeSportTypeOperation;
	}
	
	//2018-06-29: 12335
	//2018-04-12: 12335
	private Operation createLoadParentDocumentOperation( DocumentItem item) {
		final String parentDocumentName = (item instanceof Activity) ?
				((Activity) item).getFilepath(): ((Course) item).getFilepath();
		String group = getMessage("operation.group.loadParent");
		String name = getMessage("operation.name.loadParent");
		String description = getMessage("operation.description.loadParent");
			
		Runnable action = new Runnable() {				
			@Override
			public void run() {
				File file = new File( parentDocumentName);
				if ( file.exists() )
					DocumentManager.getInstance().openDocument( file);
			}
		};
		
		return new Operation( group, name, description, action);
	}
	
	public List<Operation> createFileExportOperations(DocumentItem item) {
		List<Operation> operations = new ArrayList<Operation>();

		FileType[] supportedFileTypes = item.getSupportedFileTypes();
		Operation operation;
		for (FileType fileType : supportedFileTypes) {
			operation = createFileExportOperation(item, fileType);
			operations.add(operation);
		}

		return operations;
	}

	// 12335: 2015-09-22
	public <T extends DocumentItem>Operation createFileExportOperation( final T item, final FileType fileType) {
		String group = getMessage("operation.group.fileExport");
		String name = getMessage("operation.name.fileExport", fileType.getDescription());
		String description = getMessage("operation.description.fileExport",
				item.getDocumentItemName(), fileType.getDescription());
			
		Runnable action = new Runnable() {
			
			@Override
			public void run() {
				doFileExportOperation( item, fileType);
			}
		};
		
		Operation fileExportOperation = new Operation(group, name, description,
				action);
		return fileExportOperation;
	}
	
	public boolean doFileExportOperation( DocumentItem item, FileType fileType) {
		for(GPSDocument d: DocumentManager.getInstance().getDocuments())
			System.out.println("ANTES " + d.getFileName());
		// Get the file to write to, return if user decides to abort
		File file = chooseFile(item, fileType, false);
		if ( file == null )
			return false;
		
		// Create new GPSDocument and assign item to it
		GPSDocument document = new GPSDocument(null);
		// To add document waypoints (writer will decide if to include) (12335: 2018-04-09)
		GPSDocument origin = null;
		for(GPSDocument d: DocumentManager.getInstance().getDocuments())
			System.out.println("CRIADO " + d.getFileName());
		if ( item instanceof GPSDocument ) {
			origin = (GPSDocument) item;
			DocumentManager.getInstance().fullyMergeDocuments((GPSDocument)item, document);
			List<Activity> activities = ((GPSDocument) item).getActivities();
			if ( activities != null && activities.size() > 0 ) {
				Activity a = activities.get(0);
				if ( a!= null )
					System.out.println( "Original " + a.getId() + " " + a.getStartTime() + "  " + a.getEndTime());
			}
			else {
				List<Course> courses = ((GPSDocument) item).getCourses();
				if ( courses != null && courses.size() > 0 ) {
					Course c = courses.get(0);
					if ( c != null )
						System.out.println( "Original " + c.getId() + " " + c.getStartTime() + "  " + c.getEndTime());
				}
			}
		}
		else {
			if ( item .isActivity() ) {
				origin = ((Activity) item).getParent();
				document.add((Activity) item);
				document.setName(((Activity) item).getName());
			}
			else {
				System.out.println( "Course parent " + ((Course)item).getParent().getName());
				origin = ((Course) item).getParent();
				document.add((Course) item);
				document.setName(((Course) item).getName());
			}
		}
		if ( origin != null )
			document.setWaypoints( origin.getWaypoints());
		document.setFileName(file.getAbsolutePath());
		for(GPSDocument d: DocumentManager.getInstance().getDocuments())
			System.out.println("ACERTADO " + d.getFileName());
		if ( document.getActivities().size() > 0 ) {
			Activity a = document.getActivities().get(0);
			if ( a!= null )
				System.out.println( "Cópia " + a.getId() + " " + a.getStartTime() + "  " + a.getEndTime());
		}
		System.out.println("EXPORTING " + item.getDocumentItemName() + "\n\t to " 
				+ file.getAbsolutePath());
		// Try to write to file
		boolean success = writeDocument(document, file, item.getDocumentItemName(),
				"operation.actionName.fileExport",
//				"operation.success.fileExport",
				"operation.failure.fileExport",
				true);
		// Exit if unsuccessful
		if ( !success )
			return false;
		
		Database.getInstance().updateDB(document, true);
		
		// Success, make it known
		JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
				getMessage("operation.success.fileExport",	item.getDocumentItemName()));

		for(GPSDocument d: DocumentManager.getInstance().getDocuments())
			System.out.println("DEPOIS " + d.getFileName());
		return true;
	}

	public <T extends DocumentItem> Operation createSaveDocumentOperation( 
										final GPSDocument item, final boolean saveToFileOnly) {
		String group = getMessage("operation.group.save");
		String name  = getMessage("operation.name.save");
		String description = getMessage("operation.description.save", item.getDocumentItemName());

		Runnable action = new Runnable() {
			public void run() {
				boolean fileWasSelected = false;
				
				// Save to file only when document really needs it
				if ( item.needsToBeSavedToFile() ) {
					// Create document copy to work
					GPSDocument document = new GPSDocument(null);
					DocumentManager.getInstance().fullyMergeDocuments(item, document);
					// Variable used when the document to save has no file name to
					// 1) Set the filename to the original document if write is successful
					// 2) Update the export directory if write is successful
					    // to know if to update the export directory 
					// Check if document has yet to be assigned a file (prompt for it if yes)
					File selectedFile = new File(document.getFileName()); 
					if ( !selectedFile.exists() || !selectedFile.isFile() ) {
						selectedFile = chooseFile(item, FileType.ALL, true);
						// exit if still unsuccessful
						if ( selectedFile == null )
							return;
						fileWasSelected = true;
						document.setFileName(selectedFile.getAbsolutePath());
						// set the new filename to the document only after successful file write
					}
					
					// Attempt to write the document to file, quit if unsuccessful
					boolean success = writeDocument(document, selectedFile, item.getDocumentItemName(),
							"operation.name.save",
//							"operation.success.save",
							"operation.failure.save",
							fileWasSelected);
					if ( !success )
						return;
					
					// Update 1) export directory; 2) document filename, if file had to be selected
					if ( fileWasSelected ) {
						TrackIt.getApplicationPanel().updateInitialExportDirectory(selectedFile);
						item.setFileName(selectedFile.getAbsolutePath());
					}
				}

				// Update DB if not save to file only
				if ( !saveToFileOnly )
					Database.getInstance().updateDB(item, fileWasSelected);
				
				// Reset status
				item.resetStatus();
				
				// Successful, make it known
				JOptionPane.showMessageDialog( TrackIt.getApplicationFrame(),
						getMessage("operation.success.save", item.getDocumentItemName()));
			}
		};
		
		return new Operation(group, name, description, action);
	}

	private File chooseFile( final DocumentItem item, final FileType fileType, final boolean isSave) {
		File fileSelected = null;
		String message1;
		String titleMessage;		//12335: 2017-08-07
		
		// Set FileChooser
		UIManager.put("FileChooser.cancelButtonText", Messages.getMessage( "dialog.fileExport.cancelButton"));	//12335: 2017-08-07 	
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		TrackIt.getApplicationPanel().setInitialExportDirectory(fileChooser);
		FileFilterFactory factory = FileFilterFactory.getInstance();
		if ( fileType == FileType.ALL )
			for( FileType f: item.getSupportedFileTypes() )
				fileChooser.addChoosableFileFilter(factory.getFilter(f));
		else
			fileChooser.addChoosableFileFilter(factory.getFilter(fileType));
		if ( isSave ) {
			message1 = getMessage("dialog.fileSave.saveButton");
			titleMessage = getMessage( "dialog.fileSave.title");		//12335: 2017-08-07
		}
		else {
			message1 =getMessage("dialog.fileExport.exportButton");
			titleMessage = getMessage( "dialog.fileExport.title");		//12335: 2017-08-07
		}
		fileChooser.setDialogTitle( titleMessage);						//12335: 2017-08-07
		// Repeat until we get a selection or selection cancellation
		boolean repeat = true;
		while( repeat ) {
			int choice = fileChooser.showDialog(TrackIt.getApplicationFrame(), message1);
//			int choice = fileChooser.showSaveDialog( TrackIt.getApplicationFrame());
			if ( choice == JFileChooser.APPROVE_OPTION ) {
				fileSelected = fileChooser.getSelectedFile();
				// Check file extension
				String requiredExtension =
						factory.getFileType(fileChooser.getFileFilter()).getExtension();
				System.out.println( "Required extension: " + requiredExtension);
				System.out.println( "Absolute path     : " + fileSelected.getAbsolutePath());
//12335: 2017-08-06
//				if ( !fileSelected.getAbsolutePath().toLowerCase().endsWith(requiredExtension) )
				if ( !fileSelected.getAbsolutePath().toLowerCase().endsWith( "." + requiredExtension) )
					fileSelected = new File( fileSelected.getAbsolutePath() + "." + requiredExtension);
				// File exists? If yes, we need to ask to overwrite
				System.out.println( "Absolute path     : " + fileSelected.getAbsolutePath());
				if ( fileSelected.exists() ) {
					int overwrite = JOptionPane.showConfirmDialog(TrackIt.getApplicationFrame(),
							getMessage("operation.overwriteFile"),
							getMessage("operation.title.chooseOption"),
							JOptionPane.YES_NO_OPTION);
					if ( overwrite == JOptionPane.YES_OPTION )
						repeat = false;
				}
				else
					repeat = false;
			}
			else {
				fileSelected = null;
				repeat = false;
			}
		}
		return fileSelected;
	}

	public boolean writeTemporaryDocument( GPSDocument document, File file) {
		return writeDocument(document, file, "", "", "", false);
	}
	
	private boolean writeDocument( GPSDocument document, File file, String nameToDisplay,
			String operationName, /*String successMessage,*/ String failureMessage,
			boolean updateExportDirectory) {
		try {
			Map<String, Object> options = new HashMap<>();
			options.put(Constants.Writer.OUTPUT_DIR, file.getParentFile());
			Writer writer = WriterFactory.getInstance().getWriter(file, options);
			writer.write(document);
		} catch ( TrackItException e) {
			// Write failed
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
					getMessage(failureMessage,nameToDisplay), operationName,
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
//		// Write succeeded, make it known
//		JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
//				getMessage(successMessage, nameToDisplay));
		
		// updateEx export directory, if applicable
		if ( updateExportDirectory )
			TrackIt.getApplicationPanel().updateInitialExportDirectory(file);
		
		return true;
	}



	// 12335: 2015-10-13: Copy operation #######################################################
	// 12335: 2016-06-17: this is an interim solution based on file write to temporary file
	//                    followed by read/import
	// 12335: 2017-04-08: No longer an interim solution based on file write to temporary file
	//                    Uses DocumentManager.copyTo that supports both copy and move through
	//                    the (new) 2nd argument (isCopy)
	private Operation createCopyToOperation( final DocumentItem item, final boolean isCopy) {
		String itemType = "";
		if ( item instanceof GPSDocument )
			itemType = getMessage("dataView.dataType.document");
		else
			if ( item.isActivity() )
				itemType = getMessage("dataView.dataType.activity");
			else
				itemType = getMessage("dataView.dataType.course");
		String group;
		String name;
		String description;
		if ( isCopy ) {
			group = getMessage("operation.group.copyTo");
			name = getMessage("operation.name.copyTo");
			description = getMessage("operation.description.copyTo", itemType);
		}
		else {
			group = getMessage("operation.group.moveTo");
			name = getMessage("operation.name.moveTo");
			description = getMessage("operation.description.moveTo", itemType);
		}
		Runnable action = new Runnable() {
			public void run() {
				DocumentManager.getInstance().copyTo(						// 12335: 2017-04-08
												new ArrayList<>( Arrays.asList( item)), isCopy);
			}
		};
		Operation copyOperation = new Operation(group, name, description, action);
		return copyOperation;
	}
	// #########################################################################################

	
	//12335: 2018-06-29 - uses the services from confirmAndRemoveFromDatabaseOperation
	//					  checks if document exists in the DB
	private Operation createRemoveFromDatabaseOperation( final GPSDocument document) {
		String group = getMessage("operation.group.removedb");
		String name = getMessage("operation.name.removedb");
		String description = getMessage("operation.description.removedb");
		Runnable action = new Runnable() {
			public void run() {
				confirmAndRemoveFromDatabaseOperation( 
						document, getMessage( "gpsDocument.label"), document.getName());
//				int result = JOptionPane.showConfirmDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage("operation.removedb.confirm"),
//						getMessage("warning"), JOptionPane.YES_NO_OPTION);
//				if (result == JOptionPane.YES_OPTION) {
//					DocumentManager.getInstance().remove(document);
//				}
			}
		};

		Operation removeOperation = new Operation(group, name, description,
				action);
		//12335: 2018-07-02 - disable if document is not registered
		if ( ! Database.getInstance().doesDocumentExistInDB( document) )
			removeOperation.setEnabled( false);
		return removeOperation;			
	}

	//12335: 2018-06-29 - uses the services from confirmAndRemoveFromDatabaseOperation
	//					  checks if activity exists in the DB
	private Operation createRemoveFromDatabaseOperation(final Activity activity) {
		String group = getMessage("operation.group.removedb");
		String name = getMessage("operation.name.removedb");
		String description = getMessage("operation.description.removedb");
		Runnable action = new Runnable() {
			public void run() {
				confirmAndRemoveFromDatabaseOperation( 
						activity, getMessage( "activity.label"), activity.getName());
//				int result = JOptionPane.showConfirmDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage("operation.removedb.confirm"),
//						getMessage("warning"), JOptionPane.YES_NO_OPTION);
//				if (result == JOptionPane.YES_OPTION) {
//					DocumentManager.getInstance().remove(activity);
//					JOptionPane.showMessageDialog(
//							TrackIt.getApplicationFrame(),
//							getMessage("operation.success.delete",
//									activity.getDocumentItemName()));
//				}
			}
		};

		Operation removeOperation = new Operation(group, name, description,
				action);
		List<String> list = Database.getInstance().getActivityNames( activity.getFilepath());
		if ( list != null && ! list.contains( activity.getName()) )
			removeOperation.setEnabled( false);
		return removeOperation;
	}

	//12335: 2018-06-29 - uses the services from confirmAndRemoveFromDatabaseOperation
	//					  checks if document exists in the DB
	private Operation createRemoveFromDatabaseOperation(final Course course) {
		String group = getMessage("operation.group.removedb");
		String name = getMessage("operation.name.removedb");
		String description = getMessage("operation.description.removedb");
		Runnable action = new Runnable() {
			public void run() {
				confirmAndRemoveFromDatabaseOperation( 
						course, getMessage( "course.label"), course.getName());
//				int result = JOptionPane.showConfirmDialog(
//						TrackIt.getApplicationFrame(),
//						getMessage("operation.removedb.confirm"),
//						getMessage("warning"), JOptionPane.YES_NO_OPTION);
//				if (result == JOptionPane.YES_OPTION) {
//					DocumentManager.getInstance().remove(course);
//					JOptionPane.showMessageDialog(
//							TrackIt.getApplicationFrame(),
//							getMessage("operation.success.delete",
//									course.getDocumentItemName()));
//				}
			}
		};

		Operation removeOperation = new Operation(group, name, description,
				action);
		List<String> list = Database.getInstance().getCourseNames( course.getFilepath());
		if ( list != null & ! list.contains( course.getName()) )
			removeOperation.setEnabled( false);
			return removeOperation;
	}

	//12335: 2018-06-29
	private void confirmAndRemoveFromDatabaseOperation( DocumentItem item,
			  								 			String documentItemType, String documentItemName) {
		if ( item.isGPSDocument() || item.isActivity() || item.isCourse() ) {
			if ( OperationConfirmationDialog.showOperationConfirmationDialog(
					getMessage( "operation.confirm.removedb", documentItemType, documentItemName),
					getMessage( "messages.warning")) ) {
				if ( item.isActivity() )
					DocumentManager.getInstance().remove( (Activity) item);
				else
					if ( item.isCourse() )
						DocumentManager.getInstance().remove( (Course) item);
					else
						DocumentManager.getInstance().remove( (GPSDocument) item);
				OperationConfirmationDialog.showOperationCompletionDialog( 
						getMessage( "operation.success.removedb", documentItemType, documentItemName),
						getMessage( "messages.warning"));
			}
		}
	}
	
//	// 12335: 2016-06-05
//	private Operation createImportPhotographiesOperation(final Activity activity) {
//		return createImportPhotographiesOperation((DocumentItem) activity);
//	}
//	// 12335: 2016-06-05 end

	
//	private Operation createImportPhotographiesOperation(final Activity activity) { // 12335
	private Operation createImportPhotographiesOperation(final DocumentItem item) {
		String group = getMessage("operation.group.importPicture");
		String name = getMessage("operation.name.importPicture");
		String description = getMessage("operation.description.importPicture",
				"Activity");
		Runnable action = new Runnable() {
			public void run() {
				UIManager.put("FileChooser.cancelButtonText", Messages.getMessage( "applicationPanel.cancel.importPhotos"));	//12335: 2017-08-07 	
				String action = getMessage("applicationPanel.button.importPhotos");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle( getMessage("applicationPanel.title.importPhotos"));		//12335: 2017-08-07

				String[] acceptedExtensions = { "jpg", "jpeg" };
				FileFilter filter = new FileNameExtensionFilter(
						"JPEG (*.JPG;*JPEG)", acceptedExtensions);
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setFileFilter(filter);
				setPictureDirectory(fileChooser);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.showDialog(TrackIt.getApplicationFrame(), action);
				final File[] files = fileChooser.getSelectedFiles();
				new Task(new Action() {

					@Override
					public String getMessage() {
						return Messages.getMessage( "operation.progress.importPicture");	//12335: 2018-07-07
					}

					@Override
					public Object execute() throws TrackItException {
						if ( files.length > 0 ) {					//12335: 2017-08-07
							if ( item.isActivity() )
								((Activity)item).addPictures(files);
							else
								((Course)item).addPictures(files);
							updatePictureDirectory(files[0]);
						}
						return null;
					}

					@Override
					public void done(Object result) {
						// TODO Auto-generated method stub

					}

				}).execute();
			}
		};

		Operation importPhotoOperation = new Operation(group, name,
				description, action);
		return importPhotoOperation;
	}

//	private Operation createImportPhotographiesOperation(final Course course) {
//		return createImportPhotographiesOperation((DocumentItem) course);
//		String group = getMessage("operation.group.importpicture");
//		String name = getMessage("operation.name.importpicture");
//		String description = getMessage("operation.description.importpicture",
//				getMessage("dataView.dataType.course"));
//		Runnable action = new Runnable() {
//			public void run() {
//				String action = getMessage("applicationPanel.button.import");
//				JFileChooser fileChooser = new JFileChooser();
//
//				String[] acceptedExtensions = { "jpg", "jpeg" };
//				FileFilter filter = new FileNameExtensionFilter(
//						"JPEG (*.JPG;*JPEG)", acceptedExtensions);
//				fileChooser.setAcceptAllFileFilterUsed(false);
//				fileChooser.setFileFilter(filter);
//				setPictureDirectory(fileChooser);
//				fileChooser.setMultiSelectionEnabled(true);
//				fileChooser.showDialog(TrackIt.getApplicationFrame(), action);
//				final File[] files = fileChooser.getSelectedFiles();
//				new Task(new Action() {
//
//					@Override
//					public String getMessage() {
//						// TODO Auto-generated method stub
//						return null;
//					}
//
//					@Override
//					public Object execute() throws TrackItException {
//						course.addPictures(files);
//						updatePictureDirectory(files[0]);
//						return null;
//					}
//
//					@Override
//					public void done(Object result) {
//						// TODO Auto-generated method stub
//
//					}
//
//				}).execute();
//			}
//		};
//
//		Operation importPhotoOperation = new Operation(group, name,
//				description, action);
//		return importPhotoOperation;
//	}

	private void updatePictureDirectory(File file) {
		TrackIt.getPreferences().setPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.LAST_PICTURE_DIRECTORY,
				file.getParent());
	}

	private void setPictureDirectory(final JFileChooser fileChooser) {
		TrackItPreferences prefs = TrackIt.getPreferences();
		String initialDirectory = prefs.getPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.LAST_PICTURE_DIRECTORY,
				System.getProperty("user.home"));
		File currentDirectory = new File(initialDirectory);
		fileChooser.setCurrentDirectory(currentDirectory);
	}
	
	private Operation createDiscardPictureOperation(final Picture picture){
		String group = getMessage("operation.group.removePicture");
		String name = getMessage("operation.name.removePicture");
		String description = getMessage("operation.description.removePicture");
		Runnable action = new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.removePicture.confirm"),
						getMessage("warning"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					picture.getContainer().removePicture(picture);
				}
			}
		};

		Operation removeOperation = new Operation(group, name, description,
				action);
		return removeOperation;
	}
	
	private Operation createDiscardPicturesOperation(final PicturesItem pictures){
		String group = getMessage("operation.group.removePictures");
		String name = getMessage("operation.name.removePictures");
		String description = getMessage("operation.description.removePictures");
		Runnable action = new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.removePictures.confirm"),
						getMessage("warning"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					pictures.getPictures().get(0).getContainer().removePictures();
//					for (Picture picture : pictures.getPictures()){
//						picture.getContainer().removePicture(picture);
//					}
				}
			}
		};

		Operation removeOperation = new Operation(group, name, description,
				action);
		return removeOperation;
	}
}
