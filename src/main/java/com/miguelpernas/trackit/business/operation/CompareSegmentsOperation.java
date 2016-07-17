package com.miguelpernas.trackit.business.operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.henriquemalheiro.trackit.TrackIt;
import com.henriquemalheiro.trackit.business.common.BoundingBox2;
import com.henriquemalheiro.trackit.business.common.Location;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.WGS84;
import com.henriquemalheiro.trackit.business.domain.Course;
import com.henriquemalheiro.trackit.business.domain.Trackpoint;
import com.henriquemalheiro.trackit.presentation.view.map.MapView;
import com.henriquemalheiro.trackit.presentation.view.map.provider.MapProvider;
import com.miguelpernas.trackit.presentation.CompareSegmentDialog;

public class CompareSegmentsOperation {
	private List<Course> segments;
	
	

	public CompareSegmentsOperation(List<Course> segments) {
		this.segments = segments;
	}
	
		//57421
		private List<Long> segmentIntersection(final List<Course> segments) {
			List<Long> segmentIntersetionIds = new ArrayList<Long>();
			long id1;
			long id2;
			Course course1;
			Course course2;
			for (int i = 0; i < segments.size(); i++) {
				course1 = segments.get(i);
				id1 = course1.getId();
				for (int j = 0; j < segments.size(); j++) {
					course2 = segments.get(j);
					id2 = course2.getId();

					if (id1 != id2 && (!(segmentIntersetionIds.contains(id1)) || !(segmentIntersetionIds.contains(id2)))) {
						if (coursesIntersect(course1, course2)) {
							if (!(segmentIntersetionIds.contains(id1))) {
								segmentIntersetionIds.add(course1.getId());
							}
							if (!(segmentIntersetionIds.contains(id2))) {
								segmentIntersetionIds.add(course2.getId());
							}
						}
					}
				}
			}

			return segmentIntersetionIds;
		}
		//57421
		private boolean coursesIntersect(Course course1, Course course2) {
			boolean intersect = false;
			int numberOfIntersections = 0;
			int maxIntersections = 3;
			double lat1, lat2, long1, long2;
			BoundingBox2<Location> bounds1 = course1.getBounds();
			BoundingBox2<Location> bounds2 = course2.getBounds();
			double minLat = bounds1.getBottomRight().getLatitude();
			double maxLat = bounds1.getTopLeft().getLatitude();
			double minLong = bounds1.getTopLeft().getLongitude();
			double maxLong = bounds1.getBottomRight().getLongitude();
			for (Trackpoint trackpoint2 : course2.getTrackpoints()) {
				lat2 = trackpoint2.getLatitude();
				long2 = trackpoint2.getLongitude();
				if(lat2>=minLat && lat2<=maxLat && long2>=minLong && long2<=maxLong){
					numberOfIntersections += 1;
					if (numberOfIntersections >= maxIntersections) {
						intersect = true;
						break;
					}
				}
				
			}
			
			return intersect;
		}
		//57421
		private double[] getScaleModifier(Course course){
			double modifier;
			MapView mapView = TrackIt.getApplicationPanel().getMapView();
			com.henriquemalheiro.trackit.presentation.view.map.Map map = mapView.getMap();
			byte zoom = map.getMapProvider().getZoom();
			double averageLatitude = (course.getStartLatitude() + course.getEndLatitude())/2;
			
			modifier = calculateGroundResolution(averageLatitude, zoom);
			double longitude = metersXToLongitude(modifier);
			double latitude = metersYToLatitude(modifier);
			double modifiers[] = new double[2];
			modifiers[0] = latitude;
			modifiers[1] = longitude;
			
			return modifiers;
		}
		
		private double calculateGroundResolution(double latitude, byte zoom) {
			MapProvider mapProvider = TrackIt.getApplicationPanel().getMapView().getMap().getMapProvider();
			return Math.cos(latitude * Math.PI / 180) * 40075016.686
					/ (mapProvider.getTileWidth() << zoom);
		}
		
		private double metersXToLongitude(double x) {
			return Math.toDegrees(x / WGS84.EQUATORIALRADIUS);
		}

		private double metersYToLatitude(double y) {
			return Math.toDegrees(Math.atan(Math.sinh(y / WGS84.EQUATORIALRADIUS)));
		}
		
		//57421
		public void compareSegments() {
			Objects.requireNonNull(segments);
			for (Course segment : segments) {
				if (segment.getSegmentDistance() == null || segment.getSegmentMovingTime() == null) {
					JOptionPane.showMessageDialog(TrackIt.getApplicationFrame(),
							Messages.getMessage("dialog.compareSegments.notSegment", segment.getName()),
							Messages.getMessage("trackIt.error"), JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			MapView mapView = TrackIt.getApplicationPanel().getMapView();
			
			boolean firstSegment = true;
			//double modifier = 0.0002;
			double modifiers[] = getScaleModifier(segments.get(0));
			int pixelModifier = 15;
			double latitudeModifier = modifiers[0] * pixelModifier;
			double longitudeModifier = modifiers[1] * pixelModifier;
			
			double modifierMult = 1;
			int middleSegment = segments.size() / 2;
			List<Long> shiftedIds = segmentIntersection(segments);
			for (Course course : segments) {

				if (!firstSegment) {
					if (shiftedIds.contains(course.getId())) {

						for (Trackpoint trackpoint : course.getTrackpoints()) {
							trackpoint.setLatitude(trackpoint.getLatitude() - (latitudeModifier * modifierMult));
							trackpoint.setLongitude(trackpoint.getLongitude() + (longitudeModifier * modifierMult));

						}
						modifierMult++;;
					}
				}
				if (firstSegment) {
					firstSegment = false;
				}

			}
			
			mapView.updateDisplay();
			// map.zoomToFitFeature(segments.get(middleSegment));
			JDialog compareSegmentDialog = new CompareSegmentDialog(segments, latitudeModifier, longitudeModifier, modifierMult, shiftedIds, this);
			compareSegmentDialog.setVisible(true);

			//finish(latitudeModifier, longitudeModifier, modifierMult, shiftedIds);
		}

		public void finish(double latitudeModifier, double longitudeModifier, double modifierMult,
				List<Long> shiftedIds) {
			long lastId = segments.get(0).getId();
			Collections.reverse(segments);
			modifierMult--;
			for (Course course : segments) {
				if (course.getId() != lastId && shiftedIds.contains(course.getId())) {
					for (Trackpoint trackpoint : course.getTrackpoints()) {
						trackpoint.setLatitude(trackpoint.getLatitude() + (latitudeModifier * modifierMult));
						trackpoint.setLongitude(trackpoint.getLongitude() - (longitudeModifier * modifierMult));
					}
					modifierMult--;
				}

			}
		}

	
}
