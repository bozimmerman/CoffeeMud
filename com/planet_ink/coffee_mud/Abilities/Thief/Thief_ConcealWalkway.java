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

public class Thief_ConcealWalkway extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_ConcealWalkway";
	}

	private final static String localizedName = CMLib.lang().L("Conceal Walkway");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ITEMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"WALKWAYCONCEAL","WALKCONCEAL","WCONCEAL","CONCEALWALKWAY"});
	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALTHY;
	}

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public int code=Integer.MIN_VALUE;

	@Override
	public int abilityCode()
	{
		if(code<0)
			code=CMath.s_int(text());
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code=newCode;
		super.miscText=""+newCode;
	}

	@Override
	public void affectPhyStats(Physical host, PhyStats stats)
	{
		super.affectPhyStats(host,stats);
		if(host instanceof Exit)
		{
			stats.setDisposition(stats.disposition()|PhyStats.IS_HIDDEN);
			stats.setLevel(stats.level()+abilityCode());
		}
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(canBeUninvoked() && (invoker()!=null) && (!msg.source().isMonster()) && (msg.source()!=invoker()) && (msg.sourceMinor()==CMMsg.TYP_ENTER))
		{
			final Physical affected=super.affected;
			if(affected != null)
			{
				if(!CMLib.flags().isInTheGame(invoker(), true))
				{
					unInvoke();
					affected.delEffect(this);
					affected.recoverPhyStats();
				}
				else
				{
					final Set<MOB> grp=invoker().getGroupMembers(new HashSet<MOB>());
					if(!grp.contains(msg.source()))
					{
						unInvoke();
						affected.delEffect(this);
						affected.recoverPhyStats();
					}
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell(L("Which way would you like to conceal?"));
			return false;
		}
		Environmental chkE=null;
		final String typed=CMParms.combine(commands,0);
		if(CMLib.directions().getGoodDirectionCode(typed)<0)
			chkE=mob.location().fetchFromMOBRoomItemExit(mob,null,typed,Wearable.FILTER_WORNONLY);
		else
			chkE=mob.location().getExitInDir(CMLib.directions().getGoodDirectionCode(typed));
		int direction=-1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			if(mob.location().getExitInDir(d)==chkE)
				direction=d;
		}
		if((!(chkE instanceof Exit))||(!CMLib.flags().canBeSeenBy(chkE,mob))||(direction<0))
		{
			mob.tell(L("You don't see any directions called '@x1' here.",typed));
			return false;
		}
		final Room R2=mob.location().getRoomInDir(direction);
		if((!CMath.bset(mob.location().domainType(),Room.INDOORS))
		&&(R2!=null)
		&&(!CMath.bset(R2.domainType(),Room.INDOORS)))
		{
			mob.tell(L("This only works on walkways into or within buildings."));
			return false;
		}
		final Exit X=(Exit)chkE;
		if((!auto)&&(X.phyStats().level()>(adjustedLevel(mob,asLevel)*2)))
		{
			mob.tell(L("You aren't good enough to conceal that direction."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,X,this,CMMsg.MSG_THIEF_ACT,L("<S-NAME> conceal(s) <T-NAME>."),CMMsg.MSG_THIEF_ACT,null,CMMsg.MSG_THIEF_ACT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Ability A=(Ability)super.copyOf();
				A.setInvoker(mob);
				A.setAbilityCode((adjustedLevel(mob,asLevel)*2)-X.phyStats().level());
				final Room R=mob.location();
				if((CMLib.law().doesOwnThisLand(mob,R))
				||((R2!=null)&&(CMLib.law().doesOwnThisLand(mob,R2))))
				{
					X.addNonUninvokableEffect(A);
					CMLib.database().DBUpdateExits(mob.location());
				}
				else
					A.startTickDown(mob,X,15*(adjustedLevel(mob,asLevel)));
				X.recoverPhyStats();
			}
		}
		else
			beneficialVisualFizzle(mob,X,L("<S-NAME> attempt(s) to conceal <T-NAME>, but obviously fail(s)."));
		return success;
	}
}
