package com.henriquemalheiro.trackit.business.operation;

import java.util.ArrayList;
import java.util.List;

public class UndoManagerCustom {

	private List<UndoItem> undoItemList;
	private List<UndoItem> redoItemList;
	private boolean canUndo;
	private boolean canRedo;
	
	public UndoManagerCustom(){
		undoItemList = new ArrayList<UndoItem>();
		redoItemList = new ArrayList<UndoItem>();
		canUndo = false;
		canRedo = false;
	}
	
	public boolean canUndo(){
		return canUndo;
	}
	
	public boolean canRedo(){
		return canRedo;
	}
	
	public void addUndo(UndoItem item){
		redoItemList.clear();
		undoItemList.add(item);
		canUndo = true;
		canRedo = false;
	}
	
	
	
	public void pushUndo(UndoItem item){
		undoItemList.add(item);
		canUndo = true;
	}
	
	
	public UndoItem getUndoableItem(){
		int itemIndex = undoItemList.size() - 1;
		UndoItem lastItem = undoItemList.get(itemIndex);
		return lastItem;
	}
	
	public UndoItem getRedoableItem(){
		int itemIndex = redoItemList.size() - 1;
		UndoItem lastItem = redoItemList.get(itemIndex);
		return lastItem;
	}

	public void popUndo(){
		int itemIndex = undoItemList.size() - 1;
		UndoItem popItem = undoItemList.get(itemIndex);
		undoItemList.remove(itemIndex);
		pushRedo(popItem);
		if(undoItemList.isEmpty()){
			canUndo = false;
		}
	}
	
	public void deleteUndo(){
		int itemIndex = undoItemList.size() - 1;
		undoItemList.remove(itemIndex);
		if(undoItemList.isEmpty()){
			canUndo = false;
		}
	}
	
	public void deleteRedo(){
		int itemIndex = redoItemList.size() - 1;
		redoItemList.remove(itemIndex);
		if(redoItemList.isEmpty()){
			canRedo = false;
		}
	}
	
	
	public void pushRedo(UndoItem item){
		redoItemList.add(item);
		canRedo = true;
	}
	
	public void popRedo(){
		int itemIndex = redoItemList.size() - 1;
		UndoItem popItem = redoItemList.get(itemIndex);
		redoItemList.remove(itemIndex);
		pushUndo(popItem);
		if(redoItemList.isEmpty()){
			canRedo = false;
		}
	}
	
	public void clearRedo(){
		redoItemList.clear();
		canRedo = false;
	}
	
	
}
