package com.planet_ink.coffee_mud.core.collections;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/*
   Copyright 2012-2020 Bo Zimmerman

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
public class StringXVector extends XVector<String>
{
	/**
	 *
	 */
	private static final long	serialVersionUID	= -2080154538499166336L;

	public StringXVector(final List<?> V)
	{
		super();
		if (V != null)
		{
			for(final Object o : V)
				add(o.toString());
		}
	}

	public StringXVector()
	{
		super();
	}

	public StringXVector(final int size, final boolean boo)
	{
		super(size, boo);
	}

	public StringXVector(final Object[] E)
	{
		super();
		if (E != null)
		{
			for (final Object o : E)
				add(o.toString());
		}
	}

	public StringXVector(final Object E)
	{
		super();
		if (E != null)
			add(E.toString());
	}

	public StringXVector(final Object E, final Object E2)
	{
		this(E);
		if (E2 != null)
			add(E2.toString());
	}

	public StringXVector(final Object E, final Object E2, final Object E3)
	{
		this(E, E2);
		if (E3 != null)
			add(E3.toString());
	}

	public StringXVector(final Object E, final Object E2, final Object E3, final Object E4)
	{
		this(E, E2, E3);
		if (E4 != null)
			add(E4.toString());
	}

	public StringXVector(final Set<?> E)
	{
		super();
		if (E != null)
		{
			for (final Object o : E)
				add(o.toString());
		}
	}

	public StringXVector(final Enumeration<?> E)
	{
		super();
		if (E != null)
		{
			for (; E.hasMoreElements();)
				add(E.nextElement().toString());
		}
	}

	public StringXVector(final Iterator<?> E)
	{
		super();
		if (E != null)
		{
			for (; E.hasNext();)
				add(E.next().toString());
		}
	}

}
