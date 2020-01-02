package com.planet_ink.coffee_mud.core.collections;

/*
   Copyright 2012-2020 Bo Zimmerman

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
public class Quint<T, K, L, M, N> extends Quad<T, K, L, M>
{
	public N	fifth;

	public Quint(final T frst, final K scnd, final L thrd, final M frth, final N fith)
	{
		super(frst, scnd, thrd, frth);
		fifth = fith;
	}

	public static final class FirstConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, T>
	{
		@Override
		public T convert(final Quint<T, K, L, M, N> obj)
		{
			return obj.first;
		}
	}

	public static final class SecondConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, K>
	{
		@Override
		public K convert(final Quint<T, K, L, M, N> obj)
		{
			return obj.second;
		}
	}

	public static final class ThirdConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, L>
	{
		@Override
		public L convert(final Quint<T, K, L, M, N> obj)
		{
			return obj.third;
		}
	}

	public static final class FourthConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, M>
	{
		@Override
		public M convert(final Quint<T, K, L, M, N> obj)
		{
			return obj.fourth;
		}
	}

	public static final class FifthConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, N>
	{
		@Override
		public N convert(final Quint<T, K, L, M, N> obj)
		{
			return obj.fifth;
		}
	}

	@Override
	public boolean equals(final Object o)
	{
		if (o == this)
			return true;
		if (o instanceof Quint)
		{
			final Quint<?,?,?,?,?> p = (Quint<?,?,?,?,?>) o;
			return ((p.first == first) || ((p.first != null) && (p.first.equals(first)))) && ((p.second == second) || ((p.second != null) && (p.second.equals(second))))
			        && ((p.third == third) || ((p.third != null) && (p.third.equals(third)))) && ((p.fourth == fourth) || ((p.fourth != null) && (p.fourth.equals(fourth))))
			        && ((p.fifth == fifth) || ((p.fifth != null) && (p.fifth.equals(fifth))));
		}
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() ^ ((fifth == null) ? 0 : fifth.hashCode());
	}
}
