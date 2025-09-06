package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

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
 * An implementation of a List which is backed by a doubly linked list of nodes.
 * Each node contains pointers to the next and previous nodes, as well as
 * pointers to the next and previous nodes in a randomized list. This allows for
 * very fast insertion and deletion of nodes, as well as fast iteration through
 * the list in both sequential and random order.
 *
 * @param <K> the type of elements held in this collection
 * @author Bo Zimmerman
 */
public class CMList<K> implements Serializable, Cloneable, Iterable<K>, Collection<K>, Deque<K>, List<K>, Queue<K>
{
	private static final long	serialVersionUID	= -4174213459327144471L;
	private static final Random	rand				= new Random(System.currentTimeMillis());

	/**
	 * A node in the CMList, containing the object and pointers to the next and
	 * previous nodes in both the sequential and randomized lists.
	 */
	private class CMListNode
	{
		/** The object contained in this node */
		public K			obj;
		/** True if this node is active (not removed) */
		public boolean		active		= false;
		/** Pointer to the next node in the sequential list */
		public CMListNode	next		= null;
		/** Pointer to the previous node in the sequential list */
		public CMListNode	prev		= null;
		/** Pointer to the next node in the randomized list */
		public CMListNode	randNext	= null;
		/** Pointer to the previous node in the randomized list */
		public CMListNode	randPrev	= null;

		/**
		 * Constructs a new node containing the specified object.
		 *
		 * @param obj the object to contain in this node
		 */
		public CMListNode(final K obj)
		{
			this.obj = obj;
		}
	}

	/** Pointer to a random node in the list */
	private volatile CMListNode	randNode	= null;
	/** Pointer to the head (first) node in the list */
	private volatile CMListNode	head		= null;
	/** Pointer to the tail (last) node in the list */
	private volatile CMListNode	tail		= null;
	/** The number of active nodes in the list */
	private volatile int		size		= 0;

	/**
	 * Constructs a new, empty list.
	 */
	public CMList()
	{
	}

	/**
	 * Constructs a new list containing the elements of the specified Enumeration, in
	 * the order they are returned by the array's iterator.
	 *
	 * @param E the Enumeration whose elements are to be placed into this list
	 */
	public CMList(final Enumeration<K> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}

	/**
	 * Constructs a new list containing the elements of the specified Iterator, in
	 * the order they are returned by the array's iterator.
	 *
	 * @param E the Iterator whose elements are to be placed into this list
	 */
	public CMList(final Iterator<K> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}

	/**
	 * Constructs a new list containing the elements of the specified
	 * Set, in the order they are returned by the collection's iterator.
	 *
	 * @param E the Set whose elements are to be placed into this list
	 */
	public CMList(final Set<K> E)
	{
		if(E!=null)
			for(final K o : E)
				add(o);
	}

	/**
	 * Add all of the elements in the given Enumeration to this list.
	 *
	 * @param E the Enumeration of elements to add
	 */
	public synchronized void addAll(final Enumeration<K> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				add(E.nextElement());
	}

	/**
	 * Add all of the elements in the given array to this list.
	 *
	 * @param E the array of elements to add
	 */
	public synchronized void addAll(final K[] E)
	{
		if(E!=null)
			for(final K e : E)
				add(e);
	}

	/**
	 * Add all of the elements in the given Iterator to this list.
	 *
	 * @param E the Iterator of elements to add
	 */
	public synchronized void addAll(final Iterator<K> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				add(E.next());
	}

	/**
	 * Remove all of the elements in the given Enumeration from this list.
	 *
	 * @param E the Enumeration of elements to remove
	 */
	public synchronized void removeAll(final Enumeration<K> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				remove(E.nextElement());
	}

	/**
	 * Remove all of the elements in the given Iterator from this list.
	 *
	 * @param E the Iterator of elements to remove
	 */
	public synchronized void removeAll(final Iterator<K> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				remove(E.next());
	}

	/**
	 * Remove all of the elements in the given list from this list.
	 *
	 * @param E the list of elements to remove
	 */
	public synchronized void removeAll(final List<K> E)
	{
		if(E!=null)
			for(final K o : E)
				remove(o);
	}

