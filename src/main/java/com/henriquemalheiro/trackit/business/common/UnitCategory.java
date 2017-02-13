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
package com.henriquemalheiro.trackit.business.common;

public enum UnitCategory {
	NONE("NONE"), DISTANCE("DISTANCE"), HEIGHT("HEIGHT"), TIME("TIME"), TIMESTAMP("TIMESTAMP"), DURATION("DURATION"), TEMPERATURE(
	        "TEMPERATURE"), ANGLE("ANGLE"), CALORIES("CALORIES"), HEART_RATE("HEART_RATE"), CADENCE("CADENCE"), SPEED(
	        "SPEED"), VERSION("VERSION"), PRODUCT("PRODUCT"), GRADE("GRADE"), POWER("POWER"), WORK("WORK"), VOLTAGE(
	        "VOLTAGE"), TRACKPOINT_DENSITY("TRACKPOINT_DENSITY");

	private String name;

	private UnitCategory(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	private static final String[] messageCodes = { "unitCategory.none", "unitCategory.distance", "unitCategory.height",
	        "unitCategory.time", "unitCategory.timestamp", "unitCategory.duration", "unitCategory.temperature",
	        "unitCategory.angle", "unitCategory.calories", "unitCategory.heartRate", "unitCategory.cadence",
	        "unitCategory.speed", "unitCategory.version", "unitCategory.product", "unitCategory.grade",
	        "unitCategory.power", "unitCategory.work", "unitCategory.voltage", "unitCategory.trackpointDensity" };

	public static UnitCategory lookup(String unitCategoryName) {
		for (UnitCategory unitCategory : values()) {
			if (unitCategoryName.equals(unitCategory.getName())) {
				return unitCategory;
			}
		}

		return null;
	}

	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}