/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
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
package com.henriquemalheiro.trackit.business.common;

import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

import com.henriquemalheiro.trackit.business.domain.FieldMetadata;

public class Formatters {
	private static final String UTC = "Etc/GMT";

	private static final String DEFAULT_DECIMAL_FORMAT_PATTERN = "0.00000000000000";

	private static DecimalFormat decimalFormat;
	private static DecimalFormat decimalFormatWithGrouping;
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static SimpleDateFormat simpleDateFormatMilis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static SimpleDateFormat hourMinuteSecondFormat = new SimpleDateFormat("HH:mm:ss");
	private static SimpleDateFormat localDateFormat = new SimpleDateFormat("EE, dd-MM-yyyy, HH:mm:ss",
	        Messages.getLocale());
	private static SimpleDateFormat fileDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm");  // 12335: 2015-07-16

	static {
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(UTC));
		simpleDateFormatMilis.setTimeZone(TimeZone.getTimeZone(UTC));
		hourMinuteSecondFormat.setTimeZone(TimeZone.getTimeZone(UTC));

		decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
		decimalFormat.applyPattern(DEFAULT_DECIMAL_FORMAT_PATTERN);
		DecimalFormatSymbols custom = new DecimalFormatSymbols();
		custom.setDecimalSeparator('.');
		decimalFormat.setDecimalFormatSymbols(custom);

