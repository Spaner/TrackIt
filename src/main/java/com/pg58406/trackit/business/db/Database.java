/*
 * This file is part of Track It!.
 * Copyright (C) 2015 Pedro Gomes
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
package com.pg58406.trackit.business.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sqlite.OSInfo;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.pg58406.trackit.business.domain.PhotoContainer;
import com.pg58406.trackit.business.domain.Picture;

public class Database {

	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	private static Database database;
	private static int databaseVersion = 3;						// 12335: 2016-06-06
	
	private static String databaseName = "jdbc:sqlite:TrackIt.db";
	private Connection c               = null;
	private Statement  stmt            = null;
	private ResultSet  res             = null;
	private boolean    isConnected     = false;

	private Path      workingDirectory = Paths.get(System.getProperty("user.dir"));
																		// 12335: 2015-09-11
	
public Database() {
		initDb();
	}

	static {
		database = new Database();
	}

	public synchronized static Database getInstance() {
		return database;
	}
	
	public void initDb() {
		try {
			Class.forName("org.sqlite.JDBC");
			
			// FOR MIGRATION TESTING - Start
			if ( Files.exists( Paths.get("dbVersion.txt"), LinkOption.NOFOLLOW_LINKS) ) {
				System.out.println( "dbVersion exists");
				BufferedReader reader = new BufferedReader( new FileReader( new File( "dbVersion.txt")));
				String versionToStart = reader.readLine();
				reader.close();
				int startVersion = (versionToStart.charAt(0) == '3') ? 3 : 2;
				System.out.println( "\nDB START VERSION: " + startVersion +"\n");
				if ( startVersion == 2 )
					Files.copy( Paths.get( "TrackIt-V2.db"), Paths.get( "TrackIt.db"), StandardCopyOption.REPLACE_EXISTING);
				else
					Files.copy( Paths.get( "TrackIt-V3.db"), Paths.get( "TrackIt.db"), StandardCopyOption.REPLACE_EXISTING);
			}
			// FOR MIGRATION TESTING - End
			
			if ( doesDatabaseTableExists( "GPSFiles") ) {
				// A database exists, determine its version
				int dbVersion = 0;
				if ( doesDatabaseTableExists( "DBVersion") ) {
					// For versions 3 and up a table stores the version number
					openConnection();
					res = stmt.executeQuery(
							"SELECT VersionNo,CreationDate,SportsCatalogDate FROM DBVersion");
					if ( res.next() ) {
						dbVersion = (int) res.getDouble(1);
						Date date = new Date( res.getLong( 2));
						long sportsLastUpdate = res.getLong( 3);
						String sportsLastUpdateDate = (sportsLastUpdate != 0 ?
								(new Date( sportsLastUpdate)).toString() : "none");
						logger.info( "DB created: " + date + "\tDB Version: " + dbVersion);
						logger.info( "Sports catalog issue date: " + sportsLastUpdateDate);
					}
					closeConnection();
				}
				// Its either version 1 or 2
				else {
					// Version 2 has the Sport and SubSport tables, check the first
					if ( doesDatabaseTableExists( "Sport"))
						dbVersion = 2;
					else
						dbVersion = 1;
				}
				// Upgrade DB if DB version is not the current version
				if ( dbVersion < databaseVersion ) {
					upgradeGPSFiles( dbVersion);
					upgradeMediaTable( "Audio", dbVersion);
					upgradeMediaTable( "Picture", dbVersion);
					upgradeMediaTable( "Video", dbVersion);
					upgradeVersionTable( dbVersion);
					upgradeDesktopTable( dbVersion);
				}
			}
			else
				// No database, create from scratch
				createDatabaseCurrentTables();
						
		} catch (Exception e ) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("initDB");
			System.exit(0);
		}
	}
	
// ########################  OPERATION SUPPORT   ###########################################
// 12335: 2015-09-12 
	private void openConnection() {
		try {
			c = DriverManager.getConnection(databaseName);
			stmt = c.createStatement();
			stmt.executeUpdate("PRAGMA foreign_keys = ON");
			isConnected = true;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("openConnection");
			closeConnection();
		}
	}
	
	private void closeConnection() {
		try {
			if ( res != null )
				res.close();
			if ( stmt != null )
				stmt.close();
			if ( c != null )
				c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());		
			logger.error("closeConnection");
		} finally {
			res  = null;
			stmt = null;
			c    = null;
			isConnected = false;
		}
	}
	
	public boolean connected() {
		return isConnected;
	}
	
	// 12335: 2015-09-13 - Takes a SQL update request opening a connection before executing
	//                     and closing it afterwards
	//                     Returns the returning result or -1 if there was an error
	private int executeUpdateRequest( String sql) {
		int result = -1;
		try {
			openConnection();
			result = stmt.executeUpdate(sql);
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("executeUpdateRequest");
			logger.error( sql);
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		return result;
	}

	// 12335: 2015-09-12 - suport: gets the foreign keys state (ON or OFF)
	private boolean foreignKeyState() {
		if ( isConnected ) {
			try {
				res = stmt.executeQuery("PRAGMA foreign_keys");
				res.next();
				if ( res.getInt(1) == 1)
					return true;
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			}
		}
		return false;
	}

	// 12335: 2015-09-03, support needed by database version upgrade   #################
	private Boolean doesDatabaseTableExists( String tableName) {
		openConnection();
		if ( connected() ) {
			try {
				res = c.getMetaData().getTables(null, null, tableName, new String [] {"TABLE"});
				return !res.isAfterLast();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}			
		}
		return false;
	}

// ########################  OPERATION SUPPORT   - End   ###################################

// ########################  CURRENT TABLES CREATION   #####################################   
	
	private void createDatabaseCurrentTables() throws SQLException {
		createGPSFilesTable();
		createMediaTable( "Audio");
		createMediaTable( "Picture");
		createMediaTable( "Video");
		createVersionTable();						// 2016-06-07: 12335
		createDesktopTable();						// 2016-06-07: 12335
		createSportsTable();
		createSubSportsTable();
		addSportsAndSubsports();
	}
	
	// Version 3, 2016-09-13, 12335
	private void createGPSFilesTable() {
		String sql = "CREATE TABLE IF NOT EXISTS GPSFiles "
				+ "(Filepath TEXT NOT NULL,"
				+ "Name TEXT NOT NULL," 
				+ "IsActivity SMALLINT DEFAULT -1,"
				+ "Sport SMALLINT DEFAULT -1,"
				+ "SubSport SMALLINT DEFAULT -1,"
				+ "StartTime INTEGER DEFAULT 0,"
				+ "EndTime INTEGER DEFAULT 0,"
				+ "TotalTime INTEGER DEFAULT 0,"
				+ "MovingTime INTEGER DEFAULT 0,"
				+ "Distance DOUBLE DEFAULT 0,"
				+ "Ascent DOUBLE DEFAULT 0,"
				+ "Descent DOUBLE DEFAULT 0,"
				+ "MinLongitude DOUBLE NOT NULL,"
				+ "MaxLongitude DOUBLE NOT NULL,"
				+ "MinLatitude DOUBLE NOT NULL,"
				+ "MaxLatitude DOUBLE NOT NULL, "
				+ "PRIMARY KEY(Filepath, Name))";
		executeUpdateRequest(sql);
	}
	
	// Version 3, 2015-09-13, 12335
	private void createMediaTable(String media) {
		String sql = "CREATE TABLE IF NOT EXISTS " + media
				+ "(Filepath TEXT NOT NULL,"
				+ "Name TEXT,"
				+ "Longitude DOUBLE,"
				+ "Latitude DOUBLE,"
				+ "Altitude DOUBLE,"
				+ "Container TEXT NOT NULL, "
				+ "ParentName TEXT NOT NULL,"
				+ "PRIMARY KEY (Filepath, Container, ParentName) "
				+ "FOREIGN KEY (Container,ParentName) REFERENCES GPSFiles(Filepath,Name) "
				+ "ON UPDATE CASCADE ON DELETE CASCADE)";
		executeUpdateRequest(sql);
	}
	
	// Version 3, 2015-09-16, 12335
	private void createVersionTable() {
		executeUpdateRequest("CREATE TABLE IF NOT EXISTS DBVersion "
				+ "(VersionNo INTEGER DEFAULT " + databaseVersion + ", "
				+  "CreationDate INTEGER DEFAULT 0, "
				+  "SportsCatalogDate INTEGER DEFAULT 0)" );
	}
	
	// Version 3, 2016-06-09, 12335
	private void createDesktopTable() {
		executeUpdateRequest("CREATE TABLE IF NOT EXISTS Desktop "
				+ "(DocumentName TEXT NOT NULL,"
				+ " Filepath TEXT NOT NULL,"
				+ " TrackName TEXT NOT NULL,"
				+ " IsActivity SMALLINT DEFAULT -1,"
				+ " Sport SMALLINT DEFAULT -1,"
				+ " SubSport SMALLINT DEFAULT -1,"
				+ " Folder SMALLINT DEFAULT 1,"
				+ " Loaded SMALLINT DEFAULT 0,"
				+ " PRIMARY KEY(TrackName,Filepath,IsActivity)" 
				+ ")");
	}
		
	// Version 2, current version
	private void createSportsTable() {
		String sql = "CREATE TABLE IF NOT EXISTS Sport "
				+ "(SportID SMALLINT NOT NULL,"
				+ "Name TEXT NOT NULL,"
				+ "PRIMARY KEY (SportID))";
		executeUpdateRequest(sql);
	}
	
	// Version 2, current version
	private void createSubSportsTable() {
		String sql = "CREATE TABLE IF NOT EXISTS SubSport "
				+ "(TableID SHORT NOT NULL,"
				+ "SportID SMALLINT NOT NULL,"
				+ "SportName TEXT NOT NULL,"
				+ "SubSportID SMALLINT NOT NULL,"
				+ "SubSportName TEXT NOT NULL,"
				+ "DefaultAverageSpeed DOUBLE,"
				+ "MaximumAllowedSpeed DOUBLE,"
				+ "PauseThresholdSpeed DOUBLE,"
				+ "DefaultPauseDuration LONG,"
				+ "FollowRoads BOOLEAN,"
				+ "JoinMaximumWarningDistance DOUBLE,"
				+ "JoinMergeDistanceTolerance DOUBLE,"
				+ "JoinMergeTimeTolerance LONG,"
				+ "GradeLimit DOUBLE,"
				+ "CurrentDefaultAverageSpeed DOUBLE,"
				+ "CurrentMaximumAllowedSpeed DOUBLE,"
				+ "CurrentPauseThresholdSpeed DOUBLE,"
				+ "CurrentDefaultPauseDuration LONG,"
				+ "CurrentFollowRoads BOOLEAN,"
				+ "CurrentJoinMaximumWarningDistance DOUBLE,"
				+ "CurrentJoinMergeDistanceTolerance DOUBLE,"
				+ "CurrentJoinMergeTimeTolerance LONG,"
				+ "CurrentGradeLimit DOUBLE,"
				+ "PRIMARY KEY (TableID)"
				+ "FOREIGN KEY (SportID) REFERENCES Sport(SportID))";
		executeUpdateRequest(sql);
	}

// ########################  CURRENT TABLES CREATION   - End   #############################   

	
// ########################  UPGRADE FROM PREVIOUS VERSIONS   ##############################   

	// 2016-06-07: 12335
	private void upgradeGPSFiles( int fromVersion) {
		executeUpdateRequest( "ALTER TABLE GPSFiles RENAME TO Temp");
		createGPSFilesTable();
		String list = "Filepath,Name,";
		if ( fromVersion == 2 )
			list += "Sport,SubSport,";
		list += "MinLongitude,MaxLongitude,MinLatitude,MaxLatitude";
		String sql = "INSERT OR REPLACE INTO GPSFiles (" + list + ") SELECT " + list+ " FROM temp";
		int code = executeUpdateRequest( sql);
		executeUpdateRequest( "DROP TABLE Temp");
	}
	
	// 2016-06-07: 12335
	private void upgradeMediaTable( String mmediaTableName, int fromVersion) {
		// From version 2 to version 3 we only need to copy
		// (the difference was the foreign keys definition)
	}
	
	// 2016-06-07: 12335
	private void upgradeVersionTable( int fromVersion) {
		if ( fromVersion < 3 )
			createVersionTable();
		openConnection();
		if ( connected() ) {
			try {
				PreparedStatement pstmt = c.prepareStatement("INSERT INTO DBVersion "
						+ "(VersionNo,CreationDate) VALUES (?,?)");
				pstmt.setDouble(1, databaseVersion);
				pstmt.setLong(2, (new Date()).getTime());
				pstmt.executeUpdate();
				pstmt.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				return;
			} finally {
				closeConnection();
			}
		}
	}
	
	// 2016-06-07: 12335
	private void upgradeDesktopTable( int fromVersion) {
		if ( fromVersion < 3 ) {
			createDesktopTable();
			openConnection();
			try {
				res = stmt.executeQuery( "SELECT Filepath,Name,Sport,SubSport FROM GPSFiles");
				String sql ="INSERT INTO Desktop (DocumentName,TrackName,Filepath,Sport,SubSport,Folder) "
						+   "VALUES (?,?,?,?,?,1)";
				PreparedStatement pst = c.prepareStatement( sql);
				while( res.next() ) {
					pst.setString( 1, "DUMMY");
					pst.setString( 2, res.getString( "Name"));
					pst.setString( 3, res.getString( "Filepath"));
					pst.setShort(  4, res.getShort( "Sport"));
					pst.setShort(  5, res.getShort( "SubSport"));
					pst.executeUpdate();
				}
				closeConnection();
//				openConnection();
//				String sql = "INSERT INTO Desktop (DocumentName,Filepath) VALUES (?,?)";
//				PreparedStatement pst = c.prepareStatement( sql);
//				for( String s: filesList ) {
//					pst.setString( 1, "DUMMY");
//					pst.setString( 2, s);
//					pst.executeUpdate();
//				}
//				closeConnection();
			} catch (Exception e ) {
				logger.error( e.getClass().getName() + ": " + e.getMessage());
				System.exit( 0);
			}
		}
	}
	
	// ########################  UPGRADE FROM PREVIOUS VERSIONS   - End   ######################   

	// ########################  SPORTS AND SUBSPORTS SUPPORT   ################################   
	
	private void addSportsAndSubsports() throws SQLException{
		List<SportType> sportList = Arrays.asList(SportType.values()); 
			for(SportType sportType : sportList){
				addSport(sportType);
			}
		
			List<SubSportType> subSportList = Arrays.asList(SubSportType.values()); 
			int tableID = 0;
			for(SubSportType subSportType : subSportList){
				addSubSport(subSportType, tableID);
				tableID++;
			}
		
	}
	
	public void addSport(SportType sport){
		//Connection c = null;
		
		openConnection();
		try {
			String sql = "SELECT * FROM Sport WHERE SportID='" + sport.getSportID()
					+ "'";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if ( exists ) {
				sql = "UPDATE Sport SET Name=? " 
						+ "WHERE SportID=?";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setShort(1, sport.getSportID());
				statement.setString(2, sport.getName());
				statement.executeUpdate();
				statement.close();
			}
			else{
				sql = "INSERT OR REPLACE INTO Sport (SportID, Name) VALUES (?,?)";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setShort(1, sport.getSportID());
				statement.setString(2, sport.getName());
				statement.executeUpdate();
				statement.close();
			}
		
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("AddSport");
			
		}
		finally{
			closeConnection();
		}
	}

	
	public void addSubSport(SubSportType subSport, int tableID){
		openConnection();
		try {
			String sql = "SELECT * FROM SubSport WHERE SubSportID='" + subSport.getSubSportID() + "' AND SportID='" + subSport.getSportID()
					+ "'";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if(exists){
				/*sql = "UPDATE SubSport SET SubSportID=?, SportID=?, Name=?, DefaultAverageSpeed=?, MaximumAllowedSpeed=?, PauseThresholdSpeed=?, DefaultPauseDuration=?, "
						+ "FollowRoads=?, JoinMaximumWarningDistance=?, JoinMergeDistanceTolerance=?, JoinMergeTimeTolerance=?, "
						+ "CurrentDefaultAverageSpeed=?, CurrentMaximumAllowedSpeed=?, CurrentPauseThresholdSpeed=?, CurrentDefaultPauseDuration=?, "
						+ "CurrentFollowRoads=?, CurrentJoinMaximumWarningDistance=?, CurrentJoinMergeDistanceTolerance=?, CurrentJoinMergeTimeTolerance=? "
						+ "WHERE TableID=?" ;
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setShort(1, subSport.getSubSportID());
				statement.setShort(2, subSport.getSportID());
				statement.setString(3, subSport.getName());
				statement.setDouble(4, subSport.getDefaultAverageSpeed());
				statement.setDouble(5, subSport.getMaximumAllowedSpeed());
				statement.setDouble(6, subSport.getPauseThresholdSpeed());
				statement.setLong(7, subSport.getDefaultPauseDuration());
				statement.setBoolean(8, subSport.getFollowRoads());
				statement.setDouble(9, subSport.getJoinMaximumWarningDistance());
				statement.setDouble(10, subSport.getJoinMergeDistanceTolerance());
				statement.setDouble(11, subSport.getJoinMergeTimeTolerance());
				statement.setDouble(12, subSport.getDefaultAverageSpeed());
				statement.setDouble(13, subSport.getMaximumAllowedSpeed());
				statement.setDouble(14, subSport.getPauseThresholdSpeed());
				statement.setLong(15, subSport.getDefaultPauseDuration());
				statement.setBoolean(16, subSport.getFollowRoads());
				statement.setDouble(17, subSport.getJoinMaximumWarningDistance());
				statement.setDouble(18, subSport.getJoinMergeDistanceTolerance());
				statement.setDouble(19, subSport.getJoinMergeTimeTolerance());
				statement.setShort(20, (short) tableID);
				statement.executeUpdate();
				statement.close();*/
			}
			else{
				sql = "SELECT Name FROM Sport WHERE SportID='"+subSport.getSportID()+"'";
				PreparedStatement statement = c.prepareStatement(sql);
				ResultSet rs = statement.executeQuery();
				String sportName = rs.getString("Name");
				
				sql = "INSERT OR REPLACE INTO SubSport (TableID, SportID, SportName, SubSportID, SubSportName, "
						+ "DefaultAverageSpeed, MaximumAllowedSpeed, PauseThresholdSpeed, DefaultPauseDuration, "
						+ "FollowRoads, JoinMaximumWarningDistance, JoinMergeDistanceTolerance, JoinMergeTimeTolerance, GradeLimit, "
						+ "CurrentDefaultAverageSpeed, CurrentMaximumAllowedSpeed, CurrentPauseThresholdSpeed, CurrentDefaultPauseDuration, "
						+ "CurrentFollowRoads, CurrentJoinMaximumWarningDistance, CurrentJoinMergeDistanceTolerance, CurrentJoinMergeTimeTolerance, CurrentGradeLimit) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				statement = c.prepareStatement(sql);
				
				statement.setShort(1, (short) tableID);
				statement.setShort(2, subSport.getSportID());
				statement.setString(3, sportName);
				statement.setShort(4, subSport.getSubSportID());
				statement.setString(5, subSport.getName());
				statement.setDouble(6, subSport.getDefaultAverageSpeed());
				statement.setDouble(7, subSport.getMaximumAllowedSpeed());
				statement.setDouble(8, subSport.getPauseThresholdSpeed());
				statement.setLong(9, subSport.getDefaultPauseDuration());
				statement.setBoolean(10, subSport.getFollowRoads());
				statement.setDouble(11, subSport.getJoinMaximumWarningDistance());
				statement.setDouble(12, subSport.getJoinMergeDistanceTolerance());
				statement.setLong(13, subSport.getJoinMergeTimeTolerance());
				statement.setDouble(14, subSport.getGradeLimit());
				statement.setDouble(15, subSport.getDefaultAverageSpeed());
				statement.setDouble(16, subSport.getMaximumAllowedSpeed());
				statement.setDouble(17, subSport.getPauseThresholdSpeed());
				statement.setLong(18, subSport.getDefaultPauseDuration());
				statement.setBoolean(19, subSport.getFollowRoads());
				statement.setDouble(20, subSport.getJoinMaximumWarningDistance());
				statement.setDouble(21, subSport.getJoinMergeDistanceTolerance());
				statement.setLong(22, subSport.getJoinMergeTimeTolerance());
				statement.setDouble(23, subSport.getGradeLimit());
				statement.executeUpdate();
				statement.close();
			}

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("Add SubSport");
			
		}
		finally{
			closeConnection();
		}
		
	}

