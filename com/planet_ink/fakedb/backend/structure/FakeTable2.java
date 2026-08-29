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
				blobStore = new FlatFileFS(blobStoreFile().getAbsolutePath());
			}
			catch (final IOException e)
			{
				throw new SQLException("Unable to open blob store for table " + name, e);
			}
		}
		return blobStore;
	}

	private File blobStoreFile()
	{
		return new File(fileName.getParentFile(), name + ".flatfs");
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

	@Override
	public void eraseDataFile()
	{
		close();
		if (fileName.exists())
			fileName.delete();
		final File blobFile = blobStoreFile();
		if (blobFile.exists())
			blobFile.delete();
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
			col.indexOffset = -1;
			if (col.indexNumber >= 0)
			{
				col.indexOffset = width;
				width += (longSize * 2);
			}
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

	private synchronized ComparableValue readIndexValue(final FakeColumn col, final long rowOffset) throws IOException
	{
		final int valueWidth = col.getStoreValueWidth();
		final byte[] field = new byte[valueWidth];
		file.seek(rowOffset + col.valueOffset);
		file.readFully(field);
		return parseValue(col, field, 0, valueWidth);
	}

	private synchronized long readChild(final long rowOffset, final int slotOffset) throws IOException
	{
		file.seek(rowOffset + slotOffset);
		return readCheckedLong().longValue();
	}

	private synchronized void writeChild(final long rowOffset, final int slotOffset, final long child) throws IOException
	{
		file.seek(rowOffset + slotOffset);
		file.write(paddedLong(child).getBytes(StandardCharsets.US_ASCII));
	}

	private long getRoot(final FakeColumn col)
	{
		final Long r = indexRoots.get(col);
		return (r == null) ? 0 : r.longValue();
	}

	private int columnIndexOf(final FakeColumn col)
	{
		for (int i = 0; i < columns.length; i++)
			if (columns[i] == col)
				return i;
		return -1;
	}

	private long headerRootOffset(final FakeColumn col)
	{
		return (longSize * 2L) + ((long) columnIndexOf(col) * longSize);
	}

	private synchronized void setRoot(final FakeColumn col, final long root) throws IOException
	{
		indexRoots.put(col, Long.valueOf(root));
		file.seek(headerRootOffset(col));
		file.write(paddedLong(root).getBytes(StandardCharsets.US_ASCII));
	}

	/**
	 * Insert a row into column col's on-disk binary search tree.
	 * Ordering is (key, offset): equal keys are ordered by row offset.
	 */
	private void link(final FakeColumn col, final long rowOffset) throws IOException
	{
		final ComparableValue key = readIndexValue(col, rowOffset);
		final long root = getRoot(col);
		if (root == 0)
		{
			setRoot(col, rowOffset);
			writeChild(rowOffset, col.indexOffset, 0);
			writeChild(rowOffset, col.indexOffset + longSize, 0);
			return;
		}
		long cur = root;
		while (true)
		{
			final ComparableValue curKey = readIndexValue(col, cur);
			final int cmp = key.compareTo(curKey);
			final boolean goLeft = (cmp != 0) ? (cmp < 0) : (rowOffset < cur);
			final int slot = goLeft ? col.indexOffset : (col.indexOffset + longSize);
			final long child = readChild(cur, slot);
			if (child == 0)
			{
				writeChild(cur, slot, rowOffset);
				writeChild(rowOffset, col.indexOffset, 0);
				writeChild(rowOffset, col.indexOffset + longSize, 0);
				return;
			}
			cur = child;
		}
	}

	/**
	 * Remove a row from column col's on-disk binary search tree.
	 */
	private void unlink(final FakeColumn col, final long rowOffset) throws IOException
	{
		final ComparableValue key = readIndexValue(col, rowOffset);
		final long root = getRoot(col);
		if (root == 0)
			return;
		long parent = 0;
		int parentSlot = -1;
		long cur = root;
		while ((cur != 0) && (cur != rowOffset))
		{
			final ComparableValue curKey = readIndexValue(col, cur);
			final int cmp = key.compareTo(curKey);
			final boolean goLeft = (cmp != 0) ? (cmp < 0) : (rowOffset < cur);
			final int slot = goLeft ? col.indexOffset : (col.indexOffset + longSize);
			parent = cur;
			parentSlot = slot;
			cur = readChild(cur, slot);
		}
		if (cur == 0)
			return; // not found
		final long left = readChild(rowOffset, col.indexOffset);
		final long right = readChild(rowOffset, col.indexOffset + longSize);
		if ((left != 0) && (right != 0))
		{
			// two children: replace with in-order successor (leftmost of right subtree)
			long succ = right;
			long succParent = rowOffset;
			int succParentSlot = col.indexOffset + longSize;
			long succLeft = readChild(succ, col.indexOffset);
			while (succLeft != 0)
			{
				succParent = succ;
				succParentSlot = col.indexOffset;
				succ = succLeft;
				succLeft = readChild(succ, col.indexOffset);
			}
			final long succRight = readChild(succ, col.indexOffset + longSize);
			if (succ != right)
				writeChild(succParent, succParentSlot, succRight);
			writeChild(succ, col.indexOffset, left);
			if (succ != right)
				writeChild(succ, col.indexOffset + longSize, right);
			if (parent == 0)
				setRoot(col, succ);
			else
				writeChild(parent, parentSlot, succ);
		}
		else
		{
			final long child = (left != 0) ? left : right;
			if (parent == 0)
				setRoot(col, child);
			else
				writeChild(parent, parentSlot, child);
		}
	}

	/**
	 * Build the on-disk indexes by linking every active row into each indexed
	 * column's tree, then mark the header as indexed.  Rows are linked in a
	 * balanced (median-first) order so the resulting tree is height-balanced,
	 * regardless of the physical row order in the file.
	 */
	private void buildIndexes() throws IOException
	{
		final byte[] row = new byte[rowWidth];
		for (final FakeColumn col : columns)
		{
			if (col.indexNumber < 0)
				continue;
			final List<Object[]> pairs = new ArrayList<Object[]>();
			for (long off = headerSize; off < file.length(); off += rowWidth)
			{
				file.seek(off);
				file.readFully(row);
				if (row[0] == '-')
					pairs.add(new Object[] { parseValue(col, row, col.valueOffset, col.getStoreValueWidth()), Long.valueOf(off) });
			}
			Collections.sort(pairs, new Comparator<Object[]>()
			{
				@Override
				public int compare(final Object[] a, final Object[] b)
				{
					final ComparableValue ka = (ComparableValue) a[0];
					final ComparableValue kb = (ComparableValue) b[0];
					final int cmp = ka.compareTo(kb);
					if (cmp != 0)
						return cmp;
					return Long.compare(((Long) a[1]).longValue(), ((Long) b[1]).longValue());
				}
			});
			final List<Long> offsets = new ArrayList<Long>(pairs.size());
			for (final Object[] p : pairs)
				offsets.add((Long) p[1]);
			linkBalanced(col, offsets, 0, offsets.size() - 1);
		}
		file.seek(11);
		file.write((byte) 'I');
		file.getFD().sync();
	}

	/**
	 * Link the given (sorted by key) row offsets into col's tree in median-first
	 * order, producing a balanced binary search tree.
	 */
	private void linkBalanced(final FakeColumn col, final List<Long> offsets, final int lo, final int hi) throws IOException
	{
		if (lo > hi)
			return;
		final int mid = (lo + hi) >>> 1;
		link(col, offsets.get(mid).longValue());
		linkBalanced(col, offsets, lo, mid - 1);
		linkBalanced(col, offsets, mid + 1, hi);
	}

	private final class InOrderIterator implements Iterator<RecordInfo>
	{
		private final FakeColumn		col;
		private final boolean			descending;
		private final ComparableValue	lowKey;
		private final ComparableValue	highKey;
		private final Deque<Long>		stack	= new ArrayDeque<Long>();
		private RecordInfo				next	= null;

		InOrderIterator(final FakeColumn col, final boolean descending, final ComparableValue lowKey, final ComparableValue highKey)
		{
			this.col = col;
			this.descending = descending;
			this.lowKey = lowKey;
			this.highKey = highKey;
			if (descending)
				pushRightSpine(getRoot(col));
			else
				pushLeftSpine(getRoot(col));
			advance();
		}

		private ComparableValue key(final long off)
		{
			try
			{
				return readIndexValue(col, off);
			}
			catch (final IOException e)
			{
				return null;
			}
		}

		private long readChildSafe(final long node, final int slot)
		{
			try
			{
				return readChild(node, slot);
			}
			catch (final IOException e)
			{
				return 0;
			}
		}

		private void pushLeftSpine(long node)
		{
			while (node != 0)
			{
				final ComparableValue k = key(node);
				final int c = (lowKey == null) ? 1 : ((k == null) ? -1 : k.compareTo(lowKey));
				if (c >= 0)
				{
					stack.push(Long.valueOf(node));
					node = readChildSafe(node, col.indexOffset);
				}
				else
					node = readChildSafe(node, col.indexOffset + longSize);
			}
		}

		private void pushRightSpine(long node)
		{
			while (node != 0)
			{
				stack.push(Long.valueOf(node));
				node = readChildSafe(node, col.indexOffset + longSize);
			}
		}

		private void advance()
		{
			while (!stack.isEmpty())
			{
				final long node = stack.pop().longValue();
				if (highKey != null)
				{
					final ComparableValue k = key(node);
					if ((k == null) || (k.compareTo(highKey) > 0))
						continue;
				}
				next = new RecordInfo(node, rowWidth);
				if (descending)
					pushRightSpine(readChildSafe(node, col.indexOffset));
				else
					pushLeftSpine(readChildSafe(node, col.indexOffset + longSize));
				return;
			}
			next = null;
		}

		@Override
		public boolean hasNext()
		{
			return next != null;
		}

		@Override
		public RecordInfo next()
		{
			if (next == null)
				throw new NoSuchElementException();
			final RecordInfo info = next;
			advance();
			return info;
		}
	}

	private Iterator<RecordInfo> fullScanIterator()
	{
		return new Iterator<RecordInfo>()
		{
			private long	off		= headerSize;
			private final long	end		= length();
			private long	nextOff	= -1;

			private long length()
			{
				try
				{
					return file.length();
				}
				catch (final IOException e)
				{
					return headerSize;
				}
			}

			private void advance()
			{
				while (off < end)
				{
					try
					{
						final int m;
						synchronized (FakeTable2.this)
						{
							file.seek(off);
							m = file.read();
						}
						off += rowWidth;
						if (m == '-')
						{
							nextOff = off - rowWidth;
							return;
						}
					}
					catch (final IOException e)
					{
						nextOff = -1;
						return;
					}
				}
				nextOff = -1;
			}

			@Override
			public boolean hasNext()
			{
				if (nextOff < 0)
					advance();
				return nextOff >= 0;
			}

			@Override
			public RecordInfo next()
			{
				if (nextOff < 0)
					advance();
				if (nextOff < 0)
					throw new NoSuchElementException();
				final RecordInfo info = new RecordInfo(nextOff, rowWidth);
				nextOff = -1;
				return info;
			}
		};
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
			writeHeader(file, columns.length);
			file.getFD().sync();
		}
		else
		if(((file.length() - headerSize) % rowWidth) != 0)
			throw new IOException("Table data file for "+name+" has an incorrect width, and must be assumed corrupt");

		file.seek(longSize);
		this.firstFreeRow = readCheckedLong().longValue();

		// index flag (byte 11) + index roots
		file.seek(11);
		final int flagByte = file.read();
		final boolean indexesBuilt = (flagByte == 'I');
		if(indexesBuilt)
		{
			int position = longSize * 2;
			for(final FakeColumn col : columns)
			{
				file.seek(position);
				indexRoots.put(col, readCheckedLong());
				position += longSize;
			}
		}
		else
		{
			for(final FakeColumn col : columns)
				indexRoots.put(col, Long.valueOf(0));
		}
		if(((file.length() - headerSize) % rowWidth) != 0)
			throw new IOException("Table data file for "+name+" has an incorrect width, and must be assumed corrupt");
		if(!indexesBuilt)
			buildIndexes();
	}

	@Override
	public void recordIterator(final List<FakeCondition> conditions, final FakeConditionResponder callBack) throws Exception
	{
		if(version < 2)
		{
			super.recordIterator(conditions, callBack);
			return;
		}
		final boolean[] dataLoaded = new boolean[1];
		final ComparableValue[] values = new ComparableValue[columns.length];
		for (final Iterator<RecordInfo> iter = fullScanIterator(); iter.hasNext();)
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

	@Override
	public boolean recordCompare(final RecordInfo info, final List<FakeCondition> conditions, final boolean[] dataLoaded, final ComparableValue[] values)
	{
		if(version < 2)
			return super.recordCompare(info, conditions, dataLoaded, values);
		dataLoaded[0] = getRecord(values, info);
		if(!dataLoaded[0])
			return false;
		boolean lastOne = true;
		ConnectorType connector = ConnectorType.AND;
		for (final FakeCondition cond : conditions)
		{
			boolean thisOne = false;
			if (cond.contains != null)
				thisOne = recordCompare(info, cond.contains, dataLoaded, values);
			else
				thisOne = cond.compareValue(values[cond.conditionIndex]);
			if (connector == ConnectorType.OR)
				lastOne = lastOne || thisOne;
			else
				lastOne = lastOne && thisOne;
			connector = cond.connector;
		}
		return lastOne;
	}

	@Override
	public Iterator<RecordInfo> indexIterator(final int[] orderByIndexDex, final String[] orderByConditions)
	{
		if(version < 2)
			return super.indexIterator(orderByIndexDex, orderByConditions);
		if ((orderByIndexDex == null) || (orderByIndexDex.length == 0))
			return fullScanIterator();
		final boolean descending = (orderByConditions != null) && "DESC".equals(orderByConditions[0]);
		final FakeColumn col = columns[orderByIndexDex[0]];
		return new InOrderIterator(col, descending, null, null);
	}

	@Override
	public Iterator<RecordInfo> indexLookupIterator(final int columnIndex, final ComparableValue value)
	{
		if(version < 2)
			return super.indexLookupIterator(columnIndex, value);
		if ((columnIndex < 0) || (columnIndex >= columns.length))
			return Collections.<RecordInfo>emptyList().iterator();
		final FakeColumn col = columns[columnIndex];
		if (col.indexNumber < 0)
			return Collections.<RecordInfo>emptyList().iterator();
		return new InOrderIterator(col, false, value, value);
	}

	private synchronized void writeHeader(final RandomAccessFile out, final int numColumns) throws IOException
	{
		int position = 0;
		out.seek(0);
		final String header = "V"+version+"H"+schemaHash;
		out.write(paddedString(header,longSize).getBytes(StandardCharsets.US_ASCII));
		position += longSize;
		out.seek(position);
		out.write(paddedLong(0).getBytes(StandardCharsets.US_ASCII));
		for(int i=0;i<numColumns;i++)
		{
			position += longSize;
			out.seek(position);
			out.write(paddedLong(0).getBytes(StandardCharsets.US_ASCII));
		}
		out.write(hdrPad.substring((int)out.getFilePointer()).getBytes(StandardCharsets.US_ASCII));
	}

	/**
	 * Rebuild the on-disk data file to match a new column layout (ALTER TABLE).
	 * Rows are streamed by offset; the header is written without the 'I' flag so
	 * the caller's re-open (Backend.alterTable -> open()) rebuilds the indexes.
	 * @param tableDef the full new table definition, first line is "NAME V2"
	 * @throws SQLException if the rebuild fails
	 */
	public synchronized void rebuildDataFile(final List<String> tableDef) throws SQLException
	{
		final FakeColumn[] oldColumns = this.columns;
		final int oldRowWidth = this.rowWidth;

		final List<String> colLines = new ArrayList<String>(tableDef);
		colLines.remove(0);
		this.initializeColumns(colLines);

		try
		{
			computeLayout();
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to compute new layout for table " + name, e);
		}

		final Set<String> newColNames = new HashSet<String>();
		for (final FakeColumn col : this.columns)
			newColNames.add(col.name);
		final List<Integer> droppedBlobCols = new ArrayList<Integer>();
		for (int i = 0; i < oldColumns.length; i++)
			if (oldColumns[i].isBlobColumn() && !newColNames.contains(oldColumns[i].name))
				droppedBlobCols.add(Integer.valueOf(i));

		final List<String> blobRefsToFree = new ArrayList<String>();
		final List<ComparableValue[]> oldRows = new ArrayList<ComparableValue[]>();
		try
		{
			final byte[] row = new byte[oldRowWidth];
			for (long off = headerSize; off < file.length(); off += oldRowWidth)
			{
				file.seek(off);
				file.readFully(row);
				if (row[0] != '-')
					continue;
				final ComparableValue[] values = new ComparableValue[oldColumns.length];
				for (int i = 0; i < oldColumns.length; i++)
					values[i] = parseValue(oldColumns[i], row, oldColumns[i].valueOffset, oldColumns[i].getStoreValueWidth());
				for (final Integer di : droppedBlobCols)
				{
					final ComparableValue v = values[di.intValue()];
					final Object o = (v == null) ? null : v.getValue();
					if (o != null)
						blobRefsToFree.add(o.toString());
				}
				oldRows.add(values);
			}
		}
		catch (final IOException e)
		{
			throw new SQLException("Unable to read rows for table " + name, e);
		}

		final Map<String, Integer> oldIndex = new HashMap<String, Integer>();
		for (int i = 0; i < oldColumns.length; i++)
			oldIndex.put(oldColumns[i].name, Integer.valueOf(i));

		try
		{
			final File tempFileName = new File(fileName.getParentFile(), fileName.getName() + ".tmp");
			final File tempFileName2 = new File(fileName.getParentFile(), fileName.getName() + ".cpy");
			final RandomAccessFile tempOut = new RandomAccessFile(tempFileName, "rw");
			writeHeader(tempOut, this.columns.length);
			for (final ComparableValue[] oldValues : oldRows)
			{
				final byte[] newRow = new byte[rowWidth];
				Arrays.fill(newRow, (byte) ' ');
				newRow[0] = (byte) '-';
				int idxPos = 1;
				for (final FakeColumn col : this.columns)
				{
					if (col.indexNumber >= 0)
					{
						writeLongSlot(newRow, idxPos, 0);
						writeLongSlot(newRow, idxPos + longSize, 0);
						idxPos += longSize * 2;
					}
				}
				for (int i = 0; i < this.columns.length; i++)
				{
					final Integer oi = oldIndex.get(this.columns[i].name);
					final ComparableValue val = (oi != null) ? oldValues[oi.intValue()] : new ComparableValue(null);
					encodeValue(this.columns[i], val, newRow);
				}
				tempOut.write(newRow, 0, rowWidth);
			}
			tempOut.getFD().sync();
			tempOut.close();
			file.close();
			tempFileName2.delete();
			fileName.renameTo(tempFileName2);
			tempFileName.renameTo(fileName);
			tempFileName2.delete();
			file = new RandomAccessFile(fileName, "rw");
		}
		catch (final Exception e)
		{
			throw new SQLException("Unable to rebuild data file for table " + name, e);
		}

		for (final String ref : blobRefsToFree)
		{
			try
			{
				freeBlob(ref);
			}
			catch (final SQLException e)
			{
			}
		}
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
				if (col.indexNumber >= 0)
				{
					writeLongSlot(row, idxPos, 0);
					writeLongSlot(row, idxPos + longSize, 0);
					idxPos += longSize * 2;
				}
			}
			for (int i = 0; i < columns.length; i++)
				encodeValue(columns[i], values[i], row);

			final long recordPos;
			if (prevRecord != null)
				recordPos = prevRecord.offset;
			else
			if (firstFreeRow != 0)
			{
				recordPos = firstFreeRow;
				file.seek(recordPos + 1);
				final long nextFree = readCheckedLong().longValue();
				firstFreeRow = nextFree;
				file.seek(longSize);
				file.write(paddedLong(firstFreeRow).getBytes(StandardCharsets.US_ASCII));
			}
			else
				recordPos = file.length();

			file.seek(recordPos);
			file.write(row, 0, rowWidth);
			for (final FakeColumn col : columns)
				if (col.indexNumber >= 0)
					link(col, recordPos);
			file.getFD().sync();
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
		final List<String> blobRefsToFree = new ArrayList<String>();
		try
		{
			final FakeConditionResponder responder = new FakeConditionResponder()
			{
				public int[]	count;
				public List<String>	blobRefs;

				public FakeConditionResponder init(final int[] c, final List<String> b)
				{
					count = c;
					blobRefs = b;
					return this;
				}

				@Override
				public void callBack(final ComparableValue[] values, final RecordInfo info) throws Exception
				{
					for (final FakeColumn col : columns)
						if (col.indexNumber >= 0)
							unlink(col, info.offset);
					file.seek(info.offset);
					file.write(new byte[] { (byte) '*' });
					if(rowWidth >= longSize + 1)
					{
						file.seek(info.offset + 1);
						file.write(paddedLong(firstFreeRow).getBytes());
						firstFreeRow = info.offset;
						file.seek(longSize);
						file.write(paddedLong(firstFreeRow).getBytes());
					}
					for (int i = 0; i < columns.length; i++)
					{
						if (columns[i].isBlobColumn())
						{
							final ComparableValue v = values[i];
							final Object o = (v == null) ? null : v.getValue();
							if (o != null)
								blobRefs.add(o.toString());
						}
					}
					count[0]++;
				}
			}.init(count, blobRefsToFree);
			recordIterator(conditions, responder);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		for (final String ref : blobRefsToFree)
		{
			try
			{
				freeBlob(ref);
			}
			catch (final SQLException e)
			{
			}
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
		final int[] count = { 0 };
		final FakeColumn[] allCols = this.columns;
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
					boolean somethingChanged = false;
					ComparableValue[] keyChanges = null;
					final List<Integer> changedIndexCols = new ArrayList<Integer>();
					for (int sub = 0; sub < newCols.length; sub++)
					{
						final int colDex = newCols[sub];
						final FakeColumn col = allCols[colDex];
						final ComparableValue oldVal = values[colDex];
						ComparableValue newVal = updatedValues[sub];
						if (col.isBlobColumn())
						{
							final Object oldO = (oldVal == null) ? null : oldVal.getValue();
							final Object newO = (newVal == null) ? null : newVal.getValue();
							final String oldRef = (oldO == null) ? null : oldO.toString();
							final String newContent = (newO == null) ? null : newO.toString();
							boolean unchanged;
							if ((oldRef == null) && (newContent == null))
								unchanged = true;
							else if ((oldRef == null) || (newContent == null))
								unchanged = false;
							else
								unchanged = newContent.equals(loadBlob(oldRef));
							if (unchanged)
								newVal = oldVal;
							else
							{
								if (newContent == null)
									newVal = new ComparableValue(null);
								else
									newVal = new ComparableValue(storeBlob(newContent));
								if (oldRef != null)
									freeBlob(oldRef);
							}
						}
						if (!oldVal.equals(newVal))
						{
							if((dupDangerTable != null)
							&&(colDex < dupDangerTable.columns.length)
							&&(dupDangerTable.columns[colDex].keyNumber >=0))
							{
								if(keyChanges == null)
									keyChanges = new ComparableValue[colDex+1];
								else
								if(keyChanges.length<=colDex)
									keyChanges=Arrays.copyOf(keyChanges, colDex+1);
								keyChanges[colDex] = newVal;
							}
							if (col.indexNumber >= 0)
								changedIndexCols.add(Integer.valueOf(colDex));
							values[colDex] = newVal;
							somethingChanged = true;
						}
					}
					if(somethingChanged)
					{
						if(dupDangerTable != null)
						{
							final String[] strVals = new String[values.length];
							for(int x=0;x<values.length;x++)
							{
								@SuppressWarnings("rawtypes")
								final Comparable val = values[x].getValue();
								strVals[x]=(val == null) ? null : val.toString();
							}
							if(keyChanges != null)
							{
								for(int i=0;i<keyChanges.length;i++)
								{
									if(keyChanges[i]!= null)
									{
										@SuppressWarnings("rawtypes")
										final Comparable val = keyChanges[i].getValue();
										strVals[i] = (val == null) ? null : val.toString();
									}
								}
								backend.dupKeyCheck(dupDangerTable.name, dupDangerTable.columnNames, strVals);
							}
						}
						for (final Integer cIdx : changedIndexCols)
							unlink(allCols[cIdx.intValue()], info.offset);

						// re-read the current row so unchanged index slots are preserved
						final byte[] row = new byte[rowWidth];
						file.seek(info.offset);
						file.readFully(row);
						for (int i = 0; i < allCols.length; i++)
							encodeValue(allCols[i], values[i], row);
						file.seek(info.offset);
						file.write(row, 0, rowWidth);
						for (final Integer cIdx : changedIndexCols)
							link(allCols[cIdx.intValue()], info.offset);
						file.getFD().sync();
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
			return -1;
		}
		return count[0];
	}
}
