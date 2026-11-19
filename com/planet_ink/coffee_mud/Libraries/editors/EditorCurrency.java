package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;

import java.util.*;

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

/**
 * The CURRENCY editor picks from the currency codes this mud defines, the
 * way Behaviors/MoneyChanger.java and other money handling parms name them.
 * Matching is case-insensitive, since CMLib.beanCounter() uppercases the
 * code before looking it up.
 */
public class EditorCurrency extends AbilityParmEditorImpl
{
	public EditorCurrency()
	{
		super("CURRENCY",CMLib.lang().L("Currency"),ParmType.CHOICES);
	}

	@Override
	public void createChoices()
	{
		createChoices(CMLib.beanCounter().getAllCurrencies());
	}

	@Override
	public boolean confirmValue(final String oldVal, final Map<String,String> args)
	{
		if((oldVal==null)||(oldVal.trim().length()==0))
			return true;
		return CMLib.beanCounter().getCurrencySet(oldVal.trim())!=null;
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}
}
