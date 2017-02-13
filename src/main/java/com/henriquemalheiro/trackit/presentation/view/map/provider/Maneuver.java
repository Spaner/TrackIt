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
package com.henriquemalheiro.trackit.presentation.view.map.provider;

import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.CoursePointType;

public enum Maneuver {
	LEFT("turn-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	SHARP_LEFT("turn-sharp-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	SLIGHT_LEFT("turn-slight-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	KEEP_LEFT("keep-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	U_TURN_LEFT("uturn-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	ROUNDABOUT_LEFT("roundabout-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	FORK_LEFT("fork-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	RAMP_LEFT("ramp-left") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	RIGHT("turn-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	SHARP_RIGHT("turn-sharp-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	SLIGHT_RIGHT("turn-slight-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	KEEP_RIGHT("keep-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	U_TURN_RIGHT("uturn-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	ROUNDABOUT_RIGHT("roundabout-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	FORK_RIGHT("fork-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	RAMP_RIGHT("ramp-right") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.RIGHT;
		}
	},
	STRAIGHT("straight") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.STRAIGHT;
		}
	},
	MERGE("merge") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.LEFT;
		}
	},
	FERRY("ferry") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.GENERIC;
		}
	},
	FERRY_TRAIN("ferry-train") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.GENERIC;
		}
	},
	ARRIVE("arrive") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.GENERIC;
		}
	},
	DEPART("depart") {
		@Override
		public CoursePointType getCoursePointType() {
			return CoursePointType.GENERIC;
		}
	};
	
	private static final String[] messageCodes = {"maneuver.left", "maneuver.sharpLeft", "maneuver.slightLeft", "maneuver.keepLeft",
		"maneuver.uturnLeft", "maneuver.roundaboutLeft", "maneuver.forkLeft", "maneuver.rampLeft", "maneuver.right",
		"maneuver.sharpRight", "maneuver.slightRight", "maneuver.keepRight", "maneuver.uturnRight", "maneuver.roundaboutRight",
		"maneuver.forkRight", "maneuver.rampRight", "maneuver.straight", "maneuver.merge", "maneuver.ferry", "maneuver.ferryTrain"};
	
	private String maneuverName;
	
	private Maneuver(String maneuverName) {
		this.maneuverName = maneuverName;
	}
	
	public String getManeuverName() {
		return maneuverName;
	}
	
	public CoursePointType getCoursePointType() {
		return CoursePointType.GENERIC;
	}
	
	public String getDescription() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
	
	public static Maneuver lookup(String maneuverName) {
		for (Maneuver maneuver : values()) {
			if (maneuver.getManeuverName().equalsIgnoreCase(maneuverName)) {
				return maneuver;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
}