package com.trackit.presentation.utilities;

import com.toedter.calendar.JDateChooser;

import java.beans.PersistenceDelegate;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JTextField;

public class SynchronizedDateRangeChooser {
	
	private final JDateChooser fromDateChooser = new JDateChooser( (Date) null);
	private final JDateChooser toDateChooser   = new JDateChooser( (Date) null);
	
	protected Synchronizer<SynchronizedDateRangeChooser> peers = null;
	
	public SynchronizedDateRangeChooser() {
		init();
	}
	
	public Date fromDate() {
		return getDateRangeEnd( fromDateChooser.getDate(), toDateChooser.getDate(), true);
	}
	
	public Date toDate() {
		return getDateRangeEnd( fromDateChooser.getDate(), toDateChooser.getDate(), false);
	}
	
	public JDateChooser getFromDateChooser() {
		return fromDateChooser;
	}
	
	public JDateChooser getToDateChooser() {
		return toDateChooser;
	}
	
	public void setDates( Date from, Date to) {
		fromDateChooser.setDate( from);
		toDateChooser.setDate(   to);
	}
	
	public void setLocale( Locale locale) {
		fromDateChooser.setLocale( locale);
		toDateChooser.setLocale( locale);
		if ( peers != null ) {
			SynchronizedDateRangeChooser peer;
			while( (peer = peers.next( this)) != null ) {
				peer.fromDateChooser.setLocale( locale);
				peer.toDateChooser.setLocale( locale);
			}
		}
	}
	
	public void startSynchronizing( SynchronizedDateRangeChooser peer) {
		if ( peer != null ) {
			if ( peers == null )
				peers = new Synchronizer<>();
			peer.peers = peers.add( this, peer);
		}
	}
	
	public void stopSynchronizing( SynchronizedDateRangeChooser peer) {
		peers.remove( peer);
	}
	
	public static Date getDateRangeEnd( Date from, Date to, boolean getStartDate) {
		if ( from != null && to != null ) {
			from = zeroTimeDate( from);
			if ( getStartDate )
				return from;
			to   = zeroTimeDate( to);
			if ( to.compareTo( from) == 0 ) {
				Calendar helper = Calendar.getInstance();
				helper.setTime( to);
				helper.add( Calendar.DATE, 1);
				return helper.getTime();
			}
			return to;
		}
		else {
			if ( getStartDate ) {
				if ( from != null)
					from = zeroTimeDate( from);
				return from;
			}
			else {
				if ( to != null )
					to = zeroTimeDate( to);
				return to;
			}
		}
	}
	
	public static Date zeroTimeDate( Date dateToZeroTime) {
		if ( dateToZeroTime != null ) {
			Calendar date = Calendar.getInstance();
			date.setTime( dateToZeroTime);
			date.set( Calendar.MILLISECOND, 0);
			date.set( Calendar.SECOND, 0);
			date.set( Calendar.MINUTE, 0);
			date.set( Calendar.HOUR_OF_DAY, 0);
			return date.getTime();
		}
		return null;
	}
	
//	Date lastDate = null;
	
	protected void init() {
		fromDateChooser.getDateEditor().addPropertyChangeListener( new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( "date".equals( evt.getPropertyName()) ) {
					Date from = fromDateChooser.getDate();
					Date to   = toDateChooser.getDate();
					if ( from!= null && to != null ) {
						if ( from.compareTo( to) > 0 )
							fromDateChooser.setDate( to);
					}
					propagate();
				} else {
					if ( ( (JTextField) fromDateChooser.getDateEditor().getUiComponent()).getText().length() == 0 )
						propagate();
				}
			}
		});
		toDateChooser.getDateEditor().getUiComponent().addPropertyChangeListener( new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ( "date".equals( evt.getPropertyName()) ) {
					Date from = fromDateChooser.getDate();
					Date to = toDateChooser.getDate();
					if ( from != null && to != null ) {
						if ( from.compareTo( to) > 0 )
							toDateChooser.setDate( from);
					}
					propagate();
				}else {
					if ( ( (JTextField) toDateChooser.getDateEditor().getUiComponent()).getText().length() == 0 )
						propagate();
				}
			}
		});
	}
	
	protected void propagate() {
		if ( peers != null ) {
			Date from = fromDate();
			Date to   = toDate();
			SynchronizedDateRangeChooser peer;
			while( (peer = peers.next( this)) != null ) {
				peer.fromDateChooser.setDate( from);
				peer.toDateChooser.setDate( to);
			}
		}
	}
	
}
