package com.jb12335.trackit.common.test;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Locale;

import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;

public class FormatTest {
	
	public static void main(String[] args) {
		System.out.println( Locale.getDefault().toString());
		Messages.setLocale( new Locale( "PT", "PT"));
		testFormats();
		Messages.setLocale( new Locale( "EN", "EN"));
		testFormats();
		Messages.setLocale( new Locale( "FR", "FR"));
		testFormats();
		Messages.setLocale( new Locale( "EN", "EN"));
		testFormats();
		Messages.setLocale( new Locale( "PT", "PT"));
		testFormats();
		Messages.setLocale( new Locale( "JP", "JP"));
		testFormats();
		Messages.setLocale( new Locale( "ar_SA", "ar_SA"));
		testFormats();
	}
	
	private static void testFormats() {
//		DateTimeFormatter pattern = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).withLocale(Messages.getLocale());
		Calendar cal = Calendar.getInstance( Messages.getLocale());
		System.out.println( "\n" + Messages.getLocale().toString() + "  " + cal.getFirstDayOfWeek());
//		System.out.println();
//		System.out.println( String.format ( "%s %s %s %s %s %s", 
//				Formatters.getDecimalFormat().format(12.3456),
//				Formatters.getDecimalFormat(3).format(12.3456),
//				Formatters.getDecimalFormat(0).format(12.3456),
//				Formatters.getDecimalFormatWithGrouping().format(12987423.3456),
//				Formatters.getDecimalFormatWithGrouping(3).format(12987423.3456),
//				Formatters.getDecimalFormatWithGrouping(0).format(12987423.3456)));
//		System.out.println( String.format ( "%s %s %s %s %s %s", 
//				Formatters.getDefaultDecimalFormat().format(12.3456),
//				Formatters.getDefaultDecimalFormat(3).format(12.3456),
//				Formatters.getDefaultDecimalFormat(0).format(12.3456),
//				Formatters.getDefaultDecimalFormatWithGrouping().format(12987423.3456),
//				Formatters.getDefaultDecimalFormatWithGrouping(3).format(12987423.3456),
//				Formatters.getDefaultDecimalFormatWithGrouping(0).format(12987423.3456)));
	}
	
}
