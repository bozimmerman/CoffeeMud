package com.planet_ink.coffee_mud.core.collections;
import java.io.Serializable;
import java.util.*;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2014-2020 Bo Zimmerman

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
 * A version of the Vector class that provides to "safe" adds
 * and removes by copying the underlying vector whenever those
 * operations are done.  Also maintains a tree, using the CMObject
 * ID() of the stored object as sort key.
 */
public class CMSTreeVector<T extends CMObject> implements Serializable, Iterable<T>, Collection<T>, List<T>, RandomAccess
{
	private static final long serialVersionUID = 6687178785122561992L;
	private volatile Vector<T> V;
	private final    TreeMap<String,T> S;

	public CMSTreeVector()
	{
		V=new Vector<T>();
		S=new TreeMap<String,T>();
	}

	public CMSTreeVector(final int size)
	{
		V=new Vector<T>(size);
		S=new TreeMap<String,T>();
	}

	public CMSTreeVector(final List<T> E)
	{
		V=new Vector<T>();
		S=new TreeMap<String,T>();
		if(E!=null)
			addAll(E);
	}

	public CMSTreeVector(final T[] E)
	{
		V=new Vector<T>();
		S=new TreeMap<String,T>();
		if(E!=null)
		{
			for(final T o : E)
				addBoth(o);
		}
	}

	public CMSTreeVector(final Enumeration<T> E)
	{
		V=new Vector<T>();
		S=new TreeMap<String,T>();
		if(E!=null)
		{
			for(;E.hasMoreElements();)
				addBoth(E.nextElement());
		}
	}

	public CMSTreeVector(final Iterator<T> E)
	{
		V=new Vector<T>();
		S=new TreeMap<String,T>();
		for(;E.hasNext();)
			addBoth(E.next());
	}

	public CMSTreeVector(final Set<T> E)
	{
		V=new Vector<T>();
		S=new TreeMap<String,T>();
		for(final T o : E)
			addBoth(o);
	}

