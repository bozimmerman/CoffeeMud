package com.planet_ink.fakedb.backend.structure;
/*
Copyright 2001 Thomas Neumann
Copyright 2004-2026 Bo Zimmerman

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
*
*/
public class FakeColumn
{
	public static enum FakeColType
	{
		UNKNOWN,
		INTEGER,
		STRING,
		LONG,
		DATETIME,
		CLOB,
		BLOB
		;
		public int getSQLType()
		{
			switch(this)
			{
			case BLOB: return java.sql.Types.BLOB;
			case CLOB: return java.sql.Types.CLOB;
			case DATETIME: return java.sql.Types.TIMESTAMP;
			case INTEGER: return java.sql.Types.INTEGER;
			case LONG: return java.sql.Types.BIGINT;
			case STRING: return java.sql.Types.VARCHAR;
			case UNKNOWN:
			default:
				return java.sql.Types.OTHER;
			}
		}
	}

	public String		name		= "undefined";
	public FakeColType	type		= FakeColType.UNKNOWN;
	public int			size		= Integer.MAX_VALUE;
	public boolean		canNull		= false;
	public int			keyNumber	= -1;
	public int			indexNumber	= -1;
	public int			version		= 1;
	public int			valueOffset	= 0;
	public String		defaultValue= null;
	public String		tableName;

	public int getStoreValueWidth()
	{
		switch(type)
		{
		case BLOB:
		case CLOB:
			return 38; // 1 for type marker, 36 for uuid, +1 \n
		case INTEGER:
			return 12; // 11 for int, +1 \n
		case DATETIME:
		case LONG:
			return 20; // 19 for long, +1 \n
		case STRING:
			if(size == Integer.MAX_VALUE)
				throw new IllegalArgumentException("Column " + name + " of table " + tableName + " has no size.");
			return size + 4; // +3 len, +str, +1 \n
		case UNKNOWN:
		default:
			return 20; // 19 for long, +1 \n
		}
	}

	public boolean isBlobColumn()
	{
		return (type == FakeColType.BLOB) || (type == FakeColType.CLOB);
	}

	public static final int	INDEX_COUNT		= Integer.MAX_VALUE;
}
