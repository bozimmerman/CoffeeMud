package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

public class CMSortSVec<T extends CMObject> extends SortedStrSVector<T> implements SearchIDList<T>
{
	private static final long serialVersionUID = 6687178785122361992L;

	@SuppressWarnings("rawtypes")
	private static final SortedStrSVector.Str idStringer=new SortedStrSVector.Str<CMObject>()
	{
		@Override
		public String toString(CMObject t)
		{
			return t.ID();
		}
	};

	@SuppressWarnings("unchecked")
	public CMSortSVec(int size)
	{
		super(idStringer,size);
	}

	@SuppressWarnings("unchecked")
	public CMSortSVec()
	{
		super(idStringer);
	}
}
