/*
 * This file is part of Track It!.
 * Copyright (C) 2017 João Brisson Lopes
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

import java.util.ArrayList;
import java.util.List;

import com.trackit.business.common.Messages;

public enum TrackConditionType {
	UNDEFINED( 	(short) 0),
	UNKNOWN( 	(short) 1),
	UNKEPT(     (short) 2),
	BAD( 		(short) 3),
	ACCEPTABLE( (short) 4),
	GOOD(  		(short) 5),
	VERY_GOOD(  (short) 6);
	
	private short value;
	
	private TrackConditionType( short value) {
		this.value = value;
	}
	
	public short getValue() {
		return this.value;
	}

	public static TrackConditionType lookup( short trackConditionType) {
		for (TrackConditionType conditionType : values()) {
			if ( trackConditionType == conditionType.getValue() ) {
				return conditionType;
			}
		}
		return null;
	}
	
	public static TrackConditionType lookup( String name) {
		for( TrackConditionType condition: values() )
			if( condition.toString().equals( name) )
				return condition;
		return null;
	}

	@Override
	public String toString() {
		return Messages.getMessage( messageCodes[this.ordinal()]);
	}
	
	public static List<TrackConditionType> getList( boolean addAll) {
		List<TrackConditionType> list = getList();
		if ( !addAll )
			list.remove( UNDEFINED);
		return list;
	}
	
	public static List<TrackConditionType> getList() {
		List<TrackConditionType> list = new ArrayList<>();
		for( TrackConditionType condition: values())
			list.add( condition);
		return list;
	}
	
	private static String[] messageCodes = {
			"conditionLevelType.undefined",
			"conditionLevelType.unknown",
			"conditionLevelType.unkept",
			"conditionLevelType.bad",
			"conditionLevelType.acceptable",
			"conditionLevelType.good",
			"conditionLevelType.veryGood" };

}