	/**
	 * Returns a LinkedList containing all of the elements in this list in
	 * proper sequence (from first to last element).
	 *
	 * @return a LinkedList containing all of the elements in this list in
	 *         proper sequence
	 */
	public LinkedList<K> toLinkedList()
	{
		final LinkedList<K> L=new LinkedList<K>();
		for (final K k : this)
			L.add(k);
		return L;
	}

	/**
	 * Returns a Vector containing all of the elements in this list in proper
	 * sequence (from first to last element).
	 *
	 * @return a Vector containing all of the elements in this list in proper
	 *         sequence
	 */
	public Vector<K> toVector()
	{
		final Vector<K> V=new Vector<K>(size());
		for (final K k : this)
			V.add(k);
		return V;
	}

	/**
	 * Finds the first node in the list containing the specified object.
	 *
	 * @param arg0 the object to search for
	 * @return the first node containing the object, or null if not found
	 */
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

	/**
	 * Finds the last node in the list containing the specified object.
	 *
	 * @param arg0 the object to search for
	 * @return the last node containing the object, or null if not found
	 */
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

	/**
	 * Removes the specified node from the list.
	 *
	 * @param here the node to remove
	 */
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

	/**
	 * Adds a new node containing the specified object after the specified node.
	 *
	 * @param here the node to add after, or null to add at the head
	 * @param arg1 the object to add
	 * @return the newly added node
	 */
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

	/**
	 * Returns the node before the specified index.
	 *
	 * @param arg0 the index to find the node before
	 * @return the node before the specified index, or null if index is 0 or
	 *         list is empty
	 */
	private CMListNode nodeBefore(final int arg0)
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

	/**
	 * Returns the node at the specified index.
	 *
	 * @param arg0 the index to find
	 * @return the node at the specified index, or null if index is out of range
	 */
	private CMListNode nodeAt(final int arg0)
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

	/**
	 * Inserts the specified element at the specified position in this list
	 * (optional operation). Shifts the element currently at that position (if
	 * any) and any subsequent elements to the right (adds one to their
	 * indices).
	 *
	 * @param arg0 index at which the specified element is to be inserted
	 * @param arg1 element to be inserted
	 * @throws NoSuchElementException if the index is out of range (
	 *             <code>index &lt; 0 || index &gt; size()</code>)
	 */
	@Override
	public synchronized void add(final int arg0, final K arg1)
	{
		addAfter(nodeBefore(arg0),arg1);
	}

	/**
	 * Appends the specified element to the end of this list (optional
	 * operation).
	 *
	 * @param arg0 element to be appended to this list
	 * @return <code>true</code> (as specified by {@link Collection#add})
	 */
	@Override
	public synchronized boolean add(final K arg0)
	{
		addAfter(tail,arg0);
		return true;
	}

	/**
	 * Appends all of the elements in the specified collection to the end of
	 * this list, in the order that they are returned by the specified
	 * collection's iterator (optional operation). The behavior of this
	 * operation is undefined if the specified collection is modified while the
	 * operation is in progress. (This implies that the behavior of this call is
	 * undefined if the specified collection is this list, and this list is
	 * non-empty.)
	 *
	 * @param arg0 collection containing elements to be added to this list
	 * @return <code>true</code> if this list changed as a result of the call
	 */
	@Override
	public synchronized boolean addAll(final Collection<? extends K> arg0)
	{
		for (final K name : arg0)
			if(!add(name))
				 return false;
		return true;
	}

	/**
	 * Inserts all of the elements in the specified collection into this list,
	 * starting at the specified position. Shifts the element currently at that
	 * position (if any) and any subsequent elements to the right (increases
	 * their indices). The new elements will appear in the list in the order
	 * that they are returned by the specified collection's iterator.
	 *
	 * @param arg0 index at which to insert the first element from the specified
	 *            collection
	 * @param arg1 collection containing elements to be added to this list
	 * @return <code>true</code> if this list changed as a result of the call
	 */
	@Override
	public synchronized boolean addAll(final int arg0, final Collection<? extends K> arg1)
	{
		CMListNode curr=nodeBefore(arg0);
		for (final K name : arg1)
			curr=addAfter(curr,name);
		return true;
	}

	/**
	 * Inserts the specified element at the front of this list.
	 *
	 * @param arg0 the element to add
	 */
	@Override
	public synchronized void addFirst(final K arg0)
	{
		addAfter(null,arg0);
	}

