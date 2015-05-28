package com.henriquemalheiro.trackit.business.operation;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileFilter;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.FileType;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.CoursePoint;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.Event;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.Lap;
import com.henriquemalheiro.trackit.business.domain.Track;
import com.henriquemalheiro.trackit.business.domain.TrackSegment;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.exception.ReaderException;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.reader.Reader;
import com.henriquemalheiro.trackit.business.reader.ReaderFactory;
import com.henriquemalheiro.trackit.business.utility.decoratedlist.EventList;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterFactory;
import com.henriquemalheiro.trackit.presentation.FileFilterFactory;
import com.pg58406.trackit.business.domain.Pause;
import com.pg58406.trackit.business.domain.Picture;

import org.apache.commons.io.FilenameUtils;

public class CopyOperation extends OperationBase implements Operation {

	private Course course;

	public CopyOperation() {
		super();
		setUp();
	}

	public CopyOperation(Map<String, Object> options) {
		this();
		this.options.putAll(options);
		setUp();
	}

	private void setUp() {
		course = (Course) options.get(Constants.CopyOperation.COURSE);
	}

	@Override
	public String getName() {
		return Constants.CopyOperation.NAME;
	}

	@Override
	public void process(GPSDocument document) throws TrackItException {
		// Course course = document.getCourses().get(0);
		copyOut(document);
		// document.add(newCourse);
		document.setChangedTrue();
	}
	
	private void mergeDocuments(GPSDocument source, GPSDocument destination) {
		for (Activity activity : source.getActivities()) {
			activity.setParent(destination);
		}
		destination.addActivities(source.getActivities());

		for (Course course : source.getCourses()) {
			course.setParent(destination);
		}
		destination.addCourses(source.getCourses());

		for (Waypoint waypoint : source.getWaypoints()) {
			waypoint.setParent(destination);
		}
		destination.addWaypoints(source.getWaypoints());

	}
	
	private InputStream getInputStream(final File file) throws ReaderException {
		ProgressMonitorInputStream progressMonitor;
		BufferedInputStream in;
		try {
			progressMonitor = new ProgressMonitorInputStream(
					TrackIt.getApplicationFrame(), "Reading "
							+ file.getAbsolutePath(), new FileInputStream(file));
			in = new BufferedInputStream(progressMonitor);
		} catch (FileNotFoundException fnfe) {
			throw new ReaderException(fnfe.getMessage());
		}
		return in;
	}

	private void closeInputStream(InputStream inputStream) {
		try {
			inputStream.close();
		} catch (IOException e) {
			logger.debug("Failed to close input stream.");
		}
	}

