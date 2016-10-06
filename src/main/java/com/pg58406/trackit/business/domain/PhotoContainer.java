/*
 * This file is part of Track It!.
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
package com.pg58406.trackit.business.domain;

import java.io.File;
import java.util.List;

public interface PhotoContainer {
	
	public List<Picture> getPictures();
	
	public void addPictures(File[] files);			// 12335: 2015-10-03
//	public void addPicture(File file);
	
	public void removePicture(Picture pic);
	
	// 12335: 2015-09-17 - to speed up operation and solve a bug
	public void removePictures();

	public String getFilepath();
	
	public String getName();

}
