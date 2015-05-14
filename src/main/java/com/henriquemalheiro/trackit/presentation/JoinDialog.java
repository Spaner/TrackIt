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
package com.henriquemalheiro.trackit.presentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.utility.Utilities;

class JoinDialog extends JDialog {
	private static final long serialVersionUID = -2970227573201328334L;
	private static final int COURSES_LIST_ROW_COUNT = 5;
	
	private List<Course> courses;
	
	private JLabel lblJoinOrder;
	private JScrollPane scrollableCoursesList;
	private JList<Course> lstCourses;
	private JButton cmdMoveUp;
	private JButton cmdMoveDown;
	private JButton cmdJoin;
	private JButton cmdCancel;
	private DefaultListModel<Course> coursesModel;
	
	JoinDialog(List<Course> courses) {
		super(TrackIt.getApplicationFrame());
		this.courses = courses;
		
		init();
	}
	
	private void init() {
		setTitle(Messages.getMessage("joinDialog.title"));
		initComponents();
		setLayout();
	}

	private void setLayout() {
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);

		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblJoinOrder)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addComponent(scrollableCoursesList)
										.addGroup(layout.createSequentialGroup()
												.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(cmdJoin)
												.addComponent(cmdCancel)))
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(cmdMoveUp)
										.addComponent(cmdMoveDown))));

		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addComponent(lblJoinOrder)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(scrollableCoursesList)
								.addGroup(layout.createSequentialGroup()
										.addComponent(cmdMoveUp)
										.addComponent(cmdMoveDown)))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(cmdJoin)
								.addComponent(cmdCancel)));
		
		getRootPane().setDefaultButton(cmdJoin);
		
		pack();
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(TrackIt.getApplicationFrame());
	}

	private void initComponents() {
		lblJoinOrder = new JLabel(Messages.getMessage("joinDialog.label.joinOrder"));
		
		initCmdMoveUp();
        initCmdMoveDown();
		initCoursesList();
		
		cmdJoin = new JButton(Messages.getMessage("joinDialog.button.join"));
		cmdJoin.addActionListener(new JoinCmdActionListener());
		
		cmdCancel = new JButton(Messages.getMessage("trackIt.cmdCancel"));
		cmdCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
	}

	private void initCoursesList() {
		initCoursesListModel();
		
		lstCourses = new JList<Course>(coursesModel);
		lstCourses.setPrototypeCellValue(new Course() {
			@Override
			public String toString() {
				return "Course (course longCourseName)";
			}
		});
		lstCourses.setVisibleRowCount(COURSES_LIST_ROW_COUNT);
		lstCourses.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstCourses.addListSelectionListener(new CoursesListSelectionListener());
		lstCourses.setSelectedIndex(0);
		
		scrollableCoursesList = new JScrollPane(lstCourses);
		scrollableCoursesList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollableCoursesList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}

	private void initCoursesListModel() {
		coursesModel = new DefaultListModel<>();
		for (Course course : courses) {
			coursesModel.addElement(course);
		}
	}

	private void initCmdMoveUp() {
		URL imageURL = JoinDialog.class.getResource("/icons/arrow_up.png");
		ImageIcon icon = new ImageIcon(imageURL, Messages.getMessage("joinDialog.tooltip.moveUp"));
		
		cmdMoveUp = new JButton();
        cmdMoveUp.setIcon(icon);
        cmdMoveUp.setToolTipText(Messages.getMessage("joinDialog.tooltip.moveUp"));
        cmdMoveUp.setEnabled(false);
        cmdMoveUp.addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedItemIndex = lstCourses.getSelectedIndex();
				Course selectedCourse = coursesModel.remove(selectedItemIndex);
				selectedItemIndex--;
				coursesModel.add(selectedItemIndex, selectedCourse);
				lstCourses.setSelectedIndex(selectedItemIndex);
			}
		});
	}
	
	private void initCmdMoveDown() {
		URL imageURL = JoinDialog.class.getResource("/icons/arrow_down.png");;
		ImageIcon icon = new ImageIcon(imageURL, Messages.getMessage("joinDialog.tooltip.moveDown"));
		
		cmdMoveDown = new JButton();
        cmdMoveDown.setIcon(icon);
        cmdMoveDown.setToolTipText(Messages.getMessage("joinDialog.tooltip.moveDown"));
        cmdMoveDown.setEnabled(false);
        cmdMoveDown.addActionListener(new ActionListener() {
        	
			@Override
			public void actionPerformed(ActionEvent event) {
				int selectedItemIndex = lstCourses.getSelectedIndex();
				Course selectedCourse = coursesModel.remove(selectedItemIndex);
				selectedItemIndex++;
				coursesModel.add(selectedItemIndex, selectedCourse);
				lstCourses.setSelectedIndex(selectedItemIndex);
			}
		});
	}
	
	private final class JoinCmdActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			List<Course> joiningCourses = Collections.list(coursesModel.elements());
			join(joiningCourses);
		}

		private void join(List<Course> joiningCourses) {
			if (validJoin(joiningCourses)) {
				final Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.JoinOperation.COURSES, joiningCourses);
				DocumentManager.getInstance().join(joiningCourses);
				
				JoinDialog.this.dispose();
			}
		}

		private boolean validJoin(List<Course> joiningCourses) {
			boolean validJoin = true;
			
			if (!warnDistanceExceeded()) {
				return validJoin;
			}
			
			final double warningDistance = getWarningDistance();
			Trackpoint trailingTrackpoint;
			Trackpoint leadingTrackpoint;
			double distance;
			
			for (int i = 0; i < joiningCourses.size() - 1; i++) {
				trailingTrackpoint = joiningCourses.get(i).getLastTrackpoint();
				leadingTrackpoint = joiningCourses.get(i + 1).getFirstTrackpoint();
				distance = calculateDistance(trailingTrackpoint, leadingTrackpoint) * 1000.0;
				
				if (distance > warningDistance) {
					validJoin = false;
					break;
				}
			}
			
			if (!validJoin) {
				int option = JOptionPane.showConfirmDialog(TrackIt.getApplicationFrame(),
						Messages.getMessage("joinDialog.message.warningDistanceExceeded", Formatters.getFormatedDistance(warningDistance)),
						Messages.getMessage("trackIt.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				validJoin = (option == JOptionPane.YES_OPTION); 
			}
			
			return validJoin;
		}

		private boolean warnDistanceExceeded() {
			return TrackIt.getPreferences().getBooleanPreference(Constants.PrefsCategories.JOIN, null,
					Constants.JoinPreferences.WARN_DISTANCE_EXCEEDED, Boolean.TRUE);
		}
		
		private double getWarningDistance() {
			return TrackIt.getPreferences().getDoublePreference(Constants.PrefsCategories.JOIN, null,
					Constants.JoinPreferences.WARNING_DISTANCE, 100.0);
		}

		private Double calculateDistance(Trackpoint trailingTrackpoint, Trackpoint leadingTrackpoint) {
			return Utilities.getGreatCircleDistance(trailingTrackpoint.getLatitude(), trailingTrackpoint.getLongitude(),
					leadingTrackpoint.getLatitude(), leadingTrackpoint.getLongitude());
		}
	}

	private final class CoursesListSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (!event.getValueIsAdjusting()) {
				updateMoveButtons();
			}
		}
		
		private void updateMoveButtons() {
			cmdMoveUp.setEnabled(canMoveUp());
			cmdMoveDown.setEnabled(canMoveDown());
		}
		
		private boolean canMoveUp() {
			return (lstCourses.getSelectedIndex() > 0);
		}
		
		private boolean canMoveDown() {
			return (lstCourses.getSelectedIndex() < (coursesModel.getSize() - 1) && coursesModel
					.getSize() > 1);
		}
	}
}
