package com.planet_ink.coffee_mud.core.collections;
import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2011-2025 Bo Zimmerman

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
 * A list which can change its internal backing store based on some external
 * signal. The signaler is responsible for rebuilding the list when it becomes
 * deprecated.
 *
 * @param <K> the type of object in the list
 * @author Bo Zimmerman
 */
public class ChameleonList<K> implements List<K>, SizedIterable<K>
{
	private volatile List<K> 		list;
	private volatile Signaler<K>	signaler;

	/**
	 * A signaler is responsible for telling the chameleon list when its backing
	 * store is no longer valid, and for rebuilding it when that happens.
	 *
	 * @param <K> the type of object in the list
	 */
	public static abstract class Signaler<K>
	{
		protected WeakReference<List<K>> 	oldReferenceListRef;

		/**
		 * Creates a new signaler.
		 *
		 * @param referenceList the list to watch for changes
		 */
		public Signaler(final List<K> referenceList)
		{
			oldReferenceListRef = new WeakReference<List<K>>(referenceList);
		}

		/**
		 * Rebuilds the given chameleon list.
		 *
		 * @param me the chameleon list to rebuild
		 */
		public abstract void rebuild(final ChameleonList<K> me);

		/**
		 * Returns whether the chameleon list is deprecated and needs to be
		 * rebuilt.
		 *
		 * @return true if the list is deprecated
		 */
		public abstract boolean isDeprecated();

		/**
		 * If the list is deprecated, rebuilds it.
		 *
		 * @param me the chameleon list to possibly rebuild
		 */
		public final synchronized void possiblyChangeMe(final ChameleonList<K> me)
		{
			if(!isDeprecated())
				return;
			rebuild(me);
		}
	}

	/**
	 * Creates a new chameleon list.
	 *
	 * @param l the initial backing store
	 * @param signaler the signaler responsible for rebuilding this list when
	 *            needed
	 */
	public ChameleonList(final List<K> l, final Signaler<K> signaler)
	{
		list=l;
		this.signaler = signaler;
	}

	/**
	 * Changes this chameleon list into a new chameleon list, by switching its
	 * backing store and signaler.
	 *
	 * @param fromList the other chameleon list to copy from
	 */
	public void changeMeInto(final ChameleonList<K> fromList)
	{
		this.list=fromList.list;
		this.signaler=fromList.signaler;
	}

	/**
	 * Gets the signaler responsible for this list.
	 *
	 * @return the signaler
	 */
	public Signaler<K> getSignaler()
	{
		return signaler;
	}

