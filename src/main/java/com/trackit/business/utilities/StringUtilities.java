package com.trackit.business.utilities;

import java.io.File;
import java.util.ArrayList;

public class StringUtilities {
	
	public static final int LEFT_PAD = -1;
	public static final int RIGHT_PAD = 1;

	public static String regularPathSeparator() {
		//12335: 2018-07-05 - A reverse slash before the separator character ensures that the regular expression
		//					  does not fail under Windows
		return "(?<=" + "\\" + File.separator + ")";
	}
	
	public static String regularSpaceSeparator() {
		return "(?<= )";
	}
	
	public static String regularCommaSeparator() {
		return "(?<=,)";
	}
	
	public static String wrapString( String stringToCut, int maxWidth, String lineBreaker) {
		if ( stringToCut != null ) {
			if ( stringToCut.length() > maxWidth) {
				ArrayList<String> cutString = new ArrayList<>();
				int width = maxWidth;
				if ( width <= 0 )
					width = 80;
				cutString = splitString( stringToCut, regularSpaceSeparator(), false);
				ArrayList<String> recutString = new ArrayList<>();
				for (String str : cutString) 
					recutString.addAll( splitString( str, regularPathSeparator(), false));
				return makeConnectedLine( recutString, lineBreaker, recutString.size(), width);
			} else
				return stringToCut;
		}
		return null;
	}
	
	public static String breakPathString( String pathToBreak, int maxWidth, String lineBreaker) {
//		if ( pathToBreak!= null ) {
//			int width = maxWidth;
//			if ( width <= 0 )
//				width = 80;
//			if ( pathToBreak.length() > width ) {
//				ArrayList<String> cutString = splitString( pathToBreak, regularPathSeparator(), false);
//				return makeConnectedLine( cutString, lineBreaker, cutString.size()-1, width);
//			} else
//				return pathToBreak;
//		}
//		return null;
		return breakFilePath( pathToBreak, maxWidth, lineBreaker, false);
	}
	
	private static String breakFilePath( String pathToBreak, int maxWidth, String lineBreaker, boolean includeName) {
		if ( pathToBreak!= null ) {
			int width = maxWidth;
			if ( width <= 0 )
				width = 80;
			if ( pathToBreak.length() > width ) {
				ArrayList<String> cutString = splitString( pathToBreak, regularPathSeparator(), false);
				int elementsToKeep = cutString.size() - ( includeName ? 0: 1);
				return makeConnectedLine( cutString, lineBreaker, elementsToKeep, width);
			} else
				return pathToBreak;
		}
		return null;
	}

	public static ArrayList<String> splitString( String stringToSplit, String separator, boolean trim) {
		if ( stringToSplit != null && stringToSplit.length() > 0 ) {
			String localSeparator = separator;
			if ( separator == null || separator.length() == 0 )
				localSeparator = "\\s* \\s*";
			ArrayList<String> splitString = new ArrayList<>();
			String[] splitLine = stringToSplit.split( localSeparator);
			for( int i=0; i<splitLine.length; i++) {
				if ( !(splitLine[i] == null) || splitLine[i].length() != 0 )
					splitString.add( trim ? splitLine[i].trim(): splitLine[i]);
			}
			return splitString;
		}
		else
			return null;
	}
	
	public static String makeConnectedLine( ArrayList<String> strings, String connector,
			          						int noToConnect, int maxWidth) {
		String connectedString = "";
		if ( strings != null && strings.size() > 0 && noToConnect > 0 ) {
			int noToProcess = Math.min( noToConnect, strings.size());
			connectedString = strings.get( 0);
			int size        = connectedString.length();
			for( int i=1; i<noToProcess; i++) {
				if ( maxWidth > 0 ) {
					int length = strings.get( i).length();
					if ( size + length > maxWidth ) {
						connectedString += connector;
						size             = length;
					} else
						size += length;
				} else
					connectedString += connector;
				connectedString += strings.get( i);
			}
		}
		return connectedString;
	}

	//12335: 3018-09-10 - relocated here from busines.utilities.Utilities
	public static String pad(String text, char pad, int size, int direction) {
		StringBuffer sb = new StringBuffer();
		
		if (direction == LEFT_PAD) {
			for (int i = 0; i < size - text.length(); i++) {
				sb.append(pad);
			}
			sb.append(text);
		} else if (direction == RIGHT_PAD) {
			sb.append(text);
			for (int i = 0; i < size - text.length(); i++) {
				sb.append(pad);
			}
		} else {
			sb.append(text);
		}
		
		return sb.toString();
	}
}
