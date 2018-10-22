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
package com.trackit.business.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.ldap.ManageReferralControl;

import org.apache.log4j.Logger;
import org.sqlite.OSInfo;

import com.garmin.fit.Decode.RETURN;
import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.BoundingBox2;
import com.trackit.business.common.Location;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.AscentDescentType;
import com.trackit.business.domain.CircuitType;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DifficultyLevelType;
import com.trackit.business.domain.Folder;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.PhotoContainer;
import com.trackit.business.domain.Picture;
import com.trackit.business.domain.Session;
import com.trackit.business.domain.SportType;
import com.trackit.business.domain.SubSportType;
import com.trackit.business.domain.TrackConditionType;
import com.trackit.business.domain.TrackStatus;
import com.trackit.business.domain.TrackType;
import com.trackit.business.utilities.UniqueValidFilenamesList;

public class Database {

	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	private static Database database;
	private static int databaseVersion = 3;						// 12335: 2016-06-06
	
	private static String databaseName = "jdbc:sqlite:TrackIt.db";
	private Connection c               = null;
	private Statement  stmt            = null;
	private ResultSet  res             = null;
	private boolean    isConnected     = false;
	private String     foreign_keys    = "PRAGMA foreign_keys = ON";	// 12335: 2017-02-21
	
	private Path      workingDirectory = Paths.get(System.getProperty("user.dir"));
																		// 12335: 2015-09-11
	private static String TABLE_AUDIO   = "Audio";
	private static String TABLE_PICTURE = "Picture";
	private static String TABLE_VIDEO   = "Video";
	private static String TABLE_NOTE    = "Note";
	
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
				int startVersion = Integer.parseInt( versionToStart);
				System.out.println( "\nDB START VERSION: " + startVersion +"\n");
				switch ( startVersion ) {
					case 1:
						Files.copy( Paths.get( "TrackIt-V1.db"), Paths.get( "TrackIt.db"), StandardCopyOption.REPLACE_EXISTING);
						break;
					case 2:
						Files.copy( Paths.get( "TrackIt-V2.db"), Paths.get( "TrackIt.db"), StandardCopyOption.REPLACE_EXISTING);
						break;
					case 3:
						Files.copy( Paths.get( "TrackIt-V3.db"), Paths.get( "TrackIt.db"), StandardCopyOption.REPLACE_EXISTING);
						break;
					case 0:
						Files.deleteIfExists( Paths.get( "TrackIt.db"));
						break;
					case -1:
					default:
						break;
				}
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
						dbVersion             = (int) res.getDouble(1);
						Date databaseDate     = new Date( res.getLong( 2));
						Date sportsLastUpdate = new Date( res.getLong( 3));
						// correct SportsCatalogDate if = 0 - 2017-02-17, 12335
						if ( res.getLong( 3) == 0 ) {
							sportsLastUpdate = databaseDate;
							closeConnection();
							executeUpdateRequest( "UPDATE DBVersion SET SPortsCatalogDate="
									+ ( sportsLastUpdate.getTime()) + " WHERE VersionNo=" + dbVersion);
						}
						// Log DB version info
						logger.info( "DB Version no: " + dbVersion);
						logger.info( "DB created:           " + databaseDate);
						logger.info( "Sports catalog date:  " + sportsLastUpdate );
						logger.info( "Most recent document: " + getMostRecentDocumentDate()+ "\n");
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
					// Always recreate DBVersion table
					createVersionTable();
					// Version 1 had no Sports and subSports tables
					if ( dbVersion == 1) {
						createSportsTable();
						createSubSportsTable();
//						addSportsAndSubsports();			//12335: 2017-06-10
					}
					upgradeGPSFiles( dbVersion);
					upgradeMediaTable( TABLE_AUDIO, dbVersion);
					upgradeMediaTable( TABLE_PICTURE, dbVersion);
					upgradeMediaTable( TABLE_VIDEO, dbVersion);
					createMediaTable(  TABLE_NOTE);
					upgradeDesktopTable( dbVersion);
				}
				
				// make sure the Desktop table is correct
				correctDesktopTable();
				// make sure the GPSFiles table is correct
				correctGPSFilesTable();
				// make sure the SubSport table is correct
				correctSubSportTable();
			}
			else
				// No database, create from scratch
				createDatabaseCurrentTables();
						
		} catch (Exception e ) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("initDB");
			System.exit(0);
		}
		
		// Compare Sports Catalog to database, update default values as necessary
		checkSportsAndSubSports();
		// Load SubSport current (not default) data
		loadAllSubSportsCurrentValues( false);
		
		// make sure that table Note exists
		createMediaTable( TABLE_NOTE);
		
//		new MediaLoader( this);
	}
	
