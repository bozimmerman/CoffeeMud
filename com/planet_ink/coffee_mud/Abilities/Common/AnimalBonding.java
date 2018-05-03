package com.planet_ink.coffee_mud.Abilities.Common;
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
   Copyright 2015-2018 Bo Zimmerman

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

public class AnimalBonding extends CommonSkill
{
	@Override
	public String ID()
	{
		return "AnimalBonding";
	}

	private final static String	localizedName	= CMLib.lang().L("Animal Bonding");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ANIMALBOND", "ANIMALBONDING" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected MOB		 bonding	= null;
	protected boolean	 messedUp	= false;

	public AnimalBonding()
	{
		super();
		displayText=L("You are bonding...");
		verb=L("bonding");
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(text().length()==0)
		{
			if(canBeUninvoked())
			{
				if((affected!=null)
				&&(affected instanceof MOB)
				&&(tickID==Tickable.TICKID_MOB))
				{
					final MOB mob=(MOB)affected;
					if((bonding==null)||(mob.location()==null))
					{
						messedUp=true;
						unInvoke();
					}
					if(!mob.location().isInhabitant(bonding))
					{
						messedUp=true;
						unInvoke();
					}
				}
			}
			return super.tick(ticking,tickID);
		}
		return ! this.unInvoked;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if((bonding!=null)&&(!aborted))
				{
					MOB animal=bonding;
					if((messedUp)||(animal==null))
						commonTell(mob,L("You've failed to bond with @x1!",bonding.name()));
					else
					{
						if(animal.fetchEffect("AnimalBonding")!=null)
							commonTell(mob,L("@x1 is already bond.",bonding.name()));
						else
						{
							AnimalBonding bonding=(AnimalBonding)this.copyOf();
							bonding.setMiscText(mob.Name());
							bonding.canBeUninvoked = false;
							animal.addNonUninvokableEffect(bonding);
							mob.location().show(mob,null,getActivityMessageType(),L("<S-NAME> manage(s) to bond with @x1.",animal.name()));
						}
					}
				}
			}
		}
		super.unInvoke();
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((!super.canBeUninvoked) && (affected != null))
		{
			if((affected.fetchEffect(0)!=this)
			&&(affected instanceof MOB))
			{
				final MOB M=(MOB)affected;
				M.delEffect(this);
				M.addPriorityEffect(this);
				affected=M;
			}
			if((msg.target()==affected)
			&&(msg.source()!=affected)
			&&(msg.targetMinor()==CMMsg.TYP_ORDER)
			&&(msg.source().Name().equals(text())))
			{
				Language L=CMLib.utensils().getLanguageSpoken(affected);
				if((L!=null)&&(msg.tool()!=L))
					msg.setTool(L);
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		verb=L("bonding");
		bonding=null;
		final String str=CMParms.combine(commands,0);
		MOB M=super.getTarget(mob, commands, givenTarget);
		if(M==null)
			return false;
		bonding=null;
		if(!CMLib.flags().canBeSeenBy(M,mob))
		{
			commonTell(mob,L("You don't see anyone called '@x1' here.",str));
			return false;
		}
		if((!M.isMonster())
		   ||(!M.isMonster())
		   ||(!CMLib.flags().isAnimalIntelligence(M)))
		{
			commonTell(mob,L("You can't bond with @x1.",M.name(mob)));
			return false;
		}
		if(M.amFollowing() != mob)
		{
			commonTell(mob,L("@x1 doesn't seem willing to cooperate.",M.name(mob)));
			return false;
		}
		bonding=M;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		messedUp=!proficiencyCheck(mob,-bonding.phyStats().level()+(2*getXLEVELLevel(mob)),auto);
		final int duration=getDuration(35,mob,bonding.phyStats().level(),10);
		verb=L("bonding with @x1",M.name());
		final CMMsg msg=CMClass.getMsg(mob,null,this,getActivityMessageType(),L("<S-NAME> start(s) bonding with @x1.",M.name()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
