package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2012-2018 Bo Zimmerman

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
public class Quad<T, K, L, M> extends Triad<T, K, L>
{
	public M	fourth;

	public Quad(T frst, K scnd, L thrd, M frth)
	{
		super(frst, scnd, thrd);
		fourth = frth;
	}

	public static final class FirstConverter<T, K, L, M> implements Converter<Quad<T, K, L, M>, T>
	{
		@Override
		public T convert(Quad<T, K, L, M> obj)
		{
			return obj.first;
		}
	}

	public static final class SecondConverter<T, K, L, M> implements Converter<Quad<T, K, L, M>, K>
	{
		@Override
		public K convert(Quad<T, K, L, M> obj)
		{
			return obj.second;
		}
	}

	public static final class ThirdConverter<T, K, L, M> implements Converter<Quad<T, K, L, M>, L>
	{
		@Override
		public L convert(Quad<T, K, L, M> obj)
		{
			return obj.third;
		}
	}

	public static final class FourthConverter<T, K, L, M> implements Converter<Quad<T, K, L, M>, M>
	{
		@Override
		public M convert(Quad<T, K, L, M> obj)
		{
			return obj.fourth;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (o instanceof Quad)
		{
			@SuppressWarnings("rawtypes")
			final Quad p = (Quad) o;
			return ((p.first == first) || ((p.first != null) && (p.first.equals(first)))) && ((p.second == second) || ((p.second != null) && (p.second.equals(second))))
			        && ((p.third == third) || ((p.third != null) && (p.third.equals(third)))) && ((p.fourth == fourth) || ((p.fourth != null) && (p.fourth.equals(fourth))));
		}
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ ((fourth == null) ? 0 : fourth.hashCode());
	}
}
