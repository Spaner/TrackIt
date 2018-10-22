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
package com.trackit.presentation.view.map.provider.armysurvey;

import com.trackit.business.common.Messages;

public enum MilitaryMapResolution {
	JPG_6401x4001(6401, 4000, 12, 23, "jpg"),
	JPG_5336x3336(5336, 3336, 24, 23, "jpg");
	
	private int width;
	private int height;
	private int rows;
	private int cols;
	private String fileFormat;
	
	private MilitaryMapResolution(int width, int height, int rows, int cols, String fileFormat) {
		this.width = width;
		this.height = height;
		this.rows = rows;
		this.cols = cols;
		this.fileFormat = fileFormat;
	}
	
	private String getName() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getCols() {
		return cols;
	}
	
	public String getFileFormat() {
		return fileFormat;
	}
	
	private static final String[] messageCodes = { "militaryMapResolution.jpg6401x4001", "militaryMapResolution.jpg5336x3336" };
	
	public static MilitaryMapResolution lookup(String resolution) {
		for (MilitaryMapResolution currentResolution : values()) {
			if (resolution.equals(currentResolution.getName())) {
				return currentResolution;
			}
		}
		
		return null;
	}
	
	public static String[] getResolutionNames() {
		String[] names = new String[values().length];
		for (int i = 0; i < values().length; i++) {
			names[i] = values()[i].getName();
		}
		
		return names;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}