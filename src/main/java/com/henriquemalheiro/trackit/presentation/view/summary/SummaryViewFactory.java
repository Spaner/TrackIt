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
package com.henriquemalheiro.trackit.presentation.view.summary;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Predicate;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.ActivityMetadata;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.DeviceInfo;
import com.henriquemalheiro.trackit.business.domain.Event;
import com.henriquemalheiro.trackit.business.domain.FieldGroup;
import com.henriquemalheiro.trackit.business.domain.FieldMetadata;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Session;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;


public class SummaryViewFactory {
	public static enum SummaryViewPanelType {
		NO_INFO_PANEL,
		ACTIVITY_PANEL,
		SESSION_PANEL,
		COURSE_PANEL,
		COURSE_POINT_PANEL,
		LAP_PANEL,
		EVENT_PANEL,
		DEVICE_PANEL,
		WAYPOINT_PANEL,
		TRACKPOINT_PANEL,
		SEGMENT_PANEL
	};
	private static SummaryViewFactory instance;
	
	private SummaryViewFactory() {
	}
	
	public static synchronized SummaryViewFactory getInstance() {
		if (instance == null) {
			instance = new SummaryViewFactory();
		}
		return instance;
	}
	
	public SummaryViewPanel createPanel(SummaryViewPanelType panelType) {
		return createPanel(panelType, Collections.<FieldGroup>emptyList());
	}
	
	public SummaryViewPanel createPanel(SummaryViewPanelType panelType, List<FieldGroup> groups) {
		SummaryViewPanel panel = null;
		
		switch (panelType) {
		case NO_INFO_PANEL :
			panel = createNoInfoPanel();
			break;
		case ACTIVITY_PANEL :
			panel = createActivityPanel(groups);
			break;
		case SESSION_PANEL :
			panel = createSessionPanel();
			break;
		case COURSE_PANEL :
			panel = createCoursePanel(groups);
			break;
		case COURSE_POINT_PANEL :
			panel = createCoursePointPanel();
			break;
		case LAP_PANEL :
			panel = createLapPanel();
			break;
		case EVENT_PANEL :
			panel = createEventPanel();
			break;
		case DEVICE_PANEL :
			panel = createDevicePanel();
			break;
		case WAYPOINT_PANEL :
			panel = createWaypointPanel();
			break;
		case TRACKPOINT_PANEL :
			panel = createTrackpointPanel();
			break;
		case SEGMENT_PANEL :
			panel = createSegmentPanel();
			break;
		default :
		}
		return panel;
	}
	
	private SummaryViewPanel createNoInfoPanel() {
		JPanel noInfoPanel = new JPanel();
		noInfoPanel.setBackground(Color.WHITE);
		noInfoPanel.add(new JLabel(getMessage("summaryView.label.noInfoToDisplay")),
				BorderLayout.PAGE_START);
		
		return new SummaryViewPanel(noInfoPanel, new ArrayList<DisplayValue>());
	}
	
	private SummaryViewPanel createActivityPanel(final List<FieldGroup> groups) {
		Collection<FieldMetadata> fieldsMetadata = new LinkedHashSet<FieldMetadata>();
		fieldsMetadata.addAll(Activity.getFieldsMetadata(Activity.class.getName()));
		fieldsMetadata.addAll(ActivityMetadata.getFieldsMetadata(ActivityMetadata.class.getName()));
		fieldsMetadata.addAll(Session.getFieldsMetadata(Session.class.getName()));
		
		return createGenericPanel(fieldsMetadata, groups);
	}
	
	private SummaryViewPanel createSessionPanel() {
		return createGenericPanel(Session.getFieldsMetadata(Session.class.getName()));
	}
	
	private SummaryViewPanel createCoursePanel(final List<FieldGroup> groups) {
		return createGenericPanel(Course.getFieldsMetadata(Course.class.getName()), groups);
	}
	
