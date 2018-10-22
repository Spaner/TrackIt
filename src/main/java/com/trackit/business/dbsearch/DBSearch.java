/*
 * This file is part of Track It!.
 * Copyright (C) 2017 Diogo Xavier
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

import org.apache.log4j.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garmin.fit.Sport;
import com.trackit.TrackIt;
import com.trackit.business.common.Messages;
import com.trackit.business.database.Database;
import com.trackit.business.domain.CalendarData;
import com.trackit.business.domain.CalendarInfo;
import com.trackit.business.domain.CircuitType;
import com.trackit.business.domain.DifficultyLevelType;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;
import com.trackit.business.domain.TrackConditionType;

public class DBSearch {
	
	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	private static DBSearch dbSearch;
	private Database database;
	public static DBSearch dbsearch;
	private Connection c               = null;
	private Statement  stmt            = null;
	private ResultSet  res             = null;
	
	public static final int WithNotes    = 1 << 1;
	public static final int WithPictures = 1 << 2;
	public static final int WithAudio    = 1 << 3;
	public static final int WithVideo    = 1 << 4;
	
	protected static int[]    ids    = { WithNotes, WithPictures, WithAudio, WithVideo};
	protected static String[] tables = { "Note",    "Picture",    "Audio",   "Video"};
	
	
	public DBSearch(){
		database = Database.getInstance();
	}

	static {
		dbsearch = new DBSearch();
	}

	public synchronized static DBSearch getInstance() {
		return dbsearch;
	}
	
	private boolean isConnected(){
		return database.connected();
	}
	
	private void openConnection(){
		database.openConnection();
		c = database.getConnection();
	}
	
	private void closeConnection(){
		database.closeConnection();
	}
	
	public List<String> getDocumentsFromArea(
			double minLongitude, double maxLongitude,
			double minLatitude,  double maxLatitude) {
		List<String> names = new ArrayList<>();
		openConnection();
		if ( isConnected() )
			try {
				String sql = "SELECT Filepath FROM GPSFiles"
							+ " WHERE MinLongitude >= ? AND" + " MaxLongitude <= ? AND"
							+ " MinLatitude >= ? AND" + " MaxLatitude <= ?";
				PreparedStatement stmt = null;
				stmt = c.prepareStatement(sql);
				stmt.setDouble(1, minLongitude);
				stmt.setDouble(2, maxLongitude);
				stmt.setDouble(3, minLatitude);
				stmt.setDouble(4, maxLatitude);
				res = stmt.executeQuery();
				while ( res.next() ) {
					String filepath = new String(res.getString(1));
					if ( !names.contains(filepath))
						names.add(filepath);
				}
				stmt.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		return names;
	}
	
	public List<String> getActivitiesFromArea(
			double minLongitude, double maxLongitude,
			double minLatitude,  double maxLatitude, int isActivity, String filePath) {
		List<String> names = new ArrayList<>();
		openConnection();
		if ( isConnected() )
			try {
				String sql = "SELECT Name FROM GPSFiles"
							+ " WHERE MinLongitude >= ? AND" + " MaxLongitude <= ? AND"
							+ " MinLatitude >= ? AND" + " MaxLatitude <= ? AND"
							+ " isActivity = ? AND FilePath = ?";
				PreparedStatement stmt = null;
				stmt = c.prepareStatement(sql);
				stmt.setDouble(1, minLongitude);
				stmt.setDouble(2, maxLongitude);
				stmt.setDouble(3, minLatitude);
				stmt.setDouble(4, maxLatitude);
				stmt.setInt(5, isActivity);
				stmt.setString(6, filePath);
				res = stmt.executeQuery();
				while ( res.next() ) {
					String filepath = new String(res.getString(1));
					if ( !names.contains(filepath))
						names.add(filepath);
				}
				stmt.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		return names;
	}

//	12335: 2018-03-04 - substituted by getCalendarInfo
//	public String getTimeString(Date startDate, Date endDate, int aux){
//		String answer = "";
//		if(aux==0){
//			answer = Messages.getMessage("calendar.dialog.noMonth");
//		}
//		else{
////			answer = Messages.getMessage("calendar.dialog.noDay");
//			answer = Messages.getMessage("calendarInfo.noDay");
//		}
//		openConnection();
//		String sql = null;
//		float totalDistance = 0;
//		float totalTime = 0;
//		int numberOf = 0;
//		String filepath = "";
//		Long startString = startDate.getTime();
//		Long endString = endDate.getTime();
//		if ( isConnected() ){
//			try {
//				sql = "SELECT Distance, TotalTime, Filepath FROM GPSFiles"
//						+ " WHERE StartTime >= '" + startString +"'  AND StartTime <= '" + endString + "';";
//				PreparedStatement stmt = null;
//				stmt = c.prepareStatement(sql);
//				res = stmt.executeQuery();
//				while ( res.next() ) {
//					filepath = new String(res.getString(3));
//					File newFile = new File(filepath);
//					if(newFile.exists()){
//						totalDistance = totalDistance + Float.valueOf(res.getString(1));
//						totalTime = totalTime + Float.valueOf(res.getString(2));
//						numberOf++;
//					}
//				}
//				if(numberOf>0){
//					CalendarInfo info = new CalendarInfo(totalDistance, totalTime, numberOf, aux);
//					answer = info.getFinalString(aux);
//				}
//				stmt.close();
//			} catch (Exception e) {
//				logger.error(e.getClass().getName() + ": " + e.getMessage());
//			} finally {
//				closeConnection();
//			}
//		}
//		return answer;
//	}
	
	//12335: 2018-03-04 - substitutes getTimeString, return a CalendarInfo object
	public CalendarInfo getCalendarInfo( Date from, Date upTo, boolean forMonth) {
		CalendarInfo info = null;
		openConnection();
		if ( isConnected() ) 
			try {
				String sql = "SELECT Distance, TotalTime, FilePath FROM GPSFiles WHERE StartTime >= '" +
							 from.getTime() + "' AND StartTime <= '" + upTo.getTime() + "'";
				PreparedStatement stmt = null;
				stmt = c.prepareStatement(sql);
				res = stmt.executeQuery();
				float totalDistance = 0;
				float totalTime     = 0;
				int	  numberOf      = 0;
				while ( res.next()) {
					if ( (new File( res.getString( 3))).exists() ) {
						numberOf++;
						totalDistance += res.getFloat( 1);
						totalTime     += res.getFloat( 2);
					}
				}
				if ( numberOf != 0 )
					info = new CalendarInfo( totalDistance, totalTime, numberOf, forMonth);
			} catch (Exception e) {
				logger.error( e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		if ( info == null ) {
			if ( forMonth )
				info = CalendarInfo.voidMonth;
			else
				info = CalendarInfo.voidDay;
		}
		return info;
	}
	
	public HashMap<String, String> getDataByDate(Date startDate, Date endDate){
		HashMap<String, String> hashMap = new HashMap<String, String>();
		openConnection();
		String sql = null;
		Long startString = startDate.getTime();
		Long endString = endDate.getTime();
		if ( isConnected() ){
			try {
				sql = "SELECT Filepath, StartTime FROM GPSFiles"
				+ " WHERE StartTime >= "+ startString + " AND StartTime <= " + endString + ";";
				PreparedStatement stmt = null;
				stmt = c.prepareStatement(sql);
				res = stmt.executeQuery();
				while ( res.next() ) {
					String filepath = new String(res.getString(1));
					String startTime = new String(res.getString(2));
					File newFile = new File(filepath);
					if(newFile.exists()){
						if (!hashMap.containsValue(filepath)&&!hashMap.containsKey(startTime))
							hashMap.put(startTime, filepath);
					}
				}
				stmt.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		}
		return hashMap;
	}
	
	public HashMap<String, HashMap<String, List<String>>> getCourseData(){
		HashMap<String, HashMap<String, List<String>>> hash = new HashMap<String, HashMap<String, List<String>>>();
		return hash;
	}
	
	public List<CalendarData> getCalendarData(Date startTime, Date endTime){
		List<CalendarData> list = new ArrayList<CalendarData>();
		openConnection();
		String sql = null;
		Long startString = startTime.getTime();
		Long endString = endTime.getTime();
		if ( isConnected() ){
			try {
				sql = "SELECT FilePath, Name, isActivity, Distance, Ascent, Descent, TotalTime, Sport, SubSport, "
						+ "TrackDifficulty, TrackState, CircularPath, MaxAltitude, MinAltitude, MaxLatitude, "
						+ " MaxLongitude, MinLatitude, MinLongitude"
						+ " FROM GPSFiles"
						+ " WHERE StartTime >= "+ startString + " AND" + " StartTime <= "+ endString + ";"; 
				PreparedStatement stmt = null;
				stmt = c.prepareStatement(sql);
				res = stmt.executeQuery();
				while ( res.next() ) {
					String filepath = new String(res.getString(1));
					File newFile = new File(filepath);
					if(newFile.exists()){
						SportType sport = SportType.lookup(res.getShort(8));
						SubSportType subSport = SubSportType.lookup(res.getShort(9));
						DifficultyLevelType difficulty =  DifficultyLevelType.lookup(res.getShort(10));
						TrackConditionType condition = TrackConditionType.lookup(res.getShort(11));
						CircuitType circuitType = CircuitType.lookup(res.getShort(12));
						String isActivity = "";
						if(res.getInt(3)==1){
							isActivity = "Activity";
						}
						else{
							isActivity = "Course";
						}
						CalendarData calendarData = new CalendarData(res.getString(1), res.getString(2), res.getDouble(4), res.getInt(5), isActivity,
								res.getInt(6), res.getDouble(7), sport, subSport, difficulty, condition, circuitType,
								res.getDouble(13), res.getDouble(14), res.getDouble(15) , res.getDouble(16), res.getDouble(17), res.getDouble(18));
						list.add(calendarData);
					}
				}
				stmt.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		}
		return list;
	}
	
	public List<String> getActivityByDate(String filePath, Date startTime, Date endTime){
		List<String> list = new ArrayList<String>();
		openConnection();
		String sql = null;
		if ( isConnected() ){
			try {
				sql = "SELECT Name FROM GPSFiles"
				+ " WHERE FilePath = '" + filePath +"' AND" + " isActivity = 1 AND"
				+ " StartTime >= "+ startTime.getTime() + " AND" + " StartTime <= "+ endTime.getTime()+ ";"; 
				PreparedStatement stmt = null;
				stmt = c.prepareStatement(sql);
				res = stmt.executeQuery();
				while ( res.next() ) {
					String name = new String(res.getString(1));
					list.add(name);
				}
				stmt.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		}
		return list;
	}

	public List<String> getCoursesByDate(String filePath, Date startTime, Date endTime){
		List<String> list = new ArrayList<String>();
		openConnection();
		String sql = null;
		if ( isConnected() ){
			try {
				sql = "SELECT Name FROM GPSFiles"
				+ " WHERE FilePath = '" + filePath +"' AND" + " isActivity = 0 AND"
				+ " StartTime >= "+ startTime.getTime() + " AND" + " StartTime <= "+ endTime.getTime() + ";"; 
				PreparedStatement stmt = null;
				stmt = c.prepareStatement(sql);
				res = stmt.executeQuery();
				while ( res.next() ) {
					String name = new String(res.getString(1));
					list.add(name);
				}
				stmt.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		}
		return list;
	}
	
	
	public List<String> getData(SearchObject searchObject, boolean file){ 
		return getData(searchObject, file, "");
	}
	
	public List<String> getData( HashMap< DBSearchField, String> fields) {
		return getData( fields, 0);
	}

	
	public List<String> getData( HashMap< DBSearchField, String> fields, int mediaMask) {
		List<String> listOfNames = new ArrayList<>();
		String sql    = "";
		String where  = "";
		String select = "SELECT " 
		               + (fields.get( DBSearchField.Filepath) == null ? DBSearchField.Filepath.getFieldId() :
		            		   											DBSearchField.TrackName.getFieldId())
		               + " FROM GPSFiles";
		for( DBSearchField field: fields.keySet()) {
			if ( where.length() > 0 )
				where += " AND ";
			where += field.getValue() + "'" + fields.get( field) + "'";
		}
		if ( where.length() > 0 )
			where = " WHERE " + where;
		
		if ( mediaMask == 0 ) {
			sql = select + where;
		} else {
			for( int i=0; i<ids.length; i++) {
				if ( (mediaMask & ids[i]) != 0 ) {
					if ( sql.length() > 0 )
						sql += " UNION ";
					sql += select + addInnerJoin( tables[i], fields) + where;
				}
			}
		}
		openConnection();
		if ( isConnected() )
			try {
				PreparedStatement stmt = c.prepareStatement(sql);
				res = stmt.executeQuery();
				while (res.next()) {
					String filepath = new String(res.getString(1));
					if ( !listOfNames.contains(filepath))
						listOfNames.add(filepath);
				}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				logger.error("getDatafromArea");
			}
			  finally {
				closeConnection();
			}
		return listOfNames;
	}
	
	// 2018-04-07: 12335 
	protected String addInnerJoin( String mediaTableName, HashMap<DBSearchField, String> fields) {
		String join = " INNER JOIN " + mediaTableName + " ON "
		            + DBSearchField.Filepath.getFieldId() + "=" + 
		            				mediaTableName + "." + DBSearchField.MediaFilepath.getFieldId();
		if ( fields.get( DBSearchField.Filepath) != null )
			join += " AND " + DBSearchField.TrackName.getFieldId() + "=" +
									mediaTableName + "." + DBSearchField.MediaTrackName.getFieldId();
		return join;
	}
	
	
//	public List<String> getData(SearchObject searchObject, boolean file){ 
	public List<String> getData(SearchObject searchObject, boolean file, String extra){ 
		List<String> nameList = new ArrayList<>();
		//SearchControl control = SearchControl.getInstance();
		//control.addSearchObject(searchObject);
		PreparedStatement stmt;
		int aux = searchObject.getAux();
		openConnection();
		String sql = null;
		if ( isConnected() )
		try {
			if(file){
				sql = "SELECT Filepath FROM GPSFiles";
			}
			if(!file){
				sql = "SELECT Name FROM GPSFiles";
			}
			if(aux >= 1){
				sql = sql + " WHERE ";
			}
			if(!(searchObject.getName()==null)){
				sql = sql + "Name = " + "'" + searchObject.getName() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getFilePath()==null)){
				sql = sql + "Filepath = " + "'" + searchObject.getFilePath() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getActivity()==null)){
				sql = sql + "IsActivity =" + "'" + searchObject.getActivity() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getSport()==null)){
				sql = sql + "Sport =" + "'" + searchObject.getSport() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getSubSport()==null)){
				sql = sql + "SubSport =" + "'" + searchObject.getSubSport() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxStartTime()==null)){
				sql = sql + "StartTime <=" + "'" + searchObject.getMaxStartTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinStartTime()==null)){
				sql = sql + "StartTime >=" + "'" + searchObject.getMinStartTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxEndTime()==null)){
				sql = sql + "EndTime <=" + "'" + searchObject.getMaxEndTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinEndTime()==null)){
				sql = sql + "EndTime >=" + "'" + searchObject.getMinEndTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxTotalTime()==null)){
				sql = sql + "TotalTime <=" + "'" + searchObject.getMaxTotalTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinTotalTime()==null)){
				sql = sql + "TotalTime >=" + "'" + searchObject.getMinTotalTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxMovingTime()==null)){
				sql = sql + "MovingTime <=" + "'" + searchObject.getMaxTotalTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinMovingTime()==null)){
				sql = sql + "MovingTime >=" + "'" + searchObject.getMinTotalTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxTotalTime()==null)){
				sql = sql + "TotalTime <=" + "'" + searchObject.getMaxTotalTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinTotalTime()==null)){
				sql = sql + "TotalTime >=" + "'" + searchObject.getMinTotalTime() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxDistance()==null)){
				sql = sql + "Distance <=" + "'" + searchObject.getMaxDistance() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinDistance()==null)){
				sql = sql + "Distance >=" + "'" + searchObject.getMinDistance() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxAscent()==null)){
				sql = sql + "Ascent <=" + "'" + searchObject.getMaxAscent() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinAscent()==null)){
				sql = sql + "Ascent >=" + "'" + searchObject.getMinAscent() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxDescent()==null)){
				sql = sql + "Descent <=" + "'" + searchObject.getMaxDescent() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinDescent()==null)){
				sql = sql + "Descent >=" + "'" + searchObject.getMinDescent() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxAltitude()==null)){
				sql = sql + "MaxAltitude =" + "'" + searchObject.getMaxAltitude() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinAltitude()==null)){
				sql = sql + "MinAltitude =" + "'" + searchObject.getMinAltitude() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxLongitude()==null)){
				sql = sql + "MaxLongitude <=" + "'" + searchObject.getMaxLongitude() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinLongitude()==null)){
				sql = sql + "MinLongitude >=" + "'" + searchObject.getMinLongitude() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMaxLatitude()==null)){
				sql = sql + "MaxLatitude <=" + "'" + searchObject.getMaxLatitude() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getMinLatitude()==null)){
				sql = sql + "MinLatitude >=" + "'" + searchObject.getMinLatitude() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getTrackState()==null)){
				sql = sql + "TrackState =" + "'" + searchObject.getTrackState() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getTrackDifficulty()==null)){
				sql = sql + "TrackDifficulty =" + "'" + searchObject.getTrackDifficulty() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
			if(!(searchObject.getCircularPath()==null)){
				sql = sql + "CircularPath =" + "'" + searchObject.getCircularPath() + "'";
				aux--;
				if(aux>=1){
					sql = sql + " AND ";
				}
			}
//			sql = sql + ";";
			sql += extra + ";";
System.out.println( "Issuing: " + sql + "\nResults are");
			stmt = c.prepareStatement(sql);
			res = stmt.executeQuery();
			while (res.next()) {
				String filepath = new String(res.getString(1));
				System.out.println( "\t" + filepath);
				if ( !nameList.contains(filepath))
					nameList.add(filepath);
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getDatafromArea");
			return nameList;
		}
		return nameList;
	}
	
	/*public List<String> getMultiSports(SearchObject searchObject, Map<String, String> sports){
		List<String> nameList = new ArrayList<>();
		List<String> auxList = new ArrayList<>();
		for (Map.Entry<String, String> entry : sports.entrySet()) {
			searchObject.setSport(entry.getKey());
			searchObject.setSubSport(entry.getValue());
			auxList = getData(searchObject);
			nameList.add(auxList);
		}
		return nameList;
	}*/
}

