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
package com.trackit.business.utilities;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

import com.trackit.business.common.KeyConstructor;
import com.trackit.business.common.LineDistance;
import com.trackit.business.common.LinePart;
import com.trackit.business.common.Predicate;
import com.trackit.business.common.WGS84;
import com.trackit.business.domain.DocumentItem;


public class Utilities {
	private static final double C_RADIUS_EARTH_KM = 6370.97327862;
	
	//12335: 2018-03-17
	public static Double EarthRadius() {
		return C_RADIUS_EARTH_KM;
	}
	
	public static Double getGreatCircleDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
		Double delta;
		
		// convert to radians: radians = (degrees/180) * PI
		lat1 = (lat1 / 180) * Math.PI;
		lat2 = (lat2 / 180) * Math.PI;
		lon1 = (lon1 / 180) * Math.PI;
		lon2 = (lon2 / 180) * Math.PI;
		
		// get the central spherical angle
		delta = ((2 * Math.asin(Math.sqrt(Math.pow((Math.sin((lat1 - lat2) / 2)), 2)
				+ Math.cos(lat1) * Math.cos(lat2) * (Math.pow((Math.sin((lon1 - lon2) / 2)), 2))))));
		
		return delta * C_RADIUS_EARTH_KM;
	}

	/**
	 * Converts semicircles unit to degrees.
	 * 
	 * @param semicircles the value in semicircles to convert
	 * @return the converted value in degrees
	 */
	public static Double semicirclesToDegrees(Integer semicircles) {
		return semicircles / (Math.pow(2, 31) / 180);
	}
	
	/**
	 * Converts degrees to semicircles unit.
	 * 
	 * @param degrees the value in degrees to convert
	 * @return the converted value in semicircles
	 */
	public static Integer degreesToSemicircles(Double degrees) {
		return Double.valueOf(degrees * (Math.pow(2, 31) / 180)).intValue();
	}
		
	/**
	 * Returns the (initial) bearing between two points, in degrees.
	 *
	 * @param point1 the Latitude/longitude of the origin point
	 * @param point2 the Latitude/longitude of the destination point
	 * @returns the initial bearing in degrees from North
	 */
	public static double getInitialBearing(double latitude1, double longitude1, double latitude2, double longitude2) {
		double lat1 = degreesToRad(latitude1);
		double lat2 = degreesToRad(latitude2);
		double dLon = degreesToRad(longitude2 - longitude1);
		
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
		double bearing = Math.atan2(y, x);
		
		return ((radToDegrees(bearing) + 360) % 360);
	}
	
	public static double getBearing(double latitude1, double longitude1, double latitude2, double longitude2) {
		double lat1 = degreesToRad(latitude1);
		double lat2 = degreesToRad(latitude2);
		double lon1 = degreesToRad(longitude1);
		double lon2 = degreesToRad(longitude2);
		
		double bearingRad = Math.atan2(Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2)
	               * Math.cos(lon2 - lon1), Math.sin(lon2 - lon1) * Math.cos(lat2)) % (2 * Math.PI);
		
		return radToDegrees(bearingRad);
	}
	
	/** Converts numeric degrees to radians */
	public static double degreesToRad(double degrees) {
		return degrees * Math.PI / 180;
	}

	/** Converts radians to numeric (signed) degrees */
	public static double radToDegrees(double radians) {
		return radians * 180 / Math.PI;
	}
	
	/* Note: this conversion to miles uses the WGS84 value for the radius of
	 * the earth at the equator.
	 * (radius in meters)*(100cm/m) -> (radius in cm)
	 * (radius in cm) / (2.54 cm/in) -> (radius in in)
	 * (radius in in) / (12 in/ft) -> (radius in ft)
	 * (radius in ft) / (5280 ft/mi) -> (radius in mi)
	 * If the compiler is half-decent, it'll do all the math for us at compile
	 * time, so why not leave the expression human-readable? */
	public static double radtoMiles(double rads) {
		double radmiles = WGS84.EQUATORIALRADIUS * 100.0 / 2.54 / 12.0 / 5280.0;
		return (rads * radmiles);
	}
	
	public static double radToMeters(double rads) {
		return (rads * WGS84.EQUATORIALRADIUS);
	}
	
	public static double radToKm(double rads) {
// 12335: 2018-09-11
//		final double earthRadiusKm = 6370.97327862;
//		return (rads * earthRadiusKm);
		return rads * C_RADIUS_EARTH_KM;
	}
	
	private static double[] crossProduct(double x1, double y1, double z1, double x2, double y2, double z2) {
		double[] result = new double[3];
		
		result[0] = (y1 * z2) - (y2 * z1);
		result[1] = (z1 * x2) - (z2 * x1);
		result[2] = (x1 * y2) - (y1 * x2);
		
		return result;
	}
	
	private static double dotProduct(double x1, double y1, double z1, double x2, double y2, double z2) {
		return ((x1 * x2) + (y1 * y2) + (z1 * z2));
	}
	
	public static LineDistance lineDistProjected(double lat1, double lon1, double lat2, double lon2, double lat3, double lon3) {
		double aLat1 = -9999;
		double aLat2 = -9999;
		double aLon1 = -9999;
		double aLon2 = -9999;
		
		double x1 = 0.0, y1 = 0.0, z1 = 0.0;
		double x2 = 0.0, y2 = 0.0, z2 = 0.0;
		double xa = 0.0, ya = 0.0, za = 0.0, la = 0.0;
		
		double x3, y3, z3;
		double xp, yp, zp, lp;
		
		double xa1, ya1, za1;
		double xa2, ya2, za2;
		
		double d1, d2;
		double c1, c2;
		
		double dot;
		
		boolean newPoints;
		
		LineDistance result = new LineDistance();
		result.setLatProjected(lat1);
		result.setLonProjected(lon1);
		result.setFraction(0.0);
		
		/* degrees to radians */
		lat1 = degreesToRad(lat1);
		lon1 = degreesToRad(lon1);
		lat2 = degreesToRad(lat2);
		lon2 = degreesToRad(lon2);
		lat3 = degreesToRad(lat3);
		lon3 = degreesToRad(lon3);
		
		if (lat1 == aLat1 && lat2 == aLat2 && lon1 == aLon1 && lon2 == aLon2) {
			newPoints = false;
		} else {
			aLat1 = lat1;
			aLat2 = lat2;
			aLon1 = lon1;
			aLon2 = lon2;
			
			newPoints = true;
		}
		
		/* polar to ECEF rectangular */
		if (newPoints) {
			x1 = Math.cos(lon1) * Math.cos(lat1);
			y1 = Math.sin(lat1);
			z1 = Math.sin(lon1) * Math.cos(lat1);
			x2 = Math.cos(lon2) * Math.cos(lat2);
			y2 = Math.sin(lat2);
			z2 = Math.sin(lon2) * Math.cos(lat2);
		}
		
		x3 = Math.cos(lon3) * Math.cos(lat3);
		y3 = Math.sin(lat3);
		z3 = Math.sin(lon3) * Math.cos(lat3);
		
		if (newPoints) {
			
			/* 'a' is the axis; the line that passes through the center of the earth
			 * and is perpendicular to the great circle through point 1 and point 2
			 * It is computed by taking the cross product of the '1' and '2' vectors.*/
			double[] crossProduct = crossProduct(x1, y1, z1, x2, y2, z2);
			
			xa = crossProduct[0];
			ya = crossProduct[1];
			za = crossProduct[2];
			
			la = Math.sqrt(xa * xa + ya * ya + za * za);
			
			if (la != 0) {
			  xa /= la;
			  ya /= la;
			  za /= la;
			}
		}
		
		if (la != 0) {
		
			/* dot is the component of the length of '3' that is along the axis.
			 * What's left is a non-normalized vector that lies in the plane of
			 * 1 and 2. */
			
			dot = dotProduct(x3, y3, z3, xa, ya, za);
			
			xp = x3 - dot * xa;
			yp = y3 - dot * ya;
			zp = z3 - dot * za;
			
			lp = Math.sqrt(xp * xp + yp * yp + zp * zp);
			
			if (lp != 0) {
			
			  /* After this, 'p' is normalized */
			  xp /= lp;
			  yp /= lp;
			  zp /= lp;
			
			  double[] crossProduct = crossProduct(x1, y1, z1, xp, yp, zp);
			  xa1 = crossProduct[0];
			  ya1 = crossProduct[1];
			  za1 = crossProduct[2];
			  
			  d1 = dotProduct(xa1, ya1, za1, xa, ya, za);
			
			  crossProduct = crossProduct(xp,yp,zp,x2,y2,z2);
			  xa2 = crossProduct[0];
			  ya2 = crossProduct[1];
			  za2 = crossProduct[2];
			  
			  d2 = dotProduct(xa2, ya2, za2, xa, ya, za);
			
			  if (d1 >= 0 && d2 >= 0) {
				  
			    /* rather than call greatCircleDist and all its sines and cosines and
				 * worse, we can get the angle directly.  It's the arctangent
				 * of the length of the component of vector 3 along the axis
				 * divided by the length of the component of vector 3 in the
				 * plane.  We already have both of those numbers.
				 *
				 * atan2 would be overkill because lp and abs(dot) are both
				 * known to be positive. */
			
			    result.setLatProjected(radToDegrees(Math.asin(yp)));
			    
			    if (xp == 0 && zp == 0) {
			    	result.setLonProjected(0.0);
			    } else {
			    	result.setLonProjected(radToDegrees(Math.atan2(zp, xp)));
			    }
			    
			    result.setFraction(d1 / (d1 + d2));
			    
			    result.setDistance(Math.atan(Math.abs(dot) / lp));
			    
			    return result;
			  }
			
			  /* otherwise, get the distance from the closest endpoint */
			  c1 = dotProduct(x1, y1, z1, xp, yp, zp);
			  c2 = dotProduct(x2, y2, z2, xp, yp, zp);
			  d1 = Math.abs(d1);
			  d2 = Math.abs(d2);
			
			  /* This is a hack.  d$n$ is proportional to the sine of the angle
			   * between point $n$ and point p.  That preserves orderedness up
			   * to an angle of 90 degrees.  c$n$ is proportional to the cosine
			   * of the same angle; if the angle is over 90 degrees, c$n$ is
			   * negative.  In that case, we flop the sine across the y=1 axis
			   * so that the resulting value increases as the angle increases.
			   *
			   * This only works because all of the points are on a unit sphere. */
			
			  if (c1 < 0) {
				  d1 = 2 - d1;
			  }
			  
			  if (c2 < 0) {
				  d2 = 2 - d2;
			  }
			
			  if (Math.abs(d1) < Math.abs(d2)) {
				  result.setDistance(getGreatCircleDistance(lat1, lon1, lat3, lon3));
				  return result;
			  } else {
				  result.setLatProjected(radToDegrees(lat2));
				  result.setLonProjected(radToDegrees(lon2));
				  result.setFraction(1.0);
				  result.setDistance(getGreatCircleDistance(lat2, lon2, lat3, lon3));
				  return result;
			  }
			} else {
				
				/* lp is 0 when 3 is 90 degrees from the great circle */
				result.setDistance(Math.PI / 2);
				return result;
			}
		} else {
		
			/* la is 0 when 1 and 2 are either the same point or 180 degrees apart */
			dot = dotProduct(x1, y1, z1, x2, y2, z2);
			if (dot >= 0) {
				result.setDistance(getGreatCircleDistance(lat1, lon1, lat3, lon3));
				return result;
			} else {
				result.setDistance(0.0);
				return result;
			}
		}
	}

	public static double lineDistance(double lat1, double lon1, double lat2, double lon2, double lat3, double lon3) {
		LineDistance lineDistance = lineDistProjected(lat1, lon1, lat2, lon2, lat3, lon3);
		return lineDistance.getDistance();
	}

	/*
	 * Compute the position of a point partially along the geodesic from
	 * lat1,lon1 to lat2,lon2
	 * 
	 * Ref: http://mathworld.wolfram.com/RotationFormula.html
	 */
	public static LinePart linePart(double lat1, double lon1, double lat2, double lon2, double frac) {
		double x1, y1, z1;
		double x2, y2, z2;
		double xa, ya, za, la;
		double xr, yr, zr;
		double xx, yx, zx;
		
		double theta = 0.0;
		double phi = 0.0;
		double cosphi = 0.0;
		double sinphi = 0.0;
		
		/* result must be in degrees */
		LinePart result = new LinePart(lat1, lon1);
		
		/* degrees to radians */
		lat1 = degreesToRad(lat1);
		lon1 = degreesToRad(lon1);
		lat2 = degreesToRad(lat2);
		lon2 = degreesToRad(lon2);
		
		/* polar to ECEF rectangular */
		x1 = Math.cos(lon1) * Math.cos(lat1);
		y1 = Math.sin(lat1);
		z1 = Math.sin(lon1) * Math.cos(lat1);
		x2 = Math.cos(lon2) * Math.cos(lat2);
		y2 = Math.sin(lat2);
		z2 = Math.sin(lon2) * Math.cos(lat2);
		
		/* 'a' is the axis; the line that passes through the center of the earth
		 * and is perpendicular to the great circle through point 1 and point 2
		 * It is computed by taking the cross product of the '1' and '2' vectors.*/
		double[] crossProduct = crossProduct(x1, y1, z1, x2, y2, z2);
		xa = crossProduct[0];
		ya = crossProduct[1];
		za = crossProduct[2];
		
		la = Math.sqrt(xa * xa + ya * ya + za * za);
		
		if (la != 0) {
			xa /= la;
			ya /= la;
			za /= la;
		}
		
		/* if la is zero, the points are either equal or directly opposite
		 * each other.  Either way, there's no single geodesic, so we punt. */
		if (la != 0) {
			crossProduct = crossProduct(x1, y1, z1, xa, ya, za);
			xx = crossProduct[0];
			yx = crossProduct[1];
			zx = crossProduct[2];
		
			theta = Math.atan2(dotProduct(xx, yx, zx, x2, y2, z2), dotProduct(x1, y1, z1, x2, y2, z2));
			
			phi = frac * theta;
			cosphi = Math.cos(phi);
			sinphi = Math.sin(phi);

			/* The second term of the formula from the mathworld reference is always
			 * zero, because r (lat1, lon1) is always perpendicular to n (a here) */
			xr = x1 * cosphi + xx * sinphi;
			yr = y1 * cosphi + yx * sinphi;
			zr = z1 * cosphi + zx * sinphi;
		
			if (xr > 1) {
			  xr = 1;
			}
			
			if (xr < -1) {
			  xr = -1;
			}
			
			if (yr > 1) {
			  yr = 1;
			}
			
			if (yr < -1) {
			  yr = -1;
			}
			
			if (zr > 1) {
			  zr = 1;
			}
			
			if (zr < -1) {
			  zr = -1;
			}
			
			result.setResLatitude(radToDegrees(Math.asin(yr)));
			if (xr == 0 && zr == 0) {
				result.setResLongitude(0.0);
			} else {
				result.setResLongitude(radToDegrees(Math.atan2(zr, xr)));
			}
			
			return result;
		}
		
		return result;
	}
	
	//12335: 2018-09-10 - moved to business.utilities.StringUtilities
