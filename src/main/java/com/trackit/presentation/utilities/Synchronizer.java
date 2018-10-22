package com.trackit.presentation.utilities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import com.garmin.fit.Decode.RETURN;

public class Synchronizer<T> {

	private HashSet<T> peers     = new HashSet<>();
	private boolean    propagate = true;
	private T  	       value;
	private T 		   caller;
	private Iterator<T> iterator = null;
	
	public Synchronizer() {}
	
	public Synchronizer<T> add( T originator, T peer) {
		if ( peer != null && originator != null && peer != originator ) {
			peers.add( originator);
			peers.add( peer);
		}
		return this;
	}
	
	public void remove( T peer) {
		if ( peer != null )
			peers.remove( peer);
	}
	
	public Iterator<T> iterator() {
		System.out.println( "Resquesting iterator - result " + (propagate?"Ok":"denied"));
		if ( propagate ) {
			propagate = false;
			return peers.iterator();
		}
		return null;
	}
	
	public T next( T caller) {
		if ( iterator == null ) {
			if ( caller != null ) {
				propagate = false;
				this.caller = caller;
				iterator = peers.iterator();
				return getNext();
			}
		} else {
			if ( caller == this.caller )
				return getNext();
		}
		return null;
	}
	
	private T getNext() {
		if ( iterator.hasNext() ) {
			T answer = iterator.next();
			if ( answer == caller)
				return getNext();
			return answer;
		}
		caller = null;
		iterator = null;
		propagate = true;
		return null;
	}
	
	public boolean mayPropagate() {
		return propagate;
	}
	
	public void endPropagation() {
		propagate = true;
	}
	
	public void setValue( T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
}
