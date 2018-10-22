package com.trackit.presentation.utilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.trackit.business.common.Messages;

public class SliderUtilities {
	JTextField maxVal = new JTextField();
	JTextField minVal = new JTextField();
	RangeSlider rangeSlider = new RangeSlider();
	int maximum;
	
	Synchronizer<SliderUtilities> peers = null;
	
	public SliderUtilities(int min, int max){
		maxVal.setText(Integer.toString(max));
		minVal.setText(Integer.toString(min));
		rangeSlider.setMinimum(min);
		rangeSlider.setMaximum(max);
		rangeSlider.setValue(min);
		rangeSlider.setUpperValue(max);
		maximum = max;
		Listeners();
	}
	
	public void startSynchronizing( SliderUtilities peer) {
		if ( peer != null ) {
			if ( peers == null )
				peers = new Synchronizer<>();
			peer.peers = peers.add( this, peer);
		}
	}
	
	public void stopSynchronizing( SliderUtilities peer) {
		if ( peer != null )
			peers.remove( peer);
	}
	
	public void Listeners(){
		rangeSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				RangeSlider slider = (RangeSlider) e.getSource();
				minVal.setText(String.valueOf(slider.getValue()));
				maxVal.setText(String.valueOf(slider.getUpperValue()));
			}
		});
		minVal.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int val;
				try{
					val = Integer.parseInt(minVal.getText());
					if(val<0){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error0"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					if(val>Integer.parseInt(maxVal.getText())){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error1"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					else{
						rangeSlider.setValue(val);
					}
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error2"), "Dialog",
					        JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		maxVal.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int val;
				try{
					val = Integer.parseInt(maxVal.getText());
					if(val<Integer.parseInt(minVal.getText())){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error0"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					if(val>maximum){
						maximum = val;
						rangeSlider.setMaximum(maximum);
						rangeSlider.setUpperValue(maximum);
					}
					else{
						rangeSlider.setUpperValue(val);
					}
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error2"), "Dialog",
					        JOptionPane.ERROR_MESSAGE);
				}	
			}
		});
	}
	
	public String getMaxVal(){
		int max = rangeSlider.getMaximum()*1000;
		return String.valueOf(max);
	}
	
	public String getMinVal(){
		int min = rangeSlider.getMinimum()*1000;
		return String.valueOf(min);
	}
	
	public JTextField getMaxTextField(){
		return maxVal;
	}
	
	public JTextField getMinTextField(){
		return minVal;
	}
	
	public RangeSlider getRangeSlider(){
		return rangeSlider;
	}
	
	public int getMax(){
		return maximum;
	}
}