// ########################  SPORTS AND SUBSPORTS SUPPORT   - End   ########################   

//########################### START AND STOP ######################################
	
	// 12335: 2015-09-17 - 
	public ArrayList<String> initFromDb( int folderID) {
		ArrayList<String> filepathList = new ArrayList<String>();
		openConnection();
		if ( connected() ) {
			try {
				res = stmt.executeQuery("SELECT Filepath FROM Desktop WHERE Folder='"
						+ folderID + "'");
				while ( res.next() ) {
					String filename = res.getString(1);
					if ( (new File( filename)).exists() ) {
						if ( !filepathList.contains( filename)) {
							filepathList.add( filename);
						}						
					}
				}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());			
			} finally {
				closeConnection();
			}
			for( String filename : filepathList)
				executeUpdateRequest("UPDATE Desktop SET Loaded=1 WHERE Filepath='"
						+ filename +"' AND Folder='" + folderID + "'");				
		}
		return filepathList;
	}
	
	// 12335: 2015-09-14 - Close the database
	// Update document working set and check/fix documents for inconsistencies
	// 12335: 2016-06-08 - All tracks (activities and courses) are now registered
	//                     instead of document names and filepaths only
	// 12335: 2016-06-09 - Provision to store library documents besides workspace documents
	public void closeDatabase( List<GPSDocument> documents) {
		executeUpdateRequest("DELETE FROM Desktop WHERE Loaded=1");
		DocumentManager manager = DocumentManager.getInstance();
		for( GPSDocument doc: documents) {
			if ( (new File(doc.getFileName())).exists() ) {
				int folderID = manager.getFolder( doc).getFolderID();
				for( Activity activity : doc.getActivities())
					executeUpdateRequest(
						"INSERT INTO Desktop (DocumentName,Filepath,TrackName,Sport,SubSport,IsActivity,Folder) "
					  + " Values ('" + doc.getName() 
					  + "','" + doc.getFileName()
					  + "','" + activity.getName()
					  + "','" + activity.getSport().getSportID()
					  + "','" + activity.getSubSport().getSubSportID()
					  + "','1"
					  + "','" + folderID
					  + "')");
				for( Course course : doc.getCourses() )
					executeUpdateRequest(
						"INSERT INTO Desktop (DocumentName,Filepath,TrackName,Sport,SubSport,IsActivity,Folder) "
					  + " Values ('" + doc.getName() 
					  + "','" + doc.getFileName()
					  + "','" + course.getName()
					  + "','" + course.getSport().getSportID()
					  + "','" + course.getSubSport().getSubSportID()
					  + "','0"
					  + "','" + folderID
					  + "')");
			}
		}
	}

