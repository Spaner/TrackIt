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
package com.henriquemalheiro.trackit.presentation.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.business.domain.DocumentItem;

public class EventManager {
	private static EventManager eventManager;
	private List<EventListener> eventListeners;
	private Logger logger = Logger.getLogger(EventManager.class.getName());
	
	private EventManager() {
		eventListeners = new ArrayList<EventListener>();
	}
	
	public synchronized static EventManager getInstance() {
		if (eventManager == null) {
			eventManager = new EventManager();
		}
		return eventManager;
	}
	
	public void register(EventListener listener) {
		eventListeners.add(listener);
	}
	
	public synchronized void publish(final EventPublisher publisher, final Event event, final DocumentItem item) {
		for (EventListener listener : eventListeners) {
			if (!listener.equals(publisher)) {
				listener.process(event, item);
				logEvent(event, listener, publisher);
			}
		}
	}
	
	private void logEvent(Event event, EventListener listener, EventPublisher publisher) {
		if (event.equals(Event.TRACKPOINT_HIGHLIGHTED)) {
			//58406 - comment logger to simplify debug
			logger.trace(String.format("Dispatching event %s to %s from %s.", event, listener, publisher));
		} else {
			//58406 - comment logger to simplify debug 
			logger.debug(String.format("Dispatching event %s to %s from %s.", event, listener, publisher));
		}
	}

	public synchronized void publish(final EventPublisher publisher, final Event event,
			final DocumentItem parent, final List<? extends DocumentItem> items) {
		for (EventListener listener : eventListeners) {
			if (!listener.equals(publisher)) {
				listener.process(event, parent, items);
				//58406 - comment logger to simplify debug
				logger.debug(String.format("Dispatching list event %s with parent %s to %s.", event, parent, listener));
			}
		}
	}
}