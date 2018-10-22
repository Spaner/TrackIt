package com.trackit.business.domain;

import java.util.Calendar;
import java.util.Date;

import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;

public class CalendarInfo {
	public static CalendarInfo voidMonth       = new CalendarInfo( 0, 0, 0, true);
	public static CalendarInfo voidDay         = new CalendarInfo( 0, 0, 0, false);
//	public static String       voidMonthString = voidMonth.toString();
//	public static String       voidDayString   = voidDay.toString();
	
	private float   _distance;
	private float   _time;
	private int     _numberOf;
	private boolean _isMonth;
	
	public CalendarInfo(float totalDistance, float totalTime, int numberOf, boolean isMonth){
		_distance = totalDistance;
		_time     = totalTime;
		_numberOf = numberOf;
		_isMonth  = isMonth;
	}
	
	public float getDistance(){
		return _distance;
	}
	
	public float getTime(){
		return _time;
	}
	
	public int getNumber(){
		return _numberOf;
	}
	
	public String getTimeString(){
//		long time = (long) _time;							//12335: 2018-02-28
//		long s = time % 60;
//		long m = (time / 60) % 60;
//		long h = (time / (60 * 60)) % 24;
//		return String.format("%d:%02d:%02d", h,m,s);
		return Formatters.getFormatedDuration( _time);
	}
	
	public String getDistanceString(){
//		return new String(String.valueOf(_distance/1000));	//12335: 2018-02-28
		return Formatters.getFormatedDistance( _distance);
	}

//12335: 2018-03-04 - superseeded by toString()
//	public String getFinalString(int aux){
//		String mes;
//		if(aux==0){
//			mes = Messages.getMessage("calendarInfo.monthlyStats");
//		}
//		else{
//			mes = Messages.getMessage("calendarInfo.dailyStats");
//		}
//		String result = "<html><div style='text-align: center;'>" + mes + "</div><br>"+
//						"<table align='left;'> <tr> <th>"+ Messages.getMessage("calendarInfo.distance") + "</th>" +
//						"<td>" + getDistanceString() + "</td> <tr> <th>" + Messages.getMessage("calendarInfo.time") +
//						"</th> <td>" + getTimeString() + "</td> </tr> <tr> <th>" + Messages.getMessage("calendarInfo.numberOf")+
//						"</th> <td>" + getNumber() +  "</td> </tr> </table>";
//						return result;
//	}
	
	//12335: 2018-03-04
	public String toString() {
		String calendarInfoString;
		calendarInfoString = Messages.getMessage( "calendarInfo.heading",
				_isMonth ? Messages.getMessage( "calendarInfo.monthlyStats") :
						   Messages.getMessage("calendarInfo.dailyStats"));
		if ( _numberOf != 0 )
			calendarInfoString += Messages.getMessage( "calendarInfo.message.withTracks",
					Messages.getMessage( "calendarInfo.distance"), getDistanceString(),
					Messages.getMessage( "calendarInfo.time"), 	   getTimeString(),
					Messages.getMessage( "calendarInfo.numberOf"), getNumber());
		else
			if ( _isMonth )
				calendarInfoString += Messages.getMessage( "calendarInfo.message.noTracks",
						Messages.getMessage( "calendarInfo.noTracks.month.line1"),
						Messages.getMessage( "calendarInfo.noTracks.month.line2"),
						Messages.getMessage( "calendarInfo.noTracks.month.line3"));
			else
				calendarInfoString += Messages.getMessage( "calendarInfo.message.noTracks",
						Messages.getMessage( "calendarInfo.noTracks.day.line1"),
						Messages.getMessage( "calendarInfo.noTracks.day.line2"),
						Messages.getMessage( "calendarInfo.noTracks.day.line3"));
		return calendarInfoString;
	}
	
	
}
