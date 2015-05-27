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
package com.pg58406.trackit.business.utility;

import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.henriquemalheiro.trackit.business.common.Constants;
import com.henriquemalheiro.trackit.business.common.Messages;
import com.henriquemalheiro.trackit.presentation.utilities.ImageUtilities;

public class AboutDialog extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9186173243769465449L;
	
	public AboutDialog(JFrame parent){
		super(parent, Messages.getMessage("about.about"), true);
		ImageIcon thumb = ImageUtilities.createImageIcon("trackit.png");
		
		JPanel panel = new JPanel();
	    BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
	    panel.setLayout(layout);
	    panel.add(Box.createGlue());
	    JLabel label = new JLabel(thumb);
	    label.setAlignmentX(CENTER_ALIGNMENT);
	    panel.add(label);
	    label = new JLabel(Messages.getMessage("about.about"));
	    label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(16.0f));
	    label.setAlignmentX(CENTER_ALIGNMENT);
	    label.setAlignmentY(BOTTOM_ALIGNMENT);
	    panel.add(label);
	    label = new JLabel(Messages.getMessage("about.version")+Constants.APP_VERSION);
	    label.setAlignmentX(CENTER_ALIGNMENT);
	    label.setAlignmentY(BOTTOM_ALIGNMENT);
	    panel.add(label);	    
	    label = new JLabel(Messages.getMessage("about.copyright.henrique"));
	    label.setAlignmentX(CENTER_ALIGNMENT);
	    label.setAlignmentY(BOTTOM_ALIGNMENT);
	    panel.add(label);    
	    label = new JLabel(Messages.getMessage("about.copyright.pedro"));
	    label.setAlignmentX(CENTER_ALIGNMENT);
	    label.setAlignmentY(BOTTOM_ALIGNMENT);
	    panel.add(label);
	    label = new JLabel(Messages.getMessage("about.copyright.miguel"));
	    label.setAlignmentX(CENTER_ALIGNMENT);
	    label.setAlignmentY(BOTTOM_ALIGNMENT);
	    panel.add(label);
	    panel.add(Box.createGlue());
	    getContentPane().add(panel, "Center");
	    
	    pack();
	    setSize(getWidth() + 150, getHeight() + 20);
	    setLocationRelativeTo(null);
	    setVisible(true);
	}
}
