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
package com.trackit.business.common;


public class BoundingBox2<T> {
	private T topLeft;
	private T topRight;
	private T bottomRight;
	private T bottomLeft;

	public BoundingBox2(T topLeft, T topRight, T bottomRight, T bottomLeft) {
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomRight = bottomRight;
		this.bottomLeft = bottomLeft;
	}

	public T getTopLeft() {
		return topLeft;
	}

	public void setTopLeft(T topLeft) {
		this.topLeft = topLeft;
	}

	public T getTopRight() {
		return topRight;
	}

	public void setTopRight(T topRight) {
		this.topRight = topRight;
	}

	public T getBottomRight() {
		return bottomRight;
	}

	public void setBottomRight(T bottomRight) {
		this.bottomRight = bottomRight;
	}

	public T getBottomLeft() {
		return bottomLeft;
	}

	public void setBottomLeft(T bottomLeft) {
		this.bottomLeft = bottomLeft;
	}

	@Override
	public String toString() {
		return "BoundingBox [topLeft=" + topLeft + ", topRight=" + topRight
				+ ", bottomRight=" + bottomRight + ", bottomLeft=" + bottomLeft + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bottomLeft == null) ? 0 : bottomLeft.hashCode());
		result = prime * result
				+ ((bottomRight == null) ? 0 : bottomRight.hashCode());
		result = prime * result + ((topLeft == null) ? 0 : topLeft.hashCode());
		result = prime * result
				+ ((topRight == null) ? 0 : topRight.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) {
			return false;
		}

		BoundingBox2<?> other = (BoundingBox2<?>) obj;
		if (bottomLeft == null) {
			if (other.bottomLeft != null)
				return false;
		} else if (!bottomLeft.equals(other.bottomLeft))
			return false;
		if (bottomRight == null) {
			if (other.bottomRight != null)
				return false;
		} else if (!bottomRight.equals(other.bottomRight))
			return false;
		if (topLeft == null) {
			if (other.topLeft != null)
				return false;
		} else if (!topLeft.equals(other.topLeft))
			return false;
		if (topRight == null) {
			if (other.topRight != null)
				return false;
		} else if (!topRight.equals(other.topRight))
			return false;
		return true;
	}
}
