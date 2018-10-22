package com.trackit.presentation.utilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.trackit.business.common.Messages;

public class SliderBoth {
	JTextField maxValSimple = new JTextField();
	JTextField minValSimple = new JTextField();
	RangeSlider rangeSliderSimple = new RangeSlider();
	JTextField maxValAdvanced = new JTextField();
	JTextField minValAdvanced = new JTextField();
	RangeSlider rangeSliderAdvanced = new RangeSlider();
	int maximum;

	public SliderBoth(int min, int max){
		maxValSimple.setText(Integer.toString(max));
		minValSimple.setText(Integer.toString(min));
		rangeSliderSimple.setMinimum(min);
		rangeSliderSimple.setMaximum(max);
		rangeSliderSimple.setValue(min);
		rangeSliderSimple.setUpperValue(max);
		
		maxValAdvanced.setText(Integer.toString(max));
		minValAdvanced.setText(Integer.toString(min));
		rangeSliderAdvanced.setMinimum(min);
		rangeSliderAdvanced.setMaximum(max);
		rangeSliderAdvanced.setValue(min);
		rangeSliderAdvanced.setUpperValue(max);
		
		simpleListeners();
		advancedListeners();
	}
	
	public void simpleListeners(){
		rangeSliderSimple.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				RangeSlider slider = (RangeSlider) e.getSource();
				int val = slider.getValue();
				rangeSliderAdvanced.setValue(val);
				val = slider.getUpperValue();
				rangeSliderAdvanced.setUpperValue(val);
				minValSimple.setText(String.valueOf(slider.getValue()));
				maxValSimple.setText(String.valueOf(slider.getUpperValue()));
			}
		});
		minValSimple.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int val;
				try{
					val = Integer.parseInt(minValSimple.getText());
					if(val<0){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error0"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					if(val>Integer.parseInt(maxValSimple.getText())){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error1"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					else{
						rangeSliderSimple.setValue(val);
						rangeSliderAdvanced.setValue(val);
					}
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error2"), "Dialog",
					        JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		maxValSimple.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int val;
				try{
					val = Integer.parseInt(maxValSimple.getText());
					if(val<Integer.parseInt(minValSimple.getText())){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error0"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					if(val>maximum){
						maximum = val;
						rangeSliderSimple.setMaximum(maximum);
						rangeSliderSimple.setUpperValue(maximum);
						rangeSliderAdvanced.setMaximum(maximum);
						rangeSliderAdvanced.setUpperValue(maximum);
					}
					else{
						rangeSliderSimple.setUpperValue(val);
						rangeSliderAdvanced.setUpperValue(val);
					}
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error2"), "Dialog",
					        JOptionPane.ERROR_MESSAGE);
				}	
			}
		});
	}
	
	public void advancedListeners(){
		rangeSliderAdvanced.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				RangeSlider slider = (RangeSlider) e.getSource();
				int val = slider.getValue();
				rangeSliderSimple.setValue(val);
				val = slider.getUpperValue();
				rangeSliderSimple.setUpperValue(val);
				minValAdvanced.setText(String.valueOf(slider.getValue()));
				maxValAdvanced.setText(String.valueOf(slider.getUpperValue()));
			}
		});
		minValAdvanced.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int val;
				try{
					val = Integer.parseInt(minValAdvanced.getText());
					if(val<0){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error0"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					if(val>Integer.parseInt(maxValAdvanced.getText())){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error1"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					else{
						rangeSliderSimple.setValue(val);
						rangeSliderAdvanced.setValue(val);
					}
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error2"), "Dialog",
					        JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		maxValAdvanced.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int val;
				try{
					val = Integer.parseInt(maxValAdvanced.getText());
					if(val<Integer.parseInt(minValAdvanced.getText())){
						JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error0"), "Dialog",
						        JOptionPane.ERROR_MESSAGE);
					}
					if(val>maximum){
						maximum = val;
						rangeSliderSimple.setMaximum(maximum);
						rangeSliderSimple.setUpperValue(maximum);
						rangeSliderAdvanced.setMaximum(maximum);
						rangeSliderAdvanced.setUpperValue(maximum);
					}
					else{
						rangeSliderSimple.setUpperValue(val);
						rangeSliderAdvanced.setUpperValue(val);
					}
				}catch(NumberFormatException e){
					JOptionPane.showMessageDialog(new JFrame(), Messages.getMessage("search.dialog.error2"), "Dialog",
					        JOptionPane.ERROR_MESSAGE);
				}	
			}
		});
	}
	
	public String getMaxAdvancedValue(){
		int max = rangeSliderAdvanced.getMaximum()*1000;
		return String.valueOf(max);
	}
	
	public String getMinAdvancedValue(){
		int min = rangeSliderAdvanced.getMinimum()*1000;
		return String.valueOf(min);
	}
	
	public String getMaxSimpleValue(){
		int max = rangeSliderSimple.getMaximum()*1000;
		return String.valueOf(max);
	}
	
	public String getMinSimpleValue(){
		int min = rangeSliderSimple.getMinimum()*1000;
		return String.valueOf(min);
	}
	
	public JTextField getSimpleTextMin(){
		return minValSimple;
	}
	
	public JTextField getSimpleTextMax(){
		return maxValSimple;
	}
	
	public RangeSlider getSimpleSlider(){
		return rangeSliderSimple;
	}
	
	public JTextField getAdvancedTextMin(){
		return minValAdvanced;
	}
	
	public JTextField getAdvancedTextMax(){
		return maxValAdvanced;
	}
	
	public RangeSlider getAdvancedSlider(){
		return rangeSliderAdvanced;
	}
}
