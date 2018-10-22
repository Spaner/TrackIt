package com.trackit.presentation.utilities;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.trackit.TrackIt;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.operation.PauseDetectionPicCaseOperation;
import com.trackit.business.utilities.PhotoPlacer;
import com.trackit.business.utilities.PhotoPlacerBreadthFirst;
import com.trackit.business.utilities.PhotoPlacerDepthFirst;
import com.trackit.business.utilities.geo.NOAAMagneticDeclinationService;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;

public class TestPanel extends JDialog implements EventListener{
	private JButton endButton = new JButton( "Dismiss dialogue");
	
	private JButton breadth = new JButton( "Breadth first (default)");
	private JButton depth   = new JButton( "Depth first (wrong?)");
	private DocumentItem selectedItem = null;
	
	private JButton btnDeclination = new JButton( "Get declination");
	private JLabel  lblDeclination = new JLabel( "");
	Date declinationDate = null;
	final NOAAMagneticDeclinationService noaa = new NOAAMagneticDeclinationService();
	
	public TestPanel( DocumentItem item) {
		init(  this);
	}
	
	private void init( final TestPanel testPanel) {
		
		endButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				testPanel.dispose();
			}
		});
		
		breadth.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processRequest( true);
			}
		});
		
		depth.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				processRequest( false);
			}
		});
		
		btnDeclination.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Date altDate = new Date( 2015-1900, 1, 1);
				Double declination;
				if ( declinationDate == null)
					declination = noaa.getDeclination( 41.5, -8.5);
				else
					declination = noaa.getDeclination( 41.5, -8.5, altDate);
				lblDeclination.setText( declination != null ? Double.toString( declination): "null");
				declinationDate = declinationDate == null ? altDate : null;
			}
		});
		

		GridBagLayout gridLayout = new GridBagLayout();
		setLayout( gridLayout);
		GridBagConstraints constraints = new GridBagConstraints();
		

		constraints.fill   = GridBagConstraints.VERTICAL;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.insets = new Insets( 15, 15, 15, 15);
		
		constraints.gridy = 0;
		constraints.gridx = 0;
		this.add( breadth, constraints);
		constraints.gridx = 2;
		this.add( depth, constraints);
		breadth.setEnabled( false);
		depth.setEnabled( false);
		
		constraints.gridy = 3;
		constraints.gridx = 0;
		this.add( btnDeclination, constraints);
		constraints.gridx = 2;
		this.add( lblDeclination, constraints);
		
		constraints.gridy = 30;
		constraints.gridx = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		this.add( endButton, constraints);
		
		pack();
		setPreferredSize( new Dimension( 600, 300));
		setResizable( true);
//		setModal( true);
		setLocationRelativeTo( TrackIt.getApplicationFrame());
		this.setVisible( true);
		
		EventManager.getInstance().register( this);
	}
	
	private void processRequest( boolean breadth) {
		if ( selectedItem.isActivity() ) {
			Activity activity = (Activity) selectedItem;
			new PauseDetectionPicCaseOperation().process( activity);
			PhotoPlacer placer = breadth ?
					new PhotoPlacerBreadthFirst(  activity) :
					new PhotoPlacerDepthFirst(  activity);
					placer.EstimateLocation( activity.getTrackpoints());
		} else {
			Course course = (Course) selectedItem;
			new PauseDetectionPicCaseOperation().process( course);
			PhotoPlacer placer = breadth ?
					new PhotoPlacerBreadthFirst( course) :
					new PhotoPlacerDepthFirst( course);
					placer.EstimateLocation( course.getTrackpoints());
		}
	}
	
	@Override
	public void process(Event event, DocumentItem item) {
		if ( event.equals( Event.ACTIVITY_SELECTED) || event.equals( Event.COURSE_SELECTED) ) {
			System.out.println( "SELECTION " + item.getDocumentItemName());
			selectedItem = item;
			breadth.setEnabled( true);
			depth.setEnabled( true);
		} 
		toFront();
	}
	
	@Override
	public void process(Event event, DocumentItem parent, List<? extends DocumentItem> items) {
		toFront();
	}
}