	private SummaryViewPanel createCoursePointPanel() {
		return createGenericPanel(CoursePoint.getFieldsMetadata(Course.class.getName()));
	}
	
	private SummaryViewPanel createLapPanel() {
		return createGenericPanel(Lap.getFieldsMetadata(Lap.class.getName()));
	}
	
	private SummaryViewPanel createEventPanel() {
		return createGenericPanel(Event.getFieldsMetadata(Event.class.getName()));
	}
	
	private SummaryViewPanel createDevicePanel() {
		return createGenericPanel(DeviceInfo.getFieldsMetadata(DeviceInfo.class.getName()));
	}
	
	private SummaryViewPanel createWaypointPanel() {
		return createGenericPanel(Waypoint.getFieldsMetadata(Waypoint.class.getName()));
	}
	
	private SummaryViewPanel createTrackpointPanel() {
		return createGenericPanel(Trackpoint.getFieldsMetadata(Trackpoint.class.getName()));
	}
	
	private SummaryViewPanel createSegmentPanel() {
		return createGenericPanel(TrackSegment.getFieldsMetadata(TrackSegment.class.getName()));
	}
	
	private SummaryViewPanel createGenericPanel(Collection<FieldMetadata> fieldsMetadata) {
		return createGenericPanel(fieldsMetadata, Collections.<FieldGroup>emptyList());
	}
	
	private SummaryViewPanel createGenericPanel(Collection<FieldMetadata> fieldsMetadata, List<FieldGroup> groups) {
		JPanel genericPanel = new JPanel();
		genericPanel.setLayout(new BoxLayout(genericPanel, BoxLayout.PAGE_AXIS));
		genericPanel.setBackground(Color.WHITE);
		
		JLabel label = null;
		JTextField value = null;
		
		List<DisplayValue> displayValues = new ArrayList<DisplayValue>();
		for (FieldMetadata metadata : fieldsMetadata) {
			for (FieldGroup group : metadata.getGroups()) {
				label = new JLabel(getMessage(metadata.getMessageCode()));
				label.setVisible(false);
				value = new JTextField();
				value.setVisible(false);
				value.setEditable(false);
				value.setBackground(null);
				value.setBorder(null);

				displayValues.add(new DisplayValue(group, metadata.getFieldName(), label, value));
			}
		}
		
		// Add Groups
		for (FieldGroup group : FieldGroup.values()) {
			if (!groups.isEmpty() && !groups.contains(group)) {
				continue;
			}
			addGroup(genericPanel, group, displayValues);
		}
		
		fillBottomSpace(genericPanel);
		
		return new SummaryViewPanel(genericPanel, displayValues);
	}
	
