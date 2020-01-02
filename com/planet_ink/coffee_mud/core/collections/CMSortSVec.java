package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

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
public class CMSortSVec<T extends CMObject> extends SortedStrSVector<T> implements SearchIDList<T>
{
	private static final long serialVersionUID = 6687178785122361992L;

	@SuppressWarnings("rawtypes")
	private static final SortedStrSVector.Str idStringer=new SortedStrSVector.Str<CMObject>()
	{
		@Override
		public String toString(final CMObject t)
		{
			return t.ID();
		}
	};

	@SuppressWarnings("unchecked")
	public CMSortSVec(final int size)
	{
		super(idStringer,size);
	}

	@SuppressWarnings("unchecked")
	public CMSortSVec()
	{
		super(idStringer);
	}
}