// ########################  OPERATION SUPPORT   ###########################################
	
	// 12335: 2015-09-12 
	public void openConnection() {
		try {
			c = DriverManager.getConnection(databaseName);
			stmt = c.createStatement();
//			stmt.executeUpdate("PRAGMA foreign_keys = ON");			// 2017-02-21: 12335
			stmt.executeUpdate( foreign_keys);
			isConnected = true;
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("openConnection");
			closeConnection();
		}
	}
	
	public void closeConnection() {
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
	public int executeUpdateRequest( String sql) {
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
	
	// 2017-02-21: 12335 - support: turn foreign keys on and off
	private void enableForeignKeys( Boolean enable) {
		foreign_keys = "PRAGMA foreign_keys = " + (enable? "ON": "OFF");
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
	
	// 12335: 2017-02-16, check whether a column exists in a table
	private boolean doesColumnExistInTable( String tableName, String columnName) {
		boolean exists = false;
		if ( doesDatabaseTableExists( tableName) ) {
			openConnection();
			if ( connected() ) {
				try {
					res = c.getMetaData().getColumns( null, null, tableName, columnName);
					exists = ! res.isAfterLast();
				} catch (Exception e) {
					logger.error( e.getClass().getName() + ": " + e.getMessage());
				} finally {
					closeConnection();
				}				
			} 
		}
		return exists;
	}
	
	public Connection getConnection(){
		return c;
	}

// ########################  OPERATION SUPPORT   - End   ###################################

// ########################  CURRENT TABLES CREATION   #####################################   
	
	private void createDatabaseCurrentTables() throws SQLException {
		createVersionTable();						// 2016-06-07: 12335, moved here 2017-02-15
		createGPSFilesTable();
		createMediaTable( TABLE_AUDIO);
		createMediaTable( TABLE_PICTURE);
		createMediaTable( TABLE_VIDEO);
		createMediaTable( TABLE_NOTE);				// 2016-10-13: 12335
		createDesktopTable();						// 2016-06-07: 12335
		createSportsTable();
		createSubSportsTable();
//		addSportsAndSubsports();					// 2017-06-10: 12335
		checkSportsAndSubSports();
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
				+ "MaxAltitude DOUBLE DEFAULT -100000,"
				+ "MinAltitude DOUBLE DEFAULT -100000,"
				+ "MinLongitude DOUBLE NOT NULL,"
				+ "MaxLongitude DOUBLE NOT NULL,"
				+ "MinLatitude DOUBLE NOT NULL,"
				+ "MaxLatitude DOUBLE NOT NULL,"
				+ "TrackState SMALLINT DEFAULT 0,"					// 2017-02-20: 71052
				+ "TrackDifficulty SMALLINT DEFAULT 0,"				// 2017-02-20: 71052
				+ "CircularPath BOOLEAN NOT NULL DEFAULT 0, "		// 2017-02-20: 71052
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
				+ "Timestamp INTEGER DEFAULT 0,"
				+ "Container TEXT NOT NULL, "
				+ "ParentName TEXT NOT NULL,"
				+ "PRIMARY KEY (Filepath, Container, ParentName) "
				+ "FOREIGN KEY (Container,ParentName) REFERENCES GPSFiles(Filepath,Name) "
				+ "ON UPDATE CASCADE ON DELETE CASCADE)";
		executeUpdateRequest(sql);
	}
	
	// Version 3, 2015-09-16, 12335
	// 2017-02-15, 12335 - Does both table creation and table update
	private void createVersionTable() {
		Date previousCatalogDate = null;
		
		// Delete table if it exists, preserving SportsCatalogDate
		if ( doesDatabaseTableExists( "DBVersion") ) {
			// Get SportsCatalogDate
			try {
				openConnection();
				if ( connected() ) {
					res = stmt.executeQuery( "SELECT SportsCatalogDate FROM DBVersion");
					if ( res.next() )
						previousCatalogDate = new Date( res.getLong( 1));
				}
			}
			catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				return;
			} finally {
				closeConnection();
			}

			// Drop the existing table
			executeUpdateRequest( "DROP TABLE DBVersion");
		}
		
		// Make sure the sports catalog date is OK
		if ( previousCatalogDate == null )
			previousCatalogDate = new Date();
		
		// Create the table
		executeUpdateRequest("CREATE TABLE IF NOT EXISTS DBVersion "
				+ "(VersionNo INTEGER DEFAULT " + databaseVersion + ", "
				+  "CreationDate INTEGER DEFAULT 0, "
				+  "SportsCatalogDate INTEGER DEFAULT 0)" );
		// And insert the values
		openConnection();
		if ( connected() ) {
			try {
				PreparedStatement pstmt = c.prepareStatement("INSERT INTO DBVersion "
						+ "(VersionNo,CreationDate,SportsCatalogDate) VALUES (?,?,?)");
				pstmt.setDouble(1, databaseVersion);
				pstmt.setLong(2, (new Date()).getTime());
				pstmt.setLong( 3, previousCatalogDate.getTime());
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
	
	// Version 3, 2016-06-09, 12335
	private void createDesktopTable() {
		executeUpdateRequest("CREATE TABLE IF NOT EXISTS Desktop "
				+ "(DocumentName TEXT NOT NULL,"
				+ " Filepath TEXT NOT NULL,"
				+ " TrackName TEXT NOT NULL,"
				+ " IsActivity SMALLINT DEFAULT -1,"
				+ " Sport SMALLINT DEFAULT -1,"
				+ " SubSport SMALLINT DEFAULT -1,"
//				+ " Folder SMALLINT DEFAULT 1,"						// 12335: 2016-10-12
				+ " InWorkspace BOOLEAN NOT NULL DEFAULT 1,"
				+ " Loaded SMALLINT DEFAULT 0,"
//				+ " PRIMARY KEY(TrackName,Filepath,IsActivity)"  	// 12335: 2017-02-23
				+ " PRIMARY KEY(TrackName,Filepath,IsActivity,InWorkspace)" 
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
	
	// Version 2,
	//         2a, added AscentDescentClass column
	private void createSubSportsTable() {
		String sql = "CREATE TABLE IF NOT EXISTS SubSport "
				+ "(TableID SHORT NOT NULL,"
				+ "SportID SMALLINT NOT NULL,"
				+ "SportName TEXT NOT NULL,"
				+ "SubSportID SMALLINT NOT NULL,"
				+ "SubSportName TEXT NOT NULL,"
				+ "AscentDescentClass SMALLINT DEFAULT 0,"		//12335: 2017-06-07
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
		enableForeignKeys( false);										// 2017-02-21: 12335
		executeUpdateRequest( "ALTER TABLE GPSFiles RENAME TO Temp");
		createGPSFilesTable();
		String list = "Filepath,Name,";
//		if ( fromVersion == 2 )											// 2017-02-21: 12335
		if ( fromVersion >= 2 )
			list += "Sport,SubSport,";
		if ( fromVersion >= 3)
			list += "StartTime,EndTime,TotalTime,MovingTime,Distance,Ascent,Descent,MaxAltitude,MinAltitude,";
		list += "MinLongitude,MaxLongitude,MinLatitude,MaxLatitude";
		String sql = "INSERT OR REPLACE INTO GPSFiles (" + list + ") SELECT " + list+ " FROM Temp";
		int code = executeUpdateRequest( sql);
		executeUpdateRequest( "DROP TABLE Temp");
		logger.info( "Table GPSFiles upgraded, # rows converted: " + code);
		enableForeignKeys( true);										// 2017-02-21: 12335
	}
	
	// 2016-06-07: 12335
	private void upgradeMediaTable( String mediaTableName, int fromVersion) {
		// From version 2 to version 3 we only need to copy
		// (the difference was the foreign keys definition)
		enableForeignKeys( false);												// 12335: 2017-02-23
		executeUpdateRequest( "ALTER TABLE " + mediaTableName + " RENAME To TempMedia");
		createMediaTable( mediaTableName);
		String list = "Filepath,Name,Longitude,Latitude,Altitude,Container,ParentName";
		String sql = "INSERT OR REPLACE INTO " + mediaTableName + "(" + list + ") SELECT "
				   + list + " FROM TempMedia";
		int code = executeUpdateRequest( sql);
		executeUpdateRequest( "DROP TABLE TempMedia");
		enableForeignKeys( true);												// 12335: 2017-02-23
		logger.info( "Table " + mediaTableName + " upgraded, # rows converted: " + code);
	}
	
	// 2017-02-15: 12335
	// Changes SportsCatalogDate to the current date
	private void updateSportsCatalogDate( Date catalogDate) {
		openConnection();
		if ( connected() ) {
			try {
				Date sportsCatalogDate = catalogDate;
				if ( catalogDate == null )
					sportsCatalogDate = new Date();
				executeUpdateRequest( "UPDATE DBVersion SET SPortsCatalogDate="
						+ ( sportsCatalogDate.getTime()) + " WHERE VersionNo=" + databaseVersion);
			}
			catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				return;
			} finally {
				closeConnection();
			}
		}
	}
	
	// 2017-07-08: 12335
	private Date getSportsCatalogDate() {
		Date sportsCatalogDate = new Date();
		openConnection();
		if ( connected() ) {
			try {
				res = stmt.executeQuery( "SELECT SportsCatalogDate FROM DBVersion WHERE VersionNo=" + databaseVersion);
				while ( res.next() )
					sportsCatalogDate = new Date( res.getLong(1));
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		}
		return sportsCatalogDate;
	}
	
//	// 2016-06-07: 12335
//	private void upgradeVersionTable( int fromVersion) {
//		if ( fromVersion < 3 )
//			createVersionTable();
//		openConnection();
//		if ( connected() ) {
//			try {
//				PreparedStatement pstmt = c.prepareStatement("INSERT INTO DBVersion "
//						+ "(VersionNo,CreationDate) VALUES (?,?)");
//				pstmt.setDouble(1, databaseVersion);
//				pstmt.setLong(2, (new Date()).getTime());
//				pstmt.executeUpdate();
//				pstmt.close();
//			} catch (Exception e) {
//				logger.error(e.getClass().getName() + ": " + e.getMessage());
//				return;
//			} finally {
//				closeConnection();
//			}
//		}
//	}
	
	// 2017-06-08
	private void correctSubSportTable() {
		// make sure that column AscentDescentClass exists in SubSport table
		if ( ! doesColumnExistInTable( "SubSport", "AscentDescentClass") ) {
			openConnection();
			try {
				enableForeignKeys( false);												// 12335: 2017-02-23
				executeUpdateRequest( "ALTER TABLE SubSport RENAME To TempSubSport");
				createSubSportsTable();
				String list = 
						  "TableID,SportID,SportName,SubSportID,SubSportName,"
						+  "DefaultAverageSpeed,MaximumAllowedSpeed,"
						+ "PauseThresholdSpeed,DefaultPauseDuration,FollowRoads,"
						+ "JoinMaximumWarningDistance,JoinMergeDistanceTolerance,JoinMergeTimeTolerance,"
						+ "GradeLimit,"
						+ "CurrentDefaultAverageSpeed,CurrentMaximumAllowedSpeed,"
						+ "CurrentPauseThresholdSpeed,CurrentDefaultPauseDuration,CurrentFollowRoads,"
						+ "CurrentJoinMaximumWarningDistance,CurrentJoinMergeDistanceTolerance,"
						+ "CurrentJoinMergeTimeTolerance,"
						+ "CurrentGradeLimit";
				String sql = "INSERT OR REPLACE INTO SubSport (" + list + ") SELECT "
						   + list + " FROM TempSubSport";
				int code = executeUpdateRequest( sql);
				executeUpdateRequest( "DROP TABLE TempSubSport");
				enableForeignKeys( true);												// 12335: 2017-02-23
				logger.info( "Table SubSport upgraded, # rows converted: " + code + "\n");
			} catch (Exception e) {
				logger.error( e.getClass().getName() + ": " + e.getMessage());
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
//				String sql ="INSERT INTO Desktop (DocumentName,TrackName,Filepath,Sport,SubSport,Folder) "
//						+   "VALUES (?,?,?,?,?,1)";
				String sql ="INSERT INTO Desktop (DocumentName,TrackName,Filepath,Sport,SubSport,InWorkspace) "
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
	
	// 2017-02-20: 12335
	private void correctGPSFilesTable() {
		// Make sure that all version 3 columns are present
		if ( !doesColumnExistInTable( "GPSFiles", "TrackState")  )
			upgradeGPSFiles( 3);
	}
	
	// 2017-02-17: 12335 - Convert any Desktop table with column Folder instead of InWorkspace
	private void correctDesktopTable() {
		// Rename column Folder to InWorspace
		if ( doesColumnExistInTable( "Desktop", "Folder") ) {
			logger.info( "Correcting Desktop table: Folder -> InWorkspace");
			executeUpdateRequest( "ALTER TABLE Desktop RENAME TO TempDesktop");
			createDesktopTable();
			String srcList = "DocumentName,Filepath,TrackName,IsActivity,Sport,SubSport,";
			String dstList = srcList + "InWorkspace,Loaded";
			srcList       += "Folder,Loaded";
			String sql     = "INSERT OR REPLACE INTO Desktop (" + dstList + ") SELECT " + srcList
					       + " FROM TempDesktop";
			int code = executeUpdateRequest( sql);
			executeUpdateRequest( "DROP TABLE TempDesktop");
			logger.info( "Table Desktop corrected, # rows converted: " + code);
		}
		
		// Make sure column InWorkspace is part of the primary key		// 12335: 2017-02-23
		boolean needsConversion = false;
		openConnection();
		if ( connected() ) {
			try {
				res = stmt.executeQuery( "PRAGMA table_info(Desktop)");
				while( res.next() ) {
					if ( res.getString( "name").equals( "InWorkspace") && res.getInt( "pk") == 0 ) {
						needsConversion = true;
						break;
					}
				}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		}
		if ( needsConversion ) {
			logger.info( "Correcting Desktop Table: making InWorkspace part of primary key");
			executeUpdateRequest( "ALTER TABLE Desktop RENAME TO TempDesktop");
			createDesktopTable();
			String list = "DocumentName,Filepath,TrackName,IsActivity,Sport,SubSport,InWorkspace,Loaded";
			String sql     = "INSERT OR REPLACE INTO Desktop (" + list + ") SELECT " + list
					       + " FROM TempDesktop";
			int code = executeUpdateRequest( sql);
			executeUpdateRequest( "DROP TABLE TempDesktop");
			logger.info( "Table Desktop corrected, # rows converted: " + code);
		}
	}
	
	// ########################  UPGRADE FROM PREVIOUS VERSIONS   - End   ###################### 
	
	
	// ########################           COMMON SUPPORT                  ######################
	
	// 12335: 2017-03-16 - get a short value from Desktop and/or GPSFiles table
	private short getShortField( String fieldName, String filepath, String trackName,
							     boolean isActivity) {
		short value = -1;
		openConnection();
		if ( connected() ) {
			String sql = "";
			if ( fieldName == "Sport" || fieldName == "SubSport" )
				sql = "SELECT " + fieldName + " FROM desktop WHERE Filepath='" + filepath
				    + "' AND TrackName='" + trackName + "' AND Isactivity='"
				    + (isActivity?1:0) + "' UNION ";
			sql += "SELECT " + fieldName + " FROM GPSFiles WHERE Filepath='" 
				 + filepath + "' AND Name='" + trackName + "' AND Isactivity='" + (isActivity?1:0) +"'";
			try {
				res = stmt.executeQuery( sql);
				while( res.next() ) 
					value = res.getShort(1);
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace();
			} finally {
				closeConnection();
			}
		}
		return value;
	}
	
	//12335: 2018-02-30 - searches registered files in reverse chronological order for the latest existing file
	public Date getMostRecentDocumentDate() {
		Date latest = null;
		if ( ! connected() )
			openConnection();
		if ( connected() ) {
			try {
				res = stmt.executeQuery( "SELECT Filepath, EndTime from GPSFiles ORDER BY EndTime DESC");
				while( res.next() ) {
					System.out.println( (new Date( res.getLong( 2))) + "  " + res.getString( 1));
					if ( Files.exists( Paths.get( res.getString( 1)))) {
						latest = new Date( res.getLong( 2));
						break;
					}
				}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace();
			} finally {
				closeConnection();
			}
//			openConnection();
//			try {
//				res = stmt.executeQuery( "SELECT MAX(EndTime) FROM GPSFiles");
//				while( res.next() )
//					latest = new Date(res.getLong(1));
//			} catch ( Exception e ) {
//				logger.error(e.getClass().getName() + ": " + e.getMessage());
//				e.printStackTrace();
//			} finally {
//				closeConnection();
//			}
		}
		return latest;
	}

	// ########################           COMMON SUPPORT   - end          ######################
	
	
	// ########################  SPORTS AND SUBSPORTS SUPPORT   ################################   
	
	//12335: 2017-06-09
	public void checkSportsAndSubSports() {	
		logger.info( "Checking the Sports and SubSports Catalog");

		// Check if the sports table misses any SportType value 
		int noSportsAdded = 0;
		List<Short> sportsIdsInDB = getSportsIds();
		for ( SportType sport: Arrays.asList( SportType.values())) {
			if ( checkSport( sport) )
				noSportsAdded++;
			if ( sportsIdsInDB.contains( (Short) sport.getSportID()) )
				sportsIdsInDB.remove( (Short) sport.getSportID());
		}
		
		// Add all SubSportType values missing in the SubSport table
		int noSubSportsAdded = 0;
		for( SubSportType subsport: Arrays.asList( SubSportType.values()))
			if ( checkSubSport(subsport) )
				noSubSportsAdded ++;

		// Remove from Sport table any sports without a SportType
		// and set Sport and SubSport to invalid in all affected GPSFiles entries
		int noSportsDeleted         = 0;
		int noRecordsModifiedSports = 0;
		for( short sportIdToRemove: sportsIdsInDB ) {
			noRecordsModifiedSports += removeSport( sportIdToRemove);
			noSportsDeleted ++;
		}

		// Remove from SubSport table any subsports without a SubSportType
		// and set Sport and SubSport to invalid in all affected GPSFiles entries
		TreeMap<Short, HashSet<Short>> subSportsInDB = new TreeMap<>();
		// get all subsports in the DB
		openConnection();
		if ( connected() ) {
			try {
				// get all subsports in the SubSport table
				res = stmt.executeQuery( "SELECT SportID, SubSportID FROM SubSport");
				while( res.next() ) {
					short sportId = res.getShort( 1);
					short subsportId = res.getShort( 2);
					if ( !subSportsInDB.containsKey( sportId) )
						subSportsInDB.put( sportId, new HashSet<Short>());
					subSportsInDB.get( sportId).add( subsportId);
					}
				// them all subsports in the GPSFiles table
				res = stmt.executeQuery( "SELECT Sport, SubSport FROM GPSFiles");
				while ( res.next() ) {
					short sportId = res.getShort( 1);
					short subsportId = res.getShort( 2);
					if ( !subSportsInDB.containsKey( sportId) )
						subSportsInDB.put( sportId, new HashSet<Short>());
					subSportsInDB.get( sportId).add( subsportId);
				}
//				for( short sport: subSportsInDB.keySet())
//					if ( ! subSportsInDB.get( sport).isEmpty() ) {
//						System.out.print( sport + " :");
//						for( short sub: subSportsInDB.get( sport) )
//							System.out.print( " " + sub);
//						System.out.println();
//					}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				logger.error("CheckSportsAndSubSports: obtaining subsports complete list in DB");
			} finally {
				closeConnection();
			}
		}
		// Filter, leaving only those that have no SubSportType value 
		for( SubSportType subsport: Arrays.asList( SubSportType.values()) ) {
			short sportId   = subsport.getSportID();
			if ( subSportsInDB.containsKey( sportId) )
				subSportsInDB.get( sportId).remove( subsport.getSubSportID());
		}
		// and remove all them from the SubSport table and GPSFiles table
		int noSubSportsDeleted  = 0;
		int noRecordsModifiedSubSports = 0;
		for( Short sportId: subSportsInDB.keySet()) {
			HashSet<Short> subSportsIds = subSportsInDB.get( sportId);
			if ( ! subSportsIds.isEmpty() ) {
				for( short subSportId: subSportsIds) {
					noRecordsModifiedSubSports += removeSubSport( sportId, subSportId);
					logger.info( "Removed SubSport with SportID: " + sportId + "  SubSportID: " + subSportId);
					noSubSportsDeleted ++;
				}
			}
		}
		
		// Report
		int totalModified = noSportsAdded + noSportsDeleted + noSubSportsAdded + noSubSportsDeleted;
		if ( totalModified > 0 ) {
			if ( noSportsAdded > 0)
				logger.info( "Sport table: " + noSportsAdded + " entries were added or updated.");
			if ( noSportsDeleted > 0)
				logger.info( "Sport table: " + noSportsDeleted + " entries were removed from the table.\n\t"
					    + noRecordsModifiedSports + " GPSFiles table entries were modified.");
			if ( noSubSportsAdded > 0 )
				logger.info( "SubSport table: " + noSubSportsAdded + " entries were added or updated.");
			if ( noSubSportsDeleted > 0 )
				logger.info( "SubSport table: " + noSubSportsDeleted + " entries were removed from the table.\n\t"
						    + noRecordsModifiedSubSports + " GPSFiles table entries were modified.");
			updateSportsCatalogDate(null);
		}
		else
			logger.info( "The Sports and SubSports Catalog is up to date.");
		logger.info( "\n");
	}
		
	// 2017-06-08: 12335
	private boolean checkSport( SportType sport) {
		boolean modified = false;
		openConnection();
		try {
			boolean needsUpdate = false;
			String sql = "SELECT * FROM Sport WHERE SportID='" + sport.getSportID() + "'";
			res = stmt.executeQuery( sql);
			if ( res.next() ) {
				// Check if there was a name change, otherwise no update is needed
				if ( ! res.getString( "Name").equals( sport.getName()) )
					needsUpdate = true;
			} else {
				// sport does not exist in DB, needs to be inserted
				needsUpdate = true;
			}
			if ( needsUpdate ) {
				sql = "INSERT OR REPLACE INTO Sport (SportID, Name) VALUES (?,?)";
				PreparedStatement statement = c.prepareStatement(sql);
				statement.setShort(  1, sport.getSportID());
				statement.setString( 2, sport.getName());
				statement.executeUpdate();
				statement.close();
				logger.info( "Sport " + sport.getName() + " (" + sport.getSportID() + ") updated.");
				modified = true;
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("Checking Sport");
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		return modified;
	}

	// 2017-06-09: 12335
	private boolean checkSubSport( SubSportType subSport) {
		boolean modified = false;
		openConnection();
		try {
			short   tableID     = 0;
			boolean needsUpdate = false;
			String sql = "SELECT * FROM SubSport WHERE SubSportID='" + subSport.getSubSportID()
			           + "' AND SportID='" + subSport.getSportID() + "'";
			res = stmt.executeQuery( sql);
			if ( res.next() ) {
				// exists in DB, but values need checking
				if ( res.getShort( "AscentDescentClass")          != subSport.getAscentDescentClass()         ||
					 res.getDouble( "DefaultAverageSpeed")        != subSport.getDefaultAverageSpeed()        ||
					 res.getDouble( "MaximumAllowedSpeed")        != subSport.getMaximumAllowedSpeed()        ||
					 res.getDouble( "PauseThresholdSpeed")        != subSport.getPauseThresholdSpeed()        ||
					 res.getLong( "DefaultPauseDuration")         != subSport.getDefaultPauseDuration()       ||
					 res.getBoolean( "FollowRoads")               != subSport.getFollowRoads()                ||
					 res.getDouble( "JoinMaximumWarningDistance") != subSport.getJoinMaximumWarningDistance() ||
					 res.getDouble( "JoinMergeDistanceTolerance") != subSport.getJoinMergeDistanceTolerance() ||
					 res.getLong( "JoinMergeTimeTolerance")       != subSport.getJoinMergeTimeTolerance()     ||
					 res.getDouble( "GradeLimit") 				  != subSport.getGradeLimit()				  ||
					 !res.getString( "SubSportName").equals( subSport.getName())  		             			) {
					// exists but values have changed, needs updating
					needsUpdate = true;
					tableID     =  res.getShort( "TableID");
				}				
			} else {
				// does not exist, must be inserted
				needsUpdate = true;
				closeConnection();
				tableID     = newSubSportTableID();
				openConnection();
			}
			if ( needsUpdate ) { 
				sql = "INSERT OR REPLACE INTO SubSport (TableID, SportID, SportName, SubSportID, SubSportName, "
						+ "AscentDescentClass,"
						+ "DefaultAverageSpeed, MaximumAllowedSpeed, PauseThresholdSpeed, DefaultPauseDuration, "
						+ "FollowRoads, JoinMaximumWarningDistance, JoinMergeDistanceTolerance, JoinMergeTimeTolerance, GradeLimit, "
						+ "CurrentDefaultAverageSpeed, CurrentMaximumAllowedSpeed, CurrentPauseThresholdSpeed, CurrentDefaultPauseDuration, "
						+ "CurrentFollowRoads, CurrentJoinMaximumWarningDistance, CurrentJoinMergeDistanceTolerance, CurrentJoinMergeTimeTolerance, CurrentGradeLimit) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				PreparedStatement statement = c.prepareStatement(sql);				
				statement.setShort(    1, tableID);
				statement.setShort(    2, subSport.getSportID());
				statement.setString(   3, SportType.lookup( subSport.getSportID()).getName());
				statement.setShort(    4, subSport.getSubSportID());
				statement.setString(   5, subSport.getName());
				statement.setShort(    6, subSport.getAscentDescentClass());
				statement.setDouble(   7, subSport.getDefaultAverageSpeed());
				statement.setDouble(   8, subSport.getMaximumAllowedSpeed());
				statement.setDouble(   9, subSport.getPauseThresholdSpeed());
				statement.setLong(    10, subSport.getDefaultPauseDuration());
				statement.setBoolean( 11, subSport.getFollowRoads());
				statement.setDouble(  12, subSport.getJoinMaximumWarningDistance());
				statement.setDouble(  13, subSport.getJoinMergeDistanceTolerance());
				statement.setLong(    14, subSport.getJoinMergeTimeTolerance());
				statement.setDouble(  15, subSport.getGradeLimit());
				statement.setDouble(  16, subSport.getDefaultAverageSpeed());
				statement.setDouble(  17, subSport.getMaximumAllowedSpeed());
				statement.setDouble(  18, subSport.getPauseThresholdSpeed());
				statement.setLong(    19, subSport.getDefaultPauseDuration());
				statement.setBoolean( 20, subSport.getFollowRoads());
				statement.setDouble(  21, subSport.getJoinMaximumWarningDistance());
				statement.setDouble(  22, subSport.getJoinMergeDistanceTolerance());
				statement.setLong(    23, subSport.getJoinMergeTimeTolerance());
				statement.setDouble(  24, subSport.getGradeLimit());
				statement.executeUpdate();
				statement.close();
				short sportId = subSport.getSportID();
				logger.info( "SubSport " + subSport.getName() + " (" + subSport.getSubSportID()
						    + ") - Sport: " + SportType.lookup( sportId).getName() + " ("+ sportId + ") - updated.");
				modified = true;
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("Checking SubSport");
			e.printStackTrace();
		} finally {
			closeConnection();
		}
		return modified;
	}
	
	// 2017-06-08: 12335
	private short newSubSportTableID() {
		TreeSet<Short> tableIds = new TreeSet<>( getSubSportTableIds());
		short tableID = 0;
		while( tableIds.contains( tableID) )
			tableID++;
		return tableID;
	}
		
	//12335: 2017-06-10
	private int removeSport( short sportID) {
		int recordsUpdated = 0;
		openConnection();
		if ( connected() )
			try {
				// get the name of the sport
				String name = "NONAME";
				String sql = "SELECT Name FROM Sport WHERE SportID=" + sportID;
				res = stmt.executeQuery( sql);
				if ( res.next() )
					name = res.getString( 1);
				// get the SubSports of this Sport, first from activities and courses
				TreeSet<Short> subSportIds = new TreeSet<>();
				sql = "SELECT SubSport FROM GPSFiles WHERE Sport=" + sportID;
				res = stmt.executeQuery( sql);
				while( res.next() )
					subSportIds.add( res.getShort( 1));
				// and then from the SubSport table
				sql = "SELECT SubSportID FROM SubSport WHERE SportID=" + sportID;
				res = stmt.executeQuery( sql);
				while ( res.next() )
					subSportIds.add( res.getShort( 1));
				closeConnection();
				// Remove the SubSports of this Sport, one at a time
				for( short subSportId: subSportIds) {
					logger.info( "Removing " + sportID + " " + subSportId);
					recordsUpdated += removeSubSport( sportID, subSportId);
				}
				// Finally, remove the Sport from the Sport table
				openConnection();
//				enableForeignKeys( false);
				sql = "DELETE FROM Sport WHERE SportID=" + sportID;
				stmt.execute( sql);
//				enableForeignKeys( true);
				// And log the result
				logger.info( "Removed Sport " + name + " (" + sportID + ") updating " + recordsUpdated + " records.");
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				logger.error("Removing Sport");
				e.printStackTrace();
			} finally {
				closeConnection();
			}
		return recordsUpdated;
	}
	
	//12355: 2017-06-09
	private int removeSubSport( short sportId, short subSportId) {
		int recordsUpdated = 0;
		openConnection();
		if ( connected() )
			try {
				String sql = "UPDATE GPSFiles SET Sport=-1, SubSport =-1 WHERE Sport=" + sportId
						+    " AND SubSport=" + subSportId;
				recordsUpdated = stmt.executeUpdate( sql);
				stmt.executeUpdate( "DELETE FROM SubSport WHERE SubSportID=" + subSportId + " AND SportID=" + sportId);
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				logger.error("Removing SubSport");
				e.printStackTrace();
			} finally {
				closeConnection();
			}
		return recordsUpdated;
	}
	
	//12335: 2017-04-22
	private boolean doesSubSportsExistInDB( SubSportType subSport) {
		boolean exists = false;
		openConnection();
		if ( connected() )
			try {
				String sql = "SELECT TableID From SubSport WHERE SubSportID='" + subSport.getSubSportID() +
						     "' AND SportID='" + subSport.getSportID() + "'";
				exists = !stmt.executeQuery(sql).isAfterLast();
			} catch (Exception e) {
				logger.error( e.getClass().getName() + ": " + e.getMessage());
				logger.error( "doesSubSportsExistInDB");
			} finally {
				closeConnection();
			}
		return exists;
	}
	
	private List<Short> getSubSportTableIds() {
		List<Short> tableIds = new ArrayList<>();
		openConnection();
		if ( connected() )
			try {
				res = stmt.executeQuery( "SELECT TableID FROM SubSport");
				while( res.next() )
					tableIds.add( res.getShort( "TableID"));
			}
			catch (Exception e) {
				logger.error( e.getClass().getName() + ": " + e.getMessage());
				logger.error( "getSubSportTableIds");
			}
			finally {
				closeConnection();
			}
		return tableIds;
	}

// ########################  SPORTS AND SUBSPORTS SUPPORT   - End   ########################   

//########################### START AND STOP ######################################
	
	// 12335: 2015-09-17 
	// 12335: 2016-10-12: argument folderID substituted by folder to do away with folderID
	public ArrayList<String> initFromDb( Folder folder) {
//		ArrayList<String> filepathList = new ArrayList<String>();		// 2017-02-16: 12335
		UniqueValidFilenamesList filepathList = new UniqueValidFilenamesList();
		openConnection();
		if ( connected() ) {
			int inWorkspace = DocumentManager.getInstance().isWorspaceFolder( folder) ? 1 : 0;
			try {
				res = stmt.executeQuery("SELECT Filepath FROM Desktop WHERE InWorkspace='"
						+ inWorkspace + "'");
				while ( res.next() ) {
					filepathList.add( res.getString( 1));
				}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());			
			} finally {
				closeConnection();
			}
			for( String filename : filepathList.getFilenames())
				executeUpdateRequest("UPDATE Desktop SET Loaded=1 WHERE Filepath='"
						+ filename +"' AND InWorkspace='" + inWorkspace + "'");				
		}
//		return filepathList;											// 2017-02-16: 12335
		return filepathList.getFilenames();
	}
	//12335: 2017-03-10
	public List<String> getDocumentActivitiesNames( String filepath, Folder folder) {
		return getDocumentItemsNames( filepath, folder, true);
	}

	//12335: 2017-03-10
	public List<String> getDocumentCoursesNames( String filepath, Folder folder) {
		return getDocumentItemsNames( filepath, folder, false);
	}

	//12335. 2017-03-10
	private List<String> getDocumentItemsNames( String filepath, Folder folder, boolean activities) {
		List<String> results = new ArrayList<>();
		
		openConnection();
		if ( connected() ) {
			String sql = "";
			try {
				int inWorkspace = DocumentManager.getInstance().isWorspaceFolder( folder) ? 1 : 0;
				sql = "SELECT TrackName FROM Desktop WHERE Filepath='" + filepath
						  +  "' AND IsActivity='" + (activities ? 1 : 0)
						  +  "' AND InWorkspace='" + inWorkspace + "'";
				res = stmt.executeQuery( sql);
				while ( res.next() ) {
					results.add( res.getString( 1));
				}
			} catch (Exception e) {
				logger.error( sql);			
				logger.error(e.getClass().getName() + ": " + e.getMessage());			
			} finally {
				closeConnection();
			}
		}

		return results;
	}
	
	// 12335: 2015-09-14 - Close the database
	// Update document working set and check/fix documents for inconsistencies
	// 12335: 2016-06-08 - All tracks (activities and courses) are now registered
	//                     instead of document names and filepaths only
	// 12335: 2016-06-09 - Provision to store library documents besides workspace documents
	// 12335: 2016-10-12 - Done away with Folder.getFolderID() calls
	// 12335: 2016-12-13 - Renamed to saveWorkspaceAndLibraryStatus
//	public void closeDatabase( List<GPSDocument> documents) {
	public void saveWorkspaceAndLibraryStatus( List<GPSDocument> documents) {
		executeUpdateRequest("DELETE FROM Desktop WHERE Loaded=1");
		DocumentManager manager = DocumentManager.getInstance();
		Folder workspaceFolder  = manager.getWorspaceFolder();
		GPSDocument collectionDocument = manager.getDefaultCollectionDocument();  	//12335: 2017-03-09
		for( GPSDocument doc : documents)
			System.out.println( manager.getFolder( doc).equals( workspaceFolder) ? 1 : 0 + doc.getFileName());
		for( GPSDocument doc: documents) {
			System.out.println( "Closing " + doc.getName());
//			if ( (new File(doc.getFileName())).exists() ) {
			if ( (new File(doc.getFileName())).exists() || doc.equals( collectionDocument)) {
//				int folderID = manager.getFolder( doc).getFolderID();
				int inWorkspace = manager.getFolder( doc).equals( workspaceFolder) ? 1 : 0;
//				for( Activity activity : doc.getActivities())			// 12335: 2017-02-23
//					executeUpdateRequest(
//						"INSERT OR REPLACE INTO Desktop (DocumentName,Filepath,TrackName,Sport,SubSport,IsActivity,InWorkspace) "
//					  + " Values ('" + doc.getName() 
//					  + "','" + doc.getFileName()
//					  + "','" + activity.getName()
//					  + "','" + activity.getSport().getSportID()
//					  + "','" + activity.getSubSport().getSubSportID()
//					  + "','1"
//					  + "','" + inWorkspace
//					  + "')");
//				for( Course course : doc.getCourses() )
//					executeUpdateRequest(
//						"INSERT OR REPLACE INTO Desktop (DocumentName,Filepath,TrackName,Sport,SubSport,IsActivity,InWorkspace) "
//					  + " Values ('" + doc.getName() 
//					  + "','" + doc.getFileName()
//					  + "','" + course.getName()
//					  + "','" + course.getSport().getSportID()
//					  + "','" + course.getSubSport().getSubSportID()
//					  + "','0"
//					  + "','" + inWorkspace
//					  + "')");
				String documentName = doc.getName();
				if ( doc.equals( collectionDocument) )
					documentName = Messages.getMessage( "document.collection.databaseName");
				String docFileName  = doc.getFileName();
				String fileName     = docFileName;
				for( Activity activity: doc.getActivities() ) {
					if ( doc.equals( collectionDocument) )
						fileName = activity.getFilepath();
					insertOrUpdateDesktopEntry( documentName, fileName, activity.getName(), 1,
							                    inWorkspace, activity.getSport().getSportID(),
							                                 activity.getSubSport().getSubSportID());
				}
				for( Course course: doc.getCourses() ) {
					if ( doc.equals( collectionDocument) )
						fileName = course.getFilepath();
					insertOrUpdateDesktopEntry( documentName, fileName, course.getName(), 0,
							                    inWorkspace, course.getSport().getSportID(),
							                    			 course.getSubSport().getSubSportID());
				}
			}
		}
	}
	
	// 12335: 2017-02-23
	private void insertOrUpdateDesktopEntry( String documentName, String filepath, String trackName,
											 int isActivity, int inWorkspace, int sportID, int subSportID) {
		String where = "WHERE Filepath='" + filepath +
				       "' AND TrackName='" + trackName +
				       "' AND IsActivity='" + isActivity +
				       "' AND InWorkspace='" + inWorkspace + "'";
		boolean exists = false;
		openConnection();
		if ( connected() ) 
			try {
				exists = stmt.executeQuery( "SELECT Loaded FROM Desktop " + where).next();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());			
			}finally {
				closeConnection();
			}
		if ( exists )
			executeUpdateRequest( "UPDATE Desktop SET Loaded='0' " + where);
		else {
			String sql = "INSERT INTO Desktop (DocumentName,Filepath,TrackName,IsActivity,Sport,SubSport,InWorkspace) "
					  + " Values ('" + documentName
					  + "','" + filepath
					  + "','" + trackName
					  + "','" + isActivity
					  + "','" + sportID
					  + "','" + subSportID
					  + "','" + inWorkspace
					  + "')";
			executeUpdateRequest( sql);
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
		return executeUpdateRequest("UPDATE Desktop SET Loaded=" +
				(state?"1":"0") + " WHERE Filepath='" + document.getFileName() + "'");
	}
	
	// 12335: 2015-10-03 - Removes document from the Desktop table
	public int deleteFromDocumentsList( GPSDocument document, boolean isLoaded) {
		return executeUpdateRequest("DELETE FROM Desktop Where Filepath='" + 
				document.getFileName() + "' AND Loaded=" + (isLoaded?"1":"0"));
	}
	
	// 12335: 2016-10-13 - check whether DB update (of any kind) is necessary
	public boolean documentNeedsDBUpdate( GPSDocument document) {
		
		// Check document
		// Was it renamed?
		if ( document.wasRenamed() )
			return true;
		// Were there any new activities and/or courses or any Activities/course were deleted?
		if ( document.hasUnsavedChanges() )
			return true;
		
		TrackStatus status;
		
		// Now check courses, one at a time
		for( Course c : document.getCourses() ) {
			// Were Sport and/or SubSport modified?
			if ( c.getSport().getSportID()       != getSport( c)   || 
				 c.getSubSport().getSubSportID() != getSubSport( c) )
				return true;
			status = c.getStatus();
			// Was course renamed or course data changed?
			if ( status.wasRenamed() || status.trackWasChanged() )
				return true;
			// Was any media changed?
			if ( status.picturesWereChanged() || status.moviesWereChanged() ||
				 status.audioWasChanged()     || status.notesWereChanged()     )
				return true;
		}
		
		// Finally check activities, one at a time
		for( Activity a : document.getActivities() ) {
			// Were Sport and/or SubSport modified?
			if ( a.getSport().getSportID()       != getSport( a)   || 
				 a.getSubSport().getSubSportID() != getSubSport( a) )
				return true;
			status = a.getStatus();
			// Was activity renamed?
			if ( status.wasRenamed() )
				return true;
			// Was any media changed?
			if ( status.picturesWereChanged() || status.moviesWereChanged() ||
				 status.audioWasChanged()     || status.notesWereChanged()     )
				return true;
		}

		// All checks complete, there is no need to update the DB
		return false;
	}

	// 12335: 2015-09-30 - update whole document
	public void updateDB( GPSDocument document, boolean startFromScratch) {
		String sql;
		String docFilename = document.getFileName();
		List <String> activitiesInDB, coursesInDB;
		
		System.out.println("PHASE 1");
		// PHASE 1: Overwrite cleanup
		// Remove everything in the DB if 
		// 1) Export to a file that is to be overwritten
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
			if ( doesDocumentExistInDB( document) ) {
				System.out.println("moving on");
				// Handle document renaming
				if ( document.wasRenamed() ) {
					System.out.println("updating name");
					sql = "UPDATE Desktop SET DocumentName ='" + document.getName()
					    + "' WHERE Filepath='" + docFilename + "'";
					System.out.println("executing " + sql);
					if ( executeUpdateRequest(sql) == -1 )
						return;
					document.setOldNameWhenSaving();
				}
				System.out.println("activities and courses rename");
				// Handle activity and course renaming
				// To avoid name collisions in the DB (e.g., when name swapping)
				// assign unique provisional names
				activitiesInDB = getActivityNames( docFilename);
				coursesInDB    = getCourseNames(   docFilename);
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
		activitiesInDB = getActivityNames( docFilename);
		for( Activity a: document.getActivities()) {
			TrackStatus status = a.getStatus();
			if ( status.mediaWasChanged() || a.hasChangedDBOnlyData() ||
				 !activitiesInDB.contains(a.getName())                     )
				updateDB( a, document);
		}
		coursesInDB = getCourseNames( docFilename);
		for( Course c: document.getCourses() ) {
			TrackStatus status = c.getStatus();
			if ( status.trackWasChanged()   || status.mediaWasChanged() || 
				 c.hasChangedDBOnlyData() || !coursesInDB.contains(c.getName())  )
				updateDB( c, document);
		}
		
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
		return result;
	}
	
	// 12335: 2016-10-10
	public boolean doesMediaExistInDB( String mediaFilepath,   String mediaDocumentFilename,
			                           String mediaParentName, String mediatTable) {
		boolean result = false;
		openConnection();
		if ( connected() )
			try {
				String sql = "SELECT Name FROM " + mediatTable 
						   + " WHERE Filepath= ? AND Container= ? AND ParentName= ?";
				PreparedStatement stmt = c.prepareStatement( sql);
				stmt.setString( 1, mediaFilepath);
				stmt.setString( 2, mediaDocumentFilename);
				stmt.setString( 3, mediaParentName);
				result = !stmt.executeQuery().isAfterLast();
				stmt.close();
				c.close();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		return result;
	}

	// 12335: 2015-10-05 - Correct document tracks for which IsActivity = -1
	// 12335: 2016-08-26 - Eliminate tracks with IsActivity = -1 not in the document
	public void correctTracksWithInterimIsActivityValue( GPSDocument document) {
		List<String> names = getActivityAndOrCourseNames(document.getFileName(),-1);
		if ( !names.isEmpty() ) {
			for( Activity activity: document.getActivities() )
				if ( names.contains(activity.getName()) )
					updateDB(activity, document);
				
			for( Course course: document.getCourses())
				if ( names.contains(course.getName()))
					updateDB(course, document);
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
	
	// 12335: 2017-03-16
	public short getDifficultyLevel( Activity activity) {
		return getShortField( "TrackDifficulty", activity.getFilepath(), activity.getName(), true);
	}
	
	// 12335: 2017-03-16
	public short getDifficultyLevel( Course course) {
		return getShortField( "TrackDifficulty", course.getFilepath(), course.getName(), false);
	}
	
	// 12335: 2017-03-16
	public short getTrackCondition( Activity activity) {
		return getShortField( "TrackState", activity.getFilepath(), activity.getName(), true);
	}
	
	// 12335: 2017-03-16
	public short getTrackCondition( Course course) {
		return getShortField( "TrackState", course.getFilepath(), course.getName(), false);
	}
	
	// 12335: 2017-03-16
	public short getCircuitType( Activity activity) {
		return getShortField( "CircularPath", activity.getFilepath(), activity.getName(), true);
	}
	
	// 12335: 2017-03-16
	public short getCircuitType( Course course) {
		return getShortField( "CircularPath", course.getFilepath(), course.getName(), false);
	}
	
///	public void updateDB(Activity activity, File file) {	//12335: 2016-08-26
	public void updateDB( Activity activity, GPSDocument parent) {
		//12335: 2016-08-19
		Session session = activity.getFirstSession();
		if ( session == null ) 
			return;
		updateDBTrack( activity.getName(), parent, true, session.getSport(), session.getSubSport(),
				session.getStartTime(), session.getEndTime(),
				session.getTimerTime(), session.getMovingTime(),
				session.getDistance(), session.getTotalAscent(), session.getTotalDescent(),
				session.getMaximumAltitude(), session.getMinimumAltitude(),
				activity.getBounds(),
				activity.getTrackCondition(), activity.getDifficulty(), activity.getCircuitType());
		if ( activity.getStatus().mediaWasChanged() )
			updateTrackMedia( activity);
//		Connection c = null;
//		PreparedStatement stmt = null;
//		BoundingBox2<Location> bb = activity.getBounds();
//		try {
//			Class.forName("org.sqlite.JDBC");
//			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
//			c.setAutoCommit(false);
//			String sql = "INSERT OR REPLACE INTO GPSFiles (Filepath, Name, Sport, SubSport, MinLongitude, "
//					+ "MaxLongitude, MinLatitude, MaxLatitude) VALUES (?,?,?,?,?,?,?,?)";
//			stmt = c.prepareStatement(sql);
//			stmt.setString(1, file.getAbsolutePath());
//			stmt.setString(2, activity.getName());
//			stmt.setShort(3, activity.getSport().getSportID());
//			stmt.setShort(4, activity.getSubSport().getSubSportID());
//			stmt.setDouble(5, bb.getTopLeft().getLongitude());
//			stmt.setDouble(6, bb.getTopRight().getLongitude());
//			stmt.setDouble(7, bb.getBottomLeft().getLatitude());
//			stmt.setDouble(8, bb.getTopLeft().getLatitude());
//			stmt.executeUpdate();
//			stmt.close();
//			c.commit();
//			c.close();
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("updateDB activity");
//			return;
//		}
	}
	
	// 12335: 2016-08-26
	// Formerly: public void updateDB(Course course, File file) {
	public void updateDB( Course course, GPSDocument parent) {
		//12335: 2016-08-19
		if ( course.getTrackpoints().size() != 0 ) {
			updateDBTrack( course.getName(), parent, false, course.getSport(), course.getSubSport(),
					course.getStartTime(), course.getEndTime(),
					course.getTimerTime(), course.getMovingTime(),
					course.getDistance(), course.getTotalAscent(), course.getTotalDescent(),
					course.getMaximumAltitude(), course.getMinimumAltitude(),
					course.getBounds(),
					course.getTrackCondition(), course.getDifficulty(), course.getCircuitType());
			if ( course.getStatus().mediaWasChanged() )
				updateTrackMedia( course);
		}
		else {
			Location equator = new Location(0., 0.);
			BoundingBox2<Location> loc = new BoundingBox2<>(equator, equator, equator, equator);
			updateDBTrack( course.getName(), parent, false, SportType.GENERIC, SubSportType.GENERIC_SUB,
					new Date(), new Date(), 0., 0.,
					0., 0, 0, 0., 0.,
					loc,
					TrackConditionType.UNDEFINED, DifficultyLevelType.UNDEFINED,
					CircuitType.OPEN);
		}
	}
	
	//12355: 2016-10-15
	// Updates or inserts track data into GPSFiles
	// Does not handle dependent media
	private void updateDBTrack( String trackName, GPSDocument parent, boolean isActivity,
								SportType sport, SubSportType subSport,
								Date startTime,  Date endTime, double totalTime, double movingTime,
								double distance, int totalAscent, int totalDescent,
								double maxAltitude, double minAltitude,
								BoundingBox2<Location> box,
								TrackConditionType condition, DifficultyLevelType difficulty, 
								CircuitType circuit) {
		try {
			// Are we inserting or updating
			String  path   = parent.getFileName();
			boolean exists = false;
			openConnection();
			String sql = "SELECT IsActivity FROM GPSFiles WHERE Filepath='" + path
					   + "' AND Name ='" + trackName + "'";
			res = stmt.executeQuery( sql);
			while ( res.next() ) {
				int id = res.getInt( 1);
				if ( id == (isActivity? 1 : 0) || id == -1 ) {
					exists = true;
					break;
				}
			}
			
			// change SQL query according to whether the tracks exists or not
			if ( exists )
				sql = "UPDATE GPSFiles SET "
				        + "IsActivity=?, Sport=?, SubSport=?, "
						+ "StartTime=?, EndTime=?, TotalTime=?, MovingTime=?,"
						+ "Distance=?, Ascent=?, Descent=?, "
						+ "MaxAltitude=?, MinAltitude=?,"
						+ "MinLongitude=?, MaxLongitude=?, MinLatitude=?, MaxLatitude=?, "
						+ "TrackState=?, TrackDifficulty=?, CircularPath=? "
						+ "WHERE Filepath=? AND Name=?";
			else
				sql = "INSERT OR REPLACE INTO GPSFiles " 
						+ "(IsActivity, Sport, SubSport, "
						+ "StartTime, EndTime, TotalTime, MovingTime, Distance, Ascent, Descent, "
						+ "MaxAltitude, MinAltitude, "
						+ "MinLongitude, MaxLongitude, MinLatitude, MaxLatitude, "
						+ "TrackState, TrackDifficulty, CircularPath, "
						+ "Filepath, Name) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement stmt = c.prepareStatement(sql);
			stmt.setShort( 1, (short) (isActivity? 1: 0));
			stmt.setInt(   2, sport.getSportID());					// 12335: 2016-06-12
			stmt.setInt(   3, subSport.getSubSportID());			// 12335: 2016-06-12
			stmt.setDate(  4, dateToSQL(startTime));
			stmt.setDate(  5, dateToSQL(endTime));
			stmt.setDouble(6, totalTime);
			stmt.setDouble(7, movingTime);
			stmt.setDouble(8, distance);
			stmt.setDouble(9, totalAscent);
			stmt.setDouble(10, totalDescent);
			stmt.setDouble(11, maxAltitude);
			stmt.setDouble(12, minAltitude);
			stmt.setDouble(13, box.getTopLeft().getLongitude());
			stmt.setDouble(14, box.getTopRight().getLongitude());
			stmt.setDouble(15, box.getBottomLeft().getLatitude());
			stmt.setDouble(16, box.getTopLeft().getLatitude());
			stmt.setShort( 17, (short) condition.getValue());
			stmt.setShort( 18, (short) difficulty.getValue());
//			stmt.setShort( 19, (short) (circuit.getValue()? 1: 0));		//12335: 2017-08-09
			stmt.setShort( 19, circuit.getValue());
			stmt.setString(20, path);
			stmt.setString(21, trackName);
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
		}finally {
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
	
	//12335: 2016-08-26
	private void deleteTrackFromDB( String trackName, String parentFilePath, TrackType trackType) {
		String sql = "DELETE FROM Picture WHERE Container = '" + parentFilePath
				   + "' AND ParentName='" + trackName + "'";
		executeUpdateRequest( sql);
		sql = "DELETE FROM GPSFiles WHERE Filepath ='" + parentFilePath
		    + "' AND Name ='" + trackName + "'";
		executeUpdateRequest( sql);
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
	
	//##############################    MEDIA - Start    #####################################
	
	private void updateTrackMedia( Course course) {
		TrackStatus status      = course.getStatus();
		String documentFilepath = course.getParent().getFileName();
		String name             = course.getName();
		// Pictures
		if ( status.picturesWereChanged() ) {
			updateTrackPictures( course.getPictures(), documentFilepath, name);
		}
		// Audio files
		if ( status.audioWasChanged() ) {
		}
		// Movie files
		if ( status.moviesWereChanged() ) {
		}
		// Notes
		if ( status.notesWereChanged() ) {
		}
	}
	
	private void updateTrackMedia( Activity activity) {
		TrackStatus status      = activity.getStatus();
		String documentFilepath = activity.getParent().getFileName();
		String name             = activity.getName();
		// Pictures
		if ( status.picturesWereChanged() ) {
			updateTrackPictures( activity.getPictures(), documentFilepath, name);
		}
		// Audio files
		if ( status.audioWasChanged() ) {
		}
		// Movie files
		if ( status.moviesWereChanged() ) {
		}
		// Notes
		if ( status.notesWereChanged() ) {
		}
	}

	//##############################     MEDIA - End     #####################################
	
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
	
	// Get all pictures associated with a document (container)
	public HashMap<String, ArrayList<String>> getPicturesFromDB( String container) {
		return getMediaInDB( container, "Picture");
	}
	
	// Get all audio files associated with a document (container)
	public HashMap<String, ArrayList<String>> getAudioFilesFromDB( String container) {
		return getMediaInDB( container, "Audio");
	}
	
	// Get all video files associated with a document (container)
	public HashMap<String, ArrayList<String>> getVideoFilesFromDB( String container) {
		return getMediaInDB( container, "Video");
	}
	
	// Get all notes associated with a document (container)
	public HashMap<String, ArrayList<String>> getNotesFromDB( String container) {
		return getMediaInDB( container, "Notes");
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
		openConnection();
		if ( connected() )
			try{
				String sql = "INSERT OR REPLACE INTO Picture (Filepath, Name, Longitude, "
						+ "Latitude, Altitude, Container, ParentName) VALUES (?,?,?,?,?,?,?)";
				PreparedStatement pstmt = null;
				pstmt = c.prepareStatement(sql);
				pstmt.setString(1, pic.getFilePath());
				pstmt.setString(2, pic.getName());
				pstmt.setDouble(3, pic.getLongitude());
				pstmt.setDouble(4, pic.getLatitude());
				pstmt.setDouble(5, pic.getAltitude());			
				pstmt.setString(6, parentFilepath);
				pstmt.setString(7, pic.getContainer().getName());
				System.out.println( sql);
				pstmt.executeUpdate();
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
	
	// 12335: 2016-10-13 - Get the filenames of all media in the DB
	//        			   associated with a given document (container)
	//                     for a given media type (mediaTable)
	public HashMap<String, ArrayList<String>> getMediaInDB( String container, String mediaTable) {
		HashMap<String, ArrayList<String>> mediaInDB = new HashMap<>();
		openConnection();
		if ( connected() )
			try {
				String sql = "SELECT ParentName, Filepath FROM " + mediaTable +
						     " WHERE Container='" + container + "'";
				res = stmt.executeQuery(sql);
				String parentName;
				ArrayList<String> array;
				while( res.next()) {
					parentName = res.getString(1);
					array = mediaInDB.get(parentName);
					if ( array == null ) {
						array = new ArrayList<>();
						mediaInDB.put(parentName, array);
					}
					array.add(res.getString(2));
				}
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
		return mediaInDB;
	}
	
	// 12335: 2016-10-10
	public void updateMedia( String mediaName, String mediaFilepath, 
							 String mediaDocumentFilename, String mediaParentName,
							 double latitude,              double longitude,
							 double altitude,              Date mediaDate,
							 String mediaTable) {
		openConnection();
		if ( connected() )
			try{
				PreparedStatement pstmt = null;
				String sql;
				if ( doesMediaExistInDB( mediaFilepath, mediaDocumentFilename, mediaParentName, mediaTable) )
						sql = "UPDATE " + mediaTable + " SET Longitude=? Latitude=? Altitude=? "
						    + "Timestamp=? "
							+ "WHERE Name=? AND Filepath=? AND Container=? AND ParentName=?";
					else
						sql = "INSERT INTO " + mediaTable
							+ "(Longitude, Latitude, Altitude, Timestamp, Name, Filepath, Container, ParentName) "
							+ "VALUES (?,?,?,?,?,?,?,?)";
				pstmt = c.prepareStatement(sql);
				pstmt.setDouble( 1, longitude);
				pstmt.setDouble( 2, latitude);
				pstmt.setDouble( 3, altitude);
				pstmt.setLong(   4, mediaDate.getTime());
				pstmt.setString( 5, mediaName);
				pstmt.setString( 6, mediaFilepath);
				pstmt.setString( 7, mediaDocumentFilename);
				pstmt.setString( 8, mediaParentName);
				System.out.println( sql);
				pstmt.executeUpdate();
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace();
			} finally {
				closeConnection();
			}
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

	
//##############################    SPORTS AND SUBSPORTS  ################################
	
	// 12335: 2018-07-16
	protected void loadAllSubSportsCurrentValues( boolean defaultValues) {
		openConnection();
		if ( connected() )
			try {
				String current = "";
				if( !defaultValues )
					current = "Current";
				int sportID, subSportID;
				for( SubSportType subSport: SubSportType.values() ) {
					sportID    = subSport.getSportID();
					subSportID = subSport.getSubSportID();
					PreparedStatement stmt;
					ResultSet rs;
					String sql = "SELECT " + 
							current + "DefaultAverageSpeed, " +
							current + "MaximumAllowedSpeed, " +
							current + "PauseThresholdSpeed, " +
							current + "DefaultPauseDuration, " +
							current + "FollowRoads, " +
							current + "JoinMaximumWarningDistance, " +
							current + "JoinMergeDistanceTolerance, " +
							current + "JoinMergeTimeTolerance, " +
							current + "GradeLimit, " +
							"AscentDescentClass "  +
						   "FROM SubSport WHERE SportID='"+ sportID+"' AND SubSportID='"+subSportID+"'";
					stmt = c.prepareStatement(sql);
					rs = stmt.executeQuery();
					if ( rs.next() ) 
						subSport.setValues( rs.getDouble(1), rs.getDouble(2),  rs.getDouble(3), 
									        rs.getLong(4),   rs.getBoolean(5), rs.getDouble(6), 
									        rs.getDouble(7), rs.getLong(8),    rs.getDouble(9),
									        rs.getShort(10));
					stmt.close();
				}
			} catch (Exception e) {
				logger.error( "Could not load subsports current values");
				logger.error(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				closeConnection();
			}
	}
	
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
			if ( rs.next() )   														//12335: 2017-04-08
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
	
	//12335: 2017-06-07
	public short getAscentDescentClass( SportType sport, SubSportType subSport) {
		openConnection();
		short sportID    		 = sport.getSportID();
		short subSportID 		 = subSport.getSubSportID();
		short ascentdescentClass = AscentDescentType.UNDEFINED.getValue();
		if ( connected() ) 
			try {
				String sql = "SELECT AscentDescentClass FROM SubSport WHERE SportID='" +
							  sportID + "' AND SubSportID='" + subSportID + "'";
				res = stmt.executeQuery(sql);
				if ( res.next() )
					ascentdescentClass = res.getShort( 1);
			} catch (Exception e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage());
				logger.error("getAscentDescentClass");
			} finally {
				closeConnection();
			}
		return ascentdescentClass;
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
													// 12335: 2017-03-16: uses getShortField
//		return getSportOrSubSport( true, activity.getFilepath(), activity.getName(), true);
		return getShortField( "Sport", activity.getFilepath(), activity.getName(), true);
	}
	
	public short getSport(Course course) {  		// 12335: 2016-06-08: uses getSportOrSportType
													// 12335: 2017-03-16: uses getShortField
//		return getSportOrSubSport( true, course.getFilepath(), course.getName(), false);
		return getShortField( "Sport", course.getFilepath(), course.getName(), false);
	}
	
	public short getSubSport(Activity activity) {	// 12335: 2017-03-16: uses getShortField
//		return getSportOrSubSport( false, activity.getFilepath(), activity.getName(), true);
		return getShortField( "SubSport", activity.getFilepath(), activity.getName(), true);
	}
	
	public short getSubSport(Course course) {		// 12335: 2017-03-16: uses getShortField
//		return getSportOrSubSport( false, course.getFilepath(), course.getName(), false);
		return getShortField( "SubSport", course.getFilepath(), course.getName(), false);
	}
	
//	// 12335: 2016-06-13
//	public List<String> getSportsOrdered() {
//		List<String> list = getSports();
//		Collections.sort( list);
//		return list;
//	}
	
//	public ArrayList<String> getSports() {
//		openConnection();
//		ArrayList<String> nameList = new ArrayList<String>();
//		try {
//			String sql = "Select Name FROM Sport";
//			PreparedStatement stmt = c.prepareStatement(sql);
//			ResultSet rs = stmt.executeQuery();
//			while (rs.next()) {
//				String sportName = new String(rs.getString("Name"));
//				if (!nameList.contains(sportName))
//					nameList.add(sportName);
//			}
//			stmt.close();
//			c.close();
//
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("getSports");
//		}
//		finally{
//			closeConnection();
//		}
//		return nameList;
//	}

//	// 12335: 2016-06-13
//	public List<String> getSubSportsOrdered( SportType sport) {
//		List<String> list = getSubSports( sport);
//		Collections.sort( list);
//		return list;
//	}
	
//	public ArrayList<String> getSubSports(SportType sport) {
//		openConnection();
//		ArrayList<String> nameList = new ArrayList<String>();
//		try {
//			String sql = "Select SubSportName FROM SubSport WHERE SportID='"+sport.getSportID()+"'";
//			PreparedStatement stmt = c.prepareStatement(sql);
//			ResultSet rs = stmt.executeQuery();
//			while (rs.next()) {
//				String subSportName = new String(rs.getString("SubSportName"));
//				if (!nameList.contains(subSportName))
//					nameList.add(subSportName);
//			}
//			stmt.close();
//			c.close();
//
//		} catch (Exception e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage());
//			logger.error("getSubSports");
//		}
//		finally{
//			closeConnection();
//		}
//		return nameList;
//	}
	
	//12335: 2017-04-20
	public List<Short> getSportsIds() {
		openConnection();
		ArrayList<Short> idList = new ArrayList<>();
		try {
			String sql = "Select SportID FROM Sport";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				short id = rs.getShort("SportID");
				if (!idList.contains(id))
					idList.add(id);
			}
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			logger.error("getSportsIds");
		}
		finally{
			closeConnection();
		}
		return idList;
	}
	
	//12335: 2017-04-20
	public List<Short> getSubSportsIds( SportType sport) {
		openConnection();
		ArrayList<Short> idList = new ArrayList<>();
		try {
			String sql = "Select SubSportID FROM SubSport WHERE SportID='"+sport.getSportID()+"'";
			PreparedStatement stmt = c.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				short id = rs.getShort("SubSportID");
				if (!idList.contains(id))
					idList.add(id);
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
		return idList;
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
	
//#######################    SPORTS AND SUBSPORTS - end  ################################
		
//###########################    HELPER FUNCTIONS   #####################################
	
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