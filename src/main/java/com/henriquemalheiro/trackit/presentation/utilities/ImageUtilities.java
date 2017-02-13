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
package com.henriquemalheiro.trackit.presentation.utilities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.net.URL;

import javax.swing.ImageIcon;

import com.henriquemalheiro.trackit.presentation.ApplicationPanel;

public class ImageUtilities {
	public static ImageIcon createImageIcon(String iconName) {
    	URL imgURL = ApplicationPanel.class.getResource("/icons/" + iconName);
    	if (imgURL != null) {
    		return new ImageIcon(imgURL);
    	} else {
    		return null;
    	}
    }
	
	public static ImageIcon createImageIcon(String folder, String iconName) {
    	URL imgURL = ApplicationPanel.class.getResource("/icons/" + folder + "/" + iconName);
    	if (imgURL != null) {
    		return new ImageIcon(imgURL);
    	} else {
    		return null;
    	}
    }
	
	public static BufferedImage toBufferedImage(ImageIcon icon) {
		BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufferedImage.createGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		
		return bufferedImage;
	}
	
	public synchronized static BufferedImage combineImages(Image image1, Image image2) {
		return combineImages(toBufferedImage(image1), toBufferedImage(image2), 1.0f);
	}
	
	private static BufferedImage toBufferedImage(Image img) {
	    if (img instanceof BufferedImage) {
	        return (BufferedImage) img;
	    }

	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();
	    
	    return bimage;
	}
	
	public synchronized static BufferedImage combineImages(BufferedImage image1, BufferedImage image2) {
		return combineImages(image1, image2, 1.0f);
	}
	
	public synchronized static BufferedImage combineImages(BufferedImage image1, BufferedImage image2, float alpha) {
		int width = Math.max(image1.getWidth(), image2.getWidth());
		int height = Math.max(image1.getHeight(), image2.getHeight());

		BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) combinedImage.getGraphics();
		
//		int rule = AlphaComposite.SRC_OVER;
//		alpha = (alpha > 1.0f ? 1.0f : alpha);
//        Composite comp = AlphaComposite.getInstance(rule, alpha);
//        g.setComposite(comp );
        
        g.drawImage(image1, 0, 0, null);
		g.drawImage(image2, 0, 0, null);
		
		g.dispose();
		
		return combinedImage;
	}
	
	public static BufferedImage resize(Image image, int width, int height) {
	    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
	    Graphics2D g2d = (Graphics2D) bi.createGraphics();
	    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    
	    double scaleX = (double) width / image.getWidth(null);
	    double scaleY = (double) height / image.getHeight(null);
	    
	    AffineTransform xform = AffineTransform.getScaleInstance(scaleX, scaleY);
	    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	    g2d.drawImage(image, xform, null);
	    g2d.dispose();
	    
	    return bi;
	}
	
	public static Color applyTransparency(Color color, float alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * alpha));
	}
	
	public static BufferedImage makeColorTransparent(final BufferedImage im, final Color color) {
		final ImageFilter filter = new RGBImageFilter() {
			
			// the color we are looking for (white)... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFFFFFFFF;
			
			public final int filterRGB(final int x, final int y, final int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					
					// nothing to do
					return rgb;
				}
			}
		};
		
		final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		Image image = Toolkit.getDefaultToolkit().createImage(ip);
		
		return imageToBufferedImage(image);
	}
	
	public static BufferedImage TransformColorToTransparency(BufferedImage image, Color c1, Color c2) {
		
		// Primitive test, just an example
		final int r1 = c1.getRed();
		final int g1 = c1.getGreen();
		final int b1 = c1.getBlue();
		final int r2 = c2.getRed();
		final int g2 = c2.getGreen();
		final int b2 = c2.getBlue();
		
		ImageFilter filter = new RGBImageFilter() {
			public final int filterRGB(int x, int y, int rgb) {
				int r = (rgb & 0xFF0000) >> 16;
				int g = (rgb & 0xFF00) >> 8;
				int b = rgb & 0xFF;
				if (r >= r1 && r <= r2 && g >= g1 && g <= g2 && b >= b1 && b <= b2) {
					
					// Set fully transparent but keep color
					return rgb & 0xFFFFFF;
				}
				return rgb;
			}
		};

		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
		Image finalImage = Toolkit.getDefaultToolkit().createImage(ip);
		
		return imageToBufferedImage(finalImage);
	}
	
	private static BufferedImage imageToBufferedImage(final Image image) {
		final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = bufferedImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		
		return bufferedImage;
	}  
}