//########################### START AND STOP (END) ################################
		
//########################### DOCUMENTS ###########################################
	
	// 12335: 2015-10-03 - update loaded state in the Desktop table	
	public int setDocumentUnloaded(GPSDocument document) {
		return setDocumentLoadState( document, false);
	}
	
	public int setDocumentLoaded( GPSDocument document) {
		return setDocumentLoadState( document, true);
	}
	
	private int setDocumentLoadState( GPSDocument document, boolean state) {
		return executeUpdateRequest("UPDATE TrackItDesktop SET Loaded=" +
				(state?"1":"0") + " WHERE Filepath='" + document.getFileName() + "'");
	}
	
	// 12335: 2015-10-03 - Removes document from the Desktop table
	public int deleteFromDocumentsList( GPSDocument document, boolean isLoaded) {
		return executeUpdateRequest("DELETE FROM TrackItDesktop Where Filepath='" + 
				document.getFileName() + "' AND Loaded=" + (isLoaded?"1":"0"));
	}

	// 12335: 2015-09-30 - update whole document
	public void updateDB( GPSDocument document, boolean startFromScratch) {
		String sql;
		String docFilename = document.getFileName();
		File file = new File(docFilename);
		List <String> activitiesInDB, coursesInDB;
		
		System.out.println("PHASE 1");
		// PHASE 1: Overwrite cleanup
		// Remove everything in the DB if 
		// 1) Export to a file that is overwritten
		// 2) Save to file overwriting a file that was not the original document's file
		if ( startFromScratch ) {
			sql = "DELETE FROM Desktop WHERE Filepath='" + docFilename + "'";
			executeUpdateRequest(sql);
			sql = "DELETE FROM GPSFiles WHERE Filepath='" + docFilename + "'";
			executeUpdateRequest(sql);
		}
		
		System.out.println("PHASE 2");
		// PHASE 2: Rename items if not overwriting, the document is already in the DB and 
		// items were renamed
		// Check if file is referenced in the DB
		if ( !startFromScratch ){
			boolean exists = false;
			try {
				sql = "SELECT * FROM GPSFiles WHERE Filepath='" + docFilename + "'";
				System.out.println(sql);
				openConnection();
				exists = !stmt.executeQuery(sql).isAfterLast();
				System.out.println("exists =" + exists);
				closeConnection();
				System.out.println("closed connection");
			} catch (Exception e) { e.printStackTrace();}
			if ( exists ) {
				System.out.println("moving on");
				// Handle document renaming
				if ( document.wasRenamed() ) {
					System.out.println("updating name");
					sql = "UPDATE TrackItDesktop SET Name ='" + document.getName()
					+ "' WHERE Name='" + document.getOldName() + "' AND Filepath='"
					+ docFilename + "'";
					System.out.println("executing " + sql);
					if ( executeUpdateRequest(sql) == -1 )
						return;
					document.setOldNameWhenSaving();
				}
				System.out.println("activities and courses rename");
				// Handle activities and courses renaming
				// To avoid name collisions in the DB (e.g., when two names were swapped)
				// assign unique provisional names
				activitiesInDB = getActivityNames(docFilename);
				coursesInDB = getCourseNames(docFilename);
				String baseName = "_db_kslp_temp_";
				int index = -1;
				// set provisional names
				for( Activity a: document.getActivities() )
					if ( a.wasRenamed() && activitiesInDB.contains(a.getOldName())) 
						renameTrack(a.getOldName(), baseName+Integer.toString(++index),
									docFilename, true);
				for( Course c: document.getCourses() )
					if ( c.wasRenamed() && coursesInDB.contains(c.getOldName()))
						renameTrack(c.getOldName(), baseName+Integer.toString(++index),
									docFilename, false);
				// Now we can assign the definitive names and
				// update the activities and courses old names
				index = -1;
				for( Activity a: document.getActivities() )
					if ( a.wasRenamed() && activitiesInDB.contains(a.getOldName())) {
						renameTrack(baseName+Integer.toString(++index), a.getName(),
									docFilename, true);
						a.setOldNameWhenSaving();
					}
				for(Course c: document.getCourses() )
					if ( c.wasRenamed() && coursesInDB.contains(c.getOldName())) {
						renameTrack(baseName+Integer.toString(++index), c.getName(),
									docFilename, false);
						c.setOldNameWhenSaving();
					}
			}
		}
		
		System.out.println("PHASE 3");
		// PHASE 3: update activities and courses that:
		// 1) Were changed or
		// 2) are not in the DB
		// Note: this also updates any dependent media
		activitiesInDB = getActivityNames(docFilename);
		for( Activity a: document.getActivities())
			if ( a.getUnsavedChanges() || !activitiesInDB.contains(a.getName()))
				updateDB( a, file);
		coursesInDB = getCourseNames(docFilename);
		for( Course c: document.getCourses() )
			if ( c.getUnsavedChanges() || !coursesInDB.contains(c.getName()))
				updateDB( c, file);
		
		System.out.println("PHASE 4");
		// PHASE 4: delete from the DB all activities and courses
		// that are no longer part of the document
		// Note: media references are also removed from the DB (automatically)
		for( Activity a: document.getActivities() )
			if ( activitiesInDB.contains(a.getName()) )
				activitiesInDB.remove(a.getName());
		for( String s: activitiesInDB ) {
			sql = "DELETE FROM GPSFiles WHERE Filepath='" + docFilename +
					"' AND Name='" + s + "' AND IsActivity=1";
			executeUpdateRequest(sql);			
		}
		for( Course c: document.getCourses() )
			if ( coursesInDB.contains(c.getName()) )
				coursesInDB.remove(c.getName());
		for( String s: coursesInDB ) {
			sql = "DELETE FROM GPSFiles WHERE Filepath='" + docFilename +
					"' AND Name='" + s + "' AND IsActivity=0";
			executeUpdateRequest(sql);			
		}
		System.out.println("PHASE OUT");
	}

	public boolean doesDocumentExistInDB( GPSDocument document) {
		boolean result = false;
		if ( document.countActivitiesAndCourses() != 0 ) {
			String sql = "SELECT Name FROM GPSFiles WHERE Filepath ='" 
					+ document.getFileName() + "'";
			System.out.println( sql);
			openConnection();
			if ( connected() )
				try {
					result = !stmt.executeQuery(sql).isAfterLast();
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					closeConnection();
				}
		}
		System.out.println( "Result is " + result);
		return result;
	}

	// 12335: 2015-10-05 - Correct document tracks for which IsActivity = -1
	public void correctTracksWithInterimIsActivityValue( GPSDocument document) {
		List<String> names = getActivityAndOrCourseNames(document.getFileName(),-1);
		if ( !names.isEmpty() ) {
			for( Activity activity: document.getActivities() )
				if ( names.contains(activity.getName()) )
					updateDB(activity, new File(document.getFileName()));
			for( Course course: document.getCourses())
				if ( names.contains(course.getName()))
					updateDB(course, new File(document.getFileName()));
		}
	}