	/**
	 * Adds an element to the list, which is not supported.
	 * @param arg0 the element to add
	 * @return never returns normally
	 */
	@Override
	public boolean add(final K arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Adds an element to the list at a specific index, which is not supported.
	 * @param arg0 the index at which to add
	 * @param arg1 the element to add
	 */
	@Override
	public void add(final int arg0, final K arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Adds a collection of elements to the list, which is not supported.
	 * @param arg0 the collection of elements to add
	 * @return never returns normally
	 */
	@Override
	public boolean addAll(final Collection<? extends K> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Adds a collection of elements to the list at a specific index, which is
	 * not supported.
	 *
	 * @param arg0 the index at which to add
	 * @param arg1 the collection of elements to add
	 * @return never returns normally
	 */
	@Override
	public boolean addAll(final int arg0, final Collection<? extends K> arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Clears the list, which is not supported.
	 */
	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Returns whether the list contains a given element.
	 *
	 * @param arg0 the element to check for
	 * @return true if the list contains the element
	 */
	@Override
	public boolean contains(final Object arg0)
	{
		signaler.possiblyChangeMe(this);
		return list.contains(arg0);
	}

	/**
	 * Returns whether the list contains all elements of a given collection.
	 *
	 * @param arg0 the collection of elements to check for
	 * @return true if the list contains all the elements
	 */
	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		signaler.possiblyChangeMe(this);
		return list.containsAll(arg0);
	}

	/**
	 * Returns the element at a given index.
	 * @param arg0 the index of the element to return
	 * @return the element at that index
	 */
	@Override
	public K get(final int arg0)
	{
		signaler.possiblyChangeMe(this);
		return list.get(arg0);
	}

	/**
	 * Returns the index of a given element in the list.
	 * @param arg0 the element to look for
	 * @return the index of that element, or -1 if not found
	 */
	@Override
	public int indexOf(final Object arg0)
	{
		signaler.possiblyChangeMe(this);
		return list.indexOf(arg0);
	}

	/**
	 * Returns whether the list is empty.
	 *
	 * @return true if the list is empty
	 */
	@Override
	public boolean isEmpty()
	{
		signaler.possiblyChangeMe(this);
		return list.isEmpty();
	}

	/**
	 * Returns an iterator over the elements of the list.
	 *
	 * @return an iterator
	 */
	@Override
	public Iterator<K> iterator()
	{
		signaler.possiblyChangeMe(this);
		return new ReadOnlyIterator<K>(list.iterator());
	}

	/**
	 * Returns the last index of a given element in the list.
	 *
	 * @param arg0 the element to look for
	 * @return the last index of that element, or -1 if not found
	 */
	@Override
	public int lastIndexOf(final Object arg0)
	{
		signaler.possiblyChangeMe(this);
		return list.lastIndexOf(arg0);
	}

	/**
	 * Returns a list iterator over the elements of the list.
	 *
	 * @return a list iterator
	 */
	@Override
	public ListIterator<K> listIterator()
	{
		signaler.possiblyChangeMe(this);
		return new ReadOnlyListIterator<K>(list.listIterator());
	}

	/**
	 * Returns a list iterator over the elements of the list, starting at a
	 * given index.
	 *
	 * @param arg0 the index at which to start
	 * @return a list iterator
	 */
	@Override
	public ListIterator<K> listIterator(final int arg0)
	{
		signaler.possiblyChangeMe(this);
		return new ReadOnlyListIterator<K>(list.listIterator(arg0));
	}

	/**
	 * Removes an element from the list, which is not supported.
	 *
	 * @param arg0 the element to remove
	 * @return never returns normally
	 */
	@Override
	public boolean remove(final Object arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Removes the element at a given index from the list, which is not
	 * supported.
	 *
	 * @param arg0 the index of the element to remove
	 * @return never returns normally
	 */
	@Override
	public K remove(final int arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Removes a collection of elements from the list, which is not supported.
	 *
	 * @param arg0 the collection of elements to remove
	 * @return never returns normally
	 */
	@Override
	public boolean removeAll(final Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Retains a collection of elements in the list, which is not supported.
	 *
	 * @param arg0 the collection of elements to retain
	 * @return never returns normally
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Sets the element at a given index in the list, which is not supported.
	 *
	 * @param arg0 the index at which to set
	 * @param arg1 the element to set
	 * @return never returns normally
	 */
	@Override
	public K set(final int arg0, final K arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	/**
	 * Returns the size of the list.
	 *
	 * @return the size of the list
	 */
	@Override
	public int size()
	{
		signaler.possiblyChangeMe(this);
		return list.size();
	}

	/**
	 * Returns a sublist of the list between two given indices.
	 *
	 * @param arg0 the starting index, inclusive
	 * @param arg1 the ending index, exclusive
	 * @return the sublist
	 */
	@Override
	public List<K> subList(final int arg0, final int arg1)
	{
		signaler.possiblyChangeMe(this);
		return new ReadOnlyList<K>(list.subList(arg0,arg1));
	}

	/**
	 * Returns an array containing all the elements of the list.
	 *
	 * @return the array of elements
	 */
	@Override
	public Object[] toArray()
	{
		signaler.possiblyChangeMe(this);
		return list.toArray();
	}

	/**
	 * Returns an array containing all the elements of the list, in an array of
	 * a given type.
	 *
	 * @param <T> the type of object in the array
	 * @param arg0 the array type
	 * @return the array of elements
	 */
	@Override
	public <T> T[] toArray(final T[] arg0)
	{
		signaler.possiblyChangeMe(this);
		return list.toArray(arg0);
	}
}
