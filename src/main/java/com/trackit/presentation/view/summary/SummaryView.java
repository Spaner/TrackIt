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
package com.trackit.presentation.view.summary;

import java.awt.CardLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.CoursePoint;
import com.trackit.business.domain.DeviceInfo;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Event;
import com.trackit.business.domain.Lap;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.TrackSegment;
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.domain.Waypoint;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.view.summary.SummaryViewFactory.SummaryViewPanelType;


public class SummaryView extends JPanel implements EventListener {
	private static final long serialVersionUID = -3659381533867705833L;
	
	private static final String NO_INFO_PANEL = "NO_INFO_PANEL";
	private static final String ACTIVITY_PANEL = "ACTIVITY_PANEL";
	private static final String SESSION_PANEL = "SESSION_PANEL";
	private static final String COURSE_PANEL = "COURSE_PANEL";
	private static final String COURSE_POINT_PANEL = "COURSE_POINT_PANEL";
	private static final String LAP_PANEL = "LAP_PANEL";
	private static final String EVENT_PANEL = "EVENT_PANEL";
	private static final String DEVICE_PANEL = "DEVICE_PANEL";
	private static final String WAYPOINT_PANEL = "WAYPOINT_PANEL";
	private static final String TRACKPOINT_PANEL = "TRACKPOINT_PANEL";
	private static final String SEGMENT_PANEL = "SEGMENT_PANEL";
	
	private SummaryViewPanel noInfoPanel;
	private SummaryViewPanel activityPanel;
	private SummaryViewPanel sessionPanel;
	private SummaryViewPanel coursePanel;
	private SummaryViewPanel coursePointPanel;
	private SummaryViewPanel lapPanel;
	private SummaryViewPanel eventPanel;
	private SummaryViewPanel devicePanel;
	private SummaryViewPanel waypointPanel;
	private SummaryViewPanel trackpointPanel;
	private SummaryViewPanel segmentPanel;
	
	private CardLayout cardLayout;
	
	public SummaryView() {
		initComponents();
	}
	
	private void initComponents() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		SummaryViewFactory factory = SummaryViewFactory.getInstance();
		
		noInfoPanel = factory.createPanel(SummaryViewPanelType.NO_INFO_PANEL);
		activityPanel = factory.createPanel(SummaryViewPanelType.ACTIVITY_PANEL);
		sessionPanel = factory.createPanel(SummaryViewPanelType.SESSION_PANEL);
		coursePanel = factory.createPanel(SummaryViewPanelType.COURSE_PANEL);
		coursePointPanel = factory.createPanel(SummaryViewPanelType.COURSE_POINT_PANEL);
		lapPanel = factory.createPanel(SummaryViewPanelType.LAP_PANEL);
		eventPanel = factory.createPanel(SummaryViewPanelType.EVENT_PANEL);
		devicePanel = factory.createPanel(SummaryViewPanelType.DEVICE_PANEL);
		waypointPanel = factory.createPanel(SummaryViewPanelType.WAYPOINT_PANEL);
		trackpointPanel = factory.createPanel(SummaryViewPanelType.TRACKPOINT_PANEL);
		segmentPanel = factory.createPanel(SummaryViewPanelType.SEGMENT_PANEL);
        
        add(noInfoPanel, NO_INFO_PANEL);
		add(activityPanel, ACTIVITY_PANEL);
		add(sessionPanel, SESSION_PANEL);
		add(coursePanel, COURSE_PANEL);
		add(coursePointPanel, COURSE_POINT_PANEL);
		add(lapPanel, LAP_PANEL);
		add(eventPanel, EVENT_PANEL);
		add(devicePanel, DEVICE_PANEL);
		add(waypointPanel, WAYPOINT_PANEL);
		add(trackpointPanel, TRACKPOINT_PANEL);
		add(segmentPanel, SEGMENT_PANEL);
		
		cardLayout.first(this);
	}
	
	/* Event Listener interface implementation */
	
	@Override
	public void process(com.trackit.presentation.event.Event event, DocumentItem item) {
		switch (event) {
		case ACTIVITY_SELECTED:
		case ACTIVITY_UPDATED:
			showInfo((Activity) item);
			break;
		case COURSE_SELECTED:
		case COURSE_UPDATED:
			showInfo((Course) item);
			break;
		case SESSION_SELECTED:
			showInfo((Session) item);
			break;
		case LAP_SELECTED:
			showInfo((Lap) item);
			break;
		case EVENT_SELECTED:
			showInfo((Event) item);
			break;
		case DEVICE_SELECTED:
			showInfo((DeviceInfo) item);
			break;
		case COURSE_POINT_SELECTED:
			showInfo((CoursePoint) item);
			break;
		case WAYPOINT_SELECTED:
			showInfo((Waypoint) item);
			break;
		case TRACKPOINT_SELECTED:
			showInfo((Trackpoint) item);
			break;
		case TRACKPOINT_HIGHLIGHTED:
			// Do nothing
			break;
		case SEGMENT_SELECTED:
			showInfo((TrackSegment) item);
			break;
		case ZOOM_TO_ITEM:
			break;
		default:
			showNoInfo();
		}
	}
	
	private void showInfo(Activity activity) {
		SummaryViewFactory.getInstance().updatePanelInfo(activityPanel, activity);
		cardLayout.show(this, ACTIVITY_PANEL);
	}
	
	private void showInfo(Course course) {
		SummaryViewFactory.getInstance().updatePanelInfo(coursePanel, course);
		cardLayout.show(this, COURSE_PANEL);
	}
	
	private void showInfo(Session session) {
		SummaryViewFactory.getInstance().updatePanelInfo(sessionPanel, session);
		cardLayout.show(this, SESSION_PANEL);
	}
	
	private void showInfo(Lap lap) {
		SummaryViewFactory.getInstance().updatePanelInfo(lapPanel, lap);
		cardLayout.show(this, LAP_PANEL);
	}
	
	private void showInfo(Event event) {
		SummaryViewFactory.getInstance().updatePanelInfo(eventPanel, event);
		cardLayout.show(this, EVENT_PANEL);
	}
	
	private void showInfo(DeviceInfo device) {
		SummaryViewFactory.getInstance().updatePanelInfo(devicePanel, device);
		cardLayout.show(this, DEVICE_PANEL);
	}
	
	private void showInfo(CoursePoint coursePoint) {
		SummaryViewFactory.getInstance().updatePanelInfo(coursePointPanel, coursePoint);
		cardLayout.show(this, COURSE_POINT_PANEL);
	}
	
	private void showInfo(Waypoint waypoint) {
		SummaryViewFactory.getInstance().updatePanelInfo(waypointPanel, waypoint);
		cardLayout.show(this, WAYPOINT_PANEL);
	}
	
	private void showInfo(Trackpoint trackpoint) {
		SummaryViewFactory.getInstance().updatePanelInfo(trackpointPanel, trackpoint);
		cardLayout.show(this, TRACKPOINT_PANEL);
	}
	
	private void showInfo(TrackSegment segment) {
		SummaryViewFactory.getInstance().updatePanelInfo(segmentPanel, segment);
		cardLayout.show(this, SEGMENT_PANEL);
	}
	
	private void showNoInfo() {
		cardLayout.show(this, NO_INFO_PANEL);
	}
	
	@Override
	public void process(com.trackit.presentation.event.Event event, DocumentItem parent,
			List<? extends DocumentItem> items) {
		switch (event) {
		default:
			showNoInfo();
		}
	}
	
	@Override
	public String toString() {
		return Messages.getMessage("view.summary.name");
	}
	
	public SummaryViewPanel getActivityPanel() {
		return activityPanel;
	}
}
