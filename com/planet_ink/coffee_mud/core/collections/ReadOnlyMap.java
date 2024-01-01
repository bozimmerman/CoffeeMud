package com.planet_ink.coffee_mud.core.collections;

import java.util.*;

/*
   Copyright 2022-2024 Bo Zimmerman

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

public class ReadOnlyMap<K, V> implements Map<K, V>
{

	final Map<K,V> map;
	
	public ReadOnlyMap(final Map<K,V> map)
	{
		this.map = map;
	}
	
	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return map.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public V remove(Object key)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public void clear()
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public Set<K> keySet()
	{
		return new ReadOnlySet<K>(map.keySet());
	}

	@Override
	public Collection<V> values()
	{
		return new ReadOnlyCollection<V>(map.values());
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return new ReadOnlySet<Entry<K,V>>(map.entrySet());
	}

}
