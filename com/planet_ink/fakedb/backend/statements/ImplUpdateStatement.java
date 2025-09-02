package com.planet_ink.fakedb.backend.statements;

import java.util.ArrayList;
import java.util.List;

import com.planet_ink.fakedb.backend.jdbc.Statement;
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
 * Parameters to execute an update statement
 *
 * @author Bo Zimmerman
 */
public class ImplUpdateStatement extends ImplAbstractStatement
{
	public ImplUpdateStatement(final String tableName, final List<FakeCondition> conditions, final String[] columns, final String[] sqlValues, final Boolean[] unPreparedValues)
	{
		this.tableName = tableName;
		this.columns = columns;
		this.sqlValues = sqlValues;
		this.conditions = conditions;
		this.unPreparedValues = unPreparedValues;
	}

	public final String					tableName;
	public final String[]				columns;
	public final String[]				sqlValues;
	public final Boolean[]				unPreparedValues;
	public final List<FakeCondition>	conditions;

	@Override
	public final String[] values()
	{
		return sqlValues;
	}

	@Override
	public final List<FakeCondition> conditions()
	{
		return conditions;
	}

	@Override
	public final Boolean[] unPreparedValuesFlags()
	{
		return unPreparedValues;
	}

	@Override
	public final StatementType getStatementType()
	{
		return StatementType.UPDATE;
	}

	public static ImplUpdateStatement parse(final Statement stmt, String sql, final String[] token) throws java.sql.SQLException
	{
		String[] r = parseVal(sql);
		sql = skipWS(r[0]);
		final String tableName = r[1].trim().toUpperCase();
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("set"))
			throw new java.sql.SQLException("no set");
		final java.util.List<String> columnList = new java.util.LinkedList<String>();
		final java.util.List<String> valueList = new java.util.LinkedList<String>();
		final java.util.List<Boolean> unPreparedValueList = new java.util.LinkedList<Boolean>();
		final StringBuffer buffer = new StringBuffer();
		while (true)
		{
			sql = skipWS(sql);
			buffer.setLength(0);
			while (sql.length() > 0)
			{
				final char c = sql.charAt(0);
				if ((c == '=') || (c == ' '))
					break;
				buffer.append(c);
				sql = sql.substring(1);
			}
			sql = skipWS(sql);
			final String attr = buffer.toString();
			if((sql.length() == 0)||(sql.equals(";")))
			{
				final List<FakeCondition> conditions = new ArrayList<FakeCondition>();
				return new ImplUpdateStatement(tableName, conditions, columnList.toArray(new String[0]), valueList.toArray(new String[0]), unPreparedValueList.toArray(new Boolean[0]));
			}
			if (sql.charAt(0) != '=')
			{
				if (!attr.equalsIgnoreCase("where"))
					throw new java.sql.SQLException("no where");
				break;
			}
			sql = skipWS(sql.substring(1));
			if (sql.length() == 0)
				throw new java.sql.SQLException("no no sql no mo");
			buffer.setLength(0);
			if (sql.charAt(0) == '\'')
			{
				int sub = 1;
				for (; sub < sql.length(); sub++)
				{
					char c = sql.charAt(sub);
					if (c == '\'')
						break;
					if (c == '\\')
						c = sql.charAt(++sub);
					buffer.append(c);
				}
				columnList.add(attr);
				valueList.add(buffer.toString());
				sql = sql.substring(sub + 1);
				unPreparedValueList.add(Boolean.valueOf(false));
			}
			else
			{
				r = parseVal(sql);
				sql = r[0];
				columnList.add(attr);
				valueList.add(r[1]);
				unPreparedValueList.add(Boolean.valueOf(r[2] != null));
			}
			sql = skipWS(sql);
			if (sql.length() > 0)
			{
				if (sql.charAt(0) == ',')
					sql = skipWS(sql.substring(1));
				else
				if(sql.charAt(0) == ';')
				{
					sql = skipWS(sql.substring(1));
					break;
				}
			}
		}
		final List<FakeCondition> conditions = new ArrayList<FakeCondition>();
		if(sql.length()>0)
		{
			sql = stmt.parseWhereClause(tableName, sql, conditions);
			if (conditions.size() == 0)
				throw new java.sql.SQLException("no more where clause!");
		}
		sql = skipWS(sql);
		if ((sql.length() > 0) && (sql.charAt(0) == ';'))
			sql = sql.substring(1);
		sql = skipWS(sql);
		if (sql.length() > 0)
			throw new java.sql.SQLException("no more sql or missing comma/paren");
		return new ImplUpdateStatement(tableName, conditions, columnList.toArray(new String[0]), valueList.toArray(new String[0]), unPreparedValueList.toArray(new Boolean[0]));

	}

}
