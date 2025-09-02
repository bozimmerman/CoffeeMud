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
 * Parameters to execute an select statement
 *
 * @author Bo Zimmerman
 */
public class ImplSelectStatement extends ImplAbstractStatement
{
	public ImplSelectStatement(final Statement s, final String tableName, final List<String> cols, final List<FakeCondition> conditions, final String[] orderVars, final String[] orderModifiers)
	{
		this.s = s;
		this.tableName = tableName;
		this.cols = cols;
		this.conditions = conditions;
		this.orderVars = orderVars;
		this.orderModifiers = orderModifiers;
	}

	public Statement					s;
	public String						tableName;
	public List<String>					cols;
	public List<FakeCondition>			conditions;
	public String[]						orderVars;
	public String[]						orderModifiers;
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
		return conditions;
	}

	@Override
	public final StatementType getStatementType()
	{
		return StatementType.SELECT;
	}


	public static ImplSelectStatement parse(final Statement stmt, String sql, final String[] token) throws java.sql.SQLException
	{
		final List<String> cols = new ArrayList<String>();
		sql = splitColumns(sql, cols);
		if (cols.size() == 0)
			throw new java.sql.SQLException("no columns given");
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("from"))
			throw new java.sql.SQLException("no from clause");
		final String[] r = parseVal(sql);
		sql = skipWS(r[0]);
		final String tableName = r[1].trim().toUpperCase();
		final List<FakeCondition> conditions = new ArrayList<FakeCondition>();
		String[] orderVars = null;
		String[] orderConditions = null;
		if (sql.length() > 0)
		{
			sql = split(sql, token);
			if (token[0].equalsIgnoreCase("where"))
			{
				sql = stmt.parseWhereClause(tableName, sql, conditions);
				if (conditions.size() == 0)
					throw new java.sql.SQLException("no more where clause!");
				sql = split(sql, token);
			}
			if ((token[0] != null) && (token[0].equalsIgnoreCase("order")))
			{
				sql = split(sql, token);
				if (!token[0].equalsIgnoreCase("by"))
					throw new java.sql.SQLException("no by token");
				sql = split(sql, token);
				orderVars = new String[] { token[0] };
				orderConditions = new String[1];
				if (sql.length() > 0)
				{
					split(sql, token);
					if (token[0].equalsIgnoreCase("ASC") || token[0].equalsIgnoreCase("DESC"))
					{
						orderConditions = new String[] { token[0].toUpperCase().trim() };
						sql = split(sql, token);
					}
				}
			}
			sql = skipWS(sql);
			if ((sql.length() > 0) && (sql.charAt(0) == ';'))
				sql = sql.substring(1);
			sql = skipWS(sql);
			if (sql.length() > 0)
				throw new java.sql.SQLException("no more sql or missing comma/paren");
		}
		return new ImplSelectStatement(stmt, tableName, cols, conditions, orderVars, orderConditions);
	}
}
