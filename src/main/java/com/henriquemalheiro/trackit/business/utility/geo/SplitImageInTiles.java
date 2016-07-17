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
package com.henriquemalheiro.trackit.business.utility.geo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.henriquemalheiro.trackit.business.utility.Utilities;

public class SplitImageInTiles {

	public static void main(String[] args) throws IOException {  

		File file = new File("/Users/ga/Desktop/Military Maps/431.jpg");  
		FileInputStream fis = new FileInputStream(file);  
		BufferedImage image = ImageIO.read(fis);  

		int rows = 8;  
		int cols = 8;  

		int chunkWidth = image.getWidth() / cols;  
		int chunkHeight = image.getHeight() / rows;  
		BufferedImage img = null;  
		for (int x = 1; x <= rows; x++) {  
			for (int y = 1; y <= cols; y++) {  
				img = new BufferedImage(chunkWidth, chunkHeight, image.getType());  

				Graphics2D gr = img.createGraphics();  
				gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);  
				gr.dispose();
				
				file = new File("/Users/ga/Sandbox/MilitaryMaps/431/" + Utilities.pad(String.valueOf(y), '0', 2, Utilities.LEFT_PAD) + "_"
							+ Utilities.pad(String.valueOf(x), '0', 2, Utilities.LEFT_PAD) + ".jpg");
				file.mkdirs();
				
				ImageIO.write(img, "jpg", file);  
			}  
		}
		
		System.out.println("Splitting done");  
		System.out.println("Mini images created");  
	}  
}