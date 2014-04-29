package com.planet_ink.coffee_mud.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.planet_ink.coffee_mud.core.interfaces.CMObject;

public class CMUniqSortSVec<T extends CMObject> extends SVector<T> implements SearchIDList<T>
{
	private static final long serialVersionUID = 6687178785122361992L;
	
	public CMUniqSortSVec(int size)
	{
		super();
	}
	
	public CMUniqSortSVec()
	{
	}
	
	private int compareTo(CMObject arg0, String arg1)
	{
		return arg0.ID().compareToIgnoreCase(arg1);
	}
	
	private int compareTo(CMObject arg0, CMObject arg1)
	{
		return arg0.ID().compareToIgnoreCase(arg1.ID());
	}
	
	@Override
	public synchronized boolean add(T arg0) 
	{
		if(arg0==null) return false;
		if(size()==0)
			return super.add(arg0);
		int start=0;
		int end=size()-1;
		int comp=-1;
		int mid=-1;
		while(start<=end)
		{
			mid=(end+start)/2;
			comp=compareTo(super.get(mid),arg0);
			if(comp==0)
				return false;
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;
		}
		if(comp==0)
			super.add(mid,arg0);
		else
		if(comp>0)
		{
			while((mid>=0)&&(compareTo(super.get(mid),arg0)>0))
				mid--;
			if(mid>=size()-1)
				super.add(arg0);
			else
			if(mid<0)
				super.add(0,arg0);
			else
				super.add(mid+1,arg0);
		}
		else
		{
			while((mid<size())&&(compareTo(super.get(mid),arg0)<0))
				mid++;
			if(mid>=size())
				super.add(arg0);
			else
				super.add(mid,arg0);
		}
		return true;
	}

	@Override
	public void add(int arg0, T arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends T> arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}
	@Override
	public boolean contains(Object arg0)
	{
		return indexOf(arg0)>=0;
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		for(Object o : arg0)
			if(!contains(o))
				return false;
		return true;
	}

	@Override
	public T get(int arg0)
	{
		return super.get(arg0);
	}

	@Override
	public synchronized int indexOf(Object arg0)
	{
		if(arg0==null) return -1;
		if(size()==0) return -1;
		int start=0;
		int end=size()-1;
		if(arg0 instanceof CMObject)
		{
			while(start<=end)
			{
				int mid=(end+start)/2;
				int comp=compareTo(super.get(mid),(CMObject)arg0);
				if(comp==0)
					return mid;
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
	
			}
		}
		else
		if(arg0 instanceof String)
		{
			while(start<=end)
			{
				int mid=(end+start)/2;
				int comp=compareTo(super.get(mid),(String)arg0);
				if(comp==0)
					return mid;
				else
				if(comp>0)
					end=mid-1;
				else
					start=mid+1;
	
			}
		}
		return -1;
	}

	public synchronized T find(String arg0) 
	{
		if(arg0==null) return null;
		if(size()==0) return null;
		int start=0;
		int end=size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=compareTo(super.get(mid),arg0);
			if(comp==0)
				return super.get(mid);
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;
		}
		return null;
	}
	
	public synchronized T find(CMObject arg0) 
	{
		if(arg0==null) return null;
		if(size()==0) return null;
		int start=0;
		int end=size()-1;
		while(start<=end)
		{
			int mid=(end+start)/2;
			int comp=compareTo(super.get(mid),arg0);
			if(comp==0)
				return super.get(mid);
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;

		}
		return null;
	}
	
	@Override
	public synchronized int lastIndexOf(Object arg0)
	{
		return indexOf(arg0); // only holds one-of-a-kind, so all is well!
	}

	@Override
	public synchronized boolean remove(Object arg0)
	{
		final int index=indexOf(arg0);
		if(index >= 0) 
			return remove(index)==arg0;
		return false;
	}

	@Override
	public T set(int arg0, T arg1)
	{
		throw new java.lang.UnsupportedOperationException();
	}
}
