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
package com.henriquemalheiro.trackit.presentation.view.map.layer;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.Pair;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.CoursePointType;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.Operation;
import com.henriquemalheiro.trackit.presentation.utilities.swing.TrackItTextField;
import com.henriquemalheiro.trackit.presentation.view.map.Map;
import com.henriquemalheiro.trackit.presentation.view.map.MapUtilities;

public class CoursePointsLayer extends MapLayer implements EventPublisher {
	private static final long serialVersionUID = 868019341841095506L;

	public CoursePointsLayer(Map map) {
		super(map);
	}

	@Override
	public MapLayerType getType() {
		return MapLayerType.COURSE_POINTS_LAYER;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;

		super.paintComponent(graphics);

		Set<DocumentItem> itemsToPaint = new LinkedHashSet<DocumentItem>();
		List<Course> courses = getCourses();
		for (Course course : courses) {
			List<CoursePoint> coursePoints = course.getCoursePoints();
			for (CoursePoint coursePoint : coursePoints) {
				itemsToPaint.add(coursePoint);
			}
		}

		for (DocumentItem item : itemsToPaint) {
			if (getItems().contains(item)) {
				continue;
			}

			java.util.Map<String, Object> attributes = MapUtilities.getPaintingAttributes(item);
			item.paint(graphics, this, attributes);
		}
	}

	private List<Course> getCourses() {
		List<Course> courses = new ArrayList<Course>();
		for (DocumentItem item : getItems()) {
			while (item != null && !item.isCourse()) {
				item = item.getParent();
			}

			if (item != null && item.isCourse()) {
				courses.add((Course) item);
			}
		}

		return courses;
	}

	@Override
	public List<DocumentItem> getItems(Location location) {
		List<DocumentItem> items = new ArrayList<DocumentItem>();

		List<CoursePoint> coursePoints = new ArrayList<CoursePoint>();
		for (DocumentItem item : map.getItems()) {
			while (item != null && !item.isCourse()) {
				item = item.getParent();
			}

			if (item != null && item.isCourse()) {
				coursePoints.addAll(item.getCoursePoints());
			}
		}

		Point2D.Double locationPoint = getPoint(location);

		for (CoursePoint coursePoint : coursePoints) {
			Point2D.Double coursePointXY = getPoint(coursePoint.getLongitude(), coursePoint.getLatitude());

			Rectangle2D.Double boundingBox = new Rectangle2D.Double(coursePointXY.x - 16, coursePointXY.y - 32, 32, 32);
			if (boundingBox.contains(locationPoint)) {
				items.add(coursePoint);
			}
		}

		return items;
	}

	private Point2D.Double getPoint(Location location) {
		final int centerX = getWidth() / 2;
		final int centerY = getHeight() / 2;

		Pair<Integer, Integer> locationOffset = getMapProvider().getCenterOffsetInPixels(location);
		Point2D.Double locationPoint = new Point2D.Double(centerX + locationOffset.getFirst(), centerY
		        + locationOffset.getSecond());

		return locationPoint;
	}

	private Point2D.Double getPoint(double longitude, double latitude) {
		final int centerX = getWidth() / 2;
		final int centerY = getHeight() / 2;

		int[] coursePointOffset = getMapProvider().getCenterOffsetInPixels(longitude, latitude);
		Point2D.Double point = new Point2D.Double(centerX + coursePointOffset[0], centerY + coursePointOffset[1]);

		return point;
	}

	@Override
	public List<Operation> getSupportedOperations(Location location) {
		List<Operation> supportedOperations = new ArrayList<Operation>();
		List<Course> courses = getCourses();

		for (final Course course : courses) {
			final Trackpoint trackpoint = getTrackpoint(course, location);
			if (trackpoint != null) {
				Operation createCoursePointOperation = new Operation(
				        Messages.getMessage("operation.group.createCoursePoint"), String.format("%s on course %s (%s)",
				                Messages.getMessage("operation.name.createCoursePoint"), course.getName(),
				                Formatters.getFormatedDistance(trackpoint.getDistance())),
				        Messages.getMessage("operation.description.createCoursePoint"), new Runnable() {
					        @Override
					        public void run() {
						        JDialog dialog = new CreateCoursePointDialog(course, trackpoint);
						        dialog.setVisible(true);
					        }
				        });
				supportedOperations.add(createCoursePointOperation);
			}
		}

		List<DocumentItem> items = getItems(location);
		if (!items.isEmpty()) {
			for (DocumentItem item : items) {
				final CoursePoint coursePoint = (CoursePoint) item;
				Operation deleteCoursePointOperation = new Operation(
				        Messages.getMessage("operation.group.deleteCoursePoint"), String.format("%s %s",
				                Messages.getMessage("operation.name.delete"), coursePoint.getDocumentItemName()),
				        Messages.getMessage("operation.description.deleteCoursePoint"), new Runnable() {
					        @Override
					        public void run() {
						        Course course = coursePoint.getParent();
						        course.getCoursePoints().remove(coursePoint);
						        course.publishUpdateEvent(null);
						        JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
						                String.format("Course point %s removed", coursePoint.toString()));
					        }
				        });
				supportedOperations.add(deleteCoursePointOperation);
			}
		}