//###########################    DOCUMENTS   - End   #####################################
	
//########################    COURSES AND ACTIVITIES    ##################################
	
	// 12335: 2015-10-04
	public List<String> getActivityNames( final String filepath) {
		return getActivityAndOrCourseNames(filepath, 1);
	}
	
	public List<String> getCourseNames( final String filepath) {
		return getActivityAndOrCourseNames(filepath, 0);
	}
	
	public List<String> getTracksThatNeedUpdateNames( final String filepath) {
		return getActivityAndOrCourseNames(filepath, -1);
	}
	
	public List<String> getAllTracksNames( final String filepath) {
		return getActivityAndOrCourseNames(filepath, 2);
	}
		
	private List<String> getActivityAndOrCourseNames(String filepath, int type) {
		// type =  0	select courses
		//      =  1    select activities
		//      = -1	select those that need to be updated
		//		=  2	(or any other value) all courses and activities in any state (full list)
		List<String> names = new ArrayList<String>();
		openConnection();
		if ( connected() )
			try {
				String sql = "SELECT Name FROM GPSFiles WHERE Filepath='" + filepath + "'";
				if ( type >= -1 && type <= 1 )
					sql += "AND IsActivity=" + Integer.toString(type);
				res = stmt.executeQuery(sql);
				while( res.next() )
					names.add(res.getString(1));
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		return names;
	}
	
	public void updateDB(Activity activity, File file) {
		Connection c = null;
		PreparedStatement stmt = null;
		BoundingBox2<Location> bb = activity.getBounds();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "INSERT OR REPLACE INTO GPSFiles (Filepath, Name, Sport, SubSport, MinLongitude, "
					+ "MaxLongitude, MinLatitude, MaxLatitude) VALUES (?,?,?,?,?,?,?,?)";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, file.getAbsolutePath());
			stmt.setString(2, activity.getName());
			stmt.setShort(3, activity.getSport().getSportID());
			stmt.setShort(4, activity.getSubSport().getSubSportID());
			stmt.setDouble(5, bb.getTopLeft().getLongitude());
			stmt.setDouble(6, bb.getTopRight().getLongitude());
			stmt.setDouble(7, bb.getBottomLeft().getLatitude());
			stmt.setDouble(8, bb.getTopLeft().getLatitude());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("updateDB activity");
			return;
		}
	}
	
	public void updateDB(Course course, File file) {
		Connection c = null;
		PreparedStatement stmt = null;
		BoundingBox2<Location> bb = course.getBounds();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "INSERT OR REPLACE INTO GPSFiles (Filepath, Name, Sport, SubSport, MinLongitude, "
					+ "MaxLongitude, MinLatitude, MaxLatitude) VALUES (?,?,?,?,?,?,?,?)";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, file.getAbsolutePath());
			stmt.setString(2, course.getName());
			stmt.setShort(3, course.getSport().getSportID());
			stmt.setShort(4, course.getSubSport().getSubSportID());
			stmt.setDouble(5, bb.getTopLeft().getLongitude());
			stmt.setDouble(6, bb.getTopRight().getLongitude());
			stmt.setDouble(7, bb.getBottomLeft().getLatitude());
			stmt.setDouble(8, bb.getTopLeft().getLatitude());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("updateDB course");
			return;
		}
	}

	private void updateDBTrack(File file, String name,
			int id, SportType sport, SubSportType subSport,
			Date startTime, Date endTime, double totalTime, double movingTime,
			double distance, int totalAscent, int totalDescent,
			BoundingBox2<Location> box) {
		try {
			String path = file.getAbsolutePath();
			openConnection();
			// Are we inserting or updating ?
			String sql = "SELECT * FROM GPSFiles WHERE Filepath='" + path
					+ "' AND Name ='" + name +"'";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			System.out.println(exists + " " + file + " " + name);
			// Insert a new activity/course
			if ( exists ) {
				sql = "UPDATE GPSFiles SET IsActivity=?, Sport=?, SubSport=?, "
				+ "StartTime=?, EndTime=?, TotalTime=?, MovingTime=?,"
				+ "Distance=?, Ascent=?, Descent=?, "
				+ "MinLongitude=?, MaxLongitude=?, MinLatitude=?, MaxLatitude=? " 
				+ "WHERE Filepath=? AND Name=?";
				PreparedStatement stmt = c.prepareStatement(sql);
				stmt.setShort( 1, (short) id);
//				stmt.setInt(2, sport.getValue());					// 12335: 2016-06-12
//				stmt.setInt(3, subSport.getValue());				// 12335: 2016-06-12
				stmt.setInt(2, sport.getSportID());					// 12335: 2016-06-12
				stmt.setInt(3, subSport.getSubSportID());			// 12335: 2016-06-12
				stmt.setDate(4, dateToSQL(startTime));
				stmt.setDate(5, dateToSQL(endTime));
				stmt.setDouble(6, totalTime);
				stmt.setDouble(7, movingTime);
				stmt.setDouble(8, distance);
				stmt.setDouble(9, totalAscent);
				stmt.setDouble(10, totalDescent);
				stmt.setDouble(11, box.getTopLeft().getLongitude());
				stmt.setDouble(12, box.getTopRight().getLongitude());
				stmt.setDouble(13, box.getBottomLeft().getLatitude());
				stmt.setDouble(14, box.getTopLeft().getLatitude());
				stmt.setString(15, path);
				stmt.setString(16, name);
				stmt.executeUpdate();
				stmt.close();
			}
			else {
				sql = "INSERT OR REPLACE INTO GPSFiles " 
				+ "(Filepath, Name, IsActivity, Sport, SubSport, "
				+ "StartTime, EndTime, TotalTime, MovingTime, Distance, Ascent, Descent, "
				+ "MinLongitude, MaxLongitude, MinLatitude, MaxLatitude) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement stmt = c.prepareStatement(sql);
				stmt.setString(1, path);
				stmt.setString(2, name);
				stmt.setShort(3, (short) id);
//				stmt.setInt(4, sport.getValue());				//12335: 2016-06-12
//				stmt.setInt(5, subSport.getValue());			//12335: 2016-06-12
				stmt.setInt(4, sport.getSportID());				//12335: 2016-06-12
				stmt.setInt(5, subSport.getSubSportID());		//12335: 2016-06-12
				stmt.setDate(6, dateToSQL(startTime));
				stmt.setDate(7, dateToSQL(endTime));
				stmt.setDouble(8, totalTime);
				stmt.setDouble(9, movingTime);
				stmt.setDouble(10, distance);
				stmt.setDouble(11, totalAscent);
				stmt.setDouble(12, totalDescent);
				stmt.setDouble(13, box.getTopLeft().getLongitude());
				stmt.setDouble(14, box.getTopRight().getLongitude());
				stmt.setDouble(15, box.getBottomLeft().getLatitude());
				stmt.setDouble(16, box.getTopLeft().getLatitude());
				stmt.executeUpdate();
				stmt.close();
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
		} finally {
			closeConnection();
		}
	}

	// 12335: 2015-09-15 Renaming courses and activities
	public boolean renameTrack( String name, String newName, String filepath, boolean isActivity) {
		if ( newName != name ) {
			int result = executeUpdateRequest("UPDATE GPSFiles Set Name='" + newName + "' WHERE Name ='"
					+ name + "' AND Filepath='" + filepath + "' AND IsActivity ="
					+ (isActivity?"1":"0"));
			if ( result > 0 )
				return true;
		}
		return false;
	}

	public void deleteFromDB(Activity activity) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String s = "DELETE FROM GPSFiles WHERE Filepath = ? AND Name = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, activity.getFilepath());
			stmt.setString(2, activity.getName());
			stmt.executeUpdate();
			stmt.close();
			c.commit();s = "DELETE FROM Picture WHERE Container = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, activity.getFilepath());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("deleteFromDB activity");
			return;
		}
	}
	
	// 12335: 2015-09-15 - delete all Activities and Courses
	public boolean delete( String filepath) {
		if ( executeUpdateRequest("DELETE FROM GPSFiles WHERE Filepath='" + filepath + "'") > 0 )
			return true;
		return false;
	}
	
	// 12335: 2015-10-14 Activity/Course central location
	public Location getCentralLocation( String documentFilename, String trackName) {
		Location centralLocation = null;
		openConnection();
		if ( connected() ) {
			String sql = "SELECT MinLongitude, MaxLongitude, MinLatitude, MaxLatitude FROM GPSfile"
					+ "WHERE Filepath 0'" + documentFilename + "' AND Name='" + trackName + "'";
			try {
				res = stmt.executeQuery( sql);
				if ( !res.isAfterLast() ) {
					centralLocation = new Location((res.getDouble(1)+res.getDouble(2))*.5, 
												   (res.getDouble(3)+res.getDouble(4))*.5);
				}
			} catch( Exception e) {
				logger.error(e.getMessage());
			} finally {
				closeConnection();
			}
		}
		return centralLocation;
	}

	public void deleteFromDB(Course course) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String s = "DELETE FROM GPSFiles WHERE Filepath = ? AND Name = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, course.getFilepath());
			stmt.setString(2, course.getName());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			s = "DELETE FROM Picture WHERE Container = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, course.getFilepath());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("deleteFromDB course");
			return;
		}
	}

	public void renameCourse(String name, String newName) {
		if (name.equals(newName))
			return;
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(true);
			String sql = "Update GPSFiles SET Name = ? WHERE Name = ?";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, newName);
			stmt.setString(2, name);
			stmt.executeUpdate();
			stmt.close();
			c.close();
		} catch (Exception e) {
			if (!(e.getMessage()
					.equalsIgnoreCase("query does not return results"))){
				logger.error(e.getClass().getName() + ": "
						+ e.getMessage());
				logger.error("renameCourse");
			}
		}
		System.out.println("Database Update Successful.");
	}

