package com.planet_ink.coffee_mud.core.collections;

public class Quint<T, K, L, M, N> extends Quad<T, K, L, M>
{
	public N	fifth;

	public Quint(T frst, K scnd, L thrd, M frth, N fith)
	{
		super(frst, scnd, thrd, frth);
		fifth = fith;
	}

	public static final class FirstConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, T>
	{
		@Override
		public T convert(Quint<T, K, L, M, N> obj)
		{
			return obj.first;
		}
	}

	public static final class SecondConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, K>
	{
		@Override
		public K convert(Quint<T, K, L, M, N> obj)
		{
			return obj.second;
		}
	}

	public static final class ThirdConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, L>
	{
		@Override
		public L convert(Quint<T, K, L, M, N> obj)
		{
			return obj.third;
		}
	}

	public static final class FourthConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, M>
	{
		@Override
		public M convert(Quint<T, K, L, M, N> obj)
		{
			return obj.fourth;
		}
	}

	public static final class FifthConverter<T, K, L, M, N> implements Converter<Quint<T, K, L, M, N>, N>
	{
		@Override
		public N convert(Quint<T, K, L, M, N> obj)
		{
			return obj.fifth;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		if (o instanceof Quint)
		{
			@SuppressWarnings("rawtypes")
			final Quint p = (Quint) o;
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
