package com.planet_ink.fakedb;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

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
public class Driver implements java.sql.Driver
{
	static
	{
		try
		{
			java.sql.DriverManager.registerDriver(new Driver());
		}
		catch (final java.sql.SQLException E)
		{
			E.printStackTrace();
		}
	}

	public Driver()
	{
	}

	@Override
	public synchronized java.sql.Connection connect(String url, Properties info) throws java.sql.SQLException
	{
		final Properties p = parseUrl(url, info);
		if (p == null)
			return null;
		return new Connection(p.getProperty("PATH"));
	}

	@Override
	public synchronized boolean acceptsURL(String url) throws java.sql.SQLException
	{
		return parseUrl(url, null) != null;
	}

	@Override
	public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws java.sql.SQLException
	{
		return new java.sql.DriverPropertyInfo[0];
	}

	@Override
	public int getMajorVersion()
	{
		return 3;
	}

	@Override
	public int getMinorVersion()
	{
		return 0;
	}

	@Override
	public boolean jdbcCompliant()
	{
		return false;
	}

	private Properties parseUrl(String url, Properties defaults)
	{
		if (!url.startsWith("jdbc:fakedb:"))
			return null;

		String path = url.substring(12);
		if ((path.length() > 0) && (!path.endsWith(java.io.File.separator)))
			path = path + java.io.File.separator;

		final Properties result = new Properties(defaults);
		result.put("PATH", path.replace('/', java.io.File.separatorChar));

		return result;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		throw new SQLFeatureNotSupportedException();
	}
}
