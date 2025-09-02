package com.planet_ink.fakedb.backend.statements;

import java.util.List;

import com.planet_ink.fakedb.backend.structure.FakeCondition;
/*
Copyright 2001 Thomas Neumann
Copyright 2025-2025 Bo Zimmerman

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
 * Parameters to execute an delete statement
 *
 * @author Bo Zimmerman
 */
public class ImplDropStatement extends ImplAbstractStatement
{
	public ImplDropStatement(final String tableName)
	{
		this.tableName = tableName;
	}

	public final String					tableName;
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
		return null;
	}

	@Override
	public final StatementType getStatementType()
	{
		return StatementType.DROP;
	}

	public static ImplDropStatement parse(String sql, final String[] token) throws java.sql.SQLException
	{
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("table"))
			throw new java.sql.SQLException("no table token");
		final String[] r = parseVal(sql);
		sql = skipWS(r[0]);
		final String tableName = r[1].trim().toUpperCase();
		sql = skipWS(sql);
		if ((sql.length() > 0) && (sql.charAt(0) == ';'))
			sql = sql.substring(1);
		sql = skipWS(sql);
		if (sql.length() > 0)
			throw new java.sql.SQLException("no more sql or missing comma/paren");
		return new ImplDropStatement(tableName);
	}


}
