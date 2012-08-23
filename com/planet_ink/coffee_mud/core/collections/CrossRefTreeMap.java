package com.planet_ink.coffee_mud.core.collections;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/*
Copyright 2012-2012 Bo Zimmerman

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
public class CrossRefTreeMap<T extends Comparable<T>, K extends Comparable<K>>
{
	final TreeMap<T,TreeSet<K>> map1		=new TreeMap<T,TreeSet<K>>();
	final TreeMap<K,TreeSet<T>> map2		=new TreeMap<K,TreeSet<T>>();
	final int 					maxKsInMap1;
	final int 					maxTsInMap2;
	@SuppressWarnings("rawtypes")
    private static final Set empty=new TreeSet();
	
	public CrossRefTreeMap(int maxFirst, int maxSecond)
	{
		if(maxFirst<=0) maxFirst=1;
		if(maxSecond<=0) maxSecond=1;
		maxKsInMap1=maxFirst;
		maxTsInMap2=maxSecond;
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
			return (Set<K>)empty;
		return kSet;
	}
	
	@SuppressWarnings("unchecked")
    public Set<T> getSecond(K k)
	{
		final Set<T> tSet=map2.get(k);
		if(tSet == null)
			return (Set<T>)empty;
		return tSet;
	}
	
	public synchronized void remove(T t, K k)
	{
		if(map1.containsKey(t))
		{
			final TreeSet<K> tKs=map1.get(t);
			if(tKs.contains(k))
			{
				if(tKs.size()==1)
					map1.remove(t);
				else
    				tKs.remove(k);
			}
		}
		if(map2.containsKey(k))
		{
			final TreeSet<T> kTs=map2.get(k);
			if(kTs.contains(k))
			{
				if(kTs.size()==1)
					map2.remove(k);
				else
    				kTs.remove(t);
			}
		}
	}
	
	public synchronized void change(T t, K k)
	{
		final TreeSet<K> tKs;
		if(!map1.containsKey(t))
		{
			tKs=new TreeSet<K>();
			map1.put(t, tKs);
		}
		else
			tKs=map1.get(t);
		final TreeSet<T> kTs;
		if(!map2.containsKey(k))
		{
			kTs=new TreeSet<T>();
			map2.put(k, kTs);
		}
		else
			kTs=map2.get(k);
		if(!kTs.contains(t))
		{
    		while(kTs.size()>=maxTsInMap2)
    			kTs.remove(kTs.first());
    		kTs.add(t);
		}
		if(!tKs.contains(k))
		{
    		while(tKs.size()>=maxKsInMap1)
    			tKs.remove(tKs.first());
    		tKs.add(k);
		}
	}
}
