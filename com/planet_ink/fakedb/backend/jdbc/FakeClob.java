package com.planet_ink.fakedb.backend.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/*
   Copyright 2026 Bo Zimmerman

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
 * Minimal read-only Clob implementation backed by a String.
 */
public class FakeClob implements Clob
{
	private String	data;
	private boolean	freed	= false;

	public FakeClob(final String data)
	{
		this.data = (data == null) ? "" : data;
	}

	private void checkFreed() throws SQLException
	{
		if (freed)
			throw new SQLException("Clob has already been freed");
	}

	@Override
	public long length() throws SQLException
	{
		checkFreed();
		return data.length();
	}

	@Override
	public String getSubString(final long pos, final int length) throws SQLException
	{
		checkFreed();
		final int start = (int) (pos - 1);
		if ((start < 0) || (start > data.length()))
			throw new SQLException("Invalid clob position " + pos);
		final int end = Math.min(start + length, data.length());
		return data.substring(start, end);
	}

	@Override
	public Reader getCharacterStream() throws SQLException
	{
		checkFreed();
		return new StringReader(data);
	}

	@Override
	public Reader getCharacterStream(final long pos, final long length) throws SQLException
	{
		return getCharacterStream();
	}

	@Override
	public InputStream getAsciiStream() throws SQLException
	{
		checkFreed();
		return new ByteArrayInputStream(data.getBytes(StandardCharsets.US_ASCII));
	}

	@Override
	public long position(final String searchstr, final long start) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public long position(final Clob searchstr, final long start) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int setString(final long pos, final String str) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int setString(final long pos, final String str, final int offset, final int len) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public OutputStream setAsciiStream(final long pos) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public Writer setCharacterStream(final long pos) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void truncate(final long len) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void free() throws SQLException
	{
		data = null;
		freed = true;
	}
}
