package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;
/*
	Copyright 2015-2020 Bo Zimmerman

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
public class SafeChildSet<K> implements Set<K>
{
	private final Set<K> set;
	private final SafeCollectionHost host;

	public SafeChildSet(final Set<K> set, final SafeCollectionHost host)
	{
		this.set = set;
		this.host = host;
	}

	@Override
	public int size()
	{
		return set.size();
	}

	@Override
	public boolean isEmpty()
	{
		return set.isEmpty();
	}

	@Override
	public boolean contains(final Object o)
	{
		return set.contains(o);
	}

	@Override
	public Iterator<K> iterator()
	{
		return new SafeFeedbackIterator<K>(set.iterator(), host);
	}

	@Override
	public Object[] toArray()
	{
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a)
	{
		return set.toArray(a);
	}

	@Override
	public boolean add(final K e)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean remove(final Object o)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.UnsupportedOperationException();
	}
}