//	public static String pad(String text, char pad, int size, int direction) {
//		StringBuffer sb = new StringBuffer();
//		
//		if (direction == LEFT_PAD) {
//			for (int i = 0; i < size - text.length(); i++) {
//				sb.append(pad);
//			}
//			sb.append(text);
//		} else if (direction == RIGHT_PAD) {
//			sb.append(text);
//			for (int i = 0; i < size - text.length(); i++) {
//				sb.append(pad);
//			}
//		} else {
//			sb.append(text);
//		}
//		
//		return sb.toString();
//	}
	
	public static boolean between(double value, double lowerLimit, double upperLimit) {
		return (value >= lowerLimit && value <= upperLimit);
	}
	
	public static boolean between(Date date, Date lowerLimit, Date upperLimit) {
		return (date.getTime() >= lowerLimit.getTime() && date.getTime() <= upperLimit.getTime());
	}
	
	public static <T> List<T> filter(List<T> objects, Predicate<T> predicate){
    	List<T> result = new ArrayList<T>();
    	for(T entry : objects){
    		if(predicate.apply(entry))
    			result.add(entry);
    	}
    	
    	return result;
    }
	
	@SafeVarargs
	public static <T> List<List<T>> addLists(List<T>... lists) {
		List<List<T>> finalList = new ArrayList<List<T>>(); 
    	
		for (List<T> list : lists) {
    		finalList.add(list);
    	}

    	return finalList;
    }
	
	/**
     * Transforma um array num Map, utilizando as chaves dos objectos do array.
     *
     * @param objs array a converter
     * @param keyConstructor define a chave dos objectos
     * @param type define os tipos dos objectos do array
     * @return array covertida em hashMap
     */
	public static <E, R> Map<E, R> toMap(Object[] objs, KeyConstructor<E, R> keyConstructor, Class<R> type) {
		if (objs == null) {
            return null;
        }
		
		Map<E, R> map = new HashMap<E, R>();

		for (Object o : objs) {
			R value = type.cast(o);
			E key = keyConstructor.getKey(value);
			map.put(key, value);
		}

		return map;
	}
	
	/**
	 * Transforma uma lista num Map, utilizando as chaves dos objectos da lista.
	 *
	 * @param objs lista a converter
	 * @param keyConstructor define a chave dos objectos
	 * @return lista covertida em hashMap
	 */
	public static <E, R> Map<E, R> toMap(List<R> objs, KeyConstructor<E, R> keyConstructor) {
		if (objs == null) {
			return null;
		}
		
		Map<E, R> map = new HashMap<E, R>();
		
		for (R o : objs) {
			E key = keyConstructor.getKey(o);
			map.put(key, o);
		}
		
		return map;
	}
	
	public static <E> boolean forAll(Collection<E> dados, Predicate<E> predicate) {
		boolean result = true;
		if (dados.isEmpty())
			result = false;
		else {
			for (E entry : dados) {
				result = result && predicate.apply(entry);
				if (result == false)
					break;
			}
		}

		return result;
	}
	
	public static <E> boolean exists(Collection<E> dados, Predicate<E> predicate) {
		boolean result = false;
		if (dados != null && !dados.isEmpty()){
			for(E entry : dados){
				if(predicate.apply(entry)){
					result = true;
					break;
				}
					
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> convert(List<? extends DocumentItem> items) {
		List<T> newList = new ArrayList<T>();
		for (DocumentItem item : items) {
			newList.add((T) item);
		}
		return newList;
	}
	
	public static void recursivelyDelete(File rootPath) {
		File[] currList;
		Stack<File> stack = new Stack<File>();
		stack.push(rootPath);
		
		while (!stack.isEmpty()) {
		    if (stack.lastElement().isDirectory()) {
		        currList = stack.lastElement().listFiles();
		        if (currList.length > 0) {
		            for (File curr: currList) {
		                stack.push(curr);
		            }
		        } else {
		            stack.pop().delete();
		        }
		    } else {
		        stack.pop().delete();
		    }
		}
	}
	
	public static int getRandomNumber(int min, int max) {
		return min + (int) (Math.random() * ((max - min) + 1));
	}
}