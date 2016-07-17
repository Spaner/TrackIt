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
package com.miguelpernas.trackit.presentation;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

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

public class InputTimeDialog extends JDialog {
	private static final long serialVersionUID = -2970227573201328334L;

	private enum PanelType {
		TIME_PANEL
	};

	private JPanel timePanel;
	private JPanel dataPanel;
	private CardLayout cardLayout;

	private double targetTime;
	private int targetYear;
	private int targetMonth;
	private int targetDay;
	private int targetHour;
	private int targetMinute;
	private int targetSecond;
	private List<Long> savedTime;

	private Course course;
	private String mode;

	public InputTimeDialog(Course course, List<Long> savedTime, String mode) {
		super(TrackIt.getApplicationFrame());

		this.course = course;
		this.savedTime = savedTime;
		this.mode = mode;
		initComponents();
	}

	private void initComponents() {
		// targetTime = -1.0;
		Date timestamp = new Date();
		if (mode.equals("start")) {
			timestamp = course.getFirstTrackpoint().getTimestamp();
		}
		else if(mode.equals("end")){
			timestamp = course.getLastTrackpoint().getTimestamp();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		TimeZone utc = TimeZone.getTimeZone("UTC");
		sdf.setTimeZone(utc);

		Calendar date2 = new GregorianCalendar(utc);
		date2.setTime(timestamp);
		date2.setTimeZone(utc);
		// double time = date2.get(Calendar.HOUR_OF_DAY)*3600 +
		// date2.get(Calendar.MINUTE)*60 + date2.get(Calendar.SECOND);
		// double time = timestamp.getHours()*3600 + timestamp.getMinutes()*60 +
		// timestamp.getSeconds();
		timePanel = getTimePanel(date2, course.getTimerTime());
		// noPanel = new JPanel();

		dataPanel = new JPanel();
		cardLayout = new CardLayout();
		dataPanel.setLayout(cardLayout);
		dataPanel.add(timePanel, PanelType.TIME_PANEL.name());

		cardLayout.first(dataPanel);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		setTitle(Messages.getMessage("dialog.changeStartTime.title"));

		JLabel lblDescription = new JLabel(Messages.getMessage("dialog.changeStartTime.description"));

		JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if (targetTime == -1.0) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.mandatoryField", "Target Time"),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				int hours = (int) (targetTime / 3600);
				int hourRemainder = (int) (targetTime % 3600);
				int minutes = hourRemainder / 60;
				int seconds = hourRemainder % 60;
				TimeZone utc = TimeZone.getTimeZone("UTC");
				Date newStartTime = course.getFirstTrackpoint().getTimestamp();
				SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
				sdf.setTimeZone(utc);
				// System.out.println("Current Date & Time: " +
				// sdf.format(newStartTime));
				// System.out.println(newStartTime);

				Calendar date = new GregorianCalendar(utc);

				// System.out.println(date.toString());
				date.setTime(newStartTime);
				date.setTimeZone(utc);

				date.set(Calendar.YEAR, targetYear);
				date.set(Calendar.MONTH, targetMonth - 1);
				date.set(Calendar.DAY_OF_MONTH, targetDay);
				date.set(Calendar.HOUR_OF_DAY, targetHour);
				date.set(Calendar.MINUTE, targetMinute);
				date.set(Calendar.SECOND, targetSecond);
				newStartTime = date.getTime();
				// System.out.println(sdf.format(newStartTime));
				savedTime.clear();
				savedTime.add(date.getTime().getTime());

				InputTimeDialog.this.dispose();
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
		targetYear = calendar.get(Calendar.YEAR);
		targetMonth = calendar.get(Calendar.MONTH) + 1;
		targetDay = calendar.get(Calendar.DAY_OF_MONTH);
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
		double time = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60
				+ calendar.get(Calendar.SECOND);
		// targetTime = time;
		setTime(calendar);
		final long secondsInYear = 31536000;
		final long secondsInMonth = secondsInYear / 12;
		final long secondsInDay = 86400;
		final SpinnerModel yearModel = new DurationSpinnerModel(targetYear, 0, 3000, 1);
		final SpinnerModel monthModel = new DurationSpinnerModel(targetMonth, 1, 12, 1);
		((DurationSpinnerModel) monthModel).setLinkedMaxModel(yearModel);
		((DurationSpinnerModel) monthModel).setLinkedMinModel(yearModel);
		final SpinnerModel dayModel = new DurationSpinnerModel(targetDay, 1, 31, 1);
		((DurationSpinnerModel) dayModel).setLinkedMaxModel(monthModel);
		((DurationSpinnerModel) dayModel).setLinkedMinModel(monthModel);
		final SpinnerModel hourModel = new DurationSpinnerModel(targetHour, 0, 999, 1);
		((DurationSpinnerModel) hourModel).setLinkedMaxModel(dayModel);
		((DurationSpinnerModel) hourModel).setLinkedMinModel(dayModel);
		final SpinnerModel minuteModel = new DurationSpinnerModel(targetMinute, 0, 59, 1);
		((DurationSpinnerModel) minuteModel).setLinkedMaxModel(hourModel);
		((DurationSpinnerModel) minuteModel).setLinkedMinModel(hourModel);
		final SpinnerModel secondModel = new DurationSpinnerModel(targetSecond, 0, 59, 1);
		((DurationSpinnerModel) secondModel).setLinkedMaxModel(minuteModel);
		((DurationSpinnerModel) secondModel).setLinkedMinModel(minuteModel);

		final JSpinner yearSpinner = new JSpinner(yearModel);
		format(yearSpinner, 3, "0000");
		final JSpinner monthSpinner = new JSpinner(monthModel);
		format(monthSpinner, 2, "00");
		final JSpinner daySpinner = new JSpinner(dayModel);
		format(daySpinner, 2, "00");

		final JSpinner hourSpinner = new JSpinner(hourModel);
		format(hourSpinner, 3, "00");
		final JSpinner minuteSpinner = new JSpinner(minuteModel);
		format(minuteSpinner, 2, "00");
		final JSpinner secondSpinner = new JSpinner(secondModel);
		format(secondSpinner, 2, "00");

		ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				updateTargetTime(yearModel.getValue(), monthModel.getValue(), dayModel.getValue(), hourModel.getValue(),
						minuteSpinner.getValue(), secondSpinner.getValue());
			}

			private void updateTargetTime(Object year, Object month, Object day, Object hour, Object minute,
					Object second) {

				long daySeconds = 3600 * 24;
				long monthSeconds = 31 * daySeconds;
				if (year == null || month == null || day == null || hour == null || minute == null || second == null) {
					targetTime = -1.0;
				} else {
					TimeZone utc = TimeZone.getTimeZone("UTC");
					Calendar newCalendar = new GregorianCalendar(utc);

					int newYear = Integer.parseInt(year.toString());
					int newMonth = Integer.parseInt(month.toString());
					int newDay = Integer.parseInt(day.toString());
					int newHour = Integer.parseInt(hour.toString());
					int newMinute = Integer.parseInt(minute.toString());
					int newSecond = Integer.parseInt(second.toString());

					newCalendar.set(newYear, newMonth, newDay, newHour, newMinute, newSecond);

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
					targetYear = newYear;
					targetMonth = newMonth;
					targetDay = newDay;
					targetHour = newHour;
					targetMinute = newMinute;
					targetSecond = newSecond;

				}
			}
		};

