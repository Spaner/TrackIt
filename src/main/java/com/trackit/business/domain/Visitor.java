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

import com.trackit.business.exception.TrackItException;

public interface Visitor {
	public void visit(DocumentItem item) throws TrackItException;
	public void visit(GPSDocument item) throws TrackItException;
	public void visit(Activity activity) throws TrackItException;
	public void visit(Course course) throws TrackItException;
	public void visit(Session session) throws TrackItException;
	public void visit(Lap lap) throws TrackItException;
	public void visit(Track track) throws TrackItException;
	public void visit(Event event) throws TrackItException;
	public void visit(DeviceInfo device) throws TrackItException;
	public void visit(CoursePoint coursePoint) throws TrackItException;
	public void visit(Picture picture) throws TrackItException;//58406
}
