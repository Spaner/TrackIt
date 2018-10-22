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

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.KeyAlreadyExistsException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.plaf.MenuBarUI;
import javax.swing.text.DefaultEditorKit;

import org.apache.log4j.Logger;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Folder;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.operation.UndoManagerCustom;
import com.trackit.business.utilities.AboutDialog;
import com.trackit.presentation.task.ActionType;
import com.trackit.presentation.utilities.Operation;
import com.trackit.presentation.view.folder.ActivitiesItem;
import com.trackit.presentation.view.folder.CoursesItem;

class ApplicationMenu {
	private JMenu    subMenuReverse;				//12335: 2016-07-26
	private JMenuBar applicationMenu;
	private JMenuItem importMenu;
	private JMenuItem exportMenu;
	private JMenuItem newCourseMenu;
	private JMenuItem newSearch;					//12335: 2017-03-10
	private JMenuItem calendarMenu;					//71052: 2017-07-10
	private JMenuItem splitAtSelectedMenu;
	private JMenuItem joinMenu;
	private JMenuItem reverseMenu;
	private JMenuItem detectClimbsDescentsMenu;
	private JMenuItem markingMenu;
	private JMenuItem setPaceMenu;
	private JMenuItem consolidationMenu;
	private JMenuItem simplificationMenu;
	private JMenuItem detectPausesMenu;//58406
	private JMenuItem autoLocatePicturesMenu;//58406
	private JMenuItem pictureMenu;//58406
	private JMenuItem undoMenu;//57421
	private JMenuItem redoMenu;//57421
	private JMenuItem duplicateMenu;				//12335: 2017-03-30
	private JMenuItem copyMenu;//57421
	private JMenuItem moveToMenu; 					//12335: 2017-03-27
	private JMenuItem returnCourseMenu;//57421
	private JMenuItem addPauseMenu;//57421
	private JMenuItem changePauseDurationMenu;//57421
	private JMenuItem removePauseMenu;//57421
	private JMenuItem removePausesMenu;//57421
	private JMenuItem splitIntoSegmentsMenu; //57421
	private JMenuItem compareSegmentsMenu; //57421
	private JMenuItem createSegmentMenu;//57421
	private JMenuItem testMenu;//57421
	private JMenuItem colorGradingMenu;//57421
	private static Map<ActionType, JMenuItem> menuActionMap;
	private static Logger logger = Logger.getLogger(ApplicationMenu.class);
	
	static {
		menuActionMap = new HashMap<>();
	}

	ApplicationMenu(ActionListener handler) {
		createApplicationMenu(handler);
		updateMenuActionMap();
	}
	
	JMenuBar getMenu() {
		return applicationMenu;
	}
	
	void refreshUndo(UndoManagerCustom undoManager){
		undoMenu.setEnabled(undoManager.canUndo());//57421
		redoMenu.setEnabled(undoManager.canRedo());//57421
	}
	
	void refreshMenu(List<DocumentItem> items, UndoManagerCustom undoManager) {
		boolean singleItem = (items.size() == 1 && !(items.get(0) instanceof Folder));
		boolean itemsOkForDuplicationCopyToMoveTo = selectedItemsOkForDuplicationCopyToMoveTo(items);
		
		importMenu.setEnabled(singleItem);
		exportMenu.setEnabled(singleItem && (items.get(0).isActivity() || items.get(0).isCourse() || items.get(0) instanceof GPSDocument));
		splitAtSelectedMenu.setEnabled(singleItem && items.get(0) instanceof Trackpoint && items.get(0).getParent().isCourse());
		subMenuReverse.setEnabled(singleItem && items.get(0).isCourse());	//12335:2016-07-26
		reverseMenu.setEnabled(singleItem && items.get(0).isCourse());
		returnCourseMenu.setEnabled(singleItem && items.get(0).isCourse());
		splitIntoSegmentsMenu.setEnabled(singleItem && items.get(0).isCourse());
		duplicateMenu.setEnabled( itemsOkForDuplicationCopyToMoveTo);  				//12335: 2017-04-11
		copyMenu.setEnabled( itemsOkForDuplicationCopyToMoveTo);					//12335: 2017-04-11
		moveToMenu.setEnabled( itemsOkForDuplicationCopyToMoveTo);					//12335: 2017-04-11
//		createSegmentMenu.setEnabled(items.get(0).isSegment());						//12335: 2017-04-11
		createSegmentMenu.setEnabled(singleItem && items.get(0).isSegment());
		testMenu.setEnabled( true/*singleItem && items.get(0).isCourse()*/);
		//----------------------------
		//test option in the tools dropdown menu, used to easily test parts of operations
		//set to true to activate
//		testMenu.setVisible( false);
		testMenu.setVisible( true);
		
		///////////////////////////
		
		
		colorGradingMenu.setEnabled(singleItem && items.get(0).isCourse());
		if(singleItem && items.get(0) instanceof Trackpoint&& items.get(0).getParent().isCourse()){
			Course course = (Course) items.get(0).getParent();
			Trackpoint trackpoint = (Trackpoint) items.get(0);
			addPauseMenu.setEnabled(!course.isInsidePause(trackpoint.getTimestamp().getTime()));
			changePauseDurationMenu.setEnabled(course.isInsidePause(trackpoint.getTimestamp().getTime()));
			removePauseMenu.setEnabled(course.isInsidePause(trackpoint.getTimestamp().getTime()));
		}
		
		
			
		boolean joinMenuEnabled = true;
		joinMenuEnabled &= (items.size() > 1);
		for (DocumentItem item : items) {
			joinMenuEnabled &= item.isCourse();
		}
		joinMenu.setEnabled(joinMenuEnabled);
		compareSegmentsMenu.setEnabled(joinMenuEnabled);
		
		if (singleItem) {
			List<ActionType> supportedActions = items.get(0).getSupportedActions();

			for (ActionType action : menuActionMap.keySet()) {
				JMenuItem menuItem = menuActionMap.get(action);
				menuItem.setEnabled(actionSupported(supportedActions, action));
			}
		} else {
			for (ActionType action : menuActionMap.keySet()) {
				JMenuItem menuItem = menuActionMap.get(action);
				menuItem.setEnabled(false);
			}
		}
	}
	
