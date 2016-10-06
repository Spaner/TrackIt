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
package com.henriquemalheiro.trackit.presentation.utilities;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.TrackItPreferences;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterFactory;
import com.henriquemalheiro.trackit.presentation.FileFilterFactory;
import com.miguelpernas.trackit.presentation.InputTimeDialog;
import com.miguelpernas.trackit.presentation.SportDialog;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;
import com.jb12335.trackit.business.domain.TrackStatus;
import com.jb12335.trackit.presentation.utilities.SportSelectionDialog;
import com.pg58406.trackit.business.db.Database;
import com.pg58406.trackit.business.domain.Picture;
import com.pg58406.trackit.presentation.view.folder.PicturesItem;

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
		
		operations.add(createSaveDocumentOperation(document));         	// 12335: 2015-08-08
		List<Operation> ops = createFileExportOperations( document);
		for( Operation op : ops)
			operations.add(op);
		operations.add( getMenuItemsSeparatorFakeOperation(1));
		operations.add(createCopyOperation(document));				// 12335: 2015-10-13
		operations.add( getMenuItemsSeparatorFakeOperation(2));		// 12335: 2015-10-13
		
		operations.add(createRenameOperation(document));            // 12335: 2015-08-01
		operations.add(createShowPropertiesOperation(document));  	// 12335 : 2015-07-16

		return operations;
	}

	public List<Operation> getSupportedOperations(Activity activity) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add( createDiscardOperation( activity));
		operations.add( getMenuItemsSeparatorFakeOperation(0));       	// 12335: 2015-09-23
		Operation importPhotos = createImportPhotographiesOperation(activity);  //12335
		if ( importPhotos != null )
			operations.add( importPhotos);
		for( Operation op : createFileExportOperations( activity) )
			operations.add( op);
		operations.add( getMenuItemsSeparatorFakeOperation(1));       	// 12335
		operations.add(createActivityToCourseOperation(activity));
		operations.add( getMenuItemsSeparatorFakeOperation(2));       	// 12335
		operations.add( createCopyOperation( activity));
		operations.add( getMenuItemsSeparatorFakeOperation(3));       	// 12335
		operations.add(createRenameOperation(activity));
		operations.add(changeSportType(activity));						//57421
		return operations;
	}

	public List<Operation> getSupportedOperations(Course course) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add( createDiscardOperation( course));				//12335: 2015-09-23
		operations.add( getMenuItemsSeparatorFakeOperation(0));      	//12335
		Operation importPhotos = createImportPhotographiesOperation(course); // 12335
		if ( importPhotos != null )
			operations.add( importPhotos);
		for( Operation op : createFileExportOperations( course) )
			operations.add( op);
		operations.add( getMenuItemsSeparatorFakeOperation(1));      	//12335
		operations.add( createCopyOperation( course)); 					//12335: 2015-10-13
		operations.add( getMenuItemsSeparatorFakeOperation(2));      	//12335
		operations.add(createRenameOperation(course));
		operations.add(changeStartTimeOperation(course));				//57421
		operations.add(changeEndTimeOperation(course));					//57421
		operations.add(changeSportType(course));						//57421
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

	private Operation createDiscardOperation(final GPSDocument document) {
		String group = getMessage("operation.group.discard");
		String name = getMessage("operation.name.discard");
		String description = getMessage("operation.description.discard");
		Runnable action = new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.discard.confirm"),
						getMessage("warning"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					DocumentManager.getInstance().discard(document);
				}
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}

	private Operation createDiscardOperation(final Activity activity) {
		String group = getMessage("operation.group.discard");
		String name = getMessage("operation.name.discard");
		String description = getMessage("operation.description.discard");
		Runnable action = new Runnable() {
			public void run() {
				DocumentManager.getInstance().delete(activity);
				JOptionPane.showMessageDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.success.delete",
								activity.getDocumentItemName()));
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}

	private Operation createDiscardOperation(final Course course) {
		String group = getMessage("operation.group.discard");
		String name = getMessage("operation.name.discard");
		String description = getMessage("operation.description.discard");
		Runnable action = new Runnable() {
			public void run() {
				DocumentManager.getInstance().delete(course);
				JOptionPane.showMessageDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.success.delete",
								course.getDocumentItemName()));
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
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
								document.getName(), file.getName(), file.getParent(),
								NumberFormat.getInstance(Locale.FRENCH).format(file.length()/1024),
								Formatters.getDateStringForFiles(attrs.creationTime()),
								Formatters.getDateStringForFiles(attrs.lastModifiedTime()),
								Formatters.getDateStringForFiles(attrs.lastAccessTime()));
					} catch ( IOException e)
					{
						System.out.println("Error Accessing file attributes");
					}
				}
				else
				if ( message.isEmpty() )
					message = getMessage("operation.message2.docProperties", document.getName());
				System.out.println(message);
				JOptionPane.showOptionDialog(TrackIt.getApplicationFrame(), message, 
						document.getName() + ": " + getMessage("operation.name.docProperties"),
						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0);
			}
		};
		
		Operation showPropertiesOperation =
				new Operation(group, name, description, action);
		return showPropertiesOperation;
	}
	

	// 12335: 2015-08-01 supersedes old rename that renamed courses only
	//                   documents and activities' names can now be changed
	private Operation createRenameOperation ( final DocumentItem item) {
		System.out.println("CHANGING NAME for " +item.getDocumentItemName());
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
				String answer = (String) JOptionPane.showInputDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.prompt.rename",item.getDocumentItemName()),
						getMessage("operation.group.rename") + "...", 
						JOptionPane.QUESTION_MESSAGE, null, null, oldName);
				while( answer != null && !answer.isEmpty() ){
					if ( DocumentManager.getInstance().rename(item, oldName, answer) )
						break;
					answer = (String) JOptionPane.showInputDialog(
							TrackIt.getApplicationFrame(),
							getMessage("operation.prompt2.rename") + "\n"
							          + getMessage("operation.prompt.rename",item.getDocumentItemName()),
							getMessage("operation.group.rename") + "...",
							JOptionPane.QUESTION_MESSAGE, null, null, oldName);
				}
			}
		};
		
		return new Operation(group, name, description, action);
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
	

	private Operation createDeleteOperation(final Activity activity) {
		String group = getMessage("operation.group.delete");
		String name = getMessage("operation.name.delete");
		String description = getMessage("operation.description.delete");
		Runnable action = new Runnable() {
			public void run() {
				DocumentManager.getInstance().delete(activity);
				JOptionPane.showMessageDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.success.delete",
								activity.getDocumentItemName()));
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}

	private Operation createDeleteOperation(final Course course) {
		String group = getMessage("operation.group.delete");
		String name = getMessage("operation.name.delete");
		String description = getMessage("operation.description.delete");
		Runnable action = new Runnable() {
			public void run() {
				DocumentManager.getInstance().delete(course);
				JOptionPane.showMessageDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.success.delete",
								course.getDocumentItemName()));
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}
	
	//57421 ->
	private Operation changeSportType(final Activity activity) {
		String group = getMessage("operation.group.changeSportType");
		String name = getMessage("operation.name.changeSportType");
		String description = getMessage("operation.description.changeSportType");
		Runnable action = new Runnable() {
			public void run() {
				//12335: 2016-06-14: added the following and commented next 
				if ( SportSelectionDialog.showSportSelectionDialog( activity, false) == SportSelectionDialog.YES)
					activity.updateSportAndSubSport( SportSelectionDialog.selectedSport(), 
												  	 SportSelectionDialog.selectedSubSport());
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
		String group = getMessage("operation.group.changeSportType");
		String name = getMessage("operation.name.changeSportType");
		String description = getMessage("operation.description.changeSportType");
		Runnable action = new Runnable() {
			public void run() {
				//12335: 2016-06-14: added the following and commented next 
				if ( SportSelectionDialog.showSportSelectionDialog( course, false) == SportSelectionDialog.YES)
					course.updateSportAndSubSport( SportSelectionDialog.selectedSport(), 
							  					   SportSelectionDialog.selectedSubSport());
//				JDialog sportDialog = new SportDialog(course);
//				sportDialog.setVisible(true);
//				DocumentManager.getInstance().changeSportType(course, false);
			}
		};

		Operation changeSportTypeOperation = new Operation(group, name, description,
				action);
		return changeSportTypeOperation;
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
		System.out.println( "Group: " + group + "  Name: " + name + "   Descr: " + description);
		
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
		for(GPSDocument d: DocumentManager.getInstance().getDocuments())
			System.out.println("CRIADO " + d.getFileName());
		if ( item instanceof GPSDocument ) {
			DocumentManager.getInstance().fullyMergeDocuments((GPSDocument)item, document);
		}
		else {
			if ( item .isActivity() ) {
				document.add((Activity) item);
				document.setName(((Activity) item).getName());
			}
			else {
				document.add((Course) item);
				document.setName(((Course) item).getName());
			}
		}
		document.setFileName(file.getAbsolutePath());
		for(GPSDocument d: DocumentManager.getInstance().getDocuments())
			System.out.println("ACERTADO " + d.getFileName());
		
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

	public <T extends DocumentItem> Operation createSaveDocumentOperation( final GPSDocument item) {
		String group = getMessage("operation.group.save");
		String name  = getMessage("operation.name.save");
		String description = getMessage("operation.description.save", item.getDocumentItemName());

		Runnable action = new Runnable() {
			public void run() {
				// Create document copy to work
				GPSDocument document = new GPSDocument(null);
				DocumentManager.getInstance().fullyMergeDocuments(item, document);
				// Variable used when the document to save has no file name to
				// 1) Set the filename to the original document if write is successful
				// 2) Update the export directory if write is successful
				boolean fileWasSelected = false;    // to know if to update the export directory 
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
//						"operation.success.save",
						"operation.failure.save",
						fileWasSelected);
				if ( !success )
					return;

				// Update 1) export directory; 2) document filename, if file had to be selected
				if ( fileWasSelected ) {
					TrackIt.getApplicationPanel().updateInitialExportDirectory(selectedFile);
					item.setFileName(selectedFile.getAbsolutePath());
				}
				
				System.out.println("Before db " + item.wasRenamed() + " " + document.wasRenamed());
				Database.getInstance().updateDB(item, fileWasSelected);
				
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
		
		// Set FileChooser
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
		if ( isSave )
			message1 = getMessage("operation.name.save");
		else
			message1 =getMessage("operation.actionName.fileExport");
		
		// Repeat until we get a selection or selection cancellation
		boolean repeat = true;
		while( repeat ) {
			int choice = fileChooser.showDialog(TrackIt.getApplicationFrame(), message1);
			if ( choice == JFileChooser.APPROVE_OPTION ) {
				fileSelected = fileChooser.getSelectedFile();
				// Check file extension
				String requiredExtension =
						factory.getFileType(fileChooser.getFileFilter()).getExtension();
				if ( !fileSelected.getAbsolutePath().toLowerCase().endsWith(requiredExtension) )
					fileSelected = new File( fileSelected.getAbsolutePath() + "." + requiredExtension);
				// File exists? If yes, we need to ask to overwrite
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
	private Operation createCopyOperation( final DocumentItem item) {
		String itemType = "";
		if ( item instanceof GPSDocument )
			itemType = getMessage("dataView.dataType.document");
		else
			if ( item.isActivity() )
				itemType = getMessage("dataView.dataType.activity");
			else
				itemType = getMessage("dataView.dataType.course");
		String group = getMessage("operation.group.copy");
		String name = getMessage("operation.name.copy");
		String description = getMessage("operation.description.copy", itemType);
		Runnable action = new Runnable() {
			public void run() {
				DocumentManager.getInstance().copyItem(item);				
			}
		};
		Operation copyOperation = new Operation(group, name, description, action);
		return copyOperation;
	}
	// #########################################################################################

	private Operation createRemoveFromDatabaseOperation(
			final GPSDocument document) {
		String group = getMessage("operation.group.removedb");
		String name = getMessage("operation.name.removedb");
		String description = getMessage("operation.description.removedb");
		Runnable action = new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.removedb.confirm"),
						getMessage("warning"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					DocumentManager.getInstance().remove(document);
				}
			}
		};

		Operation removeOperation = new Operation(group, name, description,
				action);
		return removeOperation;
	}

	private Operation createRemoveFromDatabaseOperation(final Activity activity) {
		String group = getMessage("operation.group.removedb");
		String name = getMessage("operation.name.removedb");
		String description = getMessage("operation.description.removedb");
		Runnable action = new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.removedb.confirm"),
						getMessage("warning"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					DocumentManager.getInstance().remove(activity);
					JOptionPane.showMessageDialog(
							TrackIt.getApplicationFrame(),
							getMessage("operation.success.delete",
									activity.getDocumentItemName()));
				}
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}

	private Operation createRemoveFromDatabaseOperation(final Course course) {
		String group = getMessage("operation.group.removedb");
		String name = getMessage("operation.name.removedb");
		String description = getMessage("operation.description.removedb");
		Runnable action = new Runnable() {
			public void run() {
				int result = JOptionPane.showConfirmDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.removedb.confirm"),
						getMessage("warning"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					DocumentManager.getInstance().remove(course);
					JOptionPane.showMessageDialog(
							TrackIt.getApplicationFrame(),
							getMessage("operation.success.delete",
									course.getDocumentItemName()));
				}
			}
		};

		Operation deleteOperation = new Operation(group, name, description,
				action);
		return deleteOperation;
	}

//	// 12335: 2016-06-05
//	private Operation createImportPhotographiesOperation(final Activity activity) {
//		return createImportPhotographiesOperation((DocumentItem) activity);
//	}
//	// 12335: 2016-06-05 end

	
//	private Operation createImportPhotographiesOperation(final Activity activity) { // 12335
	private Operation createImportPhotographiesOperation(final DocumentItem item) {
		String group = getMessage("operation.group.importpicture");
		String name = getMessage("operation.name.importpicture");
		String description = getMessage("operation.description.importpicture",
				"Activity");
		Runnable action = new Runnable() {
			public void run() {
				String action = getMessage("applicationPanel.button.import");
				JFileChooser fileChooser = new JFileChooser();

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
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Object execute() throws TrackItException {
						if ( item.isActivity() )
							((Activity)item).addPictures(files);
						else
							((Course)item).addPictures(files);
						updatePictureDirectory(files[0]);
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
