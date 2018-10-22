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
package com.trackit.presentation;

import static com.trackit.business.common.Messages.getMessage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.FileType;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.dbsearch.SearchControl;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.CoordinatesFormatter;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;
import com.trackit.business.domain.TrackSegment;
import com.trackit.business.domain.TrackStatus;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.operation.AltitudeSmoothing;
import com.trackit.business.operation.ConsolidationLevel;
import com.trackit.business.operation.PauseDetectionPicCaseOperation;
import com.trackit.business.operation.ReportingPauseDetectionOperation;
import com.trackit.business.operation.NonReportingPauseDetectionOperation;
import com.trackit.business.utilities.PhotoPlacer;
import com.trackit.business.utilities.PhotoPlacerBreadthFirst;
import com.trackit.business.utilities.SaveTools;
import com.trackit.business.utilities.TrackItPreferences;
import com.trackit.business.utilities.Utilities;
import com.trackit.business.writer.Writer;
import com.trackit.business.writer.WriterFactory;
import com.trackit.presentation.calendar.CalendarInterface;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;
import com.trackit.presentation.utilities.OperationsFactory;
import com.trackit.presentation.utilities.TestPanel;
import com.trackit.presentation.view.browse.BrowseView;
import com.trackit.presentation.view.chart.ChartView;
import com.trackit.presentation.view.data.DataView;
import com.trackit.presentation.view.folder.ActivitiesItem;
import com.trackit.presentation.view.folder.CoursesItem;
import com.trackit.presentation.view.folder.FolderView;
import com.trackit.presentation.view.log.LogView;
import com.trackit.presentation.view.map.MapView;
import com.trackit.presentation.view.summary.SummaryView;

public class ApplicationPanel extends JPanel implements EventPublisher, EventListener {
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

	private static Logger logger = Logger.getLogger(ApplicationPanel.class.getName());

	public ApplicationPanel(JFrame application) {
		this.application = application;
		initComponents();
	}

