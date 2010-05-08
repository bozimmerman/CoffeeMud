package com.planet_ink.coffee_mud.core.collections;
import java.io.Serializable;
import java.util.*;

/* 
Copyright 2000-2010 Bo Zimmerman

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
public class SVector<T> implements Serializable, Iterable<T>, Collection<T>, List<T>, RandomAccess 
{
	private static final long serialVersionUID = 6687178785122561992L;
	private volatile Vector<T> V;

	public SVector()
	{
		V=new Vector<T>();
	}
	
	public SVector(int size)
	{
		V=new Vector<T>(size);
	}
	
	public SVector(List<T> E)
	{
		V=new Vector<T>();
		if(E!=null)
			this.V.addAll(E);
	}
	
	public SVector(T... E)
	{
		V=new Vector<T>();
		if(E!=null)
			for(T o : E)
				V.add(o);
	}
	
	public SVector(Enumeration<T> E)
	{
		V=new Vector<T>();
		if(E!=null)
			for(;E.hasMoreElements();)
				V.add(E.nextElement());
	}
	
	public SVector(Iterator<T> E)
	{
		V=new Vector<T>();
		if(E!=null)
			for(;E.hasNext();)
				V.add(E.next());
	}
	
	public SVector(Set<T> E)
	{
		if(E!=null)
			for(T o : E)
				add(o);
	}
	
	public void addAll(Enumeration<T> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				V.add(E.nextElement());
	}
	
	public void addAll(Iterator<T> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				V.add(E.next());
	}
	
	public void removeAll(Enumeration<T> E)
	{
		if(E!=null)
			for(;E.hasMoreElements();)
				V.remove(E.nextElement());
	}
	
	public void removeAll(Iterator<T> E)
	{
		if(E!=null)
			for(;E.hasNext();)
				V.remove(E.next());
	}
	
	public void removeAll(List<T> E)
	{
		if(E!=null)
			for(T o : E)
				V.remove(o);
	}
	
	public synchronized int capacity() {
		return V.capacity();
	}

	@SuppressWarnings("unchecked")
	public synchronized Vector<T> toVector() {
		return (Vector<T>)V.clone();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized SVector<T> copyOf() {
		SVector<T> SV=new SVector<T>();
		SV.V=(Vector<T>)V.clone();
		return SV;
	}

	@Override
	public synchronized boolean contains(Object o) {
		return V.contains(o);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> c) {
		return V.containsAll(c);
	}

	@SuppressWarnings("unchecked")
	public synchronized void copyInto(Object[] anArray) {
		V=(Vector<T>)V.clone();
		V.copyInto(anArray);
	}

	public synchronized T elementAt(int index) {
		return V.elementAt(index);
	}

	public synchronized Enumeration<T> elements() {
		return V.elements();
	}

	public synchronized void ensureCapacity(int minCapacity) {
		V.ensureCapacity(minCapacity);
	}

	@Override
	public synchronized boolean equals(Object o) {
		return o==this;
	}

	public synchronized T firstElement() {
		return V.firstElement();
	}

	@Override
	public synchronized T get(int index) {
		return V.get(index);
	}

	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}

	public synchronized int indexOf(Object o, int index) {
		return V.indexOf(o, index);
	}

	@Override
	public synchronized int indexOf(Object o) {
		return V.indexOf(o);
	}

	@Override
	public synchronized boolean isEmpty() {
		return V.isEmpty();
	}

	public synchronized T lastElement() {
		return V.lastElement();
	}

	public synchronized int lastIndexOf(Object o, int index) {
		return V.lastIndexOf(o, index);
	}

	@Override
	public synchronized int lastIndexOf(Object o) {
		return V.lastIndexOf(o);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		V=(Vector<T>)V.clone();
		return V.retainAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized T set(int index, T element) {
		V=(Vector<T>)V.clone();
		return V.set(index, element);
	}

	@SuppressWarnings("unchecked")
	public synchronized void setElementAt(T obj, int index) {
		V=(Vector<T>)V.clone();
		V.setElementAt(obj, index);
	}

	@SuppressWarnings("unchecked")
	public synchronized void setSize(int newSize) {
		V=(Vector<T>)V.clone();
		V.setSize(newSize);
	}

	@Override
	public synchronized int size() {
		return V.size();
	}

	@Override
	public synchronized List<T> subList(int fromIndex, int toIndex) {
		return V.subList(fromIndex, toIndex);
	}

	@Override
	public synchronized Object[] toArray() {
		return V.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public synchronized <T> T[] toArray(T[] a) {
		return V.toArray(a);
	}

	@Override
	public synchronized String toString() {
		return super.toString();
	}

	@SuppressWarnings("unchecked")
	public synchronized void trimToSize() {
		V=(Vector<T>)V.clone();
		V.trimToSize();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void add(int index, T element) 
	{
		V=(Vector<T>)V.clone();
		V.add(index, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean add(T e) 
	{
		V=(Vector<T>)V.clone();
		return V.add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean addAll(Collection<? extends T> c) 
	{
		V=(Vector<T>)V.clone();
		return V.addAll(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean addAll(int index, Collection<? extends T> c) 
	{
		V=(Vector<T>)V.clone();
		return V.addAll(index, c);
	}

	@SuppressWarnings("unchecked")
	public synchronized void addElement(T obj) 
	{
		V=(Vector<T>)V.clone();
		V.addElement(obj);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void clear() 
	{
		V=(Vector<T>)V.clone();
		V.clear();
	}

	@SuppressWarnings("unchecked")
	public synchronized void insertElementAt(T obj, int index) 
	{
		V=(Vector<T>)V.clone();
		V.insertElementAt(obj, index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean remove(Object o) 
	{
		V=(Vector<T>)V.clone();
		return V.remove(o);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized T remove(int index) 
	{
		V=(Vector<T>)V.clone();
		return V.remove(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean removeAll(Collection<?> c) 
	{
		V=(Vector<T>)V.clone();
		return V.removeAll(c);
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeAllElements() 
	{
		V=(Vector<T>)V.clone();
		V.removeAllElements();
	}

	@SuppressWarnings("unchecked")
	public synchronized boolean removeElement(Object obj) 
	{
		V=(Vector<T>)V.clone();
		return V.removeElement(obj);
	}

	@SuppressWarnings("unchecked")
	public synchronized void removeElementAt(int index) 
	{
		V=(Vector<T>)V.clone();
		V.removeElementAt(index);
	}

	@Override
	public synchronized Iterator<T> iterator() {
		return new ReadOnlyIterator<T>(V.iterator());
	}

	@Override
	public synchronized ListIterator<T> listIterator() {
		return new ReadOnlyListIterator<T>(V.listIterator());
	}

	@Override
	public synchronized ListIterator<T> listIterator(int index) {
		return new ReadOnlyListIterator<T>(V.listIterator());
	}
}
