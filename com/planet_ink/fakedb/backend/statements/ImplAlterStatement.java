package com.planet_ink.fakedb.backend.statements;

import java.util.List;

import com.planet_ink.fakedb.backend.statements.ImplAbstractStatement.StatementType;
import com.planet_ink.fakedb.backend.structure.FakeColumn;
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
public class ImplAlterStatement extends ImplAbstractStatement
{
	public ImplAlterStatement(final String tableName, final StatementType subType, final String objType, final String[] changes, final FakeColumn col)
	{
		this.tableName = tableName;
		this.subType = subType;
		this.objType = objType;
		this.col = col;
		this.changes=changes;
	}

	public final String			tableName;
	public final StatementType 	subType;
	public final String 		objType;
	public final FakeColumn		col;
	public final String[]		changes;
	private final Boolean[]		unPreparedValues	= new Boolean[0];

	@Override
	public final Boolean[] unPreparedValuesFlags()
	{
		return unPreparedValues;
	}

	@Override
	public final String[] values()
	{
		return changes;
	}

	@Override
	public final List<FakeCondition> conditions()
	{
		return null;
	}

	@Override
	public Object[] extValues()
	{
		return new Object[] {objType, col};
	}

	@Override
	public StatementType getSubStatementType()
	{
		return subType;
	}

	@Override
	public final StatementType getStatementType()
	{
		return StatementType.DROP;
	}

	public static ImplAlterStatement parse(String sql, final String[] token) throws java.sql.SQLException
	{
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("table"))
			throw new java.sql.SQLException("no table token");
		String[] r = parseVal(sql);
		sql = skipWS(r[0]);
		final String tableName = r[1].trim().toUpperCase();
		sql = skipWS(sql);
		sql = split(sql, token);
		final String action = token[0].toUpperCase();
		if((!action.equals("ADD"))
		&&(!action.equals("ALTER"))
		&&(!action.equals("MODIFY"))
		&&(!action.equals("DROP")))
			throw new java.sql.SQLException("no add/alter/modify/drop token");
		String what="";
		if(sql.length()>10)
		{
			final String next = sql.substring(0,11).toUpperCase();
			if(next.startsWith("PRIMARY "))
			{
				sql=sql.substring(8);
				sql=skipWS(sql);
				what="PRIMARY";
				if(sql.length()>4)
				{
					if(sql.substring(0,4).toUpperCase().startsWith("KEY "))
					{
						sql=sql.substring(3);
						sql=skipWS(sql);
					}
					else
						throw new java.sql.SQLException("no key token");
				}
			}
			else
			if(next.startsWith("UNIQUE "))
			{
				sql=sql.substring(6);
				sql=skipWS(sql);
				what="PRIMARY";
				if(sql.length()>4)
				{
					if(sql.substring(0,4).toUpperCase().startsWith("KEY "))
					{
						sql=sql.substring(3);
						sql=skipWS(sql);
					}
					else
					if(sql.substring(0,6).toUpperCase().startsWith("INDEX "))
					{
						sql=sql.substring(6);
						sql=skipWS(sql);
					}
					else
						throw new java.sql.SQLException("no key token");
				}
			}
			else
			if(next.startsWith("COLUMN "))
			{
				sql=sql.substring(6);
				sql=skipWS(sql);
				what="COLUMN";
			}
			else
			if(next.startsWith("CONSTRAINT "))
			{
				sql=sql.substring(10);
				sql=skipWS(sql);
				what="INDEX";
			}
			else
			if(next.startsWith("INDEX "))
			{
				sql=sql.substring(5);
				sql=skipWS(sql);
				what="INDEX";
			}
		}
		if(action.equals("DROP"))
		{
			if ((!what.equals("COLUMN")) && (!what.equals("PRIMARY")) && (!what.equals("INDEX")))
				throw new java.sql.SQLException("no column/primary token");
			sql = skipWS(sql);
			r = parseVal(sql);
			final String name = r[1].toUpperCase().trim();
			sql = skipWS(r[0]);
			if ((sql.length() > 0) && (sql.charAt(0) == ';'))
				sql = skipWS(sql.substring(1));
			return new ImplAlterStatement(tableName, StatementType.DROP, what, new String[] {name}, null);
		}
		if(action.equals("ADD"))
		{
			if ((what.equals("PRIMARY")) || (what.equals("INDEX")))
			{
				sql = skipWS(sql);
				if(!sql.startsWith("("))
					throw new java.sql.SQLException("no opening paren");
				sql=skipWS(sql.substring(1));
				final List<String> cols = new java.util.ArrayList<String>();
				r = parseVal(sql);
				String name = r[1].toUpperCase().trim();
				cols.add(name);
				sql = skipWS(r[0]);
				while(true)
				{
					sql=skipWS(sql);
					if(sql.startsWith(")"))
					{
						sql = sql.substring(1);
						sql = skipWS(sql);
						break;
					}
					if (!sql.startsWith(","))
						throw new java.sql.SQLException("no comma or closing paren");
					sql=skipWS(sql.substring(1));
					r = parseVal(sql);
					name = r[1].toUpperCase().trim();
					sql = skipWS(r[0]);
					cols.add(name);
				}
				sql = skipWS(sql);
				if ((sql.length() > 0) && (sql.charAt(0) == ';'))
					sql = skipWS(sql.substring(1));
				return new ImplAlterStatement(tableName, StatementType.CREATE, what, cols.toArray(new String[0]), null);
			}
		}
		if(action.equals("MODIFY")||action.equals("ALTER"))
		{
			if (!what.equals("COLUMN"))
				throw new java.sql.SQLException("no column token");
			sql = skipWS(sql);
		}
		boolean openingParen = false;
		if ((sql.length() > 0) && (sql.charAt(0) == '('))
		{
			openingParen = true;
			sql = skipWS(sql.substring(1));
		}

