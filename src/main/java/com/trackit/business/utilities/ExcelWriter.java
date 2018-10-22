/*
 * This file is part of Track It!.
 * Copyright (C) 2015 Pedro Gomes
 * Copyright (C) 2016 J M Brisson Lopes
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
import java.io.IOException;
import java.util.Date;

import jxl.Workbook;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelWriter {
	
	private WritableWorkbook workbook;
	private WritableSheet sheet;
	private DateFormat customDateFormat;
	WritableCellFormat dateFormat;
	private static WritableCellFormat coordinatesFormat =		//12335: 2016-09-29
										new WritableCellFormat( new NumberFormat( "#0.00000000"));
	
	public ExcelWriter(String inputFile){
//		customDateFormat = new DateFormat ("hh:mm:ss");			//12335: 2016-09-29
		customDateFormat = new DateFormat ("HH:mm:ss");
		dateFormat = new WritableCellFormat (customDateFormat);
		try {
			workbook = Workbook.createWorkbook(new File(inputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		sheet = workbook.createSheet("Times", 0); 
	}
	
	public void writeValue(int column, int row, Date time){
		DateTime label = new DateTime(column, row, time, dateFormat, true);
		try {
			sheet.addCell(label);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	//12335: 2016-09-29
	public void writeString( int column, int row, String string) {
		try {
			Label txt = new Label( column, row, string);
			sheet.addCell( txt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//12335: 2016-09-29
	public void writeDouble( int column, int row, double value) {
		try {
			Number val = new Number( column, row, value);
			sheet.addCell( val);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//12335: 2016-09-29
	public void writeCoordinate( int column, int row, double value) {
		try {
			Number val = new Number( column, row, value, coordinatesFormat);
			sheet.addCell( val);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close(){		
		try {
			workbook.write();
			workbook.close();
		} catch (WriteException | IOException e) {
			e.printStackTrace();
		}
	}

	
	
	
}
