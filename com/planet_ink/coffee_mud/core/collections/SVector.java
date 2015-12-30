package com.planet_ink.coffee_mud.core.collections;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/* 
Copyright 2000-2012 Bo Zimmerman

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
public class SVector<T> implements Serializable, Iterable<T>, Collection<T>, List<T>, RandomAccess, SafeCollectionHost 
{
    private static final long serialVersionUID = 6687178785122561992L;
    private volatile Vector<T> V;
	private final Set<Object> iterators = new HashSet<Object>();

    public SVector()
    {
    	V = new Vector<T>();
    }
    
    public SVector(int size)
    {
    	V = new Vector<T>(size);
    }
    
    public SVector(List<T> E)
    {
    	V = new Vector<T>();
        if(E!=null)
            this.V.addAll(E);
    }
    
    public SVector(T... E)
    {
    	V = new Vector<T>();
        if(E!=null)
            for(T o : E)
                V.add(o);
    }
    
    public SVector(Enumeration<T> E)
    {
    	V = new Vector<T>();
        if(E!=null)
            for(;E.hasMoreElements();)
                V.add(E.nextElement());
    }
    
    public SVector(Iterator<T> E)
    {
    	V = new Vector<T>();
        if(E!=null)
            for(;E.hasNext();)
                V.add(E.next());
    }
    
    public SVector(Set<T> E)
    {
    	V = new Vector<T>();
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
    
    public synchronized int capacity() 
    {
        return V.size();
    }

    @SuppressWarnings("unchecked")
    public synchronized CopyOnWriteArrayList<T> toVector() 
    {
        return (CopyOnWriteArrayList<T>)V.clone();
    }
    
    public synchronized SVector<T> copyOf() 
    {
        return new SVector<T>(this);
    }

    @Override
    public synchronized boolean contains(Object o) 
    {
        return V.contains(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) 
    {
        return V.containsAll(c);
    }

    public synchronized void copyInto(Object[] anArray) 
    {
        V.toArray(anArray);
    }

    public synchronized T elementAt(int index) 
    {
        return V.get(index);
    }

    public synchronized Enumeration<T> elements2() 
    {
    	final SafeFeedbackEnumeration<T> enumer = new SafeFeedbackEnumeration<T>(
	    	new Enumeration<T>()
	        {
	            int i=0;
	            @Override
				public boolean hasMoreElements() {
	                return i<V.size();
	            }
	            @Override
				public T nextElement() {
	                try
	                {
	                    return V.get(i++);
	                }
	                catch(Exception e)
	                {
	                	e.printStackTrace();
	                    throw new NoSuchElementException();
	                }
	            }
	        }
	    , this);
		synchronized(this.iterators)
		{
			this.iterators.add(enumer);
		}
		return enumer;
    }

    public synchronized Enumeration<T> elements() 
    {
    	return new SafeFeedbackEnumeration<T>(
	        new Enumeration<T>()
	        {
	            final Iterator<T> i=V.iterator();
	            @Override
				public boolean hasMoreElements() {
	                return i.hasNext();
	            }
	            @Override
				public T nextElement() {
	                return i.next();
	            }
	        }
	    , this);
    }
    
    @SuppressWarnings("unchecked")
	public synchronized void ensureCapacity(int minCapacity) {
		if(doClone())
			V=(Vector<T>)V.clone();
		V.ensureCapacity(minCapacity);
    }

    @Override
    public synchronized boolean equals(Object o) {
        return o==this;
    }

    public synchronized T firstElement() {
        return (size()==0)?null:V.get(0);
    }

    @Override
    public synchronized T get(int index) {
        return V.get(index);
    }

    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }

    public synchronized int indexOf(Object o, int index) {
    	return V.indexOf(o, index);
    }

    @Override
    public synchronized int indexOf(Object o) {
        return V.indexOf(o);
    }

    @Override
    public synchronized boolean isEmpty() {
        return V.isEmpty();
    }

    public synchronized T lastElement() {
        return (size()==0)?null:V.get(size()-1);
    }

    public synchronized int lastIndexOf(Object o, int index) {
    	return V.lastIndexOf(o, index);
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        return V.lastIndexOf(o);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized boolean retainAll(Collection<?> c) {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.retainAll(c);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized T set(int index, T element) {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.set(index, element);
    }

    @SuppressWarnings("unchecked")
	public synchronized void setElementAt(T obj, int index) {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.set(index, obj);
    }

    @SuppressWarnings("unchecked")
	public synchronized void setSize(int newSize) {
		if(doClone())
			V=(Vector<T>)V.clone();
		V.setSize(newSize);
    }

    @Override
    public int size() { return V.size(); }

    @Override
    public synchronized List<T> subList(int fromIndex, int toIndex) {
        return new SafeChildList<T>(V.subList(fromIndex, toIndex), this);
    }

    @Override
    public synchronized Object[] toArray() {
        return V.toArray();
    }

    @SuppressWarnings("hiding")
    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return V.toArray(a);
    }

    @Override
    public synchronized String toString() {
        return super.toString();
    }

    @SuppressWarnings("unchecked")
	public synchronized void trimToSize() {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.trimToSize();
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public synchronized void add(int index, T element) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.add(index, element);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized boolean add(T e) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.add(e);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized boolean addAll(Collection<? extends T> c) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.addAll(c);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized boolean addAll(int index, Collection<? extends T> c) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.addAll(index, c);
    }

    @SuppressWarnings("unchecked")
	public synchronized void addElement(T obj) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.add(obj);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized void clear() 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.clear();
    }

    @SuppressWarnings("unchecked")
	public synchronized void insertElementAt(T obj, int index) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.add(index, obj);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized boolean remove(Object o) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.remove(o);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized T remove(int index) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.remove(index);
    }

    @SuppressWarnings("unchecked")
	@Override
    public synchronized boolean removeAll(Collection<?> c) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.removeAll(c);
    }

    @SuppressWarnings("unchecked")
	public synchronized void removeAllElements() 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.clear();
    }

    @SuppressWarnings("unchecked")
	public synchronized boolean removeElement(Object obj) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        return V.remove(obj);
    }

    @SuppressWarnings("unchecked")
	public synchronized void removeElementAt(int index) 
    {
		if(doClone())
			V=(Vector<T>)V.clone();
        V.remove(index);
    }

    @Override
    public synchronized Iterator<T> iterator() {
        return new SafeFeedbackIterator<T>(V.iterator(), this);
    }

    @Override
    public synchronized ListIterator<T> listIterator() {
        return new SafeFeedbackListIterator<T>(V.listIterator(), this);
    }

    @Override
    public synchronized ListIterator<T> listIterator(int index) {
        return new SafeFeedbackListIterator<T>(V.listIterator(index), this);
    }
    
	private boolean doClone()
	{
		synchronized(this.iterators)
		{
			return this.iterators.size() > 0;
		}
	}
	
	@Override
	public void returnIterator(Object iter) 
	{
		synchronized(this.iterators)
		{
			this.iterators.remove(iter);
		}
	}
	
	@Override
	public void submitIterator(Object iter) 
	{
		synchronized(this.iterators)
		{
			this.iterators.add(iter);
		}
	}
}
