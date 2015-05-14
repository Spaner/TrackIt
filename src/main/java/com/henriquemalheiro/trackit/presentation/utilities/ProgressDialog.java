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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressDialog extends JDialog {
	private static final long serialVersionUID = 5837477125742709168L;
	private static final int PROGRESS_BAR_WIDTH = 300;

    private JProgressBar progressBar;
    private JLabel lblMessage;

    public ProgressDialog(JFrame parent, String message) {
        super(parent);
        init(message);
    }

    public void setMessage(String message) {
        lblMessage.setText(message);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setIndeterminate(false);
        }
        super.setVisible(visible);
    }

    private void init(String message) {
        setupControls();
        setupComponent();
        setMessage(message);
    }

    private void setupControls() {
        progressBar = new JProgressBar();
        Dimension preferredSize = progressBar.getPreferredSize();
        preferredSize.width = PROGRESS_BAR_WIDTH;
        progressBar.setPreferredSize(preferredSize);
        lblMessage = new JLabel(" ");
    }

    private void setupComponent() {
        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        gc.anchor = GridBagConstraints.NORTHWEST;
        contentPane.add(lblMessage, gc);
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(progressBar, gc);

        setTitle("");
        setModal(true);
        pack();
    }
}