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
package com.henriquemalheiro.trackit.presentation.view.data;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.henriquemalheiro.trackit.business.common.Formatters;
import com.henriquemalheiro.trackit.business.domain.DocumentItem;
import com.henriquemalheiro.trackit.business.domain.FieldMetadata;
import com.henriquemalheiro.trackit.business.domain.TrackItBaseType;

public class DataViewTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1255980896105364223L;

	private DataType dataType;
	private List<? extends DocumentItem> displayedElements;
	
	DataViewTableModel(DataType dataType, List<? extends DocumentItem> displayedElements) {
		this.displayedElements = displayedElements;
		this.dataType = dataType;
		init();
	}
	
	private void init() {
	}

	@Override
	public String getColumnName(int col) {
		return dataType.getFieldsMetadata()[col].toString();
    }
	
	@Override
    public int getRowCount() {
		return (displayedElements != null ? displayedElements.size() : 0);
    }
    
	@Override
    public int getColumnCount() {
		return (dataType != null ? dataType.getFieldsMetadata().length : 0);
	}
	
	@Override
    public Object getValueAt(int row, int col) {
		DocumentItem rowElement = displayedElements.get(row);
		FieldMetadata metadata = TrackItBaseType.getMetadata(rowElement.getClass().getName(),
				dataType.getFieldsMetadata()[col].getFieldName());
		
		return Formatters.getFormatedValue(rowElement.get(metadata.getFieldName()), metadata);
    }

	@Override
    public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	@Override
    public void setValueAt(Object value, int row, int col) {
		displayedElements.get(row).set(dataType.getFieldsMetadata()[col].getFieldName(), value);
		fireTableCellUpdated(row, col);
    }

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}
}
