package com.planet_ink.coffee_mud.core.collections;
import java.lang.ref.WeakReference;
import java.util.*;
/*
Copyright 2000-2011 Bo Zimmerman

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
public class CameleonList<K> implements List<K> 
{
	public volatile List<K> 	list;
	public volatile int[] 	 	masterCounter = {0};
	public volatile Signaler<K>signaler;
	
	public static abstract class Signaler<K>
	{
		private    volatile int 					counter = 0;
		protected  volatile WeakReference<List<K>> 	oldList;
		
		public Signaler(List<K> list)
		{
			oldList = new WeakReference<List<K>>(list);
		}
		
		public abstract boolean isInnerDeprecated();
		public abstract List<K> innerChangeMe(final CameleonList<K> me);
		
		public boolean isDeprecated(final int myCounter)
		{ 
			return (myCounter != counter) || isInnerDeprecated();
		}
		public final synchronized List<K> changeMe(final int[] deprecounter, final CameleonList<K> me)
		{
			if(!isDeprecated(deprecounter[0])) return me.list;
			deprecounter[0]=counter;
			oldList=new WeakReference<List<K>>(innerChangeMe(me));
			return oldList.get();
		}
		public void deprecate()
		{
			counter++;
		}
	}
	
	public CameleonList(List<K> l, final Signaler<K> deprecator)
	{
		list=l;
		this.signaler = deprecator;
	}
	
	@Override
	public boolean add(K arg0) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void add(int arg0, K arg1) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(Collection<? extends K> arg0) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends K> arg1) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void clear() {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean contains(Object arg0) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.contains(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.containsAll(arg0);
	}

	@Override
	public K get(int arg0) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.get(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.indexOf(arg0);
	}

	@Override
	public boolean isEmpty() {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.isEmpty();
	}

	@Override
	public Iterator<K> iterator() {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return new ReadOnlyIterator<K>(list.iterator());
	}

	@Override
	public int lastIndexOf(Object arg0) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.lastIndexOf(arg0);
	}

	@Override
	public ListIterator<K> listIterator() {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return new ReadOnlyListIterator<K>(list.listIterator());
	}

	@Override
	public ListIterator<K> listIterator(int arg0) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return new ReadOnlyListIterator<K>(list.listIterator(arg0));
	}

	@Override
	public boolean remove(Object arg0) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K remove(int arg0) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K set(int arg0, K arg1) {
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size() {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.size();
	}

	@Override
	public List<K> subList(int arg0, int arg1) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return new ReadOnlyList<K>(list.subList(arg0,arg1));
	}

	@Override
	public Object[] toArray() {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		if(signaler.isDeprecated(masterCounter[0]))
			list=signaler.changeMe(masterCounter, this);
		return list.toArray(arg0);
	}
}
