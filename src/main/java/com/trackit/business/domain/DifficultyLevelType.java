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

public enum DifficultyLevelType {
	UNDEFINED( 		(short) 0),
	UNKNOWN (		(short) 1),
	EASY (	 		(short) 2),
	MODERATE (		(short) 3),
	DIFFICULT(		(short) 4),
	VERY_DIFFICULT(	(short) 5),
	EXPERTS_ONLY ( 	(short) 6);
	
	private short value;
	
	private DifficultyLevelType( short value) {
		this.value = value;
	}
	
	public short getValue() {
		return this.value;
	}

	public static DifficultyLevelType lookup( short difficultyLevelType) {
		for (DifficultyLevelType difficultyType : values()) {
			if ( difficultyLevelType == difficultyType.getValue() ) {
				return difficultyType;
			}
		}
		return null;
	}
	
	public static DifficultyLevelType lookup( String name) {
		for( DifficultyLevelType difficulty: values() ) {
			if ( difficulty.toString().equals( name) )
				return difficulty;
		}
		return null;
	}
	

	@Override
	public String toString() {
		return Messages.getMessage( messageCodes[this.ordinal()]);
	}
	
	public static List<DifficultyLevelType> getList( boolean addAll) {
		List<DifficultyLevelType> list = getList();
		if ( ! addAll )
			list.remove( UNDEFINED);
		return list;
	}
	
	public static List<DifficultyLevelType> getList() {
		List<DifficultyLevelType> list = new ArrayList<>();
		for( DifficultyLevelType difficulty: values() )
			list.add( difficulty);
		return list;
	}
	
	private static String[] messageCodes = {
			"difficultyLevelType.undefined",
			"difficultyLevelType.unknown",
			"difficultyLevelType.easy",
			"difficultyLevelType.moderate",
			"difficultyLevelType.difficult",
			"difficultyLevelType.veryDifficult",
			"difficultyLevelType.expertsOnly" };
}
