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
package com.trackit.business.domain;

import com.trackit.business.common.Messages;

public enum GarminProductType implements Product {
	HRM1((short) 1),
	AXH01((short) 2),
	AXB01((short) 3),
	AXB02((short) 4),
	HRM2SS((short) 5),
	DSI_ALF02((short) 6),
	EDGE705((short) 625),
	FR405((short) 717),
	FR50((short) 782),
	FR60((short) 988),
	DSI_ALF01((short) 1011),
	FR310XT((short) 1018),
	EDGE500((short) 1036),
	FR110((short) 1124),
	EDGE800((short) 1169),
	CHIRP((short) 1253),
	EDGE200((short) 1325),
	FR910XT((short) 1328),
	ALF04((short) 1341),
	FR610((short) 1345),
	FR70((short) 1436),
	FR310XT_4T((short) 1446),
	AMX((short) 1461),
	SDM4((short) 10007),
	TRAINING_CENTER((short) 20119),
	CONNECT((short) 65534);
	
	private short value;
	
	private GarminProductType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"garminProductType.hrm1", "garminProductType.axh01", "garminProductType.axb01",
		"garminProductType.axb02", "garminProductType.hrm2ss", "garminProductType.dsi_alf02", "garminProductType.edge705", "garminProductType.fr405",
		"garminProductType.fr50", "garminProductType.fr60", "garminProductType.dsi_alf01", "garminProductType.fr310xt",
		"garminProductType.edge500", "garminProductType.fr110", "garminProductType.edge800", "garminProductType.chirp",
		"garminProductType.edge200", "garminProductType.fr910xt", "garminProductType.alf04", "garminProductType.fr610",
		"garminProductType.fr70", "garminProductType.fr310xt_4t", "garminProductType.amx", "garminProductType.sdm4",
		"garminProductType.training_center", "garminProductType.connect"};
	
	public static GarminProductType lookup(short garminProductValue) {
		for (GarminProductType garminProduct : values()) {
			if (garminProductValue == garminProduct.getValue()) {
				return garminProduct;
			}
		}
		return null;
	}
	
	public static GarminProductType lookup(String productName) {
		for (GarminProductType garminProduct : values()) {
			if (garminProduct.name().equalsIgnoreCase(productName)) {
				return garminProduct;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(Messages.getMessage(messageCodes[this.ordinal()]));
		builder.append(" (").append(Messages.getMessage("garminProductType.productId")).append(": ");
		builder.append(value).append(")");
		
		return builder.toString();
	}
}