package com.jb12335.trackit.business.domain;

public class Sport {
	private short sport;
	private short subSport;
	
	public Sport() {
		sport = subSport = -1;
	}
	
	public Sport( short sport, short subSport) {
		this.sport    = sport;
		this.subSport = subSport;
	}
	
	public short getSport() {
		return sport;
	}
	
	public short getSubSport() {
		return subSport;
	}

}
