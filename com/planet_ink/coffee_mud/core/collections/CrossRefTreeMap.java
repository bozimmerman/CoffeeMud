package com.planet_ink.coffee_mud.core.collections;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

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
public class CrossRefTreeMap<T, K>
{
	final static Comparator<Object> comparator=new Comparator<Object>()
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			if(o1 == null)
			{
				if(o2 == null)
					return 0;
				return 1;
			}
			if(o2 == null)
				return -1;
			if(o1.hashCode() == o2.hashCode())
				return 0;
			return o1.hashCode() > o2.hashCode() ? 1 : -1;
		}
	};
	final TreeMap<T,TreeSet<K>> map1		=new TreeMap<T,TreeSet<K>>(comparator);
	final TreeMap<K,TreeSet<T>> map2		=new TreeMap<K,TreeSet<T>>(comparator);
	final int 					maxKsInMap1;
	final int 					maxTsInMap2;
	@SuppressWarnings("rawtypes")
	private static final Set empty=new TreeSet();

	public CrossRefTreeMap(int maxFirstForEachSecond, int maxSecondForEachFirst)
	{
		if(maxSecondForEachFirst<=0)
			maxSecondForEachFirst=1;
		if(maxFirstForEachSecond<=0)
			maxFirstForEachSecond=1;
		maxKsInMap1=maxSecondForEachFirst;
		maxTsInMap2=maxFirstForEachSecond;
	}

	public boolean containsFirst(T t)
	{
		return map1.containsKey(t);
	}

	public boolean containsSecond(K k)
	{
		return map2.containsKey(k);
	}

	@SuppressWarnings("unchecked")
	public Set<K> getFirst(T t)
	{
		final Set<K> kSet=map1.get(t);
		if(kSet == null)
			return empty;
		return kSet;
	}

	@SuppressWarnings("unchecked")
	public Set<T> getSecond(K k)
	{
		final Set<T> tSet=map2.get(k);
		if(tSet == null)
			return empty;
		return tSet;
	}

	public synchronized void remove(T t, K k)
	{
		final TreeSet<K> tKs=map1.get(t);
		if(tKs!=null)
		{
			if(tKs.contains(k))
			{
				if(tKs.size()==1)
				{
					tKs.clear();
					map1.remove(t);
				}
				else
					tKs.remove(k);
			}
		}
		final TreeSet<T> kTs=map2.get(k);
		if(kTs!=null)
		{
			if(kTs.contains(t))
			{
				if(kTs.size()==1)
				{
					kTs.clear();
					map2.remove(k);
				}
				else
					kTs.remove(t);
			}
		}
	}

	public synchronized void removeFirst(T t)
	{
		final TreeSet<K> tKs=map1.get(t);
		if(tKs!=null)
		{
			for(final K k : tKs)
			{
				final TreeSet<T> kTs=map2.get(k);
				if(kTs!=null)
				{
					if(kTs.size()==1)
					{
						kTs.clear();
						map2.remove(k);
					}
					else
						kTs.remove(t);
				}
			}
			map1.remove(t);
		}
	}

	public synchronized void removeSecond(K k)
	{
		final TreeSet<T> kTs=map2.get(k);
		if(kTs!=null)
		{
			for(final T t : kTs)
			{
				final TreeSet<K> tKs=map1.get(t);
				if(tKs!=null)
				{
					if(tKs.size()==1)
					{
						tKs.clear();
						map1.remove(t);
					}
					else
						tKs.remove(k);
				}
			}
			map2.remove(k);
		}
	}

	public synchronized void change(T t, K k)
	{
		TreeSet<K> tKs=map1.get(t);
		while((tKs!=null)&&(tKs.size()>=maxKsInMap1))
			remove(t,tKs.first());
		tKs=map1.get(t);
		if(tKs==null)
		{
			tKs=new TreeSet<K>(comparator);
			map1.put(t, tKs);
		}
		TreeSet<T> kTs=map2.get(k);
		while((kTs!=null)&&(kTs.size()>=maxTsInMap2))
			remove(kTs.first(),k);
		kTs=map2.get(k);
		if(kTs==null)
		{
			kTs=new TreeSet<T>(comparator);
			map2.put(k, kTs);
		}
		if(!kTs.contains(t))
			kTs.add(t);
		if(!tKs.contains(k))
			tKs.add(k);
	}

	public synchronized void clear()
	{
		map1.clear();
		map2.clear();
	}

}
