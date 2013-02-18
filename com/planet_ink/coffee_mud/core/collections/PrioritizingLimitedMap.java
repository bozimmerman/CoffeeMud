package com.planet_ink.coffee_mud.core.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/*
Copyright 2012-2013 Bo Zimmerman

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
public class PrioritizingLimitedMap<T extends Comparable<T>, K> implements Map<T, K>
{
	protected int  		 itemLimit;
	protected final long touchAgeLimitMillis;
	protected final long maxAgeLimitMillis;
	protected int 		 threshHoldToExpand;
	
	
	private class LinkedEntry<V,W> extends Pair<V,W>
	{
		public volatile LinkedEntry<V,W> next=null;
		public volatile LinkedEntry<V,W> prev=null;
		public volatile int 			 priority=0;
		public volatile int  			 index=0;
		public volatile long			 lastTouch=System.currentTimeMillis(); 
		public final    long  			 birthDate=System.currentTimeMillis();
		public LinkedEntry(V frst, W scnd)
        {
	        super(frst, scnd);
        }
	}
	
	protected volatile LinkedEntry<T,K>  head=null;
	protected volatile LinkedEntry<T,K>  tail=null;
	protected final TreeMap<T,LinkedEntry<T,K>> map=new TreeMap<T,LinkedEntry<T,K>>(); 

    public PrioritizingLimitedMap(int itemLimit, long touchAgeLimitMillis, long maxAgeLimitMillis, int threshHoldToExpand)
	{
		if(itemLimit<=0) itemLimit=1;
		this.itemLimit=itemLimit;
		this.touchAgeLimitMillis=touchAgeLimitMillis;
		this.maxAgeLimitMillis=maxAgeLimitMillis;
		this.threshHoldToExpand=threshHoldToExpand;
	}
	
    public PrioritizingLimitedMap(int itemLimit, long touchAgeLimitMillis, long maxAgeLimitMillis)
	{
		this(itemLimit,touchAgeLimitMillis,maxAgeLimitMillis,Integer.MAX_VALUE);
	}
	
	@Override
	public K get(Object key)
	{
		LinkedEntry<T,K> p=map.get(key);
		if(p!=null)
		{
			markFoundAgain(p);
			trimDeadwood();
			return p.second;
		}
		return null;
	}

	@Override
    public synchronized void clear() 
	{
		head=null;
		tail=null;
		map.clear();
    }

	@Override
    public boolean containsKey(Object arg0) { return map.containsKey(arg0); }
	
	public Enumeration<T> prioritizedKeys()
	{
		return new Enumeration<T>()
				{
					private LinkedEntry<T,K>ptr=head;
					@Override
					public boolean hasMoreElements()
					{
						return ptr!=null;
					}

					@Override
					public T nextElement()
					{
						if(ptr!=null)
						{
							 T elem=ptr.first;
							 ptr=ptr.next;
							 return elem;
						}
						return null;
					}
				};
	}
	
	@Override
    public synchronized boolean containsValue(Object arg0) {
		for(LinkedEntry<T,K> p : map.values())
			if(p.first==arg0)
				return true;
		return false;
    }

	@Override
    public synchronized Set<java.util.Map.Entry<T, K>> entrySet() {
		final Set<java.util.Map.Entry<T, K>> c= new TreeSet<java.util.Map.Entry<T, K>>();
		for(T t : map.keySet())
			c.add(new Pair<T,K>(t,map.get(t).second));
		return c;
    }

	@Override
    public boolean isEmpty() { return map.isEmpty(); }

	@Override
    public Set<T> keySet() { return map.keySet(); }

	private void markFoundAgain(LinkedEntry<T,K> p)
	{
		p.priority++;
		p.lastTouch=System.currentTimeMillis();
		LinkedEntry<T,K> pp=p.prev;
		while((pp!=null) && (p.priority > pp.priority))
		{
			LinkedEntry<T,K> pn=p.next;
			int ppIndex=pp.index;
			pp.index=p.index;
			p.index=ppIndex;
			p.prev=pp.prev;
			p.next=pp;
			if(pp.prev==null)
				head=p;
			else
				pp.prev.next=p;
			pp.prev=p;
			if(pn != null)
				pn.prev=pp;
			else
				tail=pp;
			pp.next=pn;
			pp=p.prev;
		}
	}
	
	private void trimDeadwood()
	{
		if(map.size() > itemLimit)
		{
			LinkedEntry<T,K> prev=tail;
			final long touchTimeout=System.currentTimeMillis()-touchAgeLimitMillis;
			final long maxAgeTimeout=System.currentTimeMillis()-maxAgeLimitMillis;
			int expands=0;
			while((prev != null)&&(prev != head)&&(prev.index <=0)&&(map.size() > itemLimit))
			{
				final LinkedEntry<T,K> pprev=prev.prev;
				if((prev.lastTouch<touchTimeout)||(prev.birthDate<maxAgeTimeout))
					remove(prev.first);
				else
				if(prev.priority > this.threshHoldToExpand)
					expands=1; // dont want to count the same ones every time through
				prev=pprev;
			}
			itemLimit+=expands;
		}
	}
	
	@Override
    public synchronized K put(T arg0, K arg1) {
		LinkedEntry<T,K> p=map.get(arg0);
		if(p == null)
		{
			p=new LinkedEntry<T,K>(arg0,arg1);
			map.put(arg0,p);
			if(tail == null)
			{
				head=p;
				tail=p;
				p.index=itemLimit;
			}
			else
			{
				p.index=tail.index-1;
				tail.next=p;
				p.prev=tail;
				tail=p;
			}
		}
		else
		{
			if(p.second!=arg1)
				p.second=arg1;
			markFoundAgain(p);
		}
		trimDeadwood();
		return arg1;
    }

	@Override
    public synchronized void putAll(Map<? extends T, ? extends K> arg0) {
		for(T t : arg0.keySet())
			put(t,arg0.get(t));
    }

	@Override
    public synchronized K remove(Object arg0) {
		final LinkedEntry<T,K> p=map.get(arg0);
		if(p == null) return null;
		map.remove(arg0);
		LinkedEntry<T,K> pn=p.next;
		while((pn != null)&&(tail != pn))
		{
			pn.index++;
			pn=pn.next;
		}
		if(head == p) head=p.next;
		if(tail == p) tail=p.prev;
		if(p.prev != null)
			p.prev.next=p.next;
		if(p.next!=null)
			p.next.prev=p.prev;
		return p.second;
    }

	@Override
    public int size() {
		return map.size();
    }

	@Override
    public synchronized Collection<K> values() {
		final Collection<K> c= new Vector<K>(map.size());
		for(T t : map.keySet())
			c.add(map.get(t).second);
		return c;
    }
	
}
