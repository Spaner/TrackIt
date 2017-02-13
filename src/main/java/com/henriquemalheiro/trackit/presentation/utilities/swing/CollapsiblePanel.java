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
package com.henriquemalheiro.trackit.presentation.utilities.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * A custom panel, showing a button which makes the panel collapse/expand.
 * The button can be chosen to be shown at the follwing locations:
 * NORTH,WEST,SOUTH and EAST
 * @author Tomas
 */
@SuppressWarnings("serial")
public class CollapsiblePanel extends JPanel implements MouseListener {

    /**
     * Indicate where what orientation the collapsisble panel has.
     * I.e. NORTH indicate that the expand/minimizer button is located at
     * the very top of the panel, WEST indicate that the button is located to
     * the very left of the panel.
     */
    public static final int NORTH = 1, WEST = 2, SOUTH = 3, EAST = 4;
    private JPanel contentPanel;                //Content panel, area available to the user
    private JPanel north, south, west, east;    //Panels containing expand/collapse button
    private boolean expanded = true;            //Indicate if panel expanded
    private JButton collapseButton;             //Button making panel expand/collapse
    private int orientation;                    //The current orientation of the panel
    //can attain the values NORTH,WEST,SOUTH or EAST
    private int buttonSize = 25;                 //The default collapsButton size
    private int arrowSize;                      //The size of the arrow, displayed in the
    private Polygon downArrow, leftArrow, upArrow, rightArrow;

    /**
     * Create a collapsible panel, with a collapse/expand button located
     * at the given location
     * @param   collapseOrientation the location where the collapse/expand
     *          button should be located
     */
    public CollapsiblePanel(int collapseOrientation) {
        contentPanel = new JPanel();

        super.setLayout(new BorderLayout());
        orientation = collapseOrientation;

        //Check if the collapseOrientation is in the correct range
        if (collapseOrientation > 4 || collapseOrientation < 1) {
            throw new IllegalArgumentException("collapseOrientation is not in the correct range");
        }
        //Make sure the arrow is large enough
        if (buttonSize < 4) {
            arrowSize = buttonSize;
        } else {
            arrowSize = buttonSize - 1;
        }
        //Create all the arrows, one of them displayed on the collapseButton
        arrowUp(arrowSize);
        arrowDown(arrowSize);
        arrowRight(arrowSize);
        arrowLeft(arrowSize);


        //Set up expand/minimizer component depending on input argument
        switch (collapseOrientation) {

            case NORTH:
                collapseButton = new JButton() {

                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(Color.GRAY);
                        if (expanded) {
                            g.translate(getSize().width / 2, getSize().height);
                            g.fillPolygon(downArrow);

                        } else {
                            g.translate(getSize().width / 2, 0);
                            g.fillPolygon(upArrow);
                        }
                        g.dispose();
                    }
                };

                north = new JPanel(new BorderLayout());
                north.add(collapseButton, BorderLayout.CENTER);
                north.setPreferredSize(new Dimension(0, buttonSize));
                north.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                super.add(north, BorderLayout.PAGE_START);
                break;

            case EAST:
                collapseButton = new JButton() {

                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(Color.GRAY);
                        if (expanded) {
                            g.translate(0, getSize().height / 2);
                            g.fillPolygon(leftArrow);
                        } else {
                            g.translate(getSize().width, getSize().height / 2);
                            g.fillPolygon(rightArrow);
                        }
                        g.dispose();
                    }
                };
                east = new JPanel(new BorderLayout());
                east.add(collapseButton, BorderLayout.CENTER);
                east.setPreferredSize(new Dimension(buttonSize, 0));
                east.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                super.add(east, BorderLayout.LINE_END);
                break;

