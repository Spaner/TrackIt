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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.utility.TrackItPreferences;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterFactory;
import com.henriquemalheiro.trackit.presentation.FileFilterFactory;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;
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

	public List<Operation> getSupportedOperations(GPSDocument document) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add(createDiscardOperation(document));
		operations.add(createRemoveFromDatabaseOperation(document));// 58406
		
//		operations.add(createFileExportOperation(document));  // 12335

		return operations;
	}

	public List<Operation> getSupportedOperations(Activity activity) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add(createImportPhotographiesOperation(activity));// 58406
		operations.add(createActivityToCourseOperation(activity));
		operations.add(createDeleteOperation(activity));
		operations.add(createRemoveFromDatabaseOperation(activity));// 58406
		operations.add(createFileExportOperation(activity));// 58406

		return operations;
	}

	public List<Operation> getSupportedOperations(Course course) {
		List<Operation> operations = new ArrayList<Operation>();

		operations.add(createImportPhotographiesOperation(course));// 58406
		operations.add(createRenameOperation(course));
		operations.add(createDeleteOperation(course));
		operations.add(createRemoveFromDatabaseOperation(course));// 58406
		operations.add(createFileExportOperation(course));// 58406

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

	private Operation createRenameOperation(final Course course) {
		String group = getMessage("operation.group.renameCourse");
		String name = getMessage("operation.name.renameCourse");
		String description = getMessage("operation.description.renameCourse");
		Runnable action = new Runnable() {
			public void run() {
				String newName = JOptionPane.showInputDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.renameCourse.courseName"),
						course.getName());
				DocumentManager.getInstance().renameCourse(course, newName);
			}
		};

		Operation activityToCourseOperation = new Operation(group, name,
				description, action);
		return activityToCourseOperation;
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

	private <T extends DocumentItem> Operation createFileExportOperation(
			final T item, final FileType fileType) {
		String group = getMessage("operation.group.fileExport");
		String name = getMessage("operation.name.fileExport",
				fileType.getDescription());
		String description = getMessage("operation.description.fileExport",
				item.getDocumentItemName(), fileType.getDescription());
		Runnable action = new Runnable() {
			public void run() {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);

				FileFilterFactory factory = FileFilterFactory.getInstance();
				fileChooser.addChoosableFileFilter(factory.getFilter(fileType));

				int returnValue = fileChooser.showDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.actionName.fileExport",
								fileType.getDescription()));

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					if (!selectedFile.getAbsolutePath().toLowerCase()
							.endsWith(fileType.getExtension().toLowerCase())) {
						selectedFile = new File(selectedFile.getAbsolutePath()
								+ "." + fileType.getExtension());
					}

					if (selectedFile.exists()) {
						int selectedOption = JOptionPane.showConfirmDialog(
								TrackIt.getApplicationFrame(),
								getMessage("operation.overwriteFile"),
								getMessage("operation.title.chooseOption"),
								JOptionPane.YES_NO_OPTION);

						if (selectedOption == JOptionPane.NO_OPTION) {
							return;
						}
					}

					String filename = selectedFile.getAbsolutePath();
					GPSDocument document = new GPSDocument(filename);
					document.setFileName(filename);

					if (item.isActivity()) {
						document.add((Activity) item);
					} else if (item.isCourse()) {
						document.add((Course) item);
					} else if (item instanceof GPSDocument) {
						document = (GPSDocument) item;
						document.setFileName(filename);
					}

					try {
						Map<String, Object> options = new HashMap<>();
						options.put(Constants.Writer.OUTPUT_DIR, selectedFile
								.getParentFile().getAbsolutePath());
						Writer writer = WriterFactory.getInstance().getWriter(
								selectedFile, options);
						writer.write(document);
					} catch (TrackItException e) {
						logger.error(e.getMessage());
						JOptionPane.showMessageDialog(
								TrackIt.getApplicationFrame(),
								getMessage("operation.failure.fileExport",
										item.getDocumentItemName()),
								getMessage("operation.failure"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					if(item.isActivity()) ((Activity)item).setFilepath(filename);

					if(item.isCourse()) ((Course)item).setFilepath(filename);

					JOptionPane.showMessageDialog(
							TrackIt.getApplicationFrame(),
							getMessage("operation.success.fileExport",
									item.getDocumentItemName()));
				}
			}
		};

		Operation fileExportOperation = new Operation(group, name, description,
				action);
		return fileExportOperation;
	}

	// 58406###################################################################################
	public <T extends DocumentItem> Operation createSaveOperation(
			final Course item, final FileType fileType) {
		String group = getMessage("operation.group.fileExport");// change this
		String name = getMessage("operation.name.fileExport",
				fileType.getDescription());// same as the above
		String description = getMessage("operation.description.fileExport",
				item.getDocumentItemName(), fileType.getDescription());

		Runnable action = new Runnable() {
			public void run() {
				GPSDocument document = null;
				GPSDocument doc = item.getParent();
				String filepath = item.getFilepath();
				if (filepath == null || filepath.isEmpty()) {
					// System.err
					// .println("IMPOSSIBLE TO SAVE! Course has no filepath information.");
					return;
				}
				File selectedFile = new File(filepath);
				String filename = selectedFile.getAbsolutePath();
				if (doc.getCourses().size() + doc.getActivities().size() > 1) {
					document = doc;
					document.setFileName(filename);
				} else {
					document = new GPSDocument(filename);
					document.setFileName(filename);
					document.add(item);
				}
				try {
					Map<String, Object> options = new HashMap<>();
					options.put(Constants.Writer.OUTPUT_DIR, selectedFile
							.getParentFile().getAbsolutePath());
					Writer writer = WriterFactory.getInstance().getWriter(
							selectedFile, options);
					writer.write(document);
				} catch (TrackItException e) {
					logger.error(e.getMessage());
					JOptionPane.showMessageDialog(
							TrackIt.getApplicationFrame(),
							getMessage("operation.failure.fileExport",
									item.getDocumentItemName()),
							getMessage("operation.failure"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				for (Activity a : item.getParent().getActivities()){
					a.setFilepath(filename);
				}

				for (Course c : item.getParent().getCourses()) {
					c.setFilepath(filename);
					c.setUnsavedFalse();
				}

				JOptionPane.showMessageDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.success.fileExport",
								item.getDocumentItemName()));
				item.setUnsavedFalse();
			}

		};

		Operation fileExportOperation = new Operation(group, name, description,
				action);
		return fileExportOperation;
	}

	public <T extends DocumentItem> Operation createFileExportOperation(
			final T item) {
		String group = getMessage("operation.group.fileExport");
		Runnable action = new Runnable() {
			public void run() {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);

				FileType[] supportedFileTypes = item.getSupportedFileTypes();
				FileFilterFactory factory = FileFilterFactory.getInstance();

				for (FileType type : supportedFileTypes) {
					fileChooser.addChoosableFileFilter(factory.getFilter(type));
				}

				int returnValue = fileChooser.showDialog(
						TrackIt.getApplicationFrame(),
						getMessage("operation.actionName.fileExport"));

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String filepath = selectedFile.getAbsolutePath();
					String[] split = filepath.split("\\.");
					String type = split[split.length - 1];
					FileType fileType = null;
					switch (type) {
					case "csv":
						fileType = FileType.CSV;
						break;
					case "fit":
						fileType = FileType.FIT;
						break;
					case "fitlog":
						fileType = FileType.FITLOG;
						break;
					case "gpx":
						fileType = FileType.GPX;
						break;
					case "kml":
						fileType = FileType.KML;
						break;
					case "tcx":
						fileType = FileType.TCX;
						break;
					default:
						fileType = findExtension(fileChooser.getFileFilter());
						break;
					}

					if (!selectedFile.getAbsolutePath().toLowerCase()
							.endsWith(fileType.getExtension().toLowerCase())) {
						selectedFile = new File(selectedFile.getAbsolutePath()
								+ "." + fileType.getExtension());
					}

					if (selectedFile.exists()) {
						int selectedOption = JOptionPane.showConfirmDialog(
								TrackIt.getApplicationFrame(),
								getMessage("operation.overwriteFile"),
								getMessage("operation.title.chooseOption"),
								JOptionPane.YES_NO_OPTION);

						if (selectedOption == JOptionPane.NO_OPTION) {
							return;
						}
					}
					try {
						String filename = selectedFile.getAbsolutePath();
						GPSDocument document = new GPSDocument(filename);
						document.setFileName(filename);

						if (item.isActivity()) {
							Activity a = (Activity) item;
							a.setFilepath(filename);
							document.add(a);
							Database.getInstance().updateDB((Activity) item,
									selectedFile);
						} else if (item.isCourse()) {
							Course c = (Course) item;
							c.setFilepath(filename);
							document.add(c);
							Database.getInstance().updateDB((Course) item,
									selectedFile);
							c.setUnsavedFalse();
						} else if (item instanceof GPSDocument) {
							document = (GPSDocument) item;
							for (Activity a : document.getActivities()) {
								a.setFilepath(filename);
								Database.getInstance().updateDB(a,
										selectedFile);
							}
							for (Course c : document.getCourses()) {
								c.setFilepath(filename);
								Database.getInstance().updateDB(c,
										selectedFile);
							}
							document.setFileName(filename);
							document.setChangedFalse();
						}

						Map<String, Object> options = new HashMap<>();
						options.put(Constants.Writer.OUTPUT_DIR, selectedFile
								.getParentFile().getAbsolutePath());
						Writer writer = WriterFactory.getInstance().getWriter(
								selectedFile, options);
						writer.write(document);
					} catch (TrackItException e) {
						logger.error(e.getMessage());
						JOptionPane.showMessageDialog(
								TrackIt.getApplicationFrame(),
								getMessage("operation.failure.fileExport",
										item.getDocumentItemName()),
								getMessage("operation.failure"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					JOptionPane.showMessageDialog(
							TrackIt.getApplicationFrame(),
							getMessage("operation.success.fileExport",
									item.getDocumentItemName()));
				}
			}
		};

		String name = getMessage("operation.name.fileExport");
		String description = getMessage("operation.description.fileExport",
				item.getDocumentItemName());
		Operation fileExportOperation = new Operation(group, name, description,
				action);
		return fileExportOperation;
	}

	private FileType findExtension(FileFilter filter) {
		FileType type = null;
		String description = filter.getDescription();
		if (description.contains(".csv"))
			type = FileType.CSV;
		if (description.contains(".fit"))
			type = FileType.FIT;
		if (description.contains(".fitlog"))
			type = FileType.FITLOG;
		if (description.contains(".gpx"))
			type = FileType.GPX;
		if (description.contains(".kml"))
			type = FileType.KML;
		if (description.contains(".tcx"))
			type = FileType.TCX;
		return type;
	}

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

	private Operation createImportPhotographiesOperation(final Activity activity) {
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
						for (File f : files) {
							activity.addPicture(f);
							updatePictureDirectory(f);
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

	private Operation createImportPhotographiesOperation(final Course course) {
		String group = getMessage("operation.group.importpicture");
		String name = getMessage("operation.name.importpicture");
		String description = getMessage("operation.description.importpicture",
				"Course");
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
						for (File f : files) {
							course.addPicture(f);
							updatePictureDirectory(f);
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
					for (Picture picture : pictures.getPictures()){
						picture.getContainer().removePicture(picture);
					}
				}
			}
		};

		Operation removeOperation = new Operation(group, name, description,
				action);
		return removeOperation;
	}
}
