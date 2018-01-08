package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2006-2018 Bo Zimmerman

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

public class Thief_MarkTrapped extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_MarkTrapped";
	}

	private final static String localizedName = CMLib.lang().L("Mark Trapped");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_EXITS|Ability.CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"MARKTRAPPED"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DETRAP;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public int code=0;
	public LinkedList<Physical> lastMarked = new LinkedList<Physical>();

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code=newCode;
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats stats)
	{
		super.affectPhyStats(host,stats);
		stats.addAmbiance("^Wtrapped^?");
	}

	public void marked(Physical P)
	{
		synchronized(lastMarked)
		{
			if(lastMarked.size()>=5)
			{
				final Physical P2=lastMarked.removeFirst();
				final Ability A=P2.fetchEffect(ID());
				if((A!=null)&&(A.invoker()==invoker()))
				{
					A.unInvoke();
					P2.delEffect(A);
					P2.recoverPhyStats();
				}
			}
			lastMarked.add(P);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("What item would you like to mark as trapped?"));
			return false;
		}
		final int dir=CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,0));
		Physical item=givenTarget;
		if((dir>=0)
		&&(item==null)
		&&(mob.location()!=null)
		&&(mob.location().getExitInDir(dir)!=null)
		&&(mob.location().getRoomInDir(dir)!=null))
			item=mob.location().getExitInDir(dir);
		if((item==null)
		&&(CMParms.combine(commands,0).equalsIgnoreCase("room")
			||CMParms.combine(commands,0).equalsIgnoreCase("here")))
			item=mob.location();
		if(item==null)
			item=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY,false,true);
		if(item==null)
			return false;

		if((!auto)&&(item instanceof MOB))
		{
			mob.tell(L("Umm.. you can't mark @x1 as trapped.",item.name()));
			return false;
		}

		if(item instanceof Item)
		{
			if((!auto)
			&&(item.phyStats().weight()>((adjustedLevel(mob,asLevel)*2)+(getXLEVELLevel(mob)*10))))
			{
				mob.tell(L("You aren't good enough to effectively mark anything that large."));
				return false;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg;
			final Ability A=item.fetchEffect(ID());
			if((A!=null)&&((givenTarget==null)||(auto)))
				msg=CMClass.getMsg(mob,item,null,CMMsg.MSG_THIEF_ACT,L("<S-NAME> remove(s) the mark on <T-NAME>."),CMMsg.MSG_THIEF_ACT,null,CMMsg.MSG_THIEF_ACT,null);
			else
				msg=CMClass.getMsg(mob,item,this,CMMsg.MSG_THIEF_ACT,L("<S-NAME> mark(s) <T-NAME> as trapped."),CMMsg.MSG_THIEF_ACT,null,CMMsg.MSG_THIEF_ACT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(A!=null)
				{
					if((givenTarget==null)||(auto))
					{
						A.unInvoke();
						item.delEffect(A);
					}
				}
				else
				{
					marked(item);
					this.beneficialAffect(mob, item, asLevel, 900); // approx an hour
				}
				item.recoverPhyStats();
			}
		}
		else
			beneficialVisualFizzle(mob,item,L("<S-NAME> attempt(s) to mark <T-NAME> as trapped, but fail(s)."));
		return success;
	}
}
