package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class ConvertingCollection<L,K> implements Collection<K>
{
	private final Collection<L> list;
	Converter<L, K> converter;

	public ConvertingCollection(final Collection<L> l, final Converter<L, K> conv)
	{
		list=l;
		converter=conv;
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
		list.clear();
	}

	@Override
	public boolean contains(final Object arg0)
	{
		for(final L o : list)
		{
			if(arg0 == o)
				return true;
			if(o == null && arg0 == null)
				return true;
			if(arg0 == converter.convert(o))
				return true;
		}
		return list.contains(arg0);
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		for(final Object o : arg0)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new ConvertingIterator<L, K>(list.iterator(),converter);
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
		return list.size();
	}

	@Override
	public Object[] toArray()
	{
		final Object[] obj=new Object[list.size()];
		final int x=0;
		for(final L o : list)
			obj[x]=converter.convert(o);
		return obj;
	}

	@SuppressWarnings("unchecked")

	@Override
	public <T> T[] toArray(final T[] a)
	{
		if (a.length < list.size())
			return (T[]) Arrays.copyOf(toArray(), list.size(), a.getClass());
		System.arraycopy(toArray(), 0, a, 0, list.size());
		if (a.length > list.size())
			a[list.size()] = null;
		return a;
	}
}
