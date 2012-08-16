package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
Copyright 2000-2012 Bo Zimmerman

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
public final class Quad<T,K,L,M> 
{
	public T first;
	public K second;
	public L third;
	public M fourth;
	public Quad(T frst, K scnd, L thrd, M frth)
	{
		first=frst;
		second=scnd;
		third=thrd;
		fourth=frth;
	}
	public static final class FirstConverter<T,K,L,M> implements Converter<Quad<T,K,L, M>,T> 
	{
		public T convert(Quad<T, K,L, M> obj) { return obj.first;}
	}
	public static final class SecondConverter<T,K,L,M> implements Converter<Quad<T,K,L, M>,K> 
	{
		public K convert(Quad<T, K, L, M> obj) { return obj.second;}
	}
	public static final class ThirdConverter<T,K,L,M> implements Converter<Quad<T,K,L, M>,L>
	{
		public L convert(Quad<T, K, L, M> obj) { return obj.third;}
	}
	public static final class FourthConverter<T,K,L,M> implements Converter<Quad<T,K,L, M>,M>
	{
		public M convert(Quad<T, K, L, M> obj) { return obj.fourth;}
	}
}