	@SuppressWarnings("unchecked")
	public synchronized void addAll(final Enumeration<T> E)
	{
		if(E!=null)
		{
			V=(Vector<T>)V.clone();
			for(;E.hasMoreElements();)
				addBoth(E.nextElement());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void addAll(final T[] E)
	{
		if(E!=null)
		{
			V=(Vector<T>)V.clone();
			for(final T e : E)
				addBoth(e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void addAll(final Iterator<T> E)
	{
		if(E!=null)
		{
			V=(Vector<T>)V.clone();
			for(;E.hasNext();)
				addBoth(E.next());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAll(final Enumeration<T> E)
	{
		if(E!=null)
		{
			V=(Vector<T>)V.clone();
			for(;E.hasMoreElements();)
				removeBoth(E.nextElement());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAll(final Iterator<T> E)
	{
		if(E!=null)
		{
			V=(Vector<T>)V.clone();
			for(;E.hasNext();)
				removeBoth(E.next());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAll(final List<T> E)
	{
		if(E!=null)
		{
			V=(Vector<T>)V.clone();
			for(final T o : E)
				removeBoth(o);
		}
	}

	public synchronized int capacity()
	{
		return V.capacity();
	}

	@SuppressWarnings("unchecked")
	public synchronized Vector<T> toVector()
	{
		return (Vector<T>)V.clone();
	}

	@SuppressWarnings("unchecked")
	public synchronized CMSTreeVector<T> copyOf()
	{
		final CMSTreeVector<T> SV=new CMSTreeVector<T>();
		SV.V=(Vector<T>)V.clone();
		SV.S.putAll(S);
		return SV;
	}

	@Override
	public synchronized boolean contains(final Object o)
	{
		if(o instanceof CMObject)
			return S.containsKey(((CMObject)o).ID().toUpperCase());
		return V.contains(o);
	}

	@Override
	public synchronized boolean containsAll(final Collection<?> c)
	{
		return V.containsAll(c);
	}

	@SuppressWarnings("unchecked")
	public synchronized void copyInto(final Object[] anArray)
	{
		V=(Vector<T>)V.clone();
		V.copyInto(anArray);
	}

	public synchronized T elementAt(final int index)
	{
		return V.elementAt(index);
	}

	public synchronized Enumeration<T> elements()
	{
		return V.elements();
	}

	public synchronized void ensureCapacity(final int minCapacity)
	{
		V.ensureCapacity(minCapacity);
	}

	@Override
	public synchronized boolean equals(final Object o)
	{
		return o==this;
	}

	public synchronized T firstElement()
	{
		return V.firstElement();
	}

	@Override
	public synchronized T get(final int index)
	{
		return V.get(index);
	}

	@Override
	public synchronized int hashCode()
	{
		return super.hashCode();
	}

	public synchronized int indexOf(final Object o, final int index)
	{
		return V.indexOf(o, index);
	}

	@Override
	public synchronized int indexOf(final Object o)
	{
		return V.indexOf(o);
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return V.isEmpty();
	}

	public synchronized T lastElement()
	{
		return V.lastElement();
	}

	public synchronized int lastIndexOf(final Object o, final int index)
	{
		return V.lastIndexOf(o, index);
	}

	@Override
	public synchronized int lastIndexOf(final Object o)
	{
		return V.lastIndexOf(o);
	}

	@Override
	public synchronized boolean retainAll(final Collection<?> c)
	{
		final int oldSize=size();
		for(final T o : V)
		{
			if(!c.contains(o))
				remove(o);
		}
		return oldSize < size();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized T set(final int index, final T element)
	{
		if(element==null)
			return null;
		if(!S.containsKey(element.ID().toUpperCase()))
		{
			V=(Vector<T>)V.clone();
			final T oldT = V.set(index,element);
			if(oldT!=null)
				S.remove(oldT.ID().toUpperCase());
			S.put(element.ID().toUpperCase(), element);
			return oldT;
		}
		return null;
	}

	public synchronized void setElementAt(final T obj, final int index)
	{
		set(index,obj);
	}

	@SuppressWarnings("unchecked")
	public synchronized void setSize(final int newSize)
	{
		V=(Vector<T>)V.clone();
		V.setSize(newSize);
	}

	@Override
	public synchronized int size()
	{
		return V.size();
	}

	@Override
	public synchronized List<T> subList(final int fromIndex, final int toIndex)
	{
		return V.subList(fromIndex, toIndex);
	}

	@Override
	public synchronized Object[] toArray()
	{
		return V.toArray();
	}

	@SuppressWarnings("hiding")

	@Override
	public synchronized <T> T[] toArray(final T[] a)
	{
		return V.toArray(a);
	}

	@Override
	public synchronized String toString()
	{
		return super.toString();
	}

	@SuppressWarnings("unchecked")
	public synchronized void trimToSize()
	{
		V=(Vector<T>)V.clone();
		V.trimToSize();
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void add(final int index, final T element)
	{
		if(element==null)
			return;
		if(!S.containsKey(element.ID().toUpperCase()))
		{
			V=(Vector<T>)V.clone();
			V.add(index, element);
			S.put(element.ID().toUpperCase(), element);
		}
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean add(final T e)
	{
		if(e==null)
			return false;
		if(!S.containsKey(e.ID().toUpperCase()))
		{
			V=(Vector<T>)V.clone();
			if(V.add(e))
				S.put(e.ID().toUpperCase(), e);
			return true;
		}
		return false;
	}

	private boolean addBoth(final T e)
	{
		if(S.containsKey(e.ID().toUpperCase()))
			return false;
		V.add(e);
		S.put(e.ID().toUpperCase(), e);
		return true;
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean addAll(final Collection<? extends T> c)
	{
		V=(Vector<T>)V.clone();
		boolean kaplah=false;
		for(final Object o : c)
		{
			if(o instanceof CMObject)
				kaplah = addBoth((T)o) || kaplah;
		}
		return kaplah;
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean addAll(int index, final Collection<? extends T> c)
	{
		final int oldSize=size();
		if(index>=size())
			addAll(c);
		else
		{
			for(final Object o : c)
				if(o instanceof CMObject)
					insertElementAt((T)o, index++);
		}
		return oldSize < size();
	}

	public synchronized void addElement(final T obj)
	{
		add(obj);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized void clear()
	{
		V=(Vector<T>)V.clone();
		V.clear();
		S.clear();
	}

	public synchronized void insertElementAt(final T obj, final int index)
	{
		if(obj==null)
			return;
		if(index>=size())
			add(obj);
		else
		if(!S.containsKey(obj.ID().toUpperCase()))
		{
			V.insertElementAt(obj, index);
			S.put(obj.ID().toUpperCase(), obj);
		}
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean remove(final Object o)
	{
		if(!(o instanceof CMObject))
			return false;
		final CMObject O=(CMObject)o;
		final String OID=O.ID().toUpperCase();
		if(!S.containsKey(OID))
			return false;
		V=(Vector<T>)V.clone();
		S.remove(OID);
		return V.remove(o);
	}

	private boolean removeBoth(final Object o)
	{
		if(!(o instanceof CMObject))
			return false;
		final CMObject O=(CMObject)o;
		final String OID=O.ID().toUpperCase();
		if(!S.containsKey(OID))
			return false;
		S.remove(OID);
		return V.remove(o);
	}

	private boolean removeBoth(final CMObject o)
	{
		final String OID=o.ID().toUpperCase();
		if(!S.containsKey(OID))
			return false;
		S.remove(OID);
		return V.remove(o);
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized T remove(final int index)
	{
		V=(Vector<T>)V.clone();
		final T O=V.remove(index);
		if(O==null)
			return null;
		final String OID=O.ID().toUpperCase();
		if(S.containsKey(OID))
			S.remove(OID);
		return O;
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized boolean removeAll(final Collection<?> c)
	{
		V=(Vector<T>)V.clone();
		boolean kaplah=false;
		for(final Object o : c)
			kaplah = removeBoth(o) || kaplah;
		return kaplah;
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAllElements()
	{
		V=(Vector<T>)V.clone();
		V.removeAllElements();
		S.clear();
	}

	public T find(final String key)
	{
		if(key==null)
			return null;
		return S.get(key.toUpperCase());
	}

	public synchronized boolean removeElement(final Object obj)
	{
		return remove(obj);
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeElementAt(final int index)
	{
		V=(Vector<T>)V.clone();
		removeBoth(V.get(index));
	}

	@Override
	public synchronized Iterator<T> iterator()
	{
		return new ReadOnlyIterator<T>(V.iterator());
	}

	@Override
	public synchronized ListIterator<T> listIterator()
	{
		return new ReadOnlyListIterator<T>(V.listIterator());
	}

	@Override
	public synchronized ListIterator<T> listIterator(final int index)
	{
		return new ReadOnlyListIterator<T>(V.listIterator());
	}
}
