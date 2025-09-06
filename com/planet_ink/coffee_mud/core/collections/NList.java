package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

/*
   Copyright 2012-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * A multi-dimensional list of objects, where each row has the same number
 * of columns (dimensions).
 *
 * @param <T> the type of objects in this list
 */
public interface NList<T>
{
	/**
	 * Clear out all the elements of this list.
	 */
	public void clear();

	/**
	 * Trim the internal storage of this list to the actual number of elements.
	 */
	public void trimToSize();

	/**
	 * Gets the index of the first occurrence of the given element in the
	 * list, or -1 if it is not present.
	 *
	 * @param O the element to add
	 * @return the index of the first occurrence of the given element
	 */
	public int indexOf(T O);

	/**
	 * Returns the elements at the given row as an array.
	 *
	 * @param x the row index
	 * @return the elements at the given row as an array
	 */
	public T[] elementsAt(int x);

	/**
	 * Removes and returns the elements at the given row as an array.
	 *
	 * @param x the row index
	 * @return the elements at the given row as an array
	 */
	public T[] removeElementsAt(int x);

	/**
	 * Returns a copy of this NList
	 *
	 * @return a copy of this NList
	 */
	public NList<T> copyOf();

	/**
	 * Sorts the list by the given dimension.  All elements in that dimension
	 * must implement Comparable.
	 *
	 * @param dim the dimension to sort by
	 */
	public void sortBy(int dim);

	/**
	 * Adds a new row to the list.
	 *
	 * @param O the elements of the new row
	 */
	public void addSharedElements(T[] O);

	/**
	 * Returns whether or not the given element is contained in the list,
	 *
	 * @param O the element to look for
	 * @return true if the element is found in the list
	 */
	public boolean contains(T O);

	/**
	 * Returns whether or not the given String is contained in the list,
	 * ignoring case.
	 *
	 * @param S the String to look for
	 * @return true if the String is found in the list
	 */
	public boolean containsIgnoreCase(String S);

	/**
	 * Returns the number of rows in the list.
	 *
	 * @return the number of rows in the list
	 */
	public int size();

	/**
	 * Removes the elements at the given row index.
	 *
	 * @param i the row index to remove
	 */
	public void removeElementAt(int i);

	/**
	 * Removes the elements at the given row index.
	 *
	 * @param i the row index to remove
	 */
	public void remove(int i);

	/**
	 * Removes the first row containing the given element.
	 *
	 * @param O the element to look for
	 */
	public void removeElement(T O);

	/**
	 * Returns a list of all the elements in the given dimension.
	 *
	 * @param dim the dimension to return
	 * @return a list of all the elements in the given dimension
	 */
	public List<T> getDimensionList(int dim);

	/**
	 * Returns a list of all the elements in the given row.
	 *
	 * @param row the row to return
	 * @return a list of all the elements in the given row
	 */
	public List<T> getRowList(int row);

	/**
	 * Gets the element at the given row and dimension.
	 *
	 * @param i row
	 * @param dim dimension
	 * @return the element at the given row and dimension
	 */
	public T elementAt(int i, int dim);

	/**
	 * Gets the element at the given row and dimension.
	 *
	 * @param i row
	 * @param dim dimension
	 * @return the element at the given row and dimension
	 */
	public T get(int i, int dim);

	/**
	 * Sets the element at the given row and dimension.
	 *
	 * @param index row
	 * @param dim dimension
	 * @param O the new value
	 */
	public void setElementAt(int index, int dim, T O);

	/**
	 * Sets the element at the given row and dimension.
	 *
	 * @param index row
	 * @param dim dimension
	 * @param O the new value
	 */
	public void set(int index, int dim, T O);
}
