package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2010-2018 Bo Zimmerman

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

public class PaladinSkill extends StdAbility
{
	@Override
	public String ID()
	{
		return "PaladinSkill";
	}

	private final static String	localizedName	= CMLib.lang().L("Paladin Skill");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected Set<MOB>			paladinsGroup	= null;
	protected final List<MOB>	removeFromGroup	= new LinkedList<MOB>();

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL;
	}

	@Override
	public CMObject copyOf()
	{
		if(paladinsGroup==null)
			return super.copyOf();
		final PaladinSkill P=(PaladinSkill)super.copyOf();
		if(P==null)
			return super.copyOf();
		P.paladinsGroup=new HashSet<MOB>(paladinsGroup);
		return P;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return false;
		final MOB paladinMob=(invoker == null) ? (MOB)affected : invoker;
		if(paladinMob==null)
			return false;
		if(!(CMLib.flags().isGood(paladinMob)))
			return false;
		final Set<MOB> paladinsGroup=this.paladinsGroup;
		if(paladinsGroup!=null)
		{
			synchronized(paladinsGroup)
			{
				paladinsGroup.clear();
				paladinMob.getGroupMembers(paladinsGroup);
				removeFromGroup.clear();
				for(final MOB M : paladinsGroup)
				{
					if(M.location()!=paladinMob.location())
						removeFromGroup.add(M);
				}
				paladinsGroup.removeAll(removeFromGroup);
			}
		}
		if((CMLib.dice().rollPercentage()==1)
		&&(CMLib.dice().rollPercentage()<10))
			helpProficiency(paladinMob, 0);
		return true;
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		return super.autoInvocation(mob, force);
	}
}
