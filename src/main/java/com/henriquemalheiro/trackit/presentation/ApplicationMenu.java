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

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.plaf.MenuBarUI;
import javax.swing.text.DefaultEditorKit;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.OperatingSystem;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Folder;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.operation.UndoManagerCustom;
import com.henriquemalheiro.trackit.presentation.task.ActionType;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.pg58406.trackit.business.utility.AboutDialog;

class ApplicationMenu {
	private JMenuBar applicationMenu;
	private JMenuItem importMenu;
	private JMenuItem exportMenu;
	private JMenuItem newCourseMenu;
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
	private JMenuItem removePausesMenu;	
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
	
	void refreshMenu(List<DocumentItem> items, UndoManagerCustom undoManager) {
		boolean singleItem = (items.size() == 1 && !(items.get(0) instanceof Folder));
		
		importMenu.setEnabled(singleItem);
		exportMenu.setEnabled(singleItem && (items.get(0).isActivity() || items.get(0).isCourse()));
		splitAtSelectedMenu.setEnabled(singleItem && items.get(0) instanceof Trackpoint && items.get(0).getParent().isCourse());
		reverseMenu.setEnabled(singleItem && items.get(0).isCourse());
		
			
		boolean joinMenuEnabled = true;
		joinMenuEnabled &= (items.size() > 1);
		for (DocumentItem item : items) {
			joinMenuEnabled &= item.isCourse();
		}
		joinMenu.setEnabled(joinMenuEnabled);
		
		
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
		logger.debug("\nUNDOMANAGER " + undoManager.canUndo() + "\n");
		undoMenu.setEnabled(undoManager.canUndo());//57421
		redoMenu.setEnabled(undoManager.canRedo());//57421
	}
	
	private boolean actionSupported(List<ActionType> supportedActions, ActionType action) {
		return supportedActions.contains(action);
	}
	
	private void updateMenuActionMap() {
		menuActionMap.put(ActionType.CONSOLIDATION, consolidationMenu);
		menuActionMap.put(ActionType.SET_PACE, setPaceMenu);
		menuActionMap.put(ActionType.DETECT_CLIMBS_DESCENTS, detectClimbsDescentsMenu);
		menuActionMap.put(ActionType.MARKING, markingMenu);
		menuActionMap.put(ActionType.NEW_COURSE, newCourseMenu);
		menuActionMap.put(ActionType.SIMPLIFICATION, simplificationMenu);
		menuActionMap.put(ActionType.REMOVE_PAUSES, removePausesMenu);
		menuActionMap.put(ActionType.IMPORT_PICTURE, pictureMenu);
		menuActionMap.put(ActionType.DETECT_PAUSES, detectPausesMenu);
		menuActionMap.put(ActionType.AUTO_LOCATE_PICTURES, autoLocatePicturesMenu);
		menuActionMap.put(ActionType.UNDO, undoMenu);
		menuActionMap.put(ActionType.REDO, redoMenu);
	}

