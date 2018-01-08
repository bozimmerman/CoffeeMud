package com.planet_ink.fakedb;

/*
   Copyright 2001 Thomas Neumann
   Copyright 2004-2018 Bo Zimmerman

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

import java.io.*;
import java.util.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Backend
{
	public static enum StatementType
	{
		SELECT, INSERT, UPDATE, DELETE
	}

	File							basePath;
	private Map<String, FakeTable>	fakeTables	= new HashMap<String, FakeTable>();

	/**
	*
	*/
	protected static class FakeColumn
	{
		String					name;
		int						type;
		boolean					canNull;
		int						keyNumber		= -1;
		int						indexNumber		= -1;
		String					tableName;
		public static final int	TYPE_UNKNOWN	= 0;
		public static final int	TYPE_INTEGER	= 1;
		public static final int	TYPE_STRING		= 2;
		public static final int	TYPE_LONG		= 3;

		public static final int	INDEX_COUNT		= Integer.MAX_VALUE;
	}

	/**
	*
	*/
	protected static class RecordInfo
	{
		int					offset;
		int					size;
		ComparableValue[]	indexedData	= null;

		RecordInfo(int o, int s)
		{
			offset = o;
			size = s;
		}
	}

	/**
	*
	*/
	public void clearFakeTables()
	{
		basePath = null;
		if (fakeTables != null)
			for (final FakeTable R : fakeTables.values())
				R.close();
		fakeTables = new HashMap<String, FakeTable>();
	}

	/**
	 * 
	 * @author Bo Zimmerman
	 * 
	 */
	public static enum ConnectorType
	{
		AND, OR
	}

	/**
	 * 
	 * @author Bo Zimmerman
	 * 
	 */
	public class FakeCondition
	{
		public int					conditionIndex;
		public ComparableValue		conditionValue;
		public String				lowStr		= null;
		public boolean				like		= false;
		public boolean				eq			= false;
		public boolean				lt			= false;
		public boolean				gt			= false;
		public boolean				not			= false;
		public boolean				unPrepared	= false;
		public int					colType		= 0;
		public ConnectorType		connector	= ConnectorType.AND;
		public List<FakeCondition>	contains	= null;

		public boolean compareValue(ComparableValue subKey)
		{
			if (subKey == null)
				subKey = new ComparableValue(null);
			if (like && conditionValue.getValue() instanceof String)
			{
				if (lowStr == null)
					lowStr = ((String) conditionValue.getValue()).toLowerCase();
				boolean chk = false;
				if (lowStr.length() == 0)
					chk = conditionValue.equals(subKey);
				else 
				if (subKey.equals(null) || (!(subKey.getValue() instanceof String)))
					chk = false;
				else
				{
					final String s = ((String) subKey.getValue()).toLowerCase();
					final int x = lowStr.indexOf('%');
					if ((x < 0) || (lowStr.length() == 1))
						chk = lowStr.equals(s);
					else 
					if (x == 0)
					{
						if (lowStr.charAt(lowStr.length() - 1) == '%')
							chk = (s.indexOf(lowStr.substring(1, lowStr.length() - 1)) >= 0);
						else
							chk = s.startsWith(lowStr.substring(1));
					}
					else 
					if (lowStr.charAt(lowStr.length() - 1) == '%')
						chk = s.endsWith(lowStr.substring(0, lowStr.length() - 1));
					else
						chk = s.startsWith(lowStr.substring(0, x)) && s.endsWith(lowStr.substring(x + 1));
				}
				return not ? !chk : chk;
			}
			final int sc = (lt || gt) ? subKey.compareTo(conditionValue) : 0;
			if (!(((eq) && (subKey.equals(conditionValue))) || ((lt) && (sc < 0)) || ((gt) && (sc > 0))))
				return not;
			return !not;
		}
	}

	/**
	 * 
	 * @author Bo Zimmerman
	 * 
	 */
	public interface FakeConditionResponder
	{
		public void callBack(ComparableValue[] values, RecordInfo info) throws Exception;
	}

	/**
	 * 
	 * @author Bo Zimmerman
	 * 
	 */
	public static class ComparableValue implements Comparable
	{
		private Comparable	v;

		public ComparableValue(Comparable v)
		{
			if (v instanceof ComparableValue)
				this.v = ((ComparableValue) v).v;
			else
				this.v = v;
		}

		@Override
		public int hashCode()
		{
			if (v != null)
				return v.hashCode();
			return 0;
		}

		public Comparable getValue()
		{
			return v;
		}

		@Override
		public boolean equals(Object o)
		{
			Object t = o;
			if (o instanceof ComparableValue)
				t = ((ComparableValue) o).getValue();
			if ((v == null) && (t == null))
				return true;
			if ((v == null) || (t == null))
				return false;
			return v.equals(t);
		}

		@Override
		public int compareTo(Object o)
		{
			Object to = o;
			if (o instanceof ComparableValue)
				to = ((ComparableValue) o).v;
			if ((v == null) && (to == null))
				return 0;
			if (v == null)
				return -1;
			if (to == null)
				return 1;
			return v.compareTo(to);
		}
	}

	/**
	 * 
	 * @param fakeTable
	 * @param columns
	 * @param sqlValues
	 * @return
	 * @throws java.sql.SQLException
	 */
	public void dupKeyCheck(final String tableName, final String[] doCols, final String[] sqlValues) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + tableName);
		final List<Backend.FakeCondition> conditions = new ArrayList<Backend.FakeCondition>(2);
		for (int i = 0; i < doCols.length; i++)
		{
			final int id = fakeTable.findColumn(doCols[i]);
			if (id < 0)
				continue;
			final FakeColumn col = fakeTable.columns[id];
			if (col.keyNumber >= 0)
			{
				final Backend.FakeCondition condition = buildFakeCondition(fakeTable.name, col.name, "=", sqlValues[i], false);
				condition.connector = Backend.ConnectorType.AND;
				conditions.add(condition);
			}
		}
		if (conditions.size() == 0)
			return;
		final FakeConditionResponder responder = new FakeConditionResponder() 
		{
			@Override
			public void callBack(ComparableValue[] values, RecordInfo info) throws Exception
			{
				throw new java.sql.SQLException("duplicate key error");
			}
		};
		try
		{
			fakeTable.recordIterator(conditions, responder);
		}
		catch (final Exception e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	protected static class IndexedRowMapComparator implements Comparator
	{
		private final int		index;
		private final boolean	descending;

		public IndexedRowMapComparator(int index, boolean descending)
		{
			this.index = index;
			this.descending = descending;
		}

		@Override
		public int compare(Object arg0, Object arg1)
		{
			final RecordInfo inf0 = (RecordInfo) arg0;
			final RecordInfo inf1 = (RecordInfo) arg1;
			if (descending)
				return inf1.indexedData[index].compareTo(inf0.indexedData[index]);
			else
				return inf0.indexedData[index].compareTo(inf1.indexedData[index]);
		}
	}

	/**
	 * 
	 * @author Bo Zimmerman
	 * 
	 */
	protected static class IndexedRowMap
	{
		private final Vector<RecordInfo>		unsortedRecords		= new Vector<RecordInfo>();
		private List<RecordInfo>[]				forwardSorted		= null;
		private List<RecordInfo>[]				reverseSorted		= null;
		private IndexedRowMapComparator[]		forwardComparators	= null;
		private IndexedRowMapComparator[]		reverseComparators	= null;
		private static final List<RecordInfo>	empty				= new ArrayList<RecordInfo>(1);

		public synchronized void add(RecordInfo record)
		{
			unsortedRecords.add(record);
			clearSortCaches(record.indexedData.length);
		}

		public synchronized void remove(RecordInfo record)
		{
			unsortedRecords.remove(record);
			clearSortCaches(record.indexedData.length);
		}

		private void clearSortCaches(int size)
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

		public synchronized Iterator<RecordInfo> iterator(int sortIndex, boolean descending)
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

	/**
	*
	*/
	protected static class FakeTable
	{
		private File					fileName;
		private final String			name;
		private RandomAccessFile		file;
		private int						fileSize;
		private byte[]					fileBuffer;
		private FakeColumn[]			columns;
		private Map<String, Integer>	columnHash	= new Hashtable<String, Integer>();
		private int[]					columnIndexesOfIndexed;
		private IndexedRowMap			rowRecords	= new IndexedRowMap();

		FakeTable(String tableName, File name)
		{
			this.name = tableName;
			fileName = name;
		}

		protected int numColumns()
		{
			return columns.length;
		}

		/**
		 * 
		 * @param name
		 * @return
		 */
		protected int findColumn(String name)
		{
			if ((name != null) && (columnHash.containsKey(name)))
				return columnHash.get(name).intValue();
			return -1;
		}

		/**
		 * 
		 * @param orderByIndexDex
		 * @param orderByConditions
		 * @return
		 */
		public Iterator<RecordInfo> indexIterator(int[] orderByIndexDex, String[] orderByConditions)
		{
			if ((orderByIndexDex == null) || (orderByIndexDex.length == 0))
				return rowRecords.iterator(-1, false);
			final boolean descending = (orderByConditions != null) && "DESC".equals(orderByConditions[0]);
			final FakeColumn col = columns[orderByIndexDex[0]];
			return rowRecords.iterator(col.indexNumber, descending);
		}

		/**
		 * 
		 * @param index
		 * @return
		 */
		protected String getColumnName(int index)
		{
			if ((index < 0) || (index > columns.length))
				return null;
			return columns[index].name;
		}

		/**
		 * 
		 * @param index
		 * @return
		 */
		public FakeColumn getColumnInfo(int index)
		{
			if ((index < 0) || (index > columns.length))
				return null;
			return columns[index];
		}

		protected void close()
		{
			fileName = null;
			if (file != null)
			{
				try
				{
					file.close();
				}
				catch (final Exception e)
				{
				}
				file = null;
			}
			columns = null;
			columnHash = null;
			columnIndexesOfIndexed = null;
			rowRecords = new IndexedRowMap();
		}

		/**
		 * 
		 * @throws IOException
		 */
		protected void open() throws IOException
		{
			file = new RandomAccessFile(fileName, "rw");
			fileSize = 0;
			fileBuffer = new byte[4096];

			int remaining = 0;
			int ofs = 0;
			int found = 0, skipped = 0;
			while (true)
			{
				if (remaining == 0)
				{
					ofs = 0;
					remaining = file.read(fileBuffer);
					if (remaining < 0)
						break;
				}
				boolean skip;
				if (fileBuffer[ofs] == '-') // deleted
				{
					skip = false;
				}
				else 
				if (fileBuffer[ofs] == '*') // active
				{
					skip = true;
				}
				else
					break;
				// check if valid...
				boolean valid = true;
				int size = 0;
				while (true)
				{
					int toCheck = columns.length + 1;
					for (int index = ofs, left = remaining; left > 0; left--, index++)
					{
						if (fileBuffer[index] == 0x0A)
						{
							if (--toCheck == 0)
							{
								size = index - ofs + 1;
								break;
							}
						}
					}
					if (toCheck == 0)
						break;
					if (ofs > 0)
					{
						System.arraycopy(fileBuffer, ofs, fileBuffer, 0, remaining);
						ofs = 0;
					}
					if (ofs + remaining == fileBuffer.length)
					{
						final byte[] newFileBuffer = new byte[fileBuffer.length * 2];
						System.arraycopy(fileBuffer, 0, newFileBuffer, 0, remaining);
						fileBuffer = newFileBuffer;
					}
					final int additional = file.read(fileBuffer, remaining, fileBuffer.length - remaining);
					if (additional < 0)
					{
						valid = false;
						break;
					}
					remaining += additional;
				}
				if (!valid)
					break;
				// Build index string
				if (!skip)
				{
					int current = -1;
					FakeColumn col = null;
					final int[] sub = new int[] { ofs };
					final ComparableValue[] indexData = new ComparableValue[columnIndexesOfIndexed.length];
					for (int index = 0; index < columnIndexesOfIndexed.length; index++)
					{
						while (current < columnIndexesOfIndexed[index])
						{
							while (fileBuffer[sub[0]] != 0x0A)
								sub[0]++;
							sub[0]++;
							current++;
						}
						col = columns[columnIndexesOfIndexed[index]];
						indexData[index] = getNextLine(col.type, fileBuffer, sub);
					}
					final RecordInfo info = new RecordInfo(fileSize, size);
					info.indexedData = indexData;
					rowRecords.add(info);
				}
				else
					skipped += size;
				found += size;
				// Fix pointers
				ofs += size;
				remaining -= size;
				fileSize += size;
			}
			// Too much space wasted?
			if (skipped > (found / 10))
				vacuum();
		}

		/**
		 * 
		 * @throws IOException
		 */
		private void vacuum() throws IOException
		{
			final File tempFileName = new File(fileName.getName() + ".tmp");
			final File tempFileName2 = new File(fileName.getName() + ".cpy");
			final RandomAccessFile tempOut = new RandomAccessFile(tempFileName, "rw");
			int newFileSize = 0;
			for (final Iterator<RecordInfo> iter = rowRecords.iterator(-1, false); iter.hasNext();)
			{
				final RecordInfo info = iter.next();
				file.seek(info.offset);
				file.readFully(fileBuffer, 0, info.size);
				tempOut.write(fileBuffer, 0, info.size);
				info.offset = newFileSize;
				newFileSize += info.size;
			}
			tempOut.getFD().sync();
			tempOut.close();
			file.close();
			tempFileName2.delete();
			fileName.renameTo(tempFileName2);
			tempFileName.renameTo(fileName);
			tempFileName2.delete();
			file = new RandomAccessFile(fileName, "rw");
			fileSize = newFileSize;
		}

		/**
		 * 
		 * @param values
		 * @param info
		 * @return
		 */
		protected synchronized boolean getRecord(ComparableValue[] values, RecordInfo info)
		{
			try
			{
				file.seek(info.offset);
				file.readFully(fileBuffer, 0, info.size);
				final int[] ofs = new int[] { 0 };
				FakeColumn col = null;
				for (int index = 0; index < columns.length; index++)
				{
					col = columns[index];
					while (fileBuffer[ofs[0]] != 0x0A)
						ofs[0]++;
					ofs[0]++;
					values[index] = getNextLine(col.type, fileBuffer, ofs);
				}
				return true;
			}
			catch (final IOException e)
			{
				return false;
			}
		}

		/**
		 * 
		 * @param colType
		 * @param fileBuffer
		 * @param dex
		 * @return
		 */
		public ComparableValue getNextLine(int colType, byte[] fileBuffer, int[] dex)
		{
			if ((fileBuffer[dex[0]] == '\\') && (fileBuffer[dex[0] + 1] == '?'))
				return new ComparableValue(null);
			else
			{
				final StringBuilder buffer = new StringBuilder("");
				for (;; dex[0]++)
				{
					char c = (char) (fileBuffer[dex[0]] & 0xFF);
					if (c == 0x0A)
						break;
					if (c == '\\')
					{
						if (fileBuffer[dex[0] + 1] == '\\')
						{
							buffer.append('\\');
							dex[0]++;
						}
						else 
						if (fileBuffer[dex[0] + 1] == 'n')
						{
							buffer.append((char) 0x0A);
							dex[0]++;
						}
						else
						{
							int val = 0;
							for (int i = 0; i < 4; i++)
							{
								c = (char) (fileBuffer[++dex[0]] & 0xFF);
								if (c >= 'A')
									val = (16 * val) + (c - 'A');
								else
									val = (16 * val) + (c - '0');
							}
						}
					}
					else
						buffer.append(c);
				}
				if (buffer.toString().equals("null"))
					return new ComparableValue(null);
				else
				{
					switch (colType)
					{
					case FakeColumn.TYPE_INTEGER:
						return new ComparableValue(Integer.valueOf(buffer.toString()));
					case FakeColumn.TYPE_LONG:
						return new ComparableValue(Long.valueOf(buffer.toString()));
					default:
						return new ComparableValue(buffer.toString());
					}
				}
			}
		}

		/**
		 * 
		 * @param required
		 */
		private void increaseBuffer(int required)
		{
			final int newSize = ((required + 4095) >>> 12) << 12;
			final byte[] newBuffer = new byte[newSize];
			System.arraycopy(fileBuffer, 0, newBuffer, 0, fileBuffer.length);
			fileBuffer = newBuffer;
		}

		/**
		 * 
		 * @param prevRecord
		 * @param indexData
		 * @param values
		 * @return
		 */
		protected synchronized boolean insertRecord(RecordInfo prevRecord, ComparableValue[] indexData, ComparableValue[] values)
		{
			try
			{
				int ofs = 2;
				fileBuffer[0] = (byte) '-';
				fileBuffer[1] = (byte) 0x0A;
				for (final ComparableValue value : values)
				{
					if ((value == null) || (value.getValue() == null))
					{
						if (ofs + 3 > fileBuffer.length)
							increaseBuffer(ofs + 3);
						fileBuffer[ofs + 0] = (byte) '\\';
						fileBuffer[ofs + 1] = (byte) '?';
						fileBuffer[ofs + 2] = (byte) 0x0A;
						ofs += 3;
					}
					else
					{
						int size = 0;
						final String s = value.getValue().toString();
						for (int sub = 0; sub < s.length(); sub++)
						{
							final char c = s.charAt(sub);
							if (c == '\\')
								size += 2;
							else 
							if (c == '\n')
								size += 2;
							else 
							if (c > 255)
								size += 5;
							else
								size++;
						}
						if (ofs + size + 1 > fileBuffer.length)
							increaseBuffer(ofs + size + 1);
						for (int sub = 0; sub < s.length(); sub++)
						{
							char c = s.charAt(sub);
							if (c == '\\')
							{
								fileBuffer[ofs] = (byte) '\\';
								fileBuffer[ofs + 1] = (byte) '\\';
								ofs += 2;
							}
							else 
							if (c == '\n')
							{
								fileBuffer[ofs] = (byte) '\\';
								fileBuffer[ofs + 1] = (byte) 'n';
								ofs += 2;
							}
							else 
							if (c > 255)
							{
								fileBuffer[ofs++] = (byte) '\\';
								for (int i = 0; i < 4; i++)
								{
									fileBuffer[ofs++] = (byte) ("0123456789ABCDEF".charAt(c >>> 12));
									c <<= 4;
								}
							}
							else
								fileBuffer[ofs++] = (byte) c;
						}
						fileBuffer[ofs++] = (byte) 0x0A;
					}
				}
				int recordPos = fileSize;
				if ((prevRecord != null) && (prevRecord.size == ofs))
					recordPos = prevRecord.offset;
				else
					fileSize += ofs;
				file.seek(recordPos);
				file.write(fileBuffer, 0, ofs);
				file.getFD().sync();
				final RecordInfo info = new RecordInfo(recordPos, ofs);
				info.indexedData = indexData;
				rowRecords.add(info);
				return true;
			}
			catch (final IOException e)
			{
				return false;
			}
		}

		/**
		 * 
		 * @param conditions
		 * @return
		 */
		protected synchronized int deleteRecord(List<FakeCondition> conditions)
		{
			final int[] count = { 0 };
			try
			{
				final FakeConditionResponder responder = new FakeConditionResponder() 
				{
					public int[]	count;

					public FakeConditionResponder init(int[] c)
					{
						count = c;
						return this;
					}

					@Override
					public void callBack(ComparableValue[] values, RecordInfo info) throws Exception
					{
						file.seek(info.offset);
						file.write(new byte[] { (byte) '*' });
						rowRecords.remove(info);
						count[0]++;
					}
				}.init(count);
				recordIterator(conditions, responder);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				return -1;
			}
			return count[0];
		}

		/**
		 * 
		 * @param info
		 * @param conditions
		 * @param dataLoaded
		 * @param values
		 * @return
		 */
		public boolean recordCompare(RecordInfo info, List<FakeCondition> conditions, boolean[] dataLoaded, ComparableValue[] values)
		{
			boolean lastOne = true;
			ConnectorType connector = ConnectorType.AND;
			final ComparableValue[] rowIndexesData = info.indexedData;
			for (final FakeCondition cond : conditions)
			{
				boolean thisOne = false;
				if (cond.contains != null)
					thisOne = recordCompare(info, cond.contains, dataLoaded, values);
				else
				{
					final FakeColumn column = columns[cond.conditionIndex];
					if (column.indexNumber >= 0)
						thisOne = cond.compareValue(rowIndexesData[column.indexNumber]);
					else
					{
						if (!dataLoaded[0])
							dataLoaded[0] = getRecord(values, info);
						if (dataLoaded[0])
						{
							if (values[cond.conditionIndex] == null)
								thisOne = false;
							else 
							if (cond.not)
								thisOne = !cond.compareValue(values[cond.conditionIndex]);
							else
								thisOne = cond.compareValue(values[cond.conditionIndex]);
						}
					}
				}
				if (connector == ConnectorType.OR)
					lastOne = lastOne || thisOne;
				else
					lastOne = lastOne && thisOne;
				connector = cond.connector;
			}
			return lastOne;
		}

		/**
		 * 
		 * @param conditions
		 * @param callBack
		 */
		public void recordIterator(List<FakeCondition> conditions, FakeConditionResponder callBack) throws Exception
		{
			final boolean[] dataLoaded = new boolean[1];
			final ComparableValue[] values = new ComparableValue[columns.length];
			for (final Iterator<RecordInfo> iter = rowRecords.iterator(-1, false); iter.hasNext();)
			{
				final RecordInfo info = iter.next();
				dataLoaded[0] = false;
				if (recordCompare(info, conditions, dataLoaded, values))
				{
					if (!dataLoaded[0])
						dataLoaded[0] = getRecord(values, info);
					if (dataLoaded[0])
						callBack.callBack(values, info);
				}
			}
		}

		/**
		 * 
		 * @param conditions
		 * @param columns
		 * @param values
		 * @return
		 */
		protected synchronized int updateRecord(List<FakeCondition> conditions, int[] columns, ComparableValue[] values)
		{
			final int[] count = { 0 };
			try
			{
				final FakeConditionResponder responder = new FakeConditionResponder() {
					public int[]				count;
					public int[]				newCols;
					public ComparableValue[]	updatedValues	= null;

					public FakeConditionResponder init(int[] c, int[] a, ComparableValue[] n)
					{
						count = c;
						newCols = a;
						updatedValues = n;
						return this;
					}

					@Override
					public void callBack(ComparableValue[] values, RecordInfo info) throws Exception
					{
						final ComparableValue[] rowIndexData = info.indexedData;
						boolean somethingChanged = false;
						for (int sub = 0; sub < newCols.length; sub++)
						{
							if (!values[newCols[sub]].equals(updatedValues[sub]))
							{
								values[newCols[sub]] = updatedValues[sub];
								somethingChanged = true;
							}
							for (int k = 0; k < rowIndexData.length; k++)
								if (columnIndexesOfIndexed[k] == newCols[sub])
									rowIndexData[k] = updatedValues[sub];
						}
						if (somethingChanged)
						{
							file.seek(info.offset);
							file.write(new byte[] { (byte) '*' });
							rowRecords.remove(info);
							insertRecord(info, rowIndexData, values);
						}
						count[0]++;
					}
				}.init(count, columns, values);
				recordIterator(conditions, responder);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				return -1;
			}
			return count[0];
		}
	}

	/**
	 * 
	 * @param basePath
	 * @param schema
	 * @throws IOException
	 */
	private void readSchema(File basePath, File schema) throws IOException
	{
		final BufferedReader in = new BufferedReader(new FileReader(schema));

		try
		{
			while (true)
			{
				final String fakeTableName = in.readLine();
				if (fakeTableName == null)
					break;
				if (fakeTableName.length() == 0)
					throw new IOException("Can not read schema: tableName is null");
				if (fakeTableName.startsWith("#")) // comment
					continue;
				if (fakeTables.get(fakeTableName) != null)
					throw new IOException("Can not read schema: tableName is missing: " + fakeTableName);

				final List<FakeColumn> columns = new Vector<FakeColumn>();
				final List<String> keys = new Vector<String>();
				final List<String> indexes = new Vector<String>();
				while (true)
				{
					String line = in.readLine();
					if (line == null)
						break;
					if (line.length() == 0)
						break;
					int split = line.indexOf(' ');
					if (split < 0)
						throw new IOException("Can not read schema: expected space in line '" + line + "'");
					final String columnName = line.substring(0, split);
					line = line.substring(split + 1);
					split = line.indexOf(' ');
					String columnType;
					String[] columnModifiers = null;
					if (split < 0)
					{
						columnType = line;
						columnModifiers = new String[0];
					}
					else
					{
						columnType = line.substring(0, split);
						final String lineRes = line.substring(split + 1).trim();
						final int split2 = lineRes.indexOf(' ');
						if (split2 > 0)
							columnModifiers = new String[] { lineRes.substring(0, split2).trim(), lineRes.substring(split2 + 1).trim() };
						else
							columnModifiers = new String[] { lineRes };
					}

					final FakeColumn info = new FakeColumn();
					info.tableName = fakeTableName;
					info.name = columnName;
					if (columnType.equals("string"))
						info.type = FakeColumn.TYPE_STRING;
					else 
					if (columnType.equals("integer"))
						info.type = FakeColumn.TYPE_INTEGER;
					else 
					if (columnType.equals("long"))
						info.type = FakeColumn.TYPE_LONG;
					else 
					if (columnType.equals("datetime"))
						info.type = FakeColumn.TYPE_LONG;
					else
						throw new IOException("Can not read schema: attributeType '" + columnType + "' is unknown");
					for (final String modifier : columnModifiers)
					{
						if (modifier.equals(""))
							continue;
						else 
						if (modifier.equals("NULL"))
							info.canNull = true;
						else 
						if (modifier.equals("KEY"))
						{
							info.keyNumber = keys.size();
							keys.add(columnName);
							info.indexNumber = indexes.size();
							indexes.add(columnName);
						}
						else 
						if (modifier.equals("INDEX"))
						{
							info.indexNumber = indexes.size();
							indexes.add(columnName);
						}
						else
							throw new IOException("Can not read schema: attributeSpecial '" + modifier + "' is unknown");
					}
					columns.add(info);
				}

				final FakeTable fakeTable = new FakeTable(fakeTableName, new File(basePath, "fakedb.data." + fakeTableName));
				fakeTable.columns = new FakeColumn[columns.size()];
				fakeTable.columnHash = new Hashtable<String, Integer>();
				int index = 0;
				for (final Iterator iter = columns.iterator(); iter.hasNext(); ++index)
				{
					final FakeColumn current = (FakeColumn) iter.next();
					fakeTable.columns[index] = current;
					fakeTable.columnHash.put(current.name, Integer.valueOf(index));
				}
				index = 0;
				fakeTable.columnIndexesOfIndexed = new int[indexes.size()];
				for (final Iterator iter = indexes.iterator(); iter.hasNext(); ++index)
					fakeTable.columnIndexesOfIndexed[index] = fakeTable.findColumn((String) iter.next());

				fakeTable.open();
				fakeTables.put(fakeTableName, fakeTable);
			}
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * 
	 * @param basePath
	 * @return
	 */
	protected boolean open(File basePath)
	{
		try
		{
			readSchema(basePath, new File(basePath, "fakedb.schema"));
			return true;
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param s
	 * @param tableName
	 * @param cols
	 * @param conditions
	 * @param orderVars
	 * @param orderModifiers
	 * @return
	 * @throws java.sql.SQLException
	 */
	protected java.sql.ResultSet constructScan(final ImplSelectStatement stmt) throws java.sql.SQLException
	{
		final Statement s = stmt.s;
		final String tableName = stmt.tableName;
		final List<String> cols = stmt.cols;
		final List<Backend.FakeCondition> conditions = stmt.conditions;
		final String[] orderVars = stmt.orderVars;
		final String[] orderModifiers = stmt.orderModifiers;
		final FakeTable table = fakeTables.get(tableName);
		if (table == null)
			throw new java.sql.SQLException("unknown table " + tableName);
		int[] showCols;
		if ((cols.size() == 0) || (cols.contains("*")))
		{
			showCols = new int[table.numColumns()];
			for (int i = 0; i < showCols.length; i++)
				showCols[i] = i;
		}
		else
		{
			int index = 0;
			showCols = new int[cols.size()];
			for (final String col : cols)
			{
				if (col.toLowerCase().startsWith("count("))
					showCols[index] = FakeColumn.INDEX_COUNT;
				else
				{
					showCols[index] = table.findColumn(col);
					if (showCols[index] < 0)
					{
						try
						{
							Integer.parseInt(col);
							showCols[index] = FakeColumn.INDEX_COUNT;
						}
						catch (final Exception e)
						{
							throw new java.sql.SQLException("unknown column " + tableName + "." + col);
						}
					}
				}
				index++;
			}
		}

		int[] orderDexIndexes = null;
		if (orderVars != null)
		{
			orderDexIndexes = new int[orderVars.length];
			int d = 0;
			for (final String var : orderVars)
			{
				final int index = table.findColumn(var);
				int indexDex = -1;
				if (index < 0)
					throw new java.sql.SQLException("unknown column " + var);
				for (final int i : table.columnIndexesOfIndexed)
					if (i == index)
						indexDex = i;
				if (indexDex < 0)
					throw new java.sql.SQLException("unable to order by non-indexed " + var);
				orderDexIndexes[d] = indexDex;
				d++;
			}
		}
		return new ResultSet(s, table, showCols, conditions, orderDexIndexes, orderModifiers);
	}

	/**
	 * For prepared statements, an abstract way into things
	 * 
	 * @author Bo Zimmermanimmerman
	 */
	public static abstract class ImplAbstractStatement
	{
		public abstract String[] values();

		public abstract Boolean[] unPreparedValuesFlags();

		public abstract List<FakeCondition> conditions();

		public abstract StatementType getStatementType();
	}

	/**
	 * Parameters to execute an insert statement
	 * 
	 * @author Bo Zimmermanimmerman
	 */
	public static class ImplInsertStatement extends ImplAbstractStatement
	{
		public ImplInsertStatement(final String tableName, final String[] columns, final String[] sqlValues, final Boolean[] unPreparedValues)
		{
			this.tableName = tableName;
			this.columns = columns;
			this.sqlValues = sqlValues;
			this.unPreparedValues = unPreparedValues;
		}

		public final String		tableName;
		public final String[]	columns;
		public final String[]	sqlValues;
		public final Boolean[]	unPreparedValues;

		@Override
		public final String[] values()
		{
			return sqlValues;
		}

		@Override
		public final List<FakeCondition> conditions()
		{
			return null;
		}

		@Override
		public final Boolean[] unPreparedValuesFlags()
		{
			return unPreparedValues;
		}

		@Override
		public final StatementType getStatementType()
		{
			return StatementType.INSERT;
		}
	}

	/**
	 * Parameters to execute an update statement
	 * 
	 * @author Bo Zimmermanimmerman
	 */
	public static class ImplUpdateStatement extends ImplAbstractStatement
	{
		public ImplUpdateStatement(final String tableName, final List<FakeCondition> conditions, final String[] columns, final String[] sqlValues, final Boolean[] unPreparedValues)
		{
			this.tableName = tableName;
			this.columns = columns;
			this.sqlValues = sqlValues;
			this.conditions = conditions;
			this.unPreparedValues = unPreparedValues;
		}

		public final String					tableName;
		public final String[]				columns;
		public final String[]				sqlValues;
		public final Boolean[]				unPreparedValues;
		public final List<FakeCondition>	conditions;

		@Override
		public final String[] values()
		{
			return sqlValues;
		}

		@Override
		public final List<FakeCondition> conditions()
		{
			return conditions;
		}

		@Override
		public final Boolean[] unPreparedValuesFlags()
		{
			return unPreparedValues;
		}

		@Override
		public final StatementType getStatementType()
		{
			return StatementType.UPDATE;
		}
	}

	/**
	 * Parameters to execute an select statement
	 * 
	 * @author Bo Zimmermanimmerman
	 */
	public static class ImplSelectStatement extends ImplAbstractStatement
	{
		public ImplSelectStatement(final Statement s, final String tableName, final List<String> cols, final List<Backend.FakeCondition> conditions, final String[] orderVars, final String[] orderModifiers)
		{
			this.s = s;
			this.tableName = tableName;
			this.cols = cols;
			this.conditions = conditions;
			this.orderVars = orderVars;
			this.orderModifiers = orderModifiers;
		}

		final Statement						s;
		final String						tableName;
		final List<String>					cols;
		final List<Backend.FakeCondition>	conditions;
		final String[]						orderVars;
		final String[]						orderModifiers;
		private final Boolean[]				unPreparedValues	= new Boolean[0];

		@Override
		public final Boolean[] unPreparedValuesFlags()
		{
			return unPreparedValues;
		}

		@Override
		public final String[] values()
		{
			return null;
		}

		@Override
		public final List<FakeCondition> conditions()
		{
			return conditions;
		}

		@Override
		public final StatementType getStatementType()
		{
			return StatementType.SELECT;
		}
	}

	/**
	 * Parameters to execute an delete statement
	 * 
	 * @author Bo Zimmermanimmerman
	 */
	public static class ImplDeleteStatement extends ImplAbstractStatement
	{
		public ImplDeleteStatement(final String tableName, final List<FakeCondition> conditions)
		{
			this.tableName = tableName;
			this.conditions = conditions;
		}

		public final String					tableName;
		public final List<FakeCondition>	conditions;
		private final Boolean[]				unPreparedValues	= new Boolean[0];

		@Override
		public final Boolean[] unPreparedValuesFlags()
		{
			return unPreparedValues;
		}

		@Override
		public final String[] values()
		{
			return null;
		}

		@Override
		public final List<FakeCondition> conditions()
		{
			return conditions;
		}

		@Override
		public final StatementType getStatementType()
		{
			return StatementType.DELETE;
		}
	}

	/**
	 * 
	 * @param tableName
	 * @param columns
	 * @param dataValues
	 * @throws java.sql.SQLException
	 */
	protected void insertValues(final ImplInsertStatement stmt) throws java.sql.SQLException
	{
		final String tableName = stmt.tableName;
		final String[] columns = stmt.columns;
		final String[] sqlValues = stmt.sqlValues;

		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + tableName);

		final ComparableValue[] values = new ComparableValue[fakeTable.columns.length];
		for (int index = 0; index < columns.length; index++)
		{
			final int id = fakeTable.findColumn(columns[index]);
			if (id < 0)
				throw new java.sql.SQLException("unknown column " + columns[index]);
			final FakeColumn col = fakeTable.columns[id];
			try
			{
				if ((sqlValues[index] == null) || (sqlValues[index].equals("null")))
					values[id] = new ComparableValue(null);
				else
				{
					switch (col.type)
					{
					case FakeColumn.TYPE_INTEGER:
						values[id] = new ComparableValue(Integer.valueOf(sqlValues[index]));
						break;
					case FakeColumn.TYPE_LONG:
						values[id] = new ComparableValue(Long.valueOf(sqlValues[index]));
						break;
					default:
						values[id] = new ComparableValue(sqlValues[index]);
						break;
					}
				}
			}
			catch (final Exception e)
			{
				throw new java.sql.SQLException("illegal value '" + sqlValues[index] + "' for column " + col.name);
			}
		}
		final ComparableValue[] keys = new ComparableValue[fakeTable.columnIndexesOfIndexed.length];
		for (int index = 0; index < fakeTable.columnIndexesOfIndexed.length; index++)
		{
			final int id = fakeTable.columnIndexesOfIndexed[index];
			if (values[id] == null)
				keys[index] = new ComparableValue(null);
			else
				keys[index] = new ComparableValue(values[id]);
		}
		if (!fakeTable.insertRecord(null, keys, values))
			throw new java.sql.SQLException("unable to insert record");
	}

	/**
	 * 
	 * @param tableName
	 * @param conditionVar
	 * @param conditionValue
	 * @throws java.sql.SQLException
	 */
	protected void deleteRecord(final ImplDeleteStatement stmt) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(stmt.tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + stmt.tableName);

		fakeTable.deleteRecord(stmt.conditions);
	}

	/**
	 * 
	 * @param tableName
	 * @param conditionVar
	 * @param conditionValue
	 * @param varNames
	 * @param values
	 * @throws java.sql.SQLException
	 */
	protected void updateRecord(final ImplUpdateStatement stmt) throws java.sql.SQLException
	{
		final String tableName = stmt.tableName;
		final List<FakeCondition> conditions = stmt.conditions;
		final String[] varNames = stmt.columns;
		final String[] sqlValues = stmt.sqlValues;

		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + tableName);

		final int[] vars = new int[varNames.length];
		for (int index = 0; index < vars.length; index++)
			if ((vars[index] = fakeTable.findColumn(varNames[index])) < 0)
				throw new java.sql.SQLException("unknown column " + varNames[index]);

		final ComparableValue[] values = new ComparableValue[fakeTable.columns.length];
		for (int index = 0; index < sqlValues.length; index++)
		{
			final FakeColumn col = fakeTable.columns[vars[index]];
			try
			{
				if ((sqlValues[index] == null) || (sqlValues[index].equals("null")))
					values[index] = new ComparableValue(null);
				else
				{
					switch (col.type)
					{
					case FakeColumn.TYPE_INTEGER:
						values[index] = new ComparableValue(Integer.valueOf(sqlValues[index]));
						break;
					case FakeColumn.TYPE_LONG:
						values[index] = new ComparableValue(Long.valueOf(sqlValues[index]));
						break;
					default:
						values[index] = new ComparableValue(sqlValues[index]);
						break;
					}
				}
			}
			catch (final Exception e)
			{
				throw new java.sql.SQLException("illegal value '" + sqlValues[index] + "' for column " + col.name);
			}
		}
		fakeTable.updateRecord(conditions, vars, values);
	}

	/**
	 * 
	 * @param tableName
	 * @param columnName
	 * @param comparitor
	 * @param value
	 * @return
	 * @throws java.sql.SQLException
	 */
	public FakeCondition buildFakeCondition(String tableName, String columnName, String comparitor, String value, boolean unPrepared) throws java.sql.SQLException
	{
		final FakeTable fakeTable = fakeTables.get(tableName);
		if (fakeTable == null)
			throw new java.sql.SQLException("unknown table " + tableName);
		final FakeCondition fake = new FakeCondition();
		fake.unPrepared = unPrepared;
		if (columnName == null)
		{
			fake.conditionIndex = 0;
			fake.conditionValue = new ComparableValue(null);
			return fake;
		}
		if ((fake.conditionIndex = fakeTable.findColumn(columnName)) < 0)
			throw new java.sql.SQLException("unknown column " + tableName + "." + columnName);
		final FakeColumn col = fakeTable.columns[fake.conditionIndex];
		if (col == null)
			throw new java.sql.SQLException("bad column " + tableName + "." + columnName);
		fake.colType = col.type;
		if ((value == null) || value.equals("null") || unPrepared)
			fake.conditionValue = new ComparableValue(null);
		else
		{
			switch (col.type)
			{
			case FakeColumn.TYPE_INTEGER:
			{
				try
				{
					fake.conditionValue = new ComparableValue(Integer.valueOf(value));
				}
				catch (final Exception e)
				{
					throw new java.sql.SQLException("can't compare " + value + " to " + tableName + "." + columnName);
				}
				break;
			}
			case FakeColumn.TYPE_LONG:
			{
				try
				{
					fake.conditionValue = new ComparableValue(Long.valueOf(value));
				}
				catch (final Exception e)
				{
					throw new java.sql.SQLException("can't compare " + value + " to " + tableName + "." + columnName);
				}
				break;
			}
			default:
				fake.conditionValue = new ComparableValue(value);
				break;
			}
		}
		if (comparitor.equalsIgnoreCase("like"))
		{
			if ((col.type != FakeColumn.TYPE_STRING) && (col.type != FakeColumn.TYPE_UNKNOWN))
				throw new java.sql.SQLException("can't do like comparison on " + tableName + "." + columnName);
			fake.like = true;
		}
		else
		{
			for (final char c : comparitor.toCharArray())
			{
				switch (c)
				{
				case '!':
					fake.not = true;
					break;
				case '=':
					fake.eq = true;
					break;
				case '<':
					fake.lt = true;
					break;
				case '>':
					fake.gt = true;
					break;
				}
			}
		}
		if (fake.lt && fake.gt && (!fake.eq))
		{
			fake.lt = false;
			fake.gt = false;
			fake.not = !fake.not;
			fake.eq = true;
		}
		return fake;
	}

}
