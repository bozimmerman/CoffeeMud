package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
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
public interface PairList<T, K> extends List<Pair<T, K>>
{
	public Pair.FirstConverter<T, K> getFirstConverter();

	public Pair.SecondConverter<T, K> getSecondConverter();

	public Iterator<T> firstIterator();

	public Iterator<K> secondIterator();

	public int indexOfFirst(T t);

	public int indexOfSecond(K k);

	public T getFirst(int index);

	public K getSecond(int index);

	public void add(T t, K k);

	public boolean containsFirst(T t);

	public boolean containsSecond(K k);

	public T elementAtFirst(int index);

	public K elementAtSecond(int index);

	public int indexOfFirst(T t, int index);

	public int indexOfSecond(K k, int index);

	public int lastIndexOfFirst(T t, int index);

	public int lastIndexOfSecond(K k, int index);

	public int lastIndexOfFirst(T t);

	public int lastIndexOfSecond(K k);

	public boolean removeFirst(T t);

	public boolean removeSecond(K k);

	public boolean removeElementFirst(T t);

	public boolean removeElementSecond(K k);

	public T[] toArrayFirst(T[] a);

	public K[] toArraySecond(K[] a);
}
