package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

/*
   Copyright 2010-2025 Bo Zimmerman

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
 * A pair of objects, named first and second
 * @param <T> the type of the first object
 * @param <K> the type of the second object
 */
public class Pair<T, K> implements Map.Entry<T, K>, Serializable
{
	private static final long serialVersionUID = 5801807195720264263L;
	public T	first;
	public K	second;

	/**
	 * Default constructor, initializes both elements to null
	 */
	public Pair()
	{
		first = null;
		second = null;
	}

	/**
	 * Constructor, initializes both elements to the given values
	 *
	 * @param frst the first element
	 * @param scnd the second element
	 */
	public Pair(final T frst, final K scnd)
	{
		first = frst;
		second = scnd;
	}

	/**
	 * A converter that converts a Pair to its first value.
	 */
	public static final class FirstConverter<T, K> implements Converter<Pair<T, K>, T>
	{
		@Override
		public T convert(final Pair<T, K> obj)
		{
			return obj.first;
		}
	}

	/**
	 * A converter that converts a Pair to its second value.
	 */
	public static final class SecondConverter<T, K> implements Converter<Pair<T, K>, K>
	{
		@Override
		public K convert(final Pair<T, K> obj)
		{
			return obj.second;
		}
	}

	/**
	 * A comparator that compares Pairs by their first element
	 * @param <T> the type of the first object
	 * @param <K> the type of the second object
	 */
	public static final class FirstComparator<T, K> implements Comparator<Pair<T, K>>
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(final Pair<T, K> arg0, final Pair<T, K> arg1)
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
				return ((Comparable)arg0.first).compareTo(arg1.first);
			return Integer.valueOf(arg0.first.hashCode()).compareTo(Integer.valueOf(arg1.first.hashCode()));
		}
	}

	/**
	 * A comparator that compares Pairs by their second element
	 *
	 * @param <T> the type of the first object
	 * @param <K> the type of the second object
	 */
	public static final class SecondComparator<T, K> implements Comparator<Pair<T, K>>
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(final Pair<T, K> arg0, final Pair<T, K> arg1)
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
				return ((Comparable)arg0.second).compareTo(arg1.second);
			return Integer.valueOf(arg0.second.hashCode()).compareTo(Integer.valueOf(arg1.second.hashCode()));
		}
	}

	/**
	 * A comparator that compares Pairs by their first element, and then by
	 * their second element
	 *
	 * @param <T> the type of the first object
	 * @param <K> the type of the second object
	 */
	public static final class DoubleComparator<T, K> implements Comparator<Pair<T, K>>
	{
		final FirstComparator<T, K>		fc	= new FirstComparator<T, K>();
		final SecondComparator<T, K>	sc	= new SecondComparator<T, K>();

		@Override
		public int compare(final Pair<T, K> arg0, final Pair<T, K> arg1)
		{
			final int f=fc.compare(arg0, arg1);
			if(f!=0)
				return f;
			return sc.compare(arg0, arg1);
		}
	}

	/**
	 * Returns the first element of the pair
	 *
	 * @return the first element
	 */
	@Override
	public T getKey()
	{
		return first;
	}

	/**
	 * Returns the second element of the pair
	 *
	 * @return the second element
	 */
	@Override
	public K getValue()
	{
		return second;
	}

	/**
	 * Sets the second element of the pair
	 *
	 * @param value the new value for the second element
	 * @return the value that was set
	 */
	@Override
	public K setValue(final K value)
	{
		second = value;
		return value;
	}

	/**
	 * Standard equals method - true if both first and second elements are
	 * equal
	 *
	 * @param o the other object
	 * @return true if equal, false otherwise
	 */
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

	/**
	 * Standard hashcode method - a combination of the hashcodes of the first
	 * and second elements
	 *
	 * @return the hashcode
	 */
	@Override
	public int hashCode()
	{
		return ((first == null) ? 0 : first.hashCode()) ^ ((second == null) ? 0 : second.hashCode());
	}
}
