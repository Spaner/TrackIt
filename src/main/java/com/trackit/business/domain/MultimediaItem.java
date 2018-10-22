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
package com.trackit.business.domain;

import javax.swing.ImageIcon;
import java.util.Date;

public abstract class MultimediaItem extends TrackItBaseType{
	
	// 12335: 2016-10-20: first step into generalization
	protected String name;
	protected String    filename;
	protected double    latitude;
	protected double    longitude;
	protected double    altitude;
	protected Date      timestamp;
	protected int       width;
	protected int       height;
	protected int       locationX;
	protected int       locationY;
	protected boolean   landscape;
	protected ImageIcon icon;
	protected int       iconWidth;
	protected int       iconHeight;			// 12335: 2016-10-20 end

	public MultimediaItem(){
		super();
	}
}
