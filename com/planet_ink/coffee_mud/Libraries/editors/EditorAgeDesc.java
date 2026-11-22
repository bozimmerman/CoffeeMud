package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class EditorAgeDesc extends AbilityParmEditorImpl
{
	public EditorAgeDesc()
	{
		super("AGE_DESC",CMLib.lang().L("Age Description"),ParmType.CHOICES);
	}

	@Override
	public void createChoices()
	{
		createChoices(Race.AGE_DESCS);
	}

	@Override
	public boolean confirmValue(final String oldVal, final Map<String,String> args)
	{
		if((oldVal == null)||(oldVal.trim().length()==0))
			return true;
		// an age description may be written short, as in young for Young adult
		final String val = oldVal.trim().toUpperCase();
		for(final String desc : Race.AGE_DESCS)
		{
			final String upper = desc.toUpperCase();
			if((upper.startsWith(val))||(val.startsWith(upper)))
				return true;
		}
		return false;
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public String convertFromItem(final ItemCraftor C, final Item I)
	{
		return "";
	}
}
