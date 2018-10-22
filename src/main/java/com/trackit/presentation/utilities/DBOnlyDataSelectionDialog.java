/*
 * This file is part of Track It!.
 * Copyright (C) 2017 João Brisson Lopes
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
package com.trackit.presentation.utilities;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.xml.crypto.Data;

import com.trackit.TrackIt;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.database.Database;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DifficultyLevelType;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;
import com.trackit.business.domain.TrackConditionType;
import com.trackit.business.utilities.StringUtilities;

import java.util.Random;
import java.util.TreeMap;

public class DBOnlyDataSelectionDialog extends JDialog {
	
	public static final int YES = 1;
	public static final int NO  = 0;

	public static int showSelectionDialog( Activity activity, boolean showForAllCheckbox,
			                                                  boolean isChange) {
		int n = showSelectionDialog( activity.getName(), activity.getParent().getName(),
				                     activity.getSport(), activity.getSubSport(),
				                     activity.getDifficulty(), activity.getTrackCondition(),
				                     showForAllCheckbox, true);
		if ( n == DBOnlyDataSelectionDialog.NO )
			last.resetOnCancel();
		if ( isChange ) {
			activity.updateSportAndSubSport( last.selectedSport, last.selectedSubSport);
			activity.updateDifficulty( last.selectedDifficulty);
			activity.updateTrackCondition( last.selectedTrackCondition);
		}
		else {
			activity.setSportAndSubSport( last.selectedSport, last.selectedSubSport);
			activity.setDifficulty( last.selectedDifficulty);
			activity.setTrackCondition( last.selectedTrackCondition);
		}
		return n;
	}
	
	public static int showSelectionDialog( Course course, boolean showForAllCheckbox,
														  boolean isChange) {
		int n = showSelectionDialog( course.getName(), course.getParent().getName(),
									 course.getSport(), course.getSubSport(),
									 course.getDifficulty(), course.getTrackCondition(),
				                     showForAllCheckbox, false);
		if ( n == DBOnlyDataSelectionDialog.NO )
			last.resetOnCancel();
		if ( isChange ) {
			course.updateSportAndSubSport( last.selectedSport, last.selectedSubSport);
			course.updateDifficulty( last.selectedDifficulty);
			course.updateTrackCondition( last.selectedTrackCondition);
		}
		else {
			course.setSportAndSubSport( last.selectedSport, last.selectedSubSport);
			course.setDifficulty( last.selectedDifficulty);
			course.setTrackCondition( last.selectedTrackCondition);
		}
		return n;
	}
	
	public static boolean sameForAll() {
		return last.forAllSelected;
	}
	
	public static SportType selectedSport() {
		return last.selectedSport;
	}
	
	public static SubSportType selectedSubSport() {
		return last.selectedSubSport;
	}
	
	public static DifficultyLevelType selectedDifficultyLevel() {
		return last.selectedDifficulty;
	}
	
	public static TrackConditionType selectedTrackCondition() {
		return last.selectedTrackCondition;
	}
	
	private static int showSelectionDialog( String itemName, String parentName, 
											SportType currentSport, SubSportType currentSubSport,
											DifficultyLevelType difficulty,
											TrackConditionType trackCondition,
											boolean showForAllCheckbox, boolean isActivity) {
		last = new DBOnlyDataSelectionDialog( itemName, parentName, currentSport, currentSubSport,
										      difficulty, trackCondition,
				                              showForAllCheckbox, isActivity);
		return last.result;
	}
	
	private static DBOnlyDataSelectionDialog last = null;
	// provisional
//	private static Random random = new Random();
	
	private String       		itemName;
	private String       		parentName;
	private SportType    		currentSport,          selectedSport;
	private SubSportType 		currentSubSport,       selectedSubSport;
	private DifficultyLevelType currentDifficulty,     selectedDifficulty;
	private TrackConditionType  currentTrackCondition, selectedTrackCondition;
	private boolean      		isActivity;
	private boolean      		forAllSelected = false;
	private boolean      		showForAllCheckbox = false;
	private int          		result = NO;
	private char         		okChar, cancelChar;
	private JRootPane    		rootPane;
	
	public DBOnlyDataSelectionDialog( String itemName, String parentName, 
			SportType currentSport, SubSportType currentSubSport,
			DifficultyLevelType currentDifficulty, TrackConditionType currentCondition,
			boolean showForAllCheckbox, boolean isActivity) {
		this.itemName               = itemName;
		this.parentName             = parentName;
		this.selectedSport          = currentSport;
		this.selectedSubSport       = currentSubSport;
		this.selectedDifficulty     = currentDifficulty;
		this.selectedTrackCondition = currentCondition;
		this.showForAllCheckbox     = showForAllCheckbox;
		this.isActivity             = isActivity;
		
		if ( selectedSport == null )
			selectedSport = SportType.GENERIC;
		if ( selectedSubSport == null )
			selectedSubSport = SubSportType.GENERIC_SUB;
		if ( selectedDifficulty == null )
			selectedDifficulty = DifficultyLevelType.UNKNOWN;
		if ( selectedTrackCondition == null )
			selectedTrackCondition = TrackConditionType.UNKNOWN;
		
		this.currentSport          = this.selectedSport;
		this.currentSubSport       = this.selectedSubSport;
		this.currentDifficulty     = this.selectedDifficulty;
		this.currentTrackCondition = this.selectedTrackCondition;
		
		setTitle( Messages.getMessage( "dialog.dbOnlyTrackDataSelection.title"));
		initComponents();
	}
		
	private void initComponents() {
		
		JLabel heading        = new JLabel( Messages.getMessage( "dialog.dbOnlyTrackDataSelection.headline"));
		JLabel itemLabel      = new JLabel();
//	12335: 2018-02-26: control text width, new clear to read html table layout 
//		if ( isActivity )
//			itemLabel.setText( Messages.getMessage( "activity.label") + ": " + itemName);
//		else
//			itemLabel.setText( Messages.getMessage( "course.label") + ": " + itemName);
//		JLabel parentLabel    = new JLabel( Messages.getMessage( "gpsDocument.label") + ": " + parentName);
		itemLabel.setText(
			Messages.getMessage( "dialog.dbOnlyTrackDataSelection.itemData", 
					( isActivity ? Messages.getMessage( "activity.label"): 
								   Messages.getMessage( "course.label")) + ": ",
					StringUtilities.wrapString( itemName, 48, "<br>"),
					Messages.getMessage( "gpsDocument.label") + ": ",
					StringUtilities.wrapString( parentName, 48, "<br>")				));

		final SportSubSportComboBoxes comboBoxes = new SportSubSportComboBoxes(selectedSport, selectedSubSport);
		
		JLabel sportLabel = new JLabel( Messages.getMessage( "dialog.dbOnlyTrackDataSelection.sport.label"));
		final JComboBox<String> sportCombobox = comboBoxes.getSportsComboBox();
		
		JLabel subSportLabel  = new JLabel( Messages.getMessage( "dialog.dbOnlyTrackDataSelection.subSport.label"));
		final JComboBox<String> subSportCombobox = comboBoxes.getSubSportsComboBox();
		
		JLabel difficultyLabel = new JLabel( Messages.getMessage( "dialog.dbOnlyTrackDataSelection.difficulty.label"));
		List<DifficultyLevelType> difficulties = DifficultyLevelType.getList();
		difficulties.remove( 0);
		String[] difficultyLabels = new String[ difficulties.size()];
		for( int i=0; i<difficulties.size(); i++)
			difficultyLabels[i] = difficulties.get( i).toString();
		DefaultComboBoxModel<String> modelDifficulties = new DefaultComboBoxModel<>( difficultyLabels);
		final JComboBox<String> difficultyCombobox = new JComboBox<>(modelDifficulties);
		difficultyCombobox.setSelectedItem( selectedDifficulty.toString());
		
		JLabel conditionLabel = new JLabel( Messages.getMessage( "dialog.dbOnlyTrackDataSelection.condition.label"));
		List<TrackConditionType> conditions = TrackConditionType.getList();
		conditions.remove( 0);
		String[] conditionLabels = new String[ conditions.size()];
		for( int i=0; i<conditions.size(); i++)
			conditionLabels[i] = conditions.get( i).toString();
		DefaultComboBoxModel<String> modelCondition = new DefaultComboBoxModel<>( conditionLabels);
		final JComboBox<String> conditionCombobox = new JComboBox<>( modelCondition);
		conditionCombobox.setSelectedItem( selectedTrackCondition.toString());
		
		
//		sportCombobox.addActionListener( new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				String selection = sportCombobox.getSelectedItem().toString();
//				SportType newlySelectedSport = SportType.lookup( sportLabelsAndIds.get( selection));
//				if ( newlySelectedSport != selectedSport ) {
//					selectedSport = newlySelectedSport;
//					subSportsLabelsAndIds = SubSportType.toString( Database.getInstance().getSubSportsIds( selectedSport));
//					String [] availableSubSports = subSportsLabelsAndIds.keySet().toArray( new String[0]);
//					DefaultComboBoxModel<String> model = 
//							                new DefaultComboBoxModel<>( availableSubSports);
//					subSportCombobox.setModel( model);
//					selectedSubSport = SubSportType.lookup(
//							subSportsLabelsAndIds.get( subSportCombobox.getSelectedItem().toString())); 
//				}
//				
//			}
//		});
		
//		subSportCombobox.addActionListener( new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				String selection = subSportCombobox.getSelectedItem().toString();
//				SubSportType candidate = SubSportType.lookup( subSportsLabelsAndIds.get( selection));
//				if ( candidate != selectedSubSport )
//					selectedSubSport = candidate;
//			}
//		});
		
		difficultyCombobox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selection = difficultyCombobox.getSelectedItem().toString();
				DifficultyLevelType candidate = DifficultyLevelType.lookup(selection);
				if ( candidate != selectedDifficulty )
					selectedDifficulty = candidate;
			}
		});
		
		conditionCombobox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selection = conditionCombobox.getSelectedItem().toString();
				TrackConditionType candidate = TrackConditionType.lookup(selection);
				if ( candidate != selectedTrackCondition )
					selectedTrackCondition = candidate;
			}
		});
		
		
		AbstractAction eventListener = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				String first = e.getActionCommand().toLowerCase();
				if ( first.charAt(0) == KeyEvent.VK_ESCAPE ) {
					first = rootPane.getDefaultButton().getText().toLowerCase();
				}
				result = (first.charAt(0) == okChar) ? YES : NO;
				if ( !showForAllCheckbox && result == YES )
					forAllSelected = false;
				selectedSport = comboBoxes.getSport();
				selectedSubSport = comboBoxes.getSubSport();
				DBOnlyDataSelectionDialog.this.dispose();
//				JOptionPane.showMessageDialog( TrackIt.getApplicationFrame(), 
//						"Choice:  " + selectedSport.toString() + "  " + selectedSubSport.toString() +
//					"\n" + selectedSport.getName() + "   " + selectedSubSport.getName());
			}
		};
		
		int maxWidth  = 0;
		int maxHeight = 0;
		
		String messageText = Messages.getMessage( "messages.ok");
		okChar            = messageText.substring( 0, 1).toLowerCase().charAt(0);
		JButton cmdYes    = new JButton( eventListener);
		cmdYes.setText( messageText);
		cmdYes.setDisplayedMnemonicIndex(0);
		maxWidth  = Math.max( maxWidth, cmdYes.getPreferredSize().width);
		maxHeight = Math.max( maxHeight, cmdYes.getPreferredSize().height);
		
		messageText    = Messages.getMessage( "messages.cancel");
		cancelChar     = messageText.substring( 0, 1).toLowerCase().charAt(0);
		JButton cmdNo  = new JButton( eventListener);
		cmdNo.setText( messageText);
		cmdNo.setDisplayedMnemonicIndex( 0);
		maxWidth  = Math.max( maxWidth, cmdNo.getPreferredSize().width);
		maxHeight = Math.max( maxHeight, cmdNo.getPreferredSize().height);
		
		cmdYes.setPreferredSize( new Dimension( maxWidth, maxHeight));
		cmdNo.setPreferredSize( new Dimension( maxWidth, maxHeight));
		JButton left  = cmdYes;
		JButton right = cmdNo;
		if ( OperatingSystem.isMac() ) {
			left  = cmdNo;
			right = cmdYes;
		}

		JPanel buttonsPanel = new JPanel();
		FlowLayout flowlayout = new FlowLayout( FlowLayout.TRAILING);
		buttonsPanel.setLayout( flowlayout);
		buttonsPanel.add( left);
		buttonsPanel.add( right);
		
// 12335: 2018-02-26 - creates a fixed width panel for the comboboxes
		JPanel comboPanel = createComboBoxesPanel( sportLabel,      sportCombobox, 
												   subSportLabel,   subSportCombobox, 
												   difficultyLabel, difficultyCombobox, 
												   conditionLabel,  conditionCombobox);

		GridBagLayout gridLayout = new GridBagLayout();
		setLayout( gridLayout);
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.fill   = GridBagConstraints.VERTICAL;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.insets = new Insets( 15, 15, 15, 15);
		this.add( heading, constraints);
		
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridy = 1;
		constraints.insets = new Insets( 0, 15, 0, 15);
		this.add( itemLabel, constraints);
		
//		12335: 2018-02-26: control text width, new clear to read html table layout 
//		constraints.gridy = 2;
//		this.add( parentLabel, constraints);
		
// 12335: 2018-02-26 - using the new comboboxes panel
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridy = 3;
		constraints.insets = new Insets( 15, 15, 0, 15);
		this.add( comboPanel, constraints);

// 12335: 2018-02-26 - replaced by the comboboxes panel
//		constraints.gridy = 3;
//		constraints.insets = new Insets( 15, 15, 0, 15);
//		this.add( sportLabel, constraints);
//		
//		constraints.fill = GridBagConstraints.HORIZONTAL;
//		constraints.gridy = 4;
//		constraints.insets = new Insets( 0, 15, 0, 15);
//		this.add( sportCombobox, constraints);
//		
//		constraints.fill = GridBagConstraints.NONE;
//		constraints.gridy = 5;
//		constraints.insets = new Insets( 15, 15, 0, 15);
//		this.add( subSportLabel, constraints);
//		
//		constraints.fill = GridBagConstraints.HORIZONTAL;
//		constraints.gridy = 6;
//		constraints.insets = new Insets( 0, 15, 0, 15);
//		this.add( subSportCombobox, constraints);
		
//		constraints.gridy = 7;
//		constraints.insets = new Insets( 15, 15, 0, 15);
//		this.add( difficultyLabel, constraints);
//		
//		constraints.fill = GridBagConstraints.HORIZONTAL;
//		constraints.gridy = 8;
//		constraints.insets = new Insets( 0, 15, 0, 15);
//		this.add( difficultyCombobox, constraints);
//		
//		constraints.gridy = 9;
//		constraints.insets = new Insets( 15, 15, 0, 15);
//		this.add( conditionLabel, constraints);
//		
//		constraints.fill = GridBagConstraints.HORIZONTAL;
//		constraints.gridy = 10;
//		constraints.insets = new Insets( 0, 15, 0, 15);
//		this.add( conditionCombobox, constraints);

		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 11;
		constraints.gridwidth = 1;
		constraints.insets = new Insets( 15, 15, 15, 15);
		
		if ( showForAllCheckbox ) {
			JCheckBox box = new JCheckBox( Messages.getMessage( "dialog.dbOnlyTrackDataSelection.sameForAll.label"));
			box.addItemListener( new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					forAllSelected = ! forAllSelected;
				}
			});
			this.add( box, constraints);

		}
		
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 1;
		this.add( buttonsPanel, constraints);
		
		rootPane = getRootPane();

		rootPane.setDefaultButton( cmdNo);
		
		InputMap inputMap = rootPane.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = rootPane.getActionMap();
		inputMap.put( KeyStroke.getKeyStroke( Character.toString(okChar).toUpperCase().charAt(0), 
				                              InputEvent.SHIFT_DOWN_MASK), 
				       "uppercaseOK");
		actionMap.put( "uppercaseOK", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( okChar), 
				      "lowercaseOK");
		actionMap.put("lowercaseOK", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( Character.toString(cancelChar).toUpperCase().charAt(0),
				                              InputEvent.SHIFT_DOWN_MASK),
				       "uppercaseCancel");
		actionMap.put( "uppercaseCancel", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( cancelChar),
				       "lowercaseCancel");
		actionMap.put( "lowercaseCancel", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER,0), "enter");
		actionMap.put( "enter", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), "escape");
		actionMap.put( "escape", eventListener);
	
		pack();
		setModal( true);
		setResizable( false);
		setLocationRelativeTo( TrackIt.getApplicationFrame());
		this.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setVisible( true);
	}
	
	// 12335: 2018-02-26 - Creates a fixed width panel for the comboxes
	private JPanel createComboBoxesPanel( JLabel label1, JComboBox<String> comboBox1,
									JLabel label2, JComboBox<String> comboBox2,
									JLabel label3, JComboBox<String> comboBox3,
									JLabel label4, JComboBox<String> comboBox4 ) {
		JPanel test = new JPanel();
		GroupLayout grpLayout = new GroupLayout(test);
		test.setLayout( grpLayout);
		grpLayout.setAutoCreateGaps(true);
		grpLayout.setAutoCreateContainerGaps(true);
		
		comboBox1.setPreferredSize( new Dimension(  240, comboBox1.getPreferredSize().height));
		
		grpLayout.linkSize( SwingConstants.HORIZONTAL, comboBox1, comboBox2, comboBox3, comboBox4);
		
		GroupLayout.ParallelGroup hGroup = grpLayout.createParallelGroup( Alignment.LEADING);
		
		hGroup.addGroup( grpLayout.createSequentialGroup()
				.addGroup( grpLayout.createParallelGroup( Alignment.LEADING)
						.addComponent( label1)
						.addComponent( comboBox1)
						.addComponent( label2)
						.addComponent( comboBox2)
						.addComponent( label3)
						.addComponent( comboBox3)
						.addComponent( label4)
						.addComponent( comboBox4)
						)
				);
		grpLayout.setHorizontalGroup( hGroup);
		
		GroupLayout.SequentialGroup vGroup = grpLayout.createSequentialGroup();
		
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( label1));
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( comboBox1));
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( label2));
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( comboBox2));
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( label3));
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( comboBox3));
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( label4));
		vGroup.addGroup( grpLayout.createParallelGroup()
				.addComponent( comboBox4));
		
		grpLayout.setVerticalGroup( vGroup);
		
		return test;
	}
	
	private void resetOnCancel() {
		selectedSport          = currentSport;
		selectedSubSport       = currentSubSport;
		selectedDifficulty     = currentDifficulty;
		selectedTrackCondition = currentTrackCondition;
	}

}