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

		try {
			Map<String, Object> options = new HashMap<>();
			options.put(Constants.Writer.OUTPUT_DIR, tempFile.getParentFile()
					.getAbsolutePath());
			Writer writer = WriterFactory.getInstance().getWriter(tempFile,
					options);
			writer.write(document);

		} catch (TrackItException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(
					TrackIt.getApplicationFrame(),
					getMessage("operation.failure.fileExport",
							course.getDocumentItemName()),
					getMessage("operation.failure"), JOptionPane.ERROR_MESSAGE);
		}
		course.setName(oldCourseName);

		DocumentManager documentManager = DocumentManager.getInstance();

		GPSDocument readDocument = null;

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

		for (Course c : document.getCourses()) {
			documentManager.getDatabase().updateDB(c, tempFile);
			c.setFilepath(tempFile.getAbsolutePath());
			if (c.getTrackpoints().get(0).getSpeed() != null)
				c.setNoSpeedInFile(false);
			c.setUnsavedFalse();
		}

		document.getCourses().get(0).setFilepath(filename);

		tempFile.delete();

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
