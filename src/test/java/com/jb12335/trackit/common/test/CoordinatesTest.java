package com.jb12335.trackit.common.test;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import com.trackit.business.domain.CoordinatesFormatter;
import com.trackit.business.domain.CoordinatesType;
import com.trackit.business.domain.GeographicLocation;
import com.trackit.business.domain.UTMLocation;
import com.trackit.presentation.utilities.CoordinatesScale;
import com.trackit.presentation.utilities.LineClipper;
import com.trackit.presentation.utilities.Scale;
import com.trackit.presentation.view.map.layer.GridLayer;

public class CoordinatesTest {

	static CoordinatesFormatter formatter = CoordinatesFormatter.getInstance();
	public static void main(String[] args) {

		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);
		df.setRoundingMode( RoundingMode.HALF_UP);
		for (Number n : Arrays.asList(12, 123.12345, 0.23, 0.1, 2341234.212431324)) {
		    Double d = n.doubleValue();
		    System.out.println(df.format(d));
		}
		displayValue( -8.544444444444443);
		displayValue( -8.799999999999999);
//		System.out.println( formatter.formatAsDecimalDegrees( 37.3) + "\t" + 
//		                    formatter.formatAsDecimalDegrees(-8.3567));
//		System.out.println( formatter.formatLatitude(37.3, 6) + 
//					"\t\t" + formatter.formatLongitude( -8.3567, 6));
		
//		CoordinatesScale.setCoordinatesType( CoordinatesType.DEGREES_DECIMAL_MINUTES);
		CoordinatesScale.setCoordinatesType( CoordinatesType.DECIMAL_DEGREES);
//		CoordinatesScale scale = new CoordinatesScale( 0., .02347469*2, 15);
//		scale = new CoordinatesScale( 0., .02347469, 15);
//		System.out.println( .2 * .1 + "  " + 2 * .01 + "  " + 0.020000000000000004/.1);
//		showScale( new Scale( 0., 0.187798, 15));
//		showScale( new Scale( 38.648845, 38.739817, .020000000	));
//		showScale( new Scale( 0, 0.751190185546875));
//		showScale( new Scale( -2.4, 2.4));
//		showScale( new Scale( .001, .0085));
//		showScale( new Scale( 0, 12.4, 6));
//		showScale( new Scale( 99000, 498000));
//		showScale( new Scale( -.01, .028, 10));
	}
	
	private static void displayValue( double value) {
		String a = formatter.formatCoordinate( value, 0, CoordinatesType.DEGREES_MINUTES_SECONDS);
		String b = formatter.formatCoordinate( value, 1, CoordinatesType.DEGREES_MINUTES_SECONDS);
		System.out.println( value + "  " + a + "  " + b);
	}
	
	private static void showCoordinate( double value) {
		System.out.println( value + ": "
				+ formatter.formatCoordinate(value, 0, CoordinatesType.DECIMAL_DEGREES) + "  "
				+ formatter.formatCoordinate(value, 0, CoordinatesType.DEGREES_DECIMAL_MINUTES) + "  "
				+ formatter.formatCoordinate(value, 0, CoordinatesType.DEGREES_MINUTES_SECONDS));
	}
	
	private static void showScale( Scale scale) {
		System.out.println( scale.toString());
	}
	

}
