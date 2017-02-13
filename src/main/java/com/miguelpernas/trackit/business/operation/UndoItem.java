package com.miguelpernas.trackit.business.operation;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.operation.RemovePausesOperation;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;

public class UndoItem {
	
	public static class PauseInformation{
		
		private final long pauseTime;
		private final long startPauseID;
		private long endPauseID;
		private final Double oldSpeed;
		private final List<Trackpoint> removedPoints;
		private final Trackpoint startPoint;
		private final Trackpoint endPoint;
		
		public PauseInformation(long pauseTime, long startPauseID, long endPauseID, Double oldSpeed){
			this.pauseTime = pauseTime;
			this.startPauseID = startPauseID;
			this.endPauseID = endPauseID;
			this.oldSpeed = oldSpeed;
			this.removedPoints = null;
			this.startPoint = null;
			this.endPoint = null;
		}
		
		public PauseInformation(long pauseTime, List<Trackpoint> removedPoints, Trackpoint leftEdge, Trackpoint rightEdge){
			this.pauseTime = pauseTime;
			this.removedPoints = removedPoints;
			this.startPoint = leftEdge;
			this.endPoint = rightEdge;
			this.oldSpeed = 0.0;
			this.startPauseID = 0;
			this.endPauseID = 0;
			
		}
		
		public long getStartPauseID(){
			return startPauseID;
		}
		
		public long getEndPauseID(){
			return endPauseID;
		}
		
		public long getPauseTime(){
			return pauseTime;
		}
		
		public Double getOldSpeed(){
			return oldSpeed;
		}
		
		public List<Trackpoint> getRemovedPoints(){
			return removedPoints;
		}
		
		public void setEndID(long id){
			this.endPauseID = id;
		}
		
		public Trackpoint getStart(){
			return startPoint;
		}
		
		public Trackpoint getEnd(){
			return endPoint;
		}
		
		
	}
	
	private final List<RemovePausesOperation.PauseInformation> removedPauses;
	private final String operationType;
	private final List<Long> changedCoursesIds;
	private final List<Long> duplicatePointIds;
	private final List<String> oldNames;
	private final Map<Long, Long> connectingPointsIds; //join
	private final boolean joinMergedPoints;
	private final long documentId;
	private final Trackpoint trackpoint;
	private final String reverseMode; //reverse
	private final Course course;
	private final Double splitSpeed; //split
	private final long deletedCourseId; //split/join
	private final int trackpointIndex;
	private final long pausedTime;
	private final List<Lap> laps;
	//long[0] = trackpoint id, long[1] = pausedTime, Double[0] = speed at trackpoint, Double[1] = speeds at next trackpoint
	//private final Map<Long[], Double[]> pausedSpeeds;
	private final PauseInformation pauseInformation;
	
	private final MapProvider mapProvider;
	private final Map<String, Object> routingOptions;
	private final Location location;
	private final List<Long> routeIds;
	private final Map<Long, Date[]> startTimes;
	
	private final Map<String, Object> paceOptions;
	private final boolean keepTimes;
	private final List<Pair<Boolean, Course>> joinedCoursesInfo;
	private final long oldTime;
	private final long newTime;
	private final Pair<Double, Double> oldPauseTime;
	
	private final boolean isNumber;
	private final boolean isTime;
	private final boolean isDistance;
	private final double segmentValue;
	private final List<Long> removedCourses;
	private final TrackSegment segment;
	
	private UndoItem(UndoItemBuilder builder) {
		this.operationType = builder.operationType;
		this.changedCoursesIds = builder.changedCoursesIds;
		this.documentId = builder.documentId;
		this.trackpoint = builder.trackpoint;
		this.reverseMode = builder.reverseMode;
		this.course = builder.course;
		this.splitSpeed = builder.splitSpeed;
		this.deletedCourseId = builder.deletedCourseId;
		this.connectingPointsIds = builder.connectingPointsIds;
		this.trackpointIndex = builder.trackpointIndex;
		this.joinMergedPoints = builder.joinMergedPoints;
		this.duplicatePointIds = builder.duplicatePointIds;
		this.mapProvider = builder.mapProvider;
		this.routingOptions = builder.routingOptions;
		this.location = builder.location;
		this.routeIds = builder.routeIds;
		this.startTimes = builder.startTimes;
		this.paceOptions = builder.paceOptions;
		this.keepTimes = builder.keepTimes;
		this.pausedTime = builder.pausedTime;
		//this.pausedSpeeds = builder.pausedSpeeds;
		this.pauseInformation = builder.pauseInformation;
		this.removedPauses = builder.removedPauses;
		this.joinedCoursesInfo = builder.joinedCoursesInfo;
		this.oldNames = builder.oldNames;
		this.oldTime = builder.oldTime;
		this.newTime = builder.newTime;
		this.oldPauseTime = builder.oldPauseTime;
		this.isNumber = builder.isNumber;
		this.isTime = builder.isTime;
		this.isDistance = builder.isDistance;
		this.segmentValue = builder.segmentValue;
		this.removedCourses = builder.removedCourses;
		this.segment = builder.segment;
		this.laps = builder.laps;
	}

