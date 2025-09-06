package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

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
 * A simple linked list implementation of a Set.
 *
 * @param <K> the type of object in the set
 * @author Bo Zimmerman
 */
public class LinkedSet<K> implements Set<K>
{
	private volatile LinkedEntry<K> head = null;
	private volatile LinkedEntry<K> tail = null;

	@SuppressWarnings("rawtypes" )
	private static final Iterator empty=EmptyIterator.INSTANCE;

	/**
	 * A single entry in the linked list.
	 *
	 * @param <K> the type of object in the set
	 */
	public static class LinkedEntry<K>
	{
		public LinkedEntry<K> prev;
		public LinkedEntry<K> next;
		public K value;

		public LinkedEntry(final K val)
		{
			this.value=val;
		}
	}

	/**
	 * Constructor
	 */
	public LinkedSet()
	{
	}

	@Override
	public boolean add(final K arg0)
	{
		return add(new LinkedEntry<K>(arg0));
	}

	/**
	 * Add the given linked entry to the end of the list. If it is already in
	 * the list, it is first removed.
	 *
	 * @param arg0 the entry to add
	 * @return true if added, false otherwise
	 */
	public boolean add(final LinkedEntry<K> arg0)
	{
		if(head == arg0)
			head = arg0.next;
		if(tail == arg0)
			tail = arg0.prev;
		if(arg0.prev != null)
			arg0.prev.next=arg0.next;
		if(arg0.next != null)
			arg0.next.prev=arg0.prev;
		arg0.prev=null;
		arg0.next=null;
		if(tail != null)
		{
			arg0.prev=tail;
			tail.next=arg0;
		}
		tail = arg0;
		if(head == null)
			head = arg0;
		return true;
	}

	/**
	 * Add all the items in the given collection to this one.
	 *
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(final Collection<? extends K> arg0)
	{
		for(final K k : arg0)
		{
			if(!add(k))
				return false;
		}
		return true;
	}

	/**
	 * Clear out the list.
	 */
	@Override
	public void clear()
	{
		head=null;
		tail=null;
	}

	/**
	 * Checks to see if the given object is in the list.
	 *
	 * @param arg0 the object to check for
	 * @return true if found, false otherwise
	 *
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object arg0)
	{
		for(@SuppressWarnings("rawtypes") LinkedEntry n = head; n != null; n=n.next)
		{
			if((n == arg0) || (n.value == arg0))
				return true;
		}
		return false;
	}

	/**
	 * Checks to see if all the items in the given collection are in this list.
	 *
	 * @param arg0 the collection to check for
	 * @return true if all are found, false otherwise
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> arg0)
	{
		for(final Object o : arg0)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	/**
	 * Checks to see if the list is empty.
	 *
	 * @return true if empty, false otherwise
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty()
	{
		return head == null;
	}

	/**
	 * Returns an iterator for the list.
	 *
	 * @return the iterator
	 * @see java.lang.Iterable#iterator()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator<K> iterator()
	{
		if(head == null)
			return empty;

		final LinkedSet<K> me1=this;
		return new Iterator<K>()
		{
			final LinkedSet<K> me=me1;
			LinkedEntry<K> next = head;
			LinkedEntry<K> curr = null;

			@Override
			public boolean hasNext()
			{
				return next != null;
			}

			@Override
			public void remove()
			{
				if(curr != null)
				{
					me.remove(curr);
					curr=null;
				}
			}

			@Override
			public K next()
			{
				if(next != null)
				{
					curr = next;
					final K k = next.value;
					next = next.next;
					return k;
				}
				throw new java.util.NoSuchElementException();
			}
		};
	}

	/**
	 * Remove the given object from the list, if it is in the list.
	 *
	 * @param arg0 the object to remove
	 * @return true if removed, false otherwise
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(final Object arg0)
	{
		if(arg0 instanceof LinkedEntry)
		{
			@SuppressWarnings("rawtypes")
			final LinkedEntry l = (LinkedEntry)arg0;
			if(head == l)
				head = l.next;
			if(tail == l)
				tail = l.prev;
			if(l.prev != null)
				l.prev.next=l.next;
			if(l.next != null)
				l.next.prev=l.prev;
			l.prev=null;
			l.next=null;
			return true;
		}
		else
		{
			LinkedEntry<K> n=head;
			while(n != null)
			{
				if(n.value == arg0)
					return remove(n);
				n=n.next;
			}
			return false;
		}
	}

	/**
	 * Remove all the items in the given collection from this list.
	 *
	 * @param arg0 the collection of items to remove
	 * @return true if all were removed, false otherwise
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> arg0)
	{
		for(final Object o : arg0)
		{
			if(!remove(o))
				return false;
		}
		return true;
	}

	/**
	 * Retain only the items in this list that are also in the given collection.
	 *
	 * @param arg0 the collection of items to retain
	 * @return true if all were retained, false otherwise
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean retainAll(final Collection<?> arg0)
	{
		clear();
		for(final Object o : arg0)
		{
			if(o instanceof LinkedEntry)
			{
				if(!add((LinkedEntry)o))
					return false;
			}
			else
			if(!add((K)o))
				return false;
		}
		return true;
	}

	/**
	 * Returns the number of items in the list.
	 *
	 * @return the number of items
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size()
	{
		int size = 0;
		LinkedEntry<K> n=head;
		while(n != null)
		{
			size++;
			n=n.next;
		}
		return size;
	}

	/**
	 * Returns an array containing all the items in the list.
	 *
	 * @return the array of items
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray()
	{
		final Object[] obj=new Object[size()];
		int x=0;
		for(LinkedEntry<K> n=head;n != null;n=n.next)
			obj[x++] = n.value;
		return obj;
	}

	/**
	 * Returns an array containing all the items in the list. If the given array
	 * is big enough, it is used, otherwise a new array of the same type is
	 * created.
	 *
	 * @param <T> the type of object in the array
	 * @param a the array to use, if big enough
	 * @return the array of items
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final T[] a)
	{
		final int size=size();
		final Object[] array = toArray();
		if (a.length < size)
			return (T[]) Arrays.copyOf(array, size, a.getClass());
		System.arraycopy(array, 0, a, 0, size);
		if (a.length > size)
			a[size] = null;
		return a;
	}
}
