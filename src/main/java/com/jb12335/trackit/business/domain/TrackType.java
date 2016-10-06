package com.jb12335.trackit.business.domain;

import com.henriquemalheiro.trackit.business.common.Messages;

public enum TrackType {
	COURSE      ((short) 0),
	ACTIVITY 	((short) 1),
	UNKNOWN 	((short) -1);
	
	private short value;

	public static final TrackType DEFAULT = COURSE;
	
	private TrackType( short value) {
		this.value = value;
	}
	
	public short valueOf() {
		return value;
	}
	
	public static TrackType lookup( int valueToLookup) {
		for( TrackType type: values())
			if ( type.value == valueToLookup )
				return type;
		return DEFAULT;
	}
	
	private static final String[] messageCodes = {
		"trackType.course",	
		"trackType.activity",	
		"trackType.unknown"	
	};

	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}
