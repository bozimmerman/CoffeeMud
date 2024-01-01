package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.OperationNotSupportedException;

/*
   Copyright 2022-2024 Bo Zimmerman

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
 * A List wrapper that attempts to keep any elements that do not
 * pass the filter OUT of the list, removing them during iterations
 * so they stay out.  This means size() can change unexpectedly,
 * making numeric iterations tricky, and indexOf very fluid.
 *
 * @author Bo Zimmerman
 *
 * @param <T> the type of list to wrap
 */
public class FilteredListWrapper<T> implements List<T>
{
	private final Filterer<T>	filter;
	private final List<T>		list;

	public FilteredListWrapper(final List<T> list, final Filterer<T> filter)
	{
		super();
		this.list = list;
		this.filter = filter;
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public boolean contains(final Object o)
	{
		if((o == null) || (!list.contains(o)))
			return false;
		@SuppressWarnings("unchecked")
		final T t = (T)o;
		if(filter.passesFilter(t))
			return true;
		list.remove(o);
		return false;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new FilteredIterator<T>(list.iterator(), filter, true);
	}

	@Override
	public Object[] toArray()
	{
		final List<Object> newList = new ArrayList<Object>(list.size());
		// this is done because it auto-filters
		for(final Iterator<T> i = iterator(); i.hasNext();)
			newList.add(i.next());
		return newList.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F> F[] toArray(F[] a)
	{
		final Object[] os = toArray();
		if(os.length>a.length)
			a = (F[])new Object[os.length];
		for(int i=0;i<os.length;i++)
			a[i] = (F)os[i];
		return a;
	}

	@Override
	public boolean add(final T e)
	{
		if((e != null) && (filter.passesFilter(e)))
			return list.add(e);
		return false;
	}

	@Override
	public void add(final int index, final T element)
	{
		if((element != null) && (filter.passesFilter(element)))
			list.add(index, element);
	}

	@Override
	public boolean remove(final Object o)
	{
		return list.remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		if(c == null)
			return false;
		for(final Object o : c)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends T> c)
	{
		return list.addAll(c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> c)
	{
		return list.addAll(index, c);
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return list.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return list.retainAll(c);
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public T get(final int index)
	{
		final T t = list.get(index);
		if(!filter.passesFilter(t))
		{
			list.remove(index);
			if(index < size())
				return get(index);
			return null;
		}
		return t;
	}

	@Override
	public T set(final int index, final T element)
	{
		if(element != null)
		{
			if(!filter.passesFilter(element))
				return null;
			return list.set(index, element);
		}
		return null;
	}

	@Override
	public T remove(final int index)
	{
		final T t = list.remove(index);
		if(!filter.passesFilter(t))
			return null;
		return t;
	}

	@Override
	public int indexOf(final Object o)
	{
		if(o == null)
			return -1;
		final int index = list.indexOf(o);
		if(index < 0)
			return index;
		try
		{
			final T t = list.get(index);
			if(o.equals(t) && (filter.passesFilter(t)))
				return index;
			return -1;
		}
		catch(final Exception e)
		{
			return indexOf(o);
		}
	}

	@Override
	public int lastIndexOf(final Object o)
	{
		if(o == null)
			return -1;
		final int index = list.lastIndexOf(o);
		if(index < 0)
			return index;
		try
		{
			final T t = list.get(index);
			if(o.equals(t) && (filter.passesFilter(t)))
				return index;
			return -1;
		}
		catch(final Exception e)
		{
			return indexOf(o);
		}
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return new FilteredListIterator<T>(list.listIterator(), filter, true);
	}

	@Override
	public ListIterator<T> listIterator(final int index)
	{
		return new FilteredListIterator<T>(list.listIterator(index), filter, true);
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex)
	{
		return new FilteredListWrapper<T>(list.subList(fromIndex, toIndex), filter);
	}

}
