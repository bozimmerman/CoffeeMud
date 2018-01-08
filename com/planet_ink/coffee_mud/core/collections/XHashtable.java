package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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

/*
 * A version of the Vector class that provides to "safe" adds
 * and removes by copying the underlying vector whenever those
 * operations are done.
 */
public class XHashtable<K, V> extends Hashtable<K, V>
{
	private static final long	serialVersionUID	= 6687178785122563992L;

	public XHashtable()
	{
		super();
	}

	public XHashtable(Map<K, V> V)
	{
		super();
		if (V != null)
			putAll(V);
	}

	public synchronized void removeAll(Enumeration<K> E)
	{
		if (E != null)
		{
			for (; E.hasMoreElements();)
				remove(E.nextElement());
		}
	}

	public synchronized void removeAll(Iterator<K> E)
	{
		if (E != null)
		{
			for (; E.hasNext();)
				remove(E.next());
		}
	}

	public synchronized void removeAll(List<K> E)
	{
		if (E != null)
		{
			for (final K o : E)
				remove(o);
		}
	}
}
