package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

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
