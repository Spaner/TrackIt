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
package com.trackit.presentation.view.summary;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.trackit.business.domain.FieldGroup;

class DisplayValue {
	private FieldGroup group;
	private String fieldName;
	private JLabel label;
	private JTextField value;
	
	DisplayValue(FieldGroup group, String fieldName, JLabel label, JTextField value) {
		this.group = group;
		this.fieldName = fieldName;
		this.label = label;
		this.value = value;
	}

	FieldGroup getGroup() {
		return group;
	}

	String getFieldName() {
		return fieldName;
	}

	JLabel getLabel() {
		return label;
	}

	JTextField getValue() {
		return value;
	}
}