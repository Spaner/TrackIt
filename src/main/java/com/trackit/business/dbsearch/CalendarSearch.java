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
package com.trackit.business.dbsearch;

import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.trackit.business.domain.CalendarData;

public class CalendarSearch {
	HashMap<Date, List<HashMap<String, String>>> monthHash = new HashMap<Date, List<HashMap<String, String>>>();
	DBSearch dbSearch = new DBSearch();
	
	public HashMap<Date, HashMap<String, HashMap<List<String>, List<String>>>> getMonth(int month, int year){
		HashMap<Date, HashMap<String, HashMap<List<String>, List<String>>>> tracksPerDate = new HashMap<Date, HashMap<String, HashMap<List<String>, List<String>>>>();
		Calendar date = new GregorianCalendar(year, month, 1, 0, 0);
		Calendar date1 = new GregorianCalendar(year, month+1, 1, 0, 0);
		HashMap<String, String> listPath = dbSearch.getDataByDate(date.getTime(), date1.getTime());
		if(listPath.size()!=0){
			for(HashMap.Entry<String, String> entry : listPath.entrySet()){
				HashMap<List<String>,List<String>> activitiesCourses = new HashMap<List<String>,List<String>>();
				HashMap<String, HashMap<List<String>, List<String>>> pathTrack = new HashMap<String, HashMap<List<String>, List<String>>>();
				List<String> activities = dbSearch.getActivityByDate(entry.getValue(), date.getTime(), date1.getTime());
				List<String> courses = dbSearch.getCoursesByDate(entry.getValue(), date.getTime(), date1.getTime());
				activitiesCourses.put(activities, courses);
				pathTrack.put(entry.getValue(), activitiesCourses);
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeZone(date.getTimeZone());
				Long dateInMillis = Long.valueOf(entry.getKey());
				calendar.setTimeInMillis(dateInMillis);
				tracksPerDate.put(calendar.getTime(), pathTrack);
			}
		}
		return tracksPerDate;
	}
	
	public String getTotalMonthDistance(int month, int year){
		Calendar date = new GregorianCalendar(year, month, 1, 0, 0);
		Calendar date1 = new GregorianCalendar(year, month+1, 1, 0, 0);
//12335: 2018.03-04
//		String distanceString = dbSearch.getTimeString(date.getTime(), date1.getTime(), 0);
//		return distanceString;			
		return dbSearch.getCalendarInfo( date.getTime(), date1.getTime(), true).toString();
	}
	
	public String getTotalDayDistance(int day,int month, int year){
		Calendar date = new GregorianCalendar(year, month, day, 0, 0);
		Calendar date1 = new GregorianCalendar(year, month, day+1, 0, 0);
//12335: 2018.03-04
//		String distanceString = dbSearch.getTimeString(date.getTime(), date1.getTime(), 1);
//		return distanceString;
		return dbSearch.getCalendarInfo( date.getTime(), date1.getTime(), false).toString();
	}
	
	public List<CalendarData> getDay(int day, int month, int year){
		List<CalendarData> list = new ArrayList<CalendarData>();
		Calendar dayCalendar = new GregorianCalendar(year, month, day, 0, 0);
		Calendar auxCalendar = new GregorianCalendar(year, month, day+1, 0,0);
		list = dbSearch.getCalendarData(dayCalendar.getTime(), auxCalendar.getTime());
		return list;
	}
	
}