		r = parseVal(sql);
		final String val = r[1].trim();
		sql = skipWS(r[0]);
		final FakeColumn col = new FakeColumn();
		if((val.length()==0)||(!Character.isLetter(val.charAt(0))))
			throw new java.sql.SQLException("Illegal column name: " + val);
		col.name = val.toUpperCase().trim();
		r=parseVal(sql);
		final String type = r[1].trim();
		sql = skipWS(r[0]);
		try
		{
			col.type =  FakeColumn.FakeColType.valueOf(type.toUpperCase().trim());
		}
		catch(final Exception e)
		{
			throw new java.sql.SQLException("Illegal column type: " + val);
		}
		boolean not=false;
		while(true)
		{
			if(sql.length()==0)
				throw new java.sql.SQLException("Unexpected end of list.");
			if(sql.startsWith(",")||sql.startsWith(";"))
				break;
			if(openingParen && sql.startsWith(")"))
			{
				sql=sql.substring(1);
				sql = skipWS(sql);
				break;
			}
			if(sql.startsWith("("))
			{
				final int x = sql.indexOf(")");
				if(x<0)
					throw new java.sql.SQLException("Unclosed size expression.");
				final String size = sql.substring(1,x);
				try
				{
					col.size=Integer.parseInt(size);
				}
				catch(final Exception e)
				{
					throw new java.sql.SQLException("Illegal size expression.");
				}
				sql=sql.substring(x+1);
				sql = skipWS(sql);
			}
			else
			{
				r = parseVal(sql);
				String parm = r[1].toUpperCase().trim();
				sql = skipWS(r[0]);
				if(parm.equals("PRIMARY")||parm.equals("KEY"))
					col.keyNumber=1;
				else
				if(parm.equals("NOT"))
					not=true;
				else
				if(parm.equals("NULL"))
					col.canNull = !not;
				else
				if(parm.equals("DEFAULT")&&(col.defaultValue==null))
				{
					r = parseVal(sql);
					parm = r[1].toUpperCase().trim();
					sql = skipWS(r[0]);
					col.defaultValue=parm;
				}
				else
					throw new java.sql.SQLException("Unknown column attribute: "+parm+sql);
			}
		}
		sql = skipWS(sql);
		if ((sql.length() > 0) && (sql.charAt(0) == ';'))
			sql = sql.substring(1);
		sql = skipWS(sql);
		if (sql.length() > 0)
			throw new java.sql.SQLException("no more sql or missing comma/paren");
		if(action.equals("ADD"))
			return new ImplAlterStatement(tableName, StatementType.CREATE, what, new String[] {col.name}, col);
		if(action.equals("DROP"))
			return new ImplAlterStatement(tableName, StatementType.DROP, what, new String[] {col.name}, col);
		if (action.equals("MODIFY") || action.equals("ALTER"))
			return new ImplAlterStatement(tableName, StatementType.ALTER, what, new String[] { col.name }, col);
		throw new java.sql.SQLException("Unknown action: " + action);
	}
}
