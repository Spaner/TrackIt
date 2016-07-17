/*
 * This file is part of Track It!.
 * Copyright (C) 2015, 2016 João Brisson
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
package com.jb12335.trackit.business.utilities;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

//import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.Document;

import com.drew.metadata.exif.GpsDescriptor;
import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Folder;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.henriquemalheiro.trackit.presentation.utilities.OperationsFactory;
import com.jb12335.trackit.presentation.utilities.CheckSaveDialog;
import com.jb12335.trackit.presentation.utilities.SportSelectionDialog;
import com.pg58406.trackit.business.db.Database;

public class SaveTools {

	private static SaveTools saveTools;

	static {
		saveTools = new SaveTools();
	}

	public static SaveTools getInstance() {
		return saveTools;
	}
	
	// #####################    12335: 2015-09-22 Start  ############################## 

	public void saveAndExit( boolean fromMenu, boolean runningOnMac) {// 58406
		// 12335: 2016-06-09: Added fromMenu argument to separate window closing from
		//                    application closing from the menu
		
		System.out.println( "\nTHIS IS NEW SAVE TOOLS with fromMenu " + fromMenu 
				+ " running on a Mac " + runningOnMac + "\n");
		
		String warn = (fromMenu? "MENU" :"DESTRUCTION") + "  " + 
		(runningOnMac? "MAC" : "WINDOWS");
		
		// 12335: 2016-06-09: Confirm that we really want to exit before doing anything else
		//                    (Mac OSX: window destruction cannot be reversed)		
		if ( fromMenu || !runningOnMac ) {
			int option = JOptionPane.showOptionDialog(
					TrackIt.getApplicationFrame(),
					getMessage("applicationPanel.menu.exitConfirmationMessage"),
					getMessage("applicationPanel.menu.confirmation"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					null, null);
			// Go back if not
			if ( option != JOptionPane.YES_OPTION )
				return;
		}
		
		List<GPSDocument> documents = DocumentManager.getInstance()
															.getWorspaceFolder().getDocuments();
		Database database = Database.getInstance();
		
		//12335: 2016-06-11: Process all documents with changes
		//                   Note: a changed document may not be registered
		// Get documents with changes
		ArrayList<GPSDocument> needSave     = new ArrayList<>();
		ArrayList<GPSDocument> needNoSave   = new ArrayList<>();
		ArrayList<GPSDocument> toRegister   = new ArrayList<>();
		ArrayList<GPSDocument> needRegister = new ArrayList<>();
		for( GPSDocument doc : documents) {
			if ( doc.needsToBeSavedToFile() )
				needSave.add( doc);
			else
				needNoSave.add( doc);
		}
		// Ask whether to save
		if ( needSave.size() > 0 ) {
			Iterator<GPSDocument> docIterator = needSave.iterator();
			int noRemainingToCheck = needSave.size();
			while ( docIterator.hasNext() ) {
				GPSDocument document = docIterator.next();
				int result = CheckSaveDialog.showSaveConfirmDialog(
						     	document.getName(),
						     	((Path)Paths.get(document.getFileName())).getFileName().toString(),
						     	(noRemainingToCheck>1));
				if ( result == CheckSaveDialog.NO ) {
					docIterator.remove();
					if ( CheckSaveDialog.getIsForall() )
						while ( docIterator.hasNext() ) {
							docIterator.next();
							docIterator.remove();
						}
				}
				else {
					if ( CheckSaveDialog.getIsForall() )
						break;
				}
					
			}
		}
		// Save those remaining on the list
		for( GPSDocument doc : needSave ) {
			System.out.println( "Would have saved: " + doc.getFileName());
//			Operation op = OperationsFactory.getInstance().createSaveDocumentOperation( doc);
//			op.actionPerformed(null);
		}
		// Changed documents that were saved must ask whether to add/modify register
		needRegister.addAll( needSave);
		
		//12335: 2016-06-11: Process unregistered documents
		//Get unregistered documents
		for( GPSDocument doc: needNoSave ) {
			if ( ! database.doesDocumentExistInDB( doc) ) 
				needRegister.add( doc);	
			else { // document is registered but sport/subsport may have changed
				if ( doc.changedSportOrSubSport() )
					toRegister.add( doc);
			}
		}
		// Ask whether to register
		if ( needRegister.size() > 0 ) {
			// Check whether to register
			Iterator<GPSDocument> docIterator = needRegister.iterator();
			while (docIterator.hasNext()) {
				GPSDocument gpsDocument = (GPSDocument) docIterator.next();
				int answer = CheckSaveDialog.showRegisterConfirmDialog(
						gpsDocument.getName(), 
						((Path)Paths.get(gpsDocument.getFileName())).getName(0).toString(),
						docIterator.hasNext());
				if ( answer == CheckSaveDialog.YES) {
					if ( CheckSaveDialog.getIsForall() )
						break;
				}
				else {
					docIterator.remove();
					if ( CheckSaveDialog.getIsForall() )
						while( docIterator.hasNext() ) {
							docIterator.next();
							docIterator.remove();
						}
				}
			}
			// Add those with mandatory register update/add
			needRegister.addAll( toRegister);
			// Register (if any remaining)
			for( GPSDocument document : needRegister ) {
				System.out.println( "Would register: " + document.getFileName());
				database.updateDB( document, true);
			}
		}
		
//		Operation op = null;
//		for (GPSDocument doc : DocumentManager.getInstance().getDocuments()) {
//			for (Course course : doc.getCourses()) {
//				if (course.getUnsavedChanges() || doc.getChanged()) {
//					Object[] options = { "New File", "Yes", "No" };
//					int n = JOptionPane
//							.showOptionDialog(
//									TrackIt.getApplicationFrame(),
//									"The course "
//											+ course.getName()
//											+ " has unsaved changes. "
//											+ "Would you like to save before exiting TrackIt?",
//									"Save course" + course.getName() + "?" + warn,
//									JOptionPane.YES_NO_CANCEL_OPTION,
//									JOptionPane.WARNING_MESSAGE, null, options,
//									options[2]);
//					/*System.err.println("Course " + course.getName()
//							+ " has unsaved changes");*/
//					String filepath = course.getFilepath();
//					if ((n == 1 && filepath == null)
//							|| (n == 1 && doc.getActivities().size()
//									+ doc.getCourses().size() > 1))
//						n = 0;
//					switch (n) {
//					case 0:// new file
//						if ((course.getParent().getCourses().size()
//								+ course.getParent().getActivities().size() > 1)) {
//							op = OperationsFactory.getInstance()
//									.createFileExportOperation(doc);
//						} else {
//							op = OperationsFactory.getInstance()
//									.createFileExportOperation(course);
//						}
//						op.actionPerformed(null);
//						break;
//					case 1:// yes
//						File file = new File(filepath);
//						if (file.exists() && !file.isDirectory()) {
//							String[] split = filepath.split("\\.");
//							String type = split[split.length - 1];
//							switch (type) {
//							case "csv":
//								op = OperationsFactory.getInstance()
//										.createSaveOperation(course,
//												FileType.CSV);
//								break;
//							case "fit":
//								op = OperationsFactory.getInstance()
//										.createSaveOperation(course,
//												FileType.FIT);
//								break;
//							case "fitlog":
//								op = OperationsFactory.getInstance()
//										.createSaveOperation(course,
//												FileType.FITLOG);
//								break;
//							case "gpx":
//								op = OperationsFactory.getInstance()
//										.createSaveOperation(course,
//												FileType.GPX);
//								break;
//							case "kml":
//								op = OperationsFactory.getInstance()
//										.createSaveOperation(course,
//												FileType.KML);
//								break;
//							case "tcx":
//								op = OperationsFactory.getInstance()
//										.createSaveOperation(course,
//												FileType.TCX);
//								break;
//							}
//						} else {
//							op = OperationsFactory.getInstance()
//									.createFileExportOperation(course);
//						}
//						Database.getInstance().updateDB(course, file);
//						op.actionPerformed(null);
//						break;
//					case 2:// no
//						System.out.println("Exiting without saving "
//								+ course.getName() + ".");
//						break;
//					}
//					// op.actionPerformed(null);
//					break;// Assim não repete várias vezes para o mesmo
//							// documento
//				}
//			}
//		}
		
		// Check out all documents in the workspace for changes
		checkoutDocuments( DocumentManager.getInstance().
				                              getFolder("Workspace").getDocuments());
