package com.planet_ink.fakedb.backend.structure;

import java.util.List;
import java.util.regex.Pattern;

import com.planet_ink.fakedb.backend.Backend;
import com.planet_ink.fakedb.backend.Backend.ConnectorType;
import com.planet_ink.fakedb.backend.structure.FakeColumn.FakeColType;
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
			if (subKey.getValue() instanceof String)
			{
				final String s = ((String) subKey.getValue()).toLowerCase();
				chk = likeMatches(lowStr, s);
			}
			return not ? !chk : chk;
		}
		final int sc = (lt || gt) ? subKey.compareTo(conditionValue) : 0;
		if (!(((eq) && (subKey.equals(conditionValue))) || ((lt) && (sc < 0)) || ((gt) && (sc > 0))))
			return not;
		return !not;
	}

	/**
	 * SQL LIKE matching on a lower-cased pattern and value.  '%' matches any
	 * sequence (including empty) and '_' matches any single character.
	 */
	private static boolean likeMatches(final String pattern, final String s)
	{
		final StringBuilder re = new StringBuilder(pattern.length() + 8);
		for (int i = 0; i < pattern.length(); i++)
		{
			final char c = pattern.charAt(i);
			if (c == '%')
				re.append(".*");
			else
			if (c == '_')
				re.append('.');
			else
				re.append(Pattern.quote(String.valueOf(c)));
		}
		return Pattern.compile(re.toString()).matcher(s).matches();
	}
}
