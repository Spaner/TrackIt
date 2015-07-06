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
package com.miguelpernas.trackit.business.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Constants.GlobalPreferences;
import com.henriquemalheiro.trackit.business.common.Constants.PrefsCategories;
import com.henriquemalheiro.trackit.business.utility.TrackItPreferences;

/*public class JoinOptions {
	private static final String DEFAULT_OPTION = "constant default speed";
	private static final String BUNDLE_NAME = "MessagesBundle";
	
	private static ResourceBundle resourceBundle;
	private static String currentOption;
	
	private static List<String> availableOptions;
	
	static {
		TrackItPreferences preferences = TrackIt.getPreferences();
		
		String country = preferences.getPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.COUNTRY, DEFAULT_LOCALE.getCountry());
		String language = preferences.getPreference(Constants.PrefsCategories.GLOBAL, null,
				Constants.GlobalPreferences.LANGUAGE, DEFAULT_LOCALE.getLanguage());
		
		currentLocale = new Locale(language, country);
		
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
		initAvailableLocales();
	}
	
	public static Locale getLocale() {
		return currentLocale;
	}
	
	public static void setLocale(Locale locale) {
		currentLocale = locale;
		resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
	}
	
	public static List<Locale> getAvailableLocales() {
		return availableLocales;
	}
	
	public static String getMessage(String key) {
		try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
	}
	
	public static String getMessage(String key, Object... parameters) {
		try {
            return MessageFormat.format(resourceBundle.getString(key), parameters);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
	}
	
	private static void initAvailableLocales() {
		availableLocales = new ArrayList<Locale>();
		availableLocales.add(new Locale("en", "UK"));
		availableLocales.add(new Locale("pt", "PT"));
	}
}*/
