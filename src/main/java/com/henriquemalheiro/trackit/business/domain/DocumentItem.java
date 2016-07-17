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

import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.operation.ConsolidationLevel;
import com.henriquemalheiro.trackit.presentation.event.Publishable;
import com.henriquemalheiro.trackit.presentation.task.ActionType;
import com.henriquemalheiro.trackit.presentation.view.data.DataViewElement;
import com.henriquemalheiro.trackit.presentation.view.map.MapObject;

public interface DocumentItem extends Publishable, Visitable, MapObject, DataViewElement {
	public long getId();
	public void setId(long id);
	
	public Object get(String fieldName);
	public void set(String fieldName, Object value);
	
	public boolean isActivity();
	public boolean isCourse();
	
	public DocumentItem getParent();

	public List<Lap> getLaps();
	public List<Track> getTracks();
	public List<Trackpoint> getTrackpoints();
	public List<Event> getEvents();
	public List<DeviceInfo> getDevices();
	public List<CoursePoint> getCoursePoints();
	public List<TrackSegment> getSegments();
	public BoundingBox2<Location> getBounds();
	
	public String getDocumentItemName();
	
	public com.henriquemalheiro.trackit.business.common.FileType[] getSupportedFileTypes();
	public List<ActionType> getSupportedActions();
	
	public void consolidate(ConsolidationLevel level);
	
	public Object getAttribute(String key);
	public void setAttribute(String key, Object value);
	public boolean isSegment();
}
