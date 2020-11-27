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
public class Pair<T, K> implements Map.Entry<T, K>
{
	public T	first;
	public K	second;

	public Pair()
	{
		first = null;
		second = null;
	}

	public Pair(final T frst, final K scnd)
	{
		first = frst;
		second = scnd;
	}

	public static final class FirstConverter<T, K> implements Converter<Pair<T, K>, T>
	{
		@Override
		public T convert(final Pair<T, K> obj)
		{
			return obj.first;
		}
	}

	public static final class SecondConverter<T, K> implements Converter<Pair<T, K>, K>
	{
		@Override
		public K convert(final Pair<T, K> obj)
		{
			return obj.second;
		}
	}

	public static final class FirstComparator<T, K> implements Comparator<Pair<T, K>>
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Pair<T, K> arg0, Pair<T, K> arg1)
		{
			if(arg0==null)
			{
				if(arg1==null)
					return 0;
				return -1;
			}
			else
			if(arg1==null)
				return 1;
			if(arg0.first==null)
			{
				if(arg1.first==null)
					return 0;
				return -1;
			}
			else
			if(arg1.first==null)
				return 1;
			if((arg0.first instanceof Comparable)&&(arg1.first instanceof Comparable))
				return ((Comparable)arg0.first).compareTo((Comparable)arg1.first);
			return Integer.valueOf(arg0.first.hashCode()).compareTo(Integer.valueOf(arg1.first.hashCode()));
		}
	}

	public static final class SecondComparator<T, K> implements Comparator<Pair<T, K>>
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Pair<T, K> arg0, Pair<T, K> arg1)
		{
			if(arg0==null)
			{
				if(arg1==null)
					return 0;
				return -1;
			}
			else
			if(arg1==null)
				return 1;
			if(arg0.second==null)
			{
				if(arg1.second==null)
					return 0;
				return -1;
			}
			else
			if(arg1.second==null)
				return 1;
			if((arg0.second instanceof Comparable)&&(arg1.second instanceof Comparable))
				return ((Comparable)arg0.second).compareTo((Comparable)arg1.second);
			return Integer.valueOf(arg0.second.hashCode()).compareTo(Integer.valueOf(arg1.second.hashCode()));
		}
	}

	@Override
	public T getKey()
	{
		return first;
	}

	@Override
	public K getValue()
	{
		return second;
	}

	@Override
	public K setValue(final K value)
	{
		second = value;
		return value;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;
		if (o instanceof Pair)
		{
			final Pair<?,?> p = (Pair<?,?>) o;
			return ((p.first == first) || ((p.first != null) && (p.first.equals(first)))) && ((p.second == second) || ((p.second != null) && (p.second.equals(second))));
		}
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return ((first == null) ? 0 : first.hashCode()) ^ ((second == null) ? 0 : second.hashCode());
	}
}
