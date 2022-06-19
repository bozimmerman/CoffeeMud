package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;

import com.planet_ink.coffee_mud.core.collections.LinkedCollection.LinkedEntry;

/*
   Copyright 2022-2022 Bo Zimmerman

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
public class MultiValueMap<K,J> implements Map<K,J>
{
	private final Map<K,List<J>>		map;
	private final Class<?>				collectionClass;
	private final LinkedList<List<J>>	factory			= new LinkedList<List<J>>();
	private final MultiValueMap<K,J>	me				= this;

	@SuppressWarnings("rawtypes" )
	private static final Iterator	empty				= EmptyIterator.INSTANCE;

	private final ListGetIterator.ListIteratorRemover<Object,J> remover = new ListGetIterator.ListIteratorRemover<Object,J>()
	{
		@Override
		public void remove(final Object context, final J j)
		{
			@SuppressWarnings("unchecked")
			final K key = (K)context;
			final List<J> coll = map.get(key);
			if((coll != null) && (coll.size()==0))
				me.remove(key);
		}
	};


	public MultiValueMap()
	{
		map = new Hashtable<K,List<J>>();
		collectionClass = java.util.Vector.class;
	}

	public MultiValueMap(final Map<K,List<J>> baseMap, final Class<?> collectionClass)
	{
		map = baseMap;
		this.collectionClass = collectionClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized J put(final K key, final J value)
	{
		if((key == null)||(value==null))
			throw new NullPointerException();
		if(map.containsKey(key))
			map.get(key).add(value);
		else
		{
			if(factory.size()>0)
			{
				synchronized(factory)
				{
					if(factory.size()>0)
					{
						final List<J> newList=factory.removeFirst();
						newList.add(value);
						map.put(key, newList);
					}
				}
			}
			try
			{
				final List<J> newList = (List<J>)collectionClass.newInstance();
				newList.add(value);
				map.put(key, newList);
			}
			catch (final Exception e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		return value;
	}

	@Override
	public synchronized void putAll(final Map<? extends K, ? extends J> map)
	{
		for(final Map.Entry<? extends K, ? extends J> e : map.entrySet())
			put(e.getKey(),e.getValue());
	}

	@SuppressWarnings("unchecked")
	public synchronized Iterator<J> iterator(final K key)
	{
		if((size()>0)&&(map.containsKey(key)))
		{
			final List<J> lst = map.get(key);
			if((lst != null)&&(lst.size()>0))
				return new ListGetIterator<J>(lst,remover,key);
		}
		return empty;
	}

	@SuppressWarnings("unchecked")
	public synchronized Iterator<K> keyIterator()
	{
		if(size()==0)
			return empty;
		final SLinkedList<K> list = new SLinkedList<K>(map.keySet());
		return new Iterator<K>()
		{
			final Iterator<K> i =list.iterator();
			volatile K lastK = null;

			@Override
			public boolean hasNext()
			{
				return i.hasNext();
			}

			@Override
			public K next()
			{
				lastK = i.next();
				return lastK;
			}

			@Override
			public void remove()
			{
				i.remove();
				me.remove(lastK);
			}
		};
	}

	@Override
	public synchronized J remove(final Object key)
	{
		if(map.containsKey(key))
		{
			final List<J> lst = map.remove(key);
			synchronized(factory)
			{
				if(lst != null)
					factory.addLast(lst);
			}
			if((lst!=null)&&(lst.size()>0))
				return lst.get(0);
		}
		return null;
	}

	@Override
	public synchronized void clear()
	{
		map.clear();
	}

	@Override
	public synchronized int size()
	{
		return map.size();
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public synchronized boolean containsKey(final Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public synchronized boolean containsValue(final Object value)
	{
		for(final Iterator<K> k = keyIterator();k.hasNext();)
		{
			for(final Iterator<J> i = iterator(k.next());i.hasNext();)
			{
				if(i.next()==value)
					return true;
			}
		}
		return false;
	}

	@Override
	public synchronized J get(final Object key)
	{
		if(map.containsKey(key))
		{
			final List<J> list = map.get(key);
			if(list == null)
				return null;
			try
			{
				return list.get(0);
			}
			catch(final Exception e)
			{}
		}
		return null;
	}

	@Override
	public synchronized Set<K> keySet()
	{
		return map.keySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized Collection<J> values()
	{
		List<J> newList;
		try
		{
			newList = (List<J>)collectionClass.newInstance();
		}
		catch (final Exception e)
		{
			newList = new Vector<J>(size());
		}
		for(final Iterator<K> k = keyIterator();k.hasNext();)
		{
			for(final Iterator<J> i = iterator(k.next());i.hasNext();)
				newList.add(i.next());
		}
		return newList;
	}

	@Override
	public synchronized Set<Entry<K, J>> entrySet()
	{
		final Set<Entry<K, J>> newList = new TreeSet<Entry<K, J>>();
		for(final Iterator<K> k = keyIterator();k.hasNext();)
		{
			final K key = k.next();
			for(final Iterator<J> i = iterator(key);i.hasNext();)
			{
				final J val = i.next();
				newList.add(new Map.Entry<K, J>(){
					private final K k = key;
					private J j = val;

					@Override
					public K getKey()
					{
						return k;
					}

					@Override
					public J getValue()
					{
						return j;
					}

					@Override
					public J setValue(final J value)
					{
						final J oj = j;
						j=value;
						if(map.containsKey(k))
						{
							final List<J> list = map.get(k);
							if(list != null)
							{
								try
								{
									list.remove(j);
									list.add(value);
								}
								catch(final Exception e)
								{}
							}
						}
						return oj;
					}
				});
			}
		}
		return newList;
	}
}
