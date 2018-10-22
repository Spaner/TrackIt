package com.trackit.business.writer.kml;

import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Trackpoint;

public enum KMLExtendedDataType {
	
	SPEED(		"Speed",	  "double"),
	HEART_RATE( "Heart Rate", "short"),
	POWER(      "Power",      "int"),
	CADENCE(    "Cadence",    "short"),
	GRADE(      "Grade",      "float");
	
	private String typeName;
	private String dataType;
	
	private static String extendedDataSchemaID = "TrackIt!"; 
	
	private KMLExtendedDataType( String name, String dataType) {
		this.typeName = name;
		this.dataType = dataType;
	}
	
	public static String getSchemaID() {
		return extendedDataSchemaID;
	}
	
	public String getDataLabel() {
		return Messages.getMessage( "kml.extendedDataLabel." + typeName.toLowerCase().replaceAll( "\\s", ""));
//		return typeName;
	}
	
	public String getDataType() {
		return dataType;
	}
	
	public String getDataTypeID() {
		return (extendedDataSchemaID + typeName).toLowerCase().replaceAll( "\\s", "");
	}
	
	public static KMLExtendedDataType lookup( String name) {
		for( KMLExtendedDataType type: values())
			if ( type.getDataTypeID().equalsIgnoreCase( name))
				return type;
		return null;
	}
	
	public String formatValue( Trackpoint trackpoint) {
		String formattedValue;
		switch ( this) {
			case HEART_RATE:
				formattedValue = String.format( "%d", trackpoint.getHeartRate());
				break;
			case POWER:
				formattedValue = String.format( "%d", trackpoint.getPower());
				break;
			case CADENCE:
				formattedValue = String.format( "%d", trackpoint.getCadence());
				break;
			case SPEED:
				formattedValue = Formatters.getDefaultDecimalFormat(3).format( trackpoint.getSpeed()*3.6);
				break;
			case GRADE:
				formattedValue = Formatters.getDefaultDecimalFormat(1).format( trackpoint.getGrade());
				break;
			default:
				formattedValue = "";
				break;
		}
		return formattedValue;
	}
	
	public void parseValue( String valueString, Trackpoint trackpoint) {
		switch ( this) {
		case HEART_RATE:
			trackpoint.setHeartRate( (short) Integer.parseInt( valueString));
			break;
		case POWER:
			trackpoint.setPower( Integer.parseInt( valueString));
			break;
		case CADENCE:
			trackpoint.setCadence( (short) Integer.parseInt( valueString));
			break;
		case GRADE:
		default:
			break;
		}
	}

}