	public void updatePanelInfo(SummaryViewPanel panel, Activity activity) {
		Object newValue = null;
		FieldMetadata metadata = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = activity.get(displayValue.getFieldName());
			metadata = Activity.getMetadata(Activity.class.getName(), displayValue.getFieldName());
			if (metadata == null) {
				continue;
			}
			updateFieldValue(metadata, displayValue, newValue);
		}
		
		
		Session session = activity.getFirstSession();
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = session.get(displayValue.getFieldName());
			metadata = Session.getMetadata(Session.class.getName(), displayValue.getFieldName());
			if (metadata == null) {
				continue;
			}
			updateFieldValue(metadata, displayValue, newValue);
		}
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = activity.getMetadata().get(displayValue.getFieldName());
			metadata = ActivityMetadata.getMetadata(ActivityMetadata.class.getName(), displayValue.getFieldName());
			if (metadata == null) {
				continue;
			}
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, Session session) {
		Object newValue = null;
		FieldMetadata metadata = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = session.get(displayValue.getFieldName());
			metadata = Session.getMetadata(Session.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	public void updatePanelInfo(SummaryViewPanel panel, Course course) {
		FieldMetadata metadata = null;
		Object newValue = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = course.get(displayValue.getFieldName());
			metadata = Course.getMetadata(Course.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, CoursePoint coursePoint) {
		FieldMetadata metadata = null;
		Object newValue = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = coursePoint.get(displayValue.getFieldName());
			metadata = CoursePoint.getMetadata(CoursePoint.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, Lap lap) {
		FieldMetadata metadata = null;
		Object newValue = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = lap.get(displayValue.getFieldName());
			metadata = Lap.getMetadata(Lap.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, Event event) {
		FieldMetadata metadata = null;
		Object newValue = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = event.get(displayValue.getFieldName());
			metadata = Event.getMetadata(Event.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, DeviceInfo device) {
		Object newValue = null;
		FieldMetadata metadata = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = device.get(displayValue.getFieldName());
			metadata = DeviceInfo.getMetadata(DeviceInfo.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, Waypoint waypoint) {
		Object newValue = null;
		FieldMetadata metadata = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = waypoint.get(displayValue.getFieldName());
			metadata = DeviceInfo.getMetadata(Waypoint.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, Trackpoint trackpoint) {
		Object newValue = null;
		FieldMetadata metadata = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = trackpoint.get(displayValue.getFieldName());
			metadata = Trackpoint.getMetadata(Trackpoint.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	void updatePanelInfo(SummaryViewPanel panel, TrackSegment segment) {
		Object newValue = null;
		FieldMetadata metadata = null;
		
		for (DisplayValue displayValue : panel.getDisplayValues()) {
			newValue = segment.get(displayValue.getFieldName());
			metadata = TrackSegment.getMetadata(TrackSegment.class.getName(), displayValue.getFieldName());
			updateFieldValue(metadata, displayValue, newValue);
		}
	}
	
	private void updateFieldValue(FieldMetadata metadata, DisplayValue displayValue, Object newValue) {
		if (newValue != null && metadata != null) {
			String value = Formatters.getFormatedValue(newValue, metadata);
			value = processText(value);
			
			displayValue.getValue().setText(value);
			displayValue.getLabel().setVisible(true);
			displayValue.getValue().setVisible(true);
		} else {
			displayValue.getLabel().setVisible(false);
			displayValue.getValue().setVisible(false);
		}
	}
	
	private String processText(String text) {
		String processedText = text;
		if (processedText.contains("\n")) {
			StringBuilder textBuilder = new StringBuilder();
			textBuilder.append("<html><body style='width:145px;'>");
			textBuilder.append(text.replace("\n", "<br>"));
			textBuilder.append("</body></html>");
			processedText = textBuilder.toString();
		}
		return processedText;
	}

	private void addGroup(JPanel container, final FieldGroup group, List<DisplayValue> displayValues) {
		List<DisplayValue> filteredDisplayValues = Utilities.filter(displayValues, new Predicate<DisplayValue>() {
			@Override
			public boolean apply(DisplayValue displayValue) {
				return displayValue.getGroup().equals(group);
			}
		});
		
		if (filteredDisplayValues.size() > 0) {
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			addLabelTextRows(filteredDisplayValues, content);
			
			boolean collapsed = true;
			JPanel section = new SummaryViewSection(group.toString(), group.getIcon(), content, collapsed);
			container.add(section);
		}
	}
	
	private void addLabelTextRows(List<DisplayValue> displayValues, JPanel container) {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3, 17, 3, 0);

		JLabel label;
		JTextField value;
		for (DisplayValue displayValue : displayValues) {
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridwidth = GridBagConstraints.RELATIVE;
			c.fill = GridBagConstraints.NONE;
			c.weightx = 0.15;
			c.weighty = 0.0;
			label = displayValue.getLabel();
			container.add(label, c);
			
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.85;
			c.weighty = 0.0;
			value = displayValue.getValue();
			container.add(value, c);
		}
		fillBottomSpace(container);
	}
	
	private void fillBottomSpace(JPanel container) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 1.0;
		container.add(new JLabel(" "), c);
	}
}
