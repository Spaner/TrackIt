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
package com.trackit.business.utilities.trie;

import java.util.HashMap;
import java.util.Map;

public class TrieNode<T> {
    private Character nodeKey;
    private T nodeValue;
    private boolean terminal;
    private Map<Character, TrieNode<T>> children = new HashMap<Character, TrieNode<T>>();

    public Character getNodeKey() {
	return nodeKey;
    }

    public void setNodeKey(Character nodeKey) {
	this.nodeKey = nodeKey;
    }

    public T getNodeValue() {
	return nodeValue;
    }

    public void setNodeValue(T nodeValue) {
	this.nodeValue = nodeValue;
    }

    public boolean isTerminal() {
	return terminal;
    }

    public void setTerminal(boolean terminal) {
	this.terminal = terminal;
    }

    public Map<Character, TrieNode<T>> getChildren() {
	return children;
    }

    public void setChildren(Map<Character, TrieNode<T>> children) {
	this.children = children;
    }
}