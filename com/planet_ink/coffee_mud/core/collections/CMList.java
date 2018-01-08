package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

/*
   Copyright 2012-2018 Bo Zimmerman

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
public class CMList<K> implements Serializable, Cloneable, Iterable<K>, Collection<K>, Deque<K>, List<K>, Queue<K>
{
	private static final long serialVersionUID = -4174213459327144471L;
	private static final Random rand=new Random(System.currentTimeMillis());
	private class CMListNode
	{
		public K obj;
		public boolean active=false;
		public CMListNode next=null;
		public CMListNode prev=null;
		public CMListNode randNext=null;
		public CMListNode randPrev=null;
		public CMListNode(K obj) { this.obj=obj;}
	}

	private volatile CMListNode randNode=null;
	private volatile CMListNode head=null;
	private volatile CMListNode tail=null;
	private volatile int size=0;

	public CMList()
	{
	}

	public CMList(final Enumeration<K> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}

	public CMList(final Iterator<K> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}

	public CMList(final Set<K> E)
	{
		if(E!=null)
			for(final K o : E)
				add(o);
	}

	public synchronized void addAll(Enumeration<K> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}

	public synchronized void addAll(K[] E)
	{
		if(E!=null)
			for(final K e : E)
				add(e);
	}

	public synchronized void addAll(Iterator<K> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}

	public synchronized void removeAll(Enumeration<K> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				remove(E.nextElement());
	}

	public synchronized void removeAll(Iterator<K> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				remove(E.next());
	}

	public synchronized void removeAll(List<K> E)
	{
		if(E!=null)
			for(final K o : E)
				remove(o);
	}

	public LinkedList<K> toLinkedList()
	{
		final LinkedList<K> L=new LinkedList<K>();
		for (final K k : this)
			L.add(k);
		return L;
	}

	public Vector<K> toVector()
	{
		final Vector<K> V=new Vector<K>(size());
		for (final K k : this)
			V.add(k);
		return V;
	}

	private CMListNode findFirstNode(final Object arg0)
	{
		CMListNode curr=head;
		while(curr!=null)
		{
			if(curr.active)
			{
				if(arg0 == null)
				{
					if(curr.obj==null)
						return curr;
				}
				else
				if(arg0.equals(curr.obj))
					return curr;
			}
			curr=curr.next;
		}
		return null;
	}

	private CMListNode findLastNode(final Object arg0)
	{
		CMListNode curr=tail;
		while(curr!=null)
		{
			if(curr.active)
			{
				if(arg0 == null)
				{
					if(curr.obj==null)
						return curr;
				}
				else
				if(arg0.equals(curr.obj))
					return curr;
			}
			curr=curr.prev;
		}
		return null;
	}

	private synchronized void removeNode(final CMListNode here)
	{
		if((here == null)||(!here.active))
			return;
		here.active=false;
		size--;
		final CMListNode next=here.next;
		final CMListNode prev=here.prev;
		if(head == here)
			head=next;
		if(tail == here)
			tail=prev;
		if(prev != null)
			prev.next=next;
		if(next != null)
			next.prev=prev;
		final CMListNode randNext=here.randNext;
		final CMListNode randPrev=here.randPrev;
		if(randNode == here)
		{
			if(here.randNext == here)
				randNode = null;
			else
				randNode=here.randNext;
		}
		if(randPrev != null)
			randPrev.randNext=randNext;
		if(randNext != null)
			randNext.randPrev=randPrev;
		// in time, garbage collector will make all right again.
		// in time.
	}

	private synchronized CMListNode addAfter(final CMListNode here, final K arg1)
	{
		final CMListNode newNode=new CMListNode(arg1);
		if(here == null)
		{
			newNode.next=head;
			head=newNode;
			if(newNode.next!=null)
				newNode.next.prev=newNode;
			if(tail==null)
				tail=newNode;
		}
		else
		{
			newNode.next=here.next;
			here.next=newNode;
			newNode.prev=here;
			if(newNode.next!=null)
				newNode.next.prev=newNode;
			if(tail==here)
				tail=newNode;
		}
		if(randNode==null)
		{
			randNode=newNode;
			newNode.randNext=newNode;
			newNode.randPrev=newNode;
		}
		else
		if(rand.nextDouble()<0.5)
		{
			newNode.randNext=randNode;
			newNode.randPrev=randNode.randPrev;
			randNode.randPrev=newNode;
			newNode.randPrev.randNext=newNode;
			randNode = newNode;
		}
		else
		{
			newNode.randPrev=randNode;
			newNode.randNext=randNode.randNext;
			randNode.randNext.randPrev=newNode;
			randNode.randNext=newNode;
		}
		size++;
		newNode.active=true;
		return newNode;
	}

	private CMListNode nodeBefore(int arg0)
	{
		if((head == null) || (arg0 == 0))
			return null;
		else
		{
			CMListNode curr=head;
			for(int i=1;i<arg0;i++)
			{
				if(curr.next!=null)
				{
					if(!curr.active)
						i--;
					curr=curr.next;
				}
			}
			return curr;
		}
	}

	private CMListNode nodeAt(int arg0)
	{
		if((arg0<0)||(arg0>=size))
			return null;
		else
		{
			CMListNode curr;
			int i;
			int ch;
			if((arg0==size-1) || (arg0 > size/2))
			{
				curr=tail;
				i=size-1;
				ch=-1;
			}
			else
			{
				curr=head;
				i=0;
				ch=1;
			}
			while(curr != null)
			{
				if(i==arg0)
				{
					if(!curr.active)
						i=i+(ch*-1);
					else
						break;
				}
				else
					i=i+ch;
				curr=curr.next;
			}
			return curr;
		}
	}

	@Override
	public synchronized void add(int arg0, K arg1)
	{
		addAfter(nodeBefore(arg0),arg1);
	}

	@Override
	public synchronized boolean add(K arg0)
	{
		addAfter(tail,arg0);
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends K> arg0)
	{
		for (final K name : arg0)
			if(!add(name))
				 return false;
		return true;
	}

	@Override
	public synchronized boolean addAll(int arg0, Collection<? extends K> arg1)
	{
		CMListNode curr=nodeBefore(arg0);
		for (final K name : arg1)
			curr=addAfter(curr,name);
		return true;
	}

	@Override
	public synchronized void addFirst(K arg0)
	{
		addAfter(null,arg0);
	}

	@Override
	public synchronized void addLast(K arg0)
	{
		addAfter(tail,arg0);
	}

	@Override
	public synchronized void clear()
	{
		head=null;
		tail=null;
		randNode=null;
		size=0;
	}

	public synchronized K getNextRandom()
	{
		final CMListNode node=randNode;
		if(node == null)
			return null;
		randNode=node.randNext;
		return node.obj;
	}

	public synchronized K getPreviousRandom()
	{
		final CMListNode node=randNode;
		if(node == null)
			return null;
		randNode=node.randPrev;
		return node.obj;
	}

	public synchronized CMList<K> copyOf()
	{
		final CMList<K> newList=new CMList<K>();
		CMListNode curr=tail;
		while(curr!=null)
		{
			if(curr.active)
				newList.addAfter(null, curr.obj);
			curr=curr.prev;
		}
		return newList;
	}

	@Override
	public boolean contains(Object arg0)
	{
		return findFirstNode(arg0) != null;
	}

	public boolean containsFromEnd(Object arg0)
	{
		return findLastNode(arg0) != null;
	}

	public Enumeration<K> elements()
	{
		final CMListNode firstNode=nodeAt(0);
		return new Enumeration<K>()
		{
			private CMListNode nextNode = firstNode;
			private void makeNext()
			{
				if(nextNode != null)
				{
					nextNode=nextNode.next;
					while((nextNode != null)&&(!nextNode.active))
						nextNode=nextNode.next;
				}
			}

			@Override
			public boolean hasMoreElements()
			{
				return nextNode != null;
			}

			@Override
			public K nextElement()
			{
				if(!hasMoreElements())
					throw new NoSuchElementException();
				final K obj=nextNode.obj;
				makeNext();
				return obj;
			}
		};
	}

	@Override
	public Iterator<K> descendingIterator()
	{
		final CMListNode firstNode=nodeAt(size-1);
		return new Iterator<K>()
		{
			private CMListNode nextNode = firstNode;
			private CMListNode lastNode = null;
			private void makeNext()
			{
				if(nextNode != null)
				{
					nextNode=nextNode.prev;
					while((nextNode != null)&&(!nextNode.active))
						nextNode=nextNode.prev;
				}
			}

			@Override
			public boolean hasNext()
			{
				return nextNode != null;
			}

			@Override
			public K next()
			{
				if(!hasNext())
					throw new NoSuchElementException();
				lastNode=nextNode;
				final K obj=nextNode.obj;
				makeNext();
				return obj;
			}

			@Override
			public void remove()
			{
				removeNode(lastNode);
			}
		};
	}

	@Override
	public K element()
	{
		return getFirst();
	}

	@Override
	public K get(int arg0)
	{
		final CMListNode node = nodeAt(arg0);
		if(node == null)
			throw new NoSuchElementException();
		return node.obj;
	}

	@Override
	public K getFirst()
	{
		final CMListNode node = nodeAt(0);
		if(node != null)
			return node.obj;
		throw new NoSuchElementException();
	}

	@Override
	public K getLast()
	{
		final CMListNode node = nodeAt(size-1);
		if(node != null)
			return node.obj;
		throw new NoSuchElementException();
	}

	@Override
	public int indexOf(Object arg0)
	{
		for(final ListIterator<K> o=listIterator();o.hasNext();)
		{
			if(o.next()==arg0)
				return o.previousIndex();
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object arg0)
	{
		int i=size-1;
		for(final Iterator<K> o=descendingIterator();o.hasNext();)
		{
			if(o.next()==arg0)
				return i;
			i--;
		}
		return -1;
	}

	@Override
	public ListIterator<K> listIterator(final int arg0)
	{
		final CMListNode firstNode=nodeAt(arg0);
		return new ListIterator<K>()
		{
			private CMListNode lastNode = null;
			private CMListNode nextNode = firstNode;
			private int nextIndex = arg0;
			private CMListNode prevNode = null;

			private void makeNext()
			{
				if(nextNode != null)
				{
					prevNode=nextNode;
					nextIndex++;
					nextNode=nextNode.next;
					while((nextNode != null)&&(!nextNode.active))
						nextNode=nextNode.next;
				}
			}

			private void makePrev()
			{
				if(prevNode != null)
				{
					nextNode=prevNode;
					nextIndex--;
					prevNode=prevNode.prev;
					while((prevNode != null)&&(!prevNode.active))
						prevNode=prevNode.prev;
				}
			}

			@Override
			public boolean hasNext()
			{
				return nextNode != null;
			}

			@Override
			public K next()
			{
				if(!hasNext())
					throw new NoSuchElementException();
				lastNode=nextNode;
				final K obj=nextNode.obj;
				makeNext();
				return obj;
			}

			@Override
			public void remove()
			{
				removeNode(lastNode);
			}

			@Override
			public void add(K arg0)
			{
				addAfter(prevNode,arg0);
			}

			@Override
			public boolean hasPrevious()
			{
				return prevNode != null;
			}

			@Override
			public int nextIndex()
			{
				if(!hasNext())
					return size;
				return nextIndex;
			}

			@Override
			public K previous()
			{
				if(!hasPrevious())
					throw new NoSuchElementException();
				lastNode=prevNode;
				final K obj=prevNode.obj;
				makePrev();
				return obj;
			}

			@Override
			public int previousIndex()
			{
				if(!hasPrevious())
					return -1;
				return nextIndex-1;
			}

			@Override
			public void set(K arg0)
			{
				if(lastNode == null)
					throw new IllegalStateException();
				lastNode.obj=arg0;
			}
		};
	}

	@Override
	public synchronized boolean offer(K arg0)
	{
		return add(arg0);
	}

	@Override
	public synchronized boolean offerFirst(K arg0)
	{
		addFirst(arg0);
		return true;
	}

	@Override
	public synchronized boolean offerLast(K arg0)
	{
		addLast(arg0);
		return true;
	}

	@Override
	public K peek()
	{
		return peekFirst();
	}

	@Override
	public K peekFirst()
	{
		if(size == 0)
			return null;
		return head.obj;
	}

	@Override
	public K peekLast()
	{
		if(size == 0)
			return null;
		return head.obj;
	}

	@Override
	public synchronized K poll()
	{
		if(size == 0)
			return null;
		return removeFirst();
	}

	@Override
	public synchronized K pollFirst()
	{
		if(size == 0)
			return null;
		return removeFirst();
	}

	@Override
	public synchronized K pollLast()
	{
		if(size == 0)
			return null;
		return removeLast();
	}

	@Override
	public synchronized K pop()
	{
		return removeFirst();
	}

	@Override
	public synchronized void push(K arg0)
	{
		addFirst(arg0);
	}

	@Override
	public synchronized K remove()
	{
		return removeFirst();
	}

	@Override
	public synchronized K remove(int arg0)
	{
		final CMListNode node=nodeAt(arg0);
		if(node == null)
			throw new NoSuchElementException();
		removeNode(node);
		return node.obj;
	}

	@Override
	public synchronized boolean remove(Object arg0)
	{
		return removeFirstOccurrence(arg0);
	}

	@Override
	public synchronized K removeFirst()
	{
		final CMListNode node=nodeAt(0);
		if(node == null)
			throw new NoSuchElementException();
		removeNode(node);
		return node.obj;
	}

	@Override
	public synchronized boolean removeFirstOccurrence(Object arg0)
	{
		final CMListNode node = findFirstNode(arg0);
		if(node == null)
			return false;
		removeNode(node);
		return true;
	}

	@Override
	public synchronized K removeLast()
	{
		final CMListNode node=nodeAt(size-1);
		if(node == null)
			throw new NoSuchElementException();
		removeNode(node);
		return node.obj;
	}

	@Override
	public synchronized boolean removeLastOccurrence(Object arg0)
	{
		final CMListNode node = findLastNode(arg0);
		if(node == null)
			return false;
		removeNode(node);
		return true;
	}

	@Override
	public synchronized K set(int arg0, K arg1)
	{
		final CMListNode node = nodeAt(arg0);
		if(node == null)
			throw new NoSuchElementException();
		final K oldObj=node.obj;
		node.obj=arg1;
		return oldObj;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public synchronized Object[] toArray()
	{
		final Object[] result = new Object[size];
		int i = 0;
		for (CMListNode e = head; e != null; e = e.next)
			result[i++] = e.obj;
		return result;
	}

	@SuppressWarnings("unchecked")

	@Override
	public synchronized <T> T[] toArray(T[] arg0)
	{
		if (arg0.length < size)
			arg0 = (T[])java.lang.reflect.Array.newInstance(arg0.getClass().getComponentType(), size);
		int i = 0;
		final Object[] result = arg0;
		for (CMListNode e = head; e != null; e = e.next)
			result[i++] = e.obj;
		if (arg0.length > size)
			arg0[size] = null;
		return arg0;
	}

	@Override
	public Iterator<K> iterator()
	{
		return listIterator();
	}

	@Override
	public boolean equals(Object arg0)
	{
		return this==arg0;
	}

	@Override
	public int hashCode()
	{
		int hashCode = 1;
		final Iterator<K> i = iterator();
		while (i.hasNext())
		{
			final K obj = i.next();
			hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
		}
		return hashCode;
	}

	@Override
	public ListIterator<K> listIterator()
	{
		return listIterator(0);
	}

	@Override
	public List<K> subList(int arg0, int arg1)
	{
		final CMList<K> newList=new CMList<K>();
		for(final ListIterator<K> l=listIterator();l.hasNext();)
		{
			final K obj=l.next();
			if((l.previousIndex() >=arg0) && (l.previousIndex() < arg1))
				newList.add(obj);
		}
		return newList;
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for(final Object o : c)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return size==0;
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c)
	{
		if(c.size()==0)
			return true;
		boolean success=false;
		for (final Object name : c)
			success=remove(name)||success;
		return success;
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c)
	{
		boolean modified = false;
		for(final Iterator<K> e = iterator();e.hasNext();)
		{
			final Object o=e.next();
			if((o!=null)&&(!c.contains(o)))
			{
				e.remove();
				modified=true;
			}
		}
		return modified;
	}

	@Override
	public String toString()
	{
		final Iterator<K> i = iterator();
		if (! i.hasNext())
			return "[]";
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (;;)
		{
			final K e = i.next();
			sb.append(e == this ? "(this Collection)" : e);
			if (! i.hasNext())
				return sb.append(']').toString();
			sb.append(", ");
		}
	}
}
