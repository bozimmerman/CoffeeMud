package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

/*
   Copyright 2012-2025 Bo Zimmerman

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
public class SortedStrSVector<T> extends SVector<T> implements SearchIDList<T>
{
	private static final long	serialVersionUID	= 6687178785122361992L;
	private final Str<T>	  stringer;

	public static interface Str<T>
	{
		public String toString(T t);
	}

	public SortedStrSVector(final Str<T> stringer, final int size)
	{
		super(size);
		this.stringer = stringer;
	}

	public SortedStrSVector(final Str<T> stringer)
	{
		super();
		this.stringer = stringer;
	}

	private int compareTo(final T arg0, final String arg1)
	{
		return stringer.toString(arg0).compareToIgnoreCase(arg1);
	}

	private int compareTo(final T arg0, final T arg1)
	{
		return stringer.toString(arg0).compareToIgnoreCase(stringer.toString(arg1));
	}

	@Override
	public Iterator<String> keyIterator()
	{
		return new ConvertingIterator<T,String>(this.iterator(),new Converter<T,String>()
		{
			@Override
			public String convert(final T obj)
			{
				if(obj!=null)
					return stringer.toString(obj);
				return null;
			}
		});
	}

	@Override
	public synchronized boolean add(final T arg0)
	{
		if (arg0 == null)
			return false;
		if (size() == 0)
			return super.add(arg0);
		int start = 0;
		int end = size() - 1;
		int comp = -1;
		int mid = -1;
		while (start <= end)
		{
			mid = (end + start) / 2;
			comp = compareTo(super.get(mid), arg0);
			if (comp == 0)
				break;
			else
			if (comp > 0)
				end = mid - 1;
			else
				start = mid + 1;
		}
		if (comp == 0)
			super.add(mid, arg0);
		else
		if (comp > 0)
		{
			while ((mid >= 0) && (compareTo(super.get(mid), arg0) > 0))
				mid--;
			if (mid >= size() - 1)
				super.add(arg0);
			else
			if (mid < 0)
				super.add(0, arg0);
			else
				super.add(mid + 1, arg0);
		}
		else
		{
			while ((mid < size()) && (compareTo(super.get(mid), arg0) < 0))
				mid++;
			if (mid >= size())
				super.add(arg0);
			else
				super.add(mid, arg0);
		}
		return true;
	}

	@Override
	public void add(final int arg0, final T arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int arg0, final Collection<? extends T> arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean contains(final Object arg0)
	{
		return indexOf(arg0) >= 0;
	}

	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		for (final Object o : arg0)
		{
			if (!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public T get(final int arg0)
	{
		return super.get(arg0);
	}

	public synchronized void reSort(final T arg0)
	{
		if (super.contains(arg0))
		{
			super.remove(arg0);
			this.add(arg0);
		}
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized int indexOf(final Object arg0)
	{
		if (arg0 == null)
			return -1;
		if (size() == 0)
			return -1;
		int start = 0;
		int end = size() - 1;
		if (arg0 instanceof CMObject)
		{
			while (start <= end)
			{
				final int mid = (end + start) / 2;
				final int comp = compareTo(super.get(mid), (T) arg0);
				if (comp == 0)
					return mid;
				else
				if (comp > 0)
					end = mid - 1;
				else
					start = mid + 1;

			}
		}
		else
		if (arg0 instanceof String)
		{
			while (start <= end)
			{
				final int mid = (end + start) / 2;
				final int comp = compareTo(super.get(mid), (String) arg0);
				if (comp == 0)
					return mid;
				else
				if (comp > 0)
					end = mid - 1;
				else
					start = mid + 1;

			}
		}
		return -1;
	}

	@Override
	public synchronized T find(final String arg0)
	{
		if (arg0 == null)
			return null;
		if (size() == 0)
			return null;
		int start = 0;
		int end = size() - 1;
		while (start <= end)
		{
			final int mid = (end + start) / 2;
			final int comp = compareTo(super.get(mid), arg0);
			if (comp == 0)
				return super.get(mid);
			else
			if (comp > 0)
				end = mid - 1;
			else
				start = mid + 1;
		}
		return null;
	}

	@Override
	public synchronized T find(final T arg0)
	{
		if (arg0 == null)
			return null;
		if (size() == 0)
			return null;
		int start = 0;
		int end = size() - 1;
		while (start <= end)
		{
			final int mid = (end + start) / 2;
			final int comp = compareTo(super.get(mid), arg0);
			if (comp == 0)
				return super.get(mid);
			else
			if (comp > 0)
				end = mid - 1;
			else
				start = mid + 1;

		}
		return null;
	}

	@Override
	public synchronized int lastIndexOf(final Object arg0)
	{
		return indexOf(arg0); // only holds one-of-a-kind, so all is well!
	}

	@Override
	public synchronized boolean remove(final Object arg0)
	{
		final int index = indexOf(arg0);
		if (index >= 0)
			return remove(index) == arg0;
		return false;
	}

	@Override
	public T set(final int arg0, final T arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}
}
