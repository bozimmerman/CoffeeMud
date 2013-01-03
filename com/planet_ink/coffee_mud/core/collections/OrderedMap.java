package com.planet_ink.coffee_mud.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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
public class OrderedMap<K,J> extends Hashtable<K,J> implements Iterable<J>
{
	private static final long serialVersionUID = -6379440278237091571L;
	private volatile ArrayList<J> list = new ArrayList<J>(0);


	@Override public Iterator<J> iterator() {
		return list.iterator();
	}

	private void addToList(final ArrayList<J> newList, K key, J value)
	{
		if(containsKey(key))
		{
			if((list.size()>0)&&(list.get(0)==value))
				return;
			list.remove(value);
		}
		newList.add(value);
		newList.addAll(list);
	}
	
	@Override public synchronized J put(K key, J value)
	{
		ArrayList<J> newList=new ArrayList<J>(list.size()+1);
		addToList(newList, key,value);
		list=newList;
		return super.put(key, value);
	}
	
	@Override public synchronized void putAll(Map<? extends K, ? extends J> t)
	{
		ArrayList<J> newList=new ArrayList<J>(list.size()+t.size());
		for(Map.Entry<? extends K,? extends J> i : t.entrySet())
			addToList(newList, i.getKey(), i.getValue());
		list=newList;
		super.putAll(t);
	}
	
	@Override public synchronized J remove(Object key)
	{
		if(super.containsKey(key))
		{
			@SuppressWarnings("unchecked")
			ArrayList<J> newList=(ArrayList<J>)list.clone();
			newList.remove(get(key));
		}
		return super.remove(key);
	}
}
