package com.henriquemalheiro.trackit.business.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class UndoManagerCustom {

	private List<UndoItem> itemList;
	private int currentIndex;
	
	public UndoManagerCustom(){
		itemList = new ArrayList<UndoItem>();
		currentIndex = 0;
	}
	
	
	public boolean canUndo(){
		if(currentIndex > 0 && !itemList.isEmpty()) { 
			return true; 
			}
		return false;
	}
	
	public boolean canRedo(){
		boolean redoActionPossible = itemList.listIterator(currentIndex).hasNext();
		if(currentIndex >= 0 && !itemList.isEmpty() &&  redoActionPossible){ 
			return true; 
			}
		return false;
	}
	
	public int getIndex(){
		return currentIndex;
	}
	
	public void addItem(UndoItem item){
		if(currentIndex >= 0){
			itemList.add(item);
			currentIndex++;
		}
	}
	
	public void removeItem(){
		if(currentIndex > 0){
			itemList.remove(currentIndex);
			currentIndex--;
		}
	}
	
	public UndoItem getItem(){
		return itemList.get(currentIndex);
	}
	
	public UndoItem undo(){
		UndoItem item = new UndoItem();
		currentIndex--;
		item = getItem();
		return item;
	}
	
	public UndoItem redo(){
		UndoItem item = new UndoItem();
		item = getItem();
		currentIndex++;
		return item;
	}
	
	public void newItemClear(){
		ListIterator<UndoItem> iterator = itemList.listIterator(currentIndex);
		while(iterator.hasNext()){
			itemList.remove(currentIndex);
		}
		currentIndex++;
	}
	
	
}
