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
package com.trackit.presentation.utilities;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class Operation extends AbstractAction {
	private static final long serialVersionUID = 2817873193015311279L;
	
	private static final String GROUP = "GROUP";
	private static final String ACTION = "ACTION";
	
	public Operation(String group, String name, String description, Runnable action) {
		putValue(GROUP, group);
		putValue(NAME, name);
		putValue(Action.SHORT_DESCRIPTION, description);
		putValue(ACTION, action);
	}
	
	public String getGroup() {
		return (String) getValue(GROUP);
	}
	
	public String getName() {
		return (String) getValue(NAME);
	}
	
	public String getDescription() {
		return (String) getValue(SHORT_DESCRIPTION);
	}
	
	public void actionPerformed(ActionEvent event) {
		Runnable action = (Runnable) getValue(ACTION);
		action.run();
	}
	
	//12335: 2018-07-02
	public void setEnabled( boolean enable) {
		enabled = enable;
	}
}
