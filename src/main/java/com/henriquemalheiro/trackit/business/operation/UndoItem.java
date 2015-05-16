package com.henriquemalheiro.trackit.business.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;

public class UndoItem {
	
	private String operationType;
	private List<Course> changedCourses;
	private Trackpoint trackpoint;
	
	public UndoItem() {
		changedCourses = new ArrayList<Course>();
	}
	
	public UndoItem(String operation, List<Course> courseList, Trackpoint trackpoint) {
		this.operationType = operation;
		this.changedCourses = new ArrayList<Course>();
		this.trackpoint = trackpoint;
		addCourses(changedCourses, courseList);
		
	}
	
	
	public String getOperationType(){
		return operationType;
	}
	
	public void addCourses(List<Course> newList, List<Course> courseList){
		int listIndex = 0;
		while (listIndex < courseList.size()) {
			newList.add(courseList.get(listIndex));
			listIndex++;
		}
	}
	
	public List<Course> getCourses(){
		return changedCourses;
	}
	
	public Trackpoint getTrackpoint(){
		return trackpoint;
	}
	
	public Course getCourseAt(int index){
		return changedCourses.get(index);
	}
	
	public Course getFirstCourse(){
		int first = 0;
		return changedCourses.get(first);
	}
		
}
