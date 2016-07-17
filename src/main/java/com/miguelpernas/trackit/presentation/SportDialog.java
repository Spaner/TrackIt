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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.SetPaceMethod;
import com.henriquemalheiro.trackit.business.common.Unit;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.task.Action;
import com.henriquemalheiro.trackit.presentation.task.Task;
import com.miguelpernas.trackit.business.common.JoinOptions;
import com.pg58406.trackit.business.db.Database;

public class SportDialog extends JDialog {
	private static final long serialVersionUID = -2970227573201328334L;
	public static Logger logger = Logger.getLogger(TrackIt.class.getName());

	private enum PanelType {
		SPORT_PANEL
	};

	private JPanel sportPanel;
	private JPanel dataPanel;
	private CardLayout cardLayout;

	private SportType activeSport;
	private SportType newSelectedSport;
	private SubSportType activeSubSport;
	private SubSportType newSelectedSubSport;
	private Course course;
	private Activity activity;
	private boolean isCourse;
	private boolean isActivity;
	private String courseType;
	private String activityType;

	public SportDialog(Activity activity) {
		super(TrackIt.getApplicationFrame());
		this.activity = activity;
		if(activity.getSport() == null){
			//this.activeSport = SportType.GENERIC;
			this.newSelectedSport = SportType.GENERIC;
		}
		else{
			//this.activeSport = activity.getSport();
			this.newSelectedSport = activity.getSport();
		}
		if(activity.getSubSport() == null){
			//this.activeSubSport = SubSportType.GENERIC;
			this.newSelectedSubSport = SubSportType.GENERIC_SUB;
		}
		else{
			//this.activeSubSport = activity.getSubSport();
			this.newSelectedSubSport = activity.getSubSport();
		}

		isCourse = false;
		isActivity = true;
		initComponents();
	}

	public SportDialog(Course course) {
		super(TrackIt.getApplicationFrame());

		this.course = course;
		if(course.getSport() == null){
			//this.activeSport = SportType.GENERIC;
			this.newSelectedSport = SportType.GENERIC;
		}
		else{
			//this.activeSport = course.getSport();
			this.newSelectedSport = course.getSport();
		}
		if(course.getSubSport() == null){
			//this.activeSubSport = SubSportType.GENERIC;
			this.newSelectedSubSport = SubSportType.GENERIC_SUB;
		}
		else{
			//this.activeSubSport = course.getSubSport();
			this.newSelectedSubSport = course.getSubSport();
		}
		isCourse = true;
		isActivity = false;
		initComponents();
	}

	private void initComponents() {
		courseType = Messages.getMessage("course.label") + ": ";
		activityType = Messages.getMessage( "activity.label") + ": ";
		sportPanel = getSportPanel();
		dataPanel = new JPanel();
		cardLayout = new CardLayout();
		dataPanel.setLayout(cardLayout);
		dataPanel.add(sportPanel, PanelType.SPORT_PANEL.name());

		cardLayout.first(dataPanel);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		setTitle(Messages.getMessage("dialog.changeSportType.title"));

		JLabel lblDescription = new JLabel(Messages.getMessage("dialog.changeSportType.description"));

		JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				
				if(isActivity && !isCourse){
//					activity.setTemporarySport(newSelectedSport);
//					activity.setTemporarySubSport(newSelectedSubSport);
				}
				else{
					if(!isActivity && isCourse){
//						course.setTemporarySport(newSelectedSport);
//						course.setTemporarySubSport(newSelectedSubSport);
					}
				}
				
				SportDialog.this.dispose();
			}

		});

		JButton cmdCancel = new JButton(Messages.getMessage("trackIt.cmdCancel"));
		cmdCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if(isActivity && !isCourse){
					activity.setTemporarySport(SportType.GENERIC);
					activity.setTemporarySubSport(SubSportType.GENERIC_SUB);
				}
				else{
					if(!isActivity && isCourse){
//						course.setTemporarySport(SportType.GENERIC);
//						course.setTemporarySubSport(SubSportType.GENERIC_SUB);
					}
				}
				SportDialog.this.dispose();
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

	private JPanel getSportPanel() {
		JPanel panel = new JPanel();

		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);

		JLabel itemName = new JLabel("");
		if (isCourse && !isActivity) {
			itemName = new JLabel(courseType + course.getName());
		} else if (!isCourse && isActivity) {
			itemName = new JLabel(activityType + activity.getName());
		}

		final JLabel sportLabel = new JLabel(getMessage("applicationPanel.label.sport"));
		final JLabel subSportLabel = new JLabel(getMessage("applicationPanel.label.subSport"));

		String[] availableSports = DocumentManager.getInstance().getDatabase().getSports().toArray(new String[0]);
		final JComboBox<String> sportsChooser = new JComboBox<String>(availableSports);

		String[] availableSubSports = DocumentManager.getInstance().getDatabase().getSubSports(newSelectedSport).toArray(new String[0]);
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(availableSubSports);
		final JComboBox<String> subSportsChooser = new JComboBox<String>(model);
		
		
		sportsChooser.setSelectedItem(newSelectedSport.getName());
		subSportsChooser.setSelectedItem(newSelectedSubSport.getName());

		sportsChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent  e) {
				final String selectedSport = sportsChooser.getSelectedItem().toString();
				

				newSelectedSport = SportType.lookupByName(selectedSport);
				
				//subSportsChooser.removeAllItems();
				String[] availableSubSports = DocumentManager.getInstance().getDatabase().getSubSports(newSelectedSport).toArray(new String[0]);
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(availableSubSports);
				subSportsChooser.setModel(model);
				
				final String selectedSubSport = subSportsChooser.getSelectedItem().toString();
				newSelectedSubSport = SubSportType.lookupByName(selectedSubSport, newSelectedSport.getSportID());
				
			}
		});

		subSportsChooser.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final String selectedSubSport = subSportsChooser.getSelectedItem().toString();
				newSelectedSubSport = SubSportType.lookupByName(selectedSubSport, newSelectedSport.getSportID());
				
			}
		});
		
		
		

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(itemName)
						.addComponent(sportLabel).addComponent(sportsChooser).addComponent(subSportLabel)
						.addComponent(subSportsChooser)))
						// .addPreferredGap(ComponentPlacement.RELATED,
						// GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
		;

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(itemName).addComponent(sportLabel)
				.addComponent(sportsChooser).addComponent(subSportLabel).addComponent(subSportsChooser));
		return panel;
	}
	

	/*public ArrayList<String> getSports() {
		Connection c = null;
		Statement stmt = null;
		ArrayList<String> nameList = new ArrayList<String>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Name FROM Sport");
			while (rs.next()) {
				String sportName = new String(rs.getString("Name"));
				if (!nameList.contains(sportName))
					nameList.add(sportName);
			}
			rs.close();
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
		}
		return nameList;
	}

	public ArrayList<String> getSubSports() {
		Connection c = null;
		Statement stmt = null;
		ArrayList<String> nameList = new ArrayList<String>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Name FROM SubSport");
			while (rs.next()) {
				String subSportName = new String(rs.getString("Name"));
				if (!nameList.contains(subSportName))
					nameList.add(subSportName);
			}
			rs.close();
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
		}
		return nameList;
	}*/
	
	public Database getDatabase(){
		return DocumentManager.getInstance().getDatabase();
	}

}