	public String getOperationType() {
		return operationType;
	}

	public List<Long> getCoursesIds() {
		return changedCoursesIds;
	}

	public long getDocumentId() {
		return documentId;
	}

	public Trackpoint getTrackpoint() {
		return trackpoint;
	}
	
	public String getReverseMode(){
		return reverseMode;
	}
	
	public Course getCourse(){
		return course;
	}
	
	public Double getSplitSpeed(){
		return splitSpeed;
	}
	
	public long getDeletedCourseId(){
		return deletedCourseId;
	}
	
	public Map<Long, Long> getConnectingPointsIds(){
		return connectingPointsIds;
	}
	
	public int getTrackpointIndex(){
		return trackpointIndex;
	}
	
	public boolean getJoinMergedPoints(){
		return joinMergedPoints;
	}
	
	public List<Long> getDuplicatePointIds(){
		return duplicatePointIds;
	}

	public long getFirstCourseId() {
		int first = 0;
		return changedCoursesIds.get(first);
	}

	public long getCourseIdAt(int index) {
		return changedCoursesIds.get(index);
	}
	
	public MapProvider getMapProvider(){
		return mapProvider;
	}
	
	public Map<String, Object> getRoutingOptions(){
		return routingOptions;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public List<Long> getRouteIds(){
		return routeIds;
	}
	
	public Map<Long, Date[]> getStartTimes(){
		return startTimes;
	}
	
	public Map<String, Object> getPaceOptions(){
		return paceOptions;
	}

	public boolean getKeepTimes(){
		return keepTimes;
	}
	
	public long getPausedTime(){
		return pausedTime;
	}
	
	public List<String> getOldNames(){
		return oldNames;
	}
	
	/*public Map<Long[], Double[]> getPausedSpeeds(){
		return pausedSpeeds;
	}*/
	
	public PauseInformation getPauseInformation(){
		return pauseInformation;
	}
	
	public List<RemovePausesOperation.PauseInformation> getRemovedPauses(){
		return removedPauses;
	}
	
	public List<Pair<Boolean, Course>> getJoinedCoursesInfo(){
		return joinedCoursesInfo;
	}
	
	public long getOldTime(){
		return oldTime;
	}
	public long getNewTime(){
		return newTime;
	}
	
	public Pair<Double, Double> getOldPauseTime(){
		return oldPauseTime;
	}
	
	public boolean isNumber(){
		return isNumber;
	}
	
	public boolean isTime(){
		return isTime;
	}
	
	public boolean isDistance(){
		return isDistance;
	}
	
	public double getSegmentValue(){
		return segmentValue;
	}
	
	public List<Long> getRemovedCourses(){
		return removedCourses;
	}
	
	public TrackSegment getSegment(){
		return segment;
	}
	
	public List<Lap> getLaps(){
		return laps;
	}
	
	public static class UndoItemBuilder {
		private final String operationType;
		private final List<Long> changedCoursesIds;
		private final long documentId;
		private Trackpoint trackpoint;
		private String reverseMode;
		private Course course;
		private Double splitSpeed;
		private long deletedCourseId;
		private Map<Long, Long> connectingPointsIds = new HashMap<Long, Long>();
		private int trackpointIndex;
		private boolean joinMergedPoints;
		private List<Long> duplicatePointIds;
		private MapProvider mapProvider;
		private Map<String, Object> routingOptions;
		private Location location;
		private List<Long> routeIds;
		private Map<Long, Date[]> startTimes;
		private Map<String, Object> paceOptions;
		private boolean keepTimes;
		private long pausedTime;
		//private Map<Long[], Double[]> pausedSpeeds;
		private PauseInformation pauseInformation;
		private List<RemovePausesOperation.PauseInformation> removedPauses;
		private List<Pair<Boolean, Course>> joinedCoursesInfo;
		private List<String> oldNames;
		private long oldTime;
		private long newTime;
		private Pair<Double, Double> oldPauseTime;
		private boolean isNumber;
		private boolean isTime;
		private boolean isDistance;
		private double segmentValue;
		private List<Long> removedCourses;
		private TrackSegment segment;
		private List<Lap> laps;
		
		public UndoItemBuilder(String operationType,
				List<Long> changedCoursesIds, long documentId) {
			this.operationType = operationType;
			this.changedCoursesIds = changedCoursesIds;
			this.documentId = documentId;
		}

		public UndoItemBuilder trackpoint(Trackpoint trackpoint) {
			this.trackpoint = trackpoint;
			return this;
		}
		
		public UndoItemBuilder reverseMode(String reverseMode){
			this.reverseMode = reverseMode;
			return this;
		}
		
		public UndoItemBuilder course(Course course){
			this.course = course;
			return this;
		}
		
		public UndoItemBuilder splitSpeed (Double splitSpeed){
			this.splitSpeed = splitSpeed;
			return this;
		}
		
		public UndoItemBuilder deletedCourseId(long deletedCourseId){
			this.deletedCourseId = deletedCourseId;
			return this;
		}
		
		public UndoItemBuilder connectingPointsIds(Map<Long, Long> connectingPointsIds){
			this.connectingPointsIds = connectingPointsIds;
			return this;
		}
		
		public UndoItemBuilder trackpointIndex(int trackpointIndex){
			this.trackpointIndex = trackpointIndex;
			return this;
		}
		
		public UndoItemBuilder joinMergedPoints(boolean joinMergedPoints){
			this.joinMergedPoints = joinMergedPoints;
			return this;
		}
		
		public UndoItemBuilder duplicatePointIds(List<Long> duplicatePointIds){
			this.duplicatePointIds = duplicatePointIds;
			return this;
		}
		
		public UndoItemBuilder mapProvider(MapProvider mapProvider){
			this.mapProvider = mapProvider;
			return this;
		}
		
		public UndoItemBuilder routingOptions(Map<String, Object> routingOptions){
			this.routingOptions = routingOptions;
			return this;
		}
		
		public UndoItemBuilder location(Location location){
			this.location = location;
			return this;
		}
		
		public UndoItemBuilder routeIds(List<Long> routeIds){
			this.routeIds = routeIds;
			return this;
		}
		
		public UndoItemBuilder startTimes(Map<Long, Date[]> startTimes){
			this.startTimes = startTimes;
			return this;
		}
		
		public UndoItemBuilder paceOptions(Map<String, Object> paceOptions){
			this.paceOptions = paceOptions;
			return this;
		}
		
		public UndoItemBuilder keepTimes(boolean keepTimes){
			this.keepTimes = keepTimes;
			return this;
		}
		
		public UndoItemBuilder pausedTime(long pausedTime){
			this.pausedTime = pausedTime;
			return this;
		}
		
		public UndoItemBuilder pauseInformationAdd(long pausedTime, long startTrackpointID, long endTrackpointID, Double oldSpeed){
			this.pauseInformation = new PauseInformation(pausedTime, startTrackpointID, endTrackpointID, oldSpeed);
			return this;
		}
		
		public UndoItemBuilder pauseInformationRemove(long pausedTime, List<Trackpoint> removedPoints, Trackpoint leftEdge, Trackpoint rightEdge){
			this.pauseInformation = new PauseInformation(pausedTime, removedPoints, leftEdge, rightEdge);
			return this;
		}
		
		public UndoItemBuilder removedPauses(List<RemovePausesOperation.PauseInformation> removedPauses){
			this.removedPauses = removedPauses;
			return this;
		}
		
		public UndoItemBuilder joinedCoursesInfo(List<Pair<Boolean, Course>> joinedCoursesInfo){
			this.joinedCoursesInfo = joinedCoursesInfo;
			return this;
		}
		
		public UndoItemBuilder oldNames(List<String> oldNames){
			this.oldNames = oldNames;
			return this;
		}

		public UndoItemBuilder oldTime(long oldTime){
			this.oldTime = oldTime;
			return this;
		}
		
		public UndoItemBuilder newTime(long newTime){
			this.newTime = newTime;
			return this;
		}
		
		public UndoItemBuilder oldPauseTime(Pair<Double, Double> oldPauseTime){
			this.oldPauseTime = oldPauseTime;
			return this;
		}
		
		public UndoItemBuilder isNumber(boolean isNumber){
			this.isNumber = isNumber;
			return this;
		}
		
		public UndoItemBuilder isTime(boolean isTime){
			this.isTime = isTime;
			return this;
		}
		
		public UndoItemBuilder isDistance(boolean isDistance){
			this.isDistance = isDistance;
			return this;
		}
		
		public UndoItemBuilder segmentValue(double segmentValue){
			this.segmentValue = segmentValue;
			return this;
		}
		
		public UndoItemBuilder removedCourses(List<Long> removedCourses){
			this.removedCourses = removedCourses;
			return this;
		}
		
		public UndoItemBuilder segment(TrackSegment segment){
			this.segment = segment;
			return this;
		}
		
		public UndoItemBuilder laps(List<Lap> laps){
			this.laps = laps;
			return this;
		}
		
		public UndoItem build() {
			return new UndoItem(this);
		}
		
	

	}

}
