package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

/*
   Copyright 2012-2020 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface NList<T>
{
	public void clear();

	public void trimToSize();

	public int indexOf(T O);

	public T[] elementsAt(int x);

	public T[] removeElementsAt(int x);

	public NList<T> copyOf();

	public void sortBy(int dim);

	public void addSharedElements(T[] O);

	public void addElement(T... Os);

	public void add(T... Os);

	public boolean contains(T O);

	public boolean containsIgnoreCase(String S);

	public int size();

	public void removeElementAt(int i);

	public void remove(int i);

	public void removeElement(T O);

	public List<T> getDimensionList(int dim);

	public List<T> getRowList(int row);

	public T elementAt(int i, int dim);

	public T get(int i, int dim);

	public void setElementAt(int index, int dim, T O);

	public void set(int index, int dim, T O);

	public void insertElementAt(int here, T... Os);

	public void add(int here, T... Os);
}
