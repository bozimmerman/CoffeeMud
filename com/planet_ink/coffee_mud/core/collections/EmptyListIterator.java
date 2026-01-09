package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/*
   Copyright 2025-2026 Bo Zimmerman

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
 * An empty list iterator implementation.
 * @param <K> the type of object iterated over
 */
public class EmptyListIterator<K> implements ListIterator<K>
{
	private EmptyListIterator()
	{
	}

	@Override
	public boolean hasNext()
	{
		return false;
	}

	@Override
	public K next()
	{
		throw new NoSuchElementException();
	}

	@Override
	public void remove()
	{
		throw new NoSuchElementException();
	}

	@SuppressWarnings("rawtypes")
	/**
	 * A singleton instance of an empty list iterator
	 */
	public static final ListIterator	INSTANCE		= new EmptyListIterator();
	/**
	 * A singleton instance of an empty string list iterator
	 */
	public static final ListIterator<String>STRINSTANCE	= new EmptyListIterator<String>();
	@Override
	public boolean hasPrevious()
	{
		return false;
	}

	@Override
	public K previous()
	{
		return null;
	}

	@Override
	public int nextIndex()
	{
		return 0;
	}

	@Override
	public int previousIndex()
	{
		return 0;
	}

	@Override
	public void set(final K e)
	{
	}

	@Override
	public void add(final K e)
	{
	}
}
