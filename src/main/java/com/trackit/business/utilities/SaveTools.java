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
package com.trackit.business.utilities;

//import java.io.File;
import java.nio.file.Paths;

import static com.trackit.business.common.Messages.getMessage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.print.Doc;
import javax.swing.JOptionPane;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.Document;

import org.apache.derby.tools.sysinfo;

import com.drew.metadata.exif.GpsDescriptor;
import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.FileType;
import com.trackit.business.database.Database;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.Folder;
import com.trackit.business.domain.GPSDocument;
import com.trackit.presentation.utilities.CheckSaveDialog;
import com.trackit.presentation.utilities.Operation;
import com.trackit.presentation.utilities.OperationsFactory;

public class SaveTools {

	private static SaveTools saveTools;

	static {
		saveTools = new SaveTools();
	}

	public static SaveTools getInstance() {
		return saveTools;
	}
	
	// 12335: 2016-10-13 - new process + okToClose  ----- Start -----
	
	public void saveAndExit( boolean fromMenu, boolean runningOnMac) {
		if ( okToClose( fromMenu, runningOnMac) ) {
			// Process documents, updating any changed documents and the DB
			processDocumentsAtCloseTime();
			// Save Workspace and Library open items reference to the DB to reopen at start time
			Database.getInstance().saveWorkspaceAndLibraryStatus( 
									DocumentManager.getInstance().getDocuments());
			GPSDocument collection = DocumentManager.getInstance().getDefaultCollectionDocument();
			System.out.println( (collection!=null) ? collection.getName() : "Collection NOT FOUND");
			// Finally exit
			System.exit( 0);
		}
	}
	
	public boolean okToClose( boolean fromMenu, boolean runningOnMac) {
		// On Mac OS there is no way to stop from closing once the window destroy button is pressed
		String warn = (fromMenu? "MENU" :"DESTRUCTION") + "  " + 
					  (runningOnMac? "MAC" : "WINDOWS");
		System.out.println( "\nTHIS IS REALLY NEW SAVE TOOLS with fromMenu " + fromMenu 
				+ " running on a Mac " + runningOnMac + "\n");
				
		// Confirm that we really want to exit 
		// (Mac OSX: window destruction cannot be reversed)		
		if ( fromMenu || !runningOnMac ) {
//12335: 2018-07-01 - uses OperationConfirmationDialog.showOperationConfirmationDialog
//			int option = JOptionPane.showOptionDialog(
//					TrackIt.getApplicationFrame(),
//					getMessage("applicationPanel.menu.exitConfirmationMessage"),
//					getMessage("applicationPanel.menu.confirmation"),
//					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
//					null, null);
//			// Go back if not
//			if ( option != JOptionPane.YES_OPTION )
//				return false;
			if ( ! OperationConfirmationDialog.showOperationConfirmationDialog(
					getMessage("applicationPanel.menu.exitConfirmationMessage"),
					getMessage( "applicationPanel.menu.confirmation"))           )
				return false;
		}
		System.out.println( "CLOSING");
		return true;
	}
	// 12335: 2016-10-13 - new process + okToClose  -----  End  -----
	
	// 12335: 2016-10-12 - processDocumentsAtCloseTime ------- Start --------

