package com.trackit.business.database;

public class MediaLoader{
	
	String[] tables = { "Audio", "Audio", "Video", "Video", "Video", "Note", "Note" };
	String[] paths  = { "Path1", "Path2", "Path3", "Path4", "Path5", "Path6", "Path7" };
	String[] names  = { "Audio1", "Audio2", "Video1", "Video2", "Video3", "Note1", "Noteo2"};
	String[] docs   = { "/Users/brisson/OneDrive/Documents/Temp/Podbrdo.csv",
						"/Users/brisson/OneDrive/Documents/Temp/Sintra.csv",
						"/Users/brisson/OneDrive/Documents/Temp/Septimvs.csv",
						"/Users/brisson/OneDrive/Documents/Temp/07 02.gpx",
						"/Users/brisson/OneDrive/Documents/Temp/07 02.gpx",
						"/Users/brisson/OneDrive/Documents/Temp/07 02.gpx",
						"/Users/brisson/OneDrive/Documents/Temp/Podbrdo.csv" };
	String[] tracks = { "Running", "Guincho - Sintra", "Leg 3", "07 2", "07 2", "07 2", "Running"};
	
	public MediaLoader( Database db) {
		init( db);
	}
	
	private void init( Database db) {
		String sqlStart = "INSERT OR REPLACE INTO ";
		String sqlEnd   = " (Filepath, Name, Longitude, "
				+ "Latitude, Altitude, Container, ParentName) VALUES (";
		String sql;
		for( int i=0; i< tables.length; i++) {
			System.out.println( "trying " + i);
			sql = sqlStart + tables[i] + sqlEnd +
					"'" + paths[i] + "'," +
					"'" + names[i] + "'," +
					"'" + "-8.5" + "'," +
					"'" + "41.32" + "'," +
					"'" + "56.76" + "'," +
					"'" + docs[i] + "'," +
					"'" + tracks[i] + "')";
			System.out.println( sql);
			db.executeUpdateRequest( sql);
		}
	}

}
