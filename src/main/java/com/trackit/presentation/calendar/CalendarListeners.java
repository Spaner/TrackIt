/*
 * This file is part of Track It!.
 * Copyright (C) 2017 Diogo Xavier
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
package com.trackit.presentation.calendar;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CalendarListeners implements PropertyChangeListener{

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		 String propertyName = e.getPropertyName();
	     if ("month".equals(propertyName)||"year".equals(propertyName)) {
	    	 CalendarInterface calendarInterface = CalendarInterface.getInstance();
	    	 int month = calendarInterface.getMonthChooser().getMonth();
	    	 int year = calendarInterface.getYearChooser().getYear();
	    	 calendarInterface.setNewMonth(month, year);
	    	 calendarInterface.setList( calendarInterface.getDayChooser().getDay(), month, year);
	     }
	     if("day".equals(propertyName)){
	    	 CalendarInterface calendarInterface = CalendarInterface.getInstance();
	    	 int day = calendarInterface.getDayChooser().getDay();
	    	 int month = calendarInterface.getMonthChooser().getMonth();
	    	 int year = calendarInterface.getYearChooser().getYear();
	    	 calendarInterface.setList(day, month, year);
	     }
	}
}
