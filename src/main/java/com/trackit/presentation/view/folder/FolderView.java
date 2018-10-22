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
package com.trackit.presentation.view.folder;

import static com.trackit.business.common.Messages.getMessage;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.trackit.business.DocumentManager;
import com.trackit.business.common.Messages;
import com.trackit.business.domain.Activity;
import com.trackit.business.domain.Course;
import com.trackit.business.domain.DocumentItem;
import com.trackit.business.domain.Folder;
import com.trackit.business.domain.GPSDocument;
import com.trackit.business.domain.Picture;
import com.trackit.presentation.event.Event;
import com.trackit.presentation.event.EventListener;
import com.trackit.presentation.event.EventManager;
import com.trackit.presentation.event.EventPublisher;
import com.trackit.presentation.utilities.ImageUtilities;
import com.trackit.presentation.utilities.Operation;
import com.trackit.presentation.utilities.OperationsFactory;


public class FolderView extends JPanel implements EventPublisher, EventListener {
	private static final long serialVersionUID = -3228982982623268319L;
	
	static ImageIcon documentsIcon = ImageUtilities.createImageIcon("documents.png");
	static ImageIcon activitiesIcon = ImageUtilities.createImageIcon("activities_16.png");
	static ImageIcon coursesIcon = ImageUtilities.createImageIcon("courses_16.png");
	static ImageIcon lapsIcon = ImageUtilities.createImageIcon("laps_16.png");
	static ImageIcon coursePointsIcon = ImageUtilities.createImageIcon("course_points_16.png");
	static ImageIcon waypointsIcon = ImageUtilities.createImageIcon("red_pin_16.png");
	
	private DefaultMutableTreeNode rootNode;
	private DefaultMutableTreeNode workspaceNode;
	private DefaultMutableTreeNode libraryNode;
    private DefaultTreeModel treeModel;
    private JTree folderTree;
    
    public FolderTreeFactory factory = FolderTreeFactory.getInstance();
	
	public FolderView() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new GridLayout(1, 0));
		
		DefaultTreeCellRenderer renderer = new TreeRenderer();
		rootNode = new DefaultMutableTreeNode(new TextItem("", null, null));

		ListItem workspaceFolder = new ListItem(getMessage("folderView.label.workspaceFolder"), documentsIcon, new Runnable() {
			
			@Override
			public void run() {
				EventManager.getInstance().publish(FolderView.this, Event.FOLDER_SELECTED,
						DocumentManager.getInstance().getFolder(DocumentManager.FOLDER_WORKSPACE));
			}
		});
		workspaceNode = new DefaultMutableTreeNode(workspaceFolder);
		rootNode.add(workspaceNode);

		ListItem libraryFolder = new ListItem(getMessage("folderView.label.libraryFolder"), documentsIcon, new Runnable() {
			
			@Override
			public void run() {
				EventManager.getInstance().publish(FolderView.this, Event.FOLDER_SELECTED,
						DocumentManager.getInstance().getFolder(DocumentManager.FOLDER_LIBRARY));
			}
		});
		libraryNode = new DefaultMutableTreeNode(libraryFolder);
		rootNode.add(libraryNode);
		
		treeModel = new DefaultTreeModel(rootNode);
		treeModel.addTreeModelListener(new FolderTreeModelListener());
	    folderTree = new FolderTree(treeModel);
	    folderTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	    folderTree.setShowsRootHandles(true);
	    folderTree.setRootVisible(false);
	    folderTree.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	    folderTree.setCellRenderer(renderer);
	    folderTree.addTreeSelectionListener(new TreeSelectionHandler());
	    folderTree.setSelectionRow(0);
