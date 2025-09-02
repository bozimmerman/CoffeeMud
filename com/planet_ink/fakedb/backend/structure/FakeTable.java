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

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import com.planet_ink.coffee_mud.core.CMSecurity;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.fakedb.backend.Backend;
import com.planet_ink.fakedb.backend.Backend.ConnectorType;
import com.planet_ink.fakedb.backend.Backend.FakeConditionResponder;
import com.planet_ink.fakedb.backend.structure.FakeColumn.FakeColType;

/**
*
*/
public class FakeTable
{
	protected File				fileName;
	protected RandomAccessFile	file;
	protected int				fileSize;
	protected byte[]			fileBuffer;
	protected IndexedRowMap		rowRecords	= new IndexedRowMap();
	protected int				dataStart = 0;
	protected String			schemaHash = "";
	protected byte[]			dataHeader = new byte[0];

	public final String			name;
	public int					version		= 1;
	public FakeColumn[]			columns;
	public String[]				columnNames;
	public Map<String, Integer>	columnHash	= new Hashtable<String, Integer>();
	public int[]				columnIndexesOfIndexed;

	public FakeTable(final String tableName, final File name)
	{
		this.name = tableName;
		fileName = name;
	}

	public int numColumns()
	{
		return columns.length;
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public int findColumn(final String name)
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
	public Iterator<RecordInfo> indexIterator(final int[] orderByIndexDex, final String[] orderByConditions)
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
	public String getColumnName(final int index)
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
	public FakeColumn getColumnInfo(final int index)
	{
		if ((index < 0) || (index > columns.length))
			return null;
		return columns[index];
	}

	public void close()
	{
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
		columnNames = null;
		columnHash = null;
		columnIndexesOfIndexed = null;
		rowRecords = new IndexedRowMap();
	}

	private void insertFileData(final int position, final byte[] data) throws IOException
	{
		final long originalLength = file.length();
		final long bytesToShift  = originalLength - position;
		file.setLength(originalLength + data.length);
		final int bufferSize = 1024 * 1024;
		final byte[] buffer = new byte[bufferSize];
		long remain = bytesToShift;
		while(remain >0)
		{
			final int bytesToCopy = (int) Math.min(bufferSize, remain);
			file.seek(position + remain - bytesToCopy);
			file.readFully(buffer, 0, bytesToCopy);
			file.seek(position + data.length + remain - bytesToCopy);
			file.write(buffer, 0, bytesToCopy);
			remain -= bytesToCopy;
		}
		file.seek(position);
		file.write(data);
		file.getFD().sync();
	}

	/**
	 * Open the data file, validate it, and index it if necc
	 * @throws IOException
	 */
	public void open() throws IOException
	{
		file = new RandomAccessFile(fileName, "rw");
		fileSize = 0;  // actual offset index during initial reading
		fileBuffer = new byte[4096];
		dataStart = 0;
		final int c = file.read();
		if(c > 0)
		{
			int v = file.read();
			if((c == 'V') && ((v > 47)&&(v<58)))
			{
				v=v-48;
				if(version != v)
					throw new IOException("Incompatible data files (expected V"+version+", got V"+v+")");
				final ByteArrayOutputStream out = new ByteArrayOutputStream();
				int b = file.read();
				while ((b >= 0) && (b != '\n'))
				{
					out.write(b);
					b = file.read();
				}
				dataHeader = out.toByteArray();
				String dataFormatHash = "";
				for(int i=0;i<dataHeader.length;i++)
				{
					if((dataHeader[i] == 'H')&&(i<=dataHeader.length-9))
					{
						dataFormatHash = new String(dataHeader,i+1,8);
						i += 8;
					}
				}
				dataStart = (int)file.getFilePointer();
				if ((dataFormatHash == null) || (!dataFormatHash.equals(this.schemaHash)))
					throw new IOException("Incompatible fakedb data file for table "+name+".  Use DBCopy to convert.");
				if(version == 2)
					return;
			}
			else
			{
				final String header = "V1H"+schemaHash+"\n";
				this.insertFileData(0, header.getBytes());
				file.close();
				open();
				return;
			}
		}
		else
		{
			final String header = "V"+version+"H"+schemaHash+"\n";
			this.insertFileData(0, header.getBytes());
			file.close();
			open();
			return;
		}
		fileSize = dataStart;

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

	public void rewriteDataFileHash(final List<String> schema)
	{
		close();
		if((!fileName.exists())||(fileName.length()<10)||(dataStart<10))
			return;
		final java.util.zip.CRC32 crc = new java.util.zip.CRC32();
		crc.update((this.name + " " + this.version).getBytes());
		for (int i = 1; i < schema.size(); i++)
			crc.update(schema.get(i).trim().getBytes());
		final String schemaHash =  String.format("%08X", Long.valueOf(crc.getValue())).toUpperCase();
		try
		{
			file = new RandomAccessFile(fileName, "rw");
			file.seek(0);
			file.write(("V"+version+"H"+schemaHash+"\n").getBytes());
			file.getFD().sync();
			file.close();
			close();
		}
		catch (final IOException e)
		{
		}
	}

	public void initializeColumns(final List<String> in) throws IOException
	{
		final java.util.zip.CRC32 crc = new java.util.zip.CRC32();
		crc.update((this.name + " " + this.version).getBytes());
		final List<FakeColumn> columns = new ArrayList<FakeColumn>();
		final List<String> keys = new ArrayList<String>();
		final List<String> indexes = new ArrayList<String>();
		while (in.size()>0)
		{
			String line = in.remove(0).trim();
			crc.update(line.getBytes());
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
				columnModifiers = line.substring(split + 1).trim().split(" ");
			}

			final FakeColumn info = new FakeColumn();
			info.tableName = name;
			info.name = columnName;
			try
			{
				info.type = FakeColType.valueOf(columnType.toUpperCase().trim());
				switch(info.type)
				{
				case BLOB:
				case CLOB:
					//version = 2;
					break;
				default:
					break;

				}
			}
			catch(final Exception e)
			{
				throw new IOException("Can not read schema: attributeType '" + columnType + "' is unknown");
			}
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
				if(Character.isDigit(modifier.charAt(0)))
				{
					try
					{
						version = 2;
						info.size = Integer.valueOf(modifier.trim()).intValue();
					}
					catch(final NumberFormatException e)
					{
						throw new IOException("Can not read schema: column size '" + modifier + "' is malformed");
					}
				}
				else
					throw new IOException("Can not read schema: attributeSpecial '" + modifier + "' is unknown");
			}
			columns.add(info);
		}
		this.schemaHash =  String.format("%08X", Long.valueOf(crc.getValue())).toUpperCase();
		this.columns = new FakeColumn[columns.size()];
		this.columnNames = new String[columns.size()];
		this.columnHash = new Hashtable<String, Integer>();
		int index = 0;
		for (final Iterator<FakeColumn> iter = columns.iterator(); iter.hasNext(); ++index)
		{
			final FakeColumn current = iter.next();
			this.columns[index] = current;
			this.columnNames[index] = current.name;
			this.columnHash.put(current.name, Integer.valueOf(index));
		}
		index = 0;
		this.columnIndexesOfIndexed = new int[indexes.size()];
		for (final Iterator<String> iter = indexes.iterator(); iter.hasNext(); ++index)
			this.columnIndexesOfIndexed[index] = this.findColumn(iter.next());
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
		file.seek(0);
		for(int i=0;i<dataStart;i++)
			tempOut.write(file.read());
		int newFileSize = dataStart;
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
	public synchronized boolean getRecord(final ComparableValue[] values, final RecordInfo info)
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
	public ComparableValue getNextLine(final FakeColType colType, final byte[] fileBuffer, final int[] dex)
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
					switch(fileBuffer[dex[0] + 1])
					{
					case '\\':
					{
						buffer.append('\\');
						dex[0]++;
						break;
					}
					case 'n':
					{
						buffer.append((char) 0x0A);
						dex[0]++;
						break;
					}
					case '#':
					{
						dex[0]++;
						final int count= (fileBuffer[++dex[0]] & 0xFF)-'0';
						final byte[] bt=new byte[count];
						for (int i = 0; i < count; i++)
						{
							int val=0;
							for (int bi = 0; bi < 2; bi++)
							{
								c = (char) (fileBuffer[++dex[0]] & 0xFF);
								if (c >= 'A')
									val = (16 * val) + 10 + (c - 'A');
								else
									val = (16 * val) + (c - '0');
							}
							bt[i]=(byte)(val & 0xff);
						}
						try
						{
							buffer.append(new String(bt,"UTF-8"));
						}
						catch (final UnsupportedEncodingException e)
						{
						}
						break;
					}
					default:
					{
						final byte[] bt=new byte[2];
						for (int i = 0; i < 2; i++)
						{
							int val=0;
							for (int bi = 0; bi < 2; bi++)
							{
								c = (char) (fileBuffer[++dex[0]] & 0xFF);
								if (c >= 'A')
									val = (16 * val) + 10 + (c - 'A');
								else
									val = (16 * val) + (c - '0');
							}
							bt[i]=(byte)(val & 0xff);
						}
						try
						{
							buffer.append(new String(bt,"UTF-8"));
						}
						catch (final UnsupportedEncodingException e)
						{
						}
						break;
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
				case INTEGER:
					return new ComparableValue(Integer.valueOf(buffer.toString()));
				case LONG:
				case DATETIME:
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
	private void increaseBuffer(final int required)
	{
		final int newSize = ((required + 4095) >>> 12) << 12;
		final byte[] newBuffer = new byte[newSize];
		System.arraycopy(fileBuffer, 0, newBuffer, 0, fileBuffer.length);
		fileBuffer = newBuffer;
	}

	/**
	 * Insert a new row into the table
	 * @param prevRecord the record to put this one after
	 * @param indexData index data for the records?
	 * @param values values to insert, complete
	 * @return true if all went well
	 */
	public synchronized boolean insertRecord(final RecordInfo prevRecord, final ComparableValue[] indexData, final ComparableValue[] values)
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
						final char c = s.charAt(sub);
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
							final String cs="" + c;
							final byte[] bytes=cs.getBytes("UTF-8");
							final StringBuilder s1=new StringBuilder("#"+bytes.length);
							for(int ib=0;ib<bytes.length;ib++)
							{
								final String bs=Integer.toHexString(bytes[ib] & 0xff).toUpperCase();
								if(bs.length()==1)
									s1.append("0");
								s1.append(bs);
							}
							for (int i = 0; i < s1.length(); i++)
								fileBuffer[ofs++] = (byte)s1.charAt(i);
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
	 * Delete records matching the given conditions
	 * @param conditions the conditions to respect
	 * @return the number of rows deleted
	 */
	public synchronized int deleteRecord(final List<FakeCondition> conditions)
	{
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
	public boolean recordCompare(final RecordInfo info, final List<FakeCondition> conditions, final boolean[] dataLoaded, final ComparableValue[] values)
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
	 * Insert a new column into the table by inserting empty
	 * values for the new column at the appropriate spots.
	 */
	public void addColumn()
	{
		if(file == null)
			return;
		try
		{
			final File tempFileName = new File(fileName.getName() + ".tmp");
			final File tempFileName2 = new File(fileName.getName() + ".cpy");
			final RandomAccessFile tempOut = new RandomAccessFile(tempFileName, "rw");
			file.seek(0);
			for(int i=0;i<dataStart;i++)
				tempOut.write(file.read());
			int newFileSize = dataStart;
			for (final Iterator<RecordInfo> iter = rowRecords.iterator(-1, false); iter.hasNext();)
			{
				final RecordInfo info = iter.next();
				file.seek(info.offset);
				final int size = info.size+1;
				if (size > fileBuffer.length)
					increaseBuffer(size);
				file.readFully(fileBuffer, 0, info.size);
				fileBuffer[info.size] = (byte) 0x0A;
				tempOut.write(fileBuffer, 0, size);
				info.size = size;
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
		catch (final Exception e)
		{
			Log.errOut("FakeTable", e);
		}
	}

	public void removeColumn(final int index0)
	{
		if(file == null)
			return;
		try
		{
			final File tempFileName = new File(fileName.getName() + ".tmp");
			final File tempFileName2 = new File(fileName.getName() + ".cpy");
			final RandomAccessFile tempOut = new RandomAccessFile(tempFileName, "rw");
			file.seek(0);
			for(int i=0;i<dataStart;i++)
				tempOut.write(file.read());
			int newFileSize = dataStart;
			for (final Iterator<RecordInfo> iter = rowRecords.iterator(-1, false); iter.hasNext();)
			{
				final RecordInfo info = iter.next();
				file.seek(info.offset);
				final byte[] recBuffer = new byte[info.size];
				file.readFully(recBuffer, 0, info.size);
				int currentPos = 0;
				int newlinesFound = 0;
				while (currentPos < info.size && newlinesFound < index0 + 1)
				{
					if (recBuffer[currentPos] == 0x0A)
						newlinesFound++;
					currentPos++;
				}
				final int startRemove = currentPos;
				int endPos = startRemove;
				while (endPos < info.size && recBuffer[endPos] != 0x0A)
					endPos++;
				final int removeLen = endPos - startRemove + 1;
				final int newSize = info.size - removeLen;
				System.arraycopy(recBuffer, endPos + 1, recBuffer, startRemove, info.size - (endPos + 1));
				tempOut.write(recBuffer, 0, newSize);
				info.offset = newFileSize;
				newFileSize += newSize;
				info.size = newSize;
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
		catch (final IOException e)
		{
		}
	}

	public void eraseDataFile()
	{
		close();
		if (fileName.exists())
			fileName.delete();
	}

	/**
	 *
	 * @param conditions
	 * @param callBack
	 */
	public void recordIterator(final List<FakeCondition> conditions, final FakeConditionResponder callBack) throws Exception
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
	 * Update the records matching the given conditions with new data
	 * @param conditions the conditions to match
	 * @param columns the columns to change
	 * @param values the new values
	 * @param backend the backend processor
	 * @param dupDangerTable no idea
	 * @return number of records updated
	 */
	public synchronized int updateRecord(final List<FakeCondition> conditions,
											final int[] columns,
											final ComparableValue[] values,
											final Backend backend,
											final FakeTable dupDangerTable) throws SQLException
	{
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
									if(keyChanges[i]!= null)
										strVals[i] = keyChanges[i].getValue().toString();
								backend.dupKeyCheck(dupDangerTable.name, dupDangerTable.columnNames, strVals);
								for(int i=0;i<keyChanges.length;i++)
								{
									if(keyChanges[i] != null)
									{
										for (int k = 0; k < rowIndexData.length; k++)
										{
											if (columnIndexesOfIndexed[k] == i)
												rowIndexData[k] = keyChanges[i];
										}
									}
								}
							}
						}
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
			if((e instanceof SQLException)
			&&((""+e.getMessage()).indexOf("dup")>=0))
				throw (SQLException)e;
			return -1;
		}
		return count[0];
	}
}
