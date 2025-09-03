package com.planet_ink.coffee_mud.core.collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
/*
   Copyright 2020-2025 Bo Zimmerman

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
 * An enumeration wrapper for an array of objects.
 *
 * @param <K> the type of object in the array
 */
public class ArrayEnumerator<K> implements Enumeration<K>
{
	/** The array of objects to wrap. */
	private final K[] set;
	/** The current index in the array. */
	private volatile int index = 0;

	/**
	 * Constructs an enumeration wrapper for the given array of objects.
	 *
	 * @param set the array of objects to wrap
	 * @throws NullPointerException if the given array is null
	 */
	public ArrayEnumerator(final K[] set)
	{
		if(set == null)
			throw new NullPointerException();
		this.set=set;
	}

	/**
	 * Returns whether there are more elements in this enumeration.
	 */
	@Override
	public boolean hasMoreElements()
	{
		return index < set.length;
	}

	/**
	 * Returns the next element in this enumeration.
	 *
	 * @throws NoSuchElementException if there are no more elements in this
	 *             enumeration
	 */
	@Override
	public K nextElement()
	{
		if(!hasMoreElements())
			throw new NoSuchElementException();
		return set[index];
	}
}
