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

import static com.trackit.business.common.Messages.getMessage;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
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
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;
import com.trackit.business.common.SetPaceMethod;
import com.trackit.business.common.Unit;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;

class SetPaceDialog extends JDialog {
	private static final long serialVersionUID = -2970227573201328334L;

	private enum PanelType {
		NO_PANEL, TIME_PANEL, SPEED_PANEL, PERCENTAGE_PANEL, NEW_DATE_PANEL
	};

	private JPanel timePanel;
	private JPanel speedPanel;
	private JPanel percentagePanel;
	private JPanel noPanel;
	private JPanel dataPanel;
	private JPanel newDatePanel;
	private CardLayout cardLayout;

	private double targetSpeed;
	private int targetPercentage;
	private double targetTime;
	private boolean includePauses;
	private Calendar newStartDate;
	private Calendar newEndDate;
	//private String hiking = "Hiking";
	//private String cycling = "Cycling";
	private String smartPaceType;

	private Course course;

	SetPaceDialog(Course course) {
		super(TrackIt.getApplicationFrame());

		this.course = course;
		//this.sport = hiking;
		initComponents();
	}

	private void initComponents() {
		targetSpeed = -1.0;
		targetPercentage = -1;
		targetTime = -1.0;
		includePauses = true;
		newStartDate = new GregorianCalendar();
		newEndDate = new GregorianCalendar();
		newStartDate.setTime(course.getFirstTrackpoint().getTimestamp());
		newEndDate.setTime(course.getLastTrackpoint().getTimestamp());
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		TimeZone utc = TimeZone.getTimeZone("UTC");
		newStartDate.setTimeZone(utc);
		newEndDate.setTimeZone(utc);

		speedPanel = getSpeedPanel(course.getAverageSpeed() * 3.6);
		percentagePanel = getPercentagePanel();
		timePanel = getTimePanel(course.getElapsedTime(), course.getTimerTime());
		// noPanel = new JPanel();
		noPanel = getSmartPanel();
		newDatePanel = getNewDatePanel(newStartDate, newEndDate);

		dataPanel = new JPanel();
		cardLayout = new CardLayout();
		dataPanel.setLayout(cardLayout);
		dataPanel.add(noPanel, PanelType.NO_PANEL.name());
		dataPanel.add(speedPanel, PanelType.SPEED_PANEL.name());
		dataPanel.add(percentagePanel, PanelType.PERCENTAGE_PANEL.name());
		dataPanel.add(timePanel, PanelType.TIME_PANEL.name());
		dataPanel.add(newDatePanel, PanelType.NEW_DATE_PANEL.name());

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
				case NEW_DATES:
					cardLayout.show(dataPanel, PanelType.NEW_DATE_PANEL.name());
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
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("dialog.mandatoryField", "Target Speed"),
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
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("dialog.mandatoryField", "Target Percentage"),
								Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					options.put(Constants.SetPaceOperation.METHOD, method);
					options.put(Constants.SetPaceOperation.PERCENTAGE, targetPercentage / 100.0);
					options.put(Constants.SetPaceOperation.INCLUDE_PAUSES, includePauses);

					break;
				case TARGET_TIME:
					if (targetTime == -1.0) {
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("dialog.mandatoryField", "Target Time"),
								Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					options.put(Constants.SetPaceOperation.METHOD, method);
					options.put(Constants.SetPaceOperation.TIME, targetTime);
					options.put(Constants.SetPaceOperation.INCLUDE_PAUSES, includePauses);

					break;
				case NEW_DATES:
					if (targetTime == -1.0) {
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("dialog.mandatoryField", "Target Time"),
								Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
						return;
					}
					options.put(Constants.SetPaceOperation.METHOD, method);
					options.put(Constants.SetPaceOperation.NEW_START_DATE, newStartDate);
					options.put(Constants.SetPaceOperation.NEW_END_DATE, newEndDate);
					options.put(Constants.SetPaceOperation.OLD_START_DATE, null);
					options.put(Constants.SetPaceOperation.OLD_END_DATE, null);
					options.put(Constants.SetPaceOperation.INCLUDE_PAUSES, includePauses);
					break;
				case SMART_PACE:
					options.put(Constants.SetPaceOperation.METHOD, method);
					options.put(Constants.SetPaceOperation.SPORT, smartPaceType);
					break;
				default:
					// Do nothing;
				}
				if(smartPaceType.equals(getMessage("dialog.setPace.smartPaceNone")) && method.equals(SetPaceMethod.SMART_PACE)){
					SetPaceDialog.this.dispose();
				}
				else{
					setPace(options);
					SetPaceDialog.this.dispose();
					}

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
						JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
								Messages.getMessage("setPaceDialog.message.success"));
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

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblDescription)
				.addGroup(layout.createSequentialGroup().addComponent(lblMethod).addComponent(cbMethod,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE))
				.addComponent(dataPanel)
				.addGroup(layout.createSequentialGroup()
						.addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(cmdOk).addComponent(cmdCancel)));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(lblDescription)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(lblMethod)
						.addComponent(cbMethod))
				.addComponent(dataPanel)
				.addGroup(layout.createParallelGroup().addComponent(cmdOk).addComponent(cmdCancel)));

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
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							getMessage("applicationPanel.error.targetSpeed"), getMessage("trackIt.error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JLabel unitLabel = new JLabel(Unit.KILOMETER_PER_HOUR.toString());

		JCheckBox includePausesChk = new JCheckBox(Messages.getMessage("applicationPanel.label.includePauses"),
				includePauses);
		includePausesChk.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				includePauses = ((JCheckBox) e.getSource()).isSelected();
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(speedLabel).addComponent(speedSpinner, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(unitLabel))
				.addComponent(includePausesChk));

		layout.setVerticalGroup(layout
				.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(speedLabel).addComponent(speedSpinner).addComponent(unitLabel))
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
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							getMessage("applicationPanel.error.targetPercentage"), getMessage("trackIt.error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JLabel unitLabel = new JLabel(Unit.PERCENTAGE.toString());

		JCheckBox includePausesChk = new JCheckBox(Messages.getMessage("applicationPanel.label.includePauses"),
				includePauses);
		includePausesChk.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				includePauses = ((JCheckBox) e.getSource()).isSelected();
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(percentageLabel).addComponent(percentageSpinner, GroupLayout.PREFERRED_SIZE,
								GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(unitLabel))
				.addComponent(includePausesChk));

		layout.setVerticalGroup(layout
				.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(percentageLabel).addComponent(percentageSpinner).addComponent(unitLabel))
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
					targetTime = Integer.parseInt(hour.toString()) * 3600 + Integer.parseInt(minute.toString()) * 60
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

		JCheckBox includePausesChk = new JCheckBox(Messages.getMessage("applicationPanel.label.includePauses"),
				includePauses);
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

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addComponent(timeLabel).addComponent(hourSpinner)
						.addComponent(hourLabel).addComponent(minuteSpinner).addComponent(minuteLabel)
						.addComponent(secondSpinner).addComponent(secondLabel)
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
				.addComponent(includePausesChk));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(timeLabel)
						.addComponent(hourSpinner).addComponent(hourLabel).addComponent(minuteSpinner)
						.addComponent(minuteLabel).addComponent(secondSpinner).addComponent(secondLabel))
				.addComponent(includePausesChk));

		return panel;
	}

	private JPanel getSmartPanel() {
		JPanel panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		int sportID = course.getSport().getSportID();
		int subSportID = course.getSubSport().getSubSportID();
		
		final JLabel smartPaceTypeTitle = new JLabel(getMessage("dialog.setPace.smartPaceType"));
		JTextArea smartPaceTypeName = new JTextArea("");
		smartPaceTypeName.setEditable(false);
		if((sportID == 11 || sportID == 17) && (subSportID == 3 || subSportID == 7)){
			smartPaceType = getMessage("dialog.setPace.smartPaceHiking");
			smartPaceTypeName.setText(smartPaceType);
			
		}
		else if((sportID == 0 || sportID == 2) && (subSportID == 0 || subSportID == 7)){
			smartPaceType = getMessage("dialog.setPace.smartPaceCycling");
			smartPaceTypeName.setText(smartPaceType);
			
		}
		else{
			smartPaceType = getMessage("dialog.setPace.smartPaceNone");
			smartPaceTypeName.setText(getMessage("dialog.setPace.smartPaceNotSupported"));
		}
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(smartPaceTypeTitle)
						.addComponent(smartPaceTypeName)));
		
		layout.setVerticalGroup(layout.createSequentialGroup()
								.addComponent(smartPaceTypeTitle)
								.addComponent(smartPaceTypeName));

		/*final JRadioButton hikingButton = new JRadioButton("Hiking");
		hikingButton.setActionCommand("Hiking");
		hikingButton.setSelected(true);

		final JRadioButton cyclingButton = new JRadioButton("Cycling");
		cyclingButton.setActionCommand("Cycling");
		cyclingButton.setSelected(true);

		ButtonGroup group = new ButtonGroup();
		group.add(hikingButton);
		group.add(cyclingButton);

		panel.add(hikingButton);
		panel.add(cyclingButton);
		ActionListener actionListenter = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (hikingButton.isSelected()) {
					sport = hiking;
				} else if (cyclingButton.isSelected()) {
					sport = cycling;
				}
			}
		};
		hikingButton.addActionListener(actionListenter);
		cyclingButton.addActionListener(actionListenter);*/
		

		return panel;
	}

	private JPanel getNewDatePanel(final Calendar startDate, final Calendar endDate) {
		JPanel panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		final JLabel startTimeLabel = new JLabel(getMessage("dialog.setPace.newStart"));
		final JLabel endTimeLabel = new JLabel(getMessage("dialog.setPace.newEnd"));

		final SpinnerModel newStartYearModel = new DurationSpinnerModel(startDate.get(Calendar.YEAR), 0, 3000, 1);
		final SpinnerModel newStartMonthModel = new DurationSpinnerModel(startDate.get(Calendar.MONTH) + 1, 1, 12, 1);
		((DurationSpinnerModel) newStartMonthModel).setLinkedMaxModel(newStartYearModel);
		((DurationSpinnerModel) newStartMonthModel).setLinkedMinModel(newStartYearModel);
		final SpinnerModel newStartDayModel = new DurationSpinnerModel(startDate.get(Calendar.DAY_OF_MONTH), 1, 31, 1);
		((DurationSpinnerModel) newStartDayModel).setLinkedMaxModel(newStartMonthModel);
		((DurationSpinnerModel) newStartDayModel).setLinkedMinModel(newStartMonthModel);
		final SpinnerModel newStartHourModel = new DurationSpinnerModel(startDate.get(Calendar.HOUR_OF_DAY), 0, 23, 1);
		((DurationSpinnerModel) newStartHourModel).setLinkedMaxModel(newStartDayModel);
		((DurationSpinnerModel) newStartHourModel).setLinkedMinModel(newStartDayModel);
		final SpinnerModel newStartMinuteModel = new DurationSpinnerModel(startDate.get(Calendar.MINUTE), 0, 59, 1);
		((DurationSpinnerModel) newStartMinuteModel).setLinkedMaxModel(newStartHourModel);
		((DurationSpinnerModel) newStartMinuteModel).setLinkedMinModel(newStartHourModel);
		final SpinnerModel newStartSecondModel = new DurationSpinnerModel(startDate.get(Calendar.SECOND), 0, 59, 1);
		((DurationSpinnerModel) newStartSecondModel).setLinkedMaxModel(newStartMinuteModel);
		((DurationSpinnerModel) newStartSecondModel).setLinkedMinModel(newStartMinuteModel);

		final SpinnerModel newEndYearModel = new DurationSpinnerModel(endDate.get(Calendar.YEAR), 0, 3000, 1);
		final SpinnerModel newEndMonthModel = new DurationSpinnerModel(endDate.get(Calendar.MONTH) + 1, 1, 12, 1);
		((DurationSpinnerModel) newEndMonthModel).setLinkedMaxModel(newEndYearModel);
		((DurationSpinnerModel) newEndMonthModel).setLinkedMinModel(newEndYearModel);
		final SpinnerModel newEndDayModel = new DurationSpinnerModel(endDate.get(Calendar.DAY_OF_MONTH), 1, 31, 1);
		((DurationSpinnerModel) newEndDayModel).setLinkedMaxModel(newEndMonthModel);
		((DurationSpinnerModel) newEndDayModel).setLinkedMinModel(newEndMonthModel);
		final SpinnerModel newEndHourModel = new DurationSpinnerModel(endDate.get(Calendar.HOUR_OF_DAY), 0, 23, 1);
		((DurationSpinnerModel) newEndHourModel).setLinkedMaxModel(newEndDayModel);
		((DurationSpinnerModel) newEndHourModel).setLinkedMinModel(newEndDayModel);
		final SpinnerModel newEndMinuteModel = new DurationSpinnerModel(endDate.get(Calendar.MINUTE), 0, 59, 1);
		((DurationSpinnerModel) newEndMinuteModel).setLinkedMaxModel(newEndHourModel);
		((DurationSpinnerModel) newEndMinuteModel).setLinkedMinModel(newEndHourModel);
		final SpinnerModel newEndSecondModel = new DurationSpinnerModel(endDate.get(Calendar.SECOND), 0, 59, 1);
		((DurationSpinnerModel) newEndSecondModel).setLinkedMaxModel(newEndMinuteModel);
		((DurationSpinnerModel) newEndSecondModel).setLinkedMinModel(newEndMinuteModel);

		final JSpinner newStartYearSpinner = new JSpinner(newStartYearModel);
		format(newStartYearSpinner, 3, "0000");
		final JSpinner newStartMonthSpinner = new JSpinner(newStartMonthModel);
		format(newStartMonthSpinner, 2, "00");
		final JSpinner newStartDaySpinner = new JSpinner(newStartDayModel);
		format(newStartDaySpinner, 2, "00");

		final JSpinner newStartHourSpinner = new JSpinner(newStartHourModel);
		format(newStartHourSpinner, 3, "00");
		final JSpinner newStartMinuteSpinner = new JSpinner(newStartMinuteModel);
		format(newStartMinuteSpinner, 2, "00");
		final JSpinner newStartSecondSpinner = new JSpinner(newStartSecondModel);
		format(newStartSecondSpinner, 2, "00");

		final JSpinner newEndYearSpinner = new JSpinner(newEndYearModel);
		format(newEndYearSpinner, 3, "0000");
		final JSpinner newEndMonthSpinner = new JSpinner(newEndMonthModel);
		format(newEndMonthSpinner, 2, "00");
		final JSpinner newEndDaySpinner = new JSpinner(newEndDayModel);
		format(newStartDaySpinner, 2, "00");

		final JSpinner newEndHourSpinner = new JSpinner(newEndHourModel);
		format(newEndHourSpinner, 3, "00");
		final JSpinner newEndMinuteSpinner = new JSpinner(newEndMinuteModel);
		format(newEndMinuteSpinner, 2, "00");
		final JSpinner newEndSecondSpinner = new JSpinner(newEndSecondModel);
		format(newEndSecondSpinner, 2, "00");

		ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateTargetTime(newStartYearModel.getValue(), newStartMonthModel.getValue(),
						newStartDayModel.getValue(), newStartHourModel.getValue(), newStartMinuteSpinner.getValue(),
						newStartSecondSpinner.getValue(), newEndYearModel.getValue(), newEndMonthModel.getValue(),
						newEndDayModel.getValue(), newEndHourModel.getValue(), newEndMinuteSpinner.getValue(),
						newEndSecondSpinner.getValue());
			}

			private void updateTargetTime(Object newStartYear, Object newStartMonth, Object newStartDay,
					Object newStartHour, Object newStartMinute, Object newStartSecond, Object newEndYear,
					Object newEndMonth, Object newEndDay, Object newEndHour, Object newEndMinute, Object newEndSecond) {

				long daySeconds = 3600 * 24;
				long monthSeconds = 31 * daySeconds;
				if (newStartYear == null || newStartMonth == null || newStartDay == null || newStartHour == null
						|| newStartMinute == null || newStartSecond == null || newEndYear == null || newEndMonth == null
						|| newEndDay == null || newEndHour == null || newEndMinute == null || newEndSecond == null) {
					targetTime = -1.0;
				} else {
					TimeZone utc = TimeZone.getTimeZone("UTC");
					Calendar newCalendar = new GregorianCalendar(utc);

					int targetStartYear = Integer.parseInt(newStartYear.toString());
					int targetStartMonth = Integer.parseInt(newStartMonth.toString());
					int targetStartDay = Integer.parseInt(newStartDay.toString());
					int targetStartHour = Integer.parseInt(newStartHour.toString());
					int targetStartMinute = Integer.parseInt(newStartMinute.toString());
					int targetStartSecond = Integer.parseInt(newStartSecond.toString());

					int targetEndYear = Integer.parseInt(newEndYear.toString());
					int targetEndMonth = Integer.parseInt(newEndMonth.toString());
					int targetEndDay = Integer.parseInt(newEndDay.toString());
					int targetEndHour = Integer.parseInt(newEndHour.toString());
					int targetEndMinute = Integer.parseInt(newEndMinute.toString());
					int targetEndSecond = Integer.parseInt(newEndSecond.toString());

					newStartDate.set(targetStartYear, targetStartMonth-1, targetStartDay, targetStartHour,
							targetStartMinute, targetStartSecond);
					newEndDate.set(targetEndYear, targetEndMonth-1, targetEndDay, targetEndHour, targetEndMinute,
							targetEndSecond);
					/*
					 * if(month.toString().equals("2") &&
					 * Integer.parseInt(year.toString()) % 4 == 0){ monthSeconds
					 * = daySeconds * 29; } else if(month.toString().equals("2")
					 * && Integer.parseInt(year.toString()) % 4 != 0){
					 * monthSeconds = daySeconds * 28; } else
					 * if(month.toString().equals("1")||
					 * month.toString().equals("3") ||
					 * month.toString().equals("5") ||
					 * month.toString().equals("7") ||
					 * month.toString().equals("8") ||
					 * month.toString().equals("10") ||
					 * month.toString().equals("12") ){ monthSeconds =
					 * daySeconds * 30; }
					 */

				}
			}
		};

		newStartYearSpinner.addChangeListener(changeListener);
		newStartMonthSpinner.addChangeListener(changeListener);
		newStartDaySpinner.addChangeListener(changeListener);
		newStartHourSpinner.addChangeListener(changeListener);
		newStartMinuteSpinner.addChangeListener(changeListener);
		newStartSecondSpinner.addChangeListener(changeListener);
		
		newEndYearSpinner.addChangeListener(changeListener);
		newEndMonthSpinner.addChangeListener(changeListener);
		newEndDaySpinner.addChangeListener(changeListener);
		newEndHourSpinner.addChangeListener(changeListener);
		newEndMinuteSpinner.addChangeListener(changeListener);
		newEndSecondSpinner.addChangeListener(changeListener);

		JLabel yearLabel = new JLabel(Messages.getMessage("applicationPanel.label.year"));
		JLabel monthLabel = new JLabel(Messages.getMessage("applicationPanel.label.month"));
		JLabel dayLabel = new JLabel(Messages.getMessage("applicationPanel.label.day"));
		JLabel hourLabel = new JLabel(Messages.getMessage("applicationPanel.label.hour"));
		JLabel minuteLabel = new JLabel(Messages.getMessage("applicationPanel.label.minute"));
		JLabel secondLabel = new JLabel(Messages.getMessage("applicationPanel.label.second"));

		
		JCheckBox includePausesChk = new JCheckBox(Messages.getMessage("applicationPanel.label.includePauses"),
				includePauses);
		includePausesChk.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				includePauses = ((JCheckBox) e.getSource()).isSelected();
			}
		});
		 

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(startTimeLabel)
										.addComponent(newStartYearSpinner).addComponent(newStartHourSpinner).addComponent(endTimeLabel)
										.addComponent(newEndYearSpinner).addComponent(newEndHourSpinner).addComponent(includePausesChk))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(yearLabel)
								.addComponent(hourLabel).addComponent(yearLabel).addComponent(hourLabel))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(newStartMonthSpinner).addComponent(newStartMinuteSpinner)
								.addComponent(newEndMonthSpinner).addComponent(newEndMinuteSpinner))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(monthLabel)
								.addComponent(minuteLabel).addComponent(monthLabel).addComponent(minuteLabel))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(newStartDaySpinner).addComponent(newStartSecondSpinner)
								.addComponent(newEndDaySpinner).addComponent(newEndSecondSpinner))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(dayLabel)
								.addComponent(secondLabel).addComponent(dayLabel).addComponent(secondLabel)))))
								// .addPreferredGap(ComponentPlacement.RELATED,
								// GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
		;

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addGroup(layout.createSequentialGroup().addComponent(startTimeLabel)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										
										.addComponent(newStartYearSpinner)

										.addComponent(yearLabel).addComponent(newStartMonthSpinner)
										.addComponent(monthLabel).addComponent(newStartDaySpinner)
										.addComponent(dayLabel))
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(newStartHourSpinner).addComponent(hourLabel)

										.addComponent(newStartMinuteSpinner).addComponent(minuteLabel)

										.addComponent(newStartSecondSpinner).addComponent(secondLabel))
								.addComponent(endTimeLabel)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										// .addComponent(timeLabel)
										.addComponent(newEndYearSpinner)

										.addComponent(yearLabel).addComponent(newEndMonthSpinner)
										.addComponent(monthLabel).addComponent(newEndDaySpinner).addComponent(dayLabel))
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										
										.addComponent(newEndHourSpinner)

										.addComponent(hourLabel).addComponent(newEndMinuteSpinner)
										.addComponent(minuteLabel).addComponent(newEndSecondSpinner)
										.addComponent(secondLabel))
								.addComponent(includePausesChk))));

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