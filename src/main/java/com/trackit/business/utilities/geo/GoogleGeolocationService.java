/*
 * This file is part of Track It!.
 * Copyright (C) 2018 João Brisson
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

package com.trackit.business.utilities.geo;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.trackit.business.domain.GeographicBoundingBox;
import com.trackit.business.utilities.Connection;

public class GoogleGeolocationService extends GeolocationService {
	
	private static final String OK = "OK";
	public  static final String ZERO_RESULTS    = "ZERO_RESULTS";
	private static final String OVER_QUERY      = "OVER_QUERY_LIMIT";
	private static final String REQUEST_DENIED  = "REQUEST_DENIED";
	private static final String INVALID_REQUEST = "INVALID_REQUEST";
	private static final String UNKNOWN_ERROR   = "UNKNOWN_ERROR";
	
	private static final String key = "AIzaSyCDNNuvyCfG0INvds4ZBnbuwm0xXWLJctM";
	
	private static final String statusPath = "/GeocodeResponse/status";
	private static final String centerPath = "/GeocodeResponse/result/geometry/location/";
	private static final String nePath     = "/GeocodeResponse/result/geometry/bounds/northeast/";
	private static final String swPath     = "/GeocodeResponse/result/geometry/bounds/southwest/";

	Connection connection;
	
	public GoogleGeolocationService() {
		connection = Connection.getInstance();
	}

	@Override
	protected double[] getLocationCoordinates(String locationName) {
		String url = "https://maps.googleapis.com/maps/api/geocode/xml?address=" + 
					 locationName.replace( ' ', '+') +"&key="+key;
		System.out.println(url);
		InputStream inputStream = connection.getResource(url);
		System.out.println( "Trying the main way");
		double[] coordinates = new double[4];
		try {
			inputStream = connection.getResource(url);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(inputStream);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr;
			expr = xpath.compile( statusPath);
			status = expr.evaluate(doc, XPathConstants.STRING).toString();
			System.out.println( "Status(1) " + status);
			if ( status.equals( "OK") ) {
				System.out.println( "Status(2) " + status);
				expr = xpath.compile( swPath + "lat");
				String swLat = expr.evaluate(doc, XPathConstants.STRING).toString();
				expr = xpath.compile( swPath + "lng");
				String swLon = expr.evaluate(doc, XPathConstants.STRING).toString();
				expr = xpath.compile( nePath + "lat");
				String neLat = expr.evaluate(doc, XPathConstants.STRING).toString();
				expr = xpath.compile( nePath + "lng");
				String neLon = expr.evaluate(doc, XPathConstants.STRING).toString();
				System.out.println( "Status(3) " + status);
				System.out.println("neLat: "+ neLat + "  swLat: "+ swLat + "  neLon: "+ neLon + "  swLon: "+ swLon);
				if ( neLat != null && !neLat.isEmpty() && neLon != null && !neLon.isEmpty() &&
					 swLat != null && !swLat.isEmpty() && swLon != null && !swLon.isEmpty()    ) {
					System.out.println( "Status(4) " + status);
					coordinates[0] = Double.valueOf( swLat);
					coordinates[1] = Double.valueOf( swLon);
					coordinates[2] = Double.valueOf( neLat);
					coordinates[3] = Double.valueOf( neLon);
				} else
				{
					System.out.println( "The alternate way");
					expr = xpath.compile( centerPath + "lat");
					String ctrLat = expr.evaluate(doc, XPathConstants.STRING).toString();
					expr = xpath.compile( centerPath+ "lng");
					String ctrLon = expr.evaluate(doc, XPathConstants.STRING).toString();
					System.out.println("ctrLat: "+ ctrLat + "  ctrLon: "+ ctrLon);
					coordinates[0] = coordinates[2] = Double.valueOf( ctrLat);
					coordinates[1] = coordinates[3] = Double.valueOf( ctrLon);
				}
				return coordinates;
			}
			else {
				boolean error = status == ZERO_RESULTS;
				showStatus( null, error, locationName);
			}
		} catch (Exception e) {
			System.out.println( e.getCause());
		}
		return null;
	}
	
}
