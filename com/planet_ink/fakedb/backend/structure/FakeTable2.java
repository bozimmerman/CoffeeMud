package com.planet_ink.fakedb.backend.structure;


import java.io.*;
import java.sql.SQLException;
import java.util.*;

import com.planet_ink.fakedb.backend.Backend;
import com.planet_ink.fakedb.backend.Backend.ConnectorType;
import com.planet_ink.fakedb.backend.Backend.FakeConditionResponder;
import com.planet_ink.fakedb.backend.structure.FakeColumn.FakeColType;

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

/**
*
*/
public class FakeTable2 extends FakeTable
{
	protected final int		longSize	= 20;
	protected final String	longPad		= makePad(longSize);
	protected String		rowPad		= "";
	protected int			rowWidth	= 0;
	protected long			firstFreeRow= 0;
	protected final int		headerSize	= 1024;
	protected final String	hdrPad		= makePad(headerSize);

	protected Map<FakeColumn, Long>	indexRoots	= new Hashtable<FakeColumn, Long>();

	public FakeTable2(final String tableName, final File name)
	{
		super(tableName, name);
	}

	private String makePad(final int size)
	{
		final StringBuilder str = new StringBuilder("");
		for(int i=0;i<size;i++)
			str.append(" ");
		return str.toString();
	}

	private String paddedString(final String s, final int len)
	{
		return (s+"\n")+hdrPad.substring(0,len-s.length()-1);
	}

	private String paddedLong(final long x)
	{
		final String sx = Long.toString(x);
		return sx+"\n"+longPad.substring(0,longSize-sx.length()-1);
	}

	private String readCheckedNString(final int max) throws IOException
	{
		final StringBuilder str = new StringBuilder("");
		char c;
		while((c=file.readChar())!= '\n')
		{
			str.append(c);
			if(str.length()>max)
				throw new IOException("Bad size in "+name+" data.");
		}
		return str.toString();
	}

	private Long readCheckedLong() throws IOException
	{
		return Long.valueOf(readCheckedNString(longSize));
	}

	/**
	 * Open the data file, validate it, and index it if necc
	 * @throws IOException
	 */
	@Override
	public void open() throws IOException
	{
		super.open();
		if(version < 2)
			return;
		// if no exception was thrown, then we have a right to be here.

		rowWidth = 1; // the - and * is still a good idea.

		for(final FakeColumn col : columns)
		{
			if((col.keyNumber > 0)||(col.indexNumber > 0))
				rowWidth += (longSize * 2) + 2; // for left-right indexes, and \ns
			switch(col.type)
			{
			case BLOB:
			case CLOB:
				rowWidth += 38; // 1 for type marker, 36 for uuid, +1 \n
				break;
			case INTEGER:
				rowWidth += 12; // 11 for int, +1 \n
				break;
			case DATETIME:
			case LONG:
				rowWidth += 20; // 19 for long, +1 \n
				break;
			case STRING:
				if(col.size == Integer.MAX_VALUE)
					throw new IOException("Col "+col.name+", Table "+this.name+" has no size.");
				rowWidth += col.size+4; // +3 len, +str, +1 \n
				break;
			case UNKNOWN:
				rowWidth += 20; // 19 for long, +1 \n
				break;
			}
		}
		rowPad = makePad(rowWidth);
		if(file.length()<headerSize)
		{
			int position = 0;
			file.seek(0);
			final String header = "V"+version+"\n";
			file.write(paddedString(header,longSize).getBytes());
			position += longSize;
			file.seek(position);
			file.write(paddedLong(0).getBytes()); // first free block num
			for(int i=0;i<columns.length;i++)
			{
				position += longSize;
				file.seek(position);
				file.write(paddedLong(0).getBytes());
			}
			file.write(hdrPad.substring((int)file.getFilePointer()).getBytes());
			file.getFD().sync();
		}
		else
		{
			if(((file.length() - headerSize) % rowWidth) != 0)
				throw new IOException("Table data file for "+name+" has an incorrect width, and must be assumed corrupt");
			int position = longSize;
			file.seek(position);
			this.firstFreeRow = readCheckedLong().longValue();
			for(final FakeColumn col : columns)
			{
				position += longSize;
				file.seek(position);
				indexRoots.put(col,  readCheckedLong());
			}
		}
		if(((file.length() - headerSize) % rowWidth) != 0)
			throw new IOException("Table data file for "+name+" has an incorrect width, and must be assumed corrupt");
	}