//	    folderTree.setDragEnabled(true);
//	    folderTree.setTransferHandler(new FolderTreeTransferHandler());
//	    folderTree.setDropMode(DropMode.ON);
	    folderTree.setToggleClickCount(0);

        MouseListener popupListener = new FolderTreePopupListener();
        folderTree.addMouseListener(popupListener);

		JScrollPane scrollableFolderTree = new JScrollPane(folderTree);
		scrollableFolderTree.setPreferredSize(new Dimension(200, 750));
		scrollableFolderTree.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		add(scrollableFolderTree);
		
		Folder folder = DocumentManager.getInstance().getFolder(DocumentManager.FOLDER_LIBRARY);
		GPSDocument document = DocumentManager.getInstance().createDocument(folder.getName(), getMessage("folderView.label.defaultCollection"));
		addGPSDocument(libraryNode, document);
		DocumentManager.getInstance().setDefaultCollectionDocument( document);
		
		folderTree.expandPath(new TreePath(rootNode));
		folderTree.setSelectionRow(0);
	}
	
	/* Event Listener interface implementation */
	
	@Override
	public void process(Event event, DocumentItem item) {
		switch (event) {
		case DOCUMENT_ADDED:
			add((GPSDocument) item);
			break;
		case DOCUMENT_UPDATED:
			update((GPSDocument) item);
			break;
		case DOCUMENT_DISCARDED:
			remove((GPSDocument) item);
			break;
		case ACTIVITY_ADDED:
			add((Activity) item);
			break;
		case ACTIVITY_SELECTED:
		case COURSE_SELECTED:
			select(item);
			break;
		case ACTIVITY_REMOVED:
		case ACTIVITY_DISCARDED:											//12335: 2018-06-29
			Activity removedActivity = (Activity) item;
			remove(removedActivity.getParent(), removedActivity);
			break;
		case COURSE_REMOVED:
		case COURSE_DISCARDED:												//12335: 2018-06-29
			Course removedCourse = (Course) item;
			remove(removedCourse.getParent(), removedCourse);
			break;
		case SESSION_SELECTED:
		case LAP_SELECTED:
		case WAYPOINT_SELECTED:
		case PICTURE_SELECTED://58406
			select(item);
			break;
		case COURSE_POINT_SELECTED:
		case EVENT_SELECTED:
		case DEVICE_SELECTED:
		case TRACKPOINT_SELECTED:
			select(item.getParent());
			break;
		case ACTIVITY_UPDATED:
			update((Activity) item);
			break;
		case COURSE_UPDATED:
			update((Course) item);
			break;
		default:
			// Do nothing
		}
	}

	private void add(GPSDocument document) {
		Folder folder = DocumentManager.getInstance().getFolder(document);
		
		if (folder.getName().equals(DocumentManager.FOLDER_WORKSPACE)) {
			addGPSDocument(workspaceNode, document);
		} else if (folder.getName().equals(DocumentManager.FOLDER_LIBRARY)) {
			addGPSDocument(libraryNode, document);
		}
	}
	
	public void remove(GPSDocument document, DocumentItem item) {
		Folder folder = DocumentManager.getInstance().getFolder(document);
//		if (folder != null && folder.getName().equals(DocumentManager.FOLDER_WORKSPACE )) { //12335: 2017-03-09 
		if (folder != null ) {
			remove(item);
		}
	}
	
	@Override
	public void process(Event event, DocumentItem parent, List<? extends DocumentItem> items) {
		// Do nothing
	}
	
	private void addGPSDocument(DefaultMutableTreeNode rootNode, final GPSDocument gpsDocument) {
	    if (gpsDocument == null) {
	    	return;
	    }
	    
	    DefaultMutableTreeNode documentNode = factory.createDocumentNode(gpsDocument);
	    treeModel.insertNodeInto(documentNode, rootNode, rootNode.getChildCount());
	    select(gpsDocument);
	}
	
	private void select(DocumentItem item) {
		DefaultMutableTreeNode node = getNode(item);
		if (node != null) {
			folderTree.scrollPathToVisible(new TreePath(node.getPath()));
			folderTree.setSelectionPath(new TreePath(node.getPath()));
		}
	}
	
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode getNode(DocumentItem item) {
		if (item == null) {
			return null;
		}
		
		Enumeration<DefaultMutableTreeNode> enumeration = rootNode.breadthFirstEnumeration();
		DefaultMutableTreeNode node = null;
		
		while (enumeration.hasMoreElements()) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumeration.nextElement();
			if (item.equals(currentNode.getUserObject())) {
				node = currentNode;
				break;
			}
		}
		
		return node;
	}
	
	@SuppressWarnings("unchecked")
	private void add(Activity activity) {
		GPSDocument document = activity.getParent();
		DefaultMutableTreeNode documentNode = getNodeByName(document.getName());
		
		if (documentNode != null) {
			Enumeration<DefaultMutableTreeNode> enumeration = documentNode.breadthFirstEnumeration();
			
			while (enumeration.hasMoreElements()) {
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumeration.nextElement();
				
				if (currentNode.getUserObject() instanceof ActivitiesItem) {
					DefaultMutableTreeNode activityNode = factory.createActivityNode(activity);
				    treeModel.insertNodeInto(activityNode, currentNode, currentNode.getChildCount());
				    break;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private DefaultMutableTreeNode getNodeByName(String nodeName) {
		Enumeration<DefaultMutableTreeNode> enumeration = rootNode.breadthFirstEnumeration();
		DefaultMutableTreeNode node = null;
		
		while (enumeration.hasMoreElements()) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumeration.nextElement();
			String currentNodeName = currentNode.getUserObject() != null ? ((DocumentItem) currentNode.getUserObject()).getDocumentItemName() : "";
			
			if (nodeName.equals(currentNodeName)) {
				node = currentNode;
				break;
			}
		}
		
		return node;
	}
	
	private void update(GPSDocument document) {
		DefaultMutableTreeNode node = getNode(document);
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
		
		DefaultTreeModel model = (DefaultTreeModel) folderTree.getModel();
        model.removeNodeFromParent(node);
        
        DefaultMutableTreeNode newNode = factory.createDocumentNode(document);
        model.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
        
        selectNode(newNode);
	}
	
	private void update(Course course) {
		DefaultMutableTreeNode node = getNode(course);
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
		int lastIndex = parentNode.getIndex(node);
		
		DefaultTreeModel model = (DefaultTreeModel) folderTree.getModel();
        model.removeNodeFromParent(node);
        
        DefaultMutableTreeNode newNode = factory.createCourseNode(course);
        model.insertNodeInto(newNode, parentNode, lastIndex);
        
        selectNode(newNode);
	}
	
	private void update(Activity activity) {
		DefaultMutableTreeNode node = getNode(activity);
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
		int lastIndex = parentNode.getIndex(node);
		
		DefaultTreeModel model = (DefaultTreeModel) folderTree.getModel();
        model.removeNodeFromParent(node);
        
        DefaultMutableTreeNode newNode = factory.createActivityNode(activity);
        model.insertNodeInto(newNode, parentNode, lastIndex);
        
        selectNode(newNode);
	}
	
	private void selectNode(DefaultMutableTreeNode node) {
		TreeSelectionModel selectionModel = folderTree.getSelectionModel();
        selectionModel.setSelectionPath(new TreePath(node.getPath()));
	}
	
	@SuppressWarnings("unchecked")
	private void remove(DocumentItem item) {
		Enumeration<DefaultMutableTreeNode> enumeration = rootNode.breadthFirstEnumeration();
		
		while (enumeration.hasMoreElements()) {
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
		    if (item.equals(node.getUserObject())) {
		    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		    	
		        DefaultTreeModel model = (DefaultTreeModel) folderTree.getModel();
		        model.removeNodeFromParent(node);
		        
		        folderTree.scrollPathToVisible(new TreePath(parent.getPath()));
			    folderTree.setSelectionPath(new TreePath(parent.getPath()));
			    FolderTreeItem treeItem = (FolderTreeItem) parent.getUserObject();
			    treeItem.publishSelectionEvent(null);
		    }
		}
	}
	
	private class TreeSelectionHandler implements TreeSelectionListener, EventPublisher {
		private List<TreePath> selectedPaths = new ArrayList<TreePath>();
		
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath[] paths = e.getPaths();
			for (TreePath treePath : paths) {
				if (e.isAddedPath(treePath)) {
					selectedPaths.add(treePath);
				} else {
					selectedPaths.remove(treePath);
				}
			}
			
			if (selectedPaths.size() == 1) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) ((JTree) e.getSource()).getLastSelectedPathComponent();
				if (selectedNode != null) {
					FolderTreeItem selectedItem = (FolderTreeItem) selectedNode.getUserObject();
					selectedItem.publishSelectionEvent(FolderView.this);
				}
			} else if (selectedPaths.size() > 1) {
				List<DocumentItem> items = new ArrayList<DocumentItem>();
				for (TreePath path : selectedPaths) {
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
					
					if (selectedNode != null) {
						FolderTreeItem selectedItem = (FolderTreeItem) selectedNode.getUserObject();
						items.add(selectedItem);
					}
				}
				
				EventManager.getInstance().publish(FolderView.this, Event.MISCELANEOUS_SELECTION, null, items);
			}
		}
	}
	
	class FolderTreePopupListener extends MouseAdapter {
		private MenuFactory factory = MenuFactory.getInstance();
		private JPopupMenu popUpMenu;
		
		FolderTreePopupListener() {
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
 
		@Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
		
		@Override
        public void mouseClicked(MouseEvent e) {
			JTree tree = (JTree) e.getSource();
			TreePath selectedPath = tree.getClosestPathForLocation(e.getX(), e.getY());
			FolderTreeItem selectedItem = null;
			
			if (selectedPath != null) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
				selectedItem = (FolderTreeItem) selectedNode.getUserObject();
				
				if (selectedItem != null) {
					if (e.getClickCount() == 1 && tree.getSelectionCount() == 1) {
						selectedItem.publishSelectionEvent(FolderView.this);
					} else if (e.getClickCount() == 2) {
						EventManager.getInstance().publish(FolderView.this, Event.ZOOM_TO_ITEM, selectedItem);
					}
				}
			}
        }
		
		public void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JTree tree = (JTree) e.getSource();
				TreePath selectedPath = tree.getClosestPathForLocation(e.getX(), e.getY());
				
				if (selectedPath != null) {
					tree.setSelectionPath(selectedPath);
					
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
					FolderTreeItem selectedItem = (FolderTreeItem) selectedNode.getUserObject();
					if (SwingUtilities.isRightMouseButton(e)) {
						showPopupMenu(e.getComponent(), e.getX(), e.getY(), selectedItem);
					}
				}
			}
	    }
		
		private void showPopupMenu(Component component, int x, int y, FolderTreeItem selectedItem) {
			if (popUpMenu != null) {
				popUpMenu.removeAll();
			}
			
			GPSDocument collection =  DocumentManager.getInstance().getDefaultCollectionDocument();
			if (selectedItem instanceof GPSDocument) {
				GPSDocument document = (GPSDocument) selectedItem;
				if ( !document.equals( collection)) {
					List<Operation> operations = OperationsFactory.getInstance().getSupportedOperations(document);
					JPopupMenu popupMenu = factory.createPopupMenu(operations);
					popupMenu.show(component, x, y);
				}
				else { //12335: 2017-03-09
					List<Operation> operations = OperationsFactory.getInstance().getCollectionDocumentSupportedOperations();
					JPopupMenu popupMenu = factory.createPopupMenu( operations);
					popupMenu.show( component, x, y);
				}
			} else if (selectedItem.isCourse()) {
				Course course = (Course) selectedItem;
				List<Operation> operations;
				if ( course.getParent().equals( collection) )
					operations = OperationsFactory.getInstance().getCollectionSuportedOperations(course);
				else
					operations= OperationsFactory.getInstance().getSupportedOperations(course);
				JPopupMenu popupMenu = factory.createPopupMenu(operations);
				popupMenu.show(component, x, y);
			} else if (selectedItem.isActivity()) {
				Activity activity = (Activity) selectedItem;
				List<Operation> operations;
				if ( activity.getParent().equals( collection) )
					operations = OperationsFactory.getInstance().getCollectionSuportedOperations( activity);
				else
					operations = OperationsFactory.getInstance().getSupportedOperations(activity);
				popUpMenu = factory.createPopupMenu(operations);
				popUpMenu.show(component, x, y);
			} else if (selectedItem instanceof Picture){
				Picture picture = (Picture) selectedItem;
				List<Operation> operations = OperationsFactory.getInstance().getSupportedOperations(picture);
				popUpMenu = factory.createPopupMenu(operations);
				popUpMenu.show(component, x, y);
			} else if (selectedItem instanceof PicturesItem){
				PicturesItem pictures = (PicturesItem) selectedItem;
				List<Operation> operations = OperationsFactory.getInstance().getSupportedOperations(pictures);
				popUpMenu = factory.createPopupMenu(operations);
				popUpMenu.show(component, x, y);
			}
		}
	}
	
	@Override
	public String toString() {
		return Messages.getMessage("view.folder.name");
	}
}
