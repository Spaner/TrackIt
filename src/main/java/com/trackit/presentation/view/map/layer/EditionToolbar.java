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
package com.trackit.presentation.view.map.layer;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.presentation.view.map.provider.RoutingType;
import com.trackit.presentation.view.map.provider.TransportMode;

class EditionToolbar extends JDialog {
	private static final long serialVersionUID = 8565209370300733530L;
	private EditionLayer editionLayer;
	private boolean active = false;
	EditionToolbar(EditionLayer editionLayer) {
		super(TrackIt.getApplicationFrame());
		this.editionLayer = editionLayer;
		initComponents();
	}
	
	private void initComponents() {
		JLabel lblOptions = new JLabel(Messages.getMessage("editionToolbar.title.options"));
		lblOptions.setFont(lblOptions.getFont().deriveFont(Font.BOLD));
		
		JLabel lblRoutingType = new JLabel(Messages.getMessage("editionToolbar.label.routingType"));
		final JComboBox<RoutingType> routingTypeChooser = new JComboBox<>(RoutingType.values());
		routingTypeChooser.putClientProperty("JComponent.sizeVariant", "mini");
		routingTypeChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final RoutingType routingType = (RoutingType) routingTypeChooser.getSelectedItem();
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.EDITION, null,
						Constants.EditionPreferences.ROUTING_TYPE, routingType.getRoutingTypeName());
				
			}
		});
		
		JLabel lblTransportMode = new JLabel(Messages.getMessage("editionToolbar.label.transportMode"));
		final JComboBox<TransportMode> transportModeChooser = new JComboBox<>(TransportMode.values());
		transportModeChooser.putClientProperty("JComponent.sizeVariant", "mini");
		transportModeChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final TransportMode transportMode = (TransportMode) transportModeChooser.getSelectedItem();
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.EDITION, null,
						Constants.EditionPreferences.TRANSPORT_MODE, transportMode.getTransportModeName());
				
			}
		});
		
		//boolean followRoads = TrackIt.getPreferences().getBooleanPreference(
			//	Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.FOLLOW_ROADS, true);
		boolean followRoads = false;
		if(DocumentManager.getInstance().getSelectedCourse()!= null){
			followRoads = DocumentManager.getInstance().getSelectedCourse().getSubSport().getFollowRoads();
		}
		JCheckBox chkFollowRoads = new JCheckBox(Messages.getMessage("editionToolbar.label.followRoads"), followRoads);
		chkFollowRoads.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean selected = ((JCheckBox) e.getSource()).isSelected();
				DocumentManager.getInstance().getSelectedCourse().getSubSport().setFollowRoads(selected);
			}
		});
		//chkFollowRoads.setEnabled(false);
		
		boolean avoidHighways = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.AVOID_HIGHWAYS, true);
		JCheckBox chkAvoidHighways = new JCheckBox(Messages.getMessage("editionToolbar.label.avoidHighways"), avoidHighways);
		chkAvoidHighways.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean selected = ((JCheckBox) e.getSource()).isSelected();
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.EDITION, null,
						Constants.EditionPreferences.AVOID_HIGHWAYS, selected);
			}
		});
		
		boolean avoidTollRoads = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.AVOID_TOLL_ROADS, true);
		JCheckBox chkAvoidTollRoads = new JCheckBox(Messages.getMessage("editionToolbar.label.avoidTollRoads"), avoidTollRoads);
		chkAvoidTollRoads.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean selected = ((JCheckBox) e.getSource()).isSelected();
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.EDITION, null,
						Constants.EditionPreferences.AVOID_TOLL_ROADS, selected);
			}
		});
		
		boolean addCoursePointsAtJunctions = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.ADD_COURSE_POINTS_AT_JUNCTIONS, true);
		JCheckBox chkAddCoursePointsAtJunctions = new JCheckBox(
				Messages.getMessage("editionToolbar.label.addCoursePointsAtJunctions"), addCoursePointsAtJunctions);
		chkAddCoursePointsAtJunctions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean selected = ((JCheckBox) e.getSource()).isSelected();
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.EDITION, null,
						Constants.EditionPreferences.ADD_COURSE_POINTS_AT_JUNCTIONS, selected);
			}
		});
		
		//58406##################################################################################################################
		boolean keepOriginalTimesAtRemoval = TrackIt.getPreferences().getBooleanPreference(
				Constants.PrefsCategories.EDITION, null, Constants.EditionPreferences.KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL, true);
		JCheckBox chkKeepOriginalTimesAtRemoval = new JCheckBox(
				Messages.getMessage("editionToolbar.label.keepOriginalTimesAtRemoval"), keepOriginalTimesAtRemoval);
		chkKeepOriginalTimesAtRemoval.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean selected = ((JCheckBox) e.getSource()).isSelected();
				TrackIt.getPreferences().setPreference(Constants.PrefsCategories.EDITION, null,
						Constants.EditionPreferences.KEEP_ORIGINAL_TIMES_AT_POINT_REMOVAL, selected);
			}
		});
		//#######################################################################################################################
		
		
		JLabel lblActions = new JLabel(Messages.getMessage("editionToolbar.title.actions"));
		lblActions.setFont(lblActions.getFont().deriveFont(Font.BOLD));
		
		//JButton cmdUndoLastAction = new JButton(Messages.getMessage("editionToolbar.label.undoLastAction"));
		//cmdUndoLastAction.setEnabled(false);
		JButton cmdRestartCourse = new JButton(Messages.getMessage("editionToolbar.label.restartCourse"));
		cmdRestartCourse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				editionLayer.restartCourse();
			}
		});

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblOptions)
						.addGroup(layout.createSequentialGroup()
								.addComponent(lblRoutingType)
								.addComponent(routingTypeChooser))
						.addGroup(layout.createSequentialGroup()
								.addComponent(lblTransportMode)
								.addComponent(transportModeChooser))
						.addComponent(chkFollowRoads)
						.addComponent(chkAvoidHighways)
						.addComponent(chkAvoidTollRoads)
						.addComponent(chkAddCoursePointsAtJunctions)
						.addComponent(chkKeepOriginalTimesAtRemoval)//58406
						.addComponent(lblActions)
						//.addComponent(cmdUndoLastAction)58406
						.addComponent(cmdRestartCourse));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addComponent(lblOptions)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(lblRoutingType)
							.addComponent(routingTypeChooser))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(lblTransportMode)
							.addComponent(transportModeChooser))
					.addComponent(chkFollowRoads)
					.addComponent(chkAvoidHighways)
					.addComponent(chkAvoidTollRoads)
					.addComponent(chkAddCoursePointsAtJunctions)
					.addComponent(chkKeepOriginalTimesAtRemoval)//58406
					.addComponent(lblActions)
					//.addComponent(cmdUndoLastAction)
					.addComponent(cmdRestartCourse));
		
		pack();
		setModal(false);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocation(getParent().getWidth() - getWidth() - 10, getParent().getY() + 100);
	}
}