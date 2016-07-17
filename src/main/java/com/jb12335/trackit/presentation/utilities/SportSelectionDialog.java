package com.jb12335.trackit.presentation.utilities;

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
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.OperatingSystem;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.pg58406.trackit.business.db.Database;

public class SportSelectionDialog extends JDialog {
	
	public static final int YES = 1;
	public static final int NO  = 0;

	public static int showSportSelectionDialog( Activity activity, boolean showForAllCheckbox) {
		return showSportSelectionDialog( activity.getName(), activity.getParent().getName(),
				                         activity.getSport(), activity.getSubSport(),
				                         showForAllCheckbox, true);
	}
	
	public static int showSportSelectionDialog( Course course, boolean showForAllCheckbox) {
		return showSportSelectionDialog( course.getName(), course.getParent().getName(),
										 course.getSport(), course.getSubSport(),
				                         showForAllCheckbox, false);
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
	
	private static int showSportSelectionDialog( String itemName, String parentName, 
			SportType currentSport, SubSportType currentSubSport,
			boolean showForAllCheckbox, boolean isActivity) {
		last = new SportSelectionDialog( itemName, parentName, currentSport, currentSubSport,
				                         showForAllCheckbox, isActivity);
		return last.result;
	}
	
	private static SportSelectionDialog last = null;
	
	private String       itemName;
	private String       parentName;
	private SportType    currentSport,    selectedSport;
	private SubSportType currentSubSport, selectedSubSport;
	private boolean      isActivity;
	private boolean      forAllSelected = false;
	private boolean      showForAllCheckbox = false;
	private int          result = NO;
	private char         okChar, cancelChar;
	private JRootPane    rootPane;
	
	public SportSelectionDialog( String itemName, String parentName, 
			SportType currentSport, SubSportType currentSubSport,
			boolean showForAllCheckbox, boolean isActivity) {
		this.itemName           = itemName;
		this.parentName         = parentName;
		this.currentSport       = currentSport;
		this.currentSubSport    = currentSubSport;
		this.selectedSport      = currentSport;
		this.selectedSubSport   = currentSubSport;
		this.showForAllCheckbox = showForAllCheckbox;
		this.isActivity         = isActivity;
		
		if ( selectedSport == null )
			selectedSport = SportType.GENERIC;
		if ( selectedSubSport == null )
			selectedSubSport = SubSportType.GENERIC_SUB;
		
		setTitle( Messages.getMessage( "dialog.sportSelection.title"));
		initComponents();
	}
		
	private void initComponents() {
		
		JLabel heading        = new JLabel( Messages.getMessage( "dialog.sportSelection.headline"));
		JLabel itemLabel      = new JLabel();
		if ( isActivity )
			itemLabel.setText( Messages.getMessage( "activity.label") + ": " + itemName);
		else
			itemLabel.setText( Messages.getMessage( "course.label") + ": " + itemName);
		JLabel parentLabel    = new JLabel( Messages.getMessage( "gpsDocument.label") + ": " + parentName);

		JLabel sportLabel     = new JLabel( Messages.getMessage( "dialog.sportSelection.sport.label"));
		String [] avalableSports = Database.getInstance().getSportsOrdered().toArray( new String[0]);
		final JComboBox<String> sportCombobox = new JComboBox<>( avalableSports);
		sportCombobox.setSelectedItem( selectedSport.getName());
		
		JLabel subSportLabel  = new JLabel( Messages.getMessage( "dialog.sportSelection.subSport.label"));
		String [] availableSubSports = Database.getInstance().getSubSportsOrdered( selectedSport).toArray(new String[0]);
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>( availableSubSports);
		final JComboBox<String> subSportCombobox = new JComboBox<>( model);
		
		sportCombobox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selection = sportCombobox.getSelectedItem().toString();
				SportType newlySelectedSport = SportType.lookupByName( selection);
				if ( newlySelectedSport != selectedSport ) {
					selectedSport = newlySelectedSport;
					String [] availableSubSports = Database.getInstance()
					        		.getSubSportsOrdered( selectedSport).toArray( new String[0]);
					DefaultComboBoxModel<String> model = 
							                new DefaultComboBoxModel<>( availableSubSports);
					subSportCombobox.setModel( model);
					selectedSubSport = SubSportType.lookupByName(
											subSportCombobox.getSelectedItem().toString(), 
											selectedSport.getSportID());
				}
				
			}
		});
		
		subSportCombobox.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selection = subSportCombobox.getSelectedItem().toString();
				SubSportType candidate = SubSportType.lookupByName( selection, selectedSport.getSportID());
				if ( candidate != selectedSubSport )
					selectedSubSport = candidate;
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
				SportSelectionDialog.this.dispose();
			}
		};
		
		String messageText = Messages.getMessage( "messages.ok");
		okChar            = messageText.substring( 0, 1).toLowerCase().charAt(0);
		JButton cmdYes    = new JButton( eventListener);
		cmdYes.setText( messageText);
		cmdYes.setDisplayedMnemonicIndex(0);		
		
		messageText    = Messages.getMessage( "messages.cancel");
		cancelChar     = messageText.substring( 0, 1).toLowerCase().charAt(0);
		JButton cmdNo  = new JButton( eventListener);
		cmdNo.setText( messageText);
		cmdNo.setDisplayedMnemonicIndex( 0);
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
		
		constraints.gridy = 2;
		this.add( parentLabel, constraints);
		
		constraints.gridy = 3;
		constraints.insets = new Insets( 15, 15, 0, 15);
		this.add( sportLabel, constraints);
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridy = 4;
		constraints.insets = new Insets( 0, 15, 0, 15);
		this.add( sportCombobox, constraints);
		
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 5;
		constraints.insets = new Insets( 15, 15, 0, 15);
		this.add( subSportLabel, constraints);
		
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridy = 6;
		constraints.insets = new Insets( 0, 15, 0, 15);
		this.add( subSportCombobox, constraints);

		constraints.fill = GridBagConstraints.NONE;
		constraints.gridy = 7;
		constraints.gridwidth = 1;
		constraints.insets = new Insets( 15, 15, 15, 15);
		if ( showForAllCheckbox ) {
			JCheckBox box = new JCheckBox( Messages.getMessage( "dialog.sportSelection.sameForAll.label"));
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
		this.setVisible( true);
	}

}
