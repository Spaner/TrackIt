package com.jb12335.trackit.common.test;

import com.trackit.business.domain.CoordinatesType;
import com.trackit.presentation.utilities.CoordinatesScale;

public class GridTesting {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		CoordinatesScale.setCoordinatesType( CoordinatesType.DEGREES_DECIMAL_MINUTES);
		CoordinatesScale.setCoordinatesType( CoordinatesType.DEGREES_MINUTES_SECONDS);
		CoordinatesScale scale = new CoordinatesScale( -8.37, -8.332, 12);
		System.out.println( scale.toString());
		System.out.println( "Digits: " + scale.getNoDecimalDigits());
		scale = new CoordinatesScale( -8.3327,  -8.332, 12);
		System.out.println( scale.toString());
		System.out.println( "Digits: " + scale.getNoDecimalDigits());
	}

}