            case SOUTH:
                collapseButton = new JButton() {

                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(Color.GRAY);
                        if (expanded) {
                            g.translate(getSize().width / 2, 0);
                            g.fillPolygon(upArrow);
                        } else {
                            g.translate(getSize().width / 2, getSize().height);
                            g.fillPolygon(downArrow);
                        }
                        g.dispose();
                    }
                };
                south = new JPanel(new BorderLayout());
                south.add(collapseButton, BorderLayout.CENTER);
                south.setPreferredSize(new Dimension(0, buttonSize));
                south.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                super.add(south, BorderLayout.PAGE_END);
                break;

            case WEST:
                collapseButton = new JButton() {

                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(Color.GRAY);
                        if (expanded) {
                            g.translate(getSize().width, getSize().height / 2);
                            g.fillPolygon(rightArrow);
                        } else {
                            g.translate(0, getSize().height / 2);
                            g.fillPolygon(leftArrow);
                        }
                        g.dispose();
                    }
                };
                west = new JPanel(new BorderLayout());
                west.add(collapseButton, BorderLayout.CENTER);
                west.setPreferredSize(new Dimension(buttonSize, 0));
                west.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                super.add(west, BorderLayout.LINE_START);
                break;
        }
        //Remove all mouseListners from the button, so that it will not change
        //apperence while hovering over the button
        collapseButton.removeMouseListener(collapseButton.getMouseListeners()[0]);

        collapseButton.addMouseListener(this);
        super.add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Collapse the panel, making all except the expand/collapse button invisible
     */
    public void collapse() {
        if (!expanded) {
            return;
        } else {
            contentPanel.setVisible(false);
        }
        expanded = !expanded;
		validate();//58406
        repaint();
    }

    /**
     * Expand the panel, making the complete panel visible
     */
    public void expand() {
        if (expanded) {
            return;
        } else {
            contentPanel.setVisible(true);
        }
        expanded = !expanded;
		validate();//58406
        repaint();
    }

    //Create a arrow pointing downwards
    private void arrowDown(int size) {
        int[] xPoints = {0, size, -size};
        int[] yPoints = {0, -size, -size};
        downArrow = new Polygon(xPoints, yPoints, 3);
    }

    //Create a arrow pointing upwards
    private void arrowUp(int size) {
        int[] xPoints = {0, -size, size};
        int[] yPoints = {0, size, size};
        upArrow = new Polygon(xPoints, yPoints, 3);
    }

    //Create a arrow pointing to the left
    private void arrowLeft(int size) {
        int[] xPoints = {0, size, size};
        int[] yPoints = {0, size, -size};
        leftArrow = new Polygon(xPoints, yPoints, 3);
    }

    //Create a arrow pointing to the right
    private void arrowRight(int size) {
        int[] xPoints = {0, -size, -size};
        int[] yPoints = {0, -size, size};
        rightArrow = new Polygon(xPoints, yPoints, 3);
    }

    /**
     * Set the size of the collapsible panel button
     * @param size The new size of the button in pixels (size>=0)
     */
    public void setCollapsButtonSize(int size) {
        buttonSize = size;
        //Redefine the collapseButton, and add it to the collapsiblePanel
        switch (orientation) {
            case NORTH:
                north.setPreferredSize(new Dimension(0, buttonSize));
                break;
            case WEST:
                west.setPreferredSize(new Dimension(buttonSize, 0));
                break;
            case SOUTH:
                south.setPreferredSize(new Dimension(0, buttonSize));
                break;
            case EAST:
                east.setPreferredSize(new Dimension(buttonSize, 0));
                break;
        }
        //Make sure the arrow is large enough
        if (buttonSize < 4) {
            arrowSize = buttonSize;
        } else {
            arrowSize = buttonSize - 1;
        }
        //Recreate the arrows, one of them displayed on the collapseButton
        arrowUp(arrowSize);
        arrowDown(arrowSize);
        arrowRight(arrowSize);
        arrowLeft(arrowSize);
    }

    /**
     * Add a component to the content pane of the collapsible panel
     * @param comp The component to be added
     * @return The component added
     */
    @Override
    public Component add(Component comp) {
        return contentPanel.add(comp);
    }

    /**
     * Add a component to the collapsible panel, with layout defined by the
     * constraints
     * @param comp The component to be added
     * @param constraints An object expressing layout contraints for this component
     */
    @Override
    public void add(Component comp, Object constraints) {
        contentPanel.add(comp, constraints);
    }

    /**
     * Add a component to the collapsible panel, with layout defined by the
     * constraints.
     * @param comp The component to be added
     * @param constraints An object expressing layout contraints for this component
     * @param  the position in the container's list at which to insert the component;
     * -1 means insert at the end component
     */
    @Override
    public void add(Component comp, Object constraints, int index) {
        contentPanel.add(comp, constraints, index);
    }

    /**
     * Sets the layout manager for this container.
     * @param layMgr the specified layout manager
     */
    @Override
    public void setLayout(LayoutManager layMgr) {
        if (contentPanel == null) {
        } else {
            contentPanel.setLayout(layMgr);
        }
    }

    /**
     * Collapse/Expand the collapsible panel on mouse clicks
     * @param e The mouseEvent fired
     */
    public void mouseClicked(MouseEvent e) {
        if (expanded) {
            contentPanel.setVisible(false);
        } else {
            contentPanel.setVisible(true);
        }
        expanded = !expanded;
		validate();//58406
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        //Don't do anything for this event
    }

    public void mouseReleased(MouseEvent e) {
        //Don't do anything for this event
    }

    public void mouseEntered(MouseEvent e) {
        //Don't do anything for this event
    }

    public void mouseExited(MouseEvent e) {
        //Don't do anything for this event
    }
}