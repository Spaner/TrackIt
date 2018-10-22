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
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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
import com.trackit.business.domain.Trackpoint;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;

public class InputPauseTimeDialog extends JDialog {
	private static final long serialVersionUID = -2970227573201328334L;

	private enum PanelType {
		TIME_PANEL
	};

	private JPanel timePanel;
	private JPanel dataPanel;
	private CardLayout cardLayout;

	private double targetTime;
	private int targetHour;
	private int targetMinute;
	private int targetSecond;
	private Trackpoint trackpoint;
	private boolean addPause;
	private boolean changePause;

	private Course course;

	public InputPauseTimeDialog(Trackpoint trackpoint, String string) {
		super(TrackIt.getApplicationFrame());
		if (string.equals(Constants.PauseDialogOptions.ADD_PAUSE)) {
			addPause = true;
			changePause = false;
		} else if (string.equals(Constants.PauseDialogOptions.CHANGE_DURATION)) {
			addPause = false;
			changePause = true;
		}
		this.course = (Course) trackpoint.getParent();
		this.trackpoint = trackpoint;
		initComponents();
	}

	private void initComponents() {
		// targetTime = -1.0;
		Date timestamp = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		TimeZone utc = TimeZone.getTimeZone("UTC");
		sdf.setTimeZone(utc);

		Calendar date = Calendar.getInstance(utc);
		// date.setTime(timestamp);
		// date.setTimeZone(utc);
		timePanel = getTimePanel(date, course.getTimerTime());

		dataPanel = new JPanel();
		cardLayout = new CardLayout();
		dataPanel.setLayout(cardLayout);
		dataPanel.add(timePanel, PanelType.TIME_PANEL.name());

		cardLayout.first(dataPanel);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		JLabel lblDescription = new JLabel("");
		if (addPause) {
			setTitle(Messages.getMessage("inputPauseTimeDialog.name"));

			lblDescription = new JLabel(Messages.getMessage("inputPauseTimeDialog.description"));
		} else if (changePause) {
			setTitle(Messages.getMessage("inputPauseTimeDialog.changeDuration"));

			lblDescription = new JLabel(Messages.getMessage("inputPauseTimeDialog.changeDurationDescription"));
		}

		JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if (targetTime == -1.0) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.mandatoryField", "inputPauseTimeDialog.targetTime"),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}

				String time = targetHour + ":" + targetMinute + ":" + targetSecond;

				TimeZone utc = TimeZone.getTimeZone("UTC");
				Calendar date = Calendar.getInstance(utc);
				DateFormat format = new SimpleDateFormat("HH:mm:ss");
				format.setTimeZone(utc);

				try {
					date.setTime(format.parse(time));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(date.getTime());

				long pausedTime = date.getTime().getTime();
				boolean addToUndoManager = true;
				DocumentManager documentManager = DocumentManager.getInstance();
				Course course = (Course) trackpoint.getParent();
				if (changePause) {
					if (pausedTime == 0) {
						documentManager.removePause(course, trackpoint, addToUndoManager, null);
					} else {
						documentManager.changePauseDuration(course, trackpoint, pausedTime, addToUndoManager);
					}
				}
				else{
					documentManager.addPause(course, trackpoint, pausedTime, addToUndoManager);
				}

				InputPauseTimeDialog.this.dispose();
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
				layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(lblDescription)
						.addGroup(layout.createSequentialGroup())
						// .addComponent(lblMethod)
						// .addComponent(cbMethod, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.DEFAULT_SIZE,
						// GroupLayout.DEFAULT_SIZE))
						.addComponent(dataPanel)
						.addGroup(layout
								.createSequentialGroup().addPreferredGap(ComponentPlacement.UNRELATED,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(cmdOk).addComponent(cmdCancel)));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(lblDescription)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
				// .addComponent(lblMethod)
				// .addComponent(cbMethod))
				.addComponent(dataPanel)
				.addGroup(layout.createParallelGroup().addComponent(cmdOk).addComponent(cmdCancel)));

		getRootPane().setDefaultButton(cmdOk);

		pack();
		setModal(true);
		setResizable(false);
		setLocationRelativeTo(TrackIt.getApplicationFrame());
	}

	private void setTime(Calendar calendar) {
		targetHour = calendar.get(Calendar.HOUR_OF_DAY);
		targetMinute = calendar.get(Calendar.MINUTE);
		targetSecond = calendar.get(Calendar.SECOND);
	}

	private JPanel getTimePanel(final Calendar calendar, final double timerTime) {
		JPanel panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		final JLabel timeLabel = new JLabel(getMessage("applicationPanel.label.targetTime"));
		
		Double pauseDuration = (double)course.getSubSport().getDefaultPauseDuration();
		int hour = (int) (pauseDuration / 3600);
		int minute = (int) (pauseDuration % 3600 / 60);
		int second = (int) (pauseDuration % 3600 % 60);
		if (changePause) {
			pauseDuration = course.getPause(trackpoint.getTimestamp().getTime()).getDuration();

			hour = (int) (pauseDuration / 3600);
			minute = (int) (pauseDuration % 3600 / 60);
			second = (int) (pauseDuration % 3600 % 60);
		}
		targetHour = hour;
		targetMinute = minute;
		targetSecond = second;
		//setTime(calendar);
		final SpinnerModel hourModel = new DurationSpinnerModel(hour, 0, 999, 1);
		final SpinnerModel minuteModel = new DurationSpinnerModel(minute, 0, 59, 1);
		((DurationSpinnerModel) minuteModel).setLinkedMaxModel(hourModel);
		((DurationSpinnerModel) minuteModel).setLinkedMinModel(hourModel);
		final SpinnerModel secondModel = new DurationSpinnerModel(second, 0, 59, 1);
		((DurationSpinnerModel) secondModel).setLinkedMaxModel(minuteModel);
		((DurationSpinnerModel) secondModel).setLinkedMinModel(minuteModel);

		final JSpinner hourSpinner = new JSpinner(hourModel);
		format(hourSpinner, 3, "000");
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
					TimeZone utc = TimeZone.getTimeZone("UTC");
					Calendar newCalendar = new GregorianCalendar(utc);

					int newHour = Integer.parseInt(hour.toString());
					int newMinute = Integer.parseInt(minute.toString());
					int newSecond = Integer.parseInt(second.toString());

					newCalendar.set(newHour, newMinute, newSecond);
					targetHour = newHour;
					targetMinute = newMinute;
					targetSecond = newSecond;

				}
			}
		};

		hourSpinner.addChangeListener(changeListener);
		minuteSpinner.addChangeListener(changeListener);
		secondSpinner.addChangeListener(changeListener);

		JLabel hourLabel = new JLabel(Messages.getMessage("applicationPanel.label.hour"));
		JLabel minuteLabel = new JLabel(Messages.getMessage("applicationPanel.label.minute"));
		JLabel secondLabel = new JLabel(Messages.getMessage("applicationPanel.label.second"));

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										// .addComponent(timeLabel)
										.addComponent(hourSpinner))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(hourLabel))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(minuteSpinner))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(minuteLabel))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(secondSpinner))
						.addGroup(
								layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(secondLabel)))))
								// .addPreferredGap(ComponentPlacement.RELATED,
								// GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
		;

		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addGroup(layout.createSequentialGroup()

						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(hourSpinner)
								.addComponent(hourLabel)

								.addComponent(minuteSpinner).addComponent(minuteLabel)

								.addComponent(secondSpinner).addComponent(secondLabel)))));

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

	public class DurationSpinnerModel extends SpinnerNumberModel {
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