	private void createApplicationMenu(ActionListener handler) {
		JMenu menu;
		JMenuItem menuItem;
		JCheckBoxMenuItem chkMenuItem;
		KeyStroke keyStroke;
		int accelerator;

		applicationMenu = new JMenuBar();
		setUp(applicationMenu);

		menu = new JMenu(getMessage("applicationPanel.menu.file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("The file menu");
		applicationMenu.add(menu);
		
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.newDocument"), KeyEvent.VK_N);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.newDocumentAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		menuItem.getAccessibleContext().setAccessibleDescription("Creates a new document.");
		menuItem.setActionCommand(MenuActionType.NEW_DOCUMENT.name());
		menuItem.addActionListener(handler);
		menu.add(menuItem);
		
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.openDocument"), KeyEvent.VK_O);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.openDocumentAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		menuItem.getAccessibleContext().setAccessibleDescription("Opens an existing document.");
		menuItem.setActionCommand(MenuActionType.OPEN_DOCUMENT.name());
		menuItem.addActionListener(handler);
		menu.add(menuItem);
		
		menu.addSeparator();
		
		newCourseMenu = new JMenuItem(getMessage("applicationPanel.menu.newCourse"), KeyEvent.VK_N);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.newCourseAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		newCourseMenu.setAccelerator(keyStroke);
		newCourseMenu.getAccessibleContext().setAccessibleDescription("Adds a new course.");
		newCourseMenu.setActionCommand(MenuActionType.NEW_COURSE.name());
		newCourseMenu.addActionListener(handler);
		newCourseMenu.setEnabled(false);
		menu.add(newCourseMenu);
		
		menu.addSeparator();
		//58406#############################################################################################
		pictureMenu = new JMenuItem(getMessage("applicationPanel.menu.importPicture"), KeyEvent.VK_F);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.importPictureAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		pictureMenu.setAccelerator(keyStroke);
		pictureMenu.getAccessibleContext().setAccessibleDescription("Import photographs");
		pictureMenu.setActionCommand(MenuActionType.IMPORT_PICTURE.name());
		pictureMenu.addActionListener(handler);
		pictureMenu.setEnabled(false);
		menu.add(pictureMenu);
		
		menu.addSeparator();
		//##################################################################################################
		importMenu = new JMenuItem(getMessage("applicationPanel.menu.import"), KeyEvent.VK_I);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.importAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		importMenu.setAccelerator(keyStroke);
		importMenu.getAccessibleContext().setAccessibleDescription("Import a course or activity");
		importMenu.setActionCommand(MenuActionType.IMPORT_DOCUMENT.name());
		importMenu.addActionListener(handler);
		importMenu.setEnabled(false);
		menu.add(importMenu);
		//58406#############################################################################################
		exportMenu = new JMenuItem(getMessage("applicationPanel.menu.export"), KeyEvent.VK_E);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.exportAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		exportMenu.setAccelerator(keyStroke);
		exportMenu.getAccessibleContext().setAccessibleDescription("Import a course or activity");
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
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.restart"));
		menuItem.getAccessibleContext().setAccessibleDescription("Restart the application");
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
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.undoAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		undoMenu.setAccelerator(keyStroke);
		undoMenu.getAccessibleContext().setAccessibleDescription("Undo the last performed operation.");
		undoMenu.setActionCommand(MenuActionType.UNDO.name());
		undoMenu.addActionListener(handler);
		undoMenu.setEnabled(false);
		menu.add(undoMenu);
		
		redoMenu = new JMenuItem(getMessage("applicationPanel.menu.redo"), KeyEvent.VK_Y);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.redoAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		redoMenu.setAccelerator(keyStroke);
		redoMenu.getAccessibleContext().setAccessibleDescription("Redo the last undone operation.");
		redoMenu.setActionCommand(MenuActionType.REDO.name());
		redoMenu.addActionListener(handler);
		redoMenu.setEnabled(false);
		menu.add(redoMenu);
		
		menu.addSeparator();
		//<------------------------------------------------------------------------>
		
        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.copy")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
        menu.add(menuItem);

        menu.addSeparator();
		
		splitAtSelectedMenu = new JMenuItem(getMessage("applicationPanel.menu.splitAtSelected"), KeyEvent.VK_S);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.splitAtSelectedAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		splitAtSelectedMenu.setAccelerator(keyStroke);
		splitAtSelectedMenu.getAccessibleContext().setAccessibleDescription("Splits a course at the selected trackpoint.");
		splitAtSelectedMenu.setActionCommand(MenuActionType.SPLIT_AT_SELECTED.name());
		splitAtSelectedMenu.addActionListener(handler);
		splitAtSelectedMenu.setEnabled(false);
		menu.add(splitAtSelectedMenu);
		
		joinMenu = new JMenuItem(getMessage("applicationPanel.menu.join"), KeyEvent.VK_J);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.joinAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		joinMenu.setAccelerator(keyStroke);
		joinMenu.getAccessibleContext().setAccessibleDescription("Joins two or more courses.");
		joinMenu.setActionCommand(MenuActionType.JOIN.name());
		joinMenu.addActionListener(handler);
		joinMenu.setEnabled(false);
		menu.add(joinMenu);
		
		reverseMenu = new JMenuItem(getMessage("applicationPanel.menu.reverse"), KeyEvent.VK_R);
		accelerator = KeyStroke.getKeyStroke(getMessage("applicationPanel.menu.reverseAccelerator")).getKeyCode();
		keyStroke = KeyStroke.getKeyStroke(accelerator, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		reverseMenu.setAccelerator(keyStroke);
		reverseMenu.getAccessibleContext().setAccessibleDescription("Reverses the track.");
		reverseMenu.setActionCommand(MenuActionType.REVERSE.name());
		reverseMenu.addActionListener(handler);
		reverseMenu.setEnabled(false);
		menu.add(reverseMenu);
		
		JMenu viewMenu = new JMenu(getMessage("applicationPanel.menu.view"));
		viewMenu.setMnemonic(KeyEvent.VK_V);
		viewMenu.getAccessibleContext().setAccessibleDescription("The view menu");
		applicationMenu.add(viewMenu);
		
		menu = new JMenu(getMessage("applicationPanel.menu.showView"));
		menu.setMnemonic(KeyEvent.VK_S);
		menu.getAccessibleContext().setAccessibleDescription("The show view submenu");
		viewMenu.add(menu);
		
		boolean showFolder = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_FOLDER, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showFolder"), showFolder);
		chkMenuItem.setMnemonic(KeyEvent.VK_F);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_FOLDER.name());
		chkMenuItem.addActionListener(handler);
		menu.add(chkMenuItem);
		
		boolean showSummary = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_SUMMARY, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showSummary"), showSummary);
		chkMenuItem.setMnemonic(KeyEvent.VK_S);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_SUMMARY.name());
		chkMenuItem.addActionListener(handler);
		menu.add(chkMenuItem);
		
		boolean showMap = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_MAP, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showMap"), showMap);
		chkMenuItem.setMnemonic(KeyEvent.VK_M);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_MAP.name());
		chkMenuItem.addActionListener(handler);
		menu.add(chkMenuItem);
		
		boolean showChart = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_CHART, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showChart"), showChart);
		chkMenuItem.setMnemonic(KeyEvent.VK_G);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_CHART.name());
		chkMenuItem.addActionListener(handler);
		menu.add(chkMenuItem);
		
