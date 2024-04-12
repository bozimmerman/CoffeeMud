package com.planet_ink.coffee_mud.core.collections;
import java.lang.reflect.Method;
import java.util.*;

/*
   Copyright 2016-2024 Bo Zimmerman

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
public class IntegerRangeMap<K> implements Map<int[],K>
{
	private final Map<IntegerRange,K> map;
	private volatile int maxmin;
	private volatile int maxmax;
	public IntegerRangeMap()
	{
		map = Collections.synchronizedMap(new TreeMap<IntegerRange,K>());
		maxmin=-1;
		maxmax = -1;
	}
	
	private static Converter<IntegerRange, int[]> keyConverter = new Converter<IntegerRange, int[]>()
	{
		@Override
		public int[] convert(IntegerRange obj)
		{
			return obj.m;
		}

	};
	
	private final Converter<Map.Entry<IntegerRange, K>,Map.Entry<int[],K>> entryConverter = new Converter<Map.Entry<IntegerRange, K>,Map.Entry<int[],K>>()
	{
		@Override
		public Map.Entry<int[],K> convert(final Map.Entry<IntegerRange, K> e)
		{
			return new Map.Entry<int[],K>()
			{
				@Override
				public int[] getKey()
				{
					return e.getKey().m;
				}

				@Override
				public K getValue()
				{
					return e.getValue();
				}

				@Override
				public K setValue(K value)
				{
					return e.setValue(value);
				}
			};
		}

	};
	
	public static class IntegerRange implements Comparable<IntegerRange>
	{
		public final int[] m;
		public IntegerRange(int min, int max)
		{
			if(max<min)
			{
				int tmp = min;
				min=max;
				max=tmp;
			}
			this.m = new int[] {min,max};
		}
		@Override
		public int compareTo(IntegerRange o)
		{
			if(o == null)
				return 1;
			if(o.m[0]>m[1])
				return -1;
			if(o.m[1]<m[0])
				return 1;
			return 0;
		}
		
		@Override
		public boolean equals(final Object o)
		{
			if(!(o instanceof IntegerRange))
				return false;
			return compareTo((IntegerRange)o)==0;
		}

		@Override
		public int hashCode()
		{
			return this.m.hashCode();
		}
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

	protected IntegerRange makeRange(Object key)
	{
		if(key == null)
			return null;
		if(key instanceof IntegerRange)
			return (IntegerRange)key;
		if(key instanceof int[])
		{
			int[] k = (int[])key;
			IntegerRange r = new IntegerRange(k[0],k[1]);
			return r;
		}
		if(key instanceof Integer)
		{
			int k = ((Integer)key).intValue();
			IntegerRange r = new IntegerRange(k,k);
			return r;
		}
		if(key instanceof Short)
		{
			int k = ((Short)key).intValue();
			IntegerRange r = new IntegerRange(k,k);
			return r;
		}
		if(key instanceof Long)
		{
			int k = ((Long)key).intValue();
			IntegerRange r = new IntegerRange(k,k);
			return r;
		}
		if(key instanceof Double)
		{
			int k = ((Double)key).intValue();
			IntegerRange r = new IntegerRange(k,k);
			return r;
		}
		return null;
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		IntegerRange r = makeRange(key);
		if(r == null)
			return false;
		return map.containsKey(r);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	@Override
	public K get(Object key)
	{
		IntegerRange r = makeRange(key);
		if(r == null)
			return null;
		return map.get(r);
	}

	@Override
	public K put(int[] key, K value)
	{
		IntegerRange r = makeRange(key);
		if(r == null)
			return null;
		if(r.m[1] > maxmax)
		{
			maxmin = r.m[0];
			maxmax = r.m[1];
		}
		return map.put(r, value);
	}

	@Override
	public K remove(Object key)
	{
		IntegerRange r = makeRange(key);
		if(r == null)
			return null;
		final K val = map.remove(r); 
		if((val != null)
		&&(r.m[1] >=maxmin))
		{
			maxmin=-1;
			maxmax=-1;
			for(IntegerRange rs : map.keySet())
			{
				if(rs.m[0]>maxmax)
				{
					maxmin = rs.m[0];
					maxmax = rs.m[1];
				}
			}
		}
		return val;
	}

	public int getMax()
	{
		return maxmax;
	}
	
	@Override
	public void putAll(Map<? extends int[], ? extends K> m)
	{
		for(final Map.Entry<? extends int[], ? extends K> e : m.entrySet())
			this.put(e.getKey(),e.getValue());
	}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public Set<int[]> keySet()
	{
		return new ConvertingSet<IntegerRange,int[]>(map.keySet(),keyConverter);
	}

	@Override
	public Collection<K> values()
	{
		return map.values();
	}

	@Override
	public Set<Entry<int[], K>> entrySet()
	{
		return new ConvertingSet<Map.Entry<IntegerRange, K>,Map.Entry<int[],K>>(map.entrySet(),entryConverter);
	}

}
