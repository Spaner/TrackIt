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
	
	public void pushUndo(UndoItem item){
		undoItemList.add(item);
		canUndo = true;
	}

	public UndoItem popUndo(){
		int itemIndex = undoItemList.size() - 1;
		UndoItem popItem = undoItemList.get(itemIndex);
		undoItemList.remove(itemIndex);
		pushRedo(popItem);
		if(undoItemList.isEmpty()){
			canUndo = false;
		}
		return popItem;
	}
	
	
	public void pushRedo(UndoItem item){
		redoItemList.add(item);
		canRedo = true;
	}
	
	public UndoItem popRedo(){
		int itemIndex = redoItemList.size() - 1;
		UndoItem popItem = redoItemList.get(itemIndex);
		redoItemList.remove(itemIndex);
		pushUndo(popItem);
		if(redoItemList.isEmpty()){
			canRedo = false;
		}
		return popItem;
	}
	
	public void clearRedo(){
		redoItemList.clear();
		canRedo = false;
	}
	
	
}
