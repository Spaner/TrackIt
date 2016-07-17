/*
 * This file is part of Track It!.
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
package com.pg58406.trackit.business.utility;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import jxl.Workbook;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelWriter {
	
	private WritableWorkbook workbook;
	private WritableSheet sheet;
	private DateFormat customDateFormat;
	WritableCellFormat dateFormat;
	
	public ExcelWriter(String inputFile){
		customDateFormat = new DateFormat ("hh:mm:ss");
		dateFormat = new WritableCellFormat (customDateFormat);
		try {
			workbook = Workbook.createWorkbook(new File(inputFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sheet = workbook.createSheet("Times", 0); 
	}
	
	public void writeValue(int column, int row, Date time){
		DateTime label = new DateTime(column, row, time, dateFormat, true);
		try {
			sheet.addCell(label);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close(){		
		try {
			workbook.write();
			workbook.close();
		} catch (WriteException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
}
