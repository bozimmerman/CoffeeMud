package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
Copyright 2000-2010 Bo Zimmerman

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
public final class Pair<T,K> 
{
	public T first;
	public K second;
	public Pair(T frst, K scnd)
	{
		first=frst;
		second=scnd;
	}
	public static final class FirstConverter<T,K> implements Converter<Pair<T,K>,T> 
	{
		public T convert(Pair<T, K> obj) { return obj.first;}
	}
	public static final class SecondConverter<T,K> implements Converter<Pair<T,K>,K> 
	{
		public K convert(Pair<T, K> obj) { return obj.second;}
	}
}
