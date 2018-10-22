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
package com.trackit.business.common;

public class TrackItTask implements Comparable<TrackItTask> {
	private Runnable task;
	private long priority;
	
	public TrackItTask(Runnable task) {
		this(task, 0);
	}
	
	public TrackItTask(Runnable task, long priority) {
		this.task = task;
		this.priority = priority;
	}

	public Runnable getTask() {
		return task;
	}

	public long getPriority() {
		return priority;
	}

	public int compareTo(TrackItTask anotherTask) {
		return (priority < anotherTask.getPriority() ? -1 : (priority == anotherTask.getPriority() ? 0 : 1));
	}
}
