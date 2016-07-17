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
package com.henriquemalheiro.trackit.presentation.view.folder;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.henriquemalheiro.trackit.business.common.Predicate;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.DeviceInfo;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Event;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.SegmentCategory;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.miguelpernas.trackit.business.domain.SegmentColorCategory;
import com.pg58406.trackit.business.domain.PhotoContainer;
import com.pg58406.trackit.business.domain.Picture;
import com.pg58406.trackit.presentation.view.folder.PicturesItem;

public class FolderTreeFactory {
	private static final FolderTreeFactory instance = new FolderTreeFactory();

	private FolderTreeFactory() {
	}

	synchronized static FolderTreeFactory getInstance() {
		return instance;
	}

	DefaultMutableTreeNode createDocumentNode(final GPSDocument document) {
		DefaultMutableTreeNode documentNode = new DefaultMutableTreeNode(
				document);

		DefaultMutableTreeNode activitiesNode = createActivitiesNode(document);
		documentNode.add(activitiesNode);

		DefaultMutableTreeNode coursesNode = createCoursesNode(document);
		documentNode.add(coursesNode);

		DefaultMutableTreeNode waypointsNode = createWaypointsNode(document);
		documentNode.add(waypointsNode);

		return documentNode;
	}

	private DefaultMutableTreeNode createActivitiesNode(
			final GPSDocument document) {
		ActivitiesItem activitiesItem = new ActivitiesItem(document);
		DefaultMutableTreeNode activitiesNode = new DefaultMutableTreeNode(
				activitiesItem);

		for (Activity activity : document.getActivities()) {
			DefaultMutableTreeNode activityNode = createActivityNode(activity);
			activitiesNode.add(activityNode);
		}

		return activitiesNode;
	}

	DefaultMutableTreeNode createActivityNode(final Activity activity) {
		DefaultMutableTreeNode activityNode = new DefaultMutableTreeNode(
				activity);

		DefaultMutableTreeNode sessionsNode = createSessionsNode(activity);
		activityNode.add(sessionsNode);

		DefaultMutableTreeNode devicesNode = createDevicesNode(activity);
		activityNode.add(devicesNode);

		DefaultMutableTreeNode eventsNode = createEventsNode(activity);
		activityNode.add(eventsNode);		

		DefaultMutableTreeNode picturesNodes = createPicturesNode(activity);
		activityNode.add(picturesNodes);

		return activityNode;
	}

	private DefaultMutableTreeNode createSessionsNode(final Activity activity) {
		SessionsItem sessionsItem = new SessionsItem(activity);
		DefaultMutableTreeNode sessionsNode = new DefaultMutableTreeNode(
				sessionsItem);

		for (Session session : activity.getSessions()) {
			DefaultMutableTreeNode sessionNode = createSessionNode(session);
			sessionsNode.add(sessionNode);
		}

		return sessionsNode;
	}

	private DefaultMutableTreeNode createSessionNode(Session session) {
		DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode(session);

		DefaultMutableTreeNode lapsNode = createLapsNode(session.getParent(),
				session.getLaps());
		sessionNode.add(lapsNode);

		return sessionNode;
	}

	private DefaultMutableTreeNode createLapsNode(final DocumentItem parent,
			final List<Lap> laps) {
		LapsItem lapsItem = new LapsItem(parent);
		DefaultMutableTreeNode lapsNode = new DefaultMutableTreeNode(lapsItem);

		for (Lap lap : laps) {
			DefaultMutableTreeNode lapNode = new DefaultMutableTreeNode(lap);
			lapsNode.add(lapNode);
		}

		return lapsNode;
	}

	private DefaultMutableTreeNode createCoursesNode(final GPSDocument document) {
		CoursesItem coursesItem = new CoursesItem(document);
		DefaultMutableTreeNode coursesNode = new DefaultMutableTreeNode(
				coursesItem);

		for (Course course : document.getCourses()) {
			DefaultMutableTreeNode courseNode = createCourseNode(course);
			coursesNode.add(courseNode);
		}

		return coursesNode;
	}

