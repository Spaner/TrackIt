package com.trackit.business.readwrite.csv;

public enum CSVField {
	
	UNKNOWN(            "NONE",                   "unknown"),
	NO(                 "ID",                     "No"),
	DATETIME(           "TIMESTAMP",              "Date,Time"),
	DATE(               "TIMESTAMP",              "Date"),
	TIME(               "TIMESTAMP",              "Time"),
	LATITUDE(           "LATITUDE",               "Latitude"),
	LONGITUDE(          "LONGITUDE",              "Longitude"),
	LONGTITUDE(         "LONGITUDE",              "Longtitude"), // Error on some CSV (Geonaute)
	ELEVATION(          "ALTITUDE",               "Elevation"),
	DISTANCE(           "DISTANCE",               "TotalDistance"),
	DISTANCE_FROM_LAST( "DISTANCE_FROM_PREVIOUS", "DistanceFromLast"),
	TIME_FROM_LAST(     "TIME_FROM_PREVIOUS",     "TimeFromLast"),
	SPEED(              "SPEED",                  "Speed"),
	HEART_RATE(         "HEART_RATE",             "Heartrate"),
	CADENCE(            "CADENCE",                "Cadence"),
	POWER(              "POWER",                  "Power"),
	TEMPERATURE(        "TEMPERTURE",             "Temperature"),
	GRADE(              "GRADE",                  "Grade"),
	BEARING(			"BEARING",				  "Bearing")	//12335: 2018-03-03 - On output only
	;

	public String name;     	// TrackIt name as per Field.def
	public String fieldName;  	// GPS Universal CSV with structure in first line field tag
								// (for reference see gpsbabel.org)
	
	private CSVField( String name, String fieldName) {
		this.name      = name;
		this.fieldName = fieldName;
	}
	
	public static CSVField lookupByName( String elementName) {
		CSVField element = UNKNOWN;
		for( CSVField current : CSVField.values() ){
			if ( current.name.equalsIgnoreCase( elementName)) {
				element = current;
				break;
			}
		}
		return element;
	}

	public static CSVField lookupByField( String fieldName) {
		CSVField element = UNKNOWN;
		for( CSVField current : CSVField.values() ){
			if ( current.fieldName.equalsIgnoreCase( fieldName)) {
				element = current;
				break;
			}
		}
		return element;
	}	
}
