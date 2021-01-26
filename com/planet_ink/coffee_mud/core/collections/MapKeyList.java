package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2012-2021 Bo Zimmerman

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
public class MapKeyList<K, L> extends XVector<K>
{
	private static final long serialVersionUID = -1879194850201942089L;
	private final Map<K, L> m;

	public MapKeyList(final Map<K, L> map)
	{
		super((map==null)?0:map.size(),true);
		if (map != null)
		{
			for (final K o : map.keySet())
				super.add(o);
		}
		this.m=map;
	}
	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(m.keySet().iterator());
	}

	@Override
	public synchronized boolean add(final K e)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean remove(final Object o)
	{
		if(remove(o))
			return m.remove(o) != null;
		return false;
	}

	@Override
	public synchronized boolean addAll(final Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public synchronized boolean addAll(final int index, final Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public synchronized boolean removeAll(final Collection<?> c)
	{
		if(c==null)
			return false;
		for(final Object o : c)
			this.remove(o);
		return true;
	}

	@Override
	public synchronized boolean retainAll(final Collection<?> c)
	{
		if(c==null)
			return false;
		final List<K> removeThese = new ArrayList<K>();
		for(final K key : this)
		{
			if(!c.contains(key))
				removeThese.add(key);
		}
		for(final K key : removeThese)
			remove(key);
		return true;
	}

	@Override
	public void clear()
	{
		this.clear();
		m.clear();
	}

	@Override
	public synchronized K set(final int index, final K element)
	{
		if((index<0)||(index>=size()))
			throw new IndexOutOfBoundsException();
		final K key = super.set(index, element);
		if (key != null)
		{
			final L obj = m.remove(key);
			if(obj != null)
				m.put(element, obj);
		}
		return key;
	}

	@Override
	public void add(final int index, final K element)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public synchronized K remove(final int index)
	{
		final K key = super.remove(index);
		if(key != null)
			m.remove(key);
		return key;
	}

	@Override
	public ListIterator<K> listIterator()
	{
		return new ReadOnlyListIterator<K>(super.listIterator());
	}

	@Override
	public ListIterator<K> listIterator(final int index)
	{
		return new ReadOnlyListIterator<K>(super.listIterator());
	}

	@Override
	public synchronized List<K> subList(final int fromIndex, final int toIndex)
	{
		throw new java.lang.UnsupportedOperationException();
	}
}
