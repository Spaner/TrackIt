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
package com.trackit.presentation.view.map;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.trackit.business.common.Formatters;
import com.trackit.business.common.Messages;

class AnimationSlider extends JPanel implements ChangeListener, ActionListener, MouseMotionListener, MouseListener {
	private static final long serialVersionUID = 6816190892030894456L;
	
	private Map map;
	private JSlider animationSlider;
	private JLabel lblMinValue;
	private JLabel lblMaxValue;
	private JButton btnPrevious;
	private JButton btnNext;
	private int minValue;
	private int maxValue;
	
	private final JPopupMenu pop = new JPopupMenu();
    private JMenuItem item = new JMenuItem();
	
	AnimationSlider(Map map, int minValue, int maxValue, int value) {
		this.map = map;
		this.minValue = minValue;
		this.maxValue = maxValue;

		initComponents(minValue, maxValue, value);
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setPreferredSize(new Dimension(600, 600));
		add(lblMinValue);
		add(btnPrevious);
		add(animationSlider);
		add(btnNext);
		add(lblMaxValue);
		
		map.selectTrackpointAtDistance(0.0);
	}

	private void initComponents(int minValue, int maxValue, int value) {
		animationSlider = new JSlider(minValue, maxValue, value);
		animationSlider.addMouseMotionListener(this);
		animationSlider.addMouseListener(this);
		animationSlider.addChangeListener(this);
		
		lblMinValue = new JLabel(Formatters.getFormatedDistance(minValue));
		lblMinValue.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		lblMaxValue = new JLabel(Formatters.getFormatedDistance(maxValue));
		lblMaxValue.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		
		URL imageURL = AnimationSlider.class.getResource("/icons/previous_16.png");
        btnPrevious = new JButton();
        btnPrevious.setActionCommand("Previous");
        btnPrevious.setToolTipText(Messages.getMessage("animationSlider.previous"));
        btnPrevious.addActionListener(this);
        btnPrevious.addMouseListener(new ButtonPressRepeat(-50));
        btnPrevious.setIcon(new ImageIcon(imageURL, "Previous"));
        btnPrevious.setMaximumSize(new Dimension(14, 18));
        
        imageURL = AnimationSlider.class.getResource("/icons/next_16.png");
        btnNext = new JButton();
        btnNext.setActionCommand("Next");
        btnNext.setToolTipText(Messages.getMessage("animationSlider.next"));
        btnNext.addActionListener(this);
        btnNext.addMouseListener(new ButtonPressRepeat(50));
        btnNext.setIcon(new ImageIcon(imageURL, "Next"));
        btnNext.setMaximumSize(new Dimension(14, 18));
        
        initToolTip();
	}

	private void initToolTip() {
		item.setSelected(false);
		item.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		
		pop.add(item);
		pop.setFocusable(false);      
	    pop.setEnabled(false);
		pop.setDoubleBuffered(true);
		pop.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}
	
	private void showToolTip(MouseEvent event) {
		item.setText(Formatters.getFormatedDistance(animationSlider.getValue()));
		int x = event.getX();
		if (x < 0) {
			x = 0;
		}
		pop.show(event.getComponent(), x + 5, -15);
		item.setArmed(false);
    }
 
	@Override
    public void mouseDragged(MouseEvent event) {
    	showToolTip(event);
		validate();//58406
    	map.repaint();
    }
 
	@Override
    public void mouseMoved (MouseEvent event) {
    }
 
	@Override
    public void mousePressed(MouseEvent event) {
    	showToolTip(event);
		validate();//58406
    	map.repaint();
    }
 
	@Override
    public void mouseClicked(MouseEvent event) {
    }
 
	@Override
    public void mouseReleased(MouseEvent me) {
    	pop.setVisible(false);
		validate();//58406
    	map.repaint();
    }
 
	@Override
    public void mouseEntered(MouseEvent event) {
    }
 
	@Override
    public void mouseExited(MouseEvent event) {
    }

	@Override
	public void stateChanged(ChangeEvent event) {
		JSlider source = (JSlider) event.getSource();
		if (source.getValueIsAdjusting()) {
			map.selectTrackpointAtDistance(source.getValue());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int newValue = 0;
		
		if (e.getActionCommand().equals("Previous")) {
			newValue = animationSlider.getValue() - 50;
			newValue = Math.max(minValue, newValue);
			map.selectTrackpointAtDistance(newValue);
		} else if (e.getActionCommand().equals("Next")) {
			newValue = animationSlider.getValue() + 50;
			newValue = Math.min(maxValue, newValue);
			map.selectTrackpointAtDistance(newValue);
		}
		
		animationSlider.setValue(newValue);
		setToolTipText(String.valueOf(newValue));
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		btnPrevious.setEnabled(enabled);
		btnNext.setEnabled(enabled);
		animationSlider.setEnabled(enabled);
	}
	
	private class ButtonPressRepeat extends MouseAdapter {
		private Timer timer;
		private int increment;
		
		ButtonPressRepeat(int increment) {
			this.increment = increment;
		}

		@Override
		public void mousePressed(final MouseEvent event) {
			super.mousePressed(event);
			if (timer == null) {
				timer = new Timer();
			}
			
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					int distance = animationSlider.getValue() + increment;
					distance = Math.max(distance, animationSlider.getMinimum());
					distance = Math.min(distance, animationSlider.getMaximum());
					map.selectTrackpointAtDistance(distance);
					
					animationSlider.setValue(distance);
					setToolTipText(String.valueOf(distance));
				}
			}, 350, 150);
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			super.mouseReleased(event);
			
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}
}
