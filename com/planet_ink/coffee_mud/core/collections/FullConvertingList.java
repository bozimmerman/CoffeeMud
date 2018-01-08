package com.planet_ink.coffee_mud.core.collections;
import java.lang.reflect.Method;
import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class FullConvertingList<L,K> implements List<K>
{
	private final List<L> list;
	private final FullConverter<L, K> converter;
	private Class<?> lClass = null;
	private Class<?> kClass = null;

	public FullConvertingList(List<L> l, FullConverter<L, K> conv)
	{
		list=l;
		converter=conv;
		for(Method M : conv.getClass().getMethods())
		{
			if(M.getName().equals("reverseConvert") && (M.getParameterTypes().length>0))
			{
				lClass = M.getReturnType();
				kClass = M.getParameterTypes()[0];
				break;
			}
		}
	}

	@Override
	public boolean add(K arg0)
	{
		return list.add(converter.reverseConvert(arg0));
	}

	@Override
	public void add(int arg0, K arg1)
	{
		list.add(arg0,converter.reverseConvert(arg1));
	}
	
	@SuppressWarnings("unchecked")
	protected Object convertToListType(final Object arg0)
	{
		if(arg0 == null)
			return null;
		if(lClass.isAssignableFrom(arg0.getClass()))
			return arg0;
		if(kClass.isAssignableFrom(arg0.getClass()))
			return converter.reverseConvert((K)arg0);
		return null;
	}

	@Override
	public boolean addAll(Collection<? extends K> arg0)
	{
		if(arg0==null)
			return false;
		boolean didAll = true;
		for(K arg01 : arg0)
			didAll = add(arg01) && didAll;
		return didAll;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends K> arg1)
	{
		if(arg1==null)
			return false;
		for(K arg01 : arg1)
			add(arg0, arg01);
		return true;
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public boolean contains(Object arg0)
	{
		final Object o = convertToListType(arg0);
		if(o == null)
			return false;
		return list.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		if(arg0==null)
			return false;
		for(Object o : arg0)
		{
			o = convertToListType(o);
			if(o == null)
				return false;
			if(!list.contains(o))
				return false;
		}
		return true;
	}

	@Override
	public K get(int arg0)
	{
		return converter.convert(arg0,list.get(arg0));
	}

	@Override
	public int indexOf(Object arg0)
	{
		if(list.size()==0)
			return -1;
		final Object o = convertToListType(arg0);
		if(o == null)
			return -1;
		return list.indexOf(o);
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Override
	public Iterator<K> iterator()
	{
		return new FullConvertingIterator<L, K>(list.iterator(),converter);
	}

	@Override
	public int lastIndexOf(Object arg0)
	{
		if(list.size()==0)
			return -1;
		final Object o = convertToListType(arg0);
		if(o == null)
			return -1;
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<K> listIterator()
	{
		return new FullConvertingListIterator<L,K>(list.listIterator(), converter);
	}

	@Override
	public ListIterator<K> listIterator(int arg0)
	{
		return new FullConvertingListIterator<L,K>(list.listIterator(arg0), converter);
	}

	@Override
	public boolean remove(Object arg0)
	{
		final Object o = convertToListType(arg0);
		if(o == null)
			return false;
		return list.remove(o);
	}

	@Override
	public K remove(int arg0)
	{
		return converter.convert(arg0, list.remove(arg0));
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		if(arg0==null)
			return false;
		boolean didAll = true;
		for(Object o : arg0)
			didAll = remove(o) && didAll;
		return didAll;
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public K set(int arg0, K arg1)
	{
		throw new java.lang.IllegalArgumentException();
	}

	@Override
	public int size()
	{
		return list.size();
	}

	@Override
	public List<K> subList(int arg0, int arg1)
	{
		return new FullConvertingList<L,K>(list.subList(arg0,arg1),converter);
	}

	@Override
	public Object[] toArray()
	{
		final Object[] obj=new Object[list.size()];
		for(int x=obj.length-1;x>=0;x--)
			obj[x]=converter.convert(x,list.get(x));
		return obj;
	}

	@SuppressWarnings("unchecked")

	@Override
	public <T> T[] toArray(T[] a) 
	{
		if (a.length < list.size())
			return (T[]) Arrays.copyOf(toArray(), list.size(), a.getClass());
		System.arraycopy(toArray(), 0, a, 0, list.size());
		if (a.length > list.size())
			a[list.size()] = null;
		return a;
	}
}
