package com.trackit.business.dbsearch;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.trackit.TrackIt;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.CircuitType;
import com.trackit.business.domain.DifficultyLevelType;
import com.trackit.business.domain.TrackConditionType;

public class SearchObject {
	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	public String _filePath = null;
	public String _name = null;
	public String _activity = null;
	public String _sport = null;
	public String _subSport = null;
	public String _maxStartTime = null;
	public String _minStartTime = null;
	public String _maxEndTime = null;
	public String _minEndTime = null;
	public String _minTotalTime = null;
	public String _maxTotalTime = null;
	public String _minMovingTime = null;
	public String _maxMovingTime = null;
	public String _minDistance = null;
	public String _maxDistance = null;
	public String _maxAscent = null;
	public String _minAscent = null;
	public String _maxDescent = null;
	public String _minDescent = null;
	public String _maxAltitude = null;
	public String _minAltitude = null; 
	public String _maxLongitude = null;
	public String _minLongitude = null;
	public String _maxLatitude = null;
	public String _minLatitude = null;
	public String _trackState = null;
	public String _trackDifficulty = null;
	public String _circularPath = null;
	public String _searchType = Messages.getMessage("search.dialog.simple");
	int aux = 0;
	
	public SearchObject(HashMap<String, String> variables){
		for(String values : variables.keySet()){
			if(values.equals("searchType")){
				this.setSearchType(variables.get(values));
			}
			if(values.equals("filePath")){
				this.setFilePath(variables.get(values));
			}
			if(values.equals("name")){
				this.setName(variables.get(values));
			}
			if(values.equals("activity")){
				this.setActivity(variables.get(values));
			}
			if(values.equals("sport")){
				this.setSport(variables.get(values));
			}
			if(values.equals("subSport")){
				this.setSubSport(variables.get(values));
			}
			if(values.equals("maxStartTime")){
				this.setMaxStartTime(variables.get(values));
			}
			if(values.equals("minStartTime")){
				this.setMinStartTime(variables.get(values));
			}
			if(values.equals("maxEndTime")){
				this.setMaxEndTime(variables.get(values));
			}
			if(values.equals("minEndTime")){
				this.setMinEndTime(variables.get(values));
			}
			if(values.equals("minTotalTime")){
				this.setMinTotalTime(variables.get(values));
			}
			if(values.equals("maxTotalTime")){
				this.setMaxTotalTime(variables.get(values));
			}
			if(values.equals("minMovingTime")){
				this.setMinMovingTime(variables.get(values));
			}
			if(values.equals("maxMovingTime")){
				this.setMaxMovingTime(variables.get(values));
			}
			if(values.equals("minDistance")){
				this.setMinDistance(variables.get(values));
			}
			if(values.equals("maxDistance")){
				this.setMaxDistance(variables.get(values));
			}
			if(values.equals("maxAscent")){
				this.setMaxAscent(variables.get(values));
			}
			if(values.equals("minAscent")){
				this.setMinAscent(variables.get(values));
			}
			if(values.equals("maxDescent")){
				this.setMaxDescent(variables.get(values));
			}
			if(values.equals("minDescent")){
				this.setMinDescent(variables.get(values));
			}
			if(values.equals("maxAltitude")){
				this.setMaxAltitude(variables.get(values));
			}
			if(values.equals("minAltitude")){
				this.setMinAltitude(variables.get(values));
			}
			if(values.equals("maxLongitude")){
				this.setMaxLongitude(variables.get(values));
			}
			if(values.equals("minLongitude")){
				this.setMinLongitude(variables.get(values));
			}
			if(values.equals("maxLatitude")){
				this.setMaxLatitude(variables.get(values));
			}
			if(values.equals("minLatitude")){
				this.setMinLatitude(variables.get(values));
			}
			if(values.equals("trackState")){
				this.setTrackState(variables.get(values));
			}
			if(values.equals("trackDifficulty")){
				this.setTrackDifficulty(variables.get(values));
			}
			if(values.equals("circularPath")){
				this.setCircularPath(variables.get(values));
			}
		}
	}
	
	public String getFilePath(){
		return _filePath;
		
	}
	
	public void setFilePath(String filePath){
		_filePath = filePath;
		aux++;
	}
	
	public String getName(){
		return _name;
	}
	
	public void setName(String name){
		_name = name;
		aux++;
	}
	
	public String getActivity(){
		return _activity;
	}
	
	public void setActivity(String activity){
		if(_activity==null){
			_activity = activity;
			aux++;
		}
		else{
			_activity = activity;
		}
	}
	
	public String getSport(){
		return _sport;
	}
	
	public void setSport(String sport){
		_sport = sport;
		aux++;
	}
	
	public String getSubSport(){
		return _subSport;
	}
	
	public void setSubSport(String subSport){
		_subSport = subSport;
		aux++;
	}
	
	public String getMaxStartTime(){
		System.out.println( "MaxStartTime " + _maxStartTime);
		return _maxStartTime;
	}
	
	public void setMaxStartTime(String maxStartTime){
		_maxStartTime = maxStartTime;
		aux++;
	}
	
	public String getMinStartTime(){
		System.out.println( "MinStartTime " + _minStartTime);
		return _minStartTime;
	}
	