		boolean showTable = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_DATA, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showData"), showTable);
		chkMenuItem.setMnemonic(KeyEvent.VK_T);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_TABLE.name());
		chkMenuItem.addActionListener(handler);
		menu.add(chkMenuItem);
		
		boolean showLog = TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.GLOBAL, null, Constants.GlobalPreferences.SHOW_LOG, true);
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showLog"), showLog);
		chkMenuItem.setMnemonic(KeyEvent.VK_L);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_LOG.name());
		chkMenuItem.addActionListener(handler);
		menu.add(chkMenuItem);
		
		viewMenu.addSeparator();
		
		chkMenuItem = new JCheckBoxMenuItem(getMessage("applicationPanel.menu.showGridlines"), false);
		chkMenuItem.setMnemonic(KeyEvent.VK_G);
		chkMenuItem.setActionCommand(MenuActionType.SHOW_GRIDLINES.name());
		chkMenuItem.addActionListener(handler);
		viewMenu.add(chkMenuItem);
		
		menu = new JMenu(getMessage("applicationPanel.menu.tools"));
		menu.setMnemonic(KeyEvent.VK_T);
		menu.getAccessibleContext().setAccessibleDescription("The tools menu");
		applicationMenu.add(menu);
		
		detectClimbsDescentsMenu = new JMenuItem(getMessage("applicationPanel.menu.detectClimbsAndDescents"), KeyEvent.VK_D);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		detectClimbsDescentsMenu.setAccelerator(keyStroke);
		detectClimbsDescentsMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.detectClimbsAndDescents.description"));
		detectClimbsDescentsMenu.setActionCommand(MenuActionType.DETECT_CLIMBS_AND_DESCENTS.name());
		detectClimbsDescentsMenu.addActionListener(handler);
		detectClimbsDescentsMenu.setEnabled(false);
		menu.add(detectClimbsDescentsMenu);
		
		markingMenu = new JMenuItem(getMessage("applicationPanel.menu.marking"), KeyEvent.VK_M);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		markingMenu.setAccelerator(keyStroke);
		markingMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.marking.description"));
		markingMenu.setActionCommand(MenuActionType.MARKING.name());
		markingMenu.addActionListener(handler);
		markingMenu.setEnabled(false);
		menu.add(markingMenu);
		
		setPaceMenu = new JMenuItem(getMessage("applicationPanel.menu.setPace"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()+ KeyEvent.SHIFT_DOWN_MASK);
		setPaceMenu.setAccelerator(keyStroke);
		setPaceMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.markClimbsAndDescents.description"));
		setPaceMenu.setActionCommand(MenuActionType.SET_PACE.name());
		setPaceMenu.addActionListener(handler);
		setPaceMenu.setEnabled(false);
		menu.add(setPaceMenu);
		
		consolidationMenu = new JMenuItem(getMessage("applicationPanel.menu.consolidation"), KeyEvent.VK_C);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.ALT_DOWN_MASK);
		consolidationMenu.setAccelerator(keyStroke);
		consolidationMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.consolidation.description"));
		consolidationMenu.setActionCommand(MenuActionType.CONSOLIDATION.name());
		consolidationMenu.addActionListener(handler);
		consolidationMenu.setEnabled(false);
		menu.add(consolidationMenu);
		
		simplificationMenu = new JMenuItem(getMessage("applicationPanel.menu.simplification"), KeyEvent.VK_S);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		simplificationMenu.setAccelerator(keyStroke);
		simplificationMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.simplification.description"));
		simplificationMenu.setActionCommand(MenuActionType.SIMPLIFICATION.name());
		simplificationMenu.addActionListener(handler);
		simplificationMenu.setEnabled(false);
		menu.add(simplificationMenu);
		
		detectPausesMenu = new JMenuItem(getMessage("applicationPanel.menu.detectPauses"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		detectPausesMenu.setAccelerator(keyStroke);
		detectPausesMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.detectPauses.description"));
		detectPausesMenu.setActionCommand(MenuActionType.DETECT_PAUSES.name());
		detectPausesMenu.addActionListener(handler);
		detectPausesMenu.setEnabled(false);
		menu.add(detectPausesMenu);
		
		removePausesMenu = new JMenuItem(getMessage("applicationPanel.menu.removePauses"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.ALT_DOWN_MASK);
		removePausesMenu.setAccelerator(keyStroke);
		removePausesMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.removePauses.description"));
		removePausesMenu.setActionCommand(MenuActionType.REMOVE_PAUSES.name());
		removePausesMenu.addActionListener(handler);
		removePausesMenu.setEnabled(false);
		menu.add(removePausesMenu);
		
		autoLocatePicturesMenu = new JMenuItem(getMessage("applicationPanel.menu.autoLocatePictures"), KeyEvent.VK_P);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + KeyEvent.SHIFT_DOWN_MASK);
		autoLocatePicturesMenu.setAccelerator(keyStroke);
		autoLocatePicturesMenu.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.autoLocatePictures.description"));
		autoLocatePicturesMenu.setActionCommand(MenuActionType.AUTO_LOCATE_PICTURES.name());
		autoLocatePicturesMenu.addActionListener(handler);
		autoLocatePicturesMenu.setEnabled(false);
		menu.add(autoLocatePicturesMenu);
		
		menuItem = new JMenuItem(getMessage("applicationPanel.menu.altitudeSmoothing"), KeyEvent.VK_A);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		menuItem.setAccelerator(keyStroke);
		menuItem.getAccessibleContext().setAccessibleDescription(getMessage("applicationPanel.menu.altitudeSmoothing.description"));
		menuItem.setActionCommand(MenuActionType.ALTITUDE_SMOOTHING.name());
		menuItem.addActionListener(handler);
		menuItem.setEnabled(false);
		menu.add(menuItem);
		
		if (!OperatingSystem.isMac()) {
			menu.addSeparator();
			menuItem = new JMenuItem(getMessage("applicationPanel.menu.preferences"), KeyEvent.VK_P);
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
			menuItem.setAccelerator(keyStroke);
			menuItem.getAccessibleContext().setAccessibleDescription("Manage the user preferences");
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