	DefaultMutableTreeNode createCourseNode(final Course course) {
		DefaultMutableTreeNode courseNode = new DefaultMutableTreeNode(course);

		DefaultMutableTreeNode lapsNode = createLapsNode(course,
				course.getLaps());
		courseNode.add(lapsNode);

		DefaultMutableTreeNode coursePointsNode = createCoursePointsNode(course);
		courseNode.add(coursePointsNode);

		DefaultMutableTreeNode devicesNode = createDevicesNode(course);
		courseNode.add(devicesNode);

		DefaultMutableTreeNode eventsNode = createEventsNode(course);
		courseNode.add(eventsNode);

		DefaultMutableTreeNode segmentsNode = createSegmentsNode(course);
		courseNode.add(segmentsNode);		

		DefaultMutableTreeNode picturesNodes = createPicturesNode(course);
		courseNode.add(picturesNodes);

		return courseNode;
	}

	private DefaultMutableTreeNode createCoursePointsNode(final Course course) {
		CoursePointsItem coursePointsItem = new CoursePointsItem(course);
		DefaultMutableTreeNode coursePointsNode = new DefaultMutableTreeNode(
				coursePointsItem);

		for (CoursePoint coursePoint : course.getCoursePoints()) {
			DefaultMutableTreeNode coursePointNode = new DefaultMutableTreeNode(
					coursePoint);
			coursePointsNode.add(coursePointNode);
		}

		return coursePointsNode;
	}

	private DefaultMutableTreeNode createDevicesNode(final DocumentItem parent) {
		DevicesItem devicesItem = new DevicesItem(parent);
		DefaultMutableTreeNode devicesNode = new DefaultMutableTreeNode(
				devicesItem);

		for (DeviceInfo device : parent.getDevices()) {
			DefaultMutableTreeNode deviceNode = new DefaultMutableTreeNode(
					device);
			devicesNode.add(deviceNode);
		}

		return devicesNode;
	}

	private DefaultMutableTreeNode createEventsNode(final DocumentItem parent) {
		EventsItem eventsItem = new EventsItem(parent);
		DefaultMutableTreeNode eventsNode = new DefaultMutableTreeNode(
				eventsItem);

		for (Event event : parent.getEvents()) {
			DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(event);
			eventsNode.add(eventNode);
		}

		return eventsNode;
	}

	private DefaultMutableTreeNode createSegmentsNode(final DocumentItem item) {
		SegmentsItem segmentsItem = new SegmentsItem(item);
		DefaultMutableTreeNode segmentsNode = new DefaultMutableTreeNode(
				segmentsItem);

		List<TrackSegment> segments = item.getSegments();
		segments = Utilities.filter(segments, new Predicate<TrackSegment>() {
			@Override
			public boolean apply(TrackSegment segment) {
				SegmentCategory cat = segment.getCategory();
				SegmentColorCategory colorCat = segment.getColorCategory();

				return ((cat != SegmentCategory.UNCATEGORIZED_SEGMENT
						&& cat != SegmentCategory.UNCATEGORIZED_CLIMB && cat != SegmentCategory.UNCATEGORIZED_DESCENT) || colorCat!=SegmentColorCategory.UNCATEGORIZED);
			}
		});

		for (TrackSegment segment : segments) {
			DefaultMutableTreeNode segmentNode = new DefaultMutableTreeNode(
					segment);
			segmentsNode.add(segmentNode);
		}

		return segmentsNode;
	}

	private DefaultMutableTreeNode createWaypointsNode(
			final GPSDocument document) {
		WaypointsItem waypointsItem = new WaypointsItem(null, document);
		DefaultMutableTreeNode waypointsNode = new DefaultMutableTreeNode(
				waypointsItem);

		for (Waypoint waypoint : document.getWaypoints()) {
			DefaultMutableTreeNode waypointNode = new DefaultMutableTreeNode(
					waypoint);
			waypointsNode.add(waypointNode);
		}

		return waypointsNode;
	}

	private DefaultMutableTreeNode createPicturesNode(
			final PhotoContainer container) {
		PicturesItem picturesItem = new PicturesItem(container);
		DefaultMutableTreeNode picturesNode = new DefaultMutableTreeNode(
				picturesItem);

		for (Picture pic : container.getPictures()) {
			DefaultMutableTreeNode pictureNode = new DefaultMutableTreeNode(pic);
			picturesNode.add(pictureNode);
		}
		
		return picturesNode;
	}
}