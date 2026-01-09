package com.planet_ink.coffee_mud.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.planet_ink.coffee_mud.core.collections.LinkedSet.LinkedEntry;
/*
   Copyright 2013-2026 Bo Zimmerman

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
 * A simple linked list implementation of a TreeMap, that also maintains the order
 * in which entries were added.
 *
 * @param <K> the type of key
 * @param <J> the type of value
 * @author Bo Zimmerman
 */
public class TreeList<J> implements List<J>,  Iterable<J>
{
	private final List<J>							coll;
	private final TreeMap<Comparable<?>, List<J>>	map		= new TreeMap<Comparable<?>, List<J>>();
	/**
	 * A converter that converts a J to its comparable value.
	 */
	private Converter<J, Comparable<?>> converter;

	@SuppressWarnings("rawtypes" )
	private static final Iterator empty=EmptyIterator.INSTANCE;


	public TreeList(final Converter<J, Comparable<?>> conv)
	{
		if (conv != null)
			converter = conv;
		else
			throw new IllegalArgumentException();
		coll = new ArrayList<J>();
	}

	public TreeList(final Converter<J, Comparable<?>> conv, final int initialCapacity)
	{
		if (conv != null)
			converter = conv;
		else
			throw new IllegalArgumentException();
		coll = new ArrayList<J>(initialCapacity);
	}

	/**
	 * An iterator for the values in the map.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<J> iterator()
	{
		if(size()==0)
			return empty;
		return new Iterator<J>()
		{
			private final Iterator<J> it = coll.iterator();
			private J				last=null;

			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public J next()
			{
				last = it.next();
				return last;
			}

			@Override
			public void remove()
			{
				if(last != null)
				{
					synchronized (TreeList.this)
					{
						it.remove();
					}
					final Comparable<?> key = converter.convert(last);
					final List<J> lst = map.get(key);
					if (lst == null)
						return;
					if((lst.remove(last)) && (lst.size()==0))
						map.remove(key);
				}
			}
		};
	}

	@Override
	public synchronized int size()
	{
		return coll.size();
	}

	@Override
	public boolean isEmpty()
	{
		return size()==0;
	}

	/**
	 * Returns a set of the keys in the map.
	 *
	 * @return a set of the keys in the map.
	 */
	public synchronized List<J> getValuesByKey(final Comparable<?> key)
	{
		final List<J> lst = map.get(key);
		if(lst == null)
			return null;
		return new ReadOnlyList<J>(lst);
	}

	/**
	 * Returns the first value associated with the given key.
	 *
	 * @param key the key
	 * @return the first value associated with the given key.
	 */
	public synchronized J getFirstByKey(final Comparable<?> key)
	{
		final List<J> lst = map.get(key);
		if (lst == null)
			return null;
		return lst.get(0);
	}

	private boolean containsKey(final Comparable<?> key)
	{
		return map.containsKey(key);
	}