	private void initComponents() {
		Image applicationImage = Toolkit.getDefaultToolkit()
				.getImage(ApplicationPanel.class.getResource("/icons/trackit.png"));

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
		boolean showFolder = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_FOLDER, true);
		boolean showSummary = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_SUMMARY, true);
		boolean showMap = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_MAP, true);
		boolean showChart = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_CHART, true);
		boolean showData = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_DATA, true);
		boolean showLog = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_LOG, true);

		lowerTabbedPane = null;
		if (showChart || showData || showLog) {
			lowerTabbedPane = new JTabbedPane();

			if (showChart) {
				lowerTabbedPane.addTab(getMessage("applicationPanel.view.chartTitle"), chartView);
			}

			if (showData) {
				lowerTabbedPane.addTab(getMessage("applicationPanel.view.dataTitle"), dataView);
			}

			if (showLog) {
				lowerTabbedPane.addTab(getMessage("applicationPanel.view.logTitle"), logView);
			}
		}

		JComponent upperPane = null;
		if (showMap && upperTabbedPane != null) {
			horizontalContentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, upperTabbedPane, mapView);
			horizontalContentSplit.setOneTouchExpandable(false);
			upperPane = horizontalContentSplit;
		} else if (showMap) {
			upperPane = mapView;
		} else if (upperTabbedPane != null) {
			upperPane = upperTabbedPane;
		}

		rightPane = null;
		if (upperPane != null && lowerTabbedPane != null) {
			verticalContentSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPane, lowerTabbedPane);
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
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, folderView, summaryView);
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
			horizontalContentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
			horizontalContentSplit.setOneTouchExpandable(false);
			content = horizontalContentSplit;
		} else if (leftPane != null) {
			content = leftPane;
		} else if (rightPane != null) {
			content = rightPane;
		} else {
			content = new JPanel();
		}

		Component centerComponent = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
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
				case NEW_SEARCH:
					newSearch();
					break;
				case REFINE_SEARCH:
					refineSearch();
					break;
				case CALENDAR_MENU:
					calendarMenu();
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
				case DUPLICATE:						//12335: 2017-03-30
					duplicate();
					break;
				case COPYTO:
					copyTo();
					break;
				case MOVETO:						//12335: 2017-03-27
					moveTo();
					break;
				case ADD_PAUSE:
					addPause();
					break;
				case CHANGE_PAUSE_DURATION:
					changePauseDuration();
					break;
				case REMOVE_PAUSE:
					removePause();
					break;
				case SPLIT_INTO_SEGMENTS:
					splitIntoSegments();
					break;
				case COMPARE_SEGMENTS:
					compareSegments();
					break;
				case CREATE_SEGMENT:
					createSegment();
					break;
				case TEST:
					test();
					break;
				case COLOR_GRADING:
					colorGrading();
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
	//57421
	//test function for use with test menu
	private void test(){
		
		new TestPanel( null);
//		(new TestPanel( null)).showValues();
		
	}
	
	private void colorGrading(){
		Course course = (Course) selectedItems.get(0);
		DocumentManager documentManager = DocumentManager.getInstance();
		documentManager.colorGrading(course);
	}

	private void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), message,
				Messages.getMessage("applicationPanel.title.error"), JOptionPane.ERROR_MESSAGE);
	}

	private void altitudeSmoothing() {
		try {
			new AltitudeSmoothing().process(DocumentManager.getInstance().getSelectedDocument());
		} catch (TrackItException e) {
			logger.error(e.getMessage());
			showErrorMessage(Messages.getMessage("applicationPanel.error.altitudeSmoothing"));
		}
	}

	private void detectClimbsAndDescents() {
		final DocumentItem selectedItem = selectedItems.get(0);
		new Task(new Action() {
			@Override
			public String getMessage() {
				return Messages.getMessage("applicationPanel.message.detectClimbsDescents",
						selectedItem.getDocumentItemName());
			}

			@Override
			public Object execute() throws TrackItException {
				return DocumentManager.getInstance().detectClimbsDescents(selectedItem, new HashMap<String, Object>());
			}

			@Override
			public void done(Object result) {
				((DocumentItem) result).publishUpdateEvent(null);
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
						Messages.getMessage("applicationPanel.message.detectClimbsDescentsSuccess"));
			}
		}).execute();
	}

	private void showLog(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_LOG, chkMenuItem.getState());

		if (lowerTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				lowerTabbedPane.addTab(getMessage("applicationPanel.view.logTitle"), logView);
				lowerTabbedPane.setSelectedComponent(logView);
				return;
			} else if (chkMenuItem.getState() == false && lowerTabbedPane.getComponentCount() > 1) {
				lowerTabbedPane.remove(logView);
				return;
			}
		}
		layoutComponents();
	}

	private void showTable(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_DATA, chkMenuItem.getState());

		if (lowerTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				lowerTabbedPane.addTab(getMessage("applicationPanel.view.dataTitle"), dataView);
				lowerTabbedPane.setSelectedComponent(dataView);
				return;
			} else if (chkMenuItem.getState() == false && lowerTabbedPane.getComponentCount() > 1) {
				lowerTabbedPane.remove(dataView);
				return;
			}
		}
		layoutComponents();
	}

	private void showChart(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_CHART, chkMenuItem.getState());

		if (lowerTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				lowerTabbedPane.addTab(getMessage("applicationPanel.view.chartTitle"), chartView);
				lowerTabbedPane.setSelectedComponent(chartView);
				return;
			} else if (chkMenuItem.getState() == false && lowerTabbedPane.getComponentCount() > 1) {
				lowerTabbedPane.remove(chartView);
				return;
			}
		}
		layoutComponents();
	}

	private void showMap(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_MAP, chkMenuItem.getState());
		layoutComponents();
	}
	
	private void showSummary(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_SUMMARY, chkMenuItem.getState());

		if (upperTabbedPane != null) {
			if (chkMenuItem.getState() == true) {
				upperTabbedPane.addTab(getMessage("applicationPanel.view.summaryTitle"), summaryView);
				upperTabbedPane.setSelectedComponent(summaryView);
				return;
			} else if (chkMenuItem.getState() == false && upperTabbedPane.getComponentCount() > 1) {
				upperTabbedPane.remove(summaryView);
				return;
			}
		}
		layoutComponents();
	}

	private void showFolder(ActionEvent e) {
		JCheckBoxMenuItem chkMenuItem = (JCheckBoxMenuItem) e.getSource();
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.SHOW_FOLDER, chkMenuItem.getState());
		layoutComponents();
	}

	private void exitApplication() {// 58406
		// 12335: 2016-06-09: added args for menu initiated TrackIt close on non Mac
		SaveTools.getInstance().saveAndExit( true, false); 
	}

	private void openDocument() {
		String action = getMessage("applicationPanel.button.open");
		UIManager.put("FileChooser.cancelButtonText", Messages.getMessage( "applicationPanel.cancel.open"));	//12335: 2017-08-07 	
		final JFileChooser fileChooser = getFileChooser(action);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDialogTitle( getMessage("applicationPanel.title.open"));		//12335: 2017-08-07

		int result = fileChooser.showDialog(application, action);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			updateInitialDirectory(file);
			DocumentManager.getInstance().openDocument(file);
		}
	}

	private void importDocument() {
		String action = getMessage("applicationPanel.button.import");
		UIManager.put("FileChooser.cancelButtonText", Messages.getMessage( "applicationPanel.cancel.import"));	//12335: 2017-08-07 	
		final JFileChooser fileChooser = getFileChooser(action);
		fileChooser.setDialogTitle( getMessage("applicationPanel.title.import"));		//12335: 2017-08-07

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
		GPSDocument document = documentManager.createDocument(DocumentManager.FOLDER_WORKSPACE);
		EventManager.getInstance().publish(this, Event.DOCUMENT_ADDED, document);
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
		updateInitialImportExportDirectory(file, Constants.GlobalPreferences.LAST_IMPORT_DIRECTORY);
	}

	public void updateInitialExportDirectory(File file) {
		updateInitialImportExportDirectory(file, Constants.GlobalPreferences.LAST_EXPORT_DIRECTORY);
	}

	private void updateInitialImportExportDirectory(File file, String id) {
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null, id, file.getParent());
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
		setInitialImportExportDirectory(fileChooser, Constants.GlobalPreferences.LAST_IMPORT_DIRECTORY);
	}

	public void setInitialExportDirectory(final JFileChooser fileChooser) {
		setInitialImportExportDirectory(fileChooser, Constants.GlobalPreferences.LAST_EXPORT_DIRECTORY);
	}

	private void setInitialImportExportDirectory(final JFileChooser fileChooser, String id) {
		TrackItPreferences prefs = TrackIt.getPreferences();
		String initialDirectory = prefs.getPreference(Constants.PrefsCategories.GLOBAL, null, id,
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
		//int width = 644;
		//int height = 368;
		int width = 725;
		int height = 500;
		PreferencesDialog preferencesDialog;
		if ( !selectedItems.isEmpty() &&
			 (selectedItems.get(0).isActivity() || 	selectedItems.get(0).isCourse()) ) {
			if ( selectedItems.get(0).isCourse() )
				preferencesDialog = new PreferencesDialog(TrackIt.getApplicationFrame(), (Course) selectedItems.get(0));
			else
				preferencesDialog = new PreferencesDialog(TrackIt.getApplicationFrame(), (Activity) selectedItems.get(0));
		}
		else 
			preferencesDialog = new PreferencesDialog(TrackIt.getApplicationFrame());
		preferencesDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		preferencesDialog.pack();
		preferencesDialog.setMinimumSize(new Dimension(width, height));
		preferencesDialog.setPreferredSize(new Dimension(width, height));
		preferencesDialog.setVisible(true);
	}

	private void newCourse() {
		DocumentManager documentManager = DocumentManager.getInstance();
		GPSDocument document = documentManager.getSelectedDocument();
		documentManager.newCourse(document);
	}
	
	//57421
	private void splitIntoSegments(){
		final boolean addToUndoManager = true;
		Course course = (Course) selectedItems.get(0);
		JDialog splitIntoSegmentsDialog = new SplitIntoSegmentsDialog(course);
		splitIntoSegmentsDialog.setVisible(true);
	}
	
	//57421
	private void createSegment(){
		final boolean addToUndoManager = true;
		final boolean redo = false;
		TrackSegment segment = (TrackSegment)selectedItems.get(0);
		DocumentManager documentManager = DocumentManager.getInstance();
		documentManager.createSegment(segment, addToUndoManager, redo);
	}
	
	//57421
	private void compareSegments() throws TrackItException{
		List<Course> segments = new ArrayList<Course>();
		GPSDocument masterDocument = null;
		for (DocumentItem item : selectedItems) {
			if (!item.isCourse()) {
				throw new TrackItException(Messages.getMessage("applicationPanel.error.compareOnlyCourses"));
				}
			GPSDocument itemDocument = (GPSDocument) item.getParent();
			masterDocument = (masterDocument == null ? itemDocument : masterDocument);
			if (!itemDocument.equals(masterDocument)) {
				throw new TrackItException(Messages.getMessage("applicationPanel.error.compareCoursesDifferentDocuments"));
				}
			segments.add((Course) item);
			}
		DocumentManager documentManager = DocumentManager.getInstance();
		documentManager.compareSegments(segments);
	}
	
	//12355: 2017-03-10 created
	private void newSearch() {
		SearchControl.getInstance().newSearch(true);	//71052: 2017-07-12
	}
	
	//12335: 2017-03-10 created
	private void refineSearch() {
		SearchControl.getInstance().newSearch(false);	//71052: 2017-07-12
	}
	
	//71052: 2017-07-10 created
	private void calendarMenu(){
		CalendarInterface.getInstance().setVisible(true);
	}

	private void splitAtSelected() {
		Object[] options = { "Yes", "No", "Cancel" };
		boolean keepSpeed;
		final boolean addToUndoManager = true;
		final boolean isUndo = false;

		final Map<String, Object> undoOptions = new HashMap<String, Object>();
		undoOptions.put(Constants.ExtraUndoOptions.ADD_TO_MANAGER, new Boolean(true));
		undoOptions.put(Constants.ExtraUndoOptions.SPLIT_UNDO, new Boolean(false));
		undoOptions.put(Constants.ExtraUndoOptions.JOIN_UNDO, new Boolean(false));
		undoOptions.put(Constants.ExtraUndoOptions.APPEND_UNDO, new Boolean(false));

		int keepSpeedDialog = JOptionPane.showOptionDialog(TrackIt.getApplicationFrame(),
				Messages.getMessage("applicationPanel.splitAtSelected.keepSpeed"),
				Messages.getMessage("applicationPanel.splitAtSelected.keepSpeedTitle"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		DocumentManager documentManager = DocumentManager.getInstance();

		Trackpoint trackpoint = (Trackpoint) selectedItems.get(0);
		Course course = (Course) trackpoint.getParent();

		if (keepSpeedDialog == JOptionPane.YES_OPTION) {
			keepSpeed = true;
			documentManager.splitAtSelected(course, trackpoint, keepSpeed, undoOptions);
		}
		if (keepSpeedDialog == JOptionPane.NO_OPTION) {
			keepSpeed = false;
			documentManager.splitAtSelected(course, trackpoint, keepSpeed, undoOptions);
		}

	}

	private void join() throws TrackItException {
		List<Course> courses = new ArrayList<Course>();
		GPSDocument masterDocument = null;

		for (DocumentItem item : selectedItems) {
			if (!item.isCourse()) {
				throw new TrackItException(Messages.getMessage("applicationPanel.error.joinOnlyCourses"));
			}

			GPSDocument itemDocument = (GPSDocument) item.getParent();
			masterDocument = (masterDocument == null ? itemDocument : masterDocument);

			if (!itemDocument.equals(masterDocument)) {
				throw new TrackItException(Messages.getMessage("applicationPanel.error.joinCoursesDifferentDocuments"));
			}

			courses.add((Course) item);
		}
		for (int i = 1; i<courses.size(); i++){
			Course course1 = courses.get(i-1);
			Course course2 = courses.get(i);
			short sportID1 = course1.getSport().getSportID();
			short sportID2 = course2.getSport().getSportID();
			short subSportID1 = course1.getSubSport().getSubSportID();
			short subSportID2 = course2.getSubSport().getSubSportID();
			if(sportID1 != sportID2 || subSportID1 != subSportID2){
				JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), Messages.getMessage("joinDialog.message.warningDifferentSports"),
						Messages.getMessage("applicationPanel.title.warning"),
				        JOptionPane.WARNING_MESSAGE);

				return;
			}
		}

		JDialog joinDialog = new JoinDialog(courses);
		joinDialog.setVisible(true);
	}

	private void returnCourse() {
//		Object[] options = { "Yes", "No", "Cancel" };		/12335: 2017-04-19
		Object[] options = { Messages.getMessage( "messages.yes"), 
							 Messages.getMessage( "messages.no"),
							 Messages.getMessage( "messages.cancel") };
		boolean addToUndoManager = true;
//		String undoRedoMode = null;							//12335: 2017-04-19
		boolean undo = false;
		int option = JOptionPane.showConfirmDialog(TrackIt.getApplicationFrame(),
				Messages.getMessage("applicationPanel.reverse.effects"),
				Messages.getMessage("applicationPanel.title.warning"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if (option == JOptionPane.YES_OPTION) {

			int returnCourseOption = JOptionPane.showOptionDialog(TrackIt.getApplicationFrame(),
					Messages.getMessage("applicationPanel.reverse.wayBack"),
					Messages.getMessage("applicationPanel.title.wayBackTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

			if (returnCourseOption == JOptionPane.NO_OPTION) {

				DocumentManager documentManager = DocumentManager.getInstance();
				Course course = (Course) selectedItems.get(0);
				//12335: 2017-04-19
//				documentManager.reverse(course, Constants.ReverseOperation.RETURN, addToUndoManager, undoRedoMode);
				documentManager.reverse(course, Constants.ReverseOperation.RETURN, addToUndoManager, undo, -1L);
			}
			if (returnCourseOption == JOptionPane.YES_OPTION) {

				DocumentManager documentManager = DocumentManager.getInstance();
				Course course = (Course) selectedItems.get(0);
				//12335: 2017-04-19
//				documentManager.reverse(course, Constants.ReverseOperation.RETURN_NEW, addToUndoManager, undoRedoMode);
				documentManager.reverse(course, Constants.ReverseOperation.RETURN_NEW, addToUndoManager, undo, -1L);
			}
		}

	}

	private void reverse() {
		boolean addToUndoManager = true;
//		String undoRedoMode = null;				//12335: 2017-04-19
		boolean undo = false;
		int option = JOptionPane.showConfirmDialog(TrackIt.getApplicationFrame(),
				Messages.getMessage("applicationPanel.reverse.effects"),
				Messages.getMessage("applicationPanel.title.warning"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (option == JOptionPane.YES_OPTION) {
			DocumentManager documentManager = DocumentManager.getInstance();
			Course course = (Course) selectedItems.get(0);
			//12335: 2017-04-19
//			documentManager.reverse(course, Constants.ReverseOperation.NORMAL, addToUndoManager, undoRedoMode);
			documentManager.reverse(course, Constants.ReverseOperation.NORMAL, addToUndoManager, undo, -1L);
		}
	}

	public void forceRefresh() {
		DocumentManager documentManager = DocumentManager.getInstance();
		applicationMenu.refreshUndo(documentManager.getUndoManager());
	}

	private void undo() throws TrackItException {
		DocumentManager documentManager = DocumentManager.getInstance();
		documentManager.undo();
		applicationMenu.refreshUndo(documentManager.getUndoManager());
	}

	private void redo() throws TrackItException {
		DocumentManager documentManager = DocumentManager.getInstance();
		documentManager.redo();
		applicationMenu.refreshUndo(documentManager.getUndoManager());
	}
	
	// 12335: 2017-04-07
	private void duplicate() {
		DocumentManager.getInstance().duplicate( selectedItems);
	}

	// 12335: 2017-04-06
	private void copyTo() {
		DocumentManager.getInstance().copyTo( selectedItems, true);
	}
	
	// 12335: 2017-04-06
	private void moveTo() {
		DocumentManager.getInstance().copyTo( selectedItems, false);
	}

	private void setPace() {
		JDialog setPaceDialog = new SetPaceDialog((Course) selectedItems.get(0));
		setPaceDialog.setVisible(true);
	}

	private void consolidate() {
		JDialog consolidationDialog = new ConsolidationDialog(selectedItems.get(0));
		consolidationDialog.setVisible(true);
	}

	private void simplify() {
		JDialog simplificationDialog = new SimplificationDialog(selectedItems.get(0));
		simplificationDialog.setVisible(true);
	}

	private void addPause() {
		JDialog inputPauseTimeDialog = new InputPauseTimeDialog((Trackpoint) selectedItems.get(0), Constants.PauseDialogOptions.ADD_PAUSE);
		inputPauseTimeDialog.setVisible(true);
		/*
		 * boolean addToUndoManager = true; long pausedTime; int hours =
		 * Integer.parseInt((String)JOptionPane.showInputDialog(
		 * TrackIt.getApplicationFrame(),
		 * 
		 * Messages.getMessage("applicationPanel.addPause.hours"),
		 * Messages.getMessage("applicationPanel.addPause.title"),
		 * JOptionPane.PLAIN_MESSAGE, null, null, "0")); int minutes =
		 * Integer.parseInt((String)JOptionPane.showInputDialog(
		 * TrackIt.getApplicationFrame(),
		 * 
		 * Messages.getMessage("applicationPanel.addPause.minutes"),
		 * Messages.getMessage("applicationPanel.addPause.title"),
		 * JOptionPane.PLAIN_MESSAGE, null, null, "0")); int seconds =
		 * Integer.parseInt((String) JOptionPane.showInputDialog(
		 * TrackIt.getApplicationFrame(),
		 * 
		 * Messages.getMessage("applicationPanel.addPause.seconds"),
		 * Messages.getMessage("applicationPanel.addPause.title"),
		 * JOptionPane.PLAIN_MESSAGE, null, null, "0"));
		 * 
		 * if(seconds > 59){ minutes += seconds/60; seconds = seconds % 60;
		 * 
		 * 
		 * }
		 * 
		 * if(minutes > 59){ hours += minutes/60; minutes = minutes % 60; }
		 * 
		 * String time = hours+":"+minutes+":"+seconds;
		 * 
		 * TimeZone utc = TimeZone.getTimeZone("UTC"); Calendar date =
		 * Calendar.getInstance(utc); DateFormat format = new
		 * SimpleDateFormat("HH:mm:ss"); format.setTimeZone(utc);
		 * 
		 * date.setTime(format.parse(time)); System.out.println(date.getTime());
		 * pausedTime = date.getTime().getTime();
		 * 
		 * DocumentManager documentManager = DocumentManager.getInstance();
		 * Trackpoint trackpoint = (Trackpoint) selectedItems.get(0); Course
		 * course = (Course) trackpoint.getParent();
		 * documentManager.addPause(course, trackpoint, pausedTime,
		 * addToUndoManager);
		 */

	}

	private void changePauseDuration() {
		JDialog inputPauseTimeDialog = new InputPauseTimeDialog((Trackpoint) selectedItems.get(0), Constants.PauseDialogOptions.CHANGE_DURATION);
		inputPauseTimeDialog.setVisible(true);
	}

	private void removePause() {
		boolean addToUndoManager = true;
		DocumentManager documentManager = DocumentManager.getInstance();
		Trackpoint trackpoint = (Trackpoint) selectedItems.get(0);
		Course course = (Course) trackpoint.getParent();
		documentManager.removePause(course, trackpoint, addToUndoManager, null);

	}

	private void removePauses() {
		boolean addToUndoManager = true;
		if (selectedItems.size() != 1 || !selectedItems.get(0).isCourse()) {
			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
					Messages.getMessage("applicationPanel.message.removePausesCoursesOnly"), "",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		DocumentManager.getInstance().removeAllPauses((Course) selectedItems.get(0), addToUndoManager);
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
		applicationMenu.refreshUndo(documentManager.getUndoManager());
		applicationMenu.refreshMenu(Arrays.asList(item), documentManager.getUndoManager());
	}

	@Override
	public void process(Event event, DocumentItem parent, List<? extends DocumentItem> items) {
		selectedItems.clear();
		selectedItems.addAll(items);
//		if (selectedItems.isEmpty()) {				//12335: 2017-04-11
//			selectedItems.add(parent);
//		}
		DocumentManager documentManager = DocumentManager.getInstance();
		applicationMenu.refreshUndo(documentManager.getUndoManager());
		applicationMenu.refreshMenu(Utilities.<DocumentItem> convert(selectedItems), documentManager.getUndoManager());
	}

	@Override
	public String toString() {
		return Messages.getMessage("applicationPanel.name");
	}

	// 58406#########################################################################################
	public MapView getMapView() {
		return mapView;
	}

	private void exportDocument() {
		OperationsFactory.getInstance().doFileExportOperation( selectedItems.get(0),
															   FileType.ALL);
	}

	public void restart() {
		TrackIt.restartApplication();
	}

	private void detectPauses() {
		if (selectedItems.size() != 1) {
			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
					Messages.getMessage("applicationPanel.message.removePausesCoursesOnly"), "",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		DocumentItem item = selectedItems.get(0);
		new ReportingPauseDetectionOperation().process( item);		//12335: 2017-07-16
		/*                    12335: 2017-07-16
		try {
			if ( item.isActivity() )
				DocumentManager.getInstance().consolidate( (Activity) item, ConsolidationLevel.SUMMARY);
			else
				if ( item.isCourse() )
					DocumentManager.getInstance().consolidate( (Course) item, ConsolidationLevel.SUMMARY);
		try {
			if (item.isActivity()) {
				Activity a = (Activity) item;
				new PauseDetectionOperation().process(a);
				DocumentManager.getInstance().consolidate(a, ConsolidationLevel.SUMMARY);
//				DocumentManager.getInstance().consolidate(a, ConsolidationLevel.RECALCULATION);
			} else if (item.isCourse()) {
				Course c = (Course) item;
				new PauseDetectionOperation().process(c);
				HashMap<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.SUMMARY);
//				options.put(Constants.ConsolidationOperation.LEVEL, ConsolidationLevel.RECALCULATION);
				DocumentManager.getInstance().consolidate(c, options);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			*/
	}

	public void importPicture() {
		final DocumentItem item = selectedItems.get(0);
		String action = getMessage("applicationPanel.button.importPhotos");
		UIManager.put("FileChooser.cancelButtonText", Messages.getMessage( "applicationPanel.cancel.importPhotos"));	//12335: 2017-08-07 	
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle( getMessage("applicationPanel.title.importPhotos"));		//12335: 2017-08-07

		String[] acceptedExtensions = { "jpg", "jpeg" };
		FileFilter filter = new FileNameExtensionFilter("JPEG (*.JPG;*JPEG)", acceptedExtensions);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(filter);

		fileChooser.setMultiSelectionEnabled(true);
		setPictureDirectory(fileChooser);
		fileChooser.showDialog(application, action);
		final File[] files = fileChooser.getSelectedFiles();
		new Task(new Action() {

			@Override
			public String getMessage() {
				return Messages.getMessage( "operation.progress.importPicture");	//12335: 2018-07-07
			}

			@Override
			public Object execute() throws TrackItException {
				if ( files.length > 0 ) {							//12335: 2017-08-07
					if (item.isActivity()) {
						((Activity)item).addPictures(files);		//12335: 2015-10-03
					} else if (item.isCourse()) {
						((Course)item).addPictures(files);			//12335: 2015-10-03
					}
					updatePictureDirectory(files[0]);				//12335: 2017-08-07
//					Activity a = (Activity) item;
//					for (File f : files) {
//						a.addPicture(f);
//						updatePictureDirectory(f);
//					}
//					Course c = (Course) item;
//					for (File f : files) {
//						c.addPicture(f);
//						updatePictureDirectory(f);
//					}
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
		TrackIt.getPreferences().setPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.LAST_PICTURE_DIRECTORY, file.getParent());
	}

	private void setPictureDirectory(final JFileChooser fileChooser) {
		TrackItPreferences prefs = TrackIt.getPreferences();
		String initialDirectory = prefs.getPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.LAST_PICTURE_DIRECTORY, System.getProperty("user.home"));
		File currentDirectory = new File(initialDirectory);
		fileChooser.setCurrentDirectory(currentDirectory);
	}

//	private void autoLocatePictures() {
//		if (selectedItems.size() != 1) {
//			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
//					Messages.getMessage("applicationPanel.message.removePausesCoursesOnly"), "",
//					JOptionPane.WARNING_MESSAGE);
//			return;
//		}
//		DocumentItem item = selectedItems.get(0);
//		try {
//			if (item.isActivity()) {
//				Activity a = (Activity) item;
//				if (a.getPauses().isEmpty()) {
//					new PauseDetectionPicCaseOperation().process(a);
//					// DocumentManager.getInstance().consolidate(a,
//					// ConsolidationLevel.SUMMARY);
//				}
//				// PhotoPlacer placer = new
//				// PhotoPlacerDepthFirst(a.getPictures(), a.getPauses(),
//				// a.getDocumentItemName());
//				PhotoPlacer placer = new PhotoPlacerBreadthFirst(a.getPictures(), a.getPauses(),
//						a.getDocumentItemName());
//				placer.EstimateLocation(a.getTrackpoints());
//			} else if (item.isCourse()) {
//				Course c = (Course) item;
////				boolean b = c.getUnsavedChanges();			// 12335 : 2016-10-03
//				TrackStatus prevStatus = new TrackStatus( c.getStatus());
//				if (c.getPauses().isEmpty()) {
//					new PauseDetectionPicCaseOperation().process(c);
//					// HashMap<String, Object> options = new HashMap<String,
//					// Object>();
//					// options .put(Constants.ConsolidationOperation.LEVEL,
//					// ConsolidationLevel.SUMMARY);
//					// DocumentManager.getInstance().consolidate(c, options);
//				}
//				// PhotoPlacer placer = new
//				// PhotoPlacerDepthFirst(c.getPictures(), c.getPauses(),
//				// c.getDocumentItemName());
//				PhotoPlacer placer = new PhotoPlacerBreadthFirst(c.getPictures(), c.getPauses(),
//						c.getDocumentItemName());
//				placer.EstimateLocation(c.getTrackpoints());
////				c.setUnsavedChanges(b);						// 12335 : 2016-10-03
//				c.setStatus( prevStatus);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	// 12335: 2018-07-10 - code reconstructed to:
	//						- compute pauses regardless of any previous pause detection
	//						- allow operation on several items 
	//						- carry out code rewriting
	private void autoLocatePictures() {
		List<DocumentItem> items = new ArrayList<>();
		// TO DO: copy selected items to list, expand documents
		items.add( selectedItems.get( 0));
		for( DocumentItem item: items) {
			if ( item.isActivity() || item.isCourse() )
				try {
					if ( item.isActivity() ) {
						Activity a = (Activity) item;
//						new PauseDetectionPicCaseOperation().process(a);
						PhotoPlacer placer = new PhotoPlacerBreadthFirst( item );
						placer.EstimateLocation(a.getTrackpoints());
					} else {
						Course c = (Course) item;
						TrackStatus prevStatus = new TrackStatus( c.getStatus());
//						new PauseDetectionPicCaseOperation().process(c);
						PhotoPlacer placer = new PhotoPlacerBreadthFirst( item);
						placer.EstimateLocation(c.getTrackpoints());
						c.setStatus( prevStatus);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
}
