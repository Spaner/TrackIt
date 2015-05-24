package com.henriquemalheiro.trackit.business.operation;

import java.util.ArrayList;
import java.util.List;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;

public class UndoItem {
	
	private String operationType;
	private List<Long> changedCoursesIds;
	private long documentId;
	private Trackpoint trackpoint;
	
	public UndoItem(){
	}
	
	public UndoItem(String operation, long documentId, List<Long> coursesIds, Trackpoint trackpoint) {
		this.operationType = operation;
		this.changedCoursesIds = new ArrayList<Long>();
		this.trackpoint = trackpoint;
		this.documentId = documentId;
		addCourses(changedCoursesIds, coursesIds);
		
	}
	
	public String getOperationType(){
		return operationType;
	}
	
	public List<Long> getCoursesIds(){
		return changedCoursesIds;
	}
	
	public long getDocumentId(){
		return documentId;
	}
	
	public Trackpoint getTrackpoint(){
		return trackpoint;
	}
	
	public long getFirstCourseId(){
		int first = 0;
		return changedCoursesIds.get(first);
	}
	
	public long getCourseIdAt(int index){
		return changedCoursesIds.get(index);
	}
	
	
	
	private void addCourses(List<Long> newList, List<Long> courseIds){
		int listIndex = 0;
		while (listIndex < courseIds.size()) {
			newList.add(courseIds.get(listIndex));
			listIndex++;
		}
	}

		
}
