package com.henriquemalheiro.trackit.business.utility;

import java.util.ArrayList;
import java.util.List;

import com.henriquemalheiro.trackit.business.common.Location;

public class GooglePolyline {
	public static List<Location> decode(String encoded) {
		List<Location> polyline = new ArrayList<Location>();
		int index = 0;
		int len = encoded.length();
		int latitude = 0;
		int longitude = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			latitude += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			longitude += dlng;

			Location location = new Location((double) longitude / 1E5, (double) latitude / 1E5);
			polyline.add(location);
		}

		return polyline;
	}
	
	public static String encode(List<Location> locations) {
		Location lastLocation = new Location(0.0, 0.0);
		double longitudeOffset = 0.0;
		double latitudeOffset = 0.0;
		
		StringBuilder builder = new StringBuilder();
		for (Location location : locations) {
			longitudeOffset = location.getLongitude() - lastLocation.getLongitude();
			latitudeOffset = location.getLatitude() - lastLocation.getLatitude();
			builder.append(encode(longitudeOffset, latitudeOffset));
			lastLocation = location;
		}
		
		return builder.toString();
	}
	
	private static String encode(double longitude, double latitude) {
		return String.format("%s%s", encodeCoordinate(latitude), encodeCoordinate(longitude));
	}
	
	private static String encodeCoordinate(double coordinate) {
		int intCoordinate = floor1e5(coordinate);
		return encodeSignedNumber(intCoordinate);
	}
	
	private static int floor1e5(double coordinate) {
        return (int) (Math.round(coordinate * 1e5));
    }
	
	private static String encodeSignedNumber(int num) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~(sgn_num);
        }
        return(encodeNumber(sgn_num));
    }
	
	private static String encodeNumber(int num) {
        StringBuffer encodeString = new StringBuffer();
        while (num >= 0x20) {
                int nextValue = (0x20 | (num & 0x1f)) + 63;
                if (nextValue == 92) {
                        encodeString.append((char)(nextValue));
                }
                encodeString.append((char)(nextValue));
            num >>= 5;
        }

        num += 63;
        if (num == 92) {
                encodeString.append((char)(num));
        }

        encodeString.append((char)(num));

        return encodeString.toString();
    }
	
	public static void main(String[] args) {
		String encodedPolyline = "}qrkFptzv@IB[iDG}@a@kCa@gE]iEGy@zCQxSqAL?FFPJPFR@VEPOLYn@EZFzANpCf@PFPN^\\f"
				+ "@v@N^RVh@l@f@ThCp@bBd@\\Bd@GvB}@dGiCxBeAd@tBrAjG\\rBJd@r@hD`@rBBPnBkAvBwAjFoDzBoAjAo@v@k@r"
				+ "B{AjC}A|B{Az@o@\\W~@a@`@QlIoApEo@~AYPD\\Bz@DdEg@vEw@nDg@RnBb@`FH^zAlEp@jBDPbBxElGbRpBbGNv@"
				+ "@^Ev@}Az@iBnAGd@A\\?LD`@Rn@Pr@PN`@LV@LAtA}@ZA^Bl@HVFT\\T`@Rd@p@fCx@|Dj@dCLb@l@fBtAtCn@`Ar@"
				+ "z@n@p@b@Zn@ZdDtArJdEnDdBp@h@^j@Vt@Lr@Br@Al@IjBF`ANx@Xn@d@r@xB`DZd@h@z@Nd@Fn@@`@Ot@MVa@p@SX"
				+ "M`@Ed@APB`@Lf@Xd@n@f@~@f@HLL\\JdAHPBLLLHFNB^AhB_@bAOp@Ap@Jp@T^R`Af@n@`@j@r@zBhDhCzDjAlB`@`"
				+ "AF\\HfAJlCFtCAf@MrA_@tB]rAU\\]^eA`A]b@O\\A^B`@HRJJNHNB`AIlC_@lCe@r@ARFRN|@vAn@|@^Xp@TXFlAR"
				+ "ZV^^Td@Vh@@jADzACp@Gl@Ql@sAf@oK`E_Cz@sAn@m@n@q@bAg@hAe@nAm@pBYrAU~AQbBE`BBhDP`Cz@dFOJITAZH"
				+ "RLLRBHCLKHU?IfA?VB\\JVPTVN`@Fd@@r@AxBAlBH\\L\\R\\ZZpAbApClApCfAvDvAfCdAnAr@bAx@x@r@CXFRLJB"
				+ "@@HFnBAl@Gd@QIO?ODILGNANF`@LLNDN?LIlAxAPR";
		List<Location> locations = decode(encodedPolyline);
		for (Location location : locations) {
			System.out.println(location);
		}
		
		List<Location> originalLocations = new ArrayList<>();
		originalLocations.add(new Location(-120.2, 38.5));
		originalLocations.add(new Location(-120.95, 40.7));
		originalLocations.add(new Location(-126.453, 43.252));
		
		System.out.println(encode(originalLocations));
	}
}
