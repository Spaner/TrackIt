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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class MergeTiles {

	public static void main(String[] args) throws IOException {
		int rows = 2;  
        int cols = 7;  
  
        int chunkWidth, chunkHeight;  
        int type;
        
        File[][] imgFiles = new File[rows][cols];  
        for (int i = 0; i < rows; i++) {  
        	for (int j = 0; j < cols; j++) {  
        		imgFiles[i][j] = new File("/Users/ga/Sandbox/MilitaryMaps/431_" + (i + 6) + "_" + j + ".jpg");
        	}
        }  
  
        BufferedImage[][] buffImages = new BufferedImage[rows][cols];
        for (int i = 0; i < rows; i++) {  
        	for (int j = 0; j < cols; j++) {
        		buffImages[i][j] = ImageIO.read(imgFiles[i][j]);
        	}
        }
        	
        type = buffImages[0][0].getType();  
        chunkWidth = buffImages[0][0].getWidth();  
        chunkHeight = buffImages[0][0].getHeight();  
  
        BufferedImage finalImg = new BufferedImage(chunkWidth * cols, chunkHeight * rows, type);  
  
        for (int i = 0; i < rows; i++) {  
        	for (int j = 0; j < cols; j++) {
                finalImg.createGraphics().drawImage(buffImages[i][j], chunkWidth * j, chunkHeight * i, null);  
            }  
        }  
        System.out.println("Image concatenated.....");  
        ImageIO.write(finalImg, "jpeg", new File("/Users/ga/Desktop/431_new.jpg"));  
	}
}
