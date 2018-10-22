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
package com.trackit.business.utilities;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

import com.trackit.business.common.Constants;

public class TrackItPreferences {
	private static final String PATH_SEPARATOR = "/";
	private static TrackItPreferences instance; 
	private Preferences rootNode; 
	private static final Logger logger = Logger.getLogger(TrackItPreferences.class.getName());
	
	private TrackItPreferences() {
		rootNode = Preferences.userRoot().node(Constants.PREFS_ROOT_NODE);
	}
	
	public static TrackItPreferences getInstance() {
		if (instance == null) {
			instance = new TrackItPreferences();
		}
		
		return instance;
	}
	
	public void setPreference(String category, String subCategory, String key, String value) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		node.put(key, value);
		persist();
	}
	
	public void setPreference(String category, String subCategory, String key, int value) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		node.putInt(key, value);
		persist();
	}
	
	public void setPreference(String category, String subCategory, String key, double value) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		node.putDouble(key, value);
		persist();
	}
	
	public void setPreference(String category, String subCategory, String key, boolean value) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		node.putBoolean(key, value);
		persist();
	}
	
	public String getPreference(String category, String subCategory, String key, String defaultValue) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		return node.get(key, defaultValue);
	}
	
	public int getIntPreference(String category, String subCategory, String key, int defaultValue) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		return node.getInt(key, defaultValue);
	}
	
	public double getDoublePreference(String category, String subCategory, String key, double defaultValue) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		return node.getDouble(key, defaultValue);
	}
	
	public boolean getBooleanPreference(String category, String subCategory, String key, boolean defaultValue) {
		Preferences node = rootNode.node(getPath(category, subCategory));
		return node.getBoolean(key, defaultValue);
	}

	private void persist() {
		try {
			rootNode.flush();
		} catch (BackingStoreException e) {
			logger.error("It was not possible to store user preferences.");
		}
	}
	
	private String getPath(String category, String subCategory) {
		if (category == null || category.isEmpty()) {
			return "";
		} else if (subCategory == null || subCategory.isEmpty()) {
			return PATH_SEPARATOR + category;
		} else {
			return PATH_SEPARATOR + category + PATH_SEPARATOR + subCategory;
		}
	}
}