		yearSpinner.addChangeListener(changeListener);
		monthSpinner.addChangeListener(changeListener);
		daySpinner.addChangeListener(changeListener);
		hourSpinner.addChangeListener(changeListener);
		minuteSpinner.addChangeListener(changeListener);
		secondSpinner.addChangeListener(changeListener);

		JLabel yearLabel = new JLabel(Messages.getMessage("applicationPanel.label.year"));
		JLabel monthLabel = new JLabel(Messages.getMessage("applicationPanel.label.month"));
		JLabel dayLabel = new JLabel(Messages.getMessage("applicationPanel.label.day"));
		JLabel hourLabel = new JLabel(Messages.getMessage("applicationPanel.label.hour"));
		JLabel minuteLabel = new JLabel(Messages.getMessage("applicationPanel.label.minute"));
		JLabel secondLabel = new JLabel(Messages.getMessage("applicationPanel.label.second"));

		/*
		 * JCheckBox includePausesChk = new
		 * JCheckBox(Messages.getMessage("applicationPanel.label.includePauses")
		 * , true); includePausesChk.setVisible(false);
		 * includePausesChk.addChangeListener(new ChangeListener() {
		 * 
		 * @Override public void stateChanged(ChangeEvent e) { updateDuration();
		 * }
		 * 
		 * private void updateDuration() { int hours = (int) (targetTime /
		 * 3600); int minutes = (int) (targetTime % 3600) / 60; int seconds =
		 * (int) (targetTime % 60);
		 * 
		 * hourSpinner.setValue(hours); minuteSpinner.setValue(minutes);
		 * secondSpinner.setValue(seconds); } });
		 */

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										// .addComponent(timeLabel)
										.addComponent(yearSpinner).addComponent(hourSpinner))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(yearLabel)
								.addComponent(hourLabel))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(monthSpinner)
								.addComponent(minuteSpinner))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(monthLabel)
								.addComponent(minuteLabel))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(daySpinner)
								.addComponent(secondSpinner))
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(dayLabel)
								.addComponent(secondLabel)))))
								// .addPreferredGap(ComponentPlacement.RELATED,
								// GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
								;

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										// .addComponent(timeLabel)
										.addComponent(yearSpinner)

										.addComponent(yearLabel).addComponent(monthSpinner).addComponent(monthLabel)
										.addComponent(daySpinner).addComponent(dayLabel))
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(hourSpinner).addComponent(hourLabel)

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