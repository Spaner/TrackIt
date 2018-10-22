/*
 * This file is part of Track It!.
 * Copyright (C) 2017 Diogo Xavier
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
package com.trackit.presentation.calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.toedter.calendar.IDateEvaluator;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDayChooser;
import com.toedter.calendar.JMonthChooser;
import com.toedter.calendar.JYearChooser;
import com.trackit.business.DocumentManager;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;
import com.trackit.business.database.Database;
import com.trackit.business.dbsearch.CalendarSearch;
import com.trackit.business.dbsearch.DBSearch;
import com.trackit.business.domain.BoundingBox;
import com.trackit.business.domain.CalendarData;
import com.trackit.business.domain.CalendarInfo;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.exception.TrackItException;
import com.trackit.presentation.search.SearchInterface;
import com.trackit.presentation.task.Action;
import com.trackit.presentation.task.Task;
import com.trackit.presentation.view.data.DataType;
import com.trackit.presentation.view.data.DataViewTableModel;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JList;

public class CalendarInterface extends JDialog{
	
	private static CalendarInterface last = null;
	private final JPanel contentPanel = new JPanel();
	public static JCalendar calendar = new JCalendar( Messages.getLocale());
	HashMap<Date, HashMap<String, HashMap<List<String>, List<String>>>> monthDate = new HashMap<Date, HashMap<String, HashMap<List<String>, List<String>>>>();
	CalendarSearch calendarSearch = new CalendarSearch();
	JDayChooser dayChooser = calendar.getDayChooser();
	JMonthChooser monthChooser = calendar.getMonthChooser();
	JYearChooser yearChooser = calendar.getYearChooser();
	JTable jTable;
	TableModel model;
	Calendar auxCalendar = calendar.getCalendar();
//	String monthString = Messages.getMessage("calendar.dialog.noCourses");  //12335: 2018-03-04
//	String dayString = Messages.getMessage("calendarInfo.selectDay");
//	JLabel lblNewLabel_1 = new JLabel(monthString+ "<br>" +dayString, SwingConstants.CENTER);
	String monthString = CalendarInfo.voidMonth.toString();
	String dayString = CalendarInfo.voidDay.toString();
	JLabel lblNewLabel_1 = new JLabel( Messages.getMessage( "calendarInfo.fullMessage", monthString, dayString));
	List<CalendarData> data = new ArrayList<CalendarData>();
	
	private static int buttonWidth = 160;			//12335: 2018-02-27
	private JButton addCollectionButton;			//12335: 2018-02-27
	private JButton selectAllButton;				//12335: 2018-02-27
	private JButton cancelButton;					//13335: 2018-03-03
	
	/**
	 * Create the dialog.
	 */
	public CalendarInterface() {
		last = this;
		calendarInit();
		//12335: 2018-03-04 - show most recent day with recorded tracks if any records exist
		//					  or show current day otherwise
		Date dateToset = Database.getInstance().getMostRecentDocumentDate();
		if ( dateToset == null )
			dateToset = new Date();
		calendar.setDate( dateToset);
		setLocale( Messages.getLocale());  //12335: 2018-03-05 - make sure it starts with current locale
		this.setTitle( Messages.getMessage( "calendar.dialog.title"));
		this.setVisible(true);
	}
	
	public void calendarInit(){
//		setBounds(100, 100, 820, 550);
		setBounds(100, 100, 880, 550);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[grow][][]", "[][][grow]"));
		
		yearChooser.setMaximumSize(new Dimension(100, 16));
		yearChooser.setPreferredSize(new Dimension(100, 16));
		
		calendar.setPreferredSize(new Dimension(620, 300));
		calendar.setMinimumSize(new Dimension(620, 300));
		calendar.setMaximumSize(new Dimension(620, 300));
		
		contentPanel.add(calendar, "cell 0 1");
		
		List<CalendarData> displayedElements = Collections.emptyList();
		model = new DataViewTableModel(DataType.CALENDAR, displayedElements);
		jTable = new JTable(model);
		jTable.setBackground(Color.WHITE);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTable.setRowSelectionAllowed(true);
		jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		contentPanel.add(lblNewLabel_1, "cell 1 1");
		
		JScrollPane scrollPane = new JScrollPane(jTable, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(620, 200));
		JScrollBar bar = scrollPane.getVerticalScrollBar();
		scrollPane.setBackground(Color.WHITE);
		contentPanel.add(scrollPane, "cell 0 2, grow");
		monthChooser.addPropertyChangeListener("month", new CalendarListeners());
		yearChooser.addPropertyChangeListener("year", new CalendarListeners());
		dayChooser.addPropertyChangeListener("day", new CalendarListeners());
		
//		contentPanel.add(list, "cell 0 2, grow");
//		JButton addCollectionButton = new JButton(Messages.getMessage("calendar.dialog.addToCollection"));
//12335: 2018-02-27
		addCollectionButton = new JButton(Messages.getMessage("calendar.dialog.addToCollection"));
//		contentPanel.add(addCollectionButton, "cell 1 2");
		addCollectionButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						addToCollection( false);
					}
				});

		//12335: 2018-02-27 - Buttons Cancel and AddAllToCollection added
		cancelButton = new JButton( Messages.getMessage( "trackIt.cmdCancel"));
		cancelButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CalendarInterface.this.dispose();
			}
		});
		selectAllButton = new JButton( Messages.getMessage( "calendar.dialog.addAllToCollection"));
		selectAllButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addToCollection( true);
				
			}
		});
		cancelButton.setPreferredSize( selectAllButton.getPreferredSize());
		addCollectionButton.setPreferredSize( selectAllButton.getPreferredSize());
		
		selectAllButton.setEnabled( false);
		addCollectionButton.setEnabled( false);
		// make sure button layout follows OS
		JButton left = selectAllButton;
		JButton center = addCollectionButton;
		JButton right  = cancelButton;
		if ( OperatingSystem.isMac() ) {
			left   = cancelButton;
			right  = selectAllButton;
		}

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new BoxLayout( buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder( BorderFactory.createEtchedBorder( EtchedBorder.LOWERED));
		buttonPanel.add( Box.createRigidArea( new Dimension( 20, 40)));
		buttonPanel.add( left, BorderLayout.SOUTH);
		buttonPanel.add( Box.createHorizontalGlue());
		buttonPanel.add( center, BorderLayout.SOUTH);
		buttonPanel.add( Box.createRigidArea( new Dimension( 20, 40)));
		buttonPanel.add( right, BorderLayout.SOUTH);
		buttonPanel.add( Box.createRigidArea( new Dimension( 20, 40)));
		contentPanel.add( buttonPanel, BorderLayout.SOUTH);
		
//		contentPanel.add( left, "cell 0 3, align right, span 2, gaptop 32, w " + buttonWidth);
//		contentPanel.add( center, "cell 0 3, align right, span 2, gaptop 32, gapleft 24,  w 160");
//		contentPanel.add( right, "cell 0 3, align right, span 2, gaptop 32, gapleft 24, w 160");
		// 12335: 2018-02-27 end
	}
		
	public void setNewMonth(int month, int year){
		Font font = new Font("Dialog", Font.BOLD, 14);
		Font normalFont = new Font("Dialog", Font.PLAIN, 11);
		// 12335: 2018-03-04 - Make sure the day exists in the selected month
		//					   (e.g., do not accept 29 Feb when changing year from a leap year,
		//                      that is a JCalender bug)
		int newDay   = dayChooser.getDay();
		int oldDay   = newDay;
		Calendar newDate = Calendar.getInstance();
		newDate.set( year, month, newDay);
		while ( newDate.get( Calendar.MONTH) != month ) {
			newDate.set( year, month, --newDay);
		}
		if ( newDay != oldDay ) {
			calendar.getCalendar().set( year, month, newDay);
			dayChooser.setDay( newDay);
		}
		// day number correctly set now
		JPanel jpanel = calendar.getDayChooser().getDayPanel();
		Component component[] = jpanel.getComponents();
		for(Component days: component){
			days.setForeground(Color.black);
			days.setFont(normalFont);
		}
		monthDate = calendarSearch.getMonth(month, year);
		List<Date> datesList = getDates();
		Calendar aux = calendar.getCalendar();
		aux.set(Calendar.DAY_OF_MONTH, 1);
		//12335: 2018-06-30 - new offset computation
//		int dayOfWeek = aux.get(Calendar.DAY_OF_WEEK);
//		int offset = aux.getFirstDayOfWeek();
		int offset = aux.get( Calendar.DAY_OF_WEEK) - aux.getFirstDayOfWeek();
		if ( offset < 0 )
			offset += 7;
		offset += 6;
		if(!monthDate.isEmpty()){
			dayString   = calendarSearch.getTotalDayDistance( dayChooser.getDay(), month, year);
			monthString = calendarSearch.getTotalMonthDistance(month, year);
//			lblNewLabel_1.setText(monthString + "<br>" + dayString);	//12335: 2018-02-28
			lblNewLabel_1.setText( Messages.getMessage( "calendarInfo.fullMessage", monthString, dayString));
			for(Date date : datesList){
				aux.setTime(date);
				int day = aux.get(Calendar.DAY_OF_MONTH);
//12335: 2018-06-30 - using new offset
//				if(offset == 7){
//					component[6 +  dayOfWeek + day].setForeground(Color.blue.brighter());
//					component[6 +  dayOfWeek + day].setFont(font);
//				}
//				else{
//					component[6 +  dayOfWeek - offset + day].setForeground(Color.blue.brighter());
//					component[6 +  dayOfWeek - offset + day].setFont(font);
//				}
				component[ offset + day].setFont(font);
				component[ offset + day].setForeground(Color.blue.brighter());
				setList( dayChooser.getDay(), month, year);	
			}
		}
		else{
//12335: 2018-03-04
//			monthString = Messages.getMessage("calendar.dialog.noCourses");
//			dayString = Messages.getMessage("calendarInfo.selectDay");
//			lblNewLabel_1.setText(monthString + "<br>" +  dayString);
			monthString = CalendarInfo.voidMonth.toString();
			dayString   = CalendarInfo.voidDay.toString();
			lblNewLabel_1.setText( Messages.getMessage( "calendarInfo.fullMessage", monthString, dayString));
			TableModel model = new DataViewTableModel(DataType.CALENDAR, null);
			jTable.setModel(model);
		}
	}
	
	//12335: 2018-03-03 - Change calendar Locale at Messages request
	//12335: 2018-03-05 - but do it only if the interface has been instatiated
	public static void setLocale( ) {
		if ( last != null ) {
			calendar.setLocale( Messages.getLocale());
			last.setLabels();
		}
	}
	
	//12335: 2018-03-03 - set buttons' labels and summaries according to locale
	private void setLabels() {
		this.setTitle( Messages.getMessage( "calendar.dialog.title"));
		selectAllButton.setText( Messages.getMessage( "calendar.dialog.addAllToCollection"));
		addCollectionButton.setText( Messages.getMessage( "calendar.dialog.addToCollection"));
		cancelButton.setText( Messages.getMessage( "trackIt.cmdCancel"));
		cancelButton.setPreferredSize( selectAllButton.getPreferredSize());
		addCollectionButton.setPreferredSize( selectAllButton.getPreferredSize());
		setNewMonth( monthChooser.getMonth(), yearChooser.getYear());
	}
	
	public void setList(int day, int month, int year) {
		data = calendarSearch.getDay(day, month, year);
		TableModel model = new DataViewTableModel(DataType.CALENDAR, data);
		dayString = calendarSearch.getTotalDayDistance(day, month, year);
//		lblNewLabel_1.setText(monthString+ "<br>" +dayString);
		lblNewLabel_1.setText( Messages.getMessage( "calendarInfo.fullMessage", monthString, dayString));
		jTable.setModel(model);
		selectAllButton.setEnabled( model.getRowCount() != 0);		//12335: 2018-02-27
		addCollectionButton.setEnabled( model.getRowCount() != 0);	//12335: 2018-02-27
	}
	
	public List<Date> getDates(){
		List<Date> datesList = new ArrayList<Date>();
		if(!monthDate.isEmpty()){
			for(Entry<Date, HashMap<String, HashMap<List<String>, List<String>>>> entry : monthDate.entrySet()){
				datesList.add(entry.getKey());
			}
		}
		return datesList;
	}
	
	public JMonthChooser getMonthChooser(){
		return monthChooser;
	}
	
	public JYearChooser getYearChooser(){
		return yearChooser;
	}
	
	public JDayChooser getDayChooser(){
		return dayChooser;
	}
	
	public static CalendarInterface getInstance() {
	      if(last == null) {
	         last = new CalendarInterface();
	      }
	      return last;
	}

	public boolean compareDates(Date date1, Date date2){
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(date1);
		calendar2.setTime(date2);
		if((calendar1.get(Calendar.DAY_OF_MONTH)==calendar2.get(Calendar.DAY_OF_MONTH))||
				(calendar1.get(Calendar.MONTH)==calendar2.get(Calendar.MONTH))||
				(calendar1.get(Calendar.YEAR)==calendar2.get(Calendar.YEAR))){
			return true;
		}
		else{
			return false;
		}
	}
	
