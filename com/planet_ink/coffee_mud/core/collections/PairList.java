package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

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
