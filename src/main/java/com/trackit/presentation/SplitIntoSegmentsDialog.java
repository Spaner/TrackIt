package com.trackit.presentation;

import static com.trackit.business.common.Messages.getMessage;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import org.apache.log4j.Logger;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Constants;
import com.trackit.business.common.Messages;
import com.trackit.business.common.Unit;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;
import com.trackit.business.utilities.TrackItPreferences;
import com.trackit.presentation.InputPauseTimeDialog.DurationSpinnerModel;

public class SplitIntoSegmentsDialog extends JDialog {
	private static final long serialVersionUID = 5531697309767016552L;
	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	
	public enum PanelType {
		SPLIT_INTO_SEGMENTS_PANEL
	};
	

	private JPanel splitIntoSegmentsPanel;
	private JPanel dataPanel;
	private CardLayout cardLayout;
	private Course course;
	
	private double targetTime;
	private int targetHour;
	private int targetMinute;
	private int targetSecond;
	private double targetDistance;
	private double targetNumber;
	
	private boolean isNumber;
	private boolean isTime;
	private boolean isDistance;
	
	
	public SplitIntoSegmentsDialog(Course course){
		super(TrackIt.getApplicationFrame());
		this.course = course;
		this.targetTime = -1;
		this.targetDistance = -1;
		this.targetNumber = -1;
		this.isDistance = false;
		this.isNumber = false;
		this.isTime = false;
		initComponents();
	}
	
