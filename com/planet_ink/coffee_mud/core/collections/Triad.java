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
public class Triad<T, K, L> extends Pair<T, K>
{
	public L	third;

	public Triad(T frst, K scnd, L thrd)
	{
		super(frst, scnd);
		third = thrd;
	}

	public static final class FirstConverter<T, K, L> implements Converter<Triad<T, K, L>, T>
	{
		@Override
		public T convert(Triad<T, K, L> obj)
		{
			return obj.first;
		}
	}

	public static final class SecondConverter<T, K, L> implements Converter<Triad<T, K, L>, K>
	{
		@Override
		public K convert(Triad<T, K, L> obj)
		{
			return obj.second;
		}
	}

	public static final class ThirdConverter<T, K, L> implements Converter<Triad<T, K, L>, L>
	{
		@Override
		public L convert(Triad<T, K, L> obj)
		{
			return obj.third;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (o instanceof Triad)
		{
			@SuppressWarnings("rawtypes")
			final Triad p = (Triad) o;
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
