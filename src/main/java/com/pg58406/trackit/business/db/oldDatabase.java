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

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.pg58406.trackit.business.domain.PhotoContainer;
import com.pg58406.trackit.business.domain.Picture;

public class oldDatabase {

	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	private static oldDatabase database;

	public oldDatabase() {
		initDb();
	}

	static {
		database = new oldDatabase();
	}

	public synchronized static oldDatabase getInstance() {
		return database;
	}

	public void initDb() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");

			stmt = c.createStatement();
			/*String sql = "CREATE TABLE IF NOT EXISTS Courses "
					+ "(Filepath TEXT PRIMARY KEY NOT NULL,"
					+ "Name TEXT NOT NULL," + "MinLongitude DOUBLE NOT NULL,"
					+ "MaxLongitude DOUBLE NOT NULL,"
					+ "MinLatitude DOUBLE NOT NULL,"
					+ "MaxLatitude DOUBLE NOT NULL)";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS Activities "
					+ "(Filepath TEXT PRIMARY KEY NOT NULL,"
					+ "Name TEXT NOT NULL," + "MinLongitude DOUBLE NOT NULL,"
					+ "MaxLongitude DOUBLE NOT NULL,"
					+ "MinLatitude DOUBLE NOT NULL,"
					+ "MaxLatitude DOUBLE NOT NULL)";
			stmt.executeUpdate(sql);*/
			String sql = "CREATE TABLE IF NOT EXISTS GPSFiles "
					+ "(Filepath TEXT NOT NULL,"
					+ "Name TEXT NOT NULL," + "MinLongitude DOUBLE NOT NULL,"
					+ "MaxLongitude DOUBLE NOT NULL,"
					+ "MinLatitude DOUBLE NOT NULL,"
					+ "MaxLatitude DOUBLE NOT NULL, "
					+ "PRIMARY KEY(Filepath, Name))";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS Picture "
					+ "(Filepath TEXT NOT NULL," + "Name TEXT,"
					+ "Longitude DOUBLE," + "Latitude DOUBLE,"
					+ "Altitude DOUBLE," + "Container TEXT NOT NULL, "
					+ "ParentName TEXT NOT NULL,"
					+ "PRIMARY KEY (Filepath, Container, ParentName)"
					+ "FOREIGN KEY (Container) REFERENCES GPSFiles(Filepath),"
					+ "FOREIGN KEY (ParentName) REFERENCES GPSFiles(Name))";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS Video "
					+ "(Filepath TEXT NOT NULL," + "Name TEXT,"
					+ "Longitude DOUBLE," + "Latitude DOUBLE,"
					+ "Altitude DOUBLE," + "Container TEXT NOT NULL, "
					+ "ParentName TEXT NOT NULL,"
					+ "PRIMARY KEY (Filepath, Container, ParentName)"
					+ "FOREIGN KEY (Container) REFERENCES GPSFiles(Filepath),"
					+ "FOREIGN KEY (ParentName) REFERENCES GPSFiles(Name))";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS Audio "
					+ "(Filepath TEXT NOT NULL," + "Name TEXT,"
					+ "Longitude DOUBLE," + "Latitude DOUBLE,"
					+ "Altitude DOUBLE," + "Container TEXT NOT NULL, "
					+ "ParentName TEXT NOT NULL,"
					+ "PRIMARY KEY (Filepath, Container, ParentName)"
					+ "FOREIGN KEY (Container) REFERENCES GPSFiles(Filepath),"
					+ "FOREIGN KEY (ParentName) REFERENCES GPSFiles(Name))";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS Sport "
					+ "(SportID SHORT NOT NULL,"
					+ "Name TEXT NOT NULL,"
					+ "PRIMARY KEY (SportID,Name))";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE IF NOT EXISTS SubSport "
					+ "(SubSportID SHORT NOT NULL,"
					+ "SportID SHORT NOT NULL,"
					+ "Name TEXT NOT NULL,"
					+ "DefaultAverageSpeed DOUBLE,"
					+ "MaximumAllowedSpeed DOUBLE,"
					+ "PauseThresholdSpeed DOUBLE,"
					+ "DefaultPauseDuration LONG,"
					+ "FollowRoads BOOLEAN,"
					+ "JoinMaximumWarningDistance DOUBLE,"
					+ "JoinMergeDistanceTolerance DOUBLE,"
					+ "JoinMergeTimeTolerance DOUBLE,"
					+ "PRIMARY KEY (SubSportID,Name)"
					+ "FOREIGN KEY (SportID) REFERENCES Sport(SportID))";
			stmt.executeUpdate(sql);
			