	private void initComponents() {
		splitIntoSegmentsPanel = getSplitIntoSegmentsPanel();
		dataPanel = new JPanel();
		cardLayout = new CardLayout();
		dataPanel.setLayout(cardLayout);
		dataPanel.add(splitIntoSegmentsPanel, PanelType.SPLIT_INTO_SEGMENTS_PANEL.name());
		cardLayout.first(dataPanel);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		setTitle(Messages.getMessage("dialog.splitIntoSegments.title"));
		
		JLabel lblDescription = new JLabel(Messages.getMessage("dialog.splitIntoSegments.description"));
		
		JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if (isNumber && (targetNumber == -1.0 || targetNumber == 0.0)) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.mandatoryField", Messages.getMessage("dialog.splitIntoSegments.segmentNumber")),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (isTime && (targetTime >= course.getElapsedTime()*1000)) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.splitIntoSegments.errorLongTime"),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (isTime && (targetTime == -1.0 || targetTime == 0.0)) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.mandatoryField", Messages.getMessage("dialog.splitIntoSegments.segmentDuration")),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (isDistance && (targetDistance == -1.0 || targetDistance == 0.0)) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.mandatoryField", Messages.getMessage("dialog.splitIntoSegments.segmentLength")),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if (isDistance && (targetDistance >= course.getDistance())) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.splitIntoSegments.errorLongDistance"),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				DocumentManager documentManager = DocumentManager.getInstance();
				boolean addToUndoManager = true;
				boolean redo = false;
				if(isNumber){
					documentManager.splitIntoSegments(course, isNumber, isTime, isDistance, targetNumber, addToUndoManager, redo);
				}
				if(isTime){
					documentManager.splitIntoSegments(course, isNumber, isTime, isDistance, targetTime, addToUndoManager, redo);
				}
				if(isDistance){
					documentManager.splitIntoSegments(course, isNumber, isTime, isDistance, targetDistance, addToUndoManager, redo);
				}
				SplitIntoSegmentsDialog.this.dispose();
			}
			
		});

		JButton cmdCancel = new JButton(Messages.getMessage("trackIt.cmdCancel"));
		cmdCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {

				SplitIntoSegmentsDialog.this.dispose();
			}
		});
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(lblDescription)
				.addGroup(layout.createSequentialGroup())
				.addComponent(dataPanel)
				.addGroup(layout.createSequentialGroup().addPreferredGap(ComponentPlacement.UNRELATED,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(cmdOk)
						.addComponent(cmdCancel)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(lblDescription)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
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
	
	private JPanel getSplitIntoSegmentsPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		final JRadioButton numberButton = new JRadioButton(getMessage("dialog.splitIntoSegments.number"));
		numberButton.setActionCommand(getMessage("dialog.splitIntoSegments.number"));
		numberButton.setSelected(true);
		this.isNumber = true;
		
		final NumberFormat numberFormat = NumberFormat.getNumberInstance();
		final JFormattedTextField numberField = new JFormattedTextField(numberFormat);
		numberField.setValue(0);
		numberField.setHorizontalAlignment(JTextField.RIGHT);
		numberField.setColumns(3);
		
		numberField.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object source = event.getSource();
				if (source == numberField) {
					targetNumber = ((Number) numberField.getValue()).doubleValue();
					/*PpreferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.SEGMENTS, null,
									Constants.SplitIntoSegmentsPreferences.NUMBER_OF_SEGMENTS, targetNumber);
						}
					});*/
				}

			}

		});

		numberField.setMaximumSize(new Dimension(70, numberField.getHeight()));
		numberField.setHorizontalAlignment(JTextField.RIGHT);
		
		final JLabel numberUnit = new JLabel(getMessage("dialog.splitIntoSegments.segments"));
		
		final JRadioButton timeButton = new JRadioButton(getMessage("dialog.splitIntoSegments.time"));
		timeButton.setActionCommand(getMessage("dialog.splitIntoSegments.time"));
		
		Double segmentDuration = 0.0;
		int hour = 0;
		int minute = 0;
		int second = 0;

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
					
					String time = targetHour + ":" + targetMinute + ":" + targetSecond;

					Calendar date = Calendar.getInstance(utc);
					DateFormat format = new SimpleDateFormat("HH:mm:ss");
					format.setTimeZone(utc);				
					try {
						date.setTime(format.parse(time));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					targetTime = date.getTime().getTime();

				}
			}
		};

		hourSpinner.addChangeListener(changeListener);
		minuteSpinner.addChangeListener(changeListener);
		secondSpinner.addChangeListener(changeListener);

		JLabel hourLabel = new JLabel(Messages.getMessage("applicationPanel.label.hour"));
		JLabel minuteLabel = new JLabel(Messages.getMessage("applicationPanel.label.minute"));
		JLabel secondLabel = new JLabel(Messages.getMessage("applicationPanel.label.second"));
		
		final JRadioButton distanceButton = new JRadioButton(getMessage("dialog.splitIntoSegments.distance"));
		distanceButton.setActionCommand(getMessage("dialog.splitIntoSegments.distance"));
		
		final NumberFormat distanceFormat = NumberFormat.getNumberInstance();
		final JFormattedTextField distanceField = new JFormattedTextField(distanceFormat);

		distanceField.setValue(0);
		distanceField.setHorizontalAlignment(JTextField.RIGHT);
		distanceField.setColumns(5);
		
		distanceField.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object source = event.getSource();
				if (source == distanceField) {
					targetDistance = ((Number) distanceField.getValue()).doubleValue();
					/*preferencesToApply.add(new PreferenceTask() {
						public void execute() {
							appPreferences.setPreference(Constants.PrefsCategories.SEGMENTS, null,
									Constants.SplitIntoSegmentsPreferences.LENGTH_OF_SEGMENTS, targetDistance);
						}
					});*/
				}

			}

		});
		
		
		final JLabel distanceUnit = new JLabel(Unit.METER.toString());
		
		ButtonGroup group = new ButtonGroup();
		group.add(numberButton);
		group.add(timeButton);
		group.add(distanceButton);
		
		numberField.setEnabled(true);
		hourSpinner.setEnabled(false);
		minuteSpinner.setEnabled(false);
		secondSpinner.setEnabled(false);
		distanceField.setEnabled(false);
		
		ActionListener actionListenter = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (numberButton.isSelected()) {
					isNumber = true;
					isTime = false;
					isDistance = false;
					numberField.setEnabled(true);
					hourSpinner.setEnabled(false);
					minuteSpinner.setEnabled(false);
					secondSpinner.setEnabled(false);
					distanceField.setEnabled(false);
				} else if (timeButton.isSelected()) {
					isNumber = false;
					isTime = true;
					isDistance = false;
					numberField.setEnabled(false);
					hourSpinner.setEnabled(true);
					minuteSpinner.setEnabled(true);
					secondSpinner.setEnabled(true);
					distanceField.setEnabled(false);
				}
				else if (distanceButton.isSelected()) {
					isNumber = false;
					isTime = false;
					isDistance = true;
					numberField.setEnabled(false);
					hourSpinner.setEnabled(false);
					minuteSpinner.setEnabled(false);
					secondSpinner.setEnabled(false);
					distanceField.setEnabled(true);
				}
			}
		};
		numberButton.addActionListener(actionListenter);
		timeButton.addActionListener(actionListenter);
		distanceButton.addActionListener(actionListenter);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(numberButton)
						.addGroup(layout.createSequentialGroup()
								.addComponent(numberField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
								          GroupLayout.PREFERRED_SIZE)
								.addComponent(numberUnit))
						.addComponent(timeButton)
						.addGroup(layout.createSequentialGroup()
								.addComponent(hourSpinner)
								.addComponent(hourLabel)
								.addComponent(minuteSpinner)
								.addComponent(minuteLabel)
								.addComponent(secondSpinner)
								.addComponent(secondLabel))
						.addComponent(distanceButton)
						.addGroup(layout.createSequentialGroup()
								.addComponent(distanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						          GroupLayout.PREFERRED_SIZE)
								.addComponent(distanceUnit))
				));
				
				

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(numberButton))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(numberField)
						.addComponent(numberUnit))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(timeButton))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(hourSpinner)
						.addComponent(hourLabel)
						.addComponent(minuteSpinner)
						.addComponent(minuteLabel)
						.addComponent(secondSpinner)
						.addComponent(secondLabel))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(distanceButton))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(distanceField)
						.addComponent(distanceUnit)));
	
		
		
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

	private interface PreferenceTask {
		public void execute();
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
