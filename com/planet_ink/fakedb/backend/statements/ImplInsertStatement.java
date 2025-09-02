package com.planet_ink.fakedb.backend.statements;

import java.util.List;

import com.planet_ink.fakedb.backend.structure.FakeCondition;
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
 * Parameters to execute an insert statement
 *
 * @author Bo Zimmerman
 */
public class ImplInsertStatement extends ImplAbstractStatement
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


	public static ImplInsertStatement parse(String sql, final String[] token) throws java.sql.SQLException
	{
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("into"))
			throw new java.sql.SQLException("no into token");
		String[] r = parseVal(sql);
		sql = skipWS(r[0]);
		final String tableName = r[1].trim().toUpperCase();
		sql = skipWS(sql);
		if ((sql.length() <= 0) || (sql.charAt(0) != '('))
			throw new java.sql.SQLException("no open paren");
		sql = sql.substring(1);

		final java.util.List<String> columnList = new java.util.LinkedList<String>();
		while (true)
		{
			sql = skipWS(sql);
			int index = sql.indexOf(',');
			final int index2 = sql.indexOf(')');
			if ((index < 0) || (index2 < index))
				index = index2;
			if (index < 0)
				throw new java.sql.SQLException("no comma");
			columnList.add(sql.substring(0, index).trim());
			final char c = sql.charAt(index);
			sql = skipWS(sql.substring(index + 1));
			if (c == ')')
				break;
		}

		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("values"))
			throw new java.sql.SQLException("no values");
		sql = skipWS(sql);
		if ((sql.length() <= 0) || (sql.charAt(0) != '('))
			throw new java.sql.SQLException("no value open paren");
		sql = sql.substring(1);

		final java.util.List<String> valuesList = new java.util.LinkedList<String>();
		final java.util.List<Boolean> unPreparedValueList = new java.util.LinkedList<Boolean>();
		while (true)
		{
			sql = skipWS(sql);
			r = parseVal(sql);
			final String val = r[1];
			sql = skipWS(r[0]);
			valuesList.add(val);
			unPreparedValueList.add(Boolean.valueOf(r[2] != null));
			if (sql.length() == 0)
				throw new java.sql.SQLException("no sql again");
			final char c = sql.charAt(0);
			sql = skipWS(sql.substring(1));
			if (c == ')')
				break;
			if (c != ',')
				throw new java.sql.SQLException("no comma before last paren");
		}
		sql = skipWS(sql);
		if ((sql.length() > 0) && (sql.charAt(0) == ';'))
			sql = sql.substring(1);
		sql = skipWS(sql);
		if (sql.length() > 0)
			throw new java.sql.SQLException("no more sql or missing comma/paren");
		if (columnList.size() != valuesList.size())
			throw new java.sql.SQLException("column values mismatch");
		return new ImplInsertStatement(tableName, columnList.toArray(new String[0]), valuesList.toArray(new String[0]), unPreparedValueList.toArray(new Boolean[0]));
	}
}