//########################    COURSES AND ACTIVITIES  - End  #############################

//########################    SPORTS AND SUBSPORTS      ##################################
	
	public void updateDBSport(Activity activity) {
		BoundingBox2<Location> bb = activity.getBounds();
		openConnection();
		try {
			String sql = "SELECT Sport FROM GPSFiles WHERE Filepath='"+activity.getFilepath()+"' AND Name='"+activity.getName()+"'";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if(exists){
				sql = "UPDATE GPSFiles SET Sport=?, SubSport=? WHERE Filepath='"+activity.getFilepath()+"' AND Name='"+activity.getName()+"'";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setShort(1, activity.getSport().getSportID());
				statement.setShort(2, activity.getSubSport().getSubSportID());
				statement.executeUpdate();
				statement.close();
			}
			else{
				sql = "INSERT OR REPLACE INTO GPSFiles (Filepath, Name, Sport, SubSport, MinLongitude, "
						+ "MaxLongitude, MinLatitude, MaxLatitude) VALUES (?,?,?,?,?,?,?,?)";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setString(1, activity.getFilepath());
				statement.setString(2, activity.getName());
				statement.setShort(3, activity.getSport().getSportID());
				statement.setShort(4, activity.getSubSport().getSubSportID());
				statement.setDouble(5, bb.getTopLeft().getLongitude());
				statement.setDouble(6, bb.getTopRight().getLongitude());
				statement.setDouble(7, bb.getBottomLeft().getLatitude());
				statement.setDouble(8, bb.getTopLeft().getLatitude());
				statement.executeUpdate();
				statement.close();
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("updateDBSport activity");
			}
		finally{
			closeConnection();
		}
	}
	
	public void updateDBSport(Course course) {
		BoundingBox2<Location> bb = course.getBounds();
		System.out.println( "Document exists " + doesDocumentExistInDB( course.getParent()));
		openConnection();
		try {
			String sql = "SELECT Sport FROM GPSFiles WHERE Filepath='"+course.getFilepath()+"' AND Name='"+course.getName()+"'";
			PreparedStatement statement;
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if(exists){
				sql = "UPDATE GPSFiles SET Sport=?, SubSport=? WHERE Filepath='"+course.getFilepath()+"' AND Name='"+course.getName()+"'";
				statement = c.prepareStatement(sql);
				statement.setShort(1, course.getSport().getSportID());
				statement.setShort(2, course.getSubSport().getSubSportID());
				statement.executeUpdate();
				statement.close();
			}
			else{
				sql = "INSERT OR REPLACE INTO GPSFiles (Filepath, Name, Sport, SubSport, MinLongitude, "
						+ "MaxLongitude, MinLatitude, MaxLatitude) VALUES (?,?,?,?,?,?,?,?)";
				statement = c.prepareStatement(sql);
				statement.setString(1, course.getFilepath());
				statement.setString(2, course.getName());
				statement.setShort(3, course.getSport().getSportID());
				statement.setShort(4, course.getSubSport().getSubSportID());
				statement.setDouble(5, bb.getTopLeft().getLongitude());
				statement.setDouble(6, bb.getTopRight().getLongitude());
				statement.setDouble(7, bb.getBottomLeft().getLatitude());
				statement.setDouble(8, bb.getTopLeft().getLatitude());
				statement.executeUpdate();
				statement.close();
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("updateDBSport course");
			}
		finally{
			closeConnection();
		}
		System.out.println( "Document exists " + doesDocumentExistInDB( course.getParent()));
	}
	
	//########################    SPORTS AND SUBSPORTS    - End  #############################
	
	//##############################       PICTURES      #####################################
	
	public ArrayList<String> getPicturesFromDB(String container, String parentName) {
		ArrayList<String> picturesList = new ArrayList<String>();
		openConnection();
		if ( connected() )
			try {
				String sql = "SELECT Filepath from Picture WHERE Container='" + container +
						"' AND ParentName='" + parentName + "'";
				res = stmt.executeQuery(sql);
				while ( res.next() )
					picturesList.add(res.getString(1));
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		return picturesList;
	}
	
	// Get all pictures associated with a document
	public HashMap<String, ArrayList<String>> getPicturesFromDB( String container) {
		HashMap<String, ArrayList<String>> picturesInDB = new HashMap<>();
		openConnection();
		if ( connected() )
			try {
				String sql = "SELECT ParentName, Filepath FROM Picture WHERE Container='" +
						container + "'";
				res = stmt.executeQuery(sql);
				String parentName;
				ArrayList<String> array;
				while( res.next()) {
					parentName = res.getString(1);
					array = picturesInDB.get(parentName);
					if ( array == null ) {
						array = new ArrayList<>();
						picturesInDB.put(parentName, array);
					}
					array.add(res.getString(2));
				}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		return picturesInDB;
	}
	
	public void updatePictures(PhotoContainer container){
		for(Picture pic : container.getPictures()){
			updatePicture(pic);
		}
	}
	
	// 12335: 2015-09-30 
	void updateTrackPictures( List<Picture> picturesInTrack, 
			                  String parentFilepath, String parentName ) {
		List<String> picturesInDB = getPicturesFromDB(parentFilepath, parentName);
		for( Picture pic: picturesInTrack) {
			if ( picturesInDB.contains(pic.getFilePath()) )
				picturesInDB.remove(pic.getFilePath());
			else
				updatePicture(pic, parentFilepath);
		}
		for( String s: picturesInDB )
			removePicture(s, parentFilepath, parentName);
	}

	public void updatePicture(final Picture pic, String parentFilepath){
		PreparedStatement stmt = null;
		openConnection();
		if ( connected() )
			try{
				System.out.println("UPDATING PIC " + pic.getFilePath());
				System.out.println("\t" + parentFilepath + "  "+ pic.getContainer().getName());			System.out.println("\t"+ pic.getContainer().getFilepath());
				String sql = "INSERT OR REPLACE INTO Picture (Filepath, Name, Longitude, "
						+ "Latitude, Altitude, Container, ParentName) VALUES (?,?,?,?,?,?,?)";
				stmt = c.prepareStatement(sql);
				stmt.setString(1, pic.getFilePath());
				stmt.setString(2, pic.getName());
				stmt.setDouble(3, pic.getLongitude());
				stmt.setDouble(4, pic.getLatitude());
				stmt.setDouble(5, pic.getAltitude());			
				stmt.setString(6, parentFilepath);
				stmt.setString(7, pic.getContainer().getName());
				stmt.executeUpdate();
				closeConnection();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace();
			} finally {
				closeConnection();
			}
	}
	// 12335: 2015-09-30  end

	// 12335: 2015-09-30: kept for backwards compatibility but calls new updatePicture
	public void updatePicture(final Picture pic){
		updatePicture( pic, pic.getContainer().getFilepath());
//		Connection c = null;
//		PreparedStatement stmt = null;
//		try{
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
//			c.setAutoCommit(false);
//			String sql = "INSERT OR REPLACE INTO Picture (Filepath, Name, Longitude, "
//					+ "Latitude, Altitude, Container, ParentName) VALUES (?,?,?,?,?,?,?)";
//			stmt = c.prepareStatement(sql);
//			stmt.setString(1, pic.getFilePath());
//			stmt.setString(2, pic.getName());
//			stmt.setDouble(3, pic.getLongitude());
//			stmt.setDouble(4, pic.getLatitude());
//			stmt.setDouble(5, pic.getAltitude());			
//			stmt.setString(6, pic.getContainerFilePath());
//			stmt.setString(7, pic.getContainer().getName());
//			stmt.executeUpdate();
//			stmt.close();
//			c.commit();
//			c.close();
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("update picture");
//			e.printStackTrace();
//			return;
//		}
	}
	
	
	// 12335: 2015-09-30: kept for backward compatibility
	public void removePicture( Picture pic) {
		removePicture(pic.getFilePath(), 
					  pic.getContainerFilePath(),
					  pic.getContainer().getName());
//		Connection c = null;
//		PreparedStatement stmt = null;
//		try {
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
//			c.setAutoCommit(false);
//			String s = "DELETE FROM Picture WHERE Filepath = ? AND Container = ? AND ParentName = ?";
//			stmt = c.prepareStatement(s);
//			stmt.setString(1, pic.getFilePath());
//			stmt.setString(2, pic.getContainerFilePath());
//			stmt.setString(3, pic.getContainer().getName());
//			stmt.executeUpdate();
//			stmt.close();
//			c.commit();
//			c.close();
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("removePicture");
//			return;
//		}
	}
	
	public void removePicture( String pictureFilepath, String containerFilepath,
							   String containerName) {
		String sql = "DELETE FROM Picture WHERE Filepath ='" + pictureFilepath +
				"' AND Container ='" + containerFilepath +
				"' AND ParentName ='" + containerName + "'";
		executeUpdateRequest(sql);
	}

	public void removeAllPictures(Activity activity) {
		removeAllTrackPictures(activity.getFilepath(), activity.getName());
//		Connection c = null;
//		PreparedStatement stmt = null;
//		try {
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
//			c.setAutoCommit(false);
//			String s = "DELETE FROM Picture WHERE Container = ?";
//			stmt = c.prepareStatement(s);
//			stmt.setString(1, activity.getFilepath());
//			stmt.executeUpdate();
//			stmt.close();
//			c.commit();
//			c.close();
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("removeAllPictures activity");
//			return;
//		}
	}
	
	public void removeAllPictures(Course course) {
		removeAllTrackPictures(course.getFilepath(), course.getName());
//		Connection c = null;
//		PreparedStatement stmt = null;
//		try {
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
//			c.setAutoCommit(false);
//			String s = "DELETE FROM Picture WHERE Container = ?";
//			stmt = c.prepareStatement(s);
//			stmt.setString(1, course.getFilepath());
//			stmt.executeUpdate();
//			stmt.close();
//			c.commit();
//			c.close();
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("removeAllPictures course");
//			return;
//		}
	}
	
	private void removeAllTrackPictures( String documentPath, String trackName) {
		executeUpdateRequest("DELETE FROM Picture WHERE Container='" + documentPath +
							 "' AND ParentName='" + trackName + "'");
	}

//##############################       PICTURES     - End ################################

//##############################        SEARCH       #####################################

	public HashSet<String> getDatafromArea(double minLongitude,
			double maxLongitude, double minLatitude, double maxLatitude) {
		Connection c = null;
		PreparedStatement stmt = null;
		HashSet<String> nameList = new HashSet<String>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "SELECT Name FROM GPSFiles"
					+ " WHERE MinLongitude >= ? AND" + " MaxLongitude <= ? AND"
					+ " MinLatitude >= ? AND" + " MaxLatitude <= ?";
			stmt = c.prepareStatement(sql);
			stmt.setDouble(1, minLongitude);
			stmt.setDouble(2, maxLongitude);
			stmt.setDouble(3, minLatitude);
			stmt.setDouble(4, maxLatitude);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String name = new String(rs.getString("Name"));
				nameList.add(name);
			}
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getDatafromArea");
			return nameList;
		}
		return nameList;
	}

//##############################        SEARCH    - End   ################################
	
	//##############################        SEARCH       #####################################
	
	/*++++++++++++++57421+++++++++++++++++++++++++*/
	
		
	
	public double getDefaultAverageSpeed(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		double defaultAverageSpeed = -1.0;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"DefaultAverageSpeed FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			defaultAverageSpeed = rs.getDouble(currentKeyword+"DefaultAverageSpeed");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getDefaultAverageSpeed");
		}
		finally{
			closeConnection();
		}
		return defaultAverageSpeed;
	}
	
	public double getMaximumAllowedSpeed(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		double maximumAllowedSpeed = -1.0;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"MaximumAllowedSpeed FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			maximumAllowedSpeed = rs.getDouble(currentKeyword+"MaximumAllowedSpeed");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getMaximumAllowedSpeed");
		}
		finally{
			closeConnection();
		}
		return maximumAllowedSpeed;
	}
	
	public double getPauseThresholdSpeed(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		double pauseThresholdSpeed = -1.0;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"PauseThresholdSpeed FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			pauseThresholdSpeed = rs.getDouble(currentKeyword+"PauseThresholdSpeed");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getPauseThresholdSpeed");
		}
		finally{
			closeConnection();
		}
		return pauseThresholdSpeed;
	}
	
	public long getDefaultPauseDuration(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		long defaultPauseDuration = -1;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"DefaultPauseDuration FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			defaultPauseDuration = rs.getLong(currentKeyword+"DefaultPauseDuration");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getDefaultPauseDuration");
		}
		finally{
			closeConnection();
		}
		return defaultPauseDuration;
	}
	
	public boolean getFollowRoads(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		boolean followRoads = false;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"FollowRoads FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			followRoads = rs.getBoolean(currentKeyword+"FollowRoads");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getFollowRoads");
		}
		finally{
			closeConnection();
		}
		return followRoads;
	}
	
	public double getJoinMaximumWarningDistance(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		double joinMaximumWarningDistance = -1.0;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"JoinMaximumWarningDistance FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			joinMaximumWarningDistance = rs.getDouble(currentKeyword+"JoinMaximumWarningDistance");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getJoinMaximumWarningDistance");
		}
		finally{
			closeConnection();
		}
		return joinMaximumWarningDistance;
	}
	
	public double getJoinMergeDistanceTolerance(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		double joinMergeDistanceTolerance = -1.0;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"JoinMergeDistanceTolerance FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			joinMergeDistanceTolerance = rs.getDouble(currentKeyword+"JoinMergeDistanceTolerance");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getJoinMergeDistanceTolerance");
		}
		finally{
			closeConnection();
		}
		return joinMergeDistanceTolerance;
	}
	
	public long getJoinMergeTimeTolerance(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		long joinMergeTimeTolerance = -1;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"JoinMergeTimeTolerance FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			joinMergeTimeTolerance = rs.getLong(currentKeyword+"JoinMergeTimeTolerance");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getJoinMergeTimeTolerance");
		}
		finally{
			closeConnection();
		}
		return joinMergeTimeTolerance;
	}
	
	public double getGradeLimit(SportType sport, SubSportType subSport, boolean defaultValues) {
		openConnection();
		short sportID = sport.getSportID();
		short subSportID = subSport.getSubSportID();
		double gradeLimit = -1.0;
		String currentKeyword = "";
		if(!defaultValues){
			currentKeyword = "Current";
		}
		try {
			String sql = "SELECT "+currentKeyword+"GradeLimit FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			gradeLimit = rs.getDouble(currentKeyword+"GradeLimit");
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getGradeLimit");
		}
		finally{
			closeConnection();
		}
		return gradeLimit;
	}
	
	public void resetCurrent(SportType sport, SubSportType subSport){
		openConnection();
		try{
			short sportID = sport.getSportID();
			short subSportID = subSport.getSubSportID();
			String sql = "SELECT SportID, SportName, SubSportID, SubSportName, DefaultAverageSpeed, MaximumAllowedSpeed, PauseThresholdSpeed, DefaultPauseDuration, "
						+ "FollowRoads, JoinMaximumWarningDistance, JoinMergeDistanceTolerance, JoinMergeTimeTolerance, GradeLimit  FROM SubSport "
						+ "WHERE SportID ='"+sportID+"' AND SubSportID='"+subSportID+"'";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if(exists){
				PreparedStatement stmt = c.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
				String subSportName = rs.getString("SubSportName");
				String sportName = rs.getString("SportName");
				double defaultAverageSpeed = rs.getDouble("DefaultAverageSpeed");
				double maximumAllowedSpeed = rs.getDouble("MaximumAllowedSpeed");
				double pauseThresholdSpeed = rs.getDouble("PauseThresholdSpeed");
				long defaultPauseDuration = rs.getLong("DefaultPauseDuration");
				boolean followRoads = rs.getBoolean("FollowRoads");
				double joinMaximumWarningDistance = rs.getDouble("JoinMaximumWarningDistance");
				double joinMergeDistanceTolerance = rs.getDouble("JoinMergeDistanceTolerance");
				long joinMergeTimeTolerance = rs.getLong("JoinMergeTimeTolerance");
				double gradeLimit = rs.getDouble("GradeLimit");
				reset(sportID, sportName, subSportID, subSportName, defaultAverageSpeed, maximumAllowedSpeed, pauseThresholdSpeed, 
						defaultPauseDuration, followRoads, joinMaximumWarningDistance, joinMergeDistanceTolerance, joinMergeTimeTolerance, gradeLimit);
			}
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("resetCurrentSubSport");
		}
		finally{
			closeConnection();
		}
	}
	
	// 12335: 2016-07-17
	public void resetSport( SportType sport) {
		openConnection();
		try {
			String sql = "SELECT SubSportID FROM SubSport WHERE SportID='" + sport.getSportID() + "'";
			System.out.println( sql);
			res = stmt.executeQuery( sql);
			while ( res.next() ) {
				short subSportID = res.getShort( "SubSportID");
				System.out.println( "Resetting " + SubSportType.lookup( subSportID).getName() +
						"   " + subSportID);
				resetCurrent( sport, SubSportType.lookup( subSportID));
			}
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("resetSport");
		}
		finally{
			closeConnection();
		}
	}
	
	public void resetAll(){
		openConnection();
		try{
			String sql = "SELECT SportID, SportName, SubSportID, SubSportName, DefaultAverageSpeed, MaximumAllowedSpeed, PauseThresholdSpeed, DefaultPauseDuration, "
						+ "FollowRoads, JoinMaximumWarningDistance, JoinMergeDistanceTolerance, JoinMergeTimeTolerance, GradeLimit  FROM SubSport";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if(exists){
				PreparedStatement stmt = c.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					short sportID = rs.getShort("SportID");
					short subSportID = rs.getShort("SubSportID");
					String sportName = rs.getString("SportName");
					String subSportName = rs.getString("SubSportName");
					double defaultAverageSpeed = rs.getDouble("DefaultAverageSpeed");
					double maximumAllowedSpeed = rs.getDouble("MaximumAllowedSpeed");
					double pauseThresholdSpeed = rs.getDouble("PauseThresholdSpeed");
					long defaultPauseDuration = rs.getLong("DefaultPauseDuration");
					boolean followRoads = rs.getBoolean("FollowRoads");
					double joinMaximumWarningDistance = rs.getDouble("JoinMaximumWarningDistance");
					double joinMergeDistanceTolerance = rs.getDouble("JoinMergeDistanceTolerance");
					long joinMergeTimeTolerance = rs.getLong("JoinMergeTimeTolerance");
					double gradeLimit = rs.getDouble("GradeLimit");
					//stmt.close();
					//closeConnection();
					reset(sportID, sportName, subSportID, subSportName, defaultAverageSpeed, maximumAllowedSpeed, pauseThresholdSpeed, 
							defaultPauseDuration, followRoads, joinMaximumWarningDistance, joinMergeDistanceTolerance, joinMergeTimeTolerance, gradeLimit);
			    }
			}
				
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("resetAll");
		}
		finally{
			closeConnection();
		}

	}
	
	public void reset(short sportID, String sportName, short subSportID, String subSportName, double defaultAverageSpeed,
			double maximumAllowedSpeed, double pauseThresholdSpeed, long defaultPauseDuration, boolean followRoads,
			double joinMaximumWarningDistance, double joinMergeDistanceTolerance, long joinMergeTimeTolerance, double gradeLimit) {
		//openConnection();
		try {
			String sql = "SELECT CurrentDefaultAverageSpeed, CurrentMaximumAllowedSpeed, CurrentPauseThresholdSpeed, "
					+ "CurrentDefaultPauseDuration, CurrentFollowRoads, CurrentJoinMaximumWarningDistance, "
					+ "CurrentJoinMergeDistanceTolerance, CurrentJoinMergeTimeTolerance, CurrentGradeLimit FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if(exists){
				sql = "UPDATE SubSport SET CurrentDefaultAverageSpeed=?, CurrentMaximumAllowedSpeed=?, CurrentPauseThresholdSpeed=?, "
					+ "CurrentDefaultPauseDuration=?, CurrentFollowRoads=?, CurrentJoinMaximumWarningDistance=?, "
					+ "CurrentJoinMergeDistanceTolerance=?, CurrentJoinMergeTimeTolerance=?, CurrentGradeLimit=? WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setDouble(1, defaultAverageSpeed);
				statement.setDouble(2, maximumAllowedSpeed);
				statement.setDouble(3, pauseThresholdSpeed);
				statement.setLong(4, defaultPauseDuration);
				statement.setBoolean(5, followRoads);
				statement.setDouble(6, joinMaximumWarningDistance);
				statement.setDouble(7, joinMergeDistanceTolerance);
				statement.setLong(8, joinMergeTimeTolerance);
				statement.setDouble(9, gradeLimit);
				statement.executeUpdate();
				statement.close();
			}
			else{
				/*sql = "INSERT OR REPLACE INTO SubSport (SportID, SportName, SubSportID, SubSportName, "
						+ "DefaultAverageSpeed, MaximumAllowedSpeed, PauseThresholdSpeed, DefaultPauseDuration, "
						+ "FollowRoads, JoinMaximumWarningDistance, JoinMergeDistanceTolerance, JoinMergeTimeTolerance, "
						+ "CurrentDefaultAverageSpeed, CurrentMaximumAllowedSpeed, CurrentPauseThresholdSpeed, CurrentDefaultPauseDuration, "
						+ "CurrentFollowRoads, CurrentJoinMaximumWarningDistance, CurrentJoinMergeDistanceTolerance, CurrentJoinMergeTimeTolerance) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setShort(1, sportID);
				statement.setString(2, sportName);
				statement.setShort(3, subSportID);
				statement.setString(4, subSportName);
				statement.setDouble(5, defaultAverageSpeed);
				statement.setDouble(6, maximumAllowedSpeed);
				statement.setDouble(7, pauseThresholdSpeed);
				statement.setLong(8, defaultPauseDuration);
				statement.setBoolean(9, followRoads);
				statement.setDouble(10, joinMaximumWarningDistance);
				statement.setDouble(11, joinMergeDistanceTolerance);
				statement.setLong(12, joinMergeTimeTolerance);
				statement.setDouble(13, defaultAverageSpeed);
				statement.setDouble(14, maximumAllowedSpeed);
				statement.setDouble(15, pauseThresholdSpeed);
				statement.setLong(16, defaultPauseDuration);
				statement.setBoolean(17, followRoads);
				statement.setDouble(18, joinMaximumWarningDistance);
				statement.setDouble(19, joinMergeDistanceTolerance);
				statement.setLong(20, joinMergeTimeTolerance);
				statement.executeUpdate();
				statement.close();*/
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("reset");
			}
		finally{
			//closeConnection();
		}
	}
	
	public short getSport(Activity activity) {		// 12335: 2016-06-08: uses getSportOrSportType
		return getSportOrSubSport( true, activity.getFilepath(), activity.getName(), true);
	}
	
	public short getSport(Course course) {  		// 12335: 2016-06-08: uses getSportOrSportType 
		return getSportOrSubSport( true, course.getFilepath(), course.getName(), false);
	}
	
	public short getSubSport(Activity activity) {
//		openConnection();
//		short subSportID = -1;
//		try {
//			String sql = "SELECT SubSport FROM GPSFiles WHERE Filepath='"+activity.getFilepath()+"' AND Name='"+activity.getName()+"'";
//			PreparedStatement stmt = c.prepareStatement(sql);
//			ResultSet rs = stmt.executeQuery();
//			subSportID = rs.getShort("SubSport");
//			stmt.close();
//			c.close();
//
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("getSubSport activity");
//		}
//		finally{
//			closeConnection();
//		}
//		return subSportID;
		return getSportOrSubSport( false, activity.getFilepath(), activity.getName(), true);
	}
	
	public short getSubSport(Course course) {
//		openConnection();
//		short subSportID = -1;
//		try {
//			String sql = "SELECT SubSport FROM GPSFiles WHERE Filepath='"+course.getFilepath()+"' AND Name='"+course.getName()+"'";
//			PreparedStatement stmt = c.prepareStatement(sql);
//			ResultSet rs = stmt.executeQuery();
//			subSportID = rs.getShort("SubSport");
//			stmt.close();
//			c.close();
//
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("getSubSport course");
//		}
//		finally{
//			closeConnection();
//		}
//		return subSportID;
		return getSportOrSubSport( false, course.getFilepath(), course.getName(), false);
	}
	
	// 12335: 2016-06-08: Generic getSport / getSubSport
	private short getSportOrSubSport( boolean isSport, String filepath, String name, boolean isActivity) {
		short sport = -1;
		openConnection();
		try {
			String type = isSport ? "Sport" : "SubSport";
			String sss = "SELECT " + type + " FROM Desktop WHERE Filepath='" + filepath
					+ "' AND TrackName='" + name 
					+ "' UNION SELECT " + type + " FROM GPSFiles WHERE Filepath='" + filepath
					+ "' AND Name='" + name + "'";
			res = stmt.executeQuery( sss);
			while ( res.next() ) {
				sport = res.getShort( 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getSport course");
		}
		finally{
			closeConnection();
		}
		return sport;
	}
	
	// 12335: 2016-06-13
	public List<String> getSportsOrdered() {
		List<String> list = getSports();
		Collections.sort( list);
		return list;
	}
	
	public ArrayList<String> getSports() {
		openConnection();
		ArrayList<String> nameList = new ArrayList<String>();
		try {
			String sql = "Select Name FROM Sport";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String sportName = new String(rs.getString("Name"));
				if (!nameList.contains(sportName))
					nameList.add(sportName);
			}
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getSports");
		}
		finally{
			closeConnection();
		}
		return nameList;
	}

	// 12335: 2016-06-13
	public List<String> getSubSportsOrdered( SportType sport) {
		List<String> list = getSubSports( sport);
		Collections.sort( list);
		return list;
	}
	
	public ArrayList<String> getSubSports(SportType sport) {
		openConnection();
		ArrayList<String> nameList = new ArrayList<String>();
		try {
			String sql = "Select SubSportName FROM SubSport WHERE SportID='"+sport.getSportID()+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String subSportName = new String(rs.getString("SubSportName"));
				if (!nameList.contains(subSportName))
					nameList.add(subSportName);
			}
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getSubSports");
		}
		finally{
			closeConnection();
		}
		return nameList;
	}
	

	public void updateDBSubSport(SubSportType subSport, short sportID, short subSportID,  String subSportName, double defaultAverageSpeed,
			double maximumAllowedSpeed, double pauseThresholdSpeed, long defaultPauseDuration, boolean followRoads,
			double joinMaximumWarningDistance, double joinMergeDistanceTolerance, long joinMergeTimeTolerance, double gradeLimit) {
		openConnection();
		try {
			String sql = "SELECT CurrentDefaultAverageSpeed, CurrentMaximumAllowedSpeed, CurrentPauseThresholdSpeed, "
					+ "CurrentDefaultPauseDuration, CurrentFollowRoads, CurrentJoinMaximumWarningDistance, "
					+ "CurrentJoinMergeDistanceTolerance, CurrentJoinMergeTimeTolerance, CurrentGradeLimit FROM SubSport WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
			boolean exists = !stmt.executeQuery(sql).isAfterLast();
			if(exists){
				sql = "UPDATE SubSport SET CurrentDefaultAverageSpeed=?, CurrentMaximumAllowedSpeed=?, CurrentPauseThresholdSpeed=?, "
					+ "CurrentDefaultPauseDuration=?, CurrentFollowRoads=?, CurrentJoinMaximumWarningDistance=?, "
					+ "CurrentJoinMergeDistanceTolerance=?, CurrentJoinMergeTimeTolerance=?, CurrentGradeLimit=? WHERE SportID='"+sportID+"' AND SubSportID='"+subSportID+"'";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setDouble(1, defaultAverageSpeed);
				statement.setDouble(2, maximumAllowedSpeed);
				statement.setDouble(3, pauseThresholdSpeed);
				statement.setLong(4, defaultPauseDuration);
				statement.setBoolean(5, followRoads);
				statement.setDouble(6, joinMaximumWarningDistance);
				statement.setDouble(7, joinMergeDistanceTolerance);
				statement.setLong(8, joinMergeTimeTolerance);
				statement.setDouble(9, gradeLimit);
				statement.executeUpdate();
				statement.close();
			}
			else{
				/*sql = "INSERT OR REPLACE INTO SubSport (SportID, SportName ,SubSportID, SubSportName, "
						+ "DefaultAverageSpeed, MaximumAllowedSpeed, PauseThresholdSpeed, DefaultPauseDuration, "
						+ "FollowRoads, JoinMaximumWarningDistance, JoinMergeDistanceTolerance, JoinMergeTimeTolerance, "
						+ "CurrentDefaultAverageSpeed, CurrentMaximumAllowedSpeed, CurrentPauseThresholdSpeed, CurrentDefaultPauseDuration, "
						+ "CurrentFollowRoads, CurrentJoinMaximumWarningDistance, CurrentJoinMergeDistanceTolerance, CurrentJoinMergeTimeTolerance) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setShort(1, sportID);
				statement.setString(2, sportName);
				statement.setShort(3, subSportID);
				
				statement.setString(4, subSportName);
				statement.setDouble(4, subSport.getDefaultAverageSpeed());
				statement.setDouble(5, subSport.getMaximumAllowedSpeed());
				statement.setDouble(6, subSport.getPauseThresholdSpeed());
				statement.setLong(7, subSport.getDefaultPauseDuration());
				statement.setBoolean(8, subSport.getFollowRoads());
				statement.setDouble(9, subSport.getJoinMaximumWarningDistance());
				statement.setDouble(10, subSport.getJoinMergeDistanceTolerance());
				statement.setLong(11, subSport.getJoinMergeTimeTolerance());
				statement.setDouble(12, defaultAverageSpeed);
				statement.setDouble(13, maximumAllowedSpeed);
				statement.setDouble(14, pauseThresholdSpeed);
				statement.setLong(15, defaultPauseDuration);
				statement.setBoolean(16, followRoads);
				statement.setDouble(17, joinMaximumWarningDistance);
				statement.setDouble(18, joinMergeDistanceTolerance);
				statement.setLong(19, joinMergeTimeTolerance);
				statement.executeUpdate();
				statement.close();*/
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("updateDBSubSport");
			}
		finally{
			closeConnection();
		}
	}
		
//###########################    HELPER FUNCTIONS   ################################
	
	// 12335: 2015-09-17 - Date helper functions #######################################
	// 12335: 2016-06-12  not needed any more (i Think)
	static java.sql.Date dateToSQL( java.util.Date date) {
		return new java.sql.Date(date.getTime());
	}
	
	static java.util.Date dateToJava( java.sql.Date date) {
		return new java.util.Date(date.getTime());
	}
	
	// 12335: 2015-09-17 - Absolute / relative path support
	public String toRelativePath( String absolutePath) {
		return workingDirectory.relativize(Paths.get( absolutePath)).toString();
	}
	
	public String toAbsolutePath( String relativePath) {
		return Paths.get(workingDirectory.toString(),relativePath).toString();
	}
	
//###########################    HELPER FUNCTIONS   - End  #########################

//##############################      DEAD CODE     ################################

	public String getFileName(String filepath) {
		Connection c = null;
		PreparedStatement stmt = null;
		String name = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "SELECT Name FROM GPSFiles" + " WHERE Filepath = ?";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, filepath);
			ResultSet rs = stmt.executeQuery();
			name = rs.getString("Name");
			stmt.close();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getFileName");
		}

		return name;
	}
	
	//##############################      DEAD CODE   - End  ###########################

	
