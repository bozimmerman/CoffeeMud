package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
   Copyright 2010-2020 Bo Zimmerman

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
public class ReadOnlyNavigableSet<K> implements NavigableSet<K>
{
	private final NavigableSet<K> set;
	public ReadOnlyNavigableSet(final NavigableSet<K> s)
	{
		set=s;
	}

	@Override
	public K ceiling(final K arg0)
	{
		return set.ceiling(arg0);
	}

	@Override
	public Iterator<K> descendingIterator()
	{
		return new ReadOnlyIterator<K>(set.descendingIterator());
	}

	@Override
	public NavigableSet<K> descendingSet()
	{
		return new ReadOnlyNavigableSet<K>(set.descendingSet());
	}

	@Override
	public K floor(final K arg0)
	{
		return set.floor(arg0);
	}

	@Override
	public SortedSet<K> headSet(final K arg0)
	{
		return new ReadOnlySortedSet<K>(set.headSet(arg0));
	}

	@Override
	public NavigableSet<K> headSet(final K arg0, final boolean arg1)
	{
		return new ReadOnlyNavigableSet<K>(set.headSet(arg0, arg1));
	}

	@Override
	public K higher(final K arg0)
	{
		return set.higher(arg0);
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ReadOnlyIterator<K>(set.iterator());
	}

	@Override
	public K lower(final K arg0)
	{
		return set.lower(arg0);
	}

	@Override
	public K pollFirst()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K pollLast()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public SortedSet<K> subSet(final K arg0, final K arg1)
	{
		return new ReadOnlySortedSet<K>(set.subSet(arg0,arg1));
	}

	@Override
	public NavigableSet<K> subSet(final K arg0, final boolean arg1, final K arg2, final boolean arg3)
	{
		return new ReadOnlyNavigableSet<K>(set.subSet(arg0,arg1,arg2,arg3));
	}

	@Override
	public SortedSet<K> tailSet(final K arg0)
	{
		return new ReadOnlySortedSet<K>(set.tailSet(arg0));
	}

	@Override
	public NavigableSet<K> tailSet(final K arg0, final boolean arg1)
	{
		return new ReadOnlyNavigableSet<K>(set.tailSet(arg0,arg1));
	}

	@Override
	public Comparator<? super K> comparator()
	{
		return set.comparator();
	}

	@Override
	public K first()
	{
		return set.first();
	}

	@Override
	public K last()
	{
		return set.last();
	}

	@Override
	public boolean add(final K arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(final Collection<? extends K> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean contains(final Object arg0)
	{
		return set.contains(arg0);
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		return set.containsAll(arg0);
	}

	@Override
	public boolean isEmpty()
	{
		return set.isEmpty();
	}

	@Override
	public boolean remove(final Object arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(final Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean retainAll(final Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public Object[] toArray()
	{
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		return set.toArray(arg0);
	}

}
