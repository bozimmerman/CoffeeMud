package com.planet_ink.coffee_mud.core.collections;
import java.util.*;
/*
Copyright 2000-2013 Bo Zimmerman

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

import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
public class TriadVector<T,K,L> extends Vector<Triad<T,K,L>> implements List<Triad<T,K,L>>
{
	private static final long serialVersionUID = -9175373358892311411L;
	public Triad.FirstConverter<T,K,L> getFirstConverter() {
		return new Triad.FirstConverter<T, K, L>();
	}
	public Triad.SecondConverter<T,K,L> getSecondConverter() {
		return new Triad.SecondConverter<T, K, L>();
	}
	public Triad.ThirdConverter<T,K,L> getThirdConverter() {
		return new Triad.ThirdConverter<T, K, L>();
	}
	public Enumeration<T> firstElements()
	{
		return new ConvertingEnumeration<Triad<T,K,L>,T>(
				elements(),getFirstConverter());
	}
	public Enumeration<K> secondElements()
	{
		return new ConvertingEnumeration<Triad<T,K,L>,K>(
			elements(),getSecondConverter());
	}
	public Enumeration<L> thirdElements()
	{
		return new ConvertingEnumeration<Triad<T,K,L>,L>(
			elements(),getThirdConverter());
	}
	public Iterator<T> firstIterator()
	{
		return new ConvertingIterator<Triad<T,K,L>,T>(
			iterator(),getFirstConverter());
	}
	public Iterator<K> secondIterator()
	{
		return new ConvertingIterator<Triad<T,K,L>,K>(
			iterator(),getSecondConverter());
	}
	public Iterator<L> thirdIterator()
	{
		return new ConvertingIterator<Triad<T,K,L>,L>(
			iterator(),getThirdConverter());
	}
	public synchronized int indexOfFirst(T t)
	{
		return indexOfFirst(t,0);
	}
	public synchronized int indexOfSecond(K k)
	{
		return indexOfSecond(k,0);
	}
	public synchronized int indexOfThird(L l)
	{
		return indexOfThird(l,0);
	}
	public T getFirst(int index)
	{
		return get(index).first;
	}
	public K getSecond(int index)
	{
		return get(index).second;
	}
	public L getThird(int index)
	{
		return get(index).third;
	}
	public void add(T t, K k, L l)
	{
		add(new Triad<T,K,L>(t,k,l));
	}
	public void addElement(T t, K k, L l)
	{
		add(new Triad<T,K,L>(t,k,l));
	}
	public boolean containsFirst(T t)
	{
		for(Iterator<Triad<T,K,L>> i=iterator();i.hasNext();)
			if((t==null)?i.next()==null:t.equals(i.next().first))
				return true;
		return false;
	}
	public boolean containsSecond(K k)
	{
		for(Iterator<Triad<T,K,L>> i=iterator();i.hasNext();)
			if((k==null)?i.next()==null:k.equals(i.next().second))
				return true;
		return false;
	}
	public boolean containsThird(L l)
	{
		for(Iterator<Triad<T,K,L>> i=iterator();i.hasNext();)
			if((l==null)?i.next()==null:l.equals(i.next().third))
				return true;
		return false;
	}
	public T elementAtFirst(int index)
	{
		return get(index).first;
	}
	public K elementAtSecond(int index)
	{
		return get(index).second;
	}
	public L elementAtThird(int index)
	{
		return get(index).third;
	}
	public int indexOfFirst(T t, int index)
	{
		try{
			for(int i=index;i<size();i++)
				if((t==null ? get(i).first==null : t.equals(get(i).first))) 
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int indexOfSecond(K k, int index)
	{
		try{
			for(int i=index;i<size();i++)
				if((k==null ? get(i).second==null : k.equals(get(i).second))) 
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int indexOfThird(L l, int index)
	{
		try{
			for(int i=index;i<size();i++)
				if((l==null ? get(i).third==null : l.equals(get(i).third))) 
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int lastIndexOfFirst(T t, int index)
	{
		try{
			for(int i=index;i>=0;i--)
				if((t==null ? get(i).first==null : t.equals(get(i).first))) 
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int lastIndexOfSecond(K k, int index)
	{
		try{
			for(int i=index;i>=0;i--)
				if((k==null ? get(i).second==null : k.equals(get(i).second))) 
					return i;
		}catch(Exception e){}
		return -1;
	}
	public int lastIndexOfThird(L l, int index)
	{
		try{
			for(int i=index;i>=0;i--)
				if((l==null ? get(i).third==null : l.equals(get(i).third))) 
					return i;
		}catch(Exception e){}
		return -1;
	}
	public synchronized int lastIndexOfFirst(T t)
	{
		return lastIndexOfFirst(t,size()-1);
	}
	public synchronized int lastIndexOfSecond(K k)
	{
		return lastIndexOfSecond(k,size()-1);
	}
	public synchronized int lastIndexOfThird(L l)
	{
		return lastIndexOfThird(l,size()-1);
	}
	public synchronized boolean removeFirst(T t)
	{
		Triad<T,K,L> pair;
		for(final Iterator<Triad<T,K,L>> i=iterator();i.hasNext();)
		{
			pair=i.next();
			if((t==null ? pair.first==null : t.equals(pair.first))) 
			{
				i.remove();
				return true;
			}
		}
		return false;
	}
	public synchronized boolean removeSecond(K k)
	{
		Triad<T,K,L> pair;
		for(final Iterator<Triad<T,K,L>> i=iterator();i.hasNext();)
		{
			pair=i.next();
			if((k==null ? pair.second==null : k.equals(pair.second))) 
			{
				i.remove();
				return true;
			}
		}
		return false;
	}
	public synchronized boolean removeThird(L l)
	{
		Triad<T,K,L> pair;
		for(final Iterator<Triad<T,K,L>> i=iterator();i.hasNext();)
		{
			pair=i.next();
			if((l==null ? pair.third==null : l.equals(pair.third))) 
			{
				i.remove();
				return true;
			}
		}
		return false;
	}
	public boolean removeElementFirst(T t)
	{
		return removeFirst(t);
	}
	public boolean removeElementSecond(K k)
	{
		return removeSecond(k);
	}
	public boolean removeElementThird(L l)
	{
		return removeThird(l);
	}
	public T firstFirstElement(int index)
	{
		return firstElement().first;
	}
	public K firstSecondElement(int index)
	{
		return firstElement().second;
	}
	public L firstThirdElement(int index)
	{
		return firstElement().third;
	}
	public T lastFirstElement(int index)
	{
		return lastElement().first;
	}
	public K lastSecondElement(int index)
	{
		return lastElement().second;
	}
	public L lastThirdElement(int index)
	{
		return lastElement().third;
	}
	public T[] toArrayFirst(T[] a)
	{
		T[] objs= toArray(a);
		for(int x=0;x<size();x++)
			objs[x] = (T) getFirst(x);
		return objs;
	}
	public K[] toArraySecond(K[] a)
	{
		K[] objs= toArray(a);
		for(int x=0;x<size();x++)
			objs[x] = (K) getSecond(x);
		return objs;
	}
	public L[] toArrayThird(L[] a)
	{
		L[] objs= toArray(a);
		for(int x=0;x<size();x++)
			objs[x] = (L) getThird(x);
		return objs;
	}
}
