package com.trackit.presentation.view.map.provider.bing.elevation;

import java.util.ArrayList;
import java.util.List;

import com.trackit.business.common.Location;

public class PointCompression {
	private static final String codes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
	
	static String compress(List<Location> locations) {
		StringBuilder compressedLocations = new StringBuilder();

		long longitude = 0L;
		long latitude = 0L;
		
		for (Location location : locations) {
			long newLongitude = Math.round(location.getLongitude() * 1E5);
	        long newLatitude = Math.round(location.getLatitude() * 1E5);
	        
	        long longitudeDelta = newLongitude - longitude;
	        long latitudeDelta = newLatitude - latitude;
	        
	        longitude = newLongitude;
	        latitude = newLatitude;

	        longitudeDelta = (longitudeDelta << 1) ^ (longitudeDelta >> 31);
	        latitudeDelta = (latitudeDelta << 1) ^ (latitudeDelta >> 31);
	        
	        long index = ((latitudeDelta + longitudeDelta) * (latitudeDelta + longitudeDelta + 1) / 2) + latitudeDelta;

	        while (index > 0) {
	            long remainder = index & 31;
	            index = (index - remainder) / 32;
	            remainder = index > 0 ? remainder += 32 : remainder;
	            
	            compressedLocations.append(codes.charAt((int) remainder));
	        }
	    }
		
		return compressedLocations.toString();
	}
	
	public static void main(String[] args) {
		List<Location> locations = new ArrayList<>();
		locations.add(new Location(-110.72522000409663, 35.894309002906084));
		locations.add(new Location(-110.72577999904752, 35.893930979073048));
		locations.add(new Location(-110.72606003843248, 35.893744984641671));
		
		System.out.println(compress(locations)); // vx1vilihnM6hR7mE
	}
}
