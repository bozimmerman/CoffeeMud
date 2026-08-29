package com.planet_ink.fakedb.backend.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
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
 * Minimal read-only Blob implementation backed by a byte array.
 */
public class FakeBlob implements Blob
{
	private byte[]	data;
	private boolean	freed	= false;

	public FakeBlob(final byte[] data)
	{
		this.data = (data == null) ? new byte[0] : data;
	}

	private void checkFreed() throws SQLException
	{
		if (freed)
			throw new SQLException("Blob has already been freed");
	}

	@Override
	public long length() throws SQLException
	{
		checkFreed();
		return data.length;
	}

	@Override
	public byte[] getBytes(final long pos, final int length) throws SQLException
	{
		checkFreed();
		final int start = (int) (pos - 1);
		if ((start < 0) || (start > data.length))
			throw new SQLException("Invalid blob position " + pos);
		final int len = Math.min(length, data.length - start);
		final byte[] out = new byte[len];
		System.arraycopy(data, start, out, 0, len);
		return out;
	}

	@Override
	public InputStream getBinaryStream() throws SQLException
	{
		checkFreed();
		return new ByteArrayInputStream(data);
	}

	@Override
	public InputStream getBinaryStream(final long pos, final long length) throws SQLException
	{
		return getBinaryStream();
	}

	@Override
	public long position(final byte[] pattern, final long start) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public long position(final Blob pattern, final long start) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int setBytes(final long pos, final byte[] bytes) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int setBytes(final long pos, final byte[] bytes, final int offset, final int len) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public OutputStream setBinaryStream(final long pos) throws SQLException
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