	/**
	 * Appends the specified element to the end of this list.
	 *
	 * @param arg0 the element to add
	 */
	@Override
	public synchronized void addLast(final K arg0)
	{
		addAfter(tail,arg0);
	}

	/**
	 * Removes all of the elements from this list. The list will be empty after
	 * this call returns.
	 */
	@Override
	public synchronized void clear()
	{
		head=null;
		tail=null;
		randNode=null;
		size=0;
	}

	/**
	 * Returns the next object in the randomized list, or null if the list is
	 * empty. The first call to this method after a series of additions or
	 * removals will be random, but subsequent calls will cycle through the list
	 * in a random order.
	 *
	 * @return the next object in the randomized list, or null if the list is
	 *         empty
	 */
	public synchronized K getNextRandom()
	{
		final CMListNode node=randNode;
		if(node == null)
			return null;
		randNode=node.randNext;
		return node.obj;
	}

	/**
	 * Returns the previous object in the randomized list, or null if the list
	 * is empty. The first call to this method after a series of additions or
	 * removals will be random, but subsequent calls will cycle through the list
	 * in a random order.
	 *
	 * @return the previous object in the randomized list, or null if the list
	 *         is empty
	 */
	public synchronized K getPreviousRandom()
	{
		final CMListNode node=randNode;
		if(node == null)
			return null;
		randNode=node.randPrev;
		return node.obj;
	}

	/**
	 * Returns a copy of this list. The copy will contain the same elements in
	 * the same order, but will be a different object.
	 *
	 * @return a copy of this list
	 */
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

	/**
	 * Returns <code>true</code> if this list contains the specified element.
	 * More formally, returns <code>true</code> if and only if this list
	 * contains at least one element <code>e</code> such that
	 * <code>(o==null ? e==null : o.equals(e))</code>.
	 *
	 * @param arg0 element whose presence in this list is to be tested
	 * @return <code>true</code> if this list contains the specified element
	 */
	@Override
	public boolean contains(final Object arg0)
	{
		return findFirstNode(arg0) != null;
	}

	/**
	 * Returns <code>true</code> if this list contains the specified element,
	 * searching from the end of the list to the beginning. More formally,
	 * returns <code>true</code> if and only if this list contains at least one
	 * element <code>e</code> such that <code>(o==null ? e==null :
	 * o.equals(e))</code>.
	 *
	 * @param arg0 element whose presence in this list is to be tested
	 * @return <code>true</code> if this list contains the specified element
	 *         when searching from end to beginning
	 */
	public boolean containsFromEnd(final Object arg0)
	{
		return findLastNode(arg0) != null;
	}


