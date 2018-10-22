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

import java.util.Calendar;
import java.util.Date;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class NOAAMagneticDeclinationService extends MagneticDeclinationService {
	
	private final static String declinationPath = "/maggridresult/result/declination";
	private final static String uncertaintyPath = "/maggridresult/result/declination_uncertainty";
	private final static String changeRatePath  = "/maggridresult/result/declination_sv";
	private final static String elevationPath   = "/maggridresult/result/elevation";
	private final static String datePath        = "/maggridresult/result/date";
	
	private Document doc         = null;
	private XPath			xpath = null;	
	private XPathExpression expr = null;
	
	public NOAAMagneticDeclinationService() {
		super();
	}
	
	public Double getDeclination( double latitude, double longitude, Date date) {
		if ( latitude >= -90. && latitude <= 90. && longitude >= -180. && longitude <= 180. ) {
			if ( this.latitude  != null && this.latitude  == latitude &&
				 this.longitude != null && this.longitude == longitude  &&
				 this.date      != null && this.date      == date          )
				return declination;
			try {
				String url = "https://www.ngdc.noaa.gov/geomag-web/calculators/calculateDeclination?"
					        + "lat1=" + latitude + "&lon1=" + longitude;
				if ( date != null ) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime( date);
					url += "&startYear="  + calendar.get( Calendar.YEAR)
					     + "&startMonth=" + (calendar.get( Calendar.MONTH)+1)
					     + "&startDay="   + calendar.get( Calendar.DAY_OF_MONTH);
					if ( calendar.get( Calendar.YEAR) <= 2014 )
						url += "&model=IGRF";
				}
				url += "&resultFormat=xml";
				InputStream inputStream = connection.getResource( url);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				doc = builder.parse( inputStream);
				XPathFactory xPathfactory = XPathFactory.newInstance();
				xpath = xPathfactory.newXPath();
				declination = getDouble( declinationPath);
				uncertainty = getDouble( uncertaintyPath);
				changeRate = getDouble( changeRatePath);
				elevation = getDouble( elevationPath);
//				expr = xpath.compile( datePath);
//				String date = expr.evaluate(doc, XPathConstants.STRING).toString();
				this.latitude  = latitude;
				this.longitude = longitude;
				if ( date != null )
					this.date = date;
				return declination;
			} catch (Exception e) {
				System.out.println( e.getCause());
			}
		}
		return null;
	}
	
	private Double getDouble( String path) {
		try {
			expr = xpath.compile( path);
			String evaluated = expr.evaluate(doc, XPathConstants.STRING).toString();
			if ( evaluated != null )
				return Double.parseDouble( evaluated);
		} catch (Exception e) {
		}
		return null;
	}

}
