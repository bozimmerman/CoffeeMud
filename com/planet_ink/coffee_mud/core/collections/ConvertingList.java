package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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
public class ConvertingList<L,K> implements List<K>
{
	private final List<L> list;
	Converter<L, K> converter;

	public ConvertingList(List<L> l, Converter<L, K> conv)
	{
		list=l;
		converter=conv;
	}

	@Override
	public boolean add(K arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void add(int arg0, K arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(Collection<? extends K> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends K> arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean contains(Object arg0)
	{
		for(int x=0;x<size();x++)
		{
			if((arg0==null)?get(x)==null:arg0.equals(get(x)))
				return true;
		}
		return list.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		for(final Object o : arg0)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public K get(int arg0)
	{
		return converter.convert(list.get(arg0));
	}

	@Override
	public int indexOf(Object arg0)
	{
		for(int x=0;x<size();x++)
		{
			if((arg0==null)?get(x)==null:arg0.equals(get(x)))
				return x;
		}
		return list.lastIndexOf(arg0);
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
	public int lastIndexOf(Object arg0)
	{
		for(int x=size()-1;x>=0;x--)
		{
			if((arg0==null)?get(x)==null:arg0.equals(get(x)))
				return x;
		}
		return list.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<K> listIterator()
	{
		return new ConvertingListIterator<L,K>(list.listIterator(), converter);
	}

	@Override
	public ListIterator<K> listIterator(int arg0)
	{
		return new ConvertingListIterator<L,K>(list.listIterator(arg0), converter);
	}

	@Override
	public boolean remove(Object arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K remove(int arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K set(int arg0, K arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public List<K> subList(int arg0, int arg1)
	{
		return new ConvertingList<L,K>(list.subList(arg0,arg1),converter);
	}

	@Override
	public Object[] toArray()
	{
		final Object[] obj=new Object[list.size()];
		for(int x=obj.length-1;x>=0;x--)
			obj[x]=converter.convert(list.get(x));
		return obj;
	}

	@SuppressWarnings("unchecked")

	@Override
	public <T> T[] toArray(T[] a) 
	{
		if (a.length < list.size())
			return (T[]) Arrays.copyOf(toArray(), list.size(), a.getClass());
		System.arraycopy(toArray(), 0, a, 0, list.size());
		if (a.length > list.size())
			a[list.size()] = null;
		return a;
	}
}
