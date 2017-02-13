package com.henriquemalheiro.trackit.business.common;

public enum Direction {
	DIRECT, REVERSE;
	
	private static final String[] messageCodes = {"direction.direct", "direction.reverse"};
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}