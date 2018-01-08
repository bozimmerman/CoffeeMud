package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Common.CraftingSkill.CraftingActivity;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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

public class MasterDistilling extends Distilling
{
	private String	cookingID	= "";

	@Override
	public String ID()
	{
		return "MasterDistilling" + cookingID;
	}

	@Override
	public String name()
	{
		return L("Master Distilling" + cookingID);
	}

	private static final String[]	triggerStrings	= I(new String[] { "MDISTILLING", "MASTERDISTILLING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	protected List<String>	noUninvokes	= new ArrayList<String>(0);

	@Override
	protected List<String> getUninvokeException()
	{
		return noUninvokes;
	}

	@Override
	protected int getDuration(MOB mob, int level)
	{
		return getDuration(60,mob,1,8);
	}

	@Override
	protected int baseYield()
	{
		return 2;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		try
		{
			cookingID="";
			int num=1;
			while(mob.fetchEffect("MasterDistilling"+cookingID)!=null)
				cookingID=Integer.toString(++num);
			final List<String> noUninvokes=new Vector<String>(1);
			for(int i=0;i<mob.numEffects();i++)
			{
				final Ability A=mob.fetchEffect(i);
				if(((A instanceof MasterDistilling)||A.ID().equals("Distilling"))
				&&(noUninvokes.size()<5))
					noUninvokes.add(A.ID());
			}
			this.noUninvokes=noUninvokes;
			return super.invoke(mob, commands, givenTarget, auto, asLevel);
		}
		finally
		{
			cookingID="";
		}
	}
}
