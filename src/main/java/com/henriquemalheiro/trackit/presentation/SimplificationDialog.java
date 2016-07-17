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
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;

class SimplificationDialog extends JDialog {
	private static final long serialVersionUID = -2970227573201328334L;
	private static final int TRACKPOINTS_LOWER_LIMIT = 2;
	
	private DocumentItem item;
	
	private JLabel lblNumberOfTrackpoints;
	private JLabel lblTrackpoints;
	private JSlider trackpointsSlider;
	private JTextField txtCurrentValue;
	private JCheckBox chkRemoveTrackpoints;
	private JButton cmdOk;
	private JButton cmdCancel;
	private int maximumValue;
	private int currentValue;
	
	SimplificationDialog(DocumentItem item) {
		super(TrackIt.getApplicationFrame());
		this.item = item;
		
		init();
	}
	
	private void init() {
		setTitle(Messages.getMessage("simplificationDialog.title"));
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
						.addGroup(layout.createSequentialGroup()
								.addComponent(lblNumberOfTrackpoints)
								.addComponent(lblTrackpoints))
						.addGroup(layout.createSequentialGroup()
								.addComponent(trackpointsSlider)
								.addComponent(txtCurrentValue))
						.addComponent(chkRemoveTrackpoints)
						.addGroup(layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(cmdOk)
								.addComponent(cmdCancel)));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblNumberOfTrackpoints)
								.addComponent(lblTrackpoints))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(trackpointsSlider)
								.addComponent(txtCurrentValue)
								.addComponent(lblTrackpoints))
						.addComponent(chkRemoveTrackpoints)
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
		maximumValue = item.getTrackpoints().size();
		currentValue = getCurrentValue();
		
		lblNumberOfTrackpoints = new JLabel(Messages.getMessage("simplificationDialog.label.numberOfTrackpoints"));
		lblTrackpoints = new JLabel(String.valueOf(currentValue));
		txtCurrentValue = new JTextField(String.valueOf(currentValue), 4);
		txtCurrentValue.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentValue = item.getTrackpoints().size();
				try {
					currentValue = Integer.parseInt(txtCurrentValue.getText());
				} catch (NumberFormatException e1) {
					// Do nothing: consider maximum value
				}
				trackpointsSlider.setValue(currentValue);
			}
		});
		
		trackpointsSlider = new JSlider(TRACKPOINTS_LOWER_LIMIT, maximumValue, currentValue);
		trackpointsSlider.setExtent(10);
		trackpointsSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				currentValue = (int) source.getValue();
				txtCurrentValue.setText(String.valueOf(currentValue));
			}
		});
		
		chkRemoveTrackpoints = new JCheckBox(Messages.getMessage("simplificationDialog.label.removeTrackpoints", true));
		
		cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final Map<String, Object> options = new HashMap<String, Object>();
				options.put(Constants.TrackSimplificationOperation.NUMBER_OF_POINTS, currentValue);
				options.put(Constants.TrackSimplificationOperation.REMOVE_TRACKPOINTS, chkRemoveTrackpoints.isSelected());
				
				simplify(options);
				
				SimplificationDialog.this.dispose();
			}

			private void simplify(final Map<String, Object> options) {
				new Task(new Action() {
					@Override
					public String getMessage() {
						return Messages.getMessage("simplificationDialog.message.executing", item.getDocumentItemName());
					}
					@Override
					public Object execute() throws TrackItException {
						return DocumentManager.getInstance().simplify(item, options);
					}
					@Override
					public void done(Object result) {
						((DocumentItem) result).publishSelectionEvent(null);
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("simplificationDialog.message.success", item.getDocumentItemName()));
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

	private int getCurrentValue() {
		int count = 0;
		for (Trackpoint trackpoint : item.getTrackpoints()) {
			if (trackpoint.isViewable()) {
				count++;
			}
		}
		return count;
	}
}