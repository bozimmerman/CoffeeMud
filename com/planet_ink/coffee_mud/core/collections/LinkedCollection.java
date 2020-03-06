package com.planet_ink.coffee_mud.core.collections;
import java.util.*;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class LinkedCollection<K> implements Set<K>
{
	private volatile LinkedEntry<K> head = null;
	private volatile LinkedEntry<K> tail = null;

	@SuppressWarnings("rawtypes" )
	private static final Iterator empty=EmptyIterator.INSTANCE;

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

	public LinkedCollection()
	{
	}

	@Override
	public boolean add(final K arg0)
	{
		return add(new LinkedEntry<K>(arg0));
	}

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

	@Override
	public void clear()
	{
		head=null;
		tail=null;
	}

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

	@Override
	public boolean isEmpty()
	{
		return head == null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<K> iterator()
	{
		if(head == null)
			return empty;

		final LinkedCollection<K> me1=this;
		return new Iterator<K>()
		{
			final LinkedCollection<K> me=me1;
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

	@Override
	public Object[] toArray()
	{
		final Object[] obj=new Object[size()];
		int x=0;
		for(LinkedEntry<K> n=head;n != null;n=n.next)
			obj[x++] = n.value;
		return obj;
	}

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
