/*
 * This file is part of Track It!.
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
package com.pg58406.trackit.business.utility;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.io.File;

import javax.swing.JOptionPane;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.henriquemalheiro.trackit.presentation.utilities.OperationsFactory;
import com.pg58406.trackit.business.db.Database;

public class SaveTools {

	private static SaveTools saveTools;

	static {
		saveTools = new SaveTools();
	}

	public static SaveTools getInstance() {
		return saveTools;
	}

	public void saveAndExit() {// 58406
		Operation op = null;
		for (GPSDocument doc : DocumentManager.getInstance().getDocuments()) {
			for (Course course : doc.getCourses()) {
				if (course.getUnsavedChanges() || doc.getChanged()) {
					Object[] options = { "New File", "Yes", "No" };
					int n = JOptionPane
							.showOptionDialog(
									TrackIt.getApplicationFrame(),
									"The course "
											+ course.getName()
											+ " has unsaved changes. "
											+ "Would you like to save before exiting TrackIt?",
									"Save course" + course.getName() + "?",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options,
									options[2]);
					/*System.err.println("Course " + course.getName()
							+ " has unsaved changes");*/
					String filepath = course.getFilepath();
					if ((n == 1 && filepath == null)
							|| (n == 1 && doc.getActivities().size()
									+ doc.getCourses().size() > 1))
						n = 0;
					switch (n) {
					case 0:// new file
						if ((course.getParent().getCourses().size()
								+ course.getParent().getActivities().size() > 1)) {
							op = OperationsFactory.getInstance()
									.createFileExportOperation(doc);
						} else {
							op = OperationsFactory.getInstance()
									.createFileExportOperation(course);
						}
						op.actionPerformed(null);
						break;
					case 1:// yes
						File file = new File(filepath);
						if (file.exists() && !file.isDirectory()) {
							String[] split = filepath.split("\\.");
							String type = split[split.length - 1];
							switch (type) {
							case "csv":
								op = OperationsFactory.getInstance()
										.createSaveOperation(course,
												FileType.CSV);
								break;
							case "fit":
								op = OperationsFactory.getInstance()
										.createSaveOperation(course,
												FileType.FIT);
								break;
							case "fitlog":
								op = OperationsFactory.getInstance()
										.createSaveOperation(course,
												FileType.FITLOG);
								break;
							case "gpx":
								op = OperationsFactory.getInstance()
										.createSaveOperation(course,
												FileType.GPX);
								break;
							case "kml":
								op = OperationsFactory.getInstance()
										.createSaveOperation(course,
												FileType.KML);
								break;
							case "tcx":
								op = OperationsFactory.getInstance()
										.createSaveOperation(course,
												FileType.TCX);
								break;
							}
						} else {
							op = OperationsFactory.getInstance()
									.createFileExportOperation(course);
						}
						Database.getInstance().updateDB(course, file);
						op.actionPerformed(null);
						break;
					case 2:// no
						System.out.println("Exiting without saving "
								+ course.getName() + ".");
						break;
					}
					// op.actionPerformed(null);
					break;// Assim não repete várias vezes para o mesmo
							// documento
				}
			}
		}
		int option = JOptionPane.showOptionDialog(
				TrackIt.getApplicationFrame(),
				getMessage("applicationPanel.menu.exitConfirmationMessage"),
				getMessage("applicationPanel.menu.confirmation"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				null, null);

		if (option == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

}