//		public void initDb() {
//		//Connection c = null;
//		//Statement stmt = null;
//		try {
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
//
//			stmt = c.createStatement();
//			/*String sql = "CREATE TABLE IF NOT EXISTS Courses "
//					+ "(Filepath TEXT PRIMARY KEY NOT NULL,"
//					+ "Name TEXT NOT NULL," + "MinLongitude DOUBLE NOT NULL,"
//					+ "MaxLongitude DOUBLE NOT NULL,"
//					+ "MinLatitude DOUBLE NOT NULL,"
//					+ "MaxLatitude DOUBLE NOT NULL)";
//			stmt.executeUpdate(sql);
//			sql = "CREATE TABLE IF NOT EXISTS Activities "
//					+ "(Filepath TEXT PRIMARY KEY NOT NULL,"
//					+ "Name TEXT NOT NULL," + "MinLongitude DOUBLE NOT NULL,"
//					+ "MaxLongitude DOUBLE NOT NULL,"
//					+ "MinLatitude DOUBLE NOT NULL,"
//					+ "MaxLatitude DOUBLE NOT NULL)";
//			stmt.executeUpdate(sql);*/
//			String sql = "CREATE TABLE IF NOT EXISTS GPSFiles "
//					+ "(Filepath TEXT NOT NULL,"
//					+ "Name TEXT NOT NULL," + "MinLongitude DOUBLE NOT NULL,"
//					+ "Sport SHORT DEFAULT -1,"
//					+ "SubSport SHORT DEFAULT -1,"
//					+ "MaxLongitude DOUBLE NOT NULL,"
//					+ "MinLatitude DOUBLE NOT NULL,"
//					+ "MaxLatitude DOUBLE NOT NULL, "
//					+ "PRIMARY KEY(Filepath, Name))";
//			stmt.executeUpdate(sql);
//			sql = "CREATE TABLE IF NOT EXISTS Picture "
//					+ "(Filepath TEXT NOT NULL," + "Name TEXT,"
//					+ "Longitude DOUBLE," + "Latitude DOUBLE,"
//					+ "Altitude DOUBLE," + "Container TEXT NOT NULL, "
//					+ "ParentName TEXT NOT NULL,"
//					+ "PRIMARY KEY (Filepath, Container, ParentName)"
//					+ "FOREIGN KEY (Container) REFERENCES GPSFiles(Filepath),"
//					+ "FOREIGN KEY (ParentName) REFERENCES GPSFiles(Name))";
//			stmt.executeUpdate(sql);
//			sql = "CREATE TABLE IF NOT EXISTS Video "
//					+ "(Filepath TEXT NOT NULL," + "Name TEXT,"
//					+ "Longitude DOUBLE," + "Latitude DOUBLE,"
//					+ "Altitude DOUBLE," + "Container TEXT NOT NULL, "
//					+ "ParentName TEXT NOT NULL,"
//					+ "PRIMARY KEY (Filepath, Container, ParentName)"
//					+ "FOREIGN KEY (Container) REFERENCES GPSFiles(Filepath),"
//					+ "FOREIGN KEY (ParentName) REFERENCES GPSFiles(Name))";
//			stmt.executeUpdate(sql);
//			sql = "CREATE TABLE IF NOT EXISTS Audio "
//					+ "(Filepath TEXT NOT NULL," + "Name TEXT,"
//					+ "Longitude DOUBLE," + "Latitude DOUBLE,"
//					+ "Altitude DOUBLE," + "Container TEXT NOT NULL, "
//					+ "ParentName TEXT NOT NULL,"
//					+ "PRIMARY KEY (Filepath, Container, ParentName)"
//					+ "FOREIGN KEY (Container) REFERENCES GPSFiles(Filepath),"
//					+ "FOREIGN KEY (ParentName) REFERENCES GPSFiles(Name))";
//			stmt.executeUpdate(sql);
//			sql = "CREATE TABLE IF NOT EXISTS Sport "
//					+ "(SportID SHORT NOT NULL,"
//					+ "Name TEXT NOT NULL,"
//					+ "PRIMARY KEY (SportID,Name))";
//			stmt.executeUpdate(sql);
//			sql = "CREATE TABLE IF NOT EXISTS SubSport "
//					+ "(TableID SHORT NOT NULL,"
//					+ "SubSportID SHORT NOT NULL,"
//					+ "SportID SHORT NOT NULL,"
//					+ "Name TEXT NOT NULL,"
//					+ "DefaultAverageSpeed DOUBLE,"
//					+ "MaximumAllowedSpeed DOUBLE,"
//					+ "PauseThresholdSpeed DOUBLE,"
//					+ "DefaultPauseDuration LONG,"
//					+ "FollowRoads BOOLEAN,"
//					+ "JoinMaximumWarningDistance DOUBLE,"
//					+ "JoinMergeDistanceTolerance DOUBLE,"
//					+ "JoinMergeTimeTolerance DOUBLE,"
//					+ "PRIMARY KEY (TableID)"
//					+ "FOREIGN KEY (SportID) REFERENCES Sport(SportID))";
//			stmt.executeUpdate(sql);
//			
//			DatabaseMetaData md = c.getMetaData();
//			ResultSet rs = md.getTables(null, null, "%", null);
//			boolean skipAddSport = false;
//			boolean skipAddSubSport = false;
//			while (rs.next()) {
//				if(rs.getString(3).equals("Sport")){
//					//skipAddSport = true;
//					}
//				else if(rs.getString(3).equals("SubSport")){
//					//skipAddSubSport = true;
//					}
//				}
//			if(!skipAddSport){
//				List<SportType> sportList = Arrays.asList(SportType.values()); 
//				for(SportType sportType : sportList){
//					addSport(sportType.getSportID(), sportType.name());
//				}
//			}
//			if(!skipAddSubSport){
//				List<SubSportType> subSportList = Arrays.asList(SubSportType.values()); 
//				int tableID = 0;
//				for(SubSportType subSportType : subSportList){
//					addSubSport(subSportType, tableID);
//					tableID++;
//				}
//			}
//			/*
//			 * sql = "CREATE TABLE IF NOT EXISTS MockUpTable " +
//			 * "(ID INTEGER PRIMARY KEY   AUTOINCREMENT)";
//			 * stmt.executeUpdate(sql);
//			 */
//			stmt.close();
//			c.close();
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			System.exit(0);
//		}
//	}
}
