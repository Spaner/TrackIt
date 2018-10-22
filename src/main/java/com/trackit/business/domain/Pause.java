/*
 * This file is part of Track It!.
 * Copyright (C) 2015 Pedro Gomes
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
package com.trackit.business.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Pause {

	private Date start;
	private Date end;
	private Date middle;
	private Double duration;  // seconds between startDate and endDate
	private SimpleDateFormat dateFormat;
	private int currentTime;
	
	public Pause(){
		dateFormat = new SimpleDateFormat("HH:mm:ss");
	}
	
	public Pause (Date start, Date end, Double duration){
		this();
		this.start = start;
		this.end = end;
		this.duration = duration;
		//this.middle = new Date(start.getTime() + 18000);
		this.middle = determineMiddle(start, end);
	}
	
	//12355: 2017-03-24
	public Pause clone() {
		return new Pause( this.start, this.end, this.duration);
	}
	
	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}
	
	public Date getMiddle(){
		return middle;
	}

	public Double getDuration() {
		return duration;
	}
	
	public int getCurrentTime(){
		return currentTime;
	}
	
	public void setCurrentTime(int currentTime){
		this.currentTime = currentTime;
	}
	
	public String toString(){
		return "Pause detected between: ["
				+ dateFormat.format(start) + "] - ["
//				+ dateFormat.format(end) + "]: " + duration			// 12335: 2018-07-16
				+ dateFormat.format(end) + "]: " + String.format( "%.1f", duration)
				+ " seconds";
	}
	
	public Date determineMiddle(Date start, Date end){
		
		//long t = end.getTime() - 20000;
		long t = start.getTime() + ((end.getTime() - start.getTime()) / 2);
				
		return new Date(t);
	}
	
}
