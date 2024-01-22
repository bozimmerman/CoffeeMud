package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

import com.planet_ink.coffee_mud.core.collections.Pair.FirstComparator;
import com.planet_ink.coffee_mud.core.collections.Pair.SecondComparator;

/*
   Copyright 2010-2024 Bo Zimmerman

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
public class Triad<T, K, L> extends Pair<T, K>
{
	private static final long serialVersionUID = 3227647705379969966L;
	public L	third;

	public Triad(final T frst, final K scnd, final L thrd)
	{
		super(frst, scnd);
		third = thrd;
	}

	public static final class FirstConverter<T, K, L> implements Converter<Triad<T, K, L>, T>
	{
		@Override
		public T convert(final Triad<T, K, L> obj)
		{
			return obj.first;
		}
	}

	public static final class SecondConverter<T, K, L> implements Converter<Triad<T, K, L>, K>
	{
		@Override
		public K convert(final Triad<T, K, L> obj)
		{
			return obj.second;
		}
	}

	public static final class ThirdConverter<T, K, L> implements Converter<Triad<T, K, L>, L>
	{
		@Override
		public L convert(final Triad<T, K, L> obj)
		{
			return obj.third;
		}
	}

	public static final class ThirdComparator<T, K, L> implements Comparator<Triad<T, K, L>>
	{
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(final Triad<T, K, L> arg0, final Triad<T, K, L> arg1)
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
			if(arg0.third==null)
			{
				if(arg1.third==null)
					return 0;
				return -1;
			}
			else
			if(arg1.third==null)
				return 1;
			if((arg0.third instanceof Comparable)&&(arg1.third instanceof Comparable))
				return ((Comparable)arg0.third).compareTo(arg1.third);
			return Integer.valueOf(arg0.third.hashCode()).compareTo(Integer.valueOf(arg1.third.hashCode()));
		}
	}

	public static final class TripleComparator<T, K, L> implements Comparator<Triad<T, K, L>>
	{
		final FirstComparator<T, K>		fc	= new FirstComparator<T, K>();
		final SecondComparator<T, K>	sc	= new SecondComparator<T, K>();
		final ThirdComparator<T, K, L>	tc	= new ThirdComparator<T, K, L>();

		@Override
		public int compare(final Triad<T, K, L> arg0, final Triad<T, K, L> arg1)
		{
			final int f=fc.compare(arg0, arg1);
			if(f!=0)
				return f;
			final int s = sc.compare(arg0, arg1);
			if(s!=0)
				return s;
			return tc.compare(arg0, arg1);
		}
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;
		if (o instanceof Triad)
		{
			final Triad<?,?,?> p = (Triad<?,?,?>) o;
			return ((p.first == first) || ((p.first != null) && (p.first.equals(first)))) && ((p.second == second) || ((p.second != null) && (p.second.equals(second))))
					&& ((p.third == third) || ((p.third != null) && (p.third.equals(third))));
		}
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ ((third == null) ? 0 : third.hashCode());
	}
}
