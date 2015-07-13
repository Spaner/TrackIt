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

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.SetPaceMethod;
import com.henriquemalheiro.trackit.business.common.Unit;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;

class SetPaceDialog extends JDialog {
	private static final long serialVersionUID = -2970227573201328334L;
	
	private enum PanelType { NO_PANEL, TIME_PANEL, SPEED_PANEL, PERCENTAGE_PANEL };
	
	private JPanel timePanel;
	private JPanel speedPanel;
	private JPanel percentagePanel;
	private JPanel noPanel;
	private JPanel dataPanel;
	private CardLayout cardLayout;
	
	private double targetSpeed;
	private int targetPercentage;
	private double targetTime;
	private boolean includePauses;

	private Course course;
	
	SetPaceDialog(Course course) {
		super(TrackIt.getApplicationFrame());
		
		this.course = course;
		
		initComponents();
	}
	
	private void initComponents() {
		targetSpeed = -1.0;
		targetPercentage = -1;
		targetTime = -1.0;
		includePauses = true;

		speedPanel = getSpeedPanel(course.getAverageSpeed() * 3.6);
		percentagePanel = getPercentagePanel();
		timePanel = getTimePanel(course.getElapsedTime(), course.getTimerTime());
		noPanel = new JPanel();
		
		dataPanel = new JPanel();
		cardLayout = new CardLayout();
		dataPanel.setLayout(cardLayout);
		dataPanel.add(noPanel, PanelType.NO_PANEL.name());
		dataPanel.add(speedPanel, PanelType.SPEED_PANEL.name());
		dataPanel.add(percentagePanel, PanelType.PERCENTAGE_PANEL.name());
		dataPanel.add(timePanel, PanelType.TIME_PANEL.name());
		
		cardLayout.first(dataPanel);
		cardLayout.show(dataPanel, PanelType.NO_PANEL.name());
		
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		setTitle(Messages.getMessage("dialog.setPace.title"));
		
		JLabel lblDescription = new JLabel(Messages.getMessage("dialog.setPace.description"));
		JLabel lblMethod = new JLabel(Messages.getMessage("dialog.setPace.method.label"));
		ComboBoxModel<SetPaceMethod> model = new DefaultComboBoxModel<SetPaceMethod>(SetPaceMethod.values());
		final JComboBox<SetPaceMethod> cbMethod = new JComboBox<SetPaceMethod>(model);
		cbMethod.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent event) {
				JComboBox<SetPaceMethod> cbMethod = (JComboBox<SetPaceMethod>) event.getSource();
		        SetPaceMethod selectedMethod = (SetPaceMethod) cbMethod.getSelectedItem();
		        switch (selectedMethod) {
		        case TARGET_SPEED:
		        case CONSTANT_TARGET_SPEED:
		        	cardLayout.show(dataPanel, PanelType.SPEED_PANEL.name());
		        	break;
		        case SPEED_PERCENTAGE:
		        case TIME_PERCENTAGE:
		        	cardLayout.show(dataPanel, PanelType.PERCENTAGE_PANEL.name());
		        	break;
		        case TARGET_TIME:
		        	cardLayout.show(dataPanel, PanelType.TIME_PANEL.name());
		        	break;
		        case SMART_PACE:
		        	cardLayout.show(dataPanel, PanelType.NO_PANEL.name());
		        	break;
		        default:
		        	cardLayout.show(dataPanel, PanelType.NO_PANEL.name());
		        }
			}
		});
		cbMethod.setSelectedItem(SetPaceMethod.TARGET_SPEED);
		
		JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				final Map<String, Object> options = new HashMap<String, Object>();
				
				SetPaceMethod method = (SetPaceMethod) cbMethod.getSelectedItem();
				switch (method) {
				case CONSTANT_TARGET_SPEED:
				case TARGET_SPEED:
					if (targetSpeed == -1.0) {
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), Messages.getMessage("dialog.mandatoryField", "Target Speed"),
								Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					options.put(Constants.SetPaceOperation.METHOD, method);
					options.put(Constants.SetPaceOperation.SPEED, targetSpeed);
					options.put(Constants.SetPaceOperation.INCLUDE_PAUSES, includePauses);
					
					break;
				case SPEED_PERCENTAGE:
				case TIME_PERCENTAGE:
					if (targetPercentage == -1.0) {
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), Messages.getMessage("dialog.mandatoryField", "Target Percentage"),
								Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					options.put(Constants.SetPaceOperation.METHOD, method);
					options.put(Constants.SetPaceOperation.PERCENTAGE, targetPercentage / 100.0);
					options.put(Constants.SetPaceOperation.INCLUDE_PAUSES, includePauses);
					
					break;
				case TARGET_TIME:
					if (targetTime == -1.0) {
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), Messages.getMessage("dialog.mandatoryField", "Target Time"),
								Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					options.put(Constants.SetPaceOperation.METHOD, method);
					options.put(Constants.SetPaceOperation.TIME, targetTime);
					options.put(Constants.SetPaceOperation.INCLUDE_PAUSES, includePauses);
					
					break;
				case SMART_PACE:
					options.put(Constants.SetPaceOperation.METHOD, method);
					break;
				default:
					// Do nothing;
				}
				
				setPace(options);
				
				SetPaceDialog.this.dispose();
			}

			private void setPace(final Map<String, Object> options) {
				new Task(new Action() {
					@Override
					public String getMessage() {
						return Messages.getMessage("setPaceDialog.message.executing", course.getDocumentItemName());
					}
					@Override
					public Object execute() throws TrackItException {
						boolean addToUndoManager = true;
						return DocumentManager.getInstance().setPace(course, options, addToUndoManager);
					}
					@Override
					public void done(Object result) {
						((DocumentItem) result).publishSelectionEvent(null);
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), Messages.getMessage("setPaceDialog.message.success"));
					}
				}).execute();
			}
		});
		
		JButton cmdCancel = new JButton(Messages.getMessage("trackIt.cmdCancel"));
		cmdCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(lblDescription)
						.addGroup(layout.createSequentialGroup()
								.addComponent(lblMethod)
								.addComponent(cbMethod, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE))
						.addComponent(dataPanel)
						.addGroup(layout.createSequentialGroup()
								.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(cmdOk)
								.addComponent(cmdCancel)));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addComponent(lblDescription)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(lblMethod)
							.addComponent(cbMethod))
					.addComponent(dataPanel)
					.addGroup(layout.createParallelGroup()
							.addComponent(cmdOk)
							.addComponent(cmdCancel)));
		
		getRootPane().setDefaultButton(cmdOk);
		
		pack();
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(TrackIt.getApplicationFrame());
	}
	
	private JPanel getSpeedPanel(final double speed) {
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		final JLabel speedLabel = new JLabel(getMessage("applicationPanel.label.targetSpeed"));
		
		targetSpeed = speed;
		final SpinnerNumberModel model = new SpinnerNumberModel(targetSpeed, 1.0, 200.0, 0.1);
		final JSpinner speedSpinner = new JSpinner();
		speedSpinner.setModel(model);
		format(speedSpinner, 4, "#0.0");
		speedSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				targetSpeed = -1.0;
				
				if (speedSpinner.getValue() == null) {
					return;
				}
				
				try {
					targetSpeed = Formatters.getDecimalFormat().parse(speedSpinner.getValue().toString()).doubleValue();
				} catch (ParseException e1) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), getMessage("applicationPanel.error.targetSpeed"),
							getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JLabel unitLabel = new JLabel(Unit.KILOMETER_PER_HOUR.toString());
		
		JCheckBox includePausesChk = new JCheckBox(Messages.getMessage("applicationPanel.label.includePauses"), includePauses);
		includePausesChk.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				includePauses = ((JCheckBox) e.getSource()).isSelected();
			}
		});
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(speedLabel)
								.addComponent(speedSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
										.addComponent(unitLabel))
						.addComponent(includePausesChk));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(speedLabel)
								.addComponent(speedSpinner)
								.addComponent(unitLabel))
						.addComponent(includePausesChk));
		
		return panel;
	}
	
	private JPanel getPercentagePanel() {
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		final JLabel percentageLabel = new JLabel(getMessage("applicationPanel.label.targetPercentage"));
		
		targetPercentage = 100;
		final SpinnerNumberModel model = new SpinnerNumberModel(targetPercentage, 1, 500, 5);
		final JSpinner percentageSpinner = new JSpinner();
		percentageSpinner.setModel(model);
		format(percentageSpinner, 3, "#0");
		percentageSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				targetPercentage = -1;
				
				if (percentageSpinner.getValue() == null) {
					return;
				}
				
				try {
					NumberFormat formatter = NumberFormat.getInstance();
					targetPercentage = formatter.parse(percentageSpinner.getValue().toString()).intValue();
				} catch (ParseException e1) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(), getMessage("applicationPanel.error.targetPercentage"),
							getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JLabel unitLabel = new JLabel(Unit.PERCENTAGE.toString());
		
		JCheckBox includePausesChk = new JCheckBox(Messages.getMessage("applicationPanel.label.includePauses"), includePauses);
		includePausesChk.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				includePauses = ((JCheckBox) e.getSource()).isSelected();
			}
		});
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(percentageLabel)
								.addComponent(percentageSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								          GroupLayout.PREFERRED_SIZE)
								.addComponent(unitLabel))
						.addComponent(includePausesChk));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(percentageLabel)
								.addComponent(percentageSpinner)
								.addComponent(unitLabel))
						.addComponent(includePausesChk));
		
		return panel;
	}
	
	private JPanel getTimePanel(final double elapsedTime, final double timerTime) {
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		final JLabel timeLabel = new JLabel(getMessage("applicationPanel.label.targetTime"));
		
		targetTime = elapsedTime;
		final SpinnerModel hourModel = new DurationSpinnerModel((int) elapsedTime / 3600, 0, 999, 1);
		final SpinnerModel minuteModel = new DurationSpinnerModel(((int) elapsedTime % 3600) / 60, 0, 59, 1);
		((DurationSpinnerModel) minuteModel).setLinkedMaxModel(hourModel);
		((DurationSpinnerModel) minuteModel).setLinkedMinModel(hourModel);
		final SpinnerModel secondModel = new DurationSpinnerModel((int) elapsedTime % 60, 0, 59, 1);
		((DurationSpinnerModel) secondModel).setLinkedMaxModel(minuteModel);
		((DurationSpinnerModel) secondModel).setLinkedMinModel(minuteModel);
		
		final JSpinner hourSpinner = new JSpinner(hourModel);
		format(hourSpinner, 3, "00");
		final JSpinner minuteSpinner = new JSpinner(minuteModel);
		format(minuteSpinner, 2, "00");
		final JSpinner secondSpinner = new JSpinner(secondModel);
		format(secondSpinner, 2, "00");

		ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateTargetTime(hourModel.getValue(), minuteSpinner.getValue(), secondSpinner.getValue());
			}

			private void updateTargetTime(Object hour, Object minute, Object second) {
				if (hour == null || minute == null || second == null) {
					targetTime = -1.0;
				} else {
					targetTime = Integer.parseInt(hour.toString()) * 3600
							+ Integer.parseInt(minute.toString()) * 60
							+ Integer.parseInt(second.toString()); 
				}
			}
		};

		hourSpinner.addChangeListener(changeListener);
		minuteSpinner.addChangeListener(changeListener);
		secondSpinner.addChangeListener(changeListener);

		JLabel hourLabel = new JLabel(Messages.getMessage("applicationPanel.label.hour"));
		JLabel minuteLabel = new JLabel(Messages.getMessage("applicationPanel.label.minute"));
		JLabel secondLabel = new JLabel(Messages.getMessage("applicationPanel.label.second"));
		
		JCheckBox includePausesChk = new JCheckBox(Messages.getMessage("applicationPanel.label.includePauses"), includePauses);
		includePausesChk.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				includePauses = ((JCheckBox) e.getSource()).isSelected();
				targetTime = (includePauses ? elapsedTime : timerTime);
				updateDuration();
			}

			private void updateDuration() {
				int hours = (int) (targetTime / 3600);
				int minutes = (int) (targetTime % 3600) / 60;  
				int seconds = (int) (targetTime % 60);
				
				hourSpinner.setValue(hours);
				minuteSpinner.setValue(minutes);
				secondSpinner.setValue(seconds);
			}
		});
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
							.addComponent(timeLabel)
							.addComponent(hourSpinner)
							.addComponent(hourLabel)
							.addComponent(minuteSpinner)
							.addComponent(minuteLabel)
							.addComponent(secondSpinner)
							.addComponent(secondLabel)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
					.addComponent(includePausesChk));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(timeLabel)
								.addComponent(hourSpinner)
								.addComponent(hourLabel)
								.addComponent(minuteSpinner)
								.addComponent(minuteLabel)
								.addComponent(secondSpinner)
								.addComponent(secondLabel))
						.addComponent(includePausesChk));
		
		return panel;
	}
	
	private void format(JSpinner spinner, int size, String format) {
		DefaultEditor editor = new JSpinner.NumberEditor(spinner, format);
		spinner.setEditor(editor);

		JFormattedTextField field = editor.getTextField();
		field.setColumns(size);
		field.setHorizontalAlignment(JTextField.RIGHT);
		
		DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
		formatter.setCommitsOnValidEdit(false);
		formatter.setAllowsInvalid(true);
	}

	private class DurationSpinnerModel extends SpinnerNumberModel {
		private static final long serialVersionUID = 8069436847543688566L;
		private SpinnerModel linkedMinModel;
		private SpinnerModel linkedMaxModel;
		
		public DurationSpinnerModel(int value, int minValue, int maxValue, int step) {
			super(value, minValue, maxValue, step);
		}
		
		public void setLinkedMinModel(SpinnerModel linkedMinModel) {
			this.linkedMinModel = linkedMinModel;
		}

		public void setLinkedMaxModel(SpinnerModel linkedMaxModel) {
			this.linkedMaxModel = linkedMaxModel;
		}
		
		@Override
		public Object getNextValue() {
			Number value = (Number) super.getNextValue();
			
			if (value == null) {
				value = (Number) getMinimum();
				
				if (linkedMaxModel != null) {
					linkedMaxModel.setValue(linkedMaxModel.getNextValue());
				}
			}
			
			return value;
		}
		
		@Override
		public Object getPreviousValue() {
			Number value = (Number) super.getPreviousValue();
			
			if (value == null) {
				value = (Number) getMaximum();
				
				if (linkedMinModel != null) {
					linkedMinModel.setValue(linkedMinModel.getPreviousValue());
				}
			}
			
			return value;
		}
	}
}