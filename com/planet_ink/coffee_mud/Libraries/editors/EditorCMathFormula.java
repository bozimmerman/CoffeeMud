package com.planet_ink.coffee_mud.Libraries.editors;

import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.ParmType;

import java.util.Map;

import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;

/*
   Copyright 2026-2026 Bo Zimmerman

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
public class EditorCMathFormula extends AbilityParmEditorImpl
{
	public EditorCMathFormula()
	{
		super("CMATH_FORMULA",CMLib.lang().L("Formula"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public boolean confirmValue(final String oldVal, Map<String, String> args)
	{
		if((oldVal == null)||(oldVal.trim().length()==0))
			return false;
		if(oldVal.indexOf("@x1")<0)
			return false;
		try
		{
			CMath.compileMathExpression(oldVal);
			return true;
		}
		catch(final ArithmeticException e)
		{
			return false;
		}
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}

	@Override
	public String defaultValue()
	{
		return "";
	}
}