	@Override
	public synchronized boolean insertRecord(final RecordInfo prevRecord, final ComparableValue[] indexData, final ComparableValue[] values)
	{
		if(version < 2)
			return super.insertRecord(prevRecord, indexData, values);
		try
		{
			if(((file.length() - headerSize) % rowWidth) != 0)
				return false;


			return true;
		}
		catch(final IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized int deleteRecord(final List<FakeCondition> conditions)
	{
		if(version < 2)
			return super.deleteRecord(conditions);
		final int[] count = { 0 };
		try
		{
			final FakeConditionResponder responder = new FakeConditionResponder()
			{
				public int[]	count;

				public FakeConditionResponder init(final int[] c)
				{
					count = c;
					return this;
				}

				@Override
				public void callBack(final ComparableValue[] values, final RecordInfo info) throws Exception
				{
					//TODO:
					/*
					file.seek(info.offset);
					file.write(new byte[] { (byte) '*' });
					rowRecords.remove(info);
					*/
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

	@Override
	public synchronized int updateRecord(final List<FakeCondition> conditions,
										final int[] columns,
										final ComparableValue[] values,
										final Backend backend,
										final FakeTable dupDangerTable) throws SQLException
	{
		if(version < 2)
			return super.deleteRecord(conditions);
		final int[] count = { 0 };
		try
		{
			final FakeConditionResponder responder = new FakeConditionResponder()
			{
				private int[]				count;
				private int[]				newCols;
				private ComparableValue[]	updatedValues	= null;

				public FakeConditionResponder init(final int[] c, final int[] a, final ComparableValue[] n)
				{
					count = c;
					newCols = a;
					updatedValues = n;
					return this;
				}

				@Override
				public void callBack(final ComparableValue[] values, final RecordInfo info) throws Exception
				{
					final ComparableValue[] rowIndexData = info.indexedData;
					boolean somethingChanged = false;
					ComparableValue[] keyChanges = null;
					for (int sub = 0; sub < newCols.length; sub++)
					{
						if (!values[newCols[sub]].equals(updatedValues[sub]))
						{
							if((dupDangerTable != null)
							&&(newCols[sub] < dupDangerTable.columns.length)
							&&(dupDangerTable.columns[newCols[sub]].keyNumber >=0))
							{
								if(keyChanges == null)
									keyChanges = new ComparableValue[newCols[sub]+1];
								else
								if(keyChanges.length<=newCols[sub])
									keyChanges=Arrays.copyOf(keyChanges, newCols[sub]+1);
								keyChanges[newCols[sub]] = updatedValues[sub];
							}
							else
							{
								for (int k = 0; k < rowIndexData.length; k++)
									if (columnIndexesOfIndexed[k] == newCols[sub])
										rowIndexData[k] = updatedValues[sub];
							}
							values[newCols[sub]] = updatedValues[sub];
							somethingChanged = true;
						}
					}
					if (somethingChanged)
					{
						if(dupDangerTable != null)
						{
							final String[] strVals = new String[values.length];
							for(int x=0;x<values.length;x++)
								strVals[x]=values[x].getValue().toString();
							if(keyChanges != null)
							{
								for(int i=0;i<keyChanges.length;i++)
									strVals[i] = keyChanges[i].getValue().toString();
								backend.dupKeyCheck(dupDangerTable.name, dupDangerTable.columnNames, strVals);
								for(int i=0;i<keyChanges.length;i++)
								{
									for (int k = 0; k < rowIndexData.length; k++)
									{
										if (columnIndexesOfIndexed[k] == i)
											rowIndexData[k] = keyChanges[i];
									}
								}
							}
						}
						//TODO:
						/*
						file.seek(info.offset);
						file.write(new byte[] { (byte) '*' });
						rowRecords.remove(info);
						insertRecord(info, rowIndexData, values);
						*/
					}
					count[0]++;
				}
			}.init(count, columns, values);
			recordIterator(conditions, responder);
		}
		catch (final Exception e)
		{
			if((e instanceof SQLException)
			&&((""+e.getMessage()).indexOf("dup")>=0))
				throw (SQLException)e;
			e.printStackTrace();
			return -1;
		}
		return count[0];
	}
}
