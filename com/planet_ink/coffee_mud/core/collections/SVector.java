package com.planet_ink.coffee_mud.core.collections;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.OperationNotSupportedException;

/* 
Copyright 2000-2013 Bo Zimmerman

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

/*
 * A version of the CopyOnWriteArrayList class that provides to "safe" adds
 * and removes by copying the underlying CopyOnWriteArrayList whenever those
 * operations are done.
 */
public class SVector<T> extends CopyOnWriteArrayList<T> implements Serializable, Iterable<T>, Collection<T>, List<T>, RandomAccess 
{
	private static final long serialVersionUID = 6687178785122561992L;

	public SVector()
	{
		super();
	}
	public SVector(int size)
	{
		super();
	}
	public SVector(List<T> E)
	{
		super();
		if(E!=null)
			this.addAll(E);
	}
	
	public SVector(T[] E)
	{
		super();
		if(E!=null)
			for(T o : E)
				add(o);
	}
	
	public SVector(Enumeration<T> E)
	{
		super();
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}
	
	public SVector(Iterator<T> E)
	{
		super();
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}
	
	public SVector(Set<T> E)
	{
		super();
		if(E!=null)
			for(T o : E)
				add(o);
	}
	
	public synchronized void addAll(Enumeration<T> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}
	
	public synchronized void addAll(T[] E)
	{
		if(E!=null)
			for(T e : E)
				add(e);
	}
	
	public synchronized void addAll(Iterator<T> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}
	
	public synchronized void removeAll(Enumeration<T> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				remove(E.nextElement());
	}
	
	public synchronized void removeAll(Iterator<T> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				remove(E.next());
	}
	
	public synchronized void removeAll(List<T> E)
	{
		if(E!=null)
			for(T o : E)
				remove(o);
	}
	
	public int capacity() {
		return size();
	}

	public synchronized Vector<T> toVector() {
		return new XVector<T>(this);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized SVector<T> copyOf() {
		try
		{
			return (SVector<T>)clone();
		}
		catch(Exception e)
		{
			return new SVector<T>(this);
		}
	}

	public synchronized void copyInto(Object[] anArray) {
		toArray(anArray);
	}

	public synchronized T elementAt(int index) {
		return get(index);
	}

	public synchronized Enumeration<T> elements() {
		return new Enumeration<T>()
		{
			final Iterator<T> i=iterator();
			public boolean hasMoreElements() {
				return i.hasNext();
			}
			public T nextElement() {
				return i.next();
			}
		};
	}

	public synchronized void ensureCapacity(int minCapacity) {
		throw new IllegalArgumentException();
	}

	public synchronized T firstElement() {
		return (size()==0)?null:get(0);
	}

	public synchronized T lastElement() {
		return (size()==0)?null:get(size()-1);
	}

	public synchronized void setElementAt(T obj, int index) {
		set(index, obj);
	}

	public synchronized void setSize(int newSize) {
		if(newSize==0)
			clear();
		else
			throw new IllegalArgumentException();
	}

	public synchronized void trimToSize() {
	}
	
	public synchronized void addElement(T obj) 
	{
		add(obj);
	}

	public synchronized void insertElementAt(T obj, int index) 
	{
		add(index, obj);
	}

	public synchronized void removeAllElements() 
	{
		clear();
	}

	public synchronized boolean removeElement(Object obj) 
	{
		return remove(obj);
	}

	public synchronized void removeElementAt(int index) 
	{
		remove(index);
	}
}
