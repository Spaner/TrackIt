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
package com.trackit.presentation.view.folder;

import static com.trackit.business.common.Messages.getMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.trackit.presentation.utilities.Operation;


public class MenuFactory {
	private static MenuFactory instance;
	
	private MenuFactory() {
	}
	
	public static MenuFactory getInstance() {
		if (instance == null) {
			instance = new MenuFactory();
		}
		
		return instance;
	}
	
	public JMenu createMenu(String name, List<Operation> operations) {
		JMenu menu = new JMenu(name);
		JMenuItem menuItem = null;
		
		Map<String, List<Operation>> operationGroups = groupOperations(operations);
		
		for (String group : operationGroups.keySet()) {
			List<Operation> groupOperations = operationGroups.get(group);
			
			if (groupOperations.size() == 1) {
				menuItem = createMenuItem(groupOperations.get(0));
				menu.add(menuItem);
			} else {
				JMenu subMenu = new JMenu(group);
				
				for (final Operation operation : groupOperations) {
					menuItem = createMenuItem(operation);
					subMenu.add(menuItem);
				}
				
				menu.add(subMenu);
			}
		}
		
		return menu;
	}
	
	public JPopupMenu createPopupMenu(List<Operation> operations) {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem menuItem = null;
		
		Map<String, List<Operation>> operationGroups = groupOperations(operations);
		
		for (String group : operationGroups.keySet()) {
			List<Operation> groupOperations = operationGroups.get(group);
			
			if (groupOperations.size() == 1) {
				Operation operation = groupOperations.get(0);
				 // 12335: 2015-07-31 handle "insert menu item separator" fake operation 
				if ( !operation.getName().equals(getMessage("operation.name.menuSeparator")) ) {
					menuItem = createMenuItem( operation);
					popupMenu.add(menuItem);
				}
				else
					popupMenu.addSeparator();
			} else {
				JMenu subPopupMenu = new JMenu(group);
				
				for (Operation operation : groupOperations) {
					menuItem = createMenuItem(operation);
					subPopupMenu.add(menuItem);
				}
				
				popupMenu.add(subPopupMenu);
			}
		}
		
		return popupMenu;
	}

	private JMenuItem createMenuItem(final Operation operation) {
		JMenuItem menuItem = new JMenuItem(operation.getName());
		menuItem.setAction(operation);
		menuItem.setEnabled( operation.isEnabled());
		
		return menuItem;
	}

	private Map<String, List<Operation>> groupOperations(List<Operation> operations) {
		Map<String, List<Operation>> groups = new LinkedHashMap<String, List<Operation>>();
		
		if (operations == null || operations.size() == 0) {
			return groups;
		}
		
		for (Operation operation : operations) {
			if (!groups.containsKey(operation.getGroup())) {
				groups.put(operation.getGroup(), new ArrayList<Operation>());
			}
			
			groups.get(operation.getGroup()).add(operation);
		}
		
		return groups;
	}
}
