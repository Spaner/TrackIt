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
package com.trackit.business.utilities.decoratedlist;

import java.util.List;

public class ListEvent<E> {
	public enum EventType {
		BEFORE_ADD, AFTER_ADD, BEFORE_REMOVE, AFTER_REMOVE, BEFORE_REMOVE_ALL, AFTER_REMOVE_ALL;
	}

	private EventType event;
	private List<E> oldElements;
	private List<E> newElements;
	
	public ListEvent(EventType event, List<E> oldElements, List<E> newElements) {
		this.event = event;
		this.oldElements = oldElements;
		this.newElements = newElements;
	}

	public EventType getEvent() {
		return event;
	}

	public void setEvent(EventType event) {
		this.event = event;
	}

	public List<E> getOldElements() {
		return oldElements;
	}

	public void setOldElements(List<E> oldElements) {
		this.oldElements = oldElements;
	}

	public List<E> getNewElements() {
		return newElements;
	}

	public void setNewElements(List<E> newElements) {
		this.newElements = newElements;
	}
}
