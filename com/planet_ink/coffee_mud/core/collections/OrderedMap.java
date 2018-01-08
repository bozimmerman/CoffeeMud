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
   Copyright 2013-2018 Bo Zimmerman

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
	private volatile ArrayList<J> list = null;
	@SuppressWarnings("rawtypes" )
	private static final Iterator empty=EmptyIterator.INSTANCE;

	@SuppressWarnings("unchecked")

	@Override public Iterator<J> iterator()
	{
		if(size()==0)
			return empty;
		return list.iterator();
	}

	@SuppressWarnings("unchecked")

	@Override public synchronized J put(K key, J value)
	{
		final ArrayList<J> newList;
		if (list == null)
		{
			newList=new ArrayList<J>(0);
			newList.add(value);
		}
		else
		{
			if(containsKey(key))
			{
				if((list.size()>0)&&(list.get(0)==value))
					return value;
			}
			newList=(ArrayList<J>)list.clone();
			if(containsKey(key))
				newList.remove(value);
			if(newList.size()==0)
				newList.add(value);
			else
				newList.add(0, value);
		}
		list=newList;
		return super.put(key, value);
	}

	@Override public synchronized void putAll(Map<? extends K, ? extends J> t)
	{
		for(final Map.Entry<? extends K,? extends J> i : t.entrySet())
			put(i.getKey(),i.getValue());
	}

	@Override public synchronized J remove(Object key)
	{
		if(super.containsKey(key))
		{
			@SuppressWarnings("unchecked")
			final
			ArrayList<J> newList=(ArrayList<J>)list.clone();
			newList.remove(get(key));
			list=newList;
		}
		return super.remove(key);
	}

	@Override public synchronized void clear()
	{
		list=null;
		super.clear();
	}
}