	public void setMinStartTime(String minStartTime){
		_minStartTime = minStartTime;
		aux++;
	}
	
	public String getMaxEndTime(){
		System.out.println( "MaxEndTime " + _maxEndTime);
		return _maxEndTime;
	}
	
	public void setMaxEndTime(String maxEndTime){
		_maxEndTime = maxEndTime;
		aux++;
	}
	
	public String getMinEndTime(){
		System.out.println( "MinEndTime " + _minEndTime);
		return _minEndTime;
	}
	
	public void setMinEndTime(String maxEndTime){
		_maxEndTime = maxEndTime;
		aux++;
	}
	
	public String getMaxTotalTime(){
		return _maxTotalTime;
	}
	
	public void setMaxTotalTime(String maxTotalTime){
		_maxTotalTime = maxTotalTime;
		aux++;
	}
	
	public String getMinTotalTime(){
		return _minTotalTime;
	}
	
	public void setMinTotalTime(String minTotalTime){
		_minTotalTime = minTotalTime;
		aux++;
	}
	
	public String getMaxMovingTime(){
		return _maxMovingTime;
	}
	
	public void setMaxMovingTime(String maxMovingTime){
		_maxMovingTime = maxMovingTime;
		aux++;
	}
	
	public String getMinMovingTime(){
		return _minMovingTime;
	}
	
	public void setMinMovingTime(String minMovingTime){
		_minMovingTime = minMovingTime;
		aux++;
	}
	
	public String getMaxDistance(){
		return _maxDistance;
	}
	
	public void setMaxDistance(String maxDistance){
		_maxDistance = maxDistance;
		aux++;
	}
	
	public String getMinDistance(){
		return _minDistance;
	}
	
	public void setMinDistance(String minDistance){
		_minDistance = minDistance;
		aux++;
	}
	
	public String getMaxAscent(){
		return _maxAscent;
	}
	
	public void setMaxAscent(String maxAscent){
		_maxAscent = maxAscent;
		aux++;
	}
	
	public String getMinAscent(){
		return _minAscent;
	}
	
	public void setMinAscent(String minAscent){
		_minAscent = minAscent;
		aux++;
	}
	
	public String getMaxDescent(){
		return _maxDescent;
	}
	
	public void setMaxDescent(String maxDescent){
		_maxDescent = maxDescent;
		aux++;
	}
	
	public String getMinDescent(){
		return _minDescent;
	}
	
	public void setMinDescent(String minDescent){
		_minDescent = minDescent;
		aux++;
	}
	
	public String getMaxAltitude(){
		return _maxAltitude;
	}
	
	public void setMaxAltitude(String maxAltitude){
		_maxAltitude = maxAltitude;
		aux++;
	}
	
	public String getMinAltitude(){
		return _minAltitude;
	}
	
	public void setMinAltitude(String minAltitude){
		_minAltitude = minAltitude;
		aux++;
	}
	
	public String getMaxLongitude(){
		return _maxLongitude;
	}
	
	public void setMaxLongitude(String maxLongitude){
		_maxLongitude = maxLongitude;
		aux++;
	}
	
	public String getMinLongitude(){
		return _minLongitude;
	}
	
	public void setMinLongitude(String minLongitude){
		_minLongitude = minLongitude;
		aux++;
	}
	
	public String getMaxLatitude(){
		return _maxLatitude;
	}
	
	public void setMaxLatitude(String maxLatitude){
		_maxLatitude = maxLatitude;
		aux++;
	}
	
	public String getMinLatitude(){
		return _minLatitude;
	}
	
	public void setMinLatitude(String minLatitude){
		_minLatitude = minLatitude;
		aux++;
	}
	
	public String getTrackState(){
		System.out.println( "obtaining track status " + _trackState);
		if ( _trackState != null )		//12335: 2018-03-06
			return String.valueOf( TrackConditionType.lookup( _trackState).getValue());
		return _trackState;
	}
	
	public void setTrackState(String trackState){
		_trackState = trackState;
		aux++;
	}
	
	public String getTrackDifficulty(){
		System.out.println( "obtaining track difficulty " + _trackDifficulty);
//		return _trackDifficulty;		//12335: 2018-03-06
		if ( _trackDifficulty == null )
			return _trackDifficulty;
		return String.valueOf( DifficultyLevelType.lookup( _trackDifficulty).getValue());
	}
	
	public void setTrackDifficulty(String trackDifficulty){
		System.out.println( "Setting track difficulty to " + trackDifficulty);
		_trackDifficulty = trackDifficulty;
		aux++;
	}
	
	public String getCircularPath(){
		System.out.println( "obtaining track circular " + _circularPath);
		if ( _circularPath != null )		//12335: 2018-03-06
			return String.valueOf( CircuitType.lookup( _circularPath).getValue());
		return _circularPath;
	}
	
	public void setCircularPath(String circularPath){
		_circularPath = circularPath;
		aux++;
	}
	
	public String getSearchType(){
		return _searchType;
	}
	
	public void setSearchType(String searchType){
		_searchType = searchType;
		aux++;
	}
		
	public int getAux(){
		return aux;
	}
}