//	public void addToCollection(){
//		final DocumentManager manager = DocumentManager.getInstance();
//		final int[] selection = jTable.getSelectedRows();
//		final List<String> listActivities = new ArrayList<String>();
//		final List<String> listCourses = new ArrayList<String>();
//		new Task(new Action(){
//			@Override
//			public Object execute() throws TrackItException{
//				for(int s: selection){
//					CalendarData calendarData = data.get(s);
//					if(calendarData.getIsActivity().equals("Activity")){
//						listActivities.add(calendarData.getName());
//					}
//					else{
//						listCourses.add(calendarData.getName());
//					}
//					manager.selectiveImport(DocumentManager.getInstance().getDefaultCollectionDocument(), DocumentManager.getInstance().getLibraryFolder(), calendarData.getFilepath() , listActivities, listCourses);
//				}
//				return null;
//			}
//
//			@Override
//			public String getMessage() {
//				return "Loading";
//			}
//
//			@Override
//			public void done(Object result) {
//				// TODO Auto-generated method stub
//				
//			}
//		}).execute();
//	}
	
	// 12335: 2018-02-27
	// substitutes former aadToCollection()
	public void addToCollection( boolean selectAll) {
		final DocumentManager manager = DocumentManager.getInstance();
		final int[] selected;
		final List<String> activitiesList = new ArrayList<String>();
		final List<String> coursesList    = new ArrayList<String>();
		if ( selectAll ) {
			selected = new int [ jTable.getRowCount()];
			for( int i=0; i<jTable.getRowCount(); i++)
				selected[i] = i;
		} else
			selected = jTable.getSelectedRows();
//		System.out.print( "Selected:");
//		for( int k=0; k<selected.length; k++) System.out.print( " " + selected[k]);
//		System.out.println();
		
		new Task( new Action() {
			
			@Override
			public String getMessage() {
				return Messages.getMessage( "searchResult.loadingInProgress");	//12335: 2018-07-08
			}
			
			@Override
			public Object execute() throws TrackItException {
				HashMap<String, List<Integer>> map = new HashMap<>();
				for( int i=0; i<selected.length; i++) {
					int item = selected[i];
					String filename = data.get( item).getFilepath();
					List<Integer> list = map.get( filename);
					if ( list == null ) {
						list = new ArrayList<>();
						map.put( filename, list);
					}
					list.add( item);
				}
				for( String filename: map.keySet()) {
					Object value;
					activitiesList.clear();
					coursesList.clear();
					List<Integer> list = map.get( filename);
					for( int i=0; i<list.size(); i++) {
						int k = list.get( i);
						if ( data.get(k).isActivity() )
							activitiesList.add( data.get( k).getName());
						else
							coursesList.add( data.get( k).getName());
					}
					manager.selectiveImport( manager.getDefaultCollectionDocument(), manager.getLibraryFolder(), filename, activitiesList, coursesList);
				}
				return null;
			}
			
			@Override
			public void done(Object result) {
				// TODO Auto-generated method stub
				
			}
		}).execute();
	}
	

}
