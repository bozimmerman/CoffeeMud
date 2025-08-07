package com.planet_ink.fakedb.backend.structure;
/*
Copyright 2001 Thomas Neumann
Copyright 2004-2025 Bo Zimmerman

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
/**
*
* @author Bo Zimmerman
*
*/
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComparableValue implements Comparable
{
	private Comparable	v;

	public ComparableValue(final Comparable v)
	{
		if (v instanceof ComparableValue)
			this.v = ((ComparableValue) v).v;
		else
			this.v = v;
	}

	@Override
	public int hashCode()
	{
		if (v != null)
			return v.hashCode();
		return 0;
	}

	public Comparable getValue()
	{
		return v;
	}

	@Override
	public boolean equals(final Object o)
	{
		Object t = o;
		if (o instanceof ComparableValue)
			t = ((ComparableValue) o).getValue();
		if ((v == null) && (t == null))
			return true;
		if ((v == null) || (t == null))
			return false;
		return v.equals(t);
	}

	@Override
	public int compareTo(final Object o)
	{
		Object to = o;
		if (o instanceof ComparableValue)
			to = ((ComparableValue) o).v;
		if ((v == null) && (to == null))
			return 0;
		if (v == null)
			return -1;
		if (to == null)
			return 1;
		return v.compareTo(to);
	}
}

