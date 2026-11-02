package com.planet_ink.coffee_mud.Libraries.editors;

import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.ParmType;
import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Abilities.interfaces.ItemCraftor;
import com.planet_ink.coffee_mud.Items.interfaces.Item;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMath;

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
public class EditorCanDescs extends AbilityParmEditorImpl
{
	public EditorCanDescs()
	{
		super("CAN_DESCS",CMLib.lang().L("Can Descs"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
		createChoices(Ability.CAN_DESCS);
	}

	@Override
	public boolean confirmValue(final String oldVal, final Map<String,String> parameterArgs)
	{
		if((oldVal == null)||(oldVal.trim().length()==0))
			return true;
		final List<String> allowed = new ArrayList<String>();
		for(final String code : Ability.CAN_DESCS)
			allowed.add(code);
		String plusSStr = null;
		if(parameterArgs != null)
		{
			final String add = parameterArgs.get("ADD");
			if((add != null)&&(add.trim().length()>0))
				allowed.addAll(CMParms.parseCommas(add.toUpperCase().trim(),true));
			plusSStr = parameterArgs.get("PLURAL");
		}
		final boolean plusS = (plusSStr != null)&&(CMath.s_bool(plusSStr));
		for(final String code : CMParms.parseCommas(oldVal.toUpperCase().trim(),true))
		{
			boolean found = false;
			for(final String allow : allowed)
			{
				if(allow.equalsIgnoreCase(code))
				{
					found = true;
					break;
				}
			}
			if((!found)&&(plusS))
			{
				for(final String allow : allowed)
				{
					if(allow.equalsIgnoreCase(code+"S"))
					{
						found = true;
						break;
					}
				}
			}
			if(!found)
				return false;
		}
		return true;
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
