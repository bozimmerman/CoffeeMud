package com.planet_ink.coffee_mud.core.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
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
public class CaselessTreeMap<K> extends TreeMap<String,K>
{
	private static final long serialVersionUID = 5949532522375107316L;
	public CaselessTreeMap()
	{
		super(new Comparator<String>()
		{
			@Override
			public int compare(String arg0, String arg1)
			{
				return arg0.compareToIgnoreCase(arg1);
			}
		});
	}
}
