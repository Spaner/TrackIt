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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.business.exception.TrackItException;
import com.henriquemalheiro.trackit.presentation.event.Event;
import com.henriquemalheiro.trackit.presentation.event.EventManager;
import com.henriquemalheiro.trackit.presentation.event.EventPublisher;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;
import com.henriquemalheiro.trackit.presentation.view.folder.FolderTreeItem;

public class DeviceInfo extends TrackItBaseType implements DocumentItem, FolderTreeItem {
	private static ImageIcon icon = ImageUtilities.createImageIcon("device_16.png");
	
	private Date time;
	private Short deviceIndex;
	private DeviceTypeType deviceType;
	private ManufacturerType manufacturer;
	private Long serialNumber;
	private Product product;
	private Float softwareVersion;
	private Short hardwareVersion;
	private Long cummulativeOperatingTime;
	private Float batteryVoltage;
	private BatteryStatusType batteryStatus;
	private Activity parent;
	
	public DeviceInfo(Activity parent) {
		super();
		this.parent = parent;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Short getDeviceIndex() {
		return deviceIndex;
	}

	public void setDeviceIndex(Short deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	public DeviceTypeType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceTypeType deviceType) {
		this.deviceType = deviceType;
	}

	public ManufacturerType getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(ManufacturerType manufacturer) {
		this.manufacturer = manufacturer;
	}

	public Long getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Long serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Float getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(Float softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public Short getHardwareVersion() {
		return hardwareVersion;
	}

	public void setHardwareVersion(Short hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}

	public Long getCummulativeOperatingTime() {
		return cummulativeOperatingTime;
	}

	public void setCummulativeOperatingTime(Long cummulativeOperatingTime) {
		this.cummulativeOperatingTime = cummulativeOperatingTime;
	}

	public Float getBatteryVoltage() {
		return batteryVoltage;
	}

	public void setBatteryVoltage(Float batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
	}

	public BatteryStatusType getBatteryStatus() {
		return batteryStatus;
	}

	public void setBatteryStatus(BatteryStatusType batteryStatus) {
		this.batteryStatus = batteryStatus;
	}
	
	public Trackpoint getTrackpoint() {
		List<Trackpoint> trackpoints = parent.getTrackpoints(getTime(), getTime());
		
		if (trackpoints != null && !trackpoints.isEmpty()) {
			return trackpoints.get(0);
		} else {
			return null;
		}
	}
	
	@Override
	public List<Trackpoint> getTrackpoints() {
		return Arrays.asList(new Trackpoint[] { getTrackpoint() });
	}

	public Activity getParent() {
		return parent;
	}

	public static ImageIcon getIcon() {
		return icon;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		DeviceInfo other = (DeviceInfo) obj;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String getFolderTreeItemName() {
		return Messages.getMessage("folderView.label.deviceId", getDeviceIndex());
	}

	@Override
	public ImageIcon getOpenIcon() {
		return icon;
	}

	@Override
	public ImageIcon getClosedIcon() {
		return icon;
	}

	@Override
	public ImageIcon getLeafIcon() {
		return icon;
	}

	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		EventManager.getInstance().publish(publisher, Event.DEVICE_SELECTED, this);
	}
	
	@Override
	public void accept(Visitor visitor) throws TrackItException {
		visitor.visit(this);
	}
}
