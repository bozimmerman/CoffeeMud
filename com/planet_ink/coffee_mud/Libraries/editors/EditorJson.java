package com.planet_ink.coffee_mud.Libraries.editors;

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

import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.ParmType;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.MiniJSON;

public class EditorJson extends AbilityParmEditorImpl
{
	public EditorJson()
	{
		super("JSON",CMLib.lang().L("JSON"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		if((oldVal == null)||(oldVal.trim().length()==0))
			return false;
		try
		{
			new MiniJSON().parse(oldVal.trim());
			return true;
		}
		catch(final MiniJSON.MJSONException e)
		{
			return false;
		}
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "{}";
	}

	@Override
	public String defaultValue()
	{
		return "{}";
	}
}
