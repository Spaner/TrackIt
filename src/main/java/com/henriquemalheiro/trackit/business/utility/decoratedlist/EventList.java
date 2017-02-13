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
package com.henriquemalheiro.trackit.business.utility.decoratedlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class EventList<E> extends ListDecorator<E> implements EventListBroadcaster<E> {
	private List<E> decoratedList;
	private List<EventListener<E>> listeners;
	private boolean enabledEvents;
	
	public EventList(List<E> list) {
		decoratedList = list;
		listeners = new ArrayList<EventListener<E>>();
		enabledEvents = true;
	}
	
	public int size() {
		return decoratedList.size();
	}

	public boolean isEmpty() {
		return decoratedList.isEmpty();
	}

	public boolean contains(Object object) {
		return decoratedList.contains(object);
	}

	public Iterator<E> iterator() {
		return decoratedList.iterator();
	}

	public Object[] toArray() {
		return decoratedList.toArray();
	}

	public <T> T[] toArray(T[] array) {
		return decoratedList.toArray(array);
	}

	public boolean add(E element) {
		List<E> oldElements = new ArrayList<E>();
		List<E> newElements = new ArrayList<E>();
		newElements.add(element);
		
		ListEvent<E> beforeEvent = new ListEvent<E>(ListEvent.EventType.BEFORE_ADD, oldElements, newElements);
		notifyListeners(beforeEvent);
		
		boolean result = decoratedList.add(element);

		ListEvent<E> afterEvent = new ListEvent<E>(ListEvent.EventType.AFTER_ADD, oldElements, newElements);
		notifyListeners(afterEvent);
		
		return result;
	}

	public boolean remove(Object object) {
		List<E> oldElements = new ArrayList<E>();
		List<E> newElements = new ArrayList<E>();
		
		int index = decoratedList.indexOf(object);
		E element = null;
		if (index != -1) {
			element = decoratedList.get(index);
		}
		
		oldElements.add(element);
		ListEvent<E> beforeEvent = new ListEvent<E>(ListEvent.EventType.BEFORE_REMOVE, oldElements, newElements);
		notifyListeners(beforeEvent);
		
		boolean result = decoratedList.remove(object);

		oldElements.add(element);
		ListEvent<E> afterEvent = new ListEvent<E>(ListEvent.EventType.AFTER_REMOVE, oldElements, newElements);
		notifyListeners(afterEvent);
		
		return result;
	}

	public boolean containsAll(Collection<?> collection) {
		return decoratedList.containsAll(collection);
	}

	public boolean addAll(Collection<? extends E> collection) {
		return decoratedList.addAll(collection);
	}

	public boolean addAll(int index, Collection<? extends E> collection) {
		return decoratedList.addAll(collection);
	}

	public boolean removeAll(Collection<?> collection) {
		List<E> oldElements = new ArrayList<E>();
		List<E> newElements = new ArrayList<E>();
		
		for (Object object : collection) {
			int index = decoratedList.indexOf(object);
			
			E element = null;
			if (index != -1) {
				element = decoratedList.get(index);
			}
			
			oldElements.add(element);
		}
		
		ListEvent<E> beforeEvent = new ListEvent<E>(ListEvent.EventType.BEFORE_REMOVE_ALL, oldElements, newElements);
		notifyListeners(beforeEvent);
		
		boolean result = decoratedList.removeAll(collection);

		ListEvent<E> afterEvent = new ListEvent<E>(ListEvent.EventType.AFTER_REMOVE_ALL, oldElements, newElements);
		notifyListeners(afterEvent);
		
		return result;
	}

	public boolean retainAll(Collection<?> collection) {
		return decoratedList.retainAll(collection);
	}

	public void clear() {
		decoratedList.clear();
	}

	public E get(int index) {
		return decoratedList.get(index);
	}

	public E set(int index, E element) {
		return decoratedList.set(index, element);
	}

	public void add(int index, E element) {
		decoratedList.add(index, element);
	}

	public E remove(int index) {
		return decoratedList.remove(index);
	}

	public int indexOf(Object object) {
		return decoratedList.indexOf(object);
	}

	public int lastIndexOf(Object object) {
		return decoratedList.lastIndexOf(object);
	}

	public ListIterator<E> listIterator() {
		return new EventListIterator<E>(decoratedList.listIterator());
	}

	public ListIterator<E> listIterator(int index) {
		return new EventListIterator<E>(decoratedList.listIterator(index));
	}

	public List<E> subList(int fromIndex, int toIndex) {
		EventList<E> subList = new EventList<E>(decoratedList.subList(fromIndex, toIndex));
		
		for (EventListener<E> listener : listeners) {
			subList.addListener(listener);
		}
		
		return subList;
	}

	public List<EventListener<E>> getListeners() {
		return listeners;
	}
	
	public void addListener(EventListener<E> listener) {
		listeners.add(listener);
	}

	public void removeListener(EventListener<E> listener) {
		listeners.remove(listener);
	}

	public void notifyListeners(ListEvent<E> event) {
		if (!enabledEvents) {
			return;
		}
		
		for (EventListener<E> listener : listeners) {
			listener.processEvent(event);
		}
	}
	
	public void enableEvents() {
		enabledEvents = true;
	}
	
	public void disableEvents() {
		enabledEvents = false;
	}
	
	public boolean isEventsEnabled() {
		return enabledEvents;
	}

	private class EventListIterator<E1> implements ListIterator<E> {
		final String PREVIOUS_OPERATION = "PREVIOUS_OPERATION";
		final String NEXT_OPERATION = "NEXT_OPERATION";

		private String lastFetchOperation = "";
		private ListIterator<E> decoratedListIterator;
		
		
		public EventListIterator(ListIterator<E> listIterator) {
			decoratedListIterator = listIterator;
		}
		
		public void add(E element) {
			decoratedListIterator.add(element);
		}

		public boolean hasNext() {
			return decoratedListIterator.hasNext();
		}

		public E next() {
			lastFetchOperation = NEXT_OPERATION;
			return decoratedListIterator.next();
		}

		public boolean hasPrevious() {
			return decoratedListIterator.hasPrevious();
		}

		public E previous() {
			lastFetchOperation = PREVIOUS_OPERATION;
			return decoratedListIterator.previous();
		}

		public int nextIndex() {
			return decoratedListIterator.nextIndex();
		}

		public int previousIndex() {
			return decoratedListIterator.previousIndex();
		}

		public void remove() {
			List<E> oldElements = new ArrayList<E>();
			List<E> newElements = new ArrayList<E>();
			
			E element = null;
			if (NEXT_OPERATION.equals(lastFetchOperation)) {
				element = previous();
				next();
			} else if (PREVIOUS_OPERATION.equals(lastFetchOperation)) {
				element = next();
				previous();
			}
			oldElements.add(element);
			
			ListEvent<E> beforeEvent = new ListEvent<E>(ListEvent.EventType.BEFORE_REMOVE, oldElements, newElements);
			notifyListeners(beforeEvent);
			
			decoratedListIterator.remove();

			ListEvent<E> afterEvent = new ListEvent<E>(ListEvent.EventType.AFTER_REMOVE, oldElements, newElements);
			notifyListeners(afterEvent);
		}

		public void set(E element) {
			decoratedListIterator.set(element);
		}
	}
}
