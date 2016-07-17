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
package com.henriquemalheiro.trackit.presentation.view.map.provider.bingmaps.routes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="Response")
public class Response {
	
	@XmlTransient
	private String xsi;
	
	@XmlTransient
	private String xsd;
	
	@XmlTransient
	private String xmlns;
	
	private String copyright;
	private String brandLogoUri;
	private int statusCode;
	private String statusDescription;
	private String authenticationResultCode;
	private String traceId;
	private List<ResourceSet> resourceSets;
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="Copyright")
	public String getCopyright() {
		return copyright;
	}
	
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="BrandLogoUri")
	public String getBrandLogoUri() {
		return brandLogoUri;
	}

	public void setBrandLogoUri(String brandLogoUri) {
		this.brandLogoUri = brandLogoUri;
	}
	
    @XmlElement(name="StatusCode", namespace="http://schemas.microsoft.com/search/local/ws/rest/v1")
	public int getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="StatusDescription")
	public String getStatusDescription() {
		return statusDescription;
	}
	
	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="AuthenticationResultCode")
	public String getAuthenticationResultCode() {
		return authenticationResultCode;
	}
	
	public void setAuthenticationResultCode(String authenticationResultCode) {
		this.authenticationResultCode = authenticationResultCode;
	}
	
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="TraceId")
	public String getTraceId() {
		return traceId;
	}
	
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	
	@XmlElementWrapper(name="ResourceSets")
	@XmlElement(name="ResourceSet")
	public List<ResourceSet> getResourceSets() {
		if (resourceSets == null) {
			resourceSets = new ArrayList<ResourceSet>();
		}
		
		return resourceSets;
	}
	
	public void setResourceSets(List<ResourceSet> resourceSets) {
		this.resourceSets = resourceSets;
	}

	@Override
	public String toString() {
		return "Response [copyright=" + copyright + ", brandLogoUri="
				+ brandLogoUri + ", statusCode=" + statusCode
				+ ", statusDescription=" + statusDescription
				+ ", authenticationResultCode=" + authenticationResultCode
				+ ", traceId=" + traceId + ", resourceSets=" + resourceSets
				+ "]";
	}
}