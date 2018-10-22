package com.trackit.presentation.utilities;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.hamcrest.core.IsSame;

import com.trackit.TrackIt;
import com.trackit.business.common.Messages;
import com.trackit.business.common.OperatingSystem;

public class CheckSaveDialog extends JDialog {
	
	public static final int YES = 1;
	public static final int NO  = 0;
	
	private boolean showSameForAll = false;
	private boolean isForAll       = false;
	private String  dialogMessage1, dialogMessage2;
	private char    yesChar,        noChar;
	private int     result = NO;
	private JRootPane rootPane = null;
	
	static CheckSaveDialog last = null;
	
	public static int showRegisterConfirmDialog( String documentName, String documentFilename, boolean showSameForAll) {
		return showConfirmDialog( Messages.getMessage( "dialog.checkSave.register.title"),
                				  Messages.getMessage( "dialog.checkSave.register.message1", documentName, documentFilename),
                				  Messages.getMessage( "dialog.checkSave.register.message2"),
                				  showSameForAll);
	}
	
	public static int showSaveConfirmDialog(  String documentName, String documentFilename, boolean showSameForAll) {		
		return showConfirmDialog( Messages.getMessage( "dialog.checkSave.save.title", documentName),
				                  Messages.getMessage( "dialog.checkSave.save.message1", documentName, documentFilename),
				                  Messages.getMessage( "dialog.checkSave.save.message2"),
				                  showSameForAll);
	}
	
	private static int showConfirmDialog( String title, String line_1, String line_2,
										  boolean showSameForAll) {
		last = new CheckSaveDialog( title, line_1, line_2, showSameForAll);
		return last.getResult();
	}
	
	public static boolean getIsForall() {
		return last.getIsForAll();
	}

	CheckSaveDialog( String title, String textLine1, String textLine2, boolean showSameForAll) {
		super( TrackIt.getApplicationFrame());
		this.showSameForAll = showSameForAll;
		dialogMessage1      = textLine1;
		dialogMessage2      = textLine2;
		setTitle( title);
		initComponents();
	}
	
	private void initComponents() {
		
		rootPane = getRootPane();
		
		AbstractAction eventListener = new AbstractAction() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				String first = e.getActionCommand().toLowerCase();
				if ( first.charAt(0) == KeyEvent.VK_ESCAPE ) {
					first = rootPane.getDefaultButton().getText().toLowerCase();
				}
				result = (first.charAt(0) == yesChar) ? YES : NO;
				if ( !showSameForAll && result == YES )
					isForAll = false;
				CheckSaveDialog.this.dispose();
			}
		};
		
		JPanel message = messagePanel();
		
		JLabel icon = new JLabel( UIManager.getIcon( "OptionPane.questionIcon"));
		
		String messageText = Messages.getMessage( "messages.yes");
		yesChar            = messageText.substring( 0, 1).toLowerCase().charAt(0);
		JButton cmdYes = new JButton( eventListener);
		cmdYes.setText( messageText);
		cmdYes.setDisplayedMnemonicIndex(0);		
		
		messageText    = Messages.getMessage( "messages.no");
		noChar         = messageText.substring( 0, 1).toLowerCase().charAt(0);
		JButton cmdNo  = new JButton( eventListener);
		cmdNo.setText( messageText);
		cmdNo.setDisplayedMnemonicIndex( 0);
		JButton left  = cmdYes;
		JButton right = cmdNo;
		if ( OperatingSystem.isMac() ) {
			left  = cmdNo;
			right = cmdYes;
		}
		
		JPanel buttonsPanel = new JPanel();
		FlowLayout flowlayout = new FlowLayout( FlowLayout.TRAILING);
		buttonsPanel.setLayout( flowlayout);
		buttonsPanel.add( left);
		buttonsPanel.add( right);
		
		GridBagLayout gridLayout = new GridBagLayout();
		setLayout( gridLayout);
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.fill   = GridBagConstraints.VERTICAL;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets( 15, 15, 12, 0);
		this.add( icon, constraints);
		
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.insets = new Insets( 15, 15, 12, 15);
		constraints.gridwidth = 2;
		this.add( message, constraints);
		
		constraints.gridwidth = 1;
		
		if ( showSameForAll ) {
//			JCheckBox box = new JCheckBox( eventListener);
//			box.setText(Messages.getMessage( "document.message.sameForAll"));
			JCheckBox box = new JCheckBox( Messages.getMessage( "dialog.checkSave.sameForAll"));
			box.addItemListener( new ItemListener() {				
				@Override
				public void itemStateChanged(ItemEvent e) {
					isForAll = !isForAll;
				}
			});
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.WEST;
			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.insets = new Insets( 0, 15, 10, 0);
			this.add( box, constraints);
		}
		
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 2;
		constraints.gridy = 1;
		constraints.insets = new Insets( 0, 15, 10, 15);
		this.add( buttonsPanel, constraints);
		rootPane.setDefaultButton( cmdNo);
		
		InputMap inputMap = rootPane.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = rootPane.getActionMap();
		inputMap.put( KeyStroke.getKeyStroke( Character.toString(yesChar).toUpperCase().charAt(0), 
				                              InputEvent.SHIFT_DOWN_MASK), 
				       "uppercaseYes");
		actionMap.put( "uppercaseYes", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( yesChar), 
				      "lowercaseYes");
		actionMap.put( "lowercaseYes", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( Character.toString(noChar).toUpperCase().charAt(0),
				                              InputEvent.SHIFT_DOWN_MASK),
				       "uppercaseNo");
		actionMap.put( "uppercaseNo", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( noChar),
				       "lowercaseNo");
		actionMap.put( "lowercaseNo", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER,0), "enter");
		actionMap.put( "enter", eventListener);
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), "escape");
		actionMap.put( "escape", eventListener);
	
		pack();
		setModal( true);
		setResizable( false);
		setLocationRelativeTo( TrackIt.getApplicationFrame());
		this.setVisible( true);
	}
	
	public int getResult() {
		return result;
	}
	
	public boolean getIsForAll() {
		return isForAll;
	}
	
	private JPanel messagePanel() {
		JPanel messagePanel = new JPanel();
		JLabel label1 = new JLabel( dialogMessage1);
		label1.setAlignmentX( Component.LEFT_ALIGNMENT);
		JLabel label2 = new JLabel( dialogMessage2);
		label2.setAlignmentX( Component.LEFT_ALIGNMENT);//		img.setAlignmentX( Component.CENTER_ALIGNMENT);
		BoxLayout layout = new BoxLayout( messagePanel, BoxLayout.PAGE_AXIS);
		messagePanel.setLayout( layout);
		messagePanel.add( label1);
		messagePanel.add( label2);
		return messagePanel;
	}
}
