package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Iterator;
/*
	Copyright 2015-2025 Bo Zimmerman

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
public class SafeChildCollection<K> implements Collection<K>
{
	private final Collection<K> collection;
	private final SafeCollectionHost host;

	public SafeChildCollection(final Collection<K> collection, final SafeCollectionHost host)
	{
		this.collection = collection;
		this.host = host;
	}

	@Override
	public int size()
	{
		return collection.size();
	}

	@Override
	public boolean isEmpty()
	{
		return collection.isEmpty();
	}

	@Override
	public boolean contains(final Object o)
	{
		return collection.contains(o);
	}

	@Override
	public Iterator<K> iterator()
	{
		return new SafeFeedbackIterator<K>(collection.iterator(),host);
	}

	@Override
	public Object[] toArray()
	{
		return collection.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a)
	{
		return collection.toArray(a);
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
		return collection.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends K> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.UnsupportedOperationException();
	}
}