	private boolean actionSupported(List<ActionType> supportedActions, ActionType action) {
		return supportedActions.contains(action);
	}
	
	//12335: 2017-04-11
	private boolean selectedItemsOkForDuplicationCopyToMoveTo( List<DocumentItem> items) {
		int no = 0;
		for( DocumentItem item: items)
			if ( !(item instanceof Folder) ) {
				if ( item.isActivity() || item.isCourse() )
					no++;
				else 
					if ( item instanceof GPSDocument )
						no += ((GPSDocument) item).countActivitiesAndCourses();
					else
						if ( item instanceof CoursesItem )
							no += ((GPSDocument) item.getParent()).countCourses();
						else
							if ( item instanceof ActivitiesItem )
								no += ((GPSDocument) item.getParent()).countActivities();
			}
		return no > 0;
	}
	
	private void updateMenuActionMap() {
		menuActionMap.put(ActionType.CONSOLIDATION, consolidationMenu);
		menuActionMap.put(ActionType.SET_PACE, setPaceMenu);
		menuActionMap.put(ActionType.DETECT_CLIMBS_DESCENTS, detectClimbsDescentsMenu);
		menuActionMap.put(ActionType.MARKING, markingMenu);
		menuActionMap.put(ActionType.NEW_COURSE, newCourseMenu);
		menuActionMap.put(ActionType.SIMPLIFICATION, simplificationMenu);
		menuActionMap.put(ActionType.REMOVE_PAUSES, removePausesMenu);
		//menuActionMap.put(ActionType.REMOVE_PAUSE, removePauseMenu);
		//menuActionMap.put(ActionType.ADD_PAUSE, addPauseMenu);
		menuActionMap.put(ActionType.IMPORT_PICTURE, pictureMenu);
		menuActionMap.put(ActionType.DETECT_PAUSES, detectPausesMenu);
		menuActionMap.put(ActionType.AUTO_LOCATE_PICTURES, autoLocatePicturesMenu);
		//menuActionMap.put(ActionType.SPLIT_INTO_SEGMENTS, splitIntoSegmentsMenu);
		//menuActionMap.put(ActionType.COMPARE_SEGMENTS, compareSegmentsMenu);
		//menuActionMap.put(ActionType.UNDO, undoMenu);
		//menuActionMap.put(ActionType.REDO, redoMenu);
//		menuActionMap.put(ActionType.COPY, copyMenu);			// 12335: 2017-03-30
	}

