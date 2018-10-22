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
package com.trackit.business.operation;

import java.util.List;

import com.trackit.business.domain.GPSDocument;
import com.trackit.business.exception.TrackItException;

public interface Operation {
	String getName();
	void process(GPSDocument document) throws TrackItException;
	void process(List<GPSDocument> document) throws TrackItException;
	
	void undoOperation(GPSDocument document) throws TrackItException; //57421
	void undoOperation(List<GPSDocument> document) throws TrackItException; //57421
	void redoOperation(GPSDocument document) throws TrackItException; //57421
	void redoOperation(List<GPSDocument> document) throws TrackItException; //57421
	
	void start(String message);
	void finish(String message);
	void setProgress(int progress, String message);
}
