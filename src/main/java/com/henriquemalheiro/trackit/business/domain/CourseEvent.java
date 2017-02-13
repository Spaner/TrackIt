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

import java.util.Arrays;
import java.util.List;

import com.henriquemalheiro.trackit.business.exception.TrackItException;

public class CourseEvent extends Event {
	private Course parent;
	
	public CourseEvent(Course parent) {
		this.parent = parent;
	}
	
	@Override
	public Trackpoint getTrackpoint() {
		List<Trackpoint> trackpoints = parent.getTrackpoints(getTime(), getTime());
		
		if (trackpoints != null && !trackpoints.isEmpty()) {
			return trackpoints.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		return Arrays.asList(new Trackpoint[] { getTrackpoint() });
	}

	@Override
	public Course getParent() {
		return parent;
	}
	
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
}
