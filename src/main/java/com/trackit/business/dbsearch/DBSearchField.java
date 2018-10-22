/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson
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

package com.trackit.business.dbsearch;

public enum DBSearchField {
	
	NOP              ( "",                  ""   ),
	Filepath         ( "GPSFiles.Filepath", " = "),
	TrackName		 ( "GPSFiles.Name",     " = "),
	IsActivity       ( "IsActivity",        " = "),
	MinimumDistance  ( "Distance",          " >= "),
	MaximumDistance  ( "Distance",          " <= "),
	MinimumLatitude  ( "MinLatitude",       " >= "),
	Sport            ( "Sport",             " = "),
	SubSport         ( "SubSport",          " = "),
	MaximumLatitude  ( "MaxLatitude",       " <= "),
	MinimumLongitude ( "MinLongitude",      " >= "),
	MaximumLongitude ( "MaxLongitude",      " <= "),
	MinimumStartTime ( "StartTime",         " >= "),
	MaximumStartTime ( "StartTime",         " <= "),
	MinimumEndTime   ( "EndTime",           " >= "),
	MaximumEndTime   ( "EndTime",           " <= "),
	MinimumAscent    ( "Ascent",            " >= "),
	MaximumAscent    ( "Ascent",            " <= "),
	MinimumDescent   ( "Descent",           " >= "),
	MaximumDescent   ( "Descent",           " <= "),
	TrackState       ( "TrackState",        " = " ),
	TrackDifficulty  ( "TrackDifficulty",   " = " ),
	CircularPath     ( "CircularPath",      " = " ),
	
	MediaFilepath 	 ( "Container", 		" = " ),
	MediaTrackName	 ( "ParentName", 		" = " )
	;
		
	private String dbFieldID;
	private String dbFieldOperator;
	
	private DBSearchField( String dbFieldID, String dbFieldOperator) {
		this.dbFieldID       = dbFieldID;
		this.dbFieldOperator = dbFieldOperator;
	}
	
	public String getFieldId() {
		return dbFieldID;
	}
	
	public String getOperator() {
		return dbFieldOperator;
	}
	
	public String getValue() {
		return dbFieldID + dbFieldOperator;
	}

}