//		DocumentManager.getInstance().getFolder( "Workspace");

		// Save the list of open documents to the DB
		Database.getInstance().closeDatabase( DocumentManager.getInstance().getDocuments());
		
		// We can finally exit
		System.exit( 0);
	}
	
	private void checkoutDocuments( List<GPSDocument> documents) {
		for( GPSDocument document : documents ) {
			System.out.println( "Checking out " + document.getFileName());
		}
		
//		int n;
//		GPSDocument doc = documents.get(0);
//		List<Activity> acts = doc.getActivities();
//		if (acts.size() > 0 )
//			n = SportSelectionDialog.showSportSelectionDialog( acts.get(0), true);
//		else {
//			List<Course> crs = doc.getCourses();
//			n = SportSelectionDialog.showSportSelectionDialog( crs.get(0), true);
//		}
////		int n;
//		n = CheckSaveDialog.showSaveConfirmDialog( "TESTE", true);
//		System.out.println( "Resultado: " + n + "   para todos: " +  CheckSaveDialog.getIsForall());
//		n = CheckSaveDialog.showSaveConfirmDialog( "NOT FOR ALL OF THEM", false);
//		System.out.println( "Resultado: " + n + "   para todos: " +  CheckSaveDialog.getIsForall());
//		n = CheckSaveDialog.showRegisterConfirmDialog( "TO REGISTER", false);
//		System.out.println( "Resultado: " + n + "   para todos: " +  CheckSaveDialog.getIsForall());
//		n = CheckSaveDialog.showRegisterConfirmDialog( "UNREGISTERED", true);
//		System.out.println( "Resultado: " + n + "   para todos: " +  CheckSaveDialog.getIsForall());
	}

}
