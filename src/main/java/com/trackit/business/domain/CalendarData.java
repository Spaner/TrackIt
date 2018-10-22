package com.trackit.business.domain;

import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.EventPublisher;

public class CalendarData extends TrackItBaseType implements DocumentItem{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String filepath;
	private String name;
	private String isActivity;
	private double distance;
	private int totalAscent;
	private int totalDescent;
	private double elapsedTime;
	private SportType sport;
	private SubSportType subSport; 					
	private DifficultyLevelType difficulty;			
	private TrackConditionType trackCondition;		
	private CircuitType circuitType;
	private double startAltitude;
	private double endAltitude;
	private double northeastLatitude;
	private double northeastLongitude;
	private double southwestLatitude;
	private double southwestLongitude;
	
	public CalendarData(String path, String name1, double distance, int totalAscent, String isActivity,
						int totalDescent, double elapsedTime, SportType sport, SubSportType subSport,
						DifficultyLevelType difficulty, TrackConditionType trackCondition, CircuitType circuitType,
						double startAltitude, double endAltitude,
						double northeastLatitude, double northeastLongitude, double southwestLatitude, double southwestLongitude){
		
		this.setFilepath(path);
		this.setName(name1);
		this.setDistance(distance);
		this.setIsActivity(isActivity);
		this.setTotalAscent(totalAscent);
		this.setTotalDescent(totalDescent);
		this.setElapsedTime(elapsedTime);
		this.setSport(sport);
		this.setSubSport(subSport);
		this.setDifficulty(difficulty);
		this.setTrackCondition(trackCondition);
		this.setCircuitType(circuitType);
		this.setNortheastLatitude(northeastLatitude);
		this.setNortheastLongitude(northeastLongitude);
		this.setSouthwestLatitude(southwestLatitude);
		this.setSouthwestLongitude(southwestLongitude);
	}
	
	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		// TODO Auto-generated method stub
		
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIsActivity() {
		return isActivity;
	}

	public void setIsActivity(String isActivity) {
		this.isActivity = isActivity;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public int getTotalAscent() {
		return totalAscent;
	}

	public void setTotalAscent(int totalAscent) {
		this.totalAscent = totalAscent;
	}

	public int getTotalDescent() {
		return totalDescent;
	}

	public void setTotalDescent(int totalDescent) {
		this.totalDescent = totalDescent;
	}

	public double getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public SportType getSport() {
		return sport;
	}

	public void setSport(SportType sport) {
		this.sport = sport;
	}

	public SubSportType getSubSport() {
		return subSport;
	}

	public void setSubSport(SubSportType subSport) {
		this.subSport = subSport;
	}

	public DifficultyLevelType getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(DifficultyLevelType difficulty) {
		this.difficulty = difficulty;
	}

	public TrackConditionType getTrackCondition() {
		return trackCondition;
	}

	public void setTrackCondition(TrackConditionType trackCondition) {
		this.trackCondition = trackCondition;
	}

	public CircuitType getCircuitType() {
		return circuitType;
	}

	public void setCircuitType(CircuitType circuitType) {
		this.circuitType = circuitType;
	}

	public double getStartAltitude() {
		return startAltitude;
	}

	public void setStartAltitude(double startAltitude) {
		this.startAltitude = startAltitude;
	}

	public double getEndAltitude() {
		return endAltitude;
	}

	public void setEndAltitude(double endAltitude) {
		this.endAltitude = endAltitude;
	}

	public double getNortheastLatitude() {
		return northeastLatitude;
	}

	public void setNortheastLatitude(double northeastLatitude) {
		this.northeastLatitude = northeastLatitude;
	}

	public double getNortheastLongitude() {
		return northeastLongitude;
	}

	public void setNortheastLongitude(double northeastLongitude) {
		this.northeastLongitude = northeastLongitude;
	}

	public double getSouthwestLatitude() {
		return southwestLatitude;
	}

	public void setSouthwestLatitude(double southwestLatitude) {
		this.southwestLatitude = southwestLatitude;
	}

	public double getSouthwestLongitude() {
		return southwestLongitude;
	}

	public void setSouthwestLongitude(double southwestLongitude) {
		this.southwestLongitude = southwestLongitude;
	}

	
}
