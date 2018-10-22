/*
 * This file is part of Track It!.
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
package com.trackit.business.utilities;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.trackit.presentation.utilities.ImageUtilities;

public class ZoomScrollPane {

	private JScrollPane scrollPane;
	private double scale;
	private BufferedImage pic;
	private double boundWidth;
	private double boundHeight;
	private double width;
	private double height;
	private int screenWidth;
	private int screenHeight;
	private JLabel picLabel;
	private JPanel picPanel;
	private JFrame picFrame;
	private static ImageIcon icon = ImageUtilities.createImageIcon("photo_pin_16.png");

	public ZoomScrollPane(BufferedImage pic, String title) {
		this.scale = 1.;
		this.pic = pic;
		
		picFrame = new JFrame();
		picFrame.setIconImage(icon.getImage());
		picFrame.setTitle(title);
		picFrame.pack();
		picPanel = new JPanel();
		picLabel = new JLabel();
		
		Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		screenWidth = winSize.width;
		screenHeight = winSize.height - picFrame.getInsets().top - picFrame.getInsets().bottom;
		width = boundWidth = winSize.width * 0.65;
		height = boundHeight = (winSize.height - picFrame.getInsets().top - picFrame.getInsets().bottom) * 0.65;
		setSize();
		picPanel.setLayout(new FlowLayout());
		setPic(pic);

		
		picLabel.setPreferredSize(new Dimension((int)width, (int)height));
		picPanel.add(picLabel, JLabel.CENTER);
		scrollPane = new JScrollPane(picPanel);
		scrollPane.addMouseWheelListener(mwl);
		picFrame.addKeyListener(kl);
		picFrame.add(scrollPane);		
		picFrame.pack();
		centerPic();
		
		java.awt.EventQueue.invokeLater(new Runnable() {

			public void run() {
				picFrame.setVisible(true);
				picFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);				
				//picFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			}
		});
	}
	
	public void setSize() {
		double origPicWidth = pic.getWidth();
		double origPicHeight = pic.getHeight();
		if (origPicWidth >= boundWidth) {
			// scale width to fit
			width = boundWidth;
			// scale height to maintain aspect ratio
			height = (width * origPicHeight) / origPicWidth;
		}

		if (origPicHeight >= boundHeight) {
			// scale height to fit instead
			height = boundHeight;
			// scale width to maintain aspect ratio
			width = (height * origPicWidth) / origPicHeight;
		}
	}

	public void resize() {
		//boolean maximized = picFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH;
		picLabel.removeAll();
		picLabel.setIcon(resizeImage());
		picLabel.setPreferredSize(new Dimension((int) (width * scale),
				(int) (height * scale)));
		picLabel.revalidate();
		picFrame.pack();
		centerPic();
		if (((screenWidth <= (width * scale))
				|| (screenHeight <= (height * scale))))			
			picFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		//picFrame.setLocationRelativeTo(null);
	}
	
	public void centerPic(){
		Rectangle bounds = scrollPane.getViewport().getViewRect();
		Dimension size = scrollPane.getViewport().getViewSize();
		int x = (size.width - bounds.width) / 2;
		int y = (size.height - bounds.height) / 2;
		scrollPane.getViewport().setViewPosition(new Point(x, y));
	}

	public BufferedImage getPic() {
		return pic;
	}

	public void setPic(BufferedImage pic) {
		scale = 1.0;
		picLabel.setIcon(resizeImage());
	}
	
	private ImageIcon resizeImage() {
		int width = (int) (this.width * scale);
		int height = (int) (this.height * scale);

		BufferedImage resized = new BufferedImage(width, height, pic.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(pic, 0, 0, width, height, 0, 0, pic.getWidth(),
				pic.getHeight(), null);
		g.dispose();
		
		return new ImageIcon(resized);
	}

	MouseWheelListener mwl = new MouseWheelListener() {

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			// TODO Auto-generated method stub
			if (e.isControlDown()) {
				int notches = e.getWheelRotation();
				if (notches < 0) {
					if (scale < 2.0)
						scale -= (notches * 0.1);
				} else {
					if (scale > 0.1)
						scale -= (notches * 0.1);
				}
				resize();
			} else {
				e.getComponent().getParent().dispatchEvent(e);
			}
		}

	};
	
	
	KeyListener kl = new KeyListener(){

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			if ((e.getKeyCode() == KeyEvent.VK_1) && e.isAltDown()) {
                scale = 0.5;
                resize();
            }
			if ((e.getKeyCode() == KeyEvent.VK_2) && e.isAltDown()) {
                scale = 1.;
                resize();
            }
			if ((e.getKeyCode() == KeyEvent.VK_3) && e.isAltDown()) {
                scale = 2.0;
                resize();
            }
			
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
}