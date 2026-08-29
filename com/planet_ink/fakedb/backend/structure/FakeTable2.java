package com.planet_ink.fakedb.backend.structure;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

import com.planet_ink.fakedb.backend.Backend;
import com.planet_ink.fakedb.backend.Backend.ConnectorType;
import com.planet_ink.fakedb.backend.Backend.FakeConditionResponder;
import com.planet_ink.fakedb.backend.structure.FakeColumn.FakeColType;

/*
   Copyright 2001 Thomas Neumann
   Copyright 2004-2026 Bo Zimmerman

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

	private FlatFileFS				blobStore	= null;

	public FakeTable2(final String tableName, final File name)
	{
		super(tableName, name);
	}

	private FlatFileFS getBlobStore() throws SQLException
	{
		if (blobStore == null)
		{
			try
			{
				blobStore = new FlatFileFS(new File(fileName.getParentFile(), name + ".flatfs").getAbsolutePath());
			}
			catch (final IOException e)
			{
				throw new SQLException("Unable to open blob store for table " + name, e);
			}
		}
		return blobStore;
	}

	public String storeBlob(final String content) throws SQLException
	{
		final String uuid = java.util.UUID.randomUUID().toString();
		try
		{
			getBlobStore().writeFile(uuid, content);
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to store blob for table " + name, e);
		}
		return uuid;
	}

	public String loadBlob(final String ref) throws SQLException
	{
		try
		{
			return getBlobStore().readFile(ref);
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to read blob for table " + name, e);
		}
	}

	public void freeBlob(final String ref) throws SQLException
	{
		try
		{
			getBlobStore().deleteFile(ref);
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to free blob for table " + name, e);
		}
	}

	@Override
	public synchronized void close()
	{
		if (blobStore != null)
		{
			try
			{
				blobStore.close();
			}
			catch (final IOException e)
			{
			}
			blobStore = null;
		}
		super.close();
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
		byte c = file.readByte();
		while(c != '\n')
		{
			str.append((char)c);
			c = file.readByte();
			if(str.length()>max)
				throw new IOException("Bad size in "+name+" data.");
		}
		return str.toString();
	}

	private Long readCheckedLong() throws IOException
	{
		return Long.valueOf(readCheckedNString(longSize));
	}

	private void computeLayout() throws IOException
	{
		int width = 1; // the - and * marker
		for (int i = 0; i < columns.length; i++)
		{
			final FakeColumn col = columns[i];
			if ((col.keyNumber >= 0) || (col.indexNumber > 0))
				width += (longSize * 2);
		}
		for (int i = 0; i < columns.length; i++)
		{
			columns[i].valueOffset = width;
			try
			{
				width += columns[i].getStoreValueWidth();
			}
			catch (final IllegalArgumentException e)
			{
				throw new IOException(e.getMessage());
			}
		}
		rowWidth = width;
	}

	private ComparableValue parseValue(final FakeColumn col, final byte[] row, final int valueOffset, final int valueWidth)
	{
		switch(col.type)
		{
		case INTEGER:
		{
			final String s = new String(row, valueOffset, valueWidth, StandardCharsets.US_ASCII).trim();
			if (s.length() == 0)
				return new ComparableValue(null);
			try { return new ComparableValue(Integer.valueOf(s)); }
			catch (final NumberFormatException e) { return new ComparableValue(null); }
		}
		case LONG:
		case DATETIME:
		case UNKNOWN:
		{
			final String s = new String(row, valueOffset, valueWidth, StandardCharsets.US_ASCII).trim();
			if (s.length() == 0)
				return new ComparableValue(null);
			try { return new ComparableValue(Long.valueOf(s)); }
			catch (final NumberFormatException e) { return new ComparableValue(null); }
		}
		case STRING:
		{
			final String lenStr = new String(row, valueOffset, 3, StandardCharsets.US_ASCII).trim();
			try
			{
				final int len = Integer.parseInt(lenStr);
				if (len <= 0)
					return new ComparableValue(null);
				return new ComparableValue(new String(row, valueOffset + 3, len, StandardCharsets.UTF_8));
			}
			catch (final NumberFormatException e) { return new ComparableValue(null); }
		}
		case BLOB:
		case CLOB:
		{
			if (row[valueOffset] == ' ')
				return new ComparableValue(null);
			return new ComparableValue(new String(row, valueOffset + 1, 36, StandardCharsets.US_ASCII).trim());
		}
		}
		return new ComparableValue(null);
	}

	private ComparableValue[] parseIndexData(final byte[] row)
	{
		final ComparableValue[] indexData = new ComparableValue[columnIndexesOfIndexed.length];
		for (int i = 0; i < columnIndexesOfIndexed.length; i++)
		{
			final int colDex = columnIndexesOfIndexed[i];
			indexData[i] = parseValue(columns[colDex], row, columns[colDex].valueOffset, columns[colDex].getStoreValueWidth());
		}
		return indexData;
	}

	private void rethreadFreeChain(final Vector<Long> freeChain) throws IOException
	{
		if ((freeChain.size() > 0) && (columnIndexesOfIndexed.length > 0))
		{
			for (int i = 0; i < freeChain.size(); i++)
			{
				final long off = freeChain.get(i).longValue();
				final long next = (i + 1 < freeChain.size()) ? freeChain.get(i + 1).longValue() : 0;
				file.seek(off + 1);
				file.write(paddedLong(next).getBytes());
			}
			firstFreeRow = freeChain.get(0).longValue();
			file.seek(longSize);
			file.write(paddedLong(firstFreeRow).getBytes());
		}
		else
			firstFreeRow = 0;
	}

	private void scanRows() throws IOException
	{
		rowRecords = new IndexedRowMap();
		final Vector<Long> freeChain = new Vector<Long>();
		final byte[] row = new byte[rowWidth];
		for (long off = headerSize; off < file.length(); off += rowWidth)
		{
			file.seek(off);
			file.readFully(row);
			if (row[0] == (byte) '-')
			{
				final RecordInfo info = new RecordInfo((int) off, rowWidth);
				info.indexedData = parseIndexData(row);
				rowRecords.add(info);
			}
			else if (row[0] == (byte) '*')
				freeChain.add(Long.valueOf(off));
			else
				throw new IOException("Table data file for "+name+" has an unrecognized row marker at offset "+off);
		}
		rethreadFreeChain(freeChain);
	}

	/**
	 * Open the data file, validate it, and index it if necc
	 * @throws IOException
	 */
	@Override
	public synchronized void open() throws IOException
	{
		super.open();
		if(version < 2)
			return;
		// if no exception was thrown, then we have a right to be here.

		rowWidth = 1; // the - and * is still a good idea.
		computeLayout();
		rowPad = makePad(rowWidth);
		if(file.length()<headerSize)
		{
			int position = 0;
			file.seek(0);
			final String header = "V"+version+"H"+schemaHash;
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
		scanRows();
	}

	@Override
	public synchronized boolean getRecord(final ComparableValue[] values, final RecordInfo info)
	{
		if(version < 2)
			return super.getRecord(values, info);
		try
		{
			if(file == null)
				return false;
			final byte[] row = new byte[rowWidth];
			file.seek(info.offset);
			file.readFully(row);
			for (int i = 0; i < columns.length; i++)
				values[i] = parseValue(columns[i], row, columns[i].valueOffset, columns[i].getStoreValueWidth());
			return true;
		}
		catch (final IOException e)
		{
			return false;
		}
	}

	private void writeLongSlot(final byte[] row, final int offset, final long value)
	{
		final byte[] bytes = paddedLong(value).getBytes(StandardCharsets.US_ASCII);
		System.arraycopy(bytes, 0, row, offset, longSize);
	}

	private void encodeValue(final FakeColumn col, final ComparableValue value, final byte[] row)
	{
		final int valueOffset = col.valueOffset;
		final int valueWidth = col.getStoreValueWidth();
		Arrays.fill(row, valueOffset, valueOffset + valueWidth, (byte) ' ');
		row[valueOffset + valueWidth - 1] = (byte) '\n';
		final Object o = (value == null) ? null : value.getValue();
		if (o == null)
			return;
		switch (col.type)
		{
		case INTEGER:
		case LONG:
		case DATETIME:
		case UNKNOWN:
		{
			final byte[] bytes = o.toString().getBytes(StandardCharsets.US_ASCII);
			System.arraycopy(bytes, 0, row, valueOffset + valueWidth - 1 - bytes.length, bytes.length);
			break;
		}
		case STRING:
		{
			final byte[] bytes = o.toString().getBytes(StandardCharsets.UTF_8);
			final int len = Math.min(bytes.length, Math.min(col.size, 999));
			final byte[] lenBytes = String.format("%03d", Integer.valueOf(len)).getBytes(StandardCharsets.US_ASCII);
			System.arraycopy(lenBytes, 0, row, valueOffset, 3);
			System.arraycopy(bytes, 0, row, valueOffset + 3, len);
			break;
		}
		case BLOB:
		case CLOB:
		{
			final byte[] bytes = o.toString().getBytes(StandardCharsets.US_ASCII);
			row[valueOffset] = (byte) col.type.name().charAt(0);
			System.arraycopy(bytes, 0, row, valueOffset + 1, Math.min(bytes.length, 36));
			break;
		}
		}
	}

	@Override
	public synchronized boolean insertRecord(final RecordInfo prevRecord, final ComparableValue[] indexData, final ComparableValue[] values)
	{
		if(version < 2)
			return super.insertRecord(prevRecord, indexData, values);
		try
		{
			if(file == null)
				return false;
			final byte[] row = new byte[rowWidth];
			Arrays.fill(row, (byte) ' ');
			row[0] = (byte) '-';
			int idxPos = 1;
			for (final FakeColumn col : columns)
			{
				if ((col.keyNumber >= 0) || (col.indexNumber > 0))
				{
					writeLongSlot(row, idxPos, 0);
					writeLongSlot(row, idxPos + longSize, 0);
					idxPos += longSize * 2;
				}
			}
			for (int i = 0; i < columns.length; i++)
				encodeValue(columns[i], values[i], row);

			final int recordPos;
			if (prevRecord != null)
				recordPos = prevRecord.offset;
			else
			if (firstFreeRow != 0)
			{
				recordPos = (int) firstFreeRow;
				file.seek(recordPos + 1);
				final long nextFree = readCheckedLong().longValue();
				firstFreeRow = nextFree;
				file.seek(longSize);
				file.write(paddedLong(firstFreeRow).getBytes(StandardCharsets.US_ASCII));
			}
			else
				recordPos = (int) file.length();

			file.seek(recordPos);
			file.write(row, 0, rowWidth);
			file.getFD().sync();

			final RecordInfo info = new RecordInfo(recordPos, rowWidth);
			info.indexedData = (indexData != null) ? indexData : parseIndexData(row);
			rowRecords.add(info);
			return true;
		}
		catch (final IOException e)
		{
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
					file.seek(info.offset);
					file.write(new byte[] { (byte) '*' });
					rowRecords.remove(info);
					if(columnIndexesOfIndexed.length > 0)
					{
						file.seek(info.offset + 1);
						file.write(paddedLong(firstFreeRow).getBytes());
						firstFreeRow = info.offset;
						file.seek(longSize);
						file.write(paddedLong(firstFreeRow).getBytes());
					}
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
			return super.updateRecord(conditions, columns, values, backend, dupDangerTable);
		return 0;
	}
}
