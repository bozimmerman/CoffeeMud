package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2022-2025 Bo Zimmerman

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
 * An iterator wrapper for a List of objects that uses get(index) to retrieve
 * the next object.
 *
 * @param <J> the type of object in the list
 */
public class ListGetIterator<J> implements Iterator<J>
{
	private volatile int	i = -1;
	private final List<J>	coll;
	private final Object	removerContext;

	/** The callback to use when an item is removed */
	private final ListIteratorRemover<Object, J>	remover;

	/**
	 * An interface to implement to get a callback when an item is removed from
	 * the iterator.
	 *
	 * @param <K> the type of context object
	 * @param <J> the type of object being removed
	 */
	public static interface ListIteratorRemover<K,J>
	{
		public void remove(final K context, final J j);
	}

	/**
	 * Constructs an iterator wrapper for the given list of objects.
	 *
	 * @param col the list of objects to wrap
	 * @throws NullPointerException if the given list is null
	 */
	public ListGetIterator(final List<J> col)
	{
		coll = col;
		remover=null;
		removerContext=null;
	}

	/**
	 * Constructs an iterator wrapper for the given list of objects.
	 *
	 * @param col the list of objects to wrap
	 * @param rem the callback to use when an item is removed
	 * @param removerContext the context object to pass to the callback
	 * @throws NullPointerException if the given list is null
	 */
	public ListGetIterator(final List<J> col, final ListIteratorRemover<Object,J> rem, final Object removerContext)
	{
		this.coll = col;
		this.remover=rem;
		this.removerContext = removerContext;
	}

	/**
	 * Returns whether there are more elements in this iterator.
	 *
	 * @return true if there are more elements, false otherwise
	 */
	@Override
	public boolean hasNext()
	{
		return i<coll.size()-1;
	}

	/**
	 * Returns the next element in this iterator.
	 *
	 * @return the next element in this iterator
	 */
	@Override
	public J next()
	{
		synchronized(coll)
		{
			try
			{
				return coll.get(++i);
			}
			catch(final Exception e)
			{
				return null;
			}
		}
	}

	/**
	 * Removes the last element returned by this iterator from the underlying
	 * collection. If a callback was specified, it will be called.
	 */
	@Override
	public void remove()
	{
		synchronized(coll)
		{
			if((i>=0)&&(i<coll.size()))
			{
				try
				{
					final J j = coll.remove(i);
					i--;
					if(remover != null)
						remover.remove(removerContext, j);
				}
				catch(final Exception e)
				{}
			}
		}
	}
}
