package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
/*
   Copyright 2012-2025 Bo Zimmerman

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
/**
 * A list of pairs of objects.
 *
 * @param <T> the first object type
 * @param <K> the second object type
 * @author Bo Zimmerman
 */
public interface PairList<T, K> extends List<Pair<T, K>>
{
	/**
	 * A converter interface for converting objects from the first type to the second.
	 *
	 * @return the converter
	 */
	public Pair.FirstConverter<T, K> getFirstConverter();

	/**
	 * A converter interface for converting objects from the second type to the
	 * first.
	 *
	 * @return the converter
	 */
	public Pair.SecondConverter<T, K> getSecondConverter();

	/**
	 * Returns an enumeration of the first objects in the pairs.
	 *
	 * @return an enumeration of the first objects in the pairs
	 */
	public Iterator<T> firstIterator();

	/**
	 * Returns an enumeration of the second objects in the pairs.
	 *
	 * @return an enumeration of the second objects in the pairs
	 */
	public Iterator<K> secondIterator();

	/**
	 * Returns the index of the first occurrence of the given object
	 * in the first objects of the pairs.
	 *
	 * @param t the object to look for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	public int indexOfFirst(T t);

	/**
	 * Returns the index of the first occurrence of the given object in the
	 * second objects of the pairs.
	 *
	 * @param k the object to look for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	public int indexOfSecond(K k);

	/**
	 * Returns the first object of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the first object of the pair
	 */
	public T getFirst(int index);

	/**
	 * Returns the second object of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the second object of the pair
	 */
	public K getSecond(int index);

	/**
	 * Adds a new pair to the end of the list.
	 * @param t the first object
	 * @param k the second object
	 */
	public void add(T t, K k);

	/**
	 * Inserts a new pair at the given index.
	 *
	 * @param x the index to insert at
	 * @param t the first object
	 * @param k the second object
	 */
	public void add(int x, T t, K k);

	/**
	 * Returns true if the first objects of the pairs contains the given object.
	 *
	 * @param t the object to look for
	 * @return true if found
	 */
	public boolean containsFirst(T t);

	/**
	 * Returns true if the second objects of the pairs contains the given
	 * object.
	 *
	 * @param k the object to look for
	 * @return true if found
	 */
	public boolean containsSecond(K k);

	/**
	 * Returns the first object of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the first object of the pair
	 */
	public T elementAtFirst(int index);

	/**
	 * Returns the second object of the pair at the given index.
	 *
	 * @param index the index of the pair
	 * @return the second object of the pair
	 */
	public K elementAtSecond(int index);

	/**
	 * Returns the index of the first occurrence of the given object
	 * in the first objects of the pairs, starting at the given index.
	 *
	 * @param t the object to look for
	 * @param index the index to start searching from
	 * @return the index of the first occurrence, or -1 if not found
	 */
	public int indexOfFirst(T t, int index);

	/**
	 * Returns the index of the first occurrence of the given object in the
	 * second objects of the pairs, starting at the given index.
	 *
	 * @param k the object to look for
	 * @param index the index to start searching from
	 * @return the index of the first occurrence, or -1 if not found
	 */
	public int indexOfSecond(K k, int index);

	/**
	 * Returns the index of the last occurrence of the given object in the first
	 * objects of the pairs, searching backwards from the given index.
	 *
	 * @param t the object to look for
	 * @param index the index to start searching backwards from
	 * @return the index of the last occurrence, or -1 if not found
	 */
	public int lastIndexOfFirst(T t, int index);

	/**
	 * Returns the index of the last occurrence of the given object in the
	 * second objects of the pairs, searching backwards from the given index.
	 *
	 * @param k the object to look for
	 * @param index the index to start searching backwards from
	 * @return the index of the last occurrence, or -1 if not found
	 */
	public int lastIndexOfSecond(K k, int index);

	/**
	 * Returns the index of the last occurrence of the given object in the first
	 * objects of the pairs.
	 *
	 * @param t the object to look for
	 * @return the index of the last occurrence, or -1 if not found
	 */
	public int lastIndexOfFirst(T t);

	/**
	 * Returns the index of the last occurrence of the given object in the
	 * second objects of the pairs.
	 *
	 * @param k the object to look for
	 * @return the index of the last occurrence, or -1 if not found
	 */
	public int lastIndexOfSecond(K k);

	/**
	 * Removes the first occurrence of the given object in the first objects of
	 * the pairs.
	 *
	 * @param t the object to remove
	 * @return true if found and removed
	 */
	public boolean removeFirst(T t);

	/**
	 * Removes the first occurrence of the given object in the second objects of
	 * the pairs.
	 *
	 * @param k the object to remove
	 * @return true if found and removed
	 */
	public boolean removeSecond(K k);

	/**
	 * Removes the first occurrence of the given object in the first objects of
	 * the pairs.
	 *
	 * @param t the object to remove
	 * @return true if found and removed
	 */
	public boolean removeElementFirst(T t);

	/**
	 * Removes the first occurrence of the given object in the second objects of
	 * the pairs.
	 *
	 * @param k the object to remove
	 * @return true if found and removed
	 */
	public boolean removeElementSecond(K k);

	/**
	 * Converts the first objects of the pairs to an array.
	 *
	 * @param a the array to fill, or null to create a new one
	 * @return the array of first objects
	 */
	public T[] toArrayFirst(T[] a);

	/**
	 * Converts the second objects of the pairs to an array.
	 *
	 * @param a the array to fill, or null to create a new one
	 * @return the array of second objects
	 */
	public K[] toArraySecond(K[] a);
}
