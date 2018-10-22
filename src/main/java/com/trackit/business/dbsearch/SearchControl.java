/*
 * This file is part of Track It!.
 * Copyright (C) 2017 Diogo Xavier
 * Copyright (C) 2018 João Brisson Lopes
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

import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.GeographicBoundingBox;
import com.trackit.presentation.search.SearchInterface;


public class SearchControl {

	private static SearchControl searchControl;
	public HashMap<Integer, SearchObject> searchHistory;
//	public SearchInterface interfaces = null;					//2018-04-11: 12335 - commented
	public int historyAux;
	public GeographicBoundingBox generic = new GeographicBoundingBox();
	
	
	public SearchControl() {
		initSearchControl();
	}

	static {
		searchControl = new SearchControl();
	}

	public synchronized static SearchControl getInstance() {
		return searchControl;
	}
	
	public void initSearchControl() {
		historyAux = 0;
	}
	
	public boolean newSearch(boolean newSearch) {
// 2018-04-09: 12335 - previous search results should only be removed if the next search
//                     returns any results
// 2018-04-10: 12335 - reset should be left to the user
//                     SearchInterface instantiation is the task of the static class member
//		existingInterface.resetUI();
//		DocumentManager.getInstance().clearDocumentContents( 
//		DocumentManager.getInstance().getDefaultCollectionDocument());
//		SearchInterface existingInterface = SearchInterface.getInstance();
//		if(existingInterface.equals(null)){
//			existingInterface = new SearchInterface(Messages.getMessage("search.dialog.simple"), null, null, null, null,null,null, new GeographicBoundingBox(), null, null, null, null, null, null, null);
//		}
//		else{
//			if(newSearch==true){
//				existingInterface.setVisible(true);
//			}
//			else{
//				existingInterface.setVisible(true);
//			}
//			
//		}
		SearchInterface.getInstance().setVisible( true);
		return false;
	}
	
	public boolean newSearchCoordinates( GeographicBoundingBox coordinates){
// 2018-04-09: 12335 - previous search results should only be removed if the next search
//      returns any results
//		DocumentManager.getInstance().clearDocumentContents( 
//	  	DocumentManager.getInstance().getDefaultCollectionDocument());
		SearchInterface searchInterface = SearchInterface.getInstance();
		searchInterface.setVisible(true);
		searchInterface.addCoordinates(coordinates);
		return false;
	}
	
// 2018-04-11: 12335 - commented 
//		public boolean refineSearch( GeographicBoundingBox coordinates) {
//		interfaces.addCoordinates(coordinates);
//		return false;
//	}

// 2018-04-11: 12335: commented
//	public void addInterface(SearchInterface searchInterface){
//		interfaces = searchInterface;
//	}
	
	private void dummy() {
		JOptionPane.showMessageDialog(
				TrackIt.getApplicationFrame(),
				"Implementation pending", "WARNING",
						JOptionPane.WARNING_MESSAGE);

	}
	
	public void addSearchObject(SearchObject object){
		searchHistory.put(historyAux, object);
		historyAux++;
	}
}
