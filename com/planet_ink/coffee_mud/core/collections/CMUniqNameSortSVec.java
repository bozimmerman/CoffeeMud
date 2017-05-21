package com.planet_ink.coffee_mud.core.collections;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

public class CMUniqNameSortSVec<T extends CMObject> extends CMUniqSortSVec<T>
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5770001849890830938L;

	public CMUniqNameSortSVec(int size)
	{
		super(size);
	}

	public CMUniqNameSortSVec()
	{
		super();
	}

	public CMUniqNameSortSVec(CMUniqNameSortSVec<T> O)
	{
		super();
		for(T o: O)
			this.add(o);
	}

	@Override
	protected int compareTo(CMObject arg0, String arg1)
	{
		return arg0.name().compareToIgnoreCase(arg1);
	}

	@Override
	protected int compareTo(CMObject arg0, CMObject arg1)
	{
		return arg0.name().compareToIgnoreCase(arg1.name());
	}
	
	@SuppressWarnings("unchecked")

	@Override
	public synchronized SVector<T> copyOf()
	{
		try
		{
			return (CMUniqNameSortSVec<T>) clone();
		}
		catch (final Exception e)
		{
			return new CMUniqNameSortSVec<T>(this);
		}
	}

}
