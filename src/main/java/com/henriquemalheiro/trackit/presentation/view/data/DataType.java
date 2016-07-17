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
package com.henriquemalheiro.trackit.presentation.view.data;

import java.util.Collection;

import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.ActivityMetadata;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.DeviceInfo;
import com.henriquemalheiro.trackit.business.domain.Event;
import com.henriquemalheiro.trackit.business.domain.FieldMetadata;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.Track;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.domain.Waypoint;

public enum DataType implements Columnizable {
	NONE {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			return new FieldMetadata[0];
		}
	},
	DOCUMENT {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = GPSDocument.getFieldsMetadata(GPSDocument.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	ACTIVITY {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Activity.getFieldsMetadata(Activity.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	COURSE {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Course.getFieldsMetadata(Course.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	WAYPOINT {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Waypoint.getFieldsMetadata(Waypoint.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	ACTIVITY_METADATA {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = ActivityMetadata.getFieldsMetadata(ActivityMetadata.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	SESSION {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Session.getFieldsMetadata(Session.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	LAP {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Lap.getFieldsMetadata(Lap.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	TRACK {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Track.getFieldsMetadata(Track.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	TRACK_SEGMENT {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = TrackSegment.getFieldsMetadata(TrackSegment.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	TRACKPOINT {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Trackpoint.getFieldsMetadata(Trackpoint.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	COURSE_POINT {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = CoursePoint.getFieldsMetadata(CoursePoint.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	},
	EVENT {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = Event.getFieldsMetadata(Event.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	}, 
	DEVICE {
		@Override
		public FieldMetadata[] getFieldsMetadata() {
			Collection<FieldMetadata> fieldsMetadata = DeviceInfo.getFieldsMetadata(DeviceInfo.class.getName());
			return fieldsMetadata.toArray(new FieldMetadata[fieldsMetadata.size()]);
		}
	};
	
	private static final String[] names = new String[] { "dataView.dataType.none", "dataView.dataType.document", "dataView.dataType.activity",
		"dataView.dataType.course", "dataView.dataType.waypoint", "dataView.dataType.activityMetadata", "dataView.dataType.session",
		"dataView.dataType.lap", "dataView.dataType.track", "dataView.dataType.segment", "dataView.dataType.trackpoint",
		"dataView.dataType.coursePoint", "dataView.dataType.event", "dataView.dataType.device" };
	
	public String getName() {
		return Messages.getMessage(names[this.ordinal()]);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
