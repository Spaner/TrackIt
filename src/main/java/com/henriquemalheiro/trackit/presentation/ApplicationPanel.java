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
package com.henriquemalheiro.trackit.presentation;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.OperatingSystem;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.AltitudeSmoothing;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.business.utility.TrackItPreferences;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterFactory;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventListener;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;
import com.henriquemalheiro.trackit.presentation.view.browse.BrowseView;
import com.henriquemalheiro.trackit.presentation.view.chart.ChartView;
import com.henriquemalheiro.trackit.presentation.view.data.DataView;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderView;
import com.henriquemalheiro.trackit.presentation.view.log.LogView;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.henriquemalheiro.trackit.presentation.view.summary.SummaryView;
import com.pg58406.trackit.business.operation.PauseDetectionOperation;
import com.pg58406.trackit.business.operation.PauseDetectionPicCaseOperation;
import com.pg58406.trackit.business.utility.PhotoPlacer;
import com.pg58406.trackit.business.utility.PhotoPlacerBreadthFirst;
import com.pg58406.trackit.business.utility.SaveTools;

public class ApplicationPanel extends JPanel implements EventPublisher,
		EventListener {
	private static final long serialVersionUID = 1L;

	private JFrame application;
	private ApplicationMenu applicationMenu;
	private ApplicationPanelEventHandler handler;

	private JComponent rightPane;
	private JSplitPane horizontalContentSplit;
	private JSplitPane verticalContentSplit;
	private JTabbedPane upperTabbedPane;
	private JTabbedPane lowerTabbedPane;

	private FolderView folderView;
	private SummaryView summaryView;
	private BrowseView browseView;
	private MapView mapView;
	private ChartView chartView;
	private DataView dataView;
	private LogView logView;

	private List<DocumentItem> selectedItems;

	private static Logger logger = Logger.getLogger(ApplicationPanel.class
			.getName());

	public ApplicationPanel(JFrame application) {
		this.application = application;
		initComponents();
	}

	private void initComponents() {
		Image applicationImage = Toolkit.getDefaultToolkit().getImage(
				ApplicationPanel.class.getResource("/icons/trackit.png"));

		if (OperatingSystem.isMac()) {
			Application application = Application.getApplication();
			application.setPreferencesHandler(new TrackitPreferencesHandler());
			application.setDockIconImage(applicationImage);
		}

		application.setIconImage(applicationImage);

		setLayout(new BorderLayout());

		handler = new ApplicationPanelEventHandler();
		applicationMenu = new ApplicationMenu(handler);
		application.setJMenuBar(applicationMenu.getMenu());

		EventManager eventManager = EventManager.getInstance();

		folderView = new FolderView();
		folderView.setPreferredSize(new Dimension(250, 450));
		folderView.setMinimumSize(new Dimension(250, 200));
		eventManager.register(folderView);

		summaryView = new SummaryView();
		summaryView.setPreferredSize(new Dimension(250, 250));
		summaryView.setMinimumSize(new Dimension(300, 100));
		eventManager.register(summaryView);

		browseView = new BrowseView();
		browseView.setPreferredSize(new Dimension(400, 450));
		browseView.setMinimumSize(new Dimension(300, 200));

		mapView = new MapView();
		mapView.setPreferredSize(new Dimension(600, 450));
		mapView.setMinimumSize(new Dimension(300, 200));
		eventManager.register(mapView);

		chartView = new ChartView();
		chartView.setPreferredSize(new Dimension(1100, 300));
		chartView.setMinimumSize(new Dimension(100, 50));
		eventManager.register(chartView);

		dataView = new DataView();
		dataView.setPreferredSize(new Dimension(1100, 300));
		dataView.setMinimumSize(new Dimension(100, 50));
		eventManager.register(dataView);

		logView = new LogView();
		logView.setPreferredSize(new Dimension(1100, 300));
		logView.setMinimumSize(new Dimension(100, 50));

		layoutComponents();

		eventManager.register(this);

		selectedItems = new ArrayList<DocumentItem>();

	}

	private void layoutComponents() {
		boolean showFolder = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_FOLDER, true);
		boolean showSummary = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_SUMMARY, true);
		boolean showMap = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_MAP, true);
		boolean showChart = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_CHART, true);
		boolean showData = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_DATA, true);
		boolean showLog = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_LOG, true);

		lowerTabbedPane = null;
		if (showChart || showData || showLog) {
			lowerTabbedPane = new JTabbedPane();

			if (showChart) {
				lowerTabbedPane.addTab(
						getMessage("applicationPanel.view.chartTitle"),
						chartView);
			}

			if (showData) {
				lowerTabbedPane
						.addTab(getMessage("applicationPanel.view.dataTitle"),
								dataView);
			}

			if (showLog) {
				lowerTabbedPane.addTab(
						getMessage("applicationPanel.view.logTitle"), logView);
			}
		}

		JComponent upperPane = null;
		if (showMap && upperTabbedPane != null) {
			horizontalContentSplit = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT, upperTabbedPane, mapView);
			horizontalContentSplit.setOneTouchExpandable(false);
			upperPane = horizontalContentSplit;
		} else if (showMap) {
			upperPane = mapView;
		} else if (upperTabbedPane != null) {
			upperPane = upperTabbedPane;
		}

		rightPane = null;
		if (upperPane != null && lowerTabbedPane != null) {
			verticalContentSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					upperPane, lowerTabbedPane);
			verticalContentSplit.setOneTouchExpandable(false);
			verticalContentSplit.setResizeWeight(0.60);
			rightPane = verticalContentSplit;
		} else if (upperPane != null) {
			rightPane = upperPane;
		} else if (lowerTabbedPane != null) {
			rightPane = lowerTabbedPane;
		}

		JComponent leftPane = null;
		if (showFolder && showSummary) {
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					folderView, summaryView);
			splitPane.setOneTouchExpandable(false);
			splitPane.setResizeWeight(0.60);
			splitPane.setAutoscrolls(true);
			leftPane = splitPane;
		} else if (showFolder) {
			leftPane = folderView;
		} else if (showSummary) {
			leftPane = summaryView;
		}

		JComponent content = null;
		if (leftPane != null && rightPane != null) {
			horizontalContentSplit = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
			horizontalContentSplit.setOneTouchExpandable(false);
			content = horizontalContentSplit;
		} else if (leftPane != null) {
			content = leftPane;
		} else if (rightPane != null) {
			content = rightPane;
		} else {
			content = new JPanel();
		}

		Component centerComponent = ((BorderLayout) getLayout())
				.getLayoutComponent(BorderLayout.CENTER);
		if (centerComponent != null) {
			remove(centerComponent);
		}

		add(content, BorderLayout.CENTER);
		revalidate();
	}

	public void updateApplicationMenu() {
		applicationMenu = new ApplicationMenu(handler);
		application.setJMenuBar(applicationMenu.getMenu());
	}

	private class ApplicationPanelEventHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String actionCommand = e.getActionCommand();
			MenuActionType menuAction = MenuActionType.lookup(actionCommand);

			try {
				switch (menuAction) {
				case IMPORT_DOCUMENT:
					importDocument();
					break;
				case EXPORT_DOCUMENT:// 58406
					exportDocument();
					break;
				case IMPORT_PICTURE:// 58406
					importPicture();
					break;
				case NEW_DOCUMENT:
					newDocument();
					break;
				case OPEN_DOCUMENT:
					openDocument();
					break;
				case RESTART:
					restart();
					break;
				case EXIT:
					exitApplication();
					break;
				case NEW_COURSE:
					newCourse();
					break;
				case SPLIT_AT_SELECTED:
					splitAtSelected();
					break;
				case JOIN:
					join();
					break;
				case REVERSE:
					reverse();
					break;
				case RETURN:
					returnCourse();
					break;
				case SHOW_FOLDER:
					showFolder(e);
					break;
				case SHOW_SUMMARY:
					showSummary(e);
					break;
				case SHOW_MAP:
					showMap(e);
					break;
				case SHOW_CHART:
					showChart(e);
					break;
				case SHOW_TABLE:
					showTable(e);
					break;
				case SHOW_LOG:
					showLog(e);
					break;
				case DETECT_CLIMBS_AND_DESCENTS:
					detectClimbsAndDescents();
					break;
				case MARKING:
					mark();
					break;
				case SET_PACE:
					setPace();
					break;
				case CONSOLIDATION:
					consolidate();
					break;
				case SIMPLIFICATION:
					simplify();
					break;
				case ALTITUDE_SMOOTHING:
					altitudeSmoothing();
					break;
				case MENU_PREFERENCES:
					showPreferencesDialog();
					break;
				// 58406################################################
				case DETECT_PAUSES:
					detectPauses();
					break;
				case AUTO_LOCATE_PICTURES:
					autoLocatePictures();
					break;
				// #####################################################
				case REMOVE_PAUSES:
					removePauses();
					break;
				// 57421--------------------------------------------------
				case UNDO:
					undo();
					break;
				case REDO:
					redo();
					break;
				case COPY:
					copy();
					break;
				// -------------------------------------------------------
				default:
					// Do nothing
				}
			} catch (TrackItException tie) {
				showErrorMessage(tie.getMessage());
			}
		}
	}

	private void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), message,
				Messages.getMessage("applicationPanel.title.error"),
				JOptionPane.ERROR_MESSAGE);
	}

	private void altitudeSmoothing() {
		try {
			new AltitudeSmoothing().process(DocumentManager.getInstance()
					.getSelectedDocument());
		} catch (TrackItException e) {
			logger.error(e.getMessage());
			showErrorMessage(Messages
					.getMessage("applicationPanel.error.altitudeSmoothing"));
		}
	}

	private void detectClimbsAndDescents() {
		final DocumentItem selectedItem = selectedItems.get(0);
		new Task(new Action() {
			@Override
			public String getMessage() {
				return Messages.getMessage(
						"applicationPanel.message.detectClimbsDescents",
						selectedItem.getDocumentItemName());
			}

			@Override
			public Object execute() throws TrackItException {
				return DocumentManager.getInstance().detectClimbsDescents(
						selectedItem, new HashMap<String, Object>());
			}

			@Override
			public void done(Object result) {
				((DocumentItem) result).publishUpdateEvent(null);
				JOptionPane.showMessageDialog(
						TrackIt.getApplicationFrame(),
						Messages.getMessage("applicationPanel.message.detectClimbsDescentsSuccess"));
			}
		}).execute();
	}

	private void showLog(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_LOG, chkMenuItem.getState());

		if (lowerTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				lowerTabbedPane.addTab(
						getMessage("applicationPanel.view.logTitle"), logView);
				lowerTabbedPane.setSelectedComponent(logView);
				return;
			} else if (chkMenuItem.getState() == false
					&& lowerTabbedPane.getComponentCount() > 1) {
				lowerTabbedPane.remove(logView);
				return;
			}
		}
		layoutComponents();
	}

	private void showTable(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_DATA, chkMenuItem.getState());

		if (lowerTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				lowerTabbedPane
						.addTab(getMessage("applicationPanel.view.dataTitle"),
								dataView);
				lowerTabbedPane.setSelectedComponent(dataView);
				return;
			} else if (chkMenuItem.getState() == false
					&& lowerTabbedPane.getComponentCount() > 1) {
				lowerTabbedPane.remove(dataView);
				return;
			}
		}
		layoutComponents();
	}

	private void showChart(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_CHART, chkMenuItem.getState());

		if (lowerTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				lowerTabbedPane.addTab(
						getMessage("applicationPanel.view.chartTitle"),
						chartView);
				lowerTabbedPane.setSelectedComponent(chartView);
				return;
			} else if (chkMenuItem.getState() == false
					&& lowerTabbedPane.getComponentCount() > 1) {
				lowerTabbedPane.remove(chartView);
				return;
			}
		}
		layoutComponents();
	}

	private void showMap(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_MAP, chkMenuItem.getState());
		layoutComponents();
	}

	private void showSummary(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_SUMMARY,
				chkMenuItem.getState());

		if (upperTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				upperTabbedPane.addTab(
						getMessage("applicationPanel.view.summaryTitle"),
						summaryView);
				upperTabbedPane.setSelectedComponent(summaryView);
				return;
			} else if (chkMenuItem.getState() == false
					&& upperTabbedPane.getComponentCount() > 1) {
				upperTabbedPane.remove(summaryView);
				return;
			}
		}
		layoutComponents();
	}

	private void showFolder(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences()
				.setPreference(Constants.PrefsCategories.GLOBAL, null,
						Constants.GlobalPreferences.SHOW_FOLDER,
						chkMenuItem.getState());
		layoutComponents();
	}

	private void exitApplication() {// 58406
		SaveTools.getInstance().saveAndExit();
	}

	private void openDocument() {
		String action = getMessage("applicationPanel.button.open");
		final JFileChooser fileChooser = getFileChooser(action);
		fileChooser.setMultiSelectionEnabled(false);

		int result = fileChooser.showDialog(application, action);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			updateInitialDirectory(file);
			DocumentManager.getInstance().openDocument(file);
		}
	}

	private void importDocument() {
		String action = getMessage("applicationPanel.button.import");
		final JFileChooser fileChooser = getFileChooser(action);

		int result = fileChooser.showDialog(application, action);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			if (files.length > 0) {
				updateInitialDirectory(files[0]);
			}
			DocumentManager.getInstance().importDocuments(files);
		}
	}

	private void newDocument() {
		DocumentManager documentManager = DocumentManager.getInstance();
		GPSDocument document = documentManager
				.createDocument(DocumentManager.FOLDER_WORKSPACE);
		EventManager.getInstance()
				.publish(this, Event.DOCUMENT_ADDED, document);
	}

	private JFileChooser getFileChooser(String action) {
		JFileChooser fileChooser = new JFileChooser(action);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		setFileFilters(fileChooser);
		setInitialDirectory(fileChooser);

		return fileChooser;
	}

	private void updateInitialDirectory(File file) {
		TrackIt.getPreferences().setPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.LAST_IMPORT_DIRECTORY,
				file.getParent());
	}

	private void setFileFilters(final JFileChooser fileChooser) {
		FileFilter[] filters = FileFilterFactory.getInstance().getFilters();
		for (FileFilter fileFilter : filters) {
			fileChooser.addChoosableFileFilter(fileFilter);
		}
		fileChooser.setFileFilter(filters[filters.length - 1]);
		fileChooser.setAcceptAllFileFilterUsed(false);
	}

	private void setInitialDirectory(final JFileChooser fileChooser) {
		TrackItPreferences prefs = TrackIt.getPreferences();
		String initialDirectory = prefs.getPreference(
				Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.LAST_IMPORT_DIRECTORY,
				System.getProperty("user.home"));
		File currentDirectory = new File(initialDirectory);
		fileChooser.setCurrentDirectory(currentDirectory);
	}

	private class TrackitPreferencesHandler implements PreferencesHandler {
		public void handlePreferences(PreferencesEvent event) {
			showPreferencesDialog();
		}
	}

	private void showPreferencesDialog() {
		PreferencesDialog preferencesDialog = new PreferencesDialog(
				TrackIt.getApplicationFrame());
		preferencesDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		preferencesDialog.pack();
		preferencesDialog.setMinimumSize(new Dimension(644, 368));
		preferencesDialog.setPreferredSize(new Dimension(644, 368));
		preferencesDialog.setVisible(true);
	}

	private void newCourse() {
		DocumentManager documentManager = DocumentManager.getInstance();
		GPSDocument document = documentManager.getSelectedDocument();
		documentManager.newCourse(document);
	}

	private void splitAtSelected() {
		DocumentManager documentManager = DocumentManager.getInstance();

		Trackpoint trackpoint = (Trackpoint) selectedItems.get(0);
		Course course = (Course) trackpoint.getParent();

		documentManager.splitAtSelected(course, trackpoint);
	}

	private void join() throws TrackItException {
		List<Course> courses = new ArrayList<Course>();
		GPSDocument masterDocument = null;

		for (DocumentItem item : selectedItems) {
			if (!item.isCourse()) {
				throw new TrackItException(
						Messages.getMessage("applicationPanel.error.joinOnlyCourses"));
			}

			GPSDocument itemDocument = (GPSDocument) item.getParent();
			masterDocument = (masterDocument == null ? itemDocument
					: masterDocument);

			if (!itemDocument.equals(masterDocument)) {
				throw new TrackItException(
						Messages.getMessage("applicationPanel.error.joinCoursesDifferentDocuments"));
			}

			courses.add((Course) item);
		}

		JDialog joinDialog = new JoinDialog(courses);
		joinDialog.setVisible(true);
	}
	

	private void returnCourse() {
		Object[] options = { "Yes", "No", "Cancel" };
		int option = JOptionPane.showConfirmDialog(
				TrackIt.getApplicationFrame(),
				Messages.getMessage("applicationPanel.reverse.effects"),
				Messages.getMessage("applicationPanel.title.warning"),
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {

			int returnCourseOption = JOptionPane.showOptionDialog(
					TrackIt.getApplicationFrame(),
					Messages.getMessage("applicationPanel.reverse.wayBack"),
					Messages.getMessage("applicationPanel.title.wayBackTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
			
			if (returnCourseOption == JOptionPane.NO_OPTION) {

				DocumentManager documentManager = DocumentManager.getInstance();
				Course course = (Course) selectedItems.get(0);
				documentManager.reverse(course, Constants.ReverseOperation.RETURN);
			}
			if (returnCourseOption == JOptionPane.YES_OPTION) {

				DocumentManager documentManager = DocumentManager.getInstance();
				Course course = (Course) selectedItems.get(0);
				documentManager.reverse(course, Constants.ReverseOperation.NEWRETURN);
			}
		}
		


	}

	private void reverse() {

		int option = JOptionPane.showConfirmDialog(
				TrackIt.getApplicationFrame(),
				Messages.getMessage("applicationPanel.reverse.effects"),
				Messages.getMessage("applicationPanel.title.warning"),
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		if (option == JOptionPane.YES_OPTION) {
			DocumentManager documentManager = DocumentManager.getInstance();
			Course course = (Course) selectedItems.get(0);
			documentManager.reverse(course, Constants.ReverseOperation.NORMAL);


		}
	}

	private void undo() {
		DocumentManager documentManager = DocumentManager.getInstance();
		documentManager.undo();
	}

	private void redo() {
		DocumentManager documentManager = DocumentManager.getInstance();
		documentManager.redo();
	}

	private void copy() {
		DocumentManager documentManager = DocumentManager.getInstance();
		Course course = (Course) selectedItems.get(0);
		documentManager.copy(course);
	}

	private void setPace() {
		JDialog setPaceDialog = new SetPaceDialog((Course) selectedItems.get(0));
		setPaceDialog.setVisible(true);
	}

	private void consolidate() {
		JDialog consolidationDialog = new ConsolidationDialog(
				selectedItems.get(0));
		consolidationDialog.setVisible(true);
	}

	private void simplify() {
		JDialog simplificationDialog = new SimplificationDialog(
				selectedItems.get(0));
		simplificationDialog.setVisible(true);
	}

	private void removePauses() {
		if (selectedItems.size() != 1 || !selectedItems.get(0).isCourse()) {
			JOptionPane
					.showMessageDialog(
							TrackIt.getApplicationFrame(),
							Messages.getMessage("applicationPanel.message.removePausesCoursesOnly"),
							"", JOptionPane.WARNING_MESSAGE);
			return;
		}
		DocumentManager.getInstance().removePauses(
				(Course) selectedItems.get(0));
	}

	private void mark() {
		JDialog markDialog = new MarkDialog(selectedItems.get(0));
		markDialog.setVisible(true);
	}

	/* Event Listener interface implementation */

	@Override
	public void process(Event event, DocumentItem item) {
		switch (event) {
		case FOLDER_SELECTED:
		case ACTIVITY_SELECTED:
		case COURSE_SELECTED:
			selectedItems.clear();
			selectedItems.add(item);
			processItemSelected(item);
			break;
		case TRACKPOINT_SELECTED:
			selectedItems.add(0, item);
			processItemSelected(item);
			break;
		case SESSION_SELECTED:
		case LAP_SELECTED:
		case LENGTH_SELECTED:
		case TRACK_SELECTED:
		case SEGMENT_SELECTED:
		case COURSE_POINT_SELECTED:
		case EVENT_SELECTED:
		case DEVICE_SELECTED:
			selectedItems.clear();
			selectedItems.add(item);
			processItemSelected(item);
			break;
		case DOCUMENT_ADDED:
		case DOCUMENT_SELECTED:
		case DOCUMENT_UPDATED:
			selectedItems.clear();
			selectedItems.add(item);
			processItemSelected(item);
			break;
		default:
			// Do nothing
		}
	}

	private void processItemSelected(DocumentItem item) {
		DocumentManager documentManager = DocumentManager.getInstance();
		applicationMenu.refreshMenu(Arrays.asList(item),
				documentManager.getUndoManager());
	}

	@Override
	public void process(Event event, DocumentItem parent,
			List<? extends DocumentItem> items) {
		selectedItems.clear();
		selectedItems.addAll(items);
		if (selectedItems.isEmpty()) {
			selectedItems.add(parent);
		}
		DocumentManager documentManager = DocumentManager.getInstance();
		applicationMenu.refreshMenu(
				Utilities.<DocumentItem> convert(selectedItems),
				documentManager.getUndoManager());
	}

	@Override
	public String toString() {
		return Messages.getMessage("applicationPanel.name");
	}

	// 58406#########################################################################################
	public MapView getMapView() {
		return mapView;
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

	private void exportDocument() {
		DocumentItem item = selectedItems.get(0);
		String action = getMessage("operation.actionName.fileExport");

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);

		FileFilterFactory factory = FileFilterFactory.getInstance();
		for (FileFilter f : factory.getFilters()) {
			if (!f.getDescription().startsWith("FIT")) {
				if (!f.getDescription().startsWith("KML")) {
					fileChooser.addChoosableFileFilter(f);
				}
			}
		}

		int result = fileChooser.showDialog(application, action);
		if (result == JFileChooser.APPROVE_OPTION) {
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
				selectedFile = new File(selectedFile.getAbsolutePath() + "."
						+ fileType.getExtension());
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

			JOptionPane.showMessageDialog(
					TrackIt.getApplicationFrame(),
					getMessage("operation.success.fileExport",
							item.getDocumentItemName()));
		}
		if (item.isCourse()) {
			Course course = (Course) item;
			course.setUnsavedFalse();
		}

	}

	public void restart() {
		TrackIt.restartApplication();
	}

	private void detectPauses() {
		if (selectedItems.size() != 1) {
			JOptionPane
					.showMessageDialog(
							TrackIt.getApplicationFrame(),
							Messages.getMessage("applicationPanel.message.removePausesCoursesOnly"),
							"", JOptionPane.WARNING_MESSAGE);
			return;
		}
		DocumentItem item = selectedItems.get(0);
		try {
			if (item.isActivity()) {
				Activity a = (Activity) item;
				new PauseDetectionOperation().process(a);
				DocumentManager.getInstance().consolidate(a,
						ConsolidationLevel.SUMMARY);
			} else if (item.isCourse()) {
				Course c = (Course) item;
				new PauseDetectionOperation().process(c);
				HashMap<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL,
						ConsolidationLevel.SUMMARY);
				DocumentManager.getInstance().consolidate(c, options);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importPicture() {
		final DocumentItem item = selectedItems.get(0);
		String action = getMessage("applicationPanel.button.import");
		JFileChooser fileChooser = new JFileChooser();

		String[] acceptedExtensions = { "jpg", "jpeg" };
		FileFilter filter = new FileNameExtensionFilter("JPEG (*.JPG;*JPEG)",
				acceptedExtensions);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(filter);

		fileChooser.setMultiSelectionEnabled(true);
		setPictureDirectory(fileChooser);
		fileChooser.showDialog(application, action);
		final File[] files = fileChooser.getSelectedFiles();
		new Task(new Action() {

			@Override
			public String getMessage() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object execute() throws TrackItException {
				if (item.isActivity()) {
					Activity a = (Activity) item;
					for (File f : files) {
						a.addPicture(f);
						updatePictureDirectory(f);
					}
				} else if (item.isCourse()) {
					Course c = (Course) item;
					for (File f : files) {
						c.addPicture(f);
						updatePictureDirectory(f);
					}
				}
				return null;
			}

			@Override
			public void done(Object result) {
				// TODO Auto-generated method stub

			}

		}).execute();

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

	private void autoLocatePictures() {
		if (selectedItems.size() != 1) {
			JOptionPane
					.showMessageDialog(
							TrackIt.getApplicationFrame(),
							Messages.getMessage("applicationPanel.message.removePausesCoursesOnly"),
							"", JOptionPane.WARNING_MESSAGE);
			return;
		}
		DocumentItem item = selectedItems.get(0);
		try {
			if (item.isActivity()) {
				Activity a = (Activity) item;
				if (a.getPauses().isEmpty()) {
					new PauseDetectionPicCaseOperation().process(a);
					// DocumentManager.getInstance().consolidate(a,
					// ConsolidationLevel.SUMMARY);
				}
				// PhotoPlacer placer = new
				// PhotoPlacerDepthFirst(a.getPictures(), a.getPauses(),
				// a.getDocumentItemName());
				PhotoPlacer placer = new PhotoPlacerBreadthFirst(
						a.getPictures(), a.getPauses(), a.getDocumentItemName());
				placer.EstimateLocation(a.getTrackpoints());
			} else if (item.isCourse()) {
				Course c = (Course) item;
				boolean b = c.getUnsavedChanges();
				if (c.getPauses().isEmpty()) {
					new PauseDetectionPicCaseOperation().process(c);
					// HashMap<String, Object> options = new HashMap<String,
					// Object>();
					// options .put(Constants.ConsolidationOperation.LEVEL,
					// ConsolidationLevel.SUMMARY);
					// DocumentManager.getInstance().consolidate(c, options);
				}
				// PhotoPlacer placer = new
				// PhotoPlacerDepthFirst(c.getPictures(), c.getPauses(),
				// c.getDocumentItemName());
				PhotoPlacer placer = new PhotoPlacerBreadthFirst(
						c.getPictures(), c.getPauses(), c.getDocumentItemName());
				placer.EstimateLocation(c.getTrackpoints());
				c.setUnsavedChanges(b);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
