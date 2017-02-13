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
package com.henriquemalheiro.trackit.business.common;



public enum FileType {
	GPX("GPX"),
	TCX("TCX"),
	FIT("FIT"),
	KML("KML"),
	FITLOG("FITLOG"),
	CSV("CSV"),
	ALL("ALL");
	
	private static final String[] descriptions = { "fileType.gpx", "fileType.tcx", "fileType.fit",
		"fileType.kml", "fileType.fitlog", "fileType.csv", "fileType.all" };
	private static final String[] filters = { "fileType.filter.gpx", "fileType.filter.tcx",
		"fileType.filter.fit", "fileType.filter.kml", "fileType.filter.fitlog", "fileType.filter.csv", "fileType.filter.all" };
	
	private String extension;
	
	private FileType(String extension) {
		this.extension = extension;
	}
	
	public String getExtension() {
		return extension.toLowerCase();
	}

	public String getDescription() {
		return Messages.getMessage(descriptions[this.ordinal()]);
	}

	public String getFilterName() {
		return Messages.getMessage(filters[this.ordinal()]);
	}
	
	
	public static FileType lookup(String extension) {
		for (FileType fileType : values()) {
			if (fileType.getExtension().equalsIgnoreCase(extension)) {
				return fileType;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(descriptions[this.ordinal()]);
	}
}