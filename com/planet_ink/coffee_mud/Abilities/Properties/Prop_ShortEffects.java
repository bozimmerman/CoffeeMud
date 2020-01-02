package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Prop_ShortEffects extends Property
{
	@Override
	public String ID()
	{
		return "Prop_ShortEffects";
	}

	@Override
	public String name()
	{
		return "Short Effects";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	protected volatile int					lastNumEffects	= 0;
	protected int							maxTicks		= 3;
	protected MaskingLibrary.CompiledZMask	mask			= null;

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		int x=newMiscText.indexOf(';');
		maxTicks=3;
		mask=null;
		if((x>0)&&(CMath.isInteger(newMiscText.substring(0,x).trim())))
		{
			maxTicks = CMath.s_int(newMiscText.substring(0,x).trim());
			newMiscText=newMiscText.substring(x+1).trim();
		}
		if(newMiscText.trim().length()>0)
			mask=CMLib.masking().getPreCompiledMask(newMiscText);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		final Physical affected = this.affected;
		if(affected != null)
		{
			if(affected.numEffects() != this.lastNumEffects)
			{
				this.lastNumEffects = affected.numEffects();
				final Set<MOB> badInvokers = new HashSet<MOB>();
				if((affected instanceof Item)
				&&(((Item)affected).owner() instanceof MOB))
					badInvokers.add((MOB)((Item)affected).owner());
				else
				if(affected instanceof MOB)
					((MOB)affected).getGroupMembers(badInvokers);
				for(final Enumeration<Ability> a=affected.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&(A.canBeUninvoked())
					&&((A.invoker()==null)||(!badInvokers.contains(A.invoker())))
					&&((mask==null)||(CMLib.masking().maskCheck(mask, A, true))))
					{
						if(CMath.s_int(A.getStat("TICKDOWN"))>maxTicks)
							A.setStat("TICKDOWN", ""+maxTicks);
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}
}

