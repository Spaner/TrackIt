package com.trackit.presentation.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.trackit.business.common.Messages;

public class SynchroFormattedTextField extends JFormattedTextField {
		
	private Synchronizer<SynchroFormattedTextField> peers = null;
	Locale locale = Messages.getLocale();
	private int noDecimalDigits = 3;
	private Double maxValue = null, minValue = null;
	String id;
	
	public void setID( String id) {
		this.id = id;
	}
	
	public SynchroFormattedTextField() {
		super();
		init( null);
	}
	
	public SynchroFormattedTextField( Double value, int noDecimalDigits) {
		super();
		this.noDecimalDigits = noDecimalDigits;
		init( value);
	}
		
	public void setValue( Double value) {
		if ( value != null ) {
			if ( minValue != null) 
				value = Math.max( value, minValue);
			if ( maxValue != null )
				value = Math.min( value, maxValue);
		}
		super.setValue( value);
		if ( value == null )
			super.setText( null);
	}
	
	public void setLimits( Double minValue, Double maxValue) {
		if ( minValue != null && maxValue != null ) {
			if ( minValue < maxValue ) 
				storeLimits( minValue, maxValue);
		} else
			storeLimits( minValue, maxValue);
	}
	
	protected void storeLimits( Double minValue, Double maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		if ( peers != null ) {
			SynchroFormattedTextField peer;
			while( (peer = peers.next( this)) != null )
				peer.setLimits( minValue, maxValue);
		}
	}
	
	public void startSynchronizing( SynchroFormattedTextField peer) {
		if ( peers == null )
			peers = new Synchronizer<>();
		peer.peers = peers.add( this, peer);
	}
	
	public void stopSynchronizing( SynchroFormattedTextField peer) {
		if ( peer != null && peers != null ) {
			peers.remove( peer);
		}
	}
	
	public Double getDouble() {
		return extractValue();
	}
	
	protected Double extractValue() {
		Double value = null;
		if ( super.getValue() != null && ! getText().isEmpty() )
			try {
				value = (Double) super.getValue();
			} catch (Exception e) {
				Long temporary = (Long) super.getValue();
				value = (double) temporary;
			}
		return value;
	}
	
	private void init( Double value) {
		setFocusLostBehavior( JFormattedTextField.PERSIST);
		updateLocale( locale);
		super.setValue( value);
		final SynchroFormattedTextField thisField = this;
		addPropertyChangeListener( new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( "value".equals( evt.getPropertyName()) || "editValid".equals( evt.getPropertyName()) ) {
					Double value = extractValue();
					if ( value != null && !getText().isEmpty() ) {
						setValue( value);
//						propagate( Double.valueOf( parent.getValue().toString()));
					}
//					else 
//						propagate( value);
					propagate( value, thisField);
				}
			}
		});
	}
	
	protected void propagate( Double value) {
		propagate( value, this);
	}
	
	protected void propagate( Double value, SynchroFormattedTextField originator) {
		if ( peers != null ) {
			SynchroFormattedTextField peer;
			while( (peer = peers.next( this)) != null ) {
				peer.setValue( value);
			}
		}
	}
	
	public void setLocale( Locale locale) {
		Double value = extractValue();
		this.updateLocale( locale);
		setValue( value);
		if ( peers != null ) {
			SynchroFormattedTextField peer;
			while( (peer = peers.next( this)) != null ) {
				peer.updateLocale( locale);
				peer.setValue( value);
			}
		}
	}
	
	protected void updateLocale( Locale locale) {
		NumberFormat numberFormat = NumberFormat.getInstance( locale);
		numberFormat.setMinimumFractionDigits( noDecimalDigits);
		numberFormat.setMaximumFractionDigits( noDecimalDigits);
		numberFormat.setGroupingUsed( false);
		NumberFormatter numberFormatter = new NumberFormatter( numberFormat);
		super.setFormatterFactory(  new DefaultFormatterFactory( numberFormatter));
		this.locale = locale;
	}
	
}
