package com.planet_ink.coffee_mud.core.collections;

public class Quint<T,K,L,M,N> extends Quad<T,K,L,M>
{
	public N fifth;
	public Quint(T frst, K scnd, L thrd, M frth, N fith)
	{
		super(frst,scnd,thrd,frth);
		fifth=fith;
	}
	public static final class FirstConverter<T,K,L,M,N> implements Converter<Quint<T,K,L,M,N>,T> 
	{
		public T convert(Quint<T, K,L, M, N> obj) { return obj.first;}
	}
	public static final class SecondConverter<T,K,L,M,N> implements Converter<Quint<T,K,L,M,N>,K> 
	{
		public K convert(Quint<T, K, L, M, N> obj) { return obj.second;}
	}
	public static final class ThirdConverter<T,K,L,M,N> implements Converter<Quint<T,K,L,M,N>,L>
	{
		public L convert(Quint<T, K, L, M, N> obj) { return obj.third;}
	}
	public static final class FourthConverter<T,K,L,M,N> implements Converter<Quint<T,K,L,M,N>,M>
	{
		public M convert(Quint<T, K, L, M, N> obj) { return obj.fourth;}
	}
	public static final class FifthConverter<T,K,L,M,N> implements Converter<Quint<T,K,L,M,N>,N>
	{
		public N convert(Quint<T, K, L, M, N> obj) { return obj.fifth;}
	}

}
