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
package com.henriquemalheiro.trackit.business.domain;

import java.util.ArrayList;
import java.util.List;

import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.common.Unit;
import com.henriquemalheiro.trackit.business.common.UnitCategory;

public class FieldMetadata {
	String qualifiedName;
	String type;
	UnitCategory unitCategory;
	Unit unit;
	List<FieldGroup> groups;
	String messageCode;
	
	public FieldMetadata(String qualifiedName, String type, UnitCategory unitCategory, Unit unit, String messageCode) {
		this(qualifiedName, type, unitCategory, unit, new ArrayList<FieldGroup>(), messageCode);
	}
	
	public FieldMetadata(String qualifiedName, String type, UnitCategory unitCategory, Unit unit,
			List<FieldGroup> groups, String messageCode) {
		this.qualifiedName = qualifiedName;
		this.type = type;
		this.unitCategory = unitCategory;
		this.unit = unit;
		this.groups = groups;
		this.messageCode = messageCode;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}
	
	public String getClassName() {
		return qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
	}
	
	public String getFieldName() {
		return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
	}

	public String getType() {
		return type;
	}

	public UnitCategory getUnitCategory() {
		return unitCategory;
	}
	
	public Unit getUnit() {
		return unit;
	}
	
	public List<FieldGroup> getGroups() {
		return groups;
	}

	public String getMessageCode() {
		return messageCode;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCode);
	}
}