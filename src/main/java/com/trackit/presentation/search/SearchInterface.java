/*
 * This file is part of Track It!.
 * Copyright (C) 2017 Diogo Xavier
 *           (C) 2018 João Brisson Lopes
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
package com.trackit.presentation.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.trackit.business.DocumentManager;
import com.trackit.business.common.Messages;
import com.trackit.business.dbsearch.DBSearch;
import com.trackit.business.dbsearch.DBSearchActivitiesAndCourses;
import com.trackit.business.dbsearch.DBSearchField;
import com.trackit.business.domain.AscentDescentType;
import com.trackit.business.domain.CircuitType;
import com.trackit.business.domain.DifficultyLevelType;
import com.trackit.business.domain.GeographicBoundingBox;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;
import com.trackit.business.domain.TrackConditionType;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.utilities.geo.GoogleGeolocationService;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;
import com.trackit.presentation.utilities.CircuitTypeComboBox;
import com.trackit.presentation.utilities.DifficultyLevelComboBox;
import com.trackit.presentation.utilities.SportSubSportComboBoxes;
import com.trackit.presentation.utilities.SynchroFormattedTextField;
import com.trackit.presentation.utilities.SynchroTextField;
import com.trackit.presentation.utilities.SynchronizedDateRangeChooser;
import com.trackit.presentation.utilities.SynchronizedRangeSlider;
import com.trackit.presentation.utilities.TrackConditionComboBox;

import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import java.awt.Insets;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.JComponent;

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.awt.event.ActionEvent;

public class SearchInterface extends JDialog implements EventPublisher{

	private static final long serialVersionUID = -2210927404921687555L;
	
	// Default values
	static private String			   defaultLocationName     = "";
	static private Double              defaultRange            = null;
	static private Double              defaultLatLon           = null;
	static private Date                defaultFromDate		   = null;
	static private Date                defaultToDate		   = null;
	static private int                 defaultMinimumDistance  =    0;
	static private int           	   defaultMaximumDistance  =  100;
	static private int                 defaultMinimumAscent    =    0;
	static private int           	   defaultMaximumAscent    = 5000;
	static private int                 defaultMinimumDescent   =    0;
	static private int           	   defaultMaximumDescent   = 5000;
	static private DifficultyLevelType defaultDifficulty       = DifficultyLevelType.UNKNOWN;
	static private TrackConditionType  defaultTrackCondition   = TrackConditionType.UNKNOWN;
	static private CircuitType    	   defaultCircuitType      = CircuitType.ALL;
	static private SportType		   defaultSport            = SportType.ALL;
	static private SubSportType	       defaultSubSport		   = SubSportType.ALL_SUB;
	static private boolean             defaultWithMedia        = false;
	static private boolean			   defaultAddToCollection  = false;
	
	// Status variables
	private String				     locationName			   = defaultLocationName;
	private Double					 selectedRange 			   = defaultRange;
	private String					 selectedSearchType        = Messages.getMessage( "search.dialog.simple");
	private String					 selectedSearchSport       = null;

	// Button panel elements
	private JButton 				 resetButton;
	private JButton 				 okButton;
	private JButton 				 cancelButton;
	private JCheckBox                addToCollection		   = new JCheckBox();
	
	// Simple Search tab elements **************************************************************************
	private JLabel				      nameLabelSimple;
	private SynchroTextField		  nameValueSimple		   = new SynchroTextField( defaultLocationName);
	private JLabel					  rangeLabelSimple;
	private SynchroFormattedTextField rangeSimple              = new SynchroFormattedTextField( null, 2);
	
	private JLabel					  distanceLabelSimple;
	private SynchronizedRangeSlider   distanceSliderSimple     = new SynchronizedRangeSlider( 0, 100);
	
	private JLabel                   difficultyLabelSimple;
	private DifficultyLevelComboBox  difficultyComboBoxSimple  = new DifficultyLevelComboBox( false);
	
	private JLabel 					 sportLabel;
	private JLabel 					 subSportLabel;
	private SportSubSportComboBoxes  comboBoxesSimples = 
										new SportSubSportComboBoxes( defaultSport, defaultSubSport, true);
	
	private JLabel 					  coordinatesLabelSimple;
	private JButton 				  btnNewButtonSimple;
	private JLabel                    minLatLabelSimple = new JLabel();
	private JLabel                    maxLatLabelSimple = new JLabel();
	private JLabel                    minLonLabelSimple = new JLabel();
	private JLabel                    maxLonLabelSimple = new JLabel();
	private SynchroFormattedTextField minLatValueSimple = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField maxLatValueSimple = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField minLonValueSimple = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField maxLonValueSimple = new SynchroFormattedTextField( null, 5);
	
	// Advanced Search tab elements ***********************************************************************
	private JLabel				      nameLabelAdvanced;
	private SynchroTextField		  nameValueAdvanced		     = new SynchroTextField();
	private JLabel					  rangeLabelAdvanced;
	private SynchroFormattedTextField rangeAdvanced              = new SynchroFormattedTextField(null,2);
	
	private JLabel                    dateFromLabelAdvanced;
	private JLabel                    dateToLabelAdvanced;
	private SynchronizedDateRangeChooser dateRangeAdvanced      = new SynchronizedDateRangeChooser();
	
	private JLabel                    distanceLabelAdvanced;
	private SynchronizedRangeSlider   distanceSliderAdvanced     = new SynchronizedRangeSlider( 0, 100);
	
	private JLabel					  ascentLabelAdvanced;
	private SynchronizedRangeSlider   ascentSliderAdvanced       = new SynchronizedRangeSlider( 0, 1000);
	
	private JLabel					  descentLabelAdvanced;
	private SynchronizedRangeSlider   descentSliderAdvanced      = new SynchronizedRangeSlider( 0, 1000);
	
	private JLabel                    difficultyLabelAdvanced;
	private DifficultyLevelComboBox   difficultyComboBoxAdvanced = new DifficultyLevelComboBox( false);
	
	private JLabel					conditionLabelAdvanced;
	private TrackConditionComboBox	conditionComboBoxAdvanced  = new TrackConditionComboBox( false);
	
	private JLabel 					circularLabel;
	private CircuitTypeComboBox     circuitComboBoxAdvanced    = new CircuitTypeComboBox();	
	
	private JLabel 					sportLabelAdvanced;
	private JLabel 					subSportLabelAdvanced;
	private SportSubSportComboBoxes comboBoxesAdvanced = new SportSubSportComboBoxes( null, null, true);
	
	private JLabel 					  coordinatesLabelAdvanced;
	private JButton 				  btnNewButtonAdvanced;
	private JLabel                    minLatLabelAdvanced = new JLabel();
	private JLabel                    maxLatLabelAdvanced = new JLabel();
	private JLabel                    minLonLabelAdvanced = new JLabel();
	private JLabel                    maxLonLabelAdvanced = new JLabel();
	private SynchroFormattedTextField minLatValueAdvanced = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField maxLatValueAdvanced = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField minLonValueAdvanced = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField maxLonValueAdvanced = new SynchroFormattedTextField( null, 5);
	
	private JLabel					  mediaLabelAdvanced;
	private JCheckBox				  notesCheckBoxAdvanced;
	private JCheckBox				  picturesCheckBoxAdvanced;
	private JCheckBox				  audioCheckBoxAdvanced;
	private JCheckBox				  videoCheckBoxAdvanced;
	
	// Search by Sport tab elements **********************************************************************

	private JLabel				      nameLabelSport;
	private SynchroTextField		  nameValueSport   = new SynchroTextField();
	private JLabel					  rangeLabelSport;
	private SynchroFormattedTextField rangeSport       = new SynchroFormattedTextField( null, 2);
	private JLabel 					  sportLabelSports;
	private JLabel 					  subSportLabelSports;
	private SportSubSportComboBoxes   comboBoxesSport  = new SportSubSportComboBoxes( null, null, true);
	
	private JLabel                       dateFromLabelSport;
	private JLabel                       dateToLabelSport;
	private SynchronizedDateRangeChooser dateRangeSport      = new SynchronizedDateRangeChooser();

	private JLabel					  distanceLabelSport;
	private SynchronizedRangeSlider   distanceSliderSport    = new SynchronizedRangeSlider( 0, 100);
	
	private JLabel					  ascentLabelSport;
	private SynchronizedRangeSlider   ascentSliderSport     = new SynchronizedRangeSlider( 0, 1000);
	
	private JLabel					  descentLabelSport;
	private SynchronizedRangeSlider   descentSliderSport     = new SynchronizedRangeSlider( 0, 1000);
	
	private JLabel					  descentLabelSportTwo;
	private SynchronizedRangeSlider   descentSliderSportTwo  = new SynchronizedRangeSlider( 0, 1000);
	
	private JLabel 					  coordinatesLabelSport;
	private JButton 				  btnNewButtonSport;
	private JLabel                    minLatLabelSport = new JLabel();
	private JLabel                    maxLatLabelSport = new JLabel();
	private JLabel                    minLonLabelSport = new JLabel();
	private JLabel                    maxLonLabelSport = new JLabel();
	private SynchroFormattedTextField minLatValueSport = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField maxLatValueSport = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField minLonValueSport = new SynchroFormattedTextField( null, 5);
	private SynchroFormattedTextField maxLonValueSport = new SynchroFormattedTextField( null, 5);
	
	// General (One) -------------------------------------------------------------------------------------
	private JLabel                  difficultyLabelBySport;
	private DifficultyLevelComboBox difficultyComboBoxBySport      = new DifficultyLevelComboBox( false);
	
	private JLabel					conditionLabelBySport;
	private TrackConditionComboBox	conditionComboBoxBySport  = new TrackConditionComboBox( false);

	// Two ----------------------------------------------------------------------------------------------
	private JLabel					conditionLabelBySportTwo;
	private TrackConditionComboBox	conditionComboBoxBySportTwo  = new TrackConditionComboBox( false);
	// Three --------------------------------------------------------------------------------------------
	private JLabel                  difficultyLabelBySportThree;
	private DifficultyLevelComboBox difficultyComboBoxBySportThree   = new DifficultyLevelComboBox( false);
	// Four ---------------------------------------------------------------------------------------------
	private JLabel                  difficultyLabelBySportFour;
	private DifficultyLevelComboBox difficultyComboBoxBySportFour = new DifficultyLevelComboBox( false);
	
	private JLabel					conditionLabelBySportFour;
	private TrackConditionComboBox	conditionComboBoxBySportFour  = new TrackConditionComboBox( false);
	
	private final JPanel contentPanel       = new JPanel();
	private JTabbedPane  tabsPane;
	private JPanel       panel              = new JPanel();
	private JPanel       panelAdvanced      = new JPanel();
	private CardLayout   cardLayout         = new CardLayout();
	private JPanel       bySportPane        = new JPanel();
	private JPanel       bySportPrincipal   = new JPanel();
	private JPanel       bySportCard        = new JPanel(cardLayout);
	private JPanel       bySportOneCard     = new JPanel();
	private JPanel       bySportTwoCard     = new JPanel();
	private JPanel       bySportThreeCard   = new JPanel();
	private JPanel       bySportFourCard    = new JPanel();
	private JPanel       bySportPreCard     = new JPanel();
	
	
	private JTextField aux = new JTextField();
	
	private static SearchInterface last = null;		//12335: 2018-03-05 - make sure it is set to null
	private static DBSearch        dbSearch;
	
	private static GoogleGeolocationService geoLocationService = new GoogleGeolocationService();
		
	private ActionListener getCoordinatesButtonsHandler = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setVisible( false);
			EventManager.getInstance().publish( SearchInterface.this, Event.GET_COORDINATES, null);
		}
	};
	
	protected SearchInterface() {
		dbSearch = DBSearch.getInstance();
		try{
			initComponents();
			this.setVisible(true);
			this.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE);
			final SearchInterface thisInterface = this; 
			this.addWindowListener( new WindowListener() {
				@Override
				public void windowOpened(WindowEvent e) {
				}				
				@Override
				public void windowIconified(WindowEvent e) {
				}
				@Override
				public void windowDeiconified(WindowEvent e) {
				}
				@Override
				public void windowDeactivated(WindowEvent e) {
				}
				@Override
				public void windowClosing(WindowEvent e) {
					thisInterface.setVisible( false);
				}
				@Override
				public void windowClosed(WindowEvent e) {
				}				
				@Override
				public void windowActivated(WindowEvent e) {
				}
			});
		} catch (Exception e){
			e.printStackTrace();
		}
		last = this;		
		setSynchronizationOn();
		resetUI();
	}
	
	public static void setLocale() {
		if ( last != null) {
			last.setLabelsOfLocale();
		}
	}
	
	
	public void initComponents(){
		
		this.setTitle( Messages.getMessage( "search.dialog.title"));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
//		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setBorder( BorderFactory.createEtchedBorder());

		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabsPane = tabbedPane;
			
			getContentPane().add(tabbedPane, BorderLayout.PAGE_START);
			{	
				setBounds(100, 100, 500, 750);
				panel.setName(Messages.getMessage("search.dialog.simple"));
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
				panel.setLayout(gbl_panel);
				panel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED));
				tabbedPane.addTab(Messages.getMessage("search.dialog.simple"), null, panel, null);
				simpleInterface();
			}
			{
				setBounds(100, 100, 500, 750);
				panelAdvanced.setName(Messages.getMessage("search.dialog.advanced"));
				GridBagLayout gbl_panelAdvanced = new GridBagLayout();
				gbl_panelAdvanced.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
				gbl_panelAdvanced.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
				gbl_panelAdvanced.columnWeights = new double[]{};
				gbl_panelAdvanced.rowWeights = new double[]{};
				panelAdvanced.setLayout(gbl_panelAdvanced);
				panelAdvanced.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED));
				tabbedPane.addTab(Messages.getMessage("search.dialog.advanced"), null, panelAdvanced, null);
				advancedUI();
			}
			{
				bySportPane.setName(Messages.getMessage("search.dialog.bySport"));
				bySportPane.setLayout(new BorderLayout());
				bySportPane.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED));
				tabbedPane.addTab(Messages.getMessage("search.dialog.bySport"), null, bySportPane, null);
				bySportUI();
			}
			tabbedPane.getModel().addChangeListener(new ChangeListener(){
				@Override
		         public void stateChanged(ChangeEvent e) {
		            JComponent panel = (JComponent) tabbedPane.getSelectedComponent();
		            if(panel.getClass().equals(JPanel.class)){
		            	if(panel.getName().equals(Messages.getMessage("search.dialog.simple"))){
		            		selectedSearchType =  panel.getName();
		            	}
		            	if(panel.getName().equals(Messages.getMessage("search.dialog.advanced"))){
		            		selectedSearchType =  panel.getName();
		            	}
		            	if(panel.getName().equals(Messages.getMessage("search.dialog.bySport"))){
		            		selectedSearchType =  panel.getName();
		            	}
		            }
		         }
			});
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
			buttonPane.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED));
			buttonPane.add( Box.createRigidArea( new Dimension( 20, 40)));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				resetButton = new JButton( Messages.getMessage( "search.dialog.reset"));
				resetButton.setActionCommand("Reset");
				buttonPane.add(resetButton);
				resetButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						resetUI();
					}
				});
			}
				buttonPane.add( Box.createHorizontalGlue());
				addToCollection.setSelected( defaultAddToCollection);
				addToCollection.setText( Messages.getMessage( "search.dialog.addToCollection"));
				addToCollection.setToolTipText( Messages.getMessage( "search.dialog.addToCollectionToolTip"));
				addToCollection.setEnabled( false); 			//12335: 2018-07-08
				buttonPane.add( addToCollection);
			{
				okButton = new JButton(Messages.getMessage("search.dialog.search"));				
				okButton.setActionCommand("Search");
				buttonPane.add(Box.createHorizontalGlue());
				buttonPane.add(okButton);
				okButton.addActionListener(new ActionListener() { 
				    public void actionPerformed(ActionEvent e) { 
				        okButton();
				    } 
				});
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = 	new JButton(Messages.getMessage("search.dialog.cancel"));
				cancelButton.setActionCommand("Cancel");
				buttonPane.add( Box.createRigidArea( new Dimension( 20, 30)));
				buttonPane.add(cancelButton);
				buttonPane.add( Box.createRigidArea( new Dimension( 20, 30)));
				cancelButton.addActionListener(new ActionListener (){
					public void actionPerformed(ActionEvent e){
						SearchInterface interfaces = SearchInterface.getInstance();
						interfaces.setVisible(false);
					}
				});
			}
		}
	}
	
	private void setSynchronizationOn() {
		
		nameValueSimple.startSynchronizing( nameValueAdvanced);
		nameValueSimple.startSynchronizing( nameValueSport);
		rangeSimple.startSynchronizing( rangeAdvanced);
		rangeSimple.startSynchronizing( rangeSport);
		rangeSimple.setLimits( 0., 250.);
		
		ActionListener locationListenerSimple = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleLocationSearch( nameValueSimple.getText(), rangeSimple.getDouble());
			}
		};
		nameValueSimple.addActionListener( locationListenerSimple);
		rangeSimple.addActionListener(     locationListenerSimple);
		
		PropertyChangeListener propertyListenerSimple = new PropertyChangeListener() {		
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( "editValid".equals( evt.getPropertyName()) && rangeSimple.getDouble() == null )
					handleLocationSearch( nameValueSimple.getText(), rangeSimple.getDouble());
			}
		};
		rangeSimple.addPropertyChangeListener( propertyListenerSimple);
		
		ActionListener locationListenerAdvanced = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleLocationSearch( nameValueAdvanced.getText(), rangeAdvanced.getDouble());
			}
		};
		nameValueAdvanced.addActionListener( locationListenerAdvanced);
		rangeAdvanced.addActionListener(     locationListenerAdvanced);
		
		PropertyChangeListener propertyListenerAdvanced = new PropertyChangeListener() {		
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( "editValid".equals( evt.getPropertyName()) && rangeAdvanced.getDouble() == null )
					handleLocationSearch( nameValueAdvanced.getText(), rangeAdvanced.getDouble());
			}
		};
		rangeAdvanced.addPropertyChangeListener( propertyListenerAdvanced);
		
		ActionListener locationListenerSport = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleLocationSearch( nameValueSport.getText(), rangeSport.getDouble());
			}
		};
		nameValueSport.addActionListener(    locationListenerSport);
		rangeSport.addActionListener(        locationListenerSport);
		
		PropertyChangeListener propertyListenerSport = new PropertyChangeListener() {		
			@Override
			public void propertyChange(PropertyChangeEvent evt) {	
				if ( "editValid".equals( evt.getPropertyName()) && rangeSport.getDouble() == null )
					handleLocationSearch( nameValueSport.getText(), rangeSport.getDouble());
			}
		};
		rangeSport.addPropertyChangeListener( propertyListenerSport);
		
		minLatValueAdvanced.startSynchronizing( minLatValueSimple);
		maxLatValueAdvanced.startSynchronizing( maxLatValueSimple);
		minLonValueAdvanced.startSynchronizing( minLonValueSimple);
		maxLonValueAdvanced.startSynchronizing( maxLonValueSimple);
		minLatValueAdvanced.startSynchronizing( minLatValueSport);
		maxLatValueAdvanced.startSynchronizing( maxLatValueSport);
		minLonValueAdvanced.startSynchronizing( minLonValueSport);
		maxLonValueAdvanced.startSynchronizing( maxLonValueSport);
		minLatValueSimple.setLimits(  -90.,   90.);
		maxLatValueSimple.setLimits(  -90.,   90.);
		minLonValueSimple.setLimits( -180.,  180.);
		maxLonValueSimple.setLimits( -180.,  180.);
		
		dateRangeAdvanced.startSynchronizing( dateRangeSport);
		
		distanceSliderSimple.startSynchronizing( distanceSliderAdvanced);
		distanceSliderSimple.startSynchronizing( distanceSliderSport);
		
		ascentSliderAdvanced.startSynchronizing( ascentSliderSport);
		
		descentSliderAdvanced.startSynchronizing( descentSliderSport);
		descentSliderAdvanced.startSynchronizing( descentSliderSportTwo);
		
		difficultyComboBoxSimple.startSynchronizing( difficultyComboBoxAdvanced);
		difficultyComboBoxSimple.startSynchronizing( difficultyComboBoxBySport);
		difficultyComboBoxSimple.startSynchronizing( difficultyComboBoxBySportThree);
		difficultyComboBoxSimple.startSynchronizing( difficultyComboBoxBySportFour);
		
		conditionComboBoxAdvanced.startSynchronizing( conditionComboBoxBySport);
		conditionComboBoxAdvanced.startSynchronizing( conditionComboBoxBySportTwo);
		conditionComboBoxAdvanced.startSynchronizing( conditionComboBoxBySportFour);
		
		comboBoxesSimples.startSynchronizing( comboBoxesAdvanced);
		comboBoxesSimples.startSynchronizing( comboBoxesSport);
		comboBoxesSport.showAllSubSport( false);
	}
	
	private void simpleInterface(){
		{
			nameLabelSimple = new JLabel(Messages.getMessage("search.dialog.name"));
			GridBagConstraints gbc_nameLabelSimple = new GridBagConstraints();
			gbc_nameLabelSimple.insets = new Insets(15, 15, 5, 5);
			gbc_nameLabelSimple.anchor = GridBagConstraints.WEST;
			gbc_nameLabelSimple.gridx = 0;
			gbc_nameLabelSimple.gridy = 0;
			panel.add(nameLabelSimple, gbc_nameLabelSimple);
		}
		{
			GridBagConstraints gbc_nameValueSimple = new GridBagConstraints();
			gbc_nameValueSimple.gridwidth = 3;
			gbc_nameValueSimple.insets = new Insets(15, 15, 5, 15);
			gbc_nameValueSimple.fill = GridBagConstraints.HORIZONTAL;
			gbc_nameValueSimple.gridx = 2;
			gbc_nameValueSimple.gridy = 0;
			nameValueSimple.setColumns(10);
			panel.add(nameValueSimple, gbc_nameValueSimple);
			
		}
		{
			rangeLabelSimple = new JLabel(Messages.getMessage("search.dialog.range"));
			GridBagConstraints gbc_rangeLabelSimple = new GridBagConstraints();
			gbc_rangeLabelSimple.insets = new Insets(5, 15, 5, 5);
			gbc_rangeLabelSimple.anchor = GridBagConstraints.WEST;
			gbc_rangeLabelSimple.gridx = 0;
			gbc_rangeLabelSimple.gridy = 1;
			panel.add(rangeLabelSimple, gbc_rangeLabelSimple);
		}
		{
			GridBagConstraints gbc_RangeSimple = new GridBagConstraints();
			gbc_RangeSimple.gridwidth = 3;
			gbc_RangeSimple.insets = new Insets(5, 15, 5, 15);
			gbc_RangeSimple.fill = GridBagConstraints.HORIZONTAL;
			gbc_RangeSimple.gridx = 2;
			gbc_RangeSimple.gridy = 1;
			rangeSimple.setColumns(10);
			panel.add(rangeSimple, gbc_RangeSimple);
		}
		{
			distanceLabelSimple = new JLabel(Messages.getMessage("search.dialog.distance"));
			GridBagConstraints gbc_distanceLabel = new GridBagConstraints();
			gbc_distanceLabel.anchor = GridBagConstraints.WEST;
			gbc_distanceLabel.insets = new Insets(5, 15, 5, 5);
			gbc_distanceLabel.gridx = 0;
			gbc_distanceLabel.gridy = 2;
			panel.add(distanceLabelSimple, gbc_distanceLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxVal = new GridBagConstraints();
			gbc_maxVal.insets = new Insets(5, 15, 5, 15);
			gbc_maxVal.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxVal.gridx = 4;
			gbc_maxVal.gridy = 2;
			panel.add( distanceSliderSimple.getUpperEndTextField(), gbc_maxVal);
		}
		{
			GridBagConstraints gbc_minVal = new GridBagConstraints();
			gbc_minVal.anchor = GridBagConstraints.WEST;
			gbc_minVal.insets = new Insets(5, 15, 5, 5);
			gbc_minVal.gridx = 2;
			gbc_minVal.gridy = 2;
			panel.add( distanceSliderSimple.getLowerEndTextField(), gbc_minVal);
		}
		{
			distanceSliderSimple.setMinimumSize( new Dimension( 180, 15));
			GridBagConstraints gbc_rangeSlider = new GridBagConstraints();
			gbc_rangeSlider.fill = GridBagConstraints.HORIZONTAL;
			gbc_rangeSlider.insets = new Insets(5, 15, 5, 5);
			gbc_rangeSlider.gridx = 3;
			gbc_rangeSlider.gridy = 2;
			panel.add( distanceSliderSimple, gbc_rangeSlider);
			
		}
		{
			difficultyLabelSimple = new JLabel(Messages.getMessage("search.dialog.difficultyLevel"));
			GridBagConstraints gbc_difficultyLabel = new GridBagConstraints();
			gbc_difficultyLabel.anchor = GridBagConstraints.WEST;
			gbc_difficultyLabel.insets = new Insets(5, 15, 5, 5);
			gbc_difficultyLabel.gridx = 0;
			gbc_difficultyLabel.gridy = 3;
			panel.add(difficultyLabelSimple, gbc_difficultyLabel);
		}
		{
			GridBagConstraints gbc_difficultyBox = new GridBagConstraints();
			gbc_difficultyBox.gridwidth = 3;
			gbc_difficultyBox.insets = new Insets(5, 15, 5, 15);
			gbc_difficultyBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_difficultyBox.gridx = 2;
			gbc_difficultyBox.gridy = 3;
			panel.add( difficultyComboBoxSimple, gbc_difficultyBox);
		}
		{
			/*JLabel*/ sportLabel = new JLabel(Messages.getMessage("search.dialog.sport"));
			GridBagConstraints gbc_sportLabel = new GridBagConstraints();
			gbc_sportLabel.anchor = GridBagConstraints.WEST;
			gbc_sportLabel.insets = new Insets(5, 15, 5, 5);
			gbc_sportLabel.gridx = 0;
			gbc_sportLabel.gridy = 4;
			panel.add(sportLabel, gbc_sportLabel);
		}
		{
			/*JLabel*/ subSportLabel = new JLabel(Messages.getMessage("search.dialog.subSport"));
			GridBagConstraints gbc_subSportLabel = new GridBagConstraints();
			gbc_subSportLabel.anchor = GridBagConstraints.WEST;
			gbc_subSportLabel.insets = new Insets(5, 15, 5, 5);
			gbc_subSportLabel.gridx = 0;
			gbc_subSportLabel.gridy = 5;
			panel.add(subSportLabel, gbc_subSportLabel);
		}
		{
			GridBagConstraints gbc_sportBox = new GridBagConstraints();
			gbc_sportBox.gridwidth = 3;
			gbc_sportBox.insets = new Insets(5, 15, 5, 15);
			gbc_sportBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_sportBox.gridx = 2;
			gbc_sportBox.gridy = 4;
			panel.add( comboBoxesSimples.getSportsComboBox(), gbc_sportBox);
			GridBagConstraints gbc_subSportBox = new GridBagConstraints();
			gbc_subSportBox.gridwidth = 3;
			gbc_subSportBox.insets = new Insets(5, 15, 5, 15);
			gbc_subSportBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_subSportBox.gridx = 2;
			gbc_subSportBox.gridy = 5;
			panel.add( comboBoxesSimples.getSubSportsComboBox(), gbc_subSportBox);
		}
		
		coordinatesLabelSimple = new JLabel(Messages.getMessage("search.dialog.coordinates"));
		btnNewButtonSimple     = new JButton(Messages.getMessage("search.dialog.getCoordinates"));
		btnNewButtonSimple.addActionListener( getCoordinatesButtonsHandler);
		GridBagConstraints gbc_btnNewButtonSimple = new GridBagConstraints();
		gbc_btnNewButtonSimple.anchor = GridBagConstraints.WEST;
		gbc_btnNewButtonSimple.insets = new Insets(0, 15, 0, 5);
		gbc_btnNewButtonSimple.gridx = 0;
		gbc_btnNewButtonSimple.gridy = 7;
		panel.add(btnNewButtonSimple, gbc_btnNewButtonSimple);
		
		GridBagConstraints gbc_coordinatesLabelSimple = new GridBagConstraints();
		gbc_coordinatesLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_coordinatesLabelSimple.insets = new Insets(5, 15, 5, 5);
		gbc_coordinatesLabelSimple.gridx = 0;
		gbc_coordinatesLabelSimple.gridy = 6;
		panel.add(coordinatesLabelSimple, gbc_coordinatesLabelSimple);
		
		GridBagConstraints gbc_minLatLabelSimple = new GridBagConstraints();
		gbc_minLatLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_minLatLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_minLatLabelSimple.gridx = 2;
		gbc_minLatLabelSimple.gridy = 6;
		
		GridBagConstraints gbc_minLatValueSimple = new GridBagConstraints();
		gbc_minLatValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_minLatValueSimple.gridwidth = 2;
		gbc_minLatValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLatValueSimple.gridx = 3;
		gbc_minLatValueSimple.gridy = 6;
		minLatValueSimple.setColumns(10);
		
		GridBagConstraints gbc_maxLatLabelSimple = new GridBagConstraints();
		gbc_maxLatLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_maxLatLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_maxLatLabelSimple.gridx = 2;
		gbc_maxLatLabelSimple.gridy = 7;
		
		GridBagConstraints gbc_maxLatValueSimple = new GridBagConstraints();
		gbc_maxLatValueSimple.gridwidth = 2;
		gbc_maxLatValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_maxLatValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLatValueSimple.gridx = 3;
		gbc_maxLatValueSimple.gridy = 7;
		maxLatValueSimple.setColumns(10);
		
		GridBagConstraints gbc_minLonLabelSimple = new GridBagConstraints();
		gbc_minLonLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_minLonLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_minLonLabelSimple.gridx = 2;
		gbc_minLonLabelSimple.gridy = 8;
		
		GridBagConstraints gbc_minLonValueSimple = new GridBagConstraints();
		gbc_minLonValueSimple.gridwidth = 2;
		gbc_minLonValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_minLonValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLonValueSimple.gridx = 3;
		gbc_minLonValueSimple.gridy = 8;
		minLonValueSimple.setColumns(10);
		
		GridBagConstraints gbc_maxLonLabelSimple = new GridBagConstraints();
		gbc_maxLonLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_maxLonLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_maxLonLabelSimple.gridx = 2;
		gbc_maxLonLabelSimple.gridy = 9;
		
		GridBagConstraints gbc_maxLonValueSimple = new GridBagConstraints();
		gbc_maxLonValueSimple.gridwidth = 2;
		gbc_maxLonValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_maxLonValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLonValueSimple.gridx = 3;
		gbc_maxLonValueSimple.gridy = 9;
		maxLonValueSimple.setColumns(10);
		
		maxLatLabelSimple.setText(Messages.getMessage("search.dialog.maxlat"));
		minLatLabelSimple.setText(Messages.getMessage("search.dialog.minlat"));
		maxLonLabelSimple.setText(Messages.getMessage("search.dialog.maxlon"));
		minLonLabelSimple.setText(Messages.getMessage("search.dialog.minlon"));
		panel.add(maxLatLabelSimple, gbc_maxLatLabelSimple);
		panel.add(minLatLabelSimple, gbc_minLatLabelSimple);
		panel.add(minLonLabelSimple, gbc_minLonLabelSimple);
		panel.add(maxLonLabelSimple, gbc_maxLonLabelSimple);
		panel.add(maxLonValueSimple, gbc_maxLonValueSimple);
		panel.add(minLatValueSimple, gbc_minLatValueSimple);
		panel.add(minLonValueSimple, gbc_minLonValueSimple);
		panel.add(maxLatValueSimple, gbc_maxLatValueSimple);
	}
	
	private void advancedUI(){
		{
			nameLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.name"));
			GridBagConstraints gbc_nameLabel = new GridBagConstraints();
			gbc_nameLabel.fill = GridBagConstraints.VERTICAL;
			gbc_nameLabel.insets = new Insets(15, 15, 5, 5);
			gbc_nameLabel.anchor = GridBagConstraints.WEST;
			gbc_nameLabel.gridx = 0;
			gbc_nameLabel.gridy = 0;
			panelAdvanced.add( nameLabelAdvanced, gbc_nameLabel);
		}
		{
			GridBagConstraints gbc_nameValue = new GridBagConstraints();
			gbc_nameValue.gridwidth = 3;
			gbc_nameValue.insets = new Insets(15, 15, 5, 15);
			gbc_nameValue.fill = GridBagConstraints.HORIZONTAL;
			gbc_nameValue.gridx = 2;
			gbc_nameValue.gridy = 0;
			panelAdvanced.add(nameValueAdvanced, gbc_nameValue);
			nameValueAdvanced.setColumns(10);
		}
		{
			rangeLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.range"));
			GridBagConstraints gbc_rangeLabelAdvanced = new GridBagConstraints();
			gbc_rangeLabelAdvanced.insets = new Insets(5, 15, 5, 15);
			gbc_rangeLabelAdvanced.anchor = GridBagConstraints.WEST;
			gbc_rangeLabelAdvanced.gridx = 0;
			gbc_rangeLabelAdvanced.gridy = 1;
			panelAdvanced.add(rangeLabelAdvanced, gbc_rangeLabelAdvanced);
		}
		{
			rangeAdvanced.setText( "");
			GridBagConstraints gbc_rangeAdvanced = new GridBagConstraints();
			gbc_rangeAdvanced.gridwidth = 3;
			gbc_rangeAdvanced.insets = new Insets(5, 15, 5, 15);
			gbc_rangeAdvanced.fill = GridBagConstraints.HORIZONTAL;
			gbc_rangeAdvanced.gridx = 2;
			gbc_rangeAdvanced.gridy = 1;
			rangeAdvanced.setColumns(10);
			panelAdvanced.add(rangeAdvanced, gbc_rangeAdvanced);
		}
		dateFromLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.minDate"));
		GridBagConstraints gbc_JDateLabelMin = new GridBagConstraints();
		gbc_JDateLabelMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateLabelMin.insets = new Insets(5, 15, 5, 15);
		gbc_JDateLabelMin.gridx = 0;
		gbc_JDateLabelMin.gridy = 2;
		
		GridBagConstraints gbc_JDateChooserMin = new GridBagConstraints();
		gbc_JDateChooserMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateChooserMin.insets = new Insets(5, 15, 5, 15);
		gbc_JDateChooserMin.gridx = 2;
		gbc_JDateChooserMin.gridy = 2;
		gbc_JDateChooserMin.gridwidth = 3;
		panelAdvanced.add( dateFromLabelAdvanced, gbc_JDateLabelMin);
		panelAdvanced.add( dateRangeAdvanced.getFromDateChooser(), gbc_JDateChooserMin);
		
		dateToLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.maxDate"));
		GridBagConstraints gbc_JDateLabelMax = new GridBagConstraints();
		gbc_JDateLabelMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateLabelMax.insets = new Insets(5, 15, 5, 15);
		gbc_JDateLabelMax.gridx = 0;
		gbc_JDateLabelMax.gridy = 3;
		
		GridBagConstraints gbc_JDateChooserMax = new GridBagConstraints();
		gbc_JDateChooserMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateChooserMax.insets = new Insets(5, 15, 5, 15);
		gbc_JDateChooserMax.gridx = 2;
		gbc_JDateChooserMax.gridy = 3;
		gbc_JDateChooserMax.gridwidth = 3;
		panelAdvanced.add( dateToLabelAdvanced, gbc_JDateLabelMax);
		panelAdvanced.add( dateRangeAdvanced.getToDateChooser(), gbc_JDateChooserMax);
		{
			distanceLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.distance"));
			GridBagConstraints gbc_distanceLabel = new GridBagConstraints();
			gbc_distanceLabel.anchor = GridBagConstraints.WEST;
			gbc_distanceLabel.insets = new Insets(5, 15, 5, 5);
			gbc_distanceLabel.gridx = 0;
			gbc_distanceLabel.gridy = 4;
			panelAdvanced.add( distanceLabelAdvanced, gbc_distanceLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxVal = new GridBagConstraints();
			gbc_maxVal.insets = new Insets(5, 15, 5, 15);
			gbc_maxVal.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxVal.gridx = 4;
			gbc_maxVal.gridy = 4;
			panelAdvanced.add( distanceSliderAdvanced.getUpperEndTextField(), gbc_maxVal);
		}
		{
			GridBagConstraints gbc_minVal = new GridBagConstraints();
			gbc_minVal.anchor = GridBagConstraints.WEST;
			gbc_minVal.insets = new Insets(5, 15, 5, 5);
			gbc_minVal.gridx = 2;
			gbc_minVal.gridy = 4;
			panelAdvanced.add( distanceSliderAdvanced.getLowerEndTextField(), gbc_minVal);
		}
		{
			distanceSliderAdvanced.setMinimumSize( new Dimension(180,15));
			GridBagConstraints gbc_rangeSliderAdvanced = new GridBagConstraints();
			gbc_rangeSliderAdvanced.fill = GridBagConstraints.HORIZONTAL;
			gbc_rangeSliderAdvanced.insets = new Insets(5, 15, 5, 5);
			gbc_rangeSliderAdvanced.gridx = 3;
			gbc_rangeSliderAdvanced.gridy = 4;
			panelAdvanced.add( distanceSliderAdvanced, gbc_rangeSliderAdvanced);
		}
		{
			ascentLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.ascent"));
			GridBagConstraints gbc_ascentLabel = new GridBagConstraints();
			gbc_ascentLabel.anchor = GridBagConstraints.WEST;
			gbc_ascentLabel.insets = new Insets(5, 15, 5, 5);
			gbc_ascentLabel.gridx = 0;
			gbc_ascentLabel.gridy = 5;
			panelAdvanced.add( ascentLabelAdvanced, gbc_ascentLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxValAscent = new GridBagConstraints();
			gbc_maxValAscent.insets = new Insets(5, 15, 5, 15);
			gbc_maxValAscent.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxValAscent.gridx = 4;
			gbc_maxValAscent.gridy = 5;
			panelAdvanced.add( ascentSliderAdvanced.getUpperEndTextField(), gbc_maxValAscent);
		}
		{
			GridBagConstraints gbc_minValAscent = new GridBagConstraints();
			gbc_minValAscent.anchor = GridBagConstraints.WEST;
			gbc_minValAscent.insets = new Insets(5, 15, 5, 5);
			gbc_minValAscent.gridx = 2;
			gbc_minValAscent.gridy = 5;
			panelAdvanced.add( ascentSliderAdvanced.getLowerEndTextField(), gbc_minValAscent);
		}
		{
			GridBagConstraints gbc_ascentSliderAdvanced = new GridBagConstraints();
			gbc_ascentSliderAdvanced.fill = GridBagConstraints.HORIZONTAL;
			gbc_ascentSliderAdvanced.insets = new Insets(5, 15, 5, 5);
			gbc_ascentSliderAdvanced.gridx = 3;
			gbc_ascentSliderAdvanced.gridy = 5;
			panelAdvanced.add( ascentSliderAdvanced, gbc_ascentSliderAdvanced);
		}
		{
			descentLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.descent"));
			GridBagConstraints gbc_descentLabel = new GridBagConstraints();
			gbc_descentLabel.anchor = GridBagConstraints.WEST;
			gbc_descentLabel.insets = new Insets(5, 15, 5, 5);
			gbc_descentLabel.gridx = 0;
			gbc_descentLabel.gridy = 6;
			panelAdvanced.add (descentLabelAdvanced, gbc_descentLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxValDescent = new GridBagConstraints();
			gbc_maxValDescent.insets = new Insets(5, 15, 5, 15);
			gbc_maxValDescent.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxValDescent.gridx = 4;
			gbc_maxValDescent.gridy = 6;
			panelAdvanced.add( descentSliderAdvanced.getUpperEndTextField(), gbc_maxValDescent);
		}
		{
			GridBagConstraints gbc_minValDescent = new GridBagConstraints();
			gbc_minValDescent.anchor = GridBagConstraints.WEST;
			gbc_minValDescent.insets = new Insets(5, 15, 5, 5);
			gbc_minValDescent.gridx = 2;
			gbc_minValDescent.gridy = 6;
			panelAdvanced.add( descentSliderAdvanced.getLowerEndTextField(), gbc_minValDescent);
		}
		{
			GridBagConstraints gbc_rangeSliderDescent = new GridBagConstraints();
			gbc_rangeSliderDescent.fill = GridBagConstraints.HORIZONTAL;
			gbc_rangeSliderDescent.insets = new Insets(5, 15, 5, 5);
			gbc_rangeSliderDescent.gridx = 3;
			gbc_rangeSliderDescent.gridy = 6;
			panelAdvanced.add( descentSliderAdvanced, gbc_rangeSliderDescent);
		}
		{
			difficultyLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.difficultyLevel"));
			GridBagConstraints gbc_difficultyLabel = new GridBagConstraints();
			gbc_difficultyLabel.anchor = GridBagConstraints.WEST;
			gbc_difficultyLabel.insets = new Insets(5, 15, 5, 5);
			gbc_difficultyLabel.gridx = 0;
			gbc_difficultyLabel.gridy = 7;
			panelAdvanced.add(difficultyLabelAdvanced, gbc_difficultyLabel);
		}
		{
			GridBagConstraints gbc_difficultyBox = new GridBagConstraints();
			gbc_difficultyBox.gridwidth = 3;
			gbc_difficultyBox.insets = new Insets(5, 15, 5, 15);
			gbc_difficultyBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_difficultyBox.gridx = 2;
			gbc_difficultyBox.gridy = 7;
			panelAdvanced.add( difficultyComboBoxAdvanced, gbc_difficultyBox);
		}
		{
			conditionLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.state"));
			GridBagConstraints gbc_conditionLabel = new GridBagConstraints();
			gbc_conditionLabel.anchor = GridBagConstraints.WEST;
			gbc_conditionLabel.insets = new Insets(5, 15, 5, 5);
			gbc_conditionLabel.gridx = 0;
			gbc_conditionLabel.gridy = 8;
			panelAdvanced.add( conditionLabelAdvanced, gbc_conditionLabel);
		}
		{
			GridBagConstraints gbc_conditionBox = new GridBagConstraints();
			gbc_conditionBox.gridwidth = 3;
			gbc_conditionBox.insets = new Insets(5, 15, 5, 15);
			gbc_conditionBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_conditionBox.gridx = 2;
			gbc_conditionBox.gridy = 8;
			panelAdvanced.add( conditionComboBoxAdvanced, gbc_conditionBox);
		}
		{
			/*JLabel*/ sportLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.sport"));
			GridBagConstraints gbc_sportLabel = new GridBagConstraints();
			gbc_sportLabel.anchor = GridBagConstraints.WEST;
			gbc_sportLabel.insets = new Insets(5, 15, 5, 5);
			gbc_sportLabel.gridx = 0;
			gbc_sportLabel.gridy = 9;
			panelAdvanced.add(sportLabelAdvanced, gbc_sportLabel);
		}
		{
			subSportLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.subSport"));
			GridBagConstraints gbc_subSportLabel = new GridBagConstraints();
			gbc_subSportLabel.anchor = GridBagConstraints.WEST;
			gbc_subSportLabel.insets = new Insets(5, 15, 5, 5);
			gbc_subSportLabel.gridx = 0;
			gbc_subSportLabel.gridy = 10;
			panelAdvanced.add(subSportLabelAdvanced, gbc_subSportLabel);
		}
		{
			GridBagConstraints gbc_sportBox = new GridBagConstraints();
			gbc_sportBox.gridwidth = 3;
			gbc_sportBox.insets = new Insets(5, 15, 5, 15);
			gbc_sportBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_sportBox.gridx = 2;
			gbc_sportBox.gridy = 9;
			panelAdvanced.add( comboBoxesAdvanced.getSportsComboBox(), gbc_sportBox);
			GridBagConstraints gbc_subSportBox = new GridBagConstraints();
			gbc_subSportBox.gridwidth = 3;
			gbc_subSportBox.insets = new Insets(5, 15, 5, 15);
			gbc_subSportBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_subSportBox.gridx = 2;
			gbc_subSportBox.gridy = 10;
			panelAdvanced.add( comboBoxesAdvanced.getSubSportsComboBox(), gbc_subSportBox);
		}
		{
			circularLabel = new JLabel(Messages.getMessage("search.dialog.circular"));
			GridBagConstraints gbc_circularLabel = new GridBagConstraints();
			gbc_circularLabel.anchor = GridBagConstraints.WEST;
			gbc_circularLabel.insets = new Insets(5, 15, 5, 5);
			gbc_circularLabel.gridx = 0;
			gbc_circularLabel.gridy = 11;
			panelAdvanced.add(circularLabel, gbc_circularLabel);
		}
		{
			GridBagConstraints gbc_circularBox = new GridBagConstraints();
			gbc_circularBox.gridwidth = 3;
			gbc_circularBox.insets = new Insets(5, 15, 5, 15);
			gbc_circularBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_circularBox.gridx = 2;
			gbc_circularBox.gridy = 11;
			panelAdvanced.add( circuitComboBoxAdvanced, gbc_circularBox);
		}

		coordinatesLabelAdvanced = new JLabel(Messages.getMessage("search.dialog.coordinates"));
		btnNewButtonAdvanced = new JButton(Messages.getMessage("search.dialog.getCoordinates"));
		btnNewButtonAdvanced.addActionListener( getCoordinatesButtonsHandler);
		GridBagConstraints gbc_btnNewButtonAdvanced = new GridBagConstraints();
		gbc_btnNewButtonAdvanced.anchor = GridBagConstraints.WEST;
		gbc_btnNewButtonAdvanced.insets = new Insets(0, 15, 0, 5);
		gbc_btnNewButtonAdvanced.gridx = 0;
		gbc_btnNewButtonAdvanced.gridy = 13;
		panelAdvanced.add(btnNewButtonAdvanced, gbc_btnNewButtonAdvanced);
		
		GridBagConstraints gbc_coordinatesLabelAdvanced = new GridBagConstraints();
		gbc_coordinatesLabelAdvanced.anchor = GridBagConstraints.WEST;
		gbc_coordinatesLabelAdvanced.insets = new Insets(5, 15, 5, 5);
		gbc_coordinatesLabelAdvanced.gridx = 0;
		gbc_coordinatesLabelAdvanced.gridy = 12;
		panelAdvanced.add(coordinatesLabelAdvanced, gbc_coordinatesLabelAdvanced);
		
		GridBagConstraints gbc_minLatLabelAdvanced = new GridBagConstraints();
		gbc_minLatLabelAdvanced.anchor = GridBagConstraints.WEST;
		gbc_minLatLabelAdvanced.insets = new Insets(5, 15, 5, 15);
		gbc_minLatLabelAdvanced.gridx = 2;
		gbc_minLatLabelAdvanced.gridy = 12;
		
		GridBagConstraints gbc_minLatValueAdvanced = new GridBagConstraints();
		gbc_minLatValueAdvanced.insets = new Insets(5, 0, 5, 15);
		gbc_minLatValueAdvanced.gridwidth = 2;
		gbc_minLatValueAdvanced.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLatValueAdvanced.gridx = 3;
		gbc_minLatValueAdvanced.gridy = 12;
		minLatValueAdvanced.setColumns(10);
		
		GridBagConstraints gbc_maxLatLabelAdvanced = new GridBagConstraints();
		gbc_maxLatLabelAdvanced.anchor = GridBagConstraints.WEST;
		gbc_maxLatLabelAdvanced.insets = new Insets(5, 15, 5, 15);
		gbc_maxLatLabelAdvanced.gridx = 2;
		gbc_maxLatLabelAdvanced.gridy = 13;
		
		GridBagConstraints gbc_maxLatValueAdvanced = new GridBagConstraints();
		gbc_maxLatValueAdvanced.gridwidth = 2;
		gbc_maxLatValueAdvanced.insets = new Insets(5, 0, 5, 15);
		gbc_maxLatValueAdvanced.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLatValueAdvanced.gridx = 3;
		gbc_maxLatValueAdvanced.gridy = 13;
		maxLatValueAdvanced.setColumns(10);
		
		GridBagConstraints gbc_minLonLabelAdvanced = new GridBagConstraints();
		gbc_minLonLabelAdvanced.anchor = GridBagConstraints.WEST;
		gbc_minLonLabelAdvanced.insets = new Insets(5, 15, 5, 15);
		gbc_minLonLabelAdvanced.gridx = 2;
		gbc_minLonLabelAdvanced.gridy = 14;
		
		GridBagConstraints gbc_minLonValueAdvanced = new GridBagConstraints();
		gbc_minLonValueAdvanced.gridwidth = 2;
		gbc_minLonValueAdvanced.insets = new Insets(5, 0, 5, 15);
		gbc_minLonValueAdvanced.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLonValueAdvanced.gridx = 3;
		gbc_minLonValueAdvanced.gridy = 14;
		minLonValueAdvanced.setColumns(10);
		
		GridBagConstraints gbc_maxLonLabelAdvanced = new GridBagConstraints();
		gbc_maxLonLabelAdvanced.anchor = GridBagConstraints.WEST;
		gbc_maxLonLabelAdvanced.insets = new Insets(5, 15, 5, 15);
		gbc_maxLonLabelAdvanced.gridx = 2;
		gbc_maxLonLabelAdvanced.gridy = 15;
		
		GridBagConstraints gbc_maxLonValueAdvanced = new GridBagConstraints();
		gbc_maxLonValueAdvanced.gridwidth = 2;
		gbc_maxLonValueAdvanced.insets = new Insets(5, 0, 5, 15);
		gbc_maxLonValueAdvanced.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLonValueAdvanced.gridx = 3;
		gbc_maxLonValueAdvanced.gridy = 15;
		maxLonValueAdvanced.setColumns(10);
		
		maxLatLabelAdvanced.setText(Messages.getMessage("search.dialog.maxlat"));
		minLatLabelAdvanced.setText(Messages.getMessage("search.dialog.minlat"));
		maxLonLabelAdvanced.setText(Messages.getMessage("search.dialog.maxlon"));
		minLonLabelAdvanced.setText(Messages.getMessage("search.dialog.minlon"));
		panelAdvanced.add(maxLatLabelAdvanced, gbc_maxLatLabelAdvanced);
		panelAdvanced.add(minLatLabelAdvanced, gbc_minLatLabelAdvanced);
		panelAdvanced.add(minLonLabelAdvanced, gbc_minLonLabelAdvanced);
		panelAdvanced.add(maxLonLabelAdvanced, gbc_maxLonLabelAdvanced);
		panelAdvanced.add(maxLonValueAdvanced, gbc_maxLonValueAdvanced);
		panelAdvanced.add(minLatValueAdvanced, gbc_minLatValueAdvanced);
		panelAdvanced.add(minLonValueAdvanced, gbc_minLonValueAdvanced);
		panelAdvanced.add(maxLatValueAdvanced, gbc_maxLatValueAdvanced);
		
		GridBagConstraints mediaSubPanelconst = new GridBagConstraints();
		mediaSubPanelconst.anchor = GridBagConstraints.WEST;
		mediaSubPanelconst.gridx = 0;
		mediaSubPanelconst.gridy = 16;
		mediaSubPanelconst.gridwidth = 4;
		mediaSubPanelconst.insets = new Insets( 0, 10, 5, 5);
		panelAdvanced.add( mediaChooserSubPanel(), mediaSubPanelconst);
		
	}
	
	private JPanel mediaChooserSubPanel() {
		GridBagLayout layout = new GridBagLayout();
		JPanel mediaSubPanel = new JPanel( layout);
		GridBagConstraints constraints = new GridBagConstraints();
		mediaLabelAdvanced = new JLabel( Messages.getMessage( "search.dialog.media"));
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = 4;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets( 5, 20, 5, 15);
		mediaSubPanel.add( mediaLabelAdvanced, constraints);
		
		notesCheckBoxAdvanced = new JCheckBox( Messages.getMessage( "search.dialog.notes"), defaultWithMedia);
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.insets = new Insets( 5, 10, 5, 15);
		mediaSubPanel.add( notesCheckBoxAdvanced, constraints);
		
		picturesCheckBoxAdvanced = new JCheckBox( Messages.getMessage( "search.dialog.pictures"), defaultWithMedia);
		constraints.gridx = 1;
		mediaSubPanel.add( picturesCheckBoxAdvanced, constraints);
		
		audioCheckBoxAdvanced = new JCheckBox( Messages.getMessage( "search.dialog.audio"), defaultWithMedia);
		constraints.gridx = 2;
		mediaSubPanel.add( audioCheckBoxAdvanced, constraints);
		
		videoCheckBoxAdvanced = new JCheckBox( Messages.getMessage( "search.dialog.video"), defaultWithMedia);
		constraints.gridx = 3;
		mediaSubPanel.add( videoCheckBoxAdvanced, constraints);
		
		return mediaSubPanel;
	}
	
	public void sportSubSportChooser(){
		
		GridBagLayout gbl_bySportPrincipal = new GridBagLayout();
		gbl_bySportPrincipal.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_bySportPrincipal.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_bySportPrincipal.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_bySportPrincipal.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		bySportPrincipal.setLayout(gbl_bySportPrincipal);
		
		{
			/*JLabel*/ nameLabelSport = new JLabel(Messages.getMessage("search.dialog.name"));
			GridBagConstraints gbc_nameLabelSimple = new GridBagConstraints();
			gbc_nameLabelSimple.insets = new Insets(15, 15, 5, 5);
			gbc_nameLabelSimple.anchor = GridBagConstraints.WEST;
			gbc_nameLabelSimple.gridx = 0;
			gbc_nameLabelSimple.gridy = 0;
			bySportPrincipal.add(nameLabelSport, gbc_nameLabelSimple);
		}
		{
			GridBagConstraints gbc_nameValueSimple = new GridBagConstraints();
			gbc_nameValueSimple.gridwidth = 3;
			gbc_nameValueSimple.insets = new Insets(15, 15, 5, 15);
			gbc_nameValueSimple.fill = GridBagConstraints.HORIZONTAL;
			gbc_nameValueSimple.gridx = 2;
			gbc_nameValueSimple.gridy = 0;
			nameValueSport.setColumns(10);
			bySportPrincipal.add(nameValueSport, gbc_nameValueSimple);
		}
		{
			rangeLabelSport = new JLabel(Messages.getMessage("search.dialog.range"));
			GridBagConstraints gbc_rangeLabelSimple = new GridBagConstraints();
			gbc_rangeLabelSimple.insets = new Insets(5, 15, 5, 5);
			gbc_rangeLabelSimple.anchor = GridBagConstraints.WEST;
			gbc_rangeLabelSimple.gridx = 0;
			gbc_rangeLabelSimple.gridy = 1;
			bySportPrincipal.add(rangeLabelSport, gbc_rangeLabelSimple);
		}
		{
			GridBagConstraints gbc_RangeSimple = new GridBagConstraints();
			gbc_RangeSimple.gridwidth = 3;
			gbc_RangeSimple.insets = new Insets(5, 15, 5, 15);
			gbc_RangeSimple.fill = GridBagConstraints.HORIZONTAL;
			gbc_RangeSimple.gridx = 2;
			gbc_RangeSimple.gridy = 1;
			rangeSport.setColumns(10);
			bySportPrincipal.add( rangeSport, gbc_RangeSimple);
		}
		
		{
			distanceLabelSport = new JLabel(Messages.getMessage("search.dialog.distance"));
			GridBagConstraints gbc_distanceLabel = new GridBagConstraints();
			gbc_distanceLabel.anchor = GridBagConstraints.WEST;
			gbc_distanceLabel.insets = new Insets(5, 15, 5, 5);
			gbc_distanceLabel.gridx = 0;
			gbc_distanceLabel.gridy = 2;
			bySportPrincipal.add(distanceLabelSport, gbc_distanceLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxVal = new GridBagConstraints();
			gbc_maxVal.insets = new Insets(5, 15, 5, 15);
			gbc_maxVal.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxVal.gridx = 4;
			gbc_maxVal.gridy = 2;
			bySportPrincipal.add( distanceSliderSport.getUpperEndTextField(), gbc_maxVal);
		}
		{
			GridBagConstraints gbc_minVal = new GridBagConstraints();
			gbc_minVal.anchor = GridBagConstraints.WEST;
			gbc_minVal.insets = new Insets(5, 15, 5, 5);
			gbc_minVal.gridx = 2;
			gbc_minVal.gridy = 2;
			bySportPrincipal.add( distanceSliderSport.getLowerEndTextField(), gbc_minVal);
		}
		{
			distanceSliderSport.setMinimumSize(new Dimension( 180,15));
			GridBagConstraints gbc_rangeSliderAdvanced = new GridBagConstraints();
			gbc_rangeSliderAdvanced.fill = GridBagConstraints.HORIZONTAL;
			gbc_rangeSliderAdvanced.insets = new Insets(5, 15, 5, 5);
			gbc_rangeSliderAdvanced.gridx = 3;
			gbc_rangeSliderAdvanced.gridy = 2;
			bySportPrincipal.add( distanceSliderSport, gbc_rangeSliderAdvanced);
		}
		
		dateFromLabelSport = new JLabel(Messages.getMessage("search.dialog.minDate"));
		GridBagConstraints gbc_JDateLabelMin = new GridBagConstraints();
		gbc_JDateLabelMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateLabelMin.insets = new Insets(5, 15, 5, 15);
		gbc_JDateLabelMin.gridx = 0;
		gbc_JDateLabelMin.gridy = 3;
		
		GridBagConstraints gbc_JDateChooserMin = new GridBagConstraints();
		gbc_JDateChooserMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateChooserMin.insets = new Insets(5, 15, 5, 15);
		gbc_JDateChooserMin.gridx = 2;
		gbc_JDateChooserMin.gridy = 3;
		gbc_JDateChooserMin.gridwidth = 3;
		bySportPrincipal.add( dateFromLabelSport, gbc_JDateLabelMin);
		bySportPrincipal.add( dateRangeSport.getFromDateChooser(), gbc_JDateChooserMin);
		
		dateToLabelSport = new JLabel(Messages.getMessage("search.dialog.maxDate"));
		GridBagConstraints gbc_JDateLabelMax = new GridBagConstraints();
		gbc_JDateLabelMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateLabelMax.insets = new Insets(5, 15, 5, 15);
		gbc_JDateLabelMax.gridx = 0;
		gbc_JDateLabelMax.gridy = 4;
		
		GridBagConstraints gbc_JDateChooserMax = new GridBagConstraints();
		gbc_JDateChooserMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_JDateChooserMax.insets = new Insets(5, 15, 5, 15);
		gbc_JDateChooserMax.gridx = 2;
		gbc_JDateChooserMax.gridy = 4;
		gbc_JDateChooserMax.gridwidth = 3;
		bySportPrincipal.add( dateToLabelSport, gbc_JDateLabelMax);
		bySportPrincipal.add( dateRangeSport.getToDateChooser(), gbc_JDateChooserMax);
		
		coordinatesLabelSport = new JLabel(Messages.getMessage("search.dialog.coordinates"));
		btnNewButtonSport     = new JButton(Messages.getMessage("search.dialog.getCoordinates"));
		btnNewButtonSport.addActionListener( getCoordinatesButtonsHandler);
		
		GridBagConstraints gbc_btnNewButtonSimple = new GridBagConstraints();
		gbc_btnNewButtonSimple.anchor = GridBagConstraints.WEST;
		gbc_btnNewButtonSimple.insets = new Insets(0, 15, 0, 5);
		gbc_btnNewButtonSimple.gridx = 0;
		gbc_btnNewButtonSimple.gridy = 6;
		bySportPrincipal.add(btnNewButtonSport, gbc_btnNewButtonSimple);
		
		GridBagConstraints gbc_coordinatesLabelSimple = new GridBagConstraints();
		gbc_coordinatesLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_coordinatesLabelSimple.insets = new Insets(5, 15, 5, 5);
		gbc_coordinatesLabelSimple.gridx = 0;
		gbc_coordinatesLabelSimple.gridy = 5;
		bySportPrincipal.add(coordinatesLabelSport, gbc_coordinatesLabelSimple);
		
		GridBagConstraints gbc_minLatLabelSimple = new GridBagConstraints();
		gbc_minLatLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_minLatLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_minLatLabelSimple.gridx = 2;
		gbc_minLatLabelSimple.gridy = 5;
		
		GridBagConstraints gbc_minLatValueSimple = new GridBagConstraints();
		gbc_minLatValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_minLatValueSimple.gridwidth = 2;
		gbc_minLatValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLatValueSimple.gridx = 3;
		gbc_minLatValueSimple.gridy = 5;
		minLatValueSport.setColumns(10);
		
		GridBagConstraints gbc_maxLatLabelSimple = new GridBagConstraints();
		gbc_maxLatLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_maxLatLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_maxLatLabelSimple.gridx = 2;
		gbc_maxLatLabelSimple.gridy = 6;
		
		GridBagConstraints gbc_maxLatValueSimple = new GridBagConstraints();
		gbc_maxLatValueSimple.gridwidth = 2;
		gbc_maxLatValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_maxLatValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLatValueSimple.gridx = 3;
		gbc_maxLatValueSimple.gridy = 6;
		maxLatValueSport.setColumns(10);
		
		GridBagConstraints gbc_minLonLabelSimple = new GridBagConstraints();
		gbc_minLonLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_minLonLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_minLonLabelSimple.gridx = 2;
		gbc_minLonLabelSimple.gridy = 7;
		
		GridBagConstraints gbc_minLonValueSimple = new GridBagConstraints();
		gbc_minLonValueSimple.gridwidth = 2;
		gbc_minLonValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_minLonValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLonValueSimple.gridx = 3;
		gbc_minLonValueSimple.gridy = 7;
		minLonValueSport.setColumns(10);
		
		GridBagConstraints gbc_maxLonLabelSimple = new GridBagConstraints();
		gbc_maxLonLabelSimple.anchor = GridBagConstraints.WEST;
		gbc_maxLonLabelSimple.insets = new Insets(5, 15, 5, 15);
		gbc_maxLonLabelSimple.gridx = 2;
		gbc_maxLonLabelSimple.gridy = 8;
		
		GridBagConstraints gbc_maxLonValueSimple = new GridBagConstraints();
		gbc_maxLonValueSimple.gridwidth = 2;
		gbc_maxLonValueSimple.insets = new Insets(5, 0, 5, 15);
		gbc_maxLonValueSimple.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLonValueSimple.gridx = 3;
		gbc_maxLonValueSimple.gridy = 8;
		maxLonValueSport.setColumns(10);
		
		maxLatLabelSport.setText(Messages.getMessage("search.dialog.maxlat"));
		minLatLabelSport.setText(Messages.getMessage("search.dialog.minlat"));
		maxLonLabelSport.setText(Messages.getMessage("search.dialog.maxlon"));
		minLonLabelSport.setText(Messages.getMessage("search.dialog.minlon"));
		bySportPrincipal.add(maxLatLabelSport, gbc_maxLatLabelSimple);
		bySportPrincipal.add(minLatLabelSport, gbc_minLatLabelSimple);
		bySportPrincipal.add(minLonLabelSport, gbc_minLonLabelSimple);
		bySportPrincipal.add(maxLonLabelSport, gbc_maxLonLabelSimple);
		bySportPrincipal.add(maxLonValueSport, gbc_maxLonValueSimple);
		bySportPrincipal.add(minLatValueSport, gbc_minLatValueSimple);
		bySportPrincipal.add(minLonValueSport, gbc_minLonValueSimple);
		bySportPrincipal.add(maxLatValueSport, gbc_maxLatValueSimple);
		
		{
			/*JLabel*/ sportLabelSports = new JLabel(Messages.getMessage("search.dialog.sport"));
			GridBagConstraints gbc_sportLabel = new GridBagConstraints();
			gbc_sportLabel.anchor = GridBagConstraints.WEST;
			gbc_sportLabel.insets = new Insets(5, 15, 5, 5);
			gbc_sportLabel.gridx = 0;
			gbc_sportLabel.gridy = 9;
			bySportPrincipal.add(sportLabelSports, gbc_sportLabel);
		}
		{
			subSportLabelSports = new JLabel(Messages.getMessage("search.dialog.subSport"));
			GridBagConstraints gbc_subSportLabel = new GridBagConstraints();
			gbc_subSportLabel.anchor = GridBagConstraints.WEST;
			gbc_subSportLabel.insets = new Insets(5, 15, 5, 5);
			gbc_subSportLabel.gridx = 0;
			gbc_subSportLabel.gridy = 10;
			bySportPrincipal.add(subSportLabelSports, gbc_subSportLabel);
		}
		{
//			sportComboboxSport = comboBoxesSport.getSportsComboBox();
			GridBagConstraints gbc_sportBox = new GridBagConstraints();
			gbc_sportBox.insets = new Insets(5, 15, 5, 15);
			gbc_sportBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_sportBox.gridwidth = 3;
			gbc_sportBox.gridx = 2;
			gbc_sportBox.gridy = 9;
			bySportPrincipal.add( comboBoxesSport.getSportsComboBox(), gbc_sportBox);
//			subSportComboboxSport = comboBoxesSport.getSubSportsComboBox();
			GridBagConstraints gbc_subSportBox = new GridBagConstraints();
			gbc_subSportBox.insets = new Insets(5, 15, 5, 15);
			gbc_subSportBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_subSportBox.gridwidth = 3;
			gbc_subSportBox.gridx = 2;
			gbc_subSportBox.gridy = 10;
			bySportPrincipal.add( comboBoxesSport.getSubSportsComboBox(), gbc_subSportBox);
			
			comboBoxesSport.getSubSportsComboBox().addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setBySportSubCard();
				}
			});
			
			comboBoxesSport.getSubSportsComboBox().addPropertyChangeListener(
			new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					setBySportSubCard();
				}
			});
		}
		{
			bySportPane.add(bySportPrincipal, BorderLayout.NORTH);
		}
	}
	
	
	
	
	private void bySportUI(){
		
		bySportPane.add(bySportCard, BorderLayout.CENTER);
		
		sportSubSportChooser();
		
		{
		GridBagLayout gbl_bySportOneCard = new GridBagLayout();
		gbl_bySportOneCard.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_bySportOneCard.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_bySportOneCard.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_bySportOneCard.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		bySportOneCard.setLayout(gbl_bySportOneCard);
		}
		{
			GridBagLayout gbl_bySportTwoCard = new GridBagLayout();
			gbl_bySportTwoCard.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
			gbl_bySportTwoCard.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_bySportTwoCard.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_bySportTwoCard.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
			bySportTwoCard.setLayout(gbl_bySportTwoCard);
		}
		{
			GridBagLayout gbl_bySportThreeCard = new GridBagLayout();
			gbl_bySportThreeCard.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
			gbl_bySportThreeCard.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_bySportThreeCard.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_bySportThreeCard.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
			bySportThreeCard.setLayout(gbl_bySportThreeCard);
		}
		{
			GridBagLayout gbl_bySportFourCard = new GridBagLayout();
			gbl_bySportFourCard.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
			gbl_bySportFourCard.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_bySportFourCard.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_bySportFourCard.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
			bySportFourCard.setLayout(gbl_bySportFourCard);
		}
		{
			GridBagLayout gbl_bySportPreCard = new GridBagLayout();
			gbl_bySportPreCard.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
			gbl_bySportPreCard.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_bySportPreCard.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_bySportPreCard.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
			bySportPreCard.setLayout(gbl_bySportPreCard);
		}
		{
			descentLabelSport = new JLabel(Messages.getMessage("search.dialog.descent"));
			descentLabelSport.setMinimumSize(btnNewButtonSport.getPreferredSize());
			GridBagConstraints gbc_descentLabel = new GridBagConstraints();
			gbc_descentLabel.anchor = GridBagConstraints.NORTHWEST;
			gbc_descentLabel.insets = new Insets(5, 15, 5, 5);
			gbc_descentLabel.gridx = 0;
			gbc_descentLabel.gridy = 1;
			descentLabelSport.setPreferredSize(sportLabelSports.getSize());
			bySportOneCard.add( descentLabelSport, gbc_descentLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxValDescentSport = new GridBagConstraints();
			gbc_maxValDescentSport.insets = new Insets(5, 15, 5, 15);
			gbc_maxValDescentSport.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxValDescentSport.gridx = 4;
			gbc_maxValDescentSport.gridy = 1;
			bySportOneCard.add( descentSliderSport.getUpperEndTextField(), gbc_maxValDescentSport);
		}
		{
			GridBagConstraints gbc_minValDescentSport = new GridBagConstraints();
			gbc_minValDescentSport.anchor = GridBagConstraints.WEST;
			gbc_minValDescentSport.insets = new Insets(5, 15, 5, 5);
			gbc_minValDescentSport.gridx = 2;
			gbc_minValDescentSport.gridy = 1;
			bySportOneCard.add( descentSliderSport.getLowerEndTextField(), gbc_minValDescentSport);
		}
		{
			GridBagConstraints gbc_rangeSliderDescent = new GridBagConstraints();
			gbc_rangeSliderDescent.fill = GridBagConstraints.HORIZONTAL;
			gbc_rangeSliderDescent.insets = new Insets(5, 15, 5, 5);
			gbc_rangeSliderDescent.gridx = 3;
			gbc_rangeSliderDescent.gridy = 1;
			bySportOneCard.add( descentSliderSport, gbc_rangeSliderDescent);
		}
			difficultyLabelBySport = new JLabel(Messages.getMessage("search.dialog.difficultyLevel"));
			GridBagConstraints gbc_difficultyLabel = new GridBagConstraints();
			gbc_difficultyLabel.anchor = GridBagConstraints.WEST;
			gbc_difficultyLabel.insets = new Insets(5, 15, 5, 5);
			gbc_difficultyLabel.gridx = 0;
			gbc_difficultyLabel.gridy = 2;
			bySportOneCard.add(difficultyLabelBySport, gbc_difficultyLabel);
		
		{
			GridBagConstraints gbc_difficultyBoxSport = new GridBagConstraints();
			gbc_difficultyBoxSport.gridwidth = 3;
			gbc_difficultyBoxSport.insets = new Insets(5, 15, 5, 15);
			gbc_difficultyBoxSport.fill = GridBagConstraints.HORIZONTAL;
			gbc_difficultyBoxSport.gridx = 2;
			gbc_difficultyBoxSport.gridy = 2;
			bySportOneCard.add( difficultyComboBoxBySport, gbc_difficultyBoxSport);
		}
		{
			conditionLabelBySport = new JLabel(Messages.getMessage("search.dialog.state"));
			GridBagConstraints gbc_conditionLabel = new GridBagConstraints();
			gbc_conditionLabel.anchor = GridBagConstraints.WEST;
			gbc_conditionLabel.insets = new Insets(5, 15, 5, 5);
			gbc_conditionLabel.gridx = 0;
			gbc_conditionLabel.gridy = 3;
			bySportOneCard.add(conditionLabelBySport, gbc_conditionLabel);
		}
		{
			GridBagConstraints gbc_conditionBoxSport = new GridBagConstraints();
			gbc_conditionBoxSport.gridwidth = 3;
			gbc_conditionBoxSport.insets = new Insets(5, 15, 5, 15);
			gbc_conditionBoxSport.fill = GridBagConstraints.HORIZONTAL;
			gbc_conditionBoxSport.gridx = 2;
			gbc_conditionBoxSport.gridy = 3;
			conditionComboBoxBySport.setSelectedItem( defaultTrackCondition);
			bySportOneCard.add( conditionComboBoxBySport, gbc_conditionBoxSport);
		}
		{
			conditionLabelBySportTwo = new JLabel(Messages.getMessage("search.dialog.state"));
			conditionLabelBySportTwo.setPreferredSize( btnNewButtonSport.getPreferredSize());
			GridBagConstraints gbc_conditionLabel = new GridBagConstraints();
			gbc_conditionLabel.anchor = GridBagConstraints.WEST;
			gbc_conditionLabel.insets = new Insets(5, 15, 5, 5);
			gbc_conditionLabel.gridx = 0;
			gbc_conditionLabel.gridy = 1;
			bySportTwoCard.add( conditionLabelBySportTwo, gbc_conditionLabel);
		}
		{
			GridBagConstraints gbc_conditionBoxSportTwo = new GridBagConstraints();
			gbc_conditionBoxSportTwo.gridwidth = 3;
			gbc_conditionBoxSportTwo.insets = new Insets(5, 15, 5, 15);
			gbc_conditionBoxSportTwo.fill = GridBagConstraints.HORIZONTAL;
			gbc_conditionBoxSportTwo.gridx = 2;
			gbc_conditionBoxSportTwo.gridy = 1;
			conditionComboBoxBySportTwo.setSelectedItem( defaultTrackCondition);
			bySportTwoCard.add( conditionComboBoxBySportTwo, gbc_conditionBoxSportTwo);
		}
		{
			difficultyLabelBySportThree = new JLabel(Messages.getMessage("search.dialog.difficultyLevel"));
			difficultyLabelBySportThree.setPreferredSize(btnNewButtonSport.getPreferredSize());
			GridBagConstraints gbc_difficultyLabelSport = new GridBagConstraints();
			gbc_difficultyLabelSport.anchor = GridBagConstraints.WEST;
			gbc_difficultyLabelSport.insets = new Insets(5, 15, 5, 5);
			gbc_difficultyLabelSport.gridx = 0;
			gbc_difficultyLabelSport.gridy = 1;
			bySportThreeCard.add(difficultyLabelBySportThree, gbc_difficultyLabelSport);
		}
		{
			GridBagConstraints gbc_difficultyBoxTwo = new GridBagConstraints();
			gbc_difficultyBoxTwo.gridwidth = 3;
			gbc_difficultyBoxTwo.insets = new Insets(5, 15, 5, 15);
			gbc_difficultyBoxTwo.fill = GridBagConstraints.HORIZONTAL;
			gbc_difficultyBoxTwo.gridx = 2;
			gbc_difficultyBoxTwo.gridy = 1;
			difficultyComboBoxBySportThree.setSelectedItem( defaultDifficulty);
			bySportThreeCard.add( difficultyComboBoxBySportThree, gbc_difficultyBoxTwo);
		}
		{
			ascentLabelSport = new JLabel(Messages.getMessage("search.dialog.ascent"));
			ascentLabelSport.setMinimumSize(btnNewButtonSport.getPreferredSize());
			GridBagConstraints gbc_ascentLabel = new GridBagConstraints();
			gbc_ascentLabel.anchor = GridBagConstraints.WEST;
			gbc_ascentLabel.insets = new Insets(5, 15, 5, 5);
			gbc_ascentLabel.gridx = 0;
			gbc_ascentLabel.gridy = 0;
			bySportFourCard.add( ascentLabelSport, gbc_ascentLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxValAscentSport = new GridBagConstraints();
			gbc_maxValAscentSport.insets = new Insets(5, 15, 5, 15);
			gbc_maxValAscentSport.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxValAscentSport.gridx = 4;
			gbc_maxValAscentSport.gridy = 0;
			bySportFourCard.add( ascentSliderSport.getUpperEndTextField(), gbc_maxValAscentSport);
		}
		{
			GridBagConstraints gbc_minValAscentSport = new GridBagConstraints();
			gbc_minValAscentSport.anchor = GridBagConstraints.WEST;
			gbc_minValAscentSport.insets = new Insets(5, 15, 5, 5);
			gbc_minValAscentSport.gridx = 2;
			gbc_minValAscentSport.gridy = 0;
			bySportFourCard.add( ascentSliderSport.getLowerEndTextField(), gbc_minValAscentSport);
		}
		{
			GridBagConstraints gbc_ascentSliderSport = new GridBagConstraints();
			gbc_ascentSliderSport.fill = GridBagConstraints.HORIZONTAL;
			gbc_ascentSliderSport.insets = new Insets(5, 15, 5, 5);
			gbc_ascentSliderSport.gridx = 3;
			gbc_ascentSliderSport.gridy = 0;
			bySportFourCard.add( ascentSliderSport, gbc_ascentSliderSport);
		}
		{
			descentLabelSportTwo = new JLabel(Messages.getMessage("search.dialog.descent"));
			GridBagConstraints gbc_descentLabel = new GridBagConstraints();
			gbc_descentLabel.anchor = GridBagConstraints.WEST;
			gbc_descentLabel.insets = new Insets(5, 15, 5, 5);
			gbc_descentLabel.gridx = 0;
			gbc_descentLabel.gridy = 1;
			bySportFourCard.add( descentLabelSportTwo, gbc_descentLabel);
		}
		{
			aux.setText("1000");
			GridBagConstraints gbc_maxValDescentSportTwo = new GridBagConstraints();
			gbc_maxValDescentSportTwo.insets = new Insets(5, 15, 5, 15);
			gbc_maxValDescentSportTwo.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxValDescentSportTwo.gridx = 4;
			gbc_maxValDescentSportTwo.gridy = 1;
			bySportFourCard.add( descentSliderSportTwo.getUpperEndTextField(), gbc_maxValDescentSportTwo);
		}
		{
			GridBagConstraints gbc_minValDescentSportTwo = new GridBagConstraints();
			gbc_minValDescentSportTwo.anchor = GridBagConstraints.WEST;
			gbc_minValDescentSportTwo.insets = new Insets(5, 15, 5, 5);
			gbc_minValDescentSportTwo.gridx = 2;
			gbc_minValDescentSportTwo.gridy = 1;
			bySportFourCard.add( descentSliderSportTwo.getLowerEndTextField(), gbc_minValDescentSportTwo);
		}
		{
			GridBagConstraints gbc_rangeSliderDescentSportTwo = new GridBagConstraints();
			gbc_rangeSliderDescentSportTwo.fill = GridBagConstraints.HORIZONTAL;
			gbc_rangeSliderDescentSportTwo.insets = new Insets(5, 15, 5, 5);
			gbc_rangeSliderDescentSportTwo.gridx = 3;
			gbc_rangeSliderDescentSportTwo.gridy = 1;
			bySportFourCard.add( descentSliderSportTwo, gbc_rangeSliderDescentSportTwo);
		}
		{
			difficultyLabelBySportFour = new JLabel(Messages.getMessage("search.dialog.difficultyLevel"));
			GridBagConstraints gbc_difficultyLabelSport = new GridBagConstraints();
			gbc_difficultyLabelSport.anchor = GridBagConstraints.WEST;
			gbc_difficultyLabelSport.insets = new Insets(5, 15, 5, 5);
			gbc_difficultyLabelSport.gridx = 0;
			gbc_difficultyLabelSport.gridy = 2;
			bySportFourCard.add(difficultyLabelBySportFour, gbc_difficultyLabelSport);
		}
		{
			GridBagConstraints gbc_difficultyBox = new GridBagConstraints();
			gbc_difficultyBox.gridwidth = 3;
			gbc_difficultyBox.insets = new Insets(5, 15, 5, 15);
			gbc_difficultyBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_difficultyBox.gridx = 2;
			gbc_difficultyBox.gridy = 2;
			difficultyComboBoxBySportFour.setSelectedItem( defaultDifficulty);
			bySportFourCard.add( difficultyComboBoxBySportFour, gbc_difficultyBox);
		}
		{
			conditionLabelBySportFour = new JLabel(Messages.getMessage("search.dialog.state"));
			GridBagConstraints gbc_conditionLabel = new GridBagConstraints();
			gbc_conditionLabel.anchor = GridBagConstraints.WEST;
			gbc_conditionLabel.insets = new Insets(5, 15, 5, 5);
			gbc_conditionLabel.gridx = 0;
			gbc_conditionLabel.gridy = 3;
			bySportFourCard.add( conditionLabelBySportFour, gbc_conditionLabel);
		}
		{
			GridBagConstraints gbc_conditionBox = new GridBagConstraints();
			gbc_conditionBox.gridwidth = 3;
			gbc_conditionBox.insets = new Insets(5, 15, 5, 15);
			gbc_conditionBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_conditionBox.gridx = 2;
			gbc_conditionBox.gridy = 3;
			conditionComboBoxBySportFour.setSelectedItem( defaultTrackCondition);
			bySportFourCard.add( conditionComboBoxBySportFour, gbc_conditionBox);
		}
		
		
		
		bySportCard.add(bySportPreCard, "bySportPreCard");
		bySportCard.add(bySportFourCard, "bySportFourCard");
		bySportCard.add(bySportOneCard, "bySportOneCard");
		bySportCard.add(bySportTwoCard, "bySportTwoCard");
		bySportCard.add(bySportThreeCard, "bySportThreeCard");
	}
	
	//2018-03-11: 12335 - added locale change
	private void setLabelsOfLocale() {
		
		Locale locale = Messages.getLocale();
		
		this.setTitle( Messages.getMessage( "search.dialog.title"));
		
		tabsPane.setTitleAt( 0, Messages.getMessage( "search.dialog.simple"));
		
		nameLabelSimple.setText( Messages.getMessage( "search.dialog.name"));
		rangeLabelSimple.setText( Messages.getMessage( "search.dialog.range"));
		rangeSimple.setLocale( locale);
		distanceLabelSimple.setText( Messages.getMessage( "search.dialog.distance"));
		difficultyLabelSimple.setText( Messages.getMessage( "search.dialog.difficultyLevel"));
		difficultyComboBoxSimple.setLocale( locale);
		sportLabel.setText( Messages.getMessage( "search.dialog.sport"));
		subSportLabel.setText( Messages.getMessage( "search.dialog.subSport"));
		comboBoxesSimples.setLocale( locale);
		coordinatesLabelSimple.setText( Messages.getMessage( "search.dialog.coordinates"));
		maxLatLabelSimple.setText(Messages.getMessage( "search.dialog.maxlat"));
		minLatLabelSimple.setText(Messages.getMessage( "search.dialog.minlat"));
		maxLonLabelSimple.setText(Messages.getMessage( "search.dialog.maxlon"));
		minLonLabelSimple.setText(Messages.getMessage( "search.dialog.minlon"));
		btnNewButtonSimple.setText(Messages.getMessage("search.dialog.getCoordinates"));
		
		tabsPane.setTitleAt( 1, Messages.getMessage( "search.dialog.advanced"));
		
		nameLabelAdvanced.setText( Messages.getMessage( "search.dialog.name"));
		rangeLabelAdvanced.setText( Messages.getMessage( "search.dialog.range"));
		dateFromLabelAdvanced.setText( Messages.getMessage( "search.dialog.minDate"));
		dateToLabelAdvanced.setText( Messages.getMessage( "search.dialog.maxDate"));
		dateRangeAdvanced.setLocale( locale);
		distanceLabelAdvanced.setText( Messages.getMessage( "search.dialog.distance"));
		ascentLabelAdvanced.setText( Messages.getMessage( "search.dialog.ascent"));
		descentLabelAdvanced.setText( Messages.getMessage( "search.dialog.descent"));
		difficultyLabelAdvanced.setText( Messages.getMessage( "search.dialog.difficultyLevel"));
		conditionLabelAdvanced.setText( Messages.getMessage( "search.dialog.state"));
		conditionComboBoxAdvanced.setLocale( locale);
		sportLabelAdvanced.setText( Messages.getMessage( "search.dialog.sport"));
		subSportLabelAdvanced.setText( Messages.getMessage( "search.dialog.subSport"));
		circularLabel.setText( Messages.getMessage( "search.dialog.circular"));
		circuitComboBoxAdvanced.setLocale( locale);
		coordinatesLabelAdvanced.setText( Messages.getMessage( "search.dialog.coordinates"));
		maxLatLabelAdvanced.setText(Messages.getMessage( "search.dialog.maxlat"));
		minLatLabelAdvanced.setText(Messages.getMessage( "search.dialog.minlat"));
		maxLonLabelAdvanced.setText(Messages.getMessage( "search.dialog.maxlon"));
		minLonLabelAdvanced.setText(Messages.getMessage( "search.dialog.minlon"));
		maxLatValueAdvanced.setLocale( locale);
		minLatValueAdvanced.setLocale( locale);
		maxLonValueAdvanced.setLocale( locale);
		minLonValueAdvanced.setLocale( locale);
		btnNewButtonAdvanced.setText( Messages.getMessage( "search.dialog.getCoordinates"));
		mediaLabelAdvanced.setText( Messages.getMessage( "search.dialog.media"));
		notesCheckBoxAdvanced.setText( Messages.getMessage( "search.dialog.notes"));
		picturesCheckBoxAdvanced.setText( Messages.getMessage( "search.dialog.pictures"));
		audioCheckBoxAdvanced.setText( Messages.getMessage( "search.dialog.audio"));
		videoCheckBoxAdvanced.setText( Messages.getMessage( "search.dialog.video"));
		
		tabsPane.setTitleAt( 2, Messages.getMessage( "search.dialog.bySport"));
		
		nameLabelSport.setText( Messages.getMessage( "search.dialog.name"));
		rangeLabelSport.setText( Messages.getMessage( "search.dialog.range"));
		distanceLabelSport.setText( Messages.getMessage( "search.dialog.distance"));
		dateFromLabelSport.setText( Messages.getMessage( "search.dialog.minDate"));
		dateToLabelSport.setText( Messages.getMessage( "search.dialog.maxDate"));
		btnNewButtonSport.setText(Messages.getMessage("search.dialog.getCoordinates"));
		coordinatesLabelSport.setText( Messages.getMessage( "search.dialog.coordinates"));
		maxLatLabelSport.setText(Messages.getMessage( "search.dialog.maxlat"));
		minLatLabelSport.setText(Messages.getMessage( "search.dialog.minlat"));
		maxLonLabelSport.setText(Messages.getMessage( "search.dialog.maxlon"));
		minLonLabelSport.setText(Messages.getMessage( "search.dialog.minlon"));
		sportLabelSports.setText( Messages.getMessage( "search.dialog.sport"));
		subSportLabelSports.setText( Messages.getMessage( "search.dialog.subSport"));
		difficultyLabelBySport.setText( Messages.getMessage( "search.dialog.difficultyLevel"));
		conditionLabelBySport.setText( Messages.getMessage( "search.dialog.condition"));
		conditionLabelBySportTwo.setText( Messages.getMessage( "search.dialog.condition"));
		difficultyLabelBySportThree.setText( Messages.getMessage( "search.dialog.difficultyLevel"));
		ascentLabelSport.setText( Messages.getMessage( "search.dialog.ascent"));
		descentLabelSport.setText( Messages.getMessage( "search.dialog.descent"));
		difficultyLabelBySportFour.setText( Messages.getMessage( "search.dialog.state"));
		conditionLabelBySportFour.setText( Messages.getMessage( "search.dialog.condition"));
		
		resetButton.setText( Messages.getMessage( "search.dialog.reset"));
		okButton.setText( Messages.getMessage( "search.dialog.search"));
		cancelButton.setText( Messages.getMessage( "search.dialog.cancel"));
		addToCollection.setText( Messages.getMessage( "search.dialog.addToCollection"));
		addToCollection.setToolTipText( Messages.getMessage( "search.dialog.addToCollectionToolTip"));
		addToCollection.setEnabled( false); 			//12335: 2018-07-08
	}
		
	public void okButton() {
		HashMap< DBSearchField, String> searchMap = new HashMap<>();
		int mediaMask = 0;
		
		// Common stuff
		// 		Bounding coordinates
		getCoordinatesFromFields( searchMap);
		// 		Distance (minimum, maximum)
		int selectedMinAux = distanceSliderSimple.getValue() * 1000;
		if ( selectedMinAux > 0. )
			searchMap.put( DBSearchField.MinimumDistance, String.valueOf(  selectedMinAux));
		int selectedMaxAux = distanceSliderSimple.getUpperValue() * 1000;
		searchMap.put( DBSearchField.MaximumDistance, String.valueOf(  selectedMaxAux));
		
		// Basic Search Dialogue
		
		if( selectedSearchType.equals(Messages.getMessage("search.dialog.simple")) ) {
			// Sport and subsport
			getSportAndSubSportFromComboBoxes( comboBoxesSimples, searchMap);
			// Difficulty level
			getDifficultyLevel( searchMap);
		}
		
		// Advanced Search Dialogue
		
		if( selectedSearchType.equals( Messages.getMessage("search.dialog.advanced")) ) {
			// Sport and subsport
			getSportAndSubSportFromComboBoxes( comboBoxesAdvanced, searchMap);
			// Start and end dates
			getStartAndEndDates( searchMap);
			// Ascent
			int selectedValue = ascentSliderAdvanced.getValue();
			if ( selectedValue > 0 )
				searchMap.put( DBSearchField.MinimumAscent,  String.valueOf( selectedValue));
			searchMap.put( DBSearchField.MaximumAscent,  String.valueOf( ascentSliderAdvanced.getUpperValue()));
			// Descent
			selectedValue = descentSliderAdvanced.getValue();
			if ( selectedValue > 0 )
				searchMap.put( DBSearchField.MinimumDescent, String.valueOf( selectedValue));
			searchMap.put( DBSearchField.MaximumDescent, String.valueOf( descentSliderAdvanced.getUpperValue()));
			// Track difficulty
			getDifficultyLevel( searchMap);
			// Track condition
			getTrackCondition( searchMap);
			// Track type (circular, open)
			getCircuitType( searchMap);
			// With attached media
			// Notes
			if ( notesCheckBoxAdvanced.isSelected() )
				mediaMask += DBSearch.WithNotes;
			// Pictures
			if ( picturesCheckBoxAdvanced.isSelected() )
				mediaMask += DBSearch.WithPictures;
			// Audio
			if ( audioCheckBoxAdvanced.isSelected() )
				mediaMask += DBSearch.WithAudio;
			if ( videoCheckBoxAdvanced.isSelected() )
				mediaMask += DBSearch.WithVideo;
		}
		
		// Search by Sport
		
		if( selectedSearchType.equals( Messages.getMessage( "search.dialog.bySport"))){
			// Sport and subsport
			getSportAndSubSportFromComboBoxes( comboBoxesSport, searchMap);
			// Start and end dates
			getStartAndEndDates( searchMap);
			if( selectedSearchSport == null ){
				//do nothing
			}
			else{
				if( selectedSearchSport.equals("bySportOneCard") ) {
					// Descent
					int selectedValue = descentSliderSport.getValue();
					if ( selectedValue > 0 )
						searchMap.put( DBSearchField.MinimumDescent, String.valueOf( selectedValue));
					searchMap.put( DBSearchField.MaximumDescent, String.valueOf( descentSliderSport.getUpperValue()));
					// Difficulty level
					if ( !difficultyComboBoxBySport.getSelectedDifficultyLevel().equals( DifficultyLevelType.UNKNOWN) )
						searchMap.put( DBSearchField.TrackDifficulty, difficultyComboBoxBySport.getSelectedItemValueString());
					// Track condition
					if ( ! conditionComboBoxBySport.getSelectedTrackCondition().equals( TrackConditionType.UNKNOWN))
						searchMap.put( DBSearchField.TrackState, conditionComboBoxBySport.getSelectedItemValueString());
				}
				if( selectedSearchSport.equals("bySportTwoCard") ) {
					// Track condition
					if ( ! conditionComboBoxBySport.getSelectedTrackCondition().equals( TrackConditionType.UNKNOWN))
						searchMap.put( DBSearchField.TrackState, conditionComboBoxBySport.getSelectedItemValueString());
				}
				if( selectedSearchSport.equals("bySportThreeCard") ) {
					// Difficulty level
					if ( !difficultyComboBoxBySport.getSelectedDifficultyLevel().equals( DifficultyLevelType.UNKNOWN) )
						searchMap.put( DBSearchField.TrackDifficulty, difficultyComboBoxBySport.getSelectedItemValueString());
				}
				if( selectedSearchSport.equals("bySportFourCard") ) {
					// Descent
					int selectedValue = descentSliderSport.getValue();
					if ( selectedValue > 0 )
						searchMap.put( DBSearchField.MinimumDescent, String.valueOf( selectedValue));
					searchMap.put( DBSearchField.MaximumDescent, String.valueOf( descentSliderSport.getUpperValue()));
					// Ascent
					selectedValue = ascentSliderSport.getValue();
					if ( selectedValue > 0 )
						searchMap.put( DBSearchField.MinimumAscent, String.valueOf( selectedValue));
					searchMap.put( DBSearchField.MaximumAscent, String.valueOf( ascentSliderSport.getUpperValue()));
					// Difficulty level
					if ( !difficultyComboBoxBySport.getSelectedDifficultyLevel().equals( DifficultyLevelType.UNKNOWN) )
						searchMap.put( DBSearchField.TrackDifficulty, difficultyComboBoxBySport.getSelectedItemValueString());
					// Track condition
					if ( ! conditionComboBoxBySport.getSelectedTrackCondition().equals( TrackConditionType.UNKNOWN))
						searchMap.put( DBSearchField.TrackState, conditionComboBoxBySport.getSelectedItemValueString());
				}
			}
		}
		
		// 2018-03-09: 12335 - complete new section
		final DBSearchActivitiesAndCourses searcher = new DBSearchActivitiesAndCourses( searchMap, mediaMask);
		if ( searcher.hasActivitiesOrCourses() ) 
		{
			this.setVisible(false);
			final DocumentManager manager = DocumentManager.getInstance();
			if ( ! addToCollection.isSelected() )							//2018-04-10: 12335
				manager.clearDocumentContents( manager.getDefaultCollectionDocument());
			new Task(new Action(){
				@Override
				public Object execute() throws TrackItException{
					if ( searcher.hasActivitiesOrCourses() ) {
						String filename;
						while( (filename = searcher.getNext()) != null ) {
							manager.selectiveImport( manager.getDefaultCollectionDocument(), 
													 manager.getLibraryFolder(), 
													 filename, 
													 searcher.getActivities(), searcher.getCourses());
						}
					} else {
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error3"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					return null;
				}

				@Override
				public String getMessage() {
					return Messages.getMessage( "searchResult.loadingInProgress");	//12335: 2018-07-08
				}

				@Override
				public void done(Object result) {
				}
			}).execute();
			
			//EventManager.getInstance().publish(SearchInterface.this, Event.PLACE_BUTTONS, null);
			
			
		} else {
			JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error3"), "Dialog",
	        JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private static void storeDoubleField( HashMap<DBSearchField, String> map,
										  DBSearchField fieldID, Double value) {
		if ( value != null )
			map.put( fieldID, String.valueOf( value));
	}
	
	private void getCoordinatesFromFields( HashMap<DBSearchField, String> map) { 
		if ( minLatValueSimple.getDouble() != null )
			storeDoubleField( map, DBSearchField.MinimumLatitude,  minLatValueSimple.getDouble());
		if ( maxLatValueSimple.getDouble() != null )
			storeDoubleField( map, DBSearchField.MaximumLatitude,  maxLatValueSimple.getDouble());
		if ( minLonValueSimple.getDouble() != null )
			storeDoubleField( map, DBSearchField.MinimumLongitude, minLonValueSimple.getDouble());
		if ( maxLonValueSimple.getDouble() != null )
			storeDoubleField( map, DBSearchField.MaximumLongitude, maxLonValueSimple.getDouble());
	}
	
	// 2018-03-10: 12335
	private void getSportAndSubSportFromComboBoxes( SportSubSportComboBoxes comboBoxes,
													HashMap< DBSearchField, String> map) {
		if ( ! comboBoxes.getSport().equals( SportType.ALL) &&  !comboBoxes.getSport().equals( SportType.GENERIC)) {
			map.put( DBSearchField.Sport, String.valueOf( comboBoxes.getSport().getSportID()));
			if ( ! comboBoxes.getSubSport().equals( SubSportType.ALL_SUB) )
				map.put( DBSearchField.SubSport, String.valueOf( comboBoxes.getSubSport().getSubSportID()));
		}
	}
	
	//2018-03-16: 12335
	private void getTrackCondition( HashMap<DBSearchField, String> map) {
		if ( conditionComboBoxAdvanced.getSelectedTrackCondition() != TrackConditionType.UNKNOWN )
			map.put( DBSearchField.TrackState, conditionComboBoxAdvanced.getSelectedItemValueString());
	}
	
	// 2018-03-16: 12335
	private void getDifficultyLevel( HashMap<DBSearchField, String> map) {
		if ( difficultyComboBoxSimple.getSelectedDifficultyLevel() != DifficultyLevelType.UNKNOWN )
			map.put( DBSearchField.TrackDifficulty, difficultyComboBoxSimple.getSelectedItemValueString());
	}
	
	// 2018-03-16: 12335
	private void getCircuitType( HashMap<DBSearchField, String> map) {
		if ( circuitComboBoxAdvanced.getSelectedCircuitType() != CircuitType.ALL )
			map.put( DBSearchField.CircularPath, circuitComboBoxAdvanced.getSelectedItemValueString());
	}
	
	// 2018-04-03: 12335
	private void getStartAndEndDates( HashMap<DBSearchField, String> map) {
		Date date = dateRangeAdvanced.fromDate();
		if ( date != null)
			map.put( DBSearchField.MinimumStartTime, String.valueOf( date.getTime()));
		date = dateRangeAdvanced.toDate();
		if ( date != null)
			map.put( DBSearchField.MaximumEndTime, String.valueOf( date.getTime()));
	}
	
	// 2018-03-24: 12335
	private void handleLocationSearch( String name, Double range) {
		if ( !name.isEmpty() ) {
			boolean rangeIsTheSame = range == null ? selectedRange == null : range.equals( selectedRange);
			if (  name.equals( locationName) ? !rangeIsTheSame : true ) {
				Double searchRange = range != null ? range * 1000.: null;
				GeographicBoundingBox boundingBox = geoLocationService.getBoundingBox( name, searchRange);
				if ( boundingBox != null ) {
					minLatValueAdvanced.setValue( boundingBox.minLatitude());
					maxLatValueAdvanced.setValue( boundingBox.maxLatitude());
					minLonValueAdvanced.setValue( boundingBox.minLongitude());
					maxLonValueAdvanced.setValue( boundingBox.maxLongitude());
					locationName  = name;
					selectedRange = range;
					return;
				}
			}
		}
		minLatValueAdvanced.setValue( null);
		maxLatValueAdvanced.setValue( null);
		minLonValueAdvanced.setValue( null);
		maxLonValueAdvanced.setValue( null);
		locationName = "";
		nameValueSimple.setText( locationName);		
	}
	
	public void setBySportSubCard(){		
		SportType    selectedSport    = comboBoxesSport.getSport();
		SubSportType selectedSubSport = comboBoxesSport.getSubSport();
		
		CardLayout cardLayout = (CardLayout)(bySportCard.getLayout());
		if ( selectedSubSport.getAscentDescentClass() == AscentDescentType.DESCENT_ONLY.getValue() ) {
			selectedSearchSport = "bySportOneCard";
			cardLayout.show( bySportCard, "bySportOneCard");
		}
		else if ( selectedSport.toString().equals( SportType.RUNNING.toString()) && 
				  selectedSubSport.toString().equals( SubSportType.RUNNING_TRACK.toString())){
			selectedSearchSport ="bySportTwoCard";
			cardLayout.show( bySportCard, "bySportTwoCard");
		}
		else if ( selectedSubSport.getAscentDescentClass() == AscentDescentType.FLAT.getValue() ) {
			selectedSearchSport = "bySportThreeCard";
			cardLayout.show( bySportCard, "bySportThreeCard");
		}
		else{
			selectedSearchSport = "bySportFourCard";
			cardLayout.show( bySportCard, "bySportFourCard");
		}
	}

	public void addCoordinates( GeographicBoundingBox coordinates) {
		if ( coordinates != null ) {
			minLatValueSimple.setValue( coordinates.minLatitude());
			maxLatValueSimple.setValue( coordinates.maxLatitude());
			minLonValueSimple.setValue( coordinates.minLongitude());
			maxLonValueSimple.setValue( coordinates.maxLongitude());
		}
	}
	
	public void resetUI(){
		CardLayout cardLayout = (CardLayout)(bySportCard.getLayout());
		cardLayout.show(bySportCard, "bySportPreCard");
		
		locationName = defaultLocationName;
		selectedRange = defaultRange;
		
		// Simple tab
		
		nameValueSimple.setText( defaultLocationName);
		rangeSimple.setValue( defaultRange);
		distanceSliderSimple.setValue(      defaultMinimumDistance);
		distanceSliderSimple.setUpperValue( defaultMaximumDistance);
		difficultyComboBoxSimple.setSelectedItem( defaultDifficulty);
		comboBoxesSimples.SetSport(     defaultSport);
		comboBoxesSimples.setSubSport( defaultSubSport);
		maxLatValueSimple.setValue(  defaultLatLon);
		minLatValueSimple.setValue(  defaultLatLon);
		maxLonValueSimple.setValue(  defaultLatLon);
		minLonValueSimple .setValue( defaultLatLon);

		// Advanced tab (synchronized values have already been set)
		dateRangeAdvanced.setDates( defaultFromDate, defaultToDate);
		ascentSliderAdvanced.setMaximum(    defaultMaximumAscent);
		ascentSliderAdvanced.setValue(      defaultMinimumAscent);
		ascentSliderAdvanced.setUpperValue( defaultMaximumAscent);
		descentSliderAdvanced.setMaximum(    defaultMaximumDescent);
		descentSliderAdvanced.setValue(      defaultMinimumDescent);
		descentSliderAdvanced.setUpperValue( defaultMaximumDescent);
		conditionComboBoxAdvanced.setSelectedItem( defaultTrackCondition);
		circuitComboBoxAdvanced.setSelectedItem( defaultCircuitType);
		notesCheckBoxAdvanced.setSelected( defaultWithMedia);
		picturesCheckBoxAdvanced.setSelected( defaultWithMedia);
		audioCheckBoxAdvanced.setSelected( defaultWithMedia);
		videoCheckBoxAdvanced.setSelected( defaultWithMedia);
		addToCollection.setSelected( defaultAddToCollection);
	}
	
	public String getSearchType(){
		return selectedSearchType;
	}
	
	public String getName(){
		return nameValueSimple.getText();
	}
	
	public String getCurrentMaxLon(){
		return String.valueOf( maxLonValueSimple.getDouble());
	}
	
	public String getCurrentMaxLat(){
		return String.valueOf( maxLatValueSimple.getDouble());
	}
	
	public String getCurrentMinLon(){
		return String.valueOf( minLonValueSimple.getDouble());
	}
	
	public String getCurrentMinLat(){
		return String.valueOf( minLatValueSimple.getDouble());
	}
	
	public SportType getCurrentSport(){
		return comboBoxesSimples.getSport();
	}
	
	public SubSportType getCurrentSubSport(){
		return comboBoxesSimples.getSubSport();
	}
	
	public SubSportType getCurrentSubSportBySport(){
		return comboBoxesSport.getSubSport();
	}
	
	public DifficultyLevelType getDifficulty(){
		return difficultyComboBoxSimple.getSelectedDifficultyLevel();
	}
	
	public void setLast(SearchInterface searchInterface){
		last = searchInterface;
	}
	
	public String formatCoordinate(double coordinate){
		DecimalFormat dc = new DecimalFormat("#.000000");
		String formattedCoordinate = dc.format(coordinate).replace(',', '.');
		return formattedCoordinate;
	}
	
	public void makeVisible(){
		this.setVisible(true);
	}
	
	public static SearchInterface getInstance() {
	      if(last == null) {
	         last = new SearchInterface();
	      }
	      return last;
	}
}
