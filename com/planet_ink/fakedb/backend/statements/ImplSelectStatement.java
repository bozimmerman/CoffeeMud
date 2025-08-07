package com.planet_ink.fakedb.backend.statements;

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
}
