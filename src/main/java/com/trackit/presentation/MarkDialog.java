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
package com.trackit.presentation;

import java.awt.CheckboxGroup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;

class MarkDialog extends JDialog {
	private static final long serialVersionUID = -541992580364613841L;

	private DocumentItem item;
	
	private JLabel lblDescription;
	private JPanel climbsPanel;
	private JPanel descentsPanel;
	private JCheckBox chkMarkClimbStart;
	private JCheckBox chkMarkClimbFinish;
	private JCheckBox chkMarkClimbMaxGrade;
	private JCheckBox chkMarkClimbMinGrade;
	private JCheckBox chkMarkDescentStart;
	private JCheckBox chkMarkDescentFinish;
	private JCheckBox chkMarkDescentMaxGrade;
	private JCheckBox chkMarkDescentMinGrade;
	private JCheckBox chkRemoveExistingMarks;
	private JButton cmdOk;
	private JButton cmdCancel;
	
	MarkDialog(DocumentItem item) {
		super(TrackIt.getApplicationFrame());
		this.item = item;
		
		init();
	}
	
	private void init() {
		setTitle(Messages.getMessage("markDialog.title"));
		initComponents();
		setLayout();
	}

	private void setLayout() {
		GroupLayout climbsPanelLayout = new GroupLayout(climbsPanel);
		climbsPanelLayout.setAutoCreateGaps(true);
		climbsPanelLayout.setAutoCreateContainerGaps(true);
		climbsPanel.setLayout(climbsPanelLayout);
		
		climbsPanelLayout.setHorizontalGroup(
				climbsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(chkMarkClimbStart)
						.addComponent(chkMarkClimbFinish)
						.addComponent(chkMarkClimbMaxGrade)
						.addComponent(chkMarkClimbMinGrade));
		
		climbsPanelLayout.setVerticalGroup(
				climbsPanelLayout.createSequentialGroup()
						.addComponent(chkMarkClimbStart)
						.addComponent(chkMarkClimbFinish)
						.addComponent(chkMarkClimbMaxGrade)
						.addComponent(chkMarkClimbMinGrade));
		
		GroupLayout descentsPanelLayout = new GroupLayout(descentsPanel);
		descentsPanelLayout.setAutoCreateGaps(true);
		descentsPanelLayout.setAutoCreateContainerGaps(true);
		descentsPanel.setLayout(descentsPanelLayout);
		
		descentsPanelLayout.setHorizontalGroup(
				descentsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(chkMarkDescentStart)
						.addComponent(chkMarkDescentFinish)
						.addComponent(chkMarkDescentMaxGrade)
						.addComponent(chkMarkDescentMinGrade));
		
		descentsPanelLayout.setVerticalGroup(
				descentsPanelLayout.createSequentialGroup()
						.addComponent(chkMarkDescentStart)
						.addComponent(chkMarkDescentFinish)
						.addComponent(chkMarkDescentMaxGrade)
						.addComponent(chkMarkDescentMinGrade));
		
		
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);

		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblDescription)
						.addComponent(climbsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(descentsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(chkRemoveExistingMarks)
						.addGroup(layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(cmdOk)
								.addComponent(cmdCancel)));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addComponent(lblDescription)
						.addComponent(climbsPanel)
						.addComponent(descentsPanel)
						.addComponent(chkRemoveExistingMarks)
						.addGroup(layout.createParallelGroup()
								.addComponent(cmdOk)
								.addComponent(cmdCancel)));
		
		getRootPane().setDefaultButton(cmdOk);
		
		pack();
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(TrackIt.getApplicationFrame());
	}

	private void initComponents() {
		lblDescription = new JLabel(Messages.getMessage("markDialog.label.description"));
		
		climbsPanel = new JPanel();
		climbsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getMessage("markDialog.title.climbs")));
		chkMarkClimbStart = new JCheckBox(Messages.getMessage("markDialog.label.markClimbStart"), true);
		chkMarkClimbFinish = new JCheckBox(Messages.getMessage("markDialog.label.markClimbFinish"), true);
		chkMarkClimbMaxGrade = new JCheckBox(Messages.getMessage("markDialog.label.markClimbMaxGrade"), false);
		chkMarkClimbMinGrade = new JCheckBox(Messages.getMessage("markDialog.label.markClimbMinGrade"), false);
		
		descentsPanel = new JPanel();
		descentsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getMessage("markDialog.title.descents")));
		chkMarkDescentStart = new JCheckBox(Messages.getMessage("markDialog.label.markDescentStart"), true);
		chkMarkDescentFinish = new JCheckBox(Messages.getMessage("markDialog.label.markDescentFinish"), true);
		chkMarkDescentMaxGrade = new JCheckBox(Messages.getMessage("markDialog.label.markDescentMaxGrade"), false);
		chkMarkDescentMinGrade = new JCheckBox(Messages.getMessage("markDialog.label.markDescentMinGrade"), false);
		
		CheckboxGroup optionsGroup = new CheckboxGroup();
		chkRemoveExistingMarks = new JCheckBox(Messages.getMessage("markDialog.label.removeExistingMarks", optionsGroup, true));
		
		cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.MarkingOperation.MARK_CLIMB_START, chkMarkClimbStart.isSelected());
				options.put(Constants.MarkingOperation.MARK_CLIMB_FINISH, chkMarkClimbFinish.isSelected());
				options.put(Constants.MarkingOperation.MARK_CLIMB_MAX_GRADE, chkMarkClimbMaxGrade.isSelected());
				options.put(Constants.MarkingOperation.MARK_CLIMB_MIN_GRADE, chkMarkClimbMinGrade.isSelected());
				options.put(Constants.MarkingOperation.MARK_DESCENT_START, chkMarkDescentStart.isSelected());
				options.put(Constants.MarkingOperation.MARK_DESCENT_FINISH, chkMarkDescentFinish.isSelected());
				options.put(Constants.MarkingOperation.MARK_DESCENT_MAX_GRADE, chkMarkDescentMaxGrade.isSelected());
				options.put(Constants.MarkingOperation.MARK_DESCENT_MIN_GRADE, chkMarkDescentMinGrade.isSelected());
				options.put(Constants.MarkingOperation.REMOVE_EXISTING_MARKS, chkRemoveExistingMarks.isSelected());
				
				mark(options);
				
				MarkDialog.this.dispose();
			}

			private void mark(final Map<String, Object> options) {
				new Task(new Action() {
					@Override
					public String getMessage() {
						return Messages.getMessage("markDialog.message.executing", item.getDocumentItemName());
					}
					@Override
					public Object execute() throws TrackItException {
						return DocumentManager.getInstance().mark(item, options);
					}
					@Override
					public void done(Object result) {
						((DocumentItem) result).publishUpdateEvent(null);
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("markDialog.message.success", item.getDocumentItemName()));
					}
				}).execute();
			}
		});
		
		cmdCancel = new JButton(Messages.getMessage("trackIt.cmdCancel"));
		cmdCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
	}
}