	public void processDocumentsAtCloseTime() {
		DocumentManager        docManager  = DocumentManager.getInstance();
		Database               database    = Database.getInstance();
		List<GPSDocument>      documents   = docManager.getWorspaceFolder().getDocuments();
		
		ArrayList<GPSDocument> notRegistered = new ArrayList<>();
		ArrayList<GPSDocument> registered    = new ArrayList<>();
		ArrayList<GPSDocument> notToRegister = new ArrayList<>();
		Iterator<GPSDocument>  docIterator;
		
		// Check which documents are registered and which are not
		for( GPSDocument document: documents) {
			if ( database.doesDocumentExistInDB( document) )
				registered.add( document);
			else
				notRegistered.add( document);
		}
		
		// Filter unregistered documents to get:
		// a) Documents that are to be registered (and saved)           -> notRegistered
		// b) Documents that are not to be registered but were modified -> notToRegister
		
		// Ask whether to register unregistered documents
		if ( !notRegistered.isEmpty() ) {
			notToRegister = queryUser( notRegistered, true, "register unregistered docs?");
			for( GPSDocument doc: notToRegister)
				System.out.println( "\tnotToRegister: " + doc.getName());
			// After this, notRegistered holds documents to register (and save to file if modified)
			//             notToRegister holds documents that are not to be registered
			// Check which of the later have been modified
			docIterator = notToRegister.iterator();
			while( docIterator.hasNext() ) {
				if ( !(docIterator.next().needsToBeSavedToFile()) )
					docIterator.remove();
			}
			// Now notToRegister holds all unregistered documents that were modified
			// and may need save to file (upon user selection)
			
			// Ask now whether to save them
			if ( !notToRegister.isEmpty() )
				queryUser( notToRegister, false, "save docs to file?");
		}
		// Result: notRegistered holds all unregistered documents that will be saved and registered
		//         notToRegister holds all unregistered modified documents that will only be saved
		
		// Filter registered documents keeping only those that need saving
		
		if ( !registered.isEmpty() ) {
			docIterator = registered.iterator();
			while ( docIterator.hasNext()) {
				GPSDocument document = (GPSDocument)  docIterator.next();
				if ( !document.needsToBeSavedToFile() && !document.hasMediaChanges() &&
					 !document.hasChangedDBOnlyData() )
					docIterator.remove();
			}
			// 'registered' holds now all registered documents that were modified

			// Ask whether to save
			if ( !registered.isEmpty() )
				queryUser( registered, false, "save modified docs?");
		}
		// Result: 'registered' holds all modified registered documents to be saved
		
		System.out.println( "# registered documents to save:                " + registered.size());
		for (GPSDocument document : registered)
			System.out.println( "\t" + document.getName());	
		System.out.println( "# unregistered documents to register and save: " + notRegistered.size());
		for (GPSDocument document : notRegistered)
			System.out.println( "\t" + document.getName());	
		System.out.println( "# unregistered documents to save only:         " + notToRegister.size());
		for (GPSDocument document : notToRegister)
			System.out.println( "\t" + document.getName());
		
		OperationsFactory factory = OperationsFactory.getInstance();
		// Registered modified documents - update file and registration
		for( GPSDocument document: registered) {
			System.out.println( "Updating document: " + document.getName());
			factory.createSaveDocumentOperation( document, false).actionPerformed( null);;
		}
		// Unregistered documents - register and file update
		for( GPSDocument document: notRegistered) {
			System.out.println( "Registering and updating file: " + document.getName());
			factory.createSaveDocumentOperation( document, false).actionPerformed( null);;
		}
		// Unregistered not to register documents: update file only
		for( GPSDocument document: notToRegister) {
			System.out.println( "Updating only file: " + document.getName());
			factory.createSaveDocumentOperation( document, true).actionPerformed( null);;
		}
	}
	// 12335: 2016-10-12 - processDocumentsAtCloseTime -------  End  --------
	
	private ArrayList<GPSDocument> queryUser( ArrayList<GPSDocument> candidates, boolean registration,
											  String traceLabel) {
		if ( !traceLabel.isEmpty() )
			System.out.println( "Querying at phase " + traceLabel);
		ArrayList<GPSDocument> rejected = new ArrayList<>();
		Iterator<GPSDocument> docIterator = candidates.iterator();
		while( docIterator.hasNext() ) {
			GPSDocument document = docIterator.next();//			System.out.println( "\t\t" + document.getName() + " ");
			int answer;
//			12335: 2018-02-25: using Utilities to control file name and path widths
			String documentName = StringUtilities.wrapString( document.getName(), 64, "<br>");
			String documentPath = StringUtilities.breakPathString( document.getFileName(), 64, "<br>");
			if ( registration )
				answer = CheckSaveDialog.showRegisterConfirmDialog(
//		12335: 2018-02-25 - dialogue box is now under control
//							document.getName(),
//							((Path)Paths.get( document.getFileName())).getName(0).toString(), 
							documentName, documentPath,
							docIterator.hasNext());
			else
				answer = CheckSaveDialog.showSaveConfirmDialog(
//		12335: 2018-02-25 - dialogue box width is now under control
//							document.getName(), 
//							((Path)Paths.get(document.getFileName())).getFileName().toString(),
							documentName, documentPath,
							docIterator.hasNext());
			if ( answer == CheckSaveDialog.YES ) {
					if ( CheckSaveDialog.getIsForall() )
						break;
				}
				else {
					docIterator.remove();
					rejected.add( document);
					if ( CheckSaveDialog.getIsForall() )
						while( docIterator.hasNext() ) {
							rejected.add( docIterator.next());
							docIterator.remove();
						}
				}
		}
		return rejected;
	}	

}
