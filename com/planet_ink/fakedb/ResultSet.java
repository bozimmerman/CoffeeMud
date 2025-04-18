package com.planet_ink.fakedb;

import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.planet_ink.fakedb.Backend.*;

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
public class ResultSet implements java.sql.ResultSet
{
	private final Statement							statement;
	private final Backend.FakeTable					fakeTable;
	private java.util.Iterator<Backend.RecordInfo>	iter;
	private int										currentRow	= 0;
	private final List<FakeCondition>				conditions;
	private final ComparableValue[]					values;
	private final int[]								showCols;
	private final int[]								orderByKeyDexCols;
	private final String[]							orderByConditions;
	private final Map<String, Integer>				showColMap	= new Hashtable<String, Integer>();
	private boolean									wasNullFlag	= false;
	private Object									countValue	= null;
	private boolean									closeStatementOnClose;
	private static final List<RecordInfo>			fakeList	= new Vector<RecordInfo>(1);
	static
	{
		final RecordInfo info = new RecordInfo(0, 0);
		info.indexedData = new ComparableValue[0];
		fakeList.add(info);
	}

	ResultSet(final Statement stmt, final Backend.FakeTable table, final int[] showCols, final List<FakeCondition> conditions, final int[] orderByKeyDexCols, final String[] orderByConditions)
	{
		this.statement = stmt;
		this.fakeTable = table;
		this.conditions = conditions;
		currentRow = 0;
		this.values = new ComparableValue[table.numColumns()];
		this.showCols = showCols;
		this.orderByKeyDexCols = orderByKeyDexCols;
		this.orderByConditions = orderByConditions;
		this.iter = table.indexIterator(this.orderByKeyDexCols, this.orderByConditions);
		try
		{
			this.closeStatementOnClose = stmt.isCloseOnCompletion();
		}
		catch (final SQLException e)
		{
			this.closeStatementOnClose = false;
		}
		for (int s = 0; s < showCols.length; s++)
		{
			if (showCols[s] == FakeColumn.INDEX_COUNT)
			{
				showColMap.put("COUNT", Integer.valueOf(FakeColumn.INDEX_COUNT));
				int ct = 0;
				while (iter.hasNext())
				{
					iter.next();
					ct++;
				}
				countValue = Integer.valueOf(ct);
				iter = fakeList.iterator();
			}
			else
				showColMap.put(table.getColumnName(showCols[s]), Integer.valueOf(s));
		}
	}

	@Override
	public java.sql.Statement getStatement() throws java.sql.SQLException
	{
		return statement;
	}

	@Override
	public boolean next() throws java.sql.SQLException
	{
		while (true)
		{
			if (!iter.hasNext())
				return false;
			final Backend.RecordInfo rowInfo = iter.next();
			if (countValue != null)
			{
				currentRow++;
				return true;
			}
			if (conditions.size() > 0)
			{
				final boolean[] dataLoaded = new boolean[1];
				dataLoaded[0] = false;
				if (!fakeTable.recordCompare(rowInfo, conditions, dataLoaded, values))
					continue;
				currentRow++;
				if (!dataLoaded[0])
					dataLoaded[0] = fakeTable.getRecord(values, rowInfo);
				if (!dataLoaded[0])
					return false;
				return true;
			}
			currentRow++;
			return fakeTable.getRecord(values, rowInfo);
		}
	}

	@Override
	public void close() throws java.sql.SQLException
	{
		if ((this.statement != null) && (closeStatementOnClose))
			this.statement.close();
	}

	@Override
	public boolean wasNull() throws java.sql.SQLException
	{
		return wasNullFlag;
	}

	private Object getProperValue(int columnIndex) throws SQLException
	{
		wasNullFlag = false;
		if ((columnIndex < 1) || (columnIndex > showCols.length))
			throw new SQLException("Illegal column number "+columnIndex+"/"+showCols.length);
		columnIndex = showCols[columnIndex - 1];
		if (columnIndex == FakeColumn.INDEX_COUNT)
			return this.countValue;
		final Object v = values[columnIndex].getValue();
		if (v == null)
		{
			wasNullFlag = true;
			return null;
		}
		return v;
	}

	@Override
	public String getString(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		return o.toString();
	}

	@Override
	public java.sql.Array getArray(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		throw new java.sql.SQLException();
	}