		decimalFormatWithGrouping = (DecimalFormat) DecimalFormat.getInstance();
		decimalFormatWithGrouping.applyPattern(DEFAULT_DECIMAL_FORMAT_PATTERN);
		custom = new DecimalFormatSymbols();
		custom.setDecimalSeparator('.');
		custom.setGroupingSeparator(',');
		decimalFormatWithGrouping.setDecimalFormatSymbols(custom);
	}

	public Formatters() {
	}

	public static String formatLocalDate(Date date) {
		return localDateFormat.format(date);
	}

	public static SimpleDateFormat getSimpleDateFormat() {
		return simpleDateFormat;
	}

	public static SimpleDateFormat getSimpleDateFormatMilis() {
		return simpleDateFormatMilis;
	}

	// 12335 : 2015-07-16
	public static SimpleDateFormat getSimpleDateFormatForFiles() {
		return fileDateFormat;
	}
	
	// 12335 : 2015-07-17
	public static String getDateStringForFiles( FileTime time) {
		return getSimpleDateFormatForFiles().format(new Date(time.toMillis()));
	}

	public static String getDateFormatW3C(Date date) {
		Locale locale = Locale.getDefault();
		TimeZone currentTimeZone = TimeZone.getTimeZone("UTC");

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", locale);
		df.setTimeZone(currentTimeZone);

		String text = df.format(date);
		String dateTimeText = text.substring(0, 22) + ":" + text.substring(22);

		return dateTimeText;
	}

	public static Date parseW3CLocalDate(String date) {
		Date parsedDate = null;
		try {
			TimeZone currentTimeZone = TimeZone.getTimeZone("UTC");

			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
				private static final long serialVersionUID = 1L;

				@Override
				public Date parse(String source) throws ParseException {
					return super.parse(source.replaceFirst(":(?=[0-9]{2}$)", ""));
				}
			};
			df.setTimeZone(currentTimeZone);

			parsedDate = df.parse(date);
		} catch (ParseException e) {
			// ignore
		}

		return parsedDate;
	}

	public static Date parseDate(String dateTime) throws ParseException {
		return DatatypeConverter.parseDate(dateTime).getTime();
	}

	public static String getFormatedDuration(double seconds) {
		long numberOfSeconds = (long) seconds;

		return String.format("%d:%02d:%02d", numberOfSeconds / 3600, (numberOfSeconds % 3600) / 60,
		        (numberOfSeconds % 60));
	}

	public static String getFormatedDistance(double meters) {
		String formatedDistance = "";

		if (meters >= 1000.0) {
			double kilometers = meters / 1000;
			formatedDistance = String
			        .format("%s %s", getDecimalFormat(3).format(kilometers), Unit.KILOMETER.toString());
		} else {
			formatedDistance = String.format("%s %s", getDecimalFormat(0).format(meters), Unit.METER.toString());
		}

		return formatedDistance;
	}

	public static String getFormatedSpeed(double speed) {
		double speedKmH = speed / 1000 * 3600;

		return getDecimalFormat(1).format(speedKmH) + " km/h";
	}

	public static String getFormatedAltitude(double altitude) {
		return getDecimalFormat(0).format(altitude) + " m";
	}

	public static DecimalFormat getDecimalFormat() {
		decimalFormat.applyPattern(DEFAULT_DECIMAL_FORMAT_PATTERN);
		return decimalFormat;
	}

	public static DecimalFormat getDecimalFormatWithGrouping() {
		decimalFormatWithGrouping.applyPattern(DEFAULT_DECIMAL_FORMAT_PATTERN);
		return decimalFormatWithGrouping;
	}

	public static String getFormatedExecutionTime(long executionTime) {
		SimpleDateFormat executionTimeFormat = new SimpleDateFormat("mm:ss.SSS");
		executionTimeFormat.setTimeZone(TimeZone.getTimeZone(UTC));

		return executionTimeFormat.format(executionTime);
	}

	public static DecimalFormat getDecimalFormat(int precision) {
		final int MAX_PRECISION = 15;
		final String decimalPlaces = "000000000000000";

		String decimalPart = decimalPlaces.substring(0, Math.min(precision, MAX_PRECISION));
		String pattern = "0" + (decimalPart.length() > 0 ? "." : "") + decimalPart;
		decimalFormat.applyPattern(pattern);

		return decimalFormat;
	}

	public static DecimalFormat getDecimalFormatWithGrouping(int precision) {
		final int MAX_PRECISION = 15;
		final String decimalPlaces = "000000000000000";

		String decimalPart = decimalPlaces.substring(0, Math.min(precision, MAX_PRECISION));
		String pattern = "0" + (decimalPart.length() > 0 ? "." : "") + decimalPart;
		decimalFormatWithGrouping.applyPattern(pattern);

		return decimalFormatWithGrouping;
	}

	public static String getFormatedValue(Object value, FieldMetadata metadata) throws NumberFormatException {
		if (value == null) {
			return "";
		}

		switch (metadata.getUnitCategory()) {

		case ANGLE:
			if (!metadata.getType().equals(Double.class.getName())) {
				throw new NumberFormatException();
			}
			return getDecimalFormat(8).format((Double) value);

		case CADENCE:
			if (!metadata.getType().equals(Short.class.getName())) {
				throw new NumberFormatException();
			}
			return ((Short) value) + " " + metadata.getUnit().toString();

		case CALORIES:
			if (!metadata.getType().equals(Integer.class.getName())) {
				throw new NumberFormatException();
			}
			return ((Integer) value) + " " + metadata.getUnit().toString();

		case TRACKPOINT_DENSITY:
			if (!metadata.getType().equals(Double.class.getName())) {
				throw new NumberFormatException();
			}

			Double density = (Double) value;

			return getDecimalFormat(5).format(density) + " " + metadata.getUnit().toString();

		case DISTANCE:
			if (!metadata.getType().equals(Double.class.getName())) {
				throw new NumberFormatException();
			}

			Double newValue = (Double) value;
			if (newValue <= 1000) {
				return getDecimalFormat(3).format(newValue) + " " + metadata.getUnit().toString();
			} else {
				return getDecimalFormat(3).format(newValue / 1000) + " " + Unit.KILOMETER.toString();
			}

		case DURATION:
			if (metadata.getType().equals(Double.class.getName())) {
				return getFormatedDuration((Double) value);
			} else if (metadata.getType().equals(Float.class.getName())) {
				return getFormatedDuration((Float) value);
			} else if (metadata.getType().equals(Long.class.getName())) {
				return getFormatedDuration((Long) value);
			} else {
				throw new NumberFormatException();
			}

		case GRADE:
			return String.format("%s %s", getDecimalFormat(1).format(value), metadata.getUnit().toString());

		case HEART_RATE:
			if (!metadata.getType().equals(Short.class.getName())) {
				throw new NumberFormatException();
			}
			return ((Short) value) + " " + metadata.getUnit().toString();

		case HEIGHT:
			if (metadata.getType().equals(Double.class.getName())) {
				return getDecimalFormatWithGrouping(1).format((Double) value) + " " + metadata.getUnit().toString();
			} else if (metadata.getType().equals(Float.class.getName())) {
				return getDecimalFormatWithGrouping(1).format((Float) value) + " " + metadata.getUnit().toString();
			} else if (metadata.getType().equals(Integer.class.getName())) {
				return ((Integer) value) + " " + metadata.getUnit().toString();
			} else {
				throw new NumberFormatException();
			}

		case NONE:
			if (metadata.getType().equals(List.class.getName())) {
				return String.valueOf(((List<?>) value).size());
			} else {
				return value.toString();
			}

		case POWER:
			if (!metadata.getType().equals(Integer.class.getName())) {
				throw new NumberFormatException();
			}

			Integer power = (Integer) value;
			return power + " " + metadata.getUnit().toString();

		case PRODUCT:
			return value.toString();

		case SPEED:
			if (metadata.getType().equals(Double.class.getName())) {
				Double speed = ((Double) value) * 3600.0 / 1000.0;
				return getDecimalFormat(1).format(speed) + " " + metadata.getUnit().toString();
			} else if (metadata.getType().equals(Float.class.getName())) {
				Float speed = ((Float) value) * 3600.0f / 1000.0f;
				return getDecimalFormat(1).format(speed) + " " + metadata.getUnit().toString();
			} else {
				throw new NumberFormatException();
			}

		case TEMPERATURE:
			if (metadata.getType().equals(Double.class.getName())) {
				Double temperature = (Double) value;
				return getDecimalFormat(0).format(temperature) + " " + metadata.getUnit().toString();
			} else if (metadata.getType().equals(Byte.class.getName())) {
				Byte temperature = (Byte) value;
				return getDecimalFormat(0).format(temperature) + " " + metadata.getUnit().toString();
			} else {
				throw new NumberFormatException();
			}

		case TIME:
			if (!metadata.getType().equals(Date.class.getName())) {
				throw new NumberFormatException();
			}
			SimpleDateFormat dateFormat = new SimpleDateFormat("EE dd MMM yyyy HH:mm:ss", Messages.getLocale());

			return dateFormat.format((Date) value);

		case TIMESTAMP:
			if (!metadata.getType().equals(Date.class.getName())) {
				throw new NumberFormatException();
			}

			return getSimpleDateFormat().format((Date) value);

		case VERSION:
			if (metadata.getType().equals(Integer.class.getName())) {
				Double version = ((Integer) value) / 100.0;
				return getDecimalFormat(2).format(version) + " " + metadata.getUnit().toString();
			} else if (metadata.getType().equals(Short.class.getName())) {
				Double version = ((Short) value) / 100.0;
				return getDecimalFormat(2).format(version) + " " + metadata.getUnit().toString();
			} else if (metadata.getType().equals(Float.class.getName())) {
				Float version = ((Float) value);
				return getDecimalFormat(2).format(version) + " " + metadata.getUnit().toString();
			} else {
				throw new NumberFormatException();
			}

		case WORK:
			if (!metadata.getType().equals(Long.class.getName())) {
				throw new NumberFormatException();
			}

			Long work = (Long) value;
			return work + " " + metadata.getUnit().toString();

		default:
			return value.toString();
		}
	}
}
