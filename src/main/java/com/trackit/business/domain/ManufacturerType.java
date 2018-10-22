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

public enum ManufacturerType {
	GARMIN((short) 1),
	GARMIN_FR405_ANTFS((short) 2),
	ZEPHYR((short) 3),
	DAYTON((short) 4),
	IDT((short) 5),
	SRM((short) 6),
	QUARQ((short) 7),
	IBIKE((short) 8),
	SARIS((short) 9),
	SPARK_HK((short) 10),
	TANITA((short) 11),
	ECHOWELL((short) 12),
	DYNASTREAM_OEM((short) 13),
	NAUTILUS((short) 14),
	DYNASTREAM((short) 15),
	TIMEX((short) 16),
	METRIGEAR((short) 17),
	XELIC((short) 18),
	BEURER((short) 19),
	CARDIOSPORT((short) 20),
	A_AND_D((short) 21),
	HMM((short) 22),
	SUUNTO((short) 23),
	THITA_ELEKTRONIK((short) 24),
	GPULSE((short) 25),
	CLEAN_MOBILE((short) 26),
	PEDAL_BRAIN((short) 27),
	PEAKSWARE((short) 28),
	SAXONAR((short) 29),
	LEMOND_FITNESS((short) 30),
	DEXCOM((short) 31),
	WAHOO_FITNESS((short) 32),
	OCTANE_FITNESS((short) 33),
	ARCHINOETICS((short) 34),
	THE_HURT_BOX((short) 35),
	CITIZEN_SYSTEMS((short) 36),
	MAGELLAN((short) 37),
	OSYNCE((short) 38),
	HOLUX((short) 39),
	CONCEPT2((short) 40),
	ONE_GIANT_LEAP((short) 42),
	ACE_SENSOR((short) 43),
	BRIM_BROTHERS((short) 44),
	XPLOVA((short) 45),
	PERCEPTION_DIGITAL((short) 46),
	BF1SYSTEMS((short) 47),
	PIONEER((short) 48),
	SPANTEC((short) 49),
	METALOGICS((short) 50),
	_4IIIIS((short) 51),
	SEIKO_EPSON((short) 52),
	SEIKO_EPSON_OEM((short) 53),
	IFOR_POWELL((short) 54),
	MAXWELL_GUIDER((short) 55),
	STAR_TRAC((short) 56),
	BREAKAWAY((short) 57),
	ALATECH_TECHNOLOGY_LTD((short) 58),
	ROTOR((short) 60),
	GEONAUTE((short) 61),
	ID_BIKE((short) 62),
	SPECIALIZED((short) 63),
	WTEK((short) 64),
	PHYSICAL_ENTERPRISES((short) 65),
	NORTH_POLE_ENGINEERING((short) 66),
	DEVELOPMENT((short) 255),
	ACTIGRAPHCORP((short) 5759);
	
	private short value;
	
	private ManufacturerType(short value) {
		this.value = value;
	}
	
	public short getValue() {
		return value;
	}
	
	private static final String[] messageCodes = {"manufacturerType.garmin", "manufacturerType.garmin_fr_antfs", "manufacturerType.zephyr",
		"manufacturerType.dayton", "manufacturerType.idt", "manufacturerType.srm", "manufacturerType.quarq", "manufacturerType.ibike",
		"manufacturerType.saris", "manufacturerType.spark_hk", "manufacturerType.tanita", "manufacturerType.echowell",
		"manufacturerType.dynastream_oem", "manufacturerType.nautilus", "manufacturerType.dynastream", "manufacturerType.timex",
		"manufacturerType.metrigear", "manufacturerType.xelic", "manufacturerType.beurer", "manufacturerType.cardiosport",
		"manufacturerType.a_and_d", "manufacturerType.hmm", "manufacturerType.suunto", "manufacturerType.thita_elektronik",
		"manufacturerType.gpulse", "manufacturerType.clean_mobile", "manufacturerType.pedal_brain", "manufacturerType.peaksware",
		"manufacturerType.saxonar", "manufacturerType.lemond_fitness", "manufacturerType.dexcom", "manufacturerType.wahoo_fitness",
		"manufacturerType.octane_fitness", "manufacturerType.archinoetics", "manufacturerType.the_hurt_box", "manufacturerType.citizen_systems",
		"manufacturerType.magellan", "manufacturerType.osynce", "manufacturerType.holux", "manufacturerType.concept2",
		"manufacturerType.one_giant_leap", "manufacturerType.ace_sensor", "manufacturerType.brim_brothers", "manufacturerType.xplova",
		"manufacturerType.perception_digital", "manufacturerType.bfsystems", "manufacturerType.pioneer", "manufacturerType.spantec",
		"manufacturerType.metalogics", "manufacturerType.4iiiis", "manufacturerType.seiko_epson", "manufacturerType.seiko_epson_oem",
		"manufacturerType.ifor_powell", "manufacturerType.maxwell_guider", "manufacturerType.star_trac", "manufacturerType.breakaway",
		"manufacturerType.alatech_technology_ltd", "manufacturerType.rotor", "manufacturerType.geonaute", "manufacturerType.id_bike",
		"manufacturerType.specialized", "manufacturerType.wtek", "manufacturerType.physical_enterprises",
		"manufacturerType.north_pole_engineering", "manufacturerType.development", "manufacturerType.actigraphcorp"};
	
	public static ManufacturerType lookup(short manufacturerValue) {
		for (ManufacturerType manufacturer : values()) {
			if (manufacturerValue == manufacturer.getValue()) {
				return manufacturer;
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return Messages.getMessage(messageCodes[this.ordinal()]);
	}
}