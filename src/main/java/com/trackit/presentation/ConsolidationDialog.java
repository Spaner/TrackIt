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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.operation.ConsolidationLevel;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;

class ConsolidationDialog extends JDialog {
	private static final long serialVersionUID = -307278924439808786L;
	private DocumentItem item;
	
	private JLabel lblDescription;
	private JLabel lblMethod;
	private JLabel lblConsolidationLevelDescription;
	private JComboBox<ConsolidationLevel> cbLevels;
	private JButton cmdOk;
	private JButton cmdCancel;
	
	ConsolidationDialog(DocumentItem item) {
		super(TrackIt.getApplicationFrame());
		this.item = item;
		
		init();
	}
	
	private void init() {
		setTitle(Messages.getMessage("consolidationDialog.title.dialog"));
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
						.addComponent(lblDescription)
						.addGroup(layout.createSequentialGroup()
								.addComponent(lblMethod)
								.addComponent(cbLevels, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addComponent(lblConsolidationLevelDescription)
						.addGroup(layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(cmdOk)
								.addComponent(cmdCancel)));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addComponent(lblDescription)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(lblMethod)
							.addComponent(cbLevels))
							.addComponent(lblConsolidationLevelDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE)
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
		lblMethod = new JLabel(Messages.getMessage("consolidationDialog.label.method"));
		lblDescription = new JLabel(Messages.getMessage("consolidationDialog.label.description"));
		lblConsolidationLevelDescription = new JLabel();
		
		ComboBoxModel<ConsolidationLevel> model = new DefaultComboBoxModel<ConsolidationLevel>(ConsolidationLevel.values());
		cbLevels = new JComboBox<>(model); 
		cbLevels.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent event) {
				JComboBox<ConsolidationLevel> cbLevel = (JComboBox<ConsolidationLevel>) event.getSource();
				updateLevelDescription((ConsolidationLevel) cbLevel.getSelectedItem());
			}

			private void updateLevelDescription(ConsolidationLevel level) {
				lblConsolidationLevelDescription.setPreferredSize(new Dimension(250, 40));
				lblConsolidationLevelDescription.setText(level.getDescription());
				pack();
			}
		});
		cbLevels.setSelectedItem(ConsolidationLevel.BASIC);
		
		cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final Map<String, Object> options = new HashMap<String, Object>();
				
				ConsolidationLevel level = (ConsolidationLevel) cbLevels.getSelectedItem();
				options.put(Constants.ConsolidationOperation.LEVEL, level);
				consolidate(options);
				
				ConsolidationDialog.this.dispose();
			}

			private void consolidate(final Map<String, Object> options) {
				new Task(new Action() {
					@Override
					public String getMessage() {
						return Messages.getMessage("consolidationDialog.message.executing", item.getDocumentItemName());
					}
					@Override
					public Object execute() throws TrackItException {
						if (item.isActivity()) {
							DocumentManager.getInstance().consolidate((Activity) item,
									(ConsolidationLevel) options.get(Constants.ConsolidationOperation.LEVEL));
						} else if (item.isCourse()) {
							DocumentManager.getInstance().consolidate((Course) item, options);
						}
						return item;
					}
					@Override
					public void done(Object result) {
						((DocumentItem) result).publishSelectionEvent(null);
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("consolidationDialog.message.success", item.getDocumentItemName()));
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