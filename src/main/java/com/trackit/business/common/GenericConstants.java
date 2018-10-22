/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
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
package com.trackit.business.common;


public class GenericConstants {
	
	public static enum FieldType {
		TIME;
		
		private static final String[] descriptions = {"Time"};
		
		public String getDescription() {
			return descriptions[this.ordinal()];
		}
		
		public static FileType lookup(String fileTypeDescription) {
			for (int i = 0; i < FileType.values().length; i++) {
				if (FileType.values()[i].getDescription().equalsIgnoreCase(fileTypeDescription)) {
					return FileType.values()[i];
				}
			}
			return null;
		}
	}
	
	public static enum RemovePausesMethod {
		ONE_SECOND_RECORDING,
		SPEED;
		
		private static final String[] descriptions = {"1s recording", "speed"};
		
		public String getDescription() {
			return descriptions[this.ordinal()];
		}
		
		public static RemovePausesMethod lookup(String removePausesMethodDescription) {
			for (int i = 0; i < RemovePausesMethod.values().length; i++) {
				if (RemovePausesMethod.values()[i].getDescription().equalsIgnoreCase(removePausesMethodDescription)) {
					return RemovePausesMethod.values()[i];
				}
			}
			return null;
		}
	}
}