	private void copyOut(GPSDocument document) {

		String filename = course.getFilepath();
		String newName = FilenameUtils.removeExtension(filename);
		String extension = FilenameUtils.getExtension(filename);
		String suffix = "_copy";
		String newFilename = newName + suffix + "." + extension;

		File tempFile = new File(newFilename);
		document.setFileName(newFilename);

		String oldCourseName = course.getName();
		String newCourseName = course.getName() + suffix;
		course.setName(newCourseName);
		// boolean exists = file.exists();

		// if (!exists){
		try {
			Map<String, Object> options = new HashMap<>();
			options.put(Constants.Writer.OUTPUT_DIR, tempFile.getParentFile()
					.getAbsolutePath());
			Writer writer = WriterFactory.getInstance()
					.getWriter(tempFile, options);
			writer.write(document);
			//TimeUnit.SECONDS.sleep(2);
		} catch (TrackItException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(
					TrackIt.getApplicationFrame(),
					getMessage("operation.failure.fileExport",
							course.getDocumentItemName()),
					getMessage("operation.failure"), JOptionPane.ERROR_MESSAGE);
		} //catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
		// }
		/*
		 * else { logger.error("The file " + file.getAbsolutePath() +
		 * " already exists."); }
		 */
		// course.setUnsavedFalse();
		// course.setName(course.getName().substring(0,
		// course.getName().length()-5));
		//try {
			course.setName(oldCourseName);
			//File[] files = new File[1];
			//files[0] = tempFile;
			DocumentManager documentManager = DocumentManager.getInstance();
			//documentManager.importDocuments(files);
			GPSDocument readDocument = null;
			
			InputStream inputStream = null;
			Reader reader;
					try {
						inputStream = getInputStream(tempFile);
						reader = ReaderFactory.getInstance().getReader(
								tempFile, null);
						readDocument = reader.read(inputStream,
								tempFile.getAbsolutePath());// 58406
						mergeDocuments(readDocument, document);
					} catch (ReaderException re) {
						
					} finally {
						closeInputStream(inputStream);
					}
		
				 

				/*for (Course c : document.getCourses()) {
					documentManager.getDatabase().updateDB(c, tempFile);
					c.setFilepath(tempFile.getAbsolutePath());						
					if(c.getTrackpoints().get(0).getSpeed() != null)
						c.setNoSpeedInFile(false);
					c.setUnsavedFalse();
				}*/
			
			
			
			
			
			//TimeUnit.SECONDS.sleep(2);
			tempFile.delete();
		//} //catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}

		/*
		 * Course newCourse = new Course(); newCourse.setParent(document);
		 * String newCourseName = course.getName().concat(" copy");
		 * newCourse.setName(newCourseName);
		 * 
		 * 
		 * newCourse.setSport(course.getSport());
		 * newCourse.setSubSport(course.getSubSport());
		 * newCourse.setNotes(course.getNotes());
		 * 
		 * newCourse.setElapsedTime((course.getElapsedTime() == null) ? null :
		 * new Double(course.getElapsedTime()));
		 * newCourse.setTimerTime((course.getTimerTime() == null) ? null : new
		 * Double(course.getTimerTime()));
		 * newCourse.setMovingTime((course.getMovingTime() == null) ? null : new
		 * Double(course.getMovingTime()));
		 * newCourse.setPausedTime((course.getPausedTime() == null) ? null : new
		 * Double(course.getPausedTime()));
		 * newCourse.setDistance((course.getPausedTime() == null) ? null : new
		 * Double(course.getDistance()));
		 * newCourse.setAverageSpeed((course.getPausedTime() == null) ? null :
		 * new Double(course.getAverageSpeed()));
		 * newCourse.setAverageMovingSpeed((course.getPausedTime() == null) ?
		 * null : new Double(course.getAverageMovingSpeed()));
		 * newCourse.setMaximumSpeed((course.getPausedTime() == null) ? null :
		 * new Double(course.getMaximumSpeed()));
		 * newCourse.setAverageHeartRate((course.getAverageHeartRate() == null)
		 * ? null : new Short(course.getAverageHeartRate()));
		 * newCourse.setMinimumHeartRate((course.getMinimumHeartRate() == null)
		 * ? null : new Short(course.getMinimumHeartRate()));
		 * newCourse.setMaximumHeartRate((course.getMaximumHeartRate() == null)
		 * ? null : new Short(course.getMaximumHeartRate()));
		 * newCourse.setAverageCadence((course.getAverageCadence() == null) ?
		 * null : new Short(course.getAverageCadence()));
		 * newCourse.setMaximumCadence((course.getMaximumCadence() == null) ?
		 * null : new Short(course.getMaximumCadence()));
		 * newCourse.setAverageRunningCadence((course.getAverageRunningCadence()
		 * == null) ? null : new Short(course.getAverageRunningCadence()));
		 * newCourse.setMaximumRunningCadence((course.getMaximumRunningCadence()
		 * == null) ? null : new Short(course.getMaximumRunningCadence()));
		 * newCourse.setAveragePower((course.getAveragePower() == null) ? null :
		 * new Integer(course.getAveragePower()));
		 * newCourse.setMaximumPower((course.getMaximumPower() == null) ? null :
		 * new Integer(course.getMaximumPower()));
		 * newCourse.setCalories((course.getCalories() == null) ? null : new
		 * Integer(course.getCalories()));
		 * newCourse.setFatCalories((course.getFatCalories() == null) ? null :
		 * new Integer(course.getFatCalories()));
		 * newCourse.setAverageTemperature((course.getAverageTemperature() ==
		 * null) ? null : new Byte(course.getAverageTemperature()));
		 * newCourse.setMinimumTemperature((course.getMinimumTemperature() ==
		 * null) ? null : new Byte(course.getMinimumTemperature()));
		 * newCourse.setMaximumTemperature((course.getMaximumTemperature() ==
		 * null) ? null : new Byte(course.getMaximumTemperature()));
		 * newCourse.setTotalAscent((course.getTotalAscent() == null) ? null :
		 * new Integer(course.getTotalAscent()));
		 * newCourse.setTotalDescent((course.getTotalDescent() == null) ? null :
		 * new Integer(course.getTotalDescent()));
		 * newCourse.setAverageAltitude((course.getAverageAltitude() == null) ?
		 * null : new Float(course.getAverageAltitude()));
		 * newCourse.setMinimumAltitude((course.getMinimumAltitude() == null) ?
		 * null : new Float(course.getMinimumAltitude()));
		 * newCourse.setMaximumAltitude((course.getMaximumAltitude() == null) ?
		 * null : new Float(course.getMaximumAltitude()));
		 * newCourse.setAverageGrade((course.getAverageGrade() == null) ? null :
		 * new Float(course.getAverageGrade()));
		 * newCourse.setAveragePositiveGrade((course.getAveragePositiveGrade()
		 * == null) ? null : new Float(course.getAveragePositiveGrade()));
		 * newCourse.setAverageNegativeGrade((course.getAverageNegativeGrade()
		 * == null) ? null : new Float(course.getAverageNegativeGrade()));
		 * newCourse.setMaximumPositiveGrade((course.getMaximumPositiveGrade()
		 * == null) ? null : new Float(course.getMaximumPositiveGrade()));
		 * newCourse.setMaximumNegativeGrade((course.getMaximumNegativeGrade()
		 * == null) ? null : new Float(course.getMaximumNegativeGrade()));
		 * newCourse
		 * .setAveragePositiveVerticalSpeed((course.getAveragePositiveVerticalSpeed
		 * () == null) ? null : new
		 * Float(course.getAveragePositiveVerticalSpeed()));
		 * newCourse.setAverageNegativeVerticalSpeed
		 * ((course.getAverageNegativeVerticalSpeed() == null) ? null : new
		 * Float(course.getAverageNegativeVerticalSpeed()));
		 * newCourse.setMaximumPositiveVerticalSpeed
		 * ((course.getMaximumPositiveVerticalSpeed() == null) ? null : new
		 * Float(course.getMaximumPositiveVerticalSpeed()));
		 * newCourse.setMaximumNegativeVerticalSpeed
		 * ((course.getMaximumNegativeVerticalSpeed() == null) ? null : new
		 * Float(course.getMaximumNegativeVerticalSpeed()));
		 * newCourse.setStartLatitude((course.getStartLatitude() == null) ? null
		 * : new Double(course.getStartLatitude()));
		 * newCourse.setStartLongitude((course.getStartLongitude() == null) ?
		 * null : new Double(course.getStartLongitude()));
		 * newCourse.setStartAltitude((course.getStartAltitude() == null) ? null
		 * : new Double(course.getStartAltitude()));
		 * newCourse.setEndLatitude((course.getEndLatitude() == null) ? null :
		 * new Double(course.getEndLatitude()));
		 * newCourse.setEndLongitude((course.getEndLongitude() == null) ? null :
		 * new Double(course.getEndLongitude()));
		 * newCourse.setEndAltitude((course.getEndAltitude() == null) ? null :
		 * new Double(course.getEndAltitude()));
		 * newCourse.setNortheastLatitude((course.getNortheastLatitude() ==
		 * null) ? null : new Double(course.getNortheastLatitude()));
		 * newCourse.setNortheastLongitude((course.getNortheastLongitude() ==
		 * null) ? null : new Double(course.getNortheastLongitude()));
		 * newCourse.setSouthwestLatitude((course.getSouthwestLatitude() ==
		 * null) ? null : new Double(course.getSouthwestLatitude()));
		 * newCourse.setSouthwestLongitude((course.getSouthwestLongitude() ==
		 * null) ? null : new Double(course.getSouthwestLongitude()));
		 * 
		 * 
		 * newCourse.setUnsavedChanges(course.getUnsavedChanges());
		 * newCourse.setFilepath((course.getFilepath() == null) ? null : new
		 * String(course.getFilepath()));
		 * newCourse.setCreator((course.getCreator() == null) ? null : new
		 * String(course.getCreator()));
		 * newCourse.setColorSchemeV2(course.getColorSchemeV2());
		 * newCourse.setNoSpeedInFile(course.getNoSpeedInFile());
		 */

		// newCourse.setParent(course.getParent());
		/*
		 * newCourse.setUnsavedChanges(new Boolean(course.getUnsavedChanges()));
		 * newCourse.setFilepath(new String(course.getFilepath()));
		 * newCourse.setCreator(new String(course.getCreator()));
		 * newCourse.setColorSchemeV2(course.getColorSchemeV2());
		 * newCourse.setNoSpeedInFile(new Boolean (course.getNoSpeedInFile()));
		 */

		/*
		 * newCourse.setSport(course.getSport());
		 * newCourse.setSubSport(course.getSubSport());
		 * newCourse.setNotes(course.getNotes());
		 * newCourse.setElapsedTime(course.getElapsedTime());
		 * newCourse.setTimerTime(course.getTimerTime());
		 * newCourse.setMovingTime(course.getMovingTime());
		 * newCourse.setPausedTime(course.getPausedTime());
		 * newCourse.setDistance(course.getDistance());
		 * newCourse.setAverageSpeed(course.getAverageSpeed());
		 * newCourse.setAverageMovingSpeed(course.getAverageMovingSpeed());
		 * newCourse.setMaximumSpeed(course.getMaximumSpeed());
		 * newCourse.setAverageHeartRate(course.getAverageHeartRate());
		 * newCourse.setMinimumHeartRate(course.getMinimumHeartRate());
		 * newCourse.setMaximumHeartRate(course.getMaximumHeartRate());
		 * newCourse.setAverageCadence(course.getAverageCadence());
		 * newCourse.setMaximumCadence(course.getMaximumCadence());
		 * newCourse.setAverageRunningCadence
		 * (course.getAverageRunningCadence());
		 * newCourse.setMaximumRunningCadence
		 * (course.getMaximumRunningCadence());
		 * newCourse.setAveragePower(course.getAveragePower());
		 * newCourse.setMaximumPower(course.getMaximumPower());
		 * newCourse.setCalories(course.getCalories());
		 * newCourse.setFatCalories(course.getFatCalories());
		 * newCourse.setAverageTemperature(course.getAverageTemperature());
		 * newCourse.setMinimumTemperature(course.getMinimumTemperature());
		 * newCourse.setMaximumTemperature(course.getMaximumTemperature());
		 * newCourse.setTotalAscent(course.getTotalAscent());
		 * newCourse.setTotalDescent(course.getTotalDescent());
		 * newCourse.setAverageAltitude(course.getAverageAltitude());
		 * newCourse.setMinimumAltitude(course.getMinimumAltitude());
		 * newCourse.setMaximumAltitude(course.getMaximumAltitude());
		 * newCourse.setAverageGrade(course.getAverageGrade());
		 * newCourse.setAveragePositiveGrade(course.getAveragePositiveGrade());
		 * newCourse.setAverageNegativeGrade(course.getAverageNegativeGrade());
		 * newCourse.setMaximumPositiveGrade(course.getMaximumPositiveGrade());
		 * newCourse.setMaximumNegativeGrade(course.getMaximumNegativeGrade());
		 * newCourse
		 * .setAveragePositiveVerticalSpeed(course.getAveragePositiveVerticalSpeed
		 * ()); newCourse.setAverageNegativeVerticalSpeed(course.
		 * getAverageNegativeVerticalSpeed());
		 * newCourse.setMaximumPositiveVerticalSpeed
		 * (course.getMaximumPositiveVerticalSpeed());
		 * newCourse.setMaximumNegativeVerticalSpeed
		 * (course.getMaximumNegativeVerticalSpeed());
		 * newCourse.setStartLatitude(course.getStartLatitude());
		 * newCourse.setStartLongitude(course.getStartLongitude());
		 * newCourse.setStartAltitude(course.getStartAltitude());
		 * newCourse.setEndLatitude(course.getEndLatitude());
		 * newCourse.setEndLongitude(course.getEndLongitude());
		 * newCourse.setEndAltitude(course.getEndAltitude());
		 * newCourse.setNortheastLatitude(course.getNortheastLatitude());
		 * newCourse.setNortheastLongitude(course.getNortheastLongitude());
		 * newCourse.setSouthwestLatitude(course.getSouthwestLatitude());
		 * newCourse.setSouthwestLongitude(course.getSouthwestLongitude());
		 * //newCourse.setParent(course.getParent());
		 * newCourse.setUnsavedChanges(course.getUnsavedChanges());
		 * newCourse.setFilepath(course.getFilepath());
		 * newCourse.setCreator(course.getCreator());
		 * newCourse.setColorSchemeV2(course.getColorSchemeV2());
		 * newCourse.setNoSpeedInFile(course.getNoSpeedInFile());
		 */

		/*
		 * List<Lap> newLaps = new ArrayList<Lap>(); for (Lap lap :
		 * course.getLaps()){ newLaps.add(lap); } newCourse.setLaps(newLaps);
		 * 
		 * List<Track> newTracks = new ArrayList<Track>(); for (Track track :
		 * course.getTracks()){ newTracks.add(track); }
		 * newCourse.setTracks(newTracks);
		 * 
		 * EventList<Trackpoint> newTrackpoints = new EventList<Trackpoint>(new
		 * ArrayList<Trackpoint>()); for (Trackpoint trackpoint :
		 * course.getTrackpoints()){ newTrackpoints.add(trackpoint); }
		 * newCourse.setTrackpoints(newTrackpoints);
		 * 
		 * EventList<CoursePoint> newCoursePoints = new
		 * EventList<CoursePoint>(new ArrayList<CoursePoint>()); for
		 * (CoursePoint coursePoint : course.getCoursePoints()){
		 * newCoursePoints.add(coursePoint); }
		 * newCourse.setCoursePoints(newCoursePoints);
		 * 
		 * List<TrackSegment> newSegments = new ArrayList<TrackSegment>(); for
		 * (TrackSegment segment : course.getSegments()){
		 * newSegments.add(segment); } newCourse.setSegments(newSegments);
		 * 
		 * List<Event> newEvents = new ArrayList<Event>(); for (Event event :
		 * course.getEvents()){ newEvents.add(event); }
		 * newCourse.setEvents(newEvents);
		 * 
		 * List<Pause> newPauses = new ArrayList<Pause>(); for (Pause pause :
		 * course.getPauses()){ newPauses.add(pause); }
		 * newCourse.setPauses(newPauses);
		 * 
		 * List<Picture> newPictures = new ArrayList<Picture>(); for (Picture
		 * picture : course.getPictures()){ newPictures.add(picture); }
		 * newCourse.setPictures(newPictures);
		 * 
		 * return newCourse;
		 */

		// JFileChooser fileChooser = new JFileChooser();
		// fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// fileChooser.setAcceptAllFileFilterUsed(false);

		/*
		 * FileFilterFactory factory = FileFilterFactory.getInstance(); for
		 * (FileFilter f : factory.getFilters()) {
		 * if(!f.getDescription().startsWith("FIT")){ if
		 * (!f.getDescription().startsWith("KML")){
		 * fileChooser.addChoosableFileFilter(f); } } }
		 */

		// int result = fileChooser.showDialog(application, action);
		// if (result == JFileChooser.APPROVE_OPTION) {

	}

	@Override
	public void process(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void undoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void undoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(List<GPSDocument> document)
			throws TrackItException {
		// TODO Auto-generated method stub

	}

}
