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
import java.util.*;

/**
*
* @author Bo Zimmerman
*
*/
public class IndexedRowMap
{
	@SuppressWarnings("rawtypes")
	public static class IndexedRowMapComparator implements Comparator
	{
		private final int		index;
		private final boolean	descending;

		public IndexedRowMapComparator(final int index, final boolean descending)
		{
			this.index = index;
			this.descending = descending;
		}

		@Override
		public int compare(final Object arg0, final Object arg1)
		{
			final RecordInfo inf0 = (RecordInfo) arg0;
			final RecordInfo inf1 = (RecordInfo) arg1;
			if (descending)
				return inf1.indexedData[index].compareTo(inf0.indexedData[index]);
			else
				return inf0.indexedData[index].compareTo(inf1.indexedData[index]);
		}
	}

	private final Vector<RecordInfo>		unsortedRecords		= new Vector<RecordInfo>();
	private List<RecordInfo>[]				forwardSorted		= null;
	private List<RecordInfo>[]				reverseSorted		= null;
	private IndexedRowMapComparator[]		forwardComparators	= null;
	private IndexedRowMapComparator[]		reverseComparators	= null;
	private static final List<RecordInfo>	empty				= new ArrayList<RecordInfo>(1);

	public synchronized void add(final RecordInfo record)
	{
		unsortedRecords.add(record);
		clearSortCaches(record.indexedData.length);
	}

	public synchronized void remove(final RecordInfo record)
	{
		unsortedRecords.remove(record);
		clearSortCaches(record.indexedData.length);
	}

	@SuppressWarnings("unchecked")
	private void clearSortCaches(final int size)
	{
		forwardSorted = new List[size];
		reverseSorted = new List[size];
		if (forwardComparators == null)
		{
			forwardComparators = new IndexedRowMapComparator[size];
			for (int i = 0; i < size; i++)
				forwardComparators[i] = new IndexedRowMapComparator(i, false);
		}
		if (reverseComparators == null)
		{
			reverseComparators = new IndexedRowMapComparator[size];
			for (int i = 0; i < size; i++)
				reverseComparators[i] = new IndexedRowMapComparator(i, true);
		}
	}

	public synchronized Iterator<RecordInfo> recordIterator(final boolean descending)
	{
		if(!descending)
			return unsortedRecords.iterator();
		return unsortedRecords.stream().sorted(Collections.reverseOrder()).iterator();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized Iterator<RecordInfo> iterator(final int sortIndex, final boolean descending)
	{
		Iterator iter = null;
		if (sortIndex < 0)
			iter = Arrays.asList(unsortedRecords.toArray()).iterator();
		else
		{
			final List<RecordInfo>[] whichList = descending ? reverseSorted : forwardSorted;
			if ((whichList == null) || (sortIndex < 0) || (sortIndex >= whichList.length))
				iter = empty.iterator();
			else
			{
				synchronized (whichList)
				{
					if (whichList[sortIndex] != null)
						iter = whichList[sortIndex].iterator();
					else
					{
						final IndexedRowMapComparator comparator = descending ? reverseComparators[sortIndex] : forwardComparators[sortIndex];
						final List<RecordInfo> newList = (List<RecordInfo>) unsortedRecords.clone();
						Collections.sort(newList, comparator);
						whichList[sortIndex] = newList;
						iter = newList.iterator();
					}
				}
			}
		}
		return iter;
	}
}
