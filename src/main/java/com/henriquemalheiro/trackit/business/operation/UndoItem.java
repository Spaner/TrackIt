package com.henriquemalheiro.trackit.business.operation;

import java.util.List;

import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;

public class UndoItem {

	private final String operationType;
	private final List<Long> changedCoursesIds;
	private final long documentId;
	private final Trackpoint trackpoint;
	private final String reverseMode;
	private final Course course;
	private final Double splitSpeed;
	
	private UndoItem(UndoItemBuilder builder) {
		this.operationType = builder.operationType;
		this.changedCoursesIds = builder.changedCoursesIds;
		this.documentId = builder.documentId;
		this.trackpoint = builder.trackpoint;
		this.reverseMode = builder.reverseMode;
		this.course = builder.course;
		this.splitSpeed = builder.splitSpeed;
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

	public long getFirstCourseId() {
		int first = 0;
		return changedCoursesIds.get(first);
	}

	public long getCourseIdAt(int index) {
		return changedCoursesIds.get(index);
	}

	public static class UndoItemBuilder {
		private final String operationType;
		private final List<Long> changedCoursesIds;
		private final long documentId;
		private Trackpoint trackpoint;
		private String reverseMode;
		private Course course;
		private Double splitSpeed;

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

		public UndoItem build() {
			return new UndoItem(this);
		}

	}

}
