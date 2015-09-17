package com.miguelpernas.trackit.business.operation;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;

public class UndoItem {
	
	public static class PauseInformation{
		
		private final Double firstTrackpointSpeed;
		private final Double secondTrackpointSpeed;
		private final long pausedTime;
		private final long trackpointID;
		
		public PauseInformation(Double firstTrackpointSpeed, Double secondTrackpointSpeed, long pausedTime, long trackpointID){
			this.firstTrackpointSpeed = firstTrackpointSpeed;
			this.secondTrackpointSpeed = secondTrackpointSpeed;
			this.pausedTime = pausedTime;
			this.trackpointID = trackpointID;
		}
		
		public Double getFirstTrackpointSpeed(){
			return firstTrackpointSpeed;
		}
		
		public Double getSecondTrackpointSpeed(){
			return secondTrackpointSpeed;
		}
		
		public long getPausedTime(){
			return pausedTime;
		}
		
		public long getTrackpointID(){
			return trackpointID;
		}
		
		
	}
	
	
	private final String operationType;
	private final List<Long> changedCoursesIds;
	private final List<Long> duplicatePointIds;
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
	
	/*public Map<Long[], Double[]> getPausedSpeeds(){
		return pausedSpeeds;
	}*/
	
	public PauseInformation getPauseInformation(){
		return pauseInformation;
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
		
		public UndoItemBuilder pauseInformation(Double firstTrackpointSpeed, Double secondTrackpointSpeed, long pausedTime, long trackpointID){
			this.pauseInformation = new PauseInformation(firstTrackpointSpeed, secondTrackpointSpeed, pausedTime, trackpointID);
			return this;
		}

		public UndoItem build() {
			return new UndoItem(this);
		}
		
	

	}

}
