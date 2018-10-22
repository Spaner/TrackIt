package com.trackit.business.domain;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Map;

import com.trackit.business.common.BoundingBox2;
import com.trackit.business.common.FileType;
import com.trackit.business.common.Location;
import com.trackit.business.exception.TrackItException;
import com.trackit.business.operation.ConsolidationLevel;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.task.ActionType;
import com.trackit.presentation.view.data.DataType;
import com.trackit.presentation.view.map.layer.MapLayer;

public class BoundingBox extends TrackItBaseType implements DocumentItem{
	double _maxLat;
	double _minLat;
	double _maxLon;
	double _minLon;
	int _source = 0;
	private double minLongitude = -181.0;
	private double maxLongitude = 181.0;
	private double minLatitude = -91.0;
	private double maxLatitude = 91.0;
	
	/*double minLongitude = 180.0;
	double minLatitude = 90.0;
	double maxLongitude = -180.0;
	double maxLatitude = -90.0;*/
	
	public BoundingBox(double maxLat, double minLat, double maxLon, double minLon){
		_maxLat = maxLat;
		_minLat = minLat;
		_maxLon = maxLon;
		_minLon = minLon;
	}
	
	public BoundingBox(){
		_maxLat = maxLatitude;
		_minLat = minLatitude;
		_maxLon = maxLongitude;
		_minLon = minLongitude;
	}
	
	public double getMaxLat(){
		return _maxLat; 
	}
	
	public double getMinLat(){
		return _minLat; 
	}
	
	public double getMaxLon(){
		return _maxLon; 
	}
	
	public double getMinLon(){
		return _minLon; 
	}
	
	public void setMaxLat(double maxLat){
		_maxLat = maxLat;
	}
	
	public void setMaxLon(double maxLon){
		_maxLon = maxLon;
	}
	
	public void setMinLat(double minLat){
		_minLat = minLat;
	}
	
	public void setMinLon(double minLon){
		_minLon = minLon;
	}
	
	@Override
	public void publishSelectionEvent(EventPublisher publisher) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accept(Visitor visitor) throws TrackItException {
		// TODO Auto-generated method stub
		visitor.visit(this);
	}

	
	

}
