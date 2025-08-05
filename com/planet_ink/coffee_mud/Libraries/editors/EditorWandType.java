package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2008-2025 Bo Zimmerman

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
public class EditorWandType extends AbilityParmEditorImpl
{
	public EditorWandType()
	{
		super("WAND_TYPE",CMLib.lang().L("MagicT"),ParmType.CHOICES);
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return (o instanceof Wand) ? 2 : -1;
	}

	@Override
	public int minColWidth()
	{
		return 3;
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		if(oldVal==null)
			return false;
		if(oldVal.length()==0)
			return true;
		return super.confirmValue(oldVal);
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		if((oldVal==null) || (oldVal.length()==0))
			return new String[] { "ANY" };
		return new String[] { oldVal };
	}

	@Override
	public void createChoices()
	{
		choices = new PairVector<String,String>();
		choices.add("ANY", "Any");
		for(final String[] set : Wand.WandUsage.WAND_OPTIONS)
			choices.add(set[0], CMStrings.capitalizeAllFirstLettersAndLower(set[1]));
	}

	@Override
	public String defaultValue()
	{
		return "ANY";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		if(I instanceof Wand)
		{
			final int ofType=((Wand)I).getEnchantType();
			if((ofType<0)||(ofType>Ability.ACODE.DESCS_.size()))
				return "ANY";
			return Ability.ACODE.DESCS_.get(ofType);
		}
		return "ANY";
	}
}
