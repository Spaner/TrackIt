package com.miguelpernas.trackit.business.operation;

import static com.henriquemalheiro.trackit.business.common.Messages.getMessage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.DocumentManager;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.domain.Activity;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.GPSDocument;
import com.henriquemalheiro.trackit.business.domain.SportType;
import com.henriquemalheiro.trackit.business.domain.SubSportType;
import com.henriquemalheiro.trackit.business.domain.Waypoint;
import com.henriquemalheiro.trackit.business.exception.ReaderException;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.business.operation.Operation;
import com.henriquemalheiro.trackit.business.operation.OperationBase;
import com.henriquemalheiro.trackit.business.reader.Reader;
import com.henriquemalheiro.trackit.business.reader.ReaderFactory;
import com.henriquemalheiro.trackit.business.writer.Writer;
import com.henriquemalheiro.trackit.business.writer.WriterFactory;

import org.apache.commons.io.FilenameUtils;

public class CopyOperation extends OperationBase implements Operation {

	private Course course;
	private SportType sport;
	private SubSportType subSport;

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
			progressMonitor = new ProgressMonitorInputStream(TrackIt.getApplicationFrame(),
					"Reading " + file.getAbsolutePath(), new FileInputStream(file));
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
		sport = course.getSport();
		subSport = course.getSubSport();
		course.setName(newCourseName);

		try {
			Map<String, Object> options = new HashMap<>();
			options.put(Constants.Writer.OUTPUT_DIR, tempFile.getParentFile().getAbsolutePath());
			Writer writer = WriterFactory.getInstance().getWriter(tempFile, options);
			writer.write(document);

		} catch (TrackItException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
					getMessage("operation.failure.fileExport", course.getDocumentItemName()),
					getMessage("operation.failure"), JOptionPane.ERROR_MESSAGE);
		}
		course.setName(oldCourseName);

		DocumentManager documentManager = DocumentManager.getInstance();

		GPSDocument readDocument = null;
		//GPSDocument newDoc = new GPSDocument(course.getName() + suffix);

		InputStream inputStream = null;
		Reader reader;
		try {
			inputStream = getInputStream(tempFile);
			reader = ReaderFactory.getInstance().getReader(tempFile, null);
			readDocument = reader.read(inputStream, tempFile.getAbsolutePath());// 58406
			mergeDocuments(readDocument, document);
		} catch (ReaderException re) {

		} finally {
			closeInputStream(inputStream);
		}
//		readDocument.getCourses().get(0).setSport(sport);						//12335: 2016-06-15
//		readDocument.getCourses().get(0).setSubSport(subSport);					//12335: 2016-06-15
		readDocument.getCourses().get(0).setSportAndSubSport( sport, subSport); //12335: 2016-06-15
		for (Course c : document.getCourses()) {
			 //documentManager.getDatabase().updateDB(c, tempFile);
			c.setFilepath(tempFile.getAbsolutePath());
			if (c.getTrackpoints().get(0).getSpeed() != null)
				c.setNoSpeedInFile(false);
//			c.setUnsavedTrue();					// 12335 : 2016-10-03
			c.setTrackStatusTo( true);
		}
		try {
			//newDoc.getCourses().get(0).setFilepath(filename);
			document.getCourses().get(0).setFilepath(filename);
			//DocumentManager manager = documentManager.getInstance();
			//manager.addDocument(manager.getFolder(manager.FOLDER_WORKSPACE), newDoc);
		} finally {
			tempFile.delete();
		}

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
	public void undoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(GPSDocument document) throws TrackItException {
		// TODO Auto-generated method stub

	}

	@Override
	public void redoOperation(List<GPSDocument> document) throws TrackItException {
		// TODO Auto-generated method stub

	}

}
