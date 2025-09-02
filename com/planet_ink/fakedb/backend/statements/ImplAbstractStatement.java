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
 * For prepared statements, an abstract way into things
 *
 * @author Bo Zimmerman
 */
public abstract class ImplAbstractStatement
{
	public static enum StatementType
	{
		SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER
	}

	public abstract String[] values();

	public abstract Boolean[] unPreparedValuesFlags();

	public abstract List<FakeCondition> conditions();

	public Object[] extValues()
	{
		return null;
	}

	public StatementType getSubStatementType()
	{
		return null;
	}

	public abstract StatementType getStatementType();

	protected static String split(String sql, final String[] token)
	{
		while (true)
		{
			if (sql.length() == 0)
			{
				token[0] = "";
				return "";
			}
			if (sql.charAt(0) == ' ')
			{
				sql = sql.substring(1);
				continue;
			}
			if (sql.charAt(0) == ';')
			{
				sql = sql.substring(1);
				continue;
			}
			int index;
			for (index = 0; index < sql.length(); index++)
			{
				char c = sql.charAt(index);
				if (c == ' ')
				{
					break;
				}
				else
				if (c == ';')
				{
					break;
				}
				else
				if (c == '\'')
				{
					for (++index; index < sql.length(); index++)
					{
						c = sql.charAt(index);
						if (c == '\\')
							index++;
						else
						if (c == '\'')
							break;
					}
				}
			}
			if (index >= sql.length())
			{
				token[0] = sql;
				return "";
			}
			token[0] = sql.substring(0, index);
			return sql.substring(index + 1);
		}
	}

	protected static String splitColumns(final String sql, final List<String> cols)
	{
		int s = 0;
		while ((sql.length() > 0) && (s < sql.length()))
		{
			if ((s < sql.length()) && ((sql.charAt(s) == ' ') || (sql.charAt(s) == '\t')))
				s++;
			if (s >= sql.length())
				return "";
			int e = s;
			while ((e < sql.length())
			&& (sql.charAt(e) != ' ')
			&& (sql.charAt(e) != '\t')
			&& (sql.charAt(e) != ','))
				e++;
			if (e >= sql.length()) // was whatever it was the last word.. done
				return sql.substring(s);
			final String word = sql.substring(s, e);
			cols.add(word);
			if (sql.charAt(e) != ',')
			{
				while ((e < sql.length())
				&& ((sql.charAt(e) == ' ') || (sql.charAt(e) == '\t')))
					e++;
			}
			if ((e >= sql.length()) || (sql.charAt(e) != ','))
				return sql.substring(e);
			while (sql.charAt(e) == ',')
				e++;
			s = e;
		}
		return "";
	}

	protected static String skipWS(final String sql)
	{
		int index;
		for (index = 0; index < sql.length(); index++)
		{
			final char c = sql.charAt(index);
			if ((c != ' ') && (c != '\t') && (c != '\r') && (c != '\n'))
				break;
		}
		if (index == 0)
			return sql;
		return sql.substring(index);
	}

	protected static String[] parseVal(String sql)
	{
		final String[] result = new String[3];
		sql = skipWS(sql);
		if (sql.length() == 0)
		{
			result[0] = result[1] = "";
			result[2] = null;
		}
		else
		if (sql.charAt(0) == '\'')
		{
			final StringBuffer buffer = new StringBuffer();
			int index;
			for (index = 1; index < sql.length(); ++index)
			{
				char c = sql.charAt(index);
				if (c == '\'')
					break;
				if (c == '\\')
					c = sql.charAt(++index);
				buffer.append(c);
			}
			if (index >= sql.length())
				index = sql.length() - 1;
			result[0] = sql.substring(index + 1);
			result[1] = buffer.toString();
			result[2] = null;
		}
		else
		{
			final StringBuffer buffer = new StringBuffer();
			int index;
			for (index = 0; index < sql.length(); ++index)
			{
				final char c = sql.charAt(index);
				if ((c == ' ') || (c == ',') || (c == ')')|| (c == '(') || ((c == ';')&&(index>0)))
					break;
				buffer.append(c);
			}
			result[0] = sql.substring(index);
			result[1] = buffer.toString();
			result[2] = buffer.toString().equals("?") ? "" : null;
		}
		return result;
	}

}