		return supportedOperations;
	}

	private Trackpoint getTrackpoint(Course course, Location location) {
		double distance = Double.MAX_VALUE;
		Trackpoint candidateTrackpoint = null;

		for (Trackpoint trackpoint : course.getTrackpoints()) {
			double newDistance = Utilities.getGreatCircleDistance(trackpoint.getLatitude(), trackpoint.getLongitude(),
			        location.getLatitude(), location.getLongitude()) * 1000.0;

			if (newDistance < distance) {
				distance = newDistance;
				candidateTrackpoint = trackpoint;
			}
		}

		final double referenceDistance = 100.0;
		if (distance <= referenceDistance) {
			return candidateTrackpoint;
		}

		return null;
	}

	private class CreateCoursePointDialog extends JDialog {
		private static final long serialVersionUID = 3369959324609798517L;

		private Course course;
		private Trackpoint trackpoint;

		CreateCoursePointDialog(Course course, Trackpoint trackpoint) {
			super(TrackIt.getApplicationFrame());

			this.course = course;
			this.trackpoint = trackpoint;

			initComponents();
		}

		private void initComponents() {
			GroupLayout layout = new GroupLayout(getContentPane());
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			setLayout(layout);
			setTitle(Messages.getMessage("dialog.createCoursePoint.title"));

			JLabel lblDescription = new JLabel(Messages.getMessage("dialog.coursePoint.create"));
			JLabel lblName = new JLabel(Messages.getMessage("dialog.coursePointName"));
			final JTextField txtName = new TrackItTextField(10);
			txtName.setColumns(15);

			JLabel lblTypes = new JLabel(Messages.getMessage("dialog.coursePointType"));
			final JComboBox<CoursePointType> cbTypes = new JComboBox<CoursePointType>(CoursePointType.values());
			cbTypes.setRenderer(new ComboBoxCoursePointTypeRenderer());

			JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
			cmdOk.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					CoursePoint coursePoint = new CoursePoint(txtName.getText(), course);
					coursePoint.setTime(trackpoint.getTimestamp());
					coursePoint.setDistance(trackpoint.getDistance());
					coursePoint.setLatitude(trackpoint.getLatitude());
					coursePoint.setLongitude(trackpoint.getLongitude());
					coursePoint.setAltitude(trackpoint.getAltitude());
					coursePoint.setType((CoursePointType) cbTypes.getSelectedItem());
					course.add(coursePoint);

					Collections.sort(course.getCoursePoints(), new Comparator<CoursePoint>() {
						@Override
						public int compare(CoursePoint cp1, CoursePoint cp2) {
							return cp1.getDistance().compareTo(cp2.getDistance());
						}
					});

					EventManager.getInstance().publish(CoursePointsLayer.this, Event.COURSE_UPDATED, course);

					CreateCoursePointDialog.this.dispose();
				}
			});
			JButton cmdCancel = new JButton(Messages.getMessage("trackIt.cmdCancel"));
			cmdCancel.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					dispose();
				}
			});

			layout.setHorizontalGroup(layout
			        .createParallelGroup(GroupLayout.Alignment.LEADING)
			        .addComponent(lblDescription)
			        .addGroup(
			                layout.createSequentialGroup()
			                        .addGroup(
			                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			                                        .addComponent(lblName).addComponent(lblTypes))
			                        .addGroup(
			                                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
			                                        .addComponent(txtName)
			                                        .addComponent(cbTypes)
			                                        .addGroup(
			                                                layout.createSequentialGroup().addComponent(cmdOk)
			                                                        .addComponent(cmdCancel)))));

			layout.setVerticalGroup(layout
			        .createSequentialGroup()
			        .addComponent(lblDescription)
			        .addGroup(
			                layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblName)
			                        .addComponent(txtName))
			        .addGroup(
			                layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblTypes)
			                        .addComponent(cbTypes))
			        .addGroup(
			                layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(cmdOk)
			                        .addComponent(cmdCancel)));

			pack();
			setModal(true);
			setLocationRelativeTo(TrackIt.getApplicationFrame());
		}
	}

	private class ComboBoxCoursePointTypeRenderer extends JLabel implements ListCellRenderer<CoursePointType> {
		private static final long serialVersionUID = 6712054538628844559L;

		ComboBoxCoursePointTypeRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends CoursePointType> list, CoursePointType value,
		        int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			CoursePointType type = (CoursePointType) value;
			setText(type.toString());
			setIcon(type.getIcon());

			return this;
		}
	}
}
