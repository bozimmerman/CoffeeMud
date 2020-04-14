package com.planet_ink.coffee_mud.core.collections;

import java.lang.reflect.Array;
import java.util.*;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Modifiable;

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
public class CModifiableStringMap implements Map<String, String>, Modifiable
{
	final Map<String,String> baseMap;

	public CModifiableStringMap(final Map<String,String> baseMap)
	{
		this.baseMap=baseMap;
	}

	@Override
	public String ID()
	{
		return "CModifiableStringMap";
	}

	@Override
	public String name()
	{
		return "CModifiableStringMap";
	}

	@Override
	public CMObject newInstance()
	{
		return new CModifiableStringMap(baseMap);
	}

	@Override
	public CMObject copyOf()
	{
		return new CModifiableStringMap(baseMap);
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		if(o==null)
			return 1;
		final Integer hash1=Integer.valueOf(this.hashCode());
		final Integer hash2=Integer.valueOf(o.hashCode());
		return hash1.compareTo(hash2);
	}

	@Override
	public String[] getStatCodes()
	{
		final String[] codes = new String[baseMap.size()];
		int index=0;
		for(final String key : baseMap.keySet())
			codes[index++] = key;
		return codes;
	}

	@Override
	public int getSaveStatIndex()
	{
		return 0;
	}

	@Override
	public String getStat(final String code)
	{
		if(baseMap.containsKey(code))
			return baseMap.get(code);
		return "";
	}

	@Override
	public boolean isStat(final String code)
	{
		return baseMap.containsKey(code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		baseMap.put(code, val);
	}

	@Override
	public int size()
	{
		return baseMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return baseMap.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key)
	{
		return baseMap.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value)
	{
		return baseMap.containsValue(value);
	}

	@Override
	public String get(final Object key)
	{
		return baseMap.get(key);
	}

	@Override
	public String put(final String key, final String value)
	{
		return baseMap.put(key, value);
	}

	@Override
	public String remove(final Object key)
	{
		return baseMap.remove(key);
	}

	@Override
	public void putAll(final Map<? extends String, ? extends String> m)
	{
		baseMap.putAll(m);
	}

	@Override
	public void clear()
	{
		baseMap.clear();
	}

	@Override
	public Set<String> keySet()
	{
		return baseMap.keySet();
	}

	@Override
	public Collection<String> values()
	{
		return baseMap.values();
	}

	@Override
	public Set<Entry<String, String>> entrySet()
	{
		return baseMap.entrySet();
	}
}