	/**
	 * Returns an enumeration of the elements in this list in proper sequence (from
	 * first to last element).
	 * @return an enumeration of the elements in this list in proper sequence
	 */
	@SuppressWarnings("unchecked")
	public Enumeration<K> elements()
	{
		if(size==0)
			return EmptyEnumeration.INSTANCE;
		final CMListNode firstNode=nodeAt(0);
		return new Enumeration<K>()
		{
			private CMListNode nextNode = firstNode;

			/**
			 * Moves the next pointers to the next active nodes in the list.
			 */
			private void makeNext()
			{
				if(nextNode != null)
				{
					nextNode=nextNode.next;
					while((nextNode != null)&&(!nextNode.active))
						nextNode=nextNode.next;
				}
			}

			/**
			 * Returns true if there is a next node.
			 */
			@Override
			public boolean hasMoreElements()
			{
				return nextNode != null;
			}

			/**
			 * Returns the next node's object.
			 *
			 * @return the next node's object
			 */
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

	/**
	 * Returns a sequential iterator over the elements in this list,
	 * from the end of the list to the beginning.
	 * @return a sequential iterator over the elements in this list
	 */
	@Override
	public Iterator<K> descendingIterator()
	{
		final CMListNode firstNode=nodeAt(size-1);
		return new Iterator<K>()
		{
			private CMListNode nextNode = firstNode;
			private CMListNode lastNode = null;

			/**
			 * Moves the next pointers to the next active nodes in the list.
			 */
			private void makeNext()
			{
				if(nextNode != null)
				{
					nextNode=nextNode.prev;
					while((nextNode != null)&&(!nextNode.active))
						nextNode=nextNode.prev;
				}
			}

			/**
			 * Returns true if there is a next node.
			 */
			@Override
			public boolean hasNext()
			{
				return nextNode != null;
			}

			/**
			 * Returns the next node's object.
			 * @return the next node's object
			 */
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

			/**
			 * Removes the last node returned by next() from the list.
			 */
			@Override
			public void remove()
			{
				removeNode(lastNode);
			}
		};
	}

	/**
	 * Returns the first element in this list.
	 * @return the first element in this list
	 */
	@Override
	public K element()
	{
		return getFirst();
	}

	/**
	 * Returns the element at the given index in the list.
	 * @param arg0 the index of the element to return
	 * @return the element at the given index in the list
	 * @throws NoSuchElementException if the index is out of range
	 */
	@Override
	public K get(final int arg0)
	{
		final CMListNode node = nodeAt(arg0);
		if(node == null)
			throw new NoSuchElementException();
		return node.obj;
	}

	/**
	 * Returns the first element in this list.
	 * @return the first element in this list
	 */
	@Override
	public K getFirst()
	{
		final CMListNode node = nodeAt(0);
		if(node != null)
			return node.obj;
		throw new NoSuchElementException();
	}

	/**
	 * Returns the last element in this list.
	 * @return the last element in this list
	 */
	@Override
	public K getLast()
	{
		final CMListNode node = nodeAt(size-1);
		if(node != null)
			return node.obj;
		throw new NoSuchElementException();
	}

	/**
	 * Returns the first index of the given object in the list,
	 * searching from the beginning of the list to the end.
	 * @param arg0 the object to search for
	 * @return the first index of the given object in the list,
	 */
	@Override
	public int indexOf(final Object arg0)
	{
		for(final ListIterator<K> o=listIterator();o.hasNext();)
		{
			if(o.next()==arg0)
				return o.previousIndex();
		}
		return -1;
	}

	/**
	 * Returns the last index of the given object in the list,
	 * searching from the end of the list to the beginning.
	 * @param arg0 the object to search for
	 * @return the last index of the given object in the list,
	 */
	@Override
	public int lastIndexOf(final Object arg0)
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

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence).
	 *
	 * @param arg0 index of the first element to be returned from the
	 *           list iterator (by a call to {@link
	 *           ListIterator#next next})
	 * @return a list iterator over the elements in this list (in proper
	 *         sequence)
	 */
	@Override
	public ListIterator<K> listIterator(final int arg0)
	{
		final CMListNode firstNode=nodeAt(arg0);
		return new ListIterator<K>()
		{
			private CMListNode	lastNode	= null;
			private CMListNode	nextNode	= firstNode;
			private int			nextIndex	= arg0;
			private CMListNode	prevNode	= null;

			/**
			 * Moves the next pointers to the next
			 * active nodes in the list.
			 */
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

			/**
			 * Moves previous pointers to the previous
			 * active nodes in the list.
			 */
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

			/**
			 * Returns true if there is a next node.
			 */
			@Override
			public boolean hasNext()
			{
				return nextNode != null;
			}

			/**
			 * Returns the next node's object.
			 * @return the next node's object
			 */
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

			/**
			 * Removes the last node returned by next() or previous() from the
			 * list.
			 */
			@Override
			public void remove()
			{
				removeNode(lastNode);
			}

			/**
			 * Adds a new node after the previous node.
			 */
			@Override
			public void add(final K arg0)
			{
				addAfter(prevNode,arg0);
			}

			/**
			 * Returns true if there is a previous node.
			 */
			@Override
			public boolean hasPrevious()
			{
				return prevNode != null;
			}

			/**
			 * Returns the next ordinal index.
			 */
			@Override
			public int nextIndex()
			{
				if(!hasNext())
					return size;
				return nextIndex;
			}

			/**
			 * Returns the previous node's object.
			 *
			 * @return the previous node's object
			 */
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

			/**
			 * Returns the previous ordinal index.
			 */
			@Override
			public int previousIndex()
			{
				if(!hasPrevious())
					return -1;
				return nextIndex-1;
			}

			/**
			 * Sets the object in the last node returned by next() or previous()
			 * to the specified object.
			 */
			@Override
			public void set(final K arg0)
			{
				if(lastNode == null)
					throw new IllegalStateException();
				lastNode.obj=arg0;
			}
		};
	}

	/**
	 * Appends the specified element to the end of this list.
	 *
	 * @param arg0 the element to add
	 * @return true
	 */
	@Override
	public synchronized boolean offer(final K arg0)
	{
		return add(arg0);
	}

	/**
	 * Inserts the specified element at the front of this list.
	 *
	 * @param arg0 the element to add
	 * @return true
	 */
	@Override
	public synchronized boolean offerFirst(final K arg0)
	{
		addFirst(arg0);
		return true;
	}

	/**
	 * Appends the specified element to the end of this list.
	 *
	 * @param arg0 the element to add
	 * @return true
	 */
	@Override
	public synchronized boolean offerLast(final K arg0)
	{
		addLast(arg0);
		return true;
	}

	/**
	 * Retrieves, but does not remove, the head of the list represented by this
	 * deque.
	 *
	 * @return the head of the list represented by this deque or null
	 */
	@Override
	public K peek()
	{
		return peekFirst();
	}

	/**
	 * Returns the first element in this list, or null if the list is empty.
	 *
	 * @return the first element in this list, or null if the list is empty
	 */
	@Override
	public K peekFirst()
	{
		if(size == 0)
			return null;
		return head.obj;
	}

	/**
	 * Returns the last element in this list, or null if the list is empty.
	 *
	 * @return the last element in this list, or null if the list is empty
	 */
	@Override
	public K peekLast()
	{
		if(size == 0)
			return null;
		return head.obj;
	}

	/**
	 * Removes and returns the first element from this list, or returns null if
	 * this list is empty.
	 *
	 * @return the first element from this list, or null if this list is empty
	 */
	@Override
	public synchronized K poll()
	{
		if(size == 0)
			return null;
		return removeFirst();
	}

	/**
	 * Removes and returns the first element from this list, or returns null if
	 * this list is empty.
	 *
	 * @return the first element from this list, or null if this list is empty
	 */
	@Override
	public synchronized K pollFirst()
	{
		if(size == 0)
			return null;
		return removeFirst();
	}

	/**
	 * Removes and returns the last element from this list, or returns null if
	 * this list is empty.
	 *
	 * @return the last element from this list, or null if this list is empty
	 */
	@Override
	public synchronized K pollLast()
	{
		if(size == 0)
			return null;
		return removeLast();
	}

	/**
	 * Pops an element from the stack represented by this list. In other words,
	 * removes and returns the first element of this list.
	 *
	 * @return the element at the front of this list (which is the top of the
	 *         stack represented by this list)
	 * @throws NoSuchElementException if this list is empty
	 */
	@Override
	public synchronized K pop()
	{
		return removeFirst();
	}

	/**
	 * Pushes an element onto the stack represented by this list. In other
	 * words, inserts the element at the front of this list.
	 *
	 * @param arg0 the element to push
	 */
	@Override
	public synchronized void push(final K arg0)
	{
		addFirst(arg0);
	}

	/**
	 * Removes and returns the head of the list represented by this deque.
	 *
	 * @return the head of the list represented by this deque
	 * @throws NoSuchElementException if this deque is empty
	 */
	@Override
	public synchronized K remove()
	{
		return removeFirst();
	}

	/**
	 * Removes the element at the specified position in this list. Shifts any
	 * subsequent elements to the left (subtracts one from their indices).
	 * Returns the element that was removed from the list.
	 *
	 * @param arg0 the index of the element to be removed
	 * @return the element previously at the specified position
	 * @throws NoSuchElementException if the index is out of range (
	 *             <code>index &lt; 0 || index &gt;= size()</code>)
	 */
	@Override
	public synchronized K remove(final int arg0)
	{
		final CMListNode node=nodeAt(arg0);
		if(node == null)
			throw new NoSuchElementException();
		removeNode(node);
		return node.obj;
	}

	/**
	 * Removes the first occurrence of the specified element from this list, if
	 * it is present. If the list does not contain the element, it is unchanged.
	 * More formally, removes the first element with the specified value such
	 * that <code>(o==null ? get(i)==null : o.equals(get(i)))</code> (if such an
	 * element exists). Returns <code>true</code> if this list contained the
	 * specified element (or equivalently, if this list changed as a result of
	 * the call).
	 *
	 * @param arg0 element to be removed from this list, if present
	 * @return <code>true</code> if this list contained the specified element
	 */
	@Override
	public synchronized boolean remove(final Object arg0)
	{
		return removeFirstOccurrence(arg0);
	}

	/**
	 * Removes and returns the first element from this list.
	 *
	 * @return the first element from this list
	 * @throws NoSuchElementException if the list is empty
	 */
	@Override
	public synchronized K removeFirst()
	{
		final CMListNode node=nodeAt(0);
		if(node == null)
			throw new NoSuchElementException();
		removeNode(node);
		return node.obj;
	}

	/**
	 * Removes the first occurrence of the specified element from this list
	 * (when traversing the list from head to tail). If the list does not
	 * contain the element, it is unchanged. More formally, removes the first
	 * element with the specified value such that
	 * <code>(o==null ? get(i)==null : o.equals(get(i)))</code> (if such an
	 * element exists). Returns <code>true</code> if the list contained the
	 * specified element (or equivalently, if the list changed as a result of
	 * the call).
	 *
	 * @param arg0 element to be removed from this list, if present
	 * @return <code>true</code> if the list contained the specified element
	 */
	@Override
	public synchronized boolean removeFirstOccurrence(final Object arg0)
	{
		final CMListNode node = findFirstNode(arg0);
		if(node == null)
			return false;
		removeNode(node);
		return true;
	}

	/**
	 * Removes and returns the last element from this list.
	 *
	 * @return the last element from this list
	 * @throws NoSuchElementException if the list is empty
	 */
	@Override
	public synchronized K removeLast()
	{
		final CMListNode node=nodeAt(size-1);
		if(node == null)
			throw new NoSuchElementException();
		removeNode(node);
		return node.obj;
	}

	/**
	 * Removes the last occurrence of the specified element from this list (when
	 * traversing the list from head to tail). If the list does not contain the
	 * element, it is unchanged. More formally, removes the last element
	 * with the specified value such that
	 * <code>(o==null ? get(i)==null : o.equals(get(i)))</code> (if such
	 * an element exists). Returns <code>true</code> if the list contained
	 * the specified element (or equivalently, if the list changed as a
	 * result of the call).
	 * @param arg0 element to be removed from this list, if present
	 * @return <code>true</code> if the list contained the specified element
	 */
	@Override
	public synchronized boolean removeLastOccurrence(final Object arg0)
	{
		final CMListNode node = findLastNode(arg0);
		if(node == null)
			return false;
		removeNode(node);
		return true;
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element (optional operation).
	 *
	 * @param arg0 index of the element to replace
	 * @param arg1 element to be stored at the specified position
	 * @return the element previously at the specified position
	 * @throws NoSuchElementException if the index is out of range
	 */
	@Override
	public synchronized K set(final int arg0, final K arg1)
	{
		final CMListNode node = nodeAt(arg0);
		if(node == null)
			throw new NoSuchElementException();
		final K oldObj=node.obj;
		node.obj=arg1;
		return oldObj;
	}

	/**
	 * Returns the number of elements in this list. If this list contains more
	 * than <code>Integer.MAX_VALUE</code> elements, returns
	 * <code>Integer.MAX_VALUE</code>.
	 *
	 * @return the number of elements in this list
	 */
	@Override
	public int size()
	{
		return size;
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element).
	 *
	 * @return an array containing all of the elements in this list in proper
	 *         sequence
	 */
	@Override
	public synchronized Object[] toArray()
	{
		final Object[] result = new Object[size];
		int i = 0;
		for (CMListNode e = head; e != null; e = e.next)
			result[i++] = e.obj;
		return result;
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence; the runtime type of the returned array is that of the specified
	 * array. If the list fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this list.
	 *
	 * <p>
	 * If the list fits in the specified array with room to spare (i.e., the
	 * array has more elements than the list), the element in the array
	 * immediately following the end of the collection is set to
	 * <code>null</code>. (This is useful in determining the length of the list
	 * <i>only</i> if the caller knows that the list does not contain any null
	 * elements.)
	 *
	 * @param arg0 the array into which the elements of the list are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose.
	 * @return an array containing the elements of the list
	 * @throws ArrayStoreException if the runtime type of the specified array is
	 *             not a supertype of the runtime type of every element in this
	 *             list
	 * @throws NullPointerException if the specified array is null
	 */
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

	/**
	 * Returns an iterator over the elements in this list in proper sequence.
	 *
	 * @return an iterator over the elements in this list in proper sequence
	 */
	@Override
	public Iterator<K> iterator()
	{
		return listIterator();
	}

	/**
	 * Compares the specified object with this list for equality. Returns
	 * <code>true</code> if and only if the specified object is also a list,
	 * both lists have the same size, and all corresponding pairs of elements in
	 * the two lists are <em>equal</em>. (Two elements <code>e1</code> and
	 * <code>e2</code> are <em>equal</em> if
	 * <code>(e1==null ? e2==null : e1.equals(e2))</code>.) In other words, two
	 * lists are defined to be equal if they contain the same elements in the
	 * same order. This definition ensures that the equals method works properly
	 * across different implementations of the <code>List</code> interface.
	 *
	 * @param arg0 the object to be compared for equality with this list
	 * @return <code>true</code> if the specified object is equal to this list
	 */
	@Override
	public boolean equals(final Object arg0)
	{
		return this==arg0;
	}

	/**
	 * Returns the hash code value for this list. The hash code of a list is
	 * defined to be the result of the following calculation:
	 *
	 * <pre>
	 * int hashCode = 1;
	 * for (E e : list)
	 * 	hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
	 * </pre>
	 *
	 * This ensures that <code>list1.equals(list2)</code> implies that
	 * <code>list1.hashCode()==list2.hashCode()</code> for any two lists,
	 * <code>list1</code> and <code>list2</code>, as required by the general
	 * contract of <code>Object.hashCode()</code>.
	 *
	 * @return the hash code value for this list
	 */
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

	/**
	 * Returns a list iterator over the elements in this list (in proper
	 * sequence), starting at the specified position in the list. The specified
	 * index indicates the first element that would be returned by an initial
	 * call to the <code>next</code> method. An initial call to the
	 * <code>previous</code> method would return the element with the specified
	 * index minus one.
	 *
	 * @return a list iterator over the elements in this list (in proper
	 *         sequence), starting at the specified position in the list
	 */
	@Override
	public ListIterator<K> listIterator()
	{
		return listIterator(0);
	}

	/**
	 * Returns a view of the portion of this list between the specified
	 * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive.
	 * (If <code>fromIndex</code> and <code>toIndex</code> are equal, the
	 * returned list is empty.) The returned list is backed by this list, so
	 * non-structural changes in the returned list are reflected in this list,
	 * and vice-versa. The returned list supports all of the optional list
	 * operations supported by this list.
	 *
	 * @param arg0 low endpoint (inclusive) of the subList
	 * @param arg1 high endpoint (exclusive) of the subList
	 * @return a view of the specified range within this list
	 */
	@Override
	public List<K> subList(final int arg0, final int arg1)
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

	/**
	 * Returns <code>true</code> if this collection contains all of the elements
	 * in the specified collection.
	 *
	 * @param c collection to be checked for containment in this collection
	 * @return <code>true</code> if this collection contains all of the elements
	 *         in the specified collection
	 */
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		for(final Object o : c)
		{
			if(!contains(o))
				return false;
		}
		return true;
	}

	/**
	 * Returns true if this collection contains no elements.
	 *
	 * @return <code>true</code> if this collection contains no elements
	 */
	@Override
	public boolean isEmpty()
	{
		return size==0;
	}

	/**
	 * Removes from this collection all of its elements that are contained in
	 * the specified collection (optional operation). After this call returns,
	 * this collection will contain no elements in common with the specified
	 * collection.
	 *
	 * @param c collection containing elements to be removed from this
	 *            collection
	 * @return <code>true</code> if this collection changed as a result of the
	 *         call
	 */
	@Override
	public synchronized boolean removeAll(final Collection<?> c)
	{
		if(c.size()==0)
			return true;
		boolean success=false;
		for (final Object name : c)
			success=remove(name)||success;
		return success;
	}

	/**
	 * Retains only the elements in this collection that are contained in the
	 * specified collection (optional operation). In other words, removes from
	 * this collection all of its elements that are not contained in the
	 * specified collection.
	 *
	 * @param c collection containing elements to be retained in this collection
	 * @return <code>true</code> if this collection changed as a result of the
	 *         call
	 */
	@Override
	public synchronized boolean retainAll(final Collection<?> c)
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

	/**
	 *  Constructs a string representation of this collection.  The string
	 *  consists of a list of the collection's elements in the order they are
	 *  returned by its iterator, enclosed in square brackets ("[]").
	 */
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