	private void createApplicationMenu(ActionListener handler) {
		JMenu menu;
		JMenu subMenu;							// 12335: 2016-07-05
		JMenuItem menuItem;
		JCheckBoxMenuItem chkMenuItem;
		KeyStroke keyStroke;
		int accelerator;
		String tooltip;

		applicationMenu = new JMenuBar();
		setUp(applicationMenu);

		menu = new JMenu(getMessage("applicationPanel.menu.file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("The file menu");
		applicationMenu.add(menu);
		
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.newDocument"), KeyEvent.VK_N);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.newDocumentAccelerator")).getKeyCode();
		tooltip = getMessage("applicationPanel.menu.newDocumentDescription"); //12335: 2016-07-30
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		menuItem.getAccessibleContext().setAccessibleDescription( tooltip); //12335: 2016-07-30
		menuItem.setToolTipText( tooltip);									//12335: 2016-07-30
		menuItem.setActionCommand(MenuActionType.NEW_DOCUMENT.name());
		menuItem.addActionListener(handler);
		menu.add(menuItem);
		
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.openDocument"), KeyEvent.VK_O);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.openDocumentAccelerator")).getKeyCode();
		tooltip = getMessage("applicationPanel.menu.openDocumentDescription"); //12335: 2016-07-30
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		menuItem.getAccessibleContext().setAccessibleDescription( tooltip); //12335: 2016-07-30
		menuItem.setToolTipText( tooltip);									//12335: 2016-07-30
		menuItem.setActionCommand(MenuActionType.OPEN_DOCUMENT.name());
		menuItem.addActionListener(handler);
		menu.add(menuItem);
		
		menu.addSeparator();
		//##################################################################################################
		importMenu = new JMenuItem(getMessage("applicationPanel.menu.import"), KeyEvent.VK_I);
		tooltip = getMessage("applicationPanel.menu.importDescription"); //12335: 2016-07-30
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.importAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		importMenu.setAccelerator(keyStroke);
		importMenu.getAccessibleContext().setAccessibleDescription( tooltip); //12335: 2016-07-30
		importMenu.setToolTipText( tooltip);									//12335: 2016-07-30
		importMenu.setActionCommand(MenuActionType.IMPORT_DOCUMENT.name());
		importMenu.addActionListener(handler);
		importMenu.setEnabled(false);
		menu.add(importMenu);
		//58406#############################################################################################
		exportMenu = new JMenuItem(getMessage("applicationPanel.menu.export"), KeyEvent.VK_E);
		tooltip = getMessage("applicationPanel.menu.exportDescription"); //12335: 2016-07-30
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.exportAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		exportMenu.setAccelerator(keyStroke);
		exportMenu.getAccessibleContext().setAccessibleDescription( tooltip); //12335: 2016-07-30
		exportMenu.setToolTipText( tooltip);									//12335: 2016-07-30
		exportMenu.setActionCommand(MenuActionType.EXPORT_DOCUMENT.name());
		exportMenu.addActionListener(handler);
		exportMenu.setEnabled(false);
		menu.add(exportMenu);

		/*exportMenu = new JMenu(getMessage("applicationPanel.menu.export"));
		exportMenu.setMnemonic(KeyEvent.VK_E);
		exportMenu.getAccessibleContext().setAccessibleDescription("Export a course or activity to a file.");
		exportMenu.setEnabled(false);
		menu.add(exportMenu);*/
		
		//##################################################################################################
		
		List<Operation> operations = Collections.emptyList();
		for (Operation operation : operations) {
			menuItem = new JMenuItem(operation.getName());
			menuItem.getAccessibleContext().setAccessibleDescription(operation.getDescription());
			menuItem.setAction(operation);
			exportMenu.add(menuItem);
		}
		
