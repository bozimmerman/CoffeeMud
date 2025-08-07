package com.planet_ink.fakedb.backend.structure;

import java.util.List;

import com.planet_ink.fakedb.backend.Backend;
import com.planet_ink.fakedb.backend.Backend.ConnectorType;
import com.planet_ink.fakedb.backend.structure.FakeColumn.FakeColType;
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
*
* @author Bo Zimmerman
*
*/
public class FakeCondition
{
	public int					conditionIndex;
	public ComparableValue		conditionValue;
	public String				lowStr		= null;
	public boolean				like		= false;
	public boolean				eq			= false;
	public boolean				lt			= false;
	public boolean				gt			= false;
	public boolean				not			= false;
	public boolean				unPrepared	= false;
	public FakeColType			colType		= FakeColType.UNKNOWN;
	public ConnectorType		connector	= ConnectorType.AND;
	public List<FakeCondition>	contains	= null;

	public boolean compareValue(ComparableValue subKey)
	{
		if (subKey == null)
			subKey = new ComparableValue(null);
		if (like && conditionValue.getValue() instanceof String)
		{
			if (lowStr == null)
				lowStr = ((String) conditionValue.getValue()).toLowerCase();
			boolean chk = false;
			if (lowStr.length() == 0)
				chk = conditionValue.equals(subKey);
			else
			if (subKey.equals(null) || (!(subKey.getValue() instanceof String)))
				chk = false;
			else
			{
				final String s = ((String) subKey.getValue()).toLowerCase();
				final int x = lowStr.indexOf('%');
				if ((x < 0) || (lowStr.length() == 1))
					chk = lowStr.equals(s);
				else
				if (x == 0)
				{
					if (lowStr.charAt(lowStr.length() - 1) == '%')
						chk = (s.indexOf(lowStr.substring(1, lowStr.length() - 1)) >= 0);
					else
						chk = s.startsWith(lowStr.substring(1));
				}
				else
				if (x==lowStr.length() - 1)
					chk = s.endsWith(lowStr.substring(0, lowStr.length() - 1));
				else
					chk = s.startsWith(lowStr.substring(0, x)) && s.endsWith(lowStr.substring(x + 1));
			}
			return not ? !chk : chk;
		}
		final int sc = (lt || gt) ? subKey.compareTo(conditionValue) : 0;
		if (!(((eq) && (subKey.equals(conditionValue))) || ((lt) && (sc < 0)) || ((gt) && (sc > 0))))
			return not;
		return !not;
	}
}
