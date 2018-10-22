package com.trackit.presentation;

import static com.trackit.business.common.Messages.getMessage;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.trackit.TrackIt;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Messages;
import com.trackit.business.common.Unit;
import com.trackit.business.domain.Course;
import com.trackit.business.operation.CompareSegmentsOperation;
import com.trackit.presentation.SplitIntoSegmentsDialog.DurationSpinnerModel;
import com.trackit.presentation.SplitIntoSegmentsDialog.PanelType;

public class CompareSegmentDialog extends JDialog {
	private static final long serialVersionUID = -1829914848739484008L;
	public static Logger logger = Logger.getLogger(TrackIt.class.getName());
	
	private enum PanelType {
		COMPARE_SEGMENTS_PANEL
	};
	
	private JPanel compareSegmentsPanel;
	private JPanel dataPanel;
	private CardLayout cardLayout;
	private List<Course> courseList;
	
	//needed for the compare segments finish method, due to the window being set as not modal
	private double latitudeModifier;
	private double longitudeModifier;
	private double modifierMult;
	private List<Long> shiftedIds;
	private CompareSegmentsOperation compareSegmentsOperation;
	
	public CompareSegmentDialog(List<Course> courseList){
		super(TrackIt.getApplicationFrame());
		this.courseList = courseList;
		initComponents();
	}
	
	public CompareSegmentDialog(List<Course> courseList, double latitudeModifier, double longitudeModifier, double modifierMult,
			List<Long> shiftedIds, CompareSegmentsOperation compareSegmentsOperation){
		super(TrackIt.getApplicationFrame());
		this.courseList = courseList;
		this.latitudeModifier = latitudeModifier;
		this.longitudeModifier = longitudeModifier;
		this.modifierMult = modifierMult;
		this.shiftedIds = shiftedIds;
		this.compareSegmentsOperation = compareSegmentsOperation;
		initComponents();
	}
	private void initComponents() {
		compareSegmentsPanel = getCompareSegmentsPanel();
		dataPanel = new JPanel();
		cardLayout = new CardLayout();
		dataPanel.setLayout(cardLayout);
		dataPanel.add(compareSegmentsPanel, PanelType.COMPARE_SEGMENTS_PANEL.name());
		cardLayout.first(dataPanel);
		
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		setLayout(layout);
		setTitle(Messages.getMessage("dialog.compareSegments.title"));
				
		JButton cmdOk = new JButton(Messages.getMessage("trackIt.cmdOk"));
		cmdOk.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				
				CompareSegmentDialog.this.dispose();
				compareSegmentsOperation.finish(latitudeModifier, longitudeModifier, modifierMult, shiftedIds);
			}
			
		});
		
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	CompareSegmentDialog.this.dispose();
				compareSegmentsOperation.finish(latitudeModifier, longitudeModifier, modifierMult, shiftedIds);
            }
        });
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup())
				.addComponent(dataPanel)
				.addGroup(layout.createSequentialGroup().addPreferredGap(ComponentPlacement.UNRELATED,
										GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(cmdOk)));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE))
				.addComponent(dataPanel)
				.addGroup(layout.createParallelGroup()
						.addComponent(cmdOk)));

		getRootPane().setDefaultButton(cmdOk);

		pack();
		setModal(false);
		setResizable(true);
		setLocationRelativeTo(TrackIt.getApplicationFrame());
	}
	
	public void resizeColumnWidth(JTable table) {
	    final TableColumnModel columnModel = table.getColumnModel();
	    for (int column = 0; column < table.getColumnCount(); column++) {
	        int width = 50; // Min width
	        for (int row = 0; row < table.getRowCount(); row++) {
	            TableCellRenderer renderer = table.getCellRenderer(row, column);
	            Component comp = table.prepareRenderer(renderer, row, column);
	            width = Math.max(comp.getPreferredSize().width +1 , width);
	        }
	        columnModel.getColumn(column).setPreferredWidth(width);
	    }
	}
	
	private JPanel getCompareSegmentsPanel() {
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		String columnNames[] = {"Segment", "Distance", "Distance from Start", "Moving Time", "Moving Time from Start"};
		int numberOfColumns = 5;
		String segmentName;
		double segmentDistance;
		double segmentDistanceFromStart;
		double segmentMovingTime;
		double segmentMovingTimeFromStart;
		List<String[]> rowList = new ArrayList<String[]>();
		for(Course course : courseList){
			segmentName = course.getName();
			segmentDistance = (double) Math.round(course.getDistance() *100)/100;
			segmentDistanceFromStart = (double) Math.round(course.getSegmentDistance()*100)/100;
			segmentMovingTime =(double)  Math.round(course.getMovingTime()*100)/100;
			segmentMovingTimeFromStart = (double) Math.round(course.getSegmentMovingTime()*100)/100;
			String[] row = {segmentName, new Double(segmentDistance).toString() + " m", new Double(segmentDistanceFromStart).toString() + " m",
					new Double(segmentMovingTime).toString()+" s", new Double(segmentMovingTimeFromStart).toString()+" s"};
			rowList.add(row);
			
		}
		Object[][] data = new Object[rowList.size()][numberOfColumns];
		int i = 0;
		for(String[] row : rowList){
			data[i] = row;
			i++;
		}
		
		JTable table = new JTable(data, columnNames);
		resizeColumnWidth(table);

		
		JScrollPane scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(850, 200));
		
		
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(scrollPane)));
				
				

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(scrollPane)));
	
		
		
		return panel;
	}
}
