package com.trackit.business.utilities;

import java.text.Normalizer;
import java.util.Comparator;

public class DiacriticalStringComparator implements Comparator<String> {
	@Override
	public int compare( String str1, String str2) {
		return toAscii(str1).compareToIgnoreCase(toAscii(str2));
	}
	
	private static String toAscii( String string) {
		return Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll( "\\p{M}", "");
	}
}
