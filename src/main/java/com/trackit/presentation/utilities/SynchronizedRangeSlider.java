/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SynchronizedRangeSlider extends RangeSlider {
	
	protected JTextField maxTextField = new JTextField( 4);
	protected JTextField minTextField = new JTextField( 4);
//	protected int        maximumValue;
	
	protected Synchronizer<SynchronizedRangeSlider> peers = null;
	
	private JTextField dummy = new JTextField( "0000");
	
	protected static int nseq = -1;
	protected int no;
	
	public SynchronizedRangeSlider() {
		super();
		init();
	}
	
	public SynchronizedRangeSlider( int minimum, int maximum) {
		super( minimum, maximum);
		super.setValue( minimum);
		super.setUpperValue( maximum);
//		maximumValue = maximum;
		maxTextField.setText( Integer.toString( maximum));
		minTextField.setText( Integer.toString( minimum));
		init();
	}
	
	public  JTextField getLowerEndTextField() {
		return minTextField;
	}
	
	public JTextField getUpperEndTextField() {
		return maxTextField;
	}
	
	public void startSynchronizing( SynchronizedRangeSlider peer) {
		if ( peer != null ) {
			if ( peers == null )
				peers = new Synchronizer<>();
			peer.peers = peers.add( this, peer);
			propagate( true);
		}
	}
	
	public void stopSyncrhonizing( SynchronizedRangeSlider peer) {
		if ( peer != null )
			peers.remove( peer);
	}
	
	protected void init() {
		no = ++nseq;
		maxTextField.setHorizontalAlignment( JTextField.RIGHT);
		minTextField.setHorizontalAlignment( JTextField.RIGHT);
		maxTextField.setPreferredSize( dummy.getPreferredSize());
		minTextField.setPreferredSize( dummy.getPreferredSize());
		maxTextField.setMinimumSize( dummy.getPreferredSize());
		minTextField.setMinimumSize( dummy.getPreferredSize());
		super.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				propagate( false);
			}
		});
		minTextField.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println( "min bax set to " + minTextField.getText());
					int value = Integer.parseInt( minTextField.getText());
					value = Math.max( value, getMinimum());
					setValue( value);
				} catch (Exception e2) {
					minTextField.setText( Integer.toString( getValue()));
				}
			}
		});
		maxTextField.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int value = Integer.parseInt( maxTextField.getText());
					if ( value > getMaximum() )
						setMaximum( value);
					setUpperValue( value);
				} catch (Exception e2) {
					maxTextField.setText( Integer.toString( getUpperValue()));
				}
			}
		});
	}
	
	protected void propagate( boolean all) {
		int minimum = getValue();
		int maximum = getUpperValue();
		int top     = getMaximum();
		int bottom  = getMinimum();
		minTextField.setText( Integer.toString( minimum));
		maxTextField.setText( Integer.toString( maximum));
		if ( peers != null ) {
			SynchronizedRangeSlider peer;
			while( (peer = peers.next( this)) != null ) {
				if ( all )
					peer.setMinimum( bottom);
				peer.setMaximum( top);
				peer.setValue( minimum);
				peer.setUpperValue( maximum);
				peer.minTextField.setText( Integer.toString( minimum));
				peer.maxTextField.setText( Integer.toString( maximum));
			}
		}
	}
	
	public String toString() {
		return getMinimum() + "  " +  getValue() + "  " + getUpperValue() + "  " + getMaximum();
	}
}
