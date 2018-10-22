package com.trackit.business.dbsearch;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.trackit.business.utilities.UniqueValidFilenamesList;

public class DBSearchActivitiesAndCourses {
	
	private HashMap< DBSearchField, String> searchMap;
	private ArrayList<String> filenames = null;
	private Iterator< String> filenamesIterator;
	private String            currentFilename = null;
	DBSearch                  dbSearch  = DBSearch.getInstance();
	private int				  mediaMask = 0;
	
	public DBSearchActivitiesAndCourses( HashMap< DBSearchField, String> searchMap, int mediaMask) {
		this.searchMap = new HashMap<>( searchMap);
		List<String> unfiltered = dbSearch.getData( searchMap, mediaMask);
		if ( unfiltered != null && !unfiltered.isEmpty() ) {
			filenames = (new UniqueValidFilenamesList( unfiltered)).getFilenames();
			filenamesIterator = filenames.iterator();
			this.mediaMask = mediaMask;
		}
	}
	
	public boolean hasActivitiesOrCourses() {
		return filenames != null;
	}
	
	public String getNext() {
		if ( filenames != null && filenamesIterator.hasNext() ) {
			currentFilename = filenamesIterator.next();
			searchMap.put( DBSearchField.Filepath, currentFilename);
		}
		else
			currentFilename = null;
		return currentFilename;
	}
	
	public List<String> getActivities() {
		if ( currentFilename != null ) {
			searchMap.put( DBSearchField.IsActivity, "1");
			return dbSearch.getData( searchMap, mediaMask);
		}
		;
		return null;
	}
	
	public List<String> getCourses() {
		if ( currentFilename != null ) {
			searchMap.put( DBSearchField.IsActivity, "0");
			return dbSearch.getData( searchMap, mediaMask);
		}
		return null;		
	}

}