	private boolean containsValue(final J value)
	{
		final Comparable<?> key=converter.convert(value);
		final List<J> lst = map.get(key);
		if(lst == null)
			return false;
		return lst.contains(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean contains(final Object o)
	{
		try
		{
			return containsValue((J)o);
		}
		catch (final ClassCastException e)
		{
			if(o instanceof Comparable<?>)
				return containsKey((Comparable<?>)o);
			return false;
		}
	}

	@Override
	public Object[] toArray()
	{
		return coll.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a)
	{
		return coll.toArray(a);
	}

	@Override
	public synchronized boolean add(final J e)
	{
		final boolean found = coll.add(e);
		if(found)
		{
			final Comparable<?> key=converter.convert(e);
			List<J> lst = map.get(key);
			if (lst == null)
			{
				lst = new LinkedList<J>();
				map.put(key, lst);
			}
			lst.add(e);
		}
		return found;
	}

	@Override
	public synchronized boolean remove(final Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			final Comparable<?> key = converter.convert((J)o);
			if (!map.containsKey(key))
				return false;
			final boolean found = coll.remove(o);
			final List<J> lst = map.get(key);
			if (lst.remove(o) && (lst.size()==0))
				map.remove(key);
			return found;
		}
		catch (final ClassCastException e)
		{
			return false;
		}
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		boolean found=true;
		for (final Object o : c)
			found = found && contains(o);
		return found;
	}

	@Override
	public boolean addAll(final Collection<? extends J> c)
	{
		boolean changed=false;
		for (final J o : c)
			changed = add(o) || changed;
		return changed;
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		boolean changed=false;
		for (final Iterator<J> i = iterator(); i.hasNext();)
		{
			final J o = i.next();
			if (!c.contains(o))
			{
				i.remove();
				changed=true;
			}
		}
		return changed;
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		boolean changed=false;
		for (final Object o : c)
			changed = remove(o) || changed;
		return changed;
	}

	@Override
	public synchronized void clear()
	{
		coll.clear();
		map.clear();
	}

	@Override
	public synchronized boolean addAll(int index, final Collection<? extends J> c)
	{
		for (final J o : c)
			add(index++, o);
		return c.size()>0;
	}

	@Override
	public synchronized  J get(final int index)
	{
		return coll.get(index);
	}

	@Override
	public synchronized J set(final int index, final J element)
	{
		final J j = remove(index);
		add(index, element);
		return j;
	}

	@Override
	public synchronized void add(final int index, final J element)
	{
		coll.add(index, element);
		final Comparable<?> key=converter.convert(element);
		List<J> lst = map.get(key);
		if (lst == null)
		{
			lst = new LinkedList<J>();
			map.put(key, lst);
		}
		lst.add(element);
	}

	@Override
	public synchronized J remove(final int index)
	{
		final J o = coll.remove(index); // let it throw an exception if index is bad
		if (o == null)
			return null;
		final Comparable<?> key = converter.convert(o);
		final List<J> lst = map.get(key);
		if (lst == null)
			return o;
		lst.remove(o);
		if (lst.size() == 0)
			map.remove(key);
		return o;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized int indexOf(final Object o)
	{
		try
		{
			final Comparable<?> key = converter.convert((J)o);
			if (!map.containsKey(key))
				return -1;
		}
		catch (final ClassCastException e)
		{
			return -1;
		}
		return coll.indexOf(o);
	}

	@Override
	public synchronized int lastIndexOf(final Object o)
	{
		try
		{
			@SuppressWarnings("unchecked")
			final Comparable<?> key = converter.convert((J)o);
			if (!map.containsKey(key))
				return -1;
		}
		catch (final ClassCastException e)
		{
			return -1;
		}
		return coll.lastIndexOf(o);
	}

	@Override
	public ListIterator<J> listIterator()
	{
		return listIterator(0);
	}

	@Override
	public ListIterator<J> listIterator(final int index)
	{
		return new ListIterator<J>()
		{
			volatile int previ = index-1;
			volatile int i = index;
			@Override
			public boolean hasNext()
			{
				return i< size();
			}

			@Override
			public J next()
			{
				previ=i;
				return get(i++);
			}

			@Override
			public boolean hasPrevious()
			{
				return i>0;
			}

			@Override
			public J previous()
			{
				previ=i;
				return get(--i);
			}

			@Override
			public int nextIndex()
			{
				return i;
			}

			@Override
			public int previousIndex()
			{
				return i-1;
			}

			@Override
			public void remove()
			{
				if(previ>=0)
				{
					TreeList.this.remove(previ);
					if(previ < i)
					{
						i--;
						previ--;
					}
				}
			}

			@Override
			public void set(final J e)
			{
				if(previ>=0)
					TreeList.this.set(previ, e);
			}

			@Override
			public void add(final J e)
			{
				TreeList.this.add(i,e);
				i++;
				previ++;
			}
		};
	}

	@Override
	public List<J> subList(final int fromIndex, final int toIndex)
	{
		throw new java.lang.UnsupportedOperationException();
	}
}