			DatabaseMetaData md = c.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			boolean skipAddSport = false;
			boolean skipAddSubSport = false;
			while (rs.next()) {
				if(rs.getString(3).equals("Sport")){
					skipAddSport = true;
					}
				else if(rs.getString(3).equals("SubSport")){
					skipAddSubSport = true;
					}
				}
			if(!skipAddSport){
				List<SportType> sportList = Arrays.asList(SportType.values()); 
				for(SportType sportType : sportList){
					addSport(sportType.getSportID(), sportType.name());
				}
			}
			if(!skipAddSubSport){
				List<SubSportType> subSportList = Arrays.asList(SubSportType.values()); 
				for(SubSportType subSportType : subSportList){
					addSubSport(subSportType);
				}
			}
			/*
			 * sql = "CREATE TABLE IF NOT EXISTS MockUpTable " +
			 * "(ID INTEGER PRIMARY KEY   AUTOINCREMENT)";
			 * stmt.executeUpdate(sql);
			 */
			stmt.close();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}
	
	public void addSport(short sportID, String name){
		Connection c = null;
		PreparedStatement statement = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "INSERT INTO Sport (SportID, Name) VALUES (?,?)";
			statement = c.prepareStatement(sql);
			statement.setShort(1, sportID);
			statement.setString(2, name);
			statement.executeUpdate();
			statement.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return;
		}
	}
	
	public void addSubSport(SubSportType subSport){
		Connection c = null;
		PreparedStatement statement = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "INSERT INTO SubSport (SubSportID, SportID, Name) VALUES (?,?,?)";
			statement = c.prepareStatement(sql);
			statement.setShort(1, subSport.getSubSportID());
			statement.setShort(2, (short) 1);
			statement.setString(3, subSport.name());
			statement.executeUpdate();
			statement.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
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
			String sql = "INSERT OR REPLACE INTO GPSFiles (Filepath, Name, MinLongitude, "
					+ "MaxLongitude, MinLatitude, MaxLatitude) VALUES (?,?,?,?,?,?)";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, file.getAbsolutePath());
			stmt.setString(2, course.getName());
			stmt.setDouble(3, bb.getTopLeft().getLongitude());
			stmt.setDouble(4, bb.getTopRight().getLongitude());
			stmt.setDouble(5, bb.getBottomLeft().getLatitude());
			stmt.setDouble(6, bb.getTopLeft().getLatitude());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return;
		}
	}

	public void updateDB(Activity activity, File file) {
		Connection c = null;
		PreparedStatement stmt = null;
		BoundingBox2<Location> bb = activity.getBounds();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "INSERT OR REPLACE INTO GPSFiles (Filepath, Name, MinLongitude, "
					+ "MaxLongitude, MinLatitude, MaxLatitude) VALUES (?,?,?,?,?,?)";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, file.getAbsolutePath());
			stmt.setString(2, activity.getName());
			stmt.setDouble(3, bb.getTopLeft().getLongitude());
			stmt.setDouble(4, bb.getTopRight().getLongitude());
			stmt.setDouble(5, bb.getBottomLeft().getLatitude());
			stmt.setDouble(6, bb.getTopLeft().getLatitude());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return;
		}
	}
	
	public void updatePicture(final Picture pic){
		Connection c = null;
		PreparedStatement stmt = null;
		try{
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "INSERT OR REPLACE INTO Picture (Filepath, Name, Longitude, "
					+ "Latitude, Altitude, Container, ParentName) VALUES (?,?,?,?,?,?,?)";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, pic.getFilePath());
			stmt.setString(2, pic.getName());
			stmt.setDouble(3, pic.getLongitude());
			stmt.setDouble(4, pic.getLatitude());
			stmt.setDouble(5, pic.getAltitude());			
			stmt.setString(6, pic.getContainerFilePath());
			stmt.setString(7, pic.getContainer().getName());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	public void removeAllPictures(Activity activity) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String s = "DELETE FROM Picture WHERE Container = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, activity.getFilepath());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return;
		}
	}
	
	public void removeAllPictures(Course course) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String s = "DELETE FROM Picture WHERE Container = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, course.getFilepath());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return;
		}
	}
	
	public void removePicture(Picture pic) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String s = "DELETE FROM Picture WHERE Filepath = ? AND Container = ? AND ParentName = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, pic.getFilePath());
			stmt.setString(2, pic.getContainerFilePath());
			stmt.setString(3, pic.getContainer().getName());
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return;
		}
	}
	
	public void updatePictures(PhotoContainer container){
		for(Picture pic : container.getPictures()){
			updatePicture(pic);
		}
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
			return;
		}
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
					.equalsIgnoreCase("query does not return results")))
				logger.error(e.getClass().getName() + ": "
						+ e.getMessage());
		}
		System.out.println("Database Update Successful.");
	}

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
			return nameList;
		}
		return nameList;
	}

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
		}

		return name;
	}

	public ArrayList<String> initFromDb() {
		Connection c = null;
		Statement stmt = null;
		ArrayList<String> filepathList = new ArrayList<String>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Filepath FROM GPSFiles");
			while (rs.next()) {
				String filepath = new String(rs.getString("Filepath"));
				if(!filepathList.contains(filepath))
				filepathList.add(filepath);				
			}
			rs.close();
			stmt.close();
			c.close();

		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
		}
		return filepathList;
	}
	
	
	public ArrayList<String> getPicturesFromDB(String filePath) {
		Connection c = null;
		PreparedStatement stmt = null;
		ArrayList<String> picturesList = new ArrayList<String>();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String sql = "SELECT Filepath, ParentName FROM Picture" + " WHERE Container = ?";
			stmt = c.prepareStatement(sql);
			stmt.setString(1, filePath);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String photoPath = new String(rs.getString("Filepath"));
				String parentName = new String(rs.getString("ParentName"));
				picturesList.add(photoPath+"?"+parentName);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
		return picturesList;
	}
	
	public void delete(String path) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:TrackIt.db");
			c.setAutoCommit(false);
			String s = "DELETE FROM GPSFiles WHERE Filepath = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, path);
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			s = "DELETE FROM Picture WHERE Container = ?";
			stmt = c.prepareStatement(s);
			stmt.setString(1, path);
			stmt.executeUpdate();
			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			return;
		}
	}
}
