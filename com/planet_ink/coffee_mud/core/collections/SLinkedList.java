package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

import com.planet_ink.coffee_mud.core.Log;

/*
   Copyright 2000-2015 Bo Zimmerman

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
public class SLinkedList<K> implements Serializable, Cloneable, Iterable<K>, Collection<K>, Deque<K>, List<K>, Queue<K>, SafeCollectionHost
{
	private static final long	   serialVersionUID	= -4174213459327144771L;
	private volatile LinkedList<K>	L;
	private final Set<Object> iterators = new HashSet<Object>();

	public SLinkedList()
	{
		L = new LinkedList<K>();
	}

	public SLinkedList(K[] E)
	{
		L = new LinkedList<K>();
		if (E != null)
		{
			for (final K o : E)
				L.add(o);
		}
	}

	public SLinkedList(Enumeration<K> E)
	{
		L = new LinkedList<K>();
		if (E != null)
		{
			for (; E.hasMoreElements();)
				L.add(E.nextElement());
		}
	}

	public SLinkedList(Iterator<K> E)
	{
		L = new LinkedList<K>();
		if (E != null)
		{
			for (; E.hasNext();)
				L.add(E.next());
		}
	}

	public SLinkedList(Set<K> E)
	{
		L = new LinkedList<K>();
		if (E != null)
		{
			for (final K o : E)
				add(o);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void addAll(Enumeration<K> E)
	{
		if (E != null)
		{
			if (doClone())
				L = (LinkedList<K>) L.clone();
			for (; E.hasMoreElements();)
				L.add(E.nextElement());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void addAll(K[] E)
	{
		if (E != null)
		{
			if (doClone())
				L = (LinkedList<K>) L.clone();
			for (final K e : E)
				L.add(e);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void addAll(Iterator<K> E)
	{
		if (E != null)
		{
			if (doClone())
				L = (LinkedList<K>) L.clone();
			for (; E.hasNext();)
				L.add(E.next());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAll(Enumeration<K> E)
	{
		if (E != null)
		{
			if (doClone())
				L = (LinkedList<K>) L.clone();
			for (; E.hasMoreElements();)
				L.remove(E.nextElement());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAll(Iterator<K> E)
	{
		if (E != null)
		{
			if (doClone())
				L = (LinkedList<K>) L.clone();
			for (; E.hasNext();)
				L.remove(E.next());
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAll(List<K> E)
	{
		if (E != null)
		{
			if (doClone())
				L = (LinkedList<K>) L.clone();
			for (final K o : E)
				L.remove(o);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized LinkedList<K> toLinkedList()
	{
		return (LinkedList<K>) L.clone();
	}

	public synchronized Vector<K> toVector()
	{
		final Vector<K> V = new Vector<K>(size());
		for (final K k : L)
			V.add(k);
		return V;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void add(int arg0, K arg1)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		L.add(arg0, arg1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean add(K arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.add(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean addAll(Collection<? extends K> arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.addAll(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean addAll(int arg0, Collection<? extends K> arg1)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.addAll(arg0, arg1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void addFirst(K arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		L.addFirst(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void addLast(K arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		L.addLast(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void clear()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		L.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized SLinkedList<K> copyOf()
	{
		final SLinkedList<K> SL = new SLinkedList<K>();
		SL.L = (LinkedList<K>) L.clone();
		return SL;
	}

	@Override
	public synchronized boolean contains(Object arg0)
	{
		return L.contains(arg0);
	}

	@Override
	public synchronized Iterator<K> descendingIterator()
	{
		return new SafeFeedbackIterator<K>(L.descendingIterator(), this);
	}

	@Override
	public synchronized K element()
	{
		return L.element();
	}

	@Override
	public synchronized K get(int arg0)
	{
		Log.errOut("SLinkedList", new Exception());
		return L.get(arg0);
	}

	@Override
	public synchronized K getFirst()
	{
		return L.getFirst();
	}

	@Override
	public synchronized K getLast()
	{
		return L.getLast();
	}

	@Override
	public synchronized int indexOf(Object arg0)
	{
		return L.indexOf(arg0);
	}

	@Override
	public synchronized int lastIndexOf(Object arg0)
	{
		return L.lastIndexOf(arg0);
	}

	@Override
	public synchronized ListIterator<K> listIterator(int arg0)
	{
		return new SafeFeedbackListIterator<K>(L.listIterator(arg0), this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean offer(K arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.offer(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean offerFirst(K arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.offerFirst(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean offerLast(K arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.offerLast(arg0);
	}

	@Override
	public synchronized K peek()
	{
		return L.peek();
	}

	@Override
	public synchronized K peekFirst()
	{
		return L.peekFirst();
	}

	@Override
	public synchronized K peekLast()
	{
		return L.peekLast();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K poll()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.poll();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K pollFirst()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.pollFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K pollLast()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.pollLast();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K pop()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.pop();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void push(K arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		L.push(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K remove()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.remove();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K remove(int arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.remove(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean remove(Object arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.remove(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K removeFirst()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.removeFirst();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean removeFirstOccurrence(Object arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.removeFirstOccurrence(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K removeLast()
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.removeLast();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean removeLastOccurrence(Object arg0)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.removeLastOccurrence(arg0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized K set(int arg0, K arg1)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.set(arg0, arg1);
	}

	@Override
	public synchronized int size()
	{
		return L.size();
	}

	@Override
	public synchronized Object[] toArray()
	{
		return L.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] arg0)
	{
		return L.toArray(arg0);
	}

	@Override
	public synchronized Iterator<K> iterator()
	{
		return new SafeFeedbackIterator<K>(L.iterator(), this);
	}

	@Override
	public boolean equals(Object arg0)
	{
		return this == arg0;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public synchronized ListIterator<K> listIterator()
	{
		return new SafeFeedbackListIterator<K>(L.listIterator(), this);
	}

	@Override
	public synchronized List<K> subList(int arg0, int arg1)
	{
		return new SafeChildList<K>(L.subList(arg0, arg1), this);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> c)
	{
		return L.containsAll(c);
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return L.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean removeAll(Collection<?> c)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.removeAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean retainAll(Collection<?> c)
	{
		if (doClone())
			L = (LinkedList<K>) L.clone();
		return L.retainAll(c);
	}

	@Override
	public String toString()
	{
		return super.toString();
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
