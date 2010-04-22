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

public class STreeSet<K> implements Serializable, Cloneable, Iterable<K>, Collection<K>, NavigableSet<K>, Set<K>, SortedSet<K>
{
	private static final long serialVersionUID = -6713012858869312626L;
	private TreeSet<K> T;
	public STreeSet()
	{
		T=new TreeSet<K>();
	}
	public STreeSet(Comparator<K> comp)
	{
		T=new TreeSet<K>(comp);
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean add(K e) {
		T=(TreeSet<K>)T.clone();
		return T.add(e);
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean addAll(Collection<? extends K> c) {
		T=(TreeSet<K>)T.clone();
		return T.addAll(c);
	}
	@Override
	public K ceiling(K e) {
		return T.ceiling(e);
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void clear() {
		T=(TreeSet<K>)T.clone();
		T.clear();
	}
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		STreeSet<K> TS=new STreeSet<K>();
		TS.T=(TreeSet<K>)T.clone();
		return TS;
	}
	@Override
	public Comparator<? super K> comparator() {
		return T.comparator();
	}
	@Override
	public boolean contains(Object o) {
		return T.contains(o);
	}
	@Override
	public Iterator<K> descendingIterator() {
		return new ReadOnlyIterator<K>(T.descendingIterator());
	}
	@Override
	public NavigableSet<K> descendingSet() {
		return T.descendingSet();
	}
	@Override
	public K first() {
		return T.first();
	}
	@Override
	public K floor(K e) {
		return T.floor(e);
	}
	@Override
	public NavigableSet<K> headSet(K toElement, boolean inclusive) {
		return T.headSet(toElement, inclusive);
	}
	@Override
	public SortedSet<K> headSet(K toElement) {
		return T.headSet(toElement);
	}
	@Override
	public K higher(K e) {
		return T.higher(e);
	}
	@Override
	public boolean isEmpty() {
		return T.isEmpty();
	}
	@Override
	public Iterator<K> iterator() {
		return new ReadOnlyIterator<K>(T.iterator());
	}
	@Override
	public K last() {
		return T.last();
	}
	@Override
	public K lower(K e) {
		return T.lower(e);
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized K pollFirst() {
		T=(TreeSet<K>)T.clone();
		return T.pollFirst();
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized K pollLast() {
		T=(TreeSet<K>)T.clone();
		return T.pollLast();
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean remove(Object o) {
		T=(TreeSet<K>)T.clone();
		return T.remove(o);
	}
	@Override
	public int size() {
		return T.size();
	}
	@Override
	public NavigableSet<K> subSet(K fromElement, boolean fromInclusive,
			K toElement, boolean toInclusive) {
		return T.subSet(fromElement, fromInclusive, toElement, toInclusive);
	}
	@Override
	public SortedSet<K> subSet(K fromElement, K toElement) {
		return T.subSet(fromElement, toElement);
	}
	@Override
	public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
		return T.tailSet(fromElement, inclusive);
	}
	@Override
	public SortedSet<K> tailSet(K fromElement) {
		return T.tailSet(fromElement);
	}
	@Override
	public boolean equals(Object arg0) {
		return this==arg0;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean removeAll(Collection<?> arg0) {
		T=(TreeSet<K>)T.clone();
		return T.removeAll(arg0);
	}
	@Override
	public boolean containsAll(Collection<?> arg0) {
		return T.containsAll(arg0);
	}
	@SuppressWarnings("unchecked")
	@Override
	public synchronized boolean retainAll(Collection<?> arg0) {
		T=(TreeSet<K>)T.clone();
		return T.retainAll(arg0);
	}
	@Override
	public Object[] toArray() {
		return T.toArray();
	}
	@Override
	public <T> T[] toArray(T[] arg0) {
		return T.toArray(arg0);
	}
	@Override
	public String toString() {
		return super.toString();
	}

}
