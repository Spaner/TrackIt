package com.miguelpernas.trackit.business.domain;

public class Pair<L, R> {
	private L left;
	private R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}
	
	void setLeft(L left){
		this.left = left;
	}

	void setRight(R right){
		this.right = right;
	}
}
