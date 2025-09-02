package com.planet_ink.fakedb.backend.statements;

import java.util.List;

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
 * Parameters to execute an create statement
 * ext values are 6 indx: 0=col name, 1=type, 2=key, 3=null, 4=size, 5=default
 *
 * @author Bo Zimmerman
 */
public class ImplCreateStatement extends ImplAbstractStatement
{
	final FakeColumn[] columns;

	public ImplCreateStatement(final String tableName, final FakeColumn[] columns)
	{
		this.tableName = tableName;
		this.columns = columns;
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

	/**
	 *  ext values are 6 indx: 0=col name, 1=type, 2=key, 3=null, 4=size, 5=default
	 * @return the columns
	 */
	@Override
	public Object[] extValues()
	{
		return columns;
	}

	@Override
	public final StatementType getStatementType()
	{
		return StatementType.CREATE;
	}

	public static ImplCreateStatement parse(String sql, final String[] token) throws java.sql.SQLException
	{
		sql = split(sql, token);
		if (!token[0].equalsIgnoreCase("table"))
			throw new java.sql.SQLException("no table token");
		String[] r = parseVal(sql);
		sql = skipWS(r[0]);
		final String tableName = r[1].trim().toUpperCase();
		sql = skipWS(sql);
		if ((sql.length() <= 0) || (sql.charAt(0) != '('))
			throw new java.sql.SQLException("no open paren");
		sql = sql.substring(1);

		final java.util.List<FakeColumn> columnList = new java.util.LinkedList<FakeColumn>();
		while (true)
		{
			sql = skipWS(sql);
			if(sql.length()==0)
				throw new java.sql.SQLException("Unexpected end of columns.");
			r = parseVal(sql);
			String val = r[1].trim();
			sql = skipWS(r[0]);
			if(val.equalsIgnoreCase("PRIMARY"))
			{
				r = parseVal(sql);
				val = r[1].trim();
				sql = skipWS(r[0]);
				if(!val.equalsIgnoreCase("KEY"))
					throw new java.sql.SQLException("Illegal column name: PRIMARY");
				if ((sql.length() <= 0) || (sql.charAt(0) != '('))
					throw new java.sql.SQLException("no open paren for keys");
				while(true)
				{
					r = parseVal(sql);
					final String potKey = r[1].trim();
					sql = skipWS(r[0]);
					boolean found = false;
					for(final FakeColumn col : columnList)
					{
						if(col.name.equalsIgnoreCase(potKey))
						{
							col.keyNumber=1;
							found=true;
						}
					}
					if(!found)
						throw new java.sql.SQLException("Unknown column "+potKey+" can not be key.");
					if(sql.length()==0)
						throw new java.sql.SQLException("no close paren/comma for keys");
					if(sql.charAt(0)==')')
					{
						sql=sql.substring(1);
						sql = skipWS(sql);
						break;
					}
					if(sql.charAt(0)==',')
					{
						sql=sql.substring(1);
						sql = skipWS(sql);
					}
					else
						throw new java.sql.SQLException("illegal char, not paren/comma for keys");
				}
				if ((sql.length() <= 0) || (sql.charAt(0) != ','))
					throw new java.sql.SQLException("no close paren for columns");
				continue;
			}
			final FakeColumn col = new FakeColumn();
			// * ext values are 6 indx: 0=col name, 1=type, 2=key, 3=null, 4=size, 5=default
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
			boolean exit = false;
			boolean not=false;
			col.canNull = true;
			while(true)
			{
				if(sql.length()==0)
					throw new java.sql.SQLException("Unexpected end of list.");
				if(sql.startsWith(","))
				{
					sql=sql.substring(1);
					sql = skipWS(sql);
					break;
				}
				if(sql.startsWith(")"))
				{
					sql=sql.substring(1);
					sql = skipWS(sql);
					exit=true;
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
					if(parm.equals("NOT"))
						not=true;
					if(parm.equals("NULL"))
						col.canNull = !not;
					if(parm.equals("DEFAULT")&&(col.defaultValue==null))
					{
						r = parseVal(sql);
						parm = r[1].toUpperCase().trim();
						sql = skipWS(r[0]);
						col.defaultValue=parm;
					}
				}
			}
			columnList.add(col);
			if(exit)
				break;
		}

		sql = skipWS(sql);
		if ((sql.length() > 0) && (sql.charAt(0) == ';'))
			sql = sql.substring(1);
		sql = skipWS(sql);
		if (sql.length() > 0)
			throw new java.sql.SQLException("no more sql or missing comma/paren");
		return new ImplCreateStatement(tableName, columnList.toArray(new FakeColumn[0]));
	}
}
