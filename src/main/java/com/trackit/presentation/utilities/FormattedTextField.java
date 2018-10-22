package com.trackit.presentation.utilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class FormattedTextField extends JFormattedTextField {
	
	
	private Synchronizer<FormattedTextField> peers = null;
	private int id;
	
	FormattedTextField( Double value, int id) {
		super();
		this.id = id;
		init( value);
	}
	
	public void setValue( Double value) {
		super.setValue( value);
		if (value == null )
			super.setText( null);
	}
	
	public void startSynchro( FormattedTextField peer) {
		if ( peers == null )
			peers = new Synchronizer<>();
		peer.peers = peers.add( this, peer);
	}
	
	private  void init( Double value) {
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMinimumFractionDigits( 3);
		numberFormat.setMaximumFractionDigits( 3);
		numberFormat.setGroupingUsed( false);
		NumberFormatter numberFormatter = new NumberFormatter( numberFormat);
		super.setFormatterFactory(  new DefaultFormatterFactory( numberFormatter));
		setValue( value);
		final int fId = this.id;
		addPropertyChangeListener( new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( "value".equals( evt.getPropertyName()) || "editValid".equals( evt.getPropertyName()) ) {
					System.out.println( "I´m " + fId + " o valor é " + getValue() + " e o texto é: " + getText());
					System.out.println(evt.getPropertyName());
					System.out.println( evt.getOldValue() + " " + evt.getNewValue());
					if ( getValue() != null && !getText().isEmpty() ) {
						System.out.println( "I'm " + fId + "\t myvalue: " + getValue().toString());
						setValue( getValue());
						propagate( Double.valueOf( getValue().toString()));
					} else
						propagate( null);
				}
			}
		});
	}
	
	private void propagate( Double value) {
		propagate( value, this);
	}
	
	private void propagate( Double value, FormattedTextField originator) {
		if ( peers != null ) {
			FormattedTextField peer;
			while( (peer = peers.next( this)) != null ) {
				System.out.println( this.id + " Propagating " + value + " to " + peer.id);
				peer.setValue( value);
			}
		}
	}
	
	private void propagateOld( Double value, FormattedTextField originator) {
		if ( peers != null && peers.mayPropagate() ) {
			Iterator<FormattedTextField> iterator = peers.iterator();
			FormattedTextField peer;
			while ( iterator.hasNext() ) {
				peer = iterator.next();
				if ( peer != originator ) {
					System.out.println( this.id + " Propagating " + value + " to " + peer.id);
					peer.setValue( value);
				}
			}
			peers.endPropagation();
		}
	}
	
	protected Double extractValue() {
		Double value = null;
		if ( getValue() != null && !getText().isEmpty() ) {
			System.out.println( "Value = " + getValue());
			try {
				value = (Double) getValue();
			} catch (Exception e) {
				System.out.println( "exceprion");
				Long o = (Long) getValue();
				value = (double) o;
			}
		}
		return value;
	}
	
	public void setLocale( Locale locale) {
		Double value = extractValue();
		this.updateLocale( locale);
		setValue( value);
		if ( peers!= null ) {
			FormattedTextField peer;
			while( (peer = peers.next( this)) != null ) {
					peer.updateLocale( locale);
					peer.setValue( value);
				}
		}
	}

	public void setLocaleOld( Locale locale) {
		Double value = extractValue();
		this.updateLocale( locale);
		setValue( value);
		if ( peers!= null && peers.mayPropagate() ) {
			Iterator<FormattedTextField> iterator = peers.iterator();
			FormattedTextField peer;
			while( iterator.hasNext() ) {
				peer = iterator.next();
				if ( peer != this) {
					peer.updateLocale( locale);
					peer.setValue( value);
				}
			}
			peers.endPropagation();
		}
	}

	protected void updateLocale( Locale locale) {
//		System.out.println( "Updating locale to " + locale.toString());
		NumberFormat numberFormat = NumberFormat.getInstance( locale);
		numberFormat.setMinimumFractionDigits( 3);
		numberFormat.setMaximumFractionDigits( 3);
		numberFormat.setGroupingUsed( false);
		NumberFormatter numberFormatter = new NumberFormatter( numberFormat);
		super.setFormatterFactory(  new DefaultFormatterFactory( numberFormatter));
	}
	
}
