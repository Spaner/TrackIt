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
package com.trackit.business.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.trackit.business.common.Messages;
import com.trackit.business.exception.TrackItException;

public class CourseLap extends Lap {
	private Course parent;
	
	public CourseLap(Course parent) {
		super();
		this.parent = parent;
	}

	@Override
	public Course getParent() {
		return parent;
	}
	
	public void setParent(Course parent) {
		this.parent = parent;
	}

	@Override
	public List<Track> getTracks() {
		return Collections.unmodifiableList(getParent().getTracks(firstTrack, lastTrack));
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		return Collections.unmodifiableList(getParent().getTrackpoints(getStartTime(), getEndTime()));
	}

	@Override
	public List<Length> getLengths() {
		return new ArrayList<Length>();
	}

	@Override
	public String getFolderTreeItemName() {
		return Messages.getMessage("folderView.label.lapId", getParent().getLaps().indexOf(this) + 1);
	}
	
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	//12335: 2017-03-26
	public CourseLap clone() {
		return clone( this.parent);
	}
	
	//12335: 2017-03-26
	public CourseLap clone( Course parent) {
		CourseLap lap = new CourseLap( parent);
		lap.setTrackpoints( getParent().getTrackpoints( this.getStartTime(), this.getEndTime()));
		lap.copy( this);
		return lap;
	}
}
