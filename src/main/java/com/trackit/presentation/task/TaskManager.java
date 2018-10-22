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
package com.trackit.presentation.task;

import org.apache.log4j.Logger;

import com.trackit.business.operation.event.OperationEventListener;
import com.trackit.business.operation.event.OperationEventManager;

public class TaskManager implements OperationEventListener {
	private static TaskManager taskManager;
	private Logger logger = Logger.getLogger(TaskManager.class.getName());
	
	private TaskManager() {
	}
	
	public synchronized static TaskManager getInstance() {
		if (taskManager == null) {
			taskManager = new TaskManager();
			OperationEventManager.getInstance().register(taskManager);
		}
		return taskManager;
	}
	
	public void run(Task task) {
		logger.debug("Running task...");
		task.execute();
	}

	@Override
	public void operationStarted(String title, String message) {
		logger.debug("Operation started: creating progress monitor!");
	}

	@Override
	public void operationFinished(final String message) {
		logger.debug("Operation finished: destroying progress monitor!");
	}

	@Override
	public void operationProgress(final int progress, final String message) {
		logger.debug("Operation had progress: updating progress monitor!");
	}
}