	@Override
	public java.sql.Blob getBlob(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		throw new java.sql.SQLException();
	}

	@Override
	public java.sql.Clob getClob(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		throw new java.sql.SQLException();
	}

	@Override
	public java.sql.Ref getRef(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		throw new java.sql.SQLException();
	}

	@Override
	public boolean getBoolean(final int columnIndex) throws java.sql.SQLException
	{
		final String s = getString(columnIndex);
		if ((s != null) && (s.length() > 0))
		{
			switch (Character.toUpperCase(s.charAt(0)))
			{
			case 'T':
			case 'Y':
			case '1':
				return true;
			}
		}
		return false;
	}

	@Override
	public byte getByte(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return 0;
		if (o instanceof Integer)
			return ((Integer) o).byteValue();
		if (o instanceof Long)
			return ((Long) o).byteValue();
		try
		{
			return Byte.parseByte(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public short getShort(final int columnIndex) throws java.sql.SQLException
	{
		return (short) getLong(columnIndex);
	}

	@Override
	public int getInt(final int columnIndex) throws java.sql.SQLException
	{
		return (int) getLong(columnIndex);
	}

	@Override
	public long getLong(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return 0;
		if (o instanceof Integer)
			return ((Integer) o).longValue();
		if (o instanceof Long)
			return ((Long) o).longValue();
		try
		{
			return Long.parseLong(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public float getFloat(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return 0;
		if (o instanceof Integer)
			return ((Integer) o).floatValue();
		if (o instanceof Long)
			return ((Long) o).floatValue();
		try
		{
			return Float.parseFloat(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public double getDouble(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return 0;
		if (o instanceof Integer)
			return ((Integer) o).doubleValue();
		if (o instanceof Long)
			return ((Long) o).doubleValue();
		try
		{
			return Double.parseDouble(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public java.math.BigDecimal getBigDecimal(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return new java.math.BigDecimal(0);
		try
		{
			return new java.math.BigDecimal(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public java.math.BigDecimal getBigDecimal(final int columnIndex, final int scale) throws java.sql.SQLException
	{
		final java.math.BigDecimal decimal = getBigDecimal(columnIndex);
		decimal.setScale(scale);
		return decimal;
	}

	@Override
	public byte[] getBytes(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		try
		{
			return o.toString().getBytes();
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public java.sql.Date getDate(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		if (o instanceof Integer)
			return new java.sql.Date(((Integer) o).longValue());
		if (o instanceof Long)
			return new java.sql.Date(((Long) o).longValue());
		try
		{
			return java.sql.Date.valueOf(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public java.sql.Time getTime(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		if (o instanceof Integer)
			return new java.sql.Time(((Integer) o).longValue());
		if (o instanceof Long)
			return new java.sql.Time(((Long) o).longValue());
		try
		{
			return java.sql.Time.valueOf(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public java.sql.Timestamp getTimestamp(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		if (o == null)
			return null;
		if (o instanceof Integer)
			return new java.sql.Timestamp(((Integer) o).longValue());
		if (o instanceof Long)
			return new java.sql.Timestamp(((Long) o).longValue());
		try
		{
			return java.sql.Timestamp.valueOf(o.toString());
		}
		catch (final NumberFormatException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public java.io.InputStream getAsciiStream(final int columnIndex) throws java.sql.SQLException
	{
		return getBinaryStream(columnIndex);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public java.io.InputStream getUnicodeStream(final int columnIndex) throws java.sql.SQLException
	{
		return getBinaryStream(columnIndex);
	}

	@Override
	public java.io.InputStream getBinaryStream(final int columnIndex) throws java.sql.SQLException
	{
		final byte b[] = getBytes(columnIndex);
		if (b == null)
			return null;
		return new java.io.ByteArrayInputStream(b);
	}

	@Override
	public java.io.Reader getCharacterStream(final int columnIndex) throws java.sql.SQLException
	{
		final String s = getString(columnIndex);
		if (s == null)
			return null;
		return new java.io.CharArrayReader(s.toCharArray());
	}

	@Override
	public Object getObject(final int columnIndex) throws java.sql.SQLException
	{
		final Object o = getProperValue(columnIndex);
		return o;
	}

	@Override
	public java.net.URL getURL(final int columnIndex) throws java.sql.SQLException
	{
		final String s = getString(columnIndex);
		if (s == null)
			return null;
		try
		{
			return new java.net.URL(s);
		}
		catch (final java.net.MalformedURLException e)
		{
			throw new java.sql.SQLException(e.getMessage());
		}
	}

	@Override
	public int findColumn(final String columnName) throws java.sql.SQLException
	{
		if (!showColMap.containsKey(columnName))
			return -1;
		return showColMap.get(columnName).intValue() + 1;
	}

	@Override
	public String getString(final String columnName) throws java.sql.SQLException
	{
		return getString(findColumn(columnName));
	}

	@Override
	public java.sql.Array getArray(final String columnName) throws java.sql.SQLException
	{
		return getArray(findColumn(columnName));
	}

	@Override
	public java.sql.Blob getBlob(final String columnName) throws java.sql.SQLException
	{
		return getBlob(findColumn(columnName));
	}

	@Override
	public java.sql.Clob getClob(final String columnName) throws java.sql.SQLException
	{
		return getClob(findColumn(columnName));
	}

	@Override
	public java.sql.Ref getRef(final String columnName) throws java.sql.SQLException
	{
		return getRef(findColumn(columnName));
	}

	@Override
	public boolean getBoolean(final String columnName) throws java.sql.SQLException
	{
		return getBoolean(findColumn(columnName));
	}

	@Override
	public byte getByte(final String columnName) throws java.sql.SQLException
	{
		return getByte(findColumn(columnName));
	}

	@Override
	public short getShort(final String columnName) throws java.sql.SQLException
	{
		return getShort(findColumn(columnName));
	}

	@Override
	public int getInt(final String columnName) throws java.sql.SQLException
	{
		return getInt(findColumn(columnName));
	}

	@Override
	public long getLong(final String columnName) throws java.sql.SQLException
	{
		return getLong(findColumn(columnName));
	}

	@Override
	public float getFloat(final String columnName) throws java.sql.SQLException
	{
		return getFloat(findColumn(columnName));
	}

	@Override
	public double getDouble(final String columnName) throws java.sql.SQLException
	{
		return getDouble(findColumn(columnName));
	}

	@Override
	public java.math.BigDecimal getBigDecimal(final String columnName) throws java.sql.SQLException
	{
		return getBigDecimal(findColumn(columnName));
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public java.math.BigDecimal getBigDecimal(final String columnName, final int scale) throws java.sql.SQLException
	{
		return getBigDecimal(findColumn(columnName), scale);
	}

	@Override
	public byte[] getBytes(final String columnName) throws java.sql.SQLException
	{
		return getBytes(findColumn(columnName));
	}

	@Override
	public java.sql.Date getDate(final String columnName) throws java.sql.SQLException
	{
		return getDate(findColumn(columnName));
	}

	@Override
	public java.sql.Date getDate(final int columnName, final java.util.Calendar c) throws java.sql.SQLException
	{
		return getDate(columnName);
	}

	@Override
	public java.sql.Date getDate(final String columnName, final java.util.Calendar c) throws java.sql.SQLException
	{
		return getDate(findColumn(columnName));
	}

	@Override
	public java.sql.Time getTime(final String columnName) throws java.sql.SQLException
	{
		return getTime(findColumn(columnName));
	}

	@Override
	public java.sql.Time getTime(final int columnName, final java.util.Calendar c) throws java.sql.SQLException
	{
		return getTime(columnName);
	}

	@Override
	public java.sql.Time getTime(final String columnName, final java.util.Calendar c) throws java.sql.SQLException
	{
		return getTime(findColumn(columnName));
	}

	@Override
	public java.sql.Timestamp getTimestamp(final String columnName) throws java.sql.SQLException
	{
		return getTimestamp(findColumn(columnName));
	}

	@Override
	public java.sql.Timestamp getTimestamp(final int columnName, final java.util.Calendar c) throws java.sql.SQLException
	{
		return getTimestamp(columnName);
	}

	@Override
	public java.sql.Timestamp getTimestamp(final String columnName, final java.util.Calendar c) throws java.sql.SQLException
	{
		return getTimestamp(findColumn(columnName));
	}

	@Override
	public java.io.Reader getCharacterStream(final String columnName) throws java.sql.SQLException
	{
		return getCharacterStream(findColumn(columnName));
	}

	@Override
	public java.io.InputStream getAsciiStream(final String columnName) throws java.sql.SQLException
	{
		return getAsciiStream(findColumn(columnName));
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public java.io.InputStream getUnicodeStream(final String columnName) throws java.sql.SQLException
	{
		return getUnicodeStream(findColumn(columnName));
	}

	@Override
	public java.io.InputStream getBinaryStream(final String columnName) throws java.sql.SQLException
	{
		return getBinaryStream(findColumn(columnName));
	}

	@Override
	public java.net.URL getURL(final String columnName) throws java.sql.SQLException
	{
		return getURL(findColumn(columnName));
	}

	@Override
	public Object getObject(final String columnName) throws java.sql.SQLException
	{
		return getObject(findColumn(columnName));
	}

	@Override
	public java.sql.SQLWarning getWarnings() throws java.sql.SQLException
	{
		return null;
	}

	@Override
	public void clearWarnings() throws java.sql.SQLException
	{
	}

	@Override
	public String getCursorName() throws java.sql.SQLException
	{
		throw new java.sql.SQLException("Positioned Update not supported.", "S1C00");
	}

	@Override
	public java.sql.ResultSetMetaData getMetaData() throws java.sql.SQLException
	{
		return new java.sql.ResultSetMetaData()
		{
			@Override
			public <T> T unwrap(final Class<T> iface) throws SQLException
			{
				return null;
			}

			@Override
			public boolean isWrapperFor(final Class<?> iface) throws SQLException
			{
				return false;
			}

			@Override
			public int getColumnCount() throws SQLException
			{
				return showCols.length;
			}

			@Override
			public boolean isAutoIncrement(final int column) throws SQLException
			{
				return false;
			}

			@Override
			public boolean isCaseSensitive(final int column) throws SQLException
			{
				return true;
			}

			@Override
			public boolean isSearchable(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return false;
				return true;
			}

			@Override
			public boolean isCurrency(final int column) throws SQLException
			{
				return false;
			}

			@Override
			public int isNullable(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return columnNoNulls;
				return fakeTable.getColumnInfo(showCols[column - 1]).canNull ? columnNullable : columnNoNulls;
			}

			@Override
			public boolean isSigned(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return false;
				switch (fakeTable.getColumnInfo(showCols[column - 1]).type)
				{
				case FakeColumn.TYPE_INTEGER:
					return true;
				case FakeColumn.TYPE_LONG:
					return true;
				case FakeColumn.TYPE_STRING:
					return false;
				case FakeColumn.TYPE_UNKNOWN:
					return false;
				}
				return false;
			}

			@Override
			public int getColumnDisplaySize(final int column) throws SQLException
			{
				if ((column < 1) || (column > showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return 6;
				switch (fakeTable.getColumnInfo(showCols[column - 1]).type)
				{
				case FakeColumn.TYPE_INTEGER:
					return 9;
				case FakeColumn.TYPE_LONG:
					return 19;
				case FakeColumn.TYPE_STRING:
					return 40;
				case FakeColumn.TYPE_UNKNOWN:
					return 10;
				}
				return 10;
			}

			@Override
			public String getColumnLabel(final int column) throws SQLException
			{
				return getColumnName(column);
			}

			@Override
			public String getColumnName(final int column) throws SQLException
			{
				if ((column < 1) || (column > showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return "COUNT";
				return fakeTable.getColumnName(showCols[column - 1]);
			}

			@Override
			public String getSchemaName(final int column) throws SQLException
			{
				return statement.getFakeConnection().getSchema();
			}

			@Override
			public int getPrecision(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return 9;
				switch (fakeTable.getColumnInfo(showCols[column - 1]).type)
				{
				case FakeColumn.TYPE_INTEGER:
					return 9;
				case FakeColumn.TYPE_LONG:
					return 19;
				case FakeColumn.TYPE_STRING:
					return Integer.MAX_VALUE;
				case FakeColumn.TYPE_UNKNOWN:
					return 0;
				}
				return 0;
			}

			@Override
			public int getScale(final int column) throws SQLException
			{
				return 0;
			}

			@Override
			public String getTableName(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return "FAKEDB";
				return fakeTable.getColumnInfo(showCols[column - 1]).tableName;
			}

			@Override
			public String getCatalogName(final int column) throws SQLException
			{
				return statement.getConnection().getCatalog();
			}

			@Override
			public int getColumnType(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return java.sql.Types.INTEGER;
				switch (fakeTable.getColumnInfo(showCols[column - 1]).type)
				{
				case FakeColumn.TYPE_INTEGER:
					return java.sql.Types.INTEGER;
				case FakeColumn.TYPE_LONG:
					return java.sql.Types.BIGINT;
				case FakeColumn.TYPE_STRING:
					return java.sql.Types.VARCHAR;
				case FakeColumn.TYPE_UNKNOWN:
					return java.sql.Types.JAVA_OBJECT;
				}
				return java.sql.Types.JAVA_OBJECT;
			}

			@Override
			public String getColumnTypeName(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return "integer";
				switch (fakeTable.getColumnInfo(showCols[column - 1]).type)
				{
				case FakeColumn.TYPE_INTEGER:
					return "integer";
				case FakeColumn.TYPE_LONG:
					return "long";
				case FakeColumn.TYPE_STRING:
					return "string";
				case FakeColumn.TYPE_UNKNOWN:
					return "unknown";
				}
				return "unknown";
			}

			@Override
			public boolean isReadOnly(final int column) throws SQLException
			{
				return false;
			}

			@Override
			public boolean isWritable(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return false;
				return true;
			}

			@Override
			public boolean isDefinitelyWritable(final int column) throws SQLException
			{
				return isWritable(column);
			}

			@Override
			public String getColumnClassName(final int column) throws SQLException
			{
				if ((column < 1) || (column >= showCols.length))
					throw new SQLException("Value out of range.");
				if (showCols[column - 1] == FakeColumn.INDEX_COUNT)
					return Integer.class.getName();
				switch (fakeTable.getColumnInfo(showCols[column - 1]).type)
				{
				case FakeColumn.TYPE_INTEGER:
					return Integer.class.getName();
				case FakeColumn.TYPE_LONG:
					return Long.class.getName();
				case FakeColumn.TYPE_STRING:
					return String.class.getName();
				case FakeColumn.TYPE_UNKNOWN:
					return String.class.getName();
				}
				return String.class.getName();
			}
		};
	}

	@Override
	public void updateArray(final int columnIndex, final java.sql.Array x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateArray(final String columnName, final java.sql.Array x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateAsciiStream(final int columnIndex, final java.io.InputStream x, final int length) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateAsciiStream(final String columnName, final java.io.InputStream x, final int length) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBigDecimal(final int columnIndex, final java.math.BigDecimal x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBigDecimal(final String columnName, final java.math.BigDecimal x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBinaryStream(final int columnIndex, final java.io.InputStream x, final int length) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBinaryStream(final String columnName, final java.io.InputStream x, final int length) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBlob(final int columnIndex, final java.sql.Blob x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBlob(final String columnName, final java.sql.Blob x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBoolean(final int columnIndex, final boolean x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBoolean(final String columnName, final boolean x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateByte(final int columnIndex, final byte x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateByte(final String columnName, final byte x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBytes(final int columnIndex, final byte[] x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateBytes(final String columnName, final byte[] x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateCharacterStream(final int columnIndex, final java.io.Reader x, final int length) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateCharacterStream(final String columnName, final java.io.Reader reader, final int length) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateClob(final int columnIndex, final java.sql.Clob x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateClob(final String columnName, final java.sql.Clob x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateDate(final int columnIndex, final java.sql.Date x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateDate(final String columnName, final java.sql.Date x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateDouble(final int columnIndex, final double x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateDouble(final String columnName, final double x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateFloat(final int columnIndex, final float x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateFloat(final String columnName, final float x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateInt(final int columnIndex, final int x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateInt(final String columnName, final int x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateLong(final int columnIndex, final long x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateLong(final String columnName, final long x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateNull(final int columnIndex) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateNull(final String columnName) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateObject(final int columnIndex, final Object x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateObject(final int columnIndex, final Object x, final int scale) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateObject(final String columnName, final Object x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateObject(final String columnName, final Object x, final int scale) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateRef(final int columnIndex, final java.sql.Ref x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateRef(final String columnName, final java.sql.Ref x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateRow() throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateShort(final int columnIndex, final short x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateShort(final String columnName, final short x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateString(final int columnIndex, final String x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateString(final String columnName, final String x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateTime(final int columnIndex, final java.sql.Time x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateTime(final String columnName, final java.sql.Time x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateTimestamp(final int columnIndex, final java.sql.Timestamp x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void updateTimestamp(final String columnName, final java.sql.Timestamp x) throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void deleteRow() throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void moveToInsertRow() throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void moveToCurrentRow() throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void cancelRowUpdates() throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void insertRow() throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public void refreshRow() throws java.sql.SQLException
	{
		throw new java.sql.SQLException();
	}

	@Override
	public int getRow()
	{
		return currentRow;
	}

	@Override
	public boolean first()
	{
		return false;
	}

	@Override
	public boolean previous()
	{
		return false;
	}

	@Override
	public boolean isFirst()
	{
		return false;
	}

	private boolean	afterLast	= false;

	@Override
	public boolean last()
	{
		try
		{
			while (next())
			{
			}
		}
		catch (final java.sql.SQLException sqle)
		{
			sqle.printStackTrace();
		}
		afterLast = true;
		return true;
	}

	@Override
	public boolean isLast()
	{
		return false;
	}

	@Override
	public void beforeFirst() throws java.sql.SQLException
	{
		if (fakeTable == null)
			throw new java.sql.SQLException();
		iter = fakeTable.indexIterator(this.orderByKeyDexCols, this.orderByConditions);
		currentRow = 0;
	}

	@Override
	public boolean isBeforeFirst()
	{
		return (currentRow == 0);
	}

	@Override
	public void afterLast()
	{
		last();
	}

	@Override
	public boolean isAfterLast()
	{
		return afterLast;
	}

	@Override
	public boolean absolute(final int i)
	{
		return true;
	}

	@Override
	public boolean relative(final int i)
	{
		return false;
	}

	@Override
	public boolean rowDeleted()
	{
		return false;
	}

	@Override
	public boolean rowInserted()
	{
		return false;
	}

	@Override
	public boolean rowUpdated()
	{
		return false;
	}

	@Override
	public int getConcurrency()
	{
		return 0;
	}

	@Override
	public int getType()
	{
		return 0;
	}

	@Override
	public void setFetchSize(final int i) throws java.sql.SQLException
	{
		statement.setFetchSize(i);
	}

	@Override
	public int getFetchSize() throws java.sql.SQLException
	{
		return statement.getFetchSize();
	}

	@Override
	public void setFetchDirection(final int i) throws java.sql.SQLException
	{
		statement.setFetchDirection(i);
	}

	@Override
	public int getFetchDirection() throws java.sql.SQLException
	{
		return statement.getFetchDirection();
	}

	public int getResultSetConcurrency() throws java.sql.SQLException
	{
		return statement.getResultSetConcurrency();
	}

	public int getResultSetType() throws java.sql.SQLException
	{
		return statement.getResultSetType();
	}

	@Override
	public int getHoldability() throws SQLException
	{
		return 0;
	}

	@Override
	public Reader getNCharacterStream(final int arg0) throws SQLException
	{
		return null;
	}

	@Override
	public Reader getNCharacterStream(final String arg0) throws SQLException
	{
		return null;
	}

	@Override
	public NClob getNClob(final int arg0) throws SQLException
	{
		return null;
	}

	@Override
	public NClob getNClob(final String arg0) throws SQLException
	{
		return null;
	}

	@Override
	public String getNString(final int arg0) throws SQLException
	{
		return null;
	}

	@Override
	public String getNString(final String arg0) throws SQLException
	{
		return null;
	}

	// public Object getObject(int arg0, Map arg1) throws SQLException { return
	// getString(arg0); }
	@Override
	public Object getObject(final int arg0, final Map<String, Class<?>> arg1) throws SQLException
	{
		return getString(arg0);
	}

	@Override
	public Object getObject(final String arg0, final Map<String, Class<?>> arg1) throws SQLException
	{
		return getObject(findColumn(arg0), arg1);
	}

	// public Object getObject(String arg0, Map arg1) throws SQLException {
	// return getObject(findColumn(arg0),arg1); }
	@Override
	public RowId getRowId(final int arg0) throws SQLException
	{
		return null;
	}

	@Override
	public RowId getRowId(final String arg0) throws SQLException
	{
		return null;
	}

	@Override
	public SQLXML getSQLXML(final int arg0) throws SQLException
	{
		return null;
	}

	@Override
	public SQLXML getSQLXML(final String arg0) throws SQLException
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException
	{
		if (type == Long.class)
			return (T) Long.valueOf(this.getLong(columnIndex));
		if (type == Integer.class)
			return (T) Long.valueOf(this.getInt(columnIndex));
		if (type == Short.class)
			return (T) Short.valueOf(this.getShort(columnIndex));
		if (type == String.class)
			return (T) String.valueOf(this.getString(columnIndex));
		if (type == Short.class)
			return (T) Short.valueOf(this.getShort(columnIndex));
		if (type == Double.class)
			return (T) Double.valueOf(this.getDouble(columnIndex));
		if (type == Float.class)
			return (T) Float.valueOf(this.getFloat(columnIndex));
		if (type == Byte.class)
			return (T) Byte.valueOf(this.getByte(columnIndex));
		if (type == Boolean.class)
			return (T) Boolean.valueOf(this.getBoolean(columnIndex));
		if (type == java.sql.Array.class)
			return (T) this.getArray(columnIndex);
		if (type == java.sql.Blob.class)
			return (T) this.getBlob(columnIndex);
		if (type == java.sql.Clob.class)
			return (T) this.getClob(columnIndex);
		if (type == java.math.BigDecimal.class)
			return (T) this.getBigDecimal(columnIndex);
		if (type == byte[].class)
			return (T) this.getBytes(columnIndex);
		if (type == java.sql.Date.class)
			return (T) this.getDate(columnIndex);
		if (type == java.sql.Time.class)
			return (T) this.getTime(columnIndex);
		if (type == java.sql.Timestamp.class)
			return (T) this.getTimestamp(columnIndex);
		if (type == Object.class)
			return (T) this.getObject(columnIndex);
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException
	{
		return getObject(findColumn(columnLabel), type);
	}

	@Override
	public boolean isClosed() throws SQLException
	{
		return false;
	}

	@Override
	public void updateAsciiStream(final int arg0, final InputStream arg1) throws SQLException
	{
	}

	@Override
	public void updateAsciiStream(final String arg0, final InputStream arg1) throws SQLException
	{
	}

	@Override
	public void updateAsciiStream(final int arg0, final InputStream arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateAsciiStream(final String arg0, final InputStream arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateBinaryStream(final int arg0, final InputStream arg1) throws SQLException
	{
	}

	@Override
	public void updateBinaryStream(final String arg0, final InputStream arg1) throws SQLException
	{
	}

	@Override
	public void updateBinaryStream(final int arg0, final InputStream arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateBinaryStream(final String arg0, final InputStream arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateBlob(final int arg0, final InputStream arg1) throws SQLException
	{
	}

	@Override
	public void updateBlob(final String arg0, final InputStream arg1) throws SQLException
	{
	}

	@Override
	public void updateBlob(final int arg0, final InputStream arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateBlob(final String arg0, final InputStream arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateCharacterStream(final int arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateCharacterStream(final String arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateCharacterStream(final int arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateCharacterStream(final String arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateClob(final int arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateClob(final String arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateClob(final int arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateClob(final String arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateNCharacterStream(final int arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateNCharacterStream(final String arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateNCharacterStream(final int arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateNCharacterStream(final String arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateNClob(final int arg0, final NClob arg1) throws SQLException
	{
	}

	@Override
	public void updateNClob(final String arg0, final NClob arg1) throws SQLException
	{
	}

	@Override
	public void updateNClob(final int arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateNClob(final String arg0, final Reader arg1) throws SQLException
	{
	}

	@Override
	public void updateNClob(final int arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateNClob(final String arg0, final Reader arg1, final long arg2) throws SQLException
	{
	}

	@Override
	public void updateNString(final int arg0, final String arg1) throws SQLException
	{
	}

	@Override
	public void updateNString(final String arg0, final String arg1) throws SQLException
	{
	}

	@Override
	public void updateRowId(final int arg0, final RowId arg1) throws SQLException
	{
	}

	@Override
	public void updateRowId(final String arg0, final RowId arg1) throws SQLException
	{
	}

	@Override
	public void updateSQLXML(final int arg0, final SQLXML arg1) throws SQLException
	{
	}

	@Override
	public void updateSQLXML(final String arg0, final SQLXML arg1) throws SQLException
	{
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException
	{
		return false;
	}

	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException
	{
		return null;
	}
}
