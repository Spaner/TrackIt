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

import java.util.Date;

import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;

public class ActivityMetadata extends TrackItBaseType {
	private RecordingType recordingType;
	private ManufacturerType manufacturer;
	private Product product;
	private Long serialNumber;
	private Date timeCreated;
	private Integer softwareVersion;
	private Short hardwareVersion;
	
	public ActivityMetadata() {
		recordingType = RecordingType.ONE_SECOND_RECORDING;
	}

	public RecordingType getRecordingType() {
		return recordingType;
	}

	public void setRecordingType(RecordingType recordingType) {
		this.recordingType = recordingType;
	}

	public ManufacturerType getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(ManufacturerType manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Long getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Long serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	public Integer getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(Integer softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public Short getHardwareVersion() {
		return hardwareVersion;
	}

	public void setHardwareVersion(Short hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((timeCreated == null) ? 0 : timeCreated.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityMetadata other = (ActivityMetadata) obj;
		if (timeCreated == null) {
			if (other.timeCreated != null)
				return false;
		} else if (!timeCreated.equals(other.timeCreated))
			return false;
		return true;
	}
	
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.NOTHING_SELECTED, this);
	}
}
