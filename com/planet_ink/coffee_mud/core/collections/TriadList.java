package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public interface TriadList<T, K, L> extends List<Triad<T, K, L>>
{
	public Triad.FirstConverter<T, K, L> getFirstConverter();

	public Triad.SecondConverter<T, K, L> getSecondConverter();

	public Triad.ThirdConverter<T, K, L> getThirdConverter();

	public Iterator<T> firstIterator();

	public Iterator<K> secondIterator();

	public Iterator<L> thirdIterator();

	public int indexOfFirst(T t);

	public int indexOfSecond(K k);

	public int indexOfThird(L l);

	public T getFirst(int index);

	public K getSecond(int index);

	public L getThird(int index);

	public void add(T t, K k, L l);

	public boolean containsFirst(T t);

	public boolean containsSecond(K k);

	public boolean containsThird(L l);

	public T elementAtFirst(int index);

	public K elementAtSecond(int index);

	public L elementAtThird(int index);

	public int indexOfFirst(T t, int index);

	public int indexOfSecond(K k, int index);

	public int indexOfThird(L l, int index);

	public int lastIndexOfFirst(T t, int index);

	public int lastIndexOfSecond(K k, int index);

	public int lastIndexOfThird(L l, int index);

	public int lastIndexOfFirst(T t);

	public int lastIndexOfSecond(K k);

	public int lastIndexOfThird(L l);

	public boolean removeFirst(T t);

	public boolean removeSecond(K k);

	public boolean removeThird(L l);

	public boolean removeElementFirst(T t);

	public boolean removeElementSecond(K k);

	public boolean removeElementThird(L l);

	public T[] toArrayFirst(T[] a);

	public K[] toArraySecond(K[] a);
	
	public L[] toArrayThird(L[] a);
}