//		JMenuItemm exportMenu = new JMenuItem(getMessage("applicationPanel.menu.export"), KeyEvent.VK_E);
//		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
//		menuItem.setAccelerator(keyStroke);
//		menuItem.getAccessibleContext().setAccessibleDescription("Export a course or activity");
//		add(menuItem);
//		menuItem.add()

		//58406#############################################################################################

		menu.addSeparator();
		
		//12335: 2016-07-30
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.closeDocument"), KeyEvent.VK_C);
		tooltip = getMessage("applicationPanel.menu.closeDocumentDescription");
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.closeDocumentAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		menuItem.getAccessibleContext().setAccessibleDescription( tooltip);
		menuItem.setToolTipText( tooltip);
		menuItem.setActionCommand(MenuActionType.CLOSE_DOCUMENT.name());
		menuItem.addActionListener(handler);
		menu.add(menuItem);
		
		//12335: 2016-07-30
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.closeAllDocuments"), KeyEvent.VK_C);
		tooltip = getMessage("applicationPanel.menu.closeAllDocumentsDescription");
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.closeAllDocumentsAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		menuItem.getAccessibleContext().setAccessibleDescription( tooltip);
		menuItem.setToolTipText( tooltip);
		menuItem.setToolTipText(getMessage("applicationPanel.menu.closeAllDocumentsDescription"));
		menuItem.setActionCommand(MenuActionType.CLOSE_ALL_DOCUMENTS.name());
		menuItem.addActionListener(handler);
		menu.add(menuItem);
		
		menu.addSeparator();
		
		newCourseMenu = new JMenuItem(getMessage("applicationPanel.menu.newCourse"), KeyEvent.VK_N);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.newCourseAccelerator")).getKeyCode();
		tooltip = getMessage("applicationPanel.menu.newCourseDescription"); //12335: 2016-07-30
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		newCourseMenu.setAccelerator(keyStroke);
		newCourseMenu.getAccessibleContext().setAccessibleDescription( tooltip); //12335: 2016-07-30
		newCourseMenu.setToolTipText( tooltip);									//12335: 2016-07-30
		newCourseMenu.setActionCommand(MenuActionType.NEW_COURSE.name());
		newCourseMenu.addActionListener(handler);
		newCourseMenu.setEnabled(false);
		menu.add(newCourseMenu);
		
		menu.addSeparator();
		
		//58406#############################################################################################
		pictureMenu = new JMenuItem(getMessage("applicationPanel.menu.importPicture"), KeyEvent.VK_F);
		tooltip = getMessage("applicationPanel.menu.importPictureDescription"); //12335: 2016-07-30
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.importPictureAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		pictureMenu.setAccelerator(keyStroke);
		pictureMenu.getAccessibleContext().setAccessibleDescription( tooltip); //12335: 2016-07-30
		pictureMenu.setToolTipText( tooltip);									//12335: 2016-07-30
		pictureMenu.setActionCommand(MenuActionType.IMPORT_PICTURE.name());
		pictureMenu.addActionListener(handler);
		pictureMenu.setEnabled(false);
		menu.add(pictureMenu);
		
		menu.addSeparator();

		menuItem = new JMenuItem(getMessage("applicationPanel.menu.restart"));
		tooltip = getMessage("applicationPanel.menu.restartDescription"); 	//12335: 2016-07-30
		menuItem.getAccessibleContext().setAccessibleDescription( tooltip); //12335: 2016-07-30
		menuItem.setToolTipText( tooltip);									//12335: 2016-07-30
		menuItem.setActionCommand(MenuActionType.RESTART.name());
		menuItem.addActionListener(handler);
		menu.add(menuItem);	
		//##################################################################################################

		
		if (!OperatingSystem.isMac()) {
			menu.addSeparator();
			
			menuItem = new JMenuItem(getMessage("applicationPanel.menu.exit"));
			accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.exitAccelerator")).getKeyCode();
			keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
			menuItem.setAccelerator(keyStroke);
			menuItem.getAccessibleContext().setAccessibleDescription("Exit the application");
			menuItem.setActionCommand(MenuActionType.EXIT.name());
			menuItem.addActionListener(handler);
			menu.add(menuItem);
		}
		
		menu = new JMenu(getMessage("applicationPanel.menu.edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		menu.getAccessibleContext().setAccessibleDescription("The edit menu");
		applicationMenu.add(menu);
		
		//< ---------------------------------57421------------------------------------->
		undoMenu = new JMenuItem(getMessage("applicationPanel.menu.undo"), KeyEvent.VK_U);
		tooltip = getMessage( "applicationPanel.menu.undoDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.undoAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		undoMenu.setAccelerator(keyStroke);
		undoMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-07-31
		undoMenu.setToolTipText( tooltip); 									//12335: 2016-07-31
		undoMenu.setActionCommand(MenuActionType.UNDO.name());
		undoMenu.addActionListener(handler);
		undoMenu.setEnabled(false);
		menu.add(undoMenu);
		
		redoMenu = new JMenuItem(getMessage("applicationPanel.menu.redo"), KeyEvent.VK_Y);
		tooltip = getMessage( "applicationPanel.menu.redoDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.redoAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		redoMenu.setAccelerator(keyStroke);
		redoMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-07-31
		redoMenu.setToolTipText( tooltip); 									//12335: 2016-07-31
		redoMenu.setActionCommand(MenuActionType.REDO.name());
		redoMenu.addActionListener(handler);
		redoMenu.setEnabled(false);
		menu.add(redoMenu);
		
		menu.addSeparator();
		//<------------------------------------------------------------------------>
		
        /*menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.copy")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        menu.addSeparator();*/
		
		// 12335: 2017-03-30
		duplicateMenu = new JMenuItem(getMessage("applicationPanel.menu.duplicate"), KeyEvent.VK_C);
		tooltip = getMessage( "applicationPanel.menu.duplicateDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.duplicateAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		duplicateMenu.setAccelerator(keyStroke);
		duplicateMenu.getAccessibleContext().setAccessibleDescription( tooltip);
		duplicateMenu.setToolTipText( tooltip);
		duplicateMenu.setActionCommand(MenuActionType.DUPLICATE.name());
		duplicateMenu.addActionListener(handler);
		duplicateMenu.setEnabled(false);
		menu.add(duplicateMenu);

		copyMenu = new JMenuItem(getMessage("applicationPanel.menu.copyTo"), KeyEvent.VK_C);
		tooltip = getMessage( "applicationPanel.menu.copyToDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.copyToAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		copyMenu.setAccelerator(keyStroke);
		copyMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		copyMenu.setToolTipText( tooltip); 									//12335: 2016-08-02
		copyMenu.setActionCommand(MenuActionType.COPYTO.name());
		copyMenu.addActionListener(handler);
		copyMenu.setEnabled(false);
		menu.add(copyMenu);
		
		//12335: 2017-03-27
		moveToMenu = new JMenuItem( getMessage( "applicationPanel.menu.moveTo"), KeyEvent.VK_M);
		tooltip = getMessage( "applicationPanel.menu.moveToDescription");
		accelerator = KeyStroke.getKeyStroke( getMessage( "applicationPanel.menu.moveToAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke( accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		moveToMenu.setAccelerator( keyStroke);
		moveToMenu.getAccessibleContext().setAccessibleDescription( tooltip);
		moveToMenu.setToolTipText( tooltip);
		moveToMenu.setActionCommand( MenuActionType.MOVETO.name());
		moveToMenu.addActionListener( handler);
		moveToMenu.setEnabled( true);
		menu.add( moveToMenu);
		
		menu.addSeparator();
		
		
		splitAtSelectedMenu = new JMenuItem(getMessage("applicationPanel.menu.splitAtSelected"), KeyEvent.VK_S);
		tooltip = getMessage( "applicationPanel.menu.splitAtSelectedDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.splitAtSelectedAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		splitAtSelectedMenu.setAccelerator(keyStroke);
		splitAtSelectedMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		splitAtSelectedMenu.setToolTipText( tooltip); 									//12335: 2016-08-02
		splitAtSelectedMenu.setActionCommand(MenuActionType.SPLIT_AT_SELECTED.name());
		splitAtSelectedMenu.addActionListener(handler);
		splitAtSelectedMenu.setEnabled(false);
		menu.add(splitAtSelectedMenu);
		
		joinMenu = new JMenuItem(getMessage("applicationPanel.menu.join"), KeyEvent.VK_J);
		tooltip = getMessage( "applicationPanel.menu.joinDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.joinAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		joinMenu.setAccelerator(keyStroke);
		joinMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		joinMenu.setToolTipText( tooltip); 									//12335: 2016-08-02
//		joinMenu.getAccessibleContext().setAccessibleDescription("Joins two or more courses.");
		joinMenu.setActionCommand(MenuActionType.JOIN.name());
		joinMenu.addActionListener(handler);
		joinMenu.setEnabled(false);
		menu.add(joinMenu);
		
		//12335: 2016-07-26
		subMenuReverse = new JMenu(getMessage("applicationPanel.menu.reverse"));
		menu.add( subMenuReverse);

		
		reverseMenu = new JMenuItem(getMessage("applicationPanel.menu.reverseCourse"), KeyEvent.VK_R);
		tooltip = getMessage( "applicationPanel.menu.reverseCourseDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.reverseCourseAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		reverseMenu.setAccelerator(keyStroke);
		reverseMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		reverseMenu.setToolTipText( tooltip); 									//12335: 2016-08-02
		reverseMenu.setActionCommand(MenuActionType.REVERSE.name());
		reverseMenu.addActionListener(handler);
		reverseMenu.setEnabled(false);
//		menu.add(reverseMenu);
		subMenuReverse.add(reverseMenu);							//12335: 2016-07-26
		
		returnCourseMenu = new JMenuItem(getMessage("applicationPanel.menu.returnCourse"));
		tooltip = getMessage( "applicationPanel.menu.returnCourseDescription");	//12335: 2016-07-31
		//accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.reverseAccelerator")).getKeyCode();
		//keyStroke = KeyStroke.getKeyStroke(null, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		//returnCourseMenu.setAccelerator(keyStroke);
		returnCourseMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		returnCourseMenu.setToolTipText( tooltip); 									//12335: 2016-08-02
		returnCourseMenu.setActionCommand(MenuActionType.RETURN.name());
		returnCourseMenu.addActionListener(handler);
		returnCourseMenu.setEnabled(false);
//		menu.add(returnCourseMenu);
		subMenuReverse.add(returnCourseMenu);						//12335: 2016-07-26
		
		JMenu viewMenu = new JMenu(getMessage("applicationPanel.menu.view"));
		tooltip = getMessage( "applicationPanel.menu.viewDescription");		//12335: 2016-08-02
		viewMenu.setMnemonic(KeyEvent.VK_V);
		viewMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		viewMenu.setToolTipText( tooltip); 									//12335: 2016-08-02
		applicationMenu.add(viewMenu);
		
//		12335: 2017-05-04
//		menu = new JMenu(getMessage("applicationPanel.menu.showView"));
//		tooltip = getMessage( "applicationPanel.menu.showViewDescription");	//12335: 2016-08-02
//		menu.setMnemonic(KeyEvent.VK_S);
//		menu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
//		menu.setToolTipText( tooltip); 									//12335: 2016-08-02
//		viewMenu.add(menu);
		
		boolean showFolder = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_FOLDER, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showFolder"), showFolder);
		tooltip = getMessage( "applicationPanel.menu.showFolderDescription");//12335: 2016-08-02
		chkMenuItem.setMnemonic(KeyEvent.VK_F);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_FOLDER.name());
		chkMenuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		chkMenuItem.setToolTipText( tooltip); 									//12335: 2016-08-02
		chkMenuItem.addActionListener(handler);
//		menu.add(chkMenuItem);													//12335: 2017-05-04
		viewMenu.add( chkMenuItem);
		
		boolean showSummary = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_SUMMARY, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showSummary"), showSummary);
		tooltip = getMessage( "applicationPanel.menu.showSummaryDescription");	//12335: 2016-08-02
		chkMenuItem.setMnemonic(KeyEvent.VK_S);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_SUMMARY.name());
		chkMenuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		chkMenuItem.setToolTipText( tooltip); 									//12335: 2016-08-02
		chkMenuItem.addActionListener(handler);
//		menu.add(chkMenuItem);													//12335: 2017-05-04
		viewMenu.add( chkMenuItem);
		
		boolean showMap = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_MAP, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showMap"), showMap);
		tooltip = getMessage( "applicationPanel.menu.showMapDescription");	//12335: 2016-08-02
		chkMenuItem.setMnemonic(KeyEvent.VK_M);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_MAP.name());
		chkMenuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		chkMenuItem.setToolTipText( tooltip); 									//12335: 2016-08-02
		chkMenuItem.addActionListener(handler);
//		menu.add(chkMenuItem);													//12335: 2017-05-04
		viewMenu.add( chkMenuItem);
		
		boolean showChart = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_CHART, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showChart"), showChart);
		tooltip = getMessage( "applicationPanel.menu.showChartDescription");//12335: 2016-08-02
		chkMenuItem.setMnemonic(KeyEvent.VK_G);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_CHART.name());
		chkMenuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		chkMenuItem.setToolTipText( tooltip); 									//12335: 2016-08-02
		chkMenuItem.addActionListener(handler);
//		menu.add(chkMenuItem);													//12335: 2017-05-04
		viewMenu.add( chkMenuItem);
		
		boolean showTable = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_DATA, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showData"), showTable);
		tooltip = getMessage( "applicationPanel.menu.showDataDescription");//12335: 2016-08-02
		chkMenuItem.setMnemonic(KeyEvent.VK_T);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_TABLE.name());
		chkMenuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		chkMenuItem.setToolTipText( tooltip); 									//12335: 2016-08-02
		chkMenuItem.addActionListener(handler);
//		menu.add(chkMenuItem);													//12335: 2017-05-04
		viewMenu.add( chkMenuItem);
		
		boolean showLog = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_LOG, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showLog"), showLog);
		tooltip = getMessage( "applicationPanel.menu.showLogDescription");//12335: 2016-08-02
		chkMenuItem.setMnemonic(KeyEvent.VK_L);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_LOG.name());
		chkMenuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
		chkMenuItem.setToolTipText( tooltip); 									//12335: 2016-08-02
		chkMenuItem.addActionListener(handler);
//		menu.add(chkMenuItem);													//12335: 2017-05-04
		viewMenu.add( chkMenuItem);
		
//		viewMenu.addSeparator();			// 12335: 2017-05-03
//		
//		boolean showGridlines = TrackIt.getPreferences().getBooleanPreference( 	//12335: 2017-05-01
//					Constants.PrefsCategories.GLOBAL, null, 
//					Constants.GlobalPreferences.SHOW_GRIDLINES, true);
//		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showGridlines"), //12335: 2017-05-01
//					showGridlines);
//		tooltip = getMessage( "applicationPanel.menu.showGridlinesDescription");//12335: 2016-08-02
//		chkMenuItem.setMnemonic(KeyEvent.VK_G);
//		chkMenuItem.setActionCommand(MenuActionType.SHOW_GRIDLINES.name());
//		chkMenuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-02
//		chkMenuItem.setToolTipText( tooltip); 									//12335: 2016-08-02
//		chkMenuItem.addActionListener(handler);
//		viewMenu.add(chkMenuItem);
		
		menu = new JMenu(getMessage("applicationPanel.menu.tools"));
		menu.setMnemonic(KeyEvent.VK_T);
		tooltip = getMessage( "applicationPanel.menu.toolsDescription");//12335: 2016-08-03
		menu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		menu.setToolTipText( tooltip); 									//12335: 2016-08-03
		applicationMenu.add(menu);
		
		newSearch = new JMenuItem(getMessage("applicationPanel.menu.newSearch"), KeyEvent.VK_S);
		tooltip = getMessage( "applicationPanel.menu.newSearchDescription");	//12335: 2016-07-31
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.newSearchAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		newSearch.setAccelerator(keyStroke);
		newSearch.getAccessibleContext().setAccessibleDescription( tooltip);
		newSearch.setToolTipText( tooltip);
		newSearch.setActionCommand(MenuActionType.NEW_SEARCH.name());
		newSearch.addActionListener(handler);
		newSearch.setEnabled( true);
		menu.add(newSearch);
		
		calendarMenu = new JMenuItem(getMessage("applicationPanel.menu.calendarMenu"), KeyEvent.VK_C);
		tooltip = getMessage( "applicationPanel.menu.calendarMenuDescription");
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.calendarMenuAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		calendarMenu.setAccelerator(keyStroke);
		calendarMenu.getAccessibleContext().setAccessibleDescription( tooltip);
		calendarMenu.setToolTipText( tooltip);
		calendarMenu.setActionCommand(MenuActionType.CALENDAR_MENU.name());
		calendarMenu.addActionListener(handler);
		calendarMenu.setEnabled( true);
		menu.add(calendarMenu);
		
		menu.addSeparator();
		
		subMenu = new JMenu( getMessage("applicationPanel.menu.climbsAndDescents"));
		tooltip = getMessage( "applicationPanel.menu.climbsAndDescentsDescription");//12335: 2016-08-03
		subMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		subMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		menu.add( subMenu);
		
		detectClimbsDescentsMenu = new JMenuItem(getMessage("applicationPanel.menu.detectClimbsAndDescents"), KeyEvent.VK_D);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		detectClimbsDescentsMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.detectClimbsAndDescentsDescription");//12335: 2016-08-03
		detectClimbsDescentsMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		detectClimbsDescentsMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		detectClimbsDescentsMenu.setActionCommand(MenuActionType.DETECT_CLIMBS_AND_DESCENTS.name());
		detectClimbsDescentsMenu.addActionListener(handler);
		detectClimbsDescentsMenu.setEnabled(false);
//		menu.add(detectClimbsDescentsMenu);					// 12335: 2016-07-05
		subMenu.add( detectClimbsDescentsMenu);
		
		markingMenu = new JMenuItem(getMessage("applicationPanel.menu.marking"), KeyEvent.VK_M);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		markingMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.markingDescription");//12335: 2016-08-03
		markingMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		markingMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		markingMenu.setActionCommand(MenuActionType.MARKING.name());
		markingMenu.addActionListener(handler);
		markingMenu.setEnabled(false);
//		menu.add(markingMenu);								// 12335: 2016-07-05
		subMenu.add( markingMenu);
		
		colorGradingMenu = new JMenuItem(getMessage("applicationPanel.menu.colorGrading"), KeyEvent.VK_L);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		colorGradingMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.colorGradingDescription");//12335: 2016-08-03
		colorGradingMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		colorGradingMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		colorGradingMenu.setActionCommand(MenuActionType.COLOR_GRADING.name());
		colorGradingMenu.addActionListener(handler);
		colorGradingMenu.setEnabled(false);
//		menu.add(colorGradingMenu);							// 12335: 2016-07-05
		subMenu.add( colorGradingMenu);
		
		setPaceMenu = new JMenuItem(getMessage("applicationPanel.menu.setPace"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+ KeyEvent.SHIFT_DOWN_MASK);
		setPaceMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.setPaceDescription");//12335: 2016-08-03
		setPaceMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		setPaceMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		setPaceMenu.setActionCommand(MenuActionType.SET_PACE.name());
		setPaceMenu.addActionListener(handler);
		setPaceMenu.setEnabled(false);
		menu.add(setPaceMenu);
		
		consolidationMenu = new JMenuItem(getMessage("applicationPanel.menu.consolidation"), KeyEvent.VK_C);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.ALT_DOWN_MASK);
		consolidationMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.consolidationDescription");//12335: 2016-08-03
		consolidationMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		consolidationMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		consolidationMenu.setActionCommand(MenuActionType.CONSOLIDATION.name());
		consolidationMenu.addActionListener(handler);
		consolidationMenu.setEnabled(false);
		menu.add(consolidationMenu);
		
		simplificationMenu = new JMenuItem(getMessage("applicationPanel.menu.simplification"), KeyEvent.VK_S);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		simplificationMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.simplificationDescription");//12335: 2016-08-03
		simplificationMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		simplificationMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		simplificationMenu.setActionCommand(MenuActionType.SIMPLIFICATION.name());
		simplificationMenu.addActionListener(handler);
		simplificationMenu.setEnabled(false);
		menu.add(simplificationMenu);
		
		subMenu = new JMenu(getMessage("applicationPanel.menu.pauses"));
		tooltip = getMessage( "applicationPanel.menu.pausesDescription");	//12335: 2016-08-03
		subMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		subMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		menu.add( subMenu);
		
		detectPausesMenu = new JMenuItem(getMessage("applicationPanel.menu.detectPauses"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		detectPausesMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.detectPausesDescription");//12335: 2016-08-03
		detectPausesMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		detectPausesMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		detectPausesMenu.setActionCommand(MenuActionType.DETECT_PAUSES.name());
		detectPausesMenu.addActionListener(handler);
		detectPausesMenu.setEnabled(false);
//		menu.add(detectPausesMenu);					// 12335: 2016-07-06
		subMenu.add( detectPausesMenu);
		
		
		menu.addSeparator();
		
		addPauseMenu = new JMenuItem(getMessage("applicationPanel.menu.addPause"), KeyEvent.VK_A);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.ALT_DOWN_MASK);
		addPauseMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.addPauseDescription");//12335: 2016-08-03
		addPauseMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		addPauseMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		addPauseMenu.setActionCommand(MenuActionType.ADD_PAUSE.name());
		addPauseMenu.addActionListener(handler);
		addPauseMenu.setEnabled(false);
//		menu.add(addPauseMenu);						// 12335: 2016-07-06
		subMenu.add( addPauseMenu);
		
		changePauseDurationMenu = new JMenuItem(getMessage("applicationPanel.menu.changePause"), KeyEvent.VK_C);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.ALT_DOWN_MASK);
		changePauseDurationMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.changePauseDescription");//12335: 2016-08-03
		changePauseDurationMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		changePauseDurationMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		changePauseDurationMenu.setActionCommand(MenuActionType.CHANGE_PAUSE_DURATION.name());
		changePauseDurationMenu.addActionListener(handler);
		changePauseDurationMenu.setEnabled(false);
//		menu.add(changePauseDurationMenu);			// 12335: 2016-07-06
		subMenu.add( changePauseDurationMenu);
		
		removePauseMenu = new JMenuItem(getMessage("applicationPanel.menu.removePause"), KeyEvent.VK_R);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.ALT_DOWN_MASK);
		removePauseMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.removePauseDescription");//12335: 2016-08-03
		removePauseMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		removePauseMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		removePauseMenu.setActionCommand(MenuActionType.REMOVE_PAUSE.name());
		removePauseMenu.addActionListener(handler);
		removePauseMenu.setEnabled(false);
//		menu.add(removePauseMenu);					// 12335: 2016-07-06
		subMenu.add( removePauseMenu);
		
		removePausesMenu = new JMenuItem(getMessage("applicationPanel.menu.removePauses"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.ALT_DOWN_MASK);
		removePausesMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.removePausesDescription");//12335: 2016-08-03
		removePausesMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		removePausesMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		removePausesMenu.setActionCommand(MenuActionType.REMOVE_PAUSES.name());
		removePausesMenu.addActionListener(handler);
		removePausesMenu.setEnabled(false);
//		menu.add(removePausesMenu);					// 12335: 2016-07-06
		subMenu.add( removePausesMenu);
		
		menu.addSeparator();
		
		subMenu = new JMenu( getMessage( "applicationPanel.menu.segments"));
		tooltip = getMessage( "applicationPanel.menu.segmentsDescription");//12335: 2016-08-03
		subMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		subMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		menu.add( subMenu);
		
		createSegmentMenu = new JMenuItem(getMessage("applicationPanel.menu.createSegment"), KeyEvent.VK_G);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		createSegmentMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.createSegmentDescription");//12335: 2016-08-03
		createSegmentMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		createSegmentMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		createSegmentMenu.setActionCommand(MenuActionType.CREATE_SEGMENT.name());
		createSegmentMenu.addActionListener(handler);
		createSegmentMenu.setEnabled(false);
//		menu.add(createSegmentMenu);				// 12335: 2016-07-06
		subMenu.add( createSegmentMenu);
		
		splitIntoSegmentsMenu = new JMenuItem(getMessage("applicationPanel.menu.splitIntoSegments"), KeyEvent.VK_S);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		splitIntoSegmentsMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.splitIntoSegmentsDescription");//12335: 2016-08-03
		splitIntoSegmentsMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		splitIntoSegmentsMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		splitIntoSegmentsMenu.setActionCommand(MenuActionType.SPLIT_INTO_SEGMENTS.name());
		splitIntoSegmentsMenu.addActionListener(handler);
		splitIntoSegmentsMenu.setEnabled(false);
//		menu.add(splitIntoSegmentsMenu);			// 12335: 2016-07-06
		subMenu.add( splitIntoSegmentsMenu);
		
		compareSegmentsMenu = new JMenuItem(getMessage("applicationPanel.menu.compareSegments"), KeyEvent.VK_C);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		compareSegmentsMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.compareSegmentsDescription");//12335: 2016-08-03
		compareSegmentsMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		compareSegmentsMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		compareSegmentsMenu.setActionCommand(MenuActionType.COMPARE_SEGMENTS.name());
		compareSegmentsMenu.addActionListener(handler);
		compareSegmentsMenu.setEnabled(false);
//		menu.add(compareSegmentsMenu);				// 12335: 2016-07-06
		subMenu.add( compareSegmentsMenu);
		
		menu.addSeparator();
		
		testMenu = new JMenuItem( getMessage( "applicationPanel.menu.test"), KeyEvent.VK_J);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		testMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.testDescription");		//12335: 2016-08-03
		testMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		testMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		testMenu.setActionCommand(MenuActionType.TEST.name());
		testMenu.addActionListener(handler);
		menu.add(testMenu);
		
		menu.addSeparator();
		
		autoLocatePicturesMenu = new JMenuItem(getMessage("applicationPanel.menu.autoLocatePictures"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		autoLocatePicturesMenu.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.autoLocatePicturesDescription");//12335: 2016-08-03
		autoLocatePicturesMenu.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		autoLocatePicturesMenu.setToolTipText( tooltip); 									//12335: 2016-08-03
		autoLocatePicturesMenu.setActionCommand(MenuActionType.AUTO_LOCATE_PICTURES.name());
		autoLocatePicturesMenu.addActionListener(handler);
		autoLocatePicturesMenu.setEnabled(false);
		menu.add(autoLocatePicturesMenu);
		
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.altitudeSmoothing"), KeyEvent.VK_A);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		tooltip = getMessage( "applicationPanel.menu.altitudeSmoothingDescription");//12335: 2016-08-03
		menuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
		menuItem.setToolTipText( tooltip); 									//12335: 2016-08-03
		menuItem.setActionCommand(MenuActionType.ALTITUDE_SMOOTHING.name());
		menuItem.addActionListener(handler);
		menuItem.setEnabled(false);
		menu.add(menuItem);
		
		if (!OperatingSystem.isMac()) {
			menu.addSeparator();
			menuItem = new JMenuItem(getMessage("applicationPanel.menu.preferences"), KeyEvent.VK_P);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
			menuItem.setAccelerator(keyStroke);
			tooltip = getMessage( "applicationPanel.menu.preferencesDescription");//12335: 2016-08-03
			menuItem.getAccessibleContext().setAccessibleDescription( tooltip);	//12335: 2016-08-03
			menuItem.setToolTipText( tooltip); 									//12335: 2016-08-03
			menuItem.setActionCommand(MenuActionType.MENU_PREFERENCES.name());
			menuItem.addActionListener(handler);
			menu.add(menuItem);
			
			menu = new JMenu(getMessage("applicationPanel.menu.about"));
		    //menu.setMnemonic(KeyEvent.VK_F1);
		    
		    menu.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				
				@Override
				public void mousePressed(MouseEvent e) {					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {					
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					new AboutDialog(TrackIt.getApplicationFrame());
					
				}
			});
			applicationMenu.add(menu);
		}
		
	}

	private void setUp(JMenuBar applicationMenu) {
		if (OperatingSystem.isMac()) {
			try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                applicationMenu.setUI((MenuBarUI) Class.forName("com.apple.laf.AquaMenuBarUI").newInstance());
            } catch (Exception ex) {
                logger.debug("Could not set macosx application menu.");
            }
        }
	}
}