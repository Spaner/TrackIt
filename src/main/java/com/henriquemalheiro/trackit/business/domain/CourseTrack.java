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
package com.henriquemalheiro.trackit.business.domain;

import java.util.List;

import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;

public class CourseTrack extends Track {
	private Course parent;
	
	public CourseTrack(Course parent) {
		super();
		this.parent = parent;
	}

	@Override
	public List<Trackpoint> getTrackpoints() {
		return parent.getTrackpoints(getStartTime(), getEndTime());
	}
	
	@Override
	public Course getParent() {
		return parent;
	}
	
	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.TRACK_SELECTED, this);
	}
}
