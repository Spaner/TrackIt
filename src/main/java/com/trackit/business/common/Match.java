package com.trackit.business.common;

import com.trackit.business.domain.Activity;

public class Match {
	private Activity activity;
	private double matchingGoal;
	private int matchingTrackpoints;
	private Direction direction;
	
	public Match(Activity activity) {
		this.activity = activity;
	}
	
	public Activity getMatchingActivity() {
		return activity;
	}
	
	public void addMatchingTrackpoints(int count) {
		this.matchingTrackpoints += count;
	}
	
	public double getMatchingPercentage() {
		return ((double) matchingTrackpoints / (double) matchingGoal);
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public void setMatchingGoal(int count) {
		this.matchingGoal = count;
	}
	
	@Override
	public String toString() {
		return String.format("Activity %s matches segment by %.1f %%, on %s direction.",
				activity.getName(), getMatchingPercentage(), direction.name());
	}
}